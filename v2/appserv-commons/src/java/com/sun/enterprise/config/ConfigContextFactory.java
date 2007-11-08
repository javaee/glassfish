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
import java.util.Hashtable;
import org.xml.sax.helpers.DefaultHandler;
import com.sun.enterprise.config.impl.ConfigContextImpl;
import com.sun.enterprise.config.pluggable.EnvironmentFactory;
import com.sun.enterprise.config.pluggable.ConfigEnvironment;

/**
 * A factory to create ConfigContext
 *
 */
public class ConfigContextFactory {
    
    private static Hashtable _ctxCache = new Hashtable();
    
    
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
     * TBD
     */
    public ConfigContext createConfigContext(String url, String rootClass) {
        ConfigEnvironment ce = getConfigEnvironment();
        ce.setUrl(url);
        ce.setRootClass(rootClass);
        return createConfigContext(ce);
    }
    
    /**
     *
     */
    public static ConfigContext createConfigContext(ConfigEnvironment ce) {
        
        if(!ce.isCachingEnabled()) {
            //log that a new one is created //FIXME
            return newConfigContext(ce);
        }
        
        ConfigContext context = getConfigContextFromCache(ce.getUrl());
        
        if(context == null) {
            context = newConfigContext(ce);
             addConfigContextToCache(ce.getUrl(), context);
             //log that it was created. //FIXME
        } else {
            //log that it was found in cache //FIXME
        }
        return context;
    }
    
    public static void removeConfigContext(ConfigContext ctx) {
        String url = ctx.getUrl();
        removeConfigContext(url);
    }
    
    public static synchronized void removeConfigContext(String url) {
        Object obj = _ctxCache.remove(url);
        invalidateConfigContext(obj);
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
        
        assert (oldCtx != null);
        assert (newCtx != null);
        
        String url = oldCtx.getUrl();
        
        if(_ctxCache.containsKey(url)) {
            _ctxCache.put(url, newCtx);
        } else {
            throw new ConfigException
            ("Old ConfigContext is not found. Cannot replace with new one");
            //FIxME
        }
    }
    
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
     *
     * FIXME move this to configcontext
     */
    public static boolean enableLastModifiedCheck(ConfigContext ctx, boolean value) {
        return ((ConfigContextImpl)ctx).enableLastModifiedCheck(value);
    }
    
    private static ConfigEnvironment getConfigEnvironment() {
        ConfigEnvironment ce = null;
        try {
            ce = EnvironmentFactory.
                    getEnvironmentFactory().
                    getConfigEnvironment();
        } catch(Exception e) {
            throw new ConfigRuntimeException
                    ("err_getting_config_env", 
                    "err_getting_config_env",
                    e); //FIXME
        }
        return ce;
    }
    
    public static ConfigContext getConfigContextFromCache(String url) {
        return (ConfigContext) _ctxCache.get(url);
    }
    
    private static ConfigContext newConfigContext(ConfigEnvironment ce) {
        return new ConfigContextImpl(ce);
    }
    
    private static void addConfigContextToCache(String url, ConfigContext ctx) {
        _ctxCache.put(url, ctx);
    }
    
    private static void invalidateConfigContext(Object obj) {
        try {
            if(obj != null)
                ((ConfigContextImpl)obj).cleanup(); //FIXME
        } catch(Exception e) {
            // ignore since it is just cleanup.
            // multiple calls to remove should not fail.
        }
    }
}
