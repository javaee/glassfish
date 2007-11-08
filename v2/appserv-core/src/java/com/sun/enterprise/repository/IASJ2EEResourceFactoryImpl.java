
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
package com.sun.enterprise.repository;

import java.util.*;
import java.io.*;
import com.sun.enterprise.ServerConfiguration;
import com.sun.enterprise.util.FileUtil;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.ElementProperty;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

/**
 * Implementation of J2EEResourceFactory.  Reads and writes
 * XML Resource properties and converts them to J2EEResources.
 * IASRI #4626188
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 * @author Sridatta Viswanath
 */
public class IASJ2EEResourceFactoryImpl implements J2EEResourceFactory {

// IASRI 4660742 START
    private static final Logger _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);

// IASRI 4660742 END
    static final String JMS_QUEUE = "javax.jms.Queue";
    static final String JMS_TOPIC = "javax.jms.Topic";
    static final String JMS_QUEUE_CONNECTION_FACTORY = "javax.jms.QueueConnectionFactory";
    static final String JMS_TOPIC_CONNECTION_FACTORY = "javax.jms.TopicConnectionFactory";

    // START OF IASRI 4693548 - resource enabled flag needs to be checked
    /** generic resource type for jndi */
    public static final String CUSTOM_RES_TYPE = "custom-resource";

    /** resource type residing in an external JNDI repository */
    public static final String EXT_JNDI_RES_TYPE = "external-jndi-resource";

    /** JDBC javax.sql.(XA)DataSource resource type */
    public static final String JDBC_RES_TYPE = "jdbc-resource";

    /** java mail resource type */
    public static final String MAIL_RES_TYPE = "mail-resource";

    /** jms resource type */
    public static final String JMS_RES_TYPE = "jms-resource";

    /** persistence manager runtime configuration resource type */
    public static final String PMF_RES_TYPE="persistence-manager-factory-resource";
    /** jdbc connection pool resource type */
    public static final String JDBC_CONN_POOL_RES_TYPE = "jdbc-connection-pool";
    /** Connector resource type */
    public static final String CONNECTOR_CONN_POOL_TYPE = "connector-connection-pool";
    // END OF IASRI 4693548


    private ConfigContext ctx = null;
    private Resources rBean = null;
    
    private String serverName = null;
    private volatile J2EEResourceCollection  resources = null;    

    public J2EEResourceCollection loadDefaultResourceCollection()
        throws J2EEResourceException {        
        if (resources != null) {
            return resources;
        }

        return loadResourceCollection(null);
    }

    public synchronized J2EEResourceCollection
    loadResourceCollection(String resourcesURL)
        throws J2EEResourceException {

        resources = new J2EEResourceCollectionImpl();

           ServerContext sc = ApplicationServer.getServerContext();
           if(sc == null) {
//IASRI 4660742               System.out.println("Not Running in server. Returning empty resources...");
// START OF IASRI 4660742
	_logger.log(Level.FINE,"Not Running in server. Returning empty resources...");
// END OF IASRI 4660742
               return resources;
           } else {
                // name of this server instance
                serverName = sc.getInstanceName();
           }

           try {
               ctx = sc.getConfigContext();
               //ROB: config changes
               //rBean = ServerBeansFactory.getServerBean(ctx).getResources();
               rBean = ServerBeansFactory.getDomainBean(ctx).getResources();
               //RAMAKANTH
               if (rBean == null)
               {
                   rBean = new Resources();
               }
           } catch (ConfigException ce) {
/** IASRI 4660742
               ce.printStackTrace();
               System.out.println("Error initializing context");
               System.out.println("Returning empty resources...");
*/
// START OF IASRI 4660742
		 _logger.log(Level.SEVERE,"enterprise.empty_resource",ce);
		_logger.log(Level.FINE,"Error initializing context. Returning empty resources...");
// END OF IASRI 4660742
               return resources;
           }

        try {
            _logger.log(Level.FINE,"Loading PMF Resource==========");
            loadPMFResource(resources);
            _logger.log(Level.FINE,"Loading Custom Resource==========");
            loadCustomResource(resources);
            _logger.log(Level.FINE,"Loading External Jndi Resource==========");
            loadExternalJndiResource(resources);
// START OF IASRI 4650786
            _logger.log(Level.FINE,"Loading Mail Resource==========");
            loadMailResource(resources);
// END OF IASRI 4650786
// END OF IASRI 4660742
        } catch (ConfigException ce) {
// IASRI 4660742            ce.printStackTrace();
// IASRI 4660742            System.out.println("Error Loading resources: "+ce.getMessage());
// START OF IASRI 4660742
		 _logger.log(Level.SEVERE,"enterprise.config_exception",ce);
		_logger.log(Level.FINE,"Error Loading resources: "+ce.getMessage());
// END OF IASRI 4660742
            throw new J2EEResourceException(ce);
        }
        return resources;

    }

    public void storeDefaultResourceCollection(J2EEResourceCollection resources)
        throws J2EEResourceException {
        storeResourceCollection(resources, null);
    }

    public void storeResourceCollection(J2EEResourceCollection resources, String resourcesURL)
    throws J2EEResourceException {
 /*               ServerContext sc = ApplicationServer.getServerContext();

        if(sc == null) {
            throw new J2EEResourceException("Server Not Initialized");
        }

        try {
            ctx = sc.getConfigContext();
            rBean = ConfigBean.getServerObject(ctx).getResources();
        } catch (ConfigException ce) {
// IASRI 4660742            ce.printStackTrace();
// IASRI 4660742            System.out.println("Error initializing context");
// START OF IASRI 4660742
		 _logger.log(Level.SEVERE,"enterprise.config_exception",ce);
		_logger.log(Level.FINE,"Error initializing context ");
// END OF IASRI 4660742
            throw new J2EEResourceException(ce);
        }

        try {
            rBean.removeAllChildObjects();

// IASRI 4660742            System.out.println("Saving Jdbc Connection Pool==========");
// IASRI 4660742            saveJdbcConnectionPool(resources);
// IASRI 4660742            System.out.println("Saving Jdbc Resource==========");
// IASRI 4660742            saveJdbcResource(resources);
// IASRI 4660742            System.out.println("Saving Jms Resource==========");
// IASRI 4660742            saveJmsResource(resources);
// IASRI 4660742            System.out.println("Saving PMF Resource==========");
// IASRI 4660742            savePMFResource(resources);
// IASRI 4660742            System.out.println("Saving Custom Resource==========");
// IASRI 4660742            saveCustomResource(resources);
// IASRI 4660742            System.out.println("Saving External Jndi Resource==========");
// START OF IASRI 4660742
            _logger.log(Level.FINE,"Saving Jdbc Connection Pool==========");
            saveJdbcConnectionPool(resources);
            _logger.log(Level.FINE,"Saving Jdbc Resource==========");
            saveJdbcResource(resources);
            _logger.log(Level.FINE,"Saving Jms Resource==========");
            saveJmsResource(resources);
            _logger.log(Level.FINE,"Saving PMF Resource==========");
            savePMFResource(resources);
            _logger.log(Level.FINE,"Saving Custom Resource==========");
            saveCustomResource(resources);
            _logger.log(Level.FINE,"Saving External Jndi Resource==========");
// END OF IASRI 4660742
            saveExternalJndiResource(resources);
// START OF IASRI 4650786
            _logger.log(Level.FINE,"Saving Mail Resource==========");
            saveMailResource(resources);
// END OF IASRI 4650786
            rBean.save();
        } catch (ConfigException ce) {
//IASRI 4660742            ce.printStackTrace();
//IASRI 4660742            System.out.println("Error Loading resources");
// START OF IASRI 4660742
		 _logger.log(Level.SEVERE,"enterprise.config_exception",ce);
		_logger.log(Level.FINE,"Error Loading resources ");
// END OF IASRI 4660742
            throw new J2EEResourceException(ce);
        }
  */
    }

    public J2EEResource createResource(int type, String name) {

        J2EEResource resource = null;

        switch(type) {
            case J2EEResource.PMF_RESOURCE :
                resource = new PMFResource(name);
                break;
            case J2EEResource.CUSTOM_RESOURCE :
                resource = new CustomResource(name);
                break;
            case J2EEResource.EXTERNAL_JNDI_RESOURCE :
                resource = new ExternalJndiResource(name);
                break;
// START OF IASRI 4650786
            case J2EEResource.MAIL_RESOURCE :
                resource = new MailResource(name);
                break;
// END OF IASRI 4650786
            default :
                throw new java.lang.IllegalArgumentException();
        }

        return resource;
    }

    public ResourceProperty createProperty(String name) {
        return new ResourcePropertyImpl(name);
    }

   /**
     * Returns true if the given resource is referenced by this server. 
     *
     * @param   resourceName   name (jndi) of the resource
     * @return  true if the named resource is used/referred by this server
     *
     * @throws  ConfigException  if an error while parsing domain.xml
     */
    private boolean isReferenced(String resourceName) throws ConfigException {
        return ServerHelper.serverReferencesResource(ctx, serverName, resourceName);
    }       

    /**
     * Loads all the available custom resources into the resource collection.
     *
     * @param    resources    j2ee resource collection
     * @throws   ConfigException  if an error while loading data into beans
     */
    private void loadCustomResource(J2EEResourceCollection resources)
            throws ConfigException {

        com.sun.enterprise.config.serverbeans.CustomResource[] jBeanSet =
            rBean.getCustomResource();

        if (jBeanSet == null) {
            return;
        }

        for (int i=0; i < jBeanSet.length; i++) {
            com.sun.enterprise.config.serverbeans.CustomResource next =
                (com.sun.enterprise.config.serverbeans.CustomResource) jBeanSet[i];

            // custom resource is not referenced by this server
            if ( !isReferenced(next.getJndiName()) ) {
                _logger.fine("Skipping Custom Resource:" + next.getJndiName() 
                            + ". It is not used by server: " + serverName);
                continue;
            }           

			// Start Bug Fix: 4693395
            if (!next.isEnabled()) {
				logMessageIfNotEnabled(next.getJndiName(), CUSTOM_RES_TYPE);
				continue;
			}
			// End Bug Fix: 4693395

            // convets the config bean to j2ee resource
            J2EEResource jr = toCustomJ2EEResource(next);

            resources.addResource(jr);
        }
    }

    /**
     * Returns a new instance of j2ee custom resource from the given
     * config bean.
     *
     * This method gets called from the custom resource deployer
     * to convert custom-resource config bean into custom j2ee resource.
     *
     * @param    rbean   custom-resource config bean
     *
     * @return   new instance of j2ee custom resource
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    public static J2EEResource toCustomJ2EEResource(
            com.sun.enterprise.config.serverbeans.CustomResource rbean)
            throws ConfigException {

        CustomResource jr = new CustomResource( rbean.getJndiName() );

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error

        // sets the enable flag
        jr.setEnabled( rbean.isEnabled() );

        // sets the resource type
        jr.setResType( rbean.getResType() );

        // sets the factory class name
        jr.setFactoryClass( rbean.getFactoryClass() );

        // sets the properties
        ElementProperty[] s = rbean.getElementProperty();
        if (s!= null) {
            for(int j = 0; j <s.length; j++) {

                com.sun.enterprise.config.serverbeans.ElementProperty next =
                    (com.sun.enterprise.config.serverbeans.ElementProperty) s[j];

                ResourceProperty rp =
                    new ResourcePropertyImpl(next.getName(), next.getValue());

                jr.addProperty(rp);
            }
        }

        return jr;
    }

    /**
     * Returns a new instance of j2ee external jndi resource from the given
     * config bean.
     *
     * This method gets called from the external resource
     * deployer to convert external-jndi-resource config bean into
     * external-jndi  j2ee resource.
     *
     * @param    rbean    external-jndi-resource config bean
     *
     * @return   a new instance of j2ee external jndi resource
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    public static J2EEResource toExternalJndiJ2EEResource(
            com.sun.enterprise.config.serverbeans.ExternalJndiResource rbean)
            throws ConfigException {

        ExternalJndiResource jr = new ExternalJndiResource(rbean.getJndiName());

        //jr.setDescription( rbean.getDescription() ); // FIXME: getting error

        // sets the enable flag
        jr.setEnabled( rbean.isEnabled() );

        // sets the jndi look up name
        jr.setJndiLookupName( rbean.getJndiLookupName() );

        // sets the resource type
        jr.setResType( rbean.getResType() );

        // sets the factory class name
        jr.setFactoryClass( rbean.getFactoryClass() );

        // sets the properties
        ElementProperty[] s = rbean.getElementProperty();
        if (s!= null) {
            for (int j=0; j<s.length; j++) {
                com.sun.enterprise.config.serverbeans.ElementProperty next =
                    (com.sun.enterprise.config.serverbeans.ElementProperty) s[j];

                ResourceProperty rp =
                    new ResourcePropertyImpl(next.getName(), next.getValue());
                jr.addProperty(rp);
            }
        }

        return jr;
    }

    /**
     * Loads all available external jndi resources into the given resource
     * collection.
     *
     * @param    resources    j2ee resource collection
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    private void loadExternalJndiResource(J2EEResourceCollection resources)
            throws ConfigException {

        com.sun.enterprise.config.serverbeans.ExternalJndiResource[] jBeanSet =
            rBean.getExternalJndiResource();

        if (jBeanSet == null) {
            return;
        }

        for (int i=0; i<jBeanSet.length; i++) {
            com.sun.enterprise.config.serverbeans.ExternalJndiResource next =
                (com.sun.enterprise.config.serverbeans.ExternalJndiResource) jBeanSet[i];
            
            // external jndi resource is not referenced by this server
            if ( !isReferenced(next.getJndiName()) ) {
                _logger.fine("Skipping External JNDI Resource:" 
                            + next.getJndiName() 
                            + ". It is not used by server: " + serverName);
                continue;
            }

			// Start Bug Fix: 4693395
            if (!next.isEnabled()) {
				logMessageIfNotEnabled(next.getJndiName(), EXT_JNDI_RES_TYPE);
				continue;
			}
			// End Bug Fix: 4693395

            // convets the config bean to j2ee resource
            J2EEResource jr = toExternalJndiJ2EEResource(next);

            resources.addResource(jr);
        }
    }

    private void loadPMFResource(J2EEResourceCollection resources) throws ConfigException {
        com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource[] jBeanSet = rBean.getPersistenceManagerFactoryResource();
        if(jBeanSet == null) return;

        for(int i = 0; i <jBeanSet.length; i++) {
            com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource next =
            (com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource) jBeanSet[i];
            
            // PMF resource is not referenced by this server
            if ( !isReferenced(next.getJndiName()) ) {
                _logger.fine("Skipping PMF Resource:" + next.getJndiName() 
                            + ". It is not used by server: " + serverName);
                continue;
            }            

			// Start Bug Fix: 4693395
            if (!next.isEnabled()) {
				logMessageIfNotEnabled(next.getJndiName(), PMF_RES_TYPE);
				continue;
			}
			// End Bug Fix: 4693395

            resources.addResource(toPMFJ2EEResource(next));
        }
    }

    /**
     * Returns a new instance of j2ee pmf resource from the given config bean.
     *
     * This method gets called from the Persistence Manager Factory Resource
     * deployer to convert persistence-manager-resource-factory config bean into
     * pmf j2ee resource.
     *
     * @param rbean persistence-manager-resource-factory config bean
     *
     * @return a new instance of j2ee pmf resource
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    public static J2EEResource toPMFJ2EEResource(
            com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource
            rbean) throws ConfigException {
        PMFResource jr = new PMFResource(rbean.getJndiName());
        jr.setEnabled(rbean.isEnabled());
        jr.setFactoryClass(rbean.getFactoryClass());
        jr.setJdbcResourceJndiName(rbean.getJdbcResourceJndiName());

        ElementProperty[] s = rbean.getElementProperty();
        if (s!= null) {
            for (int j = 0; j <s.length; j++) {
                com.sun.enterprise.config.serverbeans.ElementProperty next1 =
                (com.sun.enterprise.config.serverbeans.ElementProperty) s[j];
                ResourceProperty rp = new ResourcePropertyImpl(next1.getName(),
                        next1.getValue());
                jr.addProperty(rp);
            }
        }
        //jr.setDescription(next.getDescription()); // FIXME add this

        return jr;
    }

// START OF IASRI 4650786
    /**
     * Returns a new instance of j2ee mail resource from the given config bean.
     *
     * This method gets called from the mail resource deployer to convert mail
     * config bean into mail j2ee resource.
     *
     * @param    rbean    mail-resource config bean
     *
     * @return   a new instance of j2ee mail resource
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    public static J2EEResource toMailJ2EEResource(
        com.sun.enterprise.config.serverbeans.MailResource rbean)
        throws ConfigException {

        MailResource jr = new MailResource(rbean.getJndiName());

        //jr.setDescription(rbean.getDescription()); // FIXME: getting error
        jr.setEnabled(rbean.isEnabled());
        jr.setStoreProtocol(rbean.getStoreProtocol());
        jr.setStoreProtocolClass(rbean.getStoreProtocolClass());
        jr.setTransportProtocol(rbean.getTransportProtocol());
        jr.setTransportProtocolClass(rbean.getTransportProtocolClass());
        jr.setMailHost(rbean.getHost());
        jr.setUsername(rbean.getUser());
        jr.setMailFrom(rbean.getFrom());
        jr.setDebug(rbean.isDebug());

        // sets the properties
        ElementProperty[] s = rbean.getElementProperty();
        if (s != null) {
            for (int j = 0; j < s.length; j++) {
                com.sun.enterprise.config.serverbeans.ElementProperty next =
                    (com.sun.enterprise.config.serverbeans.ElementProperty)s[j];

                ResourceProperty rp =
                    new ResourcePropertyImpl(next.getName(), next.getValue());
                jr.addProperty(rp);
            }
        }

        return jr;
    }

    /**
     * Loads all available mail resources into the given resource collection.
     *
     * @param    resources    j2ee resource collection
     *
     * @throws   ConfigException  if an error while loading data into beans
     */
    private void loadMailResource(J2EEResourceCollection resources)
        throws ConfigException {

        com.sun.enterprise.config.serverbeans.MailResource[] jBeanSet =
            rBean.getMailResource();

        if (jBeanSet == null) {
            return;
        }

        for (int i = 0; i < jBeanSet.length; i++) {
            com.sun.enterprise.config.serverbeans.MailResource next =
                (com.sun.enterprise.config.serverbeans.MailResource)jBeanSet[i];
            
            // mail resource is not referenced by this server
            if ( !isReferenced(next.getJndiName()) ) {
                _logger.fine("Skipping Mail Resource:" + next.getJndiName() 
                            + ". It is not used by server: " + serverName);
                continue;
            }            

			// Start Bug Fix: 4693395
            if (!next.isEnabled()) {
				logMessageIfNotEnabled(next.getJndiName(), MAIL_RES_TYPE);
				continue;
			}
			// End Bug Fix: 4693395

            // convets the config bean to j2ee resource
            J2EEResource jr = toMailJ2EEResource(next);

            resources.addResource(jr);
        }
    }

    // Start Bug Fix: 4693395
    private void logMessageIfNotEnabled(String name, String type) {
		_logger.log(Level.INFO, "enterprise.resource_disabled", new Object[] {name, type});
	}
	// End Bug Fix: 4693395

}
