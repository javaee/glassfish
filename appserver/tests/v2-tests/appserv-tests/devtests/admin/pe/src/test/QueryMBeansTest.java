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
