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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

import com.sun.mfwk.MfObjectFactory;
import com.sun.mfwk.agent.appserv.lifecycle.ListenerImpl;
import com.sun.mfwk.agent.appserv.lifecycle.ListenerManager;
import com.sun.mfwk.agent.appserv.lifecycle.ListenerManagerFactory;
import com.sun.mfwk.agent.appserv.modeler.ModelerContext;
import com.sun.mfwk.agent.appserv.modeler.MBeanModeler;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryServiceFactory;
import com.sun.mfwk.agent.appserv.relation.RelationModeler;
import com.sun.mfwk.agent.appserv.relation.RelationMappingServiceFactory;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.discovery.ASMBeanDiscoveryServiceFactory;
import com.sun.mfwk.agent.appserv.discovery.ASMBeanDiscoveryService;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryService;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryServiceFactory;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.relations.RelationServiceImpl;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.MfAgentNode;
import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.MfObjectFactory;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.agent.appserv.util.Utils;

import java.io.IOException;
import javax.management.MalformedObjectNameException;
import com.sun.mfwk.agent.appserv.mapping.AS_ObjectNameNotFoundException;
import com.sun.mfwk.agent.appserv.util.Utils;

/**
 * Mediator class that instruments CMM mbeans for a server instance
 * runtime services.
 */
public class ASServerManager {
    
    /**
     * Creates a new instance of ASServermanager
     *
     * @param  serverName  name of the server
     * @param  domainName  name of the domain
     */
    public ASServerManager(String serverName, String domainName) {
        this(Constants.DEF_MODULE_NAME, serverName, domainName);
    }
    
    /**
     * Creates a new instance of ASServermanager
     *
     * @param  moduleName  name of the module: com.sun.cmm.as
     * @param  serverName  name of the server
     * @param  domainName  name of the application server domain
     */
    public ASServerManager(String moduleName, String serverName,
            String domainName) {
        
        _serverName  = serverName;
        _domainName  = domainName;
        _context     = new ModelerContext(serverName, domainName);
        _context.setModuleName(moduleName);
        _mBeanModeler = new MBeanModeler(_context);
        _relationModeler = new RelationModeler(_context);
    }
    
    /**
     * Adds the key-val pair as a token. During the processing of the
     * declarative template, if any tokens are found that matches the
     * key, it will be replaced with the given val. Tokens are
     * expected to be expressed as ${key} in the template.
     *
     * @param  key  token key
     * @param  val  token value
     */
    public void addToken(String key, String val) {
        _context.addToken(key, val);
    }
    
    /**
     * creates cmm mbeans and builds relations.
     *
     * @throws Exception if a problem occurred
     */
    public void load() throws Exception {
        loadMBeans();
        loadRelations();
    }
    
    /**
     * creates cmm mbeans and builds relations for the given set.
     *
     * @param  mbeans  object name of application server mbeans
     *
     * @throws Exception if any problem occurred
     */
    public void load(Set mbeans) throws Exception {
        loadMBeans(mbeans);
        loadRelations(mbeans);
    }

    /**
     * creates cmm mbeans and builds relations for the given AS mbean.
     *
     * @param  asObjectName object name of application server mbean
     *
     * @throws Exception if any problem occurred
     */
    public void load(ObjectName asObjectName) throws Exception {
        loadMBeans(asObjectName);
        loadRelations(asObjectName);
    }
    
    private void loadMBeans() throws Exception {
        //discover and instrument all appserver mbeans
        loadMBeans(discoverASMonitorMBeans());
    }
    
    /**
     * creates cmm mbeans.
     *
     * @param  mbeans  object name of application server mbeans
     */
    public void loadMBeans(Set mbeans)
    throws IOException, MalformedObjectNameException {
        
        if (mbeans == null) {
            throw new IllegalArgumentException();
        }
        
        MBeanModeler mm = new MBeanModeler(_context);
        
        // mapping service
        MappingQueryServiceFactory mqsf =
                MappingQueryServiceFactory.getInstance();
        MappingQueryService queryService =
                mqsf.getMappingQueryService(_serverName, _domainName);
        
        // instrument mbeans
        Iterator iter = mbeans.iterator();
        while (iter.hasNext()) {
            ObjectName on = (ObjectName) iter.next();
            try {
                mm.model(on, queryService);
            } catch (AS_ObjectNameNotFoundException onfe) {
                 Utils.log(Level.INFO, "No mbean mapping found for: " + on);
                //"No mbean mapping found for: " + on, onfe);
            } catch (Exception e) {
                Utils.log(Level.INFO, "Error while instrumenting mbean: " + on.getCanonicalName(), e);
            }
        }
    }
    
    
    /**
     * creates cmm mbeans.
     *
     * @param  asObjectName object name of application server mbean
     */
    public void loadMBeans(ObjectName asObjectName)
    throws IOException, MalformedObjectNameException {
        if(_queryService == null) {
            initQueryService();
        }

        if(_queryService != null) {
            // instrument mbeans
            try {
                _mBeanModeler.model(asObjectName, _queryService);
            } catch (AS_ObjectNameNotFoundException onfe) {
                Utils.log(Level.INFO, "No mbean mapping found for: " + asObjectName);
                //"No mbean mapping found for: " + asObjectName, onfe);
            } catch (Exception e) {
                Utils.log(Level.INFO, "Error while instrumenting mbean: " + asObjectName.getCanonicalName(), e);
            }
        } else {
            Utils.log(Level.WARNING, "Not able to get hold of Mapping Query Service");
        }
    }
   
 
    /**
     * builds relations for all the cmm mbeans.
     *
     */
    private void loadRelations() throws Exception {
        // instrument relations
        RelationModeler rm = new RelationModeler(_context);
        rm.load(_serverName);
    }
    
