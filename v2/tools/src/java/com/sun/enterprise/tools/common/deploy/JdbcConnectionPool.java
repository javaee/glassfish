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
 * JdbcConnectionPool.java
 *
 * Created on January 15, 2002, 12:15 PM
 *
 * @author  Shirley Chiang
 */

package com.sun.enterprise.tools.common.deploy;

import java.beans.*;
import java.util.List;
//import java.net.InetAddress;
import java.io.File;
//import com.netscape.server.deployment.PMFDescriptor;
import com.sun.enterprise.tools.common.deploy.IJdbcConnectionPool;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;

public class JdbcConnectionPool extends Object implements java.io.Serializable {
  
        private String name;
        private String id;  
        private String JdbcFactoryClassName;
        private String ResType;
 /*       private String DatabaseURL;
        private String UserName;
        private String Password;  
*/	private String MinConnectionsInPool;
//	private String InitialConnectionsInPool;
	private String MaxConnectionsInPool;
	private String MaxConnectionWaitTimeInMillis;
	private String ConnectionsIncrement;
	private String ConnectionIdleTimeoutInSeconds;
	private String IsConnectionValidationRequired;
	private String ConnectionValidationType;
	private String ValidationTableName;
	private String FailAllConnections;
        private String XIsolationLevel;
        private String IsIsolationLevelGuaranteed;
        private NameValuePair[] extParams;

    transient private PropertyChangeSupport propertySupport;

