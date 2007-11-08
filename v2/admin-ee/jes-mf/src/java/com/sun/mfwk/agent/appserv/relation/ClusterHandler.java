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


import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

import javax.management.ObjectName; 
import javax.management.MBeanServerConnection;

import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.agent.appserv.relation.RelationModeler;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.relations.RelationServiceImpl;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.util.Constants;

import javax.management.ObjectName; 
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServerConnection;


/*
 * Handler to handle cluster relationsships
 */
 public class ClusterHandler implements RelationMappingHandler {

     /**
      * Creates relations between cluster and its instances. 
      * Queries DAS on app server to get list of instances.
      */
     public void relationHandler(Element elem, ObjectName ON, 
            RelationMappingService rms, RelationModeler rm) throws Exception {

        Logger logger = LogDomains.getLogger();

        // application server domain
        String domain = (String) rms.getProperty(Constants.DOMAIN_NAME_PROP);

        ConnectionRegistry registry = ConnectionRegistry.getInstance();
        MBeanServerConnection connection = 
            registry.getConnection(Constants.ADMIN_SERVER_NAME, domain);

        RelationFactory rf = new RelationFactory(rm.getContext());
             
        ObjectName[] instances = 
            (ObjectName[])connection.invoke(
                new ObjectName("com.sun.appserv:type=cluster,name=" 
                         + ON.getKeyProperty("name") + ",category=config"),
                "listServerInstances", null, null);

         for (int i = 0; i<instances.length; i++) {

            logger.finest("Adding cluster-instance relation for instance " 
                + instances[i]);
                String instanceName = instances[i].getKeyProperty("name");
                try {
                    MBeanServerConnection con =
                        registry.getConnection(instanceName, domain);
                } catch (Exception ex) {
                    continue;
                }
                String dest = "com.sun.cmm.as:type=CMM_J2eeServer,domain=" + 
                    ON.getKeyProperty("domain") 
                + ",name=" + instances[i].getKeyProperty("name") ;
            
            
            // get CMM object name for this instance
                Relation relation = 
                    rf.create(ON.toString(), dest, rms.getType(elem), true);

                // add the newly created relation 
                if (relation != null) { 
                    RelationServiceImpl.getRelationService().addRelation(relation);
                }
         }
     }
 }
