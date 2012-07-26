/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package devtests.deployment.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * This class compares deployment Descriptors for testing purpose.
 * It is not thread safe.
 *
 * @author Shing Wai Chan
 */
public class DescriptorContentComparator {
    private static final Class[] excludedClasses = new Class[] {
            java.lang.Object.class,
            java.beans.PropertyChangeSupport.class,
            com.sun.enterprise.util.LocalStringManagerImpl.class,
            java.util.Observable.class
            };

    private static final String[] excludedFieldNames = new String[] {
            "propListeners",
            "wsdlPortNamespacePrefix",
            "specVersion"
            };

    private static Logger logger = null;
                
    // for easy debug
    private Field lastField = null;

    public DescriptorContentComparator() {
        initLogger();
    }

    public boolean compareContent(Descriptor d1, Descriptor d2) {
        boolean result = compareContent(null, d1, d2, new HashSet());
        if (!result) {
            logger.severe("last compared Field = " + lastField);
        }
        return result;
    }

    /**
     * Use reflection to look at fields for comparison.
     * It is important to note that if instances under comparison are in
     * parameter <i>set</i>, then the comparison result will always be
     * true. This resolves the issues of circular references.
     * @param field  Field under comparison if known
     * @param o1  Object to be compared
     * @param o2  Object to be compared
     * @param set stack of Descriptor instances under comparison
     * @return boolean result of comparison
     */
    private boolean compareContent(Field field, Object o1, Object o2, Set set) {
        if (o1 == o2 || 
                isNullEquivalent(field, o1) && isNullEquivalent(field, o2)) {
            return true;
        }

        if (o1 == null && o2 != null || o1 != null && o2 == null ||
                !o2.getClass().equals(o1.getClass()) ||
                isExcludedClass(o1.getClass())) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("... null, class mismatch or excluded for " +
                    ((field != null)? field : "") + 
                    ", o1 = " + o1 + ", o2 = " + o2);
            }

