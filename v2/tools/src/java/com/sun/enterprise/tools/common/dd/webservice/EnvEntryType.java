/**
 *	This generated bean class EnvEntryType matches the schema element env-entryType
 *
 *	===============================================================
 *	
 *	
 *		The env-entryType is used to declare an application's
 *		environment entry. The declaration consists of an optional
 *		description, the name of the environment entry, and an
 *		optional value.  If a value is not specified, one must be
 *		supplied during deployment.
 *	
 *		It is used by env-entry elements.
 *	
 *	      
 *	===============================================================
 *	Generated on Fri Apr 22 15:42:52 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class EnvEntryType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String ENV_ENTRY_NAME = "EnvEntryName";	// NOI18N
	static public final String ENV_ENTRY_TYPE = "EnvEntryType";	// NOI18N
	static public final String ENV_ENTRY_VALUE = "EnvEntryValue";	// NOI18N

	public EnvEntryType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EnvEntryType(int options)
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
		this.createProperty("env-entry-name", 	// NOI18N
			ENV_ENTRY_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("env-entry-type", 	// NOI18N
			ENV_ENTRY_TYPE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("env-entry-value", 	// NOI18N
			ENV_ENTRY_VALUE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(ENV_ENTRY_VALUE, "id", "Id", 
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

	// This attribute is mandatory
	public void setEnvEntryName(java.lang.String value) {
		this.setValue(ENV_ENTRY_NAME, value);
	}

	//
	public java.lang.String getEnvEntryName() {
		return (java.lang.String)this.getValue(ENV_ENTRY_NAME);
	}

	// This attribute is mandatory
	public void setEnvEntryType(java.lang.String value) {
		this.setValue(ENV_ENTRY_TYPE, value);
	}

	//
	public java.lang.String getEnvEntryType() {
		return (java.lang.String)this.getValue(ENV_ENTRY_TYPE);
	}

	// This attribute is optional
	public void setEnvEntryValue(java.lang.String value) {
		this.setValue(ENV_ENTRY_VALUE, value);
	}

	//
	public java.lang.String getEnvEntryValue() {
		return (java.lang.String)this.getValue(ENV_ENTRY_VALUE);
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
		str.append("EnvEntryName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEnvEntryName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENV_ENTRY_NAME, 0, str, indent);

		str.append(indent);
		str.append("EnvEntryType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEnvEntryType();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENV_ENTRY_TYPE, 0, str, indent);

		str.append(indent);
		str.append("EnvEntryValue");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEnvEntryValue();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENV_ENTRY_VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EnvEntryType\n");	// NOI18N
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
