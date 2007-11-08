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

package com.sun.enterprise.config.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigChangeFactory;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.ConfigSet;
import com.sun.enterprise.config.ConfigUpdate;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigRuntimeException;
import com.sun.enterprise.config.StaleWriteConfigException;

import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;
import com.sun.enterprise.config.pluggable.ConfigEnvironment;
import com.sun.enterprise.config.pluggable.EnvironmentFactory;
import com.sun.enterprise.config.ConfigContextEventListener;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigBeansFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;

import org.netbeans.modules.schema2beans.BaseBean;

import java.util.logging.Logger;
import java.util.logging.Level;
//import com.sun.logging.LogDomains;

/**
 * A configuration context represents a heirarchical view of the configuration
 * attributes. It reads the configuration attributes from <i> domain.xml </i>
 * The configuration context has a one-to-one realationship with
 * a configuration file. 
 *
 * There is no public constructor. Use ConfigFactory to construct ConfigContext. 
 * Once ConfigContext is created, then, it is tied to the configuration file forever.
 *
 * This object can be make readOnly and autoCommit during creation but these parameters
 * cannot be changed once the object is created. Note that this object might be shared
 * by number of threads.
 *
 * Here are some examples of code to illustrate lookup:
 * 1. If you want to specifically locate a Node in the configuration: 
 *    example:  jms resource in server.xml
 *  <PRE>
 *   ConfigBean conBean = 
 *        ctx.exactLookup("/server/resources/jms-resource[@name='jms1']");
 *  </PRE>
 *
 * Follow the syntax below for creating xpath expression for use in lookup/exactLookup. This is the only
 *    syntax supported.
 *  <PRE>
 *      expression := /tagName | /tagName/tagExpression
 *      tagExpression := tagName| tagName[@name='value'] | tagName/tagExpression | tagName[@name='value']/tagExpression
 *  </PRE>
 *
 */
