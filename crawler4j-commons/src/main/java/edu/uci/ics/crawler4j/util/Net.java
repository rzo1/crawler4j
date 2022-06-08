/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-commons
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
package edu.uci.ics.crawler4j.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;

import edu.uci.ics.crawler4j.Constants;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;

/**
 * Created by Avi Hayun on 9/22/2014.
 * Net related Utils
 */
public class Net {

    private final Function<Url, WebURL> urlMapper;
    private final CrawlConfig config;

    public Net(CrawlConfig config, TLDList tldList, WebURLFactory factory) {
        this.config = config;
        this.urlMapper = url -> {
            WebURL webUrl = factory.newWebUrl();
            webUrl.setTldList(tldList);
            webUrl.setURL(url.getFullUrl());
            return webUrl;
        };
    }

    public Set<WebURL> extractUrls(String input) {
        if (input == null || Constants.EMPTY_HTML_TAGS.equalsIgnoreCase(input)) {
            return Collections.emptySet();
        } else {
            UrlDetector detector = new UrlDetector(input, getOptions());
            List<Url> urls = detector.detect();
            return urls.stream().map(urlMapper).collect(Collectors.toSet());
        }
    }

    private UrlDetectorOptions getOptions() {
        if (config.isAllowSingleLevelDomain()) {
            return UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN;
        } else {
            return UrlDetectorOptions.Default;
        }
    }

}
