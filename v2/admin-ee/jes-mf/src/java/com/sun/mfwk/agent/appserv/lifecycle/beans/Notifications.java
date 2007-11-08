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

/**
 *	This generated bean class Notifications
 *	matches the schema element 'notifications'.
 *
 *	Generated on Sun Aug 28 23:45:36 PDT 2005
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the bean graph.
 *
 * 	notifications <notifications> : Notifications
 * 		[attr: domain CDATA #REQUIRED ]
 * 		objectName <object-name> : ObjectName[0,n]
 * 			[attr: monitoring-mbean-name-template CDATA #IMPLIED ]
 * 			attribute <attribute> : Attribute[1,n]
 * 				name <name> : String
 * 				value <value> : String
 * 			mapping <mapping> : Mapping[0,n]
 * 				runtimeMbeanAttributeName <runtime-mbean-attribute-name> : String
 * 				monitoringMbeanAttributeValue <monitoring-mbean-attribute-value> : String
 *
 * @Generated
 */

package com.sun.mfwk.agent.appserv.lifecycle.beans;

public class Notifications {
	public static final String DOMAIN = "Domain";	// NOI18N
	public static final String OBJECT_NAME = "ObjectName";	// NOI18N

	private java.lang.String _Domain;
	private java.util.List _ObjectName = new java.util.ArrayList();	// List<ObjectName>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public Notifications() {
		_Domain = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Notifications(java.lang.String domain) {
		_Domain = domain;
	}

