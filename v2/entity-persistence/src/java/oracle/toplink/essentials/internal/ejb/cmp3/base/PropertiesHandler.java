/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.logging.Level;
import java.security.AccessController;
import java.security.PrivilegedAction;
import oracle.toplink.essentials.config.*;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.logging.SessionLog;

/**
 * 
 * The class processes some of TopLink properties.
 * The class may be used for any properties, but it only makes sense 
 * to use it in the following two cases:
 *      1. There is a set of legal values defined
 *          either requiring translation (like CacheTypeProp);
 *          or not (like LoggingLevelProp).
 *      2. Property is really a prefix from which the property obtained by 
 *      appending entity or class name (like DescriptorCustomizerProp -
 *      it corresponds to "toplink.descriptor.customizer." prefix that allows to
 *      define propereties like "toplink.descriptor.customizer.myPackage.MyClass"). 
 * 
 * TopLink properties and their values defined in oracle.toplink.essentials.config package.
 * 
 * To add a new property:
 *   Define a new property in TopLinkProperties;
 *   Add a class containing property's values if required to config package (like CacheType);
 *      Alternatively values may be already defined elsewhere (like in LoggingLevelProp).
 *   Add an inner class to this class extending Prop corresponding to the new property (like CacheTypeProp);
 *      The first constructor parameter is property name; the second is default value;
 *      In constructor 
 *          provide 2-dimensional value array in case the values should be translated (like CacheTypeProp);
 *              in case translation is not required provide a single-dimension array (like LoggingLevelProp).
 *   In inner class Prop static initializer addProp an instance of the new prop class (like addProp(new CacheTypeProp())).
 * 
 * @see TopLinkProperties
 * @see CacheType
 * @see TargetDatabase
 * @see TargetServer
 * 
 */
public class PropertiesHandler {
    
    /**
     * INTERNAL:
     * Gets property value from the map, if none found looks in System properties.
     * Use this to get a value for a non-prefixed property.
     * Could be used on prefixes (like "oracle.toplink.cache-type.") too,
     * but will always return null
     * Throws IllegalArgumentException in case the property value is illegal.
     */
    public static String getPropertyValue(String name, Map m) {
        return Prop.getPropertyValueToApply(name, m, null, true);
    }

    public static String getPropertyValueLogDebug(String name, Map m, AbstractSession session) {
        return Prop.getPropertyValueToApply(name, m, session, true);
    }
    
    /**
     * INTERNAL:
     * Gets property value from the map, if none found looks in System properties.
     * Use this to get a value for a prefixed property:
     * for "oracle.toplink.cache-type.Employee"
     * pass "oracle.toplink.cache-type.", "Employee".
     * Throws IllegalArgumentException in case the property value is illegal.
     */
    public static String getPrefixedPropertyValue(String prefix, String suffix, Map m) {
        return (String)getPrefixValues(prefix, m).get(suffix);
    }

    /**
     * INTERNAL:
     * Gets properties' values from the map, if none found looks in System properties.
     * In the returned map values keyed by suffixes.
     * Use it with prefixes (like "oracle.toplink.cache-type.").
     * Could be used on simple properties (not prefixes, too),
     * but will always return either an empty map or a map containing a single 
     * value keyed by an empty String.
     * Throws IllegalArgumentException in case the property value is illegal.
     */
    public static Map getPrefixValues(String prefix, Map m) {
        return Prop.getPrefixValuesToApply(prefix, m, null, true);
    }
    
    public static Map getPrefixValuesLogDebug(String prefix, Map m, AbstractSession session) {
        return Prop.getPrefixValuesToApply(prefix, m, session, true);
    }
    
    /**
     * INTERNAL:
     * Returns the default property value that should be applied.
     * Throws IllegalArgumentException in case the name doesn't correspond
     * to any property.
     */
    public static String getDefaultPropertyValue(String name) {
        return Prop.getDefaultPropertyValueToApply(name, null);
    }
    
    public static String getDefaultPropertyValueLogDebug(String name, AbstractSession session) {
        return Prop.getDefaultPropertyValueToApply(name, session);
    }
    
    /**
     * INTERNAL:
     * Gets property value from AbstractSession.getProperties() map,
     * if none found looks in its parent recursively;
     * if none is found looks in System properties.
     * Use this to get a value for a non-prefixed property.
     * Throws IllegalArgumentException in case the property value is illegal.
     */
    public static String getSessionPropertyValue(String name, AbstractSession session) {
        String value = null;
        while(value == null && session != null) {
            AbstractSession parent = session.getParent();
            // Don't use System properties as default unless session has no parent.
            // Motivation: look in all the recursive parents' properties before looking in System properties.
            value = Prop.getPropertyValueToApply(name, session.getProperties(), null, parent == null);
            session = parent;
        }
        return value;
    }

