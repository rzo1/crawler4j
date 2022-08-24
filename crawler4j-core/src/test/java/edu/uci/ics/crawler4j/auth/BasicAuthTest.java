package edu.uci.ics.crawler4j.auth;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.test.TestUtils;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;
import edu.uci.ics.crawler4j.url.WebURLFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.io.File;
import java.net.InetAddress;
import java.util.List;

import org.apache.hc.client5.http.impl.InMemoryDnsResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class BasicAuthTest {

	@TempDir
  public File crawlStorageFolder;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
        .options(new WireMockConfiguration().dynamicPort())
        .build();

    @Disabled("TODO test/fix basic auth")
    @Test
    void httpBasicAuth() throws Exception {
        // given: "two pages on first.com behind basic auth"
        wm.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(TestUtils.getInputStringFrom("/html/basicAuth/landingPage.html")))
              );
        wm.stubFor(get(urlEqualTo("/some/index.html"))
                .withBasicAuth("user", "pass")
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(TestUtils.getInputStringFrom("/html/basicAuth/index.html")))
              );

        // and: "two pages on second.com are not with basic auth"
        wm.stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(TestUtils.getInputStringFrom("/html/basicAuth/landingPage.html")))
              );
        wm.stubFor(get(urlEqualTo("/some/index.html"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html")
                .withHeader("Host", "first.com")
                .withBody(TestUtils.getInputStringFrom("/html/basicAuth/index.html")))
              );

        //when: "just resolve first.com and second.com to localhost"
        final InMemoryDnsResolver inMemDnsResolver = new InMemoryDnsResolver();
        inMemDnsResolver.add("first.com"
                , InetAddress.getByName("127.0.0.1"));
        inMemDnsResolver.add("second.com"
                , InetAddress.getByName("127.0.0.1"));


        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder.getAbsolutePath());
        config.setMaxPagesToFetch(10);
        config.setPolitenessDelay(500);
        BasicAuthInfo basicAuthInfo = new BasicAuthInfo(
                "user", "pass",
                "http://first.com:" + wm.getPort() + "/"
        );
        config.setDnsResolver(inMemDnsResolver);
        config.setAuthInfos(List.of(basicAuthInfo));

        BasicURLNormalizer normalizer = TestUtils.newNormalizer();
        PageFetcher pageFetcher = new PageFetcher(config, normalizer);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        WebURLFactory webURLFactory = new SleepycatWebURLFactory();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher, webURLFactory);
        CrawlController controller = new CrawlController(config, normalizer, pageFetcher, robotstxtServer, TestUtils.createFrontierConfiguration(config));

        controller.addSeed("http://first.com:" + wm.getPort() + "/");
        controller.addSeed("http://second.com:" + wm.getPort() + "/");
        controller.start(WebCrawler.class, 1);


        // then: "first.com will receive credentials"
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html"))
                .withBasicAuth(new BasicCredentials("user", "pass"))
                .withHeader("Host", new EqualToPattern( "first.com:" + wm.getPort()))
        );
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/"))
                .withBasicAuth(new BasicCredentials("user", "pass"))
                .withHeader("Host", new EqualToPattern( "first.com:" + wm.getPort()))
        );

        // and: "second.com won't see secrets"
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/some/index.html"))
                .withHeader("Host", new EqualToPattern( "second.com:" + wm.getPort()))
        );
        wm.verify(exactly(1), getRequestedFor(urlEqualTo("/"))
                .withHeader("Host", new EqualToPattern( "second.com:" + wm.getPort()))
        );
    }

}
