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

/** MetaData.java

 *This class provides the static data required for editing WebApp Data Descriptors
 *Author gaur , created on November 28 5:25 PM
 */
package com.sun.enterprise.tools.common.dd.webapp;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.jar.*;
import org.netbeans.modules.schema2beans.Schema2BeansException;

import com.sun.enterprise.tools.common.dd.webapp.data.*;
import com.sun.enterprise.tools.common.dd.ParamData;

public class MetaData {

static final java.util.ResourceBundle helpBundle = java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.HelpIDBundle"); // NOI18N
    // Static constants for Cache
    public static final int    CMAPNUMBCOLS = 8;
    public static final int    CMAPCACHETARGET = 0;
    public static final int    CMAPCACHETARGETVALUE = 1;
    public static final int    CMAPHTTPMETHOD = 2;
    public static final int    CMAPKEYFIELD = 3;
    public static final int    CMAPFIELDCONSTRAINT = 4;
    public static final int    CMAPTIMEOUT = 5;
    public static final int    CMAPTIMEOUTNAME = 6;
    public static final int    CMAPTIMEOUTSCOPE = 7;

    public static final String DESCRIPTION = "Description"; // NOI18N
    public static final String NAME = "Name"; // NOI18N
    public static final String VALUE = "Value"; // NOI18N
    public static final String DEFAULT_LOCALE = "DefaultLocale"; // NOI18N
    public static final String LOCALE = "Locale"; // NOI18N
    public static final String CHARSET = "Charset"; // NOI18N
    public static final String AGENT = "Agent"; // NOI18N
    public static final String PERSISTENCE_TYPE = "PersistenceType"; // NOI18N

    public static final String LESSER = "lesser"; // NOI18N
    public static final String GREATER = "greater"; // NOI18N
    
    public static final String CLASS_NAME = "ClassName"; // NOI18N

    public static final String [] CACHE_SCOPE = {
	"request.attribute", // NOI18N
	"request.header", // NOI18N
	"request.parameter", // NOI18N
	"session.attribute", // NOI18N
	"request.cookie", // NOI18N
	"context.attribute" // NOI18N
    };

    public static final String [] CACHE_KEY_SCOPE = {
	"request.parameter", // NOI18N
	"request.header", // NOI18N
	"request.cookie", // NOI18N
	"context.attribute", // NOI18N
        "session.id", //NOI18N
	"session.attribute" // NOI18N
    };

    public static final String CACHE_MAX_ENTRIES = "4096"; // NOI18N
    public static final String CACHE_TIMEOUT = "30"; // NOI18N

    public static final String [] CACHE_KEY_BOOLEAN = {
        "true", // NOI18N
	"false" // NOI18N
    };

    public static final String [] CACHE_KEY_EXPR = {
	"equals", // NOI18N
	"greater", // NOI18N
	"lesser", // NOI18N
	"not-equals" // NOI18N
    };

    public static final String MATCH_ALL_VALUE = "*"; // NOI18N

    public static final String [] CACHE_TARGET = {
	"ServletName", // NOI18N
	"URLPattern" // NOI18N
    };
    public static final String CACHE_TARGET_SERVLET = "ServletName"; // NOI18N
    public static final String CACHE_TARGET_URL = "URLPattern"; // NOI18N

    public static final String [] CACHE_REF = {
	"CacheHelperRef", // NOI18N
	"CachePolicyRef" // NOI18N
    };
    public static final String CACHE_HELPER_STR = "CacheHelperRef"; // NOI18N
    public static final String CACHE_POLICY_OBJ = "CachePolicyRef"; // NOI18N

    public static final String CACHE_REF_DEFAULT_VAL = "defaultCacheRefValue"; // NOI18N

    public static final String [] HTTP_METHODS = {
	"GET", // NOI18N
	"POST" // NOI18N
    };

    public static final int EXTRAPARAMS = 1;
    public static final int SESSIONPARAM = 2;
    public static final int STOREPARAM = 3;
    public static final int JSPPARAM = 4;
    public static final int COOKIEPARAM = 5;
    public static final int MANAGERPARAM = 6;
    public static final int PERSISTPARAM = 7;
    public static final int HELPERCLASSPARAM = 8;

    // Cache Object Types
    public static final int CACHE_HELPER = 9;
    public static final int DEFAULT_HELPER = 10;
    public static final int CACHE_PROPERTY_ARRAY = 11;
    public static final int CACHE_MAP = 12;
    public static final int CACHE_MAP_HTTP_METHOD = 13;
    public static final int CACHE_MAP_KEY_FIELD = 14;
    public static final int CACHE_MAP_CONSTRAINT_FIELD = 15;
    public static final int CACHE_MAP_CONSTRAINT_FIELD_VALUE = 16;

    // Cache Helper Table Columns
    public static final int CACHE_HELPER_NUMB_COLS = 3;
    public static final int CACHE_HELPER_COL_NAME = 0;
    public static final int CACHE_HELPER_COL_CLASS_NAME = 1;
    public static final int CACHE_HELPER_COL_PROPERTY = 2;

