/**
 *	This generated bean class ServiceRefType matches the schema element service-refType
 *
 *	===============================================================
 *	
 *	
 *		This group keeps the usage of the contained description related
 *		elements consistent across J2EE deployment descriptors.
 *	
 *		All elements may occur multiple times with different languages,
 *		to support localization of the content.
 *	
 *	      
 *	===============================================================
 *	Generated on Fri Apr 22 15:42:58 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ServiceRefType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DISPLAY_NAME = "DisplayName";	// NOI18N
	static public final String ICON = "Icon";	// NOI18N
	static public final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
	static public final String SERVICE_INTERFACE = "ServiceInterface";	// NOI18N
	static public final String WSDL_FILE = "WsdlFile";	// NOI18N
	static public final String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile";	// NOI18N
	static public final String SERVICE_QNAME = "ServiceQname";	// NOI18N
	static public final String PORT_COMPONENT_REF = "PortComponentRef";	// NOI18N
	static public final String HANDLER = "Handler";	// NOI18N

	public ServiceRefType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ServiceRefType(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("display-name", 	// NOI18N
			DISPLAY_NAME, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISPLAY_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DISPLAY_NAME, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("icon", 	// NOI18N
			ICON, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IconType.class);
		this.createAttribute(ICON, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(ICON, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("service-ref-name", 	// NOI18N
			SERVICE_REF_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("service-interface", 	// NOI18N
			SERVICE_INTERFACE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("wsdl-file", 	// NOI18N
			WSDL_FILE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.net.URI.class);
		this.createAttribute(WSDL_FILE, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jaxrpc-mapping-file", 	// NOI18N
			JAXRPC_MAPPING_FILE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("service-qname", 	// NOI18N
			SERVICE_QNAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			javax.xml.namespace.QName.class);
		this.createAttribute(SERVICE_QNAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("port-component-ref", 	// NOI18N
			PORT_COMPONENT_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PortComponentRefType.class);
		this.createAttribute(PORT_COMPONENT_REF, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("handler", 	// NOI18N
			HANDLER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceRefHandlerType.class);
		this.createAttribute(HANDLER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array, possibly empty
	public void setDescription(int index, java.lang.String value) {
		this.setValue(DESCRIPTION, index, value);
	}

	//
	public java.lang.String getDescription(int index) {
		return (java.lang.String)this.getValue(DESCRIPTION, index);
	}

	// This attribute is an array, possibly empty
	public void setDescription(java.lang.String[] value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String[] getDescription() {
		return (java.lang.String[])this.getValues(DESCRIPTION);
	}

	// Return the number of properties
	public int sizeDescription() {
		return this.size(DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addDescription(java.lang.String value) {
		return this.addValue(DESCRIPTION, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDescription(java.lang.String value) {
		return this.removeValue(DESCRIPTION, value);
	}

	// This attribute is an array, possibly empty
	public void setDisplayName(int index, java.lang.String value) {
		this.setValue(DISPLAY_NAME, index, value);
	}

	//
	public java.lang.String getDisplayName(int index) {
		return (java.lang.String)this.getValue(DISPLAY_NAME, index);
	}

	// This attribute is an array, possibly empty
	public void setDisplayName(java.lang.String[] value) {
		this.setValue(DISPLAY_NAME, value);
	}

	//
	public java.lang.String[] getDisplayName() {
		return (java.lang.String[])this.getValues(DISPLAY_NAME);
	}

	// Return the number of properties
	public int sizeDisplayName() {
		return this.size(DISPLAY_NAME);
	}

	// Add a new element returning its index in the list
	public int addDisplayName(java.lang.String value) {
		return this.addValue(DISPLAY_NAME, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDisplayName(java.lang.String value) {
		return this.removeValue(DISPLAY_NAME, value);
	}

	// This attribute is an array, possibly empty
	public void setIcon(int index, IconType value) {
		this.setValue(ICON, index, value);
	}

	//
	public IconType getIcon(int index) {
		return (IconType)this.getValue(ICON, index);
	}

	// This attribute is an array, possibly empty
	public void setIcon(IconType[] value) {
		this.setValue(ICON, value);
	}

	//
	public IconType[] getIcon() {
		return (IconType[])this.getValues(ICON);
	}

	// Return the number of properties
	public int sizeIcon() {
		return this.size(ICON);
	}

	// Add a new element returning its index in the list
	public int addIcon(com.sun.enterprise.tools.common.dd.webservice.IconType value) {
		return this.addValue(ICON, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeIcon(com.sun.enterprise.tools.common.dd.webservice.IconType value) {
		return this.removeValue(ICON, value);
	}

	// This attribute is mandatory
	public void setServiceRefName(java.lang.String value) {
		this.setValue(SERVICE_REF_NAME, value);
	}

	//
	public java.lang.String getServiceRefName() {
		return (java.lang.String)this.getValue(SERVICE_REF_NAME);
	}

	// This attribute is mandatory
	public void setServiceInterface(java.lang.String value) {
		this.setValue(SERVICE_INTERFACE, value);
	}

	//
	public java.lang.String getServiceInterface() {
		return (java.lang.String)this.getValue(SERVICE_INTERFACE);
	}

	// This attribute is optional
	public void setWsdlFile(java.net.URI value) {
		this.setValue(WSDL_FILE, value);
	}

	//
	public java.net.URI getWsdlFile() {
		return (java.net.URI)this.getValue(WSDL_FILE);
	}

	// This attribute is optional
	public void setJaxrpcMappingFile(java.lang.String value) {
		this.setValue(JAXRPC_MAPPING_FILE, value);
	}

	//
	public java.lang.String getJaxrpcMappingFile() {
		return (java.lang.String)this.getValue(JAXRPC_MAPPING_FILE);
	}

	// This attribute is optional
	public void setServiceQname(javax.xml.namespace.QName value) {
		this.setValue(SERVICE_QNAME, value);
	}

	//
	public javax.xml.namespace.QName getServiceQname() {
		return (javax.xml.namespace.QName)this.getValue(SERVICE_QNAME);
	}

	// This attribute is an array, possibly empty
	public void setPortComponentRef(int index, PortComponentRefType value) {
		this.setValue(PORT_COMPONENT_REF, index, value);
	}

	//
	public PortComponentRefType getPortComponentRef(int index) {
		return (PortComponentRefType)this.getValue(PORT_COMPONENT_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setPortComponentRef(PortComponentRefType[] value) {
		this.setValue(PORT_COMPONENT_REF, value);
	}

	//
	public PortComponentRefType[] getPortComponentRef() {
		return (PortComponentRefType[])this.getValues(PORT_COMPONENT_REF);
	}

	// Return the number of properties
	public int sizePortComponentRef() {
		return this.size(PORT_COMPONENT_REF);
	}

	// Add a new element returning its index in the list
	public int addPortComponentRef(com.sun.enterprise.tools.common.dd.webservice.PortComponentRefType value) {
		return this.addValue(PORT_COMPONENT_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePortComponentRef(com.sun.enterprise.tools.common.dd.webservice.PortComponentRefType value) {
		return this.removeValue(PORT_COMPONENT_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setHandler(int index, ServiceRefHandlerType value) {
		this.setValue(HANDLER, index, value);
	}

	//
	public ServiceRefHandlerType getHandler(int index) {
		return (ServiceRefHandlerType)this.getValue(HANDLER, index);
	}

	// This attribute is an array, possibly empty
	public void setHandler(ServiceRefHandlerType[] value) {
		this.setValue(HANDLER, value);
	}

	//
	public ServiceRefHandlerType[] getHandler() {
		return (ServiceRefHandlerType[])this.getValues(HANDLER);
	}

	// Return the number of properties
	public int sizeHandler() {
		return this.size(HANDLER);
	}

	// Add a new element returning its index in the list
	public int addHandler(com.sun.enterprise.tools.common.dd.webservice.ServiceRefHandlerType value) {
		return this.addValue(HANDLER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeHandler(com.sun.enterprise.tools.common.dd.webservice.ServiceRefHandlerType value) {
		return this.removeValue(HANDLER, value);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Description["+this.sizeDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getDescription(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DESCRIPTION, i, str, indent);
		}

		str.append(indent);
		str.append("DisplayName["+this.sizeDisplayName()+"]");	// NOI18N
		for(int i=0; i<this.sizeDisplayName(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getDisplayName(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DISPLAY_NAME, i, str, indent);
		}

		str.append(indent);
		str.append("Icon["+this.sizeIcon()+"]");	// NOI18N
		for(int i=0; i<this.sizeIcon(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getIcon(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ICON, i, str, indent);
		}

		str.append(indent);
		str.append("ServiceRefName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServiceRefName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_REF_NAME, 0, str, indent);

		str.append(indent);
		str.append("ServiceInterface");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServiceInterface();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_INTERFACE, 0, str, indent);

		str.append(indent);
		str.append("WsdlFile");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getWsdlFile();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WSDL_FILE, 0, str, indent);

		str.append(indent);
		str.append("JaxrpcMappingFile");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getJaxrpcMappingFile();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JAXRPC_MAPPING_FILE, 0, str, indent);

		str.append(indent);
		str.append("ServiceQname");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getServiceQname();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_QNAME, 0, str, indent);

		str.append(indent);
		str.append("PortComponentRef["+this.sizePortComponentRef()+"]");	// NOI18N
		for(int i=0; i<this.sizePortComponentRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPortComponentRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PORT_COMPONENT_REF, i, str, indent);
		}

		str.append(indent);
		str.append("Handler["+this.sizeHandler()+"]");	// NOI18N
		for(int i=0; i<this.sizeHandler(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getHandler(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(HANDLER, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ServiceRefType\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


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
