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

import java.beans.*;
import java.net.InetAddress;
import java.io.File;
import java.util.ResourceBundle;
import java.util.List;
import javax.swing.JOptionPane;
//import com.sun.enterprise.tools.buzz.iascom.IDeployListener;
//import com.sun.enterprise.tools.buzz.iascom.DeployException;
//import com.netscape.server.deployment.DatasourceDescriptor;
//import com.sun.enterprise.tools.common.deployment.DSDescriptor;
//import com.iplanet.ias.tools.forte.datasource.DSBean;
import com.sun.enterprise.tools.common.datasource.IDSBean;
import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
//import com.sun.enterprise.tools.common.deploy.IASDeployer;
//import com.iplanet.ias.tools.forte.util.TempDirManager;
//import org.openide.filesystems.FileSystem;
//import org.openide.filesystems.FileObject;
//import org.openide.filesystems.LocalFileSystem;
//import com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings;

public class DSFields extends Object implements java.io.Serializable {

    static final ResourceBundle bundle = ResourceBundle.getBundle("com.sun.enterprise.tools.common.deploy.Bundle");  //NOI18N
    private static String filename;

    transient private PropertyChangeSupport propertySupport;

    private String name;   
    private String JndiName;
    private String PoolName;
 //   private String SupportXA;
    private String Enabled;
    private String Description;
    private NameValuePair[] extParams;
    
    private String RegPoolName;
    
/*    private String JdbcFactoryClassName;
    private String DatabaseURL;
    private String UserName;
    private String Password;
    private String ResType;
    private ResPool resPoolProperties;
*/    
    /** Creates new ServerInstance */
    public DSFields(List ds) {
        propertySupport = new PropertyChangeSupport ( this );
        
//      JdbcFactoryClassName = "JdbcFactoryClassName";//NOI18N
        PoolName = bundle.getString("Unconfigured");  //NOI18N
        RegPoolName = PoolName;
//        SupportXA = "false";  //NOI18N
        Enabled = "true"; //NOI18N
        Description = "";   //NOI18N
        extParams = new NameValuePair[0];
/*       DatabaseURL = "DatabaseURL";//NOI18N
        UserName = "UserName";//NOI18N
        Password = "Password";//NOI18N
        ResType = "ResourceType";//NOI18N
        resPoolProperties = new ResPool();    
*/   
        //List ds = com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings.DEFAULT.getDataSources();
        String t_name = null;
        if(ds.size() != 0){
           int num = ds.size()+1; 
           t_name = "DataSource_" + num;//NOI18N
           boolean exists = DataSourceName(t_name,ds);
           while(exists){
             num++;
             t_name = "DataSource_" + num;//NOI18N
             exists = DataSourceName(t_name,ds);
           }
        }else{
          t_name = "DataSource_1";//NOI18N
        }
        name = t_name;
        JndiName = "jdbc/";//NOI18N
    }

    public String getJndiName() {
      return JndiName;
    }
    public void setJndiName(java.lang.String jndiName) {
       String prev = JndiName;
       this.JndiName = jndiName;
       initPropertyChangeSupport();
       propertySupport.firePropertyChange ("JndiName", prev, JndiName);//NOI18N
/*       
       if (JndiName.startsWith("jdbc")) // NOI18N
           setName(JndiName.substring(5));
       else 
 */
           setName(JndiName);
    }
      
    public void simpleSetJndiName(String value) {
        this.JndiName = value;
        setName(JndiName);
    }  
    
    public String getPoolName() {
        Reporter.info(PoolName + "  " + RegPoolName);  //NOI18N
       return PoolName;
    }
    
    public void setPoolName(java.lang.String val) {
        String prev = PoolName;
        this.PoolName = val;
        setRegPoolName(PoolName);
        Reporter.info(PoolName + "  " + RegPoolName);  //NOI18N
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("PoolName", prev, PoolName);//NOI18N
    }
    
