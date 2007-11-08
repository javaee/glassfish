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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.modeler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

import java.io.IOException;
import javax.management.MalformedObjectNameException;

/**
 * Utility class to manage object name.
 */
public class ObjectNameHelper {

    /**
     * Returns a valid object name. If the given object name contains
     * query (*), this mehtod attempts to finds a match. 
     *
     * @param  on  object name as a string
     * @param  mbs mbean server connection
     *
     * @return valid object name
     *
     * @throws  MalformedObjectNameException  if object name is not unique
     * @throws  IOException  communication problem while talking to the 
     *          MBean server
     */
    public static ObjectName getObjectName(String on, MBeanServerConnection mbs)
            throws MalformedObjectNameException, IOException {
        
        // offline mode
        if (mbs == null) {
            return new ObjectName(on);
        }

        ObjectName objectName = null;

        if (on != null) {
            int idx = on.indexOf(WILD_CARD);
            if (idx != -1) { // object name is a pattern

                ObjectName pattern = new ObjectName(on);
                Set mbeans = mbs.queryNames(pattern, null);

                // found more than 1 mbeans
                if (mbeans.size() > 1) {
                    String msg = "Object name is not unique: " + on;
                    throw new MalformedObjectNameException(msg);

                } else {

                    Iterator iter = mbeans.iterator();
                    objectName = (ObjectName) iter.next();
                }
            } else {

                // not a pattern
                objectName = new ObjectName(on);
            }
        }

        // did not find object name
        if (objectName == null) {
            String msg = "Object name is malformed: " + on;
            throw new MalformedObjectNameException(msg);
        }

        return objectName;
    }

    /**
     * Tokenizes a object name. Replaces all ${key} tokens with 
     * vales found in the environment. 
     *
     * @param  on object name with tokens
     * @param  env  map with values for all keys
     *
     * @throws NoSuchFieldException  key not found in environment map 
     */
    public static String tokenize(String on, Map env) 
            throws NoSuchFieldException {

        if (on == null) return on;

        String[] result = on.split(DEF_DELIM_TOKEN);
        StringBuffer s = new StringBuffer();

        for (int i= 0; i<result.length; i++) {
            if (result[i].startsWith(DEF_START_TOKEN) == true) {
                int idx       = result[i].indexOf(DEF_END_TOKEN);
                String key    = result[i].substring(1, idx);

                // key is not available in the environment map
                if (!env.containsKey(key)) {
                    String msg = "Key not found in environment: " + key;
                    throw new NoSuchFieldException(msg);
                }

                String value  = (String) env.get(key);
                s.append(value);

                s.append(result[i].substring(idx+1));
            } else {
                s.append(result[i]);
            }    
        }
        return s.toString();
    }


    /**
     * Returns the token keys from the given object name template.
     *
     * @param  on object name template
     *
     * @returns List the <code>List</code> containing the tokens keys in the
     * given object name template. The list will be empty in case no key is 
     * found in the given template.
     */
    public static List getTokensKeys(String objectNameTemplate){
        ArrayList list = new ArrayList();
        if (objectNameTemplate == null) return list;

        String[] result = objectNameTemplate.split(DEF_DELIM_TOKEN);
        String key = null;
        int idx = -1;
        for (int i= 0; i<result.length; i++) {
            if (result[i].startsWith(DEF_START_TOKEN) == true) {
                idx = result[i].indexOf(DEF_END_TOKEN);
                key = result[i].substring(1, idx);
                list.add(key);
            }
        }
        return list;
    }
 