    public static String getSessionPropertyValueLogDebug(String name, AbstractSession session) {
        String value = null;
        while(value == null && session != null) {
            AbstractSession parent = session.getParent();
            // Don't use System properties as default unless session has no parent.
            // Motivation: look in all the recursive parents' properties before looking in System properties.
            value = Prop.getPropertyValueToApply(name, session.getProperties(), session, parent == null);
            session = parent;
        }
        return value;
    }
    
    /**
     * INTERNAL:
     * Empty String value indicates that the default property value
     * should be used.
     */
    protected static boolean shouldUseDefault(String value) {
        return value != null &&  value.length() == 0;
    }
    
    protected static abstract class Prop {
        static HashMap mainMap = new HashMap();
        Object[] valueArray;
        HashMap valueMap;
        String name;
        String defaultValue;
        String defaultValueToApply;
        boolean valueToApplyMayBeNull;
        boolean shouldReturnOriginalValueIfValueToApplyNotFound;
        
        static {
            addProp(new LoggerTypeProp());
            addProp(new LoggingLevelProp());
            addProp(new CategoryLoggingLevelProp());
            addProp(new TargetDatabaseProp());
            addProp(new TargetServerProp());
            addProp(new CacheSizeProp());
            addProp(new CacheTypeProp());
            addProp(new CacheSharedProp());
            addProp(new DescriptorCustomizerProp());
            addProp(new FlushClearCacheProp());
        }
        
        Prop(String name) {
            this.name = name;
        }
        
        Prop(String name, String defaultValue) {
            this(name);
            this.defaultValue = defaultValue;
        }

        static String getPropertyValueFromMap(String name, Map m, boolean useSystemAsDefault) {
            String value = (String)m.get(name);
            return value == null && useSystemAsDefault ? System.getProperty(name) : value;
        }
    
        // Collect all entries corresponding to the prefix name.
        // Note that entries from Map m override those from System properties.
        static Map getPrefixValuesFromMap(String name, Map m, boolean useSystemAsDefault) {
            Map mapOut = new HashMap();
            
            Iterator it;
            if(useSystemAsDefault) {
                it = (Iterator)AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return System.getProperties().entrySet().iterator();
                        }    
                    }
                );
    
