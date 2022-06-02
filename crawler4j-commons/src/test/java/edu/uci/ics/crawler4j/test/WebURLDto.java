package edu.uci.ics.crawler4j.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.uci.ics.crawler4j.url.TLDList;
import edu.uci.ics.crawler4j.url.WebURL;

public class WebURLDto implements WebURL {
	
	private static final long serialVersionUID = 1L;
	
	private TLDList tldList;
	private int docid;
	private String URL;
	private int parentDocid;
	private String parentUrl;
	private short depth;
	private String domain;
	private String subDomain;
	private String path;
	private String anchor;
	private byte priority;
	private String tag;
	private Map<String, String> attributes = new LinkedHashMap<>();
	
	@Override
	public String getAttribute(String name) {
		return attributes.get(name);
	}
	
	public TLDList getTldList() {
		return tldList;
	}
	
	@Override
	public void setTldList(TLDList tldList) {
		this.tldList = tldList;
	}
	
	@Override
	public int getDocid() {
		return docid;
	}
	
	@Override
	public void setDocid(int docid) {
		this.docid = docid;
	}
	
	@Override
	public String getURL() {
		return URL;
	}
	
	@Override
	public void setURL(String url) {
		this.URL = url;
	}
	
	@Override
	public int getParentDocid() {
		return parentDocid;
	}
	
	@Override
	public void setParentDocid(int parentDocid) {
		this.parentDocid = parentDocid;
	}
	
	@Override
	public String getParentUrl() {
		return parentUrl;
	}
	
	@Override
	public void setParentUrl(String parentUrl) {
		this.parentUrl = parentUrl;
	}
	
	@Override
	public short getDepth() {
		return depth;
	}
	
	@Override
	public void setDepth(short depth) {
		this.depth = depth;
	}
	
	@Override
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	@Override
	public String getSubDomain() {
		return subDomain;
	}
	
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public String getAnchor() {
		return anchor;
	}
	
	@Override
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	
	@Override
	public byte getPriority() {
		return priority;
	}
	
	@Override
	public void setPriority(byte priority) {
		this.priority = priority;
	}
	
	@Override
	public String getTag() {
		return tag;
	}
	
	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	@Override
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
