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
import edu.uci.ics.crawler4j.crawler.dynamic.DynamicWebCrawler;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * Extends {@link Parser} to use {@link DynamicTikaHtmlParser} and provides a setter method so that
 * {@link edu.uci.ics.crawler4j.crawler.WebCrawler} can register a {@link WebDriver} instance ofr itself.
 */
public class DynamicParser extends Parser {

    public DynamicParser(
            DynamicCrawlConfig config,
            BasicURLNormalizer normalizer,
            TLDList tldList,
            WebURLFactory webURLFactory,
            BiFunction<DynamicCrawlConfig, Page, By> waitSelectorSupplier) throws IOException {
        super(
                config,
                normalizer,
                new DynamicTikaHtmlParser(config, normalizer, tldList, webURLFactory, waitSelectorSupplier),
                tldList,
                webURLFactory);
    }

    /**
     * Sets the {@link WebDriver} instance for a given crawler thread (called by {@link DynamicWebCrawler#onStart()});
     * @param webDriver
     */
    public void setWebDriver(WebDriver webDriver) {
        ((DynamicTikaHtmlParser) getHtmlContentParser()).setWebDriver(webDriver);
    }

}
