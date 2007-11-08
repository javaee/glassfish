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
 *	This generated bean class MessageSecurityBinding matches the schema element 'message-security-binding'.
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

public class MessageSecurityBinding extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String AUTHLAYER = "AuthLayer";	// NOI18N
	static public final String PROVIDERID = "ProviderId";	// NOI18N
	static public final String MESSAGE_SECURITY = "MessageSecurity";	// NOI18N

	public MessageSecurityBinding() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageSecurityBinding(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("message-security", 	// NOI18N
			MESSAGE_SECURITY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageSecurity.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setAuthLayer(java.lang.String value) {
		setAttributeValue(AUTHLAYER, value);
	}

	//
	public java.lang.String getAuthLayer() {
		return getAttributeValue(AUTHLAYER);
	}

	// This attribute is optional
	public void setProviderId(java.lang.String value) {
		setAttributeValue(PROVIDERID, value);
	}

	//
	public java.lang.String getProviderId() {
		return getAttributeValue(PROVIDERID);
	}

	// This attribute is an array, possibly empty
	public void setMessageSecurity(int index, MessageSecurity value) {
		this.setValue(MESSAGE_SECURITY, index, value);
	}

	//
	public MessageSecurity getMessageSecurity(int index) {
		return (MessageSecurity)this.getValue(MESSAGE_SECURITY, index);
	}

	// Return the number of properties
	public int sizeMessageSecurity() {
		return this.size(MESSAGE_SECURITY);
	}

	// This attribute is an array, possibly empty
	public void setMessageSecurity(MessageSecurity[] value) {
		this.setValue(MESSAGE_SECURITY, value);
	}

	//
	public MessageSecurity[] getMessageSecurity() {
		return (MessageSecurity[])this.getValues(MESSAGE_SECURITY);
	}

	// Add a new element returning its index in the list
	public int addMessageSecurity(com.sun.enterprise.tools.common.dd.MessageSecurity value) {
		int positionOfNewItem = this.addValue(MESSAGE_SECURITY, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageSecurity(com.sun.enterprise.tools.common.dd.MessageSecurity value) {
		return this.removeValue(MESSAGE_SECURITY, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageSecurity newMessageSecurity() {
		return new MessageSecurity();
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
		str.append("MessageSecurity["+this.sizeMessageSecurity()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageSecurity(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageSecurity(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_SECURITY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageSecurityBinding\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

