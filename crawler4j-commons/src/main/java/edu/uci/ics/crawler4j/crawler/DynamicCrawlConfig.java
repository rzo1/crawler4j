package edu.uci.ics.crawler4j.crawler;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static edu.uci.ics.crawler4j.crawler.DynamicCrawlConfig.WebDriverType.firefox;

/**
 * Extends {@link CrawlConfig} to add configuration properties for the Selenium's <em>WebDriver</em>.
 */
public class DynamicCrawlConfig extends CrawlConfig {

    public enum WebDriverType {
        chrome,
        firefox
    }

    private WebDriverType webDriverType = firefox;

    /**
     * If dynamic content crawling is used, the maximum time to wait for dynamic content loading in seconds.
     */
    private int maxWaitForDynamicContentInSeconds = 2;

    /**
     * Absolute path to the driver binary on the local filesystem.
     */
    private String webDriverPath;

    /**
     * The user agent to be used by the web driver.
     */
    private String webDriverUserAgent;

    /**
     * Additional options for the web driver, that will be added to the default ones.
     */
    private List<String> webDriverOptions = Collections.emptyList();

    public int getMaxWaitForDynamicContentInSeconds() {
        return maxWaitForDynamicContentInSeconds;
    }

    public void setMaxWaitForDynamicContentInSeconds(int maxWaitForDynamicContentInSeconds) {
        this.maxWaitForDynamicContentInSeconds = maxWaitForDynamicContentInSeconds;
    }

    public String getWebDriverPath() {
        return webDriverPath;
    }

    public void setWebDriverPath(String webDriverPath) {
        this.webDriverPath = webDriverPath;
    }

    public WebDriverType getWebDriverType() {
        return webDriverType;
    }

    public void setWebDriverType(WebDriverType webDriverType) {
        this.webDriverType = webDriverType;
    }

    public List<String> getWebDriverOptions() {
        return webDriverOptions;
    }

    public void setWebDriverOptions(List<String> webDriverOptions) {
        this.webDriverOptions = webDriverOptions;
    }

    public String getWebDriverUserAgent() {
        return webDriverUserAgent;
    }

    public void setWebDriverUserAgent(String webDriverUserAgent) {
        this.webDriverUserAgent = webDriverUserAgent;
    }

    @Override
    public void validate() throws Exception {
        super.validate();

        if (!Files.exists(FileSystems.getDefault().getPath(webDriverPath))) {
            throw new Exception("Headless browser driver not found at " + webDriverPath);
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                "Web driver type:: " + webDriverType + "\n" +
                "Web driver path:: " + webDriverPath + "\n" +
                "Web driver user agent:: " + webDriverUserAgent + "\n" +
                "Web driver options:: " + String.join(" ", webDriverOptions) + "\n" +
                "Max wait for dynamic content loading in seconds:: " + maxWaitForDynamicContentInSeconds + "\n";
    }

}
