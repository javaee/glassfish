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
import com.sun.enterprise.tools.upgrade.common.UpgradeUtils;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;

public class IIOPListener extends BaseElement {    
    
    private final String IIOP_LISTENER_PORT_PROPERTY_NAME="IIOP_LISTENER_PORT";
    
    public void transform(Element element, Element parentSource, Element parentResult){
        
        String elementTagName = element.getTagName();
        NodeList resultElements = parentResult.getElementsByTagName(elementTagName);
        Element resultElement = null;
        for(int lh =0; lh < resultElements.getLength(); lh++){
            Element itElement = ((Element)resultElements.item(lh));
            if((element.getAttribute("id")).equals(((Element)resultElements.item(lh)).getAttribute("id"))){
                resultElement = (Element)resultElements.item(lh);
                break;
            }
        }
        java.util.List notToTransferAttrList = this.getNonTransferList(element);            
        if(resultElement == null){
            resultElement = parentResult.getOwnerDocument().createElement(elementTagName);
            if(commonInfoModel.getCurrentCluster() != null){
                notToTransferAttrList.add("port");
                String propertyName = element.getAttribute("id")+"_IIOP_LISTENER_PORT";
                UpgradeUtils.getUpgradeUtils(commonInfoModel).
                        updateListenerPortsForClusteredInstances(parentResult.getOwnerDocument().getDocumentElement(),
                            propertyName, element.getAttribute("port"), this);
                resultElement.setAttribute("port", "${"+propertyName+"}");
            }
            this.transferAttributes(element, resultElement, notToTransferAttrList);
            this.appendElementToParent(parentResult,resultElement);  
        }else {            
            if(commonInfoModel.getCurrentCluster() == null){
                // This is for stand alone servers.  Lets not change the port nos. directly in config.  
		// Rather set it in system property
                if(!UpgradeConstants.EDITION_PE.equals(commonInfoModel.getSourceEdition())){
                    String instanceName = commonInfoModel.getCurrentSourceInstance();
                    if((instanceName != null) &&(!("".equals(instanceName.trim())))){
                        if(element.getAttribute("id").equals("orb-listener-1")){
                            notToTransferAttrList.add("port");    
                            String propertyValue = element.getAttribute("port");
                            NodeList servers = parentResult.getOwnerDocument().
                                getDocumentElement().getElementsByTagName("servers");
                            NodeList serverList = ((Element)servers.item(0)).getElementsByTagName("server");
                            UpgradeUtils.getUpgradeUtils(commonInfoModel).
                                addOrUpdateSystemPropertyToServer(instanceName, serverList, 
                                    IIOP_LISTENER_PORT_PROPERTY_NAME, element.getAttribute("port"), this);
                        }
                    }
                }
            }
            this.transferAttributes(element, resultElement, notToTransferAttrList);
        } 
        super.transform(element,  parentSource, resultElement);  
    }
    
    protected void transferAttributes(Element source, Element result, java.util.List nonTransferList){
        if(!UpgradeConstants.EDITION_PE.equals(commonInfoModel.getSourceEdition())){
            if(UpgradeConstants.VERSION_7X.equals(commonInfoModel.getSourceVersion())){
                if(source.getAttribute("id").equals("orb-listener-1") && source.getAttribute("port").equals("3700")){
                    // If server-config has 3700 as port no. for orb-listener-1
		    // set it with the port no. that this listener has got.
                    // If switching is done, then it is ok to transfer the port 3700 from source to target.

                    String serverID = source.getOwnerDocument().getDocumentElement().getAttribute("name");
                    //result.getAttribute("port") may return ${IIOP_LISTENER_PORT}....need to check
                    // If the source is AS70 EE do not transfer the port. Do this only if the source is SE or 7.1EE
                    if(UpgradeConstants.EDITION_SE.equals(commonInfoModel.getSourceEdition()) || 
                            this.isAppserver71EE(source)){
                        UpgradeUtils.getUpgradeUtils(commonInfoModel).
                            switchedIIOPPorts(serverID, result.getAttribute("port"),
                                result.getOwnerDocument().getDocumentElement());                    
                    }
                }
            }
        }
        super.transferAttributes(source,result,nonTransferList);
    }
    
    protected java.util.List getNonTransferList(Element element){
        java.util.Vector notToTransferAttrList = new java.util.Vector();
        if(commonInfoModel.getCurrentCluster() != null){
            if(element.getAttribute("id").equals("orb-listener-1")){
                // In this case iiop-cluster processing transforms the port nos.  So, dont change the port no here.
                notToTransferAttrList.add("port");            
            }
        }        
        return notToTransferAttrList;
    }
    
    private boolean isAppserver71EE(Element source){
        Element docEle = source.getOwnerDocument().getDocumentElement();
        NodeList iiopClusters = docEle.getElementsByTagName("iiop-cluster");
        if((iiopClusters == null) || (iiopClusters.getLength() == 0)){
            return false;
        }
        return true;
    }
}
