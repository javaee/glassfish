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
 * ServerConfigReader.java
 *
 * Created on October 21, 2002, 2:37 PM
 */

package com.sun.enterprise.ee.web.initialization;

import java.util.logging.Logger;
import java.util.Properties;
import com.sun.logging.LogDomains;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;

import com.sun.enterprise.ee.web.sessmgmt.StorePool;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.WebContainer;


/**
 *
 * @author  lwhite
 */
public class ServerConfigReader extends ServerConfigLookup {
    
    /** The default pool size */
    //private static final int DEFAULT_HASTORE_POOL_SIZE = 20;
    private static final int DEFAULT_HASTORE_POOL_SIZE = 
        StorePool.DEFAULT_INITIAL_SIZE;
    
    /** The default pool upper size */
    //private static final int DEFAULT_HASTORE_POOL_UPPER_SIZE = 40;
    private static final int DEFAULT_HASTORE_POOL_UPPER_SIZE = 
        StorePool.DEFAULT_UPPER_SIZE;    
    
    /** The default pool poll time */
    //private static final int DEFAULT_HASTORE_POOL_POLL_TIME = 10; 
    private static final int DEFAULT_HASTORE_POOL_POLL_TIME = 
        StorePool.DEFAULT_POLL_TIME; 
    
    /** The xpath string to lookup ha-store-pool-size in server.xml */
    private static String HASTORE_POOL_SIZE_XPATH_STRING = 
          "/server/availability-service/persistence-store/property[@name='ha-store-pool-size']";
    
    /** The xpath string to lookup ha-store-upper-pool-size in server.xml */
    private static String HASTORE_POOL_UPPER_SIZE_XPATH_STRING = 
          "/server/availability-service/persistence-store/property[@name='ha-store-upper-pool-size']";
    
    /** The xpath string to lookup ha-store-poll-time in server.xml */
    private static String HASTORE_POOL_POLL_TIME_XPATH_STRING =  
          "/server/availability-service/persistence-store/property[@name='ha-store-poll-time']"; 

    /** name of a needed HADB class - used to test if HADB is installed */
    private static String HADB_DBSTATE_CLASS_NAME = 
          "com.sun.hadb.dbstate.DbState";    
    
    /** Creates a new instance of ServerConfigReader */
    public ServerConfigReader() {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
    }
    
    /**
     * is monitoring enabled
     */
    protected static boolean _isMonitoringEnabled = false;
    
    /**
     * is HADB installed
     */
    protected static boolean _isHADBInstalled = false;    
    
    static
	{
            _isMonitoringEnabled = checkMonitoringEnabled();
            _isHADBInstalled = checkHADBInstalled();
	} 

    /**
     * Is (any) monitoring enabled -- private or public
     * Statistics gathering is based on this value
     */    
    public static boolean isMonitoringEnabled() {
        return (isPrivateMonitoringEnabled() || isPublicMonitoringEnabled());
        //return _isMonitoringEnabled || WebContainer.isMonitoringEnabled();
        //return _isMonitoringEnabled;
    }

    /**
     * Is private (internal) monitoring enabled
     */    
    public static boolean isPrivateMonitoringEnabled() {
        return _isMonitoringEnabled;
    }
    
    /**
     * Is public (external) monitoring enabled
     */    
    public static boolean isPublicMonitoringEnabled() {
        return WebContainer.isMonitoringEnabled();
    }    
    
    protected static boolean checkMonitoringEnabled() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String str=props.getProperty("MONITOR_WEB_CONTAINER");
            if(null!=str) {
                if( str.equalsIgnoreCase("TRUE"))
                    result=true;
            } 
        } catch(Exception e)
        {
            //do nothing just return false
        }
        return result;
    }
    
    /**
     * Is HADB installed
     */    
    public static boolean isHADBInstalled() {   
        return _isHADBInstalled;
    }    
    
    protected static boolean checkHADBInstalled() {
        boolean result = false;
        Object dbStateInstance = null;
	try
        {
            dbStateInstance = createHADBObject(); 
        } catch(Exception e)
        {
            //do nothing just return false
        }
        if(dbStateInstance != null) {
            result = true;
        }
        return result;
    }       
     
    /**
     * Get the hastore pool size from server.xml.
     * return default size 20 if not found
     */
     public int getHAStorePoolSizeFromConfig() {
         return this.getWebContainerAvailabilityPropertyInt(HASTORE_POOL_SIZE_XPATH_STRING, 
             DEFAULT_HASTORE_POOL_SIZE);  
     }      
     
    /**
     * Get the hastore pool upper size from server.xml.
     * return default size 40 if not found
     */
     public int getHAStorePoolUpperSizeFromConfig() {
         return this.getWebContainerAvailabilityPropertyInt(HASTORE_POOL_UPPER_SIZE_XPATH_STRING, 
             DEFAULT_HASTORE_POOL_UPPER_SIZE);
     }        
     
     /**
     * Get the hastore pool poll time from server.xml.
     * return default size 10 ms if not found
     */
     public int getHAStorePoolPollTimeFromConfig() {
         return this.getWebContainerAvailabilityPropertyInt(HASTORE_POOL_POLL_TIME_XPATH_STRING, 
            DEFAULT_HASTORE_POOL_POLL_TIME);       
     }
     
    /**
     * Get the connectionURL for oracle from domain.xml.
     */ 
    public String getConnectionURLFromConfigForOracle() {

        String url = null;
        StringBuffer sb = new StringBuffer();
        JdbcConnectionPool pool = this.getHadbJdbcConnectionPoolFromConfig();
        if(pool == null)
            return null;
        if (pool.sizeElementProperty() > 0) {
            ElementProperty[] props = pool.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase("URL")) {
                    sb.append(value);
                    url = sb.toString(); 
                }
            }
        }

        _logger.finest("IN NEW getConnectionURLFromConfigForOracle: url=" + url); 
        return url;
    }
    
    /**
     * attempt to load the HADB DbState class
     * used to test if HADB client jars are installed
     *
     */     
    private static Object createHADBObject() {
        
        Object testInstance = null;
        String className = HADB_DBSTATE_CLASS_NAME;
        _logger.finest("attempting to load DbState class");      
        try {
            testInstance = 
                (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            _logger.finest("unable to load DbState - HADB is not installed");
        }    
        return testInstance;
    }
    
    
     
      /**
       * The logger to use for logging ALL web container related messages.
       */
      private static Logger _logger = null;    
    
}
