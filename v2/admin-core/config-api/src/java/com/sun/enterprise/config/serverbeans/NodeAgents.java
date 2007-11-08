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
 *	This generated bean class NodeAgents matches the DTD element node-agents
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

public class NodeAgents extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String NODE_AGENT = "NodeAgent";

	public NodeAgents() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public NodeAgents(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("node-agent", NODE_AGENT, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			NodeAgent.class);
		this.createAttribute(NODE_AGENT, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(NODE_AGENT, "system-jmx-connector-name", "SystemJmxConnectorName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(NODE_AGENT, "start-servers-in-startup", "StartServersInStartup", 
						AttrProp.CDATA,
						null, "true");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public NodeAgent getNodeAgent(int index) {
		return (NodeAgent)this.getValue(NODE_AGENT, index);
	}

	// This attribute is an array, possibly empty
	public void setNodeAgent(NodeAgent[] value) {
		this.setValue(NODE_AGENT, value);
	}

	// Getter Method
	public NodeAgent[] getNodeAgent() {
		return (NodeAgent[])this.getValues(NODE_AGENT);
	}

	// Return the number of properties
	public int sizeNodeAgent() {
		return this.size(NODE_AGENT);
	}

	// Add a new element returning its index in the list
	public int addNodeAgent(NodeAgent value)
			throws ConfigException{
		return addNodeAgent(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addNodeAgent(NodeAgent value, boolean overwrite)
			throws ConfigException{
		NodeAgent old = getNodeAgentByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(NodeAgents.class).getString("cannotAddDuplicate",  "NodeAgent"));
		}
		return this.addValue(NODE_AGENT, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeNodeAgent(NodeAgent value){
		return this.removeValue(NODE_AGENT, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeNodeAgent(NodeAgent value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(NODE_AGENT, value, overwrite);
	}

	public NodeAgent getNodeAgentByName(String id) {
	 if (null != id) { id = id.trim(); }
	NodeAgent[] o = getNodeAgent();
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
	public NodeAgent newNodeAgent() {
		return new NodeAgent();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "node-agents";
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
		str.append("NodeAgent["+this.sizeNodeAgent()+"]");	// NOI18N
		for(int i=0; i<this.sizeNodeAgent(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getNodeAgent(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(NODE_AGENT, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("NodeAgents\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

