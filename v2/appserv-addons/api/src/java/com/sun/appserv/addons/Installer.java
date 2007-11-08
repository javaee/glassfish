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

package com.sun.appserv.addons;

/**
 * <p> Name of an implementation of this interface will be put in the 
 * META-INF/services/com.sun.appserv.addons.Installer file of the addon 
 * installer jar file. 
 * 
 * SDK installer or asadmin install-addon command will instantiate this 
 * class and execute <code>install</code> method. 
 *
 * SDK uninstaller or asadmin uninstall-addon command will instantiate this 
 * class and execute <code>uninstall</code> method. 
 *
 * @see com.sun.appserv.addons.Configurator
 */
public interface Installer {

     /**
      * <p>
      * Installs the Addon on top of the appserver. Typically it 
      * will unbundle the addon installable and setup any installation
      * wide configuration or settings. The shared addon bits will be
      * unbundled into the AS_HOME/addons/<addon-name> directory.
      * <p>
      * The addon installer will also copy a configurator plugin
      * to AS_HOME/lib/addons directory. 
      * <p>
      * The addon installer will also copy any system applications
      * to AS_HOME/lib/install/applications directory. 
      * <p>
      * The <code>Installer</code> will be loaded with an instance of 
      * <code>URLClassLoader</code> and ant jars will be added to it's
      * classpath. 
      * <p>
      * If this method throws an AddonFatalException or RuntimeException
      * Appserver installation will be aborted. However if it throws
      * an AddonException, then the appserver installation will continue 
      * after displaying the error message. 
      *
      * @param ic <code>InstallationContext</code> object.
      */
     void install(InstallationContext ic) throws AddonException; 

     /**
      * <p>
      * Perform uninstallation activity. It rollbacks any change 
      * <code>Installer</code> did during the installation.
      * This include the removal of the system applications from 
      * the lib/install/applications directory also.
      * <p>
      * The <code>Installer</code> will be loaded with an instance of 
      * <code>URLClassLoader</code> and ant jars will be added to it's
      * classpath. 
      * <p>
      * If this method throws an AddonFatalException or RuntimeException
      * Appserver uninstallation will be aborted. However if it throws
      * an AddonException, then the appserver uninstallation will continue 
      * after displaying the error message. 
      *
      * @param ic <code>InstallationContext</code> object.
      */
     void uninstall(InstallationContext ic) throws AddonException; 

     /**
      * Upgrade the addon. 
      * <p>
      * If this method throws AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param ic <code>InstallationContext</code> object.
      * @param version <code>AddonVersion</code> object corresponding to the existing version.
      */
     void upgrade(InstallationContext ic, AddonVersion version) throws AddonException; 
}
