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

import java.util.Map;
import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;
import com.sun.enterprise.config.impl.ConfigContextImpl;
import com.sun.enterprise.config.pluggable.EnvironmentFactory;
import com.sun.enterprise.config.pluggable.ConfigEnvironment;

/**
 * A factory to create ConfigContext objects and ConfigBeans.
 *
 *  @deprecated use ConfigContextFactory
 */
public class ConfigFactory {


    public static ConfigContext createConfigContext(String fileUrl, DefaultHandler dh) throws ConfigException {
        final ConfigEnvironment ce = getConfigEnvironment(fileUrl, false, false, true, true);
        ce.setHandler(dh.getClass().getName());
        return ConfigContextFactory.createConfigContext(ce);
    }
    
        /**
         * Creates a configuration context for the specified URL.
         *
         * @param fileUrl The URL to server.xml
         *
         * If a configuration context object for this URL was previously
         * created then that object is returned.
         */
        public static ConfigContext createConfigContext(String fileUrl)
            throws ConfigException {
            return  createConfigContext(fileUrl, false, false);
        }
        
        /**
     	 * Creates a configuration context for the specified URL and in
         * the specified mode.
       	 * 
         * @param fileUrl The URL to server.xml
         * @param readOnly if true, then the ConfigContext returned cannot
         *                 be used to make changes to the XML file
     	 * @return If a configuration context object for this URL was 
         *         previously created then that object is returned.
     	 */
        public static ConfigContext createConfigContext(String fileUrl,
                boolean readOnly) throws ConfigException {
            return  createConfigContext(fileUrl, readOnly, false);
        }

        /**
     	 * Creates a configuration context for the specified URL and in
         * the specified mode.
         *
         * @param fileUrl The URL to server.xml
         * @param readOnly if true, then the ConfigContext returned cannot
         *                 be used to make changes to the XML file
         * @param autoCommit if true, then changes to the ConfigContext object
         *                   are automatically committed to the file.
     	 * @return If a configuration context object for this URL was 
         *         previously created then that object is returned.
         */
        public static ConfigContext createConfigContext(String fileUrl, 
                boolean readOnly, boolean autoCommit) throws ConfigException {
            return  createConfigContext(fileUrl, readOnly, autoCommit, true);
        }

        /**
         * Returns a ConfigContext object that was either previously
         * created (and stored in the cache) or created anew.
         *
         * If <code>cache</code> is <code>true</code> then the factory looks
         * up its cache of previously created ConfigContext objects and if
         * a matching one is found then that is returned. If the cache lookup
         * failed, then a new ConfigContext is created and inserted into the
         * cache.
         *
         * If <code>cache</code> is <code>false</code> then the cache is
         * <i>not</i> used and a new ConfigContext object is returned. This
         * object is <i>not</i> inserted into the cache.
         *
         * Note that if ConfigContext already exists, readOnly and autoCommit
         * are not used.
         * @param fileUrl
         * @param readOnly
         * @param autoCommit
         * @param cache
         * @throws ConfigException
         * @return  */
        public static ConfigContext createConfigContext(String fileUrl, 
                boolean readOnly, boolean autoCommit, boolean cache)
                throws ConfigException {
           
           return createConfigContext(fileUrl, readOnly, autoCommit, 
                   cache, true);
        }
        
          /**
         * Returns a ConfigContext object that was either previously
         * created (and stored in the cache) or created anew.
         *
         * If <code>cache</code> is <code>true</code> then the factory looks
         * up its cache of previously created ConfigContext objects and if
         * a matching one is found then that is returned. If the cache lookup
         * failed, then a new ConfigContext is created and inserted into the
         * cache.
         *
         * If <code>cache</code> is <code>false</code> then the cache is
         * <i>not</i> used and a new ConfigContext object is returned. This
         * object is <i>not</i> inserted into the cache.
         *
         * If <code>resolvePath</code> is <code>true</code> then the 
         * absolute paths of certain attributes in domain.xml are resolved 
         * as in the runtime context.
         *
         * Note that if ConfigContext already exists, readOnly and autoCommit
         * are not used.
         * @param fileUrl
         * @param readOnly
         * @param autoCommit
         * @param cache
         * @param resolvePath
         *
         * @throws ConfigException
         * @return  
         */
        public static ConfigContext createConfigContext(String fileUrl, 
                boolean readOnly, boolean autoCommit, boolean cache, 
                boolean resolvePath)
                throws ConfigException {    

            return ConfigContextFactory.createConfigContext(
                    getConfigEnvironment(fileUrl, readOnly, autoCommit, 
                        cache, resolvePath));
        }

