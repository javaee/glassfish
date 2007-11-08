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
 * Name of an implementation of this interface will be configured in the 
 * META-INF/services/com.sun.appserv.addons.Configurator file. Appserver
 * will instantiate this class and use it for configuring and unconfiguring
 * the addon.
 *
 * @see com.sun.appserv.addons.Installer
 * @author Binod P.G
 * @since 9.1
 */
public interface Configurator {

     /**
      * Performs the configuration of the addon. Typically it involves
      * the creation of instance/domain specific artifacts and configuring
      * the domain.xml, server.policy etc.
      * <p>
      * When ConfigurationContext.ConfigurationType is DAS, the configurator
      * is supposed to make any changes required for the domain. For example,
      * it can insert a lifecycle-module element in the domain.xml with 
      * object-type="system-all", so that the lifecycle module will be 
      * available in all instances created in that domain.
      * <p>
      * The configurator is expected to use the system property called
      * com.sun.aas.AddonRoot for referring to the shared bits of the addon.
      * For example, the location of the system-all lifecycle module may be 
      * specified as ${com.sun.aas.AddonRoot}/<addon-name>/lib/lc-impl.jar
      * <p>
      * When ConfigurationContext.ConfigurationType is INSTANCE, the 
      * configurator is expected to make any changes required for a particular 
      * instance. This may be creating a system-propery to specify a 
      * port number.
      * <p>
      * If this method throw a AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param cc <code>ConfigurationContext</code> object.
      */
     void configure(ConfigurationContext cc) throws AddonException; 

     /**
      * Performs the unconfiguration of the addon. Typically it involves
      * the removal of instance/domain specific artifacts and deleting 
      * the domain.xml entries. For example, all application-refs pointing
      * to the system application or  may be removed by the addon.
      * <p>
      * If this method throw a AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param cc <code>ConfigurationContext</code> object.
      */
     void unconfigure(ConfigurationContext cc) throws AddonException; 

     /**
      * Disable the addon. Typically, addon will modify some configuration
      * (eg: Lifecycle module) to stop it from functioning. If the addon
      * has deployed an application, then the application will be disabled
      * in this step.
      * <p>
      * If this method throw a AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param cc <code>ConfigurationContext</code> object.
      */
     void disable(ConfigurationContext cc) throws AddonException; 

     /**
      * Enable the addon. Typically, addon will modify some configuration
      * (eg: Lifecycle module) to start functioning. If the addon has disabled
      * and application, then addon will re-enable that application in 
      * this step.
      * <p>
      * If this method throw a AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param cc <code>ConfigurationContext</code> object.
      */
     void enable(ConfigurationContext cc) throws AddonException; 


     /**
      * Upgrade the addon. 
      * <p>
      * If this method throws AddonFatalException or RuntimeException, the 
      * appserver startup will not continue. If it throws AddonException,
      * then the appserver startup will continue after displaying the 
      * error message.
      *
      * @param cc <code>ConfigurationContext</code> object.
      * @param version <code>AddonVersion</code> object corresponding to the existing version.
      */
     void upgrade(ConfigurationContext cc, AddonVersion version) throws AddonException; 
}
