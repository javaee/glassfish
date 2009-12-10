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
import org.glassfish.api.admin.*;

import com.sun.enterprise.deployment.*;


import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;

import javax.naming.NamingException;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.ManagedBeanManager;
import com.sun.logging.LogDomains;

import javax.naming.*;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;


@Service
public class JavaModuleNamingProxy
        implements NamedNamingObjectProxy, PostConstruct {

    @Inject
    Habitat habitat;

    @Inject
    private ProcessEnvironment processEnv;

    private ProcessEnvironment.ProcessType processType;


    private static Logger _logger = LogDomains.getLogger(JavaModuleNamingProxy.class,
            LogDomains.NAMING_LOGGER);

    private InitialContext ic;

    public void postConstruct() {
        try {
            ic = new InitialContext();
        } catch(NamingException ne) {
            throw new RuntimeException("JavaModuleNamingProxy InitialContext creation failure", ne);
        }

        processType = processEnv.getProcessType();
    }

    private static final String JAVA_MODULE_CONTEXT
            = "java:module/";

    private static final String JAVA_APP_CONTEXT
            = "java:app/";

    private  static final String JAVA_APP_NAME
            = "java:app/AppName";

    private  static final String JAVA_MODULE_NAME
            = "java:module/ModuleName";

    public Object handle(String name) throws NamingException {

        // Return null if this proxy is not responsible for processing the name.
        Object returnValue = null;

        if( name.equals(JAVA_APP_NAME) ) {

            returnValue = getAppName();

        } else if( name.equals(JAVA_MODULE_NAME) ) {

            returnValue = getModuleName();

        } else if (name.startsWith(JAVA_MODULE_CONTEXT) || name.startsWith(JAVA_APP_CONTEXT)) {

            // Check for any automatically defined portable EJB names under
            // java:module/ or java:app/.

            // If name is not found, return null instead
            // of throwing an exception.
            // The application can explicitly define environment dependencies within this
            // same namespace, so this will allow other name checking to take place.
            returnValue = getJavaModuleOrAppEJB(name);
        }

        return returnValue;
    }

    private String getAppName() throws NamingException {

        ComponentEnvManager namingMgr =
                habitat.getComponent(ComponentEnvManager.class);

        String appName = null;

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

                appName = app.getAppName();               
            }
        }

        if( appName == null ) {
            throw new NamingException("Could not resolve " + JAVA_APP_NAME);
        }

        return appName;

    }

    private String getModuleName() throws NamingException {

        ComponentEnvManager namingMgr =
                habitat.getComponent(ComponentEnvManager.class);

        String moduleName = null;

        if( namingMgr != null ) {
            JndiNameEnvironment env = namingMgr.getCurrentJndiNameEnvironment();

            BundleDescriptor bd = null;

            if( env instanceof EjbDescriptor ) {
                bd = (BundleDescriptor) ((EjbDescriptor)env).getEjbBundleDescriptor();
            } else if( env instanceof BundleDescriptor ) {
                bd = (BundleDescriptor) env;
            }

            if( bd != null ) {
                moduleName = bd.getModuleDescriptor().getModuleName();
            }
        }

        if( moduleName == null ) {
            throw new NamingException("Could not resolve " + JAVA_MODULE_NAME);
        }

        return moduleName;

    }



    private Object getJavaModuleOrAppEJB(String name) throws NamingException {

        String newName = null;
        Object returnValue = null;

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

                    if ( !app.isVirtual() )  {
                        appName = app.getAppName();
                    }

                    String moduleName = bd.getModuleDescriptor().getModuleName();

                    StringBuffer javaGlobalName = new StringBuffer("java:global/");


                    if( name.startsWith(JAVA_APP_CONTEXT) ) {

                        // For portable EJB names relative to java:app, module
                        // name is already contained in the lookup string.  We just
                        // replace the logical java:app with the application name
                        // in the case of an .ear.  Otherwise, in the stand-alone
                        // module case the existing module-name already matches the global
                        // syntax.

                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append("/");
                        } 

                        // Replace java:app/ with the fully-qualified global portion
                        int javaAppLength = JAVA_APP_CONTEXT.length();
                        javaGlobalName.append(name.substring(javaAppLength));

                    } else {

                        // For portable EJB names relative to java:module, only add
                        // the application name if it's an .ear, but always add
                        // the module name.
   
                        if (appName != null) {
                            javaGlobalName.append(appName);
                            javaGlobalName.append("/");
                        }

                        javaGlobalName.append(moduleName);
                        javaGlobalName.append("/");

                        // Replace java:module/ with the fully-qualified global portion
                        int javaModuleLength = JAVA_MODULE_CONTEXT.length();
                        javaGlobalName.append(name.substring(javaModuleLength));
                    }

                    newName = javaGlobalName.toString();

                }
            }

        }

        if( newName != null ) {

            try {

                if( processType == ProcessType.ACC) {

                    ManagedBeanManager mbMgr = habitat.getByContract(ManagedBeanManager.class);

                    try {
                        returnValue = mbMgr.getManagedBean(newName);
                    } catch(Exception e) {
                        NamingException ne = new NamingException("Error creating ACC managed bean " + newName);
                        ne.initCause(e);
                        throw ne;                     
                    }

                }

                if( returnValue == null ) {
                    returnValue = ic.lookup(newName);
                }
            } catch(NamingException ne) {

                _logger.log(Level.FINE, newName + " Unable to map " + name + " to derived name " +
                        newName, ne);
            }
        }

        return returnValue;
    }

}



