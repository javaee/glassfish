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
 *	This generated bean class Mapping
 *	matches the schema element 'mapping'.
 *  The root bean class is Notifications
 *
 *	Generated on Sun Aug 28 23:45:36 PDT 2005
 * @Generated
 */

package com.sun.mfwk.agent.appserv.lifecycle.beans;

public class Mapping {
	public static final String RUNTIME_MBEAN_ATTRIBUTE_NAME = "RuntimeMbeanAttributeName";	// NOI18N
	public static final String MONITORING_MBEAN_ATTRIBUTE_VALUE = "MonitoringMbeanAttributeValue";	// NOI18N

	private String _RuntimeMbeanAttributeName;
	private String _MonitoringMbeanAttributeValue;

	/**
	 * Normal starting point constructor.
	 */
	public Mapping() {
		_RuntimeMbeanAttributeName = "";
		_MonitoringMbeanAttributeValue = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Mapping(String runtimeMbeanAttributeName, String monitoringMbeanAttributeValue) {
		_RuntimeMbeanAttributeName = runtimeMbeanAttributeName;
		_MonitoringMbeanAttributeValue = monitoringMbeanAttributeValue;
	}

	/**
	 * Deep copy
	 */
	public Mapping(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Mapping(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping source, boolean justData) {
		_RuntimeMbeanAttributeName = source._RuntimeMbeanAttributeName;
		_MonitoringMbeanAttributeValue = source._MonitoringMbeanAttributeValue;
	}

	// This attribute is mandatory
	public void setRuntimeMbeanAttributeName(String value) {
		_RuntimeMbeanAttributeName = value;
	}

	public String getRuntimeMbeanAttributeName() {
		return _RuntimeMbeanAttributeName;
	}

	// This attribute is mandatory
	public void setMonitoringMbeanAttributeValue(String value) {
		_MonitoringMbeanAttributeValue = value;
	}

	public String getMonitoringMbeanAttributeValue() {
		return _MonitoringMbeanAttributeValue;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "mapping";
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
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_RuntimeMbeanAttributeName != null) {
			out.write(nextIndent);
			out.write("<runtime-mbean-attribute-name");	// NOI18N
			out.write(">");	// NOI18N
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _RuntimeMbeanAttributeName, false);
			out.write("</runtime-mbean-attribute-name>\n");	// NOI18N
		}
		if (_MonitoringMbeanAttributeValue != null) {
			out.write(nextIndent);
			out.write("<monitoring-mbean-attribute-value");	// NOI18N
			out.write(">");	// NOI18N
			com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.writeXML(out, _MonitoringMbeanAttributeValue, false);
			out.write("</monitoring-mbean-attribute-value>\n");	// NOI18N
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
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "runtime-mbean-attribute-name") {
				_RuntimeMbeanAttributeName = childNodeValue;
			}
			else if (childNodeName == "monitoring-mbean-attribute-value") {
				_MonitoringMbeanAttributeValue = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException {
		boolean restrictionFailure = false;
		// Validating property runtimeMbeanAttributeName
		if (getRuntimeMbeanAttributeName() == null) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("getRuntimeMbeanAttributeName() == null", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "runtimeMbeanAttributeName", this);	// NOI18N
		}
		// Validating property monitoringMbeanAttributeValue
		if (getMonitoringMbeanAttributeValue() == null) {
			throw new com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException("getMonitoringMbeanAttributeValue() == null", com.sun.mfwk.agent.appserv.lifecycle.beans.Notifications.ValidateException.FailureType.NULL_VALUE, "monitoringMbeanAttributeValue", this);	// NOI18N
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "runtimeMbeanAttributeName")
			setRuntimeMbeanAttributeName((String)value);
		else if (name == "monitoringMbeanAttributeValue")
			setMonitoringMbeanAttributeValue((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Mapping");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "runtimeMbeanAttributeName")
			return getRuntimeMbeanAttributeName();
		if (name == "monitoringMbeanAttributeValue")
			return getMonitoringMbeanAttributeValue();
		throw new IllegalArgumentException(name+" is not a valid property name for Mapping");
	}

	public String nameSelf() {
		return "Mapping";
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
			if (child == _RuntimeMbeanAttributeName) {
				if (returnConstName) {
					return RUNTIME_MBEAN_ATTRIBUTE_NAME;
				} else if (returnSchemaName) {
					return "runtime-mbean-attribute-name";
				} else if (returnXPathName) {
					return "runtime-mbean-attribute-name";
				} else {
					return "RuntimeMbeanAttributeName";
				}
			}
			if (child == _MonitoringMbeanAttributeValue) {
				if (returnConstName) {
					return MONITORING_MBEAN_ATTRIBUTE_VALUE;
				} else if (returnSchemaName) {
					return "monitoring-mbean-attribute-value";
				} else if (returnXPathName) {
					return "monitoring-mbean-attribute-value";
				} else {
					return "MonitoringMbeanAttributeValue";
				}
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
	}

	public boolean equals(Object o) {
		return o instanceof com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping && equals((com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping) o);
	}

	public boolean equals(com.sun.mfwk.agent.appserv.lifecycle.beans.Mapping inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_RuntimeMbeanAttributeName == null ? inst._RuntimeMbeanAttributeName == null : _RuntimeMbeanAttributeName.equals(inst._RuntimeMbeanAttributeName))) {
			return false;
		}
		if (!(_MonitoringMbeanAttributeValue == null ? inst._MonitoringMbeanAttributeValue == null : _MonitoringMbeanAttributeValue.equals(inst._MonitoringMbeanAttributeValue))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_RuntimeMbeanAttributeName == null ? 0 : _RuntimeMbeanAttributeName.hashCode());
		result = 37*result + (_MonitoringMbeanAttributeValue == null ? 0 : _MonitoringMbeanAttributeValue.hashCode());
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
