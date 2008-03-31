/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.util;

import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.PropertyConfig;
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
     *	<p> Returns the value of the given property and MBean
     *
     *	@param	mbean MBean with properties
     *   (extends <code>com.sun.appserv.management.config.PropertiesAccess</code>).
     *  @param	propName property name.
     *
     *	@return	String property value.
     */
    public static String getPropertyValue(PropertiesAccess mbean, String propName) {
        if (mbean.getPropertyConfigMap().get(propName) == null){
            return "";
        }else
            return mbean.getPropertyConfigMap().get(propName).getValue();
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
    public static Map getNonSkipPropertiesMap(PropertiesAccess config, List skipList) {
        Map<String, PropertyConfig> props = config.getPropertyConfigMap();
        Map newMap = new HashMap<String, String>();

        for (String propsName : props.keySet()) {
            if (skipList.contains(propsName)) {
                continue;
            }
            newMap.put(propsName, props.get(propsName).getValue());
        }
        return newMap;
    }


    //Chagen the Property Value of a config
    public static void changeProperty(PropertiesAccess config, String propName, String propValue) {
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
}
