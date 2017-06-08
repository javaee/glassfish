/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import com.sun.appserv.test.BaseDevTest;
/*
import com.sun.messaging.AdminConnectionConfiguration;
import com.sun.messaging.AdminConnectionFactory;
*/
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;


/*
 * @author Satish
 */
public class DynamicSyncBrokerListTest extends AdminBaseDevTest {

    private static final String CLUSTER_NAME = "cluster1";
    private static final String INSTANCE1_NAME = "instance1";
    private static final String INSTANCE2_NAME = "instance2";
    private static final String INSTANCE3_NAME = "instance3";
    private static final String CLUSTER2_NAME = "cluster2";

    public static void main(String[] args) {
        new DynamicSyncBrokerListTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for the dynamic sync list broker feature";
    }

    @Override
    public void cleanup() {
        try {

            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
            //asadmin("stop-cluster", CLUSTER2_NAME);
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);

            asadmin("delete-cluster", CLUSTER_NAME);
            //asadmin("delete-cluster", CLUSTER2_NAME);
            asadmin("stop-database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        asadmin("create-cluster", CLUSTER_NAME);
        asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                /*"--node", "localhost",*/ "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848",
                INSTANCE1_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                /*"--node", "localhost",*/ "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848",
                INSTANCE2_NAME);

        asadmin("start-cluster", CLUSTER_NAME);

        asadmin("create-local-instance",
                "--node", "localhost-domain1","--cluster", CLUSTER_NAME, "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=38181:IIOP_SSL_LISTENER_PORT=33800:IIOP_LISTENER_PORT=33700:JMX_SYSTEM_CONNECTOR_PORT=37676:IIOP_SSL_MUTUALAUTH_PORT=33801:JMS_PROVIDER_PORT=38686:ASADMIN_LISTENER_PORT=34848",
                INSTANCE3_NAME);

        checkAdd2brokerlist("18686");

        asadmin("stop-local-instance", INSTANCE3_NAME);
        asadmin("delete-local-instance", INSTANCE3_NAME);

        checkDeleteFromBrokerlist("18686");

        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void checkAdd2brokerlist(String jmsport){
        String testName = "InstanceAdditionJmsClusterCheck";
        Object retval = jmxCall(jmsport, "com.sun.messaging.jms.server:type=Cluster,subtype=Monitor");
        AsadminReturn result = new AsadminReturn();
        if (retval != null && retval instanceof String[]){
            String[] addresses = (String[]) retval;
            result.out="";
            result.err="";
            for(String address : addresses)
                        result.out=result.out + address + ",";

            System.out.println("Broker Address List " + result.out);
            if (addresses.length == 3) {
                  result.returnValue=true;
            }

       } else result.returnValue=false;

        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "38686");
    }
    private void checkDeleteFromBrokerlist(String jmsport){
        String testName = "InstanceDeletionJmsClusterCheck";
        Object retval = jmxCall(jmsport, "com.sun.messaging.jms.server:type=Cluster,subtype=Monitor");
        AsadminReturn result = new AsadminReturn();
        if (retval != null && retval instanceof String[]){
            String[] addresses = (String[]) retval;
            result.out="";
            result.err="";
            for(String address : addresses)
                        result.out=result.out + address + ",";

            System.out.println("Broker Address List " + result.out);


            if (addresses.length == 2) {
                  result.returnValue=true;

            }

       } else result.returnValue=false;

        reportResultStatus(testName, result);
        reportExpectedFailureResult(testName, result, "38686");
    }

    public Object jmxCall(String jmsProviderPort, String objectName){
/*
 * Commented out because building this code depends on having GlassFish already downloaded.
 * The admin devtest must build without having GlassFish present. 
     try{
        AdminConnectionFactory acf = new AdminConnectionFactory();
        acf.setProperty(AdminConnectionConfiguration.imqAddress,    "localhost:" + jmsProviderPort);
        JMXConnector connector = acf.createConnection("admin","admin");


      MBeanServerConnection mbsc = connector.getMBeanServerConnection();

      System.out.println("connected to target server");

      ObjectName objName
               = new ObjectName(objectName); //"com.sun.messaging.jms.server:type=Broker,subtype=Monitor");

      Object [] params = null;

      String []  signature = new String[0];
      Object retval= mbsc.invoke(objName, "getBrokerAddresses", params, signature);

       connector.close();
       return retval;

      }catch(Exception ex){
                    ex.printStackTrace();
     }
*/
        return null;
    }

    private void reportFailureResultStatus(String testName, AsadminReturn result) {
        report(testName, ! result.returnValue);
        report(testName, ! result.err.isEmpty());
    }


    private void reportResultStatus(String testName, AsadminReturn result) {
        report(testName, result.returnValue);
        report(testName, result.err.isEmpty());
    }

    private void reportExpectedFailureResult(String testName, AsadminReturn result, String... expected) {
        for (String token : expected) {
            report(testName, ! result.out.contains(token));
        }
    }

    private void reportExpectedResult(String testName, AsadminReturn result, String... expected) {

        for (String token : expected) {
            report(testName, result.out.contains(token));
        }
    }

    private void reportUnexpectedResult(String testName, AsadminReturn result, String... unexpected) {
        for (String token : unexpected) {
            report(testName, !result.out.contains(token));
        }
    }
}


