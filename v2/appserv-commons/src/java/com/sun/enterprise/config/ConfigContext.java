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

package com.sun.enterprise.config;

import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;
import java.util.ArrayList;

// </addition> server.xml verifier

/**
 * A configuration context represents a heirarchical view of the configuration
 * attributes. It reads the configuration attributes from <i> server.xml </i>
 * The configuration context has a one-to-one realationship with
 * a configuration file. 
 *
 * There is no public constructor. Use ConfigFactory to construct ConfigContext. 
 *
 * This object can be made readOnly and autoCommit during creation but these parameters
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
 * Config API documentation:
 * Config API is a powerful set of APIs that handle comples operations like:
 * <PRE>
 * 1. Basic Configuration (set and get elements and attributes APIs)
 * 2. Event Notification
 * 3. Cloning
 * 4. XPath support
 * 5. Change Mananagement (add, update, delete, set)
 * 6. readonly
 * 7. Auto Commit
 * 8. Advanced Configuration (1 API to access attributes--ctx.getAttribute())
 * 9. Intelligent refresh
 * 10. Get dtd default values
 * 11. DOM like API support (appendChild, removeChild)
 * 12. Find APIs
 * 13. Support Serialization and DeSerialization
 * 14. Other Misc APIs (getChildBeansByName, getXPath, etc)
 * 15. Merge APIs
 *
 * </PRE>
 */
public interface ConfigContext {     
                
    /**
     * gets the url of the file/storage that this configcontext 
     * is representing. It could be null in the case when the
     * context has been cloned or deserialized
     */
    String getUrl();
    
        /**
         * returns the list of config changes done to this config context
         * since the last reset or refresh or flush
         */
        public ArrayList getConfigChangeList();
            
        /**
         * reset config change list. 
         */
        public void resetConfigChangeList();
            
 
        
        /**
         * Get the config bean for the root of the xml tree.
         */
        public Object getRootConfigBean() throws ConfigException;
        
        public void removeConfigChange(ConfigChange change);

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
	public Object exactLookup(String xpath) throws ConfigException;
	
        /**
         * Note the constraints on xpath
         */
        public Object[] lookup(String xpath) throws ConfigException;
        
	/**
         * saves all the changes in ConfigContext to storage
         * Throws ConfigException if there is any problem writing to storage.
         * or the file has been modified after last read. 
         * However, if overwrite is true, then it overwrites any manual changes
         * on disk
         */
        public void flush(boolean overwrite) throws ConfigException;

        /**
         * is equivalent to flush(false)
         */
        public void flush() throws ConfigException;
        
        /** discards all the changes and reloads the xml from storage based on the force flag
         *
         *  @param force  forces a reload from disk even if dirty bit is not set. 
         *  throws configexception if force is false and isDirty
         */        
        public void refresh(boolean force) throws ConfigException;

        /** 
         * is equivalent to refresh(false).
         */        
        public void refresh() throws ConfigException;
        
        /**
         * Clone entire config context. Note that configChangeList is not cloned.
         *
         */
        public Object clone();
        
        /**
         * update this context with a configuration change. Changes done to
         * configContext are maintained as a list of configChange objects
         * This method is a convenient way of updating a context with a list
         * of changes done to another context (may be in another VM)
         */
        public void updateFromConfigChange(ConfigChange configChange) throws ConfigException;
        
        /**
         * see updateFromConfigChange(ConfigChange configChange)
         * This method can be used for updating from a list of changes
         * It is also used to check last modified so that all of them succeed for fail
         * due to the changes.
         * @return ArrayList of all failed updates
         */
        public ArrayList updateFromConfigChange(ArrayList configChangeList) throws ConfigException;
        
        
        /**
         * Get the Value of an attribute with One call 
         * This equivalent of doing an exactLookup to get a bean 
         * and then a getAttributeValue on that bean
         * 
         * @return value of attribute. null if it cannot find exactly 1 element
         * or that attribute
         * 
         */
        public String getAttributeValue(String xpath, String attributeName);
        
        /**
         * similar to getAttributeValue but returns a boolean
         *
         * @return true if it finds 1 element and attributeName has value=true
         * else returns false.
         */
        public boolean getBooleanAttributeValue(String xpath, String attributeName);
        
        /**
         * Tells you if this configcontext has changed since it was read in or changes
         * were reset
         */
        public boolean isChanged();
        
        /**
         * returns true if File has changed externally (not by this configcontext)
         * after the configcontext was created
         * Depends on whether it is enabled. if disabled, returns false
         */
        public boolean isFileChangedExternally();      
        
         /*
         * Add Notification Listener
         */
        public void addConfigContextEventListener(ConfigContextEventListener ccel);
        
        /*
         * remove
         */
        public void removeConfigContextEventListener(ConfigContextEventListener ccel);
        
        /*
         * cleanup. call this method after you are done using the context
         */
        public void cleanup();
        
        
        // FOLLOWING ARE USED ONLY FOR IMPLEMENTATION. FIXME: TO BE CLEANED UP
        
        /**
         * add config update to the ConfigChangeList
         * @return ConfigChange that was just added to ConfigChangeList
         */
        ConfigChange addToConfigChangeList(String xpath, String attrName, String oldValue, String newValue);
        /**
         * add config add to the ConfigChangeList
         * @return ConfigChange that was just added to ConfigChangeList
         */
        ConfigChange addToConfigChangeList(String xpath, String childXpath, String name, ConfigBean cb);
        /**
         * add config delete to the ConfigChangeList
         * @return ConfigChange that was just added to ConfigChangeList
         */
        ConfigChange addToConfigChangeList(String xpath);
        /**
         * add config set to the ConfigChangeList
         * @return ConfigChange that was just added to ConfigChangeList
         */
        ConfigChange addToConfigChangeList(String xpath, String name, Object cb, Object[] cbArray);
        
        /**
         * used only by impl for getting changes
         */
        // <addition> srini@sun.com server.xml verifier
        //void preChange(String type);
        void preChange(ConfigContextEvent ccce);
        // </addition> server.xml verifier
        
        /**
         * used only by impl for getting changes
         */ 
        // <addition> srini@sun.com server.xml verifier
        //void postChange(String type);
        void postChange(ConfigContextEvent ccce);
        // </addition>  server.xml verifier
        
        ConfigBeanInterceptor getConfigBeanInterceptor();
}
