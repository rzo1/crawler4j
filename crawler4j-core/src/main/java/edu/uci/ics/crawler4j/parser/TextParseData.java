/*-
 * #%L
 * de.hs-heilbronn.mi:crawler4j-core
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
package edu.uci.ics.crawler4j.parser;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;

public class TextParseData implements ParseData {

    private String textContent;
    private Set<WebURL> outgoingUrls = new HashSet<>();
    
    
    public void parseAndSetOutgoingUrls(final Page page) throws Exception {
        // By default does nothing and relies on the Parser to extract urls from the textContent and set them
        // -> if the Parser exhibits this behavior (not in all cases), then setOutgoingUrls() will be called later on...
    }
    
    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public Set<WebURL> getOutgoingUrls() {
        return outgoingUrls;
    }

    @Override
    public void setOutgoingUrls(Set<WebURL> outgoingUrls) {
        this.outgoingUrls = outgoingUrls;
    }

    @Override
    public String toString() {
        return textContent;
    }
}
