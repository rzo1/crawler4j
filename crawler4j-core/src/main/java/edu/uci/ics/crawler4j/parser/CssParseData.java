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
/*
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

package edu.uci.ics.crawler4j.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.url.UrlResolver;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.apache.commons.lang3.StringUtils;

public class CssParseData extends TextParseData {

    private final WebURLFactory factory;
    private final BasicURLNormalizer normalizer;

    public CssParseData(WebURLFactory webURLFactory, BasicURLNormalizer normalizer) {
        this.factory = webURLFactory;
        this.normalizer = normalizer;
    }

    private Set<WebURL> parseOutgoingUrls(WebURL referringPage) {

        Set<String> extractedUrls = extractUrlInCssText(this.getTextContent());

        final String pageUrl = referringPage.getURL();

        Set<WebURL> outgoingUrls = new HashSet<>();
        for (String url : extractedUrls) {

        	final String seedUrl = toSeedUrl(pageUrl, url);

            WebURL webURL = factory.newWebUrl();
            webURL.setURL(seedUrl);
            outgoingUrls.add(webURL);

        }
        return outgoingUrls;
    }
    
    private String toSeedUrl(final String referenceAbsoluteUrl, final String url) {
  		// Normalization is needed, because the String will be input for URI.create(...).
    	return normalizer.filter(UrlResolver.resolveUrl((referenceAbsoluteUrl == null) ? "" : referenceAbsoluteUrl, url));
  	}

    public void setOutgoingUrls(WebURL referringPage){

        Set<WebURL> outgoingUrls = parseOutgoingUrls(referringPage);
        this.setOutgoingUrls(outgoingUrls);
    }

    private static Set<String> extractUrlInCssText(String input) {

        Set<String> extractedUrls = new HashSet<>();
        if (input == null || input.isEmpty()) {
            return extractedUrls;
        }

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String url = matcher.group(1);
            if (url == null) {
                url = matcher.group(2);
            }
            if (url == null) {
                url = matcher.group(3);
            }
            if (url == null || StringUtils.startsWithAny(url, "data:", "'data:", "\"data:")) { // test for incomplete matches as well
                continue;
            }
            extractedUrls.add(url);
        }


        return extractedUrls;
    }

    private static final Pattern pattern = initializePattern();

    private static Pattern initializePattern() {
        return Pattern.compile("url\\(\\s*'([^\\)]+)'\\s*\\)" +     // url('...')
                "|url\\(\\s*\"([^\\)]+)\"\\s*\\)" +                  // url("...")
                "|url\\(\\s*([^\\)]+)\\s*\\)" +                       // url(...)
                "|\\/\\*(\\*(?!\\/)|[^*])*\\*\\/");                 // ignore comments
    }

}