                while(it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String str = (String)entry.getKey();
                    if(str.startsWith(name)) {
                        String entityName = str.substring(name.length(), str.length());
                        mapOut.put(entityName, entry.getValue());
                    }
                }
            }
            
            it = m.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String str = (String)entry.getKey();
                if(str.startsWith(name)) {
                    String entityName = str.substring(name.length(), str.length());
                    mapOut.put(entityName, entry.getValue());
                }
            }
            
            return mapOut;
        }
    
        static String getPropertyValue(String name, boolean shouldUseDefault, Map m, AbstractSession session, boolean useSystemAsDefault) {
            Prop prop = (Prop)mainMap.get(name);
            if(prop == null) {
                // it's not our property
                return null; 
            }
            String value = (String)getPropertyValueFromMap(name, m, useSystemAsDefault);
            if(value == null) {
                return null;
            }
            return prop.getValueToApply(value, shouldUseDefault, session);
        }
                
        static String getPropertyValueToApply(String name, Map m, AbstractSession session, boolean useSystemAsDefault) {
            Prop prop = (Prop)mainMap.get(name);
            if(prop == null) {
                return null; 
            }
            String value = (String)getPropertyValueFromMap(name, m, useSystemAsDefault);
            if(value == null) {
                return null;
            }
            return prop.getValueToApply(value, shouldUseDefault(value), session);
        }
                
        static Map getPrefixValuesToApply(String prefix, Map m, AbstractSession session, boolean useSystemAsDefault) {
            Prop prop = (Prop)mainMap.get(prefix);
            if(prop == null) {
                // prefix doesn't correspond to a Prop object - it's not our property.
                return new HashMap(0); 
            }
            
            // mapps suffixes to property values
            Map mapIn = (Map)getPrefixValuesFromMap(prefix, m, useSystemAsDefault);
            if(mapIn.isEmpty()) {
                return mapIn;
            }
            
            HashMap mapOut = new HashMap(mapIn.size());
            Iterator it = mapIn.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String suffix = (String)entry.getKey();
                String value = (String)entry.getValue();
                mapOut.put(suffix, prop.getValueToApply(value, shouldUseDefault(value), suffix, session));
            }
            // mapps suffixes to valuesToApply
            return mapOut;
        }
        
        static String getDefaultPropertyValueToApply(String name, AbstractSession session) {
            Prop prop = (Prop)mainMap.get(name);
            if(prop == null) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-default-for-unknown-property", new Object[]{name}));
            }
            prop.logDefault(session);
            return prop.defaultValueToApply;
        }
                
        String getValueToApply(String value, boolean shouldUseDefault, AbstractSession session) {
            return getValueToApply(value, shouldUseDefault, null, session);
        }
        
        // suffix is used only for properties-prefixes (like CacheType)
        String getValueToApply(String value, boolean shouldUseDefault, String suffix, AbstractSession session) {            
            if(shouldUseDefault) {
                logDefault(session, suffix);
                return defaultValueToApply;
            }
            String valueToApply = value;
            if(valueMap != null) {
                String key = getUpperCaseString(value);
                valueToApply = (String)valueMap.get(key);
                if(valueToApply == null) {
                    boolean notFound = true;
                    if(valueToApplyMayBeNull) {
                        notFound = !valueMap.containsKey(key);
                    }
                    if(notFound) {
                        if(shouldReturnOriginalValueIfValueToApplyNotFound) {
                            valueToApply = value;
                        } else {
                            String propertyName = name;
                            if(suffix != null) {
                                propertyName = propertyName + suffix;
                            }
                            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("ejb30-illegal-property-value", new Object[]{propertyName, getPrintValue(value)}));
                        }
                    }
                }
            }
            String logValue = TopLinkProperties.getOverriddenLogStringForProperty(name);
            if (logValue != null){
                log(session, logValue, logValue, suffix);
            } else {
                log(session, value, valueToApply, suffix);
            }
            return valueToApply;
        }

        static String getUpperCaseString(String value) {
            if(value != null) {
                return value.toUpperCase();
            } else {
                return null;
            }
        }
        static String getPrintValue(String value) {
            if(value != null) {
                return value;
            } else {
                return "null";
            }
        }
        void initialize() {
            if(valueArray != null) {
                valueMap = new HashMap(valueArray.length);
                if(valueArray instanceof Object[][]) {
                    Object[][] valueArray2 = (Object[][])valueArray;
                    for(int i=0; i<valueArray2.length; i++) {
                        valueMap.put(getUpperCaseString((String)valueArray2[i][0]), valueArray2[i][1]);
                        if(valueArray2[i][1] == null) {
                            valueToApplyMayBeNull = true;
                        }
                    }
                } else {
                    for(int i=0; i<valueArray.length; i++) {
                        valueMap.put(getUpperCaseString((String)valueArray[i]), valueArray[i]);
                        if(valueArray[i] == null) {
                            valueToApplyMayBeNull = true;
                        }
                    }
                }
                defaultValueToApply = (String)valueMap.get(getUpperCaseString(defaultValue));
            } else {
                defaultValueToApply = defaultValue;
            }
        }

        void logDefault(AbstractSession session) {
            logDefault(session, null);
        }
        
        void logDefault(AbstractSession session, String suffix) {
            if(session != null) {
                String propertyName = name;
                if(suffix != null) {
                    propertyName = propertyName + suffix;
                }
                if(defaultValue != defaultValueToApply) {
                    session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "handler_property_value_default", new Object[]{propertyName, defaultValue, defaultValueToApply});
                } else {
                    session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_default", new Object[]{propertyName, defaultValue});
                }
            }
        }
        
        void log(AbstractSession session, String value, String valueToApply, String suffix) {
            if(session != null) {
                String propertyName = name;
                if(suffix != null) {
                    propertyName = propertyName + suffix;
                }
                if(value != valueToApply) {
                    session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "handler_property_value_specified", new Object[]{propertyName, value, valueToApply});
                } else {
                    session.log(SessionLog.FINEST, SessionLog.PROPERTIES, "property_value_specified", new Object[]{propertyName, value});
                }
            }
        }
        
        static void addProp(Prop prop) {
            prop.initialize();
            mainMap.put(prop.name, prop);
        }
    }

    protected static class LoggerTypeProp extends Prop {
        LoggerTypeProp() {
            super(TopLinkProperties.LOGGING_LOGGER, LoggerType.DEFAULT);
            this.shouldReturnOriginalValueIfValueToApplyNotFound = true;
            String pcg = "oracle.toplink.essentials.logging.";
            valueArray = new Object[][] {
                {LoggerType.DefaultLogger, pcg + "DefaultSessionLog"},
                {LoggerType.JavaLogger, pcg + "JavaLog"}
            };
        }
    }

    protected static class LoggingLevelProp extends Prop {
        LoggingLevelProp() {
            super(TopLinkProperties.LOGGING_LEVEL, Level.INFO.getName());
            valueArray = new Object[] { 
                Level.OFF.getName(),
                Level.SEVERE.getName(),
                Level.OFF.getName(),
                Level.WARNING.getName(),
                Level.INFO.getName(),
                Level.CONFIG.getName(),
                Level.FINE.getName(),
                Level.FINER.getName(),
                Level.FINEST.getName(),
                Level.ALL.getName()
            };
        }
    }

    protected static class CategoryLoggingLevelProp extends Prop {
        CategoryLoggingLevelProp() {
            super(TopLinkProperties.CATEGORY_LOGGING_LEVEL_);
            valueArray = new Object[] { 
                Level.OFF.getName(),
                Level.SEVERE.getName(),
                Level.OFF.getName(),
                Level.WARNING.getName(),
                Level.INFO.getName(),
                Level.CONFIG.getName(),
                Level.FINE.getName(),
                Level.FINER.getName(),
                Level.FINEST.getName(),
                Level.ALL.getName()
            };
        }
    }

    protected static class TargetDatabaseProp extends Prop {
        TargetDatabaseProp() {
            super(TopLinkProperties.TARGET_DATABASE, TargetDatabase.DEFAULT);
            this.shouldReturnOriginalValueIfValueToApplyNotFound = true;
            String pcg = "oracle.toplink.essentials.platform.database.";
            valueArray = new Object[][] { 
                {TargetDatabase.Auto, pcg + "DatabasePlatform"},
                {TargetDatabase.Oracle, pcg + "oracle.OraclePlatform"},
                {TargetDatabase.Attunity, pcg + "AttunityPlatform"},
                {TargetDatabase.Cloudscape, pcg + "CloudscapePlatform"},
                {TargetDatabase.Database, pcg + "DatabasePlatform"},
                {TargetDatabase.DB2Mainframe, pcg + "DB2MainframePlatform"},
                {TargetDatabase.DB2, pcg + "DB2Platform"},
                {TargetDatabase.DBase, pcg + "DBasePlatform"},
                {TargetDatabase.Derby, pcg + "DerbyPlatform"},
                {TargetDatabase.HSQL, pcg + "HSQLPlatform"},
                {TargetDatabase.Informix, pcg + "InformixPlatform"},
                {TargetDatabase.JavaDB, pcg + "JavaDBPlatform"},
                {TargetDatabase.MySQL4, pcg + "MySQL4Platform"},
                {TargetDatabase.PointBase,  pcg + "PointBasePlatform"},
                {TargetDatabase.PostgreSQL,  pcg + "PostgreSQLPlatform"},
                {TargetDatabase.SQLAnyWhere,  pcg + "SQLAnyWherePlatform"},
                {TargetDatabase.SQLServer,  pcg + "SQLServerPlatform"},
                {TargetDatabase.Sybase,  pcg + "SybasePlatform"},
                {TargetDatabase.TimesTen,  pcg + "TimesTenPlatform"}
            };
        }
    }

    protected static class TargetServerProp extends Prop {
        TargetServerProp() {
            super(TopLinkProperties.TARGET_SERVER, TargetServer.DEFAULT);
            this.shouldReturnOriginalValueIfValueToApplyNotFound = true;
            String pcg = "oracle.toplink.essentials.platform.server.";
            valueArray = new Object[][] { 
                {TargetServer.None, pcg + "NoServerPlatform"},
                {TargetServer.OC4J_10_1_3, pcg + "oc4j.Oc4jPlatform"},
                {TargetServer.SunAS9, pcg + "sunas.SunAS9ServerPlatform"}
            };
        }  
    }

    protected static class CacheSizeProp extends Prop {
        CacheSizeProp() {
            super(TopLinkProperties.CACHE_SIZE_, Integer.toString(1000));
        }  
    }

    protected static class CacheTypeProp extends Prop {
        CacheTypeProp() {
            super(TopLinkProperties.CACHE_TYPE_, CacheType.DEFAULT);
            String pcg = "oracle.toplink.essentials.internal.identitymaps.";
            valueArray = new Object[][] { 
                {CacheType.Weak, pcg + "WeakIdentityMap"},
                {CacheType.SoftWeak, pcg + "SoftCacheWeakIdentityMap"},
                {CacheType.HardWeak, pcg + "HardCacheWeakIdentityMap"},
                {CacheType.Full, pcg + "FullIdentityMap"},
                {CacheType.NONE, pcg + "NoIdentityMap"}
            };
        }
    }

    protected static class CacheSharedProp extends Prop {
        CacheSharedProp() {
            super(TopLinkProperties.CACHE_SHARED_, "false");
            valueArray = new Object[] { 
                "true",
                "false"
            };
        }  
    }

    protected static class DescriptorCustomizerProp extends Prop {
        DescriptorCustomizerProp() {
            super(TopLinkProperties.DESCRIPTOR_CUSTOMIZER_);
        }  
    }

    protected static class FlushClearCacheProp extends Prop {
        FlushClearCacheProp() {
            super(TopLinkProperties.FLUSH_CLEAR_CACHE, FlushClearCache.DEFAULT);
            valueArray = new Object[] { 
                FlushClearCache.Merge,
                FlushClearCache.Drop,
                FlushClearCache.DropInvalidate
            };
        }
    }
}