	/**
	 * Deep copy
	 */
	public Notifications(com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Notifications(com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications source, boolean justData) {
		_Domain = source._Domain;
		for (java.util.Iterator it = source._ObjectName.iterator(); 
			it.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName srcElement = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it.next();
			_ObjectName.add((srcElement == null) ? null : newObjectName(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is mandatory
	public void setDomain(java.lang.String value) {
		_Domain = value;
	}

	public java.lang.String getDomain() {
		return _Domain;
	}

	// This attribute is an array, possibly empty
	public void setObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName[] value) {
		if (value == null)
			value = new ObjectName[0];
		_ObjectName.clear();
		((java.util.ArrayList) _ObjectName).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ObjectName.add(value[i]);
		}
	}

	public void setObjectName(int index, com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName value) {
		_ObjectName.set(index, value);
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName[] getObjectName() {
		ObjectName[] arr = new ObjectName[_ObjectName.size()];
		return (ObjectName[]) _ObjectName.toArray(arr);
	}

	public java.util.List fetchObjectNameList() {
		return _ObjectName;
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName getObjectName(int index) {
		return (ObjectName)_ObjectName.get(index);
	}

	// Return the number of objectName
	public int sizeObjectName() {
		return _ObjectName.size();
	}

	public int addObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName value) {
		_ObjectName.add(value);
		int positionOfNewItem = _ObjectName.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName value) {
		int pos = _ObjectName.indexOf(value);
		if (pos >= 0) {
			_ObjectName.remove(pos);
		}
		return pos;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName newObjectName() {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName newObjectName(ObjectName source, boolean justData) {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName(source, justData);
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "notifications", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "notifications";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		// domain is an attribute with namespace null
		if (_Domain != null) {
			out.write(" domain='");
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _Domain, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _ObjectName.iterator(); it.hasNext(); 
			) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it.next();
			if (element != null) {
				element.writeNode(out, "object-name", null, nextIndent, namespaceMap);
			}
		}
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	public static Notifications read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static Notifications read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static Notifications readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static Notifications read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static Notifications read(org.w3c.dom.Document document) {
		Notifications aNotifications = new Notifications();
		aNotifications.readFromDocument(document);
		return aNotifications;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6, attrName.length());
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
				it.hasNext(); ) {
				String prefix = (String) it.next();
				String ns = (String) namespacePrefixes.get(prefix);
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("domain");
			if (attr != null) {
				attrValue = attr.getValue();
				_Domain = attrValue;
			}
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "object-name") {
				ObjectName aObjectName = newObjectName();
				aObjectName.readNode(childNode, namespacePrefixes);
				_ObjectName.add(aObjectName);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public static class ValidateException extends Exception {
		private java.lang.Object failedBean;
		private String failedPropertyName;
		private FailureType failureType;
		public ValidateException(String msg, String failedPropertyName, java.lang.Object failedBean) {
			super(msg);
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public ValidateException(String msg, FailureType ft, String failedPropertyName, java.lang.Object failedBean) {
			super(msg);
			this.failureType = ft;
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public String getFailedPropertyName() {return failedPropertyName;}
		public FailureType getFailureType() {return failureType;}
		public java.lang.Object getFailedBean() {return failedBean;}
		public static class FailureType {
			private final String name;
			private FailureType(String name) {this.name = name;}
			public String toString() { return name;}
			public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
			public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
			public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
			public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
		}
	}

	public void validate() throws com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException {
		boolean restrictionFailure = false;
		// Validating property domain
		if (getDomain() == null) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("getDomain() == null", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "domain", this);	// NOI18N
		}
		// Validating property objectName
		for (int _index = 0; _index < sizeObjectName(); ++_index) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element = getObjectName(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "domain")
			setDomain((java.lang.String)value);
		else if (name == "objectName")
			addObjectName((ObjectName)value);
		else if (name == "objectName[]")
			setObjectName((ObjectName[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Notifications");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "domain")
			return getDomain();
		if (name == "objectName[]")
			return getObjectName();
		throw new IllegalArgumentException(name+" is not a valid property name for Notifications");
	}

	public String nameSelf() {
		return "/Notifications";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _Domain) {
				if (returnConstName) {
					return DOMAIN;
				} else if (returnSchemaName) {
					return "domain";
				} else if (returnXPathName) {
					return "@domain";
				} else {
					return "Domain";
				}
			}
		}
		if (childObj instanceof ObjectName) {
			ObjectName child = (ObjectName) childObj;
			int index = 0;
			for (java.util.Iterator it = _ObjectName.iterator(); 
				it.hasNext(); ) {
				com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it.next();
				if (child == element) {
					if (returnConstName) {
						return OBJECT_NAME;
					} else if (returnSchemaName) {
						return "object-name";
					} else if (returnXPathName) {
						return "object-name[position()="+index+"]";
					} else {
						return "ObjectName."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public java.lang.Object[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		java.lang.Object[] result = new java.lang.Object[children.size()];
		return (java.lang.Object[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
		for (java.util.Iterator it = _ObjectName.iterator(); it.hasNext(); 
			) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications && equals((com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications) o);
	}

	public boolean equals(com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Domain == null ? inst._Domain == null : _Domain.equals(inst._Domain))) {
			return false;
		}
		if (sizeObjectName() != inst.sizeObjectName())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ObjectName.iterator(), it2 = inst._ObjectName.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it.next();
			com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName element2 = (com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Domain == null ? 0 : _Domain.hashCode());
		result = 37*result + (_ObjectName == null ? 0 : _ObjectName.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!--
    Document   : listener.dtd
    Created on : Aug 20, 2005, 4:52PM
    Description: DTD to defines the notifications to listen for.
-->

<!--
-->
<!ELEMENT notifications (object-name*)>
<!ATTLIST notifications
     domain CDATA  #REQUIRED>


<!--
attribute subelement provides notification filtering information.
Notifcatios from runtime mbeans are filtered by maching the name/vaue
pairsspecified by attribute subelement

mapping sub-element provide information to convert runtime mbean objectname to
corresponding monitoring mbean objectname using monitoring-mbean-name-template attribute
-->
<!ELEMENT object-name (attribute+, mapping*)>
<!ATTLIST object-name
     monitoring-mbean-name-template CDATA  #IMPLIED>


<!--
Provides attribute name and value of runtime mbean used to filter the notifications
-->
<!ELEMENT attribute (name, value)>


<!--
Provides mapping from runtime mbean attribute name to monitoring mbean attribute name
i.e value of runtime mbean attribute needs to be substituted for the vaule of monitoring
mbean attribute in monitoring-mbean-name-template to form the monitoring mbean object name.
-->
<!ELEMENT mapping (runtime-mbean-attribute-name, monitoring-mbean-attribute-value)>


<!--
-->
<!ELEMENT name (#PCDATA)>


<!--
-->
<!ELEMENT value (#PCDATA)>


<!--
-->
<!ELEMENT runtime-mbean-attribute-name (#PCDATA)>


<!--
-->
<!ELEMENT monitoring-mbean-attribute-value (#PCDATA)>

*/
