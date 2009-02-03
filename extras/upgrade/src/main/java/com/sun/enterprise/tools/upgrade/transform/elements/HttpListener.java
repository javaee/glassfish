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
 * Element.java
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
import com.sun.enterprise.tools.upgrade.common.DomainsProcessor;
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import java.util.logging.Level;

public class HttpListener extends BaseElement {

    private static java.util.logging.Logger log = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();
    private final String HTTP_LISTENER_PORT_PROPERTY_NAME="HTTP_LISTENER_PORT";

    /** Creates a new instance of Element */
    public HttpListener() {
    }
    /**
     * element - http-listener
     * parentSource - source http-service
     * parentResult - parent http-service
     */
    public void transform(Element element, Element parentSource, Element parentResult){
        // In domain.xml http-service is /domain/configs/config/http-service/http-listener
        // in server.xml http-service is /server/http-service/http-listener

        // Obtain a list of http-listeners from the result. IF the passed in element is not the one in the list then add it.
        // If its the one existing then modify it.
        // There should be either one or zero http-service and zero or more http-listener
        logger.log(Level.FINE, 
                stringManager.getString("upgrade.transform.transformingMSG", 
                this.getClass().getName(), 
                element));
        NodeList resultHttpListeners = parentResult.getElementsByTagName("http-listener");
        Element resultHttpListener = null;
        java.util.Vector notToTransferAttrList = new java.util.Vector();
        for(int lh =0; lh < resultHttpListeners.getLength(); lh++){
            // Compare id attribute of http-listener elements.
            String resultElementID = ((Element)resultHttpListeners.item(lh)).getAttribute("id");
            if((element.getAttribute("id")).equals(resultElementID)){
                resultHttpListener = (Element)resultHttpListeners.item(lh);                
                this.transferAttributes(element, resultHttpListener,notToTransferAttrList);
            }
            if(resultElementID.equals("admin-listener")){
				String domainPath = commonInfoModel.getSource().getConfigXMLFile();
				String adminPort = DomainsProcessor.getSourceAdminPort(domainPath);
				String adminSecurity = DomainsProcessor.getSourceAdminSecurity(domainPath);
				((Element)resultHttpListeners.item(lh)).setAttribute("port", adminPort);
				//security-enabled should be set to true when target is of enterprise profile. start CR 6383799
				if(commonInfoModel.isEnterpriseEdition(commonInfoModel.getSource().getEdition())
				&& adminSecurity.equals("false")) {
					((Element)resultHttpListeners.item(lh)).setAttribute("security-enabled", "true");
				} else {
					((Element)resultHttpListeners.item(lh)).setAttribute("security-enabled", adminSecurity);
				}
			}
		}
        if(resultHttpListener == null){
            // Add element - http-listener to result http-service.
            resultHttpListener = parentResult.getOwnerDocument().createElement("http-listener");            
            this.transferAttributes(element, resultHttpListener,notToTransferAttrList);
            this.appendElementToParent(parentResult,resultHttpListener);
        }
        super.transform(element, parentSource, resultHttpListener);
    }
    
    public void transferAttributes(Element sourceEle, Element targetEle, java.util.List notToTransferAttrList){
        // NOTE: server.xml of as7 has family & blocking-enabled attrs 
	// They do not exist in as8 domain.xml but exists in as81 domain.xml
        // acceptor-threads should not be transferred.  FIX for problem with LINUX
        notToTransferAttrList.add("acceptor-threads");
        logger.log(Level.FINE, this.getClass().getName() + ":: notToTransferAttrList ", 
                    notToTransferAttrList);
        super.transferAttributes(sourceEle, targetEle, notToTransferAttrList);
    }
    
    private void printAttrs(Element ele){
        org.w3c.dom.NamedNodeMap sourceAttrNodeMap = ele.getAttributes();
        for(int index=0; index < sourceAttrNodeMap.getLength(); index++){
             Node sourceAttrNode = sourceAttrNodeMap.item(index);
             log.info("******\n attr name="+sourceAttrNode.getNodeName() + 
                     " attrValue="+sourceAttrNode.getNodeValue());
        }
    }
    
    private String getHttpListenerPortProperty(String id, Element resultHttpListener){
        if(id.equals("http-listener-1")){
            return this.HTTP_LISTENER_PORT_PROPERTY_NAME;
        }
        String portValue = resultHttpListener.getAttribute("port");
        if((portValue != null) && portValue.trim().startsWith("${")){
            return portValue.substring(2,portValue.length()-1);
        }
        return null;
    }
}
