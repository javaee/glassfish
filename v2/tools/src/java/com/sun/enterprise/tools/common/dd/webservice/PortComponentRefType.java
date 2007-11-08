/**
 *	This generated bean class PortComponentRefType matches the schema element port-component-refType
 *
 *	===============================================================
 *	
 *	
 *		    The port-component-ref element declares a client dependency
 *		    on the container for resolving a Service Endpoint Interface
 *		    to a WSDL port. It optionally associates the Service Endpoint
 *		    Interface with a particular port-component. This is only used
 *		    by the container for a Service.getPort(Class) method call.
 *	
 *		  
 *	===============================================================
 *	Generated on Fri Apr 22 15:42:58 PDT 2005
 */

package com.sun.enterprise.tools.common.dd.webservice;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class PortComponentRefType extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();

	static public final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
	static public final String PORT_COMPONENT_LINK = "PortComponentLink";	// NOI18N

	public PortComponentRefType() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public PortComponentRefType(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("service-endpoint-interface", 	// NOI18N
			SERVICE_ENDPOINT_INTERFACE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createProperty("port-component-link", 	// NOI18N
			PORT_COMPONENT_LINK, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			java.lang.String.class);
		this.createAttribute(PORT_COMPONENT_LINK, "id", "Id", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is mandatory
	public void setServiceEndpointInterface(java.lang.String value) {
		this.setValue(SERVICE_ENDPOINT_INTERFACE, value);
	}

	//
	public java.lang.String getServiceEndpointInterface() {
		return (java.lang.String)this.getValue(SERVICE_ENDPOINT_INTERFACE);
	}

	// This attribute is optional
	public void setPortComponentLink(java.lang.String value) {
		this.setValue(PORT_COMPONENT_LINK, value);
	}

	//
	public java.lang.String getPortComponentLink() {
		return (java.lang.String)this.getValue(PORT_COMPONENT_LINK);
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
		str.append("ServiceEndpointInterface");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServiceEndpointInterface();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVICE_ENDPOINT_INTERFACE, 0, str, indent);

		str.append(indent);
		str.append("PortComponentLink");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPortComponentLink();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PORT_COMPONENT_LINK, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("PortComponentRefType\n");	// NOI18N
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
