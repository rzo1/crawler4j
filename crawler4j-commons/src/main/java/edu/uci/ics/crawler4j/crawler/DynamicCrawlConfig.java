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
package edu.uci.ics.crawler4j.crawler;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Extends {@link CrawlConfig} to add configuration properties for the Selenium's <em>WebDriver</em>.
 */
public class DynamicCrawlConfig extends CrawlConfig {

    public enum WebDriverType {
        chrome,
        firefox
    }

    private WebDriverType webDriverType = WebDriverType.firefox;

    /**
     * If dynamic content crawling is used, the maximum time to wait for dynamic content loading in seconds.
     */
    private int maxWaitForDynamicContentInSeconds = 2;

    /**
     * Absolute path to the driver binary on the local filesystem.
     */
    private Path webDriverPath;

    /**
     * Additional options for the web driver, that will be added to the default ones, or override them.
     */
    private Map<String, Optional<String>> webDriverOptions = Collections.emptyMap();

    public int getMaxWaitForDynamicContentInSeconds() {
        return maxWaitForDynamicContentInSeconds;
    }

    public void setMaxWaitForDynamicContentInSeconds(int maxWaitForDynamicContentInSeconds) {
        this.maxWaitForDynamicContentInSeconds = maxWaitForDynamicContentInSeconds;
    }

    public Path getWebDriverPath() {
        return webDriverPath;
    }

    public void setWebDriverPath(String webDriverPath) {
        this.webDriverPath = FileSystems.getDefault().getPath(webDriverPath).toAbsolutePath();
    }

    public WebDriverType getWebDriverType() {
        return webDriverType;
    }

    public void setWebDriverType(WebDriverType webDriverType) {
        this.webDriverType = webDriverType;
    }

    public Map<String, Optional<String>> getWebDriverOptions() {
        return webDriverOptions;
    }

    public void setWebDriverOptions(Map<String, Optional<String>> webDriverOptions) {
        this.webDriverOptions = webDriverOptions;
    }

    @Override
    public void validate() throws Exception {
        super.validate();

        if (!Files.exists(webDriverPath)) {
            throw new Exception("Headless browser driver not found at " + webDriverPath);
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                "Web driver type:: " + webDriverType + "\n" +
                "Web driver path:: " + webDriverPath + "\n" +
                "Web driver options:: " + String.join(" ", optionsAsArray(webDriverOptions)) + "\n" +
                "Max wait for dynamic content loading in seconds:: " + maxWaitForDynamicContentInSeconds + "\n";
    }

    /**
     * Default options for the headless browser
     * @param config the {@link DynamicCrawlConfig} where the user agent is defined.
     * @return the default options for the headless browser
     */
    public static Map<String, Optional<String>> defaultHeadlessBrowserOptions(DynamicCrawlConfig config) {
        return Map.of(
                "--headless", Optional.empty(),
                "--disable-gpu", Optional.empty(),
                "--window-size", Optional.of("1920,1080"),
                "--ignore-certificate-errors", Optional.empty(),
                "--user-agent", Optional.of(config.getUserAgentString()));
    }

    /**
     * Browser options defined in {@link DynamicCrawlConfig#getWebDriverOptions()} are concatenated to default options,
     * overriding in case of a duplicated option.
     */
    public static String[] headlessBrowserOptions(DynamicCrawlConfig config) {
        Map<String, Optional<String>> options = new LinkedHashMap<>();
        options.putAll(defaultHeadlessBrowserOptions(config));
        options.putAll(config.getWebDriverOptions());
        return optionsAsArray(options);
    }

    private static String[] optionsAsArray(Map<String, Optional<String>> options) {
        return options.entrySet()
                .stream()
                .map(e -> {
                    StringBuilder opt = new StringBuilder(e.getKey());
                    e.getValue().ifPresent(v -> opt.append("=").append(v));
                    return opt.toString();
                })
                .toArray(String[]::new);
    }

}
