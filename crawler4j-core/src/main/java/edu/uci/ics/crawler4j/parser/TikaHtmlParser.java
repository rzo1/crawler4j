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
package edu.uci.ics.crawler4j.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.parser.html.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

public class TikaHtmlParser implements edu.uci.ics.crawler4j.parser.HtmlParser {
    protected static final Logger logger = LoggerFactory.getLogger(TikaHtmlParser.class);

    private final CrawlConfig config;
    private final TLDList tldList;

    private final HtmlParser htmlParser;
    private final ParseContext parseContext;
    private final WebURLFactory factory;

    public TikaHtmlParser(CrawlConfig config, TLDList tldList, WebURLFactory webURLFactory) throws InstantiationException, IllegalAccessException {
        this.config = config;
        this.tldList = tldList;

        htmlParser = new HtmlParser();
        parseContext = new ParseContext();
        parseContext.set(HtmlMapper.class, AllTagMapper.class.newInstance());
        this.factory = webURLFactory;
    }

    public HtmlParseData parse(Page page, String contextURL) throws ParseException {
        HtmlParseData parsedData = new HtmlParseData();

        HtmlContentHandler contentHandler = new HtmlContentHandler();
        Metadata metadata = new Metadata();

        if (page.getContentType() != null) {
            metadata.add(Metadata.CONTENT_TYPE, page.getContentType());
        }

        try (InputStream inputStream = new ByteArrayInputStream(page.getContentData())) {
            htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
        } catch (Exception e) {
            logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
            throw new ParseException("could not parse [" + page.getWebURL().getURL() + "]", e);
        }

        String contentCharset = chooseEncoding(page, metadata);
        parsedData.setContentCharset(contentCharset);

        parsedData.setText(contentHandler.getBodyText().trim());
        parsedData.setTitle(metadata.get(DublinCore.TITLE));
        parsedData.setMetaTags(contentHandler.getMetaTags());

        try {
            Set<WebURL> outgoingUrls = getOutgoingUrls(contextURL, contentHandler, contentCharset);
            parsedData.setOutgoingUrls(outgoingUrls);

            if (page.getContentCharset() == null) {
                parsedData.setHtml(new String(page.getContentData()));
            } else {
                parsedData.setHtml(new String(page.getContentData(), page.getContentCharset()));
            }

            return parsedData;
        } catch (UnsupportedEncodingException e) {
            logger.error("error parsing the html: " + page.getWebURL().getURL(), e);
            throw new ParseException("could not parse [" + page.getWebURL().getURL() + "]", e);
        }

    }

    private Set<WebURL> getOutgoingUrls(String contextURL, HtmlContentHandler contentHandler, String contentCharset)
            throws UnsupportedEncodingException {
        Set<WebURL> outgoingUrls = new HashSet<>();

        String baseURL = contentHandler.getBaseUrl();
        if (baseURL != null) {
            contextURL = baseURL;
        }

        int urlCount = 0;
        for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {

            String href = urlAnchorPair.getHref();
            if ((href == null) || href.trim().isEmpty()) {
                continue;
            }

            String hrefLoweredCase = href.trim().toLowerCase();
            if (!hrefLoweredCase.contains("javascript:") &&
                    !hrefLoweredCase.contains("mailto:") && !hrefLoweredCase.contains("@")) {
                // Prefer page's content charset to encode href url
                Charset hrefCharset = ((contentCharset == null) || contentCharset.isEmpty()) ?
                        StandardCharsets.UTF_8 : Charset.forName(contentCharset);
                String url = URLCanonicalizer.getCanonicalURL(href, contextURL, hrefCharset);
                if (url != null) {
                    WebURL webURL = factory.newWebUrl();
                    webURL.setTldList(tldList);
                    webURL.setURL(url);
                    webURL.setTag(urlAnchorPair.getTag());
                    webURL.setAnchor(urlAnchorPair.getAnchor());
                    webURL.setAttributes(urlAnchorPair.getAttributes());
                    outgoingUrls.add(webURL);
                    urlCount++;
                    if (urlCount > config.getMaxOutgoingLinksToFollow()) {
                        break;
                    }
                }
            }
        }
        return outgoingUrls;
    }

    private String chooseEncoding(Page page, Metadata metadata) {
        String pageCharset = page.getContentCharset();
        if (pageCharset == null || pageCharset.isEmpty()) {
            return metadata.get("Content-Encoding");
        }
        return pageCharset;
    }
}
