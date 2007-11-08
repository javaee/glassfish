/**
 *	This generated bean class WebserviceDescriptionType matches the schema element webservice-descriptionType
 *
 *	===============================================================
 *	
 *	
 *		The webservice-description element defines a WSDL document file
 *		and the set of Port components associated with the WSDL ports
 *		defined in the WSDL document.  There may be multiple
 *		webservice-descriptions defined within a module.
 *	
 *		All WSDL file ports must have a corresponding port-component element
 *		defined.
 *	
 *		Used in: webservices
 *	
 *	      
 *	===============================================================
 *	Generated on Fri Apr 22 15:42:53 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class WebserviceDescriptionType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DISPLAY_NAME = "DisplayName";	// NOI18N
	static public final String ICON = "Icon";	// NOI18N
	static public final String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName";	// NOI18N
	static public final String WSDL_FILE = "WsdlFile";	// NOI18N
	static public final String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile";	// NOI18N
	static public final String PORT_COMPONENT = "PortComponent";	// NOI18N

	public WebserviceDescriptionType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public WebserviceDescriptionType(int options)
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
		this.createProperty("webservice-description-name", 	// NOI18N
			WEBSERVICE_DESCRIPTION_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(WEBSERVICE_DESCRIPTION_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("wsdl-file", 	// NOI18N
			WSDL_FILE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("jaxrpc-mapping-file", 	// NOI18N
			JAXRPC_MAPPING_FILE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("port-component", 	// NOI18N
			PORT_COMPONENT, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PortComponentType.class);
		this.createAttribute(PORT_COMPONENT, "id", "Id", 
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
	public void setWebserviceDescriptionName(java.lang.String value) {
		this.setValue(WEBSERVICE_DESCRIPTION_NAME, value);
	}

	//
	public java.lang.String getWebserviceDescriptionName() {
		return (java.lang.String)this.getValue(WEBSERVICE_DESCRIPTION_NAME);
	}

	// This attribute is mandatory
	public void setWsdlFile(java.lang.String value) {
		this.setValue(WSDL_FILE, value);
	}

	//
	public java.lang.String getWsdlFile() {
		return (java.lang.String)this.getValue(WSDL_FILE);
	}

	// This attribute is mandatory
	public void setJaxrpcMappingFile(java.lang.String value) {
		this.setValue(JAXRPC_MAPPING_FILE, value);
	}

	//
	public java.lang.String getJaxrpcMappingFile() {
		return (java.lang.String)this.getValue(JAXRPC_MAPPING_FILE);
	}

	// This attribute is an array containing at least one element
	public void setPortComponent(int index, PortComponentType value) {
		this.setValue(PORT_COMPONENT, index, value);
	}

	//
	public PortComponentType getPortComponent(int index) {
		return (PortComponentType)this.getValue(PORT_COMPONENT, index);
	}

	// This attribute is an array containing at least one element
	public void setPortComponent(PortComponentType[] value) {
		this.setValue(PORT_COMPONENT, value);
	}

	//
	public PortComponentType[] getPortComponent() {
		return (PortComponentType[])this.getValues(PORT_COMPONENT);
	}

	// Return the number of properties
	public int sizePortComponent() {
		return this.size(PORT_COMPONENT);
	}

	// Add a new element returning its index in the list
	public int addPortComponent(com.sun.enterprise.tools.common.dd.webservice.PortComponentType value) {
		return this.addValue(PORT_COMPONENT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePortComponent(com.sun.enterprise.tools.common.dd.webservice.PortComponentType value) {
		return this.removeValue(PORT_COMPONENT, value);
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
		str.append("WebserviceDescriptionName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getWebserviceDescriptionName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(WEBSERVICE_DESCRIPTION_NAME, 0, str, indent);

		str.append(indent);
		str.append("WsdlFile");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getWsdlFile();
		str.append((s==null?"null":s.trim()));	// NOI18N
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
		str.append("PortComponent["+this.sizePortComponent()+"]");	// NOI18N
		for(int i=0; i<this.sizePortComponent(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPortComponent(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PORT_COMPONENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WebserviceDescriptionType\n");	// NOI18N
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
