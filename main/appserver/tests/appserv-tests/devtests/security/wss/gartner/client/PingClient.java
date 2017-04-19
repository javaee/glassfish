/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.wss.gartner.client;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class PingClient {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    @WebServiceRef
    private static PingEjbService ejbService;

    @WebServiceRef
    private static PingServletService servletService;

    public static void main(String args[]) {
        String host = args[0];
        String port = args[1];
        stat.addDescription("security-wss-ping");

        try {
            PingEjb pingEjbPort = ejbService.getPingEjbPort();

            ((BindingProvider)pingEjbPort).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://" + host + ":" + port + 
                "/PingEjbService/PingEjb?WSDL");

            String result = pingEjbPort.ping("Hello");
            if (result == null || result.indexOf("Sun") == -1) {
                System.out.println("Unexpected ping result: " + result);
                stat.addStatus("JWSS Ejb Ping", stat.FAIL);
            }
            stat.addStatus("JWSS Ejb Ping", stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus("JWSS Ejb Ping", stat.FAIL);
        }

        try {
            PingServlet pingServletPort = servletService.getPingServletPort();

            ((BindingProvider)pingServletPort).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://" + host + ":" + port + 
                "/security-wss-gartner-web/PingServletService?WSDL");

            String result = pingServletPort.ping("Hello");
            if (result == null || result.indexOf("Sun") == -1) {
                System.out.println("Unexpected ping result: " + result);
                stat.addStatus("JWSS Servlet Ping", stat.FAIL);
            }
            stat.addStatus("JWSS Servlet Ping", stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus("JWSS Servlet Ping", stat.FAIL);
        }
        stat.printSummary("security-wss-ping");
    }
}
