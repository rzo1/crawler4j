package edu.uci.ics.crawler4j.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.test.DummyWebURLFactory;
import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;

public class NetTest {
	
	/**
	 * edu.uci.ics.crawler4j.parser.Parser will force input "<html></html>" for binary content.
	 * No urls should be detected.
	 */
	@Test
	void extractUrlsTest() {
		final Net net = createNet(false);
		final Set<WebURL> result = net.extractUrls("<html></html>");
		Assertions.assertThat(result).isEmpty();
	}
	
	/**
	 * <p>
	 * edu.uci.ics.crawler4j.parser.Parser will force input "<html></html>" for binary content.
	 * <br>No urls should be detected.
	 * <br>As long as urls are detected for this input, workarounds for this output must stay in place in other code.
	 * </p><p>
	 * Workaround: place following snippet inside the shouldVisit()-method of a WebCrawler class.
	 * <pre>
	 * 		// edu.uci.ics.crawler4j.parser.Parser will force input "&lt;html>&lt;/html>" for binary content.
	 *		// UrlDetector "detects" this url when allowSingleLevelDomain is true.
	 *		if ("http://&lt;html>&lt;/html>".equals(url.getURL())) {
	 *			return false;
	 *		}
	 * </pre>
	 * <p>
	 */
	@Test
	void extractUrlsWithAllowSingleLevelDomainTest() {
		final Net net = createNet(true);
		final Set<WebURL> result = net.extractUrls("<html></html>");
		Assertions.assertThat(result)//
				.singleElement()//
				.extracting(t -> t.getURL()).isEqualTo("http://<html></html>");
	}
	
	private static Net createNet(final boolean allowSingleLevelDomain) {
		try {
			final CrawlConfig config = new CrawlConfig();
			config.setAllowSingleLevelDomain(allowSingleLevelDomain);
			final TLDList tldList = new TLDList(config);
			final DummyWebURLFactory factory = new DummyWebURLFactory();
			final Net net = new Net(config, tldList, factory);
			return net;
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