    /**
     * For the given AS mbeans, define relations between the corresponding CMM mbeans.
     */
    private void loadRelations(Set mbeans) throws Exception {
        
        RelationModeler rm = new RelationModeler(_context);
        
        MappingQueryServiceFactory fac =
                MappingQueryServiceFactory.getInstance();
        
        MappingQueryService mqs =
                fac.getMappingQueryService(_serverName, _domainName);
        
        Set CMM_Mbeans = new HashSet();
        Iterator iter = mbeans.iterator();
       
        ObjectName asObjectName = null; 
        for(;iter.hasNext();) {
            asObjectName = (ObjectName)iter.next();
            NodeList nodes = mqs.getCMM_Mbeans(asObjectName.toString());
            String cmmNameTemplate = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                cmmNameTemplate = mqs.getCMM_ObjectName(elem);
                String cmmName =
                    ObjectNameHelper.tokenizeON(asObjectName, cmmNameTemplate, _context.getTokens());

                // discover cmm mbean
                CMMMBeanDiscoveryServiceFactory factory =
                    CMMMBeanDiscoveryServiceFactory.getInstance();

                CMMMBeanDiscoveryService dis = factory.getCMMMBeanDiscoveryService();
                ObjectName cmmObjectName = dis.discoverCMMMBean(cmmName);
                if(cmmObjectName != null) {
                    CMM_Mbeans.add(cmmObjectName);
                }
            }
        }

