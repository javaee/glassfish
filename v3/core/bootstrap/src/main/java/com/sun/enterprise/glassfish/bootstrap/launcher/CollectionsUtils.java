/*
 * A place for useful static methods dealing with Collections
 * THIS IS A COPY of the file in c.s.e.util
 * If c.s.e.util can not be made a dependency -- this copy will remain.
 */

package com.sun.enterprise.glassfish.bootstrap.launcher;

import java.util.*;


/**
 *
 * @author bnevins
 */
public class CollectionsUtils 
{

    private CollectionsUtils()
    {
        // all static methods
    }

    public static Map<String, String> stringsToMap(ArrayList<String> ss) 
    {
        if(ss == null || ss.size() <= 0)
            return new HashMap<String,String>();
        
        Map<String,String> map = new HashMap<String,String>(ss.size());
        
        for(String s : ss)
            addStringAsEntry(map, s);
        
        return map;
    }
    /**
     * It is annoying that Properties is a Map<Object,Object> even though
     *  Properties are supposed to be all Strings.  This will create a Map<String,String>
     *  that is easier to work with.
     * 
     * @param props The Properties object to convert
     * @return A Map copy with the correct types
     */
    public static Map<String,String> propsToMap(Properties props)
    {
        // there is probably a more efficient, cool way to do this, but
        // this works fine and properties objects are generally small.
        // Feel free to improve it!
        
        if(props == null || props.size() <= 0)
            return new HashMap<String,String>();
        
        Map<String,String> map = new HashMap<String,String>(props.size());
        Set<Map.Entry<Object,Object>> pset = props.entrySet();
        
        for(Map.Entry<Object,Object> entry : pset)
        {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            map.put(key, value);
        }
        
        return map;
    }
    /**
     * 
     * @param props The Properties object to convert
     * @return An array of String with key=value
     */
    public static String[] propsToStrings(Properties props)
    {
        Map<String, String> map = propsToMap(props);
        Set<Map.Entry<String,String>> set = map.entrySet();
        List<String> ss = new ArrayList<String>();
        
        for(Map.Entry<String,String> entry : set)
        {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if(ok(key))
            {
                if(ok(value))
                    ss.add(key + "=" + value);
                else
                    ss.add(key);
            }
        }
    
        int len = ss.size();
        
        if(len <= 0)
            return new String[0];
        
        String[] ret = new String[len];
        return ss.toArray(ret);
    }

    private static void addStringAsEntry(Map<String, String> map, String s) 
    {
        if(s == null || s.length() <= 0)
            return; // garbage
        
        int index = s.indexOf('=');
        
        if(index == 0)
            return;  // "=xxxx" --> garbage
        
        if(index < 0)
        {
            map.put(s, "");
            return;  // "something"
        }

        String key = s.substring(0, index);
        
        if(s.length() <= index + 1)
        {
            // "something="
            map.put(key, "");
            return;
        }

        // finally - a normal one!  "something=foo"
        
        String value = s.substring(index + 1);
        map.put(key, value);
    }

    private static boolean ok(String s)
    {
        return s != null && s.length() > 0;
    }
    
    /*
     * simple test main
     *
    public static void main(String[] args)
    {
        Properties p = new Properties();
        p.setProperty("a", "b");
        p.setProperty("a", "c");
        p.put("g", new File("c:/tmp"));
        Map<String, String> map = CollectionsUtils.propsToMap(p);
        Set<Map.Entry<String,String>> set = map.entrySet();
        for(Map.Entry<String,String> entry : set)
        {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    } 
     */   
}

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
