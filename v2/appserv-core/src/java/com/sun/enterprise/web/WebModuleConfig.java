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

package com.sun.enterprise.web;

import java.io.File;
import java.util.HashMap;

import org.apache.catalina.util.StringManager;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.util.io.FileUtils;

/**
 * Represents the configuration parameters required in order to create
 * and install a web module (web application) in the iAS 7.0 server runtime.
 */
public class WebModuleConfig {

    // ----------------------------------------------------- Instance Variables

    /**
     * The config bean containing the properties specified in the web-module
     * element in server.xml.
     */
    private WebModule _wmBean = null;

    /**
     * The parent directory under which the work directory for files generated
     * by the web application (i.e compiled JSP class files etc) resides.
     */
    private String _baseDir = null;

    /**
     * The parent classloader for the web application.
     */
    private ClassLoader _parentLoader = null;

   /**
    * Deployment descriptor information about the web application.
    */
   private WebBundleDescriptor _wbd = null;

   /** 
    * keep a list of virtual servers that this webmodule is associated with
    */
   private String _vsIDs;

    // START S1AS 6178005
    private String stubBaseDir;
    // END S1AS 6178005


   /**
     * The string manager for this package.
     */
    private static StringManager _sm =
        StringManager.getManager(Constants.Package);

    // ------------------------------------------------------------- Properties

    /**
     * Set the elements of information specified in the web-module element
     * in server.xml.
     */
    public void setBean(WebModule wmBean) {
        _wmBean = wmBean;
    }


    /**
     * Return the configuration information specified in server.xml.
     */
    public WebModule getBean() {
        return _wmBean;
    }

    /**
     * Return the name of the web application (as specified in server.xml)
     *
     * @return [$appID:]$moduleID
     */
    public String getName() {
        String name = null;
        if (_wmBean != null) {
            StringBuffer buffer = new StringBuffer();
            String appName = getAppName();
            if (appName != null) {
                // Include the application id (if this is not a 
                // standalone web module)
                buffer.append(appName);
                buffer.append(Constants.NAME_SEPARATOR);
            }
            buffer.append(getModuleName());
            name = buffer.toString();
        }
        return name;
    }

    /**
     * Return the context path at which the web application is deployed.
     */
    public String getContextPath() {
        String ctxPath = null;
        if (_wmBean != null) {
                ctxPath = _wmBean.getContextRoot().trim();
                // Don't prefix a / if this web module is the default one
                // i.e. has an empty context-root
                if ((ctxPath.length() > 0) && !ctxPath.startsWith("/")) {
                    ctxPath = "/" + ctxPath;
                } else if (ctxPath.equals("/")) {
                    ctxPath = "";
                }
        }
        return ctxPath;
    }

    /**
     * Return the directory in which the web application is deployed.
     */
    public String getLocation() {
        String dir = null;
        if (_wmBean != null) {
            dir = _wmBean.getLocation();
        }
        return dir;
    }

    /**
     * Return the list of virtual servers to which the web application is
     * deployed.
     */
    public String getVirtualServers() {
        /*String vsIDs = null;
        if (_wmBean != null) {
            vsIDs = _wmBean.getVirtualServers();
        }
         */
        return _vsIDs;
    }

    /**
     * Return the list of virtual servers to which the web application is
     * deployed.
     */
    public void setVirtualServers(String virtualServers) {
        _vsIDs = virtualServers;
    }
    
    /**
     * Set the parent classloader for the web application.
     */
    public void setParentLoader(ClassLoader parentLoader) {
        _parentLoader = parentLoader;
    }

    /**
     * Return the parent classloader for the web application.
     */
    public ClassLoader getParentLoader() {
        return _parentLoader;
    }

    /**
     * Return the work directory for this web application.
     *
     * The work directory is either
     *   generated/j2ee-apps/$appID/$moduleID
     * or
     *   generated/j2ee-modules/$moduleID
     */
    public String getWorkDir() {
        return getWebDir(_baseDir);
    }


    // START S1AS 6178005
    /**
     * Gets the stub path of this web application.
     *
     * @return Stub path of this web application
     */
    public String getStubPath() {
        return getWebDir(stubBaseDir);
    }
    // END S1AS 6178005


    /**
     * Set the base work directory for this web application.
     *
     * The actual work directory is a subdirectory (using the name of the
     * web application) of this base directory.
     *
     * @param baseDir The new base directory under which the actual work
     *                directory will be created
     */
    public void setWorkDirBase(String baseDir) {
        _baseDir = baseDir;
    }


    // START S1AS 6178005
    /**
     * Sets the base directory of this web application's stub path.
     *
     * @param stubPath Stub path
     */
    public void setStubBaseDir(String stubBaseDir) {
        this.stubBaseDir = stubBaseDir;
    }
    // END S1AS 6178005


    /**
     * Return the object representation of the deployment descriptor specified
     * for the web application.
     */
    public WebBundleDescriptor getDescriptor() {
        return _wbd;
    }


    /**
     * Set the deployment descriptor object describing the contents of the
     * web application.
     *
     * @param wbd The deployment descriptor object
     */
    public void setDescriptor(WebBundleDescriptor wbd) {
        _wbd = wbd;
    }

    // --------------------------------------------------------Private metthods

    /**
     * Return the name of the application that this web module belongs
     * to or null if this is a standalone web module.
     */
    private String getAppName() {
        String name = null;
        if (_wbd != null) {
            Application app = _wbd.getApplication();
            if ((app != null) && !app.isVirtual()) {
                String appName = app.getRegistrationName();
                if ((appName != null) && (appName.length() > 0)) {
                    name = appName.trim();
                }
            }
        }
        return name;
    }

    /**
     * Return just the name of the web module.
     */
    private String getModuleName() {
        String name = null;
        if (_wmBean != null) {
            name = _wmBean.getName();
        }
        return name;
    }


    /*
     * Appends this web module's id to the given base directory path, and
     * returns it.
     *
     * @param baseDir Base directory path
     */
    private String getWebDir(String baseDir) {

        String workDir = null;

        if (baseDir != null) {
            StringBuffer dir = new StringBuffer();
            dir.append(baseDir);

            // Append the application id (if this is not a standalone web
            // module)
            String appName = getAppName();
            if (appName != null) {
                dir.append(File.separator);
                dir.append(FileUtils.makeLegalNoBlankFileName(appName));
            }

            // Append the web module id
            String name = getModuleName();
            if (name != null) {
                dir.append(File.separator);
                if (appName == null) {
                    dir.append(FileUtils.makeLegalNoBlankFileName(name));
                } else {
                    // for embedded web module, we convert the 
                    // ".war" to "_war" when needed
                    dir.append(DeploymentUtils.getRelativeEmbeddedModulePath(
                        dir.toString(), name));
                }
            }
            workDir = dir.toString();
        }

        return workDir;
    }
}
