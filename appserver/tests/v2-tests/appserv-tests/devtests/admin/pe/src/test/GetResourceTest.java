package test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import com.sun.enterprise.util.*;

/**
 *
 * @author bnevins
 */

public class GetResourceTest extends LifeCycle
{
    public GetResourceTest()
    {
    }
    
    String testInternal() throws JMException, IOException
    {
        long msec = System.currentTimeMillis();
        title("GetResourceTest");
        title("Stage I -- deploy ");
        String name = "getResourceTest";
        String classname = "testmbeans.AWT";
        String objname = "user:foo=" + name;
        create(classname, objname, name);
        msec = System.currentTimeMillis() - msec;
        System.out.println("Created CMB, name: " + name + ", impl class: " 
                + classname + ", obj-name: " + objname + ", Time(msec): " 
                + msec + ", Memory Usage: " + getMemoryInfo());

        title("Stage II -- check ");
        List<String> list = list();
        if(!list.contains(name))
            throw new RuntimeException("Could not find " + name + " in list of deployed MBeans");
        System.out.println("Found: " + name);
        title("All MBeans were registered OK");

        if(interactive)
            Console.readLine("Go look at the AWT window and hit ENTER: ");

        title("Stage III -- delete ");
        
        if(interactive)
            Console.readLine("Shall I delete the MBeans? [y]: ");
            delete(name);
            System.out.println("Deleted: " + name);
        
        title("Stage IV  -- check deletion");
        
        list = list();
        if(list.contains(name))
            throw new RuntimeException(name + " is still in the list of deployed MBeans");
        System.out.println("Verified Deletion of " + name);
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
