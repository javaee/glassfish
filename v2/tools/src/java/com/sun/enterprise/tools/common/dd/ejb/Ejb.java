/**
 *	This generated bean class Ejb matches the schema element ejb
 *
 *	Generated on Wed Mar 03 14:29:49 PST 2004
 */

package com.sun.enterprise.tools.common.dd.ejb;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import com.sun.enterprise.tools.common.dd.ResourceRef;
import com.sun.enterprise.tools.common.dd.EjbRef;
import com.sun.enterprise.tools.common.dd.ResourceEnvRef;
import com.sun.enterprise.tools.common.dd.ServiceRef;
import com.sun.enterprise.tools.common.dd.WebserviceEndpoint;

// BEGIN_NOI18N

public class Ejb extends com.sun.enterprise.tools.common.dd.SunBaseBean
{

	static Vector comparators = new Vector();

	static public final String AVAILABILITYENABLED = "AvailabilityEnabled";	// NOI18N
	static public final String EJB_NAME = "EjbName";	// NOI18N
	static public final String JNDI_NAME = "JndiName";	// NOI18N
	static public final String EJB_REF = "EjbRef";	// NOI18N
	static public final String RESOURCE_REF = "ResourceRef";	// NOI18N
	static public final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
	static public final String SERVICE_REF = "ServiceRef";	// NOI18N
	static public final String PASS_BY_REFERENCE = "PassByReference";	// NOI18N
	static public final String CMP = "Cmp";	// NOI18N
	static public final String PRINCIPAL = "Principal";	// NOI18N
	static public final String MDB_CONNECTION_FACTORY = "MdbConnectionFactory";	// NOI18N
	static public final String JMS_DURABLE_SUBSCRIPTION_NAME = "JmsDurableSubscriptionName";	// NOI18N
	static public final String JMS_MAX_MESSAGES_LOAD = "JmsMaxMessagesLoad";	// NOI18N
	static public final String IOR_SECURITY_CONFIG = "IorSecurityConfig";	// NOI18N
	static public final String IS_READ_ONLY_BEAN = "IsReadOnlyBean";	// NOI18N
	static public final String REFRESH_PERIOD_IN_SECONDS = "RefreshPeriodInSeconds";	// NOI18N
	static public final String COMMIT_OPTION = "CommitOption";	// NOI18N
	static public final String CMT_TIMEOUT_IN_SECONDS = "CmtTimeoutInSeconds";	// NOI18N
	static public final String USE_THREAD_POOL_ID = "UseThreadPoolId";	// NOI18N
	static public final String GEN_CLASSES = "GenClasses";	// NOI18N
	static public final String BEAN_POOL = "BeanPool";	// NOI18N
	static public final String BEAN_CACHE = "BeanCache";	// NOI18N
	static public final String MDB_RESOURCE_ADAPTER = "MdbResourceAdapter";	// NOI18N
	static public final String WEBSERVICE_ENDPOINT = "WebserviceEndpoint";	// NOI18N
	static public final String FLUSH_AT_END_OF_METHOD = "FlushAtEndOfMethod";	// NOI18N
	static public final String CHECKPOINTED_METHODS = "CheckpointedMethods";	// NOI18N
	static public final String CHECKPOINT_AT_END_OF_METHOD = "CheckpointAtEndOfMethod";	// NOI18N

