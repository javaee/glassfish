/**
 *	This generated bean class PortComponentType matches the schema element port-componentType
 *
 *	===============================================================
 *	
 *	
 *		The port-component element associates a WSDL port with a web service
 *		interface and implementation.  It defines the name of the port as a
 *		component, optional description, optional display name, optional iconic
 *		representations, WSDL port QName, Service Endpoint Interface, Service
 *		Implementation Bean.
 *	
 *	      
 *	===============================================================
 *	Generated on Fri Apr 22 15:42:50 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PortComponentType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DISPLAY_NAME = "DisplayName";	// NOI18N
	static public final String ICON = "Icon";	// NOI18N
	static public final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
	static public final String WSDL_PORT = "WsdlPort";	// NOI18N
	static public final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
	static public final String SERVICE_IMPL_BEAN = "ServiceImplBean";	// NOI18N
	static public final String HANDLER = "Handler";	// NOI18N

	public PortComponentType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PortComponentType(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DESCRIPTION, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DESCRIPTION, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("display-name", 	// NOI18N
			DISPLAY_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(DISPLAY_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(DISPLAY_NAME, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("icon", 	// NOI18N
			ICON, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IconType.class);
		this.createAttribute(ICON, "xml:lang", "XmlLang", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(ICON, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("port-component-name", 	// NOI18N
			PORT_COMPONENT_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PORT_COMPONENT_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("wsdl-port", 	// NOI18N
			WSDL_PORT, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			javax.xml.namespace.QName.class);
		this.createAttribute(WSDL_PORT, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("service-endpoint-interface", 	// NOI18N
			SERVICE_ENDPOINT_INTERFACE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("service-impl-bean", 	// NOI18N
			SERVICE_IMPL_BEAN, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceImplBeanType.class);
		this.createAttribute(SERVICE_IMPL_BEAN, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("handler", 	// NOI18N
			HANDLER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PortComponentHandlerType.class);
		this.createAttribute(HANDLER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is optional
	public void setDescription(java.lang.String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public java.lang.String getDescription() {
		return (java.lang.String)this.getValue(DESCRIPTION);
	}

	// This attribute is optional
	public void setDisplayName(java.lang.String value) {
		this.setValue(DISPLAY_NAME, value);
	}

	//
	public java.lang.String getDisplayName() {
		return (java.lang.String)this.getValue(DISPLAY_NAME);
	}

	// This attribute is optional
	public void setIcon(IconType value) {
		this.setValue(ICON, value);
	}

	//
	public IconType getIcon() {
		return (IconType)this.getValue(ICON);
	}

	// This attribute is mandatory
	public void setPortComponentName(java.lang.String value) {
		this.setValue(PORT_COMPONENT_NAME, value);
	}

	//
	public java.lang.String getPortComponentName() {
		return (java.lang.String)this.getValue(PORT_COMPONENT_NAME);
	}

	// This attribute is mandatory
	public void setWsdlPort(javax.xml.namespace.QName value) {
		this.setValue(WSDL_PORT, value);
	}

	//
	public javax.xml.namespace.QName getWsdlPort() {
		return (javax.xml.namespace.QName)this.getValue(WSDL_PORT);
	}

	// This attribute is mandatory
	public void setServiceEndpointInterface(java.lang.String value) {
		this.setValue(SERVICE_ENDPOINT_INTERFACE, value);
	}

	//
	public java.lang.String getServiceEndpointInterface() {
		return (java.lang.String)this.getValue(SERVICE_ENDPOINT_INTERFACE);
	}

	// This attribute is mandatory
	public void setServiceImplBean(ServiceImplBeanType value) {
		this.setValue(SERVICE_IMPL_BEAN, value);
	}

	//
	public ServiceImplBeanType getServiceImplBean() {
		return (ServiceImplBeanType)this.getValue(SERVICE_IMPL_BEAN);
	}

	// This attribute is an array, possibly empty
	public void setHandler(int index, PortComponentHandlerType value) {
		this.setValue(HANDLER, index, value);
	}

	//
	public PortComponentHandlerType getHandler(int index) {
		return (PortComponentHandlerType)this.getValue(HANDLER, index);
	}

	// This attribute is an array, possibly empty
	public void setHandler(PortComponentHandlerType[] value) {
		this.setValue(HANDLER, value);
	}

	//
	public PortComponentHandlerType[] getHandler() {
		return (PortComponentHandlerType[])this.getValues(HANDLER);
	}

	// Return the number of properties
	public int sizeHandler() {
		return this.size(HANDLER);
	}

	// Add a new element returning its index in the list
	public int addHandler(com.sun.enterprise.tools.common.dd.webservice.PortComponentHandlerType value) {
		return this.addValue(HANDLER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeHandler(com.sun.enterprise.tools.common.dd.webservice.PortComponentHandlerType value) {
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
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getDescription();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("DisplayName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getDisplayName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DISPLAY_NAME, 0, str, indent);

		str.append(indent);
		str.append("Icon");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getIcon();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ICON, 0, str, indent);

		str.append(indent);
		str.append("PortComponentName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPortComponentName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PORT_COMPONENT_NAME, 0, str, indent);

		str.append(indent);
		str.append("WsdlPort");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getWsdlPort();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WSDL_PORT, 0, str, indent);

		str.append(indent);
		str.append("ServiceEndpointInterface");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServiceEndpointInterface();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_ENDPOINT_INTERFACE, 0, str, indent);

		str.append(indent);
		str.append("ServiceImplBean");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getServiceImplBean();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SERVICE_IMPL_BEAN, 0, str, indent);

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
		str.append("PortComponentType\n");	// NOI18N
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
