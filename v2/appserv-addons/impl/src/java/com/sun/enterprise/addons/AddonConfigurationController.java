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

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import javax.management.MBeanServer;
import com.sun.appserv.addons.ConfigurationContext;
import com.sun.appserv.addons.InstallationContext;
import com.sun.appserv.addons.Configurator;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.appserv.addons.AddonException;
import com.sun.appserv.addons.AddonVersion;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.enterprise.addons.util.ConfigurableJarFileFilter;
import com.sun.enterprise.addons.util.AddOnUtils;
import com.sun.enterprise.admin.common.MBeanServerFactory;

/**
 * Controller class that invoke configurator api. It is always 
 * called either before DAS starts up or before a remote instance
 * starts up. 
 *
 * @see com.sun.appserv.addons.Configurator.
 * @since 9.1
 * @authod binod@dev.java.net
 */
public class AddonConfigurationController extends AddonInstallationController {

    private final String SERVICEINTERFACE = "com.sun.appserv.addons.Configurator";
    private final String METAINFSERVICE = "META-INF/services/";

    private AddonRegistry ar = null;
    private DomainRoot amxDomainRoot = null;

    /**
     * Return the location of the configuration plugins.
     * It is AS_HOME/lib/addons folder.
     */
    protected File getServiceJarLocation() {
        return new File(new File(getInstallRoot(),"lib"), "addons");
    }

    /**
     * Return the service interface. It is 
     * META-INF/services/com.sun.appserv.addons.Configurator
     */
    protected String getServiceInterface() {
        return METAINFSERVICE + SERVICEINTERFACE;
    }

    /**
     * Return a file filter that returns all configurator plugins
     * whose state has been modified in the domain-registry.
     */
    protected FilenameFilter getFilenameFilter() {
        return new ConfigurableJarFileFilter(ar);
    }

    /**
     * Retrieve the addon name. It is the file name without .jar
     * extension.
     */
    protected String getName(File f) throws AddonFatalException {
        String name = f.getName();
        String regex = "\\.([j|J][a|A][r|R])";

        //Match any thing that ends with .jar
        if (name.matches(".*"+regex)) {
            return name.split(regex)[0];
        } else {
            throw new AddonFatalException
            (localStrings.getString("addon.notjarfile", name));
        }
    }

    /**
     * Invoke the configurator plugin just before starting DAS.
     */
    public void configureDAS(File installRoot, File domainRoot) 
        throws AddonFatalException{

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log
            (Level.FINER, "InstallRoot for configureDAS :" + installRoot);
            getLogger().log
            (Level.FINER, "DomainRoot for configureDAS :" + domainRoot);
        }

