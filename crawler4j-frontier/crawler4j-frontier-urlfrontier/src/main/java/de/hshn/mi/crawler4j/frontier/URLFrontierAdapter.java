/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-frontier-urlfrontier
 * %%
 * Copyright (C) 2010 - 2022 crawler4j-fork (pre-fork: Yasser Ganjisaffar)
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
package de.hshn.mi.crawler4j.frontier;

import crawlercommons.urlfrontier.URLFrontierGrpc;
import crawlercommons.urlfrontier.Urlfrontier;
import de.hshn.mi.crawler4j.url.URLFrontierWebURLImpl;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.url.WebURL;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class URLFrontierAdapter implements Frontier, DocIDServer {

    private static final Logger logger = LoggerFactory.getLogger(URLFrontierAdapter.class);

    private final String host;
    private final int port;
    private final int maxQueues;
    private final CrawlConfig crawlConfig;
    private final AtomicLong scheduledPages;
    private final AtomicLong completedPages;
    private final ManagedChannel channel;
    private final URLFrontierGrpc.URLFrontierStub asyncStub;
    private final URLFrontierGrpc.URLFrontierBlockingStub blockingStub;

    public URLFrontierAdapter(CrawlConfig crawlConfig, int maxQueues, String host, int port) {
        this.crawlConfig = crawlConfig;
        this.host = host;
        this.port = port;
        this.maxQueues = maxQueues;
        this.scheduledPages = new AtomicLong(0);
        this.completedPages = new AtomicLong(0);
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.asyncStub = URLFrontierGrpc.newStub(channel);
        this.blockingStub = URLFrontierGrpc.newBlockingStub(channel);
    }

    @Override
    public void scheduleAll(List<WebURL> urls) {

        int maxPagesToFetch = crawlConfig.getMaxPagesToFetch();

        final AtomicInteger acked = new AtomicInteger(0);
        final AtomicBoolean completed = new AtomicBoolean(false);
        int sent = 0;

        StreamObserver<Urlfrontier.String> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(crawlercommons.urlfrontier.Urlfrontier.String value) {
                        // receives confirmation that the value has been received
                        acked.addAndGet(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        completed.set(true);
                        logger.warn(t.getLocalizedMessage(), t);
                    }

                    @Override
                    public void onCompleted() {
                        completed.set(true);
                    }
                };

        final StreamObserver<Urlfrontier.URLItem> streamObserver = asyncStub.putURLs(responseObserver);

        for (WebURL url : urls) {

            if ((maxPagesToFetch > 0) &&
                    ((scheduledPages.get() + sent) >= maxPagesToFetch)) {
                break;
            }

            // don't sent too many in one go
            while (sent > acked.get() + 10000) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            final Urlfrontier.URLItem item = toItem(url);
            if (item == null) {
                logger.warn("Invalid url: {}", url.getURL());
            } else {
                streamObserver.onNext(item);
                sent++;
            }
        }

        if (sent > 0) {
            scheduledPages.addAndGet(sent);
        }

        streamObserver.onCompleted();

        // wait for completion
        while (!completed.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Urlfrontier.URLItem toItem(WebURL url) {
        try {
            final Urlfrontier.URLItem.Builder builder = Urlfrontier.URLItem.newBuilder();
            final Map<String, Urlfrontier.StringList> metadata = createMetadata(url);
            final Urlfrontier.URLInfo info = Urlfrontier.URLInfo.newBuilder()
                    .setUrl(url.getURL())
                    .putAllMetadata(metadata)
                    .build();
            builder.setDiscovered(Urlfrontier.DiscoveredURLItem.newBuilder().setInfo(info).build());
            return builder.build();
        } catch (RuntimeException e) {
            logger.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    private Map<String, Urlfrontier.StringList> createMetadata(final WebURL url) {
        final Map<String, Urlfrontier.StringList> metadata = new HashMap<>();

        if (url.getParentUrl() != null) {
            metadata.put("parent", Urlfrontier.StringList.newBuilder().addValues(url.getParentUrl()).build());
        }

        metadata.put("parentdocid", Urlfrontier.StringList.newBuilder().addValues(String.valueOf(url.getParentDocid())).build());
        metadata.put("priority", Urlfrontier.StringList.newBuilder().addValues(String.valueOf(url.getPriority())).build());
        metadata.put("depth", Urlfrontier.StringList.newBuilder().addValues(String.valueOf(url.getDepth())).build());

        if (url.getAnchor() != null) {
            metadata.put("anchor", Urlfrontier.StringList.newBuilder().addValues(url.getAnchor()).build());
        }
        metadata.put("docid", Urlfrontier.StringList.newBuilder().addValues(String.valueOf(url.getDocid())).build());
        return metadata;
    }

    @Override
    public void schedule(WebURL url) {
        int maxPagesToFetch = crawlConfig.getMaxPagesToFetch();
        if (maxPagesToFetch < 0 || scheduledPages.get() < maxPagesToFetch) {
            scheduleAll(List.of(url));
            scheduledPages.incrementAndGet();
        }
    }

    @Override
    public void getNextURLs(int max, List<WebURL> result) {
        final Urlfrontier.GetParams.Builder request =
                Urlfrontier.GetParams.newBuilder()
                        .setMaxUrlsPerQueue(max / maxQueues)
                        .setDelayRequestable(crawlConfig.getPolitenessDelay())
                        .setMaxQueues(maxQueues);

        blockingStub.getURLs(request.build())
                .forEachRemaining(
                        info -> result.add(new URLFrontierWebURLImpl(info)));

    }

    @Override
    public void setProcessed(WebURL webURL) {

        if (webURL instanceof URLFrontierWebURLImpl) {
            URLFrontierWebURLImpl url = (URLFrontierWebURLImpl) webURL;

            final StreamObserver<Urlfrontier.String> responseObserver =
                    new StreamObserver<>() {
                        @Override
                        public void onNext(crawlercommons.urlfrontier.Urlfrontier.String value) {
                            // receives confirmation that the value has been received
                            completedPages.addAndGet(1);
                        }

                        @Override
                        public void onError(Throwable t) {
                            logger.warn(t.getLocalizedMessage(), t);
                        }

                        @Override
                        public void onCompleted() {
                            //nothing to do
                        }
                    };

            final StreamObserver<Urlfrontier.URLItem> streamObserver = asyncStub.putURLs(responseObserver);

            final Urlfrontier.URLItem.Builder builder = Urlfrontier.URLItem.newBuilder();

            builder.setKnown(
                    Urlfrontier.KnownURLItem
                            .newBuilder()
                            .setInfo(url.getRawInfo())
                            .setRefetchableFromDate(0)
                            .build());

            streamObserver.onNext(builder.build());

        } else {
            logger.error("Received instance is not of type {}", URLFrontierWebURLImpl.class.getSimpleName());
        }
    }

    @Override
    public long getQueueLength() {
        return getStatistics().getNumberOfQueues();
    }

    @Override
    public long getNumberOfAssignedPages() {
        return getStatistics().getInProcess();
    }

    @Override
    public long getNumberOfProcessedPages() {
        // this is not officially implemented (impl detail) and not in the API - maybe later versions will have it?
        return completedPages.get();
    }

    @Override
    public long getNumberOfScheduledPages() {
        return scheduledPages.get();
    }

    @Override
    public boolean isFinished() {
        return getStatistics().getNumberOfQueues() > 0;
    }

    @Override
    public void close() {
        //nothing to do
    }

    @Override
    public void finish() {
       channel.shutdown();
    }

    @Override
    public int getDocId(String url) {
        logger.debug("URL Frontier does not know the concept of doc ids - so we do not care either");
        return -1;
    }

    @Override
    public int getNewDocID(String url) {
        logger.debug("URL Frontier does not know the concept of doc ids - so we do not care either");
        return -1;
    }

    @Override
    public void addUrlAndDocId(String url, int docId) {
        logger.debug("URL Frontier does not know the concept of doc ids - so we do not care either");
    }

    @Override
    public boolean isSeenBefore(String url) {
        //handled by URL Frontier
        return false;
    }

    @Override
    public int getDocCount() {
        return (int) getStatistics().getSize();
    }

    protected Urlfrontier.Stats getStatistics() {
        Urlfrontier.QueueWithinCrawlParams.Builder builder =
                Urlfrontier.QueueWithinCrawlParams.newBuilder();

        return blockingStub.getStats(builder.build());

    }
}
