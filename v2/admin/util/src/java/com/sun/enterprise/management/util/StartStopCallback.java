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
package com.sun.enterprise.management.util;

import com.sun.enterprise.admin.server.core.mbean.config.*;
import com.sun.enterprise.management.model.*;
import com.sun.enterprise.ManagementObjectManager;
import javax.management.*;


/**
 * J2EEModuleCallBack will invoke corresponding methods in this class.
 * Instantiates the mBean of corresponding module or application and
 * invokes the start or stop method appropriately.
 *
 * @version  1.0 26mar2003
 * @author   Sreenivas Munnangi
 */
public class StartStopCallback {
    private static boolean debug = false;

    /**
     * Invokes appropriate start method based on j2ee type.
     * If trying to start a module which is part of an application
     * then it will throw exception.
     */
    public void startModule(J2EEDeployedObjectMdl modObject)
        throws MBeanException {

        if (debug) {
            return;
        }

        String moduleID = modObject.getname();
        String j2eeType = modObject.getj2eeType();
        String serverName = modObject.getJ2EEServer();

        // check if trying to start a module which is part of the application
        if ((!j2eeType.equals(ManagementObjectManager.J2EE_TYPE_APPLICATION)) &&
                (!standAlone(modObject))) {
            throw new MBeanException( 
                  new Exception(
                  "cannot start individual module, start the application itself"));
        }

        if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_EJB_MODULE)) {
            startEJBModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_WEB_MODULE)) {
            startWEBModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_RAR_MODULE)) {
            startRARModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_ACC_MODULE)) {
            return;
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_APPLICATION)) {
            startApplication(moduleID, serverName);
        }
    }

    /**
     * Invokes appropriate stop method based on j2ee type.
     * If trying to stop a module which is part of an application
     * then it will throw exception.
     */
    public void stopModule(J2EEDeployedObjectMdl modObject)
        throws MBeanException {

        if (debug) {
            return;
        }

        String moduleID = modObject.getname();
        String j2eeType = modObject.getj2eeType();
        String serverName = modObject.getJ2EEServer();

        // check if trying to stop a module which is part of the application
        if ((!j2eeType.equals(ManagementObjectManager.J2EE_TYPE_APPLICATION)) &&
                (!standAlone(modObject))) {
            throw new MBeanException(
                new Exception(
                "cannot stop individual module, stop the application itself"));
        }

        if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_EJB_MODULE)) {
            stopEJBModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_WEB_MODULE)) {
            stopWEBModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_RAR_MODULE)) {
            stopRARModule(moduleID, serverName);
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_ACC_MODULE)) {
            return;
        } else if (j2eeType.equals(ManagementObjectManager.J2EE_TYPE_APPLICATION)) {
            stopApplication(moduleID, serverName);
        }
    }

    /* Start ejb module */
    private void startEJBModule(String moduleID, String serverName) 
        throws MBeanException {
        try {
            ManagedStandaloneJ2EEEjbJarModule msejbModule = new ManagedStandaloneJ2EEEjbJarModule(serverName,
                    moduleID);
            msejbModule.start();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Stop ejb module */
    private void stopEJBModule(String moduleID, String serverName)
        throws MBeanException {

        try {
            ManagedStandaloneJ2EEEjbJarModule msejbModule = new ManagedStandaloneJ2EEEjbJarModule(serverName,
                    moduleID);
            msejbModule.stop();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Start web module */
    private void startWEBModule(String moduleID, String serverName)
        throws MBeanException {

        try {
            ManagedStandaloneJ2EEWebModule mswebModule = new ManagedStandaloneJ2EEWebModule(serverName,
                    moduleID);
            mswebModule.start();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Stop web module */
    private void stopWEBModule(String moduleID, String serverName)
        throws MBeanException {

        try {
            ManagedStandaloneJ2EEWebModule mswebModule = new ManagedStandaloneJ2EEWebModule(serverName,
                    moduleID);
            mswebModule.stop();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Start connector module */
    private void startRARModule(String moduleID, String serverName)
        throws MBeanException {

        try {
            ManagedStandaloneConnectorModule mscModule = new ManagedStandaloneConnectorModule(serverName,
                    moduleID);
            mscModule.start();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Stop connector module */
    private void stopRARModule(String moduleID, String serverName)
        throws MBeanException {

        try {
            ManagedStandaloneConnectorModule mscModule = new ManagedStandaloneConnectorModule(serverName,
                    moduleID);
            mscModule.stop();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Start application */
    private void startApplication(String appID, String serverName)
        throws MBeanException {

        try {
            ManagedJ2EEApplication mApp = new ManagedJ2EEApplication(serverName,
                    appID);
            mApp.start();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Stop application */
    private void stopApplication(String appID, String serverName)
        throws MBeanException {

        try {
            ManagedJ2EEApplication mApp = new ManagedJ2EEApplication(serverName,
                    appID);
            mApp.stop();
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    /* Is it a stand-alone module or part of application */
    private boolean standAlone(J2EEDeployedObjectMdl modObject)
        throws MBeanException {

        try {
            ObjectName objName = new ObjectName(modObject.getobjectName());
            String appName = objName.getKeyProperty("J2EEApplication");

            if ((appName == null) || (appName.length() < 1) ||
                    (appName.equals("null")) || (appName.equals("NULL")) ||
                    (appName.equals(""))) {
                return true;
            }
        } catch (Exception e) {
            throw new MBeanException(e);
        }

        return false;
    }
}
