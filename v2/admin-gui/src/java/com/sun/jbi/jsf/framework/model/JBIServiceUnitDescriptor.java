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


package com.sun.jbi.jsf.framework.model;

import com.sun.jbi.jsf.framework.common.JbiConstants;
import com.sun.jbi.jsf.framework.common.JbiConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.common.XmlUtils;
import com.sun.jbi.jsf.framework.model.JBIDescriptor;
import com.sun.jbi.jsf.util.JBILogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
  * JBIServiceUnitDescriptor.java
 *
 * @author ylee
 */
public class JBIServiceUnitDescriptor extends JBIDescriptor {

    /** consumes endpoints */
    private List<JBIEndpoint> consumesEndpointList = new ArrayList();
    /** provides endpoints */
    private List<JBIEndpoint> providesEndpointList = new ArrayList();
    
    /** Get Logger to log fine mesages for debugging */
    private static Logger logger = JBILogger.getInstance();

    public JBIServiceUnitDescriptor() {
        super();
    }
    
    /**
     * @param xmlText   xml text of the descriptor
     */    
    public JBIServiceUnitDescriptor(String xmlText) {
        super(xmlText);
    }
    
    public List<JBIEndpoint> getConsumesEndpointList() {
        return consumesEndpointList;
    }
    
    public List<JBIEndpoint> getProvidesEndpointList() {
        return providesEndpointList;
    }
        
    
    public void parse() {
        super.parse();
        
        // parse services
        if ( jbiElement!=null ) {
            Element servicesElement = XmlUtils.getChildElement(jbiElement, JbiConstants.SERVICES_TAG);
            if ( servicesElement!=null ) {
                JBIEndpoint consumesEndpoint;
                consumesEndpointList.clear();
                providesEndpointList.clear();
                NodeList consumesNodeList = XmlUtils.getChildElements(servicesElement, JbiConstants.CONSUMES_TAG);
                if ( consumesNodeList!=null ) {
                    for (int i=0; i<consumesNodeList.getLength(); i++) {
                        Element consumesElement = (Element)consumesNodeList.item(i);
                        consumesEndpoint = JBIEndpoint.create2(consumesElement);
                        consumesEndpointList.add(consumesEndpoint);
                    }
                }
                JBIEndpoint providesEndpoint;
                NodeList providesNodeList = XmlUtils.getChildElements(servicesElement, JbiConstants.PROVIDES_TAG);
                if ( providesNodeList!=null ) {
                    for (int i=0; i<providesNodeList.getLength(); i++) {
                        Element providesElement = (Element)providesNodeList.item(i);
                        providesEndpoint = JBIEndpoint.create2(providesElement);
                        providesEndpointList.add(providesEndpoint);
                    }
                }
                
            }
        }
    }

    
    public Map getEndpoints(boolean isConsumer) {
        Map map = new HashMap();
        List<JBIEndpoint> list;
        if ( isConsumer ) {
            list = consumesEndpointList;
        } else {
            list = providesEndpointList;
        }
        for ( JBIEndpoint endpoint : list ) {
            String endpointName = endpoint.getEndpointName();
            String serviceName = endpoint.getServiceName();
            String namespace = getNamespaceValue(Util.trimRight(serviceName,JbiConstants.COLON_SEPARATOR));
            if ( namespace==null ) {
                namespace = "";
            }
            String localServiceName = Util.trimLeft(serviceName,JbiConstants.COLON_SEPARATOR);
            String fqEndpointName = namespace + JbiConstants.COMMA_SEPARATOR + localServiceName + 
                    JbiConstants.COMMA_SEPARATOR + endpointName;
            logger.finest(">>>> "+fqEndpointName);
            map.put(fqEndpointName,fqEndpointName);
        }
         
        return map;
    }
    
     public static void main(String[] args) {
          String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jbi xmlns=\"http://java.sun.com/xml/ns/jbi\" xmlns:ns1=\"http://localhost/SynchronousSample/SynchronousSample\" xmlns:ns2=\"http://enterprise.netbeans.org/bpel/SynchronousSample/SynchronousSample_1\" version=\"1.0\"> <services binding-component=\"true\"> <consumes endpoint-name=\"port1\" interface-name=\"ns1:portType1\" service-name=\"ns1:service1\"/> </services> </jbi>";
          JBIServiceUnitDescriptor desc = new JBIServiceUnitDescriptor(xmlText);
          desc.parse();

          Logger logger = JBILogger.getInstance();
          List<JBIEndpoint> list = desc.getConsumesEndpointList();
          for ( JBIEndpoint ep : list ) {
              logger.info("Consumes Endpoint: "+ep);
          }
          List<JBIEndpoint> pList = desc.getProvidesEndpointList();
          for ( JBIEndpoint ep : pList ) {
              logger.info("Provides Endpoint: "+ep);
          }
          logger.info(">>>> Consumes: "+desc.getEndpoints(true));
          logger.info(">>>> Provides: "+desc.getEndpoints(false));
          
     }    
  
}
 

