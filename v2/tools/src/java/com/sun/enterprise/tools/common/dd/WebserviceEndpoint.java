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
 *	This generated bean class WebserviceEndpoint matches the schema element
 *	webservice-endpoint. Hand Modified on August 8, 2006 to support
 *	MessageSecurityBinding - to avoid regenerating all the schema2beans.
 *	The generated files - Message.java, MessageSecurityBinding.java, 
 *  JavaMethod.java and	MethodParams.java are added to this directory.
 *  These files are required by MessageSecurityBinding.java
 *
 *	Generated on Wed Aug 13 12:12:27 PDT 2003
 */

package com.sun.enterprise.tools.common.dd;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class WebserviceEndpoint extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String PORT_COMPONENT_NAME = "PortComponentName";	// NOI18N
	static public final String ENDPOINT_ADDRESS_URI = "EndpointAddressUri";	// NOI18N
	static public final String LOGIN_CONFIG = "LoginConfig";	// NOI18N
        static public final String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding"; // NOI18N
	static public final String TRANSPORT_GUARANTEE = "TransportGuarantee";	// NOI18N
	static public final String SERVICE_QNAME = "ServiceQname";	// NOI18N
	static public final String TIE_CLASS = "TieClass";	// NOI18N
	static public final String SERVLET_IMPL_CLASS = "ServletImplClass";	// NOI18N

	public WebserviceEndpoint() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public WebserviceEndpoint(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("port-component-name", 	// NOI18N
			PORT_COMPONENT_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("endpoint-address-uri", 	// NOI18N
			ENDPOINT_ADDRESS_URI, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("login-config", 	// NOI18N
			LOGIN_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LoginConfig.class);
                this.createProperty("message-security-binding",         // NOI18N
                        MESSAGE_SECURITY_BINDING, Common.SEQUENCE_OR |
                        Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY,
                        MessageSecurityBinding.class);
		this.createProperty("transport-guarantee", 	// NOI18N
			TRANSPORT_GUARANTEE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("service-qname", 	// NOI18N
			SERVICE_QNAME, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ServiceQname.class);
		this.createProperty("tie-class", 	// NOI18N
			TIE_CLASS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("servlet-impl-class", 	// NOI18N
			SERVLET_IMPL_CLASS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
					
	}

	// This attribute is mandatory
	public void setPortComponentName(String value) {
		this.setValue(PORT_COMPONENT_NAME, value);
	}

	//
	public String getPortComponentName() {
		return (String)this.getValue(PORT_COMPONENT_NAME);
	}

	// This attribute is optional
	public void setEndpointAddressUri(String value) {
		this.setValue(ENDPOINT_ADDRESS_URI, value);
	}

	//
	public String getEndpointAddressUri() {
		return (String)this.getValue(ENDPOINT_ADDRESS_URI);
	}

	// This attribute is optional
	public void setLoginConfig(LoginConfig value) {
		this.setValue(LOGIN_CONFIG, value);
                if (value != null) {
                        // It's a mutually exclusive property.
                        setMessageSecurityBinding(null);
                }
	}

        // This attribute is mandatory
        public void setMessageSecurityBinding(MessageSecurityBinding value) {
                this.setValue(MESSAGE_SECURITY_BINDING, value);
                if (value != null) {
                        // It's a mutually exclusive property.
                        setLoginConfig(null);
                }
        }

	//
	public LoginConfig getLoginConfig() {
		return (LoginConfig)this.getValue(LOGIN_CONFIG);
	}

        //
        public MessageSecurityBinding getMessageSecurityBinding() {
                return (MessageSecurityBinding)this.getValue(MESSAGE_SECURITY_BINDING);
        }

	// This attribute is optional
	public void setTransportGuarantee(String value) {
		this.setValue(TRANSPORT_GUARANTEE, value);
	}

	//
	public String getTransportGuarantee() {
		return (String)this.getValue(TRANSPORT_GUARANTEE);
	}

	// This attribute is optional
	public void setServiceQname(ServiceQname value) {
		this.setValue(SERVICE_QNAME, value);
	}

	//
	public ServiceQname getServiceQname() {
		return (ServiceQname)this.getValue(SERVICE_QNAME);
	}

	// This attribute is optional
	public void setTieClass(String value) {
		this.setValue(TIE_CLASS, value);
	}

	//
	public String getTieClass() {
		return (String)this.getValue(TIE_CLASS);
	}

	// This attribute is optional
	public void setServletImplClass(String value) {
		this.setValue(SERVLET_IMPL_CLASS, value);
	}

	//
	public String getServletImplClass() {
		return (String)this.getValue(SERVLET_IMPL_CLASS);
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
		// Validating property portComponentName
		if (getPortComponentName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getPortComponentName() == null", "portComponentName", this);	// NOI18N
		}
		// Validating property endpointAddressUri
		if (getEndpointAddressUri() != null) {
		}
		// Validating property loginConfig
		if (getLoginConfig() != null) {
			getLoginConfig().validate();
		}
		// Validating property transportGuarantee
		if (getTransportGuarantee() != null) {
		}
		// Validating property serviceQname
		if (getServiceQname() != null) {
			getServiceQname().validate();
		}
		// Validating property tieClass
		if (getTieClass() != null) {
		}
		// Validating property servletImplClass
		if (getServletImplClass() != null) {
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("PortComponentName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPortComponentName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PORT_COMPONENT_NAME, 0, str, indent);

		str.append(indent);
		str.append("EndpointAddressUri");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEndpointAddressUri();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(ENDPOINT_ADDRESS_URI, 0, str, indent);

		str.append(indent);
		str.append("LoginConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLoginConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOGIN_CONFIG, 0, str, indent);

		str.append(indent);
                str.append("MessageSecurityBinding");   // NOI18N
                n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageSecurityBinding();
                if (n != null)
                        n.dump(str, indent + "\t");     // NOI18N
                else
                        str.append(indent+"\tnull");    // NOI18N
                this.dumpAttributes(MESSAGE_SECURITY_BINDING, 0, str, indent);

                str.append(indent);
		str.append("TransportGuarantee");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getTransportGuarantee();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TRANSPORT_GUARANTEE, 0, str, indent);

		str.append(indent);
		str.append("ServiceQname");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getServiceQname();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SERVICE_QNAME, 0, str, indent);

		str.append(indent);
		str.append("TieClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getTieClass();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(TIE_CLASS, 0, str, indent);

		str.append(indent);
		str.append("ServletImplClass");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getServletImplClass();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SERVLET_IMPL_CLASS, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("WebserviceEndpoint\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
This is the root element which binds an ejb reference to a jndi name.
-->
<!ELEMENT ejb-ref (ejb-ref-name, jndi-name)>

<!--
This node holds all the runtime bindings of a resource env reference.
-->
<!ELEMENT resource-env-ref ( resource-env-ref-name, jndi-name )>

<!--
This node holds all the runtime bindings of a resource reference.
-->
<!ELEMENT resource-ref  (res-ref-name, jndi-name, default-resource-principal?)>

<!ELEMENT default-resource-principal (name, password)>

<!ELEMENT security-role-mapping (role-name, (principal-name | group-name)+)> 

<!--
Information about a web service endpoint.  
-->
<!ELEMENT webservice-endpoint ( port-component-name, endpoint-address-uri?, login-config?, transport-guarantee?, service-qname?, tie-class?, servlet-impl-class? )>

<!-- 
Optional authentication configuration for an EJB web service endpoint.
Not needed for servet web service endpoints.  Their security configuration
is contained in the standard web application descriptor.
-->
<!ELEMENT login-config ( auth-method )>

<!-- 
The service-qname element declares the specific WSDL service
element that is being refered to.  It is not set by the deployer.
It is derived during deployment.
-->
<!ELEMENT service-qname (namespaceURI, localpart)>

<!-- 
Runtime information about a web service.  
wsdl-publish-location is optionally used to specify 
where the final wsdl and any dependent files should be stored.  This location
resides on the file system from which deployment is initiated.
-->
<!ELEMENT webservice-description ( webservice-description-name, wsdl-publish-location? )>

<!--
Runtime settings for a web service reference.  In the simplest case,
there is no runtime information required for a service ref.  Runtime info
is only needed in the following cases :
 * to define the port that should be used to resolve a container-managed port
 * to define default Stub/Call property settings for Stub objects
 * to define the URL of a final WSDL document to be used instead of
the one packaged with a service-ref
-->
<!ELEMENT service-ref ( service-ref-name, port-info*, call-property*, wsdl-override?, service-impl-class?, service-qname? )>

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
This info is used only by studio plug-in;it is NOT the part of the orgianl DTD.
Used by plugin to store parameters needed to execute an Application client.
-->
<!ELEMENT plugin-data ( auto-generate-sql?, client-jar-path?, client-args? )>
<!ELEMENT auto-generate-sql (#PCDATA)>
<!ELEMENT client-jar-path (#PCDATA)>
<!ELEMENT client-args (#PCDATA)>

<!--
The ejb ref name locates the name of the ejb reference in the application.
-->
<!ELEMENT ejb-ref-name (#PCDATA)>

<!--
The text in this node is a jndi name.
-->
<!ELEMENT  jndi-name (#PCDATA)>

<!--
The name of a resource env reference.
-->
<!ELEMENT resource-env-ref-name (#PCDATA)>

<!--
The name of a resource reference.
-->
<!ELEMENT res-ref-name (#PCDATA)>

<!--
This text nodes holds a name string.
-->
<!ELEMENT name (#PCDATA)>

<!--
This element holds password text.
-->
<!ELEMENT password (#PCDATA)>

<!ELEMENT role-name (#PCDATA)> 

<!ELEMENT principal-name (#PCDATA)> 

<!ELEMENT group-name (#PCDATA)> 

<!--
Unique name of a port component within a module
-->
<!ELEMENT port-component-name ( #PCDATA )>

<!--
Relative path combined with web server root to form fully qualified
endpoint address for a web service endpoint.  For servlet endpoints, this
value is relative to the servlet's web application context root.  In
all cases, this value must be a fixed pattern(i.e. no "*" allowed).
If the web service endpoint is a servlet that only implements a single
endpoint has only one url-pattern, it is not necessary to set 
this value since the container can derive it from web.xml.
-->
<!ELEMENT endpoint-address-uri ( #PCDATA )>

<!--
The namespaceURI element indicates a URI.
-->
<!ELEMENT namespaceURI (#PCDATA)>

<!--
The localpart element indicates the local part of a QNAME.
-->
<!ELEMENT localpart (#PCDATA)>

<!--
auth-method element describes the authentication method. The only supported value
is USERNAME_PASSWORD
-->  
<!ELEMENT auth-method ( #PCDATA )> 

<!--
Specifies that the communication between client and server should 
be NONE, INTEGRAL, or CONFIDENTIAL. NONE means that the application 
does not require any transport guarantees. A value of INTEGRAL means 
that the application requires that the data sent between the client 
and server be sent in such a way that it can't be changed in transit. 
CONFIDENTIAL means that the application requires that the data be 
transmitted in a fashion that prevents other entities from observing 
the contents of the transmission. In most cases, the presence of the 
INTEGRAL or CONFIDENTIAL flag will indicate that the use of SSL is 
required.
-->
<!ELEMENT transport-guarantee ( #PCDATA )>

<!--
The name of tie implementation class for a port-component.  This is
not specified by the deployer.  It is derived during deployment.
-->
<!ELEMENT tie-class (#PCDATA)>

<!--
Name of application-written servlet impl class contained in deployed war.
This is not set by the deployer.  It is derived by the container
during deployment.
-->
<!ELEMENT servlet-impl-class (#PCDATA)>

<!--
Unique name of a webservice within a module
-->
<!ELEMENT webservice-description-name ( #PCDATA )>

<!--
file: URL of a directory to which a web-service-description's wsdl should be
published during deployment.  Any required files will be published to this
directory, preserving their location relative to the module-specific
wsdl directory(META-INF/wsdl or WEB-INF/wsdl).

Example :

  For an ejb.jar whose webservices.xml wsdl-file element contains
    META-INF/wsdl/a/Foo.wsdl 

  <wsdl-publish-location>file:/home/user1/publish
  </wsdl-publish-location>

  The final wsdl will be stored in /home/user1/publish/a/Foo.wsdl
-->
<!ELEMENT wsdl-publish-location ( #PCDATA )>

<!--
Coded name (relative to java:comp/env) for a service-reference
-->
<!ELEMENT service-ref-name ( #PCDATA )>

<!--
Fully qualified name of service endpoint interface
-->
<!ELEMENT service-endpoint-interface ( #PCDATA )>

<!--
This text nodes holds a value string.
-->
<!ELEMENT value (#PCDATA)>

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
This node holds information about a logical message destination
-->
<!ELEMENT message-destination (message-destination-name, jndi-name)>

<!--
This node holds the name of a logical message destination
-->
<!ELEMENT message-destination-name (#PCDATA)>

*/
