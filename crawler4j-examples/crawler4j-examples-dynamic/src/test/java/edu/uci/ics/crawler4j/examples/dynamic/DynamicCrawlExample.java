/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-examples-base
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
package edu.uci.ics.crawler4j.examples.dynamic;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig;
import edu.uci.ics.crawler4j.crawler.dynamic.DynamicCrawlerHelper;
import edu.uci.ics.crawler4j.crawler.dynamic.DynamicWebCrawler;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicCrawlExample {

    private static final Logger log = LoggerFactory.getLogger(DynamicCrawlExample.class);

    private static class ExampleDynamicDriver extends DynamicWebCrawler {

        private final String urlPrefix;

        public ExampleDynamicDriver(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        @Override
        public boolean shouldVisit(Page referringPage, WebURL url) {
            return super.shouldVisit(referringPage, url) && url.getURL().startsWith(urlPrefix);
        }

        @Override
        public void visit(Page page) {
            log.info("Visiting {}", page.getWebURL().getURL());
        }
    }

    public static void main(String[] args) throws Exception {
        DynamicCrawlConfig config = new DynamicCrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder("/tmp/crawler4j/");

        // Be polite: Make sure that we don't send more than 2 request per second (500 milliseconds between requests).
        // Otherwise, it may overload the target servers.
        config.setPolitenessDelay(500);

        // You can set the maximum crawl depth here. The default value is -1 for unlimited depth.
        config.setMaxDepthOfCrawling(2);

        // You can set the maximum number of pages to crawl. The default value is -1 for unlimited number of pages.
        config.setMaxPagesToFetch(10);

        // Should binary data should also be crawled? example: the contents of pdf, or the metadata of images etc
        config.setIncludeBinaryContentInCrawling(false);

        // Do you need to set a proxy? If so, you can use:
        // config.setProxyHost("proxyserver.example.com");
        // config.setProxyPort(8080);

        // If your proxy also needs authentication:
        // config.setProxyUsername(username); config.getProxyPassword(password);

        // This config parameter can be used to set your crawl to be resumable
        // (meaning that you can resume the crawl from a previously
        // interrupted/crashed crawl). Note: if you enable resuming feature and
        // want to start a fresh crawl, you need to delete the contents of
        // rootFolder manually.
        config.setResumableCrawling(false);

        // Set this to true if you want crawling to stop whenever an unexpected error
        // occurs. You'll probably want this set to true when you first start testing
        // your crawler, and then set to false once you're ready to let the crawler run
        // for a long time.
        config.setHaltOnError(true);

        // Set the headless browser configuration
        // We will be using a Firefox driver
        config.setWebDriverType(DynamicCrawlConfig.WebDriverType.firefox);

        // Set the user agent to an actual Firefox agent
        config.setUserAgentString("Mozilla/5.0 (X11; Linux i686; rv:109.0) Gecko/20100101 Firefox/116.0");

        // You will need to download an actual Gecko driver from https://github.com/mozilla/geckodriver/releases
        //config.setWebDriverPath("/tmp/crawler4j/driver/geckodriver-v0.33.0-linux64/geckodriver");
        config.setWebDriverPath("/tmp/crawler4j/geckodriver-v0.33.0-linux64/geckodriver");

        // We will wait at most 5 seconds for dynamic content loading
        config.setMaxWaitForDynamicContentInSeconds(5);

        // Instantiate the controller for this crawl.
        CrawlController controller = DynamicCrawlerHelper.crawlControllerFactory(
                config,
                // We will wait until the body tag is loaded
                (conf, page) -> By.cssSelector("body"),
                new RobotstxtConfig(),
                new SleepycatFrontierConfiguration(config));

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed("https://github.com/mozilla/geckodriver/releases");

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(() -> new ExampleDynamicDriver("https://github.com/mozilla/geckodriver/"), 2);
    }

}
