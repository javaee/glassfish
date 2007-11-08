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
 * $Id: ConnectorConnectionPoolMBean.java,v 1.5 2007/02/14 04:11:31 bnevins Exp $
 * Author: Vishal Byakod (vb130608)
 */

package com.sun.enterprise.admin.mbeans;

//jdk imports
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.ArrayList;


//JMX imports
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.MBeanInfo;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.SecurityMap;
import com.sun.enterprise.config.serverbeans.BackendPrincipal;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.Resources;

import com.sun.enterprise.admin.MBeanHelper;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.ConfigBeanHelper;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.mbeanapi.IConnectorConnectionPoolMBean;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.ConfigTarget;

public class ConnectorConnectionPoolMBean extends BaseConfigMBean
    implements IConnectorConnectionPoolMBean
{
    
    private static final String USER_NAME      = "user_name";
    private static final String PASSWORD       = "password";
    private static final String NAME            = "name";
    private static final String PRINCIPAL       = "principal";
    private static final String USER_GROUP      = "user_group";
    private static final String POOL_NAME      = "pool_name";
    private static final String MAP_NAME      = "map_name";
    private static final String VERBOSE      = "verbose";
    
    private static final String ADD_PRINCIPALS  = "add_principals";
    private static final String REMOVE_PRINCIPALS = "remove_principals";
    private static final String ADD_USER_GROUPS = "add_user_groups";
    private static final String REMOVE_USER_GROUPS = "remove_user_groups";
    
    //operations 
    private static final String CREATE_SECURITY_MAP = "createSecurityMap";
    private static final String CREATE_BACKEND_PRINCIPAL 
                                                    ="createBackendPrincipal";
    private static final String GET_SECURITY_MAP = "getSecurityMap";
    private static final String GET_SECURITY_MAP_BY_NAME = "getSecurityMapByName";
    private static final String GET_BACKEND_PRINCIPAL = "getBackendPrincipal";
    private static final String GET_POOL_BY_NAME 
                                        = "getConnectorConnectionPoolByName";
    private static final String DEFAULT_TARGET = "domain";
    private static final String GET_CONNECTOR_POOLS 
                                                = "getConnectorConnectionPool";	
    
    private static final String CONFIG = "config";
    private static final String POOL_TYPE = "connector-connection-pool";
    private static final String MAP_TYPE = "security-map";
    private static final String RES_TYPE = "resources";
   
    // Exception strings....
    private static final String POOL_DOES_NOT_EXISTS 
                                    ="admin.mbeans.ccpmb.pool_does_not_exists";
    private static final String MAP_DOES_NOT_EXISTS
                                    ="admin.mbeans.ccpmb.map_does_not_exists";
    private static final String MAP_NAME_EXISTS
                                        ="admin.mbeans.ccpmb.map_name_exists";
    private static final String PRINCIPAL_USERGPS_NULL 
                                ="admin.mbeans.ccpmb.principals_usergroups_null";
    private static final String PRINCIPAL_EXISTS
                                        ="admin.mbeans.ccpmb.principal_exists";
    private static final String USERGROUP_EXISTS
                                        ="admin.mbeans.ccpmb.usergroup_exists";
    private static final String SAME_PRINCIPAL_VALUES 
                                    ="admin.mbeans.ccpmb.same_principal_values";
    private static final String SAME_USERGROUP_VALUES
                                    ="admin.mbeans.ccpmb.same_usergroup_values";
    private static final String PRINCIPAL_DOES_NOT_EXISTS 
                                ="admin.mbeans.ccpmb.principal_does_not_exists";
    private static final String USERGROUP_DOES_NOT_EXISTS
                                ="admin.mbeans.ccpmb.usergroup_does_not_exists";
    private static final String OPERATION_NOT_SUPPORTED 
                                ="admin.mbeans.ccpmb.operation_not_supported";
    private static final String USER_NAME_NULL 
                                        ="admin.mbeans.ccpmb.user_name_null";
    private static final String PRINCIPALS_USERGROUPS_NULL
                                ="admin.mbeans.ccpmb.principals_usergroups_will_be_null";
    
    private static StringManager localStrings =
    StringManager.getManager( ConnectorConnectionPoolMBean.class );
   
    
   /**
   */
    
    public ConnectorConnectionPoolMBean() {
        super();
        
    }

    public void createSecurityMap(AttributeList attrList)
            throws Exception{
        // Overriding the BaseConfigMBean operation so that this operation is
        // not called by the user, as creation of security map requires 
        // backend-principal username and password also.
        String msg = localStrings.getString(OPERATION_NOT_SUPPORTED);    
        throw new Exception(msg);
    }
      
    
    /* Creates a security map for the specified connection pool. 
      * @param AttributeList - this list contains all the parameters to create 
      * a new map
     */
     public ObjectName createSecurityMap(AttributeList attrList,String userName,
     String password ,String tgtName ) 	throws MBeanException {
        ObjectName mbean = null;
        String poolName = null;
        String mapname = null;
        
        try {
            if (tgtName == null || tgtName.equals("")) tgtName = DEFAULT_TARGET;
            
            MBeanServer server = getMBeanServer();
            String principals[] = null;
            String usergroups[] = null;
            
            if(attrList != null){
                int s = attrList.size();
                for(int i=0;i<s;i++){
                    try {
                        Attribute attribute =(Attribute)attrList.get(i);
                        if(isAttrNameMatch(attribute, NAME)){
                             mapname =(String) attribute.getValue();
                             continue;
                         }
                         if (isAttrNameMatch(attribute, PRINCIPAL))
                            principals = (String[])attribute.getValue();
                         if (isAttrNameMatch(attribute, USER_GROUP))
                            usergroups =(String[]) attribute.getValue();
                         if (isAttrNameMatch(attribute, POOL_NAME))
                            poolName = (String)attribute.getValue();  
                    } catch(Exception e){
                        throw new Exception("failed while getting attribute" +
                        "names and values");
                    }    
                }    
            }
            
           if(!doesPoolNameExists(poolName)){
                String msg = localStrings.getString(POOL_DOES_NOT_EXISTS,poolName);
                throw new Exception(msg);
            }
        
            if(doesMapNameExists(poolName,mapname)){
                String msg = localStrings.getString(MAP_NAME_EXISTS
                                                        ,mapname,poolName);    
                throw new Exception(msg);
                
            }
            
            //check if backend-principal user name is null
            if(userName == null){
                String msg = localStrings.getString(USER_NAME_NULL);    
                throw new Exception(msg);
            }
               
            // check if both principals and usergroups are null .If yes throw
            // exception since atleast one of these is required.
            if(principals == null && usergroups == null){
                String msg = localStrings.getString(PRINCIPAL_USERGPS_NULL);    
                throw new Exception(msg);
            }
            
            //get all the security maps for this pool.....
            ObjectName[] maps = getAllSecurityMapsForPool(poolName);
            
            if(principals != null){    
                for(int i=0;i<principals.length;i++){
                    if (isPrincipalExisting(principals[i],maps)){
                        String msg = localStrings.getString
                        (PRINCIPAL_EXISTS,principals[i],poolName);    
                        throw new Exception(msg);
                        
                    } 
                }	
            }
            if(usergroups != null){    
                for(int i=0;i<usergroups.length;i++){
                    if (isUserGroupExisting(usergroups[i],maps)){
                        String msg = localStrings.getString
                        (USERGROUP_EXISTS,usergroups[i],poolName);    
                        throw new Exception(msg);
                    } 
                }	
            }
         
           //This is a temporary fix for 8.0 pe.Currently there is no way you
            // can create a security map and its a backend principal in a atomic
            // manner . This would need two invoke operations on two different MBeans
            // and hence events would be fired as soon as the first invoke operation
            // completes which causes a NPE being thrown in the backend while accessing
            // the backend principal.
            //The direct call to config beans would be used until we find a better solution
            // to handle this problem.
            ConfigContext serverContext = getConfigContext(); 
             com.sun.enterprise.config.serverbeans.Resources resourcesBean =
                (Resources)ConfigBeansFactory.getConfigBeanByXPath(serverContext,
                                            ServerXPathHelper.XPATH_RESOURCES);
            com.sun.enterprise.config.serverbeans.ConnectorConnectionPool connPool =
                    resourcesBean.getConnectorConnectionPoolByName(poolName);
            com.sun.enterprise.config.serverbeans.BackendPrincipal backEndPrincipal
                = new com.sun.enterprise.config.serverbeans.BackendPrincipal();
            
            backEndPrincipal.setUserName(userName);
            
            if(password != null)
                backEndPrincipal.setPassword(password);
            
            com.sun.enterprise.config.serverbeans.SecurityMap securityMap = 
                new com.sun.enterprise.config.serverbeans.SecurityMap();
            
            if(backEndPrincipal != null)
                securityMap.setBackendPrincipal(backEndPrincipal);
            
            if(mapname !=null)
                securityMap.setName(mapname);
            
            if(principals != null)        
                securityMap.setPrincipal(principals);
            
            if(usergroups != null)
              securityMap.setUserGroup(usergroups);
            
            connPool.addSecurityMap(securityMap);
       }/*catch (MBeanException me){
	   throw me;
	}*/catch (Exception e) {
            e.printStackTrace(); 
            throw new MBeanException(e);
        }
        return mbean;
    }
    
    /* Updates a existing Security Map of the specified connection pool. 
     * @param AttributeList - this list contains all the parameters to update a
     * existing map.
     */
 
      public boolean updateSecurityMap(AttributeList attrList,String tgtName ) 
	throws MBeanException {
        
        String mapname = null;
        String poolname = null;
        String username = null;
        String password = null;
        String[] addPrincipals = null;
        String[] addUserGroups = null;
        String[] removePrincipals = null;
        String[] removeUserGroups = null;
        boolean status = false;
        //ConfigContext ctx = getConfigContext();
        
        try {
		if (tgtName == null || tgtName.equals("")) tgtName = DEFAULT_TARGET;
                    
            //get all the values from the attribute list ...
            if(attrList != null){
                int s = attrList.size();
                for(int i=0;i<s;i++){
                    try{
                        Attribute attribute =(Attribute)attrList.get(i);
                        if((isAttrNameMatch(attribute, POOL_NAME)))
                            poolname = (String)attribute.getValue();
                        if((isAttrNameMatch(attribute, NAME)))
                            mapname = (String)attribute.getValue();
                        if((isAttrNameMatch(attribute, USER_NAME)))
                            username = (String)attribute.getValue();    
                        if((isAttrNameMatch(attribute, PASSWORD)))
                            password = (String)attribute.getValue();    
                        if((isAttrNameMatch(attribute, ADD_PRINCIPALS)))
                            addPrincipals =
                                    getOptionsList((String)attribute.getValue());    
                        if((isAttrNameMatch(attribute, ADD_USER_GROUPS)))
                            addUserGroups = 
                                    getOptionsList((String)attribute.getValue());    
                        if((isAttrNameMatch(attribute, REMOVE_PRINCIPALS)))
                            removePrincipals =
                                    getOptionsList((String)attribute.getValue());    
                        if((isAttrNameMatch(attribute, REMOVE_USER_GROUPS)))
                            removeUserGroups = 
                                    getOptionsList((String)attribute.getValue()); 
                    }catch(Exception e){
                        e.printStackTrace();    
                    }    
                }    
            }
            if(!doesPoolNameExists(poolname)){
                String msg = localStrings.getString(POOL_DOES_NOT_EXISTS,poolname);
                throw new Exception(msg);
            }
            
            if(!doesMapNameExists(poolname,mapname)){
                String msg = localStrings.getString(MAP_DOES_NOT_EXISTS
                                                            ,mapname,poolname);
                throw new Exception(msg);
            }           
            //get all the security maps for this pool.....
            ObjectName[] maps = getAllSecurityMapsForPool(poolname);
                        
            //check if addPrincipals and removePrincipals have the same value
            if(addPrincipals != null && removePrincipals != null){
                for(int i=0; i < addPrincipals.length ; i++) {
                    for (int j=0; j < removePrincipals.length; j++) {
                        if(removePrincipals[j].equals(addPrincipals[i])){
                            String msg = localStrings.getString(
                                    SAME_PRINCIPAL_VALUES,addPrincipals[i]);
                            throw new Exception(msg);
                        }
                                       
                    }    
                }
            }
            
            //check if addUserGroups and removeUserGroups have the same value
            if(addUserGroups != null && removeUserGroups != null){
                for(int i=0; i < addUserGroups.length ; i++) {
                    for (int j=0; j < removeUserGroups.length; j++) {
                        if(removeUserGroups[j].equals(addUserGroups[i])){
                            String msg = localStrings.getString(
                            SAME_USERGROUP_VALUES,addUserGroups[i]);
                            throw new Exception(msg);
                        }
                                       
                    }    
                }
            }
            // make sure that the principals to be added are not existing in any map ...
            if(addPrincipals != null){    
                for(int i=0;i<addPrincipals.length;i++){
                    if (isPrincipalExisting(addPrincipals[i],maps)){
                        String msg = localStrings.getString(PRINCIPAL_EXISTS,
                                                    addPrincipals[i],poolname);
                        throw new Exception(msg);
                    }
                }	
            }
            // make sure that the user groups to be added are not existing in any map ...
            if(addUserGroups != null){    
                for(int i=0;i<addUserGroups.length;i++){
                    if (isUserGroupExisting(addUserGroups[i],maps)){
                     String msg = localStrings.getString(USERGROUP_EXISTS
                        ,addUserGroups[i],poolname);
                        throw new Exception(msg);
                    }
                }	
            }
            //get a reference to the MBean server ...  
            MBeanServer server = getMBeanServer();
            ObjectName mbean = getSecurityMapObjectName(mapname,poolname);
            String existingPrincipals[] = null;
            String existingUserGroups[] = null;
            
            existingPrincipals =(String[]) server.getAttribute(mbean,PRINCIPAL);
            existingUserGroups =(String[]) server.getAttribute(mbean,USER_GROUP);       
           
            ArrayList source = null;
            ArrayList source1 = null;
            
            if(existingPrincipals != null){
                source = new ArrayList(existingPrincipals.length);
                for(int i=0; i<existingPrincipals.length ; i++) 
                    source.add(existingPrincipals[i]);
            }
            if(existingUserGroups != null){
                source1 = new ArrayList(existingUserGroups.length);
                for(int i=0; i<existingUserGroups.length ; i++) 
                    source1.add(existingUserGroups[i]);
            }
            //check if there is any invalid principal in removePrincipals.
            if(removePrincipals != null){
                for(int i=0;i<removePrincipals.length;i++){
                    String s = removePrincipals[i];
                    if (!source.contains(s)){
                        String msg =
                            localStrings.getString(PRINCIPAL_DOES_NOT_EXISTS,s,poolname);    
                        throw new Exception(msg);
                    }
                }
            }
            //check if there is any invalid usergroup in removeUserGroups.
            if(removeUserGroups != null){
                for(int i=0;i<removeUserGroups.length;i++){
                    String s = removeUserGroups[i];
                    if (!source1.contains(s)){
                        String msg =
                        localStrings.getString(USERGROUP_DOES_NOT_EXISTS,s,poolname);    
                        throw new Exception(msg);
                    }
                }
            }
           
            //FIX : Bug 4914883.
            //The user should not delete all principals and usergroups in the map.
            // Atleast one principal or usergroup must exists.
                        
            if(addPrincipals == null && addUserGroups == null ) {
                boolean principalsEmpty = false;
                boolean userGroupsEmpty = false;	
                
                if(removePrincipals == null && existingPrincipals.length==0)
                    principalsEmpty = true;
                if(removeUserGroups == null && existingUserGroups.length==0)
                    userGroupsEmpty = true;
                
                if (( removePrincipals != null )&&
                            (removePrincipals.length== existingPrincipals.length))
                    principalsEmpty = true;
                
                if (( removeUserGroups != null ) &&
                            (removeUserGroups.length== existingUserGroups.length))
                    userGroupsEmpty = true;	
                if (userGroupsEmpty && principalsEmpty) {
                   String msg = localStrings.getString(PRINCIPALS_USERGROUPS_NULL);    
                   throw new Exception(msg);

                }
            }	
         
            //add principals to the source arraylist.
            if(addPrincipals != null){
                for(int i=0; i < addPrincipals.length; i++) {
                    String s= addPrincipals[i];
                    if (!source.contains(s)) 
                      source.add(s);
                    else{
                        String msg = localStrings.getString(PRINCIPAL_EXISTS
                                                                ,s,poolname);    
                        throw new Exception(msg);
                    }    
                }        
            }    
            //removing principals from source arraylist.
            if(removePrincipals != null){
                for(int i=0; i <removePrincipals.length ;i++) {
                    String s = removePrincipals[i];
                    source.remove(s);
                }
            }

            String newPrincipals[] = new String[source.size()];
            for(int i=0; i< source.size(); i++) 
                newPrincipals[i]=(String) source.get(i);
                        
            //adding user-groups....
            if(addUserGroups != null){
                for(int i=0; i < addUserGroups.length; i++) {
                    String s= addUserGroups[i];
                    if (!source1.contains(s)) 
                        source1.add(s);
                    else{
                        String msg = localStrings.getString
                        (USERGROUP_EXISTS,s,poolname);    
                        throw new Exception(msg);
                    }
                }
            }
            //removing user-groups....
            
            if(removeUserGroups != null){
                for(int i=0; i <removeUserGroups.length ;i++) {
                    String s = removeUserGroups[i];
                    source1.remove(s);
                }
            }
       
            String newUserGroups[] = new String[source1.size()];
            for(int i=0; i< source1.size(); i++) 
                newUserGroups[i]=(String) source1.get(i);
            
            
            //setting the updated principal user-group arrays....
             Attribute princ =null;
             Attribute ug = null;
             if(newPrincipals != null){
                princ = new Attribute(PRINCIPAL,newPrincipals);
                server.setAttribute(mbean,princ);
             }   
             if(newUserGroups != null){
                ug = new Attribute(USER_GROUP,newUserGroups);
                server.setAttribute(mbean,ug);
             }   
             
            //updating the backend-principal.......
             //get the backend principal for the given security map and pool...
             ObjectName backendPrincipal =(ObjectName) getMBeanServer().invoke(
             mbean, GET_BACKEND_PRINCIPAL,null,null); 
            
             if(username != null){
                if(!((String)(server.getAttribute(backendPrincipal,
                                        USER_NAME))).equals(username))
                    server.setAttribute(backendPrincipal,
                                            new Attribute(USER_NAME,username));            
             }
             
             if(password != null){
                if(!((String)(server.getAttribute(backendPrincipal,
                                            PASSWORD))).equals(password))
                    server.setAttribute(backendPrincipal,
                                    new Attribute(PASSWORD,password));            
             }
             status = true;
        } catch (Exception e) {
            status = false;
            e.printStackTrace();
            throw new MBeanException(e);
        }
        return status;
    }   
 
 
    
    public ArrayList listSecurityMap(String mapName,Boolean verb,String poolName,
                    String tgtName) throws MBeanException{
        
        ObjectName mbean = null;
        boolean verbose = false;
        ArrayList list = new ArrayList();

        try {
            if (tgtName == null || tgtName.equals("")) tgtName = DEFAULT_TARGET;
            if(!doesPoolNameExists(poolName)){
                String msg = localStrings.getString(POOL_DOES_NOT_EXISTS,poolName);
                throw new Exception(msg);
            }  
            if(mapName != null){
                if(!doesMapNameExists(poolName,mapName)){
                    String msg = localStrings.getString(MAP_DOES_NOT_EXISTS
                                                            ,mapName,poolName);
                    throw new Exception(msg);
                }
            }
                
             
             verbose = verb.booleanValue();
             //get all the map object names...
             ObjectName[] maps =(ObjectName[]) super.invoke(GET_SECURITY_MAP,null,null);                    
             if(mapName == null  && verbose){
                 if(maps != null && maps.length >0){
                    for(int i=0;i<maps.length;i++){
                        String map = (String)getMBeanServer().getAttribute(
                                                                maps[i],NAME);
                        ObjectName mapRef =(ObjectName)super.invoke(
                        GET_SECURITY_MAP_BY_NAME, new Object[]{map},
                                        new String[] {"java.lang.String"}); 
                        list.add(createMapInfo(mapRef));
                                                          
                    }
                }
             }else if( mapName == null && !verbose ){
                 if(maps != null && maps.length >0){
                    for(int i=0;i<maps.length;i++){
                        String map = (String)
                                getMBeanServer().getAttribute(maps[i],NAME);
                        AttributeList attr = new AttributeList();
                        if(map != null)
                            attr.add(new Attribute(NAME,map));
                 
                        //print the map names .....
                        ObjectName mapRef =(ObjectName)
                         super.invoke(GET_SECURITY_MAP_BY_NAME,new Object[]{map},
                                           new String[] {"java.lang.String"}); 
                         String mapname = (String)getMBeanServer().getAttribute(mapRef,NAME);       
                         list.add(new String[]{mapname,null,null,null,null});
                    }
                 }
             }else {
                 // map name is not null, print the map details if verbose is true...
                 ObjectName mapRef =(ObjectName)super.invoke(GET_SECURITY_MAP_BY_NAME,
                        new Object[]{mapName}, new String[] {"java.lang.String"}); 
                 if(mapRef != null) {
                     if (verbose) {
                        list.add(createMapInfo(mapRef));
                     } else {
                         list.add(new String[]{mapName,null,null,null,null});
                     }
                 }    
                           
             }   
         }catch (Exception e) {
            e.printStackTrace(); 
            throw new MBeanException(e);
        }

        return list;
    }
    

    // -----------------Set of Helper methods ---------------------------- //
    
    // Gives the attribute list for the mapname and poolname.
    public AttributeList getAttributes(String mapName,String poolName)
    throws Exception {
        String mapname = null;
        String[] existingPrincipals = null;
        String[] existingUserGroups = null;
        String username = null;
        String password = null;
        
        if(mapName != null){
            if(!doesMapNameExists(poolName,mapName)){
                String msg = localStrings.getString(MAP_DOES_NOT_EXISTS
                                                            ,mapName,poolName);
                throw new Exception(msg);
            }
        }
        
         ObjectName mapRef =(ObjectName)super.invoke(
                GET_SECURITY_MAP_BY_NAME, new Object[]{mapName},
                new String[] {"java.lang.String"}); 
         if(mapRef != null) {
            mapname = (String)getMBeanServer().getAttribute(mapRef,NAME);
            existingPrincipals =
                (String[])getMBeanServer().getAttribute(mapRef,PRINCIPAL);
            existingUserGroups =
                (String[])getMBeanServer().getAttribute(mapRef,USER_GROUP);       
         }
            
         ObjectName backEndPrincipal = (ObjectName)getMBeanServer().invoke(
         mapRef,GET_BACKEND_PRINCIPAL,null,null); 
         if(backEndPrincipal != null){
            username = (String)
                   getMBeanServer().getAttribute(backEndPrincipal,USER_NAME);
            password = (String)
                    getMBeanServer().getAttribute(backEndPrincipal,PASSWORD);
         }
         //set the attributes......
         AttributeList attributes = new AttributeList();
         if(mapname != null)
             attributes.add(new Attribute(NAME,mapname));
         if(existingPrincipals != null)
             attributes.add(new Attribute(PRINCIPAL,existingPrincipals));
         if(existingUserGroups != null)
             attributes.add(new Attribute(USER_GROUP,existingUserGroups));
         if(username != null)
             attributes.add(new Attribute(USER_NAME,username));
         if(password != null)
             attributes.add(new Attribute(PASSWORD,password));
         
         return attributes;
    }
    
    private ObjectName getConnectorConnObjectName(String poolName)throws Exception{
        return m_registry.getMbeanObjectName(POOL_TYPE,
                            new String[]{getDomainName(),poolName,CONFIG});
    }
      
    private ObjectName getSecurityMapObjectName(String mapName,String poolName)
    throws Exception{
        return m_registry.getMbeanObjectName(MAP_TYPE,
                    new String[]{getDomainName(),poolName,mapName,CONFIG});
    }
    private boolean isPrincipalExisting(String principal,ObjectName[] maps)
    throws Exception{
        boolean exists =false;
        String[] existingPrincipals = null;

        if (maps!= null && maps.length > 0){
            for (int k=0; k<maps.length && !exists ; k++){
                existingPrincipals =
                (String[])getMBeanServer().getAttribute(maps[k],PRINCIPAL);
                    if(existingPrincipals != null && principal != null){
                        for(int i=0; i < existingPrincipals.length ; i++) {
                            if(existingPrincipals[i].equals(principal)){
                                exists = true;	
                                break;
                            }   
                        }
                    }
            }
        } 
    return exists ;
    }

    private boolean isUserGroupExisting(String usergroup,ObjectName[] maps)
    throws Exception {
        boolean exists =false;	
        String[] existingUserGroups = null;
        if (maps!= null && maps.length > 0){
            for (int k=0; k<maps.length && !exists ; k++){
                existingUserGroups = 
                (String[])getMBeanServer().getAttribute(maps[k],USER_GROUP);
                    if(existingUserGroups != null && usergroup != null){
                        for(int i=0; i < existingUserGroups.length ; i++) {
                        if(existingUserGroups[i].equals(usergroup)){
                            exists = true;	
                            break;
                        }
                    }
                }
          }
        } 
    return exists ;
    }

    protected ObjectName getConfigMBean(Target target) throws Exception {
        ConfigTarget ct = target.getConfigTarget();
        return new ObjectName(
                ct.getTargetObjectName(new String[] {getDomainName()}));
    }
      
    private ObjectName[] getAllSecurityMapsForPool(String poolName )throws Exception{
         ObjectName poolObj = getConnectorConnObjectName(poolName);
            return (ObjectName[])getMBeanServer().invoke(poolObj,
                                        GET_SECURITY_MAP, null,  null);
    }
    
    private boolean doesPoolNameExists(String poolName )throws Exception {
       //check if the poolname exists.If it does not then throw an exception.
        ObjectName resObj = m_registry.getMbeanObjectName(RES_TYPE,
                            new String[]{getDomainName(),CONFIG});
        ObjectName[] pools =(ObjectName[])getMBeanServer().invoke(resObj,
                                            GET_CONNECTOR_POOLS,null,null);
        boolean doesPoolExists = false;
        if (pools!= null && pools.length > 0){
            for (int i=0; i<pools.length; i++){
                String pool = (String)getMBeanServer().getAttribute(pools[i],NAME);
                if (pool.equals(poolName)) 
                        doesPoolExists = true;
            }
        } 
        return doesPoolExists;
    }
    
    private boolean doesMapNameExists(String poolName,String mapname )throws Exception{
        //check if the mapname exists for the given pool name..
        ObjectName poolObj = getConnectorConnObjectName(poolName);
        ObjectName[] maps =(ObjectName[])getMBeanServer().invoke(poolObj,
                                    GET_SECURITY_MAP, null,  null);

        boolean doesMapNameExists = false;
        if (maps!= null && maps.length > 0){
                for (int i=0; i<maps.length; i++){
                    String mapName = 
                            (String)getMBeanServer().getAttribute(maps[i],NAME);
                    if (mapName.equals(mapname)) 
                        doesMapNameExists = true;
                }
        }
        return doesMapNameExists;
    }
    
    private String[] createMapInfo(ObjectName mapRef )throws Exception{
         String[] existingPrincipals = null;
         String[] existingUserGroups = null;
         StringBuffer buf1= new StringBuffer();
         StringBuffer buf2= new StringBuffer();
         String mapname = null;
         String username = null;
         String password = null;
                             
        if(mapRef != null) {
            mapname = (String)getMBeanServer().getAttribute(mapRef,NAME);
            existingPrincipals = 
                    (String[])getMBeanServer().getAttribute(mapRef,PRINCIPAL);
            existingUserGroups =
                    (String[])getMBeanServer().getAttribute(mapRef,USER_GROUP);       
            if(existingPrincipals != null){
                for(int j=0;j<existingPrincipals.length;j++){
                    buf1.append(existingPrincipals[j]);
                    buf1.append(",");
                }
            }
            if(existingUserGroups != null){
                for(int j=0;j<existingUserGroups.length;j++){
                    buf2.append(existingUserGroups[j]);
                    buf2.append(",");
                }
            }
            ObjectName backEndPrincipal = (ObjectName)getMBeanServer().invoke(
                                mapRef,GET_BACKEND_PRINCIPAL,null,null); 
            if(backEndPrincipal != null){
                username =
                (String)getMBeanServer().getAttribute(backEndPrincipal,USER_NAME);
                password =
                (String)getMBeanServer().getAttribute(backEndPrincipal,PASSWORD);
         
            }
        }
        return new String[]{mapname,buf1.toString(),buf2.toString(),username,password};
   
     }
    
    private String[] getOptionsList(Object sOptions){
        StringTokenizer optionTokenizer   = new StringTokenizer((String)sOptions,",");
        int             size            = optionTokenizer.countTokens();
        String []       sOptionsList = new String[size];
        for (int ii=0; ii<size; ii++){
            sOptionsList[ii] = optionTokenizer.nextToken();
        } 
        return sOptionsList;
   }
    
    protected MBeanServer getMBeanServer() {
        return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
    }

    private static boolean isAttrNameMatch(Attribute attr, String name)
    {
        //FIXME: this code should be changed after FCS
        // for now we supporting both "dashed" and "underscored" names
        return attr.getName().replace('_','-').equals(name.replace('_','-'));
    }

    
}
