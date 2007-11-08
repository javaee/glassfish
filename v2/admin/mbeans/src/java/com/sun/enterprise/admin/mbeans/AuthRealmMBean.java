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

/*
 * $Id: AuthRealmMBean.java,v 1.5 2006/09/26 04:41:29 ne110415 Exp $
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

//JMX imports
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.AttributeNotFoundException;
import javax.management.modelmbean.ModelMBeanInfo;


import com.sun.enterprise.admin.common.constant.AdminConstants;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.PropertyResolver;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import com.sun.enterprise.admin.common.constant.AdminConstants;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

//security stuff
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;
import com.sun.enterprise.security.util.IASSecurityException;

//event handling
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.UserMgmtEvent;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.admin.MBeanHelper;

// Logging
import java.util.logging.Level;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.mbeanapi.IAuthRealmMBean;

public class AuthRealmMBean extends BaseConfigMBean
    implements IAuthRealmMBean {

    static final String FILE_NAME_PROPERTY = "file";
    final private static String ADMIN_REALM = SystemPropertyConstants.ADMIN_REALM;

    private static final StringManager localStrings = 
        StringManager.getManager(AuthRealmMBean.class);

    public AuthRealmMBean() { }

    // ****************************************************************************
    //Security Realms keyfile operations
    // ****************************************************************************
    /**
     * Returns names of all the users from instance realm keyfile
     */
    public String[] getUserNames() throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        FileRealm realm = getRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getUserNames());
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns names of all the groups from the instance realm keyfile
     */
    public String[] getGroupNames() throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        FileRealm realm = getRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getGroupNames());
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns the name of all the groups that this user belongs to from the instance realm keyfile
     */
    public String[] getUserGroupNames(String userName) throws MBeanConfigException
    {
        if(userName==null)
           return getGroupNames();

        checkFileTypeRealm();
        
        FileRealm realm = getRealmKeyFile();
        try
        {
            return convertEnumerationToStringArray(realm.getGroupNames(userName));
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
    }


    /**
     * Adds new user to file realm. User cannot exist already.
     */
    public void addUser(String userName, String password, String[] groupList) 
                throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        FileRealm realm = getRealmKeyFile();
        try
        {
            realm.addUser(userName, password, groupList);
            saveInstanceRealmKeyFile(realm);
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(IASSecurityException ise)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.security_exception", mInstanceName, userName, bre.getMessage());
            throw new MBeanConfigException(ise.getMessage());
        }

        EmitUserMgmtEvent(UserMgmtEvent.ACTION_USERADD, userName, groupList);

    }

    /**
     * Remove user from file realm. User must exist.
     */
    public void removeUser(String userName) throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        if(isLastAdminUser(userName))
        {
            String msg = localStrings.getString("authRealmMBean.removeLastAdminUser",
                                        AdminConstants.DOMAIN_ADMIN_GROUP_NAME);
            throw new MBeanConfigException(msg);
        }
            
        FileRealm realm = getRealmKeyFile();
        try
        {
            realm.removeUser(userName);
            saveInstanceRealmKeyFile(realm);
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
        EmitUserMgmtEvent(UserMgmtEvent.ACTION_USERREMOVE, userName, null);
    }

    /**
     * Update data for an existing user. User must exist. This is equivalent to calling removeUser() followed by addUser().
     */
    public void updateUser(String userName, String password, String[] groupList) throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        if(!isBelogsTo(groupList, AdminConstants.DOMAIN_ADMIN_GROUP_NAME) &&
           isLastAdminUser(userName))
        {
            String msg = localStrings.getString("authRealmMBean.removeLastAdminUser",
                                        AdminConstants.DOMAIN_ADMIN_GROUP_NAME);
            throw new MBeanConfigException(msg);
        }

        FileRealm realm = getRealmKeyFile();
        try
        {
            realm.updateUser(userName, userName, password, groupList);
            saveInstanceRealmKeyFile(realm);
	    logDetailsIfAdminRealm();
        }
        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(NoSuchUserException nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
        catch(IASSecurityException ise)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.security_exception", mInstanceName, userName, bre.getMessage());
            throw new MBeanConfigException(ise.getMessage());
        }
        EmitUserMgmtEvent(UserMgmtEvent.ACTION_USERUPDATE, userName, groupList);
    }
    
    // ****************************************************************************
    private String getRealmKeyFileName()
    {
        return getProperty(FILE_NAME_PROPERTY); 
    }
    // ****************************************************************************
    private FileRealm getRealmKeyFile() throws MBeanConfigException
    {
        try
        {
            /*
            String name = (String)getAttribute(ServerTags.NAME);
            Realm realm =  Realm.getInstance(name);
            if(realm==null)
            {
                String msg = localStrings.getString("authRealmMBean.realm_not_registered", name);
                throw new MBeanConfigException(msg);
            }
            if(!(realm instanceof FileRealm))
            {
                String msg = localStrings.getString("authRealmMBean.unsupported_type");
                throw new MBeanConfigException(msg);
            }
            return (FileRealm)realm;
            */
            return new FileRealm(getRealmKeyFileName());
        }
 //       catch(MBeanConfigException mce)
 //       {
 //           throw mce;
 //       }
        catch(Exception e)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(e.getMessage());
        }
