/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.util.*;
import java.io.*;
import javax.management.MBeanServerConnection;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import static com.sun.enterprise.admin.mbeans.custom.CustomMBeanConstants.*;
public abstract class LifeCycle implements RemoteAdminQuicklookTest 
{
    abstract String testInternal() throws JMException, IOException;

    ////////////////////////////////////////////////////////////////////////////
    ////////////// RemoteAdminQuicklookTest  Impl Methods   ////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    public long getExecutionTime() 
    {
        return ( end - start ) ;
    }

    ////////////////////////////////////////////////////////////////////////////

    public String getName() 
    {
        return ( getClass().getName() );
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public void setMBeanServerConnection(MBeanServerConnection c) 
    {
        mbsc = c;
    }

    ////////////////////////////////////////////////////////////////////////////
    
    public String test() 
    {
        try 
        {
            start = System.currentTimeMillis();
            return (testInternal());
        } 
        catch(final Exception e) 
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally 
        {
            end = System.currentTimeMillis();
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    ///////////////   Tool Methods      ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    String create(String className, String objectName, String name) throws JMException, IOException
    {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> attribs = new HashMap<String, String>();
        params.put(IMPL_CLASS_NAME_KEY, className);
        
        if(ok(objectName))
            params.put(OBJECT_NAME_KEY, objectName);
        if(ok(name))
            params.put(NAME_KEY, name);
        
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "createMBean";
        final Object[] operParams   = new Object[]{ null, params, attribs };
        final String[] operSign     = new String[]{ String.class.getName(), Map.class.getName(), Map.class.getName() };
        return ( (String) mbsc.invoke(on, oper, operParams, operSign) );
    }

    ////////////////////////////////////////////////////////////////////////////

    List<String> list() throws JMException, IOException
    {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "listMBeanNames";
        final Object[] operParams   = new Object[]{ null };
        final String[] operSign     = new String[]{ String.class.getName() };
        return ( (List<String>) mbsc.invoke(on, oper, operParams, operSign) );
    }

    ////////////////////////////////////////////////////////////////////////////

    MBeanInfo getMBeanInfo(String className) throws JMException, IOException
    {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "getMBeanInfo";
        final Object[] operParams   = new Object[]{ className };
        final String[] operSign     = new String[]{ String.class.getName() };
        return ( MBeanInfo ) mbsc.invoke(on, oper, operParams, operSign);
    }

    ////////////////////////////////////////////////////////////////////////////
    
    String delete(String name) throws JMException, IOException
    {
        final ObjectName on         = new ObjectName(BACKEND_MBEAN_ON);
        final String oper           = "deleteMBean";
        final Object[] operParams   = new Object[]{ null, name };
        final String[] operSign     = new String[]{ String.class.getName(), String.class.getName() };
        return ( (String) mbsc.invoke(on, oper, operParams, operSign) );
    }

    ////////////////////////////////////////////////////////////////////////////

    MemoryUsage getMemoryInfo()
    {
        int heap = -1;
        int nonHeap = -1;

        try
        {
            JVMInformationTest jit = new JVMInformationTest();
            jit.setMBeanServerConnection(mbsc);
            String s = jit.getMemoryInfo();
            BufferedReader r = new BufferedReader(new StringReader(s));
            String line;
            
            while((line = r.readLine()) != null)
            {
                if(line.equals("Heap Memory Usage:"))
                    heap = parseMemoryUsage(r);
                if(line.equals("Non-heap Memory Usage:"))
                {
                    nonHeap = parseMemoryUsage(r);
                    break;
                }
            }
            
        }
        catch(Exception e)
        {
            // ignore...
        }
   
        return new MemoryUsage(heap, nonHeap);
   }

    ////////////////////////////////////////////////////////////////////////////

    void title(String s)
    {
        int len = s.length();
        int sub = (starsLength - 10 - len) / 2;
        
        if(sub < 5)
            sub = 5;
        
        System.out.println(stars);
        System.out.println(stars.substring(0, sub) + "     " + s + "     " + stars.substring(0, sub));
        System.out.println(stars);
    }

    ////////////////////////////////////////////////////////////////////////////
    
    private int parseMemoryUsage(BufferedReader r)
    {
        /*
            Heap Memory Usage:
            Memory that Java Virtual Machine initially requested to the Operating System: 0 Bytes
            Memory that Java Virtual Machine is guaranteed to receive from the Operating System: 55,459,840 Bytes
            Maximum Memory that Java Virtual Machine may get from the Operating System: 531,628,032 Bytes. Note that this is not guaranteed

            Memory that Java Virtual Machine uses at this time: 34,602,808 Bytes
         **/
        String searchString = "Memory that Java Virtual Machine uses at this time: ";
        String line;
        
        try
        {
            while((line = r.readLine()) != null)
            {
                if(line.startsWith(searchString))
                    break;
            }

            if(line == null)
                return -1;

            String number = line.substring(searchString.length());
            return parseNumber(number);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    private static int parseNumber(String num)
    {
        String fixed = "";
        for(int i = 0; i < num.length(); i++)
        {
            char c = num.charAt(i);
            
            if(Character.isDigit(c))
                fixed += c;
        }
        
        try
        {
            return new Integer(fixed);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    private boolean  ok(String s)
    {
        return s != null && s.length() > 0;
    }

    ////////////////////////////////////////////////////////////////////////////
    
    private MBeanServerConnection mbsc;
    private long start, end;
    private final int starsLength = stars.length();
    private static final String stars = "************************************************************";
    private static final String BACKEND_MBEAN_ON = "com.sun.appserv:category=config,type=applications";

    ////////////////////////////////////////////////////////////////////////////
    
    class MemoryUsage
    {
        MemoryUsage(int Heap, int NonHeap)
        {
            heap    = Heap;
            nonHeap = NonHeap;
        }

        public String toString()
        {
            return "Heap: " + heap + ", Non-Heap: " + nonHeap;
        }
        int heap;
        int nonHeap;
    }
}
/***

     [java] Heap Memory Usage:
     [java] Memory that Java Virtual Machine initially requested to the Operating System: 0 Bytes
     [java] Memory that Java Virtual Machine is guaranteed to receive from the Operating System: 53,731,328 Bytes
     [java] Maximum Memory that Java Virtual Machine may get from the Operating System: 531,628,032 Bytes. Note that this is not guaranteed.
     [java] Memory that Java Virtual Machine uses at this time: 31,348,920 Bytes

     [java] Non-heap Memory Usage:
     [java] Memory that Java Virtual Machine initially requested to the Operating System: 16,941,056 Bytes
     [java] Memory that Java Virtual Machine is guaranteed to receive from the Operating System: 54,886,400 Bytes
     [java] Maximum Memory that Java Virtual Machine may get from the Operating System: 100,663,296 Bytes. Note that this is not guaranteed.
     [java] Memory that Java Virtual Machine uses at this time: 54,436,552 Bytes

     [java] Approximate number of objects for which finalization is pending: 0


     [java] Class loading and unloading in the Java Virtual Machine:
     [java] Number of classes currently loaded in the Java Virtual Machine: 9,168
     [java] Number of classes loaded in the Java Virtual Machine since the startup: 9,168
     [java] Number of classes unloaded from the Java Virtual Machine: 0
     [java] Just-in-time (JIT) compilation information in the Java Virtual Machine:
*/
