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
import com.sun.jbi.jsf.framework.common.XmlUtils;
import com.sun.jbi.jsf.framework.model.JBIDescriptor;
import com.sun.jbi.jsf.util.JBILogger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * JBIServiceAssembly.java
 * 
 * @author ylee
 */
public class JBIServiceAssemblyDescriptor extends JBIDescriptor {
    /** Identification */
    private JBIIdentification identification;
    /** list of Service Units  */
    private List<JBIServiceUnit> suDescriptorList = new ArrayList();    
    /** list of connections */
    private List<JBIConnection> connectionList = new ArrayList();
    /** get logger instance */
    private Logger logger = JBILogger.getInstance();

    /**
     * @param xmlText   xml text of the descriptor
     */
    public JBIServiceAssemblyDescriptor(String xmlText) {
        super(xmlText);
    }

    public void parse() {
        super.parse();
        // parse ServiceAssemblyDescriptor
        if ( jbiElement!=null ) {
            Element saElement = XmlUtils.getChildElement(jbiElement, JbiConstants.SERVICE_ASSEMBLY_TAG);
            if ( saElement!=null ) {
                
                // parse Identification
                Element idElement = XmlUtils.getChildElement(saElement, JbiConstants.IDENTIFICATION_TAG);
                if ( idElement != null ) {
                    identification = JBIIdentification.create(idElement);
                }
                
                // parse Service Units
                suDescriptorList.clear();
                NodeList suNodeList = XmlUtils.getChildElements(saElement, JbiConstants.SERVICE_UNIT_TAG);
                for (int i=0; i <suNodeList.getLength(); i++ ) {
                     Element suElement = (Element)suNodeList.item(i);
                     if ( suElement != null ) {
                        JBIServiceUnit suDescriptor = JBIServiceUnit.create(suElement);
                        suDescriptorList.add(suDescriptor);
                    }                  
                }
                
                // parse connections
                Element connectionsElement = XmlUtils.getChildElement(saElement, JbiConstants.CONNECTIONS_TAG);
                if ( connectionsElement!=null ) {
                    NodeList cList = XmlUtils.getChildElements(connectionsElement, JbiConstants.CONNECTION_TAG);
                    for (int i=0; i<cList.getLength(); i++) {
                        Element cElement = (Element)cList.item(i);
                        if ( cElement!=null ) {
                            JBIConnection connection = JBIConnection.create(cElement);
                            connectionList.add(connection);
                        }
                    }
                }

            }
        
        }
    }
    
    /**
     * @return name of service assembly
     */
    public String getName() {
        return identification==null ? null : identification.getName();
    }
    
    /**
     * @return description of service assembly
     */
    public String getDescription() {
        return identification==null ? null : identification.getDescription();
    }
    
    /**
     * @return list of service units
     */
    public List<JBIServiceUnit> getServiceUnits() {
        return suDescriptorList;
    }
    
    /**
     * @return list of connections
     */
    public List<JBIConnection> getConnections() {
        return connectionList;
    }
    
     public static void main(String[] args) {
          String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jbi  xmlns=\"http://java.sun.com/xml/ns/jbi\" xmlns:ns1=\"http://localhost/SynchronousSample/SynchronousSample\"  xmlns:ns2=\"http://enterprise.netbeans.org/bpel/SynchronousSample/SynchronousSample_1\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi ./jbi.xsd\"> <service-assembly> <identification> <name>SynchronousSampleApplication</name> <description>Represents the Service Assembly of SynchronousSampleApplication</description> </identification> <service-unit> <identification>        <name>SynchronousSampleApplication-SynchronousSample</name> <description>Represents this Service Unit</description>   </identification> <target> <artifacts-zip>SynchronousSample.jar</artifacts-zip>       <component-name>sun-bpel-engine</component-name>  </target> </service-unit> <service-unit> <identification>        <name>SynchronousSampleApplication-sun-http-binding</name> <description>Represents this Service Unit</description>     </identification> <target> <artifacts-zip>sun-http-binding.jar</artifacts-zip>        <component-name>sun-http-binding</component-name> </target> </service-unit> <connections> <connection> <consumer  endpoint-name=\"port1\" service-name=\"ns1:service1\"/> <provider endpoint-name=\"partnerlinktyperole1_myRole\" service-name=\"ns2:SynchronousSample\"/> </connection> </connections> </service-assembly> </jbi>";
          JBIServiceAssemblyDescriptor desc = new JBIServiceAssemblyDescriptor(xmlText);
          desc.parse();
          Logger logger = JBILogger.getInstance();
          logger.info("name:"+desc.getName());
          logger.info("desc:"+desc.getDescription());
          List<JBIServiceUnit> list = desc.getServiceUnits();
          for ( JBIServiceUnit su : list ) {
              logger.info("SU: "+su);
          }
          List<JBIConnection> cList = desc.getConnections();
          for ( JBIConnection c : cList ) {
              logger.info("Connection: "+c);
          }
          
     }
     
}