/*        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
        catch(NoSuchRealmException nsr)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_realm", mInstanceName);
            throw new MBeanConfigException(nsr.getMessage());
        }
*/
    }

    // ****************************************************************************
    private void saveInstanceRealmKeyFile(FileRealm realm) throws MBeanConfigException
    {
        try
        {
            final String filePath = getRealmKeyFileName();
            _sLogger.log(Level.INFO, "filerealm.write", filePath);
            realm.writeKeyFile(filePath);
        }
        catch(IOException ioe)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.realm_io_error", mInstanceName);
            _sLogger.log(Level.WARNING, "filerealm.writeerror", ioe);
            throw new MBeanConfigException(ioe.getMessage());
        }
/*        catch(BadRealmException bre)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.bad_realm", mInstanceName);
            throw new MBeanConfigException(bre.getMessage());
        }
*/
 }

    // ****************************************************************************
    private String[] convertEnumerationToStringArray(Enumeration ee)
    {
        ArrayList list = new ArrayList();
        while(ee.hasMoreElements())
            list.add(ee.nextElement());
        return (String[])list.toArray(new String[list.size()]);
    }
    // ****************************************************************************
    private void checkFileTypeRealm() throws MBeanConfigException
    {
        String className = null;
        try 
        {
            className = (String)getAttribute(ServerTags.CLASSNAME);
        } 
        catch (Exception e) 
        {}
        if( className==null || 
          !className.equals("com.sun.enterprise.security.auth.realm.file.FileRealm"))
        {
            String msg = localStrings.getString("authRealmMBean.unsupported_type");
            throw new MBeanConfigException(msg);
        }
    }

    /**
     *
     */
    private String getProperty(String name)
    {
        try 
        {
            String unresolved = (String)invoke("getPropertyValue", new Object[]{name}, new String[]{"java.lang.String"});
            //This needs to be changed for SE/EE. The instanceName cant
            //be assumed to be the das server instance name.
            final String instanceName = MBeanRegistryFactory.getAdminContext().
                    getServerName();
            final String resolved =  new PropertyResolver(getConfigContext(), 
                    instanceName).resolve(unresolved);
            return resolved;
        } 
        catch (Exception e) 
        {
            return null;
        }
    }
    
    private void EmitUserMgmtEvent(int action, String user, String[] groups) throws MBeanConfigException
    {
        try
        {
            AdminContext adminContext = MBeanRegistryFactory.getAdminContext();
            String instanceName = adminContext.getServerName();
            String name = (String)getAttribute(ServerTags.NAME);
            UserMgmtEvent event = new UserMgmtEvent(instanceName, name, action, user, groups);
            event.setTargetDestination(getParentConfigName());
            EventContext.addEvent(event);
        } 
        catch (Exception e) 
        {
            throw new MBeanConfigException(e.getMessage());
        }
    }

    /**
     * Check if this is the last admin user in DAS admin-auth
     */
    private boolean isSystemAdminRealm() 
            throws MBeanConfigException
    {
        try {
            Server das = ServerHelper.getDAS(getConfigContext());
            String dasConfigName = das.getConfigRef();
            if(!getParentConfigName().equals(dasConfigName))
                return false; //from another config

            JmxConnector jmxc = ServerHelper.getServerSystemConnector(
                    getConfigContext(),
                    das.getName());
            return jmxc.getAuthRealmName().equals(getName());
        } catch (Exception e) {
            throw new MBeanConfigException(e.getMessage());
        }
    }
    
    /**
     * @returns name of config element which realm belongs to
     */
    private String getParentConfigName()  throws Exception
    {
        return MBeanHelper.getLocation((ModelMBeanInfo)this.getMBeanInfo())[1];
    }
    
    //returns realm name (id)
    private String getName()  throws Exception
    {
        return (String)getAttribute(ServerTags.NAME);
    }
    // ****************************************************************************
    private boolean isBelogsTo(Enumeration ee, Object objectToCheck)
    {
        while(ee.hasMoreElements())
            if(ee.nextElement().equals(objectToCheck))
                return true;
        return false;
    }
    // ****************************************************************************
    private boolean isBelogsTo(Object[] arr, Object objectToCheck)
    {
        for(int i=0; i<arr.length; i++)
            if(objectToCheck.equals(arr[i]))
                return true;
        return false;
    }

    
    /**
     * 
     */
    private boolean isUserLastInGroup(String userName, String groupName) throws MBeanConfigException
    {
        checkFileTypeRealm();
        
        FileRealm realm = getRealmKeyFile();

        try
        {
            if(!isBelogsTo(realm.getGroupNames(userName), groupName))
                return false; // user not in the group at all
            
            Enumeration users = realm.getUserNames();
            while(users.hasMoreElements())
            {
                String user = (String)users.nextElement();
                if(!userName.equals(user) &&
                    isBelogsTo(realm.getGroupNames(user), groupName))
                   return false;
            }
        }
        catch(Exception nse)
        {
            //String msg =  localStrings.getString( "admin.server.core.mbean.config.no_such_user", mInstanceName, userName);
            throw new MBeanConfigException(nse.getMessage());
        }
        return true;
    }

    /**
     * 
     */
    private boolean isLastAdminUser(String userName) throws MBeanConfigException
    {
        return (isSystemAdminRealm() &&
            isUserLastInGroup(userName,AdminConstants.DOMAIN_ADMIN_GROUP_NAME));            
    }

     private void logDetailsIfAdminRealm() throws MBeanConfigException {
         try
         {
             if (isAdminRealm()) {
                 //choosing WARNING as the log-level
                 final ConfigContext cc = super.getConfigContext();
                 final String nas = NodeAgentHelper.getNodeAgentsAsString(NodeAgentHelper.getNodeAgentsInDomain(cc));
                 final String sas = ServerHelper.getServersAsString(ServerHelper.getServersInDomainExcludingDAS(cc));
                 String msg, params[];
                 if (isOk(nas) && isOk(sas)) {
                     params = new String[]{ADMIN_REALM, nas, sas};
                     msg = localStrings.getString("admin.password.change_all_msg", params);
                     _sLogger.log(Level.WARNING, msg, params);
                 } else if (isOk(nas) && !isOk(sas)) {
                     params = new String[]{ADMIN_REALM, nas};
                     msg = localStrings.getString("admin.password.change_das_na_msg", params);
                     _sLogger.log(Level.WARNING, msg, params);
                 } else if (!isOk(nas) && !isOk(sas)) {
                     params = new String[]{ADMIN_REALM};
                     msg = localStrings.getString("admin.password.change_das_alone_msg", params);
                     _sLogger.log(Level.WARNING, msg, params);
                 }
             }
         } catch(final Exception e)
         {
            throw new RuntimeException(e);
         }
     }

     private boolean isAdminRealm() throws Exception {
         final String name = (String) getAttribute(ServerTags.NAME);
         if (ADMIN_REALM.equals(name))
             return ( true ) ;
         return ( false );
     }
     private boolean isOk(final String s) {
         return ( s != null && s.length() != 0 );
     }
}
