package de.hshn.mi.crawler4j.url;

import crawlercommons.urlfrontier.Urlfrontier;
import edu.uci.ics.crawler4j.url.AbstractWebURL;
import edu.uci.ics.crawler4j.url.WebURL;

public class URLFrontierWebURLImpl extends AbstractWebURL implements WebURL {

    private static final Urlfrontier.StringList EMPTY_STRING = Urlfrontier.StringList.newBuilder().addValues("").build();
    private static final Urlfrontier.StringList MINUS_ONE = Urlfrontier.StringList.newBuilder().addValues("-1").build();

    private Urlfrontier.URLInfo rawInfo;
    public URLFrontierWebURLImpl() {

    }

    public URLFrontierWebURLImpl(Urlfrontier.URLInfo info) {
        this.rawInfo = info;
        this.setURL(info.getUrl());
        this.setParentUrl(info.getMetadataOrDefault("parent", EMPTY_STRING).getValues(0));
        this.setParentDocid(Integer.parseInt(info.getMetadataOrDefault("parentdocid", MINUS_ONE).getValues(0)));
        this.setPriority(Byte.parseByte(info.getMetadataOrThrow("priority").getValues(0)));
        this.setDepth(Short.parseShort(info.getMetadataOrThrow("depth").getValues(0)));
        this.setAnchor(info.getMetadataOrDefault("anchor", EMPTY_STRING).getValues(0));
        this.setParentDocid(Integer.parseInt(info.getMetadataOrThrow("docid").getValues(0)));
    }

    public Urlfrontier.URLInfo getRawInfo() {
        return this.rawInfo;
    }
}
