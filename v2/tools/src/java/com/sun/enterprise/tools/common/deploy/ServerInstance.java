/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.tools.common.deploy;

/*
 * ServerInstance.java
 *
 * Created on December 12, 2000, 12:15 PM
 */

import java.beans.*;
import java.util.*;
import java.io.File;
import javax.swing.JOptionPane;
import java.net.InetAddress;
import java.net.Socket;
import com.sun.enterprise.tools.common.LoginFailureException;
import com.sun.enterprise.tools.common.ui.UIMessenger;

import com.sun.enterprise.admin.servermodel.beans.*;
import com.sun.enterprise.admin.servermodel.ServerInstanceManager;
import com.sun.enterprise.admin.servermodel.ServerInstanceManagerFactory;
import com.sun.enterprise.admin.servermodel.AppServerInstance;
import com.sun.enterprise.admin.common.constant.DeploymentConstants;
import com.sun.enterprise.admin.common.constant.ConfigAttributeName;
import com.sun.enterprise.admin.common.exception.*;
import com.sun.enterprise.admin.util.HostAndPort;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.deploy.NameValuePair;

/**
 *
 * @author  administrator
 * @version 
 */
public class ServerInstance extends Object implements java.io.Serializable {

    
    private String host;
    private String ad_host;
    transient private PropertyChangeSupport propertySupport;

    /** Holds value of property name. */
    private String name;
    
    private String dispName;
    
    /** Holds value of property password. */
    private String password;
    
    /** Holds value of property port. */
    private int port;
    private int ad_port;
    /** Holds value of property userName. */
    private String userName;
    
    public static String pw_editor;
    
    transient boolean checkAlreadyDone = false;
    transient  boolean IAS_Running ;
    transient boolean checkDone = false;
    transient  boolean KJS_Running ;
    transient boolean instRunning;
    transient boolean isEnabled;
    
    transient private ServerInstanceManager manager;
    transient private AppServerInstance instance;
    
    String depName = null;
    
    static final ResourceBundle bundle = ResourceBundle.getBundle("com.sun.enterprise.tools.common.deploy.Bundle"); // NOI18N
     
    /** Creates new ServerInstance */
    public ServerInstance(String SOM_name, String serv_name, int serv_port, String username, String passwd) {
        propertySupport = new PropertyChangeSupport ( this );
        userName = username;
        password = passwd;
        dispName = SOM_name;
        name = serv_name;
        port = serv_port;
        ad_host = serv_name.substring(serv_name.indexOf("(")+1, serv_name.indexOf(":"));//NOI18N
        ad_port = Integer.parseInt( serv_name.substring(serv_name.indexOf(":")+1, serv_name.indexOf(")")) );//NOI18N
    }
    
    public ServerInstance(String SOM_name, String serv_name, int serv_port) {
        this(SOM_name,serv_name,serv_port,"admin","admin123");//NOI18N
    }
    
    public ServerInstance(String SOM_name, String serv_name) {
        this(SOM_name,serv_name,-1);
    }
    
    public static HostAndPort configurInstance(String host_name, int port_no){
       HostAndPort hp = new HostAndPort(host_name, port_no);
       return hp;
    }
      
    public String getHost() {
        return host;
    }

    public void setHost(String value) {
        String oldValue = host;
        host = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("host", oldValue, host);//NOI18N
    }

