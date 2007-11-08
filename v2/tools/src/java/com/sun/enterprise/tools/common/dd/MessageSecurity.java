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
 *	This generated bean class MessageSecurity matches the schema element 'message-security'.
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

public class MessageSecurity extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String MESSAGE = "Message";	// NOI18N
	static public final String REQUEST_PROTECTION = "RequestProtection";	// NOI18N
	static public final String REQUESTPROTECTIONAUTHSOURCE = "RequestProtectionAuthSource";	// NOI18N
	static public final String REQUESTPROTECTIONAUTHRECIPIENT = "RequestProtectionAuthRecipient";	// NOI18N
	static public final String RESPONSE_PROTECTION = "ResponseProtection";	// NOI18N
	static public final String RESPONSEPROTECTIONAUTHSOURCE = "ResponseProtectionAuthSource";	// NOI18N
	static public final String RESPONSEPROTECTIONAUTHRECIPIENT = "ResponseProtectionAuthRecipient";	// NOI18N

	public MessageSecurity() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageSecurity(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("message", 	// NOI18N
			MESSAGE, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Message.class);
		this.createProperty("request-protection", 	// NOI18N
			REQUEST_PROTECTION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(REQUEST_PROTECTION, "auth-source", "AuthSource", 
						AttrProp.ENUM | AttrProp.IMPLIED,
						new String[] {
							"sender",
							"content"
						}, null);
		this.createAttribute(REQUEST_PROTECTION, "auth-recipient", "AuthRecipient", 
						AttrProp.ENUM | AttrProp.IMPLIED,
						new String[] {
							"before-content",
							"after-content"
						}, null);
		this.createProperty("response-protection", 	// NOI18N
			RESPONSE_PROTECTION, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(RESPONSE_PROTECTION, "auth-source", "AuthSource", 
						AttrProp.ENUM | AttrProp.IMPLIED,
						new String[] {
							"sender",
							"content"
						}, null);
		this.createAttribute(RESPONSE_PROTECTION, "auth-recipient", "AuthRecipient", 
						AttrProp.ENUM | AttrProp.IMPLIED,
						new String[] {
							"before-content",
							"after-content"
						}, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array containing at least one element
	public void setMessage(int index, Message value) {
		this.setValue(MESSAGE, index, value);
	}

	//
	public Message getMessage(int index) {
		return (Message)this.getValue(MESSAGE, index);
	}

	// Return the number of properties
	public int sizeMessage() {
		return this.size(MESSAGE);
	}

	// This attribute is an array containing at least one element
	public void setMessage(Message[] value) {
		this.setValue(MESSAGE, value);
	}

	//
	public Message[] getMessage() {
		return (Message[])this.getValues(MESSAGE);
	}

	// Add a new element returning its index in the list
	public int addMessage(com.sun.enterprise.tools.common.dd.Message value) {
		int positionOfNewItem = this.addValue(MESSAGE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessage(com.sun.enterprise.tools.common.dd.Message value) {
		return this.removeValue(MESSAGE, value);
	}

	// This attribute is optional
	public void setRequestProtection(boolean value) {
		this.setValue(REQUEST_PROTECTION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isRequestProtection() {
		Boolean ret = (Boolean)this.getValue(REQUEST_PROTECTION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setRequestProtectionAuthSource(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_PROTECTION) == 0) {
			setValue(REQUEST_PROTECTION, java.lang.Boolean.TRUE);
		}
		setAttributeValue(REQUEST_PROTECTION, "AuthSource", value);
	}

	//
	public java.lang.String getRequestProtectionAuthSource() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_PROTECTION) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_PROTECTION, "AuthSource");
		}
	}

	// This attribute is optional
	public void setRequestProtectionAuthRecipient(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(REQUEST_PROTECTION) == 0) {
			setValue(REQUEST_PROTECTION, java.lang.Boolean.TRUE);
		}
		setAttributeValue(REQUEST_PROTECTION, "AuthRecipient", value);
	}

	//
	public java.lang.String getRequestProtectionAuthRecipient() {
		// If our element does not exist, then the attribute does not exist.
		if (size(REQUEST_PROTECTION) == 0) {
			return null;
		} else {
			return getAttributeValue(REQUEST_PROTECTION, "AuthRecipient");
		}
	}

	// This attribute is optional
	public void setResponseProtection(boolean value) {
		this.setValue(RESPONSE_PROTECTION, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isResponseProtection() {
		Boolean ret = (Boolean)this.getValue(RESPONSE_PROTECTION);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setResponseProtectionAuthSource(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(RESPONSE_PROTECTION) == 0) {
			setValue(RESPONSE_PROTECTION, java.lang.Boolean.TRUE);
		}
		setAttributeValue(RESPONSE_PROTECTION, "AuthSource", value);
	}

	//
	public java.lang.String getResponseProtectionAuthSource() {
		// If our element does not exist, then the attribute does not exist.
		if (size(RESPONSE_PROTECTION) == 0) {
			return null;
		} else {
			return getAttributeValue(RESPONSE_PROTECTION, "AuthSource");
		}
	}

	// This attribute is optional
	public void setResponseProtectionAuthRecipient(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(RESPONSE_PROTECTION) == 0) {
			setValue(RESPONSE_PROTECTION, java.lang.Boolean.TRUE);
		}
		setAttributeValue(RESPONSE_PROTECTION, "AuthRecipient", value);
	}

	//
	public java.lang.String getResponseProtectionAuthRecipient() {
		// If our element does not exist, then the attribute does not exist.
		if (size(RESPONSE_PROTECTION) == 0) {
			return null;
		} else {
			return getAttributeValue(RESPONSE_PROTECTION, "AuthRecipient");
		}
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Message newMessage() {
		return new Message();
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
		str.append("Message["+this.sizeMessage()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessage(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessage(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE, i, str, indent);
		}

		str.append(indent);
		str.append("RequestProtection");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isRequestProtection()?"true":"false"));
		this.dumpAttributes(REQUEST_PROTECTION, 0, str, indent);

		str.append(indent);
		str.append("ResponseProtection");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isResponseProtection()?"true":"false"));
		this.dumpAttributes(RESPONSE_PROTECTION, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageSecurity\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

