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
 *	This generated bean class SunApplicationClient matches the schema element sun-application-client
 *
 *	Generated on Thu Jul 31 15:53:12 PDT 2003
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	sun-application-client : SunApplicationClient
 *		ejb-ref : EjbRef[0,n]
 *			ejb-ref-name : String
 *			jndi-name : String
 *		resource-ref : ResourceRef[0,n]
 *			res-ref-name : String
 *			jndi-name : String
 *			default-resource-principal : DefaultResourcePrincipal?
 *				name : String
 *				password : String
 *		resource-env-ref : ResourceEnvRef[0,n]
 *			resource-env-ref-name : String
 *			jndi-name : String
 *		service-ref : ServiceRef[0,n]
 *			service-ref-name : String
 *			port-info : PortInfo[0,n]
 *				service-endpoint-interface : String?
 *				wsdl-port : WsdlPort?
 *					namespaceURI : String
 *					localpart : String
 *				stub-property : StubProperty[0,n]
 *					name : String
 *					value : String
 *				call-property : CallProperty[0,n]
 *					name : String
 *					value : String
 *			call-property : CallProperty[0,n]
 *				name : String
 *				value : String
 *			wsdl-override : String?
 *			service-impl-class : String?
 *			service-qname : ServiceQname?
 *				namespaceURI : String
 *				localpart : String
 *		message-destination : MessageDestination[0,n]
 *			message-destination-name : String
 *			jndi-name : String
 *
 */

package com.sun.enterprise.tools.common.dd.appclient;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import com.sun.enterprise.tools.common.dd.EjbRef;
import com.sun.enterprise.tools.common.dd.ResourceEnvRef;
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.ServiceRef;
import com.sun.enterprise.tools.common.dd.MessageDestination;
import com.sun.enterprise.tools.common.dd.PluginData;

// BEGIN_NOI18N

