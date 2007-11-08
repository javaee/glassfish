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

package com.sun.enterprise.config.serverbeans.validation.tests;

import com.sun.enterprise.config.serverbeans.ServerTags;
import java.util.Locale;

import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;
import com.sun.enterprise.config.serverbeans.validation.Result;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.admin.common.ObjectNames;

import java.util.StringTokenizer;
import java.util.logging.Level;


/**
   Custom Test for Http Listener Test which calls the Generic Validation before performing custom tests

   @author Srinivas Krishnan
   @version 2.0
*/

public class HttpListenerTest extends GenericValidator {
    
    private static final String DELIMITER=",";
    
    public HttpListenerTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        String choice = cce.getChoice();
        
        if(choice.equals(StaticTest.ADD) || choice.equals(StaticTest.VALIDATE)) {
            final HttpListener h = (HttpListener)cce.getObject();
            String vsId = h.getDefaultVirtualServer();
            try {
                Config config = (Config) ((HttpService) cce.getClassObject()).parent();
                if( config!=null ) {
                    boolean exists = checkVSExists(vsId, config);
                    if(!exists) {
                        result.failed(smh.getLocalString(getClass().getName() + ".virtualserverNotFound",
                                                         "Attribute(default-virtual-server={0}) : Virtual Server not found", new Object[]{vsId}));
                    } else if (h.isEnabled()){
                            // When the listener is enabled then the virtual
                            // server must be on.
                        if (! isVirtualServerOn(h, config, result)){
                            result.failed(smh.getLocalString(getClass().getName() + ".cannotAddVsNotOn",
                                                             "Cannot add this HttpListener \"{0}\" because it is enabled but its virtual server \"{1}\" has a state other than \"on\" ({2})",
                                                             new Object[]{h.getId(), vsId, getDefaultVirtualServer(vsId, config).getState()}));
                        }
                    }
                }
                
            }
            catch(Exception e){
                _logger.log(Level.FINE, "domainxmlverifier.error", e);
                
            }
        } else if (choice.equals(StaticTest.UPDATE)) {
            if (cce.getName().equals("enabled") && ConfigBean.toBoolean((String) cce.getObject())){
                final HttpListener h = (HttpListener) cce.getClassObject();
                final Config c = (Config) ((HttpService) h.parent()).parent();
                final VirtualServer vs = getDefaultVirtualServer(h.getDefaultVirtualServer(), c);
                if (null != vs && !vs.getState().equals("on")){
                    result.failed(smh.getLocalString(getClass().getName() + ".cannotUpdateVSNotOn",
                                                     "Cannot enable this HttpListener \"{0}\" because its virtual server \"{1}\" has a state other than \"on\" ({2})",
                                                     new Object[]{h.getId(), vs.getId(), vs.getState()}));
                                                     
                }
            } else if (ServerTags.ENABLED.equals(cce.getName()) && ! ConfigBean.toBoolean((String) cce.getObject())) {
                final HttpListener h = (HttpListener) cce.getClassObject();
                if (com.sun.enterprise.config.serverbeans.ServerHelper.ADMIN_HTTP_LISTNER_ID.equals(h.getId())) {
                    //this is rather stupid
                    final String locmsg = this.getClass().getName() + ".cantDisableAdminVS";
                    final String enmsg = "ADMVAL1076: The http-listener reserved for administrative purposes can not be disabled.";
                    final String msg = smh.getLocalString(locmsg, enmsg);
                    result.failed(msg);                    
                }
            }
        } else if (StaticTest.DELETE.equals(choice)) {
            //the admin-listener cannot be deleted
            if (cce.getObject() instanceof HttpListener) {
                final HttpListener h = (HttpListener) cce.getObject();
                // this assumes that "admin-listener" is reserved in any config, although it makes sense only for DAS
                // so, I am not making any checks if this is DAS's config
                if (com.sun.enterprise.config.serverbeans.ServerHelper.ADMIN_HTTP_LISTNER_ID.equals(h.getId())) {
                    //this is rather stupid
                    final String locmsg = this.getClass().getName() + ".cantDeleteAdminListener";
                    final String enmsg = "ADMVAL1075: The http-listener reserved for administrative purposes can not be deleted.";
                    final String msg = smh.getLocalString(locmsg, enmsg);
                    result.failed(msg);
                }
            }
        } else {
            _logger.log(Level.SEVERE, "domainxmlverifier.unknownchoice", choice);
        }
        
        return result;
    }

    private final boolean isVirtualServerOn(final HttpListener h, final Config c, final Result result){
        final VirtualServer vs = getDefaultVirtualServer(h.getDefaultVirtualServer(), c);
        return (null != vs && vs.getState().equals("on"));
    }
    
        
        /**
           Checks whether a virtual server with given id is available in the given
           server. Current hierarchy is that the http-service has a single
           virtual-server-class and which has many virtual-servers. An Http lsnr
           can have any one of these virtual servers as its default-virtual-server.
           @param vsID String representing the id of vs specified
           @param server ConfigBean representing the server.xml
           @return true if and only if the given vsID exists in given Server, 
           false otherwise
        */
    private boolean checkVSExists(String vsID, Config config) {
        return getDefaultVirtualServer(vsID, config) != null;
    }

        /**
           Get the default virtual server given an ID and the config
           in which teh server should be found.
           @param vsId the id of the virtual server to be found
           @param config the config to be searched for the virtual
           server
           @return the virtual server object, if found; null otherwise.
         */
    private final VirtualServer getDefaultVirtualServer(final String vsID, final Config config){
        final VirtualServer[] virtualServer = config.getHttpService().getVirtualServer();
        for(int i = 0 ; i < virtualServer.length ; i++) {
            if(vsID.equals(virtualServer[i].getId())) {
                return virtualServer[i];
            }
        }
        return null;
    }
    
        
}
