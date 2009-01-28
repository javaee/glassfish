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
 * VirtualServer.java
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

public class VirtualServer extends BaseElement {
    
    private static java.util.logging.Logger log = 
        com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();

    /** Creates a new instance of Element */
    public VirtualServer() {
    }
    /**
     * element - virtual server 
     * parentSource - parent virtual-server-class of element for as7, virtual-server for as8
     * parentResult - parent http-service of result
     */
    public void transform(Element element, Element parentSource, Element parentResult){
        // In domain.xml http-service is /domain/configs/config/http-service/viertual-server-class/virtual-server
        // in server.xml http-service is /server/http-service/virtual-server-class/virtual-server
        
        // Obtain a list of virtual-server from the result. 
        //If the passed in element is not the one in the list then add it.
        // If its the one existing then update it.
        // There should be either one or zero http-service and zero or more vertual-server-class
                
        NodeList resultVirtualServers = parentResult.getElementsByTagName("virtual-server");
        Element resultVirtualServer = null;
        String serverID = "server1";
        
        for(int lh =0; lh < resultVirtualServers.getLength(); lh++){
            if(element.getAttribute("id").equals(serverID) &&
                    ((Element)resultVirtualServers.item(lh)).getAttribute("id").equals("server")){
                resultVirtualServer = (Element)resultVirtualServers.item(lh);
                java.util.Vector notToTransferAttrList = new java.util.Vector();
                notToTransferAttrList.add("id");
                notToTransferAttrList.add("http-listeners");
                notToTransferAttrList.add("config-file");
                this.transferAttributes(element, resultVirtualServer, notToTransferAttrList);
                break;
            }else if((element.getAttribute("id")).equals(((Element)resultVirtualServers.item(lh)).getAttribute("id"))){
                resultVirtualServer = (Element)resultVirtualServers.item(lh);
                java.util.Vector notToTransferAttrList = new java.util.Vector();
                notToTransferAttrList.add("default-web-module");
                this.transferAttributes(element, resultVirtualServer, notToTransferAttrList);
                break;
             }
        }
        if(resultVirtualServer == null){
            // Add element - virtual server to result virtual-server-class.
            resultVirtualServer = parentResult.getOwnerDocument().createElement("virtual-server");
            this.transferAttributes(element, resultVirtualServer, null);
            this.appendElementToParent(parentResult,resultVirtualServer);            
        }
        //this.printAttrs(resultVirtualServer);
        // There are few property elements like docroot, 
        //accesslog those pointing to directory/file in the current intallation.
        //   NEED TO FIX....Should avoid those properties being invoked by 
        //the below method. may be override the super method...
        super.transform(element, parentSource, resultVirtualServer);
    }
  	
    private void printAttrs(Element ele){
        log.info(" ____________ print virtual-server attributes");
        org.w3c.dom.NamedNodeMap sourceAttrNodeMap = ele.getAttributes();
        for(int index=0; index < sourceAttrNodeMap.getLength(); index++){
             Node sourceAttrNode = sourceAttrNodeMap.item(index);
             log.info("\n attr name="+sourceAttrNode.getNodeName()+" attrValue="+sourceAttrNode.getNodeValue());
        }
    }
    
}
