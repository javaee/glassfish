/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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
