package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Properties;
import javax.naming.*;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.rmi.PortableRemoteObject;

import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;



/**
 * Title: BeanTestClient implementing RASClient interface
 * Description: J2EE test client for admincontroller session bean
 * Copyright: Copyright (c) 2002
 * Company: Sun Microsystems Inc
 * @author Deepa Singh
 * @version 1.0
 */
public class BeanTestClient 
{
  private Properties beanprops=null;
  private javax.naming.Context jndi=null;
  private CustomerRemoteHome ejbHome=null;
  private CustomerRemote ejbObject;
  private static Logger logger = Logger.getLogger("bank.admin");
  private static ConsoleHandler ch = new ConsoleHandler();
  private static SimpleReporterAdapter stat=new SimpleReporterAdapter("appserv-tests");


  //bean specific variables
  String customerID="singhd";

  public BeanTestClient(Properties p)
  {
    this.beanprops=p;
  }

  public void setupJNDIContext(Properties p) 
  {
      try
      {
          jndi=new javax.naming.InitialContext();
      }catch(Exception e) 
      {
          e.printStackTrace();
      }
  }

  public EJBHome getHomeInterface()
  {
    try
    {
      Object obj=(CustomerRemoteHome)jndi.lookup("java:comp/env/ejb/CustomerBean");
      ejbHome=(CustomerRemoteHome)PortableRemoteObject.narrow(obj,com.sun.s1peqe.security.integration.bankadmin.daomanager.CustomerRemoteHome.class);
      System.out.println("Home interface of Customer Bean"+ejbHome.getClass().getName());
    }catch(Throwable e)
    {
        e.printStackTrace();
    }
    return (EJBHome)ejbHome;
  }

  
  public EJBObject getRemoteInterface(EJBHome ejbHome) {
      System.out.println("inside getting remote interface");
      try{
          ejbObject=((CustomerRemoteHome)ejbHome).createCustomer(customerID,customerID);
	  System.out.println("Remote interface of Customer Bean"+ejbObject.getClass().getName());
          
      }catch(javax.ejb.DuplicateKeyException e) {
          System.out.println("Exception:Customer already exists");
      }
      catch (Throwable e) {
          e.printStackTrace();
      }
      return (EJBObject)ejbObject;
  }
  
  public void runTestClient() 
  {
	  try
	  {
		  Object obj=(CustomerRemoteHome)jndi.lookup("java:comp/env/ejb/CustomerBean");
		  ejbHome=(CustomerRemoteHome)PortableRemoteObject.narrow(obj,com.sun.s1peqe.security.integration.bankadmin.daomanager.CustomerRemoteHome.class);
		  System.out.println("Home interface of Customer Bean"+ejbHome.getClass().getName());
	  }catch(Throwable e)
	  {
		  e.printStackTrace();
	  }
	  System.out.println("inside getting remote interface");
	  try{
		  try{ejbObject=((CustomerRemoteHome)ejbHome).findByPrimaryKey(customerID);
		  }catch(javax.ejb.ObjectNotFoundException e){System.out.println("customer does not exist");}
		  if(ejbObject==null)
		  {
			  System.out.println("Creating customer..."+customerID);
			  ejbObject=((CustomerRemoteHome)ejbHome).createCustomer(customerID,customerID);
		  }
		  System.out.println("Remote interface of Customer Bean"+ejbObject.getClass().getName());
		  boolean ret=ejbObject.TestCallerInRole();
		  if(ret==true)
			  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.PASS);
		  else if(ret==false)
			  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
		  else
		  {
			  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
			  System.out.println("Test did not get run");
		  }

	  }catch (Throwable e) {
		  e.printStackTrace();
	  }

	  try {
		  System.out.println("created customer from client"+customerID);
		  boolean ret=ejbObject.TestCallerInRole();
		  if(ret==true)
			  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.PASS);
		  else if(ret==false)
			  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
		  else
		  {
			stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
			System.out.println("Test did not get run");
		  }
			  
	  }catch(Throwable e) {
		  stat.addStatus("JACC:cmp_cmp isCallerInRole",stat.FAIL);
		  e.printStackTrace();
	  }
  }
  
  public void cleanupTests(){
      stat.printSummary("cmp_cmpID");
  }
  
  public void run() {
      try {
          logger.info("inside run method");
          stat.addDescription("This test suite exercises isCallerInRole API as unit test");
          setupJNDIContext(beanprops);
          runTestClient();
          cleanupTests();
      }
      catch(Throwable e) {
          e.printStackTrace();
      }
  }

  public static void main(String args[]) {
      Properties p=null;// we are testing locally only, no properties to test
      BeanTestClient testClient=new BeanTestClient(p);
      testClient.run();
      
  }
  
}
