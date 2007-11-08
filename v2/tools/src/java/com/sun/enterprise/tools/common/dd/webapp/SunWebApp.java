/**
 *	This generated bean class SunWebApp matches the schema element sun-web-app
 *
 *	Generated on Mon May 24 12:22:25 PDT 2004
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	sun-web-app : SunWebApp
 *		[attr: error-url CDATA ]
 *		context-root : String?
 *		security-role-mapping : SecurityRoleMapping[0,n]
 *			role-name : String
 *			(
 *			  | principal-name : String
 *			  | group-name : String
 *			)[1,n]
 *		servlet : Servlet[0,n]
 *			servlet-name : String
 *			principal-name : String?
 *			webservice-endpoint : WebserviceEndpoint[0,n]
 *				port-component-name : String
 *				endpoint-address-uri : String?
 *				login-config : LoginConfig?
 *					auth-method : String
 *				transport-guarantee : String?
 *				service-qname : ServiceQname?
 *					namespaceURI : String
 *					localpart : String
 *				tie-class : String?
 *				servlet-impl-class : String?
 *		idempotent-url-pattern : Boolean[0,n]
 *			[attr: url-pattern CDATA #REQUIRED ]
 *			[attr: num-of-retries CDATA -1]
 *			EMPTY : String
 *		session-config : SessionConfig?
 *			session-manager : SessionManager?
 *				[attr: persistence-type CDATA memory]
 *				manager-properties : ManagerProperties?
 *					property : WebProperty[0,n]
 *						[attr: name CDATA #REQUIRED ]
 *						[attr: value CDATA #REQUIRED ]
 *						description : String?
 *				store-properties : StoreProperties?
 *					property : WebProperty[0,n]
 *						[attr: name CDATA #REQUIRED ]
 *						[attr: value CDATA #REQUIRED ]
 *						description : String?
 *			session-properties : SessionProperties?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			cookie-properties : CookieProperties?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
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
 *		cache : Cache?
 *			[attr: max-entries CDATA 4096]
 *			[attr: timeout-in-seconds CDATA 30]
 *			[attr: enabled CDATA true]
 *			cache-helper : CacheHelper[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: class-name CDATA #REQUIRED ]
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			default-helper : DefaultHelper?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			property : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description : String?
 *			cache-mapping : CacheMapping[0,n]
 *				| servlet-name : String
 *				| url-pattern : String
 *				| cache-helper-ref : String
 *				| dispatcher : String[0,n]
 *				| timeout : String?
 *				| 	[attr: name CDATA #IMPLIED ]
 *				| 	[attr: scope CDATA request.attribute]
 *				| refresh-field : Boolean?
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	EMPTY : String
 *				| http-method : String[0,n]
 *				| key-field : Boolean[0,n]
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	EMPTY : String
 *				| constraint-field : ConstraintField[0,n]
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	[attr: cache-on-match CDATA true]
 *				| 	[attr: cache-on-match-failure CDATA false]
 *				| 	constraint-field-value : String[0,n]
 *				| 		[attr: match-expr CDATA equals]
 *				| 		[attr: cache-on-match CDATA true]
 *				| 		[attr: cache-on-match-failure CDATA false]
 *		class-loader : ClassLoader?
 *			[attr: extra-class-path CDATA #IMPLIED ]
 *			[attr: delegate CDATA true]
 *			[attr: dynamic-reload-interval CDATA #IMPLIED ]
 *			property : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description : String?
 *		jsp-config : JspConfig?
 *			property : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description : String?
 *		locale-charset-info : LocaleCharsetInfo?
 *			[attr: default-locale CDATA #IMPLIED ]
 *			locale-charset-map : LocaleCharsetMap[1,n]
 *				[attr: locale CDATA #REQUIRED ]
 *				[attr: agent CDATA #IMPLIED ]
 *				[attr: charset CDATA #REQUIRED ]
 *				description : String?
 *			parameter-encoding : Boolean?
 *				[attr: form-hint-field CDATA #IMPLIED ]
 *				[attr: default-charset CDATA #IMPLIED ]
 *				EMPTY : String
 *		parameter-encoding : Boolean?
 *			[attr: form-hint-field CDATA #IMPLIED ]
 *			[attr: default-charset CDATA #IMPLIED ]
 *			EMPTY : String
 *		property : WebProperty[0,n]
 *			[attr: name CDATA #REQUIRED ]
 *			[attr: value CDATA #REQUIRED ]
 *			description : String?
 *		message-destination : MessageDestination[0,n]
 *			message-destination-name : String
 *			jndi-name : String
 *		webservice-description : WebserviceDescription[0,n]
 *			webservice-description-name : String
 *			wsdl-publish-location : String?
 *
 */

