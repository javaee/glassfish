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
 *	This generated bean class AlertSubscription matches the DTD element alert-subscription
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

public class AlertSubscription extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String LISTENER_CONFIG = "ListenerConfig";
	static public final String FILTER_CONFIG = "FilterConfig";

	public AlertSubscription() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public AlertSubscription(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("listener-config", LISTENER_CONFIG, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ListenerConfig.class);
		this.createAttribute(LISTENER_CONFIG, "listener-class-name", "ListenerClassName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LISTENER_CONFIG, "subscribe-listener-with", "SubscribeListenerWith", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("filter-config", FILTER_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			FilterConfig.class);
		this.createAttribute(FILTER_CONFIG, "filter-class-name", "FilterClassName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setListenerConfig(ListenerConfig value) {
		this.setValue(LISTENER_CONFIG, value);
	}

	// Get Method
	public ListenerConfig getListenerConfig() {
		return (ListenerConfig)this.getValue(LISTENER_CONFIG);
	}

	// This attribute is optional
	public void setFilterConfig(FilterConfig value) {
		this.setValue(FILTER_CONFIG, value);
	}

	// Get Method
	public FilterConfig getFilterConfig() {
		return (FilterConfig)this.getValue(FILTER_CONFIG);
	}

	/**
	* Getter for Name of the Element alert-subscription
	* @return  the Name of the Element alert-subscription
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element alert-subscription
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element alert-subscription
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ListenerConfig newListenerConfig() {
		return new ListenerConfig();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public FilterConfig newFilterConfig() {
		return new FilterConfig();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "alert-subscription" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
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
		str.append("ListenerConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getListenerConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LISTENER_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("FilterConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getFilterConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(FILTER_CONFIG, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("AlertSubscription\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

