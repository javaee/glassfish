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
 *	This generated bean class MdbContainer matches the DTD element mdb-container
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

public class MdbContainer extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public MdbContainer() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MdbContainer(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
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
			throw new ConfigException(StringManager.getManager(MdbContainer.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for SteadyPoolSize of the Element mdb-container
	* @return  the SteadyPoolSize of the Element mdb-container
	*/
	public String getSteadyPoolSize() {
		return getAttributeValue(ServerTags.STEADY_POOL_SIZE);
	}
	/**
	* Modify  the SteadyPoolSize of the Element mdb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSteadyPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the SteadyPoolSize of the Element mdb-container
	* @param v the new value
	*/
	public void setSteadyPoolSize(String v) {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v);
	}
	/**
	* Get the default value of SteadyPoolSize from dtd
	*/
	public static String getDefaultSteadyPoolSize() {
		return "10".trim();
	}
	/**
	* Getter for PoolResizeQuantity of the Element mdb-container
	* @return  the PoolResizeQuantity of the Element mdb-container
	*/
	public String getPoolResizeQuantity() {
		return getAttributeValue(ServerTags.POOL_RESIZE_QUANTITY);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element mdb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPoolResizeQuantity(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v, overwrite);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element mdb-container
	* @param v the new value
	*/
	public void setPoolResizeQuantity(String v) {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v);
	}
	/**
	* Get the default value of PoolResizeQuantity from dtd
	*/
	public static String getDefaultPoolResizeQuantity() {
		return "2".trim();
	}
	/**
	* Getter for MaxPoolSize of the Element mdb-container
	* @return  the MaxPoolSize of the Element mdb-container
	*/
	public String getMaxPoolSize() {
		return getAttributeValue(ServerTags.MAX_POOL_SIZE);
	}
	/**
	* Modify  the MaxPoolSize of the Element mdb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxPoolSize of the Element mdb-container
	* @param v the new value
	*/
	public void setMaxPoolSize(String v) {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v);
	}
	/**
	* Get the default value of MaxPoolSize from dtd
	*/
	public static String getDefaultMaxPoolSize() {
		return "60".trim();
	}
	/**
	* Getter for IdleTimeoutInSeconds of the Element mdb-container
	* @return  the IdleTimeoutInSeconds of the Element mdb-container
	*/
	public String getIdleTimeoutInSeconds() {
		return getAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the IdleTimeoutInSeconds of the Element mdb-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setIdleTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the IdleTimeoutInSeconds of the Element mdb-container
	* @param v the new value
	*/
	public void setIdleTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of IdleTimeoutInSeconds from dtd
	*/
	public static String getDefaultIdleTimeoutInSeconds() {
		return "600".trim();
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
	    ret = "mdb-container";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.STEADY_POOL_SIZE)) return "10".trim();
		if(attr.equals(ServerTags.POOL_RESIZE_QUANTITY)) return "2".trim();
		if(attr.equals(ServerTags.MAX_POOL_SIZE)) return "60".trim();
		if(attr.equals(ServerTags.IDLE_TIMEOUT_IN_SECONDS)) return "600".trim();
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
		str.append("MdbContainer\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

