/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.mbeanapi.config;

import java.lang.reflect.Method;
import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.misc.ExceptionUtil;



/**
 * @author alexkrav
 * @version $Revision: 1.5 $
 */
public class ConfigTestHelper {

    static public String toString(Object o)
    {
        return SmartStringifier.toString(o);
    }
    
    static public void printObj(String title, Object o)
    {
        if(title==null)
            title="";
        System.out.println(title+ toString(o));
    }

    static public Object invokeInMgr(Object mgr, String operation)  throws Exception
    {
       return invokeInMgr(mgr, operation, null, null);   
    }
    
    static public Object invokeInMgr(Object mgr, String operation, Object[] params)  throws Exception
    {
        if(params==null || params.length==0)
            return invokeInMgr(mgr, operation, null, null);   
        Class[] classes = new Class[params.length];
        for(int i=0; i<params.length; i++)
            if(params[i]!=null)
                 classes[i] = params[i].getClass();
            else 
                classes[i] = null;
        return invokeInMgr(mgr, operation, params, classes);   
    }
    
    static public Object invokeInMgr(Object mgr, String operation, Object[] params, Class[] classes) throws Exception
    {
        return invokeInMgr(mgr, operation, params, classes, true);   
    }
    
    static public Object invokeInMgr(Object mgr, String operation, Object[] params, Class[] classes, boolean bHideException) throws Exception
    {
        try
        {
            _lastException = null;
            _lastOperation = operation;
            _lastParams = params;
            
            Object res = null;
            Method method =null;
            Class cl = mgr.getClass();
            method = cl.getMethod(operation, classes);
            res = method.invoke(mgr, params);
            _lastResult    = res;
            return res;
        }
        catch (Exception e)
        {
            _lastException = e;
            _lastResult    = null;
            String msg = getLastExceptionShortMsg();
            if(!bHideException)
            {
                e.printStackTrace();
                throw e;
            }
            return null;
        }
    }
    static void printStackTrace(Throwable t)
    {
       ExceptionUtil.getRootCause(t).printStackTrace();
    }
    static String getLastExceptionShortMsg()
    {
        String msg = null;
        if(_lastException!=null)
            msg = ExceptionUtil.getRootCause(_lastException).getMessage();
        int idx = msg!=null?msg.indexOf('\n'):-1; 
          if(idx>=0)
          {
              int idx2 = msg.indexOf('\n', idx+1);
              if(idx2>=0) 
                  idx = idx2;
              msg = msg.substring(0, idx);
          }
        return "Exception: " + msg + " Operation: "+_lastOperation + "("+ SmartStringifier.toString(_lastParams) +")";
    }
    
    static void replaceStr(StringBuffer buf, String strOld, String strNew)
    {
        int idx = 0;
        int lenOld = strOld.length();
        int lenNew = strNew.length();
        while((idx=buf.indexOf(strOld, idx))>=0)
        {
            buf.replace(idx, idx+lenOld, strNew);
            idx+=lenNew;
        }
    }
    
    static public String camelize(String str)
    {
        if(str.length()==0)
            return str;
        StringBuffer buf = new StringBuffer();
        buf.append(Character.toUpperCase(str.charAt(0)));
        for(int i=1; i<str.length(); i++)
        {
            if(str.charAt(i)=='-')
            {
                i++;
                buf.append(Character.toUpperCase(str.charAt(i)));
            }
            else
                buf.append(str.charAt(i));
        }
        replaceStr(buf, "Jdbc","JDBC");
        replaceStr(buf, "Jndi","JNDI");
        replaceStr(buf, "Http","HTTP");
        replaceStr(buf, "Iiop","IIOP");
        replaceStr(buf, "Ssl", "SSL");
        //String res = buf.toString();
        return buf.toString();
    }
    
    static public Object getBeanAttribute(Object bean, String attributeName) throws Exception
    {

        return invokeInMgr(bean, "get"+camelize(attributeName), null, null, true);
    }
    

    public static Throwable _lastException;
    public static Object _lastResult;
    public static String _lastOperation;
    public static Object[] _lastParams;
}
