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
 * MailResource.java
 *
 * Created on March 14, 2002, 12:23 PM
 *
 * Author: Shirley Chiang
 */

package com.sun.enterprise.tools.common.deploy;

import java.beans.*;
import java.util.List;
import java.io.File;
import java.util.Vector;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

public class MailResource extends Object implements java.io.Serializable {
  
    private String name;
    private String Description;
    private String JndiName;  
    private String StoreProtocol;
    private String StoreProtocolClass;
    private String TransportProtocol;
    private String TransportProtocolClass;
    private String Host;
//    private String LogLevel;
    private String Enabled;
    private NameValuePair[] extParams = new NameValuePair[0];
    
    private String User;
    private String From;
    private String Debug;
    
    transient protected PropertyChangeSupport propertySupport;

    public MailResource(List resources) {
        propertySupport = new PropertyChangeSupport ( this );
        JndiName = "mail/jndiname"; // NOI18N
        Description = ""; // NOI18N
        StoreProtocol = "imap";//NOI18N
        StoreProtocolClass = "com.sun.mail.imap.IMAPStore";//NOI18N
        TransportProtocol = "smtp";//NOI18N
        TransportProtocolClass = "com.sun.mail.smtp.SMTPTransport";//NOI18N
        Host = "Mail Host"; // NOI18N
 //       LogLevel = "ERROR";
        Enabled = "true"; // NOI18N
 //       extParams = new Vector();
        
        User = "User Name"; // NOI18N
        From = "User Email Address"; // NOI18N
        Debug = "false"; // NOI18N
        
        String resName = "MailResource"; // NOI18N
        String t_name = null;
        if(resources.size() != 0){
           int num = resources.size()+1; 
           t_name = resName + "_" + num;//NOI18N
           boolean resource_exists = FactoryName(t_name, resources);
           while(resource_exists){
             num++;
             t_name = resName + "_" + num;//NOI18N
             resource_exists = FactoryName(t_name, resources);
           }
        }else{
          t_name = resName + "_1";//NOI18N
        }
        name = t_name;
    }
    
   public boolean FactoryName(String value, List resources){
      boolean exists = false;  
      for(int i=0; i<resources.size(); i++){
        IMailResource instance = (IMailResource) resources.get(i);
        String inst = instance.getName();
        if(inst.equals(value))
           exists = true;
      }//for
      return exists;
    }    
   
    public String getJndiName() {
        return JndiName;
    }

    public void setJndiName(String value) {
        String oldValue = JndiName;
        this.JndiName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("JndiName", oldValue, JndiName);//NOI18N
        setName(JndiName);
    }
    
    public void simpleSetJndiName(String value) {
        this.JndiName = value;
        setName(JndiName);
    }     
    
    public String getDescription() {
        return Description;
    }

    public void setDescription(String value) {
        String oldValue = Description;
        this.Description = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Description", oldValue, Description);//NOI18N
    }    
    
    public String getStoreProtocol() {
        return StoreProtocol;
    }

    public void setStoreProtocol(String value) {
        String oldValue = StoreProtocol;
        this.StoreProtocol = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("StoreProtocol", oldValue, StoreProtocol);//NOI18N
    }
    
    public String getStoreProtocolClass() {
        return StoreProtocolClass;
    }

    public void setStoreProtocolClass(String value) {
        String oldValue = StoreProtocolClass;
        this.StoreProtocolClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("StoreProtocolClass", oldValue, StoreProtocolClass);//NOI18N
    }
        
    public String getTransportProtocol() {
        return TransportProtocol;
    }

    public void setTransportProtocol(String value) {
        String oldValue = TransportProtocol;
        this.TransportProtocol = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("TransportProtocol", oldValue, TransportProtocol);//NOI18N
    }
    
    public String getTransportProtocolClass() {
        return TransportProtocolClass;
    }

    public void setTransportProtocolClass(String value) {
        String oldValue = TransportProtocolClass;
        this.TransportProtocolClass = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("TransportProtocolClass", oldValue, TransportProtocolClass);//NOI18N
    }
            
    public String getHost() {
        return Host;
    }

    public void setHost(String value) {
        String oldValue = Host;
        this.Host = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Host", oldValue, Host);//NOI18N
    }
 
    public String getEnabled() {
        return Enabled;
    }

    public void setEnabled(String value) {
        String oldValue = Enabled;
        this.Enabled = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Enabled", oldValue, Enabled);//NOI18N
    }
 
    public String getUser() {
        return User;
    }

    public void setUser(String value) {
        String oldValue = User;
        this.User = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("User", oldValue, User);//NOI18N
    }
    
    public String getFrom() {
        return From;
    }

    public void setFrom(String value) {
        String oldValue = From;
        this.From = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("From", oldValue, From);//NOI18N
    }
    
    public String getDebug() {
        return Debug;
    }

    public void setDebug(String value) {
        String oldValue = Debug;
        this.Debug = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Debug", oldValue, Debug);//NOI18N
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
        return name;
    }
    
    public void setName(String value) {
        String oldValue = name;
        this.name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
    }
    
    public NameValuePair[] getExtParams() {
        return extParams;
    }

    public void setExtParams(Object[] value) {
        Reporter.info(new Integer(value.length)); //NOI18N
        NameValuePair[] pairs = new NameValuePair[value.length];
        for (int i = 0; i < value.length; i++) {
            NameValuePair val = (NameValuePair)value[i];
            NameValuePair pair = new NameValuePair();
            pair.setParamName(val.getParamName());
            pair.setParamValue(val.getParamValue());
            pair.setParamDescription(val.getParamDescription());
            Reporter.info(pair.getParamName() + "   " + pair.getParamValue()); //NOI18N
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extParams;
        this.extParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extParams", oldValue, extParams);//NOI18N
    }        
    
    public String toString() {
        return "name: " + getName() + ", Description: " + getDescription() + ", JndiName: " + // NOI18N
          getJndiName()  + ", Store Protocol: " + getStoreProtocol() + // NOI18N
          ", Store Protocol Class: " + getStoreProtocolClass() + // NOI18N
          ", Transport Protocol: " + getTransportProtocol() + // NOI18N
          ", Transport Protocol Class: " + getTransportProtocolClass() + ", Host: " + // NOI18N
          getHost() + ", User: " + getUser() + ", From: " + getFrom() + // NOI18N
          ", Debug: " + getDebug() + ", Enabled: " + getEnabled(); // NOI18N
    }
}
