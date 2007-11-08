
/**
 *
 * @author bnevins
 */
package test;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import com.sun.enterprise.util.*;

public class AttributeSniffer extends LifeCycle
{
    public AttributeSniffer()
    {
    }
    
    String testInternal() throws JMException, IOException
    {
        title("AttributeSniffer");
       
        for(String classname : classnames)
        {
            System.out.println(classname);
            MBeanInfo info = getMBeanInfo(classname);
            MBeanAttributeInfo[] atts = info.getAttributes();
            
            for(MBeanAttributeInfo ainfo : atts)
            {
                System.out.println("ATTRIBUTE --> name=[" + ainfo.getName() + "], type=[" + ainfo.getType() + "], Is Writable: " + ainfo.isWritable());
            }
        }
        return ( SimpleReporterAdapter.PASS );
    }

    private static final String[] classnames = {
            "testmbeans.OneClassDynamicMBean", 
            "testmbeans.MicrowaveOvenImpl", 
            "testmbeans.SimpleStandard", 
            "testmbeans.PrimitiveStandard", 
    };
    
    //private int numIter = 500;
    //private String namePrefix;
    //private String alphabet = "abcdefghijklmnopqrstuvwxyz";
    //private Random rnd;
    //private boolean interactive = false;
}
