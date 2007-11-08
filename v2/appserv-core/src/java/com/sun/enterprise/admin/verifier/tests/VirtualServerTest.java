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

/*  Test case to check the validity of Virtual Server fields
 *  Author : srini@sun.com
 */

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

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

import com.sun.enterprise.admin.verifier.*;
// Logging
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;



public class VirtualServerTest extends ServerXmlTest implements ServerCheck {
    // Logging
    static Logger _logger = LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    
    public VirtualServerTest() {
    }
 
    // check method invoked by the command line verifier
    public Result check(ConfigContext context) {
            Result result;
            result = super.getInitializedResult();
            // 8.0 XML Verifier
            /*try {
                Server server = (Server)context.getRootConfigBean();
                VirtualServerClass[] virtual = server.getHttpService().getVirtualServerClass();
                for(int i=0;i<virtual.length;i++) {
                    VirtualServer[] vserver =  virtual[i].getVirtualServer();
                    for(int j=0;j<vserver.length;j++){
                        // check the id  Bug: 4711203
                        int id;
                        try {
                            id = Integer.parseInt(vserver[j].getId().substring(0,1));
                            if(id <= 9 )
                                result.failed("Virtual Server ID cannot start with a number");
                        } catch(Exception e) {
                        }
                        // check Mime
                        String str = vserver[j].getMime();
                        if(str != null) {
                            if(!isMimeAvailable(str,context))
                                result.failed("Mime type " + vserver[j].getMime() + " not available for virtual server " + virtual[i].getId());
                        }
                        // check ACL
                        str = vserver[j].getAcls();
                        if(str!=null) {
                            // Bug 4711404
                            if(str.equals(""))
                                result.failed("ACL cannot be null String for virtual server " + vserver[i].getId());
                            Vector acls = tokens(str);
                            for(int k=0;k<acls.size();k++) {
                                if(! isAclAvailable((String)acls.get(k), context)) 
                                    // Bug 4711404
                                    result.failed("Acl not available for virtual server " + vserver[i].getId());
                            }
                        }
                        // check Httplisteners
                        str = vserver[j].getHttpListeners();
                        if(str!=null) {
                            // Bug 4711404
                            if(str.equals(""))
                                result.failed("Http Listeners cannot be null string for virtual server " + vserver[i].getId());
                            Vector https = tokens(str);
                            for(int k=0;k<https.size();k++) {
                                if(! isHttpListenerAvailable((String)https.get(i), context)) 
                                        // Bug 4711404
                                        result.failed("Http Listener not available for virtual server " + vserver[i].getId());
                            }
                        }
                        // check config file
                        String fileName = vserver[j].getConfigFile();
                        if(!(fileName==null || fileName.equals("obj.conf"))) {
                            File f = new File(fileName);
                            if(!f.exists())
                                result.failed("Config File " + fileName + " does not exists for virtual server " + virtual[i].getId());
                        }
                    }
                }
            } catch(Exception ex) {
                //<addition author="irfan@sun.com" [bug/rfe]-id="" >
                /*ex.printStackTrace();
                result.failed("Exception : " + ex.getMessage());*/
                /*_logger.log(Level.FINE, "serverxmlverifier.exception", ex);
                result.failed("Exception : " + ex.getMessage());
                //</addition>
            }*/
            return result;
    }
    
    // check method called from the admin GUI and iasadmin
    public Result check(ConfigContextEvent ccce) {
        Result result = new Result();
        Object value = ccce.getObject();
        ConfigContext context = ccce.getConfigContext();
        String beanName = ccce.getBeanName();
        result.passed("Passed ***");

        //BUG 4739891 BEGIN
        String choice = ccce.getChoice();
        if (choice.equals(StaticTest.DELETE)) {
            /* No need to verify this element if it is being DELETEd from
               the Config.
             */
            return result;
        }
        //BUG 4739891 END

        if(beanName != null) {
                    String name = ccce.getName();
                    result = testSave(name, (String)value, context);
                    return result;
        }
        VirtualServer virtual = (VirtualServer)value;
        
        // check if virtual server id is valid object name Bug : 4698687 : start
        String id = virtual.getId();
        if(StaticTest.checkObjectName(id, result)) 
            result.passed("Valid Object Name");
        else {
            result.failed("Virtual Server ID Invalid ");
            return result;
        }
        // End Bug : 4698687
        
        boolean isNumber = false;
        // check the id
        for(int i=0; i<10;i++) {
            if(virtual.getId().startsWith(String.valueOf(i)))
                    isNumber = true;
        }
        if(isNumber)
              result.failed("ID cannot start with a number");
        // check the mime type
/*
        if(!isMimeAvailable(virtual.getMime(),context))
            result.failed("Mime id not available");
*/
        return result;
    }    
    
