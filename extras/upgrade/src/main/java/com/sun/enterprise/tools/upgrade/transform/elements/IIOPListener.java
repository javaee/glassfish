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
            this.transferAttributes(element, resultElement, notToTransferAttrList);
            this.appendElementToParent(parentResult,resultElement);  
        } else {            
            this.transferAttributes(element, resultElement, notToTransferAttrList);
        } 
        super.transform(element,  parentSource, resultElement);  
    }
    
    protected void transferAttributes(Element source, Element result, java.util.List nonTransferList){
        super.transferAttributes(source,result,nonTransferList);
    }
    
    protected java.util.List getNonTransferList(Element element){
        java.util.Vector notToTransferAttrList = new java.util.Vector();     
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
