/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.common.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.apache.taglibs.standard.resources.Resources;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Support for tag handlers that evaluate XPath expressions.</p>
 *
 * @author Shawn Bayern
 * @author Ramesh Mandava ( ramesh.mandava@sun.com )
 * @author Pierre Delisle ( pierre.delisle@sun.com )
 */
// would ideally be a base class, but some of our user handlers already
// have their own parents
public class XPathUtil {
    
    //*********************************************************************
    // Constructor
    
    /**
     * Constructs a new XPathUtil object associated with the given
     * PageContext.
     */
    public XPathUtil(PageContext pc) {
        pageContext = pc;
    }    
    
    //*********************************************************************
    // Support for JSTL variable resolution
    
    // The URLs
    private static final String PAGE_NS_URL
    = "http://java.sun.com/jstl/xpath/page";
    private static final String REQUEST_NS_URL
    = "http://java.sun.com/jstl/xpath/request";
    private static final String SESSION_NS_URL
    = "http://java.sun.com/jstl/xpath/session";
    private static final String APP_NS_URL
    = "http://java.sun.com/jstl/xpath/app";
    private static final String PARAM_NS_URL
    = "http://java.sun.com/jstl/xpath/param";
    private static final String INITPARAM_NS_URL
    = "http://java.sun.com/jstl/xpath/initParam";
    private static final String COOKIE_NS_URL
    = "http://java.sun.com/jstl/xpath/cookie";
    private static final String HEADER_NS_URL
    = "http://java.sun.com/jstl/xpath/header";
    
    //*********************************************************************
    // Support for XPath evaluation
    
    private PageContext pageContext;
    private static HashMap exprCache;
    private static JSTLXPathNamespaceContext jstlXPathNamespaceContext = null;

