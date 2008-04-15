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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.admin.util;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.lang.reflect.*;
import java.io.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;


import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* This program is a utility to alter XML documents that need to be changed when
* implementing the S1AS SE/EE version on the PE base.  XSLT was deemed to be verbose
* error prone and did not have the required funtionality when working at the attribute
* level.  The XML document that governs the changes made to the base XML document is 
* constrained by an XML Shema which is called change.xsd
* <br>
* This program is meant to be modified to address and needs that are encounted in altering 
* all the base XML documents.
* <br>
* A sample XML change document could look like this
* 
* <?xml version="1.0"?>
* <alterations xmlns="http://java.sun.com/j2ee/s1as8se/node_agent" 
*  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="change.xsd">
* 
*     <alteration type="FIRST_OCCURRENCE" elementName="mbean" dnType="attribute" dnName="name" dnValue="node-controllers">
*             <addAttribute name="classname"  value="com.sun.enterprise.ee.nodeagent.mbeans.NodeAgentConfigMBean"/>
*             <addAttribute name="value"  value="someValue"/>
*             <changeAttribute name="group"  value="configxxxx"/>
*     </alteration>
*     <alteration type="ALL" elementName="mbean">
*             <addAttribute name="target"  value="TESTTEST"/>
*     </alteration>
*     <alteration type="ALL" elementName="mbean" dnType="attribute" dnName="name" dnValue="applications">
*             <removeElement/>
*     </alteration>
* </alterations>
* 
* @author
*/

final class NOOPHandler extends DefaultHandler {
   
    private final String _dtdFileName;
    
    NOOPHandler(String dtdFileName) {
        super();
        _dtdFileName = dtdFileName;
    }

    public InputSource resolveEntity(String publicId,
         String systemId) throws SAXException
    {
        InputSource is = null;
        try {
            is = new InputSource(new FileInputStream(_dtdFileName));
        } catch(Exception e) {
            throw new SAXException("cannot resolve dtd", e);
        }
        return is;
    }

}

public class XMLAlterUtil {
    
