/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;  
import com.sun.jsftemplating.annotation.HandlerInput; 
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;  
import java.util.ArrayList;
import java.util.HashMap;
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
        List<Map<String,String>> propList = (List)handlerCtx.getInputValue("propList");
        Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");

        if (attrMap==null){
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

             Map[] propMaps = new Map[propList.size()];
             int i=0;
             for(Map oneProp: propList){
                 if (oneProp.get("selected") != null){
                     oneProp.remove("selected");
                 }
                 propMaps[i++] = oneProp;
             }
             cMap.put(Util.deduceType(Property.class), propMaps);
             AMXConfigProxy amx = V3AMX.getInstance().getConfig("server-config").getSecurityService();
             AMXConfigProxy child =  amx.createChild("auth-realm", cMap);
//             V3AMX.setProperties(child.objectName().toString(), propList, false);
          }

      }catch(Exception ex){
          GuiUtil.handleException(handlerCtx, ex);
      }
    }
    
    
    static public void putOptional(Map<String,String> attrMap, List propList, String propName, String key)
    {
        Map oneProp = new HashMap();
        oneProp.put(PROPERTY_NAME, propName);
        String value = attrMap.get(key);
        if (GuiUtil.isEmpty(value))
            return;
        oneProp.put(PROPERTY_VALUE, attrMap.get(key));
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
	    @HandlerInput(name="UserId", type=String.class, required=true),
	    @HandlerInput(name="GroupList", type=String.class, required=true),
	    @HandlerInput(name="Password", type=String.class, required=true),
	    @HandlerInput(name="CreateNew", type=Boolean.class)})
    public static void saveUser(HandlerContext handlerCtx) {
        try {
            String realmName = (String) handlerCtx.getInputValue("Realm");
            String grouplist = (String)handlerCtx.getInputValue("GroupList");
            String[] groups = GuiUtil.stringToArray(grouplist, ",");
            String password = (String)handlerCtx.getInputValue("Password");
            String userid = (String)handlerCtx.getInputValue("UserId");
            Boolean createNew = (Boolean)handlerCtx.getInputValue("CreateNew");

            if (password == null) {
                password = "";
            }
            if ((createNew != null) && (createNew == Boolean.TRUE)) {
                V3AMX.getInstance().getRealmsMgr().addUser(realmName, userid, password, groups);
            } else {
                V3AMX.getInstance().getRealmsMgr().updateUser(realmName, userid, userid, password, groups);
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
        @HandlerInput(name="User", type=String.class, required=true)},
    output={
        @HandlerOutput(name="GroupList",     type=String.class)})

        public static void getUserInfo(HandlerContext handlerCtx) {

        String realmName = (String) handlerCtx.getInputValue("Realm");
        String userName = (String) handlerCtx.getInputValue("User");
        handlerCtx.setOutputValue("GroupList", getGroupNames(realmName,userName)  );
    }

   /**
     *	<p> This handler returns the list of file users for specified realm.
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getFileUsers",
        input={
            @HandlerInput(name="Realm", type=String.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void getFileUsers(HandlerContext handlerCtx){
        String realmName = (String) handlerCtx.getInputValue("Realm");
        List result = new ArrayList();
        try{
            String[] userNames = V3AMX.getInstance().getRealmsMgr().getUserNames(realmName);
            if(userNames != null) {
                Map<String, Object> map = null;
                for (int i=0; i< userNames.length; i++) {
                    map = new HashMap<String, Object>();
                    map.put("users", userNames[i]);
                    map.put("groups", getGroupNames( realmName, userNames[i]));
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
            @HandlerInput(name="selectedRows", type=List.class, required=true)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
     public static void removeUser(HandlerContext handlerCtx){

        String error = null;
        String realmName = (String) handlerCtx.getInputValue("Realm");
        try{
            List obj = (List) handlerCtx.getInputValue("selectedRows");
            List<Map> selectedRows = (List) obj;
            for(Map oneRow : selectedRows){
                String user = (String)oneRow.get("users");
                AMXProxy amx = V3AMX.getInstance().getConfig("server-config").getAdminService().getJmxConnector().get("system");
                String authRealm = (String) amx.attributesMap().get("AuthRealmName");
                if (realmName.equals(authRealm) && user.equals(GuiUtil.getSessionValue("userName"))){
                    error = GuiUtil.getMessage(COMMON_BUNDLE, "msg.error.cannotDeleteCurrent");
                    continue;
                }else{
                    V3AMX.getInstance().getRealmsMgr().removeUser(realmName, user);
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

        String realmName = (String) handlerCtx.getInputValue("realmName");
        handlerCtx.setOutputValue("result", V3AMX.getInstance().getRealmsMgr().supportsUserManagement(realmName));
    }

    private static String getGroupNames(String realmName, String userName){
        try{
            return GuiUtil.arrayToString(V3AMX.getInstance().getRealmsMgr().getGroupNames(realmName, userName), ",");
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
            @HandlerInput(name="msgSecurityName", type=String.class, required=true)
    })
    public static void addDefaultProviderInfo(HandlerContext handlerCtx){
        List<Map> providerList = (List<Map>) handlerCtx.getInputValue("providerList");
        MessageSecurityConfig msgConfig = getMsgSecurityProxy((String) handlerCtx.getInputValue("msgSecurityName"));
        String defaultProvider = msgConfig.getDefaultProvider();
        String defaultClientProvider = msgConfig.getDefaultClientProvider();
        String trueStr = GuiUtil.getMessage("common.true");
        String falseStr = GuiUtil.getMessage("common.false");
        for(Map oneRow : providerList){
            String name = (String)oneRow.get("Name");
            if (name.equals(defaultProvider) || name.equals(defaultClientProvider)){
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
        output={
            @HandlerOutput(name="layers", type=List.class)}
        )
    public static void getMessageSecurityAuthLayersForCreate(HandlerContext handlerCtx){
        List layers = new ArrayList();
        layers.add("SOAP");
        layers.add("HttpServlet");
        Set<AMXProxy> pSet = V3AMX.getInstance().getDomainRoot().getQueryMgr().queryType("message-security-config");
        for(AMXProxy msgProxy : pSet){
            layers.remove(msgProxy.getName());
        }
        handlerCtx.setOutputValue("layers", layers);
    }


    @Handler(id="getProvidersByType",
        input={
            @HandlerInput(name="msgSecurityName", type=String.class, required=true),
            @HandlerInput(name="type", type=List.class, required=true)},
        output={
            @HandlerOutput(name="result", type=List.class)})
     public static void getProvidersByType(HandlerContext handlerCtx){
        List type = (List) handlerCtx.getInputValue("type");
        List result = new ArrayList();
        MessageSecurityConfig msgConfig = getMsgSecurityProxy((String) handlerCtx.getInputValue("msgSecurityName"));
        Map<String, ProviderConfig> providers = msgConfig.childrenMap(ProviderConfig.class);
        for(ProviderConfig pp : providers.values()){
            if (type.contains(pp.getProviderType())){
                result.add(com.sun.jsftemplating.util.Util.htmlEscape(pp.getName()));
            }
        }
        result.add(0, "");
        handlerCtx.setOutputValue("result", result);
    }

    @Handler(id="getMsgProviderInfo",
        input={
            @HandlerInput(name="providerName", type=String.class, required=true),
            @HandlerInput(name="msgSecurityName", type=String.class, required=true),
            @HandlerInput(name="configName", type=String.class, required=true)
    },
        output={
            @HandlerOutput(name="attrMap", type=Map.class)}
     )
     public static void getMsgProviderInfo(HandlerContext handlerCtx){
        String providerName = (String) handlerCtx.getInputValue("providerName");
        String msgSecurityName = (String) handlerCtx.getInputValue("msgSecurityName");
        String configName = (String) handlerCtx.getInputValue("configName");
        MessageSecurityConfig msgConfig = getMsgSecurityProxy(msgSecurityName);
        ProviderConfig provider = msgConfig.childrenMap(ProviderConfig.class).get(providerName);
        Map attrMap = new HashMap();
        attrMap.put("msgSecurityName", msgSecurityName);
        attrMap.put("configName", configName);
        attrMap.put("Name", providerName);
        attrMap.put("ProviderType", provider.getProviderType());
        attrMap.put("ClassName", provider.getClassName());
        if (provider.getRequestPolicy()!= null){
            attrMap.put("Request-AuthSource",  str(provider.getRequestPolicy().getAuthSource()));
            attrMap.put("Request-AuthRecipient",  str(provider.getRequestPolicy().getAuthRecipient()));
        }
        if (provider.getResponsePolicy()!= null){
            attrMap.put("Response-AuthSource",  str(provider.getResponsePolicy().getAuthSource()));
            attrMap.put("Response-AuthRecipient",  str(provider.getResponsePolicy().getAuthRecipient()));
        }
        if (providerName.equals(msgConfig.getDefaultClientProvider()) || (providerName.equals(msgConfig.getDefaultProvider()))){
            attrMap.put("defaultProvider", "true");
        }
        handlerCtx.setOutputValue("attrMap",  attrMap);
    }


    @Handler(id="saveMsgProviderInfo",
         input={
            @HandlerInput(name="attrMap", type=Map.class, required=true),
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

        
        MessageSecurityConfig msgConfig = getMsgSecurityProxy(msgSecurityName);

        ProviderConfig provider = msgConfig.childrenMap(ProviderConfig.class).get(providerName);
        if (edit.equals("true")){
            if (provider == null){
                GuiUtil.handleError(handlerCtx, GuiUtil.getMessage(COMMON_BUNDLE, "msg.error.noSuchProvider")); //normally won't happen.
                return;
            }else{
                provider.setClassName(attrMap.get("ClassName"));
                provider.setProviderType(attrMap.get("ProviderType"));
            }
        }else{
            Map attrs = new HashMap();
            attrs.put("Name", attrMap.get("Name"));
            attrs.put("ClassName", attrMap.get("ClassName"));
            attrs.put("ProviderType", attrMap.get("ProviderType"));
            List pList = V3AMX.verifyPropertyList(propList);
            if (pList.size() > 0){
                Map[] propMaps = (Map[])pList.toArray(new Map[pList.size()]);
                attrs.put(Util.deduceType(Property.class), propMaps);
            }
            msgConfig.createChild("provider-config", attrs);
            provider = msgConfig.childrenMap(ProviderConfig.class).get(providerName);
        }

        String def = attrMap.get("defaultProvider");
        if (def == null){
            if (providerName.equals(msgConfig.getDefaultProvider())){
                msgConfig.setDefaultProvider("");
            }
            if (providerName.equals(msgConfig.getDefaultClientProvider())) {
                msgConfig.setDefaultClientProvider("");
            }
        }else{
            String type = provider.getProviderType();
            if (type.equals("server") || type.equals("client-server")){
                msgConfig.setDefaultProvider(providerName);
            }
            if (type.equals("client") || type.equals("client-server")){
                msgConfig.setDefaultClientProvider(providerName);
            }
        }

        //if we pass in "", backend will throw bean violation, since it only accepts certain values.
        String[] attrList= new String[] {"Request-AuthSource","Request-AuthRecipient", "Response-AuthSource", "Response-AuthRecipient"};
        for(int i=0; i< attrList.length; i++){
            if ("".equals(attrMap.get(attrList[i]))){
                attrMap.put( attrList[i], null);
            }
        }

        if ( provider.getRequestPolicy()== null){
            if (GuiUtil.isEmpty(attrMap.get("Request-AuthSource")) && GuiUtil.isEmpty(attrMap.get("Request-AuthRecipient"))){
                //no need to create one if all is empty.
            }else{
                Map attrs = new HashMap();
                attrs.put("authSource", attrMap.get("Request-AuthSource"));
                attrs.put("AuthRecipient", attrMap.get("Request-AuthRecipient"));
                provider.createChild("request-policy", attrs);
            }
        }else{
            provider.getRequestPolicy().setAuthSource(attrMap.get("Request-AuthSource"));
            provider.getRequestPolicy().setAuthRecipient(attrMap.get("Request-AuthRecipient"));
        }


        if ( provider.getResponsePolicy()== null){
            if (GuiUtil.isEmpty(attrMap.get("Response-AuthSource")) && GuiUtil.isEmpty(attrMap.get("Response-AuthRecipient"))){
                //no need to create one if all is empty.
            }else{
                Map attrs = new HashMap();
                attrs.put("authSource", attrMap.get("Response-AuthSource"));
                attrs.put("AuthRecipient", attrMap.get("Response-AuthRecipient"));
                provider.createChild("response-policy", attrs);
            }
        }else{
            provider.getResponsePolicy().setAuthSource(attrMap.get("Response-AuthSource"));
            provider.getResponsePolicy().setAuthRecipient(attrMap.get("Response-AuthRecipient"));
        }
        handlerCtx.setOutputValue("objName",  provider.objectName().toString());
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
