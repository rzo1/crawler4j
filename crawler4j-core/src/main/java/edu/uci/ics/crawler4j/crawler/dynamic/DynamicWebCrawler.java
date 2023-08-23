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
package edu.uci.ics.crawler4j.crawler.dynamic;

import edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.dynamic.DynamicParser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extends {@link WebCrawler} for using a Selenium {@link WebDriver} to fetch dynamically generated HTML pages.
 */
public class DynamicWebCrawler extends WebCrawler {

    private WebDriver webDriver;

    @Override
    public void onStart() {
        // Instantiate and register the WebDriver instance for this crawler thread to the parser.
        // This is due to the fact Selenium's WebDriver is not thread-safe.
        webDriver = newWebDriverInstance((DynamicCrawlConfig) getMyController().getConfig());
        ((DynamicParser) getMyController().getParser()).setWebDriver(webDriver);
    }

    @Override
    public void onBeforeExit() {
        this.webDriver.close();
    }

    /**
     * Default options for the headless browser
     * @param config the {@link DynamicCrawlConfig} where the user agent is defined.
     * @return the default options for the headless browser
     */
    public List<String> defaultHeadlessBrowserOptions(DynamicCrawlConfig config) {
        return Arrays.asList(
                "--headless",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--ignore-certificate-errors",
                "--user-agent=" + config.getWebDriverUserAgent());
    }

    /**
     * Browser options defined in {@link DynamicCrawlConfig#getWebDriverOptions()} are concatenated to default options.
     */
    private String[] headlessBrowserOptions(DynamicCrawlConfig config) {
        List<String> options = new ArrayList<>();
        options.addAll(defaultHeadlessBrowserOptions(config));
        options.addAll(config.getWebDriverOptions());
        return options.toArray(String[]::new);
    }

    private WebDriver newWebDriverInstance(DynamicCrawlConfig config) {
        System.setProperty("webdriver.firefox.driver", config.getWebDriverPath());

        switch (config.getWebDriverType()) {
            case chrome:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments(headlessBrowserOptions(config));
                return new ChromeDriver(chromeOptions);
            case firefox:
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments(headlessBrowserOptions(config));
                return new FirefoxDriver(firefoxOptions);
            default:
                throw new IllegalArgumentException("Not reached");
        }
    }

}
