/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2022 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
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
package edu.uci.ics.crawler4j.fetcher.politeness;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Date;

/**
 * This class handles the politeness logic similar to earlier versions of crawler4j, i.e. apply a delay to every request.
 */
public class SimplePolitenessServer implements edu.uci.ics.crawler4j.PolitenessServer {

    protected CrawlConfig config;
    private final Object mutex = new Object();
    protected long lastFetchTime = 0;

    public SimplePolitenessServer(CrawlConfig config) {
        this.config = config;
    }

    @Override
    public long applyPoliteness(WebURL url) {
        synchronized (mutex) {
            long now = new Date().getTime();
            if (now - this.lastFetchTime < config.getPolitenessDelay()) {
                return config.getPolitenessDelay() - (now - this.lastFetchTime);
            }
            this.lastFetchTime = new Date().getTime();

            return NO_POLITENESS_APPLIED;
        }
    }
}
