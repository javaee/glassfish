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

//JMX imports
import javax.management.*;

//Config imports
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.AuthRealm;

//Admin imports
import com.sun.enterprise.admin.common.exception.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.AFOtherException;
import com.sun.enterprise.admin.common.ObjectNames;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
    This Config MBean represents a SecurityService.
    It extends ConfigMBeanBase class which provides get/set attribute(s) and getMBeanInfo services according to text descriptions.
    ObjectName of this MBean is:
        ias: type=security-service, instance-name=<instance-name>
*/
public class ManagedSecurityService extends ConfigMBeanBase implements ConfigAttributeName.SecurityService
{
    /** 
     * MAPLIST array defines mapping between "external" name and its location in XML relatively base node
     */
    private static final String[][] MAPLIST  =
    {
        {kDefaultRealm              , ATTRIBUTE + ServerTags.DEFAULT_REALM},
        {kDefaultPrincipal          , ATTRIBUTE + ServerTags.DEFAULT_PRINCIPAL},
        {kDefaultPrincipalPassword  , ATTRIBUTE + ServerTags.DEFAULT_PRINCIPAL_PASSWORD},
        {kAnonymousRole             , ATTRIBUTE + ServerTags.ANONYMOUS_ROLE},
        {kAuditEnabled              , ATTRIBUTE + ServerTags.AUDIT_ENABLED},
        // {kLogLevel                  , ATTRIBUTE + ServerTags.LOG_LEVEL},
    };
    /** 
     * ATTRIBUTES array specifies attributes descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   ATTRIBUTES  =
    {
        kDefaultRealm              + ", String,       RW" ,
        kDefaultPrincipal          + ", String,       RW" ,
        kDefaultPrincipalPassword  + ", String,       RW" ,
        kAnonymousRole             + ", String,       RW" ,
        kAuditEnabled              + ", boolean,      RW" ,
        // kLogLevel                  + ", String,       RW" ,
    };
    /** 
     * OPERATIONS array specifies operations descriptions in format defined for MBeanEasyConfig
     */
    private static final String[]   OPERATIONS  =
    {
        "createAuthRealm(String name, String classname), ACTION",
        "deleteAuthRealm(String id), ACTION",
        "listAuthRealms(), INFO",
    };
    
   
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ManagedSecurityService.class );
    
    /**
        Default constructor sets MBean description tables
    */
    public ManagedSecurityService() throws MBeanConfigException
    {
        this.setDescriptions(MAPLIST, ATTRIBUTES, OPERATIONS);
    }

    /**
        Constructs Config MBean for Security Service Component.
        @param instanceName The server instance name.
    */
    public ManagedSecurityService(String instanceName) throws MBeanConfigException
    {
        this(); //set description tables
        initialize(ObjectNames.kSecurityServiceType, new String[]{instanceName});
    }
    
    /**
    This operation creates AuthRealm according to attributes and adds(links) it to current SecurityService;
    If attribute is 'null' then default value will be set.
     */
    public void createAuthRealm(String name, String classname) throws ConfigException
    {
        AuthRealm authRealm = new AuthRealm();
        if(name!=null)
            authRealm.setName(name);
        if(classname!=null)
            authRealm.setClassname(classname);
        SecurityService securityService = (SecurityService)getConfigBeanByXPath( ServerXPathHelper.getSecurityServiceXpath() );
        securityService.addAuthRealm(authRealm);
        
        getConfigContext().flush();
    }
    
    /**
    This operation deletes AuthRealm according to id if it connected to current SecurityService.
    @throws ConfigException in case of failure.
     */
    public void deleteAuthRealm(String id) throws ConfigException, MBeanException, AttributeNotFoundException
    {
        SecurityService securityService = (SecurityService)getConfigBeanByXPath( ServerXPathHelper.getSecurityServiceXpath() );
        AuthRealm authRealm = securityService.getAuthRealmByName(id);
		if(((String) getAttribute(kDefaultRealm)).equals(id)){
			String msg = localStrings.getString( "admin.server.core.mbean.config.default_realm_cannot_delete", id );
		  throw new ConfigException( msg );
		}
  
        if(authRealm!=null)
            securityService.removeAuthRealm(authRealm);
        getConfigContext().flush();
    }

    /**
    This operation returns list of AuthRealm's ids  connected to current SecurityService.
     */
    public String[] listAuthRealms() throws ConfigException
    {
        SecurityService securityService = (SecurityService)getConfigBeanByXPath( ServerXPathHelper.getSecurityServiceXpath() );
        AuthRealm[]     authRealms  = securityService.getAuthRealm();
        String[]        res        = new String[authRealms.length];
        for(int i=0; i<authRealms.length; i++)
        {
            res[i] = authRealms[i].getName();
        }
        return res;
    }
}