        rm.load(CMM_Mbeans);
    }

    
    /**
     * For the given AS mbean, define relations between the corresponding CMM mbeans.
     */
    private void loadRelations(ObjectName asObjectName) throws Exception {
   
        if(_queryService == null) {
            initQueryService();
        }

        if(_queryService != null) {
            Set CMM_Mbeans = new HashSet();
       
            NodeList nodes = _queryService.getCMM_Mbeans(asObjectName.toString());
            String cmmNameTemplate = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                cmmNameTemplate = _queryService.getCMM_ObjectName(elem);
                String cmmName =
                    ObjectNameHelper.tokenizeON(asObjectName, cmmNameTemplate, _context.getTokens());

                // discover cmm mbean
                CMMMBeanDiscoveryServiceFactory factory =
                    CMMMBeanDiscoveryServiceFactory.getInstance();

                CMMMBeanDiscoveryService dis = factory.getCMMMBeanDiscoveryService();
                ObjectName cmmObjectName = dis.discoverCMMMBean(cmmName);
                if(cmmObjectName != null) {
                    CMM_Mbeans.add(cmmObjectName);
                }
            }

            _relationModeler.load(CMM_Mbeans);
        } else {
            Utils.log(Level.WARNING, "Not able to get hold of Mapping Query Service");
        }
    }

    
    /**
     * Removes the given cmm mbeans and corresponding artifacts
     * (relation, settings, etc.).
     *
     * @param  mbeans  object name of cmm mbeans
     *
     * @throws Exception if problem occurred while getting mf object factory
     */
    public void unload(Set mbeans) throws Exception {
        
        if (mbeans == null) {
            throw new IllegalArgumentException();
        }
        
        Iterator iterator = mbeans.iterator();
        ObjectName objectName = null;
        
        MfObjectFactory mfObjectFactory =
                MfObjectFactory.getObjectFactory(_context.getModuleName());
        
        // relation mapping service
        RelationServiceImpl relationService =
                RelationServiceImpl.getRelationService();
        
        while (iterator.hasNext()) {
            
            objectName = (ObjectName) iterator.next();
            
            // remove relation for this cmm mbean
            try {
                RelationModeler rm = new RelationModeler(_context);
                rm.removeRelations(objectName.getCanonicalName());
            } catch (Exception e) {
                Utils.log(Level.INFO, "Error while deleting relation for: " + objectName, e);
            }
            
            try {
                // remove cmm mbean
                mfObjectFactory.deleteObject(objectName.toString());
            } catch (Exception exception) {
                Utils.log(Level.INFO, "Error while deleting mbean: " + objectName, exception);
            }
        }
    }
    
    /**
     * Starts the processing for a server
     *
     * @throws IOException if i/o problem occurred in the processing
     * @throws MalformedObjectNameException  if query pattern is malformed
     */
    public void start() throws Exception {
        try {
            // create domain cmm mbean
            String str = Constants.DEF_MODULE_NAME + ":type=CMM_J2eeDomain,name=" + _domainName;
            String mn = _context.getModuleName();
            MfObjectFactory fac = MfObjectFactory.getObjectFactory(mn);
            fac.createObject(str);
        } catch(Exception e) {
            Utils.log(Level.WARNING, "Error creating domain cmm mbean", e);
        }
        
        // instrument all app server mbeans and setup relations
        load();
        
        // register dynamic config listener
        ListenerManager listenerManager = ListenerManagerFactory.getListenerManager();
        try {
            ListenerImpl listener = new ListenerImpl(_serverName, _domainName);
            listenerManager.addNotificationListener(_serverName, _domainName,
                    listener, null, null);
        } catch(Exception ex) {
             Utils.log(Level.SEVERE, "Error while registering dynamic config listener", ex);
        }
        
        try {
            // create relation to the node agent objects (CMM_RunningOS)
            String mn = _context.getModuleName();
            MfObjectFactory fac = MfObjectFactory.getObjectFactory(mn);
            String tokenizedON =
                    ObjectNameHelper.tokenize(mn+JVM_PATTERN, _context.getTokens());
            CMM_MBean jvmMbean = fac.getObject(tokenizedON);
            if (jvmMbean != null) {
                MfAgentNode.createRelationsWithAgent(mn, jvmMbean);
            }
            
        } catch(Exception e) {
            Utils.log(Level.INFO, "Error while creating JVM relation with the node agent", e);
        }
        
    }
    
    /**
     * Stops a server by removing corresponding CMM mbeans.
     *
     * @throws IOException if i/o problem occurred in the processing
     * @throws MalformedObjectNameException  if query pattern is malformed
     */
    public void stop() throws IOException,
            MalformedObjectNameException, Exception {
        
        // discover all cmm mbeans
        CMMMBeanDiscoveryServiceFactory fac =
                CMMMBeanDiscoveryServiceFactory.getInstance();
        
        CMMMBeanDiscoveryService dis = fac.getCMMMBeanDiscoveryService();
        Set mbeans = dis.discoverCMMMBeans(_serverName, _domainName);
        
        // unload all cmm mbeans
        unload(mbeans);
        
        // remove connection
        ConnectionRegistry reg = ConnectionRegistry.getInstance();
        reg.removeConnection(_serverName, _domainName);
    }
    
    /**
     * Returns all available monitor mbeans in application server.
     * This also includes the runtime mbean for J2EEServer and
     * config mbean for Cluster. 
     *
     * @return  all monitoring mbeans
     * @throws IOException if i/o problem occurred in the processing
     * @throws MalformedObjectNameException  if query pattern is malformed
     */
    private Set discoverASMonitorMBeans() throws Exception {
        
        ConnectionRegistry registry = ConnectionRegistry.getInstance();
        MBeanServerConnection con =
                registry.getConnection(_serverName, _domainName);
        
        ASMBeanDiscoveryService discoveryService  =
                ASMBeanDiscoveryServiceFactory.getInstance().
                getASMBeanDiscoveryService(con);
        
        Set asMBeans =  discoveryService.discoverASMBeans(_serverName);

        
        //Get the config mbean for Cluster from the DAS Server instance
        //Ideally, we should instrument CMM object for Cluster on registration
        //event from DAS. Somehow, we are not getting an event from DAS for
        //Cluster mbean registration.
        MBeanServerConnection dasConn;
        try {
           dasConn = registry.getConnection(Constants.ADMIN_SERVER_NAME, _domainName);
        } catch (Exception e) {
           dasConn = null;
           Utils.log(Level.INFO, "Not able to get hold of DAS Connection. Not instrumenting Cluster objects");
        } 
        
        if(dasConn != null) {
           ASMBeanDiscoveryService dasDiscoveryService =
              ASMBeanDiscoveryServiceFactory.getInstance().
                 getASMBeanDiscoveryService(dasConn);

           asMBeans.addAll(dasDiscoveryService.discoverClusterMBeans());
        } 
        return asMBeans;  
    }
   
    private void initQueryService() throws IOException {
        // mapping service
        MappingQueryServiceFactory mqsf =
                MappingQueryServiceFactory.getInstance();
       
        _queryService =
                mqsf.getMappingQueryService(_serverName, _domainName);
    }

    // ---- VARIABLES - PRIVATE --------------------------------------------
    private ModelerContext _context           = null;
    private String _serverName                = null;
    private String _domainName                = null;
    private static final String JVM_PATTERN   =
            ":type=CMM_JVM,domain=${domain.name},server=${server.name},name=${server.name}";
    private MBeanModeler _mBeanModeler        = null;
    private RelationModeler _relationModeler  = null;  
    private MappingQueryService  _queryService	      = null;  
}
