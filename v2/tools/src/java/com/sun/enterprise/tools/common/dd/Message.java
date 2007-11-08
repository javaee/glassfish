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
 *	This generated bean class Message matches the schema element 'message'.
 *  The root bean class is SunEjbJar
 *
 *	Generated on Tue Aug 08 09:56:25 PDT 2006
 * @Generated
 */

package com.sun.enterprise.tools.common.dd;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class Message extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String JAVA_METHOD = "JavaMethod";	// NOI18N
	static public final String OPERATION_NAME = "OperationName";	// NOI18N

	public Message() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Message(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("java-method", 	// NOI18N
			JAVA_METHOD, Common.SEQUENCE_OR | 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JavaMethod.class);
		this.createProperty("operation-name", 	// NOI18N
			OPERATION_NAME, Common.SEQUENCE_OR | 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setJavaMethod(JavaMethod value) {
		this.setValue(JAVA_METHOD, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setOperationName(null);
		}
	}

	//
	public JavaMethod getJavaMethod() {
		return (JavaMethod)this.getValue(JAVA_METHOD);
	}

	// This attribute is optional
	public void setOperationName(String value) {
		this.setValue(OPERATION_NAME, value);
		if (value != null) {
			// It's a mutually exclusive property.
			setJavaMethod(null);
		}
	}

	//
	public String getOperationName() {
		return (String)this.getValue(OPERATION_NAME);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public JavaMethod newJavaMethod() {
		return new JavaMethod();
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
		str.append("JavaMethod");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJavaMethod();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JAVA_METHOD, 0, str, indent);

		str.append(indent);
		str.append("OperationName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getOperationName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(OPERATION_NAME, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Message\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

