/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.xml;

import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import oracle.toplink.essentials.exceptions.XMLParseException;
import oracle.toplink.essentials.exceptions.ValidationException;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.XPathEngine;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.XMLException;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.XMLExceptionHandler;

/**
 * Utility class used for handling element inspection.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLHelper {
	private Document m_document;
    private ClassLoader m_loader;
    private String m_documentName;
    private String m_defaultPackage;
	private XPathEngine m_xPathEngine;
	
	/**
	 * INTERNAL:
	 */
	protected XMLHelper(Document document, ClassLoader loader) {
		m_xPathEngine = XPathEngine.getInstance();
		m_loader = loader;
		m_document = document;
		
		Node node = getNode(document, new String[] {XMLConstants.ENTITY_MAPPINGS, XMLConstants.PACKAGE, XMLConstants.TEXT});
        
        if (node != null && node.getNodeValue() != null) {
        	m_defaultPackage = node.getNodeValue();
        } else {
        	m_defaultPackage = "";
        }
	}
    
    /**
     * INTERNAL:
     */
    public XMLHelper(Document document, String fileName, ClassLoader loader) {
        this(document, loader);
        m_documentName = fileName;
    }
    
    /**
     * INTERNAL:
     */
    public List<String> getCascadeTypes(Node node) {
        ArrayList<String> cTypes = new ArrayList<String>();
        
        NodeList cascadeTypes = getNodes(node, XMLConstants.CASCADE, XMLConstants.ALL_CHILDREN);
		for (int i = 0; i < cascadeTypes.getLength(); i++) {
            cTypes.add(cascadeTypes.item(i).getLocalName());
        }
        
        return cTypes;
    }
    
    /**
     * INTERNAL:
     */
	public Class getClassForName(String className) {
		return MetadataHelper.getClassForName(getFullyQualifiedClassName(className), m_loader);
	}

    /**
	 * INTERNAL:
	 * Return the Class for a given node. This method assumes that the node 
     * requires a class attribute, i.e. entity or mapped-superclass.
	 */
	public Class getClassForNode(Node node) {
		return MetadataHelper.getClassForName(getClassNameForNode(node), m_loader);
	}
	
	/**
	 * INTERNAL:
	 * Return the fully qualified class name for a given node. This method 
     * assumes that the node requires a class attribute, i.e. entity or 
     * mapped-superclass.
	 */
	public String getClassNameForNode(Node node) {
        return getFullyQualifiedClassName(getNodeValue(node, XMLConstants.ATT_CLASS));
	}
    
	/**
	 * INTERNAL:
	 * Return the instance document associated with this helper.
	 */
	public Document getDocument() {
		return m_document;
	}
    
    /**
	 * INTERNAL:
	 * Return the instance document name associated with this helper.
	 */
	public String getDocumentName() {
		return m_documentName;
	}

    /**
     * INTERNAL:
     */
    public String getFetchTypeDefaultEAGER(Node node) {
        return getNodeValue(node, XMLConstants.ATT_FETCH, MetadataConstants.EAGER);
    }
    
    /**
     * INTERNAL:
     */
    public String getFetchTypeDefaultLAZY(Node node) {
        return getNodeValue(node, XMLConstants.ATT_FETCH, MetadataConstants.LAZY);
    }
    
    /**
     * INTERNAL:
     * This convenience method will attempt to fully qualify a class name if 
     * required. This assumes that the className value is non-null, and a 
     * "qualified" class name contains at least one '.'
     */
	public String getFullyQualifiedClassName(String className) {
        return getFullyQualifiedClassName(className, m_defaultPackage);
    }
    
    /**
     * INTERNAL:
     * This convenience method will attempt to fully qualify a class name if 
     * required. This assumes that the className value is non-null, and a 
     * "qualified" class name contains at least one '.'
     */
    public static String getFullyQualifiedClassName(String className, String packageName) {
        // if there is no global package defined or the class name is qualified, return className
        if (packageName.equals("") || className.indexOf(".") != -1) {
            return className;
        }
        
        // prepend the package to the class name
        // format of global package is "foo.bar."
        if (packageName.endsWith(".")) {
            return (packageName + className);
        }
        
        // format of global package is "foo.bar"
        return (packageName + "." + className);
    }
    
    /**
     * INTERNAL:
     * This convenience method determines the type of relationship mapping the
     * node represents, and returns the appropriate logging context.
     */
    public String getLoggingContextForDefaultMappingReferenceClass(Node mappingNode) {
        if (mappingNode.getLocalName().equals(XMLConstants.ONE_TO_ONE)) {
            return MetadataLogger.ONE_TO_ONE_MAPPING_REFERENCE_CLASS;
        }
        if (mappingNode.getLocalName().equals(XMLConstants.ONE_TO_MANY)) {
            return MetadataLogger.ONE_TO_MANY_MAPPING_REFERENCE_CLASS;
        }
        if (mappingNode.getLocalName().equals(XMLConstants.MANY_TO_ONE)) {
            return MetadataLogger.MANY_TO_ONE_MAPPING_REFERENCE_CLASS;
        }
        // assume many-to-many
        return MetadataLogger.MANY_TO_MANY_MAPPING_REFERENCE_CLASS;
    }
    
    /**
     * INTERNAL:
     */
    public String getMappedBy(Node node) {
        return getNodeValue(node, XMLConstants.ATT_MAPPED_BY, "");
    }
    
    /**
     * INTERNAL:
     * Get a node off the given node.
     */
    public Node getNode(Node node, String xPath) {
        return getNode(node, new String[] {xPath});
    }
    
    /**
     * INTERNAL:
     * Get a node off the given node.
     */
    public Node getNode(Node node, String[] xPath) {
        return m_xPathEngine.selectSingleNode(node, xPath);
    }
    
    /**
     * INTERNAL:
     * Get a node off the document node.
     */
    public Node getNode(String[] xPath) {
        return getNode(m_document, xPath);
    }
    
    /**
     * INTERNAL:
     * Get the nodes off the given node.
     */
    public NodeList getNodes(String xPath1, String xPath2) {
        return getNodes(m_document, new String[] {xPath1, xPath2});
    }
    
    /**
     * INTERNAL:
     * Get the nodes off the given node.
     */
    public NodeList getNodes(String[] xPath) {
        return getNodes(m_document, xPath);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeTextValue(Node node, String xPath) {
        return getNodeValue(node, new String[] {xPath, XMLConstants.TEXT});
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeTextValue(String xPath1, String xPath2) {
        return getNodeValue(m_document, new String[] {xPath1, xPath2, XMLConstants.TEXT});
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeTextValue(String xPath1, String xPath2, String defaultValue) {
        return getNodeValue(m_document, new String[] {xPath1, xPath2, XMLConstants.TEXT}, defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(Node node, String xPath) {
        return getNodeValue(node, new String[] {xPath});
    }
    
    /**
     * INTERNAL:
     */
    public boolean getNodeValue(Node node, String xPath, boolean defaultValue) {
        return getNodeValue(node, new String[] {xPath}, defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public Class getNodeValue(Node node, String xPath, Class defaultValue) {
        return getNodeValue(node, new String[] {xPath}, defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public int getNodeValue(Node node, String xPath, int defaultValue) {
        return getNodeValue(node, new String[] {xPath}, defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(Node node, String xPath, String defaultValue) {
        return getNodeValue(node, new String[] {xPath}, defaultValue);
    }

    /**
     * INTERNAL:
     */
    public NodeList getNodes(Node node, String xPath) {
        return getNodes(node, new String[] {xPath});
    }
    
    /**
     * INTERNAL:
     */
    public NodeList getNodes(Node node, String xPath1, String xPath2) {
        return getNodes(node, new String[] {xPath1, xPath2});
    }
    
    /**
     * INTERNAL:
     */
    public NodeList getNodes(Node node, String[] xPath) {
        return m_xPathEngine.selectNodes(node, xPath);
    }
    
    /**
     * INTERNAL:
     */
    public boolean getNodeValue(Node node, String[] xPath, boolean defaultValue) {
        return getValue(getNode(node, xPath), defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public Class getNodeValue(Node node, String[] xPath, Class defaultValue) {
        return getValue(getNode(node, xPath), defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public int getNodeValue(Node node, String[] xPath, int defaultValue) {
        return getValue(getNode(node, xPath), defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(Node node, String[] xPath, String defaultValue) {
        return getValue(getNode(node, xPath), defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(Node node, String[] xPath) {
        return getNodeValue(node, xPath, "");
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(String[] xPath) {
        return getNodeValue(xPath, "");
    }
    
    /**
     * INTERNAL:
     */
    public int getNodeValue(String[] xPath, int defaultValue) {
        return getValue(getNode(xPath), defaultValue);
    }
    
    /**
     * INTERNAL:
     */
    public String getNodeValue(String[] xPath, String defaultValue) {
        return getValue(getNode(xPath), defaultValue);
    }

	/**
	 * INTERNAL:
	 */
	public String getPackage() {
		return m_defaultPackage;
	}
    
    /**
     * INTERNAL:
     */    
    public Class getTargetEntity(Node node) {
        return getNodeValue(node, XMLConstants.ATT_TARGET_ENTITY, void.class);
    }
    
    /**
     * INTERNAL:
     */
    public NodeList getTextColumnNodes(Node node) {
        return getNodes(node, new String[] {XMLConstants.COLUMN_NAME, XMLConstants.TEXT});
    }

    /**
     * INTERNAL:
     */
    private boolean getValue(Node node, boolean defaultValue) {
        if (node == null) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(node.getNodeValue());
        }
    }
    
    /**
     * INTERNAL:
     */
    private Class getValue(Node node, Class defaultValue) {
        if (node == null) {
            return defaultValue;
        } else {
            return getClassForName(node.getNodeValue());
        }
    }
    
    /**
     * INTERNAL:
     */
    private int getValue(Node node, int defaultValue) {
        if (node == null) {
            return defaultValue;
        } else {
            return Integer.parseInt(node.getNodeValue());
        }
    }
    
    /**
     * INTERNAL:
     */
    private String getValue(Node node, String defaultValue) {
        if (node == null) {
            return defaultValue;
        } else {
            String value = node.getNodeValue();
            if (value == null) {
                return defaultValue;
            } else {
                return value;
            }
        }
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasNode(Node node, String xPath) {
        return getNode(node, xPath) != null;
    }
    
    /**
     * INTERNAL:
     */
    public boolean isOptional(Node node) {
        return getNodeValue(node, XMLConstants.ATT_OPTIONAL, true);
    }
    
    /**
     * INTERNAL:
     * Locate a node in the DOM tree for a given class.  
     */
    public Node locateEmbeddableNode(Class cls) {
        return locateNode(cls, XMLConstants.EMBEDDABLE);
    }
    
    /**
     * INTERNAL:
     * Locate a node in the DOM tree for a given class.
     */
    public Node locateEntityNode(Class cls) {
        return locateNode(cls, XMLConstants.ENTITY);
    }
    
    /**
     * INTERNAL:
     * Locate a node in the DOM tree for a given class.
     */
    public Node locateMappedSuperclassNode(Class cls) {
        return locateNode(cls, XMLConstants.MAPPED_SUPERCLASS);
    }
    
    /**
     * INTERNAL:
     * Locate a node in the DOM tree for the given class. Will look for
     * an entity, embeddable, or mapped-superclass node with @class matching
     * the class name.
     */
    public Node locateNode(Class cls) {
    	Node result = null;
        result = locateEntityNode(cls);
    	
        if (result == null) {
        	result = locateMappedSuperclassNode(cls);
    	}
        
    	if (result == null) {
    		result = locateEmbeddableNode(cls);
    	}
        
    	return result;
    }
    
    /**
     * INTERNAL:
     * Locate a node in the DOM tree for a given class.
     * The search string should be used as follows:
     *  - For an entity: XMLConstants.ENTITY 
     *  - For an embeddable: XMLConstants.EMBEDDABLE
     *  - For a mapped superclass: XMLConstants.MAPPED_SUPERCLASS
     *  Or call locateNode which will check them all. For efficiency, it looks
     *  for an entity first.
     */
    private Node locateNode(Class cls, String searchString) {
        NodeList nodes = getNodes(m_document, XMLConstants.ENTITY_MAPPINGS, searchString);

        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                // process @class (required)
                if (getClassNameForNode(node).equals(cls.getName())) {
                    return node;
                }
            }
        }
        
        return null;
    }

    /**
     * INTERNAL:
     * Locate a node in the DOM tree for a given attribute name.  
     */
     // WIP - method may go away.
    public Node locateNodeForAttribute(Node node, String attributeName) {
        NodeList attributeNodes = getNodes(node, XMLConstants.ATTRIBUTES, XMLConstants.ALL_CHILDREN);

        if (attributeNodes != null) {
            Node attributeNode;
            for (int i = 0; i < attributeNodes.getLength(); i++) {
            	attributeNode = attributeNodes.item(i);
                // process @name (required)
                if (getNodeValue(attributeNode, XMLConstants.ATT_NAME).equals(attributeName)) {
                    return attributeNode;
                }
            }
        }
        
        return null;
    }

    /**
     * INTERNAL:
     * Return the root entity in an entity class hierarchy
     */
    public Class locateRootEntity(Class entityClass) {    
    	Class superclass = entityClass.getSuperclass();
        if (superclass != null) {
            Node entityNode = locateEntityNode(superclass);
            
            if (entityNode != null) {
                return locateRootEntity(superclass);
            }
        }
        
        return entityClass;
    }

    /**
     * INTERNAL:
     * Indicates if a given node has a primary-key-join-column sub-element.
     */
    public boolean nodeHasPrimaryKeyJoinColumns(Node node) {
    	if (node == null) {
    		return false;
    	}

    	NodeList nodes = getNodes(node, XMLConstants.PK_JOIN_COLUMN);
    	return (nodes != null && nodes.getLength() > 0);
    }
    
    /**
     * INTERNAL:
     * Indicates if a given node has a primary-key-join-column sub-element.
     */
    public boolean nodeHasJoinColumns(Node node) {
    	if (node == null) {
    		return false;
    	}

    	NodeList nodes = getNodes(node, XMLConstants.JOIN_COLUMN);
    	return (nodes != null && nodes.getLength() > 0);
    }
    
    /**
     * INTERNAL:
     * Build a DOM from an instance document using the provided URL.
     */
    public static Document parseDocument(InputStream xmlDocumentInputStream, String documentName, ClassLoader loader) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setAttribute(XMLConstants.SCHEMA_LANGUAGE, XMLConstants.XML_SCHEMA);
        dbf.setValidating(true);
        
        // attempt to load the schema from the classpath
        URL schemaURL = loader.getResource(XMLConstants.ORM_SCHEMA_NAME);
        if (schemaURL != null) {
        	dbf.setAttribute(XMLConstants.JAXP_SCHEMA_SOURCE, schemaURL.toString());
        }
        
        // create a document builder
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pex) {
            throw XMLParseException.exceptionCreatingDocumentBuilder(documentName, pex);
        }
        
        // set the parse exception handler
        XMLExceptionHandler xmlExceptionHandler = new XMLExceptionHandler();
        db.setErrorHandler(xmlExceptionHandler);
        
        // parse the document
        Document doc = null;
        try {
            doc = db.parse(xmlDocumentInputStream);
        } catch (IOException ioex) {
            throw XMLParseException.exceptionReadingXMLDocument(documentName, ioex);
        } catch (SAXException saxex) {
        	// XMLExceptionHandler will handle parse exceptions
        }
        
        XMLException xmlEx = xmlExceptionHandler.getXMLException();
        if (xmlEx != null) {
        	throw ValidationException.invalidEntityMappingsDocument(documentName, xmlEx);
        }
        
        return doc;
    }
    
    /**
     * INTERNAL:
     * Update the loader after it changes.
     */
    public void setLoader(ClassLoader loader) {
        m_loader = loader;
    }

    /**
     * INTERNAL:
     * Get the loader.
     */
    public ClassLoader getClassLoader() {
        return m_loader;
    }
}
