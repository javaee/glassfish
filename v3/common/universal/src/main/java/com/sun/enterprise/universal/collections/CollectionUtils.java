/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.collections;

import java.util.*;

/**
 * all-static methods for handling operations with Collections
 * @author bnevins
 */
public class CollectionUtils {
    private CollectionUtils() {
        
    }
    /**
     * Convert a Properties object, which is a Map<Object,Object> into
     * a Map<String,String>
     * @param p The Properties object to convert
     * @return The converted Map
     */
    public static Map<String,String> propertiesToStringMap(Properties p)
    {
        Map<String,String> map = new HashMap<String,String>();
        Set<Object> names = p.keySet();
        
        for(Object name : names) {
            if(name == null) // impossible
                continue;
            
            Object value = p.get(name);
            
            if(value == null)
                map.put(name.toString(), null);
            else
                map.put(name.toString(), value.toString());
        }
        return map;
    }
    
    /**
     * Tired of dumping a String representation of a Map?
     * Then call me!
     * @param map The map to turn into a printable String
     * @return The pretty String 
     */
    public static String toString(Map<String,String> map) {
        String[] arr = toStringArray(map);
        StringBuilder sb = new StringBuilder();
        
        for(String s : arr) {
            sb.append(s);
            sb.append(EOL);
        }
        return sb.toString();
    }

    public static String[] toStringArray(Map<String,String> map) {
        Set<String> set = map.keySet();
        String[] ss = new String[map.size()];
        int i = 0;
        
        for(String name : set) {
            String value = map.get(name);
            String s = name;
            
            if(value != null) {
                s += "=" + value;
            }
            ss[i++] = s;
        }
        return ss;
    }
    private static final String EOL = System.getProperty("line.separator");
}
