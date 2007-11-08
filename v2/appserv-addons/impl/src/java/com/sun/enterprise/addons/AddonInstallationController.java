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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Method;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.channels.FileChannel;
import com.sun.appserv.addons.Installer;
import com.sun.appserv.addons.InstallationContext;
import com.sun.appserv.addons.AddonException;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.enterprise.addons.util.JarFileFilter;
import com.sun.enterprise.addons.util.InstallerFileFilter;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * A controller class that install a specific addon or 
 * all addons. It is invoked either from appserver installer
 * or asadmin install-addon command. 
 *
 * @see com.sun.appserv.addons.Installer
 * @since 9.1
 * @authod binod@dev.java.net
 */
public class AddonInstallationController extends AddonController {

    private final String SERVICEINTERFACE = "com.sun.appserv.addons.Installer";
    private final String METAINFSERVICE = "META-INF/services/";
    private final String ADDONS = "addons";

    /**
     * Return the location where installer plugins will be kept.
     * It is AS_HOME/addons directory.
     */
    protected File getServiceJarLocation() {
        return new File(getInstallRoot(),"addons");
    }

    /**
     * Return the service interface name.
     * The value is META-INF/services/com.sun.appserv.addons.Installer
     */
    protected String getServiceInterface() {
        return METAINFSERVICE + SERVICEINTERFACE;
    }

    /**
     * Installers are simple jar files. Return an instance of 
     * a filenamefilter that returns all jar files. 
     */
    protected FilenameFilter getFilenameFilter() {
        return new JarFileFilter();
    }

    /**
     * Installer jar files are supposed to end with _installer.jar.
     * Split out that portion and return the addon name.
     */
    protected String getName(File f) throws AddonFatalException {
        //Match _installer.jar
        String regex = "(_installer)\\.([j|J][a|A][r|R])";
        String name = f.getName();

        //Allow any String before _installer.jar 
        if (name.matches(".*"+regex)) {
            return name.split(regex)[0];
        } else {
            throw new AddonFatalException
            (localStrings.getString("addon.notinstaller", name));
        }
    }

