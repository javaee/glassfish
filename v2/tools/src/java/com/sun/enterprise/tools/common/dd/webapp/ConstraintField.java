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
 *	This generated bean class ConstraintField matches the schema element constraint-field
 *
 *	Generated on Tue Sep 02 18:08:42 PDT 2003
 */

package com.sun.enterprise.tools.common.dd.webapp;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ConstraintField extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String NAME = "Name";	// NOI18N
	static public final String SCOPE = "Scope";	// NOI18N
	static public final String CACHEONMATCH = "CacheOnMatch";	// NOI18N
	static public final String CACHEONMATCHFAILURE = "CacheOnMatchFailure";	// NOI18N
	static public final String CONSTRAINT_FIELD_VALUE = "ConstraintFieldValue";	// NOI18N
	static public final String CONSTRAINTFIELDVALUEMATCHEXPR = "ConstraintFieldValueMatchExpr";	// NOI18N
	static public final String CONSTRAINTFIELDVALUECACHEONMATCH = "ConstraintFieldValueCacheOnMatch";	// NOI18N
	static public final String CONSTRAINTFIELDVALUECACHEONMATCHFAILURE = "ConstraintFieldValueCacheOnMatchFailure";	// NOI18N

	public ConstraintField() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ConstraintField(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("constraint-field-value", 	// NOI18N
			CONSTRAINT_FIELD_VALUE, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute(CONSTRAINT_FIELD_VALUE, "match-expr", "MatchExpr", 
						AttrProp.CDATA,
						null, "equals");
		this.createAttribute(CONSTRAINT_FIELD_VALUE, "cache-on-match", "CacheOnMatch", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CONSTRAINT_FIELD_VALUE, "cache-on-match-failure", "CacheOnMatchFailure", 
						AttrProp.CDATA,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
		
	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		setAttributeValue(NAME, value);
	}

	//
	public java.lang.String getName() {
		return getAttributeValue(NAME);
	}

	// This attribute is mandatory
	public void setScope(java.lang.String value) {
		setAttributeValue(SCOPE, value);
	}

	//
	public java.lang.String getScope() {
		return getAttributeValue(SCOPE);
	}

	// This attribute is mandatory
	public void setCacheOnMatch(java.lang.String value) {
		setAttributeValue(CACHEONMATCH, value);
	}

	//
	public java.lang.String getCacheOnMatch() {
		return getAttributeValue(CACHEONMATCH);
	}

	// This attribute is mandatory
	public void setCacheOnMatchFailure(java.lang.String value) {
		setAttributeValue(CACHEONMATCHFAILURE, value);
	}

	//
	public java.lang.String getCacheOnMatchFailure() {
		return getAttributeValue(CACHEONMATCHFAILURE);
	}

	// This attribute is an array, possibly empty
	public void setConstraintFieldValue(int index, String value) {
		this.setValue(CONSTRAINT_FIELD_VALUE, index, value);
	}

	//
	public String getConstraintFieldValue(int index) {
		return (String)this.getValue(CONSTRAINT_FIELD_VALUE, index);
	}

	// This attribute is an array, possibly empty
	public void setConstraintFieldValue(String[] value) {
		this.setValue(CONSTRAINT_FIELD_VALUE, value);
	}

	//
	public String[] getConstraintFieldValue() {
		return (String[])this.getValues(CONSTRAINT_FIELD_VALUE);
	}

	// Return the number of properties
	public int sizeConstraintFieldValue() {
		return this.size(CONSTRAINT_FIELD_VALUE);
	}

	// Add a new element returning its index in the list
	public int addConstraintFieldValue(String value) {
		return this.addValue(CONSTRAINT_FIELD_VALUE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeConstraintFieldValue(String value) {
		return this.removeValue(CONSTRAINT_FIELD_VALUE, value);
	}

	// This attribute is an array, possibly empty
	public void setConstraintFieldValueMatchExpr(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			addValue(CONSTRAINT_FIELD_VALUE, "");
		}
		setAttributeValue(CONSTRAINT_FIELD_VALUE, index, "MatchExpr", value);
	}

	//
	public java.lang.String getConstraintFieldValueMatchExpr(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			return null;
		} else {
			return getAttributeValue(CONSTRAINT_FIELD_VALUE, index, "MatchExpr");
		}
	}

	// This attribute is an array, possibly empty
	public void setConstraintFieldValueCacheOnMatch(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			addValue(CONSTRAINT_FIELD_VALUE, "");
		}
		setAttributeValue(CONSTRAINT_FIELD_VALUE, index, "CacheOnMatch", value);
	}

	//
	public java.lang.String getConstraintFieldValueCacheOnMatch(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			return null;
		} else {
			return getAttributeValue(CONSTRAINT_FIELD_VALUE, index, "CacheOnMatch");
		}
	}

	// This attribute is an array, possibly empty
	public void setConstraintFieldValueCacheOnMatchFailure(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			addValue(CONSTRAINT_FIELD_VALUE, "");
		}
		setAttributeValue(CONSTRAINT_FIELD_VALUE, index, "CacheOnMatchFailure", value);
	}

	//
	public java.lang.String getConstraintFieldValueCacheOnMatchFailure(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(CONSTRAINT_FIELD_VALUE) == 0) {
			return null;
		} else {
			return getAttributeValue(CONSTRAINT_FIELD_VALUE, index, "CacheOnMatchFailure");
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
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getName() == null", "name", this);	// NOI18N
		}
		// Validating property scope
		if (getScope() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getScope() == null", "scope", this);	// NOI18N
		}
		// Validating property cacheOnMatch
		if (getCacheOnMatch() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getCacheOnMatch() == null", "cacheOnMatch", this);	// NOI18N
		}
		// Validating property cacheOnMatchFailure
		if (getCacheOnMatchFailure() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getCacheOnMatchFailure() == null", "cacheOnMatchFailure", this);	// NOI18N
		}
		// Validating property constraintFieldValue
		for (int _index = 0; _index < sizeConstraintFieldValue(); 
			++_index) {
			String element = getConstraintFieldValue(_index);
			if (element != null) {
			}
		}
		// Validating property constraintFieldValueMatchExpr
		// Validating property constraintFieldValueCacheOnMatch
		// Validating property constraintFieldValueCacheOnMatchFailure
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("ConstraintFieldValue["+this.sizeConstraintFieldValue()+"]");	// NOI18N
		for(int i=0; i<this.sizeConstraintFieldValue(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			s = this.getConstraintFieldValue(i);
			str.append((s==null?"null":s.trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(CONSTRAINT_FIELD_VALUE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ConstraintField\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!--
  XML DTD for Sun ONE Application Server specific Web Application 
  deployment descriptor. This is a companion DTD for web-app_2_4.xsd

  $Revision: 1.3 $
-->

<!-- root element for vendor specific web application (module) configuration -->
<!ELEMENT sun-web-app (context-root?, security-role-mapping*, servlet*, session-config?,
                       ejb-ref*, resource-ref*, resource-env-ref*,  service-ref*,
                       cache?, class-loader?,
                       jsp-config?, locale-charset-info?, property*,
		           message-destination*, webservice-description*)>

<!-- 
 Context Root for the web application when the war file is a standalone module.
 When the war module is part of the J2EE Application, use the application.xml
-->
<!ELEMENT context-root (#PCDATA)>

<!ELEMENT security-role-mapping (role-name, (principal-name | group-name)+)>
<!ELEMENT role-name (#PCDATA)>

<!ELEMENT principal-name (#PCDATA)>
<!ELEMENT group-name (#PCDATA)>

<!ELEMENT servlet (servlet-name, principal-name?, webservice-endpoint*)>

<!ELEMENT session-config (session-manager?, session-properties?, cookie-properties?)>

<!ELEMENT session-manager (manager-properties?, store-properties?)>
<!ATTLIST session-manager persistence-type CDATA "memory">

<!ELEMENT manager-properties (property*)>
<!ELEMENT store-properties (property*)>
<!ELEMENT session-properties (property*)>
<!ELEMENT cookie-properties (property*)>

<!ELEMENT jndi-name (#PCDATA)>

<!ELEMENT resource-env-ref (resource-env-ref-name, jndi-name)>
<!ELEMENT resource-env-ref-name (#PCDATA)>
           
<!ELEMENT resource-ref (res-ref-name, jndi-name, default-resource-principal?)>
<!ELEMENT res-ref-name (#PCDATA)>

<!ELEMENT default-resource-principal ( name,  password)>

<!--
This node holds information about a logical message destination
-->
<!ELEMENT message-destination (message-destination-name, jndi-name)>

<!--
This node holds the name of a logical message destination
-->
<!ELEMENT message-destination-name (#PCDATA)>

<!--
This text nodes holds a name string.
-->
<!ELEMENT name (#PCDATA)>

<!--
This element holds password text.
-->
<!ELEMENT password (#PCDATA)>


<!ELEMENT ejb-ref (ejb-ref-name, jndi-name)>
<!ELEMENT ejb-ref-name (#PCDATA)>

<!-- cache element configures the cache for web application. iAS 7.0 web container    
     supports one such cache object per application: i.e. <cache> is a sub element    
     of <ias-web-app>. A cache can have zero or more cache-mapping elements and
     zero or more customizable cache-helper classes.
                                                                                   
        max-entries        Maximum number of entries this cache may hold. [4096]
        timeout-in-seconds Default timeout for the cache entries in seconds. [30]
        enabled            Is this cache enabled? [false]                                 
-->                                                                                   
<!ELEMENT cache (cache-helper*, default-helper?, property*, cache-mapping*)>
<!ATTLIST cache  max-entries         CDATA     "4096"
                 timeout-in-seconds  CDATA     "30"
                 enabled             CDATA     "false">

<!-- cache-helper specifies customizable class which implements CacheHelper interface. 

     name                     Unique name for the helper class; this is referenced in
                              the cache-mapping elements (see below).
                              "default" is reserved for the built-in default helper.
     class-name               Fully qualified class name of the cache-helper; this class
                              must extend the com.sun.appserv.web.CacheHelper class.
-->
<!ELEMENT cache-helper (property*)>
<!ATTLIST cache-helper name CDATA #REQUIRED
                       class-name CDATA #REQUIRED>

<!-- 
Default, built-in cache-helper properties
-->
<!ELEMENT default-helper (property*)>

<!-- 
cache-mapping element defines what to be cached, the key to be used, any other   
constraints to be applied and a customizable cache-helper to programmatically
hook this information.
-->
<!ELEMENT cache-mapping ((servlet-name | url-pattern), 
                        (cache-helper-ref |
                        (timeout?, refresh-field?, http-method*, key-field*, constraint-field*)))>

<!-- 
servlet-name element defines a named servlet to which this caching is enabled.
the specified name must be present in the web application deployment descriptor
(web.xml)
-->
<!ELEMENT servlet-name (#PCDATA)>

<!-- 
url-pattern element specifies the url pattern to which caching is to be enabled.
See Servlet 2.3 specification section SRV. 11.2 for the applicable patterns.
-->
<!ELEMENT url-pattern  (#PCDATA)>

<!-- 
cache-helper-ref s a reference to the cache-helper used by this cache-mapping 
-->
<!ELEMENT cache-helper-ref (#PCDATA)>

<!-- 
timeout element defines the cache timeout in seconds applicable for this mapping.
default is to use cache object's timeout. The timeout value is specified statically
ere (e.g. <timeout> 60 </timeout> or dynamically via fields in the relevant scope.

   name             Name of the field where this timeout could be found
   scope            Scope of the field. default scope is request attribute.
-->
<!ELEMENT timeout (#PCDATA)>
<!ATTLIST timeout  name  CDATA   #REQUIRED
                   scope CDATA 'request.attribute'>

<!-- 
http-method specifies HTTP method eligible for caching default is GET. 
-->
<!ELEMENT http-method (#PCDATA)>

<!-- 
specifies the request parameter name that triggers refresh. the cached entry 
is refreshed when there such a request parameter is set to "true"
example:
<cache-mapping> 
    <url-pattern> /quote </url-pattern> 
    <refresh-field name="refresh" scope="request.parameter"/> 
</cache-mapping> 
-->
<!ELEMENT refresh-field EMPTY>
<!ATTLIST refresh-field name  CDATA       #REQUIRED
                        scope CDATA       'request.parameter'>
<!-- 
key-field specifies a component of the key; container looks for the named 
field in the given scope to access the cached entry. Default is to use
the Servlet Path (the path section that corresponds to the servlet mapping 
which activated this request). See Servlet 2.3 specification section SRV 4.4 
on Servlet Path.

  name             Name of the field to look for in the given scope
  scope            Scope of the field. default scope is request parameter.
-->
<!ELEMENT key-field EMPTY>
<!ATTLIST key-field name  CDATA       #REQUIRED
                    scope CDATA       'request.parameter'>

<!-- 
constraint-field specifies a field whose value is used as a cacheability constraint.
  
  name                     Name of the field to look for in the given scope
  scope                    Scope of the field. Default scope is request parameter.
  cache-on-match           Should this constraint check pass, is the response cacheable?
                           Default is true (i.e. cache the response on success match). 
                           Useful to turn off caching when there is an attribute in the 
                           scope (e.g. don't cache when there is an attribute called UID 
                           in the session.attribute scope).
  cache-on-match-failure   Should the constraint check fail, is response not cacheable?
                           Default is false (i.e. a failure in enforcing the constraint
                           would negate caching). Useful to turn on caching when the 
                           an an attribute is not present (e.g. turn on caching 
                           when there is no session or session attribute called UID).

  Example 1: don't cache when there is a session attribute
  <constraint-field name="UID" scope="session.attribute" cache-on-match="false">

  Example 2: do cache only when there is no session attribute
  <constraint-field name="UID" scope="session.attribute" 
                    cache-on-match-failure="false">
-->
<!ELEMENT constraint-field (constraint-field-value*)>
<!ATTLIST constraint-field  name                    CDATA      #REQUIRED
                            scope                   CDATA      'request.parameter'
                            cache-on-match          CDATA      'true'
                            cache-on-match-failure  CDATA      'false'>

<!-- 
value element specifies the applicable value and a matching expression for a constraint-field
  match-expr            Expression used to match the value. Default is 'equals'.

  Example 1: cache when the category matches with any value other than a specific value
  <constraint-field name="category" scope="request.parameter>
    <value match-expr="equals" cache-on-match-failure="true">
         bogus
    </value>
  </constraint-field>
-->             
<!ELEMENT constraint-field-value (#PCDATA)>
<!ATTLIST constraint-field-value 	match-expr              CDATA     'equals'
                			cache-on-match          CDATA     'true'
                			cache-on-match-failure  CDATA     'false'>

<!ELEMENT class-loader EMPTY>
<!ATTLIST class-loader extra-class-path CDATA  #IMPLIED
                       delegate CDATA 'true'>

<!ELEMENT jsp-config (property*)>

<!ELEMENT locale-charset-info (locale-charset-map+, parameter-encoding?)>
<!ATTLIST locale-charset-info default-locale CDATA #REQUIRED>

<!ELEMENT locale-charset-map (description?)>
<!ATTLIST locale-charset-map locale  CDATA  #REQUIRED
                             agent   CDATA  #IMPLIED
                             charset CDATA  #REQUIRED>

<!ELEMENT parameter-encoding EMPTY>
<!ATTLIST parameter-encoding form-hint-field CDATA #IMPLIED
			     default-charset CDATA #IMPLIED>

<!-- 
Syntax for supplying properties as name value pairs 
-->
<!ELEMENT property (description?)>
<!ATTLIST property name  CDATA  #REQUIRED
                   value CDATA  #REQUIRED>

<!ELEMENT description (#PCDATA)>

<!--
This text nodes holds a value string.
-->
<!ELEMENT value (#PCDATA)>


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
<!ELEMENT service-ref ( service-ref-name, port-info*, call-property*, wsdl-override?, service-impl-class?, service-qname? )>

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
Runtime information about a web service.  

wsdl-publish-location is optionally used to specify 
where the final wsdl and any dependent files should be stored.  This location
resides on the file system from which deployment is initiated.

-->
<!ELEMENT webservice-description ( webservice-description-name, wsdl-publish-location? )>

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
Information about a web service endpoint.  
-->
<!ELEMENT webservice-endpoint ( port-component-name, endpoint-address-uri?, login-config?, transport-guarantee?, service-qname?, tie-class?, servlet-impl-class? )>

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
The name of tie implementation class for a port-component.  This is
not specified by the deployer.  It is derived during deployment.
-->
<!ELEMENT tie-class (#PCDATA)>

<!-- 
Optional authentication configuration for an EJB web service endpoint.
Not needed for servet web service endpoints.  Their security configuration
is contained in the standard web application descriptor.
-->
<!ELEMENT login-config ( auth-method )>

<!--
The auth-method element is used to configure the authentication
mechanism for the web application. As a prerequisite to gaining access
to any web resources which are protected by an authorization
constraint, a user must have authenticated using the configured
mechanism.
-->

<!ELEMENT auth-method (#PCDATA)>

<!--
Name of application-written servlet impl class contained in deployed war.
This is not set by the deployer.  It is derived by the container
during deployment.
-->
<!ELEMENT servlet-impl-class (#PCDATA)>

<!--
The transport-guarantee element specifies that the communication
between client and server should be NONE, INTEGRAL, or
CONFIDENTIAL. NONE means that the application does not require any
transport guarantees. A value of INTEGRAL means that the application
requires that the data sent between the client and server be sent in
such a way that it can't be changed in transit. CONFIDENTIAL means
that the application requires that the data be transmitted in a
fashion that prevents other entities from observing the contents of
the transmission. In most cases, the presence of the INTEGRAL or
CONFIDENTIAL flag will indicate that the use of SSL is required.
-->

<!ELEMENT transport-guarantee (#PCDATA)>

<!--
Runtime settings for a web service reference.  In the simplest case,
there is no runtime information required for a service ref.  Runtime info
is only needed in the following cases :
 * to define the port that should be used to resolve a container-managed port
 * to define default Stub/Call property settings for Stub objects
 * to define the URL of a final WSDL document to be used instead of
the one packaged with a service-ref
-->

<!--
The localpart element indicates the local part of a QNAME.
-->
<!ELEMENT localpart (#PCDATA)>

<!--
The namespaceURI element indicates a URI.
-->
<!ELEMENT namespaceURI (#PCDATA)>

*/
