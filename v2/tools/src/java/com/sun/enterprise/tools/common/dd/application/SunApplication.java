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
 *	This generated bean class SunApplication matches the schema element sun-application
 *
 *	Generated on Thu Jul 31 15:31:46 PDT 2003
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	sun-application : SunApplication
 *		web : Web[0,n]
 *			web-uri : String
 *			context-root : String
 *		pass-by-reference : String?
 *		unique-id : String?
 *		security-role-mapping : SecurityRoleMapping[0,n]
 *			role-name : String
 *			(
 *			  | principal-name : String
 *			  | group-name : String
 *			)[1,n]
 *		realm : String?
 *
 */

package com.sun.enterprise.tools.common.dd.application;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import com.sun.enterprise.tools.common.dd.SecurityRoleMapping;
import com.sun.enterprise.tools.common.dd.PluginData;

// BEGIN_NOI18N

public class SunApplication extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String WEB = "Web";	// NOI18N
	static public final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
	static public final String UNIQUE_ID = "UniqueId";	// NOI18N
	static public final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	static public final String REALM = "Realm";	// NOI18N

	public SunApplication() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public SunApplication(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("sun-application");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "sun-application"));
		}
		Node n = GraphManager.getElementNode("sun-application", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "sun-application", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public SunApplication(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("sun-application", "SunApplication",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, SunApplication.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("web", 	// NOI18N
			WEB, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Web.class);
		this.createProperty("pass-by-reference", 	// NOI18N
			PASS_BY_REFERENCE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("unique-id", 	// NOI18N
			UNIQUE_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("security-role-mapping", 	// NOI18N
			SECURITY_ROLE_MAPPING, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityRoleMapping.class);
		this.createProperty("realm", 	// NOI18N
			REALM, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
			
	}

	// This attribute is an array, possibly empty
	public void setWeb(int index, Web value) {
		this.setValue(WEB, index, value);
	}

	//
	public Web getWeb(int index) {
		return (Web)this.getValue(WEB, index);
	}

	// This attribute is an array, possibly empty
	public void setWeb(Web[] value) {
		this.setValue(WEB, value);
	}

	//
	public Web[] getWeb() {
		return (Web[])this.getValues(WEB);
	}

	// Return the number of properties
	public int sizeWeb() {
		return this.size(WEB);
	}

	// Add a new element returning its index in the list
	public int addWeb(com.sun.enterprise.tools.common.dd.application.Web value) {
		return this.addValue(WEB, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWeb(com.sun.enterprise.tools.common.dd.application.Web value) {
		return this.removeValue(WEB, value);
	}

	// This attribute is optional
	public void setPassByReference(String value) {
		this.setValue(PASS_BY_REFERENCE, value);
	}

	//
	public String getPassByReference() {
		return (String)this.getValue(PASS_BY_REFERENCE);
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
	public void setSecurityRoleMapping(int index, SecurityRoleMapping value) {
		this.setValue(SECURITY_ROLE_MAPPING, index, value);
	}

	//
	public SecurityRoleMapping getSecurityRoleMapping(int index) {
		return (SecurityRoleMapping)this.getValue(SECURITY_ROLE_MAPPING, index);
	}

	// This attribute is an array, possibly empty
	public void setSecurityRoleMapping(SecurityRoleMapping[] value) {
		this.setValue(SECURITY_ROLE_MAPPING, value);
	}

	//
	public SecurityRoleMapping[] getSecurityRoleMapping() {
		return (SecurityRoleMapping[])this.getValues(SECURITY_ROLE_MAPPING);
	}

	// Return the number of properties
	public int sizeSecurityRoleMapping() {
		return this.size(SECURITY_ROLE_MAPPING);
	}

	// Add a new element returning its index in the list
	public int addSecurityRoleMapping(SecurityRoleMapping value) {
		return this.addValue(SECURITY_ROLE_MAPPING, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityRoleMapping(SecurityRoleMapping value) {
		return this.removeValue(SECURITY_ROLE_MAPPING, value);
	}

	// This attribute is optional
	public void setRealm(String value) {
		this.setValue(REALM, value);
	}

	//
	public String getRealm() {
		return (String)this.getValue(REALM);
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static SunApplication createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new SunApplication(doc, Common.NO_DEFAULT_VALUES);
	}

	public static SunApplication createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static SunApplication createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static SunApplication createGraph() {
		try {
			return new SunApplication();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property web
		for (int _index = 0; _index < sizeWeb(); ++_index) {
			com.sun.enterprise.tools.common.dd.application.Web element = getWeb(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property passByReference
		if (getPassByReference() != null) {
		}
		// Validating property uniqueId
		if (getUniqueId() != null) {
		}
		// Validating property securityRoleMapping
		for (int _index = 0; _index < sizeSecurityRoleMapping(); ++_index) {
			SecurityRoleMapping element = getSecurityRoleMapping(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property realm
		if (getRealm() != null) {
		}
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			init(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Web["+this.sizeWeb()+"]");	// NOI18N
		for(int i=0; i<this.sizeWeb(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWeb(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEB, i, str, indent);
		}

		str.append(indent);
		str.append("PassByReference");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPassByReference();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASS_BY_REFERENCE, 0, str, indent);

		str.append(indent);
		str.append("UniqueId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getUniqueId();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(UNIQUE_ID, 0, str, indent);

		str.append(indent);
		str.append("SecurityRoleMapping["+this.sizeSecurityRoleMapping()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityRoleMapping(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityRoleMapping(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_ROLE_MAPPING, i, str, indent);
		}

		str.append(indent);
		str.append("Realm");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getRealm();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REALM, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SunApplication\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for Sun ONE Application Server specific J2EE Application
  deployment descriptor. This is a companion DTD to application_1.4.xsd

  $Revision: 1.3 $
-->

<!--
This is the root element of the runtime descriptor document.
-->
<!ELEMENT sun-application (web*, pass-by-reference?, unique-id?, security-role-mapping*, realm?) >

<!ELEMENT web (web-uri, context-root)>
<!ELEMENT web-uri (#PCDATA)>
<!ELEMENT context-root (#PCDATA)>

<!-- Pass by Reference semantics:  EJB spec requires pass by value,
     which will be the default mode of operation. This can be set 
     to true for non-compliant and possibly higher performance. 
     For a stand-alone, this can be set at this level. By setting 
     a similarly named element at sun-application, it can apply to 
     all the enclosed ejb modules. Allowed values are true and 
     false. Default will be false.
 -->
<!ELEMENT pass-by-reference (#PCDATA)>

<!-- Automatically generated and updated at deployment/redeployment 
     Needs to be unqiue in the system.
  -->
<!ELEMENT unique-id (#PCDATA)>

<!ELEMENT security-role-mapping (role-name, (principal-name | group-name)+)>

<!ELEMENT role-name (#PCDATA)>
<!ELEMENT principal-name (#PCDATA)>
<!ELEMENT group-name (#PCDATA)>

<!-- 
  realm: Allows specifying an optional authentication realm name which will
    be used to process all authentication requests associated with this
    application. If this element is not specified (or if it is given but
    does not match the name of a configured realm) then the default realm
    set in the server instances security-service element will be used
    instead.
-->
<!ELEMENT realm (#PCDATA)>

<!-- This information is used, only by Studio-plugin;it is NOT the part of the real DTD.
-->
<!ELEMENT plugin-data ( auto-generate-sql?, client-jar-path?, client-args? )>
<!ELEMENT auto-generate-sql (#PCDATA)>
<!ELEMENT client-jar-path (#PCDATA)>
<!ELEMENT client-args (#PCDATA)>

*/
