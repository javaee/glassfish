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
 *	This generated bean class TransformationRule matches the DTD element transformation-rule
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

public class TransformationRule extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public TransformationRule() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public TransformationRule(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(0);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	/**
	* Getter for Name of the Element transformation-rule
	* @return  the Name of the Element transformation-rule
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element transformation-rule
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element transformation-rule
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for Enabled of the Element transformation-rule
	* @return  the Enabled of the Element transformation-rule
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element transformation-rule
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element transformation-rule
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
	* Getter for ApplyTo of the Element transformation-rule
	* @return  the ApplyTo of the Element transformation-rule
	*/
	public String getApplyTo() {
		return getAttributeValue(ServerTags.APPLY_TO);
	}
	/**
	* Modify  the ApplyTo of the Element transformation-rule
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setApplyTo(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.APPLY_TO, v, overwrite);
	}
	/**
	* Modify  the ApplyTo of the Element transformation-rule
	* @param v the new value
	*/
	public void setApplyTo(String v) {
		setAttributeValue(ServerTags.APPLY_TO, v);
	}
	/**
	* Get the default value of ApplyTo from dtd
	*/
	public static String getDefaultApplyTo() {
		return "request".trim();
	}
	/**
	* Getter for RuleFileLocation of the Element transformation-rule
	* @return  the RuleFileLocation of the Element transformation-rule
	*/
	public String getRuleFileLocation() {
		return getAttributeValue(ServerTags.RULE_FILE_LOCATION);
	}
	/**
	* Modify  the RuleFileLocation of the Element transformation-rule
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRuleFileLocation(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RULE_FILE_LOCATION, v, overwrite);
	}
	/**
	* Modify  the RuleFileLocation of the Element transformation-rule
	* @param v the new value
	*/
	public void setRuleFileLocation(String v) {
		setAttributeValue(ServerTags.RULE_FILE_LOCATION, v);
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "transformation-rule" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.APPLY_TO)) return "request".trim();
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
	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("TransformationRule\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

