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

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.Constants;
import edu.uci.ics.crawler4j.url.WebURLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Yasser Ganjisaffar
 */
public class Parser {

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final CrawlConfig config;

    private final HtmlParser htmlContentParser;

    private TikaLanguageDetector languageDetector;

    private final Net net;
    private final WebURLFactory factory;
    private final BasicURLNormalizer normalizer;
    
    public Parser(CrawlConfig config, BasicURLNormalizer normalizer, TLDList tldList, WebURLFactory webURLFactory) throws IOException {
        this(config, normalizer, new TikaHtmlParser(config, normalizer, tldList, webURLFactory), tldList, webURLFactory);
    }

    public Parser(CrawlConfig config, BasicURLNormalizer normalizer, HtmlParser htmlParser, TLDList tldList, WebURLFactory webURLFactory) throws IOException {
        this.config = config;
        this.htmlContentParser = htmlParser;
        this.net = new Net(config, tldList, webURLFactory);
        this.factory = webURLFactory;
        this.normalizer = normalizer;
        if(config.isLanguageDetection()) {
            this.languageDetector = new TikaLanguageDetector();
        }
    }

    public void parse(Page page, String contextURL) throws NotAllowedContentException, ParseException {
        if (Util.hasBinaryContent(page.getContentType())) { // BINARY
            BinaryParseData parseData = createBinaryParseData();
            if (config.isIncludeBinaryContentInCrawling()) {
                if (config.isProcessBinaryContentInCrawling()) {
                    try {
                        parseData.setBinaryContent(page.getContentData());
                    } catch (Exception e) {
                        if (config.isHaltOnError()) {
                            throw new ParseException(e);
                        } else {
                            logger.error("Error parsing file", e);
                        }
                    }
                } else {
                    parseData.setHtml(Constants.EMPTY_HTML_TAGS);
                }
                page.setParseData(parseData);
                if (parseData.getHtml() == null) {
                    throw new ParseException();
                }
                parseData.setOutgoingUrls(net.extractUrls(parseData.getHtml()));
            } else {
                throw new NotAllowedContentException();
            }
        } else if (Util.hasCssTextContent(page.getContentType())) { // text/css
            try {
                CssParseData parseData = createCssParseData();
                if (page.getContentCharset() == null) {
                    parseData.setTextContent(new String(page.getContentData(), StandardCharsets.UTF_8));
                } else {
                    parseData.setTextContent(
                            new String(page.getContentData(), page.getContentCharset()));
                }
                parseData.setOutgoingUrls(page.getWebURL());
                page.setParseData(parseData);
            } catch (Exception e) {
                logger.error("{}, while parsing css: {}", e.getMessage(), page.getWebURL().getURL());
                throw new ParseException();
            }
        } else if (Util.hasPlainTextContent(page.getContentType())) { // plain Text
            try {
                TextParseData parseData = createTextParseData();
                if (page.getContentCharset() == null) {
                    parseData.setTextContent(new String(page.getContentData(), StandardCharsets.UTF_8));
                } else {
                    parseData.setTextContent(
                            new String(page.getContentData(), page.getContentCharset()));
                }
                parseData.setOutgoingUrls(net.extractUrls(parseData.getTextContent()));
                page.setParseData(parseData);
            } catch (Exception e) {
                logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
                throw new ParseException(e);
            }
        } else { // isHTML

            HtmlParseData parsedData = createHtmlParseData(page, contextURL);

            if (page.getContentCharset() == null) {
                page.setContentCharset(parsedData.getContentCharset());
            }

            if(config.isLanguageDetection()) {
                // Please note that identifying language takes less than 10 milliseconds
                page.setLanguage(languageDetector.detect(parsedData.getText()));
            } else {
                page.setLanguage("");
            }

            page.setParseData(parsedData);
        }
    }

		/**
		 * Open for extension
		 */
		protected BinaryParseData createBinaryParseData() {
			return new BinaryParseData();
		}
		
		/**
		 * Open for extension
		 */
		protected CssParseData createCssParseData() {
			return new CssParseData(getFactory(), getNormalizer());
		}
		
		/**
		 * Open for extension
		 */
		protected TextParseData createTextParseData() {
			return new TextParseData();
		}
		
		/**
		 * Open for extension
		 */
		protected HtmlParseData createHtmlParseData(final Page page, final String contextURL)
				throws ParseException
		{
			return getHtmlContentParser().parse(page, contextURL);
		}
		
		protected WebURLFactory getFactory() {
			return factory;
		}
		
		protected BasicURLNormalizer getNormalizer() {
			return normalizer;
		}
		
		protected HtmlParser getHtmlContentParser() {
			return htmlContentParser;
		}
}
