package test;
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

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