    private static final String XPATH_FACTORY_CLASS_NAME = 
            "org.apache.taglibs.standard.tag.common.xml.JSTLXPathFactory";
    private static XPathFactory XPATH_FACTORY;
    static {
        // If the system property DEFAULT_PROPERTY_NAME + ":uri" is present, 
        // where uri is the parameter to this method, then its value is read 
        // as a class name. The method will try to create a new instance of 
        // this class by using the class loader, and returns it if it is 
        // successfully created.
        if (System.getSecurityManager() !=  null) {
             AccessController.doPrivileged(new PrivilegedAction(){
                public Object run(){
                    System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + 
                            ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, 
                            XPATH_FACTORY_CLASS_NAME);
                    return null;
                }
            });
        } else {
            System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + 
                ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, 
                XPATH_FACTORY_CLASS_NAME);
        }
        try {
            XPATH_FACTORY = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
        } catch (XPathFactoryConfigurationException xpce) {
            xpce.printStackTrace();
        }
    }
    
    /** Initialize globally useful data. */
    private synchronized static void staticInit() {
        if (jstlXPathNamespaceContext == null) {
            // register supported namespaces
            jstlXPathNamespaceContext = new JSTLXPathNamespaceContext();
            jstlXPathNamespaceContext.addNamespace("pageScope", PAGE_NS_URL);
            jstlXPathNamespaceContext.addNamespace("requestScope", REQUEST_NS_URL);
            jstlXPathNamespaceContext.addNamespace("sessionScope", SESSION_NS_URL);
            jstlXPathNamespaceContext.addNamespace("applicationScope", APP_NS_URL);
            jstlXPathNamespaceContext.addNamespace("param", PARAM_NS_URL);
            jstlXPathNamespaceContext.addNamespace("initParam", INITPARAM_NS_URL);
            jstlXPathNamespaceContext.addNamespace("header", HEADER_NS_URL);
            jstlXPathNamespaceContext.addNamespace("cookie", COOKIE_NS_URL);
            
            
            // create a HashMap to cache the expressions
            exprCache = new HashMap();
        }
    }
    
    static DocumentBuilderFactory dbf = null;
    static DocumentBuilder db = null;
    static Document d = null;
    
    static Document getDummyDocument( ) {
        try {
            if ( dbf == null ) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware( true );
                dbf.setValidating( false );
            }
            db = dbf.newDocumentBuilder();

            DOMImplementation dim = db.getDOMImplementation();
            d = dim.createDocument("http://java.sun.com/jstl", "dummyroot", null); 
            //d = db.newDocument();
            return d;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

     static Document getDummyDocumentWithoutRoot( ) {
        try {
            if ( dbf == null ) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware( true );
                dbf.setValidating( false );
            }
            db = dbf.newDocumentBuilder();

            d = db.newDocument();
            return d;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }
    
    // The following variable is used for holding the modified xpath string
    // when adapting parameter for Xalan XPath engine, where we need to have
    // a Non null context node.
    String modifiedXPath = null;
    
    /**
     * Evaluate an XPath expression to a String value. 
     */
    public String valueOf(Node n, String xpathString) throws JspTagException {
        // p("******** valueOf(" + n + ", " + xpathString + ")");
        staticInit();
        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(pageContext);
        Node contextNode = adaptParamsForXalan(n, xpathString.trim(), jxvr);

        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setNamespaceContext(jstlXPathNamespaceContext);
        xpath.setXPathVariableResolver(jxvr);
        try {
            return xpath.evaluate(xpathString, contextNode);
        } catch (XPathExpressionException ex) {
            throw new JspTagException(ex.toString(), ex);
        }
    }
    
    
    /** 
     * Evaluate an XPath expression to a boolean value. 
     */
    public boolean booleanValueOf(Node n, String xpathString)
    throws JspTagException {
        
        staticInit();
        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(pageContext);
        Node contextNode = adaptParamsForXalan(n, xpathString.trim(), jxvr);
        xpathString = modifiedXPath;
        
        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setNamespaceContext(jstlXPathNamespaceContext);
        xpath.setXPathVariableResolver(jxvr);
        try {
            return ((Boolean) xpath.evaluate(
              xpathString, contextNode, XPathConstants.BOOLEAN)).booleanValue();
        } catch (XPathExpressionException ex) {
            throw new JspTagException(
                Resources.getMessage("XPATH_ERROR_XOBJECT", ex.toString()), ex);            
        }
    }
    
    /** 
     * Evaluate an XPath expression to a List of nodes. 
     */
    public List selectNodes(Node n, String xpathString)  
        throws JspTagException {
        
        staticInit();
        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(pageContext);
        Node contextNode = adaptParamsForXalan(n, xpathString.trim(), jxvr);
        xpathString = modifiedXPath;
        
        try {
            XPath xpath = XPATH_FACTORY.newXPath();
            xpath.setNamespaceContext(jstlXPathNamespaceContext);
            xpath.setXPathVariableResolver(jxvr);
            Object nl = xpath.evaluate(
                xpathString, contextNode, JSTLXPathConstants.OBJECT);
            return new JSTLNodeList( nl );
        } catch (XPathExpressionException ex ) {
            throw new JspTagException(ex.toString(), ex);
        }
    }
    
    /** 
     * Evaluate an XPath expression to a single node. 
     */
    public Node selectSingleNode(Node n, String xpathString)
    throws JspTagException {
        //p("selectSingleNode of XPathUtil = passed node:" +
        //   "xpathString => " + n + " : " + xpathString );
        
        staticInit();
        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(pageContext);
        Node contextNode = adaptParamsForXalan(n, xpathString.trim(), jxvr);
        xpathString = modifiedXPath;
        
        try {
            XPath xpath = XPATH_FACTORY.newXPath();
            xpath.setNamespaceContext(jstlXPathNamespaceContext);
            xpath.setXPathVariableResolver(jxvr);
            return (Node) xpath.evaluate(
                xpathString, contextNode, XPathConstants.NODE);
        } catch (XPathExpressionException ex) {
            throw new JspTagException(ex.toString(), ex);            
        }
    }
    
    //*********************************************************************
    // Adapt XPath expression for integration with Xalan
   
    /**
     * To evaluate an XPath expression using Xalan, we need 
     * to create an XPath object, which wraps an expression object and provides 
     * general services for execution of that expression.
     *
     * An XPath object can be instantiated with the following information:
     *     - XPath expression to evaluate
     *     - SourceLocator 
     *        (reports where an error occurred in the XML source or 
     *        transformation instructions)
     *     - PrefixResolver
     *        (resolve prefixes to namespace URIs)
     *     - type
     *        (one of SELECT or MATCH)
     *     - ErrorListener
     *        (customized error handling)
     *
     * Execution of the XPath expression represented by an XPath object
     * is done via method execute which takes the following parameters:
     *     - XPathContext 
     *        The execution context
     *     - Node contextNode
     *        The node that "." expresses
     *     - PrefixResolver namespaceContext
     *        The context in which namespaces in the XPath are supposed to be 
     *        expanded.
     *
     * Given all of this, if no context node is set for the evaluation
     * of the XPath expression, one must be set so Xalan 
     * can successfully evaluate a JSTL XPath expression.
     * (it will not work if the context node is given as a varialbe
     * at the beginning of the expression)
     *
     * @@@ Provide more details...
     */
    protected Node adaptParamsForXalan(Node n, 
                                       String xpath, 
                                       XPathVariableResolver jxvr) {
        Node boundDocument = null;
        
        modifiedXPath = xpath;
        String origXPath = xpath;
        boolean whetherOrigXPath = true;
        
        // If contextNode is not null then  just pass the values to Xalan XPath
        if ( n != null ) {
            return n;
        }
        
        if (  xpath.startsWith("$")  ) {
            // JSTL uses $scopePrefix:varLocalName/xpath expression

            String varQName=  xpath.substring( xpath.indexOf("$")+1);
            if ( varQName.indexOf("/") > 0 ) {
                varQName = varQName.substring( 0, varQName.indexOf("/"));
            }
            String varPrefix =  null;
            String varLocalName =  varQName;
            if ( varQName.indexOf( ":") >= 0 ) {
                varPrefix = varQName.substring( 0, varQName.indexOf(":") );
                varLocalName = varQName.substring( varQName.indexOf(":")+1 );
            }
            
            if ( xpath.indexOf("/") > 0 ) {
                xpath = xpath.substring( xpath.indexOf("/"));
            } else  {
                xpath = "/*";
                whetherOrigXPath = false; 
            }
           
            
            try {
                Object varObject=((JSTLXPathVariableResolver) jxvr).getVariableValue("", varPrefix,
                    varLocalName);
                //p( "varObject => : its Class " +varObject +
                // ":" + varObject.getClass() );
                
                if ( Class.forName("org.w3c.dom.Document").isInstance(
                    varObject ) )  {
                    //boundDocument = ((Document)varObject).getDocumentElement();
                    boundDocument = ((Document)varObject);
                } else {
                    
                    //p("Creating a Dummy document to pass " +
                    // " onto as context node " );
                    
                    if ( Class.forName("org.apache.taglibs.standard.tag.common.xml.JSTLNodeList").isInstance( varObject ) ) {
                        Document newDocument = getDummyDocument();

                        JSTLNodeList jstlNodeList = (JSTLNodeList)varObject;
                        if   ( jstlNodeList.getLength() == 1 ) { 
                            if ( Class.forName("org.w3c.dom.Node").isInstance(
                                jstlNodeList.elementAt(0) ) ) { 
                                Node node = (Node)jstlNodeList.elementAt(0);
                                Document doc = getDummyDocumentWithoutRoot();
                                Node importedNode = doc.importNode( node, true);
                                doc.appendChild (importedNode );
                                boundDocument = doc;
                                if ( whetherOrigXPath ) {
                                    xpath="/*" + xpath;
                                }

                            } else {

                                //Nodelist with primitive type
                                Object myObject = jstlNodeList.elementAt(0);

                                //p("Single Element of primitive type");
                                //p("Type => " + myObject.getClass());

                                xpath = myObject.toString();

                                //p("String value ( xpathwould be this) => " + xpath);
                                boundDocument = newDocument;
                            } 
                            
                        } else {

                            Element dummyroot = newDocument.getDocumentElement();
                            for ( int i=0; i< jstlNodeList.getLength(); i++ ) {
                                Node currNode = (Node)jstlNodeList.item(i);
                            
                                Node importedNode = newDocument.importNode(
                                    currNode, true );

                                //printDetails ( newDocument);

                                dummyroot.appendChild( importedNode );

                                //p( "Details of the document After importing");
                                //printDetails ( newDocument);
                            }
                            boundDocument = newDocument;
                            // printDetails ( boundDocument );
                            //Verify :As we are adding Document element we need
                            // to change the xpath expression.Hopefully this
                            // won't  change the result

                            xpath = "/*" +  xpath;
                        }
                    } else if ( Class.forName("org.w3c.dom.Node").isInstance(
                        varObject ) ) {
                        boundDocument = (Node)varObject;
                    } else {
                        boundDocument = getDummyDocument();
                        xpath = origXPath;
                    }
                    
                    
                }
            } catch ( UnresolvableException ue ) {
                // FIXME: LOG
                // p("Variable Unresolvable :" + ue.getMessage());
                ue.printStackTrace();
            } catch ( ClassNotFoundException cnf ) {
                // Will never happen
            }
        } else { 
            //p("Not encountered $ Creating a Dummydocument 2 "+
            //   "pass onto as context node " );
            boundDocument = getDummyDocument();
        }
     
        modifiedXPath = xpath;
        //p("Modified XPath::boundDocument =>" + modifiedXPath +
        //    "::" + boundDocument );
         
        return boundDocument;
    }
    

    //*********************************************************************
    // 
    
    
    //*********************************************************************
    // Static support for context retrieval from parent <forEach> tag
    
    public static Node getContext(Tag t) throws JspTagException {
        ForEachTag xt =
        (ForEachTag) TagSupport.findAncestorWithClass(
        t, ForEachTag.class);
        if (xt == null)
            return null;
        else
            return (xt.getContext());
    }
    
    //*********************************************************************
    // Utility methods
    
    private static void p(String s) {
        System.out.println("[XPathUtil] " + s);
    }
    
    public static void printDetails(Node n) {
        p("\n\nDetails of Node = > " + n ) ;
        p("Name:Type:Node Value = > " + n.getNodeName() +
        ":" + n.getNodeType() + ":" + n.getNodeValue()  ) ;
        p("Namespace URI : Prefix : localName = > " +
        n.getNamespaceURI() + ":" +n.getPrefix() + ":" + n.getLocalName());
        p("\n Node has children => " + n.hasChildNodes() );
        if ( n.hasChildNodes() ) {
            NodeList nl = n.getChildNodes();
            p("Number of Children => " + nl.getLength() );
            for ( int i=0; i<nl.getLength(); i++ ) {
                Node childNode = nl.item(i);
                printDetails( childNode );
            }
        }
    }    
}

