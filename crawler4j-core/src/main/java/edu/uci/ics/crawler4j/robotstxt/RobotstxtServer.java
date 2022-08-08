/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
 * %%
 * Copyright (C) 2010 - 2021 crawler4j-fork
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
package edu.uci.ics.crawler4j.robotstxt;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.BaseRobotsParser;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import edu.uci.ics.crawler4j.util.Util;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RobotstxtServer {

    private static final Logger logger = LoggerFactory.getLogger(RobotstxtServer.class);

    protected BaseRobotsParser ruleParser;

    protected WebURLFactory factory;

    protected RobotstxtConfig config;

    protected CrawlConfig crawlConfig;

    protected final Map<String, RobotRules> cache = new HashMap<>();

    protected PageFetcher pageFetcher;

    public RobotstxtServer(RobotstxtConfig config, PageFetcher pageFetcher, WebURLFactory factory) {
        this.config = config;
        this.pageFetcher = pageFetcher;
        this.factory = factory;
        this.ruleParser = new SimpleRobotRulesParser();
    }

    private static String getHost(URL url) {
        return url.getHost().toLowerCase(Locale.ROOT);
    }

    /**
     * Please note that in the case of a bad URL, TRUE will be returned
     *
     * @throws InterruptedException
     */
    public boolean allows(WebURL webURL) throws InterruptedException {
        return allows(webURL, false);
    }

    /**
     * Please note that in the case of a bad URL, TRUE will be returned
     *
     * @throws InterruptedException
     */
    public boolean allows(WebURL webURL, boolean isSeed) throws InterruptedException {
        if (!config.isEnabled()) {
            return true;
        }

        if(config.isSkipCheckForSeeds() && isSeed) {
            return true;
        }

        try {
            URL url = new URL(webURL.getURL());
            String host = getHost(url);

            RobotRules rule = cache.get(host);

            if (rule != null && rule.needsRefetch()) {
                synchronized (cache) {
                    cache.remove(host);
                    rule = null;
                }
            }
            if (rule == null) {
                rule = fetchDirectives(url);
            }
            return rule.isAllowed(webURL.getURL());
        } catch (MalformedURLException e) {
            logger.error("Bad URL in Robots.txt: " + webURL.getURL(), e);
        } catch (URISyntaxException e) {
            logger.error("Bad URL to Robots.txt: " + webURL.getURL(), e);
        }

        logger.warn("RobotstxtServer: default: allow for {}", webURL.getURL());
        return true;
    }

    private RobotRules fetchDirectives(URL url) throws InterruptedException, URISyntaxException {
        WebURL robotsTxtUrl = factory.newWebUrl();
        String host = getHost(url);
        String port = ((url.getPort() == url.getDefaultPort()) || (url.getPort() == -1)) ? "" :
                (":" + url.getPort());
        String proto = url.getProtocol();
        robotsTxtUrl.setURL(proto + "://" + host + port + "/robots.txt");

        BaseRobotRules directives = null;
        PageFetchResult fetchResult = null;
        try {
            for (int redir = 0; redir < 3; ++redir) {
                fetchResult = pageFetcher.fetchPage(robotsTxtUrl);
                int status = fetchResult.getStatusCode();
                // Follow redirects up to 3 levels
                if ((status == HttpStatus.SC_MULTIPLE_CHOICES ||
                        status == HttpStatus.SC_MOVED_PERMANENTLY ||
                        status == HttpStatus.SC_MOVED_TEMPORARILY ||
                        status == HttpStatus.SC_SEE_OTHER ||
                        status == HttpStatus.SC_TEMPORARY_REDIRECT ||
                        status == HttpStatus.SC_PERMANENT_REDIRECT) &&
                        fetchResult.getMovedToUrl() != null) {
                    robotsTxtUrl.setURL(fetchResult.getMovedToUrl());
                    fetchResult.discardContentIfNotConsumed();
                } else {
                    // Done on all other occasions
                    break;
                }
            }

            if (fetchResult.getStatusCode() == HttpStatus.SC_OK) {
                Page page = new Page(robotsTxtUrl);
                // Most recent answer on robots.txt max size is
                // https://developers.google.com/search/reference/robots_txt
                fetchResult.fetchContent(page, 500 * 1024);
                if (Util.hasPlainTextContent(page.getContentType())) {
                    directives = ruleParser.parseContent(robotsTxtUrl.getURL(), page.getContentData(),
                            "text/plain", config.getUserAgentName());
                } else {
                    logger.warn(
                            "Can't read this robots.txt: {}  as it is not written in plain text, " +
                                    "contentType: {}", robotsTxtUrl.getURL(), page.getContentType());
                }
            } else {
                logger.debug("Can't read this robots.txt: {}  as it's status code is {}",
                        robotsTxtUrl.getURL(), fetchResult.getStatusCode());
            }
        } catch (SocketException | UnknownHostException | SocketTimeoutException |
                NoHttpResponseException se) {
            // No logging here, as it just means that robots.txt doesn't exist on this server
            // which is perfectly ok
            logger.trace("robots.txt probably does not exist.", se);
        } catch (PageBiggerThanMaxSizeException pbtms) {
            logger.error("Error occurred while fetching (robots) url: {}, {}",
                    robotsTxtUrl.getURL(), pbtms.getMessage());
        } catch (IOException e) {
            logger.error("Error occurred while fetching (robots) url: " + robotsTxtUrl.getURL(), e);
        } catch (InterruptedException | RuntimeException e) {
            if (crawlConfig.isHaltOnError()) {
                throw e;
            } else {
                logger.error("Error occurred while fetching (robots) url: " + robotsTxtUrl.getURL(), e);
            }
        } finally {
            if (fetchResult != null) {
                fetchResult.discardContentIfNotConsumed();
            }
        }

        if (directives == null) {
            // We still need to have this object to keep track of the time we fetched it
            directives = new SimpleRobotRules(SimpleRobotRules.RobotRulesMode.ALLOW_ALL);
        }

        RobotRules robotRules = new RobotRules(directives);

        synchronized (cache) {
            if (cache.size() == config.getCacheSize()) {
                String minHost = null;
                long minAccessTime = Long.MAX_VALUE;
                for (Map.Entry<String, RobotRules> entry : cache.entrySet()) {
                    long entryAccessTime = entry.getValue().getTimeLastAccessed();
                    if (entryAccessTime < minAccessTime) {
                        minAccessTime = entryAccessTime;
                        minHost = entry.getKey();
                    }
                }
                cache.remove(minHost);
            }
            cache.put(host, robotRules);
        }
        return robotRules;
    }

    public void setCrawlConfig(CrawlConfig crawlConfig) {
        this.crawlConfig = crawlConfig;
    }
}