            return false;
        }

        // in the following, o1 and o2 are not null
        Package p1 = o1.getClass().getPackage();
        Package p2 = o2.getClass().getPackage();

        if (p1 != null && !p1.equals(p2) || p1 == null && p2 != null) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("... diff package: obj1 = " + o1);
                logger.fine("... diff package: obj2 = " + o2);
            }

            return false;
        } else if (o1.getClass().isArray()) {
            if (!compareArrayContent(o1, o2, set)) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("... diff array: obj1 = " + o1);
                    logger.fine("... diff array: obj2 = " + o2);
                }

                return false;
            }
        } else if (o1 instanceof Collection) {
            if (!compareCollectionContent((Collection)o1,
                    (Collection)o2, set)) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("... diff coll: obj1 = " + o1);
                    logger.fine("... diff coll: obj2 = " + o2);
                }

                return false;
            }
        } else if (o1 instanceof Map) {
            if (!compareMapContent((Map)o1, (Map)o2, set)) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("... diff map: obj1 = " + o1);
                    logger.fine("... diff map: obj2 = " + o2);
                }

                return false;
            }
        } else if (p1 != null &&
                (p1.getName().startsWith("com.sun.enterprise.deployment") || p1.getName().startsWith("org.glassfish.ejb.deployment") || p1.getName().startsWith("org.glassfish.web.deployment"))
                ) {

            if (o1 instanceof Descriptor) {
                // to handle circular reference
                ReferencePair rd = new ReferencePair(o1, o2);
                if (!set.add(rd)) {
                    return true;  // continue processing
                }
            }

            // looping from subclass to superclass
            Class clazz = o2.getClass();
            while (clazz != null && !isExcludedClass(clazz)) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("clazz = " + clazz);
                }
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field df : declaredFields) {
                    try {

                        lastField = df;
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("... \tdf = " + df);
                        }

                        if (isExcludedNamedField(df) ||
                                Modifier.isStatic(df.getModifiers())) {
                            continue;
                        }
                        df.setAccessible(true); // to see private fields
                        Object v1 = df.get(o1);
                        Object v2 = df.get(o2);
                        if (!compareContent(df, v1, v2, set)) {
                            return false;
                        }
                    } catch(Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } else {
            boolean result = o1.equals(o2);

            if (!result && logger.isLoggable(Level.FINE)) {
                logger.fine("... diff content: obj1 = " + o1);
                logger.fine("... diff content: obj2 = " + o2);
            }

            return result;
        }

        return true;
    }

    /**
     * The following are regarded as null for comparison purposes:
     *     Collection, Map of size 0
     *     String of size 0
     * @param df field under comparison if known
     * @param o object under comparison
     */
    private boolean isNullEquivalent(Field df, Object o) {
        boolean result = false;
        if (o == null) {
            result = true;
        } else if (o instanceof Collection) {
            Collection coll = (Collection)o;
            result = (coll.size() == 0);
        } else if (o instanceof Map) {                                                    

            Map map = (Map)o;
            int size = map.size();
            if (size == 0) {
                result = true;

            // Need to ignore dynamicAttributes with prefix-mapping entry.
            } else if (size == 1) {
                if (df != null && "dynamicAttributes".equals(df.getName())) {
                    result = (map.get("prefix-mapping") != null);
                }
            }
        } else if (o instanceof String) {
            String s = (String)o;
            result = (s.length() == 0);
        }
        return result;
    }

    private boolean isExcludedClass(Class cl) {
        boolean result = false;
        for (Class clazz : excludedClasses) {
            if (clazz.equals(cl)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isExcludedNamedField(Field f) {
        boolean result = false;
        for (String ef : excludedFieldNames) {
            if (ef.equals(f.getName())) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Assume arguments not null.
     */
    private boolean compareArrayContent(Object value1, Object value2, Set set) {
        int len1 = Array.getLength(value1);
        int len2 = Array.getLength(value2);
        if (len1 != len2) {
            return false;
        } else {
            for (int i = 0; i < len1; i++) {
                Object indObj1 = Array.get(value1, i);
                Object indObj2 = Array.get(value2, i);
                if (!(indObj1 == null && indObj2 == null) &&
                        !(indObj1 != null && indObj2 != null &&
                        compareContent(null, indObj1, indObj2, set))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Assume arguments not null.
     */
    private boolean compareCollectionContent(Collection coll1,
            Collection coll2, Set set) {
        int size1 = coll1.size();
        int size2 = coll2.size();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("compare collection with sizes " + size1 + " and " + size2);
        }

        if (size1 != size2) {
            return false;
        }
        Iterator iter1 = coll1.iterator();
        // O(n^2) comparison!!!
        while (iter1.hasNext()) {
            Object obj1 = iter1.next();
            Iterator iter2 = coll2.iterator();
            boolean tempResult = false;
            while (iter2.hasNext()) {
                Object obj2 = iter2.next();
                if (compareContent(null, obj1, obj2, set)) {
                    tempResult = true;
                    break;
                }
            }
            if (!tempResult) {
                return false;
            }
        }
        return true;
    }

    /**
     * Assume arguments not null.
     */
    private boolean compareMapContent(Map map1, Map map2, Set set) {
        Set keySet1 = map1.keySet();
        Set keySet2 = map2.keySet();
        if (!compareCollectionContent(keySet1, keySet2, set)) {
            return false;
        }
        Iterator iter = keySet1.iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (!compareContent(null, map1.get(key), map2.get(key), set)) {
                return false;
            }
        }
        return true;
    }

    private void initLogger() {
        logger = Logger.getLogger(DescriptorContentComparator.class.getName());
        Level logLevel = Boolean.getBoolean("debug") ? 
                Level.FINEST : Level.CONFIG;
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(logLevel);
        }
        logger.setLevel(logLevel);
    }


    /**
     * This class represents a pair of references where order is not important.
     */
    class ReferencePair {
        Object c1;
        Object c2;
        int hashCode = 0;

        ReferencePair(Object c1, Object c2) {
            this.c1 = c1;
            this.c2 = c2;
            if (c1 != null) {
                hashCode = c1.hashCode();
            }
            if (c2 != null) {
                hashCode ^= c2.hashCode();
            }
        }

        public int hashCode() {
            return hashCode;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ReferencePair)) {
                return false;
            }       

            ReferencePair rd = (ReferencePair)o;
            return (c1 == rd.c1 && c2 == rd.c2 ||
                    c1 == rd.c2 && c2 == rd.c1);
        }
    }
}
