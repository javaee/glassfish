/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.security.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.intf.config.AuthRealm;
import org.glassfish.admin.amx.intf.config.Property;
import org.glassfish.admingui.common.util.AMX;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;


/**
 *
 * @author anilam
 */
public class RealmsHandlers {
    

    /**
     *	<p> This handler returns the a Map for storing the attributes for realm creation.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRealmAttrForCreate",
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=List.class)})
    public static void getRealmAttrForCreate(HandlerContext handlerCtx) {
        
        handlerCtx.setOutputValue("realmClasses", realmClassList);
        handlerCtx.setOutputValue("classnameOption", "predefine");
        Map attrMap = new HashMap();
        attrMap.put("predefinedClassname", Boolean.TRUE);
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("properties", new ArrayList());
    }
    
    /**
     *	<p> This handler returns the a Map for storing the attributes for editing a realm.
     *  This can be used by either the node agent realm or the realm in configuration-Security-realm
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRealmAttrForEdit",
    input={
        @HandlerInput(name="objectNameStr", type=String.class)},
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=List.class)})
        
    public static void getRealmAttrForEdit(HandlerContext handlerCtx) {

        String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
        AuthRealm realm = (AuthRealm) V3AMX.objectNameToProxy(objectNameStr).as(AuthRealm.class);

        Map<String, Property> origProps = realm.getProperty();

        Map attrMap = new HashMap();
        attrMap.put("Name", realm.getName());
        attrMap.put("fileJaax", "fileRealm");
        attrMap.put("ldapJaax", "ldapRealm" );
        attrMap.put("solarisJaax", "solarisRealm");
        attrMap.put("jdbcJaax", "jdbcRealm");
        
        String classname = realm.getClassname();
        
        if (realmClassList.contains(classname)){
            handlerCtx.setOutputValue("classnameOption", "predefine");
            attrMap.put("predefinedClassname", Boolean.TRUE);
            attrMap.put("classname", classname);
            List props = V3AMX.getChildrenMapForTableList(realm, "property",  skipRealmPropsList);
            handlerCtx.setOutputValue("properties", props);

            if(classname.indexOf("FileRealm")!= -1){
                attrMap.put("file",  V3AMX.getPropValue(origProps, "file"));
                attrMap.put("fileJaax",  V3AMX.getPropValue(origProps, "jaas-context"));
                attrMap.put("fileAsGroups",  V3AMX.getPropValue(origProps, "assign-groups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                attrMap.put("ldapJaax",  V3AMX.getPropValue(origProps, "jaas-context"));
                attrMap.put("ldapAsGroups",  V3AMX.getPropValue(origProps, "assign-groups"));
                attrMap.put("directory",  V3AMX.getPropValue(origProps, "directory"));
                attrMap.put("baseDn",  V3AMX.getPropValue(origProps, "base-dn"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                attrMap.put("solarisJaax",  V3AMX.getPropValue(origProps, "jaas-context"));
                attrMap.put("solarisAsGroups",  V3AMX.getPropValue(origProps, "assign-groups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                attrMap.put("jdbcJaax",  V3AMX.getPropValue(origProps, "jaas-context"));
                attrMap.put("jdbcAsGroups",  V3AMX.getPropValue(origProps, "assign-groups"));
                attrMap.put("datasourceJndi",  V3AMX.getPropValue(origProps, "datasource-jndi"));
                attrMap.put("userTable",  V3AMX.getPropValue(origProps, "user-table"));
                attrMap.put("userNameColumn",  V3AMX.getPropValue(origProps, "user-name-column"));
                attrMap.put("passwordColumn",  V3AMX.getPropValue(origProps, "password-column"));
                attrMap.put("groupTable",  V3AMX.getPropValue(origProps, "group-table"));
                attrMap.put("groupNameColumn",  V3AMX.getPropValue(origProps, "group-name-column"));
                attrMap.put("dbUser",  V3AMX.getPropValue(origProps, "db-user"));
                attrMap.put("dbPassword",  V3AMX.getPropValue(origProps, "db-password"));
                attrMap.put("digestAlgorithm",  V3AMX.getPropValue(origProps, "digest-algorithm"));
                attrMap.put("encoding",  V3AMX.getPropValue(origProps, "encoding"));
                attrMap.put("charset",  V3AMX.getPropValue(origProps, "charset"));

           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                attrMap.put("certAsGroups",  V3AMX.getPropValue(origProps, "assign-groups"));
            }
        }else{
            //Custom realm class
            handlerCtx.setOutputValue("classnameOption", "input");
            attrMap.put("predefinedClassname", Boolean.FALSE);
            attrMap.put("classnameInput", classname);
            List props = V3AMX.getNonSkipPropertiesMap(realm, null);
            handlerCtx.setOutputValue("properties", props);
        }

        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("realmClasses", realmClassList);
    }
    
    
    @Handler(id="saveRealm",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class),
        @HandlerInput(name="parentObjectName",   type=String.class),
        @HandlerInput(name="classnameOption",   type=String.class),
        @HandlerInput(name="attrMap",      type=Map.class),
        @HandlerInput(name="edit",      type=Boolean.class),
        @HandlerInput(name="propList", type=List.class)
    })
    public static void saveRealm(HandlerContext handlerCtx) {
        String option = (String) handlerCtx.getInputValue("classnameOption");
        List propList = (List)handlerCtx.getInputValue("propList");
        Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");
        
        String classname = "";
        try{
          if(option.equals("predefine")){
            classname = attrMap.get("classname");
            
            if(classname.indexOf("FileRealm")!= -1){
                putOptional(attrMap, propList, "file", "file");
                putOptional(attrMap, propList, "jaas-context", "fileJaax");
                putOptional(attrMap, propList, "assign-groups", "fileAsGroups");
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                putOptional(attrMap, propList, "jaas-context", "ldapJaax");
                putOptional(attrMap, propList, "base-dn", "baseDn");
                putOptional(attrMap, propList, "directory", "directory");
                putOptional(attrMap, propList, "assign-groups", "ldapAsGroups");
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                putOptional(attrMap, propList, "jaas-context", "solarisJaax");
                putOptional(attrMap, propList, "assign-groups", "solarisAsGroups");
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                putOptional(attrMap, propList, "jaas-context", "jdbcJaax");
                putOptional(attrMap, propList, "datasource-jndi", "datasourceJndi");
                putOptional(attrMap, propList, "user-table", "userTable");
                putOptional(attrMap, propList, "user-name-column", "userNameColumn");
                putOptional(attrMap, propList, "password-column", "passwordColumn");
                putOptional(attrMap, propList, "group-table", "groupTable");
                putOptional(attrMap, propList, "group-name-column", "groupNameColumn");
                putOptional(attrMap, propList, "db-user", "dbUser");
                putOptional(attrMap, propList, "db-password", "dbPassword");
                putOptional(attrMap, propList, "digest-algorithm", "digestAlgorithm");
                putOptional(attrMap, propList, "encoding", "encoding");
                putOptional(attrMap, propList, "charset", "charset");
                putOptional(attrMap, propList, "assign-groups", "jdbcAsGroups");
           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                putOptional(attrMap, propList, "assign-groups", "certAsGroups");
            }
         } else {
            classname = attrMap.get("classnameInput");            
         }

          Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
          if (edit.booleanValue()){
              String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
              V3AMX.setAttribute(objectNameStr, new Attribute("Classname", classname));
              V3AMX.setProperties(objectNameStr, propList, false);
          }else{
             Map<String, Object> cMap = new HashMap();
             cMap.put("Name", attrMap.get("Name"));
             cMap.put("Classname", attrMap.get("classname"));
             AMXConfigProxy amx = (AMXConfigProxy) V3AMX.objectNameToProxy(AMX.SECURITY_SERVICE);
             AMXConfigProxy child =  amx.createChild("auth-realm", cMap);
             V3AMX.setProperties(child.objectName().toString(), propList, false);
          }

      }catch(Exception ex){
          GuiUtil.handleException(handlerCtx, ex);
      }
    }
    
    
    static private void putOptional(Map<String,String> attrMap, List propList, String propName, String key)
    {
        Map oneProp = new HashMap();
        oneProp.put(PROPERTY_NAME, propName);
        String value = attrMap.get(key);
        if (GuiUtil.isEmpty(value))
            return;
        oneProp.put(PROPERTY_VALUE, attrMap.get(key));
        propList.add(oneProp);
    }


    private static List skipRealmPropsList = new ArrayList();
    private static List realmClassList;
    static {
        String[] classnames = V3AMX.getInstance().getRealmsMgr().getPredefinedAuthRealmClassNames();
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

    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_VALUE = "Value";
    
}
