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
 *	This generated bean class LoadBalancers matches the DTD element load-balancers
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

public class LoadBalancers extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String LOAD_BALANCER = "LoadBalancer";

	public LoadBalancers() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public LoadBalancers(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("load-balancer", LOAD_BALANCER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LoadBalancer.class);
		this.createAttribute(LOAD_BALANCER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LOAD_BALANCER, "lb-config-name", "LbConfigName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(LOAD_BALANCER, "auto-apply-enabled", "AutoApplyEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public LoadBalancer getLoadBalancer(int index) {
		return (LoadBalancer)this.getValue(LOAD_BALANCER, index);
	}

	// This attribute is an array, possibly empty
	public void setLoadBalancer(LoadBalancer[] value) {
		this.setValue(LOAD_BALANCER, value);
	}

	// Getter Method
	public LoadBalancer[] getLoadBalancer() {
		return (LoadBalancer[])this.getValues(LOAD_BALANCER);
	}

	// Return the number of properties
	public int sizeLoadBalancer() {
		return this.size(LOAD_BALANCER);
	}

	// Add a new element returning its index in the list
	public int addLoadBalancer(LoadBalancer value)
			throws ConfigException{
		return addLoadBalancer(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addLoadBalancer(LoadBalancer value, boolean overwrite)
			throws ConfigException{
		LoadBalancer old = getLoadBalancerByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(LoadBalancers.class).getString("cannotAddDuplicate",  "LoadBalancer"));
		}
		return this.addValue(LOAD_BALANCER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLoadBalancer(LoadBalancer value){
		return this.removeValue(LOAD_BALANCER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeLoadBalancer(LoadBalancer value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(LOAD_BALANCER, value, overwrite);
	}

	public LoadBalancer getLoadBalancerByName(String id) {
	 if (null != id) { id = id.trim(); }
	LoadBalancer[] o = getLoadBalancer();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LoadBalancer newLoadBalancer() {
		return new LoadBalancer();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "load-balancers";
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
		str.append("LoadBalancer["+this.sizeLoadBalancer()+"]");	// NOI18N
		for(int i=0; i<this.sizeLoadBalancer(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getLoadBalancer(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(LOAD_BALANCER, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("LoadBalancers\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