    public Result testSave(String name, String value, ConfigContext context) {
        Result result = new Result();
        result.passed("passed ****");
        // check the config file
/*
        if(name.equals(ServerTags.CONFIG_FILE)) {
            if(value != null) {
                File f = new File(value);
                if(!f.exists())
                    result.failed("Config File Does not exist");
            }
        }
*/
        // check the log file
        if(name.equals(ServerTags.LOG_FILE)) {
            if(value != null) {
                File f = new File(value);
                if(!f.exists())
                    result.failed("Log File does not exist");
            }
        }
/*
        // check the acls
        if(name.equals(ServerTags.ACLS)){
            if(value!=null) {
                    Vector acls = tokens(value);
                    for(int i=0;i<acls.size();i++) {
                        if(! isAclAvailable((String)acls.get(i), context)) {
                            result.failed("Acl not available");
                            return result;
                        }
                    }
            }
        }
*/
        //check the httplisteners
        if(name.equals(ServerTags.HTTP_LISTENERS)) {
            if(value!=null) {
                    Vector https = tokens(value);
                    for(int i=0;i<https.size();i++) {
                        if(! isHttpListenerAvailable((String)https.get(i), context)) {
                                result.failed("Http Listener not available");
                                return result;
                        }
                    }
            }
        }
        return result;
    }
    
    public boolean isMimeAvailable(String value, ConfigContext context) {
/*
        try {
             //Server server = (Server)context.getRootConfigBean();
             Config config = StaticTest.getConfig(context);
             if(config!=null) {
                 Mime[] avlMime = config.getHttpService().getMime();
                 for(int i=0;i<avlMime.length;i++){
                     if(value.equals(avlMime[i].getId()))
                            return true;
                 }
             }
        }catch(Exception ex) {
            //<addition author="irfan@sun.com" [bug/rfe]-id="" >
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            //</addition>
        }
*/
        return false;
    }
    
    public boolean isAclAvailable(String value, ConfigContext context) {
/*
        try {
            //Server server = (Server)context.getRootConfigBean();
            Config config = StaticTest.getConfig(context);
            if(config!=null) {
                Acl[] avlAcl = config.getHttpService().getAcl();
                for(int i=0;i<avlAcl.length;i++) {
                    if(value.equals(avlAcl[i].getId()))
                        return true;
                }
            }
        }catch(Exception ex) {
            //<addition author="irfan@sun.com" [bug/rfe]-id="logging" >
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            //</addition>
        }
*/
        return false;
    }
    
    public boolean isHttpListenerAvailable(String value, ConfigContext context) {
        try {
            //Server server = (Server)context.getRootConfigBean();
            Config config = StaticTest.getConfig(context);
            if(config!=null) {
                HttpListener[] http = config.getHttpService().getHttpListener();
                for(int i=0;i<http.length;i++) {
                    if(value.equals(http[i].getId()))
                        return true;
                }
            }
        }catch(Exception ex) {
            //<addition author="irfan@sun.com" [bug/rfe]-id="logging" >
            /*e.printStackTrace();*/
            _logger.log(Level.FINE, "serverxmlverifier.exception", ex);
            //</addition>
        }
        return false;
    }
    
    public Vector tokens(String value) {
        StringTokenizer token = new StringTokenizer(value,",");
        Vector test = new Vector();
         while(token.hasMoreTokens()) 
                    test.add(token.nextToken());
        return test;
    }
}
