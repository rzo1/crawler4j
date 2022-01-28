package de.hshn.mi.crawler4j.frontier;

import de.hshn.mi.crawler4j.url.URLFrontierWebURLFactory;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.frontier.FrontierConfiguration;
import edu.uci.ics.crawler4j.url.WebURLFactory;

public class URLFrontierConfiguration implements FrontierConfiguration {

    private final URLFrontierAdapter adapter;

    public URLFrontierConfiguration(CrawlConfig crawlConfig, int maxQueues, String hostname, int port) {
        this.adapter = new URLFrontierAdapter(crawlConfig, maxQueues, hostname, port);
    }

    @Override
    public DocIDServer getDocIDServer() {
        return adapter;
    }

    @Override
    public Frontier getFrontier() {
        return adapter;
    }

    @Override
    public WebURLFactory getWebURLFactory() {
        return new URLFrontierWebURLFactory();
    }

    @Override
    public void close() {
        //nothing to do
    }
}
