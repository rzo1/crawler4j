/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2021 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.charset.Charset;
import java.time.Duration;

/**
 * Overrides {@link TikaHtmlParser} so that the page content is fetched using Selenium's {@link WebDriver}.
 * A programmatic wait for dynamic content loading is performed prior to content fetching, which content we should wait
 * for is defined by {@link DynamicTikaHtmlParser#waitPredicate}.
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
     * A function that provides a predicate on whether dynamic content is loaded.
     */
    private final DynamicContentLoadingFinishedPredicate waitPredicate;

    /**
     * The maximum time in seconds to for dynamic content loading.
     */
    private int maxWaitForDynamicContentInSeconds;

    public DynamicTikaHtmlParser(
            DynamicCrawlConfig config,
            BasicURLNormalizer normalizer,
            TLDList tldList,
            WebURLFactory webURLFactory,
            DynamicContentLoadingFinishedPredicate waitPredicate) {
        super(config, normalizer, tldList, webURLFactory);
        this.webDriver = new ThreadLocal<>();
        this.waitPredicate = waitPredicate;
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

        try {
            new WebDriverWait(
                    webDriver.get(),
                    Duration.ofSeconds(maxWaitForDynamicContentInSeconds))
                    .until(d -> waitPredicate.test(
                            (DynamicCrawlConfig) getCrawlConfig(),
                            page,
                            d));
        } catch (final TimeoutException e) {
            logger.warn("Wait for dynamic content loading timed out for {}", url);
        }

        byte[] pageSource = webDriver.get().getPageSource().getBytes(Charset.forName(page.getContentCharset()));
        int contentSize = pageSource.length;
        if (contentSize > getCrawlConfig().getMaxDownloadSize()) {
            throw new PageBiggerThanMaxSizeException(contentSize);
        }

        return pageSource;
    }

}
