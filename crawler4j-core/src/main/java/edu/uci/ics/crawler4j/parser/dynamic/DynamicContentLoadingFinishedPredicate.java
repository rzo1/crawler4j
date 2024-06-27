package edu.uci.ics.crawler4j.parser.dynamic;

import edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import org.openqa.selenium.WebDriver;

@FunctionalInterface
public interface DynamicContentLoadingFinishedPredicate<V> {

    /**
     * Given a {@link DynamicCrawlConfig}, return whether dynamic content finished loading for the given {@link Page}.
     * @param config the crawl configuration
     * @param page the page nbeing crawled
     * @param webDriver the WebDriver being used
     * @return true dynamic content finished loading, false otherwise
     */
    V test(final DynamicCrawlConfig config, final Page page, final WebDriver webDriver);
}
