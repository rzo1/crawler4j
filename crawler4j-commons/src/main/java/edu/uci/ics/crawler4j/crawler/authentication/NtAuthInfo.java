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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.swing.text.html.FormSubmitEvent.MethodType;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication information for Microsoft Active Directory
 */
public class NtAuthInfo extends AuthInfo implements CredentialsProvider {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NtAuthInfo.class);
	
    private String domain;

    public NtAuthInfo(String username, String password, String loginUrl, String domain) {
        super(AuthenticationType.NT_AUTHENTICATION, MethodType.GET, loginUrl, username, password);
        this.domain = domain;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    
    
    /**
     * Do NT auth for Microsoft AD sites.
     */
    public void addCredentials(Map<AuthScope, Credentials> credentialsMap) {
        LOGGER.info("NT authentication for: {}", getLoginTarget());
        try {
            Credentials credentials = new NTCredentials(getUsername(),
                    getPassword().toCharArray(), InetAddress.getLocalHost().getHostName(),
                    getDomain());
            credentialsMap.put(new AuthScope(getHost(), getPort()), credentials);
        } catch (UnknownHostException e) {
            LOGGER.error("Error creating NT credentials", e);
        }
    }
}
