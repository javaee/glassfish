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

package com.sun.enterprise.deployapi;

import com.sun.appserv.management.client.ConnectionSource;
import com.sun.enterprise.admin.common.exception.AFException;
import com.sun.enterprise.deployment.client.DeploymentClientUtils;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.deploy.spi.Target;
import javax.management.MBeanServerConnection;

/**
 * The Sun RI does not support clustering for now, we cannot 
 * group several servers in one logical target so we have a 
 * one to one mapping between a Target and a server
 *
 * @author Jerome Dochez
 */
public class SunTarget implements Target, Serializable {
    
    private ServerConnectionIdentifier connectionInfo;
    private String appServer;
    private boolean connected=false;
    private ConnectionSource dasConnection = null;
    private MBeanServerConnection mbsc = null;
    private String targetType;

    private static StringManager localStrings = StringManager.getManager(SunTarget.class);
    
    private static final String DAS_TARGET_NAME = "server";
    
    public SunTarget(ServerConnectionIdentifier svi) {
        this.connectionInfo = svi;
    }
    
    public SunTarget(SunTarget other) {
        this.connectionInfo = other.connectionInfo;
        this.appServer = other.appServer;
        this.dasConnection = other.dasConnection;
        this.mbsc = other.mbsc;
        this.targetType = other.targetType;
    }
    
    /** Retrieve other descriptive information
     * about the target.
     */
    public String getDescription() {
        //@@@ or connect to the server for a full version
        String version = localStrings.getString(
                             "enterprise.deployapi.spi.ProductVersion", "9.0");
                             
        return localStrings.getString(
                    "enterprise.deployapi.spi.suntargetdescription",
                    version, getHostName());
    }
    
    /** Retrieve the name of the target server.
     */
    public String getName() {
        return appServer;
    }
   
   /**
    * Release our ressources
    */
   public void release() {
       connected=false;
   }
   
   /**
    * @return the hostname that this target represents
    */ 
   public String getHostName(){
       return connectionInfo.getHostName();
   }
   
   /**
    * @return the port name to connect to the host
    */
   public String getPort() {
       return (new Integer(connectionInfo.getHostPort())).toString();
   }
   
   /**
    * @return the connection info for this target
    */
   public ServerConnectionIdentifier getConnectionInfo() {
       return connectionInfo;
   }
   
   /** 
    * @return true if the deployment manager is connected to the server
    */
   public boolean isConnected() {
       return connected;
   }              
    
   /**
    * @return a meaningful string about myself
    */   
   public String toString() {
       return getHostName() + ":" + (getPort()!=null?getPort():"DefaultPort") + "_" + appServer;
   }

   /**
    * @return a meaningful string about myself
    */   
   public String debugString() {
       String s = "";
       if (connected) {
           s = "Connected ";
       }
       return s + "Server " + getHostName() + ":" + (getPort()!=null?getPort():"DefaultPort") + "; Name: " + appServer;
   }
   
   
    /**
     * @return true if I am the equals to the other object
     */
    public boolean equals(Object other) {
        
        if (other instanceof SunTarget) {
            SunTarget theOther = (SunTarget) other;
            return (connectionInfo.equals(theOther.connectionInfo)
                 && getName() != null && getName().equals(theOther.getName())
                 && getTargetType() != null && getTargetType().equals(theOther.getTargetType()));
        }
        return false;
    }
    
    /*
     * @return a hash code based on values used in equals
     */
    public int hashCode() {
        int result = 17;
        result = 37 * result + ((connectionInfo == null) ? 0 : connectionInfo.hashCode());
        result = 37 * result + ((getName() == null) ? 0 : getName().hashCode());
        result = 37 * result + ((getTargetType() == null) ? 0 : getTargetType().hashCode());
        return result;
    }

    /**
     * @return true if I am managed by the same DAS as the other object
     */
    public boolean isManagedBySameDAS(Object other) {
        if (other instanceof SunTarget) {
            SunTarget theOther = (SunTarget) other;
            return connectionInfo.equals(theOther.connectionInfo);
        }
        return false;
    }

    /**
     *Returns whether this target is a DAS.
     *@return true if the target is a DAS
     */
    public boolean isDAS() {
        return (appServer.equals(DAS_TARGET_NAME));
    }
    
    /**
     * @return the application server associated with this target
     */
    public String getAppServerInstance() {
        return appServer;
    }
    
    /**
     * Set the application server instance associated with this target
     */
    public void setAppServerInstance(String appServer) {
        this.appServer = appServer;
    }

    /**
     * Set the type of this target
     */
    public void setTargetType(String type) {
        this.targetType = type;
    }
    
    /**
     * @return the type of this target
     */
    public String getTargetType() {
        return this.targetType;
    }
    
    public void setConnectionSource(ConnectionSource conn){
        this.dasConnection = conn;
    }

    public ConnectionSource getConnectionSource() {
        return this.dasConnection;
    }

    public MBeanServerConnection getMBeanServerConnection() {
        return this.dasConnection.getExistingMBeanServerConnection();
    }
   
    /**
     *  Exports the Client stub jar to the given location.
     *  @param appName The name of the application or module.
     *  @param destDir The directory into which the stub jar file 
     *  should be exported.
     *  @return Retruns the absolute location to the exported jar file.
     *  @throws AFException
     */
    public String exportClientStubs(String  appName, 
                                    int     appType, 
                                    String  destDir)
        throws AFException
    {
        try{
            return DeploymentClientUtils.downloadClientStubs(
                        appName, destDir, dasConnection);
        }catch(Exception e){
            e.printStackTrace();
            throw new AFException(e.getMessage());
        }
    }
}