class JSTLNodeList extends Vector implements NodeList   {
    
    Vector nodeVector;

    public JSTLNodeList ( Vector nodeVector ) {
        this.nodeVector = nodeVector;
    }

    public JSTLNodeList ( NodeList nl ) {
        nodeVector = new Vector();
        //p("[JSTLNodeList] nodelist details");
        for ( int i=0; i<nl.getLength(); i++ ) {
            Node currNode = nl.item(i);
            //XPathUtil.printDetails ( currNode );
            nodeVector.add(i, nl.item(i) );
        }
    }

    public JSTLNodeList ( Node n ) {
        nodeVector = new Vector();
        nodeVector.addElement( n );
    }

    public JSTLNodeList (Object o) {
        nodeVector = new Vector();
        
        if (o instanceof NodeList) {
            NodeList nl = (NodeList)o;
            for ( int i=0; i<nl.getLength(); i++ ) {
                Node currNode = nl.item(i);
                //XPathUtil.printDetails ( currNode );
                nodeVector.add(i, nl.item(i) );
            }
        } else {
            nodeVector.addElement( o );
        }
    }

    public Node item ( int index ) {
        return (Node)nodeVector.elementAt( index );
    }

    public Object elementAt ( int index ) {
        return nodeVector.elementAt( index );
    }

    public Object get ( int index ) {
        return nodeVector.get( index );
    }

    public int getLength (  ) {
        return nodeVector.size( );
    }

    public int size (  ) {
        //p("JSTL node list size => " + nodeVector.size() );
        return nodeVector.size( );
    }

    // Can implement other Vector methods to redirect those methods to 
    // the vector in the variable param. As we are not using them as part 
    // of this implementation we are not doing that here. If this changes
    // then we need to override those methods accordingly  

}
         



