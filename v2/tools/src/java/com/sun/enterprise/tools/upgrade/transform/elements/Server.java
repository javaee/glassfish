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
 * Server.java
 *
 * Created on August 4, 2003, 3:27 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import java.util.logging.Level;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;

public class Server extends BaseElement {
    
    /** Creates a new instance of Server */
    public Server() {
    }
    
    /**
     * element - server
     * parentSource - server
     * parentResult - domain for as7x.  server for as80
     */
    public void transform(Element element, Element parentSource, Element parentResult) {
        // Attributes of server element: name, locale, log-root, application-root, session-store
        // application-root and log-root will not be transferred, as they are different for 8.0 appserver.
        // May be application-root need to be saved to migrate applications from the source directory.
        // session-store attribute should be stored in ejb-container element
        // Fetch data from server.xml and update it to domain.xml
        //
	//NOT REQUIRED SINCE 7X NOT SUPPORTED 
        /*if(UpgradeConstants.VERSION_7X.equals(commonInfoModel.getSourceVersion())){
            if(UpgradeConstants.EDITION_PE.equals(commonInfoModel.getSourceEdition())){
                // PE
                String locale = element.getAttribute("locale");
                Element resDocEle = parentResult;
                resDocEle.setAttribute("locale", locale);
                if(commonInfoModel.getTargetEdition().equals(UpgradeConstants.EDITION_PE)){
                    super.transform(element, parentSource, parentResult); 
                }else{
                    // Obtain the config named as server_config and pass that as the parent config
                    NodeList configEles = parentResult.getElementsByTagName("config");
                    Element configEle = null;
                    String configToUpgrade = element.getAttribute("name")+"-config";
                    for(int lh =0; lh < configEles.getLength(); lh++){
                        String configName = ((Element)configEles.item(lh)).getAttribute("name");
                        if(configName.equals(configToUpgrade)){
                            configEle = (Element)configEles.item(lh);
                            parentResult = configEle;
                            break;
                         }
                    }
                    if(configEle == null){
                        // This is cannot be possible.  If so... must be domain.xml is corrupt.  
			// Log error and fail transofmration.
                        // FIXME
                    }
                    super.transform(element, parentSource, configEle);
                }
            }else{
                // SE or EE
                // Assumptions.... server1 of SE/EE would be treated as default 'server' in 80 se/ee
                // NEED TO DISCUSS THIS.  Should WE CREATE A new CONFIG for EACH SERVER or, Should we use DAS
                    String configToUpgrade = null;
                    if(commonInfoModel.getCurrentCluster() == null){
                        // This must be a SE instance or stand alone instance of EE
                        configToUpgrade = element.getAttribute("name")+"-config";
                    }else{
                        configToUpgrade = commonInfoModel.getCurrentCluster()+"-config";
                    }
                    // Harness would have created a new serverName_config for a new server element.
                    NodeList configs = parentResult.getElementsByTagName("configs");
                    // There is a promise of one and only one configs element in 8.x
                    Element configsEle = (Element)configs.item(0);
                    
                    // Obtain the config named as serverName_config and pass that as the parent config
                    NodeList configEles = configsEle.getElementsByTagName("config");
                    Element configEle = null;
                    for(int lh =0; lh < configEles.getLength(); lh++){
                        String configName = ((Element)configEles.item(lh)).getAttribute("name");
                        if(configName.equals(configToUpgrade)){
                            configEle = (Element)configEles.item(lh);
                            parentResult = configEle;
                            break;
                         }
                    }
                    if(configEle == null){
                        // This is cannot be possible.  If so... domain processor must have not finished successfully  
			// Log error and fail transofmration.
                        // FIXME
                        //START CR 6460863
                        //Exception ex = new Exception("Could not find Config Element - "+configToUpgrade+" in target domain ");
                        // logger.log(Level.SEVERE, stringManager.getString("upgrade.transform.startFailureMessage",ex.getMessage()),ex);
                        // END CR 6460863
                        return;
                    }  
                    //Element configElement = parentResult.getOwnerDocument().createElement("config");
                    
                    // Set the name attribute to this 'server element name'_config. 
                    //configElement.setAttribute("name", element.getAttribute("name")+"_config");
                    //configsEle.appendChild(configElement);
                    // Now pass this newly found config element as the parent result.  
		    // That takes care of automatic trnasofrmation of elements under server 
                    super.transform(element, parentSource, configEle);     
                //}      
             }
        }else{*/
            // AS 80
            NodeList servers = parentResult.getElementsByTagName("server");
            Element server = null;
            for(int lh =0; lh < servers.getLength(); lh++){
                String resultElementID = ((Element)servers.item(lh)).getAttribute("name");
                if((element.getAttribute("name")).equals(resultElementID)){
                    server = (Element)servers.item(lh);
                    this.transferAttributes(element, server,null);
                    if(UpgradeConstants.VERSION_80.equals(commonInfoModel.getSourceVersion())){
                        if(element.hasAttribute("node-controller-ref")){
                            server.setAttribute("node-agent-ref", element.getAttribute("node-controller-ref"));
                        }
                    }
                    break;
                 }                
            }
            if(server == null){
                // Add element - http-listener to result http-service.
                server = parentResult.getOwnerDocument().createElement("server");
                this.transferAttributes(element, server,null);
                this.appendElementToParent(parentResult,server);  
            }        
            super.transform(element, parentSource, server); 
            return;
	//NOT REQUIRED SINCE 7X NOT SUPPORTED
        //}
	//END
        //super.transform(element, parentSource, parentResult);        
    }
    
}
