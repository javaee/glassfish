/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.webservice.ksink.standalone;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StandAloneClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private String word;
    private String targetEndpointAddress;
    private String googleKey;

    public static void main (String[] args) {
        stat.addDescription("googleserver standalone");	
        StandAloneClient client = new StandAloneClient(args);
        client.doTest();
        stat.printSummary("googleserver-standaloneID");
    }
    
    public StandAloneClient(String[] args) {
        word = (args.length > 0) ? "spellng" : args[0];
        targetEndpointAddress = (args.length > 1) ?
            args[1] : "http://api.google.com/search/beta2";
        googleKey = (args.length > 2) ?
            args[2] : "vCkqMIpV1WMKIRpNa7gBiYQZxDUYOYOj";
    }

    public void doTest() {
    	try {
            GoogleSearchService googleSearchService = 
                new GoogleSearchService_Impl();

            GoogleSearchPort googleSearchPort = 
                googleSearchService.getGoogleSearchPort();

            ((Stub)googleSearchPort)._setProperty
                (Stub.ENDPOINT_ADDRESS_PROPERTY,
                 targetEndpointAddress);

             ((Stub)googleSearchPort)._setProperty
                 (Stub.USERNAME_PROPERTY, "j2ee");
             ((Stub)googleSearchPort)._setProperty
                 (Stub.PASSWORD_PROPERTY, "j2ee");

            System.out.println("Contacting google for spelling suggestion at "
                               + targetEndpointAddress);
            String spellingSuggestion =
                googleSearchPort.doSpellingSuggestion(googleKey, word);
            System.out.println("Gave google the word '" + word + "' ... " +
                               " and the suggested spelling is '" +
                               spellingSuggestion + "'");

            stat.addStatus("appclient main", stat.PASS);
                
    	} catch (Exception ex) {
            System.out.println("google client test failed");
            ex.printStackTrace();
            stat.addStatus("appclient main" , stat.FAIL);

	} 

    }
        
}

