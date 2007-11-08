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
 * IasResourceAdapterConfigBean.java
 *
 * Created on March 12, 2002, 10:02 AM
 */

package com.sun.enterprise.tools.common.beans;

import java.util.ResourceBundle;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;

import com.sun.enterprise.tools.common.properties.RoleMapElement;
import com.sun.enterprise.tools.common.properties.PropertyElements;

import com.sun.enterprise.tools.common.dd.connector.*;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author  vkraemer
 * @version 
 */
public class IasConnectorOneZero extends BasicIasBean { // implements ConfigBean {

    private static final String PROPNAME_MAXPOOLSIZE = "maxPoolSize";//NOI18N
    private static final String PROPNAME_STEADYPOOLSIZE = "steadyPoolSize";//NOI18N
    private static final String PROPNAME_MAXWAITTIMEINMILLIS= "maxWaitTimeInMilis";//NOI18N
    private static final String PROPNAME_IDLETIMEOUTINSECONDS = "idleTimeoutInSeconds";//NOI18N
    private static final String PROPNAME_JNDINAME = "jndiName"; ///NOI18N
    private static final String PROPNAME_DESCRIPTION = "description";//NOI18N
        
    protected SunConnector fileBean = null;
    protected ResourceAdapter ra = null;
    protected RoleMapElement roleMap = null;
    protected PropertyElements propertyElements = null;
    public static final String DEFAULT_MAP_ID = "1";  //any number will do since there is only one role-map
    
    //private static ResourceBundle bundle =
        //ResourceBundle.getBundle("com.sun.enterprise.tools.common.beans.Bundle"); //NOI18N

    protected IasConnectorOneZero() {
        initListeners();
    }
    
    public IasConnectorOneZero(java.io.InputStream is) {
        try {
            fileBean = SunConnector.createGraph(is);
        }
        catch (Throwable t) {
            // If there is ANY problem with the file on the input stream,
            // we will create a new EMPTY bean.
            fileBean = SunConnector.createGraph();
            ra = new ResourceAdapter();
            ra.setAttributeValue("jndi-name","default"); //NOI18N
            fileBean.setResourceAdapter(ra);
        }
        ra = fileBean.getResourceAdapter();
        RoleMap rm = fileBean.getRoleMap();
        if (null == rm) {
            rm = new RoleMap();
            rm.setAttributeValue("map-id", DEFAULT_MAP_ID);
            // if setRoleMap was called, this is the strategy I would use to allow the user
            // to edit values when the original SunConnector doesn't have a role-map
            // sub-element.
            // BUT, setRoleMap is not called....
            //
            //SunConnector tmp = SunConnector.createGraph();
            //tmp.setRoleMap(rm);
            fileBean.setRoleMap(rm);
        }
        roleMap = new RoleMapElement(rm);
        propertyElements = new PropertyElements(ra);
        initListeners();
    }
    
    private void initListeners() {
        getVCS().addVetoableChangeListener(PROPNAME_MAXPOOLSIZE, greaterThanNegOne);
        getVCS().addVetoableChangeListener(PROPNAME_STEADYPOOLSIZE, greaterThanNegOne);
        getVCS().addVetoableChangeListener(PROPNAME_MAXWAITTIMEINMILLIS, greaterThanNegOne);
        getVCS().addVetoableChangeListener(PROPNAME_IDLETIMEOUTINSECONDS, greaterThanNegOne);
        getVCS().addVetoableChangeListener(PROPNAME_JNDINAME, notNull);
    }
    
        
    public void setDescription(String newDesc) throws PropertyVetoException {
         String elementName = "description"; // NOI18N
         String propName = PROPNAME_DESCRIPTION;
         doElementSetProcessing(ra, newDesc, elementName, propName);
    }
    
    public String getDescription() {
        return ra.getDescription();
    }
    
     public void setJndiName(String newName) throws PropertyVetoException {
         String attrName = "jndi-name"; // NOI18N
         String propName = PROPNAME_JNDINAME;
         doAttrSetProcessing(ra, newName, attrName, propName);
     }
     
    
    public String getJndiName() {
        return ra.getAttributeValue("jndi-name"); //NOI18N
    }
    
