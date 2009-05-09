
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
 * FileUserHandler.java
 *
 * Created on July 20, 2006, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.security.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


import org.glassfish.admingui.common.util.AMXRoot;
import com.sun.appserv.management.ext.realm.RealmsMgr;

import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import org.glassfish.admingui.common.util.GuiUtil;


/**
 *
 * @author anilam
 */
public class FileUserHandler {
    
    /**
     *	<p> This handler returns the attribute values in the
     *      Admin Password Page.</p>
     *  <p> Input value: "ConfigName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Realm" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "UserId" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "GroupList" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    /*
    @Handler(id="getUser",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="Realm", type=String.class, required=true)    },
    output={
        @HandlerOutput(name="UserId",     type=String.class),
        @HandlerOutput(name="GroupList",     type=String.class)})

        public static void getUser(HandlerContext handlerCtx) {
        ExternalContext extContext = handlerCtx.getFacesContext().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) extContext.getRequest();   //we only deal with servlet, not Portlet
        String user = request.getRemoteUser(); 
        handlerCtx.setOutputValue("UserId", user);
        
        //Group Lists
        ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        SecurityServiceConfig sConfig = config.getSecurityServiceConfig();
        Map<String,AuthRealmConfig>realms = sConfig.getAuthRealmConfigMap();
        StringBuffer groupList = new StringBuffer();
        AuthRealmConfig aRealm = (AuthRealmConfig)realms.get((String)handlerCtx.getInputValue("Realm"));
        String[] gl = aRealm.getUserGroupNames(user);
        for(int i=0; i< gl.length; i++) {
            groupList.append(","+gl[i]);
        }
        if (groupList.length() > 0)
            groupList.deleteCharAt(0);  
        handlerCtx.setOutputValue("GroupList", groupList.toString());
    }
     */
    
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
        @HandlerInput(name="Password", type=String.class, required=true)})

        public static void saveUser(HandlerContext handlerCtx) {
        try{
            
            RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
            String realmName = (String) handlerCtx.getInputValue("Realm");
            String grouplist = (String)handlerCtx.getInputValue("GroupList");
            String[] groups = GuiUtil.stringToArray(grouplist, ",");
            String password = (String)handlerCtx.getInputValue("Password");
            String userid = (String)handlerCtx.getInputValue("UserId");
            realmsMgr.updateUser(realmName, userid, userid, password, groups);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    } 
    
    /**
     *	<p> This handler adds user to specified Realm
     *      Page.</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="addUser",
    input={
        @HandlerInput(name="Realm", type=String.class, required=true),
        @HandlerInput(name="UserId", type=String.class, required=true),
        @HandlerInput(name="GroupList", type=String.class, required=true),
        @HandlerInput(name="Password", type=String.class, required=true)})

        public static void addUser(HandlerContext handlerCtx) {
        try{
            RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
            String realmName = (String)handlerCtx.getInputValue("Realm");
            String grouplist = (String)handlerCtx.getInputValue("GroupList");
            String[] groups = GuiUtil.stringToArray(grouplist, ",");
            String password = (String)handlerCtx.getInputValue("Password");
            String userid = (String)handlerCtx.getInputValue("UserId");
            realmsMgr.addUser(realmName, userid, password, groups);
        }catch(Exception ex){
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
            RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
            String[] userNames = realmsMgr.getUserNames(realmName);
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
        
        RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
        String realmName = (String) handlerCtx.getInputValue("Realm");
        try{
            List obj = (List) handlerCtx.getInputValue("selectedRows");
            List<Map> selectedRows = (List) obj;
            for(Map oneRow : selectedRows){
                String user = (String)oneRow.get("users");
                realmsMgr.removeUser(realmName,user);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    } 
    
    
    /**
     *	<p> This handler checks to see if the current login user exists in current Realm,
     *  if it doesn't, invalidate the session.
     */
    @Handler(id="checkCurrentUser",
        input={
            @HandlerInput(name="Realm", type=String.class, required=true)},
        output={
            @HandlerOutput(name="endSession", type=Boolean.class)}
     )
     public static void checkCurrentUser(HandlerContext handlerCtx){
        boolean endSession = true;
        String realmName = (String) handlerCtx.getInputValue("Realm");
        RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
        if (realmName.equals("admin-realm")){
            String[] userNames = realmsMgr.getUserNames(realmName);
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
        RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
        handlerCtx.setOutputValue("result", realmsMgr.supportsUserManagement(realmName));
    }
    
    private static String getGroupNames(String realmName, String userName){
        try{
            RealmsMgr realmsMgr = AMXRoot.getInstance().getRealmsMgr();
            return GuiUtil.arrayToString(realmsMgr.getGroupNames(realmName, userName), ",");
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }
           
}
