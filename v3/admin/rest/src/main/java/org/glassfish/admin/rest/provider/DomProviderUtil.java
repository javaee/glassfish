/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import  org.glassfish.j2ee.statistics.Statistic;

import javax.ws.rs.core.UriInfo;

/**
 *
 * @author rajeshwar patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
public class DomProviderUtil {


    /**
     * returns just the name of the given fully qualified name.
     */
    protected String getName(String typeName) {
        return getName(typeName, '.');
    }


    /**
     * returns just the name of the given fully qualified name.
     */
    protected String getName(String typeName, char delimiter) {
        if ((typeName == null) || ("".equals(typeName))) return typeName;

        //elimiate last char from typeName if its a delimiter
        if (typeName.length() - 1 == typeName.lastIndexOf(delimiter))
            typeName = typeName.substring(0, typeName.length()-1);

        if ((typeName != null) && (typeName.length() > 0)) {
            int index = typeName.lastIndexOf(delimiter);
            if (index != -1) {
                return typeName.substring(index + 1);
            }
        }
        return typeName;
    }

    /**
     * Removes any hypens ( - ) from the given string.
     * When it removes a hypen, it converts next immidiate
     * character, if any,  to an Uppercase.(schema2beans convention)
     * @param string the input string
     * @return a <code>String</code> resulted after removing the hypens
     */
    protected String eleminateHypen(String string){
        if(!(string == null || string.length() <= 0)){
            int index = string.indexOf('-');
            while(index != -1){
                if(index == 0){
                    string = string.substring(1);
                } else {
                    if(index == (string.length() - 1)){
                        string = string.substring(0,string.length()-1);
                    } else {
                        string = string.substring(0,index) +
                            upperCaseFirstLetter(string.substring(index + 1));
                    }
                }
                index = string.indexOf('-');
            }
        }
        return string;
    }



    /**
    * Converts the first letter of the given string to Uppercase.
    *
    * @param string the input string
    * @return the string with the Uppercase first letter
    */
    protected String upperCaseFirstLetter(String string)
    {
        if(string == null || string.length() <= 0){
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }


    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places.
     */
    protected String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }


    static protected final String readAsString(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }


    static protected final String getElementLink(UriInfo uriInfo, String elementName) {
        String link = uriInfo.getAbsolutePath().toString();
        return link.endsWith("/")?
            (link + elementName):(link + "/" + elementName);
    }


    static protected String getStartXmlElement(String name) {
        assert((name != null) && name.length() > 0);
        String result ="<";
        result = result + name;
        result = result + ">";
        return result;
    }


    static protected String getEndXmlElement(String name) {
        assert((name != null) && name.length() > 0);
        String result ="<";
        result = result + "/";
        result = result + name;
        result = result + ">";
        return result;
    }


    static protected Map getStatistics(Statistic statistic) throws 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap results = new HashMap();
        Class classObject = statistic.getClass();
        Method[] methods = 	classObject.getDeclaredMethods();
        for (Method method: methods) {
             int modifier = method.getModifiers();
             //consider only the public methods
             if (Modifier.isPublic(modifier)) {
                 String name = method.getName();
                 //considier only the get* methods
                 if (name.startsWith("get")) {
                     name = name.substring("get".length());
                     Class<?> returnType = method.getReturnType();
                     //consider only the methods that return primitives or String objects)
                     if (returnType.isPrimitive() || returnType.getName().equals("java.lang.String")) {
                         results.put(name, method.invoke(statistic, null));
                     } else {
                         //control should never reach here
                         //we do not expect statistic object to return object
                         //as value for any of it's stats.
                     }
                 }
             }
        }
        return results;
    }
}