    public void setMaxPoolSize(int newVal) throws PropertyVetoException {
        String attrName = "max-pool-size"; // NOI18N
        String propName = PROPNAME_MAXPOOLSIZE;
        doAttrSetProcessing(ra, newVal,attrName, propName);
    }
    
    
    public int getMaxPoolSize() {
        return Integer.parseInt(ra.getAttributeValue("max-pool-size")); //NOI18N
    }
    
    public void setSteadyPoolSize(int newVal) throws PropertyVetoException {
        String attrName = "steady-pool-size"; // NOI18N
        String propName = PROPNAME_STEADYPOOLSIZE;
        doAttrSetProcessing(ra, newVal,attrName, propName);
    }
    
    public int getSteadyPoolSize() {
        return Integer.parseInt(ra.getAttributeValue("steady-pool-size")); //NOI18N
    }
    
    public void setMaxWaitTimeInMillis(int newVal) throws PropertyVetoException {
        String attrName = "max-wait-time-in-millis"; // NOI18N
        String propName = PROPNAME_MAXWAITTIMEINMILLIS;
        doAttrSetProcessing(ra, newVal,attrName, propName);
    }
    
    public int getMaxWaitTimeInMillis() {
        return Integer.parseInt(ra.getAttributeValue("max-wait-time-in-millis")); //NOI18N
    }
    
    public void setIdleTimeoutInSeconds(int newVal) throws PropertyVetoException {
        String attrName = "idle-timeout-in-seconds"; // NOI18N
        String propName = PROPNAME_IDLETIMEOUTINSECONDS;
        doAttrSetProcessing(ra, newVal,attrName, propName);
    }
    
    public int getIdleTimeoutInSeconds() {
        return Integer.parseInt(ra.getAttributeValue("idle-timeout-in-seconds")); //NOI18N
    }
    
    public void setRoleMap(RoleMapElement newVal) throws PropertyVetoException {
        // why doesn't this get called
        //
        RoleMapElement oldVal = roleMap;
        fireMyVetoableChange("roleMap", oldVal, newVal); // NOI18N
        roleMap = newVal;
        fileBean.setRoleMap(newVal.getRoleMap());
        fireMyPropertyChange("roleMap", oldVal, newVal); // NOI18N
    }
    
    public RoleMapElement getRoleMap() {
        return roleMap;
    }
    
    public void setPropertyElements(PropertyElements newVal) throws PropertyVetoException {
        // why doesn't this get called
        //
        //System.out.println("in set property elements: " + newVal); //NOI18N
        PropertyElements oldVal = propertyElements;
        fireMyVetoableChange("propertyElements", oldVal, newVal); // NOI18N
        propertyElements = newVal;
        ra = newVal.getResourceAdapter();
        fileBean.setResourceAdapter(ra);
        fireMyPropertyChange("propertyElements", oldVal, newVal); // NOI18N
    }
    
    public PropertyElements getPropertyElements() {
        //System.out.println("in get property elements: " + propertyElements.hashCode()); //NOI18N
        return propertyElements;
    }
    
   public void outTo(java.io.OutputStream os) throws java.io.IOException {       
       fileBean.write(os);
   }
   
   static public void main (String[] args) {
       try {
       IasConnectorOneZero bean = new IasConnectorOneZero(null);
       bean.outTo(System.out); //NOI18N
       bean.setDescription("test setDescription"); // NOI18N
       bean.setIdleTimeoutInSeconds(1);
       bean.setJndiName("testSetJndiName"); // NOI18N
       bean.setMaxPoolSize(2);
       bean.setMaxWaitTimeInMillis(-3);
       bean.setSteadyPoolSize(4);
       bean.outTo(System.out); //NOI18N
       }
       catch (Throwable t) {
           t.printStackTrace();
       }
   }
   
   public SunConnector getSunConnector(){
       return fileBean;
   }
}
