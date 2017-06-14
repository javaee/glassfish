/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
