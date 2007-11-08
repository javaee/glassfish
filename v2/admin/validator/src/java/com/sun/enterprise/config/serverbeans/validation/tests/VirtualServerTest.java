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

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;


/**
   Custom Test for Virtual Server Test which calls the Generic Validation before performing custom tests

   @author Srinivas Krishnan
   @version 2.0
*/

public class VirtualServerTest extends GenericValidator {
    
    
    public VirtualServerTest(ValidationDescriptor desc) {
        super(desc);
    } 
    
    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        try{
            if(cce.getChoice().equals(StaticTest.ADD) || cce.getChoice().equals(StaticTest.VALIDATE)) {
                ConfigContext context = cce.getConfigContext();
                VirtualServer virtual =  (VirtualServer) cce.getValidationTarget();
           
                    // For an ADD operation the virtual server is being
                    // added to an http-service, which is the
                    // configcontextevents class object. Therefore the config is
                    // the parent of the class object:
                final Config config = (Config) ((HttpService) cce.getClassObject()).parent();
                validateAttribute(ServerTags.HTTP_LISTENERS,virtual.getHttpListeners(),config, result);
                //validateAttribute(ServerTags.HOSTS,virtual.getHosts(),config, result);
            } else if(cce.getChoice().equals("UPDATE")) {
                    // For an UPDATE operation the class object is the
                    // virtual server whose attributes are being
                    // updated. Therefore the config is the parent of the
                    // parent of this object:
                final VirtualServer vs = (VirtualServer) cce.getClassObject();
                final Config config = (Config) vs.parent().parent();
                String name = cce.getName();
                String value = (String) cce.getObject();
                validateAttribute(name,value,config, result);
                    // IF the state is being turned off then this can only
                    // occur if there's no http-listener which has this
                    // virtual server as a default and which is enabled.
                if (name.equals(ServerTags.STATE) && !value.equals("on")){
                    checkAllRelatedHttpListenersAreDisabled(vs, result);
                }
            }
        }
        
        catch (final ConfigException ce){
            _logger.log(Level.WARNING, "domainxmlverifier.exception", ce);
        }


        return result;
    }
    
        

    private Set getReferers(final VirtualServer vs) throws ConfigException {
        final Set listeners = getPeerListeners(vs);
        if (listeners.isEmpty()) { return Collections.EMPTY_SET; }

        final Set result = new HashSet();
        for (final Iterator it = listeners.iterator(); it.hasNext(); ){
            final HttpListener l = (HttpListener) it.next();
            if (l.getDefaultVirtualServer().equals(vs.getId())){
                result.add(l);
            }
        }
        return result;
    }

    private Set getPeerListeners(final VirtualServer vs) throws ConfigException {
        final HttpService hs = (HttpService) vs.parent();
        if (null == hs) { return Collections.EMPTY_SET; }

        final Set result = new HashSet();
        result.addAll(Arrays.asList(hs.getHttpListener()));
        return result;
    }
        
        
    
    public void validateAttribute(String name, String value, Config config, Result result) {
        
        if(value== null || value.equals(""))
            return;
                  
        if(name.equals(ServerTags.HOSTS)) {
            Vector address = tokens(value);
            for(int i=0;i<address.size();i++) {
                try {
                    StaticTest.checkIPAddress((String) address.get(i));
                } catch(IllegalArgumentException e) {
                    result.failed(smh.getLocalString(getClass().getName() + ".addressNotAscii",
                                                             "Attribute({0}={1}) :  Invalid address syntax - {1}",
                                                             new Object[]{name, (String)address.get(i)}));
                } catch(Exception u) {
                    result.failed(smh.getLocalString(getClass().getName() + ".invalidHostsAddress",
                          "Invalid value for virtual server's ''{0}'' attribute. ''{1}'' is incorrect network address. ", 
                          new Object[]{name,(String)address.get(i)}));
                }
            }
        }
    }
   
    public Vector tokens(String value) {
        StringTokenizer token = new StringTokenizer(value,",");
        Vector test = new Vector();
        while(token.hasMoreTokens())
            test.add(token.nextToken());
        return test;
    }

    private final void checkAllRelatedHttpListenersAreDisabled(final VirtualServer vs, final Result result) throws ConfigException {
        for (final Iterator it = getReferers(vs).iterator(); it.hasNext(); ){
            final HttpListener l = (HttpListener) it.next();
            if (l.isEnabled()){
                result.failed(smh.getLocalString(getClass().getName()+".listenerEnabled",
                                                "Cannot disable the virtual server \"{0}\" because this is the default virtual server for the http listener \"{1}\".",
                                                new Object[]{vs.getId(), l.getId()}));
            }
        }
    }

    private final VirtualServer getVirtualServer(final ConfigContextEvent cce) throws ConfigException {
        return (VirtualServer) cce.getValidationTarget();
    }
    
}

