/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.intf.config.AuthRealm;
import org.glassfish.admin.amx.intf.config.JavaConfig;
import org.glassfish.admin.amx.intf.config.MessageSecurityConfig;
import org.glassfish.admin.amx.intf.config.Property;
import org.glassfish.admin.amx.intf.config.ProviderConfig;
import org.glassfish.admin.amx.intf.config.SecurityService;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.JSONUtil;
import org.glassfish.admingui.common.util.RestResponse;
import org.glassfish.admingui.common.util.V3AMX;


/**
 *
 * @author anilam
 */
public class SecurityHandler {
    

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
        @HandlerInput(name="endpoint", type=String.class)},
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="realmClasses",      type=List.class),
        @HandlerOutput(name="properties", type=List.class)})

    public static void getRealmAttrForEdit(HandlerContext handlerCtx) {

        String endpoint = (String) handlerCtx.getInputValue("endpoint");

        HashMap<String, Object> realmMap = (HashMap<String, Object>) RestApiHandlers.getEntityAttrs(endpoint, "entity");
        
        HashMap<String, Object> responseMap = (HashMap<String, Object>) RestApiHandlers.restRequest(endpoint + "/property.json", null, "GET", null);
        HashMap propsMap = (HashMap) ((Map<String, Object>) responseMap.get("data")).get("extraProperties");
        ArrayList<HashMap> propList = (ArrayList<HashMap>) propsMap.get("properties");
        HashMap origProps = new HashMap();
        for (HashMap prop : propList) {
            origProps.put(prop.get("name"), prop.get("value"));
        }

        Map attrMap = new HashMap();
        attrMap.put("Name", (String) realmMap.get("name"));
        attrMap.put("fileJaax", "fileRealm");
        attrMap.put("ldapJaax", "ldapRealm" );
        attrMap.put("solarisJaax", "solarisRealm");
        attrMap.put("jdbcJaax", "jdbcRealm");

        String classname = (String) realmMap.get("classname");

        if (realmClassList.contains(classname)){
            handlerCtx.setOutputValue("classnameOption", "predefine");
            attrMap.put("predefinedClassname", Boolean.TRUE);
            attrMap.put("classname", classname);
            List props = getChildrenMapForTableList(origProps, "property", skipRealmPropsList);
            handlerCtx.setOutputValue("properties", props);

            if(classname.indexOf("FileRealm")!= -1){
                attrMap.put("file", origProps.get("file"));
                attrMap.put("fileJaax", origProps.get("jaas-context"));
                attrMap.put("fileAsGroups", origProps.get("assign-groups"));
            }else
            if(classname.indexOf("LDAPRealm")!= -1){
                attrMap.put("ldapJaax", origProps.get("jaas-context"));
                attrMap.put("ldapAsGroups", origProps.get("assign-groups"));
                attrMap.put("directory", origProps.get("directory"));
                attrMap.put("baseDn", origProps.get("base-dn"));
            }else
            if(classname.indexOf("SolarisRealm")!= -1){
                attrMap.put("solarisJaax", origProps.get("jaas-context"));
                attrMap.put("solarisAsGroups", origProps.get("assign-groups"));
            }else
            if(classname.indexOf("JDBCRealm")!= -1){
                attrMap.put("jdbcJaax", origProps.get("jaas-context"));
                attrMap.put("jdbcAsGroups", origProps.get("assign-groups"));
                attrMap.put("datasourceJndi", origProps.get("datasource-jndi"));
                attrMap.put("userTable", origProps.get("user-table"));
                attrMap.put("userNameColumn", origProps.get("user-name-column"));
                attrMap.put("passwordColumn", origProps.get("password-column"));
                attrMap.put("groupTable", origProps.get("group-table"));
                attrMap.put("groupNameColumn", origProps.get("group-name-column"));
                attrMap.put("dbUser", origProps.get("db-user"));
                attrMap.put("dbPassword", origProps.get("db-password"));
                attrMap.put("digestAlgorithm", origProps.get("digest-algorithm"));
                attrMap.put("encoding", origProps.get("encoding"));
                attrMap.put("charset", origProps.get("charset"));

           }else
            if(classname.indexOf("CertificateRealm")!= -1){
                attrMap.put("certAsGroups", origProps.get("assign-groups"));
            }
        }else{
            //Custom realm class
            handlerCtx.setOutputValue("classnameOption", "input");
            attrMap.put("predefinedClassname", Boolean.FALSE);
            attrMap.put("classnameInput", classname);
            List<HashMap> props = getListfromMap(origProps);
            handlerCtx.setOutputValue("properties", props);
        }

        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("realmClasses", realmClassList);
    }

    public static List getChildrenMapForTableList(Map<String, Object> realmMap, String childType, List skipList){
        boolean hasSkip = true;
        if (skipList == null ){
            hasSkip = false;
        }
        List result = new ArrayList();
        if (realmMap != null) {
            Set s = realmMap.entrySet();
            Iterator it = s.iterator();
            while(it.hasNext()) {
                Map.Entry m =(Map.Entry)it.next();
                HashMap oneRow = new HashMap();
                if ( hasSkip && skipList.contains(m.getKey())){
                    continue;
                }
                oneRow.put("selected", false);
                oneRow.put(m.getKey(), m.getValue());
                oneRow.put("encodedName", GuiUtil.encode((String)m.getKey(), null,null) );
                result.add(oneRow);
            }
        }
        return result;
    }

    public static List<HashMap> getListfromMap(HashMap<String, Object> props) {
        List<HashMap> result = new ArrayList();
        Iterator it = props.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry m =(Map.Entry)it.next();
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            oneRow.put("Name", m.getKey());
            oneRow.put("Value", m.getValue());
            oneRow.put("Description", "");
            result.add(oneRow);
        }
        return result;
    }
    
    @Handler(id="saveRealm",
    input={
        @HandlerInput(name="endpoint",   type=String.class),
        @HandlerInput(name="classnameOption",   type=String.class),
        @HandlerInput(name="attrMap",      type=Map.class),
        @HandlerInput(name="edit",      type=Boolean.class),
        @HandlerInput(name="contentType", type=String.class, required=false),
        @HandlerInput(name="propList", type=List.class)
    })
    public static void saveRealm(HandlerContext handlerCtx) {
        String option = (String) handlerCtx.getInputValue("classnameOption");
        List<Map<String,String>> propList = (List)handlerCtx.getInputValue("propList");
        Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");

        if (attrMap == null) {
            attrMap = new HashMap();
        }
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
           }else {
               if(classname.indexOf("CertificateRealm")!= -1){
                   putOptional(attrMap, propList, "assign-groups", "certAsGroups");
               }
           }
        } else {
           classname = attrMap.get("classnameInput");
        }

        Boolean edit = (Boolean) handlerCtx.getInputValue("edit");
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        if (edit.booleanValue()){
            Map values = new HashMap();
            values.put("classname", classname);
            String propertyStr ="";
            for(Map oneProp: propList){
               propertyStr = propertyStr + oneProp.get("name") + "=";
               propertyStr = propertyStr + oneProp.get("value") + ":";
            }
            values.put("property", propertyStr);
            RestApiHandlers.restRequest(endpoint , values, "post", handlerCtx);
        }else{
            Map<String, Object> cMap = new HashMap();
            cMap.put("name", attrMap.get("Name"));
            cMap.put("classname", classname);
            String propertyStr ="";
            for(Map oneProp: propList){
               propertyStr = propertyStr + oneProp.get("name") + "=";
               propertyStr = propertyStr + oneProp.get("value") + ":";
            }
            endpoint = endpoint + "/auth-realm";
            cMap.put("property", propertyStr);
            RestApiHandlers.restRequest(endpoint, cMap, "post", handlerCtx);
        }

      }catch(Exception ex){
          GuiUtil.handleException(handlerCtx, ex);
      }
    }
    
    
    static public void putOptional(Map<String,String> attrMap, List propList, String propName, String key)
    {
        Map oneProp = new HashMap();
        oneProp.put("name", propName);
        String value = attrMap.get(key);
        if (GuiUtil.isEmpty(value))
            return;
        oneProp.put("value", attrMap.get(key));
        propList.add(oneProp);
    }


    /* Handler for Group/User managemenet */

    /**
     *	<p> This handler update's user info.</p>
     *  <p> Input value: "Realm" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "UserId" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "GroupList" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Password" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="saveUser",
	input={
	    @HandlerInput(name="Realm", type=String.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true),
	    @HandlerInput(name="UserId", type=String.class, required=true),
	    @HandlerInput(name="GroupList", type=String.class, required=true),
	    @HandlerInput(name="Password", type=String.class, required=true),
	    @HandlerInput(name="CreateNew", type=Boolean.class)})
    public static void saveUser(HandlerContext handlerCtx) {
        try {
            String realmName = (String) handlerCtx.getInputValue("Realm");
            String configName = (String) handlerCtx.getInputValue("configName");
            String grouplist = (String)handlerCtx.getInputValue("GroupList");
            String password = (String)handlerCtx.getInputValue("Password");
            String userid = (String)handlerCtx.getInputValue("UserId");
            Boolean createNew = (Boolean)handlerCtx.getInputValue("CreateNew");

            if (password == null) {
                password = "";
            }
            HashMap attrs = new HashMap<String, Object>();
            if ((createNew == null)) {
                String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                                            "/security-service/auth-realm/" + realmName + "/delete-user";
                attrs.put("username", userid);
                RestResponse response = RestApiHandlers.delete(endpoint, attrs);
                if (!response.isSuccess()) {
                    GuiUtil.getLogger().severe("Remove user failed.  parent=" + endpoint + "; attrs =" + attrs);
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
                }
                createNew = Boolean.TRUE;
            }
            if ((createNew != null) && (createNew == Boolean.TRUE)) {
                String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                                                "/security-service/auth-realm/" + realmName + "/create-user";
                attrs = new HashMap<String, Object>();
                attrs.put("username", userid);
                attrs.put("authrealmname", realmName);
                attrs.put("userpassword", password);
                List<String> grpList = GuiUtil.parseStringList(grouplist, ",");
                if (grpList == null) {
                    grpList = new ArrayList<String>(0);
                }
                attrs.put("groups", grpList);
                RestResponse response = RestApiHandlers.post(endpoint, attrs);
                if (!response.isSuccess()) {
                    GuiUtil.getLogger().severe("Add user failed.  parent=" + endpoint + "; attrs =" + attrs);
                    GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.error.checkLog"));
                }
            } 
        } catch(Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

   /**
     *	<p> This handler returns the attribute values in the
     *      Edit Manage User Password Page.</p>
     *  <p> Input value: "Realm" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "UserId" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "GroupList" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getUserInfo",
    input={
        @HandlerInput(name="Realm", type=String.class, required=true),
        @HandlerInput(name="configName", type=String.class, required=true),
        @HandlerInput(name="User", type=String.class, required=true)},
    output={
        @HandlerOutput(name="GroupList",     type=String.class)})

        public static void getUserInfo(HandlerContext handlerCtx) {

        String realmName = (String) handlerCtx.getInputValue("Realm");
        String userName = (String) handlerCtx.getInputValue("User");
        String configName = (String) handlerCtx.getInputValue("configName");
        handlerCtx.setOutputValue("GroupList", getGroupNames(realmName, userName, configName, handlerCtx)  );
    }

   /**
     *	<p> This handler returns the list of file users for specified realm.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getFileUsers",
        input={
            @HandlerInput(name="Realm", type=String.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getFileUsers(HandlerContext handlerCtx){
        String realmName = (String) handlerCtx.getInputValue("Realm");
        String configName = (String) handlerCtx.getInputValue("configName");
        List result = new ArrayList();
        try{
            String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                                                "/security-service/auth-realm/" + realmName + "/list-users.json";
            Map<String, Object> responseMap = RestApiHandlers.restRequest(endpoint, null, "get", handlerCtx);
            responseMap = (Map<String, Object>) responseMap.get("data");
            List<HashMap> children = (List<HashMap>) responseMap.get("children");
            if(children != null) {
                Map<String, Object> map = null;
                for (HashMap child : children) {
                    map = new HashMap<String, Object>();
                    String name = (String) child.get("message");
                    map.put("users", name);
                    map.put("groups", getGroupNames( realmName, name, configName, handlerCtx));
                    map.put("selected", false);
                    result.add(map);
                }
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
        handlerCtx.setOutputValue("result", result);
    }


  /**
     *	<p> This handler removes users for specified realm.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="removeUser",
        input={
            @HandlerInput(name="Realm", type=String.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void removeUser(HandlerContext handlerCtx){

        String error = null;
        String realmName = (String) handlerCtx.getInputValue("Realm");
        String configName = (String) handlerCtx.getInputValue("configName");
        try{
            List obj = (List) handlerCtx.getInputValue("selectedRows");
            List<Map> selectedRows = (List) obj;
            for(Map oneRow : selectedRows){
                String user = (String)oneRow.get("name");
                String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName + "/admin-service/jmx-connector/system.json";
                Map<String, Object> responseMap = RestApiHandlers.restRequest(endpoint, null, "get", handlerCtx);
                Map<String, Object> valueMap = (Map<String, Object>) responseMap.get("data");
                valueMap = (Map<String, Object>) ((Map<String, Object>) valueMap.get("extraProperties")).get("entity");
                String authRealm = (String) valueMap.get("authRealmName");
                if (realmName.equals(authRealm) && user.equals(GuiUtil.getSessionValue("userName"))){
                    error = GuiUtil.getMessage(COMMON_BUNDLE, "msg.error.cannotDeleteCurrent");
                    continue;
                }else{
                    HashMap attrs = new HashMap<String, Object>();
                    endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                                                "/security-service/auth-realm/" + realmName + "/delete-user";
                    attrs.put("name", user);
                    RestResponse response = RestApiHandlers.delete(endpoint, attrs);
                    if (!response.isSuccess()) {
                        GuiUtil.getLogger().severe("Remove user failed.  parent=" + endpoint + "; attrs =" + attrs);
                        error = GuiUtil.getMessage("msg.error.checkLog");
                    }
                }
            }
            if (error != null){
                GuiUtil.prepareAlert(handlerCtx, "error", error, null);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }


    /**
     *	<p> This handler checks to see if the current login user exists in current Realm,
     *  if it doesn't, invalidate the session.
     */
    /*  This handler is no longer used.  We already disallow the deletion of current admin user.
    @Handler(id="checkCurrentUser",
        input={
            @HandlerInput(name="Realm", type=String.class, required=true)},
        output={
            @HandlerOutput(name="endSession", type=Boolean.class)}
     )
     public static void checkCurrentUser(HandlerContext handlerCtx){
        boolean endSession = false;
        String realmName = (String) handlerCtx.getInputValue("Realm");
        AMXProxy amx = V3AMX.getInstance().getConfig("server-config").getAdminService().getJMXConnector().get("system");
        String authRealm = (String) amx.attributesMap().get("AuthRealmName");
        if (realmName.equals(authRealm)){
            String[] userNames = V3AMX.getInstance().getRealmsMgr().getUserNames(realmName);
            if (userNames == null || userNames.length ==0){
                endSession = true;
            }else{
                String currentLoginUser = (String) GuiUtil.getSessionValue("userName");
                for(int i=0; i< userNames.length; i++){
                    if(userNames[i].equals(currentLoginUser)){
                        endSession = false;
                        break;
                    }
                }
            }
        }
        if (endSession){
            ExternalContext extContext = handlerCtx.getFacesContext().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
            request.getSession().invalidate();
        }
        handlerCtx.setOutputValue("endSession", endSession);
    }
     */


     /**
     *	<p> This handler determines if a 'Manage User' button should be displayed.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="hasManageUserButton",
        input={
            @HandlerInput(name="realmName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=Boolean.class)}
     )
     public static void hasManageUserButton(HandlerContext handlerCtx){

        try{
            String realmName = (String) handlerCtx.getInputValue("realmName");
            handlerCtx.setOutputValue("result", V3AMX.getInstance().getRealmsMgr().supportsUserManagement(realmName));
        }catch(Exception ex){
            //refer to issue# 11623. Backend may throw exception if there is any issue with instantiating this realm.
            //we need to catch that and just set to false for manage user for this realm.
            handlerCtx.setOutputValue("result", false);
        }
    }

    private static String getGroupNames(String realmName, String userName, String configName, HandlerContext handlerCtx){
        try{
            String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                                                "/security-service/auth-realm/" + realmName + "/list-group-names?username=" + userName;
            Map<String, Object> responseMap = RestApiHandlers.restRequest(endpoint, null, "get", handlerCtx);
            HashMap children = (HashMap)((Map<String, Object>) responseMap.get("data")).get("extraProperties");
            String name = (String)((List)children.get("groups")).get(0);
            return name;
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
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
    private static final String COMMON_BUNDLE = "org.glassfish.common.admingui.Strings";



    @Handler(id="addDefaultProviderInfo",
        input={
            @HandlerInput(name="providerList", type=List.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="msgSecurityName", type=String.class, required=true)
    })
    public static void addDefaultProviderInfo(HandlerContext handlerCtx){
        List<HashMap> providerList = (ArrayList<HashMap>) handlerCtx.getInputValue("providerList");
        String configName = (String) handlerCtx.getInputValue("configName");
        String msgSecurityName = (String) handlerCtx.getInputValue("msgSecurityName");

        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName
                                        + "/security-service/message-security-config/" + msgSecurityName;
        Map<String, Object> valueMap = (Map<String, Object>) RestApiHandlers.getEntityAttrs(endpoint, "entity");
        String defaultProvider = (String) valueMap.get("defaultProvider");
        String defaultClientProvider = (String) valueMap.get("defaultClientProvider");
        String trueStr = GuiUtil.getMessage("common.true");
        String falseStr = GuiUtil.getMessage("common.false");
        for(Map oneRow : providerList){
            if (defaultProvider.length() > 0 || defaultClientProvider.length() > 0){
                oneRow.put("default", trueStr);
            }else{
                oneRow.put("default", falseStr);
            }
        }
    }


   @Handler(id="createMsgSecurity",
        input={
            @HandlerInput(name="attrMap", type=Map.class, required=true),
            @HandlerInput(name="propList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="providerObjName", type=String.class)}
     )
     public static void createMsgSecurity(HandlerContext handlerCtx){
        Map<String,String> attrMap = (Map<String,String>) handlerCtx.getInputValue("attrMap");

        String providerName = attrMap.get("Name");
        //setup provider attrMap
        Map providerAttrs = new HashMap();
        providerAttrs.put("Name", providerName);
        providerAttrs.put("ProviderType", attrMap.get("ProviderType"));
        providerAttrs.put("ClassName", attrMap.get("ClassName"));

        List pList = V3AMX.verifyPropertyList((List) handlerCtx.getInputValue("propList"));
        if (pList.size() > 0){
            Map[] propMaps = (Map[])pList.toArray(new Map[pList.size()]);
            providerAttrs.put(Util.deduceType(Property.class), propMaps);
        }
        //setup MsgSecurityConfig attrMap
        Map msgAttrs = new HashMap();
        msgAttrs.put("AuthLayer", attrMap.get("AuthLayer"));
        if ("true".equals(attrMap.get("defaultProvider"))){
            String type =  attrMap.get("ProviderType");
            if (type.equals("server") || type.equals("client-server")){
                msgAttrs.put("DefaultProvider", providerName);
            }
            if (type.equals("client") || type.equals("client-server")){
                msgAttrs.put("DefaultClientProvider", providerName);
            }
        }
        msgAttrs.put(Util.deduceType(ProviderConfig.class), providerAttrs);
        SecurityService ss = V3AMX.getInstance().getConfig("server-config").getSecurityService();
        AMXConfigProxy msgConfig = ss.createChild("message-security-config", msgAttrs);
        ProviderConfig provider = msgConfig.childrenMap(ProviderConfig.class).get(providerName);
        handlerCtx.setOutputValue("providerObjName", provider.objectName().toString() );
    }


    @Handler(id="getMessageSecurityAuthLayersForCreate",
        input={
            @HandlerInput(name="attrMap", type=Map.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="propList", type=List.class, required=true)},
        output={
            @HandlerOutput(name="layers", type=List.class)}
        )
    public static void getMessageSecurityAuthLayersForCreate(HandlerContext handlerCtx) throws Exception {
        List layers = new ArrayList();
        String configName = (String) handlerCtx.getInputValue("configName");
        layers.add("SOAP");
        layers.add("HttpServlet");
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName + "/security-service/message-security-config";
        Set<String> msgSecurityCfgs = (Set<String>) (RestApiHandlers.getChildMap(endpoint)).keySet();
        for(String name : msgSecurityCfgs){
            if (layers.contains(name)) {
                layers.remove(name);
            }
        }
        handlerCtx.setOutputValue("layers", layers);
    }


    @Handler(id="getProvidersByType",
        input={
            @HandlerInput(name="msgSecurityName", type=String.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true),
            @HandlerInput(name="type", type=List.class, required=true)},
        output={
            @HandlerOutput(name="result", type=List.class)})
     public static void getProvidersByType(HandlerContext handlerCtx) throws Exception {
        List type = (List) handlerCtx.getInputValue("type");
        List result = new ArrayList();
        String configName = (String) handlerCtx.getInputValue("configName");
        String msgSecurityName = (String) handlerCtx.getInputValue("msgSecurityName");
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                "/security-service/message-security-config/" + msgSecurityName + "/provider-config";
        List<String> providers = (List<String>) RestApiHandlers.getChildList(endpoint);
        for(String providerEndpoint : providers){
            Map providerAttrs = (HashMap) RestApiHandlers.getAttributesMap(providerEndpoint);
            String providerType = (String) providerAttrs.get("providerType");
            if (type.contains(providerType)) {
                result.add(com.sun.jsftemplating.util.Util.htmlEscape((String)providerAttrs.get("providerId")));
            }
        }
        result.add(0, "");
        handlerCtx.setOutputValue("result", result);
    }

    @Handler(id="saveMsgProviderInfo",
         input={
            @HandlerInput(name="attrMap", type=Map.class, required=true),
            @HandlerInput(name="configName", type=Map.class, required=true),
            @HandlerInput(name="edit", type=String.class, required=true),
            @HandlerInput(name = "propList", type = List.class)             //propList used when edit is false.
     },
        output={
            @HandlerOutput(name="objName", type=String.class)}
     )
     public static void saveMsgProviderInfo(HandlerContext handlerCtx){
        Map<String,String> attrMap = (Map<String,String>) handlerCtx.getInputValue("attrMap");
        String edit = (String)handlerCtx.getInputValue("edit");
        String providerName = attrMap.get("Name");
        String msgSecurityName = attrMap.get("msgSecurityName");
        String configName = attrMap.get("configName");
        List propList = (List) handlerCtx.getInputValue("propList");

        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/" + configName +
                                "/security-service/message-security-config/" + msgSecurityName + "/provider-config";
        String providerEndpoint = endpoint + "/" + providerName;

        if (edit.equals("true")){
            boolean providerExist = RestApiHandlers.get(providerEndpoint).isSuccess();
            if (!providerExist){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage(COMMON_BUNDLE, "msg.error.noSuchProvider")); //normally won't happen.
                return;
            }else{
                Map<String, Object> providerMap = (Map<String, Object>)RestApiHandlers.getEntityAttrs(providerEndpoint, "entity");
                providerMap.put("className", attrMap.get("ClassName"));
                providerMap.put("providerType", attrMap.get("ProviderType"));
                RestApiHandlers.sendUpdateRequest(endpoint, providerMap, null, null, null);
            }
        }else{
            Map attrs = new HashMap();
            attrs.put("providerId", attrMap.get("Name"));
            attrs.put("className", attrMap.get("ClassName"));
            attrs.put("providerType", attrMap.get("ProviderType"));
            if (propList.size() > 0){
                Map[] propMaps = (Map[])propList.toArray(new Map[propList.size()]);
                attrs.put(Util.deduceType(Property.class), propMaps);
            }
            RestApiHandlers.sendCreateRequest(endpoint, attrs, null, null, null);
        }

        //if we pass in "", backend will throw bean violation, since it only accepts certain values.
        String[] attrList= new String[] {"Request-AuthSource","Request-AuthRecipient", "Response-AuthSource", "Response-AuthRecipient"};
        for(int i=0; i< attrList.length; i++){
            if ("".equals(attrMap.get(attrList[i]))){
                attrMap.put( attrList[i], null);
            }
        }

        Map reqPolicyMap = new HashMap();
        reqPolicyMap.put("authSource", attrMap.get("Request-AuthSource"));
        reqPolicyMap.put("authRecipient", attrMap.get("Request-AuthRecipient"));
        String reqPolicyEP = providerEndpoint + "/request-policy";
        RestApiHandlers.sendUpdateRequest(reqPolicyEP, reqPolicyMap, null, null, null);

        Map respPolicyMap = new HashMap();
        respPolicyMap.put("authSource", attrMap.get("Response-AuthSource"));
        respPolicyMap.put("authRecipient", attrMap.get("Response-AuthRecipient"));
        String respPolicyEP = providerEndpoint + "/response-policy";
        RestApiHandlers.sendUpdateRequest(respPolicyEP, respPolicyMap, null, null, null);
    }


    @Handler(id="checkMsgSecurityDefaultProvider",
         input={
            @HandlerInput(name="msgSecurityName", type=String.class, required=true)
     })
     public static void checkMsgSecurityDefaultProvider(HandlerContext handlerCtx){
        String msgSecurityName = (String) handlerCtx.getInputValue("msgSecurityName");
        MessageSecurityConfig msgConfig = getMsgSecurityProxy(msgSecurityName);
        String defServer = msgConfig.getDefaultProvider();
        if ( !GuiUtil.isEmpty(defServer)){
            if (msgConfig.childrenMap(ProviderConfig.class).get(defServer) == null){
                msgConfig.setDefaultProvider(null);
            }
        }
        String defClient = msgConfig.getDefaultClientProvider();
        if ( !GuiUtil.isEmpty(defClient)){
            if (msgConfig.childrenMap(ProviderConfig.class).get(defClient) == null){
                msgConfig.setDefaultClientProvider(null);
            }
        }
    }


    @Handler(id="saveSecurityManagerValue",
         input={
            @HandlerInput(name="configName", type=String.class),
            @HandlerInput(name="value", type=String.class, required=true)
     })
     public static void saveSecurityManagerValue(HandlerContext handlerCtx){
        String configName = (String) handlerCtx.getInputValue("configName");
        if (GuiUtil.isEmpty(configName))
            configName = "server-config";
        JavaConfig javaC = V3AMX.getInstance().getConfig(configName).getJavaConfig();
        Boolean status = isSecurityManagerEnabled(javaC);
        String value= (String) handlerCtx.getInputValue("value");
        Boolean userValue = new Boolean(value);
        if (status.equals(userValue)){
            //no need to change
            return;
        }

        ArrayList newOptions = new ArrayList();
        String[] origOptions = javaC.getJvmOptions();
        if(userValue){
            for(int i=0; i<origOptions.length; i++){
                newOptions.add(origOptions[i]);
            }
            newOptions.add(JVM_OPTION_SECURITY_MANAGER);
        }else{
            for(int i=0; i<origOptions.length; i++){
                if (! (origOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                        origOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL))){
                   newOptions.add(origOptions[i]);
                }
            }
        }
        String[] jvmOptions = (String[])newOptions.toArray(new String[0]);
        javaC.setJvmOptions(jvmOptions);
    }

    @Handler(id="getSecurityManagerValue",
         input={
            @HandlerInput(name="configName", type=String.class)},
        output={
            @HandlerOutput(name="value", type=String.class)}
     )
     public static void getSecurityManagerValue(HandlerContext handlerCtx){
        String configName = (String) handlerCtx.getInputValue("configName");
        if (GuiUtil.isEmpty(configName))
            configName = "server-config";
        JavaConfig javaC = V3AMX.getInstance().getConfig(configName).getJavaConfig();
        handlerCtx.setOutputValue("value",  isSecurityManagerEnabled(javaC).toString());
    }

    private static Boolean isSecurityManagerEnabled(JavaConfig javaC){
        final String[] jvmOptions = javaC.getJvmOptions();
        for(int i=0; i<jvmOptions.length; i++){
            if (jvmOptions[i].trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                    jvmOptions[i].trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Handler(id="getSecurityManagerValue2",
         input={
            @HandlerInput(name="endpoint", type=String.class),
            @HandlerInput(name="attrs", type=Map.class, required=false)},
        output={
            @HandlerOutput(name="value", type=String.class)}
     )
     public static void getSecurityManagerValue2(HandlerContext handlerCtx){
        ArrayList<String> list = InstanceHandler.getJvmOptions(handlerCtx);
        handlerCtx.setOutputValue("value",  isSecurityManagerEnabled(list).toString());
    }

    private static Boolean isSecurityManagerEnabled(List<String> jvmOptions){
        for(String jvmOption : jvmOptions){
            if (jvmOption.trim().equals(JVM_OPTION_SECURITY_MANAGER) ||
                    jvmOption.trim().startsWith(JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    private static MessageSecurityConfig getMsgSecurityProxy(String msgSecurityName){
        Set<AMXProxy> pSet = V3AMX.getInstance().getDomainRoot().getQueryMgr().queryTypeName("message-security-config", msgSecurityName);
        for(AMXProxy msgProxy : pSet){
            //should be just one.
            return (MessageSecurityConfig) msgProxy.as(MessageSecurityConfig.class);
        }
        return null;
    }

    private static String str(String aa){
        return (aa==null) ? "" : aa;
    }

    private static final String JVM_OPTION_SECURITY_MANAGER = "-Djava.security.manager";
    private static final String JVM_OPTION_SECURITY_MANAGER_WITH_EQUAL = "-Djava.security.manager=";
    
}
