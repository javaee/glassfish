/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