package com.sun.enterprise.tools.common.dd.webapp;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import com.sun.enterprise.tools.common.dd.EjbRef;
import com.sun.enterprise.tools.common.dd.ResourceEnvRef;
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.ServiceRef;
import com.sun.enterprise.tools.common.dd.SecurityRoleMapping;
import com.sun.enterprise.tools.common.dd.MessageDestination;
import com.sun.enterprise.tools.common.dd.WebserviceDescription;

// BEGIN_NOI18N

public class SunWebApp extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String ERRORURL = "ErrorUrl";	// NOI18N
	static public final String CONTEXT_ROOT = "ContextRoot";	// NOI18N
	static public final String SECURITY_ROLE_MAPPING = "SecurityRoleMapping";	// NOI18N
	static public final String SERVLET = "Servlet";	// NOI18N
	static public final String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern";	// NOI18N
	static public final String IDEMPOTENTURLPATTERNURLPATTERN = "IdempotentUrlPatternUrlPattern";	// NOI18N
	static public final String IDEMPOTENTURLPATTERNNUMOFRETRIES = "IdempotentUrlPatternNumOfRetries";	// NOI18N
	static public final String SESSION_CONFIG = "SessionConfig";	// NOI18N
	static public final String EJB_REF = "EjbRef";	// NOI18N
	static public final String RESOURCE_REF = "ResourceRef";	// NOI18N
	static public final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
	static public final String SERVICE_REF = "ServiceRef";	// NOI18N
	static public final String CACHE = "Cache";	// NOI18N
	static public final String CLASS_LOADER = "ClassLoader";	// NOI18N
	static public final String JSP_CONFIG = "JspConfig";	// NOI18N
	static public final String LOCALE_CHARSET_INFO = "LocaleCharsetInfo";	// NOI18N
	static public final String PARAMETER_ENCODING = "ParameterEncoding";	// NOI18N
	static public final String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField";	// NOI18N
	static public final String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset";	// NOI18N
	static public final String PROPERTY = "WebProperty";	// NOI18N
	static public final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
	static public final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N

	public SunWebApp() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public SunWebApp(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("sun-web-app");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "sun-web-app"));
		}
		Node n = GraphManager.getElementNode("sun-web-app", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "sun-web-app", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public SunWebApp(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("sun-web-app", "SunWebApp",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, SunWebApp.class);

		// Properties (see root bean comments for the bean graph)
		this.createProperty("context-root", 	// NOI18N
			CONTEXT_ROOT, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("security-role-mapping", 	// NOI18N
			SECURITY_ROLE_MAPPING, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityRoleMapping.class);
		this.createProperty("servlet", 	// NOI18N
			SERVLET, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Servlet.class);
		this.createProperty("idempotent-url-pattern", 	// NOI18N
			IDEMPOTENT_URL_PATTERN, 
			Common.TYPE_0_N | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(IDEMPOTENT_URL_PATTERN, "url-pattern", "UrlPattern", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(IDEMPOTENT_URL_PATTERN, "num-of-retries", "NumOfRetries", 
						AttrProp.CDATA,
						null, "-1");
		this.createProperty("session-config", 	// NOI18N
			SESSION_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SessionConfig.class);
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
		this.createProperty("cache", 	// NOI18N
			CACHE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Cache.class);
		this.createAttribute(CACHE, "max-entries", "MaxEntries", 
						AttrProp.CDATA,
						null, "4096");
		this.createAttribute(CACHE, "timeout-in-seconds", "TimeoutInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(CACHE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("class-loader", 	// NOI18N
			CLASS_LOADER, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClassLoader.class);
		this.createAttribute(CLASS_LOADER, "extra-class-path", "ExtraClassPath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(CLASS_LOADER, "delegate", "Delegate", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CLASS_LOADER, "dynamic-reload-interval", "DynamicReloadInterval", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("jsp-config", 	// NOI18N
			JSP_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			JspConfig.class);
		this.createProperty("locale-charset-info", 	// NOI18N
			LOCALE_CHARSET_INFO, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LocaleCharsetInfo.class);
		this.createAttribute(LOCALE_CHARSET_INFO, "default-locale", "DefaultLocale", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("parameter-encoding", 	// NOI18N
			PARAMETER_ENCODING, 
			Common.TYPE_0_1 | Common.TYPE_BOOLEAN | Common.TYPE_KEY, 
			Boolean.class);
		this.createAttribute(PARAMETER_ENCODING, "form-hint-field", "FormHintField", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PARAMETER_ENCODING, "default-charset", "DefaultCharset", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("property", 	// NOI18N
			PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebProperty.class);
		this.createAttribute(PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("message-destination", 	// NOI18N
			MESSAGE_DESTINATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageDestination.class);
		this.createProperty("webservice-description", 	// NOI18N
			WEBSERVICE_DESCRIPTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebserviceDescription.class);
		this.createAttribute("error-url", "ErrorUrl", 
						AttrProp.CDATA,
						null, "");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
			
	}

	// This attribute is mandatory
	public void setErrorUrl(java.lang.String value) {
		setAttributeValue(ERRORURL, value);
	}

	//
	public java.lang.String getErrorUrl() {
		return getAttributeValue(ERRORURL);
	}

	// This attribute is optional
	public void setContextRoot(String value) {
		this.setValue(CONTEXT_ROOT, value);
	}

	//
	public String getContextRoot() {
		return (String)this.getValue(CONTEXT_ROOT);
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

	// This attribute is an array, possibly empty
	public void setServlet(int index, Servlet value) {
		this.setValue(SERVLET, index, value);
	}

	//
	public Servlet getServlet(int index) {
		return (Servlet)this.getValue(SERVLET, index);
	}

	// This attribute is an array, possibly empty
	public void setServlet(Servlet[] value) {
		this.setValue(SERVLET, value);
	}

	//
	public Servlet[] getServlet() {
		return (Servlet[])this.getValues(SERVLET);
	}

	// Return the number of properties
	public int sizeServlet() {
		return this.size(SERVLET);
	}

	// Add a new element returning its index in the list
	public int addServlet(com.sun.enterprise.tools.common.dd.webapp.Servlet value) {
		return this.addValue(SERVLET, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeServlet(com.sun.enterprise.tools.common.dd.webapp.Servlet value) {
		return this.removeValue(SERVLET, value);
	}

	// This attribute is an array, possibly empty
	public void setIdempotentUrlPattern(int index, boolean value) {
		this.setValue(IDEMPOTENT_URL_PATTERN, index, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isIdempotentUrlPattern(int index) {
		Boolean ret = (Boolean)this.getValue(IDEMPOTENT_URL_PATTERN, index);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is an array, possibly empty
	public void setIdempotentUrlPattern(boolean[] value) {
		Boolean[] values = null;
		if (value != null)
		{
			values = new Boolean[value.length];
			for (int i=0; i<value.length; i++)
				values[i] = new Boolean(value[i]);
		}
		this.setValue(IDEMPOTENT_URL_PATTERN, values);
	}

	//
	public boolean[] getIdempotentUrlPattern() {
		boolean[] ret = null;
		Boolean[] values = (Boolean[])this.getValues(IDEMPOTENT_URL_PATTERN);
		if (values != null)
		{
			ret = new boolean[values.length];
			for (int i=0; i<values.length; i++)
				ret[i] = values[i].booleanValue();
		}
		return ret;
	}

	// Return the number of properties
	public int sizeIdempotentUrlPattern() {
		return this.size(IDEMPOTENT_URL_PATTERN);
	}

	// Add a new element returning its index in the list
	public int addIdempotentUrlPattern(boolean value) {
		return this.addValue(IDEMPOTENT_URL_PATTERN, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeIdempotentUrlPattern(boolean value) {
		return this.removeValue(IDEMPOTENT_URL_PATTERN, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	// Remove an element using its index
	//
	public void removeIdempotentUrlPattern(int index) {
		this.removeValue(IDEMPOTENT_URL_PATTERN, index);
	}

	// This attribute is an array, possibly empty
	public void setIdempotentUrlPatternUrlPattern(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDEMPOTENT_URL_PATTERN) == 0) {
			addValue(IDEMPOTENT_URL_PATTERN, "");
		}
		setAttributeValue(IDEMPOTENT_URL_PATTERN, index, "UrlPattern", value);
	}

	//
	public java.lang.String getIdempotentUrlPatternUrlPattern(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDEMPOTENT_URL_PATTERN) == 0) {
			return null;
		} else {
			return getAttributeValue(IDEMPOTENT_URL_PATTERN, index, "UrlPattern");
		}
	}

	// This attribute is an array, possibly empty
	public void setIdempotentUrlPatternNumOfRetries(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(IDEMPOTENT_URL_PATTERN) == 0) {
			addValue(IDEMPOTENT_URL_PATTERN, "");
		}
		setAttributeValue(IDEMPOTENT_URL_PATTERN, index, "NumOfRetries", value);
	}

	//
	public java.lang.String getIdempotentUrlPatternNumOfRetries(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(IDEMPOTENT_URL_PATTERN) == 0) {
			return null;
		} else {
			return getAttributeValue(IDEMPOTENT_URL_PATTERN, index, "NumOfRetries");
		}
	}

	// This attribute is optional
	public void setSessionConfig(SessionConfig value) {
		this.setValue(SESSION_CONFIG, value);
	}

	//
	public SessionConfig getSessionConfig() {
		return (SessionConfig)this.getValue(SESSION_CONFIG);
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

	// This attribute is optional
	public void setCache(Cache value) {
		this.setValue(CACHE, value);
	}

	//
	public Cache getCache() {
		return (Cache)this.getValue(CACHE);
	}

	// This attribute is optional
	public void setClassLoader(ClassLoader value) {
		this.setValue(CLASS_LOADER, value);
	}

	//
	public ClassLoader getClassLoader() {
		return (ClassLoader)this.getValue(CLASS_LOADER);
	}

	// This attribute is optional
	public void setJspConfig(JspConfig value) {
		this.setValue(JSP_CONFIG, value);
	}

	//
	public JspConfig getJspConfig() {
		return (JspConfig)this.getValue(JSP_CONFIG);
	}

	// This attribute is optional
	public void setLocaleCharsetInfo(LocaleCharsetInfo value) {
		this.setValue(LOCALE_CHARSET_INFO, value);
	}

	//
	public LocaleCharsetInfo getLocaleCharsetInfo() {
		return (LocaleCharsetInfo)this.getValue(LOCALE_CHARSET_INFO);
	}

	// This attribute is optional
	public void setParameterEncoding(boolean value) {
		this.setValue(PARAMETER_ENCODING, (value ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE));
	}

	//
	public boolean isParameterEncoding() {
		Boolean ret = (Boolean)this.getValue(PARAMETER_ENCODING);
		if (ret == null)
			ret = (Boolean)Common.defaultScalarValue(Common.TYPE_BOOLEAN);
		return ((java.lang.Boolean)ret).booleanValue();
	}

	// This attribute is optional
	public void setParameterEncodingFormHintField(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PARAMETER_ENCODING) == 0) {
			setValue(PARAMETER_ENCODING, "");
		}
		setAttributeValue(PARAMETER_ENCODING, "FormHintField", value);
	}

	//
	public java.lang.String getParameterEncodingFormHintField() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PARAMETER_ENCODING) == 0) {
			return null;
		} else {
			return getAttributeValue(PARAMETER_ENCODING, "FormHintField");
		}
	}

	// This attribute is optional
	public void setParameterEncodingDefaultCharset(java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(PARAMETER_ENCODING) == 0) {
			setValue(PARAMETER_ENCODING, "");
		}
		setAttributeValue(PARAMETER_ENCODING, "DefaultCharset", value);
	}

	//
	public java.lang.String getParameterEncodingDefaultCharset() {
		// If our element does not exist, then the attribute does not exist.
		if (size(PARAMETER_ENCODING) == 0) {
			return null;
		} else {
			return getAttributeValue(PARAMETER_ENCODING, "DefaultCharset");
		}
	}

	// This attribute is an array, possibly empty
	public void setWebProperty(int index, WebProperty value) {
		this.setValue(PROPERTY, index, value);
	}

	//
	public WebProperty getWebProperty(int index) {
		return (WebProperty)this.getValue(PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setWebProperty(WebProperty[] value) {
		this.setValue(PROPERTY, value);
	}

	//
	public WebProperty[] getWebProperty() {
		return (WebProperty[])this.getValues(PROPERTY);
	}

	// Return the number of properties
	public int sizeWebProperty() {
		return this.size(PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addWebProperty(com.sun.enterprise.tools.common.dd.webapp.WebProperty value) {
		return this.addValue(PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWebProperty(com.sun.enterprise.tools.common.dd.webapp.WebProperty value) {
		return this.removeValue(PROPERTY, value);
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
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static SunWebApp createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new SunWebApp(doc, Common.NO_DEFAULT_VALUES);
	}

	public static SunWebApp createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static SunWebApp createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static SunWebApp createGraph() {
		try {
			return new SunWebApp();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
		boolean restrictionFailure = false;
		// Validating property errorUrl
		if (getErrorUrl() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getErrorUrl() == null", "errorUrl", this);	// NOI18N
		}
		// Validating property contextRoot
		if (getContextRoot() != null) {
		}
		// Validating property securityRoleMapping
		for (int _index = 0; _index < sizeSecurityRoleMapping(); ++_index) {
			SecurityRoleMapping element = getSecurityRoleMapping(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property servlet
		for (int _index = 0; _index < sizeServlet(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.Servlet element = getServlet(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property idempotentUrlPattern
		for (int _index = 0; _index < sizeIdempotentUrlPattern(); 
			++_index) {
			boolean element = isIdempotentUrlPattern(_index);
		}
		// Validating property idempotentUrlPatternUrlPattern
		// Validating property idempotentUrlPatternNumOfRetries
		// Validating property sessionConfig
		if (getSessionConfig() != null) {
			getSessionConfig().validate();
		}
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
		// Validating property cache
		if (getCache() != null) {
			getCache().validate();
		}
		// Validating property classLoader
		if (getClassLoader() != null) {
			getClassLoader().validate();
		}
		// Validating property jspConfig
		if (getJspConfig() != null) {
			getJspConfig().validate();
		}
		// Validating property localeCharsetInfo
		if (getLocaleCharsetInfo() != null) {
			getLocaleCharsetInfo().validate();
		}
		// Validating property parameterEncoding
		// Validating property parameterEncodingFormHintField
		if (getParameterEncodingFormHintField() != null) {
		}
		// Validating property parameterEncodingDefaultCharset
		if (getParameterEncodingDefaultCharset() != null) {
		}
		// Validating property webProperty
		for (int _index = 0; _index < sizeWebProperty(); ++_index) {
			com.sun.enterprise.tools.common.dd.webapp.WebProperty element = getWebProperty(_index);
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
		// Validating property webserviceDescription
		for (int _index = 0; _index < sizeWebserviceDescription(); 
			++_index) {
			WebserviceDescription element = getWebserviceDescription(_index);
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
		str.append("ContextRoot");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getContextRoot();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CONTEXT_ROOT, 0, str, indent);

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
		str.append("Servlet["+this.sizeServlet()+"]");	// NOI18N
		for(int i=0; i<this.sizeServlet(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getServlet(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SERVLET, i, str, indent);
		}

		str.append(indent);
		str.append("IdempotentUrlPattern["+this.sizeIdempotentUrlPattern()+"]");	// NOI18N
		for(int i=0; i<this.sizeIdempotentUrlPattern(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append((this.isIdempotentUrlPattern(i)?"true":"false"));
			this.dumpAttributes(IDEMPOTENT_URL_PATTERN, i, str, indent);
		}

		str.append(indent);
		str.append("SessionConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSessionConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SESSION_CONFIG, 0, str, indent);

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
		str.append("Cache");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCache();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CACHE, 0, str, indent);

		str.append(indent);
		str.append("ClassLoader");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getClassLoader();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CLASS_LOADER, 0, str, indent);

		str.append(indent);
		str.append("JspConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getJspConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(JSP_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("LocaleCharsetInfo");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLocaleCharsetInfo();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOCALE_CHARSET_INFO, 0, str, indent);

		str.append(indent);
		str.append("ParameterEncoding");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append((this.isParameterEncoding()?"true":"false"));
		this.dumpAttributes(PARAMETER_ENCODING, 0, str, indent);

		str.append(indent);
		str.append("WebProperty["+this.sizeWebProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeWebProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PROPERTY, i, str, indent);
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
		str.append("SunWebApp\n");	// NOI18N
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
