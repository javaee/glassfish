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

package com.sun.enterprise.admin.server.core.mbean.config;

import com.sun.enterprise.admin.common.exception.J2EEWebModuleException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.J2eeApplication;


//Admin Imports
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

/**
    Represents a managed Web Module. This is a part of a deployed application.
    When a J2EE application is deployed, instances of this MBean are registered
    in the MBeanServer.
    <p>
    There will be as many instances of this MBean as there are Web Modules (per
    application).
    <p>
    ObjectName of this MBean is:
        ias:type=J2EEWebModule, AppName= <appName>, ModuleName=<moduleName>
*/
public class ManagedJ2EEWebModule extends ConfigMBeanBase
{
    private static final String[][] MAPLIST    = null;
    private static final String[]   ATTRIBUTES  = null;
    private static final String[]   OPERATIONS  = 
    {
        "getServlets(),   INFO",
    };

    
    /**
        Default constructor sets MBean description tables
    */
    public ManagedJ2EEWebModule() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    public ManagedJ2EEWebModule(String instanceName, String applicationName, String moduleName)
        throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kWebModule, new String[]{instanceName, applicationName, moduleName});

    }

    public String[] getServlets() throws J2EEWebModuleException
    {
        ConfigMBeanNamingInfo namingInfo = this.getConfigMBeanNamingInfo();
        String [] locParams = namingInfo.getLocationParams();
        String applicationName = locParams[1];
        String moduleName      = locParams[2];

        try
        {
            J2eeApplication app = (J2eeApplication) this.getConfigBeanByXPath(ServerXPathHelper.getAppIdXpathExpression(applicationName));
            String location = app.getLocation();
            return ModulesXMLHelper.getServletsForWebModule(location, moduleName);
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "getServlets", e);
            throw new J2EEWebModuleException(e.getMessage());
        }
    }

	public String[] getJSPPages() throws J2EEWebModuleException
    {
        return ( null );
    }


	public int getModuleType()
	{
		return (  AdminConstants.kTypeWebModule );
	}
    //all the other ias-web.xml parameters that are exposed.
}
