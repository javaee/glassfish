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
 * IIOPListener.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import java.util.logging.Level;

public class JMSResource extends GenericResource {

    /** Creates a new instance of Element */
    public JMSResource() {
    }
    /**
     * element - jms-resource
     * parentSource - resources element of source
     * parentResult - resources element of result
     */
    public void transform(Element element, Element parentSource, Element parentResult){
        Element resultResource = null;
        logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), element.getTagName()));
        String resourceType = element.getAttribute("res-type");
        logger.log(Level.FINE, stringManager.getString(this.getClass().getName() +  ":: resourceType ", resourceType));
        /*
         * JMS resource type. This can be one of: javax.jms.Topic, javax.jms.Queue, javax.jms.TopicConnectionFactory, javax.jms.QueueConnectionFactory
         */
        if(resourceType.equals("javax.jms.Topic") || resourceType.equals("javax.jms.Queue")){
            // javax.jms.Topic, javax.jms.Queue must use admin-object-resource in 8.0
            resultResource = this.transformToAdminObjectResource(element, parentSource, parentResult);
        }else{
            // javax.jms.TopicConnectionFactory, javax.jms.QueueConnectionFactory must use connector-resouce in appserver 8.0
            resultResource = this.transformToConnectorResource(element, parentSource, parentResult);
        }
        super.updateResourceRef(resultResource, parentResult);
        super.transform(element, parentSource, resultResource);
    }
    private Element transformToAdminObjectResource(Element element, Element parentSource, Element parentResult){
        NodeList resultResources = parentResult.getElementsByTagName("admin-object-resource");
        Element resultResource = null;
        for(int lh =0; lh < resultResources.getLength(); lh++){
            // Compare one key attribute
            if((element.getAttribute("jndi-name")).equals(((Element)resultResources.item(lh)).getAttribute("jndi-name"))){
                resultResource = (Element)resultResources.item(lh);
                resultResource.setAttribute("res-type", element.getAttribute("res-type"));
                resultResource.setAttribute("enabled", element.getAttribute("enabled"));
                resultResource.setAttribute("res-adapter", "jmsra");
                break;
             }
        }
        if(resultResource == null){
            // Add element - http-listener to result http-service.
            resultResource = parentResult.getOwnerDocument().createElement("admin-object-resource");
            resultResource.setAttribute("jndi-name", element.getAttribute("jndi-name"));
            resultResource.setAttribute("res-type", element.getAttribute("res-type"));
            resultResource.setAttribute("enabled", element.getAttribute("enabled"));
            resultResource.setAttribute("res-adapter", "jmsra");
            this.appendElementToParent(parentResult,resultResource);
        }
        return resultResource;
    }
    private Element transformToConnectorResource(Element element, Element parentSource, Element parentResult){
        NodeList resultResources = parentResult.getElementsByTagName("connector-resource");
        Element resultResource = null;
        for(int lh =0; lh < resultResources.getLength(); lh++){
            // Compare one key attribute
            if((element.getAttribute("jndi-name")).equals(((Element)resultResources.item(lh)).getAttribute("jndi-name"))){
                resultResource = (Element)resultResources.item(lh);
                resultResource.setAttribute("enabled", element.getAttribute("enabled"));
                break;
             }
        }
        if(resultResource == null){
            // Add element - http-listener to result http-service.
            resultResource = parentResult.getOwnerDocument().createElement("connector-resource");
            resultResource.setAttribute("jndi-name", element.getAttribute("jndi-name"));
            resultResource.setAttribute("enabled", element.getAttribute("enabled"));
            resultResource.setAttribute("pool-name", element.getAttribute("jndi-name"));
            this.appendElementToParent(parentResult,resultResource);
            // Now search for connectionpool resource.
            this.transformConnectorPool(element, parentSource, parentResult);
        }
        return resultResource;
    }
    private void transformConnectorPool(Element element, Element parentSource, Element parentResult){
        NodeList connectorPools = parentResult.getElementsByTagName("connector-connection-pool");
        Element connectorPool = null;
        for(int lh =0; lh < connectorPools.getLength(); lh++){
            // Compare one key attribute
            if((element.getAttribute("jndi-name")).equals(((Element)connectorPools.item(lh)).getAttribute("name"))){
                connectorPool = (Element)connectorPools.item(lh);
                connectorPool.setAttribute("connection-definition-name", element.getAttribute("res-type"));
                connectorPool.setAttribute("resource-adapter-name", "jmsra");
                break;
             }
        }
        if(connectorPool == null){
            // Add element - http-listener to result http-service.
            connectorPool = parentResult.getOwnerDocument().createElement("connector-connection-pool");
            connectorPool.setAttribute("name", element.getAttribute("jndi-name"));
            connectorPool.setAttribute("connection-definition-name", element.getAttribute("res-type"));
            connectorPool.setAttribute("resource-adapter-name", "jmsra");
            this.appendElementToParent(parentResult,connectorPool);
        }
    }
}
