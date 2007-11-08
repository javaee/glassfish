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

package com.sun.enterprise.instance;

import com.sun.enterprise.config.ConfigException;


/**
 * A factory to create instance specific objects. 
 */
public class InstanceFactory {

   /**
    * Returns a AppsManager object which provides convenience fuctions 
    * to read and save data related to the specified server id. 
    * 
    * @return  a AppsManager for a given configuration context
    */
    public static AppsManager createAppsManager(String serverId) 
	throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(serverId);
	return new AppsManager(env);
    }

    /**
     * Returns a AppsManager object which provides convenience fuctions 
     * to read and save data related to the specified server id. 
     * 
     * @return  a AppsManager for a given configuration context
     */
    public static AppsManager createAppsManager(String installRoot, 
	    String serverId) throws ConfigException
    {
        return createAppsManager(installRoot, serverId, true);
    }

    /**
     * Returns a AppsManager object which provides convenience fuctions 
     * to read and save data related to the specified server id. 
     * 
     * @return  a AppsManager for a given configuration context
     */
    public static AppsManager createAppsManager(String installRoot, 
	    String serverId, boolean useBackupServerXml) throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(installRoot,serverId);
	return new AppsManager(env, useBackupServerXml);
    }

    /**
     * Returns a manager object for the J2EE applicatoins. 
     * AppsManager object provides convenience fuctions 
     * to read and save data related to the given server instance. 
     *
     * @param    env    environment object for a particular server instance
     * @param    useBackupServerXml  uses back up server configuration if true 
     *
     * @return   manager object for the j2ee applications
     */
    public static AppsManager createAppsManager(InstanceEnvironment env,
            boolean useBackupServerXml) throws ConfigException {

        return new AppsManager(env, useBackupServerXml);
    }
   
    public static EjbModulesManager createEjbModuleManager(String serverId) 
	throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(serverId);
	return new EjbModulesManager(env);
    }

    public static EjbModulesManager createEjbModuleManager(String installRoot, 
	    String serverId) throws ConfigException
    {
	return createEjbModuleManager(installRoot, serverId, true);
    }

    public static EjbModulesManager createEjbModuleManager(String installRoot, 
	    String serverId, boolean useBackupServerXml) throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(installRoot,serverId);
	return new EjbModulesManager(env, useBackupServerXml);
    }

    /**
     * Returns a manager object stand alone ejb modules. It provides 
     * convenience functions to read and save data related to the given 
     * server instance.
     *
     * @param    env    environment object for a particular server instance
     * @param    useBackupServerXml  uses back up server configuration if true 
     *
     * @return   manager object for stand alone ejb modules
     */
    public static EjbModulesManager createEjbModuleManager(
            InstanceEnvironment env, boolean useBackupServerXml) 
            throws ConfigException {

        return new EjbModulesManager(env, useBackupServerXml);
    }

    public static ConnectorModulesManager createConnectorModulesManager(String serverId) 
	throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(serverId);
	return new ConnectorModulesManager(env);
    }


    // START OF IASRI 4666602
    public static ConnectorModulesManager createConnectorModuleManager(
            InstanceEnvironment env, boolean useBackupServerXml) 
            throws ConfigException {
        return new ConnectorModulesManager(env, useBackupServerXml);        
    }

    // END OF IASRI 4666602    
    public static WebModulesManager createWebModuleManager(String serverId) 
	throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(serverId);
	return new WebModulesManager(env);
    }


    public static WebModulesManager createWebModuleManager(String installRoot, 
	    String serverId) throws ConfigException
    {
	return createWebModuleManager(installRoot, serverId, true);
    }

    public static WebModulesManager createWebModuleManager(String installRoot, 
	    String serverId, boolean useBackupServerXml) throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(installRoot,serverId);
	return new WebModulesManager(env, useBackupServerXml);
    }

    public static AppclientModulesManager createAppclientModulesManager(String serverId)
	    throws ConfigException
    {
	InstanceEnvironment env = new InstanceEnvironment(serverId);
	return new AppclientModulesManager(env);
    }

    public static AppclientModulesManager createAppclientModulesManager(InstanceEnvironment env)
	    throws ConfigException
    {
	return new AppclientModulesManager(env);
    }

}
