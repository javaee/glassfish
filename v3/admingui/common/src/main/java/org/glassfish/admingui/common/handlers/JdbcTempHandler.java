
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

/*
 * JdbcHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author anilam
 */

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Set;


import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;


public class JdbcTempHandler {
    /** Creates a new instance of JdbcHandler */
    public JdbcTempHandler() {
    }
    
            
        /**
         *	<p> This handler pings the  Jdbc Connection Pool
         */
        @Handler(id="pingJdbcConnectionPool",
            input={
                @HandlerInput(name="jndiName", type=String.class, required=true)})
        public static void pingConnectionPool(HandlerContext handlerCtx) {

            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            try {

                Map<String, Object> statusMap = V3AMX.getInstance().getConnectorRuntime().pingJDBCConnectionPool(jndiName);
                 
                if ((Boolean) statusMap.get(BOOLEAN_KEY)){
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.PingSucceed"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), statusMap.get(REASON_FAILED_KEY).toString() );
               }

            }catch(Exception ex){
		GuiUtil.handleException(handlerCtx, ex);
            }
        }        
    
        /**
         *	<p> This handler flushes the  Jdbc Connection Pool
         */
        @Handler(id="flushConnectionPool",
            input={
                @HandlerInput(name="jndiName", type=String.class, required=true)})
        public static void flushConnectionPool(HandlerContext handlerCtx) {

            String jndiName = (String) handlerCtx.getInputValue("jndiName");
            try {

                Map<String, Object> statusMap = V3AMX.getInstance().getConnectorRuntime().flushConnectionPool(jndiName);
                 
                if ((Boolean) statusMap.get(BOOLEAN_KEY)){
                    GuiUtil.prepareAlert(handlerCtx,"success", GuiUtil.getMessage("msg.FlushSucceed"), null);
                }else{
                    GuiUtil.prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), statusMap.get(REASON_FAILED_KEY).toString() );
               }

            }catch(Exception ex){
		GuiUtil.handleException(handlerCtx, ex);
            }
        }    
        
    /*
     * This handler returns a list of table names.
     */
    @Handler(id = "getTableNames",
        input = {
            @HandlerInput(name = "name", type = String.class, required = true)},
        output = {
            @HandlerOutput(name = "result", type = List.class)})
    public static void getTableNames(HandlerContext handlerCtx) {
        try {
            String name = (String) handlerCtx.getInputValue("name");
            List result = new ArrayList();
            Map<String, Object> tn = V3AMX.getInstance().getConnectorRuntime().getValidationTableNames(name);
            if (tn != null) {
                Set keys = (Set) tn.get(SET_KEY);
                if (keys != null) {
                   Iterator iter = keys.iterator();
                   while (iter.hasNext()) {
                        result.add(iter.next());
                    }
                }
            }
            handlerCtx.setOutputValue("result", result);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
        
        
        /**
         *	<p> This handler gets the default values and resource type and puts them in session
         */
        @Handler(id="setJDBCPoolWizard",
        input={
                @HandlerInput(name="fromStep2", type=Boolean.class),
                @HandlerInput(name = "attrMap", type = Map.class)},
        output={
            @HandlerOutput(name="ResTypeList", type=java.util.List.class),
            @HandlerOutput(name="DBVendorList", type=java.util.List.class)
            } )
        public static void setJDBCPoolWizard(HandlerContext handlerCtx){
            //We need to use 2 maps for JDBC Connection Pool creation because there are extra info we need to keep track in 
            //the wizard, but cannot be passed to the creation API.
            
            Boolean fromStep2 = (Boolean) handlerCtx.getInputValue("fromStep2");
            if ((fromStep2!= null) && fromStep2){
                //wizardPool is already in session map
            }else{
                Map attrMap = (Map) handlerCtx.getInputValue("attrMap");
                Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
                sessionMap.put("valueMap", attrMap);
                sessionMap.put("wizardPoolExtra", new HashMap());
                //sessionMap.put("wizardPoolProperties", new HashMap());
            }
            handlerCtx.setOutputValue("ResTypeList", resTypeList);
            handlerCtx.setOutputValue("DBVendorList", dbVendorList);
        }
        
    /**
     *	<p> This handler gets the datasource classname and properties and sets them in session
     */
    @Handler(id = "updateJDBCPoolWizardStep1")
    public static void updateJDBCPoolWizardStep1(HandlerContext handlerCtx) {
        //Map pool = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPool");
        Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");

        String resType = (String) extra.get("ResType");
        String dbVendor = (String) extra.get("DBVendor");

        String previousResType = (String) extra.get("PreviousResType");
        String previousDB = (String) extra.get("PreviousDB");

        if (resType.equals(previousResType) && dbVendor.equals(previousDB) &&
                !GuiUtil.isEmpty((String) extra.get("DatasourceClassname"))) {
        //User didn't change type and DB, keep the datasource classname as the same.
        } else {

            if (!GuiUtil.isEmpty(resType) && !GuiUtil.isEmpty(dbVendor)) {
                try {
                    String classname = "";
                    Map<String, Object> dcn = V3AMX.getInstance().getConnectorRuntime().getJdbcDriverClassNames(dbVendor, resType);
                    if (dcn != null) {
                        Set keys = (Set) dcn.get(SET_KEY);
                        Iterator iter = keys.iterator();
                        while (iter.hasNext()) {
                            classname = (String) iter.next();

                        }
                    }
                    if (resType.equals(DRIVER)) {
                        extra.put("DriverClassname", classname);
                        extra.put("DatasourceClassname", "");
                        extra.put("dsClassname", Boolean.FALSE);
                    } else {
                        extra.put("DatasourceClassname", classname);
                        extra.put("DriverClassname", "");
                        extra.put("dsClassname", Boolean.TRUE);
                    }
                    List<Map<String, String>> noprops = new ArrayList<Map<String, String>>();
                    if (!GuiUtil.isEmpty(classname)) {
                        Map result = (Map) V3AMX.getInstance().getConnectorRuntime().getConnectionDefinitionPropertiesAndDefaults(classname, resType);
                        if (result != null) {
                            Map<String, String> props = (Map) result.get(PROPERTY_MAP_KEY);
                            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", GuiUtil.convertMapToListOfMap(props));

                        } 
                    } else {
                        handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("wizardPoolProperties", noprops);
                    }
                    

                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }

            extra.put("PreviousResType", resType);
            extra.put("PreviousDB", dbVendor);

        }
    }

    /**
     *	<p> updates the wizard map properties on step 2
     */
    @Handler(id = "updateJdbcConnectionPoolWizardStep2")
    public static void updateJdbcConnectionPoolWizardStep2(HandlerContext handlerCtx) {
        Map extra = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("wizardPoolExtra");
        Map attrs = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("valueMap");

        String resType = (String) extra.get("ResType");
        String classname = (String) extra.get("DatasourceClassname");
        String driver = (String) extra.get("DriverClassname");
        String name = (String) extra.get("Name");

        attrs.put("Name", name);
        attrs.put("DatasourceClassname", classname);
        attrs.put("DriverClassname", driver);
        attrs.put("ResType", resType);
    }   

    
        public static final  String REASON_FAILED_KEY = "ReasonFailedKey";   
        public static final  String SET_KEY = "SetKey";
        public static final  String BOOLEAN_KEY = "BooleanKey";
        static private final String PROPERTY_MAP_KEY = "PropertyMapKey"; 
        static private final String DATA_SOURCE = "javax.sql.DataSource";
	static private final String XADATA_SOURCE = "javax.sql.XADataSource";
	static private final String CCDATA_SOURCE = "javax.sql.ConnectionPoolDataSource";
        static private final String DRIVER = "java.sql.Driver";

	static private final String JAVADB = "JavaDB";
	static private final String ORACLE = "Oracle";
	static private final String DERBY = "Derby";
	static private final String SYBASE = "Sybase";
	static private final String DB2 = "DB2";
	static private final String POINTBASE = "PointBase";
	static private final String POSTGRESQL = "PostgreSQL";
	static private final String INFORMIX = "Informix";
	static private final String CLOUDSCAPE = "Cloudscape";
	static private final String MSSQL = "Microsoft SQL Server";
	static private final String MYSQL = "MySQL";
        static private List resTypeList = new ArrayList();
        static private List dbVendorList = new ArrayList();
        
    
        static {
        
        resTypeList.add("");
        resTypeList.add(DATA_SOURCE);
        resTypeList.add(XADATA_SOURCE);
        resTypeList.add(CCDATA_SOURCE);
        resTypeList.add(DRIVER);
        
        dbVendorList.add("");
        dbVendorList.add(JAVADB);
        dbVendorList.add(ORACLE);
        dbVendorList.add(DERBY);
        dbVendorList.add(SYBASE);
        dbVendorList.add(DB2);
        dbVendorList.add(POINTBASE);
        dbVendorList.add(POSTGRESQL);
        dbVendorList.add(INFORMIX);
        dbVendorList.add(CLOUDSCAPE);
        dbVendorList.add(MSSQL);
        dbVendorList.add(MYSQL);
    }

}
        
 
