/**
 *	This generated bean class Method matches the schema element method
 *
 *	Generated on Wed Mar 03 14:29:49 PST 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Method extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String EJB_NAME = "EjbName";	// NOI18N
	static public final String METHOD_NAME = "MethodName";	// NOI18N
	static public final String METHOD_INTF = "MethodIntf";	// NOI18N
	static public final String METHOD_PARAMS = "MethodParams";	// NOI18N

	public Method() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Method(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("description", 	// NOI18N
			DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("ejb-name", 	// NOI18N
			EJB_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("method-name", 	// NOI18N
			METHOD_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("method-intf", 	// NOI18N
			METHOD_INTF, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("method-params", 	// NOI18N
			METHOD_PARAMS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MethodParams.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
				
	}

	// This attribute is optional
	public void setDescription(String value) {
		this.setValue(DESCRIPTION, value);
	}

	//
	public String getDescription() {
		return (String)this.getValue(DESCRIPTION);
	}

	// This attribute is optional
	public void setEjbName(String value) {
		this.setValue(EJB_NAME, value);
	}

	//
	public String getEjbName() {
		return (String)this.getValue(EJB_NAME);
	}

	// This attribute is mandatory
	public void setMethodName(String value) {
		this.setValue(METHOD_NAME, value);
	}

	//
	public String getMethodName() {
		return (String)this.getValue(METHOD_NAME);
	}

	// This attribute is optional
	public void setMethodIntf(String value) {
		this.setValue(METHOD_INTF, value);
	}

	//
	public String getMethodIntf() {
		return (String)this.getValue(METHOD_INTF);
	}

	// This attribute is optional
	public void setMethodParams(MethodParams value) {
		this.setValue(METHOD_PARAMS, value);
	}

	//
	public MethodParams getMethodParams() {
		return (MethodParams)this.getValue(METHOD_PARAMS);
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
		// Validating property description
		if (getDescription() != null) {
		}
		// Validating property ejbName
		if (getEjbName() != null) {
		}
		// Validating property methodName
		if (getMethodName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getMethodName() == null", "methodName", this);	// NOI18N
		}
		// Validating property methodIntf
		if (getMethodIntf() != null) {
		}
		// Validating property methodParams
		if (getMethodParams() != null) {
			getMethodParams().validate();
		}
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
		str.append("EjbName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEjbName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(EJB_NAME, 0, str, indent);

		str.append(indent);
		str.append("MethodName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMethodName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(METHOD_NAME, 0, str, indent);

		str.append(indent);
		str.append("MethodIntf");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMethodIntf();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(METHOD_INTF, 0, str, indent);

		str.append(indent);
		str.append("MethodParams");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMethodParams();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(METHOD_PARAMS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Method\n");	// NOI18N
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