        /**
         * this method adds a validation handler for creating a context
         * default is com.sun.enterprise.config.serverbeans.ServerValidationHandler
         */
        public static ConfigContext createConfigContext(String fileUrl, 
                boolean readOnly, boolean autoCommit, boolean cache, Class rootClass)
                throws ConfigException { 
                    
           ConfigEnvironment ce = getConfigEnvironment(fileUrl, readOnly, 
               autoCommit, cache, true);
           ce.setRootClass(rootClass.getName());
           return ConfigContextFactory.createConfigContext(ce);
        }
        
        /**
         * Creates a different class of beans based on rootClass
         * By default, in the other createConfigContext APIs, rootClass
         * is com.sun.enterprise.config.Server
         *
         * This API can be used to create serverbeans, clientbeans, or any other
         * class of beans
         */
        public static synchronized ConfigContext createConfigContext(String fileUrl, 
                boolean readOnly, boolean autoCommit, boolean cache, Class rootClass,
                DefaultHandler dh)
                throws ConfigException { 
                    
            ConfigEnvironment ce = getConfigEnvironment(fileUrl, readOnly, 
                    autoCommit, cache, true);
            ce.setRootClass(rootClass.getName());
            ce.setHandler(dh.getClass().getName());
            return ConfigContextFactory.createConfigContext(ce);
        }
                
        public static void removeConfigContext(ConfigContext ctx) {                     
            ConfigContextFactory.removeConfigContext(ctx);
        }
        
        public static synchronized void removeConfigContext(String fileUrl) {
            ConfigContextFactory.removeConfigContext(fileUrl);
        }
        
        /**
         * Replaces a cached context with a new context
         * So, next time the Url is accessed, it returns the new context
         *
         * Note the subtlity of this method: This method gets the url of
         * the old context and replaces the value for that key with the new object
         * Hence, even though there might be a configContext with the same url
         * but a different object, it is still replaced. This behavior is desirable
         * since we would like to have the newCtx set anyway.
         *
         * However, if the url was not present, then ConfigException is thrown
         * with that message. Note that: this class is not i18n complaint.
         *
         * If the old context cannot be found in cache, then throw ConfigException
         *
         * This method is synchronized so to make it thread safe.
         *
         * @param oldCtx Old ConfigContext that was cached in this class. This 
         *               cannot be null.
         * @param newCtx New ConfigContext that will be replaced in the cache. This
         *               cannot be null.
         * @throws ConfigException if url for old ctx is not found in cache
         */
        public static synchronized void replaceConfigContext(
                                ConfigContext oldCtx, 
                                ConfigContext newCtx) 
                                throws ConfigException {
            ConfigContextFactory.replaceConfigContext(oldCtx, newCtx);
        }
        
        /**
         * xpath has to conform to the syntax below.
         * <PRE>
         *     expression := /tagName | /tagName/tagExpression
         *     tagExpression := tagName| tagName[@name='value'] | tagName/tagExpression | tagName[@name='value']/tagExpression
         * <PRE>
         * @param ctx
         * @param xpath
         * @throws ConfigException
         * @return  */
    /*public static ConfigBean getConfigBeanByXPath(ConfigContext ctx, String xpath)
                throws ConfigException {
         return ConfigBeansFactory.getConfigBeanByXPath(ctx, xpath);
    }  */  
    
    /**
     * This method is used to activate the lastModified checking for a configContext
     * If activated, configbeans will carry a lastmodified timestamp in every bean.
     * This time is also carried onto the configChangeList and also to clones. When 
     * configContext.updateFromConfigChangeList is called, the timestamp is first checked
     * to see if the bean has not changed since the clone and then the update is made.
     * If a modification to the bean is detected, a staleWriteConfigException is thrown.
     *
     * @param ctx ConfigContext on which to enable/disable checking
     * @param value boolean to enable/disable
     * @return boolean previous value that was set (not the changed value)
     */
    public static boolean enableLastModifiedCheck(ConfigContext ctx, boolean value) {
        return ConfigContextFactory.enableLastModifiedCheck(ctx, value);
    }
/*
    private static ThreadLocal _threadLocalConfigContext = 
                            new ThreadLocal();

    public static ConfigContext getConfigContextFromThreadLocal() {
        return (ConfigContext)_threadLocalConfigContext.get();
    }

    public static void setConfigContextInThreadLocal(ConfigContext ctx) {

        try {
            ((ConfigContextImpl)ctx).setXPathInAllBeans();
        } catch (ConfigException ce) {
            // ignore
        }
        _threadLocalConfigContext.set(ctx);
    }*/
    
    private static ConfigEnvironment getConfigEnvironment(String fileUrl,
        boolean readOnly, boolean autoCommit, boolean cache, 
        boolean resolvePath) {

        ConfigEnvironment ce = EnvironmentFactory.getEnvironmentFactory().
            getConfigEnvironment();
        ce.setUrl(fileUrl);
        ce.setReadOnly(readOnly);
        ce.setCachingEnabled(cache);
        ce.getConfigBeanInterceptor().setResolvingPaths(resolvePath);
        return ce;
    }
}
