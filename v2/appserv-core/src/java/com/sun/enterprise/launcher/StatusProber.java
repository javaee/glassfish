/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.launcher;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.*;
import java.io.File;
import javax.xml.parsers.*;

import java.io.*;
import java.net.*;

/**
 * Create a filter for Appserver to use commons launcher. This filter is used 
 * to parse out the jvm-options from server.xml and use it to launch the vm.
 *
 * @author Ramesh Mandava (ramesh.mandava@sun.com) 
 */
public class StatusProber {

    final String domainConfigFilePath;
    
    public static void main( String[] args ) {
        if ( args.length != 1 ) {
            System.out.println("Usage : java StatusProber <s1as-instanceRoot>" );
            System.exit (1 );
        }
        String instanceRoot = args[0];
        String domainConfigFilePath = instanceRoot + File.separator +
                "config" + File.separator +
                "domain.xml";
        new StatusProber( domainConfigFilePath);
            //System.out.println("Domain Config Path -> " +domainConfigFilePath);

    }

    public StatusProber ( String configFilePath ) {
        this.domainConfigFilePath = configFilePath;
    }



    public void probeStatus ( ) {
        try {

	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
                 
	    Document doc = db.parse(domainConfigFilePath );

	    Element root = doc.getDocumentElement();
            Element httpListenerElement = null;
	    String webserverHostname= null;
	    String webserverPort= null;
	    String adminserverHostname= null;
	    String adminserverPort= null;

            NodeList httpListeners = root.getElementsByTagName("http-listener");
            // for loop. PENDING : Error condition check 
            for ( int hl=0; hl<httpListeners.getLength(); hl++ ) {
                httpListenerElement = (Element)httpListeners.item(hl);
	        String server_id = httpListenerElement.getAttribute ("id");
                String server_name= httpListenerElement.getAttribute
                    ("server-name");
                String server_port= httpListenerElement.getAttribute
                    ("server-port");
                if ( server_id.equals("admin-listener") ) {
                    adminserverHostname = server_name;
                    adminserverPort = server_port;
                } else {
                    webserverHostname = server_name;
                    webserverPort = server_port;
                }

            }
            /*
            System.out.println("Web Server HostName => " + webserverHostname);
            System.out.println("Web Server Port => " + webserverPort);
            System.out.println("Admin Server HostName => "+adminserverHostname);
            System.out.println("Admin Server Port => " +adminserverPort);
            System.out.println("Trying to check the status of Web Server");
            */
            int webServerPort = new Integer( webserverPort ).intValue();
            int adminServerPort = new Integer( adminserverPort ).intValue();
            StatusChecker sc = new StatusChecker( webserverHostname,  webServerPort );
            boolean serverUp = sc.probeServer();
            if ( !serverUp ) {
                System.out.println("Error : Web Server is not up in specified time interval");
                return;
            }
            //System.out.println("Now trying to see if admin server is up");
            sc = new StatusChecker( adminserverHostname, adminServerPort );
            serverUp = sc.probeServer();
            if ( !serverUp ) {
                System.out.println("Error : Admin Server is not up in specified time interval");
            }
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }


    protected class StatusChecker extends Thread{
 
        boolean status;
        String hostName;
        final long TIMEOUT = 100000;
        long startTime;
        long elapsedTime;
        String startPage="index.html";

        int timeout=240;

        int port= 8080;

        public StatusChecker (String hostName, int port, String startPage, int timeout ) {
            this.hostName= hostName;
            this.port= port;
            this.startPage= startPage;
            this.timeout = timeout;
        }

        public StatusChecker (String hostName, int port, String startPage  ) {
	    this( hostName, port, startPage,120);
        }

        public StatusChecker (String hostName, int port ) {
            this( hostName, port, "index.html", 120);

        }


        public boolean probeServer ( ) {
            this.start();
            try {
               this .join();
            } catch ( InterruptedException e ) {
		System.err.println("Exception : " + e );
	    }
	    if ( getStatus() ) {
	 	System.out.println("Server is up");
            } else {
                System.out.println("Error : Server is Not up");
	    }
            return getStatus();
        }

        public boolean getValue() { 
            try { 
                URL url = new URL ("http",hostName,port,"index.html");
                HttpURLConnection h = (HttpURLConnection)url.openConnection();
                if (h.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    return false; 
                } else {
                    System.out.println("Server " +hostName +" is Up ");
                    return true; 
                }
            } catch (ConnectException e) {
                //System.out.println("Server " +hostName + " is not yet up" );
                return false; 
            } catch (Exception e) {
                System.out.println("ERROR "+ e );
                return false; 
            }
        }

        public void setHostName( String hn ) {
            hostName = hn;
        }

        public void setPort( int port ) {
            this.port = port;
        }

        public void setTimeout( int timeout ) {
            this.timeout = timeout;
        }

        public boolean getStatus () {
            return status;
        }

        public void run () {
            long startTime;
            long elapsedTime;
            try {
                startTime = System.currentTimeMillis();
                //System.out.println("starttime"+startTime);
                System.out.println("Pinging localhost ...");
                status=getValue();
                //System.out.println("Status :"+ status);
                do {
                    elapsedTime = System.currentTimeMillis();
                    if ( !status) {
                        Thread.sleep (1000);
                        status=getValue();
                     } else
                         break;
                } while((elapsedTime - startTime) < timeout*1000);
            } catch (Exception e) {
                e.printStackTrace();
                status=false;
            }
        }
    }
}
