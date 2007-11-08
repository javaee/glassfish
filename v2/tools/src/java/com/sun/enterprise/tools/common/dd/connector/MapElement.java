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
 *	This generated bean class MapElement matches the schema element map-element
 *
 *	Generated on Thu Jul 31 18:16:39 PDT 2003
 */

package com.sun.enterprise.tools.common.dd.connector;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class MapElement extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String PRINCIPAL = "Principal";	// NOI18N
	static public final String BACKEND_PRINCIPAL = "BackendPrincipal";	// NOI18N
	static public final String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName";	// NOI18N
	static public final String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword";	// NOI18N
	static public final String BACKENDPRINCIPALCREDENTIAL = "BackendPrincipalCredential";	// NOI18N

	public MapElement() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MapElement(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("principal", 	// NOI18N
			PRINCIPAL, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Principal.class);
		this.createAttribute(PRINCIPAL, "user-name", "UserName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("backend-principal", 	// NOI18N
			BACKEND_PRINCIPAL, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(BACKEND_PRINCIPAL, "user-name", "UserName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(BACKEND_PRINCIPAL, "password", "Password", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(BACKEND_PRINCIPAL, "credential", "Credential", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
	
	}

	// This attribute is an array containing at least one element
	public void setPrincipal(int index, Principal value) {
		this.setValue(PRINCIPAL, index, value);
	}

	//
	public Principal getPrincipal(int index) {
		return (Principal)this.getValue(PRINCIPAL, index);
	}

	// This attribute is an array containing at least one element
	public void setPrincipal(Principal[] value) {
		this.setValue(PRINCIPAL, value);
	}

	//
	public Principal[] getPrincipal() {
		return (Principal[])this.getValues(PRINCIPAL);
	}

	// Return the number of properties
	public int sizePrincipal() {
		return this.size(PRINCIPAL);
	}

	// Add a new element returning its index in the list
	public int addPrincipal(com.sun.enterprise.tools.common.dd.connector.Principal value) {
		return this.addValue(PRINCIPAL, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePrincipal(com.sun.enterprise.tools.common.dd.connector.Principal value) {
		return this.removeValue(PRINCIPAL, value);
	}

	// This attribute is mandatory
	public void setBackendPrincipal(boolean value) {
		this.setValue(BACKEND_PRINCIPAL, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isBackendPrincipal() {
		Boolean ret = (Boolean)this.getValue(BACKEND_PRINCIPAL);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is mandatory
	public void setBackendPrincipalUserName(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(BACKEND_PRINCIPAL) == 0) {
			setValue(BACKEND_PRINCIPAL, "");
		}
		setAttributeValue(BACKEND_PRINCIPAL, "UserName", value);
	}

	//
	public java.lang.String getBackendPrincipalUserName() {
		// If our element does not exist, then the attribute does not exist.
		if (size(BACKEND_PRINCIPAL) == 0) {
			return null;
		} else {
			return getAttributeValue(BACKEND_PRINCIPAL, "UserName");
		}
	}

	// This attribute is mandatory
	public void setBackendPrincipalPassword(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(BACKEND_PRINCIPAL) == 0) {
			setValue(BACKEND_PRINCIPAL, "");
		}
		setAttributeValue(BACKEND_PRINCIPAL, "Password", value);
	}

	//
	public java.lang.String getBackendPrincipalPassword() {
		// If our element does not exist, then the attribute does not exist.
		if (size(BACKEND_PRINCIPAL) == 0) {
			return null;
		} else {
			return getAttributeValue(BACKEND_PRINCIPAL, "Password");
		}
	}

	// This attribute is mandatory
	public void setBackendPrincipalCredential(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(BACKEND_PRINCIPAL) == 0) {
			setValue(BACKEND_PRINCIPAL, "");
		}
		setAttributeValue(BACKEND_PRINCIPAL, "Credential", value);
	}

	//
	public java.lang.String getBackendPrincipalCredential() {
		// If our element does not exist, then the attribute does not exist.
		if (size(BACKEND_PRINCIPAL) == 0) {
			return null;
		} else {
			return getAttributeValue(BACKEND_PRINCIPAL, "Credential");
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
		// Validating property principal
		if (sizePrincipal() == 0) {
			throw new org.netbeans.modules.schema2beans.ValidateException("sizePrincipal() == 0", "principal", this);	// NOI18N
		}
		for (int _index = 0; _index < sizePrincipal(); ++_index) {
			com.sun.enterprise.tools.common.dd.connector.Principal element = getPrincipal(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property backendPrincipal
		// Validating property backendPrincipalUserName
		if (getBackendPrincipalUserName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getBackendPrincipalUserName() == null", "backendPrincipalUserName", this);	// NOI18N
		}
		// Validating property backendPrincipalPassword
		if (getBackendPrincipalPassword() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getBackendPrincipalPassword() == null", "backendPrincipalPassword", this);	// NOI18N
		}
		// Validating property backendPrincipalCredential
		if (getBackendPrincipalCredential() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getBackendPrincipalCredential() == null", "backendPrincipalCredential", this);	// NOI18N
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Principal["+this.sizePrincipal()+"]");	// NOI18N
		for(int i=0; i<this.sizePrincipal(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getPrincipal(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PRINCIPAL, i, str, indent);
		}

		str.append(indent);
		str.append("BackendPrincipal");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isBackendPrincipal()?"true":"false"));
		this.dumpAttributes(BACKEND_PRINCIPAL, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MapElement\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!-- 
  XML DTD for Sun ONE Application Server specific J2EE Resource Adapter
  deployment descriptor. This is a companion DTD to connector_1_5.xsd

  $Revision: 1.3 $
-->

<!-- Each deployed Resource Adapter (RAR) Module, will have ias-ra.xml 
     associated with it, which specifies several dynamic configuration 
     properties. Key aspects are pool sizing and security role maps.
 -->
<!ELEMENT sun-connector (resource-adapter, role-map?)>

<!-- Resource adapter configuration 

     jndi-name          name by which, this adapter will appear in JNDI tree
     max-pool-size      maximum size of connection to EIS
     steady-pool-size   initial and minimum number of connections to be maintained
     max-wait-in-millis if a connection is not readily found, caller will have to 
                        wait this long, before a connection is created. A value of
                        0 implies, wait till a connection becomes available. If the
                        pool is completely utilized and the timer expires, an 
                        exception will be delivered to the application.

     idle-timeout-in-seconds A timer thread periodically removed unused connections.
                             The interval at which this thread runs. All idle 
                             connections will be removed, while mainataining
                             the configured steady-pool-size.
-->
<!ELEMENT resource-adapter (description?, property*)>
<!ATTLIST resource-adapter jndi-name                        CDATA     #REQUIRED
                           max-pool-size                    CDATA     "32"
                           steady-pool-size                 CDATA     "4" 
                           max-wait-time-in-millis          CDATA     "10000" 
                           idle-timeout-in-seconds          CDATA     "1000">

<!-- Perform mapping from principal received during Servlet/EJB 
     authentication, to credentials accepted by the EIS. This 
     mapping is optional. The map consists of several 2-tuples
     map-id is the name of the mapping
 -->
<!ELEMENT role-map (description?, map-element*)>
<!ATTLIST role-map map-id   CDATA  #REQUIRED>
                

<!-- It is possible to map multiple (server) principal to the 
     same backend principal. 
-->
<!ELEMENT map-element (principal+, backend-principal)>

<!-- Principal of the Servlet and EJB client -->
<!ELEMENT principal (description?)>
<!ATTLIST principal user-name CDATA #REQUIRED>
 
<!-- Backend EIS principal -->
<!ELEMENT backend-principal EMPTY> 
<!ATTLIST backend-principal user-name  CDATA #REQUIRED
                            password   CDATA #REQUIRED
                            credential CDATA #REQUIRED>

<!ELEMENT description (#PCDATA)>

<!-- Syntax for supplying properties as name value pairs -->
<!ELEMENT property EMPTY>
<!ATTLIST property name  CDATA  #REQUIRED
                   value CDATA  #REQUIRED>

*/
