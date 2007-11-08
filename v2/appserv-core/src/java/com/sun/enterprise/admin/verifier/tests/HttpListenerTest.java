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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.enterprise.admin.verifier.tests;

/*  Test case to check the validity of Http listener fields
 *  Author : srini@sun.com
 */

import java.net.*;
import java.util.StringTokenizer;

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.admin.common.ObjectNames;

import com.sun.enterprise.admin.verifier.*;

// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;


public class HttpListenerTest extends ServerXmlTest implements ServerCheck {
    

     // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    private static final String DELIMITER=",";
    //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
    protected HttpListener thisListener;
    //</addition>
    
    public HttpListenerTest() {
    }
 
    // check method invoked by the command line verifier
    public Result check(ConfigContext context) {
            Result result;
            result = super.getInitializedResult();
            // 8.0 XML Verifier
            /*try {
                Server server = (Server)context.getRootConfigBean();
                HttpService http  = server.getHttpService();
                HttpListener[]  httpListener = http.getHttpListener();
                String httpPort = null;
                String httpAddress = null;
                for(int j=0;j<httpListener.length;j++) {
                        httpPort = httpListener[j].getPort();
                        httpAddress = httpListener[j].getAddress();
                        try {
                            if(StaticTest.isPortValid(Integer.parseInt(httpPort)))
                                result.passed("HttpListener Port is valid - " + httpPort );
                            else 
                                result.failed("HttpListener Port is Invalid - " + httpPort );
                        }catch(NumberFormatException e){
                               result.failed("Invalid Number for Http Listener Port - " + httpPort);
                        }
                        try{
                               // <addition> srini@sun.com Bug : 4697248
                               if(httpAddress == null || httpAddress.equals("")) {
                                   result.failed("Http Address cannot be Null");
                                   return result;
                               }
                               if(StaticTest.checkAddress(httpAddress)) 
                               // </addition> Bug : 4697248
                                    InetAddress.getByName(httpAddress).getHostName();
                               result.passed("Valid Http Listener IP");
                        }catch(UnknownHostException e){
                               result.failed("Host name of Http Listener not resolvable - " + httpAddress);
                        }
                 } 

            }
            catch(Exception ex) {
                // Logging
                _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
                result.failed("Exception : " + ex.getMessage());
            }*/
            return result;
    }
    
    // check method called from the admin GUI and iasadmin
    public Result check(ConfigContextEvent ccce) {
        //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
        Result result = new Result();
        //</addition>
                Object value = ccce.getObject();
                ConfigContext context = ccce.getConfigContext();
                //Result result = new Result(); - 4704985
                String beanName = ccce.getBeanName();
                if(beanName!=null) {
                    String name = ccce.getName();
                    // Bug: 4700257
                    result = testSave(name, (String)value, context);
                    return result;
                }
                HttpListener http = (HttpListener)value;
                //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
                thisListener = http;
                //</addition>
        if(ccce.getChoice().equals(StaticTest.DELETE))
        {
	    try {
                Config config = StaticTest.getConfig(context);
                if(config != null) {
                        HttpService service = config.getHttpService();
                        // 8.0 XML Verifier
                        //Server server = (Server)context.getRootConfigBean();
                        //HttpService service = server.getHttpService();
                        VirtualServer[] vs = service.getVirtualServer();

                        for (int i=0; vs != null && i < vs.length; i++) {
                            String httpListener = vs[i].getHttpListeners();
                            if (httpListener != null) {
                                StringTokenizer st = new StringTokenizer(httpListener, DELIMITER);
                                StringBuffer newHttpListener = new StringBuffer("");
                                String token = "";
                                while (st.hasMoreTokens()) {
                                    token = st.nextToken();
                                    if (token.equals(thisListener.getId())) {
                                        result.failed("This listener is being referenced by some virtual server. Please remove the reference and try again.");
                                        return result;
                                    }
                                }
                            }
                        } // end of for
                            result.passed("Test passed for referential integrity");
                }
              } catch (Exception e) {
                   result.failed("Exception Caught : " + e.getMessage());
              }
              return result;
        }
                // check if Http ID is valid object name Bug : 4698687 : start
                String id = http.getId();
                if(StaticTest.checkObjectName(id, result)) 
                    result.passed("Valid Object Name");
                else {
                    result.failed("Http Listener ID Invalid ");
                    return result;
                }
                // End Bug : 4698687
                
                try{
                     if(StaticTest.isPortValid(Integer.parseInt(http.getPort())))
                        result.passed("passed ***");
                     else {
                        result.failed("Invalid Port Number");
                        return result;
                     }
                     String port = http.getPort();
                     // <addition> srini@sun.com Bug : 4700257
                     // Checks if any other listeners uses the same port, true if used, false not used
                     //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
                     /*if(isPortUsed(port, context,result)) {
                         result.failed("Port Already In Use");
                         return result;
                     }*/
                     if(ccce.getChoice().equals(StaticTest.ADD) && isPortUsed(port, context,result)) {
                         return result;
                     }
                     // </addition>
                }
                catch(NumberFormatException e){
                    result.failed("Number Format Exception");
                    return result;
                }
                String httpAddress = http.getAddress();
                try{
                    // <addition> srini@sun.com Bug:4697248
                    if(httpAddress == null || httpAddress.equals("")) {
                          result.failed("Http Address cannot be Null value");
                          return result;
                    }
                    if(StaticTest.checkAddress(httpAddress)) 
                    // </addition> Bug:4697248
                        InetAddress.getByName(httpAddress).getHostName();
                    result.passed("Valid Http Listener IP Address");
                }catch(UnknownHostException e){
                    result.failed("Host name not resolvable - " + httpAddress);
                    return result;
                }
                String virtualServer = http.getDefaultVirtualServer();
                try {
                     // 8.0 XML Verifier 
                     //Server server = (Server)context.getRootConfigBean();
                     //boolean exists = checkVSExists(virtualServer, server); 
                     Config config = StaticTest.getConfig(context);
                     if( config!=null ) {
                         boolean exists = checkVSExists(virtualServer, config);
                         if(exists)
                             result.passed("Virtual Server found in vs class");
                         else
                             result.failed("Virtual Server not found in vs class");
                     }
                }catch(Exception e){
                     result.failed("Virtual Server specified not available ");
                }                    
                return result;
    }    
    
