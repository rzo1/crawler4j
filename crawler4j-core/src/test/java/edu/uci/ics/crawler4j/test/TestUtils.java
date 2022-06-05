package edu.uci.ics.crawler4j.test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class TestUtils {
	private TestUtils() {
	}
	
	
	public static String getInputStringFrom(final String resourcePath) {
		try {
			return Files.readString(//
					new File(TestUtils.class.getResource(resourcePath).toURI()).toPath()//
					, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
