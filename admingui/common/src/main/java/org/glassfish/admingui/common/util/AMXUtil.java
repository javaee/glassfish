/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.util;

import com.sun.appserv.management.config.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author anilam
 */
public class AMXUtil {

    /**
     *	<p> Returns the value of the  property of the MBean
     *
     *	@param	mbean MBean with properties
     *   (extends <code>com.sun.appserv.management.config.PropertiesAccess</code>).
     *  @param	key property name.
     *
     *	@return	String property value.
     */
    public static String getPropertyValue(PropertiesAccess mbean, String key) {
       return getPropertyValue(mbean, key, "");
    }

    public static String getPropertyValue(PropertiesAccess mbean, String key, String defaultValue){
        if ( mbean.getPropertyConfigMap().get(key) != null)
            defaultValue = mbean.getPropertyConfigMap().get(key).getValue();
        return defaultValue;
    }
    
    public static String getPropValue( Map<String, PropertyConfig> propMap, String key){
        if (propMap.get(key) != null)
            return propMap.get(key).getValue();
        return "";
    }
    
    
    /**
     *  Change the property of a config mbean
     *  If the property Value is null or empty, this property will be removed.
     *  If the property  already exist,  the property value will be updated if the value is different.
     *  If the property doesn't exist, a new property will be created. 
     * 
     * @param config    mbean whose Property element to be changed
     * @param propName  name of the property
     * @param propValue value of this property
     * 
     */
    public static void setPropertyValue(PropertiesAccess config, String propName, String propValue) {
        Map<String, PropertyConfig> pMap = config.getPropertyConfigMap();
        if (pMap.containsKey(propName)) {
            if (GuiUtil.isEmpty(propValue)) {
                config.removePropertyConfig(propName);
            } else {
                //don't change the value if it is equal.
                PropertyConfig cp = config.getPropertyConfigMap().get(propName);
                if (!cp.getValue().equals(propValue)) {
                    cp.setValue(propValue);
                }
            }
        } else {
            if (!GuiUtil.isEmpty(propValue)) {
                config.createPropertyConfig(propName, propValue);
            }
        }
    }

    
    public static void updateProperties(PropertiesAccess config, Map<String, String> newProps) {
        updateProperties(config, newProps, null);
    }
    

    /*
     * update the properties of a config.
     */
    public static void updateProperties(PropertiesAccess config, Map<String, String> newProps, List ignore) {

        Map<String, PropertyConfig> oldProps = config.getPropertyConfigMap();
        if (ignore == null) {
            ignore = new ArrayList();
        }
        //Remove any property that is no longer in the new list
        Iterator iter = oldProps.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (ignore.contains(key)) {
                continue;
            }
            if (!newProps.containsKey(key)) {
                config.removePropertyConfig((String) key);
            }
        }

        //update the value if the value is different or create a new property if it doesn't exist before
        Map<String, PropertyConfig> pMap = config.getPropertyConfigMap();
        for (String propName : newProps.keySet()) {
            String propValue = newProps.get(propName);
            if (pMap.containsKey(propName)) {
                pMap.get(propName).setValue(propValue);
            } else {
                config.createPropertyConfig(propName, propValue);
            }
        }
    }

    /*  converts a Property Map to a Map where the name is preceded by PropertiesAccess.PROPERTY_PREFIX.
     *  This conversion is required when this Map is used as the optional parameter when creating a config.
     *  refer to the java doc of PropertiesAccess in AMX javadoc
     */
    public static Map convertToPropertiesOptionMap(Map<String, String> props, Map<String, String> convertedMap) {
        if (convertedMap == null) {
            convertedMap = new HashMap();
        }
        if (props == null) {
            return convertedMap;
        }
        Set<String> keySet = props.keySet();
        for (String key : keySet) {
            if (!GuiUtil.isEmpty((String) props.get(key))) {
                convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + key, (String) props.get(key));
            }
        }
        return convertedMap;
    }

    /**
     * returns the Properties of a config, skipping those specified in the list thats passed in.
     * This is mostly for edit where we want to treat particular properties differently, and don't
     * show that in the Properties table.
     * Normally, this is followed by updateProperites() with the ignore list the same as the skipList
     * specified here when user does a Save.
     */
    public static Map<String, PropertyConfig> getNonSkipPropertiesMap(PropertiesAccess config, List skipList) {
        Map<String, PropertyConfig> props = config.getPropertyConfigMap();
        Map<String, PropertyConfig> newMap = new HashMap<String, PropertyConfig>();

        for (String propsName : props.keySet()) {
            if (skipList.contains(propsName)) {
                continue;
            }
            newMap.put(propsName, props.get(propsName));
        }
        return newMap;
    }

    public static void putOptionalValue(String value, Map convertedMap, String propName)
    {
       if (GuiUtil.isEmpty(value))
           return;
       convertedMap.put(PropertiesAccess.PROPERTY_PREFIX + propName, value);
    }
    
    public static boolean isAppType(ApplicationConfig testApp, String type){
        Map<String, ModuleConfig> moduleConfigMap = testApp.getModuleConfigMap();
        for (ModuleConfig module : moduleConfigMap.values()) {
            for(EngineConfig eConfig : module.getEngineConfigMap().values()){
                String sniffer = eConfig.getSniffer();
                if (sniffer.equals(type))
                    return true;
            }
        }
        return false;
    }
    
    public static Map<String, ApplicationConfig> getApplicationConfigByType(String type){
        Map result = new HashMap();
        Map<String, ApplicationConfig> appConfigMap = AMXRoot.getInstance().getApplicationsConfig().getApplicationConfigMap();
        for(ApplicationConfig aConfig : appConfigMap.values()){
            if (isAppType(aConfig, type)){
                result.put(aConfig.getName(), aConfig);
            }
        }
        return result;
    }
    
    public static ApplicationConfig getApplicationConfigByName(String name){
        ApplicationConfig appConfig = AMXRoot.getInstance().getApplicationsConfig().getApplicationConfigMap().get(name);
        return appConfig;
    }

}

   
