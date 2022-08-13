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
package edu.uci.ics.crawler4j.test;

import java.io.IOException;
import java.io.UncheckedIOException;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public final class Crawler4jTestUtils {
	private Crawler4jTestUtils() {
	}
	
	
	public static WebURLFactory newWebURLFactory() {
		return new SimpleWebURLFactory();
	}
	
	public static BasicURLNormalizer newNormalizer() {
		return BasicURLNormalizer.newBuilder()//
				.idnNormalization(BasicURLNormalizer.IdnNormalization.NONE)//
				.build();
	}
	
	public static WebURL newWebURL(final String url) {
		final WebURL result = new SimpleWebURL();
		result.setTldList(newTLDList());
		result.setURL(url);
		return result;
	}
	
	public static TLDList newTLDList() {
		// Don't allow passing in a config, as it is only used to force going online.
		final CrawlConfig config = new CrawlConfig();
		try {
			return new TLDList(config);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public static CssParseData newCssParseData() {
		return newCssParseData(false);
	}
	
	public static CssParseData newCssParseData(final boolean haltOnError) {
		return new CssParseData(newWebURLFactory(), newNormalizer(), haltOnError);
	}
}
