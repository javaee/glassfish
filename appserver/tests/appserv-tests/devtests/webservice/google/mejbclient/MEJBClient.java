/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
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

package google;

//import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.management.j2ee.ManagementHome;
import javax.management.j2ee.Management;
import javax.management.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class MEJBClient {

//    private SimpleReporterAdapter stat =
//        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) { 
        MEJBClient client = new MEJBClient(); 

        // run the tests
        client.runTestClient();   
    }

    public void runTestClient() {
        try{
//            stat.addDescription("Testing webservices mejb client app.");
            test01();
//            stat.printSummary("webservicesMejbAppID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }
    
    public void test01() {
	
	// instanciate the mejb
   	try {
            
	    Context ic = new InitialContext();
	    System.out.println("***Created initial context");
            
	    java.lang.Object objref = ic.lookup("ejb/mgmt/MEJB");
	    System.out.println("***Looked up ManagementHome!!");

	    ManagementHome  home = (ManagementHome)PortableRemoteObject.narrow(
			                     objref, ManagementHome.class);
	    System.out.println("***Narrowed ManagementHome!!");

	    Management mejb = home.create();
	    System.out.println("***Got the MEJB!!");

	    String domain = mejb.getDefaultDomain();
	    System.out.println("***MEJB default domain = "+domain);
	    int mbeanCount = mejb.getMBeanCount().intValue();
	    System.out.println("***MBeanCount = "+mbeanCount);

	    // Print attributes of all MBeans 
            ObjectName query = new ObjectName("*:j2eeType=J2EEApplication,name=googleApp,*");
	    Set mbeanNames = mejb.queryNames(query, null);
	    if ( mbeanNames.size() != 1 ) {
		System.out.println("***ERROR: mbeans returned by query is "
                +mbeanNames.size() + " it should be 1");
	    }
		
	    Iterator it = mbeanNames.iterator();
	    ObjectName name = (ObjectName)it.next();
	    System.out.println("Obtained jsr77 mbean for googleApp = "+name);

		// Get attr values from MEJB and print them
                try{                    
                    Object attr = mejb.getAttribute(name, "hasWebServices");
		    boolean hasWebServices=false;
		    if (attr instanceof Boolean) {
			hasWebServices = ((Boolean) attr).booleanValue();
		    }
		    	
		    System.out.println("For HasWebServices "+hasWebServices);

                    attr = mejb.getAttribute(name, "endpointAddresses");
		    if (attr instanceof String[]) {
			String[] addresses = (String[]) attr;
			for (int i=0;i<addresses.length;i++) {
			    System.out.println("Registered addresses " + addresses[i]);

			    // retrieve the wsdl  file
			    URL url = new URL("http://localhost:8080/"+addresses[i]+"?wsdl");
			    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			    connection.setRequestMethod("GET");
			    connection.connect();
			    int responseCode = connection.getResponseCode();
			    if (responseCode!=HttpURLConnection.HTTP_OK) {
				System.out.println("Cannot retrieve wsdl for " + addresses[i] + " error is " + connection.getResponseMessage());
			    } else {
				InputStream is = new BufferedInputStream(connection.getInputStream());
				byte[] buffer = new byte[100];
				int read;
				do {
				    read = is.read(buffer,0, 100);
				    System.out.println(new String(buffer,0, read));
				} while(read==100 && read!=-1);
			    }
			}
		    }
                   
                }
                catch(Exception exp){
                    //exp.printStackTrace();
                    System.out.println("***Exception occured while "+
                            "accessing mbean details:  Keep continuing\n");
                }

	    mejb.remove();

	} catch(NamingException ne){
            System.out.println("***Exception while initializing context.\n");
            ne.printStackTrace();
	} catch(Exception re) {
            re.printStackTrace();
	} 	
    }

}
