/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package google;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    private String googleKey;

    public static void main (String[] args) {
        stat.addDescription("webservices-google");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-googleID");
    }
    
    public void doTest(String[] args) {
        String word = (args.length == 0) ?
            "spellng" : args[0];
        String targetEndpointAddress = (args.length == 2) ?
            args[1] : "http://api.google.com/search/beta2";

    	try {
	    Context ic = new InitialContext();
            
                
            String googleKey = (String) ic.lookup("java:comp/env/googlekey");
            GoogleSearchService googleSearchService =
                (GoogleSearchService) ic.lookup("java:comp/env/service/GoogleSearch");
            GoogleSearchPort googlePort = 
                googleSearchService.getGoogleSearchPort();
            
            ((Stub)googlePort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);
            System.out.println("Contacting google for a spelling suggestion at " + targetEndpointAddress);
            String spellingSuggestion =
                googlePort.doSpellingSuggestion(googleKey, word);
            System.out.println("Gave google the word '" + word + "' ... " +
                               " and the suggested spelling is '" +
                               spellingSuggestion + "'");

            stat.addStatus("googleclient main", stat.PASS);

    	} catch (Exception ex) {
            System.out.println("google client test failed");
            ex.printStackTrace();
            stat.addStatus("googleclient main" , stat.FAIL);
            //System.exit(15);
	} 
    }
}
