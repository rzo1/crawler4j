package edu.uci.ics.crawler4j.test;

import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public class DummyWebURLFactory implements WebURLFactory {
	
	@Override
	public WebURL newWebUrl() {
		return new SimpleWebURL();
	}
}
