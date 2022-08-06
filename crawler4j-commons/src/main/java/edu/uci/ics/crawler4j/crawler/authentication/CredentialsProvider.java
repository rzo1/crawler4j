package edu.uci.ics.crawler4j.crawler.authentication;

import java.util.Map;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;

public interface CredentialsProvider {
	
	void addCredentials(Map<AuthScope, Credentials> credentialsMap);
	
}
