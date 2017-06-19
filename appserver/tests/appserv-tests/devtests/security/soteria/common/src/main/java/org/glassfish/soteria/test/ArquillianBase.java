/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.soteria.test;

import static java.util.logging.Level.SEVERE;
import static org.apache.http.HttpStatus.SC_MULTIPLE_CHOICES;
import static org.apache.http.HttpStatus.SC_OK;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.parser.Parser.xmlParser;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

public class ArquillianBase {
    
    private static final Logger logger = Logger.getLogger(ArquillianBase.class.getName());
    
    private WebClient webClient;
    private String response;

	@ArquillianResource
    private URL base;
	
    @Rule
    public TestWatcher ruleExample = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            super.failed(e, description);
            
            logger.log(SEVERE, 
                "\n\nTest failed: " + 
                description.getClassName() + "." + description.getMethodName() +
                
                "\nMessage: " + e.getMessage() +
                
                "\nLast response: " +
                
                "\n\n"  + formatHTML(response) + "\n\n");
            
        }
    };

    @Before
    public void setUp() {
        response = null;
        webClient = new WebClient() {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void printContentIfNecessary(WebResponse webResponse) {
                int statusCode = webResponse.getStatusCode();
                if (getOptions().getPrintContentOnFailingStatusCode() && !(statusCode >= SC_OK && statusCode < SC_MULTIPLE_CHOICES)) {
                    logger.log(SEVERE, webResponse.getWebRequest().getUrl().toExternalForm());
                }
                super.printContentIfNecessary(webResponse);
            }
        };
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @After
    public void tearDown() {
        webClient.getCookieManager().clearCookies();
        webClient.close();
    }
    
    protected String readFromServer(String path) {
        response = "";
        WebResponse localResponse = responseFromServer(path);
        if (localResponse != null) {
            response = localResponse.getContentAsString();
        }
        
    	return response;
    }
    
    protected WebResponse responseFromServer(String path) {
        
        WebResponse webResponse = null;
        
        Page page = pageFromServer(path);
        if (page != null) {
            webResponse = page.getWebResponse();
            if (webResponse != null) {
                response = webResponse.getContentAsString();
            }
        }
        
        return webResponse;
    }
    
    protected <P extends Page> P pageFromServer(String path) {
    	
    	if (base.toString().endsWith("/") && path.startsWith("/")) {
    		path = path.substring(1);
    	}
    	
        try {
            response = "";
            
            P page = webClient.getPage(base + path);
            
            if (page != null) {
                WebResponse localResponse = page.getWebResponse();
                if (localResponse != null) {
                    response = localResponse.getContentAsString();
                }
            }
            
            return page;
            
        } catch (FailingHttpStatusCodeException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    protected WebClient getWebClient() {
 		return webClient;
 	}
    
    public static String formatHTML(String html) {
        try {
            return parse(html, "", xmlParser()).toString();
        } catch (Exception e) {
            return html;
        }
    }
    
}
