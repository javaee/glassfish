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
 * HADBCreateResources.java
 *
 * Created on April 13, 2004, 2:22 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.EjbContainerAvailability;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JmsAvailability;
import com.sun.enterprise.config.serverbeans.WebContainerAvailability;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.StringUtils;
import java.io.*;
import java.util.*;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 *
 * @author  bnevins
 */
public class HADBResourceManager
{
    public HADBResourceManager(HADBInfo info) throws HADBSetupException
    {
        this.info = info;
        poolName = info.getClusterName() + "-hadb-pool";
        jndiName = "jdbc/" + info.getClusterName() + "-" + "hastore";
        
        target = info.getClusterName();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString()
    {
        String ret = "******  Dump of HADBCreateResources instance *****\n\n";
        
        if(poolAttribs != null)
        {
            ret += "******  Attributes for creating connection pool ****\n";
            for(Iterator it = poolAttribs.iterator(); it.hasNext(); )
            {
                Attribute at = (Attribute)it.next();
                ret += at.getName() + "=" + at.getValue() + "\n";
            }
        }
        
        if(poolProps != null)
        {
            ret += "******  Properties for creating connection pool ****\n";
            Set s = poolProps.entrySet();
            
            for(Iterator it = s.iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                ret += entry.getKey() + "=" + entry.getValue() + "\n";
            }
        }
        
        return ret;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public boolean isAvailabilityEnabled()
    {
        try
        {
            Config		config		= info.getConfigForCluster();
            AvailabilityService avail		= config.getAvailabilityService();
            return avail.isAvailabilityEnabled();
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public Boolean getAutoHadbFromConfig()
    {
        try
        {
            Config		config	= info.getConfigForCluster();
            AvailabilityService avail	= config.getAvailabilityService();
            return avail.isAutoManageHaStore();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public void setAutoHadb(boolean newAuto)
    {
        try
        {
            Config		config	= info.getConfigForCluster();
            AvailabilityService avail	= config.getAvailabilityService();
            avail.setAutoManageHaStore(newAuto,	BaseConfigMBean.OVERWRITE);
        }
        catch(Exception e)
        {
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public final String getAdminPasswordFromConfig() throws HADBSetupException
    {
        Config config = info.getConfigForCluster();
        
        if(config == null)
            return null;
        
        AvailabilityService avail = config.getAvailabilityService();
        String pw = avail.getHaAgentPassword();
        
        if(!StringUtils.ok(pw))
            return null;
        
        return pw;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public final String getHostsFromConfig() throws HADBSetupException
    {
        Config config = info.getConfigForCluster();
        
        if(config == null)
            return null;
        
        AvailabilityService avail = config.getAvailabilityService();
        String hosts = avail.getHaAgentHosts();
        
        if(!StringUtils.ok(hosts))
            return null;
        
        return hosts;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public final String getAgentPortFromConfig()  throws HADBSetupException
    {
        // performance note -- the caller is going to call Integer.parseInt() on whatever
        // we return.  So I return "0" instead of null for failure.  Then we won't waste time
        // with an Exception getting thrown by parseInt().  The calling code will then
        // reject the port number of 0
        
        Config config = info.getConfigForCluster();
        
        if(config == null)
            return null;
        
        AvailabilityService avail = config.getAvailabilityService();
        String port = avail.getHaAgentPort();
        
        if(!StringUtils.ok(port))
            return "0";
        
        return port;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    public final boolean isHA() throws HADBSetupException
    {
        boolean hasName = (getDBNameFromConfig() == null) ? false : true;
        boolean avail   = isAvailabilityEnabled();
        
        return (hasName && avail);
    }
    ///////////////////////////////////////////////////////////////////////////
    
    void createPool() throws HADBSetupException
    {
        setCreatePoolAttributes();
        setCreatePoolProperties();
        invokeCreatePool();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    void deletePool() throws HADBSetupException
    {
        invokeDeletePool();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    void createJdbcResource() throws HADBSetupException
    {
        setCreateJdbcResourceAttributes();
        invokeCreateJdbcResource();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    void deleteJdbcResource() throws HADBSetupException
    {
        //invokeDeleteJdbcResource();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    void enableAvailabilityService(boolean enable) throws HADBSetupException
    {
        try
        {
            Config config = info.getConfigForCluster();
            
            if(config == null && enable == false)
            {
                // the cluster is already gone
                return;
            }
            
            AvailabilityService avail = config.getAvailabilityService();
            
            if(enable == true)
                enableAvailabilityService(avail);
            else
                disableAvailabilityService(avail);
        }
        catch(StaleWriteConfigException swce)
        {
            throw new HADBSetupException("hadbmgmt-res.StaleWriteAvailability", swce);
        }
        catch(ConfigException ce)
        {
            throw new HADBSetupException("hadbmgmt-res.ConfigAvailability", ce);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //////////    private methods    //////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    private void enableAvailabilityService(AvailabilityService avail) throws StaleWriteConfigException, ConfigException, HADBSetupException
    {
        avail.setAutoManageHaStore(	info.getAutoHadb(),			BaseConfigMBean.OVERWRITE);
        avail.setHaStoreName(		info.getClusterName(),		BaseConfigMBean.OVERWRITE);
        avail.setHaAgentPort(		"" + info.getAgentPort(),	BaseConfigMBean.OVERWRITE);
        avail.setHaAgentHosts(		info.getHosts(),			BaseConfigMBean.OVERWRITE);
        avail.setStorePoolName(		jndiName,					BaseConfigMBean.OVERWRITE);
        avail.setAvailabilityEnabled(true,						BaseConfigMBean.OVERWRITE);
        
        String apw = info.getAdminPassword();
        String dpw = info.getDASPassword();
        
        if(apw.equals(dpw))
            avail.setHaAgentPassword("",	BaseConfigMBean.OVERWRITE);
        else
            avail.setHaAgentPassword(apw,	BaseConfigMBean.OVERWRITE);
        
        EjbContainerAvailability	ejbAvail	= avail.getEjbContainerAvailability();
        WebContainerAvailability	webAvail	= avail.getWebContainerAvailability();
        JmsAvailability				jmsAvail	= avail.getJmsAvailability();
        
        // Bug 6171921 -- need to add some default values for attributes
        
        ejbAvail.setSfsbStorePoolName(			jndiName,	BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbPersistenceType(		"ha",		BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbHaPersistenceType(		"ha",		BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbCheckpointEnabled(		"true",		BaseConfigMBean.OVERWRITE);
        ejbAvail.setAvailabilityEnabled(		"true",		BaseConfigMBean.OVERWRITE);
        
        webAvail.setHttpSessionStorePoolName(	jndiName,	BaseConfigMBean.OVERWRITE);
        webAvail.setAvailabilityEnabled(		"true",		BaseConfigMBean.OVERWRITE);
        
        jmsAvail.setAvailabilityEnabled(		false,		BaseConfigMBean.OVERWRITE);
        jmsAvail.setMqStorePoolName(			jndiName,	BaseConfigMBean.OVERWRITE);
        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void disableAvailabilityService(AvailabilityService avail) throws StaleWriteConfigException, ConfigException, HADBSetupException
    {
        avail.setAutoManageHaStore(	false,					BaseConfigMBean.OVERWRITE);
        avail.setHaStoreName(		"",						BaseConfigMBean.OVERWRITE);
        avail.setHaAgentPort(		"",						BaseConfigMBean.OVERWRITE);
        avail.setHaAgentHosts(		"",						BaseConfigMBean.OVERWRITE);
        avail.setStorePoolName(		DEFAULT_JNDI_NAME,		BaseConfigMBean.OVERWRITE);
        avail.setAvailabilityEnabled(false,					BaseConfigMBean.OVERWRITE);
        avail.setHaAgentPassword(	"",						BaseConfigMBean.OVERWRITE);
        
        EjbContainerAvailability	ejbAvail	= avail.getEjbContainerAvailability();
        WebContainerAvailability	webAvail	= avail.getWebContainerAvailability();
        JmsAvailability				jmsAvail	= avail.getJmsAvailability();
        
        // Bug 6171921 -- need to add some default values for attributes
        
        ejbAvail.setSfsbStorePoolName(			DEFAULT_JNDI_NAME,	BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbPersistenceType(		"ha",				BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbHaPersistenceType(		"ha",				BaseConfigMBean.OVERWRITE);
        ejbAvail.setSfsbCheckpointEnabled(		"true",				BaseConfigMBean.OVERWRITE);
        ejbAvail.setAvailabilityEnabled(		"false",			BaseConfigMBean.OVERWRITE);
        
        webAvail.setHttpSessionStorePoolName(	DEFAULT_JNDI_NAME,	BaseConfigMBean.OVERWRITE);
        webAvail.setAvailabilityEnabled(		"false",			BaseConfigMBean.OVERWRITE);
        
        jmsAvail.setAvailabilityEnabled(		false,				BaseConfigMBean.OVERWRITE);
        jmsAvail.setMqStorePoolName(			DEFAULT_JNDI_NAME,	BaseConfigMBean.OVERWRITE);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private final String getDBNameFromConfig() throws HADBSetupException
    {
        Config config = info.getConfigForCluster();
        
        if(config == null)
            throw new HADBSetupException("hadbmgmt-res.InternalError", "getConfigForCluster should have thrown an Exception!");
        
        AvailabilityService avail = config.getAvailabilityService();
        String dbName = avail.getHaStoreName();
        
        if(!StringUtils.ok(dbName))
            return null;
        
        return dbName;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setCreatePoolAttributes()
    {
        poolAttribs = new AttributeList();
        poolAttribs.add(new Attribute("name",									poolName));
        poolAttribs.add(new Attribute("fail_all_connections",					"false"));
        poolAttribs.add(new Attribute("is_connection_validation_required",		"true"));
        poolAttribs.add(new Attribute("idle_timeout_in_seconds",				"600"));
        poolAttribs.add(new Attribute("connection_validation_method",			"meta-data"));
        poolAttribs.add(new Attribute("transaction_isolation_level",			"repeatable-read"));
        poolAttribs.add(new Attribute("is_isolation_level_guaranteed",			"true"));
        poolAttribs.add(new Attribute("max_pool_size",							"16"));
        //poolAttribs.add(new Attribute("validation_table_name",				""));	// NOTHING HERE?!?!?
        poolAttribs.add(new Attribute("steady_pool_size",						"8"));
        poolAttribs.add(new Attribute("datasource_classname",					"com.sun.hadb.jdbc.ds.HadbDataSource"));
        poolAttribs.add(new Attribute("max_wait_time_in_millis",				"60000"));
        poolAttribs.add(new Attribute("pool_resize_quantity",					"2"));
        //poolAttribs.add(new Attribute("description",							""));	// NOTHING HERE?!?!?
        //poolAttribs.add(new Attribute("res_type",								""));	// NOTHING HERE?!?
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setCreatePoolProperties() throws HADBSetupException
    {
        poolProps = new Properties();
        poolProps.setProperty("User",								info.getDatabaseUser());
        poolProps.setProperty("Password",							info.getDatabasePassword());
        poolProps.setProperty("cacheDatabaseMetaData",				"false");
        poolProps.setProperty("eliminateRedundantEndTransaction",	"true");
        poolProps.setProperty("serverList",							info.getHostsAndPorts());
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void setCreateJdbcResourceAttributes()
    {
        jdbcResourceAttribs = new AttributeList();
        jdbcResourceAttribs.add(new Attribute("jndi_name",		jndiName));
        jdbcResourceAttribs.add(new Attribute("pool_name",		poolName));
        jdbcResourceAttribs.add(new Attribute("enabled",		"true"));
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void invokeCreatePool() throws HADBSetupException
    {
        Object[]	params		= new Object[] { poolAttribs, poolProps, target };
        String[]	signature	= new String[] { AttributeList.class.getName(),
        Properties.class.getName(), String.class.getName()};
        
        try
        {
            info.getMBeanServer().invoke(new ObjectName(POOL_OBJECT_NAME), POOL_CREATE_OPERATION_NAME, params, signature);
        }
        catch (Exception e)
        {
            if(info.getDBPreExists())
                LoggerHelper.warning("hadbmgmt-res.ResourceExists", "jdbc connection pool");
            else
                throw new HADBSetupException(e);
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void invokeDeletePool() throws HADBSetupException
    {
        // the middle param is "cascade" -- setting it to true will also
        // delete jdbc-resource 's that reference it.
        // fixme tbd NOTE:  resource-refs to the pool itself do NOT get removed - though they should be!
        Object[]	params		= new Object[] { poolName, Boolean.TRUE, target };
        Object[]	paramsNoRef	= new Object[] { poolName, Boolean.TRUE, "domain" };
        String[]	signature	= new String[] { String.class.getName(),
        Boolean.class.getName(), String.class.getName()};
        
        try
        {
            info.getMBeanServer().invoke(new ObjectName(POOL_OBJECT_NAME), POOL_DELETE_OPERATION_NAME, params, signature);
        }
        catch (Exception e)
        {
            // perhaps the cluster-config is already deleted?
            try
            {
                info.getMBeanServer().invoke(new ObjectName(POOL_OBJECT_NAME), POOL_DELETE_OPERATION_NAME, paramsNoRef, signature);
            }
            catch (Exception ee)
            {
                throw new HADBSetupException(ee);
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void invokeCreateJdbcResource() throws HADBSetupException
    {
        Object[]	params		= new Object[] { jdbcResourceAttribs, null, target };
        String[]	signature	= new String[] { AttributeList.class.getName(),
        Properties.class.getName(), String.class.getName()};
        
        try
        {
            info.getMBeanServer().invoke(new ObjectName(JDBC_RESOURCE_OBJECT_NAME),
                JDBC_CREATE_RESOURCE_OPERATION_NAME, params, signature);
        }
        catch (Exception e)
        {
            if(info.getDBPreExists())
                LoggerHelper.warning("hadbmgmt-res.ResourceExists", "jdbc resource");
            else
                throw new HADBSetupException(e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void invokeDeleteJdbcResource() throws HADBSetupException
    {
        Object[]	params		= new Object[] { jndiName, target };
        String[]	signature	= new String[] { String.class.getName(), String.class.getName()};
        
        try
        {
            info.getMBeanServer().invoke(new ObjectName(JDBC_RESOURCE_OBJECT_NAME),
                JDBC_DELETE_RESOURCE_OPERATION_NAME, params, signature);
        }
        catch (Exception e)
        {
            throw new HADBSetupException(e);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void removePropsFromConfig(AvailabilityService avail) throws ConfigException, HADBSetupException
    {
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private void addPropsToConfig(AvailabilityService avail) throws ConfigException, HADBSetupException
    {
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private					HADBInfo		info;
    private					String			jndiName;
    private					String			target;
    private static final	String			POOL_OBJECT_NAME					= "com.sun.appserv:type=resources,category=config";
    private static final	String			POOL_CREATE_OPERATION_NAME			= "createJdbcConnectionPool";
    private static final	String			POOL_DELETE_OPERATION_NAME			= "deleteJdbcConnectionPool";
    private					AttributeList	poolAttribs;
    private					Properties		poolProps;
    private					String			poolName;
    private					AttributeList	jdbcResourceAttribs;
    private static final	String			JDBC_RESOURCE_OBJECT_NAME			= POOL_OBJECT_NAME;	// same thing!
    private static final	String			JDBC_CREATE_RESOURCE_OPERATION_NAME	= "createJdbcResource";
    private static final	String			JDBC_DELETE_RESOURCE_OPERATION_NAME	= "deleteJdbcResource";
    private static final	String			DEFAULT_JNDI_NAME					= "jdbc/hastore";
}

/***** new JMS stuff
 * > sun-domain_1_1.dtd DTD changes for realizing the above changes:
 * > $ cvs -d $REDCVSROOT diff -uw sun-domain_1_1.dtd
 * > Index: sun-domain_1_1.dtd
 * > ===================================================================
 * > RCS file: /m/jws/admin-core/config-api/dtds/sun-domain_1_1.dtd,v
 * > retrieving revision 1.19
 * > diff -u -w -r1.19 sun-domain_1_1.dtd
 * > --- sun-domain_1_1.dtd  28 Oct 2004 22:53:14 -0000      1.19
 * > +++ sun-domain_1_1.dtd  19 Apr 2005 12:21:05 -0000
 * > @@ -1659,7 +1659,7 @@
 * >
 * >  <!ATTLIST jms-service
 * >      init-timeout-in-seconds CDATA "60"
 * > -    type (LOCAL | REMOTE) "LOCAL"
 * > +    type (LOCAL | EMBEDDED | REMOTE) "EMBEDDED"
 * >      start-args CDATA #IMPLIED
 * >      default-jms-host CDATA #IMPLIED
 * >      reconnect-interval-in-seconds CDATA "60"
 * > @@ -2180,7 +2180,8 @@
 * >      config
 * >  -->
 * >  <!ELEMENT availability-service
 * > -    (web-container-availability?, ejb-container-availability?, property*)>
 * > +    (web-container-availability?, ejb-container-availability?,
 * > +     jms-availability, property*)>
 * >
 * >
 * >  <!ATTLIST availability-service
 * > @@ -2287,6 +2288,35 @@
 * >      sfsb-store-pool-name CDATA #IMPLIED>
 * >
 * >
 * > +<!-- jms-availability
 * > +
 * >
 * > +  attributes
 * > +    availability-enabled
 * > +        This boolean flag controls whether the MQ cluster associated
 * > +        with the application server cluster is HA enabled or not. If this
 * > +        attribute is "false", then the MQ cluster pointed to by the
 * > +        jms-service element is considered non-HA. JMS Messages are not
 * > +        persisted to a highly available store. If this attribute is "true"
 * > +        the MQ cluster pointed to by the jms-service element is a HA cluster
 * > +        and the MQ cluster uses the database pointed to by mq-store-pool-name
 * > +        to save persistent JMS messages and other broker cluster configuration
 * > +        information. Individual applications will not be able to
 * > +        control or override MQ cluster availability levels. They inherit
 * > +        the availability attribute defined in this element.
 * > +        If this attribute is missing, availability is turned off by default [
 * > +        i.e. the MQ cluster associated with the AS cluster would behave as
 * > +        a non-HA cluster]
 * > +    mq-store-pool-name
 * > +        This is the jndi-name for the JDBC Connection Pool used by
 * > +        the MQ broker cluster for use in saving persistent JMS
 * > +        messages and other broker cluster configuration information.
 * > +        It will default to value of store-pool-name under
 * > +        availability-service (ultimately "jdbc/hastore").
 * > +-->
 * > +<!ELEMENT jms-availability (property*)>
 * > +<!ATTLIST jms-availability  availability-enabled %boolean; "false"
 * > +    mq-store-pool-name CDATA #IMPLIED>
 * > +
 * >  <!-- jdbc-connection-pool
 * >      jdbc-connection-pool defines configuration used to create and
 * >      manage a pool physical database connections. Pool definition is
 *
 */