    public String getRegPoolName(){
        Reporter.info(PoolName + "  " + RegPoolName);  //NOI18N
       return RegPoolName;
    }
    public void setRegPoolName(java.lang.String val){
        String prev = RegPoolName;
        int offset = val.indexOf("(");   //NOI18N
        if (offset == -1)
            this.RegPoolName = val;
        else 
            this.RegPoolName = val.substring(0, offset);
        Reporter.info(PoolName + "  " + RegPoolName);  //NOI18N
    }
    
/*    
    public String getSupportXA() {
       return SupportXA;
    }
    
    public void setSupportXA(java.lang.String val) {
        String prev = SupportXA;
        this.SupportXA = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("SupportXA", prev, SupportXA);//NOI18N
    }
*/   
    public String getEnabled() {
       return Enabled;
    }
    
    public void setEnabled(java.lang.String val) {
        String prev = Enabled;
        this.Enabled = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Enabled", prev, Enabled);//NOI18N
    }
        
    public String getDescription() {
       return Description;
    }
    
    public void setDescription(java.lang.String val) {
        String prev = Description;
        this.Description = val;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("Description", prev, Description);//NOI18N
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
            Reporter.info(pair.getParamName() + "   " + pair.getParamValue());  //NOI18N
            pairs[i] = pair;
        }
        NameValuePair[] oldValue = extParams;
        this.extParams = pairs;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("extParams", oldValue, extParams);//NOI18N
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
        String oldValue = " "; // NOI18N
        name = value;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("name", oldValue, name);//NOI18N
    }
 /*
    public String PWD(){
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

    public String getJdbcFactoryClassName() {
        return JdbcFactoryClassName;
    }
    public void setJdbcFactoryClassName(java.lang.String facname) {
        String prev = JdbcFactoryClassName;
        this.JdbcFactoryClassName = facname;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("JdbcFactoryClassName", prev, JdbcFactoryClassName);//NOI18N
    }

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

    public String getResType() {
        return ResType;
    }
    public void setResType(java.lang.String type) {
        String prev = ResType;
        this.ResType = type;
        initPropertyChangeSupport();
        propertySupport.firePropertyChange ("ResType", prev, ResType);//NOI18N
    }
    
    public ResPool getResPoolProperties() {
        return resPoolProperties;
    }
    public void setResPoolProperties(ResPool resPool) {
//        String prev = ResType;
        this.resPoolProperties = resPool;
//       initPropertyChangeSupport();
//        propertySupport.firePropertyChange ("ResType", prev, ResType);//NOI18N
    }
*/    
    /*
    public static DSDescriptor getDSDesc(DSBean bn){
       
        DSDescriptor newdesc = new DSDescriptor();
        newdesc.setDatabase(bn.getDatabase());
        newdesc.setDatabase_url(bn.getDatabaseURL());
        //newdesc.setResource_mgr(bn.getDatabaseURL());
        newdesc.setDatasource(bn.getDataSource());
        newdesc.setUsername(bn.getUserName());
        newdesc.setPassword(bn.getPassword());
        newdesc.setJndi_name("jdbc/"+newdesc.getDatasource());//NOI18N
        try {
            newdesc.setDriver_type(bn.getDriver());
        }
        catch (Exception ex) {
            // this should not happen. If it does, we still need to send something back up
            // the stack.
            throw new RuntimeException("Driver_type is incorrect.  Should not happen.");//NOI18N
        }
        return newdesc;
    }
    
    public static void filename(DSBean bn){
        TempDirManager tmp = new TempDirManager();
        String pathname = tmp.getDirName();
        //System.out.println("pathname" + pathname); //NOI18N
        //File test = new File("c:\\temp\\");
        //String pathname = test.getAbsolutePath();
        String ds_name = bn.getDataSource() + ".xml";//NOI18N
        filename = pathname + java.io.File.separator + ds_name;    
    }
    */
    private boolean DataSourceName(String value, List pm1){
      boolean exists = false;  
      //IasGlobalOptionsSettings val1 = new IasGlobalOptionsSettings();
      //List pm1 = com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings.DEFAULT.getDataSources();
      //List pm1 = val1.getDataSources();
      for(int i=0; i<pm1.size(); i++){
        //IDSBean instance = val1.getDSInstance(i);
        //IDSBean instance = com.iplanet.ias.tools.forte.globalsettings.IasGlobalOptionsSettings.DEFAULT.getDSInstance(i);
          IDSBean instance = (IDSBean) pm1.get(i);
        String inst = instance.getName();
        if(inst.equals(value))
           exists = true;
      }//for
      return exists;
    }
    
}

