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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServerConnection;

import com.sun.mfwk.agent.appserv.modeler.ObjectNameHelper;
import com.sun.mfwk.agent.appserv.connection.ConnectionRegistry;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.relation.RelationModeler;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.relations.RelationServiceImpl;
import com.sun.mfwk.agent.appserv.util.Utils;

public class BeanHandler implements RelationMappingHandler {
    
    /**
     *
     * Creates relations for bean pool. Looks at the appropraite app server mbean to find the type of bean.
     *
     */
    public void relationHandler(Element elem, ObjectName ON,
            RelationMappingService rms, RelationModeler rm) throws Exception {
        Logger logger = LogDomains.getLogger();
        String beanType = null;
        
        String AS_AppObjectNameTemplate = "com.sun.appserv:application=${application},category=monitor,ejb-module=${ejb-module},name=${ejb},server=${server},type=ejb";
        String AS_StandAloneObjectNameTemplate =
                "com.sun.appserv:category=monitor,name=${name},server=${server},standalone-ejb-module=${standalone-ejb-module},type=ejb";
        
        Utils.log(Level.FINEST, " RelationHandler ON = " + ON);
        
        try {
            String AS_ObjectNameTemplate = AS_StandAloneObjectNameTemplate;
            try {
                String application = ON.getKeyProperty("application");
                AS_ObjectNameTemplate = AS_AppObjectNameTemplate;
            } catch(Exception ex) {
            }
            
            String AS_ObjectNameStr = ObjectNameHelper.tokenizeON(ON,
                    AS_ObjectNameTemplate, rm.getContext().getTokens());
            
            Utils.log(Level.FINEST, " AS Template = " + AS_ObjectNameTemplate);
            Utils.log(Level.FINEST, " AS ObjecName = " + AS_ObjectNameStr);
            
            ObjectName AS_ObjectName = new ObjectName(AS_ObjectNameStr);
            
            //get the connection
            String server = (String)
            rms.getProperty(Constants.SERVER_NAME_PROP);
            String domain = (String)
            rms.getProperty(Constants.DOMAIN_NAME_PROP);
            
            ConnectionRegistry registry = ConnectionRegistry.getInstance();
            MBeanServerConnection connection =
                    registry.getConnection(server, domain);
            
            //query the bean type
            beanType = (String)connection.invoke(AS_ObjectName,
                    "getType", null, null);
            Utils.log(Level.FINE, " beantype = " + beanType);
        } catch (MalformedObjectNameException exception) {
            
            if(logger != null){
                logger.log(Level.WARNING,
                        "Error while constructing ObjectName", exception);
            }
        } catch (Exception exception) {
            if (logger != null) {
                logger.log(Level.WARNING,
                        "Error - Not able to get the bean type", exception);
            }
        }
        String CMMBeanType = null;
        if ("stateless-session-bean".equals(beanType))  {
            CMMBeanType = "CMM_J2eeStatelessSessionBean";
        }
        else if ("stateful-session-bean".equals(beanType)) {
            CMMBeanType = "CMM_J2eeStatefulSessionBean";
        }
        else if ("entity-bean".equals(beanType)) {
            CMMBeanType = "CMM_J2eeEntityBean";
        }
        else if ("message-driven-bean".equals(beanType)) {
            CMMBeanType = "CMM_J2eeMessageDrivenBean";
        }
        if (CMMBeanType == null) {
            throw new Exception("cannot handle type " + beanType);
        }
        String CMM_ObjectNameTemplate = "com.sun.cmm.as:application=${application},name=${name},type="+ CMMBeanType + ",ejb-module=${ejb-module},domain=${domain.name},server=${server.name}";
        String CMM_ObjectNameStr = ObjectNameHelper.tokenizeON(ON,
                CMM_ObjectNameTemplate, rm.getContext().getTokens());
        Utils.log(Level.FINEST, " RelationHandler CMM ON = " + CMM_ObjectNameStr);
        
        RelationFactory rf = new RelationFactory(rm.getContext());
        
        Relation relation =
                rf.create(CMM_ObjectNameStr, ON.toString(), rms.getType(elem));
        
        // add the newly created relation
        if (relation != null) {
            Utils.log(Level.FINEST, " Created Relation = " + relation );
            RelationServiceImpl.getRelationService().addRelation(relation);
        }
        
        
    }
}
