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
package com.sun.mfwk.agent.appserv.relation;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import com.sun.mfwk.agent.appserv.util.Utils;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;


class RelationMappingServiceImpl implements RelationMappingService {
    
    public RelationMappingServiceImpl(String server, String domain)
    throws IOException {
        
        this(Constants.DEF_RELATION, Constants.DEF_FILE_LOC, server, domain);
    }
    
    RelationMappingServiceImpl(String uri, String dLocation, String server,
            String domain) throws IOException {
        
        env = new HashMap();
        env.put(Constants.SERVER_NAME_PROP, server);
        env.put(Constants.DOMAIN_NAME_PROP, domain);
        
        
        this.doc = Utils.getDocument(uri, dLocation);
        if (this.doc == null) {
            throw new IOException("Unable to read relation xml file for uri :"
                    + uri + " location = " + dLocation);
        }
    }
    
    public Object getProperty(String key) {
        return env.get(key);
    }
    
    public NodeList getRelations() {
        if (relations == null) {
            relations= doc.getElementsByTagName(RELATION_DESCRIPTOR);
        }
        return relations;
        
    }
    
    public Collection getRelations(String CMM_ObjectName) {
        Set filteredList = filter(getRelations(), CMM_ObjectName, SOURCE);
        return filter(getRelations(),CMM_ObjectName,DESTINATION,filteredList);
    }
    
    public Collection getSourceRelations(String CMM_ObjectName) {
        return filter(getRelations(), CMM_ObjectName, SOURCE);
    }
    
    public Collection getDestinationRelations(String CMM_ObjectName) {
        return filter(getRelations(), CMM_ObjectName, DESTINATION);
    }
    
    
    public String getSource(Element relation) {
        return relation.getAttribute(SOURCE);
    }
    
    public String getDestination(Element relation) {
        return relation.getAttribute(DESTINATION);
    }
    
    public String getType(Element relation) {
        return relation.getAttribute(TYPE);
    }
    
    public String getHandler(Element relation) {
        return relation.getAttribute(HANDLER);
    }
    
    public boolean  getCreateAnyway(Element relation) {
        return Boolean.getBoolean(relation.getAttribute(CREATE_ANYWAY));
    }
    
    private Set  filter(NodeList nodes, String CMM_ObjectName,
            String attrName) {
        
        return filter(nodes, CMM_ObjectName, attrName, new HashSet());
    }
    
    private Set filter(NodeList nodes, String CMM_ObjectName,
            String attrName, Set filteredList) {
        
        String type = Utils.getStringRegion(CMM_ObjectName, "type=", ",");
        String primary = Utils.getStringRegion(CMM_ObjectName, "primary=", ",");
        String name = Utils.getStringRegion(CMM_ObjectName, "name=", ",");
        Map map = ObjectNameHelper.getKeysAndProperties(CMM_ObjectName);
        if (type != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Element elem = (Element)nodes.item(i);
                String xmlObjectName = elem.getAttribute(attrName);
                String xmlType =
                        Utils.getStringRegion(xmlObjectName, "type=", ",");
                
                if (type.equals(xmlType)) {
                    if ("CMM_Capabilities".equals(type)) {
                        String xmlPrimary = Utils.getStringRegion(xmlObjectName, "primary=", ",");
                        if (xmlPrimary == null || !xmlPrimary.equals(primary))
                            continue;
                    } else {
                         //TODO
                         //Special treatment in case of CMM_Service. Currently Http service, Transaction Service,
                         //Orb Service are all of type CMM_Service.  We need to exactly identify what the given
                         //CMM_Service object  represents. Should be removed once we have specialized objects
                         //such as CMM_HttpService, CMM_TransactionService etc available in MFWK 2.1 
                        if ("CMM_Service".equals(type)) {
                            String xmlName = Utils.getStringRegion(xmlObjectName, "name=", ",");
                            if (xmlName == null) continue;

                            if (("http-service".equals(name)) ||("transaction-service".equals(name))) {
                                if ( !xmlName.equals(name)) continue;
                            } else {
                                if (("http-service".equals(xmlName)) ||("transaction-service".equals(xmlName)))
                                    continue;  
                            }
                        }
                    }
                    if (ObjectNameHelper.keysMatch(map, xmlObjectName)) {
                        Utils.log(Level.FINEST, "cmmObjectName = " + CMM_ObjectName + " xmlObjectName = " + xmlObjectName +
                                " elem = " + elem.getAttribute(attrName) + " type = " + type + " attrName = " + attrName);
                        filteredList.add(elem);
                    }
                }
            }
        }
        return filteredList;
    }
    
    private Map env            = null;
    private Document doc       = null;
    private NodeList relations = null;
    
    private static final String RELATION_DESCRIPTOR  = "relation";
    private static final String TYPE                 = "type";
    private static final String NAME                 = "name";
    private static final String SOURCE               = "source";
    private static final String DESTINATION          = "destination";
    private static final String HANDLER              = "handler-class";
    private static final String CREATE_ANYWAY        = "create-anyway";
    
}

