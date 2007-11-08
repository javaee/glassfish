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
 *	This generated bean class ManagementRules matches the DTD element management-rules
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

public class ManagementRules extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String MANAGEMENT_RULE = "ManagementRule";

	public ManagementRules() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ManagementRules(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("management-rule", MANAGEMENT_RULE, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ManagementRule.class);
		this.createAttribute(MANAGEMENT_RULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MANAGEMENT_RULE, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public ManagementRule getManagementRule(int index) {
		return (ManagementRule)this.getValue(MANAGEMENT_RULE, index);
	}

	// This attribute is an array, possibly empty
	public void setManagementRule(ManagementRule[] value) {
		this.setValue(MANAGEMENT_RULE, value);
	}

	// Getter Method
	public ManagementRule[] getManagementRule() {
		return (ManagementRule[])this.getValues(MANAGEMENT_RULE);
	}

	// Return the number of properties
	public int sizeManagementRule() {
		return this.size(MANAGEMENT_RULE);
	}

	// Add a new element returning its index in the list
	public int addManagementRule(ManagementRule value)
			throws ConfigException{
		return addManagementRule(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addManagementRule(ManagementRule value, boolean overwrite)
			throws ConfigException{
		ManagementRule old = getManagementRuleByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ManagementRules.class).getString("cannotAddDuplicate",  "ManagementRule"));
		}
		return this.addValue(MANAGEMENT_RULE, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeManagementRule(ManagementRule value){
		return this.removeValue(MANAGEMENT_RULE, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeManagementRule(ManagementRule value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(MANAGEMENT_RULE, value, overwrite);
	}

	public ManagementRule getManagementRuleByName(String id) {
	 if (null != id) { id = id.trim(); }
	ManagementRule[] o = getManagementRule();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for Enabled of the Element management-rules
	* @return  the Enabled of the Element management-rules
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element management-rules
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element management-rules
	* @param v the new value
	*/
	public void setEnabled(boolean v) {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Enabled from dtd
	*/
	public static String getDefaultEnabled() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ManagementRule newManagementRule() {
		return new ManagementRule();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "management-rules";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
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
		str.append("ManagementRule["+this.sizeManagementRule()+"]");	// NOI18N
		for(int i=0; i<this.sizeManagementRule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getManagementRule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MANAGEMENT_RULE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ManagementRules\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

