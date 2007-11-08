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

/*
 * AdminInstance.java
 *
 * Created on November 19, 2001, 12:15 PM
 */

package com.sun.enterprise.tools.common.deploy;

import java.beans.*;
import java.util.*;
import java.util.List;
import javax.swing.JOptionPane;
import java.net.InetAddress;

import  com.sun.enterprise.tools.common.LoginFailureException;

import com.sun.enterprise.tools.common.ui.UIMessenger;

/**
 *
 * @author  nityad
 * @version 
 */
public class AdminInstance extends Object implements java.io.Serializable {

    
    private String host_name;
    private static String pw_editor;
    transient private PropertyChangeSupport propertySupport;

    private String name;
    
    private int port_no;
    
    private String path;
    /** Holds value of property userName. */
    private String userName;
    private String password;
    
     
    public AdminInstance(String host, int port, String path) {
        this(host,port,path,null,null);
    }
    
    public AdminInstance(String host, int port, String path, String userName, String password) {
        propertySupport = new PropertyChangeSupport ( this );
        this.host_name = host;
        this.port_no = port;
        this.name = host_name + ":" + port_no;//NOI18N
        this.path = path;
        this.userName = userName;
        this.password = password;
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
        String oldValue = host_name;
        userName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("userName", oldValue, userName);//NOI18N
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
        String oldValue = password;
        password = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("password", oldValue, password);//NOI18N
    }
   public String getHost() {
        return this.host_name;
    }

    public void setHost(String value) {
        String oldValue = host_name;
        this.host_name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("host", oldValue, host_name);//NOI18N
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

 
    public String getName() {
        return this.name;
    }
    
    public void setName(String value) {
        String oldValue = name;
        this.name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
    }
    
    public int getPort() {
        return this.port_no;
    }
    
    public void setPort(int value) {
        int oldValue = port_no;
        this.port_no = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("port", oldValue, port_no);//NOI18N
    }
    
    public String getPath() {
        return this.path;
    }
    
    public void setPath(String value) {
        String oldValue = path;
        this.path = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("path", oldValue, path);//NOI18N
    }
    
    public static boolean AdminServName(String host, int port, List admin){
       boolean exists = false;  
       String value = host + ":" + port;//NOI18N
       for(int i=0; i<admin.size(); i++){
        IAdminInstanceBean instance = (IAdminInstanceBean) admin.get(i);
        String inst = instance.getName();
        if(inst.equals(value))
           exists = true;
       }//for 
       return exists;
    } 
}
