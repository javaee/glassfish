/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

