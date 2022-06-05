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
		return new CssParseData(newWebURLFactory(), newNormalizer());
	}
}