public class SunApplicationClient extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String EJB_REF = "EjbRef";	// NOI18N
	static public final String RESOURCE_REF = "ResourceRef";	// NOI18N
	static public final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
	static public final String SERVICE_REF = "ServiceRef";	// NOI18N
	static public final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N

	public SunApplicationClient() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public SunApplicationClient(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("sun-application-client");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "sun-application-client"));
		}
		Node n = GraphManager.getElementNode("sun-application-client", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "sun-application-client", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public SunApplicationClient(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("sun-application-client", "SunApplicationClient",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, SunApplicationClient.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("ejb-ref", 	// NOI18N
			EJB_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbRef.class);
		this.createProperty("resource-ref", 	// NOI18N
			RESOURCE_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceRef.class);
		this.createProperty("resource-env-ref", 	// NOI18N
			RESOURCE_ENV_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResourceEnvRef.class);
		this.createProperty("service-ref", 	// NOI18N
			SERVICE_REF, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceRef.class);
		this.createProperty("message-destination", 	// NOI18N
			MESSAGE_DESTINATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestination.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{

	}

	// This attribute is an array, possibly empty
	public void setEjbRef(int index, EjbRef value) {
		this.setValue(EJB_REF, index, value);
	}

	//
	public EjbRef getEjbRef(int index) {
		return (EjbRef)this.getValue(EJB_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setEjbRef(EjbRef[] value) {
		this.setValue(EJB_REF, value);
	}

	//
	public EjbRef[] getEjbRef() {
		return (EjbRef[])this.getValues(EJB_REF);
	}

	// Return the number of properties
	public int sizeEjbRef() {
		return this.size(EJB_REF);
	}

	// Add a new element returning its index in the list
	public int addEjbRef(EjbRef value) {
		return this.addValue(EJB_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeEjbRef(EjbRef value) {
		return this.removeValue(EJB_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setResourceRef(int index, ResourceRef value) {
		this.setValue(RESOURCE_REF, index, value);
	}

	//
	public ResourceRef getResourceRef(int index) {
		return (ResourceRef)this.getValue(RESOURCE_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setResourceRef(ResourceRef[] value) {
		this.setValue(RESOURCE_REF, value);
	}

	//
	public ResourceRef[] getResourceRef() {
		return (ResourceRef[])this.getValues(RESOURCE_REF);
	}

	// Return the number of properties
	public int sizeResourceRef() {
		return this.size(RESOURCE_REF);
	}

	// Add a new element returning its index in the list
	public int addResourceRef(ResourceRef value) {
		return this.addValue(RESOURCE_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceRef(ResourceRef value) {
		return this.removeValue(RESOURCE_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvRef(int index, ResourceEnvRef value) {
		this.setValue(RESOURCE_ENV_REF, index, value);
	}

	//
	public ResourceEnvRef getResourceEnvRef(int index) {
		return (ResourceEnvRef)this.getValue(RESOURCE_ENV_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setResourceEnvRef(ResourceEnvRef[] value) {
		this.setValue(RESOURCE_ENV_REF, value);
	}

	//
	public ResourceEnvRef[] getResourceEnvRef() {
		return (ResourceEnvRef[])this.getValues(RESOURCE_ENV_REF);
	}

	// Return the number of properties
	public int sizeResourceEnvRef() {
		return this.size(RESOURCE_ENV_REF);
	}

	// Add a new element returning its index in the list
	public int addResourceEnvRef(ResourceEnvRef value) {
		return this.addValue(RESOURCE_ENV_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeResourceEnvRef(ResourceEnvRef value) {
		return this.removeValue(RESOURCE_ENV_REF, value);
	}

	// This attribute is an array, possibly empty
	public void setServiceRef(int index, ServiceRef value) {
		this.setValue(SERVICE_REF, index, value);
	}

	//
	public ServiceRef getServiceRef(int index) {
		return (ServiceRef)this.getValue(SERVICE_REF, index);
	}

	// This attribute is an array, possibly empty
	public void setServiceRef(ServiceRef[] value) {
		this.setValue(SERVICE_REF, value);
	}

	//
	public ServiceRef[] getServiceRef() {
		return (ServiceRef[])this.getValues(SERVICE_REF);
	}

	// Return the number of properties
	public int sizeServiceRef() {
		return this.size(SERVICE_REF);
	}

	// Add a new element returning its index in the list
	public int addServiceRef(ServiceRef value) {
		return this.addValue(SERVICE_REF, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServiceRef(ServiceRef value) {
		return this.removeValue(SERVICE_REF, value);
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
	public static SunApplicationClient createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new SunApplicationClient(doc, Common.NO_DEFAULT_VALUES);
	}

	public static SunApplicationClient createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static SunApplicationClient createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static SunApplicationClient createGraph() {
		try {
			return new SunApplicationClient();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property ejbRef
		for (int _index = 0; _index < sizeEjbRef(); ++_index) {
			EjbRef element = getEjbRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceRef
		for (int _index = 0; _index < sizeResourceRef(); ++_index) {
			ResourceRef element = getResourceRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property resourceEnvRef
		for (int _index = 0; _index < sizeResourceEnvRef(); ++_index) {
			ResourceEnvRef element = getResourceEnvRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property serviceRef
		for (int _index = 0; _index < sizeServiceRef(); ++_index) {
			ServiceRef element = getServiceRef(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property messageDestination
		for (int _index = 0; _index < sizeMessageDestination(); ++_index) {
			MessageDestination element = getMessageDestination(_index);
			if (element != null) {
				element.validate();
			}
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
		str.append("EjbRef["+this.sizeEjbRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeEjbRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(EJB_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceRef["+this.sizeResourceRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ResourceEnvRef["+this.sizeResourceEnvRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeResourceEnvRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getResourceEnvRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(RESOURCE_ENV_REF, i, str, indent);
		}

		str.append(indent);
		str.append("ServiceRef["+this.sizeServiceRef()+"]");	// NOI18N
		for(int i=0; i<this.sizeServiceRef(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServiceRef(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVICE_REF, i, str, indent);
		}

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

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SunApplicationClient\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for Sun ONE Application Server specific J2EE Client Application
  deployment descriptor. This is a companion DTD to application-client_1_4.xsd

  $Revision: 1.3 $
-->

<!--
application-client is the root element describing all the runtime bindings 
of a single application client
-->
<!ELEMENT sun-application-client (ejb-ref*, resource-ref*, resource-env-ref*, service-ref*, 
	message-destination*)>

<!--
name of a resource reference.
-->
<!ELEMENT res-ref-name (#PCDATA)>

<!--
resource-env-ref holds all the runtime bindings of a resource env reference.
-->
<!ELEMENT resource-env-ref ( resource-env-ref-name, jndi-name )>

<!--
name of a resource env reference.
-->
<!ELEMENT resource-env-ref-name (#PCDATA)>

<!--
resource-ref holds the runtime bindings of a resource reference.
-->
<!ELEMENT resource-ref  ( res-ref-name, jndi-name,  default-resource-principal?)>

<!--
default-resource-principal specifies the default principal that the container 
will use to access a resource.
-->
<!ELEMENT default-resource-principal ( name,  password)>

<!--
name element holds the user name
-->
<!ELEMENT name (#PCDATA)>

<!--
password element holds a password string.
-->
<!ELEMENT password (#PCDATA)>

<!--
ejb-ref element which binds an ejb reference to a jndi name.
-->
<!ELEMENT ejb-ref (ejb-ref-name, jndi-name)>

<!--
ejb-ref-name locates the name of the ejb reference in the application.
-->
<!ELEMENT ejb-ref-name (#PCDATA)>

<!--
jndi name of the associated entity
-->
<!ELEMENT  jndi-name (#PCDATA)>

<!--
This node holds information about a logical message destination
-->
<!ELEMENT message-destination (message-destination-name, jndi-name)>

<!--
This node holds the name of a logical message destination
-->
<!ELEMENT message-destination-name (#PCDATA)>

<!--
Specifies the name of a durable subscription associated with a message-driven bean's 
destination.  Required for a Topic destination, if subscription-durability is set to 
Durable (in ejb-jar.xml)
-->

<!--
  			W E B   S E R V I C E S 
--> 	
<!--
Runtime settings for a web service reference.  In the simplest case,
there is no runtime information required for a service ref.  Runtime info
is only needed in the following cases :
 * to define the port that should be used to resolve a container-managed port
 * to define default Stub/Call property settings for Stub objects
 * to define the URL of a final WSDL document to be used instead of
the one packaged with a service-ref
-->
<!ELEMENT service-ref ( service-ref-name, port-info*, call-property*, 
		wsdl-override?, service-impl-class?, service-qname? )>

<!--
Coded name (relative to java:comp/env) for a service-reference
-->
<!ELEMENT service-ref-name ( #PCDATA )>

<!-- 
Information for a port within a service-reference.

Either service-endpoint-interface or wsdl-port or both
(service-endpoint-interface and wsdl-port) should be specified.  

If both are specified, wsdl-port represents the
port the container should choose for container-managed port selection.

The same wsdl-port value must not appear in
more than one port-info entry within the same service-ref.

If a particular service-endpoint-interface is using container-managed port
selection, it must not appear in more than one port-info entry
within the same service-ref.

-->
<!ELEMENT port-info ( service-endpoint-interface?, wsdl-port?, stub-property*, call-property* )>

<!--
Fully qualified name of service endpoint interface
-->
<!ELEMENT service-endpoint-interface ( #PCDATA )>
<!-- 
Port used in port-info.  
-->
<!ELEMENT wsdl-port ( namespaceURI, localpart )>

<!-- 
JAXRPC property values that should be set on a stub before it's returned to 
to the web service client.  The property names can be any properties supported
by the JAXRPC Stub implementation. See javadoc for javax.xml.rpc.Stub
-->
<!ELEMENT stub-property ( name, value )>

<!-- 
JAXRPC property values that should be set on a Call object before it's 
returned to the web service client.  The property names can be any 
properties supported by the JAXRPC Call implementation.  See javadoc
for javax.xml.rpc.Call
-->
<!ELEMENT call-property ( name, value )>

<!--
This is a valid URL pointing to a final WSDL document. It is optional.
If specified, the WSDL document at this URL will be used during
deployment instead of the WSDL document associated with the
service-ref in the standard deployment descriptor.

Examples :

  // available via HTTP
  <wsdl-override>http://localhost:8000/myservice/myport?WSDL</wsdl-override>

  // in a file
  <wsdl-override>file:/home/user1/myfinalwsdl.wsdl</wsdl-override>

-->
<!ELEMENT wsdl-override ( #PCDATA )>

<!--
Name of generated service implementation class. This is not set by the 
deployer. It is derived during deployment.
-->
<!ELEMENT service-impl-class ( #PCDATA )>

<!-- 
The service-qname element declares the specific WSDL service
element that is being refered to.  It is not set by the deployer.
It is derived during deployment.
-->
<!ELEMENT service-qname (namespaceURI, localpart)>

<!--
The localpart element indicates the local part of a QNAME.
-->
<!ELEMENT localpart (#PCDATA)>

<!--
The namespaceURI element indicates a URI.
-->
<!ELEMENT namespaceURI (#PCDATA)>

<!--
This text nodes holds a value string.
-->
<!ELEMENT value (#PCDATA)>

<!-- This information is used, only by Studio-plugin;it is NOT the part of the real DTD.
-->
<!ELEMENT plugin-data ( auto-generate-sql?, client-jar-path?, client-args? )>
<!ELEMENT auto-generate-sql (#PCDATA)>
<!ELEMENT client-jar-path (#PCDATA)>
<!ELEMENT client-args (#PCDATA)>

*/
