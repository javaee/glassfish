package com.sun.s1asdev.ejb.webservice.commit.client;

import javax.xml.ws.WebServiceRef;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_ID = "ejb-webservices-commit";

    @WebServiceRef(wsdlLocation="http://localhost:8080/CommitBeanService/CommitBean?WSDL")
    private static CommitBeanService service;

    public static void main(String[] args) {
        stat.addDescription(TEST_ID);
        Client client = new Client();
        client.doTest(args);
        stat.printSummary(TEST_ID);
    }

    public void doTest(String[] args) {
        try {
            CommitBean port = service.getCommitBeanPort();

            try {
                // now do another create that should fail at commit
                // time and return an error.
                port.updateCustomer();
                System.out.println("call to updateCustomer() should" +
                                   " have failed.");
                stat.addStatus(TEST_ID, stat.FAIL);
            } catch(FinderException_Exception e) {
                System.out.println("incorrectly received Application exception"
                                   + " instead of system exception");
                stat.addStatus(TEST_ID, stat.FAIL);
            } catch(Throwable e) {
                System.out.println("Successfully received " + e + 
                                   "for commit failure");
                stat.addStatus(TEST_ID, stat.PASS);    
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus(TEST_ID, stat.FAIL);
        }
    }
}

