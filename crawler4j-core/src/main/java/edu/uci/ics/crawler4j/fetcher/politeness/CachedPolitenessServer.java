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
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.fetcher.politeness;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.WebURL;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CachedPolitenessServer implements edu.uci.ics.crawler4j.PolitenessServer {

    private static final Logger logger = LoggerFactory.getLogger(CachedPolitenessServer.class);

    protected Cache<String, Long> seenHosts;
    protected CrawlConfig config;
    private final Object mutex = new Object();

    public CachedPolitenessServer(CrawlConfig config) {
        this.seenHosts = new Cache2kBuilder<String, Long>() {
        }
                .expireAfterWrite(config.getPolitenessDelay(), TimeUnit.MILLISECONDS)
                .build();
        this.config = config;
    }

    // We should organize the urls in queues per domain and then know when we can crawl a domain next.
    // Workers would then only pick urls from ready domains. That requires major changes in architecture.
    public long applyPoliteness(WebURL url) {
        synchronized (mutex) {
            long politenessDelay = NO_POLITENESS_APPLIED;
            final String host = url.getDomain();

            if (host != null) {
                final Date now = new Date();
                final long lastFetchTime = seenHosts.computeIfAbsent(host, h -> (long) NO_POLITENESS_APPLIED);

                if (lastFetchTime != NO_POLITENESS_APPLIED) {
                    final long diff = (now.getTime() - lastFetchTime);

                    if (diff < config.getPolitenessDelay()) {
                        politenessDelay = config.getPolitenessDelay() - diff;
                        logger.debug("Applying politeness delay of {} ms for host {}", politenessDelay, host);
                    }
                }
                seenHosts.put(host, now.getTime());
            }

            return politenessDelay;
        }
    }

    public void forceCleanUp() {
        this.seenHosts.clear();
    }

    public long getSize() {
        return seenHosts.keys().size();
    }
}
