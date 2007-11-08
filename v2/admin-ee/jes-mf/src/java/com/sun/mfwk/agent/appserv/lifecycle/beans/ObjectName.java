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
 *	This generated bean class ObjectName
 *	matches the schema element 'object-name'.
 *  The root bean class is Notifications
 *
 *	Generated on Sun Aug 28 23:45:36 PDT 2005
 * @Generated
 */

package com.sun.mfwk.agent.appserv.lifecycle.beans;

public class ObjectName {
	public static final String MONITORINGMBEANNAMETEMPLATE = "MonitoringMbeanNameTemplate";	// NOI18N
	public static final String ATTRIBUTE = "Attribute";	// NOI18N
	public static final String MAPPING = "Mapping";	// NOI18N

	private java.lang.String _MonitoringMbeanNameTemplate;
	private java.util.List _Attribute = new java.util.ArrayList();	// List<Attribute>
	private java.util.List _Mapping = new java.util.ArrayList();	// List<Mapping>

	/**
	 * Normal starting point constructor.
	 */
	public ObjectName() {
	}

	/**
	 * Required parameters constructor
	 */
	public ObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute[] attribute) {
		if (attribute!= null) {
			((java.util.ArrayList) _Attribute).ensureCapacity(attribute.length);
			for (int i = 0; i < attribute.length; ++i) {
				_Attribute.add(attribute[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public ObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public ObjectName(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName source, boolean justData) {
		_MonitoringMbeanNameTemplate = source._MonitoringMbeanNameTemplate;
		for (java.util.Iterator it = source._Attribute.iterator(); 
			it.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute srcElement = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it.next();
			_Attribute.add((srcElement == null) ? null : newAttribute(srcElement, justData));
		}
		for (java.util.Iterator it = source._Mapping.iterator(); 
			it.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping srcElement = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it.next();
			_Mapping.add((srcElement == null) ? null : newMapping(srcElement, justData));
		}
	}

	// This attribute is optional
	public void setMonitoringMbeanNameTemplate(java.lang.String value) {
		_MonitoringMbeanNameTemplate = value;
	}

	public java.lang.String getMonitoringMbeanNameTemplate() {
		return _MonitoringMbeanNameTemplate;
	}

	// This attribute is an array containing at least one element
	public void setAttribute(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute[] value) {
		if (value == null)
			value = new Attribute[0];
		_Attribute.clear();
		((java.util.ArrayList) _Attribute).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Attribute.add(value[i]);
		}
	}

	public void setAttribute(int index, com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute value) {
		_Attribute.set(index, value);
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute[] getAttribute() {
		Attribute[] arr = new Attribute[_Attribute.size()];
		return (Attribute[]) _Attribute.toArray(arr);
	}

	public java.util.List fetchAttributeList() {
		return _Attribute;
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute getAttribute(int index) {
		return (Attribute)_Attribute.get(index);
	}

	// Return the number of attribute
	public int sizeAttribute() {
		return _Attribute.size();
	}

	public int addAttribute(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute value) {
		_Attribute.add(value);
		int positionOfNewItem = _Attribute.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeAttribute(com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute value) {
		int pos = _Attribute.indexOf(value);
		if (pos >= 0) {
			_Attribute.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setMapping(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping[] value) {
		if (value == null)
			value = new Mapping[0];
		_Mapping.clear();
		((java.util.ArrayList) _Mapping).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Mapping.add(value[i]);
		}
	}

	public void setMapping(int index, com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping value) {
		_Mapping.set(index, value);
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping[] getMapping() {
		Mapping[] arr = new Mapping[_Mapping.size()];
		return (Mapping[]) _Mapping.toArray(arr);
	}

	public java.util.List fetchMappingList() {
		return _Mapping;
	}

	public com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping getMapping(int index) {
		return (Mapping)_Mapping.get(index);
	}

	// Return the number of mapping
	public int sizeMapping() {
		return _Mapping.size();
	}

	public int addMapping(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping value) {
		_Mapping.add(value);
		int positionOfNewItem = _Mapping.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeMapping(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping value) {
		int pos = _Mapping.indexOf(value);
		if (pos >= 0) {
			_Mapping.remove(pos);
		}
		return pos;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute newAttribute() {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute newAttribute(Attribute source, boolean justData) {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping newMapping() {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping newMapping(Mapping source, boolean justData) {
		return new com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "object-name";
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
		// monitoring-mbean-name-template is an attribute with namespace null
		if (_MonitoringMbeanNameTemplate != null) {
			out.write(" monitoring-mbean-name-template='");
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _MonitoringMbeanNameTemplate, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _Attribute.iterator(); it.hasNext(); 
			) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it.next();
			if (element != null) {
				element.writeNode(out, "attribute", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _Mapping.iterator(); it.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it.next();
			if (element != null) {
				element.writeNode(out, "mapping", null, nextIndent, namespaceMap);
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
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("monitoring-mbean-name-template");
			if (attr != null) {
				attrValue = attr.getValue();
				_MonitoringMbeanNameTemplate = attrValue;
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
			if (childNodeName == "attribute") {
				Attribute aAttribute = newAttribute();
				aAttribute.readNode(childNode, namespacePrefixes);
				_Attribute.add(aAttribute);
			}
			else if (childNodeName == "mapping") {
				Mapping aMapping = newMapping();
				aMapping.readNode(childNode, namespacePrefixes);
				_Mapping.add(aMapping);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException {
		boolean restrictionFailure = false;
		// Validating property monitoringMbeanNameTemplate
		// Validating property attribute
		if (sizeAttribute() == 0) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("sizeAttribute() == 0", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "attribute", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeAttribute(); ++_index) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element = getAttribute(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property mapping
		for (int _index = 0; _index < sizeMapping(); ++_index) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element = getMapping(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "monitoringMbeanNameTemplate")
			setMonitoringMbeanNameTemplate((java.lang.String)value);
		else if (name == "attribute")
			addAttribute((Attribute)value);
		else if (name == "attribute[]")
			setAttribute((Attribute[]) value);
		else if (name == "mapping")
			addMapping((Mapping)value);
		else if (name == "mapping[]")
			setMapping((Mapping[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for ObjectName");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "monitoringMbeanNameTemplate")
			return getMonitoringMbeanNameTemplate();
		if (name == "attribute[]")
			return getAttribute();
		if (name == "mapping[]")
			return getMapping();
		throw new IllegalArgumentException(name+" is not a valid property name for ObjectName");
	}

	public String nameSelf() {
		return "ObjectName";
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
			if (child == _MonitoringMbeanNameTemplate) {
				if (returnConstName) {
					return MONITORINGMBEANNAMETEMPLATE;
				} else if (returnSchemaName) {
					return "monitoring-mbean-name-template";
				} else if (returnXPathName) {
					return "@monitoring-mbean-name-template";
				} else {
					return "MonitoringMbeanNameTemplate";
				}
			}
		}
		if (childObj instanceof Mapping) {
			Mapping child = (Mapping) childObj;
			int index = 0;
			for (java.util.Iterator it = _Mapping.iterator(); 
				it.hasNext(); ) {
				com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it.next();
				if (child == element) {
					if (returnConstName) {
						return MAPPING;
					} else if (returnSchemaName) {
						return "mapping";
					} else if (returnXPathName) {
						return "mapping[position()="+index+"]";
					} else {
						return "Mapping."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof Attribute) {
			Attribute child = (Attribute) childObj;
			int index = 0;
			for (java.util.Iterator it = _Attribute.iterator(); 
				it.hasNext(); ) {
				com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it.next();
				if (child == element) {
					if (returnConstName) {
						return ATTRIBUTE;
					} else if (returnSchemaName) {
						return "attribute";
					} else if (returnXPathName) {
						return "attribute[position()="+index+"]";
					} else {
						return "Attribute."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _Attribute.iterator(); it.hasNext(); 
			) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _Mapping.iterator(); it.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName && equals((com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName) o);
	}

	public boolean equals(com.sun.mfwk.agent.appserv.lifecycle.beans.ObjectName inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_MonitoringMbeanNameTemplate == null ? inst._MonitoringMbeanNameTemplate == null : _MonitoringMbeanNameTemplate.equals(inst._MonitoringMbeanNameTemplate))) {
			return false;
		}
		if (sizeAttribute() != inst.sizeAttribute())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Attribute.iterator(), it2 = inst._Attribute.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it.next();
			com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute element2 = (com.sun.mfwk.agent.appserv.lifecycle.beans.Attribute)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeMapping() != inst.sizeMapping())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Mapping.iterator(), it2 = inst._Mapping.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it.next();
			com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping element2 = (com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_MonitoringMbeanNameTemplate == null ? 0 : _MonitoringMbeanNameTemplate.hashCode());
		result = 37*result + (_Attribute == null ? 0 : _Attribute.hashCode());
		result = 37*result + (_Mapping == null ? 0 : _Mapping.hashCode());
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
