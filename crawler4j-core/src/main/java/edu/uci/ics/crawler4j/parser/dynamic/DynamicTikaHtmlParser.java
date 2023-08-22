package edu.uci.ics.crawler4j.parser.dynamic;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TikaHtmlParser;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.function.BiFunction;

/**
 * Overrides {@link TikaHtmlParser} so that the page content is fetched using Selenium's {@link WebDriver}.
 * A programmatic wait for dynamic content loading is performed prior to content fetching, which content we should wait
 * for is defined by {@link DynamicTikaHtmlParser#waitSelectorSupplier}.
 *
 * Since {@link WebDriver} is not thread safe, we use a {@link ThreadLocal} wrapper. The {@link WebDriver} for each
 * thread should be provided through the {@link DynamicTikaHtmlParser#setWebDriver(WebDriver)} method.
 */
public class DynamicTikaHtmlParser extends TikaHtmlParser {

    /**
     * Holds a {@link WebDriver} instance per crawler thread.
     */
    private final ThreadLocal<WebDriver> webDriver;

    /**
     * A function that provides a selector clause for a {@link Page} given a {@link DynamicCrawlConfig}.
     * This selector will be used to wait for dynamic content loading.
     */
    private final BiFunction<DynamicCrawlConfig, Page, By> waitSelectorSupplier;

    /**
     * The maximum time in seconds to for dynamic content loading.
     */
    private int maxWaitForDynamicContentInSeconds;

    public DynamicTikaHtmlParser(
            DynamicCrawlConfig config,
            BasicURLNormalizer normalizer,
            TLDList tldList,
            WebURLFactory webURLFactory,
            BiFunction<DynamicCrawlConfig, Page, By> waitSelectorSupplier) {
        super(config, normalizer, tldList, webURLFactory);
        this.webDriver = new ThreadLocal<>();
        this.waitSelectorSupplier = waitSelectorSupplier;
        this.maxWaitForDynamicContentInSeconds = config.getMaxWaitForDynamicContentInSeconds();
    }

    /**
     * Sets the {@link WebDriver} instance for a given crawler thread.
     * @param webDriver
     */
    public void setWebDriver(WebDriver webDriver) {
        this.webDriver.set(webDriver);
    }

    public HtmlParseData parse(Page page, String contextURL) throws ParseException {
        try {
            page.setContentData(fetchPageSource(page));
        } catch (PageBiggerThanMaxSizeException e) {
            throw new ParseException(e);
        }
        return super.parse(page, contextURL);
    }

    /**
     * Fetches the page source using the {@link WebDriver} instance for the crawler thread,
     * after waiting for dynamic content loading according to the configured wait time and selector.
     * @param page the page being crawled
     * @return the page source as a byte array
     */
    protected byte[] fetchPageSource(Page page) throws PageBiggerThanMaxSizeException {
        String url = page.getWebURL().getURL();
        webDriver.get().navigate().to(url);

        By selector = waitSelectorSupplier.apply((DynamicCrawlConfig) getCrawlConfig(), page);
        try {
            new WebDriverWait(
                    webDriver.get(),
                    Duration.ofSeconds(maxWaitForDynamicContentInSeconds))
                    .until(d -> d.findElement(selector));
        } catch (final TimeoutException e) {
            logger.warn("Wait on {} timed out for {}", selector, url);
        }

        byte[] pageSource = webDriver.get().getPageSource().getBytes(Charset.forName(page.getContentCharset()));
        int contentSize = pageSource.length;
        if (contentSize > getCrawlConfig().getMaxDownloadSize()) {
            throw new PageBiggerThanMaxSizeException(contentSize);
        }

        return pageSource;
    }

}