    /**
    * main - Static invocation method, could also be used programatically.  
    * Proper Usage: java XMLAlterUtil BASE_DOCUMENT_TO_CHANGE CHANGES_DOCUMENT OUTPUT_DOCUMENT
    *
    * @param args - command line arguments
    */
    public static void main(String[] args) {
        try {
            // minimal check for proper usage
            if (args.length != 4) {
                System.out.println("Proper Usage: java XMLAlterUtil BASE_DOCUMENT_TO_CHANGE BASE_DOCUMENT_DTD CHANGES_DOCUMENT OUTPUT_DOCUMENT");
                System.exit(1);
            }
                    
            String baseXML=args[0];
            String baseDTD=args[1];
            String changeXML=args[2];
            String outXML=args[3];
            
            if (System.getProperty("Debug") != null) {
                bDebug=true;
            }
            
            XMLAlterUtil xau=new XMLAlterUtil();
            
            // read in documents
            if (bDebug) System.out.println("parsing - " + baseXML);
            Document baseDoc=null;
            if(baseDTD.equals("null")) {
                baseDoc=xau.readDOM(baseXML);
            } else {
                baseDoc=xau.readDOM(baseXML, baseDTD);
            }
            
            if (bDebug) System.out.println("parsing - " + changeXML);
            Document changeDoc=xau.readDOM(changeXML);
            
            // execute alterations on base document
            xau.alterDOM(baseDoc, changeDoc);
            
            // baseDoc should be altered
            DocumentType baseDocType=baseDoc.getDoctype();
            
            String doctype_system=null, doctype_public=null;
            if (baseDocType != null) {
                doctype_system=baseDocType.getSystemId();
                doctype_public=baseDocType.getPublicId();
            }
            
            xau.writeDOM(baseDoc, outXML, doctype_system, doctype_public);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
    * alterDOM - method that executes the required changes to the base document
    *
    * @param baseDoc - A DOM of the base document that is going to be altered by reference
    * @param changeDoc - A DOM of the changes that are constrained by the change.xsd schema
    */
    public void alterDOM(Document baseDoc, Document changeDoc) throws Exception {

        // get root elements for both documents
        Element baseRoot=baseDoc.getDocumentElement();
        Element changeRoot=changeDoc.getDocumentElement();
        
        NodeList nlx, bnl, bnlx;
        Node bNode, bNodex;
        Element cElement, bElement, bElementx;
        String elementName, type, dnType, dnName, dnValue, dnModifier, bAttrValue;

        //
        // get list of alterations that have to be made
        NodeList nl=changeRoot.getElementsByTagName("alteration");
        boolean matched=false;
    
        if (bDebug) System.out.println("\n** Base Root Node Name is '" + baseRoot.getNodeName() + "' **");
        
        for(int ii=0; ii < nl.getLength(); ii++) {
            
            // get alteration specifics to change
            cElement=(Element)nl.item(ii);

            // get the method for identifying the element to change
            matched=false;
            elementName=cElement.getAttribute("elementName");                
            type=cElement.getAttribute("type");                
            dnType=cElement.getAttribute("dnType");                
            dnName=cElement.getAttribute("dnName");                
            dnValue=cElement.getAttribute("dnValue");  
            dnModifier=cElement.getAttribute("dnModifier");  
                          
            if (bDebug) System.out.println("\n** Trying to match an element '" + elementName + "' with dnType='" 
                + dnType + "', dnName='" + dnName + "' and dnValue='" + dnValue + "'");
            
            //
            // search through base document to see if a match can be found
            if (elementName.equals(baseRoot.getNodeName())) {
                // match is to root node so get proper node list
                bnl=baseDoc.getElementsByTagName(elementName);
            } else {
                bnl=baseRoot.getElementsByTagName(elementName);
            }
            
            if (bDebug) System.out.println("\n\tFound " + bnl.getLength() + " elements to match to.");
            for (int jj=0; jj < bnl.getLength(); jj++) {
            
                // make sure it is an element
                if (bnl.item(jj) instanceof Element) {
                    
                    // caste into element to use attribute functionality
                    bElement=(Element)bnl.item(jj);
                    
                    // match the element with critera
                    if (dnType.equals("attribute")) {
                        // match on attrbute get name
                        
                        // get value for attribute, if it doesn't exist
                        // it will return an empty String
                        bAttrValue=bElement.getAttribute(dnName);
                        
                        if ( matchValue(bAttrValue, dnValue, dnModifier) ) {
                            // match found, execute change
                            if (bDebug) System.out.println("\n\tFound match on Attibute! Executing alteration...");
                            matched=true;
                            executeAlteration(bElement, cElement);
                        }
                    
                    } else if(dnType.equals("text")) {
                        // text match, retrieve text from element
                        bnlx=bElement.getChildNodes();
                        for (int mm=0; mm < bnlx.getLength(); mm++) {
                            if (bnlx.item(mm) instanceof Text) {
                                if (bDebug) System.out.println("Text - '" + bnlx.item(mm).getNodeValue() + "'");

                                if ( matchValue(bnlx.item(mm).getNodeValue(), dnValue, dnModifier) ) {
                                    // match found, execute change
                                    if (bDebug) System.out.println("\n\tFound match on Text! Executing alteration...");
                                    matched=true;
                                    executeAlteration(bElement, cElement);
                                }
                            }
                        }
                        
                    } else if(dnType.equals("")) {
                        // no match type, so must be for all
                        if (bDebug) System.out.println("\n\tMatch on All! Executing alteration...");
                        matched=true;
                        executeAlteration(bElement, cElement);
                    }
                }
                
                // check to see how many time a change should be performes
                if (type.equals("FIRST_OCCURRENCE") && matched) {
                    // since match found and type is set to FIRST_OCCURRENCE, break out of loop
                    break;
                }
            }                    
        }
    }
    

    /**
    * matchValue - Method that executes the matching strategies that are identified by the dnModifier.
    * The default modifier is "equals"
    * @param targetString -  Target string to be matched.
    * @param dnValue -  Value used in match
    * @param dnModifier -  Modifier used in match could be startsWith, endsWith, contains or equals.
    * equals is the default modifier.
    *
    * @return boolean -  Returns true if there is a match, or false if not
    */
    public boolean matchValue(String targetString, String dnValue, String dnModifier) {
        boolean bRet=false;
        if (dnModifier.equals("startsWith")) {
            // startWith 
            bRet=targetString.startsWith(dnValue);
        } else if (dnModifier.equals("endsWith")) {
            // endsWith 
            bRet=targetString.endsWith(dnValue);
        
        } else if (dnModifier.equals("contains")) {
            // contains 
            bRet=targetString.indexOf(dnValue) >= 0 ? true : false;
        
        } else {
            // direct match
            bRet=targetString.equals(dnValue);
        }
        return bRet;
    }
    

    /**
    * executeAlteration - This method makes the alterations that are passed as an argument to 
    * the base element that is also passed in
    *
    * @param bElement - Base element that matches the change criteria.  The changes are returned by reference
    * @param cElement - Changes that are required to be performed
    */
    public void executeAlteration(Element bElement, Element cElement) throws Exception {

        // get alterations that need to be done to the item identified from
        // the above information
        NodeList nlx=cElement.getChildNodes();
        NodeList nlChild;
        Element cElementx;

        // loop so multiple changes can be performed on the same element
        for(int kk=0; kk < nlx.getLength(); kk++) {
            // compile alteration that is required to be done
            if (nlx.item(kk) instanceof Element) {
                cElementx=(Element)nlx.item(kk);
                if (bDebug) System.out.println("\n\t* Performing Action - " + cElementx.getTagName());

                // now see what action needs to be taken on the identified element
                if (cElementx.getTagName().equals("addAttribute") || 
                    cElementx.getTagName().equals("changeAttribute") ) {
                        
                    // add or change attribute to the element
                    bElement.setAttribute(cElementx.getAttribute("name"),cElementx.getAttribute("value"));

                } else if (cElementx.getTagName().equals("prefixAttribute") || 
                    cElementx.getTagName().equals("suffixAttribute")) {
                        String attName=cElementx.getAttribute("name");
                        
                        // set newValue so it will be added if it doesn't already exist
                        String newValue=cElementx.getAttribute("value");
                        
                        // see if the attibute exists
                        if(bElement.hasAttribute(attName)) {
                            // attribute exists add value in proper place
                            if(cElementx.getTagName().equals("prefixAttrbute")) {
                                // prefix new value on to attribute
                                newValue=newValue + bElement.getAttribute(attName) ;
                            } else {
                                // append new value on to attribute attribute
                                newValue=bElement.getAttribute(attName) + newValue;
                            }
                        }
                        if (bDebug) System.out.println("\n\t" + cElementx.getTagName() + " attribute:" + attName 
                            + " final value is " + newValue);
                        // set attribute with value
                        bElement.setAttribute(attName, newValue);

                } else if (cElementx.getTagName().equals("deleteAttrbute")) {
                    // delete attribute
                    bElement.removeAttribute(cElementx.getAttribute("name"));

                } else if (cElementx.getTagName().equals("removeElement")) {
                    // remove element from parents perspective
                    bElement.getParentNode().removeChild(bElement);
                    // don't continue with changes if there are more,
                    // element is gone
                    break;
                } else if (cElementx.getTagName().equals("addElement")) {
                    // add element(s) as children to the parent that are hardcoded under 
                    // the addElement or that are stored in a file
                    Node importedNode=null;

                    String docFragFile=cElementx.getAttribute("file");
                    if (!docFragFile.equals("")) {
                        // get elements for input from file
                        try {
                            Document docFrag=readDOM(docFragFile);
                            importedNode=bElement.getOwnerDocument().importNode(docFrag.getDocumentElement(),true);
                            //bElement.appendChild(bElement.getOwnerDocument().importNode(docFrag.getDocumentElement(),true));
                        } catch (Exception ee) {
                            if (bDebug) System.out.println("Exception encounted when reading/inserting fragment '" + docFragFile + "'***");
                            throw ee;
                        }

                    } else {
                        // get elements to append from addElement children
                        NodeList nlxx=cElementx.getChildNodes();

                        // loop so can add multiple elements that may be the children of addElement
                        for(int ll=0; ll < nlxx.getLength(); ll++) {
                            // compile alteration that is required to be done
                            if (nlxx.item(ll) instanceof Element) {
                                importedNode=bElement.getOwnerDocument().importNode(nlxx.item(ll),true);
                                //bElement.appendChild(bElement.getOwnerDocument().importNode(nlxx.item(ll),true));
                            }
                        }
                    }

                    //
                    // find where to insert the imported node
                    String location=cElementx.getAttribute("location");                
                    String dnType=cElementx.getAttribute("dnType");                
                    String dnName=cElementx.getAttribute("dnName");
                    String eName="null";
                    Node lNode=null;
                    Element lElement=null, nElement=null;

                    if (bDebug) System.out.println("\n\t\tAddElement Match with location='" + location + "' with dnType='" + dnType + "' and dnName='" + dnName + "'");

                    if (location.equals("first")) {
                        // add the element to the beginning of the parent element
                        bElement.insertBefore(importedNode,bElement.getFirstChild());

                    } else if (location.equals("before")) {
                        // insert before the dnType element specified in dnName
                        if (dnType.equals("element")) {
                            lNode=bElement.getFirstChild();
                            while (lNode != null) {
                                if (lNode instanceof Element) {
                                    lElement=(Element)lNode;
                                    if (lElement.getNodeName().equals(dnName)) {
                                        // if match is found, break and insert
                                        break;
                                    }
                                }

                                // go to next sibling in order
                                lNode=lNode.getNextSibling();
                               }

                           // if match is not found, node will be place at the end
                           bElement.insertBefore(importedNode,lNode);

                        } else {
                            throw new UnsupportedOperationException("Only addElement that has a location set to 'before' only supports a dnType of 'element'");
                        }

                    } else if (location.equals("after")) {
                        // insert after the dnType element specified in dnName
                        if (dnType.equals("element")) {
                            lNode=bElement.getFirstChild();
                            boolean loop=true;

                            // look for element that matches dnName
                            while (lNode != null) {
                                //if (bDebug) System.out.println("\n\t\tAddElement After - looking for match in node - " + lNode.getNodeName());
                                if (lNode instanceof Element) {
                                    lElement=(Element)lNode;
                                    if (lElement.getNodeName().equals(dnName)) {
                                        if (bDebug) System.out.println("\n\t\t\tMatch found on element for dnName='" + dnName + "', look for next element that is different");

                                        // match is found, since it is "after" loop until you get an "Element" that is 
                                        // different than the matching dnName (or null) and break
                                        lNode=lNode.getNextSibling();
                                        while(lNode != null) {
                                            if (lNode instanceof Element && !((Element)lNode).getNodeName().equals(dnName)) {
                                                break; // break of inner while loop
                                            }
                                            lNode=lNode.getNextSibling();
                                        }

                                        // used only for debugging, get name of node
                                        if (lNode != null && lNode instanceof Element) eName=lNode.getNodeName(); 
                                        if (bDebug) System.out.println("\n\t\t\tFound last item of type '" + lElement.getNodeName() 
                                            + "' with nextSibling being '" + eName + "'");

                                        break; // break out of main while loop
                                    }
                                }

                               // go to next sibling in order
                               lNode=lNode.getNextSibling();
                           }

                           // if match is not found, node will be place at the end if lNode is null
                           bElement.insertBefore(importedNode,lNode);

                        } else {
                            throw new UnsupportedOperationException("Only addElement that has a location set to 'after' only supports a dnType of 'element'");
                        }

                    } else {
                        // Default, add the element to the end of the parent element
                        bElement.appendChild(importedNode);
                    }
                } else if (cElementx.getTagName().equals("addTextToElement")) {
                    // this is a very simple alter text element routine to facilitate change to the jvm-options
                    // elements in domain.xml
                    
                    System.out.println("******** in addTextTOElement **********");
                    
                    // find how to add text and wait element use
                    String location=cElementx.getAttribute("location");                
                    String dataValue=cElementx.getAttribute("value");
                    

                    nlChild=bElement.getChildNodes();
                    for (int mm=0; mm < nlChild.getLength(); mm++) {
                        if (nlChild.item(mm) instanceof Text) {
                            // have text node
                            if (bDebug) System.out.println("addTextToElement - Text - " + dataValue + " - " + location + " - " + nlChild.item(mm).getNodeValue() + "'");
                                // check to see how to add text before or after
                            if(location.equals("before")) {
                                nlChild.item(mm).setNodeValue(dataValue + nlChild.item(mm).getNodeValue());
                            } else {
                                nlChild.item(mm).setNodeValue(nlChild.item(mm).getNodeValue() + dataValue);
                            }
                            // only apply to first text node
                            break;
                        }
                    }
                }
            }                
        }
    }


    /**
    * writeDOM - This method writes out the resulting DOM to a file
    *
    * @param doc - DOM to be written out
    * @param file - The qualified file of where to write the DOM
    */
    public void writeDOM(Document doc, String file, String doctype_system, String doctype_public) throws Exception {
        DOMSource domSource=new DOMSource(doc);
        StreamResult sr=new StreamResult(new File(file));
        TransformerFactory tf=TransformerFactory.newInstance();
        Transformer t=tf.newTransformer();
        t.setOutputProperty(OutputKeys.METHOD,"xml");
        t.setOutputProperty(OutputKeys.INDENT,"no");
        
        if (doctype_public !=null) t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype_public);
        if (doctype_system !=null) t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype_system);
        t.transform(domSource, sr);
    }


    /**
    * readDOM - This method reads in XML into a DOM
    *
    * @param file - A qualified file where to read the XML
    * @return Document - The read in DOM
    * @exception - Any thrown exception that may occur during the read process
    */
    public Document readDOM(String file) throws Exception {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        return db.parse(file);
    }
    
    public Document readDOM(String file, String dtd) throws Exception {
        DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
        DocumentBuilder db=dbf.newDocumentBuilder();
        db.setEntityResolver(new NOOPHandler(dtd));
        return db.parse(file);
    }
    

    // debug variable that will enable the printing of debug information
    private static boolean bDebug=false;
    
}
