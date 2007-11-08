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

package com.sun.enterprise.admin.verifier.tests;

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

import java.net.*;

/* Test Case to check the validity of IIOP listener fields
 * Author : srini@sun.com
 */

// 8.0 XML Verifier
//import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import  com.sun.enterprise.config.serverbeans.*;

import com.sun.enterprise.admin.verifier.*;

// Logging
import java.util.logging.*;
import com.sun.logging.*;

public class IiopListenerTest extends ServerXmlTest implements ServerCheck {
    
     // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public IiopListenerTest() {
    }

    // check method called by command line verifier
    public Result check(ConfigContext context)
    {
        Result result;
        result = super.getInitializedResult();
        // 8.0 XML Verifier
        /*try {
            Server server = (Server)context.getRootConfigBean();
            IiopService service = server.getIiopService();
            IiopListener[] listener = service.getIiopListener();
            
            // check how many listeners are enabled Bug : 4698687
            int count=0;
            for(int i=0;i<listener.length;i++) {
                if(listener[i].isEnabled())
                    count++;
            }*/
            /*
             * Fix comment: The following if check is totally spurious:
             * 1. The message shows that Only 2 iiop lsnrs can be enabled and
             *    the condition checked is count >= 2. ?????
             * 2. The exact restrictions (based on which the verifier works
             *    are not known and there is not enough time to test all
             *    that. Date : 08/14/02.
            */
            /*
            if(count >= 2) 
                result.failed("Only 2 Iiop Listeners can be enabled");
                */
            /*for(int i=0; i < listener.length;i++){
                String hostIP = listener[i].getAddress();
                String hostPort = listener[i].getPort();
                try {
                    if(!StaticTest.isPortValid(Integer.parseInt(hostPort)))
                        result.failed("Invalid IIOP Listener Port - " + hostPort);
                    else
                        result.passed("valid IIOP Listener Port");
                } catch(NumberFormatException e) {
                    result.failed("Port Number - " + hostPort + " : Invalid");
                }
                // Bug 4711404
                if(hostIP != null && hostIP.equals(""))
                    result.failed("Host IP cannot be null String");
                try{
                        InetAddress.getByName(hostIP).getHostName();
                        result.passed("Valid IIOP Listener IP");
                }catch(UnknownHostException e){
                    result.failed("Host name not resolvable - " + hostIP);
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
    
    // check method called by adminGUI and iasadmin
    public Result check(ConfigContextEvent ccce) {
        Result result = new Result();
        Object value  = ccce.getObject();
        String choice = ccce.getChoice();
        ConfigContext context = ccce.getConfigContext();
        String beanName = ccce.getBeanName();
        if(beanName!=null) 
                return testSave(ccce.getName(), ccce.getObject());
        
        IiopListener listener = (IiopListener)value;
        
        // check if IiopListener ID  is valid object name Bug : 4698687 : start
        String id = listener.getId();
        if(StaticTest.checkObjectName(id, result)) 
            result.passed("Valid Object Name");
        else {
            result.failed("IIOP Listener ID Invalid ");
            return result;
        }
        // End Bug : 4698687
        
        String hostIP = listener.getAddress();
        String hostPort = listener.getPort();
        
        if(choice.equals("ADD")) {
                try {
                    // 8.0 XML Verifier
                    //Server server = (Server)context.getRootConfigBean();
                    //IiopService service = server.getIiopService();
                    Config config = StaticTest.getConfig(context);
                    if(config!=null) {
                        IiopService service = config.getIiopService();
                        IiopListener[] listeners = service.getIiopListener();
                        // check how many listeners are enabled Bug : 4698687
                        int count=0;
                        for(int i=0;i<listeners.length;i++) {
                            if(listeners[i].isEnabled())
                                count++;
                        }
                        /*
                         * Fix comment: The following if check is totally spurious:
                         * 1. The message shows that Only 2 iiop lsnrs can be enabled and
                         *    the condition checked is count >= 2. ?????
                         * 2. The exact restrictions (based on which the verifier works
                         *    are not known and there is not enough time to test all
                         *    that. Date : 08/14/02.
                        */
                        /*
                        if(count >= 2) {
                            result.failed("Only 2 Iiop Listeners can be enabled");
                            return result;
                        }
                        */
                    }
                } catch(Exception e) {
                    result.failed("Exception occured " + e.getMessage());
                }
        }
        
        try {
            if(hostPort != null && !hostPort.equals("") ) {
                if(!StaticTest.isPortValid(Integer.parseInt(hostPort))) {
                    result.failed("Invalid IIOP Listener Port - " + hostPort);
                    // Return the result if port is invalid and do not do any further test.
                    return result;
                }
                else
                    result.passed("valid IIOP Listener Port");
            }
        } catch(NumberFormatException e) {
            result.failed("Port Number - " + hostPort + " : Invalid");
            return result;
        }
        try{
                InetAddress.getByName(hostIP).getHostName();
                result.passed("Valid IIOP Listener IP");
        }catch(UnknownHostException e){
            result.failed("Host name not resolvable - " + hostIP);
            return result;
        }
        return result;
    }
    
    public Result testSave(String name, Object value) {
           Result result = new Result();
           result.passed("Passed **");
           if(name.equals(ServerTags.ADDRESS)) {
               try{
                    InetAddress.getByName((String)value).getHostName();
                    result.passed("Valid Http Listener IP Address");
               }catch(UnknownHostException e){
                    result.failed("Host name not resolvable - " + (String)value);
               }    
           }
           String hostPort = (String) value;
           if(name.equals(ServerTags.PORT)){
                try {
                   if(hostPort != null && !hostPort.equals("")) {
                       if(!StaticTest.isPortValid(Integer.parseInt(hostPort))) {
                            result.failed("Invalid IIOP Listener Port - " + hostPort);
                       }
                       else {
                            result.passed("valid IIOP Listener Port");
                       }
                   }
                } catch(NumberFormatException e) {
                    result.failed("Port Number - " + hostPort + " : Invalid");
                }
           }
           return result;
    }
}
