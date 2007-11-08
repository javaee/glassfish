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

package com.sun.enterprise.config;

import java.util.ArrayList;

import java.lang.reflect.Method;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.util.logging.Logger;
import java.util.logging.Level;
//import com.sun.logging.LogDomains;


/**
 * A factory to create ConfigContext objects.
 */
public class ConfigBeansFactory {
    
    //private static final Logger _logger = LogDomains.getLogger(LogDomains.CONFIG_LOGGER);
		
    /*
     * will only work if xpath is of the form
     *  <PRE>
     *      expression := /tagName | /tagName/tagExpression
     *      tagExpression := tagName| tagName[name='value'] | tagName/tagExpression | tagName[name='value']/tagExpression
     *  <PRE>
     */
    
    public static ConfigBean getConfigBeanByXPath(ConfigContext ctx, String xpath)
                throws ConfigException {
         ConfigBean cb = ctx.getRootConfigBean();
         
         if(cb == null) throw new ConfigException("getConfigBeanByXPath: null context");
         cb.setConfigContext(ctx);
         return getConfigBeanByXPath(cb, xpath);
    }
    
    
    /**
     * Utility method to get the bean name based on an xpath
     *
     */
    public static String getConfigBeanNameByXPath(String xpath) {
        XpathSupport xp = new XpathSupport(xpath);
        ArrayList arr = xp.getClassNameArray();
        
        if(arr.size() == 0) return null;
        return (String)arr.get(arr.size()-1);
    }
    
    public static ConfigBean getConfigBeanByXPath(ConfigBean server, String xpath)
                throws ConfigException {
        Object ret = server;
        try {             
            XpathSupport xp = new XpathSupport(xpath);
            ArrayList arr = xp.getMethodArray();
            
            if(arr.size() == 0 || arr.size() == 1) {
                ((ConfigBean) ret).setXPath(xpath); //fixed on 040102
                return (ConfigBean) ret;
            }
            
            for(int i=1;i<arr.size();i++) {
                MethodCaller methodCaller = (MethodCaller) arr.get(i);
				//_logger.log(Level.FINE,"Method = "+methodCaller);
                Method m = ret.getClass().getMethod(methodCaller.getMethodName(), methodCaller.getParamClasses());
				//_logger.log(Level.FINE,"m=" + m);
                ret = m.invoke(ret, methodCaller.getParams());
            }
            
            ((ConfigBean) ret).setConfigContext(server.getConfigContext());
            ((ConfigBean) ret).setXPath(xpath);
            return (ConfigBean) ret;
        } catch(Throwable t) {
			//_logger.log(Level.INFO,"config.get_config_bean_xpath_exception",t);
        }
        return null;
        
    }
    
    private static class XpathSupport {
        private String xpath;
        private ArrayList arr = new ArrayList();
        private ArrayList nameArr = new ArrayList();
        
        XpathSupport(String xpath) {
            this.xpath = xpath;
            process();
        }
        ArrayList getMethodArray() {
            return arr;
        }
        ArrayList getClassNameArray() {
            return nameArr;
        }

        private static char SEPARATOR_CHAR  = '/';
        private static char OPENBRACKET_CHAR  = '[';
        private static char CLOSEBRACKET_CHAR  = ']';
        private static char ESCAPE_CHAR  = '\\';
        
        private static String[] getListOfNodes(String xpath) {
            // String[] ret = new String[];
            ArrayList arr = new ArrayList();
            char ch;
            boolean insideBracket = false;
            StringBuffer sb = new StringBuffer();
            
            if (xpath == null) 
                return new String[]{};
            
            for(int i=0;i<xpath.length();i++) {
                ch = xpath.charAt(i); 
                if(ch == SEPARATOR_CHAR && !insideBracket) {
                    if(sb.length() > 0) {
                        arr.add(sb.toString());
                        sb = new StringBuffer();
                    }
                } else if(ch == SEPARATOR_CHAR && insideBracket) {
                    sb.append(ch);
                } else if(ch == OPENBRACKET_CHAR) {
                    sb.append(ch);
                    insideBracket = true;
                } else if (ch == CLOSEBRACKET_CHAR) {
                    sb.append(ch);
                    insideBracket = false;
                } else {
                       sb.append(ch);
                }
            }
                if(sb.length() >0)
                    arr.add(sb.toString());
            
                // TEMP FIX
                String[] ret = new String[arr.size()];
                for(int j=0;j<arr.size();j++) {
                    ret[j] = (String) arr.get(j);
                }
                    
            return ret;
        }
         
