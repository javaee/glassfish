/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package jaxr;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.security.*;
import java.net.*;
import java.util.*;
public class JaxrBean implements SessionBean {
    private SessionContext sc;
    private ConnectionFactory cnfct = null;
    private Connection con = null;
//     private String regurl = "http://www-3.ibm.com/services/uddi/v2beta/inquiryapi";
//     private String regurlp = "https://www-3.ibm.com/services/uddi/v2beta/protect/publishapi";
    private StringBuffer result = new StringBuffer();
    String username = "jaxrsqa3a";
    String password = "testpass";
    public JaxrBean(){}
    
    public void ejbCreate() throws RemoteException {
	try{
	    System.out.println (" IN EJBCREATE "); 
	    InitialContext ic = new InitialContext();
	    
 	    String username = "jaxrsqa1a";
            // (String)ic.lookup("java:comp/env/username");
 	    String password = "testpass";
            // (String)ic.lookup("java:comp/env/password");
	    String regurl =  "http://uddi.ibm.com/testregistry/inquiryapi";
            //(String)ic.lookup("java:comp/env/JaxrQueryURL");
	    String regurlp = "https://uddi.ibm.com/testregistry/publishapi";
            //(String)ic.lookup("java:comp/env/JaxrQueryURL");
	    String httpProxyHost = "webcache.sfbay.sun.com";
            //(String)ic.lookup("java:comp/env/JaxrProxyHost");
	    String httpProxyPort = "8080";
            //(String)ic.lookup("java:comp/env/JaxrProxyPort");;
	    Properties props = new Properties();
	    props.setProperty("javax.xml.registry.queryManagerURL", regurl);
	    props.setProperty("javax.xml.registry.lifeCycleManagerURL", regurlp);
	    props.setProperty("com.sun.xml.registry.https.proxyHost", httpProxyHost);
	    props.setProperty("com.sun.xml.registry.http.proxyHost", httpProxyHost);
	    props.setProperty("com.sun.xml.registry.https.proxyPort", httpProxyPort);
	    props.setProperty("com.sun.xml.registry.http.proxyPort", httpProxyPort);

	    cnfct = (javax.xml.registry.ConnectionFactory)ic.lookup("eis/jaxr");
	    System.out.println (" Connection Factory = "+cnfct); 
	    cnfct.setProperties(props);	    
	} catch(JAXRException e){
	    e.printStackTrace();
	    throw new RemoteException("Cannot instantiate the factory " ,e);
	} catch(Exception e){
	    e.printStackTrace();
	    throw new RemoteException("Error in ejbCreate !!!", e);
	}
	System.out.println("In ejbCreate !! - created ConnectionFactory ");
    }


    public String getCompanyInformation(String company) throws EJBException, 
							       RemoteException{
	if(cnfct == null){
	    return "ConnectionFactory was not instantiated. Test Failed";
	}
	if(company == null)
	    throw new EJBException("Company name not specified");
	// create a jaxr connection instance

        try {
	    //	    System.setProperty("org.apache.commons.logging.simplelog.log.com.sun.xml.registry", "trace");
	    System.out.println (" Connection factory = "+ cnfct); 
	    System.out.println (" Getting connection"); 
	    con = cnfct.createConnection();
	    if (con == null) {
		System.out.println (" Connection is null");
		throw new EJBException("Connection could not be created");
	    }
	    System.out.println("Got the connection = "+ con);
	    RegistryService rs = con.getRegistryService();
	    System.out.println("Got the registry service = "+ rs);
	    BusinessQueryManager bqm = rs.getBusinessQueryManager();
	    System.out.println (" Business Query Manager = "+bqm); 
	    ArrayList names = new ArrayList();
	    names.add(company);
	    
	    Collection fqualifiers = new ArrayList();
	    fqualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
	    System.out.println (" Before findOrganizations "); 
	    BulkResponse br = bqm.findOrganizations(fqualifiers, names, null,
						    null, null, null);

	    System.out.println (" Bulk Response = "+br); 
	    if(br.getStatus() == JAXRResponse.STATUS_SUCCESS){
		System.out.println (" Results found for ("+company+")");
		Collection orgs = br.getCollection();
		Iterator rit = orgs.iterator();
		while(rit.hasNext()){
		    Organization org = (Organization)rit.next();
		    result.append(org.getName().getValue()+"\n");
		    System.out.println (" Name = "+org.getName().getValue());
		    System.out.println ("Description = "+ org.getDescription().getValue());
		    result.append(org.getDescription().getValue()+"\n\n");
		}
		
	    } else{
		System.out.println (" Could not query the registry due to the following exceptions :"); 
		Collection ex = br.getExceptions();
		Iterator it = ex.iterator();
		while(it.hasNext()){
		    Exception e = (Exception) it.next();
		    System.out.println (e.toString()); 
		    result.append(e.toString());
		}
	    }

	// publish to the registry...
	BusinessLifeCycleManager blcm = rs.getBusinessLifeCycleManager();
        // Get authorization from the registry
        PasswordAuthentication passwdAuth =
                new PasswordAuthentication(username,
                    password.toCharArray());
	
        Set creds = new HashSet();
        creds.add(passwdAuth);
        con.setCredentials(creds);
        System.out.println("Established security credentials");



        } catch (Throwable ex) {
            ex.printStackTrace();
	    System.out.println (" Test Failed");
	    result.append("Test Failed");
	    throw new java.rmi.RemoteException(ex.toString());
        }
        return result.toString();
    }
	
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
