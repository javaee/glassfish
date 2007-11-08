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
 * PersistenceManager.java
 *
 * Created on January 15, 2002, 12:15 PM
 *
 * Author: Shirley Chiang
 */

package com.sun.enterprise.tools.common.deploy;

import java.beans.*;
import java.util.List;
import java.net.InetAddress;
import java.io.File;
import java.util.Vector;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

//import com.netscape.server.deployment.PMFDescriptor;
import com.sun.enterprise.tools.common.deploy.IPMFactory;

public class PersistenceManager extends Object implements java.io.Serializable {
  
    private String name;
    private String Description;
    private String JndiName;  
//    private String ResType;
    private String FactoryClassName;
//    private String LogLevel;
    private String Enabled;
    private NameValuePair[] extParams = new NameValuePair[0];
    
    private String DataSourceName;
/*
    private String IgnoreCache;
    private String NonTXRead;
    private String NonTXWrite;
    private String Optimistic;
    private String RetainValue;
*/    
    transient protected PropertyChangeSupport propertySupport;

    public PersistenceManager(List resources) {
        propertySupport = new PropertyChangeSupport ( this );
        //JndiName = "pmf/";//NOI18N
        JndiName = "jdo/";//NOI18N
        Description = ""; // NOI18N
 //       ResType = "javax.sql.DataSource";//NOI18N
        FactoryClassName = "com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl";//NOI18N
 //       LogLevel = "ERROR";
        Enabled = "true"; // NOI18N
 //       extParams = new Vector();
        
        DataSourceName = "jdbc/";//NOI18N
/*        
        IgnoreCache = "false";//NOI18N
        NonTXRead = "false";//NOI18N
        NonTXWrite = "false";//NOI18N
        Optimistic = "false";//NOI18N
        RetainValue = "false";//NOI18N
*/        
        String resName = "PersistenceManager"; // NOI18N
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
        IPMFactory instance = (IPMFactory) resources.get(i);
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
/*    
    public String getResType() {
        return ResType;
    }

    public void setResType(String value) {
        String oldValue = ResType;
        this.ResType = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ResType", oldValue, ResType);//NOI18N
    }
*/    
    public String getFactoryClassName() {
        return FactoryClassName;
    }

    public void setFactoryClassName(String value) {
        String oldValue = FactoryClassName;
        this.FactoryClassName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("FactoryClassName", oldValue, FactoryClassName);//NOI18N
    }
 
/*
    public String getLogLevel() {
        return LogLevel;
    }

    public void setLogLevel(String value) {
        String oldValue = LogLevel;
        this.LogLevel = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("LogLevel", oldValue, LogLevel);//NOI18N
    }
*/    
    
    public String getEnabled() {
        return Enabled;
    }

    public void setEnabled(String value) {
        String oldValue = Enabled;
        this.Enabled = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Enabled", oldValue, Enabled);//NOI18N
    }
 
 /*   
    public Vector getExtParams() {
        return extParams;
    }

    public void setExtParams(Vector value) {
        Vector oldValue = extParams;
        this.extParams = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extParams", oldValue, extParams);//NOI18N
    }
 */  
    
    public String getDataSourceName() {
        return DataSourceName;
    }

    public void setDataSourceName(String value) {
        String oldValue = DataSourceName;
        this.DataSourceName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("DataSourceName", oldValue, DataSourceName);//NOI18N
    }
/*    
    public String getIgnoreCache() {
        return IgnoreCache;
    }

    public void setIgnoreCache(String value) {
        String oldValue = IgnoreCache;
        this.IgnoreCache = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("IgnoreCache", oldValue, IgnoreCache);//NOI18N
    }
    
    public String getNonTXRead() {
        return NonTXRead;
    }

    public void setNonTXRead(String value) {
        String oldValue = NonTXRead;
        this.NonTXRead = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("NonTXRead", oldValue, NonTXRead);//NOI18N
    }
    
    public String getNonTXWrite() {
        return NonTXWrite;
    }

    public void setNonTXWrite(String value) {
        String oldValue = NonTXWrite;
        this.NonTXWrite = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("NonTXWrite", oldValue, NonTXWrite);//NOI18N
    }
    
    public String getOptimistic() {
        return Optimistic;
    }

    public void setOptimistic(String value) {
        String oldValue = Optimistic;
        this.Optimistic = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Optimistic", oldValue, Optimistic);//NOI18N
    }
    
    public String getRetainValue() {
        return RetainValue;
    }

    public void setRetainValue(String value) {
        String oldValue = RetainValue;
        this.RetainValue = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("RetainValue", oldValue, RetainValue);//NOI18N
    }
*/   
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
            Reporter.info(pair.getParamName() + "   " + pair.getParamValue() + "  " + pair.getParamDescription()); //NOI18N
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extParams;
        this.extParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extParams", oldValue, extParams);//NOI18N
    }            
    public String toString() {
        return "name: " + getName() + ", Description: " + getDescription() + ", JndiName: " + // NOI18N
          getJndiName()  + ", FactoryClassName: " + // NOI18N
          getFactoryClassName() + ", Enabled: " + getEnabled(); // NOI18N
    }
}
