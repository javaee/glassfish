/*
 * LocalServiceRegistry.java
 *
 * Created on November 7, 2007, 1:15 PM
 *
 */
package com.sun.enterprise.registration;

/**
*
* This class generates a local registry file thats going to be stored under 
* install/lib dir. The service tag attributes are stored in this file for a 
* particular installation.
*
*/
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Properties;
import java.net.ConnectException;
import java.net.UnknownHostException;
import com.sun.scn.servicetags.SvcTag;
//import com.sun.enterprise.util.RegistrationUtil;
//import com.sun.enterprise.registration.SysnetRegistrationService;

public class LocalServiceRegistry {
    
    private ServiceTag  servicetag;

    //Revist: make the debug to true for debugging purpose
    boolean debug = false;
	
    private final static  LocalServiceRegistry registry = new LocalServiceRegistry();
    /** Creates a new instance of ServiceRegistry */
    private LocalServiceRegistry() {
	createSerivceRegistryFile();
    }

    public static LocalServiceRegistry getLocalRegistry() {
	 return registry;
    }
    /**
     * @param args the command line arguments
    */
    public static void main(String[] args) {
	
        Properties p = System.getProperties();
	//p.list(System.out);
	System.out.println("From Local service registry  ..");
	LocalServiceRegistry reg = LocalServiceRegistry.getLocalRegistry();
	//Revisit: set the debug flag from ant file itself
	//reg.showServiceTagAttributes();
    }

    private void createSerivceRegistryFile() {
        Properties data = System.getProperties();

        //String registryName = SysnetRegistrationService.getRepositoryFile();
	//RegistrationUtil.getServiceTagRegistry();

	//create product_defined_inst_id tag

	String registryName = System.getProperty("srvcRegisFileName");
	try {
	    File registry =  new File(registryName);
            RepositoryManager rm = new RepositoryManager(registry);
            ServiceTag st = new ServiceTag(data);
	    rm.add(st);
        } catch(RegistrationException ex) {
            //ex.printStackTrace();
        }
    }

    public  void showServiceTagAttributes() {
        Properties data = System.getProperties();
	showAttribute(ServiceTag.PRODUCT_NAME);
	showAttribute(ServiceTag.PRODUCT_VERSION);
	showAttribute(ServiceTag.PRODUCT_URN);
	showAttribute(ServiceTag.PRODUCT_PARENT);
	showAttribute(ServiceTag.PRODUCT_PARENT_URN);
	showAttribute(ServiceTag.PRODUCT_DEFINED_INST_ID);
	showAttribute(ServiceTag.CONTAINER);
	showAttribute(ServiceTag.SOURCE);
	showAttribute(ServiceTag.INSTANCE_URN);
	showAttribute(ServiceTag.STATUS);
	showAttribute(ServiceTag.REGISTRATION_STATUS);
	showAttribute(ServiceTag.SERVICE_TAG);
        //List<ServiceTag> list = rm.getServiceTags();
        //System.out.println("List of service tags:");
        //for (ServiceTag tag : list) {
              //System.out.println(tag.toString());
        //}
        //rm.write(new FileOutputStream("test.xml"));
    }
    
    public  void showAttribute(String key) {
	if (debug) {
	    System.out.println(key +" = " + System.getProperty(key));
	}
    }    
}
