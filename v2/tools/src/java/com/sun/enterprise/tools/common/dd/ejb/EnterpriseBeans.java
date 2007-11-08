/**
 *	This generated bean class EnterpriseBeans matches the schema element enterprise-beans
 *
 *	Generated on Wed Mar 03 14:29:49 PST 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import com.sun.enterprise.tools.common.dd.MessageDestination;
import com.sun.enterprise.tools.common.dd.WebserviceDescription;

// BEGIN_NOI18N

public class EnterpriseBeans extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	// NOI18N
	static public final String UNIQUE_ID = "UniqueId";	// NOI18N
	static public final String EJB = "Ejb";	// NOI18N
	static public final String PM_DESCRIPTORS = "PmDescriptors";	// NOI18N
	static public final String CMP_RESOURCE = "CmpResource";	// NOI18N
	static public final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
	static public final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N

	public EnterpriseBeans() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EnterpriseBeans(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("unique-id", 	// NOI18N
			UNIQUE_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("ejb", 	// NOI18N
			EJB, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Ejb.class);
		this.createAttribute(EJB, "availability-enabled", "AvailabilityEnabled", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("pm-descriptors", 	// NOI18N
			PM_DESCRIPTORS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			PmDescriptors.class);
		this.createProperty("cmp-resource", 	// NOI18N
			CMP_RESOURCE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CmpResource.class);
		this.createProperty("message-destination", 	// NOI18N
			MESSAGE_DESTINATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestination.class);
		this.createProperty("webservice-description", 	// NOI18N
			WEBSERVICE_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebserviceDescription.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is optional
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is optional
	public void setUniqueId(String value) {
		this.setValue(UNIQUE_ID, value);
	}

	//
	public String getUniqueId() {
		return (String)this.getValue(UNIQUE_ID);
	}

	// This attribute is an array, possibly empty
	public void setEjb(int index, Ejb value) {
		this.setValue(EJB, index, value);
	}

	//
	public Ejb getEjb(int index) {
		return (Ejb)this.getValue(EJB, index);
	}

	// This attribute is an array, possibly empty
	public void setEjb(Ejb[] value) {
		this.setValue(EJB, value);
	}

	//
	public Ejb[] getEjb() {
		return (Ejb[])this.getValues(EJB);
	}

	// Return the number of properties
	public int sizeEjb() {
		return this.size(EJB);
	}

	// Add a new element returning its index in the list
	public int addEjb(Ejb value) {
		return this.addValue(EJB, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjb(Ejb value) {
		return this.removeValue(EJB, value);
	}

	// This attribute is optional
	public void setPmDescriptors(PmDescriptors value) {
		this.setValue(PM_DESCRIPTORS, value);
	}

	//
	public PmDescriptors getPmDescriptors() {
		return (PmDescriptors)this.getValue(PM_DESCRIPTORS);
	}

	// This attribute is optional
	public void setCmpResource(CmpResource value) {
		this.setValue(CMP_RESOURCE, value);
	}

	//
	public CmpResource getCmpResource() {
		return (CmpResource)this.getValue(CMP_RESOURCE);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestination(int index, MessageDestination value) {
		this.setValue(MESSAGE_DESTINATION, index, value);
	}

	//
	public MessageDestination getMessageDestination(int index) {
		return (MessageDestination)this.getValue(MESSAGE_DESTINATION, index);
	}

	// This attribute is an array, possibly empty
	public void setMessageDestination(MessageDestination[] value) {
		this.setValue(MESSAGE_DESTINATION, value);
	}

	//
	public MessageDestination[] getMessageDestination() {
		return (MessageDestination[])this.getValues(MESSAGE_DESTINATION);
	}

	// Return the number of properties
	public int sizeMessageDestination() {
		return this.size(MESSAGE_DESTINATION);
	}

	// Add a new element returning its index in the list
	public int addMessageDestination(MessageDestination value) {
		return this.addValue(MESSAGE_DESTINATION, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageDestination(MessageDestination value) {
		return this.removeValue(MESSAGE_DESTINATION, value);
	}

	// This attribute is an array, possibly empty
	public void setWebserviceDescription(int index, WebserviceDescription value) {
		this.setValue(WEBSERVICE_DESCRIPTION, index, value);
	}

	//
	public WebserviceDescription getWebserviceDescription(int index) {
		return (WebserviceDescription)this.getValue(WEBSERVICE_DESCRIPTION, index);
	}

	// This attribute is an array, possibly empty
	public void setWebserviceDescription(WebserviceDescription[] value) {
		this.setValue(WEBSERVICE_DESCRIPTION, value);
	}

	//
	public WebserviceDescription[] getWebserviceDescription() {
		return (WebserviceDescription[])this.getValues(WEBSERVICE_DESCRIPTION);
	}

	// Return the number of properties
	public int sizeWebserviceDescription() {
		return this.size(WEBSERVICE_DESCRIPTION);
	}

	// Add a new element returning its index in the list
	public int addWebserviceDescription(WebserviceDescription value) {
		return this.addValue(WEBSERVICE_DESCRIPTION, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWebserviceDescription(WebserviceDescription value) {
		return this.removeValue(WEBSERVICE_DESCRIPTION, value);
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
		// Validating property name
		if (getName() != null) {
		}
		// Validating property uniqueId
		if (getUniqueId() != null) {
		}
		// Validating property ejb
		for (int _index = 0; _index < sizeEjb(); ++_index) {
			Ejb element = getEjb(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property pmDescriptors
		if (getPmDescriptors() != null) {
			getPmDescriptors().validate();
		}
		// Validating property cmpResource
		if (getCmpResource() != null) {
			getCmpResource().validate();
		}
		// Validating property messageDestination
		for (int _index = 0; _index < sizeMessageDestination(); ++_index) {
			MessageDestination element = getMessageDestination(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property webserviceDescription
		for (int _index = 0; _index < sizeWebserviceDescription(); 
			++_index) {
			WebserviceDescription element = getWebserviceDescription(_index);
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
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("UniqueId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getUniqueId();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIQUE_ID, 0, str, indent);

		str.append(indent);
		str.append("Ejb["+this.sizeEjb()+"]");	// NOI18N
		for(int i=0; i<this.sizeEjb(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjb(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EJB, i, str, indent);
		}

		str.append(indent);
		str.append("PmDescriptors");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPmDescriptors();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PM_DESCRIPTORS, 0, str, indent);

		str.append(indent);
		str.append("CmpResource");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCmpResource();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CMP_RESOURCE, 0, str, indent);

		str.append(indent);
		str.append("MessageDestination["+this.sizeMessageDestination()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageDestination(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageDestination(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_DESTINATION, i, str, indent);
		}

		str.append(indent);
		str.append("WebserviceDescription["+this.sizeWebserviceDescription()+"]");	// NOI18N
		for(int i=0; i<this.sizeWebserviceDescription(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebserviceDescription(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEBSERVICE_DESCRIPTION, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EnterpriseBeans\n");	// NOI18N
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
