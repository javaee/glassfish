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
 * RealmHandlers.java
 *
 * Created on August 12, 2006, 7:04 PM
 *
 */
package com.sun.enterprise.tools.admingui.handlers;

import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;

import javax.faces.model.SelectItem;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  


import com.sun.enterprise.tools.admingui.util.AMXUtil; 
import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;

import com.sun.appserv.management.config.ConfigConfig; 
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.NodeAgentConfig;


/**
 *
 * @author anilam
 */
public class RealmHandlers {
    
    /**
     *	<p> This handler returns dropdown values for Default Realm
     *      in Security Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Realms" -- Type: <code>java.util.Array</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRealms",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="Realms", type=SelectItem[].class)})

        public static void getRealms(HandlerContext handlerCtx) {
        
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuthRealmConfig>realms = sConfig.getAuthRealmConfigMap();
        String[] aRealm = (String[])realms.keySet().toArray(new String[realms.size()]);
        if(aRealm != null) {
            SelectItem[] options = ConfigurationHandlers.getOptions(aRealm);
            handlerCtx.setOutputValue("Realms", options);
        }
    }
    
    
    /**
     *	<p> This handler returns the a Map for storing the attributes for realm creation.
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRealmAttrForCreate",
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=Map.class)})
    public static void getRealmAttrMap(HandlerContext handlerCtx) {
        
        Map attrMap = new HashMap();
        attrMap.put("fileJaax", "fileRealm");
        attrMap.put("ldapJaax", "ldapRealm" );
        attrMap.put("solarisJaax", "solarisRealm");
        attrMap.put("jdbcJaax", "jdbcRealm");
        
        attrMap.put("classname", "com.sun.enterprise.security.auth.realm.file.FileRealm");
        attrMap.put("predefinedClassname", Boolean.TRUE);
        
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("classnameOption", "predefine");
        handlerCtx.setOutputValue("realmClasses", realmClassList);
        handlerCtx.setOutputValue("properties", new HashMap());
    }
    
    /**
     *	<p> This handler returns the a Map for storing the attributes for editing a realm.
     *  This can be used by either the node agent realm or the realm in configuration-Security-realm
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getRealmAttrForEdit",
    input={
        @HandlerInput(name="configName", type=String.class), 
        @HandlerInput(name="realmName", type=String.class),
        @HandlerInput(name="nodeAgentName", type=String.class)},
    output={
        @HandlerOutput(name="outName",      type=String.class),
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=Map.class)})
        
    public static void getRealmAttrForEdit(HandlerContext handlerCtx) {
        
        AuthRealmConfig realm = getAuthRealmConfig(handlerCtx, true);
        if (realm == null)
            return;
        Map origProps = realm.getProperties();
        Map attrMap = new HashMap();
        attrMap.put("fileJaax", "fileRealm");
        attrMap.put("ldapJaax", "ldapRealm" );
        attrMap.put("solarisJaax", "solarisRealm");
        attrMap.put("jdbcJaax", "jdbcRealm");
        
        String classname = realm.getClassname();
        
        if (realmClassList.contains(classname)){
            handlerCtx.setOutputValue("classnameOption", "predefine");
            attrMap.put("predefinedClassname", Boolean.TRUE);
            attrMap.put("classname", classname);
            Map props = AMXUtil.getNonSkipPropertiesMap(realm, skipRealmPropsList);
            handlerCtx.setOutputValue("properties", props);
            
            if(classname.indexOf("FileRealm")!= -1){
                attrMap.put("file",  origProps.get("file"));
                attrMap.put("fileJaax",  origProps.get("jaas-context"));
                attrMap.put("fileAsGroups",  origProps.get("assign-groups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                attrMap.put("ldapJaax",  origProps.get("jaas-context"));
                attrMap.put("ldapAsGroups",  origProps.get("assign-groups"));
                attrMap.put("directory",  origProps.get("directory"));
                attrMap.put("baseDn",  origProps.get("base-dn"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                attrMap.put("solarisJaax",  origProps.get("jaas-context"));
                attrMap.put("solarisAsGroups",  origProps.get("assign-groups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                attrMap.put("jdbcJaax",  origProps.get("jaas-context"));
                attrMap.put("jdbcAsGroups",  origProps.get("assign-groups"));
                attrMap.put("datasourceJndi",  origProps.get("datasource-jndi"));
                attrMap.put("userTable",  origProps.get("user-table"));
                attrMap.put("userNameColumn",  origProps.get("user-name-column"));
                attrMap.put("passwordColumn",  origProps.get("password-column"));
                attrMap.put("groupTable",  origProps.get("group-table"));
                attrMap.put("groupNameColumn",  origProps.get("group-name-column"));
                attrMap.put("dbUser",  origProps.get("db-user"));
                attrMap.put("dbPassword",  origProps.get("db-password"));
                attrMap.put("digestAlgorithm",  origProps.get("digest-algorithm"));
                attrMap.put("encoding",  origProps.get("encoding"));
                attrMap.put("charset",  origProps.get("charset"));
            
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                attrMap.put("certAsGroups",  origProps.get("assign-groups"));
            }
        }else{
            //Custom realm class
            handlerCtx.setOutputValue("classnameOption", "input");
            attrMap.put("predefinedClassname", Boolean.FALSE);
            attrMap.put("classnameInput", classname);
            handlerCtx.setOutputValue("properties", origProps);
        }
        
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("realmClasses", realmClassList);
    }
    
    private static AuthRealmConfig getAuthRealmConfig(HandlerContext handlerCtx,boolean getRealmName){
        String realmName = (String)handlerCtx.getInputValue("realmName");
        String configName = (String)handlerCtx.getInputValue("configName");
        String nodeAgentName = (String)handlerCtx.getInputValue("nodeAgentName");
        
        if (!GuiUtil.isEmpty(nodeAgentName)){
            NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(nodeAgentName);
            if (getRealmName){
                handlerCtx.setOutputValue("outName", agentConfig.getJMXConnectorConfig().getAuthRealmName());
            }
            return agentConfig.getContainee(AuthRealmConfig.J2EE_TYPE);
        }else
        if (GuiUtil.isEmpty(realmName) || GuiUtil.isEmpty(configName)){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("realm.internalError"));
            return null;
        }else
        {
            ConfigConfig config = AMXUtil.getConfig(configName);
            if (getRealmName){
                handlerCtx.setOutputValue("outName", realmName);
            }
            return config.getSecurityServiceConfig().getAuthRealmConfigMap().get(realmName);
        }
    }
    
    
    @Handler(id="createRealm",
    input={
        @HandlerInput(name="configName",   type=String.class),
        @HandlerInput(name="classnameOption",   type=String.class),
        @HandlerInput(name="attrMap",      type=Map.class),
        @HandlerInput(name="newProps", type=Map.class)
    })
    public static void createRealm(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("configName");
        String option = (String) handlerCtx.getInputValue("classnameOption");
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");
        
        Map convertedMap = AMXUtil.convertToPropertiesOptionMap(newProps, null);
        String classname = "";
        try{
          if(option.equals("predefine")){
            classname = attrMap.get("classname");
            
            if(classname.indexOf("FileRealm")!= -1){
                putOptional(attrMap, convertedMap, "file", "file");
                putOptional(attrMap, convertedMap, "jaas-context", "fileJaax");
                putOptional(attrMap, convertedMap, "assign-groups", "fileAsGroups");
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                putOptional(attrMap, convertedMap, "jaas-context", "ldapJaax");
                putOptional(attrMap, convertedMap, "base-dn", "baseDn");
                putOptional(attrMap, convertedMap, "directory", "directory");
                putOptional(attrMap, convertedMap, "assign-groups", "ldapAsGroups");
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                putOptional(attrMap, convertedMap, "jaas-context", "solarisJaax");
                putOptional(attrMap, convertedMap, "assign-groups", "solarisAsGroups");
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                putOptional(attrMap, convertedMap, "jaas-context", "jdbcJaax");
                putOptional(attrMap, convertedMap, "datasource-jndi", "datasourceJndi");
                putOptional(attrMap, convertedMap, "user-table", "userTable");
                putOptional(attrMap, convertedMap, "user-name-column", "userNameColumn");
                putOptional(attrMap, convertedMap, "password-column", "passwordColumn");
                putOptional(attrMap, convertedMap, "group-table", "groupTable");
                putOptional(attrMap, convertedMap, "group-name-column", "groupNameColumn");
                putOptional(attrMap, convertedMap, "db-user", "dbUser");
                putOptional(attrMap, convertedMap, "db-password", "dbPassword");
                putOptional(attrMap, convertedMap, "digest-algorithm", "digestAlgorithm");
                putOptional(attrMap, convertedMap, "encoding", "encoding");
                putOptional(attrMap, convertedMap, "charset", "charset");
                putOptional(attrMap, convertedMap, "assign-groups", "jdbcAsGroups");
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                putOptional(attrMap, convertedMap, "assign-groups", "certAsGroups");
            }
         } else {
            classname = attrMap.get("classnameInput");            
         }
        
         AMXUtil.getConfig(configName).getSecurityServiceConfig().createAuthRealmConfig(
                attrMap.get("name"), classname, convertedMap);
      }catch(Exception ex){
          GuiUtil.handleException(handlerCtx, ex);
      }
    }
    
    
    static private void putOptional(Map<String,String> attrMap, Map convertedMap, String propName, String key)
    {
        String val = attrMap.get(key);
        if (!GuiUtil.isEmpty(val))
            convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + propName, val);
    }
   
   @Handler(id="saveRealm",
    input={
        @HandlerInput(name="configName",   type=String.class),
        @HandlerInput(name="realmName",   type=String.class),
        @HandlerInput(name="nodeAgentName",   type=String.class),
        @HandlerInput(name="classnameOption",   type=String.class),
        @HandlerInput(name="attrMap",      type=Map.class),
        @HandlerInput(name="newProps", type=Map.class)
    })
    public static void saveRealm(HandlerContext handlerCtx) {
        AuthRealmConfig realm = getAuthRealmConfig(handlerCtx, false);
        if (realm == null)
            return;
        String option = (String) handlerCtx.getInputValue("classnameOption");
        Map newProps = (Map)handlerCtx.getInputValue("newProps");
        Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");
        
        try{
          if(option.equals("predefine")){
            String classname = attrMap.get("classname");
            String oldClassname = realm.getClassname();
            if (! classname.equals(oldClassname))
                realm.setClassname(classname);
            // we do want to remove all the special props previously
            AMXUtil.updateProperties(realm, newProps, null);
            
            if(classname.indexOf("FileRealm")!= -1){
                AMXUtil.changeProperty(realm, "file", attrMap.get("file"));
                AMXUtil.changeProperty(realm, "jaas-context", attrMap.get("fileJaax"));
                AMXUtil.changeProperty(realm, "assign-groups", attrMap.get("fileAsGroups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                
                AMXUtil.changeProperty(realm, "jaas-context", attrMap.get("ldapJaax"));
                AMXUtil.changeProperty(realm, "assign-groups", attrMap.get("ldapAsGroups"));
                AMXUtil.changeProperty(realm, "base-dn", attrMap.get("baseDn"));
                AMXUtil.changeProperty(realm, "directory", attrMap.get("directory"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                AMXUtil.changeProperty(realm, "jaas-context", attrMap.get("solarisJaax"));
                AMXUtil.changeProperty(realm, "assign-groups", attrMap.get("solarisAsGroups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                AMXUtil.changeProperty(realm, "jaas-context", attrMap.get("jdbcJaax"));
                AMXUtil.changeProperty(realm, "assign-groups", attrMap.get("jdbcAsGroups"));
                AMXUtil.changeProperty(realm, "datasource-jndi", attrMap.get("datasourceJndi"));
                AMXUtil.changeProperty(realm, "user-table", attrMap.get("userTable"));
                AMXUtil.changeProperty(realm, "user-name-column", attrMap.get("userNameColumn"));
                AMXUtil.changeProperty(realm, "password-column", attrMap.get("passwordColumn"));
                AMXUtil.changeProperty(realm, "group-table", attrMap.get("groupTable"));
                AMXUtil.changeProperty(realm, "group-name-column", attrMap.get("groupNameColumn"));
                AMXUtil.changeProperty(realm, "db-user", attrMap.get("dbUser"));
                AMXUtil.changeProperty(realm, "db-password", attrMap.get("dbPassword"));
                AMXUtil.changeProperty(realm, "digest-algorithm", attrMap.get("digestAlgorithm"));
                AMXUtil.changeProperty(realm, "encoding", attrMap.get("encoding"));
                AMXUtil.changeProperty(realm, "charset", attrMap.get("charset"));
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                AMXUtil.changeProperty(realm, "assign-groups", attrMap.get("certAsGroups"));
            }
         } else {
            //We need to process all the properties as user define.
            AMXUtil.updateProperties(realm, newProps, null);
            realm.setClassname(attrMap.get("classnameInput"));
         }
      }catch(Exception ex){
          GuiUtil.handleException(handlerCtx, ex);
      }
    }
   
  
    
    private static List skipRealmPropsList = new ArrayList();
    private static List realmClassList;
    static {
        String[] classnames = (String[])JMXUtil.invoke(
                "com.sun.appserv:category=config,config=server-config,type=security-service",
                "getPredefinedAuthRealmClassNames", null, null );
        realmClassList = new ArrayList();
        for(int i=0; i< classnames.length; i++){
            realmClassList.add(classnames[i]);
        }
        
        skipRealmPropsList.add("jaas-context");
        skipRealmPropsList.add("file");
        skipRealmPropsList.add("assign-groups");
        skipRealmPropsList.add("base-dn");
        skipRealmPropsList.add("directory");
        skipRealmPropsList.add("datasource-jndi");
        skipRealmPropsList.add("user-table");
        skipRealmPropsList.add("user-name-column");
        skipRealmPropsList.add("password-column");
        skipRealmPropsList.add("group-table");
        skipRealmPropsList.add("group-name-column");
        skipRealmPropsList.add("db-user");
        skipRealmPropsList.add("db-password");
        skipRealmPropsList.add("digest-algorithm");
        skipRealmPropsList.add("encoding");
        skipRealmPropsList.add("charset");
    }
   
}