    /**
     * Main API used by clients. This installs all the addons.
     */
    public void install(File installRoot) throws AddonFatalException {
        setInstallRoot(installRoot);
        loadServices(getServiceJarLocation());

        Set<Map.Entry<Object,String>> apiBasedServices = 
        getApiBasedServices().entrySet();

        for (Map.Entry<Object,String> entry : apiBasedServices) {
            _install(entry.getKey(), entry.getValue());
        }

        Set<Map.Entry<Object,String>> mainClassBasedServices = 
        getMainClassBasedServices().entrySet();

        for (Map.Entry<Object, String> entry : apiBasedServices) {
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().finer("Installing " + entry.getValue());
            }

            Object obj = entry.getKey();
            String addonName = entry.getValue();
            try {
                Method m = obj.getClass().getMethod("main", 
                       new Class[] {String[].class});
                String[] args = new String[] 
                {getInstallRoot().getCanonicalPath()};
                m.invoke(obj, new Object[] {args});

                String addonInstalled = 
                localStrings.getString("addon.installed", addonName);
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().info(addonInstalled);
                }
            } catch (Exception e) {
                getLogger().log(Level.FINE, "Fatal Exception while " + 
                "configuring the addon " + addonName, e);
                throw new AddonFatalException (e);
            }
        }
    }

    /**
     * Call Installer.install() on a specific addon. 
     */
    private void _install(Object obj, String addonName) 
        throws AddonFatalException {
        Installer inst = Installer.class.cast(obj);
        InstallationContext ic = new InstallationContext();
        ic.setInstallationDirectory(getInstallRoot()); 
        try {
            inst.install(ic);
            String addonInstalled = 
            localStrings.getString("addon.installed", addonName);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().info(addonInstalled);
            }
        } catch (AddonFatalException afe) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "installing the addon " + addonName, afe);
            throw afe;
        } catch (AddonException ae) {
            String msg = localStrings.getString
            ("addon.installationcomplete.error", 
            addonName, ae.getLocalizedMessage());
            getLogger().warning(msg);
        } catch (Exception e) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "installing the addon " + addonName, e);
            throw new AddonFatalException(e);
        }
    }

    /**
     * Call Installer.uninstall() for a specific addon. 
     */
    private void _uninstall(Object obj, String addonName) 
        throws AddonFatalException {
        Installer inst = Installer.class.cast(obj);
        InstallationContext ic = new InstallationContext();
        ic.setInstallationDirectory(getInstallRoot()); 
        try {
            inst.uninstall(ic);
            if (getLogger().isLoggable(Level.INFO)) {
                getLogger().info
                (localStrings.getString("addon.uninstalled", addonName));
            }
        } catch (AddonFatalException afe) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "uninstalling the addon " + addonName, afe);
            throw afe;
        } catch (AddonException ae) {
            String msg = localStrings.getString
            ("addon.uninstallationcomplete.error", 
            addonName, ae.getLocalizedMessage());
            getLogger().warning(msg);
        } catch (Exception e) {
            getLogger().log(Level.FINE, "Fatal Exception while " +
            "uninstalling the addon " + addonName, e);
            throw new AddonFatalException(e);
        }
    }

    /**
     * API to uninstall a specific addon. The installer will be obtained 
     * from the AS_HOME/addons folder and installer api will be invoked.
     */
    public void uninstall(File installRoot, String addonName) 
        throws AddonFatalException {
        try {
            setInstallRoot(installRoot);
            File addonDir = new File(installRoot, ADDONS);
            File[] files = 
            addonDir.listFiles(new InstallerFileFilter(addonName));
            if (files == null || files.length == 0 ) {
                throw new AddonFatalException
                (localStrings.getString("addon.installernotfound", addonName));
            }
            if (files.length > 1 ) {
                throw new AddonFatalException
                (localStrings.getString("addon.morethanoneinstaller", addonName));
            }
            String service = findApiBasedService(new JarFile(files[0]));
            if (service != null) {
                Object obj = 
                createClassLoader(files[0].toURI().toURL()).
                loadClass(service).newInstance();
                _uninstall(obj, addonName);
            } else {
                throw new AddonFatalException
                (localStrings.getString("addon.servicenotfound",
                addonName, getServiceInterface()));
            }
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
    }

    /**
     * Install a specific plugin. This plugin will be copied to AS_HOME/addons
     * directory, before invoking the install api.
     */
    public void install(File installRoot, File jarFile) 
        throws AddonFatalException {
        try {
            setInstallRoot(installRoot);
            String addonName = getName(jarFile);
            String service = findApiBasedService(new JarFile(jarFile));
            if (service != null) {
                String fileName = jarFile.getName();
                File addonDir = new File(installRoot, ADDONS);
                if (getLogger().isLoggable(Level.FINER)) {
                    getLogger().log
                    (Level.FINER, "Addon Directory is :" + addonDir);
                }
                if (addonDir.exists() == false) {
                    addonDir.mkdir();
                }
                if (addonDir.equals(jarFile.getParentFile()) == false) {
                    FileOutputStream destFile = 
                    new FileOutputStream(new File(addonDir, fileName));

                    FileChannel dstChannel = destFile.getChannel();
                    FileChannel srcChannel = 
                    new FileInputStream(jarFile).getChannel();
                    
                    dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
                    srcChannel.close();
                    dstChannel.close();
                } 
                Object obj = 
                createClassLoader(jarFile.toURI().toURL()).
                loadClass(service).newInstance();
                _install(obj, addonName);
            } else {
                throw new AddonFatalException
                (localStrings.getString("addon.servicenotfound",
                addonName, getServiceInterface()));
            }
        } catch (Exception e) {
            throw new AddonFatalException(e);
        }
    }

    /**
     * Uninstall all addons. Typically invoked when appserver is uninstalled.
     */
    public void uninstall(File installRoot) throws AddonFatalException {
        setInstallRoot(installRoot);
        loadServices(getServiceJarLocation());

        Set<Map.Entry<Object,String>> apiBasedServices = 
        getApiBasedServices().entrySet();

        for (Map.Entry<Object,String> entry : apiBasedServices) {
            _uninstall(entry.getKey(), entry.getValue());
        }
    }

}
