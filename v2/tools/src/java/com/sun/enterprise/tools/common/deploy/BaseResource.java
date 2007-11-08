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
 * BaseResource.java
 *
 * Created on January 12, 2002, 6:35 PM
 */
 
package com.sun.enterprise.tools.common.deploy;

import java.beans.*;
import java.util.List;

public class BaseResource implements java.io.Serializable {
  
    private String name;
    private String Description;
    protected String JndiName;  
    private String ResType;
/*    private String FactoryClassName;
    private String LogLevel;
*/    private String Enabled;
    
    transient protected PropertyChangeSupport propertySupport;

    public BaseResource(List resources, String resName) {
        propertySupport = new PropertyChangeSupport ( this );
        JndiName = "JndiName"; // NOI18N
        Description = ""; // NOI18N
        ResType = "ResourceType";//NOI18N
//        FactoryClassName = "FactoryClassName";
//        LogLevel = "ERROR";
        Enabled = "true"; // NOI18N
        
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
        IResource instance = (IResource) resources.get(i);
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
    
    public String getResType() {
        return ResType;
    }

    public void setResType(String value) {
        String oldValue = ResType;
        this.ResType = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ResType", oldValue, ResType);//NOI18N
    }
/*    
    public String getFactoryClassName() {
        return FactoryClassName;
    }

    public void setFactoryClassName(String value) {
        String oldValue = FactoryClassName;
        this.FactoryClassName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("FactoryClassName", oldValue, FactoryClassName);//NOI18N
    }

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
    
    protected void initPropertyChangeSupport(){
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
}
