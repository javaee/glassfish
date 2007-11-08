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

package com.sun.enterprise.addons;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.appserv.addons.AddonException;
import com.sun.appserv.addons.AddonVersion;
import com.sun.appserv.addons.ConfigurationContext;

/**
 * This class maintains the registry, it is a warpper over Properties class.
 * @author binod@dev.java.net
 */
public class AddonRegistry {
    
    /** Creates a new instance of Registry */
    private Properties registry = null;
    Properties systemRegistry = null;
    private FileOutputStream out = null;
    private FileOutputStream systemOut = null;
    private FileInputStream in = null;
    private FileInputStream systemIn = null;
    private File registryFile = null; 
    private File systemRegistryFile = null; 
    Logger logger = null;

    private final String REGISTRY = "domain-registry";
    private final String SYSTEMREGISTRY = ".domain-registry.system";
    private final String ENABLEKEY = ".enabled";
    final String CONFIGUREKEY = ".configured";
    final String INSTANCEKEY = ".instance";

    enum status { UNCHANGED, ENABLE, DISABLE, UNCONFIGURE, CONFIGURE, REMOVE, UPGRADE};
    
    AddonRegistry(File domainRoot, Logger logger) 
    throws AddonFatalException {
        this.logger = logger;
        load(domainRoot);
    }
    
    private void load(File domainRoot) throws AddonFatalException {
        try {
            this.registryFile = 
            new File(new File(domainRoot, "config"), REGISTRY);

            this.systemRegistryFile = 
            new File(new File(domainRoot,"config"), SYSTEMREGISTRY);

            if(!registryFile.exists()) {
                registryFile.createNewFile();

                if (logger.isLoggable(Level.FINER))
                    logger.log(Level.FINER, "Created : " + registryFile);
            }

            if(!systemRegistryFile.exists()) {
                systemRegistryFile.createNewFile();

                if (logger.isLoggable(Level.FINER))
                    logger.log(Level.FINER, "Created : " + systemRegistryFile);
            }
            systemIn = new FileInputStream(systemRegistryFile);
            systemRegistry = new Properties();
            systemRegistry.load(systemIn);

            if (logger.isLoggable(Level.FINER))
                logger.log(Level.FINER, "Loaded Registry : " + SYSTEMREGISTRY);

            in = new FileInputStream(registryFile);
            registry = new Properties();
            registry.load(in);

            if (logger.isLoggable(Level.FINER))
                logger.log(Level.FINER, "Loaded Registry : " + REGISTRY);
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINER))
                logger.log(Level.FINER, e.getMessage(), e);

