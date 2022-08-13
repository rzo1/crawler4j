package edu.uci.ics.crawler4j.parser.css;

import javax.annotation.Nonnull;

import com.helger.css.handler.ICSSParseExceptionCallback;
import com.helger.css.parser.ParseException;

public class ThrowingCSSParseExceptionCallback implements ICSSParseExceptionCallback {
	
	@Override
	public void onException (@Nonnull final ParseException ex) {
		throw new RuntimeException(ex);
	}
}
