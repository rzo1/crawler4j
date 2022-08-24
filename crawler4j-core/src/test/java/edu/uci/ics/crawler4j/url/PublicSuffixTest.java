/*
 * Copyright 2018 Paul Galbraith <paul.d.galbraith@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.url;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

class PublicSuffixTest {
	
	static TLDList internalTldList;
	
	static TLDList externalTldList;
	
	
	@BeforeAll
	public static void setup()
			throws IOException
	{
		CrawlConfig c1 = new CrawlConfig();
		c1.setOnlineTldListUpdate(false);
		internalTldList = new TLDList(c1);
		
		CrawlConfig c2 = new CrawlConfig();
		c2.setOnlineTldListUpdate(true);
		c2.setPublicSuffixLocalFile("src/test/resources/public_suffix_list.dat");
		externalTldList = new TLDList(c2);
	}
	
	@CsvSource({//
		"http://www.example.com, example.com, www"//
		, "http://dummy.edu.np, dummy.edu.np, ''"//
		})
	@ParameterizedTest
	void etldDomainsAreCorrectlyIdentifiedByInternalLookup(String url, String domain, String subdomain) {
		WebURL webUrl = new SleepycatWebURLFactory().newWebUrl();
		webUrl.setTldList(internalTldList);
		
		// when:
		webUrl.setURL(url);
		
		// then:
		Assertions.assertThat(webUrl.getDomain()).isEqualTo(domain);
		Assertions.assertThat(webUrl.getSubDomain()).isEqualTo(subdomain);
	}
	
	@CsvSource({//
		"http://www.example.com, example.com, www"//
		, "http://dummy.edu.np, dummy.edu.np, ''"//
		})
	@ParameterizedTest
	void etldDomainsAreCorrectlyIdentifiedByExternalLookup(String url, String domain, String subdomain) {
		WebURL webUrl = new SleepycatWebURLFactory().newWebUrl();
		webUrl.setTldList(externalTldList);
		
		// when:
		webUrl.setURL(url);
		
		// then:
		Assertions.assertThat(webUrl.getDomain()).isEqualTo(domain);
		Assertions.assertThat(webUrl.getSubDomain()).isEqualTo(subdomain);
	}
	
}
