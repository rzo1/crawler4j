package edu.uci.ics.crawler4j.crawler.dynamic;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.parser.dynamic.DynamicParser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.TLDList;
import org.openqa.selenium.By;

import java.util.function.BiFunction;

/**
 * Helper class that provides factory methods for a {@link CrawlController} set up for headless browser crawling.
 */
public class DynamicCrawlerHelper {

    /**
     * Sets up a ready to use {@link CrawlController} with a default URL normalizer
     * @param config the {@link DynamicCrawlConfig}
     * @param waitSelectorSupplier provides a selector clause for a {@link Page} given a {@link DynamicCrawlConfig} that
     *                             will be used to wait for dynamic content loading.
     * @param robotstxtConfig the robots.txt config to use
     * @param frontierConfiguration the frontier implementation to use
     * @return a ready to use {@link CrawlController}
     * @throws Exception
     */
    public static CrawlController crawlControllerFactory(
            DynamicCrawlConfig config,
            BiFunction<DynamicCrawlConfig, Page, By> waitSelectorSupplier,
            RobotstxtConfig robotstxtConfig,
            FrontierConfiguration frontierConfiguration) throws Exception {
        return crawlControllerFactory(config, waitSelectorSupplier, robotstxtConfig, frontierConfiguration,
                BasicURLNormalizer.newBuilder().idnNormalization(BasicURLNormalizer.IdnNormalization.NONE).build());
    }

    /**
     * Sets up a ready to use {@link CrawlController} with a specific URL normalizer
     * @param config the {@link DynamicCrawlConfig}
     * @param waitSelectorSupplier provides a selector clause for a {@link Page} given a {@link DynamicCrawlConfig} that
     *                             will be used to wait for dynamic content loading.
     * @param robotstxtConfig the robots.txt config to use
     * @param frontierConfiguration the frontier implementation to use
     * @param normalizer the URL normalizer to use
     * @return a ready to use {@link CrawlController}
     * @throws Exception
     */
    public static CrawlController crawlControllerFactory(
            DynamicCrawlConfig config,
            BiFunction<DynamicCrawlConfig, Page, By> waitSelectorSupplier,
            RobotstxtConfig robotstxtConfig,
            FrontierConfiguration frontierConfiguration,
            BasicURLNormalizer normalizer) throws Exception {
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtServer robotstxtServer = new RobotstxtServer(
                robotstxtConfig, pageFetcher, frontierConfiguration.getWebURLFactory());
        TLDList tldList = new TLDList(config);
        DynamicParser parser = new DynamicParser(
                config, normalizer, tldList, frontierConfiguration.getWebURLFactory(), waitSelectorSupplier);
        return new CrawlController(config, normalizer, pageFetcher, parser, robotstxtServer, tldList, frontierConfiguration);
    }

}
