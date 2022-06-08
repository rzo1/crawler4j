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

        final String pagePath = referringPage.getPath();
        final String pageUrl = referringPage.getURL();

        Set<WebURL> outgoingUrls = new HashSet<>();
        for (String url : extractedUrls) {

            String relative = getLinkRelativeTo(pagePath, url);
            String absolute = getAbsoluteUrlFrom(normalizer.filter(pageUrl), relative);

            WebURL webURL = factory.newWebUrl();
            webURL.setURL(absolute);
            outgoingUrls.add(webURL);

        }
        return outgoingUrls;
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

    private static String getAbsoluteUrlFrom(String pageUrl, String linkPath) {

        String domainUrl = getFullDomainFromUrl(pageUrl);
        if (linkPath.startsWith("/")) {
            return domainUrl + linkPath;
        }
        return domainUrl + "/" + linkPath;
    }

    private static String getLinkRelativeTo(String pagePath, String linkUrl) {

        if (linkUrl.startsWith("/") && !linkUrl.startsWith("//")) {
            return linkUrl;
        }

        if (linkUrl.startsWith("//")) {
            linkUrl = "http" + linkUrl;
        }

        if (linkUrl.startsWith("http")) {
            return getPathFromUrl(linkUrl);
        }

        if (linkUrl.startsWith("../")) {

            String[] parts = pagePath.split("/");

            int pos = linkUrl.lastIndexOf("../") + 3;
            int parents = pos / 3;
            long diff = parts.length - parents - 1;

            StringBuilder absolute = new StringBuilder();
            for (int i = 0; i < diff; i++) {
                String dir = parts[i];
                if (!dir.isEmpty()) {
                    absolute.append("/").append(dir);
                }
            }
            return absolute + "/" + linkUrl.substring(pos);
        }

        String root = getDirsFromUrl(pagePath);
        return root + linkUrl;
    }

    private static String getDirsFromUrl(String urlPath) {
        int pos = urlPath.lastIndexOf("/") + 1;
        return urlPath.substring(0, pos);
    }

    private static String getPathFromUrl(String url) {
        int pos1 = url.indexOf("//") + 2;              // http://subdomain.domain:port/dir/page.ext
        int pos2 = url.indexOf("/", pos1);
        return  url.substring(pos2);
    }

    private static String getFullDomainFromUrl(String url) {
        int pos1 = url.indexOf("//") + 2;              // http://subdomain.domain:port/dir/page.ext
        int pos2 = url.indexOf("/", pos1);
        return url.substring(0, pos2);
    }

}