        private static String getTagName(String node) {
            int n = node.indexOf('[');
            if(n == -1) n = node.length();
            String tag = node.substring(0, n);
			//_logger.log(Level.FINE,"Tag"+tag);
            return tag;
        }
        private static String getParamName(String node) {
            int n = node.indexOf('[');
            if(n == -1) return null;
            String p = node.substring(n+2);
            int eq = p.indexOf("=");
            String ret = p.substring(0, eq);
			//_logger.log(Level.FINE,"p="+ret);
            return ret;
        }
        private static String getParamValue(String node) {
            int n = node.indexOf('[');
            if(n == -1) return null;
            String p = node.substring(n+2);
            int eq = p.indexOf("=");
            String ret = p.substring(eq+2, p.length()-2);
			//_logger.log(Level.FINE,"v="+ret);
            return ret;
        }
        void process() {
            // separate the xpaths and put it in mc
            String[] n = XpathSupport.getListOfNodes(xpath);
            //StringTokenizer st = new StringTokenizer(xpath, "/");
          //while (st.hasMoreTokens()) {
            //    String node = st.nextToken();
            for(int i = 0;i<n.length;i++) {
                String node = n[i];
                MethodCaller mc = new MethodCaller();
                String tagName = XpathSupport.getTagName(node);
                String methodName = "get" + convertName(tagName);
                
                String paramName = XpathSupport.getParamName(node);
                String paramValue = XpathSupport.getParamValue(node);
                
                if(paramName!=null && !paramName.equals("")) {
                    methodName += "By" + convertName(paramName); 
                    mc.setMethodName(methodName);
                    mc.setParamClasses(new Class[] {String.class});
                    mc.setParams(new Object[] {paramValue});
                } else {
                    mc.setMethodName(methodName);
                    mc.setParamClasses(new Class[] {});
                    mc.setParams(new Object[] {});
                }
                arr.add(mc);
                nameArr.add(convertName(tagName));
            }
        }
    }
    private static class MethodCaller {
        private String methodName;
        private Class[] c;
        private Object[] o;
        
        public String toString() {
            return "MethodCaller: " + methodName + ",o=" + o;
        }
        void setMethodName(String m) {
            methodName = m;
        }
        String getMethodName(){
            return methodName;
        }
        void setParamClasses(Class[] cl)  {
            c = cl;
        }   
        Class[] getParamClasses() {
            return c;
        }
        void setParams(Object[] ob) {
            o=ob;
        }
        Object[] getParams() {
            return o;
        }
    }
    /**
     * Convert a DTD name into a bean name:
     *
     * Any - or _ character is removed. The letter following - and _
     * is changed to be upper-case.
     * If the user mixes upper-case and lower-case, the case is not
     * changed.
     * If the Word is entirely in upper-case, the word is changed to
     * lower-case (except the characters following - and _)
     * The first letter is always upper-case.
     */
    private static String convertName(String name) {
        CharacterIterator  ci;
        StringBuffer    n = new StringBuffer();
        boolean    up = true;
        boolean    keepCase = false;
        char    c;
        
        // hack need for schema2beans limitation.
        if(name.equals("property")) {
            name = "element-property";
        }
        
        ci = new StringCharacterIterator(name);
        c = ci.first();
        
        // If everything is uppercase, we'll lowercase the name.
        while (c != CharacterIterator.DONE) {
            if (Character.isLowerCase(c)) {
                keepCase = true;
                break;
            }
            c = ci.next();
        }
        
        c = ci.first();
        while (c != CharacterIterator.DONE) {
            if (c == '-' || c == '_')
                up = true;
            else {
                if (up)
                    c = Character.toUpperCase(c);
                else
                    if (!keepCase)
                        c = Character.toLowerCase(c);
                n.append(c);
                up = false;
            }
            c = ci.next();
        }
        return n.toString();
    }
}
