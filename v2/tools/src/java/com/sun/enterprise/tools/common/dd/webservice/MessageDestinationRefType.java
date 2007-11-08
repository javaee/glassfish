/**
 *	This generated bean class MessageDestinationRefType matches the schema element message-destination-refType
 *
 *	===============================================================
 *	
 *		
 *	
 *		  The message-destination-ref element contains a declaration
 *		  of Deployment Component's reference to a message destination
 *		  associated with a resource in Deployment Component's
 *		  environment. It consists of:
 *	
 *			  - an optional description
 *			  - the message destination reference name
 *			  - the message destination type
 *			  - a specification as to whether the
 *			    destination is used for
 *			    consuming or producing messages, or both
 *			  - a link to the message destination
 *	
 *		  Examples:
 *	
 *		  <message-destination-ref>
 *			  <message-destination-ref-name>jms/StockQueue
 *			  </message-destination-ref-name>
 *			  <message-destination-type>javax.jms.Queue
 *			  </message-destination-type>
 *			  <message-destination-usage>Consumes
 *			  </message-destination-usage>
 *			  <message-destination-link>CorporateStocks
 *			  </message-destination-link>
 *		  </message-destination-ref>
 *	
 *		  
 *	      
 *	===============================================================
 *	Generated on Fri Apr 22 15:43:00 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MessageDestinationRefType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String DESCRIPTION = "Description";	// NOI18N
	static public final String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName";	// NOI18N
	static public final String MESSAGE_DESTINATION_TYPE = "MessageDestinationType";	// NOI18N
	static public final String MESSAGE_DESTINATION_USAGE = "MessageDestinationUsage";	// NOI18N
	static public final String MESSAGE_DESTINATION_LINK = "MessageDestinationLink";	// NOI18N

	public MessageDestinationRefType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageDestinationRefType(int options)
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
		this.createProperty("message-destination-ref-name", 	// NOI18N
			MESSAGE_DESTINATION_REF_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("message-destination-type", 	// NOI18N
			MESSAGE_DESTINATION_TYPE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("message-destination-usage", 	// NOI18N
			MESSAGE_DESTINATION_USAGE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("message-destination-link", 	// NOI18N
			MESSAGE_DESTINATION_LINK, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
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
	public void setMessageDestinationRefName(java.lang.String value) {
		this.setValue(MESSAGE_DESTINATION_REF_NAME, value);
	}

	//
	public java.lang.String getMessageDestinationRefName() {
		return (java.lang.String)this.getValue(MESSAGE_DESTINATION_REF_NAME);
	}

	// This attribute is mandatory
	public void setMessageDestinationType(java.lang.String value) {
		this.setValue(MESSAGE_DESTINATION_TYPE, value);
	}

	//
	public java.lang.String getMessageDestinationType() {
		return (java.lang.String)this.getValue(MESSAGE_DESTINATION_TYPE);
	}

	// This attribute is mandatory
	public void setMessageDestinationUsage(java.lang.String value) {
		this.setValue(MESSAGE_DESTINATION_USAGE, value);
	}

	//
	public java.lang.String getMessageDestinationUsage() {
		return (java.lang.String)this.getValue(MESSAGE_DESTINATION_USAGE);
	}

	// This attribute is optional
	public void setMessageDestinationLink(java.lang.String value) {
		this.setValue(MESSAGE_DESTINATION_LINK, value);
	}

	//
	public java.lang.String getMessageDestinationLink() {
		return (java.lang.String)this.getValue(MESSAGE_DESTINATION_LINK);
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
		str.append("MessageDestinationRefName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMessageDestinationRefName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_DESTINATION_REF_NAME, 0, str, indent);

		str.append(indent);
		str.append("MessageDestinationType");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMessageDestinationType();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_DESTINATION_TYPE, 0, str, indent);

		str.append(indent);
		str.append("MessageDestinationUsage");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMessageDestinationUsage();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_DESTINATION_USAGE, 0, str, indent);

		str.append(indent);
		str.append("MessageDestinationLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getMessageDestinationLink();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(MESSAGE_DESTINATION_LINK, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageDestinationRefType\n");	// NOI18N
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
