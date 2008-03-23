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
}
