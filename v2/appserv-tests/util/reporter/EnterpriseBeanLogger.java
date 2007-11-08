package com.sun.ejte.ccl.reporter;

import java.rmi.RemoteException; 
import javax.rmi.PortableRemoteObject;
import javax.ejb.*;
import javax.transaction.*;
import javax.naming.*;

import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.io.Serializable;
import java.util.logging.*;

public class EnterpriseBeanLogger implements Serializable{
  public static final int PASS_BY_REFERENCE = 0;
  public static final int PASS_BY_VALUE = 1;

  public static String EJB_REF_LOCAL_NAME = "ejbRefLocalName";
  public static String EJB_REF_NAME = "ejbRefName";
  public static String EJB_RELATIONSHIP_NAME = "ejbRelationshipName";
  public static String EJB_RELATIONSHIP_NAME_2 = "ejbRelationshipName2";
  private static Logger logger = Logger.getLogger("bank.admin");
  private static ConsoleHandler ch = new ConsoleHandler();

  /*abstract public Collection getLocalContracts();
  abstract public Collection getRemoteContracts();
   **/
  
  protected PortableAdapter[] adapterHome;

  protected boolean dualMode = false;

  protected boolean doPassByValue = true;
  
  protected int REMOTE_EJB = 0;
  protected int LOCAL_EJB = 1;

  protected boolean outputXML = true;

  private static int instanceCreated;
  protected String instanceName;

  protected boolean prepareLocalAndRemoteObject(String localRef, String remoteRef) throws javax.ejb.CreateException {
    instanceCreated++;
    instanceName = getClass().getName() + "____________________" + instanceCreated;

    adapterHome = new PortableAdapter[2];
    Context ic = null;
    try{
      ic = new InitialContext();

      String mode = ic.lookup("java:comp/env/toXML").toString().toLowerCase();
      if (mode.compareTo("false") == 0){
	outputXML = false;
      }
      toXML("toXML", mode);      

    } catch(java.lang.Exception ex){
      toXML("toXML", "true");    
    } 
   
    try{
      ic = new InitialContext();
      String mode = ic.lookup("java:comp/env/passBy").toString().toLowerCase();
      if (mode.compareTo("both") == 0){
	dualMode = true;
      } else if (mode.compareTo("value") != 0){
	doPassByValue = false;
      }    
      toXML("passBy", mode);     

    } catch(java.lang.Exception ex){
      toXML("exception",ex.getMessage());
      toXML("passBy", "true");    
    } 

    Object objref = null;
    
    if ( (dualMode == true || doPassByValue) && remoteRef.compareTo("") != 0){
      try{
	toXML("remote interface",remoteRef);
	objref = ic.lookup(remoteRef);
	
	adapterHome[REMOTE_EJB] = (PortableAdapter)PortableRemoteObject.narrow(objref, PortableAdapter.class);
	toXML("Remote interface","Looked up remote interface");
      } catch(java.lang.Exception ex){
	logLocalXMLException(ex,"prepareLocalAndRemoteObject - remote");
      }
    } else {
      REMOTE_EJB = 0;
    }

    if (dualMode == true || !doPassByValue){
      if (dualMode == false){
	LOCAL_EJB = 0;
      }
      
      try{
          // Get another object just to be sure <TEST>
          ic = new InitialContext();
          
          toXML("local interface",localRef);
          toXML("local interface",ic.lookup(localRef).toString());
          adapterHome[LOCAL_EJB] = (PortableAdapter)ic.lookup(localRef);
      } catch(javax.naming.NamingException ex){
          logLocalXMLException(ex,"prepareLocalAndRemoteObject - local");
      }
    } else {
        LOCAL_EJB = 0;
    }

    toXML("REMOTE_EJB" , String.valueOf(REMOTE_EJB));
    toXML("LOCAL_EJB" , String.valueOf(LOCAL_EJB));
    return true;
  }

  public String lookupProperty(String s) throws javax.naming.NamingException{
    Context ic = new InitialContext();
    String l = ic.lookup("java:comp/env/" + s).toString();
    toXML("lookup",l);
    return l;
  }


  public String toXML(String value){
    return toXML("debugger",value);
  }

  /**
    * Static method. Very useful for local interface (but not recommended).
    */
  public String toXML(String tag, Object value){
    StringBuffer xml = new StringBuffer();
    xml.append("\n<log time=" + java.util.Calendar.getInstance().getTime().toString()+ ">\n");
    xml.append("\t<class>" + getClass().getName() + "</class>\n");
    xml.append("\t<" + tag + ">" + value.toString() + "</" + tag + ">\n");
    xml.append("</log>");

    /*if (outputXML) System.err.println(xml);
    return xml.toString();*/
    if (outputXML) logger.info(xml.toString());
    return xml.toString();
  }

  public void logLocalXMLException(java.lang.Exception ex, Object msg){
    if (outputXML){
      toXML("exception", msg.toString() + ": " + ex.getMessage());
      ex.printStackTrace(System.err);
      toXML("exception","---NOT THROWN TO CLIENT---");     
    }
  }

  public void logXMLException(java.lang.Exception ex, Object msg){
    if (outputXML){
      toXML("exception", msg.toString() + ": " + ex.getMessage());
      ex.printStackTrace(System.err);
    }
  }

  public void doPassBy(int mode){
    if (mode == EnterpriseBeanLogger.PASS_BY_VALUE)
      doPassByValue = true;
    else 
      doPassByValue = false;
  }

  public void setDualAccess(boolean b){
    dualMode = b;
  }

  public boolean isDualAccess(){
    return dualMode;
  }

  public PortableAdapter getLocalHome(){
    return adapterHome[LOCAL_EJB];
  }

  public PortableAdapter getRemoteHome(){
    return adapterHome[REMOTE_EJB];
  }

}
