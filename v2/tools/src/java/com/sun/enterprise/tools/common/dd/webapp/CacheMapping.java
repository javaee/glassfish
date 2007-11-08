/**
 *	This generated bean class CacheMapping matches the schema element cache-mapping
 *
 *	Generated on Fri Apr 02 16:57:40 PST 2004
 */

package com.sun.enterprise.tools.common.dd.webapp;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class CacheMapping extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String SERVLET_NAME = "ServletName";	// NOI18N
	static public final String URL_PATTERN = "UrlPattern";	// NOI18N
	static public final String CACHE_HELPER_REF = "CacheHelperRef";	// NOI18N
	static public final String DISPATCHER = "Dispatcher";	// NOI18N
	static public final String TIMEOUT = "Timeout";	// NOI18N
	static public final String TIMEOUTNAME = "TimeoutName";	// NOI18N
	static public final String TIMEOUTSCOPE = "TimeoutScope";	// NOI18N
	static public final String REFRESH_FIELD = "RefreshField";	// NOI18N
	static public final String REFRESHFIELDNAME = "RefreshFieldName";	// NOI18N
	static public final String REFRESHFIELDSCOPE = "RefreshFieldScope";	// NOI18N
	static public final String HTTP_METHOD = "HttpMethod";	// NOI18N
	static public final String KEY_FIELD = "KeyField";	// NOI18N
	static public final String KEYFIELDNAME = "KeyFieldName";	// NOI18N
	static public final String KEYFIELDSCOPE = "KeyFieldScope";	// NOI18N
	static public final String CONSTRAINT_FIELD = "ConstraintField";	// NOI18N

	public CacheMapping() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public CacheMapping(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("servlet-name", 	// NOI18N
			SERVLET_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("url-pattern", 	// NOI18N
			URL_PATTERN, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("cache-helper-ref", 	// NOI18N
			CACHE_HELPER_REF, Common.SEQUENCE_OR | 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("dispatcher", 	// NOI18N
			DISPATCHER, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("timeout", 	// NOI18N
			TIMEOUT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute(TIMEOUT, "name", "Name", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(TIMEOUT, "scope", "Scope", 
						AttrProp.CDATA,
						null, "request.attribute");
		this.createProperty("refresh-field", 	// NOI18N
			REFRESH_FIELD, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(REFRESH_FIELD, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(REFRESH_FIELD, "scope", "Scope", 
						AttrProp.CDATA,
						null, "request.parameter");
		this.createProperty("http-method", 	// NOI18N
			HTTP_METHOD, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("key-field", 	// NOI18N
			KEY_FIELD, 
			Common.TYPE_0_N | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(KEY_FIELD, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(KEY_FIELD, "scope", "Scope", 
						AttrProp.CDATA,
						null, "request.parameter");
		this.createProperty("constraint-field", 	// NOI18N
			CONSTRAINT_FIELD, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConstraintField.class);
		this.createAttribute(CONSTRAINT_FIELD, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CONSTRAINT_FIELD, "scope", "Scope", 
						AttrProp.CDATA,
						null, "request.parameter");
		this.createAttribute(CONSTRAINT_FIELD, "cache-on-match", "CacheOnMatch", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CONSTRAINT_FIELD, "cache-on-match-failure", "CacheOnMatchFailure", 
						AttrProp.CDATA,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
								
	}

	// This attribute is mandatory
	public void setServletName(String value) {
		this.setValue(SERVLET_NAME, value);
	}

	//
	public String getServletName() {
		return (String)this.getValue(SERVLET_NAME);
	}

	// This attribute is mandatory
	public void setUrlPattern(String value) {
		this.setValue(URL_PATTERN, value);
	}

	//
	public String getUrlPattern() {
		return (String)this.getValue(URL_PATTERN);
	}

	// This attribute is mandatory
	public void setCacheHelperRef(String value) {
		this.setValue(CACHE_HELPER_REF, value);
	}

	//
	public String getCacheHelperRef() {
		return (String)this.getValue(CACHE_HELPER_REF);
	}

	// This attribute is an array, possibly empty
	public void setDispatcher(int index, String value) {
		this.setValue(DISPATCHER, index, value);
	}

	//
	public String getDispatcher(int index) {
		return (String)this.getValue(DISPATCHER, index);
	}

	// This attribute is an array, possibly empty
	public void setDispatcher(String[] value) {
		this.setValue(DISPATCHER, value);
	}

	//
	public String[] getDispatcher() {
		return (String[])this.getValues(DISPATCHER);
	}

	// Return the number of properties
	public int sizeDispatcher() {
		return this.size(DISPATCHER);
	}

	// Add a new element returning its index in the list
	public int addDispatcher(String value) {
		return this.addValue(DISPATCHER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeDispatcher(String value) {
		return this.removeValue(DISPATCHER, value);
	}

	// This attribute is optional
	public void setTimeout(String value) {
		this.setValue(TIMEOUT, value);
	}

	//
	public String getTimeout() {
		return (String)this.getValue(TIMEOUT);
	}

	// This attribute is optional
	public void setTimeoutName(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT) == 0) {
			setValue(TIMEOUT, "");
		}
		setAttributeValue(TIMEOUT, "Name", value);
	}

	//
	public java.lang.String getTimeoutName() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT, "Name");
		}
	}

	// This attribute is mandatory
	public void setTimeoutScope(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(TIMEOUT) == 0) {
			setValue(TIMEOUT, "");
		}
		setAttributeValue(TIMEOUT, "Scope", value);
	}

	//
	public java.lang.String getTimeoutScope() {
		// If our element does not exist, then the attribute does not exist.
		if (size(TIMEOUT) == 0) {
			return null;
		} else {
			return getAttributeValue(TIMEOUT, "Scope");
		}
	}

	// This attribute is optional
	public void setRefreshField(boolean value) {
		this.setValue(REFRESH_FIELD, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRefreshField() {
		Boolean ret = (Boolean)this.getValue(REFRESH_FIELD);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is mandatory
	public void setRefreshFieldName(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_FIELD) == 0) {
			setValue(REFRESH_FIELD, "");
		}
		setAttributeValue(REFRESH_FIELD, "Name", value);
	}

	//
	public java.lang.String getRefreshFieldName() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_FIELD) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_FIELD, "Name");
		}
	}

	// This attribute is mandatory
	public void setRefreshFieldScope(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REFRESH_FIELD) == 0) {
			setValue(REFRESH_FIELD, "");
		}
		setAttributeValue(REFRESH_FIELD, "Scope", value);
	}

	//
	public java.lang.String getRefreshFieldScope() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REFRESH_FIELD) == 0) {
			return null;
		} else {
			return getAttributeValue(REFRESH_FIELD, "Scope");
		}
	}

	// This attribute is an array, possibly empty
	public void setHttpMethod(int index, String value) {
		this.setValue(HTTP_METHOD, index, value);
	}

	//
	public String getHttpMethod(int index) {
		return (String)this.getValue(HTTP_METHOD, index);
	}

	// This attribute is an array, possibly empty
	public void setHttpMethod(String[] value) {
		this.setValue(HTTP_METHOD, value);
	}

	//
	public String[] getHttpMethod() {
		return (String[])this.getValues(HTTP_METHOD);
	}

	// Return the number of properties
	public int sizeHttpMethod() {
		return this.size(HTTP_METHOD);
	}

	// Add a new element returning its index in the list
	public int addHttpMethod(String value) {
		return this.addValue(HTTP_METHOD, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeHttpMethod(String value) {
		return this.removeValue(HTTP_METHOD, value);
	}

	// This attribute is an array, possibly empty
	public void setKeyField(int index, boolean value) {
		this.setValue(KEY_FIELD, index, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isKeyField(int index) {
		Boolean ret = (Boolean)this.getValue(KEY_FIELD, index);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is an array, possibly empty
	public void setKeyField(boolean[] value) {
		Boolean[] values = null;
		if (value != null)
		{
			values = new Boolean[value.length];
			for (int i=0; i<value.length; i++)
				values[i] = new Boolean(value[i]);
		}
		this.setValue(KEY_FIELD, values);
	}

	//
	public boolean[] getKeyField() {
		boolean[] ret = null;
		Boolean[] values = (Boolean[])this.getValues(KEY_FIELD);
		if (values != null)
		{
			ret = new boolean[values.length];
			for (int i=0; i<values.length; i++)
				ret[i] = values[i].booleanValue();
		}
		return ret;
	}

	// Return the number of properties
	public int sizeKeyField() {
		return this.size(KEY_FIELD);
	}

	// Add a new element returning its index in the list
	public int addKeyField(boolean value) {
		return this.addValue(KEY_FIELD, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeKeyField(boolean value) {
		return this.removeValue(KEY_FIELD, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	// Remove an element using its index
	//
	public void removeKeyField(int index) {
		this.removeValue(KEY_FIELD, index);
	}

	// This attribute is an array, possibly empty
	public void setKeyFieldName(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(KEY_FIELD) == 0) {
			addValue(KEY_FIELD, "");
		}
		setAttributeValue(KEY_FIELD, index, "Name", value);
	}

	//
	public java.lang.String getKeyFieldName(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(KEY_FIELD) == 0) {
			return null;
		} else {
			return getAttributeValue(KEY_FIELD, index, "Name");
		}
	}

	// This attribute is an array, possibly empty
	public void setKeyFieldScope(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(KEY_FIELD) == 0) {
			addValue(KEY_FIELD, "");
		}
		setAttributeValue(KEY_FIELD, index, "Scope", value);
	}

	//
	public java.lang.String getKeyFieldScope(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(KEY_FIELD) == 0) {
			return null;
		} else {
			return getAttributeValue(KEY_FIELD, index, "Scope");
		}
	}

	// This attribute is an array, possibly empty
	public void setConstraintField(int index, ConstraintField value) {
		this.setValue(CONSTRAINT_FIELD, index, value);
	}

	//
	public ConstraintField getConstraintField(int index) {
		return (ConstraintField)this.getValue(CONSTRAINT_FIELD, index);
	}

	// This attribute is an array, possibly empty
	public void setConstraintField(ConstraintField[] value) {
		this.setValue(CONSTRAINT_FIELD, value);
	}

	//
	public ConstraintField[] getConstraintField() {
		return (ConstraintField[])this.getValues(CONSTRAINT_FIELD);
	}

	// Return the number of properties
	public int sizeConstraintField() {
		return this.size(CONSTRAINT_FIELD);
	}

	// Add a new element returning its index in the list
	public int addConstraintField(com.sun.enterprise.tools.common.dd.webapp.ConstraintField value) {
		return this.addValue(CONSTRAINT_FIELD, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConstraintField(com.sun.enterprise.tools.common.dd.webapp.ConstraintField value) {
		return this.removeValue(CONSTRAINT_FIELD, value);
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
		// Validating property servletName
		if (getServletName() != null) {
		}
		int orCount = 0;
		if (getServletName() != null) {
			++orCount;
		}
		// Validating property urlPattern
		if (getUrlPattern() != null) {
		}
		if (getUrlPattern() != null) {
			++orCount;
		}
		// Validating property cacheHelperRef
		if (getCacheHelperRef() != null) {
		}
		if (getCacheHelperRef() != null) {
			++orCount;
		}
		// Validating property dispatcher
		for (int _index = 0; _index < sizeDispatcher(); ++_index) {
			String element = getDispatcher(_index);
			if (element != null) {
			}
		}
		// Validating property timeout
		if (getTimeout() != null) {
		}
		// Validating property timeoutName
		if (getTimeoutName() != null) {
		}
		// Validating property timeoutScope
		if (getTimeoutScope() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getTimeoutScope() == null", "timeoutScope", this);	// NOI18N
		}
		// Validating property refreshField
		// Validating property refreshFieldName
		if (getRefreshFieldName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRefreshFieldName() == null", "refreshFieldName", this);	// NOI18N
		}
		// Validating property refreshFieldScope
		if (getRefreshFieldScope() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getRefreshFieldScope() == null", "refreshFieldScope", this);	// NOI18N
		}
		// Validating property httpMethod
		for (int _index = 0; _index < sizeHttpMethod(); ++_index) {
			String element = getHttpMethod(_index);
			if (element != null) {
			}
		}
		// Validating property keyField
		for (int _index = 0; _index < sizeKeyField(); ++_index) {
			boolean element = isKeyField(_index);
		}
		// Validating property keyFieldName
		// Validating property keyFieldScope
		// Validating property constraintField
		for (int _index = 0; _index < sizeConstraintField(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.ConstraintField element = getConstraintField(_index);
			if (element != null) {
				element.validate();
			}
		}
		if (orCount != 1) {
			throw new org.netbeans.modules.schema2beans.ValidateException("orCount ("+orCount+") != 1", "mutually exclusive properties", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ServletName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServletName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVLET_NAME, 0, str, indent);

		str.append(indent);
		str.append("UrlPattern");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getUrlPattern();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(URL_PATTERN, 0, str, indent);

		str.append(indent);
		str.append("CacheHelperRef");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getCacheHelperRef();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CACHE_HELPER_REF, 0, str, indent);

		str.append(indent);
		str.append("Dispatcher["+this.sizeDispatcher()+"]");	// NOI18N
		for(int i=0; i<this.sizeDispatcher(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getDispatcher(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(DISPATCHER, i, str, indent);
		}

		str.append(indent);
		str.append("Timeout");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getTimeout();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TIMEOUT, 0, str, indent);

		str.append(indent);
		str.append("RefreshField");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRefreshField()?"true":"false"));
		this.dumpAttributes(REFRESH_FIELD, 0, str, indent);

		str.append(indent);
		str.append("HttpMethod["+this.sizeHttpMethod()+"]");	// NOI18N
		for(int i=0; i<this.sizeHttpMethod(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getHttpMethod(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(HTTP_METHOD, i, str, indent);
		}

		str.append(indent);
		str.append("KeyField["+this.sizeKeyField()+"]");	// NOI18N
		for(int i=0; i<this.sizeKeyField(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append((this.isKeyField(i)?"true":"false"));
			this.dumpAttributes(KEY_FIELD, i, str, indent);
		}

		str.append(indent);
		str.append("ConstraintField["+this.sizeConstraintField()+"]");	// NOI18N
		for(int i=0; i<this.sizeConstraintField(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getConstraintField(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CONSTRAINT_FIELD, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("CacheMapping\n");	// NOI18N
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
