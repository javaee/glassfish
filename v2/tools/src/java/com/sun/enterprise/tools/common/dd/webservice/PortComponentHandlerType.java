/**
 *	This generated bean class PortComponentHandlerType matches the schema element port-component_handlerType
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
 *	Generated on Fri Apr 22 15:42:53 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PortComponentHandlerType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String DISPLAY_NAME = "DisplayName";	// NOI18N
	static public final String ICON = "Icon";	// NOI18N
	static public final String HANDLER_NAME = "HandlerName";	// NOI18N
	static public final String HANDLER_CLASS = "HandlerClass";	// NOI18N
	static public final String INIT_PARAM = "InitParam";	// NOI18N
	static public final String SOAP_HEADER = "SoapHeader";	// NOI18N
	static public final String SOAP_ROLE = "SoapRole";	// NOI18N

	public PortComponentHandlerType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PortComponentHandlerType(int options)
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
		this.createProperty("handler-name", 	// NOI18N
			HANDLER_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(HANDLER_NAME, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("handler-class", 	// NOI18N
			HANDLER_CLASS, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("init-param", 	// NOI18N
			INIT_PARAM, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ParamValueType.class);
		this.createAttribute(INIT_PARAM, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("soap-header", 	// NOI18N
			SOAP_HEADER, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			javax.xml.namespace.QName.class);
		this.createAttribute(SOAP_HEADER, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("soap-role", 	// NOI18N
			SOAP_ROLE, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(SOAP_ROLE, "id", "Id", 
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
	public void setHandlerName(java.lang.String value) {
		this.setValue(HANDLER_NAME, value);
	}

	//
	public java.lang.String getHandlerName() {
		return (java.lang.String)this.getValue(HANDLER_NAME);
	}

	// This attribute is mandatory
	public void setHandlerClass(java.lang.String value) {
		this.setValue(HANDLER_CLASS, value);
	}

	//
	public java.lang.String getHandlerClass() {
		return (java.lang.String)this.getValue(HANDLER_CLASS);
	}

	// This attribute is an array, possibly empty
	public void setInitParam(int index, ParamValueType value) {
		this.setValue(INIT_PARAM, index, value);
	}

	//
	public ParamValueType getInitParam(int index) {
		return (ParamValueType)this.getValue(INIT_PARAM, index);
	}

	// This attribute is an array, possibly empty
	public void setInitParam(ParamValueType[] value) {
		this.setValue(INIT_PARAM, value);
	}

	//
	public ParamValueType[] getInitParam() {
		return (ParamValueType[])this.getValues(INIT_PARAM);
	}

	// Return the number of properties
	public int sizeInitParam() {
		return this.size(INIT_PARAM);
	}

	// Add a new element returning its index in the list
	public int addInitParam(com.sun.enterprise.tools.common.dd.webservice.ParamValueType value) {
		return this.addValue(INIT_PARAM, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeInitParam(com.sun.enterprise.tools.common.dd.webservice.ParamValueType value) {
		return this.removeValue(INIT_PARAM, value);
	}

	// This attribute is an array, possibly empty
	public void setSoapHeader(int index, javax.xml.namespace.QName value) {
		this.setValue(SOAP_HEADER, index, value);
	}

	//
	public javax.xml.namespace.QName getSoapHeader(int index) {
		return (javax.xml.namespace.QName)this.getValue(SOAP_HEADER, index);
	}

	// This attribute is an array, possibly empty
	public void setSoapHeader(javax.xml.namespace.QName[] value) {
		this.setValue(SOAP_HEADER, value);
	}

	//
	public javax.xml.namespace.QName[] getSoapHeader() {
		return (javax.xml.namespace.QName[])this.getValues(SOAP_HEADER);
	}

	// Return the number of properties
	public int sizeSoapHeader() {
		return this.size(SOAP_HEADER);
	}

	// Add a new element returning its index in the list
	public int addSoapHeader(javax.xml.namespace.QName value) {
		return this.addValue(SOAP_HEADER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSoapHeader(javax.xml.namespace.QName value) {
		return this.removeValue(SOAP_HEADER, value);
	}

	// This attribute is an array, possibly empty
	public void setSoapRole(int index, java.lang.String value) {
		this.setValue(SOAP_ROLE, index, value);
	}

	//
	public java.lang.String getSoapRole(int index) {
		return (java.lang.String)this.getValue(SOAP_ROLE, index);
	}

	// This attribute is an array, possibly empty
	public void setSoapRole(java.lang.String[] value) {
		this.setValue(SOAP_ROLE, value);
	}

	//
	public java.lang.String[] getSoapRole() {
		return (java.lang.String[])this.getValues(SOAP_ROLE);
	}

	// Return the number of properties
	public int sizeSoapRole() {
		return this.size(SOAP_ROLE);
	}

	// Add a new element returning its index in the list
	public int addSoapRole(java.lang.String value) {
		return this.addValue(SOAP_ROLE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSoapRole(java.lang.String value) {
		return this.removeValue(SOAP_ROLE, value);
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
		str.append("HandlerName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getHandlerName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HANDLER_NAME, 0, str, indent);

		str.append(indent);
		str.append("HandlerClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getHandlerClass();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HANDLER_CLASS, 0, str, indent);

		str.append(indent);
		str.append("InitParam["+this.sizeInitParam()+"]");	// NOI18N
		for(int i=0; i<this.sizeInitParam(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getInitParam(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(INIT_PARAM, i, str, indent);
		}

		str.append(indent);
		str.append("SoapHeader["+this.sizeSoapHeader()+"]");	// NOI18N
		for(int i=0; i<this.sizeSoapHeader(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getSoapHeader(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SOAP_HEADER, i, str, indent);
		}

		str.append(indent);
		str.append("SoapRole["+this.sizeSoapRole()+"]");	// NOI18N
		for(int i=0; i<this.sizeSoapRole(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getSoapRole(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(SOAP_ROLE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PortComponentHandlerType\n");	// NOI18N
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
