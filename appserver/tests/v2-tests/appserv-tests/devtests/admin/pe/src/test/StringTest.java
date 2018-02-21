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

/*
 * StringTest.java
 *
 * Created on February 19, 2006
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import java.util.*;

/**
 */
public class StringTest implements RemoteAdminQuicklookTest
{
    public StringTest() throws Exception
    {
    }
    
    public long getExecutionTime()
    {
        return ( end - start );
    }
    
    public void setMBeanServerConnection(final MBeanServerConnection c)
    {
    }
    
    public String getName()
    {
        return ( this.getClass().getName() );
    }
    
    public String test()
    {
        try
        {
            start = System.currentTimeMillis();
            bundles = CMBStrings.getBundles();
            dump();
            return ( SimpleReporterAdapter.PASS );
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

    private void dump()
    {
        // the first bundle has the log messages...
         ResourceBundle bundle = bundles.get(0);
         makeLog(bundle);
         dumpLog();
    }

    private void makeLog(ResourceBundle bundle)
    {
        
        List<Property> props = new ArrayList<Property>();
        Enumeration<String> keys = bundle.getKeys();

        while(keys.hasMoreElements())
        {
            String key = keys.nextElement();
            
            try
            {
                String val = bundle.getString(key);

                if(key.startsWith("ADM16") || val.startsWith("ADM16"))
                    props.add(new Property(key, val));
            }
            catch(Exception e)
            {
                System.out.println("EXCEPTION getting value for " + key);
            }
            
        }

        // now create a list of LogProperty from the props...
        for(Property p : props)
        {
            if(p.cause == false)
                loglist.add(new LogProperty(p, props));
        }
        
        
    }
    private void dumpLog()
    {
        System.out.println("\n******************************************************");
        System.out.println("************* DUMP LOG MESSAGES **********************");
        System.out.println("******************************************************\n");
        for(LogProperty p : loglist)
            System.out.println(p);
    }
    
    int parseNum(String s)
    {
        try 
        {
            return Integer.parseInt(s.substring(3, 7));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    private final static String CONFIG_FILE="stringtest.properties";
    private long start, end;
    private List<ResourceBundle> bundles;
    private List<LogProperty> loglist = new ArrayList<LogProperty>();
    private final static String CMB_PREFIX = "ADM16";
    
    class Property
    {
        Property(String k, String v)
        {
            key = k;
            val = v;
            
            if(key.startsWith(CMB_PREFIX))
            {
                num = parseNum(key);
                cause = true;
            }
            else if(val.startsWith(CMB_PREFIX))
            {
                num = parseNum(val);
                cause = false;
            }
        }
        Property(Property p)
        {
            key = p.key;
            val = p.val;
            cause = p.cause;
            num = p.num;
        }
        public String toString()
        {
            return "Key: " + key + "\nVal: " + val + "\nNum: " + num +  "\n";
        }
        String key;
        String val;
        int num;
        boolean cause = false;
    }
    
    class LogProperty extends Property
    {
        LogProperty(Property mainProp, List<Property> allProps)
        {
            super(mainProp);
            assert cause == false;
            
            // look for cause...
            
            for(Property prop : allProps)
            {
                if(!prop.cause)
                    continue;
            
                if(num != prop.num)
                    continue;
                
                causes.add(prop);
            }
        }

        public String toString()
        {
            String ret = super.toString();
            
            if(causes.size() <= 0)
                return ret + " ***** MISSING DIAG MESSAGES *********" + "\n\n";

            ret += "******  ASSOCIATED DIAG MESSAGES ****\n";
            
            for(Property p : causes)
                ret += p.toString();
            
            return ret + "\n";
            
        }

        List<Property> causes = new ArrayList<Property>();
    }
    
}