    private void initPropertyChangeSupport(){
         if(propertySupport==null)
         propertySupport = new PropertyChangeSupport ( this );

    }
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
           
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        initPropertyChangeSupport();
        propertySupport.removePropertyChangeListener (listener);
    }

    /** Getter for property name.
     * @return Value of property name.
 */
    public String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
 */
    public void setName(String value) {
        String oldValue = name;
        name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
       //ServerInstanceManager.getInstance().getServerInstance(name); needs to be setServerInstanceName()
    }
    
    public String getDisplayName() {
        return dispName;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
 */
    public void setDisplayName(String value) {
        String oldValue = dispName;
        dispName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("displayname", oldValue, dispName);//NOI18N
       //ServerInstanceManager.getInstance().getServerInstance(name); needs to be setServerInstanceName()
    }
    
    /** invisible Getter for property password.
     * @return ****
 */
    public String getPrivatePassword() {
        String pw = getPassword();
        String passw = "";//NOI18N
        for(int i=0; i<pw.length(); i++)
            passw = passw + "*";//NOI18N
        return passw;
    }   
    /** Getter for property password.
     * @return Value of property password.
 */
    public String getPassword() {
        pw_editor = password;
        return password;
    }
    
    /** Setter for property password.
     * @param password New value of property password.
 */
    public void setPassword(String value) {
        String oldValue = host;
        password = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("password", oldValue, password);//NOI18N
    }
    
    /** Getter for property port.
     * @return Value of property port.
 */
    public int getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
 */
    public void setPort(int value) {
        Reporter.info("Setting Server Port to " + value);//NOI18N
        String oldValue = host;
        port = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("port", null, null);//NOI18N
    }
    
    /** Getter for property userName.
     * @return Value of property userName.
 */
    public String getUserName() {
        return userName;
    }
    
    /** Setter for property userName.
     * @param userName New value of property userName.
 */
    public void setUserName(String value) {
        String oldValue = host;
        userName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("userName", oldValue, userName);//NOI18N
    }
    
    public String getAdminHost() {
        return ad_host;
    }

    public void setAdminHost(String value) {
        String oldValue = ad_host;
        ad_host = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ad_host", oldValue, ad_host);//NOI18N
    }
    
    public int getAdminPort() {
        return ad_port;
    }

    public void setAdminPort(int value) {
        int oldValue = ad_port;
        ad_port = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ad_port", oldValue, ad_port);//NOI18N
    }
    
    public String getConnectorUrl(){
  /*      Reporter.info("####### getting the URLConnection ####"); // NOI18N
        try{
            if(!getSelectedInstance().isRunning()){
                Reporter.info("####### SLEEPING FOR 10000 seconds ####"); // NOI18N
                Thread.sleep(10000);
            }
            Thread.sleep(10000); //isRunning return true before the server is fully initialize
        }catch(Exception e){
            e.printStackTrace();
        }
        Reporter.info("####### getting the URLConnection after sleep ####"); // NOI18N*/
        return "http://"+this.getName().substring(this.getName().indexOf("(")+1, this.getName().indexOf(":")+1)+
            getPort(); //NOI18N
    }
    
    private  synchronized AppServerInstance getSelectedInstance() throws DeploymentException,AFTargetNotFoundException {
      if(instance == null){
        Reporter.info("Initializing instance");//NOI18N
        String host = this.getName().substring(this.getName().indexOf("(")+1, this.getName().indexOf(":"));//NOI18N
        int port = Integer.parseInt( this.getName().substring(this.getName().indexOf(":")+1, this.getName().indexOf(")")) );//NOI18N
        HostAndPort hostPort = new HostAndPort(host, port);
        Reporter.info("getSelectedInstance " + this.getName().substring(0, this.getName().indexOf("(")));//NOI18N
        Reporter.info(hostPort);
            manager = ServerInstanceManagerFactory.getFactory().getServerInstanceManager(hostPort,userName,password);
            Reporter.assertIt(manager);
            instance = manager.getServerInstance(this.getName().substring(0, this.getName().indexOf("(")));//NOI18N
            Reporter.assertIt(instance);
        }
        return instance;
    }
    
    public void createResource(String fname) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          getSelectedInstance().createResource(fname);
          getSelectedInstance().applyChanges();
          //Apply(); // this causes the IDE to hang
    }
    
