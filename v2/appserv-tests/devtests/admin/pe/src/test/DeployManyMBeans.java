package test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import com.sun.enterprise.util.*;

/*
 * DeployManyMBeans.java
 *
 * Created on December 12, 2005, 10:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author bnevins
 */
public class DeployManyMBeans extends LifeCycle
{
    public DeployManyMBeans()
    {
        rnd = new Random();
        int len = 4 + rnd.nextInt(7);
        char[] chars = new char[len];
        
        for(int i = 0; i < len; i++)
        {
            chars[i] = alphabet.charAt(rnd.nextInt(26));
        }
        namePrefix = new String(chars);
        numIter = LocalStrings.getInt("DeployManyMBeans.NUM_BEANS", numIter);
        System.out.println(LocalStrings.get("DeployManyMBeans.NUM_BEANS"));
        System.out.println(LocalStrings.get("foo"));
    }
    
    String testInternal() throws JMException, IOException
    {
        title("DeployManyMBeans");
        title("Stage I -- create " + numIter + " MBeans");
        
        for(int i = 0; i < numIter; i++)
        {
            long msec = System.currentTimeMillis();
            String name = namePrefix + i;
            String classname = classnames[rnd.nextInt(4)];
            String objname = "user:foo=" + name;
            create(classname, objname, name);
            msec = System.currentTimeMillis() - msec;
            System.out.println("Created CMB, name: " + name + ", impl class: " 
                    + classname + ", obj-name: " + objname + ", Time(msec): " 
                    + msec + ", Memory Usage: " + getMemoryInfo());
        }            

        title("Stage II -- check " + numIter + " MBeans");
        
        List<String> list = list();
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            if(!list.contains(name))
                throw new RuntimeException("Could not find " + name + " in list of deployed MBeans");
            System.out.println("Found: " + name);
        }
        title("All MBeans were registered OK");

        title("Stage III -- delete " + numIter + " MBeans");
        
        if(interactive)
            Console.readLine("Shall I delete all of the MBeans? [y]: ");
        
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            delete(name);
            System.out.println("Deleted: " + name);
        }
        
        title("Stage IV  -- check deletion of " + numIter + " MBeans");
        
        list = list();
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            if(list.contains(name))
                throw new RuntimeException(name + " is still in the list of deployed MBeans");
            System.out.println("Verified Deletion of " + name);
        }

        title("All Done!");
        
        return ( SimpleReporterAdapter.PASS );
    }

    private static final String[] classnames = {
            "testmbeans.OneClassDynamicMBean", 
            "testmbeans.MicrowaveOvenImpl", 
            "testmbeans.SimpleStandard", 
            "testmbeans.PrimitiveStandard", 
    };
    
    private int numIter = 500;
    private String namePrefix;
    private String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private Random rnd;
    private boolean interactive = false;
}