public class ConfigContextImpl implements 
                                ConfigContext,
                                DefaultConstants,
                                Serializable, 
                                Cloneable {
    
        //For storing ChangeEvent Listeners
        transient protected Vector listeners = null;
        
        private ConfigBean server;
        protected String xmlUrl;
        
        //will be used to keep track of manual changes
        transient private long lastModified = UNINITIALIZED_LAST_MODIFIED;
        
        // FIXME: also set xml doc readonly.
        private boolean readOnly = false; // FIXME: implement this
        private boolean autoCommit = false; //FIXME: implement this
        private Class rootClass;
        private boolean isAdministered = false; // Handle properly. FIXME TODO
        private DefaultHandler defaultHandler = null;
        private boolean resolvePath = true;
       
        private transient ConfigBeanInterceptor configBeanInterceptor = 
            EnvironmentFactory.getEnvironmentFactory().
                getConfigEnvironment().getConfigBeanInterceptor();

        /** 
         * used for checking lastModifiedTimestamps in configBeans 
         * Note that it is transient. we do not want the clone
         * to have it enabled by default
         */
        transient private boolean _lastModifiedCheck = false;
        
        transient private ArrayList configChangeList = new ArrayList();
        
        public ArrayList getConfigChangeList() {
            return configChangeList;
        }
        
        public boolean isAdministered() {
            return isAdministered;
        }
        
        public void setIsAdministered(boolean value) {
            isAdministered = value;
        }
        
        // also called by refresh()
        public synchronized void resetConfigChangeList() {
            configChangeList = new ArrayList();
            clearPersistentConfigChanges();
            
            //FIXME
            isAdministered = true;
        }
           
        public synchronized ConfigChange addToConfigChangeList(String xpath, String attrName, String oldValue, String newValue) {
            //find xpath and add. else create new obj.
           if(!isAdministered) return null;
            
            ConfigChange cChange = null;
            
            try {
                if(oldValue == null && newValue == null) return null;
                if(oldValue != null && oldValue.equals(newValue)) return null;
                
                boolean found = false;
                for(int i=0;i<configChangeList.size();i++) {
                    if(((ConfigChange)configChangeList.get(i)).getConfigChangeType().equals(ConfigChange.TYPE_UPDATE) 
                                    && ((ConfigUpdate)configChangeList.get(i)).getXPath().equals(xpath)) {
                        ((ConfigUpdate)configChangeList.get(i)).addChangedAttribute(attrName, oldValue, newValue);
						//_logger.log(Level.INFO,"config.change_added");
                        persistConfigChanges();
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    cChange = ConfigChangeFactory.createConfigUpdate
                                        (xpath, 
                                        attrName, 
                                        oldValue, 
                                        newValue);
                    configChangeList.add(cChange);
                    persistConfigChanges();
					//_logger.log(Level.INFO,"config.create_config_ch_obj",xpath);
                }
            } catch (Throwable t) {
				//_logger.log(Level.WARNING,"config.add_config_change_exception",t);
            }
           return cChange;
        }
        
       /**
         * Package private constructor. Not to be called by clients.
         * 
         * @param xmlUrl
         * @param readOnly
        * @deprecated
         */	
        public ConfigContextImpl(String xmlUrl, boolean readOnly, boolean autoCommit, Class rootClass, DefaultHandler dh) // throws ConfigException
	{
            this.xmlUrl = xmlUrl;
            this.readOnly = readOnly; 
            //FIXME: use dom readonly
           /* if(readOnly) {
                ((ElementNode)this.serverInstanceDocument.getDocumentElement()).setReadOnly(true);
            }
            */
            this.autoCommit = autoCommit;
            this.rootClass = rootClass;
            this.defaultHandler = dh;            
	}
        
        /**
         * Package private constructor. Not to be called by clients.
         * 
         * @param xmlUrl
         * @param readOnly
         * @param resolvePath
         *
         * @deprecated
         */	
        public ConfigContextImpl(String xmlUrl, boolean readOnly, boolean autoCommit, 
                Class rootClass, DefaultHandler dh, boolean resolvePath) // throws ConfigException
	{
            this.xmlUrl = xmlUrl;
            this.readOnly = readOnly; 
            //FIXME: use dom readonly
           /* if(readOnly) {
                ((ElementNode)this.serverInstanceDocument.getDocumentElement()).setReadOnly(true);
            }
            */
            this.autoCommit = autoCommit;
            this.rootClass = rootClass;
            this.defaultHandler = dh;  
            this.resolvePath = resolvePath;
	}
        
        public ConfigContextImpl(ConfigEnvironment ce) {
            try {
            this.xmlUrl = ce.getUrl();
            this.readOnly = ce.isReadOnly(); 
            //FIXME: use dom readonly
           /* if(readOnly) {
                ((ElementNode)this.serverInstanceDocument.getDocumentElement()).setReadOnly(true);
            }
            */
            this.autoCommit = ce.isAutoCommitOn();
            this.rootClass = Class.forName(ce.getRootClass());
            this.defaultHandler = 
                (DefaultHandler) Class.forName(ce.getHandler()).newInstance();  
            this.configBeanInterceptor = ce.getConfigBeanInterceptor();
            } catch (Exception e) {
                throw new ConfigRuntimeException("err_creating_ctx", e);
            }
            
        }
        public synchronized ConfigBean getRootConfigBean() throws ConfigException {
            if(server == null) {
                refresh();
            }
            return server;
        }
                
        /**
         * @deprecated use getUrl()
         */
        String getXmlUrl() {
            return this.xmlUrl;
        }
        
        /**
         * Retrieves the named object. The parameter passed is a <a href = http://www.w3.org/TR/xpath>
         * XPath version 1.0</a>
         * used as the path notation for navigating the hierarchical structure. Only the AbsoluteLocationPath
         * and Relative LocationPath elements are used.
         * The code below returns a ConfigBeam representing an application with appId="app1"
         * <PRE>
         * ConfigBean cn = conCtx.exactLookup(ServerXPathHelper.getAppIdXpathExpression("app1"));
         * </PRE>
         * @return a ConfigBean representing a node in the tree.
         * 
         * @param xpath
         * @throws ConfigException
         */
	public ConfigBean exactLookup(String xpath) throws ConfigException
	{
            if(server == null) {
                refresh();
            }
            return ConfigBeansFactory.getConfigBeanByXPath(this,xpath); 
	}
        
        /*
         * Not Yet Implemented
         */
        public ConfigBean[] lookup(String xpath) throws ConfigException
        {
            // FIXME NYI
            return null;
        }
        
        private void assertReadOnly() throws ConfigException {
            if(readOnly) {
                throw new ConfigException("Read only: you cannot write " + toString() );
            }
        }

	/**
         * saves all the changes in ConfigContext to storage
         * Throws ConfigException if there is any problem writing to storage.
         * or the file has been modified after last read. 
         * However, if overwrite is true, then it overwrites any manual changes
         * on disk
         */
        public synchronized void flush(boolean overwrite) throws ConfigException {
            if(!overwrite && isFileChangedExternally()) {
                throw new StaleWriteConfigException
                    ("ConfigContext Flush failed: File Changed Externally");
            }
            
            // <addition> srini@sun.com server.xml verifier
            //preChange(ConfigContextEvent.PRE_FLUSH_CHANGE);
            ConfigContextEvent ccce = new ConfigContextEvent(this,ConfigContextEvent.PRE_FLUSH_CHANGE);
            preChange(ccce);
            // </addition> server.xml verifier
         
            try {
                FileOutputStream fos = new FileOutputStream(this.xmlUrl);
                server.write(fos);
            } catch(Exception e) {
                throw new ConfigException("Error Flushing ConfigContext " + toString());
            }
            initLastModified();
            // <addition> srini@sun.com
            //postChange(ConfigContextEvent.POST_FLUSH_CHANGE);
            ccce = new ConfigContextEvent(this,ConfigContextEvent.POST_FLUSH_CHANGE);
            postChange(ccce);
            // </addition> server.xml verifier
            
            //FIXME: should I not write out persistentConfigChanges now??
        }
        
	/**
         * is equivalent to flush(true)
         */
        public void flush() throws ConfigException {
            flush(true);
        }
        

        /** discards all the changes and reloads the xml from storage based on the force flag
         *  @param force  forces a reload from disk even if dirty bit is not set
         */        
        public synchronized void refresh(boolean force) throws ConfigException {
            try {
                if(!force && isChanged()) {
                    throw new ConfigException
                        ("ConfigContext has changed in Memory. cannot refresh " + toString());
                }
                
                FileInputStream in = new FileInputStream(this.xmlUrl);
                if(this.defaultHandler != null) {
                    server = (ConfigBean) BaseBean.createGraph(
                                rootClass, 
                                in, 
                                true, 
                                this.defaultHandler, 
                                this.defaultHandler);
                } else {
                    server = (ConfigBean) BaseBean.createGraph(rootClass, in, true);
                }
                 
                initConfigChanges(); // not reset. load it up.
                
                initLastModified();
                
                //setXpath for the tree of readwrite mode
                if(INITIALIZE_XPATH) {
                    if(!this.readOnly) {
                        setXPathInAllBeans();
                    }
                }
                setIsAdministered(true);
                server.setInterceptor(this.configBeanInterceptor);
            } catch (Exception e) {
                throw new ConfigException("Error refreshing ConfigContext:" + this.xmlUrl, e);
            }
        }
        
        /**
         * sets xpath in all beans
         */
        public void setXPathInAllBeans() throws ConfigException {
            if(this.readOnly) return;
            
            ConfigBean cb = this.getRootConfigBean();
            //set ctx and xpath
            //_logger.fine("Setting xpath for the whole tree");
            setXPathInTree(cb,"");
            //_logger.fine("Finished setting xpath for the whole tree");
        }
        
        /**
         * recursive method
         */
        private void setXPathInTree(ConfigBean cb, String parentXpath) {
            cb.setConfigContext(this);
            String xp = cb.getAbsoluteXPath(parentXpath);
            //_logger.finer("   " + cb.name() + " " + xp);
            cb.setXPath(xp);
            
            //if cb is leaf. return.
            ConfigBean[] chChildArr = cb.getAllChildBeans();
            if(chChildArr == null || chChildArr.length <= 0) return;
            for(int i = 0; i<chChildArr.length; i++) {
                if(chChildArr[i] == null) continue;
                //_logger.finest("Child " + chChildArr[i].name());
                try {
                    setXPathInTree(chChildArr[i], cb.getXPath());
                } catch(Throwable t) {
                    //ignore this node and its children and continue with rest
                    //_logger.fine("Error: Cannot setXpath for " + chChildArr[i].name());
                    //_logger.fine("Error: Xpath is " + cb.getXPath());
                    //_logger.fine("Error: Exception is: " + t.getMessage());
                    //_logger.fine("Error: Continuing with rest of settings");
                }
                
            }
        }
      
       /**
        * recursive method that cleansup the config context and allows better memory model
        * This should be called when we know that the tree in this context is used in future 
        */
       public void cleanup() {
            //set ctx and xpath
            //_logger.fine("Cleaning up config context");
	    if(server != null)
                cleanup(server);
            //_logger.fine("Finished setting xpath for the whole tree");
	    server = null;
      }

      private void cleanup(ConfigBean cb) {
            cb.cleanup();
            //_logger.finer("   " + cb.name() + " cleaned up");
            
            //if cb is leaf. return.
            ConfigBean[] chChildArr = cb.getAllChildBeans();
            if(chChildArr == null || chChildArr.length <= 0) return;
            for(int i = 0; i<chChildArr.length; i++) {
                if(chChildArr[i] == null) continue;
                //_logger.finest("Child " + chChildArr[i].name());
                try {
                    cleanup(chChildArr[i]);
                } catch(Throwable t) {
                    //ignore this node and its children and continue with rest
                    //_logger.fine("Warning: Cannot cleanup configbean " + chChildArr[i].name());
                    //_logger.fine("Warning: Exception is: " + t.getMessage());
                    //_logger.fine("Warning: Continuing with rest of settings");
                }
            }    
       } 

        /** discards all the changes and reloads the xml from storage if dirty flag is true
         */        
        public void refresh() throws ConfigException {
           refresh(true);
        }
        
        public Object clone() {
            ConfigContextImpl ctxClone = 
                new ConfigContextImpl(this.xmlUrl, false, false, this.rootClass, this.defaultHandler);
			ctxClone.configBeanInterceptor = getConfigBeanInterceptor();
			/*
				server is initialized on refresh(). What if someone calls clone()
				before server is initialized.
			 */
			if (server != null) {
				ConfigBean rootClone = (ConfigBean)this.server.clone();
				ctxClone.setRootConfigBean(rootClone);
				rootClone.setConfigContext(ctxClone);
				rootClone.setInterceptor(ctxClone.configBeanInterceptor);
				try {
			            ((ConfigContextImpl)ctxClone).setXPathInAllBeans();
				} catch (Exception e) {
					//ignore
				}
			}
            ctxClone.setIsAdministered(true);
            
            return ctxClone;
        }
        
        void setRootConfigBean(ConfigBean c) {
            this.server = c;
        }
               
        /**
         * for add
         */
        public synchronized ConfigChange addToConfigChangeList(
                    String parentXpath, 
                    String childXpath, 
                    String name, 
                    ConfigBean cb) {
            //adds
            if(!isAdministered) return null;
            
            ConfigChange cChange = ConfigChangeFactory.createConfigAdd
                                        (parentXpath, childXpath, name, cb);
            
            configChangeList.add(cChange);
            persistConfigChanges();
            return cChange;
        }
        
        public synchronized ConfigChange addToConfigChangeList(String xpath) {
            if(!isAdministered) return null;
            
            ConfigChange cChange = null;
            if(!removeOtherConfigChanges(xpath)) {
                cChange = ConfigChangeFactory.createConfigDelete(xpath);
                configChangeList.add(cChange);
            }

            persistConfigChanges();
            return cChange;
        }

        /*
         * returns true if there is config add
         * so , we do not need to add config delete
         */ 
        private boolean removeOtherConfigChanges(String xpath) {
	    boolean ret = false;
            try {
                //Bug# 4884726 Begin
                final Iterator it = configChangeList.iterator();
                while (it.hasNext()) {
                    final ConfigChange cc = (ConfigChange)it.next();
                    if(xpath.indexOf(cc.getXPath()) >= 0) {
		        if(cc instanceof ConfigAdd) 
			    ret = true;
                        it.remove();
                    }
                }
                //Bug# 4884726 End
            } catch (Throwable t) {
		//_logger.log(Level.WARNING,"config.remove_config_change_exception",t);
            }
            return ret;	
        }
        
        public synchronized ConfigChange addToConfigChangeList(String parentXpath, String name, Object cb, Object[] cbArray) {
            if(!isAdministered) return null;
            
            ConfigChange cChange = ConfigChangeFactory.createConfigSet(
                                parentXpath, 
                                name, 
                                cb, 
                                cbArray);
            configChangeList.add(cChange);
            persistConfigChanges();
            return cChange;
        }

        public synchronized void removeConfigChange(ConfigChange change) {
            if (!isAdministered) return;

            int ndx = configChangeList.indexOf(change);
            if (ndx != -1) {
                configChangeList.remove(ndx);
            }
            persistConfigChanges();
        }

        public synchronized void updateFromConfigChange(ConfigChange configChange)  throws ConfigException {
            if(configChange == null) return;
            
            //disable configChangeList while updating.
            //otherwise, we get unnecessary changes in the list.
            boolean tmpAdministered = isAdministered;
            isAdministered = false;
            
            if(configChange.getConfigChangeType().equals(ConfigChange.TYPE_ADD))
                updateFromConfigAdd((ConfigAdd)configChange);
            else if(configChange.getConfigChangeType().equals(ConfigChange.TYPE_UPDATE))
                updateFromConfigUpdate((ConfigUpdate)configChange);
            else if(configChange.getConfigChangeType().equals(ConfigChange.TYPE_DELETE))
                updateFromConfigDelete((ConfigDelete)configChange);
            else if(configChange.getConfigChangeType().equals(ConfigChange.TYPE_SET))
                updateFromConfigSet((ConfigSet)configChange);
            
            //enable if needed.
            isAdministered = tmpAdministered;
        }
        
        private void updateFromConfigAdd(ConfigAdd configChange)  throws ConfigException {
            ConfigBean parent = ConfigBeansFactory.getConfigBeanByXPath(this,configChange.getParentXPath());
            if(parent == null) 
                throw new ConfigException("updateFromConfigAdd: Cannot find parent");
            
            ConfigBean rootBean = configChange.getConfigBean();
            if(rootBean == null)
                throw new ConfigException("updateFromConfigAdd: Cannot find root bean");
            
            ConfigBean childBean = ConfigBeansFactory.getConfigBeanByXPath(rootBean, configChange.getXPath());
            
            if(childBean == null) 
                throw new ConfigException("updateFromConfigAdd: Cannot find childBean");
            
            //lastModified Check
            if(isLastModifiedCheckEnabled()) {
                validateLastModified(parent, configChange);
            }
            
            childBean = (ConfigBean)childBean.clone();
            //System.out.println("++++++++++before add +++++++++++++++");
            //System.out.println("this childBean:");
            //System.out.println(childBean.dumpBeanNode());
            //System.out.println("++++++++++++++++++++++++++++++++");
            //System.out.println(childBean.dumpDomNode());
            //System.out.println("++++++++++before add +++++++++++++++");
            
            parent.addValue(configChange.getName(), childBean);
            //System.out.println("++++++++++after add +++++++++++++++");
            //System.out.println("this configcontxt:");
            //this.getRootConfigBean().dumpXml();
            //System.out.println("++++++++++after add +++++++++++++++");
        }

        private void updateFromConfigSet(ConfigSet configChange)  throws ConfigException {
            ConfigBean parent = ConfigBeansFactory.getConfigBeanByXPath(this,configChange.getParentXPath());
            
            if(parent == null) 
                throw new ConfigException("updateFromConfigSet: Cannot update. Could not get parent");
            Object child = configChange.getConfigBean();
            
            //lastModified Check
            if(isLastModifiedCheckEnabled()) {
                validateLastModified(parent, configChange);
            }
            
            if(child != null) {
                parent.setValue(configChange.getName(), child);
            } else { //it is an array
                Object[] childArray = configChange.getConfigBeanArray();
                parent.setValue(configChange.getName(), childArray);
            }
        }

        private void updateFromConfigUpdate(ConfigUpdate configChange) throws ConfigException  {
            //set all the attributes
            //System.out.println("configchange is:" + configChange);
            //System.out.println("++++++++++++++++++++++++++++");
            //System.out.println("this configcontext:" + this);
            //System.out.println("++++++++++++++++++++++++++++");
            ConfigBean b = ConfigBeansFactory.getConfigBeanByXPath(this,configChange.getXPath());
  	    //_logger.log(Level.FINE,"xpath="+ configChange.getXPath());
            if(b == null) 
                throw new ConfigException("updateFromConfigUpdate:Could not find ConfigBean to update");
            
            //lastModified Check
            if(isLastModifiedCheckEnabled()) {
                validateLastModified(b, configChange);
            }
            
            Set s = configChange.getAttributeSet();
            for (Iterator i = s.iterator();i.hasNext();) {   
                String name = (String) i.next();
                b.setAttributeValue(name, configChange.getNewValue(name));
            }
        }
        
        private void updateFromConfigDelete(ConfigDelete configChange) throws ConfigException {
            //delete
            String xpath = configChange.getXPath();
            ConfigBean child = ConfigBeansFactory.getConfigBeanByXPath(this,xpath);
            if(child != null) {
                //throw new ConfigException("Child not found:" + xpath);
            ConfigBean parent = (ConfigBean) child.parent();
            
            //lastModified Check
            if(isLastModifiedCheckEnabled()) {
                validateLastModified(parent, configChange);
            }
            
            parent.removeChild(child);
            }
        }
        
        /**
         * get all the important attributes of this configcontext as a String
         * Each attribute is separated by a comma
         * @return String representation of this configContext
         */
        public String toString() {
            return "com.sun.enterprise.config.ConfigContext: Url=" + this.xmlUrl 
                        + ", ReadOnly=" + this.readOnly 
                        + ", ResolvePath=" + (resolvePath)
                        + ", LastModified Timestamp=" + this.lastModified
                        + ", isChanged=" + this.isChanged()
                        + ", Autocommit=" + this.autoCommit
                        + ", isConfigBeanNull=" + (server==null);
        }
        
        /** get the ConfigBean information as a String
         * String is in a schema2beans proprietory format
         * Note that this method is not exposed in ConfigContext.
         * @return String representation of config bean. return "null bean" if
         *         there is no associated config bean
         */
        public String configBeanToString() {
            if(server == null) return "null bean";
            return this.server.dumpBeanNode();
        }
        
        
         /**
         * Get the Value of an attribute with One call 
         * This equivalent of doing an exactLookup to get a bean 
         * and then a getAttributeValue on that bean
         * 
         */
        public String getAttributeValue(String xpath, String attributeName) {
            ConfigBean c = null;
            try {
                c = this.exactLookup(xpath);
            } catch(Exception e) {}
            
            if(c!= null) {
                if(!isResolvingPaths()) {
                    return c.getRawAttributeValue(attributeName);
                } else {
                    return c.getAttributeValue(attributeName);
                }
            }
            
            return null;
        }
        
        /**
         * similar to getAttributeValue but returns a boolean
         */
        public boolean getBooleanAttributeValue(String xpath, String attributeName) {
            String ret = getAttributeValue(xpath, attributeName);
            if(ret == null) return false;
            
            if (ret.equals("true"))
                return true;

            return false;
        }
        
        public boolean isChanged() {
            if(configChangeList == null || configChangeList.size() ==0)
                return false;
            return true;
        }
        
        /**
         *
         */
        private long getLastModified() {
            long ts = INVALID_LAST_MODIFIED;
            try {
                File f = new File(this.xmlUrl);
                ts = f.lastModified();
            } catch(Exception e) {
                //ignore
            }
            return ts;
        }
        
        /**
         * checks if file has changed externally after the config context 
         * last updated the file
         */
        public boolean isFileChangedExternally() {            
            if(getLastModified() == this.lastModified)
                return false;
            return true;
        }
                
        /**
         * init to be called from constructor
         */
        private void initLastModified() {
            this.lastModified = getLastModified();
        }
        
        public boolean equals(Object obj) {
            try {
                if(this.getRootConfigBean().equals(((ConfigContext)obj).getRootConfigBean())) {
                    return true;
                }
            } catch(Throwable t) {
                //ignore
            }
            return false;
        }
        
        public int hashCode() {
            try {
                return this.getRootConfigBean().hashCode();
            } catch(Throwable t) {
                //ignore
            }
           return super.hashCode();
        }
        
               
        private String getConfigChangeUrl() {
            return this.xmlUrl + ".changes";
        }
        
        private void persistConfigChanges() {
            
            //We donot need to write if persistent
            //config changes are not required
            if(!LOAD_PERSISTENT_CONFIG_CHANGES) return;
            
            FileOutputStream ostream = null;
            ObjectOutputStream p = null;
            try {
                ostream = new FileOutputStream(getConfigChangeUrl());
                p = new ObjectOutputStream(ostream);
                p.writeObject(this.configChangeList);
                p.flush();
            } catch(Throwable t) {
                //ignore.
            } finally {
                try {
                    p.close();
                    ostream.close();
                } catch(Exception e){}
            }
            
        }
        
        private void initConfigChanges() {
            if(!LOAD_PERSISTENT_CONFIG_CHANGES) return;
            
            FileInputStream istream = null;
            try {
                istream = new FileInputStream(getConfigChangeUrl());
                ObjectInputStream p = new ObjectInputStream(istream);

                this.configChangeList = (ArrayList)p.readObject();

            } catch(Throwable t) {
                //ignore.
            } finally {
                try {
                    istream.close();
                } catch(Exception e){}
            }
            
        }
        
        private void clearPersistentConfigChanges() {
             if(!LOAD_PERSISTENT_CONFIG_CHANGES) return;
             
            //remove change file on disk
            try {
                File f = new File(getConfigChangeUrl());
                f.delete();
            } catch(Throwable t) {
                //ignore.
            }
        }
        
               /*
         * Add Notification Listener
         */
        public void addConfigContextEventListener(ConfigContextEventListener ccel) {
            if (listeners==null)
                listeners = new Vector();
            listeners.addElement(ccel);
        }
        
        /*
         * remove
         */
        public void removeConfigContextEventListener(ConfigContextEventListener ccel) {
             if (listeners==null)
                listeners = new Vector();
            listeners.removeElement(ccel);
        }
    
        
    private static final String PRE_CHANGE = "PRE_CHANGE";
    private static final String POST_CHANGE = "POST_CHANGE";
    
    /**
     * Notify all my listeners that I will change!
     *
     */
    
    // <addition> srini@sun.com server.xml verifier
    //public void preChange(String type) {
    public void preChange(ConfigContextEvent ccce) {
        //change(PRE_CHANGE, type);
        change(PRE_CHANGE, ccce);
    }
    
    //public void postChange(String type) {
    public void postChange(ConfigContextEvent ccce) {        
        //change(POST_CHANGE, type);
        change(POST_CHANGE, ccce);
    }
    
    //private void change(String when, String type) {
    private void change(String when, ConfigContextEvent ne) {
        if (listeners==null)
            return;
        
        //ConfigContextEvent ne = new ConfigContextEvent(this, type);
        String type = ne.getType();
        
        Vector listenersClone = null;
        synchronized (listeners) {
            listenersClone = (Vector) listeners.clone();
        }
        for (Enumeration e = listenersClone.elements(); e.hasMoreElements();) {
            ConfigContextEventListener nl = (ConfigContextEventListener) e.nextElement();
            if(when.equals(PRE_CHANGE)) {
                /*
                if(type.equals(ConfigContextEvent.PRE_ACCESS) || type.equals(ConfigContextEvent.POST_ACCESS))
                    nl.preAccessNotification(ne);
                else
                 */
                    nl.preChangeNotification(ne);
            } else {
                /*
                if(type.equals(ConfigContextEvent.PRE_ACCESS) || type.equals(ConfigContextEvent.POST_ACCESS))
                    nl.postAccessNotification(ne);
                else
                 */
                    nl.postChangeNotification(ne);
            }
        }
    }
    
    /**
     * Returns boolean true is this is the config context for admin.
     */
    public boolean isResolvingPaths() {
        return this.resolvePath;
    }
    
    // </addition> server.xml verifier
    
    /**
     * This method is used to activate the lastModified checking for a configContext
     * If activated, configbeans will carry a lastmodified timestamp in every bean.
     * This time is also carried onto the configChangeList and also to clones. When 
     * configContext.updateFromConfigChangeList is called, the timestamp is first checked
     * to see if the bean has not changed since the clone and then the update is made.
     * If a modification to the bean is detected, a staleWriteConfigException is thrown.
     *
     * @param value boolean to enable/disable
     * @return boolean previous value that was set (not the changed value)
     */
    public synchronized boolean enableLastModifiedCheck(boolean value) {
        boolean prev = _lastModifiedCheck;
        _lastModifiedCheck = value;
        return prev;
    }
    
    public boolean isLastModifiedCheckEnabled() {
        return _lastModifiedCheck;
    }
    
    /**
     * throws StaleWriteConfigException if lastmodified is initialized
     * and there is any modifications
     */
    private void validateLastModified(ConfigBean cb, ConfigChange cc) 
                                       throws StaleWriteConfigException {
                                           
        if(cb == null || cc == null) return;
        
        long beanLM = cb.getThisLastModified();
        if(beanLM == -1) return; // not initialized. so, don't validate
        
        long ccLM = cc.getGlobalLastModified();
        if(ccLM == -1) return; // not initialized. so, don't validate
        
        if (beanLM == ccLM) return; // okay.
        
        throw new StaleWriteConfigException("validateLastModified failed for cb=" + cb);
    }
    
    public synchronized ArrayList updateFromConfigChange(ArrayList configChangeList) 
                                                                throws ConfigException {
        
        ArrayList errList = new ArrayList();
        if (configChangeList == null || configChangeList.size() == 0) return errList;
        
        validateAllLastModified(configChangeList);
        
        boolean prev = enableLastModifiedCheck(false);
        try {
            for(int i = 0 ; i < configChangeList.size(); i++ ) {
                ConfigChange cc = (ConfigChange) configChangeList.get(i);
                try {
                    updateFromConfigChange(cc);
                } catch (Exception e) {
                    errList.add(cc);
                }
            }
	// save the context. TBD
        } finally {
            enableLastModifiedCheck(prev);
        }
        return errList;
    }
    
    private void validateAllLastModified(ArrayList arr) throws StaleWriteConfigException {
        if(arr == null || arr.size() == 0) return;
        
        for(int i = 0 ; i < arr.size(); i++ ) {
           ConfigChange cc = (ConfigChange) arr.get(i);
           validateLastModified(cc);
        }
    }
    
    private void validateLastModified(ConfigChange cc) throws StaleWriteConfigException {
        ConfigBean bean = getConfigBeanFromConfigChange(cc);
        validateLastModified(bean, cc);
    }
    
    private ConfigBean getConfigBeanFromConfigChange(ConfigChange configChange) {
        ConfigBean result = null;
        try {
            if(configChange.getConfigChangeType().equals(ConfigChange.TYPE_UPDATE)) {
                result = ConfigBeansFactory.getConfigBeanByXPath(this,configChange.getXPath());
            } else if (configChange.getConfigChangeType().equals(ConfigChange.TYPE_DELETE)) {
                String xpath = configChange.getXPath();
                ConfigBean child = ConfigBeansFactory.getConfigBeanByXPath(this,xpath);
                if(child != null) {
                    result = (ConfigBean) child.parent();
                }
            } else { // add/set
                result = ConfigBeansFactory.getConfigBeanByXPath(this, configChange.getParentXPath());
            }
        } catch (ConfigException ce) {
            //ignore
        }
        return result;
    }
    
    public String getUrl() {
         return this.xmlUrl;
    }

    public ConfigBeanInterceptor getConfigBeanInterceptor() {
        ConfigBeanInterceptor cbiClone = null;
        if (null != configBeanInterceptor) {
            cbiClone = (ConfigBeanInterceptor)configBeanInterceptor.clone();
        }
        return cbiClone;
    }
}
