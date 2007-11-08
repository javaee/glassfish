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

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.J2eeApplication;


//Admin Imports
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.exception.J2EEEjbJarModuleException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.server.core.mbean.config.naming.ConfigMBeanNamingInfo;

/**
    Represents a Ejb Jar Module within an app. This is a part of a deployed
    application. When a J2EE application is deployed, instances of this MBean
    are registered in the MBeanServer.
    <p>
    There will be as many instances of this MBean as there are Ejb Modules (per
    application).
    <p>
    ObjectName of this MBean is:
        ias:type=J2EEEjbJarModule, AppName= <appName>, ModuleName=<moduleName>

*/

public class ManagedJ2EEEjbJarModule extends ConfigMBeanBase
{
    private static final String[][] MAPLIST    = null;
    private static final String[]   ATTRIBUTES  = null;
    private static final String[]   OPERATIONS  = 
    {
        "getEnterpriseBeans(),   INFO",
        "getSessionEJBs(),   INFO",
        "getEntityEJBs(),   INFO",
    };

    /**
        Default constructor sets MBean description tables
    */
    public ManagedJ2EEEjbJarModule() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    public ManagedJ2EEEjbJarModule(String instanceName, String applicationName, String moduleName)
        throws MBeanConfigException
    {
        this(instanceName, applicationName, moduleName, null);
    }

    public ManagedJ2EEEjbJarModule(String instanceName, String applicationName, String moduleName, AdminContext adminContext)
        throws MBeanConfigException
    {
        this(); //set description tables
        setAdminContext(adminContext);
        initialize(ObjectNames.kEjbModule, new String[]{instanceName, applicationName, moduleName});
    }

    public String[] getEnterpriseBeans() throws J2EEEjbJarModuleException
    {
        return getBeansByType(ModulesXMLHelper.EJB_TYPE_ALL);
    }

    public String[] getSessionEJBs() throws J2EEEjbJarModuleException
    {
        return getBeansByType(ModulesXMLHelper.EJB_TYPE_SESSION);
    }

    public String[] getEntityEJBs() throws J2EEEjbJarModuleException
    {
        return getBeansByType(ModulesXMLHelper.EJB_TYPE_ENTITY);
    }

    private String[] getBeansByType(int ejbType) throws J2EEEjbJarModuleException
    {
        ConfigMBeanNamingInfo namingInfo = this.getConfigMBeanNamingInfo();
        String [] locParams = namingInfo.getLocationParams();
        String applicationName = locParams[1];
        String moduleName      = locParams[2];
        
        
        try
        {
            J2eeApplication app = (J2eeApplication) this.getConfigBeanByXPath(ServerXPathHelper.getAppIdXpathExpression(applicationName));
            String location = app.getLocation();
            return ModulesXMLHelper.getEnterpriseBeansForEjbModule(location, moduleName, ejbType);
        }
        catch (Exception e)
        {
            sLogger.throwing(getClass().getName(), "getBeansByType", e);
            throw new J2EEEjbJarModuleException(e.getMessage());
        }
    }

    public int getModuleType()
    {
            return (  AdminConstants.kTypeEjbModule );
    }

    //all the other ias-ejb-jar.xml parameters that are exposed.
}
