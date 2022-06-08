package edu.uci.ics.crawler4j.tests.parser;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.parser.CssParseData;
import edu.uci.ics.crawler4j.test.Crawler4jTestUtils;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.WebURL;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CssParseDataTest {

    /**
     * <p>
     * Remark: no urls should be found.
     * </p><p>
     * A problem was that the pattern inside CssParseData will match parts of data urls instead of the complete url.
     * More specifically, following will get matched
     * by the expression for "url(...)" instead of
     * by the expression for "url("...")":
     * url("...)...")
     * <p>
     */
    @Test
    void extractUrlInCssTextIgnoresDataUrlsFromBootstrapMinCssTest() {
        final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min.css");
        assertNoURLsFoundInCSS(cssText);
    }

    /**
     * Remark: no urls should be found.
     */
    @Test
    void extractUrlInCssTextIgnoresDataUrlFromBootstrapSubsetTest() {
        final String cssText = TestUtils.getInputStringFrom("/css/bootstrap.min-subset.css");
        assertNoURLsFoundInCSS(cssText);
    }
	
	/**
	 * REMARK: THE HOST OF AN ABSOLUTE URL SHOULD NOT BE ALTERED.
	 */
	@Test
	void extractAbsoluteUrlFromCssTest() {
		// This css is a subset of: https://fonts.googleapis.com/css?family=Lato|Sanchez:400italic,400|Abhaya+Libre
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-absolute.css");
		assertDataUrlsFound(cssText//
				// This was previously the result, but is wrong
//				, "http://www.test.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
				// This is what should be the result
				, "https://fonts.gstatic.com/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
	@Test
	void extractRelativeUrlFromCssTest() {
		final String cssText = TestUtils.getInputStringFrom("/css/fonts-relative.css");
		assertDataUrlsFound(cssText//
				, "http://www.test.com/path/s/sanchez/v13/Ycm0sZJORluHnXbIfmxh_zQA.woff2"//
		);
	}
	
    private void assertNoURLsFoundInCSS(final String cssText) {
        final WebURL webURL = Crawler4jTestUtils.newWebURL("http://www.test.com/path/to/bootstrap.min.css");

        final CssParseData cssParseData = Crawler4jTestUtils.newCssParseData();
        cssParseData.setTextContent(cssText);
        cssParseData.setOutgoingUrls(webURL);
        final Set<WebURL> outgoingUrls = cssParseData.getOutgoingUrls();
        assertNotNull(outgoingUrls);
        Assertions.assertThat(outgoingUrls).isEmpty();
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
