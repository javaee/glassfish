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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.relation;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.io.IOException;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.relations.RelationServiceImpl;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.modeler.ModelerContext;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.relations.InvalidRelationIdException;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryServiceFactory;
import com.sun.mfwk.agent.appserv.discovery.CMMMBeanDiscoveryService;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.mfwk.agent.appserv.util.Utils;

/**
 * Instrument relations.
 */
public class RelationModeler {
    
    /**
     * Creates a new instance of relation modeler
     */
    public RelationModeler(ModelerContext ctx) {
        
        _context = ctx;
        
    }
    
    public ModelerContext getContext() {
        return _context;
    }
    
    /**
     * Instruments relations from the descriptor.
     *
     * @param  serverName server for which relations are to be instrumented
     * @throws Exception  if an error occurs
     */
    public void load(String serverName) throws Exception {
        
        CMMMBeanDiscoveryService cmmDiscoveryService =
                CMMMBeanDiscoveryServiceFactory.getInstance().
                getCMMMBeanDiscoveryService();
        
        String domainName = _context.getDomainName();
        Set cmmMBeans  =
                cmmDiscoveryService.discoverCMMMBeans(serverName, domainName);
        cmmMBeans.addAll(cmmDiscoveryService.discoverClusterCMMMBeans());
        cmmMBeans.addAll(cmmDiscoveryService.discoverInstalledProductCMMMBeans());
        load(cmmMBeans);
//        listRelations();

    }
    
    /**
     * Instruments relations for the given set of mbeans
     *
     * @param  cmmMBeans set of cmm mbeans for which relations are
     *                   to be instrumented
     * @throws Exception  if an error occurs
     */
    public void load(Set cmmMBeans) throws Exception {        
        Iterator iter = cmmMBeans.iterator();
        for (; iter.hasNext(); ) {
            load((ObjectName)iter.next());
        }
    }
    
    public void load(ObjectName cmmObjectName) throws Exception {
        RelationMappingServiceFactory fac =
                RelationMappingServiceFactory.getInstance();
        
        RelationMappingService relationService = fac.getRelationMappingService(
                _context.getServerName(), _context.getDomainName());
        
        Utils.log(Level.FINEST, "cmmObjectName = " + cmmObjectName);
        load(cmmObjectName, relationService.getDestinationRelations(cmmObjectName.toString()), DESTINATION, relationService);
        load(cmmObjectName, relationService.getSourceRelations(cmmObjectName.toString()), SOURCE, relationService);        
    }
    
    private void load(ObjectName cmmObjectName, Collection relations, String type, RelationMappingService relationService) throws Exception {        
        RelationFactory rf = new RelationFactory(_context);
        String cmmObjectNameStr = cmmObjectName.toString();
        
        Iterator iter = relations.iterator();
        for (; iter.hasNext(); ) {
            Element r = (Element)iter.next();
            try {
                String handler = relationService.getHandler(r);
                if ("".equals(handler))
                    handler = null;
                if (handler != null) {
                    callHandler(r, cmmObjectName, relationService);
                } else {
                    String source = null, dest = null;
                    if (DESTINATION.equals(type)) {
                        dest = cmmObjectNameStr;
                        String template = relationService.getSource(r);
                        try {
                            source = ObjectNameHelper.tokenizeON(cmmObjectName,
                                    template, _context.getTokens());
                            Utils.log(Level.FINEST, "IN DEST Template = " + template );
                            Utils.log(Level.FINEST, "IN DEST dest = " + dest);
                            
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
                    } else if (SOURCE.equals(type))  {
                        source = cmmObjectNameStr;
                        String template = relationService.getDestination(r);
                        try {
                            dest = ObjectNameHelper.tokenizeON(cmmObjectName,
                                    template, _context.getTokens());
                            Utils.log(Level.FINEST, "IN SOURCE Template = " + template );
                        } catch (NoSuchFieldException e) {
                            continue;
                        }
                    }
                    if (isProcessed(source, dest, relationService.getType(r)))
                        continue;
                    Utils.log(Level.FINE, "Creating relations for dest = " + dest + " source = " + source);
                    Relation relation =
                            rf.create(source, dest, relationService.getType(r),
                            relationService.getCreateAnyway(r));
                    // add the newly created relation
                    if (relation != null) {
                        RelationServiceImpl.getRelationService().
                                addRelation(relation);
                    }
                }
            } catch (Exception e) {
                LogDomains.getLogger().log(Level.SEVERE,
                        "Error while instrumenting relation", e);
            }
        }
    }
    
    private boolean isProcessed(String source, String dest, String type) {
        Relation[] relations = RelationServiceImpl.getRelationService().getRelations(source, type, true);
        for (int i = 0; i < relations.length; i++) {
            if (dest.equals(relations[i].getDestinationInstanceID()))
                return true;
        }
        return false;
    }
    
/*    public void listRelations() {
        RelationServiceImpl relationService =
                RelationServiceImpl.getRelationService();
        Relation[] activeRelations = relationService.listRelations();
        LogDomains.getLogger().severe("Listing Relations - numRelations = " +
                activeRelations.length);
        
        for (int i = 0; i < activeRelations.length; i++) {
            Relation relation = activeRelations[i];
            String source = relation.getSourceInstanceID();
            LogDomains.getLogger().severe(relation.getDestinationInstanceID() + "," +
                    relation.getSourceInstanceID());
        }
    }
  */  
    
    
    private void callHandler(Element elem, ObjectName ON,
            RelationMappingService relationService) {
        
        try {
            Class handlerClass=Class.forName(relationService.getHandler(elem));
            
            RelationMappingHandler handler =
                    (RelationMappingHandler)handlerClass.newInstance();
            
            handler.relationHandler(elem, ON, relationService, this);
            
        } catch (Exception ex) {
            throw new RuntimeException("Error calling user defined handler",ex);
        }
    }
    
    public void removeRelations(String CMM_MBean) {
        
        RelationServiceImpl relationService =
                RelationServiceImpl.getRelationService();
        Relation[] activeRelations = relationService.listRelations();

        for (int i = 0; i < activeRelations.length; i++) {
            Relation relation = activeRelations[i];
            try {
                if (ObjectName.getInstance(relation.getDestinationInstanceID()).equals(
                    ObjectName.getInstance(CMM_MBean)) || ObjectName.getInstance( 
                       relation.getSourceInstanceID()).equals(ObjectName.getInstance(
                          CMM_MBean))) {
                    try {
                        relationService.removeRelation(relation);
                    } catch (InvalidRelationIdException ex) {
                        LogDomains.getLogger().log(Level.SEVERE,
                                "Error removing relation", ex);
                    }
                } 
            } catch (MalformedObjectNameException ex) {
                LogDomains.getLogger().log(Level.SEVERE,
                        "Error forming ObjectName", ex); 
            }      
        }
    }

// ---- PRIVATE - VARIABLES -------------------------------------
    private ModelerContext _context;
    private String DESTINATION = "dest";
    private String SOURCE = "source";
    
}