    // Cache Mapping Table Columns
    public static final int CACHE_MAP_NUMB_COLS = 4;
    public static final int CACHE_MAP_COL_TGT = 0;
    public static final int CACHE_MAP_COL_TGTVAL = 1;
    public static final int CACHE_MAP_COL_REF = 2;
    public static final int CACHE_MAP_COL_REFVAL = 3;

    // Cache Constraint Field Table Columns
    public static final int CACHE_CF_NUMB_COLS = 5;
    public static final int CACHE_CF_COL_NAME = 0;
    public static final int CACHE_CF_COL_SCOPE = 1;
    public static final int CACHE_CF_COL_COM = 2;
    public static final int CACHE_CF_COL_COMF = 3;
    public static final int CACHE_CF_COL_VALUE = 4;

    // Cache Object Names
    public static final String WEB_PROPERTY_ARRAY_WRAPPER_STR = "WebPropertyArrayWrapper"; // NOI18N
    public static final String CACHE_PROPERTY_ARRAY_STR = "CachePropertyArray"; // NOI18N
    public static final String CACHE_HELPER_ARRAY = "CacheHelperArray"; // NOI18N
    public static final String CACHE_MAP_ARRAY = "CacheMapArray"; // NOI18N
    public static final String CACHE_MAPPING = "CacheMapping"; // NOI18N
    public static final String CACHE_HTTP_METHOD_ARRAY = "CacheHTTPMethodArray"; // NOI18N
    public static final String CACHE_KEY_FIELD_ARRAY = "CacheKeyFieldArray"; // NOI18N
    public static final String CACHE_CONSTRAINT_FIELD_ARRAY = "CacheConstraintFieldArray"; // NOI18N
    public static final String CACHE_CONSTRAINT_FIELD_VALUE_ARRAY = "CacheConstraintFieldValueArray"; // NOI18N
    
    private static final String DELIMITER = "::";//NOI18N
private static final String DATAFILE = "com/sun/enterprise/tools/common/dd/webapp/data/sun-web-app-data.xml";//NOI18N
    private static Hashtable paramContainer = null;
    private static ParamData pData = null;
    
    public static String[] emptyList = {""};//NOI18N
    public static String[] extraParamName = {""};//NOI18N
    public static final String extraParamHelpID = helpBundle.getString("webmod_extraparams_add"); //NOI18N
    
    public static String[] sessionParamName;
    public static final String sessionParamHelpID = helpBundle.getString("webmod_sessionparam_add"); //NOI18N
    
    public static String[] storeParamName;
    public static final String storeParamHelpID = helpBundle.getString("webmod_storeparam_add"); //NOI18N
    
    public static String[] jspParamName;
    public static final String jspParamHelpID = helpBundle.getString("webmod_jspparam_add"); //NOI18N
    
    public static String[] cookieParamName;
    public static final String cookieParamHelpID = helpBundle.getString("webmod_cookieparam_add"); //NOI18N
    
    public static String[] managerParamName;
    public static final String managerParamHelpID = helpBundle.getString("webmod_managerparam_add"); //NOI18N
    
    public static String[] persistParamName;
    public static final String persistParamHelpID = helpBundle.getString("webmod_sessionparam_add"); //NOI18N
    
    public static String[] helperClassParamName;
    public static final String helperClassParamHelpID = helpBundle.getString("webmod_sessionparam_add"); //NOI18N
    
    
    
    
    
    public static String[] getParamNames(int type){
        if(paramContainer == null) initialize();
        switch ( type ) {
            case EXTRAPARAMS:
                return extraParamName;
            case SESSIONPARAM:
                return sessionParamName;
            case STOREPARAM:
                return storeParamName;
            case JSPPARAM:
                return jspParamName;
            case COOKIEPARAM:
                return cookieParamName;
            case MANAGERPARAM:
                return managerParamName;
            case PERSISTPARAM:
                return persistParamName;
	    case HELPERCLASSPARAM:
		return helperClassParamName;
        }
        return new String[] {};
    }
    
    public static String[] getParamValues(int type, String key){
        if(paramContainer == null) initialize();
        pData = (ParamData) paramContainer.get(type+DELIMITER+key);
        if(pData != null){
            return pData.getParamValues();
        }else{
            return emptyList;
        }
    }
    
    public static String getDefaultValue(int type, String key){
        if(paramContainer == null) initialize();
        pData = (ParamData) paramContainer.get(type+DELIMITER+key);
        if(pData != null){
            return pData.getDefaultValue();
        }else{
            return "";//NOI18N
        }
    }
    
    public static String getHelpID(int type, String key){
        if(paramContainer == null) initialize();
        pData = (ParamData) paramContainer.get(type+DELIMITER+key);
        if(pData != null){
            return pData.getHelpID();
        }else{
            return "";//NOI18N
        }
    }
    
