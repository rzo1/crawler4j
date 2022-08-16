/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-commons
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
package edu.uci.ics.crawler4j.crawler.authentication;

import java.util.Map;

import javax.swing.text.html.FormSubmitEvent.MethodType;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Avi Hayun on 11/25/2014.
 *
 * BasicAuthInfo contains the authentication information needed for BASIC authentication
 * (extending AuthInfo which
 * has all common auth info in it)
 *
 * BASIC authentication in PHP:
 * <ul>
 *  <li>http://php.net/manual/en/features.http-auth.php</li>
 *  <li>http://stackoverflow.com/questions/4150507/how-can-i-use-basic-http-authentication-in-php
 *  </li>
 * </ul>
 */
public class BasicAuthInfo extends AuthInfo implements CredentialsProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthInfo.class);
	
    /**
     * Constructor
     *
     * @param username Username used for Authentication
     * @param password Password used for Authentication
     * @param loginUrl Full Login URL beginning with "http..." till the end of the url
     */
    public BasicAuthInfo(String username, String password, String loginUrl) {
        super(AuthenticationType.BASIC_AUTHENTICATION, MethodType.GET, loginUrl, username, password);
    }
    
    
    
    /**
     * BASIC authentication<br/>
     * Official Example: https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org
     * /apache/http/examples/client/ClientAuthentication.java
     */
    public void addCredentials(Map<AuthScope, Credentials> credentialsMap) {
        LOGGER.info("BASIC authentication for: {}", getLoginTarget());
        Credentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword().toCharArray());
        credentialsMap.put(new AuthScope(getHost(), getPort()), credentials);
    }
}