    public JdbcConnectionPool(List resource) {
        propertySupport = new PropertyChangeSupport ( this );
        
            id = "PoolName";  //NOI18N
            JdbcFactoryClassName = "DatasourceClassName";//NOI18N
            ResType = "javax.sql.DataSource";  //NOI18N
/*          DatabaseURL = "jdbc:";//NOI18N
            UserName = "UserName";//NOI18N
            Password = "Password";//NOI18N          
*/          MinConnectionsInPool = "8"; // NOI18N
//	    InitialConnectionsInPool = "2";
	    MaxConnectionsInPool = "32";  //NOI18N
	    MaxConnectionWaitTimeInMillis = "60000";  //NOI18N
	    ConnectionsIncrement = "2";  //NOI18N
	    ConnectionIdleTimeoutInSeconds = "300";  //NOI18N
	    IsConnectionValidationRequired = "false";  //NOI18N
	    ConnectionValidationType = "auto-commit";  //NOI18N
	    ValidationTableName = "TAB_NAME";  //NOI18N
	    FailAllConnections = "false";	  //NOI18N
            XIsolationLevel = "";   //NOI18N
            IsIsolationLevelGuaranteed = "true";  //NOI18N
            extParams = new NameValuePair[0];       

        //List cp = com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings.DEFAULT.getJdbcConnectionPoolList();
        String t_name = null;
        if(resource.size() != 0){
           int num = resource.size()+1; 
           t_name = "JdbcConnectionPool_" + num;//NOI18N
           boolean exists = FactoryName(t_name, resource);
           while(exists){
             num++;
             t_name = "JdbcConnectionPool_" + num;//NOI18N
             exists = FactoryName(t_name, resource);
           }
        } else {
           t_name = "JdbcConnectionPool_1";//NOI18N
        }
        name = t_name;
        id = t_name;
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
    
    public String getId() {
        return id;
    }

    public void setId(String value) {
        String oldValue = id;
        this.id = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("id", oldValue, id);//NOI18N
        setName(id);
    }    
    
    public void simpleSetId(String value) {
        this.id = value;
        setName(id);
    }         
/*    
    public String PWD() {
        String pw = getPassword();
        String passw = "*";//NOI18N
        for(int i=1; i<pw.length(); i++)
            passw = passw + "*";//NOI18N
        return passw;
    } 
   
    public String getPassword() {
        return Password;
    }
    
    public void setPassword(java.lang.String passwd) {
        String prev = Password;
        this.Password = passwd;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Password", prev, Password);//NOI18N
    }
 */
    public String getJdbcFactoryClassName() {
        return JdbcFactoryClassName;
    }
    public void setJdbcFactoryClassName(java.lang.String facname) {
        String prev = JdbcFactoryClassName;
        this.JdbcFactoryClassName = facname;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("JdbcFactoryClassName", prev, JdbcFactoryClassName);//NOI18N
    }
    
    public String getResType() {
        return ResType;
    }
    public void setResType(java.lang.String val) {
        String prev = this.ResType;
        this.ResType = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ResType", prev, ResType);//NOI18N
    }    
/*    
    public String getDatabaseURL() {
        return DatabaseURL;
    }
    public void setDatabaseURL(java.lang.String dburl) {
        String prev = DatabaseURL;
        this.DatabaseURL = dburl;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("DatabaseURL", prev, DatabaseURL);//NOI18N
    }

    public String getUserName() {
        return UserName;
    }
    public void setUserName(java.lang.String name) {
        String prev = UserName;
        this.UserName = name;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("UserName", prev, UserName);//NOI18N
    }
*/    
    public void setMinConnectionsInPool(String value)
    {
        String oldValue = MinConnectionsInPool;
        this.MinConnectionsInPool = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("MinConnectionsInPool", oldValue, MinConnectionsInPool);//NOI18N
    }

    public String getMinConnectionsInPool()
    {
	return MinConnectionsInPool;
    }
/*
    public void setInitialConnectionsInPool(String value)
    {
        String oldValue = InitialConnectionsInPool;
        this.InitialConnectionsInPool = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("InitialConnectionsInPool", oldValue, InitialConnectionsInPool);//NOI18N        
    }

    public String getInitialConnectionsInPool()
    {
	return InitialConnectionsInPool;
    }
*/
    public void setMaxConnectionsInPool(String value)
    {
        String oldValue = MaxConnectionsInPool;
        this.MaxConnectionsInPool = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("MaxConnectionsInPool", oldValue, MaxConnectionsInPool);//NOI18N         
    }

	//
    public String getMaxConnectionsInPool()
    {
    	return MaxConnectionsInPool;
    }

    public void setMaxConnectionWaitTimeInMillis(String value)
    {
        String oldValue = MaxConnectionWaitTimeInMillis;
        this.MaxConnectionWaitTimeInMillis = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("MaxConnectionWaitTimeInMillis", oldValue, MaxConnectionWaitTimeInMillis);//NOI18N         
    }

    public String getMaxConnectionWaitTimeInMillis()
    {
	return MaxConnectionWaitTimeInMillis;
    }

    public void setConnectionsIncrement(String value)
    {
        String oldValue = ConnectionsIncrement;
        this.ConnectionsIncrement = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ConnectionsIncrement", oldValue, ConnectionsIncrement);//NOI18N                
    }

    public String getConnectionsIncrement()
    {
	return ConnectionsIncrement;
    }

    public void setConnectionIdleTimeoutInSeconds(String value)
    {
        String oldValue = ConnectionIdleTimeoutInSeconds;
        this.ConnectionIdleTimeoutInSeconds = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ConnectionIdleTimeoutInSeconds", oldValue, ConnectionIdleTimeoutInSeconds);//NOI18N        
    }

    public String getConnectionIdleTimeoutInSeconds()
    {
	return ConnectionIdleTimeoutInSeconds;
    }

    public void setIsConnectionValidationRequired(String value)
    {
        String oldValue = IsConnectionValidationRequired;
        this.IsConnectionValidationRequired = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("IsConnectionValidationRequired", oldValue, IsConnectionValidationRequired);//NOI18N                
    }

    public String getIsConnectionValidationRequired()
    {
	return IsConnectionValidationRequired;
    }

    public void setConnectionValidationType(String value)
    {
        String oldValue = ConnectionValidationType;
        this.ConnectionValidationType = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ConnectionValidationType", oldValue, ConnectionValidationType);//NOI18N         
    }

    public String getConnectionValidationType()
    {
	return ConnectionValidationType;
    }

    public void setValidationTableName(String value)
    {
        String oldValue = ValidationTableName;
        this.ValidationTableName = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ValidationTableName", oldValue, ValidationTableName);//NOI18N        
    }

    public String getValidationTableName()
    {
	return ValidationTableName;
    }

    public void setFailAllConnections(String value)
    {
        String oldValue = FailAllConnections;
        this.FailAllConnections = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("FailAllConnections", oldValue, FailAllConnections);//NOI18N        
    }

    public String getFailAllConnections()
    {
	return FailAllConnections;
    }
    
    public String getXIsolationLevel() {
        return XIsolationLevel;
    }
    public void setXIsolationLevel(java.lang.String val) {
        String prev = this.XIsolationLevel;
        this.XIsolationLevel = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("XIsolationLevel", prev, XIsolationLevel);//NOI18N
    }        
    
    public String getIsIsolationLevelGuaranteed() {
        return IsIsolationLevelGuaranteed;
    }
    public void setIsIsolationLevelGuaranteed(java.lang.String val) {
        String prev = this.IsIsolationLevelGuaranteed;
        this.IsIsolationLevelGuaranteed = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("IsIsolationLevelGuaranteed", prev, IsIsolationLevelGuaranteed);//NOI18N
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
     
   public boolean FactoryName(String value, List resources){
      boolean exists = false;  
      //List pm1 = com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings.DEFAULT.getPMFactoryInsts();
      for(int i=0; i<resources.size(); i++){
        IJdbcConnectionPool instance = (IJdbcConnectionPool) resources.get(i);
        String inst = instance.getName();
        if(inst.equals(value))
           exists = true;
      }//for
      return exists;
    }
}
