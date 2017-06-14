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
 * QueryMBeansTest.java
 *
 * Created on July 22, 2005, 12:53 AM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.mbeans.jvm.JVMInformationMBean;
import com.sun.enterprise.admin.server.core.AdminService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 */
public class QueryMBeansTest implements RemoteAdminQuicklookTest {

    private MBeanServerConnection mbsc;
    
    private long start, end;

    public QueryMBeansTest() {}

    public void setMBeanServerConnection(MBeanServerConnection c) {
        this.mbsc = c;
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            queryAllMBeans();
            System.out.println("Gets all MBeans");
            return SimpleReporterAdapter.PASS;
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            end = System.currentTimeMillis();
        }
    }

    public String getName() {
        return ( this.getClass().getName() );
    }

    public long getExecutionTime() {
        return ( end - start );
    }
    
    private File getFile() {
        String path = null; //set it to user passed arg
        if (path != null) 
            return new File(path);
        else return new File("./ListOfMbeans.txt");
    }
    
    private void queryAllMBeans() throws Exception {
        ObjectName name = null;
        QueryExp query = null;
        
        String[] domains = mbsc.getDomains();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(getFile()));
            for (String domain : domains) {
                name = new ObjectName(domain+":*");
                Set<ObjectName> mbeanObjNames = mbsc.queryNames(name, query);
                logDomainMBeansTofile(pw, domain, mbeanObjNames);
            }
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (pw != null) pw.close();
        }
    }
    
    private void logDomainMBeansTofile(PrintWriter pw, String domain, Set<ObjectName> mbeanObjNames) 
    throws IOException {
        pw.println("/***********************************************************/");
        pw.println("                     " + domain + "                         ");
        pw.println("/***********************************************************/");
        
        for (ObjectName objName : mbeanObjNames) 
            pw.println(objName.toString());
        pw.flush();
    }
    
}