        configure(installRoot, domainRoot, 
                  ConfigurationContext.ConfigurationType.DAS);
    }

    /**
     * Invoke the configurator plugin just before starting a remote
     * instance.
     */
    public void configureInstances(File installRoot, File domainRoot) 
        throws AddonFatalException{
        configure(installRoot, domainRoot, 
                  ConfigurationContext.ConfigurationType.INSTANCE);
    }

    /**
     * Private method that actually invokes the configurator.
     */
    private void configure(File installRoot, File domainRoot, 
    ConfigurationContext.ConfigurationType type) throws AddonFatalException {

        setup(installRoot, domainRoot, type);
        loadServices(getServiceJarLocation());

        Set<Map.Entry<Object, String>> mainBasedServices =
           getMapSortedByValue(getMainClassBasedServices()).entrySet();
        for (Map.Entry<Object, String> entry : mainBasedServices) {
            invokeMain(entry.getKey(), entry.getValue(), type);
        }

        Set<Map.Entry<Object, String>> apiBasedServices =
           getMapSortedByValue(getApiBasedServices()).entrySet();
        for (Map.Entry<Object, String> entry : apiBasedServices) {
            try {
                invokeApi(entry.getKey(), entry.getValue(), type);
            } catch (Exception e) {
                getLogger().warning
                (localStrings.getString("addon.configurationcomplete.error", 
                "configure", entry.getKey(), e.getLocalizedMessage() ));
            } finally {
                ar.store();
                ar.close();
            }
        }

        ar.store();
        ar.close();
    }

    private void setup(File installRoot, File domainRoot, 
    ConfigurationContext.ConfigurationType type) 
        throws AddonFatalException {
        setInstallRoot(installRoot);
        setDomainRoot(domainRoot);
        if (type.equals(ConfigurationContext.ConfigurationType.DAS)) {
            ar = new AddonRegistry(domainRoot, getLogger());
        } else {
            ar = new AddonInstanceRegistry(domainRoot, getLogger());
        }
    }

    /**
     * Invoke main method of the configurator plugin. New plugins
     * will be invoked using the api.
     */
    private void invokeMain(Object obj, String addonName, 
    ConfigurationContext.ConfigurationType type) throws AddonFatalException {
        AddonRegistry.status addonStatus = ar.getStatus(addonName);
        if (addonStatus.equals(AddonRegistry.status.CONFIGURE)) {
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().finer("Starting " + addonStatus + " on " + addonName);
            }

            try {
                Method m = obj.getClass().getMethod("main", 
                       new Class[]{String[].class});
                String[] args = new String[] {getInstallRoot().
                getCanonicalPath(),getDomainRoot().getCanonicalPath()};
                m.invoke(obj, new Object[] {args});
                ar.setStatus(addonName, AddonRegistry.status.CONFIGURE);
                ar.setStatus(addonName, AddonRegistry.status.ENABLE);
                if (getLogger().isLoggable(Level.INFO)) {
                    String s = localStrings.getString("addon.configurecomplete",
                                addonStatus.toString(), addonName);
                    getLogger().info(s);
                }
            } catch (Exception e) {
                getLogger().log(Level.FINE, "Fatal Exception while " + 
                "configuring the addon " + addonName, e);
                throw new AddonFatalException (e);
            }
        }
    }

    /**
     * Invokes the com.sun.appserv.Configurator api.
     */
    private void invokeApi(Object obj, String addonName, 
    ConfigurationContext.ConfigurationType type) throws AddonFatalException {

        AddonRegistry.status addonStatus = ar.getStatus(addonName);
        if (getLogger().isLoggable(Level.FINER)) {
             getLogger().finer("Starting " + addonStatus + " on " + addonName);
        }

        // check for upgrade
        AddonVersionImpl newVersion, oldVersion;
        try {
            newVersion = new AddonVersionImpl(addonName);
            oldVersion = ar.getOldVersion(newVersion);
            if (oldVersion != null) {
                if (oldVersion.isHigher(newVersion)) {
                    addonStatus = AddonRegistry.status.UPGRADE;
                } else if (oldVersion.isLower(newVersion)) {
                    addonStatus = AddonRegistry.status.UNCHANGED;
                }
            }
        } catch (AddonException aoe) {
            throw new AddonFatalException(aoe);
        }

        Configurator conf = Configurator.class.cast(obj);
        InstallationContext ic = new InstallationContext();
        ic.setInstallationDirectory(getInstallRoot());
        ConfigurationContext cc = new ConfigurationContext();
        cc.setInstallationContext(ic);
        cc.setDomainDirectory(getDomainRoot());
        cc.setConfigurationType(type);
        if (type.equals(ConfigurationContext.ConfigurationType.INSTANCE))
            cc.setAMXDomainRoot(getAMXDomainRoot());
        setAdminCredentials(cc);
        try {
            switch (addonStatus) {
                case CONFIGURE :
                    checkDependencies(addonName);
                    conf.configure(cc);
                    ar.setStatus(addonName, AddonRegistry.status.CONFIGURE);
                    ar.setStatus(addonName, AddonRegistry.status.ENABLE);
                    break;
                case ENABLE :
                    conf.enable(cc);
                    ar.setStatus(addonName, AddonRegistry.status.ENABLE);
                    break;
                case DISABLE :
                    conf.disable(cc);
                    ar.setStatus(addonName, AddonRegistry.status.DISABLE);
                    break;
                case UNCONFIGURE :
                    conf.unconfigure(cc);
                    ar.setStatus(addonName, AddonRegistry.status.UNCONFIGURE);
                    break;
                case UPGRADE :
                    ar.setStatus(oldVersion.getName(), AddonRegistry.status.REMOVE);
                    conf.upgrade(cc, oldVersion);
                    ar.setStatus(addonName, AddonRegistry.status.CONFIGURE);
                    ar.setStatus(addonName, AddonRegistry.status.ENABLE);
                    // move old component to .deleted directory
                    break;
                default :
            }
            if (getLogger().isLoggable(Level.INFO)) {
                if (! addonStatus.equals(AddonRegistry.status.UNCHANGED)) {
                    String s = localStrings.getString("addon.configurecomplete",
                           addonStatus.toString(), addonName);
                    getLogger().info(s);
                }
            }
        } catch (AddonFatalException afe) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "configuring the addon " + addonName, afe);
            throw afe;
        } catch (AddonException ae) {
            getLogger().warning
            (localStrings.getString("addon.configurationcomplete.error", 
             addonStatus.toString(), addonName, ae.getLocalizedMessage() ));
        } catch (Exception e) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "configuring the addon " + addonName, e);
            throw new AddonFatalException(e);
        }
    }

    private void setAMXDomainRoot() throws AddonFatalException {
        try {
            MBeanServer mbs = MBeanServerFactory.getMBeanServer();
            this.amxDomainRoot = ProxyFactory.getInstance(mbs).getDomainRoot();
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
    }

    private DomainRoot getAMXDomainRoot() throws AddonFatalException {
        if (this.amxDomainRoot == null)
            setAMXDomainRoot();
        return this.amxDomainRoot;
    }

    private HashMap getMapSortedByValue(Map hmap) {
        HashMap map = new LinkedHashMap();
        if ((hmap == null) || (hmap.size() < 1)) {
            return map;
        }
        List mapKeys = new ArrayList(hmap.keySet());
        List mapValues = new ArrayList(hmap.values());
        hmap.clear();
        TreeSet sortedSet = new TreeSet(mapValues);
        Object[] sortedArray = sortedSet.toArray();
        int size = sortedArray.length;
        // Ascending sort
        for (int i=0; i<size; i++) {
            map.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), sortedArray[i]);
        }
        return map;
    }

    private void checkDependencies(String addonName) throws AddonFatalException {
        try {
            JarFile jar = AddOnUtils.getAddonJarFile(addonName,
                                getServiceJarLocation(), getFilenameFilter());
            if (jar == null) return;
            String [] dependencies = AddOnUtils.getDependencies(jar);
            if (dependencies == null) return;
            for (String dependency: dependencies) {
                if (ar.getStatus(dependency).equals(AddonRegistry.status.CONFIGURE)) {
                    throw new AddonFatalException
                    (localStrings.getString("addon.dependency.missing", addonName, dependency));
                }
            }
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
    }

    /**
     * Invoke the configurator plugin just before stopping DAS.
     */
    public void unconfigureDAS(File installRoot, File domainRoot) 
        throws AddonFatalException{

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log
            (Level.FINER, "InstallRoot for unconfigureDAS :" + installRoot);
            getLogger().log
            (Level.FINER, "DomainRoot for unconfigureDAS :" + domainRoot);
        }

        unconfigure(installRoot, domainRoot, 
                  ConfigurationContext.ConfigurationType.DAS);
    }

    /**
     * Private method that actually invokes the configurator.
     */
    private void unconfigure(File installRoot, File domainRoot, 
    ConfigurationContext.ConfigurationType type) throws AddonFatalException {

        setup(installRoot, domainRoot, type);

        File deletedDir = new File(getServiceJarLocation() + File.separator + ".deleted");

        if (! deletedDir.exists()) return;

        loadServices(deletedDir);

        Set<Map.Entry<Object, String>> apiBasedServices =
           getMapSortedByValue(getApiBasedServices()).entrySet();

        AddonRegistry.status addonStatus;
        String addonName;
        for (Map.Entry<Object, String> entry : apiBasedServices) {
            addonName = entry.getValue();
            if (! ar.isInRegistry(addonName)) continue;
            if (ar.isUnConfigurationRequired(addonName)) {
                Configurator conf = Configurator.class.cast(entry.getKey());
                InstallationContext ic = new InstallationContext();
                ic.setInstallationDirectory(getInstallRoot());
                ConfigurationContext cc = new ConfigurationContext();
                cc.setInstallationContext(ic);
                cc.setDomainDirectory(getDomainRoot());
                cc.setConfigurationType(type);
                try {
                    conf.unconfigure(cc);
                    ar.setStatus(addonName, AddonRegistry.status.REMOVE);
                } catch (AddonFatalException afe) {
                    getLogger().log(Level.SEVERE, "Fatal Exception while " +
                    "unconfiguring the addon " + addonName, afe);
                    throw afe;
                } catch (AddonException ae) {
                    getLogger().warning
                    (localStrings.getString("addon.unconfigurationcomplete.error", 
                     "unconfigure", addonName, ae.getLocalizedMessage() ));
                } catch (Exception e) {
                    getLogger().log(Level.WARNING, "Fatal Exception while " +
                    "unconfiguring the addon " + addonName, e);
                    throw new AddonFatalException(e);
                }
            }
        }
        ar.store();
        ar.close();
    }

}
