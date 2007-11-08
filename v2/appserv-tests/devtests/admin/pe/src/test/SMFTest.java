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
 * SMFTest.java
 *
 * Created on August 22, 2005, 5:40 PM
 */
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.enterprise.admin.servermgmt.SMFService;
import com.sun.enterprise.admin.servermgmt.SMFService.AppserverServiceType;
import com.sun.enterprise.admin.servermgmt.SMFServiceHandler;
import com.sun.enterprise.admin.servermgmt.ServiceHandler;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import javax.management.MBeanServerConnection;
/**
 */
public class SMFTest implements RemoteAdminQuicklookTest {
    private final static String CONFIG_FILE="smftest.properties";
    //look at this file first
    
    private long start, end;
    public SMFTest() throws Exception {
        setProperties();
        start = System.currentTimeMillis();
    }

    public long getExecutionTime() {
        return ( end - start );
    }

    public void setMBeanServerConnection(final MBeanServerConnection c) {
	}

    public String getName() {
	    return ( this.getClass().getName() );
    }

    public String test() {
        try {
            start = System.currentTimeMillis();
            testPlatform();
            createSMFService();
            System.out.println("This test just creates the SMF service");
            return ( SimpleReporterAdapter.PASS );
        } catch(final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            end = System.currentTimeMillis();
        }
        
    }
    private void setProperties() throws Exception {
        final Properties additional = new Properties();
        additional.load(new FileInputStream(CONFIG_FILE));
        final Properties existing = System.getProperties();
        existing.putAll(additional);
        System.setProperties(existing);
        existing.list(System.out);
    }
    private void testPlatform() throws Exception {
        final String OS_NAME = "SunOS";
        final String OS_VERS = "5.10";
        System.out.println(System.getProperty("os.name"));
        System.out.println(System.getProperty("os.version"));
        final boolean ok = OS_NAME.equals(System.getProperty("os.name")) && 
                           OS_VERS.equals(System.getProperty("os.version"));
        if (!ok)
            throw new RuntimeException("Runs only on Solaris 10");
    }
    
    private void createSMFService() {
        final ServiceHandler smfsh = new SMFServiceHandler();
        final SMFService ss = new SMFService();
        ss.setDate(new Date().toString());
        ss.setAsadminPath(System.getProperty("AS_ADMIN_PATH"));
        ss.setName(System.getProperty("SERVICE_NAME"));
        ss.setLocation(System.getProperty("SERVICE_LOCATION"));
        ss.setFQSN();
        ss.setOSUser();
        ss.setPasswordFilePath(System.getProperty("PASSWORD_FILE_PATH"));
        ss.setType(SMFService.AppserverServiceType.valueOf(System.getProperty("SERVICE_TYPE")));
        System.out.println(ss.toString());
        final boolean v = ss.isConfigValid();
        smfsh.createService(ss.tokensAndValues());
    }
}