	public Ejb() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Ejb(int options)
	{
		super(comparators, new org.netbeans.modules.schema2beans.Version(1, 2, 0));
		// Properties (see root bean comments for the bean graph)
		this.createProperty("ejb-name", 	// NOI18N
			EJB_NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("jndi-name", 	// NOI18N
			JNDI_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
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
		this.createProperty("pass-by-reference", 	// NOI18N
			PASS_BY_REFERENCE, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("cmp", 	// NOI18N
			CMP, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Cmp.class);
		this.createProperty("principal", 	// NOI18N
			PRINCIPAL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Principal.class);
		this.createProperty("mdb-connection-factory", 	// NOI18N
			MDB_CONNECTION_FACTORY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MdbConnectionFactory.class);
		this.createProperty("jms-durable-subscription-name", 	// NOI18N
			JMS_DURABLE_SUBSCRIPTION_NAME, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("jms-max-messages-load", 	// NOI18N
			JMS_MAX_MESSAGES_LOAD, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("ior-security-config", 	// NOI18N
			IOR_SECURITY_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IorSecurityConfig.class);
		this.createProperty("is-read-only-bean", 	// NOI18N
			IS_READ_ONLY_BEAN, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("refresh-period-in-seconds", 	// NOI18N
			REFRESH_PERIOD_IN_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("commit-option", 	// NOI18N
			COMMIT_OPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("cmt-timeout-in-seconds", 	// NOI18N
			CMT_TIMEOUT_IN_SECONDS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("use-thread-pool-id", 	// NOI18N
			USE_THREAD_POOL_ID, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("gen-classes", 	// NOI18N
			GEN_CLASSES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			GenClasses.class);
		this.createProperty("bean-pool", 	// NOI18N
			BEAN_POOL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			BeanPool.class);
		this.createProperty("bean-cache", 	// NOI18N
			BEAN_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			BeanCache.class);
		this.createProperty("mdb-resource-adapter", 	// NOI18N
			MDB_RESOURCE_ADAPTER, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MdbResourceAdapter.class);
		this.createProperty("webservice-endpoint", 	// NOI18N
			WEBSERVICE_ENDPOINT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			WebserviceEndpoint.class);
		this.createProperty("flush-at-end-of-method", 	// NOI18N
			FLUSH_AT_END_OF_METHOD, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FlushAtEndOfMethod.class);
		this.createProperty("checkpointed-methods", 	// NOI18N
			CHECKPOINTED_METHODS, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("checkpoint-at-end-of-method", 	// NOI18N
			CHECKPOINT_AT_END_OF_METHOD, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CheckpointAtEndOfMethod.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options)
	{
											
	}

	// This attribute is optional
	public void setAvailabilityEnabled(java.lang.String value) {
		setAttributeValue(AVAILABILITYENABLED, value);
	}

	//
	public java.lang.String getAvailabilityEnabled() {
		return getAttributeValue(AVAILABILITYENABLED);
	}

	// This attribute is mandatory
	public void setEjbName(String value) {
		this.setValue(EJB_NAME, value);
	}

	//
	public String getEjbName() {
		return (String)this.getValue(EJB_NAME);
	}

	// This attribute is optional
	public void setJndiName(String value) {
		this.setValue(JNDI_NAME, value);
	}

	//
	public String getJndiName() {
		return (String)this.getValue(JNDI_NAME);
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
	public void setPassByReference(String value) {
		this.setValue(PASS_BY_REFERENCE, value);
	}

	//
	public String getPassByReference() {
		return (String)this.getValue(PASS_BY_REFERENCE);
	}

	// This attribute is optional
	public void setCmp(Cmp value) {
		this.setValue(CMP, value);
	}

	//
	public Cmp getCmp() {
		return (Cmp)this.getValue(CMP);
	}

	// This attribute is optional
	public void setPrincipal(Principal value) {
		this.setValue(PRINCIPAL, value);
	}

	//
	public Principal getPrincipal() {
		return (Principal)this.getValue(PRINCIPAL);
	}

	// This attribute is optional
	public void setMdbConnectionFactory(MdbConnectionFactory value) {
		this.setValue(MDB_CONNECTION_FACTORY, value);
	}

	//
	public MdbConnectionFactory getMdbConnectionFactory() {
		return (MdbConnectionFactory)this.getValue(MDB_CONNECTION_FACTORY);
	}

	// This attribute is optional
	public void setJmsDurableSubscriptionName(String value) {
		this.setValue(JMS_DURABLE_SUBSCRIPTION_NAME, value);
	}

	//
	public String getJmsDurableSubscriptionName() {
		return (String)this.getValue(JMS_DURABLE_SUBSCRIPTION_NAME);
	}

	// This attribute is optional
	public void setJmsMaxMessagesLoad(String value) {
		this.setValue(JMS_MAX_MESSAGES_LOAD, value);
	}

	//
	public String getJmsMaxMessagesLoad() {
		return (String)this.getValue(JMS_MAX_MESSAGES_LOAD);
	}

	// This attribute is optional
	public void setIorSecurityConfig(IorSecurityConfig value) {
		this.setValue(IOR_SECURITY_CONFIG, value);
	}

	//
	public IorSecurityConfig getIorSecurityConfig() {
		return (IorSecurityConfig)this.getValue(IOR_SECURITY_CONFIG);
	}

	// This attribute is optional
	public void setIsReadOnlyBean(String value) {
		this.setValue(IS_READ_ONLY_BEAN, value);
	}

	//
	public String getIsReadOnlyBean() {
		return (String)this.getValue(IS_READ_ONLY_BEAN);
	}

	// This attribute is optional
	public void setRefreshPeriodInSeconds(String value) {
		this.setValue(REFRESH_PERIOD_IN_SECONDS, value);
	}

	//
	public String getRefreshPeriodInSeconds() {
		return (String)this.getValue(REFRESH_PERIOD_IN_SECONDS);
	}

	// This attribute is optional
	public void setCommitOption(String value) {
		this.setValue(COMMIT_OPTION, value);
	}

	//
	public String getCommitOption() {
		return (String)this.getValue(COMMIT_OPTION);
	}

	// This attribute is optional
	public void setCmtTimeoutInSeconds(String value) {
		this.setValue(CMT_TIMEOUT_IN_SECONDS, value);
	}

	//
	public String getCmtTimeoutInSeconds() {
		return (String)this.getValue(CMT_TIMEOUT_IN_SECONDS);
	}

	// This attribute is optional
	public void setUseThreadPoolId(String value) {
		this.setValue(USE_THREAD_POOL_ID, value);
	}

	//
	public String getUseThreadPoolId() {
		return (String)this.getValue(USE_THREAD_POOL_ID);
	}

	// This attribute is optional
	public void setGenClasses(GenClasses value) {
		this.setValue(GEN_CLASSES, value);
	}

	//
	public GenClasses getGenClasses() {
		return (GenClasses)this.getValue(GEN_CLASSES);
	}

	// This attribute is optional
	public void setBeanPool(BeanPool value) {
		this.setValue(BEAN_POOL, value);
	}

	//
	public BeanPool getBeanPool() {
		return (BeanPool)this.getValue(BEAN_POOL);
	}

	// This attribute is optional
	public void setBeanCache(BeanCache value) {
		this.setValue(BEAN_CACHE, value);
	}

	//
	public BeanCache getBeanCache() {
		return (BeanCache)this.getValue(BEAN_CACHE);
	}

	// This attribute is optional
	public void setMdbResourceAdapter(MdbResourceAdapter value) {
		this.setValue(MDB_RESOURCE_ADAPTER, value);
	}

	//
	public MdbResourceAdapter getMdbResourceAdapter() {
		return (MdbResourceAdapter)this.getValue(MDB_RESOURCE_ADAPTER);
	}

	// This attribute is an array, possibly empty
	public void setWebserviceEndpoint(int index, WebserviceEndpoint value) {
		this.setValue(WEBSERVICE_ENDPOINT, index, value);
	}

	//
	public WebserviceEndpoint getWebserviceEndpoint(int index) {
		return (WebserviceEndpoint)this.getValue(WEBSERVICE_ENDPOINT, index);
	}

	// This attribute is an array, possibly empty
	public void setWebserviceEndpoint(WebserviceEndpoint[] value) {
		this.setValue(WEBSERVICE_ENDPOINT, value);
	}

	//
	public WebserviceEndpoint[] getWebserviceEndpoint() {
		return (WebserviceEndpoint[])this.getValues(WEBSERVICE_ENDPOINT);
	}

	// Return the number of properties
	public int sizeWebserviceEndpoint() {
		return this.size(WEBSERVICE_ENDPOINT);
	}

	// Add a new element returning its index in the list
	public int addWebserviceEndpoint(WebserviceEndpoint value) {
		return this.addValue(WEBSERVICE_ENDPOINT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeWebserviceEndpoint(WebserviceEndpoint value) {
		return this.removeValue(WEBSERVICE_ENDPOINT, value);
	}

	// This attribute is optional
	public void setFlushAtEndOfMethod(FlushAtEndOfMethod value) {
		this.setValue(FLUSH_AT_END_OF_METHOD, value);
	}

	//
	public FlushAtEndOfMethod getFlushAtEndOfMethod() {
		return (FlushAtEndOfMethod)this.getValue(FLUSH_AT_END_OF_METHOD);
	}

	// This attribute is optional
	public void setCheckpointedMethods(String value) {
		this.setValue(CHECKPOINTED_METHODS, value);
	}

	//
	public String getCheckpointedMethods() {
		return (String)this.getValue(CHECKPOINTED_METHODS);
	}

	// This attribute is optional
	public void setCheckpointAtEndOfMethod(CheckpointAtEndOfMethod value) {
		this.setValue(CHECKPOINT_AT_END_OF_METHOD, value);
	}

	//
	public CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() {
		return (CheckpointAtEndOfMethod)this.getValue(CHECKPOINT_AT_END_OF_METHOD);
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
		// Validating property availabilityEnabled
		if (getAvailabilityEnabled() != null) {
		}
		// Validating property ejbName
		if (getEjbName() == null) {
			throw new org.netbeans.modules.schema2beans.ValidateException("getEjbName() == null", "ejbName", this);	// NOI18N
		}
		// Validating property jndiName
		if (getJndiName() != null) {
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
		// Validating property passByReference
		if (getPassByReference() != null) {
		}
		// Validating property cmp
		if (getCmp() != null) {
			getCmp().validate();
		}
		// Validating property principal
		if (getPrincipal() != null) {
			getPrincipal().validate();
		}
		// Validating property mdbConnectionFactory
		if (getMdbConnectionFactory() != null) {
			getMdbConnectionFactory().validate();
		}
		// Validating property jmsDurableSubscriptionName
		if (getJmsDurableSubscriptionName() != null) {
		}
		// Validating property jmsMaxMessagesLoad
		if (getJmsMaxMessagesLoad() != null) {
		}
		// Validating property iorSecurityConfig
		if (getIorSecurityConfig() != null) {
			getIorSecurityConfig().validate();
		}
		// Validating property isReadOnlyBean
		if (getIsReadOnlyBean() != null) {
		}
		// Validating property refreshPeriodInSeconds
		if (getRefreshPeriodInSeconds() != null) {
		}
		// Validating property commitOption
		if (getCommitOption() != null) {
		}
		// Validating property cmtTimeoutInSeconds
		if (getCmtTimeoutInSeconds() != null) {
		}
		// Validating property useThreadPoolId
		if (getUseThreadPoolId() != null) {
		}
		// Validating property genClasses
		if (getGenClasses() != null) {
			getGenClasses().validate();
		}
		// Validating property beanPool
		if (getBeanPool() != null) {
			getBeanPool().validate();
		}
		// Validating property beanCache
		if (getBeanCache() != null) {
			getBeanCache().validate();
		}
		// Validating property mdbResourceAdapter
		if (getMdbResourceAdapter() != null) {
			getMdbResourceAdapter().validate();
		}
		// Validating property webserviceEndpoint
		for (int _index = 0; _index < sizeWebserviceEndpoint(); ++_index) {
			WebserviceEndpoint element = getWebserviceEndpoint(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property flushAtEndOfMethod
		if (getFlushAtEndOfMethod() != null) {
			getFlushAtEndOfMethod().validate();
		}
		// Validating property checkpointedMethods
		if (getCheckpointedMethods() != null) {
		}
		// Validating property checkpointAtEndOfMethod
		if (getCheckpointAtEndOfMethod() != null) {
			getCheckpointAtEndOfMethod().validate();
		}
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("EjbName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getEjbName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(EJB_NAME, 0, str, indent);

		str.append(indent);
		str.append("JndiName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getJndiName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JNDI_NAME, 0, str, indent);

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
		str.append("PassByReference");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getPassByReference();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(PASS_BY_REFERENCE, 0, str, indent);

		str.append(indent);
		str.append("Cmp");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCmp();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CMP, 0, str, indent);

		str.append(indent);
		str.append("Principal");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getPrincipal();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PRINCIPAL, 0, str, indent);

		str.append(indent);
		str.append("MdbConnectionFactory");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMdbConnectionFactory();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MDB_CONNECTION_FACTORY, 0, str, indent);

		str.append(indent);
		str.append("JmsDurableSubscriptionName");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getJmsDurableSubscriptionName();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JMS_DURABLE_SUBSCRIPTION_NAME, 0, str, indent);

		str.append(indent);
		str.append("JmsMaxMessagesLoad");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getJmsMaxMessagesLoad();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(JMS_MAX_MESSAGES_LOAD, 0, str, indent);

		str.append(indent);
		str.append("IorSecurityConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getIorSecurityConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(IOR_SECURITY_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("IsReadOnlyBean");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getIsReadOnlyBean();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(IS_READ_ONLY_BEAN, 0, str, indent);

		str.append(indent);
		str.append("RefreshPeriodInSeconds");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getRefreshPeriodInSeconds();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(REFRESH_PERIOD_IN_SECONDS, 0, str, indent);

		str.append(indent);
		str.append("CommitOption");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getCommitOption();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COMMIT_OPTION, 0, str, indent);

		str.append(indent);
		str.append("CmtTimeoutInSeconds");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getCmtTimeoutInSeconds();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CMT_TIMEOUT_IN_SECONDS, 0, str, indent);

		str.append(indent);
		str.append("UseThreadPoolId");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getUseThreadPoolId();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(USE_THREAD_POOL_ID, 0, str, indent);

		str.append(indent);
		str.append("GenClasses");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getGenClasses();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(GEN_CLASSES, 0, str, indent);

		str.append(indent);
		str.append("BeanPool");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getBeanPool();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(BEAN_POOL, 0, str, indent);

		str.append(indent);
		str.append("BeanCache");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getBeanCache();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(BEAN_CACHE, 0, str, indent);

		str.append(indent);
		str.append("MdbResourceAdapter");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getMdbResourceAdapter();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MDB_RESOURCE_ADAPTER, 0, str, indent);

		str.append(indent);
		str.append("WebserviceEndpoint["+this.sizeWebserviceEndpoint()+"]");	// NOI18N
		for(int i=0; i<this.sizeWebserviceEndpoint(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getWebserviceEndpoint(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(WEBSERVICE_ENDPOINT, i, str, indent);
		}

		str.append(indent);
		str.append("FlushAtEndOfMethod");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFlushAtEndOfMethod();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FLUSH_AT_END_OF_METHOD, 0, str, indent);

		str.append(indent);
		str.append("CheckpointedMethods");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		s = this.getCheckpointedMethods();
		str.append((s==null?"null":s.trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(CHECKPOINTED_METHODS, 0, str, indent);

		str.append(indent);
		str.append("CheckpointAtEndOfMethod");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCheckpointAtEndOfMethod();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CHECKPOINT_AT_END_OF_METHOD, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Ejb\n");	// NOI18N
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
