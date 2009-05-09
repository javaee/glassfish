/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.security.admingui.handlers;


import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ConfigElement;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.PropertyConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.GuiUtil;


/**
 *
 * @author anilam
 */
public class RealmsHandlers {
    
    
    /**
     *	<p> This handler returns the list of specified config elements for populating the table.
     *  <p> Input  value: "type" -- Type: <code> java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRealmsList",
        input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getRealmsList(HandlerContext handlerCtx){
        
        
        String configName = (String) handlerCtx.getInputValue("ConfigName");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuthRealmConfig>realmsMap = sConfig.getAuthRealmConfigMap();
        Iterator iter = realmsMap.values().iterator();
        
        List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
        boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;
        List result = new ArrayList();
        if (iter != null){
            while(iter.hasNext()){
                ConfigElement configE = (ConfigElement) iter.next();
                HashMap oneRow = new HashMap();
                String name=configE.getName();                
                oneRow.put("name", name);
                oneRow.put("selected", (hasOrig)? GuiUtil.isSelected(name, selectedList): false);
                String classname = ((AuthRealmConfig)configE).getClassname();
                oneRow.put("classname", (classname == null) ? " ": classname);
                result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("result", result);
    }    

     /**
     *	<p> This handler takes in selected rows, and remove the selected Realm
     *  <p> Input  value: "selectedRows" -- Type: <code>java.util.List</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id = "deleteSecurityConfig", input = {
        @HandlerInput(name = "ConfigName", type = String.class, required = true),
        @HandlerInput(name = "type", type = String.class, required = true),
        @HandlerInput(name = "selectedRows", type = List.class, required = true)
    })
    public static void deleteSecurityConfig(HandlerContext handlerCtx) {

        String configName = (String) handlerCtx.getInputValue("ConfigName");
        String type = (String) handlerCtx.getInputValue("type");
        ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        List<Map> selectedRows = (List<Map>) handlerCtx.getInputValue("selectedRows");
        for (Map oneRow : selectedRows) {
            String name = (String) oneRow.get("name");
            try{
                if (type.equals("realms")){
                    sConfig.removeAuthRealmConfig(name);
                }else{
                    sConfig.removeAuditModuleConfig(name);
                }
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Cannot delete " + name);
            }
        }
    }
    
    
    /**
     *	<p> This handler returns the a Map for storing the attributes for realm creation.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRealmAttrForCreate",
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=Map.class)})
    public static void getRealmAttrForCreate(HandlerContext handlerCtx) {
        
        handlerCtx.setOutputValue("realmClasses", realmClassList);
        handlerCtx.setOutputValue("classnameOption", "predefine");
        Map attrMap = new HashMap();
        /*
        attrMap.put("fileJaax", "fileRealm");
        attrMap.put("ldapJaax", "ldapRealm" );
        attrMap.put("solarisJaax", "solarisRealm");
        attrMap.put("jdbcJaax", "jdbcRealm");
        attrMap.put("classname", FILE_REALM_CLASS );
         */
        attrMap.put("predefinedClassname", Boolean.TRUE);
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("properties", new HashMap());
    }
    
    /**
     *	<p> This handler returns the a Map for storing the attributes for editing a realm.
     *  This can be used by either the node agent realm or the realm in configuration-Security-realm
     *	@param	handlerCtx	The HandlerContext.
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
        Map<String, PropertyConfig> origProps = realm.getPropertyConfigMap();
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
                attrMap.put("file",  AMXUtil.getPropValue(origProps, "file"));
                attrMap.put("fileJaax",  AMXUtil.getPropValue(origProps, "jaas-context"));
                attrMap.put("fileAsGroups",  AMXUtil.getPropValue(origProps, "assign-groups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                attrMap.put("ldapJaax",  AMXUtil.getPropValue(origProps, "jaas-context"));
                attrMap.put("ldapAsGroups",  AMXUtil.getPropValue(origProps, "assign-groups"));
                attrMap.put("directory",  AMXUtil.getPropValue(origProps, "directory"));
                attrMap.put("baseDn",  AMXUtil.getPropValue(origProps, "base-dn"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                attrMap.put("solarisJaax",  AMXUtil.getPropValue(origProps, "jaas-context"));
                attrMap.put("solarisAsGroups",  AMXUtil.getPropValue(origProps, "assign-groups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                attrMap.put("jdbcJaax",  AMXUtil.getPropValue(origProps, "jaas-context"));
                attrMap.put("jdbcAsGroups",  AMXUtil.getPropValue(origProps, "assign-groups"));
                attrMap.put("datasourceJndi",  AMXUtil.getPropValue(origProps, "datasource-jndi"));
                attrMap.put("userTable",  AMXUtil.getPropValue(origProps, "user-table"));
                attrMap.put("userNameColumn",  AMXUtil.getPropValue(origProps, "user-name-column"));
                attrMap.put("passwordColumn",  AMXUtil.getPropValue(origProps, "password-column"));
                attrMap.put("groupTable",  AMXUtil.getPropValue(origProps, "group-table"));
                attrMap.put("groupNameColumn",  AMXUtil.getPropValue(origProps, "group-name-column"));
                attrMap.put("dbUser",  AMXUtil.getPropValue(origProps, "db-user"));
                attrMap.put("dbPassword",  AMXUtil.getPropValue(origProps, "db-password"));
                attrMap.put("digestAlgorithm",  AMXUtil.getPropValue(origProps, "digest-algorithm"));
                attrMap.put("encoding",  AMXUtil.getPropValue(origProps, "encoding"));
                attrMap.put("charset",  AMXUtil.getPropValue(origProps, "charset"));
            
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                attrMap.put("certAsGroups",  AMXUtil.getPropValue(origProps, "assign-groups"));
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
        //String nodeAgentName = (String)handlerCtx.getInputValue("nodeAgentName");
        
        /*
        if (!GuiUtil.isEmpty(nodeAgentName)){
            NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(nodeAgentName);
            if (getRealmName){
                handlerCtx.setOutputValue("outName", agentConfig.getJMXConnectorConfig().getAuthRealmName());
            }
            return agentConfig.getContainee(AuthRealmConfig.J2EE_TYPE);
        }else
         */
        if (GuiUtil.isEmpty(realmName) || GuiUtil.isEmpty(configName)){
            GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("realm.internalError"));
            return null;
        }else
        {
            if (getRealmName){
                handlerCtx.setOutputValue("outName", realmName);
            }
            ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
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
        
         String configName = (String) handlerCtx.getInputValue("configName");
         ConfigConfig config = AMXRoot.getInstance().getConfigsConfig().getConfigConfigMap().get(configName);
         SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
         sConfig.createAuthRealmConfig(attrMap.get("name"), classname, convertedMap);
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
                AMXUtil.setPropertyValue(realm, "file", attrMap.get("file"));
                AMXUtil.setPropertyValue(realm, "jaas-context", attrMap.get("fileJaax"));
                AMXUtil.setPropertyValue(realm, "assign-groups", attrMap.get("fileAsGroups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                
                AMXUtil.setPropertyValue(realm, "jaas-context", attrMap.get("ldapJaax"));
                AMXUtil.setPropertyValue(realm, "assign-groups", attrMap.get("ldapAsGroups"));
                AMXUtil.setPropertyValue(realm, "base-dn", attrMap.get("baseDn"));
                AMXUtil.setPropertyValue(realm, "directory", attrMap.get("directory"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                AMXUtil.setPropertyValue(realm, "jaas-context", attrMap.get("solarisJaax"));
                AMXUtil.setPropertyValue(realm, "assign-groups", attrMap.get("solarisAsGroups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                AMXUtil.setPropertyValue(realm, "jaas-context", attrMap.get("jdbcJaax"));
                AMXUtil.setPropertyValue(realm, "assign-groups", attrMap.get("jdbcAsGroups"));
                AMXUtil.setPropertyValue(realm, "datasource-jndi", attrMap.get("datasourceJndi"));
                AMXUtil.setPropertyValue(realm, "user-table", attrMap.get("userTable"));
                AMXUtil.setPropertyValue(realm, "user-name-column", attrMap.get("userNameColumn"));
                AMXUtil.setPropertyValue(realm, "password-column", attrMap.get("passwordColumn"));
                AMXUtil.setPropertyValue(realm, "group-table", attrMap.get("groupTable"));
                AMXUtil.setPropertyValue(realm, "group-name-column", attrMap.get("groupNameColumn"));
                AMXUtil.setPropertyValue(realm, "db-user", attrMap.get("dbUser"));
                AMXUtil.setPropertyValue(realm, "db-password", attrMap.get("dbPassword"));
                AMXUtil.setPropertyValue(realm, "digest-algorithm", attrMap.get("digestAlgorithm"));
                AMXUtil.setPropertyValue(realm, "encoding", attrMap.get("encoding"));
                AMXUtil.setPropertyValue(realm, "charset", attrMap.get("charset"));
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                AMXUtil.setPropertyValue(realm, "assign-groups", attrMap.get("certAsGroups"));
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
    
    //final private static String FILE_REALM_CLASS = "com.sun.enterprise.security.auth.realm.file.FileRealm";
    private static List skipRealmPropsList = new ArrayList();
    private static List realmClassList;
    static {
        String[] classnames = AMXRoot.getInstance().getRealmsMgr().getPredefinedAuthRealmClassNames();
        realmClassList = new ArrayList();
        realmClassList.add("");
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