    public Result testSave(String name, String value, ConfigContext context) {
            Result result = new Result();
            result.passed("Passed ");
            if(name.equals(ServerTags.ADDRESS)){
                 try{
                    // <addition> srini@sun.com Bug:4697248
                    if(value == null || value.equals("")) {
                        result.failed("Http Address cannot be Null");
                        return result;
                    }
                    if(StaticTest.checkAddress(value)) 
                    // <addition> srini@sun.com Bug:4697248
                        InetAddress.getByName(value).getHostName();
                    result.passed("Valid Http Listener IP Address");
                 }catch(UnknownHostException e){
                    result.failed("Host name not resolvable - " + value);
                 } 
            }
            if(name.equals(ServerTags.PORT)){
                try{
                     if(StaticTest.isPortValid(Integer.parseInt(value)))
                        result.passed("passed ***");
                     else 
                        result.failed("Invalid Port Number");
                     // <addition> srini@sun.com Bug : 4700257
                     // Checks if any other listeners uses the same port, true if used, false not used
                     //<addition author="irfan@sun.com" [bug/rfe]-id="4704985" >
                     /* Removing the port duplicacy check as it is impossible to check for 
                      * duplicate port using a single algo for the paths through asadmin,
                      * admin GUI and Forte. This requires a change in the flow of code
                      * Refer BugTraq for more info on this
                      *
                     if( value!=null ) {
                             if(isPortUsed(value, context)) {
                                 result.failed("Port Already In Use");
                                 return result;
                             }
                     }*/
                     // </addition>
                }
                catch(NumberFormatException e){
                    result.failed("Number Format Exception");
                }
            }
            return result;
    }
    
    /**
        Checks whether a virtual server with given id is available in the given
        server. Current hierarchy is http-service has many virtual-servers. An Http lsnr
        can have any one of these virtual servers as its default-virtual-server.
        @param vsID String representing the id of vs specified
        @param server ConfigBean representing the server.xml
        @return true if and only if the given vsID exists in given Server, 
        false otherwise
    */
    private boolean checkVSExists(String vsID, Config config) {
	    VirtualServer vs = config.getHttpService().getVirtualServerById(vsID);
	    if(vs != null)
		return true;
	    return false;
    }
    
    // Added to check if port is already used by a listener Bug : 4700257
    //public boolean isPortUsed(String port, ConfigContext context){
    public boolean isPortUsed(String port, ConfigContext context, Result result)
    {
        result.passed("...");
        boolean flag = false;
        try {
            // 8.0 XML Verifier
            //Server server = (Server)context.getRootConfigBean();
            //HttpListener[] httpListener = server.getHttpService().getHttpListener();
            Config config = StaticTest.getConfig(context);
            if(config!=null) {
                HttpListener[] httpListener = config.getHttpService().getHttpListener();
                for(int i=0;i<httpListener.length;i++) {
                    if(!thisListener.getId().equals(httpListener[i].getId()))
                    {
                        if(port.equals(httpListener[i].getPort()) 
                            && thisListener.getAddress().equals(httpListener[i].getAddress()))
                        {
                            flag=true;
                            result.failed("Port Already In Use by : " + httpListener[i].getId());
                            break;
                        }
                    }
                }
            }
        } catch(Exception e) {
            //e.printStackTrace();
            result.failed("Exception Caught : " + e.getMessage());
        }
        return flag;
    }
    
}