            throw new AddonFatalException(e);
        }
    }

    /**
     * Retrieve the current status of the addon.
     * If the addon does not exist in the registry, then 
     * it should be configured.
     * If the addon status is changed to enabled/disabled
     * that is figured out by comparing with the system copy. 
     * of the registry.
     * If the addon is marked for unconfiguring, the addon 
     * status will be "unconfigure".
     * If the status is same in user copy and system copy
     * the status will be "unchanged"
     */
    status getStatus(String name){
        String confFlag = 
        String.class.cast(registry.get(name+CONFIGUREKEY));

        String systemConfFlag = 
        String.class.cast(systemRegistry.get(name+CONFIGUREKEY));

        String enableFlag = 
        String.class.cast(registry.get(name+ENABLEKEY));

        String systemEnableFlag = 
        String.class.cast(systemRegistry.get(name+ENABLEKEY));

        if (isNull(confFlag,systemConfFlag) && 
            isNull(enableFlag, systemEnableFlag)) {
            return status.CONFIGURE;
        }

        if (isEqual(confFlag, systemConfFlag) == false) {
            if (confFlag != null) {
                if (confFlag.equalsIgnoreCase("false")) {
                    return status.UNCONFIGURE;
                } else {
                    return status.CONFIGURE;
                }
            }
        }

        if (isEqual(enableFlag, systemEnableFlag) == false) {
            if (enableFlag != null && enableFlag.equalsIgnoreCase("true")) {
                return status.ENABLE;
            } else {
                return status.DISABLE;
            }
        }
       
        return status.UNCHANGED;
    }

    private boolean isNull(String flag, String systemFlag) {
        if (flag == null && systemFlag == null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isEqual(String flag, String systemFlag) {
        if (flag != null) {
            return flag.equals(systemFlag);
        } else {
            return systemFlag == null;
        }
    }

    /**
     * Change the status. It will make the status the same in 
     * user copy of the registry and system copy.
     */
    void setStatus(String name, status stat) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, 
            "Setting status of " + name + " as" + stat);
        }
        switch (stat) {
            case ENABLE:
                registry.setProperty(name+ENABLEKEY, "true");
                systemRegistry.setProperty(name+ENABLEKEY, "true");
                break;
            case DISABLE:
                registry.setProperty(name+ENABLEKEY, "false");
                systemRegistry.setProperty(name+ENABLEKEY, "false");
                break;
            case CONFIGURE:
                registry.setProperty(name+CONFIGUREKEY, "true");
                systemRegistry.setProperty(name+CONFIGUREKEY, "true");
                systemRegistry.setProperty
                (name+INSTANCEKEY+CONFIGUREKEY, "true");
                break;
            case UNCONFIGURE:
                registry.setProperty(name+CONFIGUREKEY, "false");
                systemRegistry.setProperty(name+CONFIGUREKEY, "false");
                systemRegistry.setProperty
                (name+INSTANCEKEY+CONFIGUREKEY, "false");
                break;
            case REMOVE:
                registry.remove(name+CONFIGUREKEY);
                registry.remove(name+ENABLEKEY);
                systemRegistry.remove(name+CONFIGUREKEY);
                systemRegistry.remove(name+ENABLEKEY);
                systemRegistry.remove(name+INSTANCEKEY+CONFIGUREKEY);
                break;
            default :
        }
    }
    
    /**
     * Write the properties to the file.
     */
    void store() throws AddonFatalException{
        try {
            out = new FileOutputStream(registryFile);
            registry.store(out, null);  
            out.close();

            systemOut = new FileOutputStream(systemRegistryFile);
            systemRegistry.store(systemOut, null);  
            systemOut.close();
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINER))
                logger.log(Level.FINER, e.getMessage(), e);

            throw new AddonFatalException(e);
        }
    }
    
    void close() throws AddonFatalException {
        try {
            if(in != null)
                in.close();
            if(systemIn != null)
                systemIn.close();
        } catch (Exception e) {
            if (logger.isLoggable(Level.FINER))
                logger.log(Level.FINER, e.getMessage(), e);

            throw new AddonFatalException (e);
        }
    }

    /**
     * Utility method that returns, if the addon status
     * require configuration or not.
     */
    public boolean isConfigurationRequired(String name) {
        if (logger.isLoggable(Level.FINER))
            logger.log(Level.FINER, 
            "Status for " + name + " is " + getStatus(name));

        if (getStatus(name) == status.UNCHANGED) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * Utility method that returns, if the addon status
     * require unconfiguration or not.
     */
    public boolean isUnConfigurationRequired(String name) {
        if (logger.isLoggable(Level.FINER))
            logger.log(Level.FINER, 
            "Status for " + name + " is " + getStatus(name));

        String confFlag = 
        String.class.cast(registry.get(name+CONFIGUREKEY));

        String systemConfFlag = 
        String.class.cast(systemRegistry.get(name+CONFIGUREKEY));

        if (isNull(confFlag,systemConfFlag)) {
            return false;
        }

        if (isEqual(confFlag, systemConfFlag) == false) {
            if (confFlag != null) {
                if (confFlag.equalsIgnoreCase("false")) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            if (confFlag != null) {
                if (confFlag.equalsIgnoreCase("false")) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Utility method that returns, if the addon status
     * require unconfiguration or not.
     */
    public boolean isInRegistry(String name) {
        if (logger.isLoggable(Level.FINER))
            logger.log(Level.FINER, 
            "Status for " + name + " is " + getStatus(name));

        String confFlag = 
        String.class.cast(registry.get(name+CONFIGUREKEY));

        String systemConfFlag = 
        String.class.cast(systemRegistry.get(name+CONFIGUREKEY));

        if (isNull(confFlag,systemConfFlag)) {
            return false;
        }

        return true;
    }
    
    protected AddonVersionImpl getOldVersion(AddonVersionImpl newVersion) 
        throws AddonException {
        String newNamePart = newVersion.getNamePart();
        ArrayList<String> al = getNames(CONFIGUREKEY);
        if ((al == null) || (al.size() < 1)) return null;
        for (String str: al) {
            if (str.startsWith(newNamePart)) {
                return (new AddonVersionImpl(str));
            }
        }
        return null;
    }

    private ArrayList<String> getNames(String suffix) {
        ArrayList<String> list = new ArrayList();
        String str;
        for (Enumeration e = registry.propertyNames(); e.hasMoreElements() ;) {
            str = (String) e.nextElement();
            if (str.endsWith(suffix)) {
                list.add(str.substring(0, (str.length() - suffix.length())));
            }
        }
        return list;
    }


}
