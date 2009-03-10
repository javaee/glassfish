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
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import java.util.logging.*;
import java.io.File;
import com.sun.enterprise.tools.upgrade.transform.AttributeExtracter;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

public class BaseElement {
    
    protected StringManager stringManager = StringManager.getManager(BaseElement.class);
    protected Logger logger = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();
    protected static CommonInfoModel commonInfoModel;
    
    /** Creates a new instance of Element */
    public BaseElement() {
    }
    
    public void transform(Element element, Element parentSource, Element parentResult){
        NodeList childNodes = element.getChildNodes();
		logger.log(Level.FINE, stringManager.getString("upgrade.transform.baseelemnt.transformingMSG",element.getTagName()));
        for(int index=0; index < childNodes.getLength(); index++){
            Node aNode = childNodes.item(index);
            try{
                if(aNode.getNodeType() == Node.ELEMENT_NODE){
                    BaseElement baseElement = ElementToObjectMapper.getMapper().getElementObject(aNode.getNodeName());
                    baseElement.transform((Element)aNode, element, parentResult);
                }
            }catch(Exception ex){
                // ****** LOG MESSAGE *************
                ex.printStackTrace();
                logger.log(Level.WARNING, stringManager.getString("upgrade.transform.baseelement.transformexception",new String[]{element.getTagName(),ex.getMessage()}));
            }
        } 
    }
    protected void transferAttributes(Element source, Element result, java.util.List nonTransferList){
        boolean debug = false;
        NamedNodeMap sourceAttrNodeMap = source.getAttributes();
         if(sourceAttrNodeMap == null) return;
        
        NamedNodeMap resultAttrNodeMap = result.getAttributes();
         
         
         for(int index=0; index < sourceAttrNodeMap.getLength(); index++){
             Node sourceAttrNode = sourceAttrNodeMap.item(index);
             if(!this.canTransferAttribute(sourceAttrNode.getNodeName(), nonTransferList)) continue;
             if(!isValidAttributeToTransfer(sourceAttrNode.getNodeName(),getAttributeListForElement(result.getTagName()))) continue;
             if(resultAttrNodeMap == null){
                 Attr addAttr = result.getOwnerDocument().createAttribute(sourceAttrNode.getNodeName());
                 addAttr.setValue(sourceAttrNode.getNodeValue());
                 result.setAttributeNode(addAttr);
             }else{
                 Node resultAttrNode = resultAttrNodeMap.getNamedItem(sourceAttrNode.getNodeName());
                 if(resultAttrNode != null){
                     resultAttrNode.setNodeValue(sourceAttrNode.getNodeValue());    
					 }else {
                     Attr addAttr = result.getOwnerDocument().createAttribute(sourceAttrNode.getNodeName());
                     addAttr.setValue(sourceAttrNode.getNodeValue());
                     result.setAttributeNode(addAttr);
                 }
             }
         }
    }
    private boolean canTransferAttribute(String attr, java.util.List attrList){
        if(attrList == null || attrList.isEmpty())
            return true;
        for(java.util.Iterator it = attrList.iterator(); it.hasNext(); ){
            if(it.next().equals(attr))
                return false;
        }
        return true;
    } 
    
    public static void setCommonInfoModel(CommonInfoModel cim){
        commonInfoModel = cim;
    }
    
    /* 
     * Returns the key mapped in mapper for the element
     * This key is used for comparing source and target elements
     * Returns NULL if no key is mapped.  This is quite common for elements that have only child elements but no attributes to transfer
     */
    protected String getKeyToCompare(String elementTagName){
        return ElementToObjectMapper.getMapper().getKeyForElement(elementTagName);
    }
    protected java.util.List getInsertElementStructure(Element element, Element parentEle){
        // Sub classes can override this method to return a different list if needed.
        // parentEle is not used in this method.  But sub classes can use it to make certain decision on structure
        return ElementToObjectMapper.getMapper().getInsertElementStructure(element.getTagName());
    }
    public void appendElementToParent(Element parentEle, Element element){
        java.util.List eleStructureList = this.getInsertElementStructure(element,parentEle);
        if(eleStructureList == null){
            // insert the element at the end
            parentEle.appendChild(element);
            return;
        }
        if(eleStructureList.isEmpty()){
            // insert the element in the beginning.
            parentEle.insertBefore(element, parentEle.getFirstChild());
            return;
        }
        String insertBeforeElementName = null;
        Node insertBeforeNode = null;            
        for(int eleIndex =0; eleIndex < eleStructureList.size(); eleIndex++){
            insertBeforeElementName = (String)eleStructureList.get(eleIndex);
            Node lNode=parentEle.getFirstChild();
            while (lNode != null) {
                if (lNode instanceof Element) {
                    Element lElement=(Element)lNode;
                    if (lElement.getNodeName().equals(insertBeforeElementName)) {
                        // if match is found, break and insert
                        insertBeforeNode = lNode;
                        break;
                    }
                }                
                // go to next sibling in order
                lNode=lNode.getNextSibling();
            } 
            if(insertBeforeNode != null){
                break;
            }            
        }
        // if match is not found, node will be place at the end
        parentEle.insertBefore(element,insertBeforeNode);
    }
    protected boolean isValidAttributeToTransfer(String attrName, java.util.List attrList){
        for(java.util.Iterator it = attrList.iterator(); it.hasNext();){
            if(((String)it.next()).equals(attrName)){
                return true;
            }
        }
        return false;
    }
    protected java.util.List getAttributeListForElement(String elementName){
		String dtdName = commonInfoModel.getTarget().getDTDFilename();
        String dtdFileName = this.commonInfoModel.getTarget().getInstallRootProperty()+"/"+"lib"+"/"+"dtds"+"/"+dtdName;
        return AttributeExtracter.getExtracter(dtdFileName).getAttributeList(elementName);        
    }
}
