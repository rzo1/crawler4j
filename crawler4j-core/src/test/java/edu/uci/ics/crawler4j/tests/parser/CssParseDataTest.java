package edu.uci.ics.crawler4j.tests.parser;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

public class CssParseDataTest {
	
	/**
	 * REMARK: NO URLS SHOULD BE FOUND.
	 * 
	 * The problem is that the pattern inside CssParseData will match parts of data urls instead of the complete url.
	 * More specifically, following will get matched
	 * by the expression for "url(...)" instead of
	 * by the expression for "url("...")":
	 * 			url("...)...")
	 * 
	 * A possible solution is to later on inside the extractUrlInCssText()-method instead of:
	 * 			if (url == null || url.startsWith("data:")) {
	 * also test for incomplete matches with:
	 * 			if (url == null || StringUtils.startsWithAny(url, "data:", "'data:", "\"data:")) {
	 * (org.apache.commons.lang3.StringUtils)
	 */
	@Test
	void extractUrlInCssTextIgnoresDataUrlsFromBootstrapMinCssTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min.css");
		assertDataUrlsFound(cssText//
				,
				"http://www.test.com/path/to/\"data:image/svg+xml,%3csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3e%3cpath stroke='rgba(0, 0, 0, 0.5"//
				,
				"http://www.test.com/path/to/\"data:image/svg+xml,%3csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3e%3cpath stroke='rgba(255, 255, 255, 0.5"//
		);
	}
	
	/**
	 * REMARK: NO URLS SHOULD BE FOUND.
	 */
	@Test
	void extractUrlInCssTextIgnoresDataUrlFromBootstrapSubsetTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min-subset.css");
		assertDataUrlsFound(cssText//
				,
				"http://www.test.com/path/to/\"data:image/svg+xml,%3csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3e%3cpath stroke='rgba(0, 0, 0, 0.5"//
		);
	}
	
	/**
	 * REMARK: THE HOST OF AN ABSOLUTE URL SHOULD NOT BE ALTERED.
	 */
	@Test
	void extractAbsoluteUrlFromCssTest() {
		// This css is a subset of: https://fonts.googleapis.com/css?family=Lato|Sanchez:400italic,400|Abhaya+Libre
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-absolute.css");
		assertDataUrlsFound(cssText//
				// This is currently the result, but is wrong
				, "http://www.test.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
				// This is what should be the result
//				, "https://fonts.gstatic.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
	@Test
	void extractRelativeUrlFromCssTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-relative.css");
		assertDataUrlsFound(cssText//
				, "http://www.test.com/path/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
	private void assertDataUrlsFound(final String cssText, final String... urls) {
		final WebURL webURL = Crawler4jTestUtils.newWebURL("http://www.test.com/path/to/bootstrap.min.css");
		
		final CssParseData cssParseData = Crawler4jTestUtils.newCssParseData();
		cssParseData.setTextContent(cssText);
		cssParseData.setOutgoingUrls(webURL);
		final Set<WebURL> outgoingUrls = cssParseData.getOutgoingUrls();
		
		Assertions.assertThat(outgoingUrls).hasSize(urls.length);
		Assertions.assertThat(outgoingUrls).map(t -> t.getURL()).isSubsetOf(urls);
	}
}
