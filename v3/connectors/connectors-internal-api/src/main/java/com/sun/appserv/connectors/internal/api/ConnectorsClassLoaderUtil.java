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

package com.sun.appserv.connectors.internal.api;

import org.glassfish.internal.api.ConnectorClassFinder;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.loader.EJBClassLoader;
import com.sun.logging.LogDomains;


/**
 * Classloader util to create a new classloader for the provided .rar deploy directory.
 *
 * @author Jagadish Ramu
 */
@Service
@Scoped(Singleton.class)
public class ConnectorsClassLoaderUtil {

    @Inject
    private ClassLoaderHierarchy clh;

    private static List<ConnectorClassFinder> systemRARClassLoaders;

    private Logger _logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);

    public ConnectorClassFinder createRARClassLoader(String moduleDir, ClassLoader deploymentParent, String moduleName)
            throws ConnectorRuntimeException {

        ClassLoader parent = null;

        //For standalone rar :
        //this is not a normal application and hence cannot use the provided parent during deployment.
        //setting the parent to connector-class-loader's parent as this is a .rar
        //For embedded rar :
        //use the deploymentParent as the class-finder created won't be part of connector class loader
        //service hierarchy
        if(deploymentParent == null){
            parent = clh.getConnectorClassLoader(null).getParent();
        }else{
            parent = deploymentParent;
        }
        return createRARClassLoader(parent, moduleDir, moduleName);
    }

    private ConnectorClassFinder createRARClassLoader(final ClassLoader parent, String moduleDir, final String moduleName)
            throws ConnectorRuntimeException{
        ConnectorClassFinder cl = null;

        try{
        cl = (ConnectorClassFinder)AccessController.doPrivileged(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                    return new ConnectorClassFinder(parent, moduleName);
            }
        });
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "failed to create connector classloader", ex);
            ConnectorRuntimeException cre = new ConnectorRuntimeException(ex.getMessage());
            cre.initCause(ex);
            throw cre;
        }

        File file = new File(moduleDir);
        try {
            cl.appendURL(file.toURI().toURL());
            appendJars(file, cl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return cl;
    }

    public Collection<ConnectorClassFinder> getSystemRARClassLoaders() throws ConnectorRuntimeException {
        if(systemRARClassLoaders == null){
            List<ConnectorClassFinder> classLoaders = new ArrayList<ConnectorClassFinder>();
            for(String rarName : ConnectorConstants.systemRarNames){
                String location = ConnectorsUtil.getSystemModuleLocation(rarName);
                ConnectorClassFinder ccf = createRARClassLoader(location, null, rarName);
                classLoaders.add(ccf);
            }
            systemRARClassLoaders = classLoaders;
        }
        return systemRARClassLoaders;
    }

    public ConnectorClassFinder getSystemRARClassLoader(String rarName) throws ConnectorRuntimeException {
        Collection<ConnectorClassFinder> systemRarCLs = getSystemRARClassLoaders();
        for(ConnectorClassFinder ccf : systemRarCLs){
            if(ccf.getResourceAdapterName().equals(rarName)){
                return ccf;
            }
        }
        throw new ConnectorRuntimeException("No Classloader found for RA [ "+ rarName +" ]");
    }


    //TODO V3 handling "unexploded jars" for now, V2 deployment module used to explode the jars also
    private void appendJars(File moduleDir, EJBClassLoader cl) throws MalformedURLException {
        //TODO V3 for embedded rars - manifest classpath
        if (moduleDir.isDirectory()) {
            for (File file : moduleDir.listFiles()) {
                if (file.getName().toUpperCase().endsWith(".JAR")) {
                    cl.appendURL(file.toURI().toURL());
                } else if (file.isDirectory()) {
                    appendJars(file, cl); //recursive add
                }
            }
        }
    }
}
