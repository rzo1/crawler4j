package edu.uci.ics.crawler4j.test;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.uci.ics.crawler4j.url.AbstractWebURL;

public class SimpleWebURL extends AbstractWebURL {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