   public static String getType(int type, String key){
        if(paramContainer == null) initialize();
        
        pData = (ParamData) paramContainer.get(type+DELIMITER+key);
        
        if(pData != null){
            return pData.getParamType();
        }else{
            return "";//NOI18N
        }
    }
 
   /* test the value to check if this is the right type
    **/
   
   public static boolean isValidType(int type, String key, String value){
       boolean valid = true;
       if ((type == EXTRAPARAMS) || (type == HELPERCLASSPARAM)) return true;
       pData = (ParamData) paramContainer.get(type+DELIMITER+key);
       String reqType = pData.getParamType();
      // System.out.println("###**** reqType = " + reqType); //NOI18N
       if(reqType != null && reqType.equalsIgnoreCase("number")){//NOI18N
           try{
               //System.out.println("###**** Trying to parse"); //NOI18N
               Integer.parseInt(value);
           }catch(Exception e){
               valid = false;
           }
       }
       return valid;
   }
   
    public static String getHelpID(int type){
        switch ( type ) {
            case EXTRAPARAMS:
                return extraParamHelpID;
            case SESSIONPARAM:
                return sessionParamHelpID;
            case STOREPARAM:
                return storeParamHelpID;
            case JSPPARAM:
                return jspParamHelpID;
            case COOKIEPARAM:
                return cookieParamHelpID;
            case MANAGERPARAM:
                return managerParamHelpID;
            case PERSISTPARAM:
                return persistParamHelpID;
            case HELPERCLASSPARAM:
                return helperClassParamHelpID;
        }
        return ""; // NOI18N
    }
    
    private static void initialize(){
        paramContainer = new Hashtable();
        InputStream in = MetaData.class.getClassLoader().getResourceAsStream(DATAFILE);
        //System.out.println("############ getParamFromFile DATAFILE =" + DATAFILE); //NOI18N
        //System.out.println("############ getParamFromFile in =" + in); //NOI18N
	SunWebAppData data = null;
	try {
	        data = SunWebAppData.createGraph(in);
	} catch (Schema2BeansException e) {
		System.out.println("Failed to create bean graph for SunWebAppData"); //NOI18N
	}
        //System.out.println("############ " + data.dumpBeanNode()); //NOI18N
        ParamData pData = null;
        
        // initialize all param name arrays
        sessionParamName = new String[data.sizeSessionParam()];
        storeParamName = new String[data.sizeStoreParam()];
        managerParamName = new String[data.sizeManagerParam()];
        jspParamName = new String[data.sizeJspParam()];
        extraParamName = new String[data.sizeExtraParam()];
        cookieParamName = new String[data.sizeCookieParam()];
        persistParamName = new String[data.sizePersistenceParam()];
        helperClassParamName = new String[data.sizeHelperClassParam()];
        
        for(int i = 0; i < data.sizeSessionParam(); i ++){
            pData = data.getSessionParam(i);
            sessionParamName[i] = pData.getParamName();
            //System.out.println("#### putting key = " + SESSIONPARAM + DELIMITER + pData.getParamName()); //NOI18N
            paramContainer.put(SESSIONPARAM + DELIMITER + pData.getParamName() , pData);
        }
        
        for(int i = 0; i < data.sizePersistenceParam(); i ++){
            pData = data.getPersistenceParam(i);
            persistParamName[i] = pData.getParamName();
            paramContainer.put(PERSISTPARAM + DELIMITER + pData.getParamName(), pData);
        }
        
        for(int i = 0; i < data.sizeStoreParam(); i ++){
            pData = data.getStoreParam(i);
            storeParamName[i] = pData.getParamName();
            paramContainer.put(STOREPARAM + DELIMITER + pData.getParamName() , pData);
        }
        
        for(int i = 0; i < data.sizeManagerParam(); i ++){
            pData = data.getManagerParam(i);
            managerParamName[i] = pData.getParamName();
            paramContainer.put(MANAGERPARAM + DELIMITER + pData.getParamName() , pData);
        }
        
        for(int i = 0; i < data.sizeJspParam(); i ++){
            pData = data.getJspParam(i);
            jspParamName[i] = pData.getParamName();
            paramContainer.put(JSPPARAM + DELIMITER + pData.getParamName() , pData);
        }
        
        for(int i = 0; i < data.sizeExtraParam(); i ++){
            pData = data.getExtraParam(i);
            extraParamName[i] = pData.getParamName();
            paramContainer.put(EXTRAPARAMS + DELIMITER + pData.getParamName() , pData);
        }
        
        for(int i = 0; i < data.sizeCookieParam(); i ++){
            pData = data.getCookieParam(i);
            cookieParamName[i] = pData.getParamName();
            paramContainer.put(COOKIEPARAM + DELIMITER + pData.getParamName() , pData);
        }
        for(int i = 0; i < data.sizeHelperClassParam(); i ++){
            pData = data.getHelperClassParam(i);
            helperClassParamName[i] = pData.getParamName();
            paramContainer.put(HELPERCLASSPARAM + DELIMITER + pData.getParamName() , pData);
        }
    }
    
    
}