   /**
    * Tokenizes a object name. This is a wrapper around the utility method.
    * For each of the tokenized field in the objectName, it adds an entry
    * in the token map. It obtains the values for tokenized fields from the
    * <code>asObjectName</code> using these fields as keys.
    * For example, it adds value of "name" key in the token map from the given
    * object name. The tokenized object name should have name=${name} pattern
    * in the object name. For example,
    *   as:name=${name},type=CMM_J2eeApplication,server=${server.name}
    * For the given token, <code>asObjectName</code> is searched for the value.
    * If value is not found in <code>asObjectName</code>, then the given input
    * map <code>tokens</code> is checked to see if the value is already present
    * in it. <code>NoSuchFieldException</code> is thrown if the value for the 
    * token is neither found in the as object name nor its already present in 
    * the provided map.   
    * 
    * @param  asObjectName  object name from application server
    * @param  objectName    object name string to be tokenized
    * @param  tokens        the given token values map
    *
    * @return tokenized object name string
    *
    * @throws IOException  if name is not found in the object name
    * @throws NoSuchFieldException  key not found in the as object name or the
    *         the given map
    */
    public static String tokenizeON(ObjectName asObjectName, String objectName,
	     Map tokens)  throws IOException, NoSuchFieldException {
 
        if (tokens == null) {
            tokens = new Hashtable();
        } 

        List keys = ObjectNameHelper.getTokensKeys(objectName);
        String key = null;
        String value = null;
        for (int i=0; i<keys.size(); i++){
            key = (String)keys.get(i);
            value = asObjectName.getKeyProperty(key);
            if(value == null){
                if (!tokens.containsKey(key)) {
                    String msg = "Key not found: " + key;
                    throw new NoSuchFieldException(msg);
                }
            }
            if (value != null){
                tokens.put(key, value);
            }
        }

        return ObjectNameHelper.tokenize(objectName, tokens);
    }
    
   /**
    * Returns a map of keys and properties of objectName. Useful in 
    * cases where the real <code>ObjectName</code> object cannot be created, 
    * since the string may be a template. For example,
    * <example>
    *   com.sun.cmm.as:webmodule-virtual-server=${webmodule-virtual-server},
    *                  name=${name},type=CMM_J2eeServlet,
    *                  standalone-web-module=${standalone-web-module},
    *                  server=${server.name} 
    *</example>
    *
    * @param  objectName objectName template
    * @return Map of keys and properties of objectName
    */   
    public static Map getKeysAndProperties(String objectName) {

        Hashtable map = new Hashtable();

        int start = objectName.indexOf(":");

        if (start >= 0 && start < objectName.length()) {

            StringTokenizer tokenizer = new StringTokenizer(
                objectName.substring(start+1, objectName.length()), "=,");

            while(tokenizer.hasMoreTokens()) {

                String key = (String)tokenizer.nextElement();

                if (tokenizer.hasMoreElements()) {
                    map.put(key, tokenizer.nextElement());
                }
            }
        }
        return map;
    }

    
   /**
    * Determines whether keys specfied in a map match the keys of objectName.
    * Useful in cases where the real <code>ObjectName</code> object cannot 
    * be created, since the string may be a template. For example,
    * <example>
    *   com.sun.cmm.as:webmodule-virtual-server=${webmodule-virtual-server},
    *                  name=${name},type=CMM_J2eeServlet,
    *                  standalone-web-module=${standalone-web-module},
    *                  server=${server.name} 
    *</example>
    *
    * @param  Map Map of keys and properties of objectName
    * @param  objectName objectName template
    * @return true if keys match; false otherwise
    */
    public static boolean keysMatch(Map map1, String objectName) {

        Map map2 = ObjectNameHelper.getKeysAndProperties(objectName);

        if (map1.size() != map2.size()) {
            return false;
        }
        Iterator iter = map1.keySet().iterator();
        for (;iter.hasNext();) {
            Object key = iter.next();
            if (!map2.containsKey(key))
                return false;
        }
        return true;
    }

    public static boolean keysMatch(String objectName1, String objectName2) {

        return ObjectNameHelper.keysMatch(getKeysAndProperties(objectName1), 
                                            objectName2);
    }
    
    // ---- Instances - Private -------------------------
    private static final String WILD_CARD       = "*";
    private static final String DEF_DELIM_TOKEN = "[$]";
    private static final String DEF_START_TOKEN = "{";
    private static final String DEF_END_TOKEN   = "}";

}