/*    
    public void createJDBCResource(String fname, String jndiName, NameValuePair[] params) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          AppServerInstance inst = getSelectedInstance();
          inst.createResource(fname);
          JDBCResource regres = inst.getJDBCResource(jndiName);

          if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    regres.setAttribute(
                         ConfigAttributeName.PROPERTY_NAME_PREFIX + params[i].getParamName(),
                         params[i].getParamValue());
                }
          }
          inst.applyChanges();
          //Apply(); // this causes the IDE to hang
    }
        
    public void createJDBCConnectionPool(String fname, String jndiName, NameValuePair[] params) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          AppServerInstance inst = getSelectedInstance();
          inst.createResource(fname);
          JDBCConnectionPool regres = inst.getJDBCConnectionPool(jndiName);

          if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    regres.setAttribute(
                         ConfigAttributeName.PROPERTY_NAME_PREFIX + params[i].getParamName(),
                         params[i].getParamValue());
                }
          }
          inst.applyChanges();
          //Apply(); // this causes the IDE to hang
    }
    
    public void createJMSResource(String fname, String jndiName, NameValuePair[] params) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          AppServerInstance inst = getSelectedInstance();
          inst.createResource(fname);
          JMSResource regres = inst.getJMSResource(jndiName);

          if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    regres.setAttribute(
                         ConfigAttributeName.PROPERTY_NAME_PREFIX + params[i].getParamName(),
                         params[i].getParamValue());
                }
          }
          inst.applyChanges();
          //Apply(); // this causes the IDE to hang
    }
    
    public void createPMFResource(String fname, String jndiName, NameValuePair[] params) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          AppServerInstance inst = getSelectedInstance();
          inst.createResource(fname);
          PersistenceManagerFactoryResource regres = inst.getPersistenceManagerFactoryResource(jndiName);

          if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    regres.setAttribute(
                         ConfigAttributeName.PROPERTY_NAME_PREFIX + params[i].getParamName(),
                         params[i].getParamValue());
                }
          }
          inst.applyChanges();
          //Apply(); // this causes the IDE to hang
    }
    
    public void createMailResource(String fname, String jndiName, NameValuePair[] params) throws AFException,AFResourceException,AFTargetNotFoundException, DeploymentException{
          AppServerInstance inst = getSelectedInstance();
          inst.createResource(fname);
          JavaMailResource regres = inst.getJavaMailResource(jndiName);

          if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    regres.setAttribute(
                         ConfigAttributeName.PROPERTY_NAME_PREFIX + params[i].getParamName(),
                         params[i].getParamValue());
                }
          }
          inst.applyChanges();
          //Apply(); // this causes the IDE to hang
    }
*/
    
    public boolean deployEarFile( String earPath, UIMessenger messenger) throws DeploymentException {
      final String path = earPath;
      Reporter.info(earPath);
      Iterator it = null;
      InetAddress IP = null;
      boolean deploySuccess = true;
      try{  
        IP = InetAddress.getByName(this.getAdminHost());
        Socket s = new Socket (IP, this.getAdminPort());
        s.close ();  
        try{
            Reporter.info("Getting App Name");//NOI18N
            String appName = path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));//NOI18N
            boolean exists = false;
            Reporter.info(appName);
            try{
                it = getSelectedInstance().getDeployedApplications();
            }catch( AFException e){
                throw new DeploymentException(e.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(AFRuntimeStoreException afre){
                throw new AFRuntimeStoreException(afre.getLocalizedMessage());
            }  
            messenger.setProgressMessage(bundle.getString("ExsistingAppCheck"));//NOI18N
            messenger.setProgressLevel(30);
            while (it.hasNext())
            {
                depName = it.next().toString();
               // System.out.println("Deployed App: " + depName);
                if(depName.equalsIgnoreCase(appName)){
                    exists = true;
                    break;
                }
            }
            Reporter.info(getSelectedInstance().getDeployedApplication(appName));
            if(exists){
                Reporter.info("Calling Redeploy");//NOI18N
                messenger.setProgressMessage( bundle.getString("RedeployMessage"));//NOI18N
                messenger.setProgressLevel(55);
                deploySuccess = getSelectedInstance().redeployApplication(path,appName);
            }else{
                Reporter.info("Calling Deploy");//NOI18N
                messenger.setProgressMessage( bundle.getString("DeployMessage"));//NOI18N
                messenger.setProgressLevel(55);
                deploySuccess = getSelectedInstance().deployApplication(path, appName, true, null, true, false, false);
            }
/*            
            
            try{
                it = getSelectedInstance().getDeployedApplications();
            }catch(AFException ex){
                throw new DeploymentException(ex.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(AFRuntimeStoreException afre){
                throw new AFRuntimeStoreException(afre.getLocalizedMessage());
            } 
 */
            messenger.setProgressLevel(90);
    /*ludo Why???        try{
                Thread.sleep(10000);// remove when status is available from som
            }catch (Exception e){
            }
     **/
            messenger.setProgressLevel(100);
        }catch(AFTargetNotFoundException te){
            throw new DeploymentException(te.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
        }catch(DeploymentException de){
            throw new DeploymentException(de.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
        }catch(AFRuntimeStoreException afre){
            messenger.ConfigXmlChanged(this.getName());
        } 
      }catch(java.net.UnknownHostException e){ 
           throw new DeploymentException(e.getLocalizedMessage());
      }catch(java.io.IOException con){
           throw new DeploymentException(con.getLocalizedMessage());
      } 
      return deploySuccess;
    }
        
    public boolean deployWarFile(String warPath, String waName, String contextRoot, UIMessenger messenger) throws DeploymentException {
      final String path = warPath;
      Reporter.info("Context Root" + contextRoot + "War Path" + warPath);//NOI18N
      Iterator it = null;
      InetAddress IP = null;
      boolean deploySuccess = true;
      try{  
        IP = InetAddress.getByName(this.getAdminHost());
        Socket s = new Socket (IP, this.getAdminPort());
        s.close (); 
        try{
            String webAppName = waName; // path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));
            Reporter.info("webAppName is : " + webAppName);//NOI18N
            boolean exists = false;
            try{
                it = getSelectedInstance().getDeployedWebModules();
            }catch( AFException e){
               throw new DeploymentException(e.getLocalizedMessage());
               //JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(AFRuntimeStoreException afre){
                throw new AFRuntimeStoreException(afre.getLocalizedMessage());
            }  
            messenger.setProgressMessage( bundle.getString("ExsistingModuleCheck"));//NOI18N
            messenger.setProgressLevel(30);
            while (it.hasNext())
            {
                depName = it.next().toString();
                if(depName.equalsIgnoreCase(webAppName)){
                    exists = true;
                    break;
                }
            }
            messenger.setProgressMessage( bundle.getString("DeployWar"));//NOI18N
            if(exists){
                Reporter.info("Calling Redeploy");//NOI18N
                messenger.setProgressMessage( bundle.getString("RedeployModuleMessage")); //NOI18N
                messenger.setProgressLevel(55);
                deploySuccess = getSelectedInstance().redeployWarModule(path, webAppName, contextRoot);
            }else{
                Reporter.info("Calling Deploy");//NOI18N
                messenger.setProgressMessage( bundle.getString("DeployModuleMessage")); //NOI18N
                messenger.setProgressLevel(55);
                deploySuccess = getSelectedInstance().deployWarModule(path, webAppName, contextRoot, true, null, true, false, false);
            }
            messenger.setProgressLevel(90);
 /*ludo why           try{
                Thread.sleep(10000);// remove when status is available from som
            }catch (Exception e){
                Reporter.verbose("Error while waiting for request to be completed");//NOI18N
            }*/
            messenger.setProgressLevel(100);
         }catch(AFTargetNotFoundException te){
            throw new DeploymentException(te.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
         }catch(DeploymentException de){
            throw new DeploymentException(de.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
         }catch(AFRuntimeStoreException afre){
                messenger.ConfigXmlChanged(this.getName());
         }
      }catch(java.net.UnknownHostException e){ 
           throw new DeploymentException(e.getLocalizedMessage());
      }catch(java.io.IOException con){
           throw new DeploymentException(con.getLocalizedMessage());
      } 
      return deploySuccess;
    }

    public boolean deployEjbJarFile(String ejbJarName, String ejbjarfilePath, UIMessenger messenger) throws DeploymentException {
        final String path = ejbjarfilePath;
        Iterator it = null;
        Reporter.info(ejbjarfilePath);
        InetAddress IP = null;
        boolean deploySuccess = true;
        try{  
            IP = InetAddress.getByName(this.getAdminHost());
            Socket s = new Socket (IP, this.getAdminPort());
            s.close (); 
            try{
                //String ejbJarName = path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));//NOI18N
                Reporter.info("EjbJar Name" + ejbJarName);//NOI18N
                boolean exists = false;
                try{
                    it = getSelectedInstance().getDeployedEJBModules();
                }catch( AFException e){
                    throw new DeploymentException(e.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                   throw new AFRuntimeStoreException(afre.getLocalizedMessage());
                }  
                messenger.setProgressMessage( bundle.getString("ExsistingModuleCheck"));//NOI18N
                messenger.setProgressLevel(30);
                while (it.hasNext())
                {
                    depName = it.next().toString();
                  //  System.out.println("Deployed App: " + depName);
                    if(depName.equalsIgnoreCase(ejbJarName)){
                        exists = true;
                        break;
                    }
                }
                messenger.setProgressMessage( bundle.getString("DeployEjbJar"));//NOI18N
                if(exists){
                    Reporter.info("Calling Redeploy");//NOI18N
                    messenger.setProgressMessage( bundle.getString("RedeployModuleMessage")); //NOI18N
                    messenger.setProgressLevel(55);
                    deploySuccess = getSelectedInstance().redeployEJBJarModule(path, ejbJarName);
                }else{
                    Reporter.info("Calling Deploy");//NOI18N
                    messenger.setProgressMessage( bundle.getString("DeployModuleMessage")); //NOI18N
                    messenger.setProgressLevel(55);
                    deploySuccess = getSelectedInstance().deployEJBJarModule(path, ejbJarName);
                }
                messenger.setProgressLevel(90);
 /*               try{
                    Thread.sleep(10000);// remove when status is available from som
                }catch (Exception e){
                    Reporter.verbose("Error while waiting for request to be completed");//NOI18N
                }*/
                messenger.setProgressLevel(100);
            }catch(AFTargetNotFoundException te){
                throw new DeploymentException(te.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(DeploymentException de){
                throw new DeploymentException(de.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(AFRuntimeStoreException afre){
                messenger.ConfigXmlChanged(this.getName());
            }
      }catch(java.net.UnknownHostException e){ 
           throw new DeploymentException(e.getLocalizedMessage());
      }catch(java.io.IOException con){
           throw new DeploymentException(con.getLocalizedMessage());
      }  
        return deploySuccess;
     }

    
    
    /*added by ludo: ability to deploy standalone RAR files...
     **/
        public boolean deployRARFile(String rarfilePath, UIMessenger messenger) throws DeploymentException {
        final String path = rarfilePath;
        Iterator it = null;
        Reporter.info(rarfilePath);
        InetAddress IP = null;
        boolean deploySuccess = true;
        try{  
            IP = InetAddress.getByName(this.getAdminHost());
            Socket s = new Socket (IP, this.getAdminPort());
            s.close (); 
            try{
                String rarName = path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));//NOI18N
                Reporter.info("rar Name" + rarName);//NOI18N
                boolean exists = false;
                try{
                    it = getSelectedInstance().getDeployedConnectors();
                }catch( AFException e){
                    throw new DeploymentException(e.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    throw new AFRuntimeStoreException(afre.getLocalizedMessage());
                }  
                if(messenger != null) messenger.setProgressMessage( bundle.getString("ExsistingModuleCheck"));//NOI18N
                if(messenger != null) messenger.setProgressLevel(30);
                while (it.hasNext())
                {
                    depName = it.next().toString();
 //                   System.out.println("Deployed App: " + depName);
                    if(depName.equalsIgnoreCase(rarName)){
                        exists = true;
                        break;
                    }
                }
                if(messenger != null) messenger.setProgressMessage( bundle.getString("DeployRar"));//NOI18N
                if(exists){
                    Reporter.info("Calling Redeploy");//NOI18N
                    if(messenger != null)  messenger.setProgressMessage( bundle.getString("RedeployModuleMessage")); //NOI18N
                        deploySuccess = getSelectedInstance().redeployConnectorModule(path, rarName);
                }else{
                    Reporter.info("Calling Deploy");//NOI18N
                    if(messenger != null) messenger.setProgressMessage( bundle.getString("DeployModuleMessage")); //NOI18N
                        deploySuccess = getSelectedInstance().deployConnectorModule(path, rarName);
                }
                if(messenger != null) messenger.setProgressLevel(90);
             /*   try{
                    Thread.sleep(10000);// remove when status is available from som
                }catch (Exception e){
                    Reporter.verbose("Error while waiting for request to be completed");//NOI18N
                }*/
                if(messenger != null) messenger.setProgressLevel(100);
            }catch(AFTargetNotFoundException te){
                throw new DeploymentException(te.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(DeploymentException de){
                throw new DeploymentException(de.getLocalizedMessage());
                //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("ErrorDeploying"), JOptionPane.ERROR_MESSAGE);//NOI18N
            }catch(AFRuntimeStoreException afre){
                messenger.ConfigXmlChanged(this.getName());
            }
      }catch(java.net.UnknownHostException e){ 
           throw new DeploymentException(e.getLocalizedMessage());
      }catch(java.io.IOException con){
           throw new DeploymentException(con.getLocalizedMessage());
      }  
        return deploySuccess;
     }
    
     public void registerDatasource (String xmlFile)  throws DeploymentException {
       //IASDeployer iasDeployer = new IASDeployer();
      //iasDeployer.registerDatasource(this, xmlFile);
     }
     
     private void initEngine() throws LoginFailureException {
         //if (null == ec) {
         //       ec = new EngineController(getHost(),Integer.toString(getPort()),getUserName(),
         //                                  getPassword(),KSRPCPacket.GXADMIN_ENGTYPE_KJS);

         //}  // ec is not null     
    }
    public void removeEngineController(){    
        //if (null != ec) {
        //     ec.disconnect();
        //     ec =null;
        // }
    }
    
//    public synchronized void cleanupPreviousTaskLaunchedByTheIDE(){
       //if (ec==null)// optimization: the engine did not start and we try to kill a process: not need to start i!!!
       //      return;
       //ec.start();

//    }
    public void stopInstance(final UIMessenger messenger){
        try{
            Reporter.info("---------------------stopInstance"); // NOI18N
            class StopThread extends Thread{
            public void run () {  
                try{
                    if(getSelectedInstance().isRunning()){
                            getSelectedInstance().stop();
                    }
                    Reporter.info("---------------------AFTER STOP-------------"); // NOI18N
                }catch(AFTargetNotFoundException te){
                    messenger.setNotify(te.getLocalizedMessage());
                }catch(DeploymentException de){
                    messenger.setNotify(de.getLocalizedMessage());
                }catch(InstanceNotRunningException inre){
                    messenger.setNotify(inre.getLocalizedMessage());
                }catch(ControlException ce){
                   messenger.setNotify(ce.getLocalizedMessage());
                }catch(AFException afe){
                    messenger.setNotify(afe.getLocalizedMessage());
                }
                
             }
            }
            StopThread stop_server = new StopThread();
            stop_server.start();
            Thread.sleep(500);
            while(stop_server.isAlive()){
              Thread.sleep(1000);
            }
            Reporter.info("---------------------After stop server"); // NOI18N
        }catch(Exception e){
             e.printStackTrace();
        } 
    }
    
    public boolean stopInstance() throws AFTargetNotFoundException, InstanceNotRunningException, 
                                                                ControlException, AFException, DeploymentException{
        Reporter.info("---------------------STARTANYKJS"); // NOI18N
        boolean status = false;
        if(getSelectedInstance().isRunning())
             status = getSelectedInstance().stop();
        Reporter.info("---------------------AFTER START-------------"); // NOI18N
        return status;
    }
    
    public void restart(){
        try{
            Reporter.info("---------------------RESTART"); // NOI18N
            class RestartThread extends Thread{
            public void run () {  
                try{
                    getSelectedInstance().restart();
                    Reporter.info("---------------------AFTER RESTART-------------"); // NOI18N
                }catch(AFTargetNotFoundException te){
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Err_RestartServer"), JOptionPane.ERROR_MESSAGE);//NOI18N
                    te.printStackTrace();
                }catch(DeploymentException de){
                    //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("Err_RestartServer"), JOptionPane.ERROR_MESSAGE);//NOI18N
                    de.printStackTrace();
                }catch(ControlException ce){
                    //JOptionPane.showMessageDialog(null, ce.getLocalizedMessage(), bundle.getString ("Err_RestartServer"), JOptionPane.ERROR_MESSAGE);//NOI18N
                    ce.printStackTrace();
                }catch(AFException afe){
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Err_RestartServer"), JOptionPane.ERROR_MESSAGE);//NOI18N
                    afe.printStackTrace();
                }
             }
            }
            RestartThread restart = new RestartThread();
            restart.start();
            //Thread.sleep(5000);
            //while(restart.isAlive()){
                //wait
            //}
            Reporter.info("---------------------Ready after restart"); // NOI18N
        }catch(Exception e){
             e.printStackTrace();
        } 
    
    }
    
    public void startInstance(final UIMessenger messenger){
        try{
            Reporter.info("---------------------STARTANYKJS"); // NOI18N
            class StartThread extends Thread{
            public void run () {  
                try{
                    if(!getSelectedInstance().isRunning()){
                            getSelectedInstance().start();
                    }
                    Reporter.info("---------------------AFTER START-------------"); // NOI18N
                }catch(AFTargetNotFoundException te){
                    messenger.setNotify(te.getLocalizedMessage());
                }catch(DeploymentException de){
                    messenger.setNotify(de.getLocalizedMessage());
                }catch(InstanceAlreadyRunningException iare){
                    messenger.setNotify(iare.getLocalizedMessage());
                }catch(ControlException ce){
                    messenger.setNotify(ce.getLocalizedMessage());
                }catch(AFException afe){
                   messenger.setNotify(afe.getLocalizedMessage());
                }
             }
            }
            
            StartThread start_server = new StartThread();
            start_server.start();
           // Thread.sleep(500);
            while(start_server.isAlive()){
                Thread.sleep(1000);
            }
            Reporter.info("---------------------Ready after start"); // NOI18N
        }catch(Exception e){
             e.printStackTrace();
        } 
    
    }
    
    public boolean startInstance() throws AFTargetNotFoundException, InstanceAlreadyRunningException, 
                                                                ControlException, AFException, DeploymentException{
        Reporter.info("---------------------STARTANYKJS"); // NOI18N
        boolean status = false;
        if(!getSelectedInstance().isRunning()){
            status = getSelectedInstance().start();
        }
        Reporter.info("---------------------AFTER START-------------"); // NOI18N
        return status;
        //    messenger.setErrorMsg(bundle.getString ("Err_NoTarget"), bundle.getString ("Err_StartServer"));* //NOI18N
    }
    
    public boolean isRunning() throws AFException, AFTargetNotFoundException, DeploymentException, AFRuntimeStoreException{
        instRunning = false;
        instRunning = getSelectedInstance().isRunning();
        return instRunning;
    }
    
    public  boolean isLocal() throws DeploymentException {
         try{
            String host=InetAddress.getLocalHost().getHostName();
            return host.equals (getHost());
        }
        catch(java.net.UnknownHostException e){
            throw new DeploymentException(e.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
        }
    }
     public  boolean isUp(){
            if (checkAlreadyDone==true)
                  return IAS_Running;
            //IAS_Running = ServerAccess.isKASAlive(getPort(), 1);
            checkAlreadyDone = true;
            Timer aTimer = new Timer();
            TimerTask task = new TimerTask(){
                public void run() {
                            synchronized (this) {
                                checkAlreadyDone = false;
                            }
                }

            };
            aTimer.schedule(task,2000);
            return IAS_Running;

            
        }
        
               
    public static boolean ServName(String value, List serv, String nod_name, HostAndPort hostport,String uName,String passwd){
       boolean exists = false;  
       value = value + "(" + nod_name + ")";//NOI18N
       for(int i=0; i<serv.size(); i++){
        IServerInstanceBean instance = (IServerInstanceBean) serv.get(i);
        String inst = instance.getName();
        if(inst.equals(value))
           exists = true;
       }//for 
       Iterator it = ServerInstanceManagerFactory.getFactory().getServerInstanceManager(hostport,uName,passwd).getAllServerInstances();
       while (it.hasNext()){
           String inst1 = ((AppServerInstance)it.next()).toString();
           if(inst1.equals(value))
              exists = true;
       }
      
      return exists;
  } 
  
  public void Undeploy(final String resName, final String resType, final UIMessenger messenger){
      final String server = this.getName();
      try{
        Reporter.info("---------------------Before Undeploy "); // NOI18N
        class UndeployThread extends Thread{
            public void run () { 
                try{
                    if(resType.trim().indexOf(".ear") != -1){ // NOI18N
                        getSelectedInstance().undeployApplication(resName);
                    }else{
                        if(resType.trim().indexOf(".war") != -1){ // NOI18N
                            getSelectedInstance().undeployModule(resName, DeploymentConstants.WAR);
                        }else if(resType.trim().indexOf(".jar") != -1){ // NOI18N
                            getSelectedInstance().undeployModule(resName, DeploymentConstants.EJB);   
                        }else if(resType.trim().indexOf(".rar") != -1){ // NOI18N
                            getSelectedInstance().undeployModule(resName, DeploymentConstants.RAR);   
                        }
                    }//else
                    getSelectedInstance().applyChanges();
                    messenger.setStatusText(java.text.MessageFormat.format(bundle.getString("Msg_FinUndeploy"), new Object[]  {server}) ); //NOI18N
                }catch(AFTargetNotFoundException te){
                    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
                    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
             }//run
        }
        UndeployThread undeploy = new UndeployThread();
        undeploy.start();
        Reporter.info("---------------------After Undeploy"); // NOI18N
        while(undeploy.isAlive()){
             Thread.sleep(500);
        }
        
        Reporter.info("---------------------Finished Undeploy"); // NOI18N
      }catch(Exception e){
             e.printStackTrace();
      } 
  }//Undeploy
  
  public void Apply(final UIMessenger messenger){
      final String server = this.getName();
      try{
        Reporter.info("---------------------Applying changes to Server Instance"); // NOI18N
        class ApplyThread extends Thread{
            public void run () { 
                try{
                    getSelectedInstance().applyChanges();
                    messenger.setStatusText(java.text.MessageFormat.format(bundle.getString("Msg_Applyed"), new Object[]  {server}) ); //NOI18N
                }catch(AFTargetNotFoundException te){
		    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
                    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
            }//run
        }
        ApplyThread appl = new ApplyThread();
        appl.start();
        Reporter.info("---------------------After Applying changes to Server Instance"); // NOI18N
        while(appl.isAlive()){
             Thread.sleep(500);
        }
        Reporter.info("---------------------Finished Applying changes to Server Instance"); // NOI18N
      }catch(Exception e){
             e.printStackTrace();
      } 
  }//Apply
  
  public void Enable(final String resName, final String resType, final UIMessenger messenger){
      final String server = this.getName();
      try{
        Reporter.info("---------------------Enabling Resource"); // NOI18N
        class EnableThread extends Thread{
            public void run () { 
                try{
                    if(resType.trim().indexOf(".ear") != -1){ // NOI18N
                        DeployedApplicationComponentBean component = getSelectedInstance().getDeployedApplication(resName);
                        component.enable();
                    }else{
                        if(resType.trim().indexOf(".jar") != -1){ // NOI18N
                            DeployedEJBModuleComponentBean  component = getSelectedInstance().getDeployedEJBModule(resName);
                            component.enable();
                        }else if(resType.trim().indexOf(".war") != -1){ // NOI18N
                            DeployedWebModuleComponentBean component = getSelectedInstance().getDeployedWebModule(resName);   
                            component.enable();
                        }else{
                            Reporter.info("####### UNKNOWN Archive Type");//NOI18N
                        }
                    }//else
                    getSelectedInstance().applyChanges();
                    messenger.setStatusText(java.text.MessageFormat.format(bundle.getString("Msg_FinEnable"), new Object[]  {server}) ); //NOI18N
                }catch(AFTargetNotFoundException te){
		    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
		    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
            }//run
        }
        EnableThread enThread = new EnableThread();
        enThread.start();
        Reporter.info("---------------------After Enabling Resource"); // NOI18N
        while(enThread.isAlive()){
            Thread.sleep(500);
        }
        Reporter.info("---------------------Finished Enabling Resource"); // NOI18N
      }catch(Exception e){
             e.printStackTrace();
      } 
  }//Enabled
  
  public void Disable(final String resName, final String resType, final UIMessenger messenger){
      final String server = this.getName();
      try{
        Reporter.info("---------------------Before Disabling Resource"); // NOI18N
        class DisableThread extends Thread{
            public void run () { 
                try{
                    if(resType.trim().indexOf(".ear") != -1){ // NOI18N
                        DeployedApplicationComponentBean component = getSelectedInstance().getDeployedApplication(resName);
                        component.disable();
                    }else{
                        if(resType.trim().indexOf(".jar") != -1){ // NOI18N
                            DeployedEJBModuleComponentBean  component = getSelectedInstance().getDeployedEJBModule(resName);
                            component.disable();
                        }else if(resType.trim().indexOf(".war") != -1){ // NOI18N
                            DeployedWebModuleComponentBean component = getSelectedInstance().getDeployedWebModule(resName);   
                            component.disable();
                        }else{
                            Reporter.info("####### UNKNOWN Archive Type");//NOI18N
                        }
                    }//else
                    getSelectedInstance().applyChanges();
                    messenger.setStatusText(java.text.MessageFormat.format(bundle.getString("Msg_FinDisable"), new Object[]  {server}) ); //NOI18N
                }catch(AFTargetNotFoundException te){
		    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
		    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
            }//run
        }
        DisableThread disThread = new DisableThread();
        disThread.start();
        Reporter.info("---------------------After Disabling Resource"); // NOI18N
        while(disThread.isAlive()){
             Thread.sleep(500);
        }
        Reporter.info("---------------------Finished Disabling Resource"); // NOI18N
      }catch(Exception e){
             e.printStackTrace();
      } 
  }//Disabled
  
  
  public boolean isEnabled(final String resName, final String resType, final UIMessenger messenger){
        final String server = this.getName();
        Reporter.info("---------------------Before checking if resource is enabled"); // NOI18N
        try{
            if(resType.trim().indexOf(".ear") != -1){ // NOI18N
               DeployedApplicationComponentBean component = getSelectedInstance().getDeployedApplication(resName);
               isEnabled = component.isEnabled();
            }else{
               if(resType.trim().indexOf(".war") != -1){ // NOI18N
                  DeployedWebModuleComponentBean component = getSelectedInstance().getDeployedWebModule(resName);
                  isEnabled = component.isEnabled();
               }else if(resType.trim().indexOf(".jar") != -1){ // NOI18N
                  DeployedEJBModuleComponentBean  component = getSelectedInstance().getDeployedEJBModule(resName);
                  isEnabled = component.isEnabled();
               }
            }//else
            //messenger.setStatusText(java.text.MessageFormat.format(bundle.getString("Msg_Applyed"), new Object[]  {server}) );
        }catch(AFTargetNotFoundException te){
            messenger.setNotify(te.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
        }catch(AFException afe){
            messenger.setNotify(afe.getLocalizedMessage());
            //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
        }catch(AFRuntimeStoreException afre){
            //messenger.ConfigXmlChanged(server);
        }
        
        Reporter.info("---------------------Finished checking if resource is enabled"); // NOI18N
        return isEnabled;
  }//isEnabled
  
  
  
  public void deleteJdbc(final String resourceName, final UIMessenger messenger){
    final String server = this.getName();
    try{
        Reporter.info("---------------------Before deleting Jdbc"); // NOI18N
        class deleteJdbcThread extends Thread{
            public void run () { 
                try{
                    getSelectedInstance().deleteJDBCResource(resourceName);
                }catch(DeploymentException de){  
		    messenger.setNotify(de.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFTargetNotFoundException te){
		    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
		    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
            }
        }//deleteJDBCThread
        deleteJdbcThread delJdbc = new deleteJdbcThread();
        delJdbc.start();
        Reporter.info("---------------------After deleting Registered Jdbc "); // NOI18N
        while(delJdbc.isAlive()){
           //wait
        }
        getSelectedInstance().applyChanges();
        Reporter.info("---------------------Finished deleting Registered Jdbc"); // NOI18N
    }catch(Exception e){
        e.printStackTrace();
    } 
  }//deleteJdbc
  
  public void deleteResource(final String resourceName, final String resType, final UIMessenger messenger){
    final String server = this.getName();
    try{
        Reporter.info("---------------------Before deleting resource"); // NOI18N
        class deleteResourceThread extends Thread{
            public void run () {
                try{
                    if (resType.equals("JDBC")) // NOI18N
                            getSelectedInstance().deleteJDBCResource(resourceName);
                    else if (resType.equals("CP")) // NOI18N
                            getSelectedInstance().deleteJDBCConnectionPool(resourceName);
                    else if (resType.equals("JMS")) // NOI18N
                            getSelectedInstance().deleteJMSResource(resourceName);
                    else if (resType.equals("PMF")) // NOI18N
                            getSelectedInstance().deletePersistenceManagerFactoryResource(resourceName);
                    else if (resType.equals("Mail")) // NOI18N
                            getSelectedInstance().deleteJavaMailResource(resourceName);
                    else
                            Reporter.error(bundle.getString("Err_NoResource"));
                }catch(DeploymentException de){
		     messenger.setNotify(de.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, de.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFTargetNotFoundException te){
		    messenger.setNotify(te.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, bundle.getString ("Err_NoTarget"), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFException afe){
		    messenger.setNotify(afe.getLocalizedMessage());
                    //JOptionPane.showMessageDialog(null, afe.getLocalizedMessage(), bundle.getString ("Error"), JOptionPane.ERROR_MESSAGE);//NOI18N
                }catch(AFRuntimeStoreException afre){
                    messenger.ConfigXmlChanged(server);
                }
            }
        }//deleteResourceThread
        deleteResourceThread del = new deleteResourceThread();
        del.start();
        Reporter.info("---------------------After deleting registered resource"); // NOI18N
        while(del.isAlive()){
           //wait
        }
        getSelectedInstance().applyChanges();
        Reporter.info("---------------------Finished deleting Registered Resource"); // NOI18N
    }catch(Exception e){
        e.printStackTrace();
    }
  }//deleteResource

  private int debugPort; 
  public int startInDebugMode(final UIMessenger messenger){
      debugPort = -1;
      try{
          Reporter.info("---------------------STARTANYKJS"); // NOI18N
          class StartDebugThread extends Thread{
              public void run() {
                  try{
                      if(!getSelectedInstance().isRunning()){
                          
                          debugPort = getSelectedInstance().startInDebugMode();
                      }
                      Reporter.info("---------------------AFTER START-------------"); // NOI18N
                  }catch(AFTargetNotFoundException te){
                       messenger.setNotify(te.getLocalizedMessage());
                  }catch(DeploymentException de){
                       messenger.setNotify(de.getLocalizedMessage());
                  }catch(InstanceAlreadyRunningException iare){
                       messenger.setNotify(iare.getLocalizedMessage());
                  }catch(ControlException ce){
                       messenger.setNotify(ce.getLocalizedMessage());
                  }catch(AFException afe){
                      messenger.setNotify(afe.getLocalizedMessage());
                  }
              }
          }
          
          StartDebugThread debug_server = new StartDebugThread();
          debug_server.start();
          Thread.sleep(500);
          
          while(debug_server.isAlive()){
              Thread.sleep(1000);
          }
          Reporter.info("---------------------Ready after start"); // NOI18N
      }catch(Exception e){
          e.printStackTrace();
      }
      return debugPort;
  }
 
  public int startInDebugMode() throws AFTargetNotFoundException, DeploymentException, ControlException,
                                                                InstanceAlreadyRunningException, AFException{
      debugPort = -1;
      Reporter.info("---------------------STARTANYKJS"); // NOI18N
      if(!getSelectedInstance().isRunning()){
          debugPort = getSelectedInstance().startInDebugMode();
      }
      Reporter.info("---------------------AFTER START-------------"); // NOI18N
      return debugPort;
  }
  
  public void OverwriteConfig() throws AFException{
      getSelectedInstance().overwriteChanges();
  }
  
  public void useManualConfig() throws AFException{
      getSelectedInstance().useManualChanges();
  }
  
  public int getORBPort() throws AttributeNotFoundException, AccessViolationException,AFTargetNotFoundException, DeploymentException, AFException {
        Integer portObj = (Integer) getSelectedInstance().getORBComponent().getIiopListener("orb-listener-1").getAttribute("port"); //NOI18N
        int port = portObj.intValue(); 
        Reporter.verbose("getORBPort() returns " + port);  //NOI18N
        return port;
  }
  
  /* return -1 if the server is not running in debug mode, otherwise, returns the JPDA port number to listen to , using dt_socket mode
   **/
  public int getJPDAPortNumber() throws  AFException {
        int p =   getSelectedInstance().getServerStatus().getDebugPort();
        Reporter.verbose("getJPDAPortNumber() returns " + p);  //NOI18N
        return p;
  }
}
