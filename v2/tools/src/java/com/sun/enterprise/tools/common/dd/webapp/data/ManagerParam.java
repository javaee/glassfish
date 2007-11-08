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
 *	This generated bean class ManagerParam matches the schema element manager-param
 *
 *	Generated on Tue Aug 12 18:27:47 PDT 2003
 */

package com.sun.enterprise.tools.common.dd.webapp.data;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ManagerParam extends com.sun.enterprise.tools.common.dd.ParamData
{

	static Vector comparators = new Vector();

	static public final String PARAM_NAME = "ParamName";	// NOI18N
	static public final String PARAM_TYPE = "ParamType";	// NOI18N
	static public final String PARAM_VALUES = "ParamValues";	// NOI18N
	static public final String DEFAULT_VALUE = "DefaultValue";	// NOI18N
	static public final String HELPID = "HelpID";	// NOI18N

	public ManagerParam() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ManagerParam(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("param-name", 	// NOI18N
			PARAM_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("param-type", 	// NOI18N
			PARAM_TYPE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("param-values", 	// NOI18N
			PARAM_VALUES, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("default-value", 	// NOI18N
			DEFAULT_VALUE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("helpID", 	// NOI18N
			HELPID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
					
	}

	// This attribute is mandatory
	public void setParamName(String value) {
		this.setValue(PARAM_NAME, value);
	}

	//
	public String getParamName() {
		return (String)this.getValue(PARAM_NAME);
	}

	// This attribute is optional
	public void setParamType(String value) {
		this.setValue(PARAM_TYPE, value);
	}

	//
	public String getParamType() {
		return (String)this.getValue(PARAM_TYPE);
	}

	// This attribute is an array, possibly empty
	public void setParamValues(int index, String value) {
		this.setValue(PARAM_VALUES, index, value);
	}

	//
	public String getParamValues(int index) {
		return (String)this.getValue(PARAM_VALUES, index);
	}

	// This attribute is an array, possibly empty
	public void setParamValues(String[] value) {
		this.setValue(PARAM_VALUES, value);
	}

	//
	public String[] getParamValues() {
		return (String[])this.getValues(PARAM_VALUES);
	}

	// Return the number of properties
	public int sizeParamValues() {
		return this.size(PARAM_VALUES);
	}

	// Add a new element returning its index in the list
	public int addParamValues(String value) {
		return this.addValue(PARAM_VALUES, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeParamValues(String value) {
		return this.removeValue(PARAM_VALUES, value);
	}

	// This attribute is optional
	public void setDefaultValue(String value) {
		this.setValue(DEFAULT_VALUE, value);
	}

	//
	public String getDefaultValue() {
		return (String)this.getValue(DEFAULT_VALUE);
	}

	// This attribute is optional
	public void setHelpID(String value) {
		this.setValue(HELPID, value);
	}

	//
	public String getHelpID() {
		return (String)this.getValue(HELPID);
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
		boolean restrictionFailure = false;
		// Validating property paramName
		if (getParamName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getParamName() == null", "paramName", this);	// NOI18N
		}
		// Validating property paramType
		if (getParamType() != null) {
		}
		// Validating property paramValues
		for (int _index = 0; _index < sizeParamValues(); ++_index) {
			String element = getParamValues(_index);
			if (element != null) {
			}
		}
		// Validating property defaultValue
		if (getDefaultValue() != null) {
		}
		// Validating property helpID
		if (getHelpID() != null) {
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ParamName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getParamName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PARAM_NAME, 0, str, indent);

		str.append(indent);
		str.append("ParamType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getParamType();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PARAM_TYPE, 0, str, indent);

		str.append(indent);
		str.append("ParamValues["+this.sizeParamValues()+"]");	// NOI18N
		for(int i=0; i<this.sizeParamValues(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getParamValues(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PARAM_VALUES, i, str, indent);
		}

		str.append(indent);
		str.append("DefaultValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getDefaultValue();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DEFAULT_VALUE, 0, str, indent);

		str.append(indent);
		str.append("HelpID");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getHelpID();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(HELPID, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ManagerParam\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XML Spy v4.1 U (http://www.xmlspy.com) by A Gaur (Sun Microsystems) -->
<!ELEMENT sun-web-app-data (session-param*, cookie-param*, jsp-param*, extra-param*, manager-param*, store-param*, persistence-param*, helper-class-param*)>
<!ELEMENT session-param (param-name, param-type?,  param-values*, default-value?, helpID?)>
<!ELEMENT cookie-param (param-name, param-type?,  param-values*, default-value?, helpID?)>
<!ELEMENT jsp-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT extra-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT manager-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT store-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT persistence-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT helper-class-param (param-name, param-type?,  param-values*, default-value? helpID?)>
<!ELEMENT param-name (#PCDATA)>
<!ELEMENT param-type (#PCDATA)>
<!ELEMENT param-values (#PCDATA)>
<!ELEMENT default-value (#PCDATA)>
<!ELEMENT helpID (#PCDATA)>

*/
