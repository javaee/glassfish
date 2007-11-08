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
 *	This generated bean class Clusters matches the DTD element clusters
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

public class Clusters extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String CLUSTER = "Cluster";

	public Clusters() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Clusters(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("cluster", CLUSTER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Cluster.class);
		this.createAttribute(CLUSTER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CLUSTER, "config-ref", "ConfigRef", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CLUSTER, "heartbeat-enabled", "HeartbeatEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(CLUSTER, "heartbeat-port", "HeartbeatPort", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(CLUSTER, "heartbeat-address", "HeartbeatAddress", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public Cluster getCluster(int index) {
		return (Cluster)this.getValue(CLUSTER, index);
	}

	// This attribute is an array, possibly empty
	public void setCluster(Cluster[] value) {
		this.setValue(CLUSTER, value);
	}

	// Getter Method
	public Cluster[] getCluster() {
		return (Cluster[])this.getValues(CLUSTER);
	}

	// Return the number of properties
	public int sizeCluster() {
		return this.size(CLUSTER);
	}

	// Add a new element returning its index in the list
	public int addCluster(Cluster value)
			throws ConfigException{
		return addCluster(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addCluster(Cluster value, boolean overwrite)
			throws ConfigException{
		Cluster old = getClusterByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Clusters.class).getString("cannotAddDuplicate",  "Cluster"));
		}
		return this.addValue(CLUSTER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeCluster(Cluster value){
		return this.removeValue(CLUSTER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeCluster(Cluster value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(CLUSTER, value, overwrite);
	}

	public Cluster getClusterByName(String id) {
	 if (null != id) { id = id.trim(); }
	Cluster[] o = getCluster();
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
	public Cluster newCluster() {
		return new Cluster();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "clusters";
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
		str.append("Cluster["+this.sizeCluster()+"]");	// NOI18N
		for(int i=0; i<this.sizeCluster(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getCluster(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(CLUSTER, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Clusters\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

