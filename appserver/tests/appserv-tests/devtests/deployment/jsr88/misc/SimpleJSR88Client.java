/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * SimpleJSR88Client.java
 *
 *Provides access to the JSR88 API through a convenient command line.
 *<p>
 *This is intended for internal testing use only. 
 *
 * Created on January 21, 2004, 11:22 AM
 */

import java.io.File;
import java.util.jar.Manifest;

import java.net.*;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.factories.*;
import javax.enterprise.deploy.shared.factories.*;
import javax.enterprise.deploy.spi.status.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;

//import javax.enterprise.deploy.model.*;

/**
 *
 * @author  tjquinn
 */
public class SimpleJSR88Client {

    
    private final String J2EE_DEPLOYMENT_MANAGER = "J2EE-DeploymentFactory-Implementation-Class";

    private DeploymentFactory deploymentFactory;

    private DeploymentManager deploymentManager;

    private String host;
    
    private String port;
    
    private String user;
    
    private String password;
    
    private String uri;
    
    /** Creates a new instance of SimpleJSR88Client */
    public SimpleJSR88Client(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.uri = "deployer:Sun:AppServer::" + host + ":" + port;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         *Get properties indicating the user, password, host, and port.
         */
        String host = null;
        String port = null; 
        String user = null; 
        String password = null;
        
        if (((host = System.getProperty("jsr88client.host")) == null)
            || ((port = System.getProperty("jsr88client.port")) == null)
            || ((user = System.getProperty("jsr88client.user")) == null)
            || ((password = System.getProperty("jsr88client.password")) == null) ) {
            System.out.println("The properties jsr88client.host, jsr88client.port, jsr88client.user, and jsr88client.password must be assigned");
            System.exit(-1);
        }
        
        try {
            new SimpleJSR88Client(host, port, user, password).run(args);
            System.exit(0);
        } catch (Throwable thr) {
            System.out.println("Error executing simple JSR88 client");
            thr.printStackTrace(System.out);
            System.exit(1);
        }
    }
    
    public void run(String[] args) throws TargetException, DeploymentManagerCreationException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Specify the JSR88 client command you want to execute as the first command line parameter");
        }
        
        if (args[0].equals("getAvailableAppClientModules") ){
            getAvailableModules(ModuleType.CAR);
        } else {
            throw new IllegalArgumentException("Unrecognized JSR88 client command specified: " + args[0]);
        }
    }
    
    private void getAvailableModules(ModuleType type) throws TargetException, DeploymentManagerCreationException {
        Target targets [] = getDeploymentManager().getTargets();
        TargetModuleID [] moduleIDs = getDeploymentManager().getAvailableModules(type, targets);
        System.out.println("Available modules listing");
        for (int i = 0; i < moduleIDs.length; i++) {
            System.out.println("Module " + i + ": " + moduleIDs[i].getModuleID());
        }
        System.out.println();
    }
    
    private DeploymentFactory loadDeploymentFactory() {
        System.out.println("Loading deployment factory");
        Object deploymentFactory = null;
        File file = null;
        String className = null;
        try {
            file = new File(System.getProperty("com.sun.aas.installRoot")
                + File.separator+ "lib" + File.separator + "deployment"
                + File.separator + "sun-as-jsr88-dm.jar");

            Manifest mf = new java.util.jar.JarFile(file).getManifest();
            className = mf.getMainAttributes().getValue(J2EE_DEPLOYMENT_MANAGER);
            URL[] urls = new URL[]{file.toURL()};
            URLClassLoader urlClassLoader = new java.net.URLClassLoader(urls, getClass().getClassLoader());
            Class factory = null;
            try {
                factory=urlClassLoader.loadClass(className);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
                System.exit(-1);
            }   

            try {   
                deploymentFactory = factory.newInstance();
            } catch (Exception ie) {
                ie.printStackTrace();
                System.exit(-1);
            }
            if (deploymentFactory instanceof DeploymentFactory) {
                DeploymentFactoryManager.getInstance().registerDeploymentFactory((DeploymentFactory) deploymentFactory);
            } else {
                    System.out.println("Expected instance of DeploymentFactory from class loading of " + className + " but got " + deploymentFactory.getClass().getName() + " instead");
                    System.exit(-1);
            }  
            
            } catch (Exception ex) {
                log("Failed to load the deployment factory using URL " + file.getAbsolutePath() + " and class " + className);
                ex.printStackTrace();
                System.exit(-1);
            }
            System.out.println("Deployment factory loaded.");
            return (DeploymentFactory) deploymentFactory;

    }
    
    private static void log(String message) {
        System.out.println("[" + getJSRClientName() + "]:: " + message);
    }
    
    protected static String getJSRClientName() {
        return "SimpleJSR88Client";
    }

    protected DeploymentFactory getDeploymentFactory() {
        if (this.deploymentFactory == null) {
            this.deploymentFactory = loadDeploymentFactory();
        }
        return this.deploymentFactory;
    }
    
    private DeploymentManager loadDeploymentManager() throws DeploymentManagerCreationException {
        System.out.println("Loading deployment manager using uri " + this.uri + " under user " + this.user);
        DeploymentManager answer = getDeploymentFactory().getDeploymentManager(this.uri, this.user, this.password); 
        System.out.println("Deployment manager loaded.");
        return answer;
    }
    
    protected DeploymentManager getDeploymentManager() throws DeploymentManagerCreationException {
        if (this.deploymentManager == null) {
            this.deploymentManager = loadDeploymentManager();
        }
        return this.deploymentManager;
    }
}
