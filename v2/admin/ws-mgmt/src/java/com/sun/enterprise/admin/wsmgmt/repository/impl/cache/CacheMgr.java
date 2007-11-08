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
package com.sun.enterprise.admin.wsmgmt.repository.impl.cache;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Responsible for keeping track of the deployed applications and stand
 * alone ejb and web modules with web services. This is a singleton for 
 * the domain. This class is intended to run on the Administration Server. 
 *
 * @author Nazrul Islam
 * @since  J2SE 5.0
 */
public class CacheMgr {

    /**
     * Returns the singleton instance of this class. 
     *
     * @return  sigleton instance of this class
     */
    public static synchronized CacheMgr getInstance() {
        if (_mgr == null) {
            _mgr = new CacheMgr();
        }

        return _mgr;
    }

    /**
     * Private constructor. Loads the cache index from the persistent store.
     */
    private CacheMgr() {
        load();
    }

    /**
     * Returns the persistent cache index location.
     *
     * @return the persistent cache file location
     */
    private String getPropertyFile() {
        String iRoot = 
            System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        String file = iRoot + File.separator + PEFileLayout.GENERATED_DIR 
                    + File.separator + CACHE_FILE;
        return file;
    }

   /**
    * Saves the current in memory cache indexes to a property file. 
    */
   void save() {

        FileOutputStream fos = null;
        try {
            Properties pro = new Properties();

            Collection eValues = _ejbModules.values();
            for (Iterator iter=eValues.iterator(); iter.hasNext();) {
                String ejbMod = (String) iter.next();
                pro.put(ejbMod, EJB);
            }
            Collection wValues = _webModules.values();
            for (Iterator iter=wValues.iterator(); iter.hasNext();) {
                String webMod = (String) iter.next();
                pro.put(webMod, WEB);
            }
            Collection aValues = _j2eeApplications.values();
            for (Iterator iter=aValues.iterator(); iter.hasNext();) {
                J2eeApplication app = (J2eeApplication) iter.next();
                pro.put(app.getName(), app.getPersistentValue());
            }

            File file = new File(getPropertyFile());
            fos = new FileOutputStream(file);
            pro.store(fos, "");

        } catch (Exception e) {
            _logger.log(Level.FINE, "Error while saving ws-mgmt cache file", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) { }
            }
        }
    }

    /**
     * Reads from the persistent cache index property file. 
     */
    private void load() {

        FileInputStream fis = null;
        try {
            File file = new File(getPropertyFile());

            // abort if file does not exist
            if (!file.exists()) {
                return;
            }

            fis = new FileInputStream(file);
            Properties pro = new Properties();
            pro.load(fis);

            for (Enumeration e = pro.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String val = pro.getProperty(key);

                if (EJB.equals(val)) {
                    _ejbModules.put(key, key);
                } else if (WEB.equals(val)) {
                    _webModules.put(key, key);
                } else {
                    J2eeApplication app = new J2eeApplication(key, val);
                    _j2eeApplications.put(key, app);
                }
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, 
                "Error while loading ws-mgmt cache file", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) { }
            }
        }
    }

    /**
     * Adds an application to the cache. 
     *
     * @param  name  name of the application 
     * @param  ejbBundles  ejb bundles with web services 
     * @param  webBundles  web bundles with web services
     */
    void addJ2eeApplication(String name, List ejbBundles, List webBundles) {

        J2eeApplication app = new J2eeApplication(name, ejbBundles, webBundles);
        _j2eeApplications.put(name, app);
    }

    /**
     * Removes an application from the cache.
     * 
     * @param  name name of the application
     */
    J2eeApplication removeJ2eeApplication(String name) {
        return (J2eeApplication) _j2eeApplications.remove(name);
    }

    /**
     * Returns all j2ee applications with web services.
     * 
     * @return  j2ee applications with web services
     */
    public Map getJ2eeApplications() {
        return _j2eeApplications;
    }

    /**
     * Returns all stand alone ejb modules with web services. 
     *
     * @return  stand alone ejb module with web services
     */
    public Map getEjbModules() {
        return _ejbModules;
    }

    /**
     * Adds an ejb module to the cache. 
     * 
     * @param  name  name of the stand alone ejb module with web services
     */
    void addEjbModule(String name) {
        _ejbModules.put(name, name);
    }

    /**
     * Removes an ejb module from cache. 
     *
     * @param  name  name of the stand alone ejb module
     */
    String removeEjbModule(String name) {
        return (String) _ejbModules.remove(name);
    }

    /**
     * Returns all stand alone web apps with web services.
     * 
     * @return  web apps with web services
     */
    public Map getWebModules() {
        return _webModules;
    }

    /**
     * Adds a web module to the cache. 
     *
     * @param  name  name of the stand alone web module with web services
     */
    void addWebModule(String name) {
        _webModules.put(name, name);
    }

    /**
     * Removes a web module from cache. 
     *
     * @param  name  name of the stand alone web module
     */
    String removeWebModule(String name) {
        return (String) _webModules.remove(name);
    }

    // ---- VARIABLES - PRIVATE -----------------------------
    private static CacheMgr _mgr  = null;
    private Map _j2eeApplications = new HashMap();
    private Map _ejbModules       = new HashMap();
    private Map _webModules       = new HashMap();
    private static final String EJB = "EJB_MODULE";
    private static final String WEB = "WEB_MODULE";
    private static final String CACHE_FILE = ".com_sun_appserv_wsindex";

    private static Logger _logger = Logger.getLogger(LogDomains.ADMIN_LOGGER);
}
