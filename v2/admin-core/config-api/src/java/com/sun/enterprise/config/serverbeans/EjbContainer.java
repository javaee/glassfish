/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
 
/**
 *	This generated bean class EjbContainer matches the DTD element ejb-container
 *
 */

package com.sun.enterprise.config.serverbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.Serializable;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.util.i18n.StringManager;

// BEGIN_NOI18N

public class EjbContainer extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String EJB_TIMER_SERVICE = "EjbTimerService";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public EjbContainer() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public EjbContainer(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("ejb-timer-service", EJB_TIMER_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			EjbTimerService.class);
		this.createAttribute(EJB_TIMER_SERVICE, "minimum-delivery-interval-in-millis", "MinimumDeliveryIntervalInMillis", 
						AttrProp.CDATA,
						null, "7000");
		this.createAttribute(EJB_TIMER_SERVICE, "max-redeliveries", "MaxRedeliveries", 
						AttrProp.CDATA,
						null, "1");
		this.createAttribute(EJB_TIMER_SERVICE, "timer-datasource", "TimerDatasource", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(EJB_TIMER_SERVICE, "redelivery-interval-internal-in-millis", "RedeliveryIntervalInternalInMillis", 
						AttrProp.CDATA,
						null, "5000");
		this.createProperty("property", ELEMENT_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setEjbTimerService(EjbTimerService value) {
		this.setValue(EJB_TIMER_SERVICE, value);
	}

	// Get Method
	public EjbTimerService getEjbTimerService() {
		return (EjbTimerService)this.getValue(EJB_TIMER_SERVICE);
	}

	// Get Method
	public ElementProperty getElementProperty(int index) {
		return (ElementProperty)this.getValue(ELEMENT_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setElementProperty(ElementProperty[] value) {
		this.setValue(ELEMENT_PROPERTY, value);
	}

	// Getter Method
	public ElementProperty[] getElementProperty() {
		return (ElementProperty[])this.getValues(ELEMENT_PROPERTY);
	}

	// Return the number of properties
	public int sizeElementProperty() {
		return this.size(ELEMENT_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addElementProperty(ElementProperty value)
			throws ConfigException{
		return addElementProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addElementProperty(ElementProperty value, boolean overwrite)
			throws ConfigException{
		ElementProperty old = getElementPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(EjbContainer.class).getString("cannotAddDuplicate",  "ElementProperty"));
		}
		return this.addValue(ELEMENT_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeElementProperty(ElementProperty value){
		return this.removeValue(ELEMENT_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeElementProperty(ElementProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ELEMENT_PROPERTY, value, overwrite);
	}

	public ElementProperty getElementPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	ElementProperty[] o = getElementProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for SteadyPoolSize of the Element ejb-container
	* @return  the SteadyPoolSize of the Element ejb-container
	*/
	public String getSteadyPoolSize() {
		return getAttributeValue(ServerTags.STEADY_POOL_SIZE);
	}
	/**
	* Modify  the SteadyPoolSize of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSteadyPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the SteadyPoolSize of the Element ejb-container
	* @param v the new value
	*/
	public void setSteadyPoolSize(String v) {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v);
	}
	/**
	* Get the default value of SteadyPoolSize from dtd
	*/
	public static String getDefaultSteadyPoolSize() {
		return "32".trim();
	}
	/**
	* Getter for PoolResizeQuantity of the Element ejb-container
	* @return  the PoolResizeQuantity of the Element ejb-container
	*/
	public String getPoolResizeQuantity() {
		return getAttributeValue(ServerTags.POOL_RESIZE_QUANTITY);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPoolResizeQuantity(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v, overwrite);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element ejb-container
	* @param v the new value
	*/
	public void setPoolResizeQuantity(String v) {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v);
	}
	/**
	* Get the default value of PoolResizeQuantity from dtd
	*/
	public static String getDefaultPoolResizeQuantity() {
		return "16".trim();
	}
	/**
	* Getter for MaxPoolSize of the Element ejb-container
	* @return  the MaxPoolSize of the Element ejb-container
	*/
	public String getMaxPoolSize() {
		return getAttributeValue(ServerTags.MAX_POOL_SIZE);
	}
	/**
	* Modify  the MaxPoolSize of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxPoolSize of the Element ejb-container
	* @param v the new value
	*/
	public void setMaxPoolSize(String v) {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v);
	}
	/**
	* Get the default value of MaxPoolSize from dtd
	*/
	public static String getDefaultMaxPoolSize() {
		return "64".trim();
	}
	/**
	* Getter for CacheResizeQuantity of the Element ejb-container
	* @return  the CacheResizeQuantity of the Element ejb-container
	*/
	public String getCacheResizeQuantity() {
		return getAttributeValue(ServerTags.CACHE_RESIZE_QUANTITY);
	}
	/**
	* Modify  the CacheResizeQuantity of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCacheResizeQuantity(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CACHE_RESIZE_QUANTITY, v, overwrite);
	}
	/**
	* Modify  the CacheResizeQuantity of the Element ejb-container
	* @param v the new value
	*/
	public void setCacheResizeQuantity(String v) {
		setAttributeValue(ServerTags.CACHE_RESIZE_QUANTITY, v);
	}
	/**
	* Get the default value of CacheResizeQuantity from dtd
	*/
	public static String getDefaultCacheResizeQuantity() {
		return "32".trim();
	}
	/**
	* Getter for MaxCacheSize of the Element ejb-container
	* @return  the MaxCacheSize of the Element ejb-container
	*/
	public String getMaxCacheSize() {
		return getAttributeValue(ServerTags.MAX_CACHE_SIZE);
	}
	/**
	* Modify  the MaxCacheSize of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxCacheSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_CACHE_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxCacheSize of the Element ejb-container
	* @param v the new value
	*/
	public void setMaxCacheSize(String v) {
		setAttributeValue(ServerTags.MAX_CACHE_SIZE, v);
	}
	/**
	* Get the default value of MaxCacheSize from dtd
	*/
	public static String getDefaultMaxCacheSize() {
		return "512".trim();
	}
	/**
	* Getter for PoolIdleTimeoutInSeconds of the Element ejb-container
	* @return  the PoolIdleTimeoutInSeconds of the Element ejb-container
	*/
	public String getPoolIdleTimeoutInSeconds() {
		return getAttributeValue(ServerTags.POOL_IDLE_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the PoolIdleTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPoolIdleTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.POOL_IDLE_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the PoolIdleTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	*/
	public void setPoolIdleTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.POOL_IDLE_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of PoolIdleTimeoutInSeconds from dtd
	*/
	public static String getDefaultPoolIdleTimeoutInSeconds() {
		return "600".trim();
	}
	/**
	* Getter for CacheIdleTimeoutInSeconds of the Element ejb-container
	* @return  the CacheIdleTimeoutInSeconds of the Element ejb-container
	*/
	public String getCacheIdleTimeoutInSeconds() {
		return getAttributeValue(ServerTags.CACHE_IDLE_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the CacheIdleTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCacheIdleTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CACHE_IDLE_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the CacheIdleTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	*/
	public void setCacheIdleTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.CACHE_IDLE_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of CacheIdleTimeoutInSeconds from dtd
	*/
	public static String getDefaultCacheIdleTimeoutInSeconds() {
		return "600".trim();
	}
	/**
	* Getter for RemovalTimeoutInSeconds of the Element ejb-container
	* @return  the RemovalTimeoutInSeconds of the Element ejb-container
	*/
	public String getRemovalTimeoutInSeconds() {
		return getAttributeValue(ServerTags.REMOVAL_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the RemovalTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRemovalTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.REMOVAL_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the RemovalTimeoutInSeconds of the Element ejb-container
	* @param v the new value
	*/
	public void setRemovalTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.REMOVAL_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of RemovalTimeoutInSeconds from dtd
	*/
	public static String getDefaultRemovalTimeoutInSeconds() {
		return "5400".trim();
	}
	/**
	* Getter for VictimSelectionPolicy of the Element ejb-container
	* @return  the VictimSelectionPolicy of the Element ejb-container
	*/
	public String getVictimSelectionPolicy() {
		return getAttributeValue(ServerTags.VICTIM_SELECTION_POLICY);
	}
	/**
	* Modify  the VictimSelectionPolicy of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setVictimSelectionPolicy(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VICTIM_SELECTION_POLICY, v, overwrite);
	}
	/**
	* Modify  the VictimSelectionPolicy of the Element ejb-container
	* @param v the new value
	*/
	public void setVictimSelectionPolicy(String v) {
		setAttributeValue(ServerTags.VICTIM_SELECTION_POLICY, v);
	}
	/**
	* Get the default value of VictimSelectionPolicy from dtd
	*/
	public static String getDefaultVictimSelectionPolicy() {
		return "nru".trim();
	}
	/**
	* Getter for CommitOption of the Element ejb-container
	* @return  the CommitOption of the Element ejb-container
	*/
	public String getCommitOption() {
		return getAttributeValue(ServerTags.COMMIT_OPTION);
	}
	/**
	* Modify  the CommitOption of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCommitOption(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.COMMIT_OPTION, v, overwrite);
	}
	/**
	* Modify  the CommitOption of the Element ejb-container
	* @param v the new value
	*/
	public void setCommitOption(String v) {
		setAttributeValue(ServerTags.COMMIT_OPTION, v);
	}
	/**
	* Get the default value of CommitOption from dtd
	*/
	public static String getDefaultCommitOption() {
		return "B".trim();
	}
	/**
	* Getter for SessionStore of the Element ejb-container
	* @return  the SessionStore of the Element ejb-container
	*/
	public String getSessionStore() {
			return getAttributeValue(ServerTags.SESSION_STORE);
	}
	/**
	* Modify  the SessionStore of the Element ejb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSessionStore(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SESSION_STORE, v, overwrite);
	}
	/**
	* Modify  the SessionStore of the Element ejb-container
	* @param v the new value
	*/
	public void setSessionStore(String v) {
		setAttributeValue(ServerTags.SESSION_STORE, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public EjbTimerService newEjbTimerService() {
		return new EjbTimerService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ElementProperty newElementProperty() {
		return new ElementProperty();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "ejb-container";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.STEADY_POOL_SIZE)) return "32".trim();
		if(attr.equals(ServerTags.POOL_RESIZE_QUANTITY)) return "16".trim();
		if(attr.equals(ServerTags.MAX_POOL_SIZE)) return "64".trim();
		if(attr.equals(ServerTags.CACHE_RESIZE_QUANTITY)) return "32".trim();
		if(attr.equals(ServerTags.MAX_CACHE_SIZE)) return "512".trim();
		if(attr.equals(ServerTags.POOL_IDLE_TIMEOUT_IN_SECONDS)) return "600".trim();
		if(attr.equals(ServerTags.CACHE_IDLE_TIMEOUT_IN_SECONDS)) return "600".trim();
		if(attr.equals(ServerTags.REMOVAL_TIMEOUT_IN_SECONDS)) return "5400".trim();
		if(attr.equals(ServerTags.VICTIM_SELECTION_POLICY)) return "nru".trim();
		if(attr.equals(ServerTags.COMMIT_OPTION)) return "B".trim();
	return null;
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
		str.append("EjbTimerService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getEjbTimerService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(EJB_TIMER_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("ElementProperty["+this.sizeElementProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeElementProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getElementProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ELEMENT_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("EjbContainer\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

