/**
 *	This generated bean class LocaleCharsetInfo matches the schema element locale-charset-info
 *
 *	Generated on Sun Feb 29 21:00:47 PST 2004
 */

package com.sun.enterprise.tools.common.dd.webapp;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class LocaleCharsetInfo extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String DEFAULTLOCALE = "DefaultLocale";	// NOI18N
	static public final String LOCALE_CHARSET_MAP = "LocaleCharsetMap";	// NOI18N
	static public final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
	static public final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
	static public final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N

	public LocaleCharsetInfo() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LocaleCharsetInfo(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("locale-charset-map", 	// NOI18N
			LOCALE_CHARSET_MAP, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LocaleCharsetMap.class);
		this.createAttribute(LOCALE_CHARSET_MAP, "locale", "Locale", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LOCALE_CHARSET_MAP, "agent", "Agent", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOCALE_CHARSET_MAP, "charset", "Charset", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("parameter-encoding", 	// NOI18N
			PARAMETER_ENCODING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(PARAMETER_ENCODING, "form-hint-field", "FormHintField", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PARAMETER_ENCODING, "default-charset", "DefaultCharset", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is optional
	public void setDefaultLocale(java.lang.String value) {
		setAttributeValue(DEFAULTLOCALE, value);
	}

	//
	public java.lang.String getDefaultLocale() {
		return getAttributeValue(DEFAULTLOCALE);
	}

	// This attribute is an array containing at least one element
	public void setLocaleCharsetMap(int index, LocaleCharsetMap value) {
		this.setValue(LOCALE_CHARSET_MAP, index, value);
	}

	//
	public LocaleCharsetMap getLocaleCharsetMap(int index) {
		return (LocaleCharsetMap)this.getValue(LOCALE_CHARSET_MAP, index);
	}

	// This attribute is an array containing at least one element
	public void setLocaleCharsetMap(LocaleCharsetMap[] value) {
		this.setValue(LOCALE_CHARSET_MAP, value);
	}

	//
	public LocaleCharsetMap[] getLocaleCharsetMap() {
		return (LocaleCharsetMap[])this.getValues(LOCALE_CHARSET_MAP);
	}

	// Return the number of properties
	public int sizeLocaleCharsetMap() {
		return this.size(LOCALE_CHARSET_MAP);
	}

	// Add a new element returning its index in the list
	public int addLocaleCharsetMap(com.sun.enterprise.tools.common.dd.webapp.LocaleCharsetMap value) {
		return this.addValue(LOCALE_CHARSET_MAP, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLocaleCharsetMap(com.sun.enterprise.tools.common.dd.webapp.LocaleCharsetMap value) {
		return this.removeValue(LOCALE_CHARSET_MAP, value);
	}

	// This attribute is optional
	public void setParameterEncoding(boolean value) {
		this.setValue(PARAMETER_ENCODING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isParameterEncoding() {
		Boolean ret = (Boolean)this.getValue(PARAMETER_ENCODING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setParameterEncodingFormHintField(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PARAMETER_ENCODING) == 0) {
			setValue(PARAMETER_ENCODING, "");
		}
		setAttributeValue(PARAMETER_ENCODING, "FormHintField", value);
	}

	//
	public java.lang.String getParameterEncodingFormHintField() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PARAMETER_ENCODING) == 0) {
			return null;
		} else {
			return getAttributeValue(PARAMETER_ENCODING, "FormHintField");
		}
	}

	// This attribute is optional
	public void setParameterEncodingDefaultCharset(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PARAMETER_ENCODING) == 0) {
			setValue(PARAMETER_ENCODING, "");
		}
		setAttributeValue(PARAMETER_ENCODING, "DefaultCharset", value);
	}

	//
	public java.lang.String getParameterEncodingDefaultCharset() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PARAMETER_ENCODING) == 0) {
			return null;
		} else {
			return getAttributeValue(PARAMETER_ENCODING, "DefaultCharset");
		}
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
		// Validating property defaultLocale
		if (getDefaultLocale() != null) {
		}
		// Validating property localeCharsetMap
		if (sizeLocaleCharsetMap() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeLocaleCharsetMap() == 0", "localeCharsetMap", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeLocaleCharsetMap(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.LocaleCharsetMap element = getLocaleCharsetMap(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property parameterEncoding
		// Validating property parameterEncodingFormHintField
		if (getParameterEncodingFormHintField() != null) {
		}
		// Validating property parameterEncodingDefaultCharset
		if (getParameterEncodingDefaultCharset() != null) {
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("LocaleCharsetMap["+this.sizeLocaleCharsetMap()+"]");	// NOI18N
		for(int i=0; i<this.sizeLocaleCharsetMap(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLocaleCharsetMap(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LOCALE_CHARSET_MAP, i, str, indent);
		}

		str.append(indent);
		str.append("ParameterEncoding");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isParameterEncoding()?"true":"false"));
		this.dumpAttributes(PARAMETER_ENCODING, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("LocaleCharsetInfo\n");	// NOI18N
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
