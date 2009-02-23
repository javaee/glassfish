/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.container.common.impl;


import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.invocation.ComponentInvocation;

import com.sun.enterprise.deployment.*;


import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import javax.naming.NamingException;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;

import javax.naming.*;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class JavaModuleNamingProxy
        implements NamedNamingObjectProxy {

    @Inject
    Habitat habitat;

    private static final String JAVA_MODULE_CONTEXT
            = "java:module/";

    public Object handle(String name) throws NamingException {

        // Initial basic implementation of access to portable global
        // naming context via java:module.   This needs to be expanded
        // greatly to handle the full capabilities of defining
        // environment dependencies at the java:module level.
        if (name.startsWith(JAVA_MODULE_CONTEXT)) {
            return getJavaModuleObject(name);
        }
        return null;
    }

    private Object getJavaModuleObject(String name) throws NamingException {

        String newName = null;

        if( habitat != null ) {
            ComponentEnvManager namingMgr =
                habitat.getComponent(ComponentEnvManager.class);

            if( namingMgr != null ) {
                JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();

                BundleDescriptor bd = null;

                if( env instanceof EjbDescriptor ) {
                    bd = (BundleDescriptor) ((EjbDescriptor)env).getEjbBundleDescriptor();
                } else if( env instanceof BundleDescriptor ) {
                    bd = (BundleDescriptor) env;
                }

                if( bd != null ) {
                    Application app = bd.getApplication();

                    String appName = null;

                    if ( (! app.isVirtual()) && (! app.isPackagedAsSingleModule()) ) {
                        appName = app.getRegistrationName();
                    }

                    // TODO replace with logic that specifically uses the new
                    // EE 6 definition of module name.

                    String moduleName = null;
                    if (appName == null) {
                        moduleName = app.getRegistrationName();
                    } else {
                        String archiveUri = bd.getModuleDescriptor().getArchiveUri();

                        // For now, just chop off the file extension
                        int length = archiveUri.length();
                        moduleName =  archiveUri.substring(0, length - 4);
                    }

                    StringBuffer javaGlobalName = new StringBuffer("java:global/");

                    if (appName != null) {
                        javaGlobalName.append(appName);
                        javaGlobalName.append("/");
                    }

                    javaGlobalName.append(moduleName);
                    javaGlobalName.append("/");

                    // Replace java:module/ with the fully-qualified global portion
                    int javaModuleLength = JAVA_MODULE_CONTEXT.length();
                    javaGlobalName.append(name.substring(javaModuleLength));

                    newName = javaGlobalName.toString();

                }
            }

        }

        if( newName == null ) {
            throw new NamingException("Invalid Java EE environment context for " +
                    name);
        }

        return new InitialContext().lookup(newName);
    }

}



