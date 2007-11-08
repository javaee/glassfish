/**
 *	This generated bean class PrefetchDisabled matches the schema element prefetch-disabled
 *
 *	Generated on Thu Apr 08 14:40:31 PDT 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PrefetchDisabled extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String QUERY_METHOD = "QueryMethod";	// NOI18N

	public PrefetchDisabled() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PrefetchDisabled(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("query-method", 	// NOI18N
			QUERY_METHOD, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			QueryMethod.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array containing at least one element
	public void setQueryMethod(int index, QueryMethod value) {
		this.setValue(QUERY_METHOD, index, value);
	}

	//
	public QueryMethod getQueryMethod(int index) {
		return (QueryMethod)this.getValue(QUERY_METHOD, index);
	}

	// This attribute is an array containing at least one element
	public void setQueryMethod(QueryMethod[] value) {
		this.setValue(QUERY_METHOD, value);
	}

	//
	public QueryMethod[] getQueryMethod() {
		return (QueryMethod[])this.getValues(QUERY_METHOD);
	}

	// Return the number of properties
	public int sizeQueryMethod() {
		return this.size(QUERY_METHOD);
	}

	// Add a new element returning its index in the list
	public int addQueryMethod(com.sun.enterprise.tools.common.dd.ejb.QueryMethod value) {
		return this.addValue(QUERY_METHOD, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeQueryMethod(com.sun.enterprise.tools.common.dd.ejb.QueryMethod value) {
		return this.removeValue(QUERY_METHOD, value);
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
		// Validating property queryMethod
		if (sizeQueryMethod() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizeQueryMethod() == 0", "queryMethod", this);	// NOI18N
		}
		for (int _index = 0; _index < sizeQueryMethod(); ++_index) {
			com.sun.enterprise.tools.common.dd.ejb.QueryMethod element = getQueryMethod(_index);
			if (element != null) {
				element.validate();
			}
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("QueryMethod["+this.sizeQueryMethod()+"]");	// NOI18N
		for(int i=0; i<this.sizeQueryMethod(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getQueryMethod(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(QUERY_METHOD, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PrefetchDisabled\n");	// NOI18N
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
