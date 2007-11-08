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
 *	This generated bean class HealthChecker matches the DTD element health-checker
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

public class HealthChecker extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public HealthChecker() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public HealthChecker(int options)
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
	* Getter for Url of the Element health-checker
	* @return  the Url of the Element health-checker
	*/
	public String getUrl() {
		return getAttributeValue(ServerTags.URL);
	}
	/**
	* Modify  the Url of the Element health-checker
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setUrl(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.URL, v, overwrite);
	}
	/**
	* Modify  the Url of the Element health-checker
	* @param v the new value
	*/
	public void setUrl(String v) {
		setAttributeValue(ServerTags.URL, v);
	}
	/**
	* Get the default value of Url from dtd
	*/
	public static String getDefaultUrl() {
		return "/".trim();
	}
	/**
	* Getter for IntervalInSeconds of the Element health-checker
	* @return  the IntervalInSeconds of the Element health-checker
	*/
	public String getIntervalInSeconds() {
		return getAttributeValue(ServerTags.INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the IntervalInSeconds of the Element health-checker
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the IntervalInSeconds of the Element health-checker
	* @param v the new value
	*/
	public void setIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of IntervalInSeconds from dtd
	*/
	public static String getDefaultIntervalInSeconds() {
		return "30".trim();
	}
	/**
	* Getter for TimeoutInSeconds of the Element health-checker
	* @return  the TimeoutInSeconds of the Element health-checker
	*/
	public String getTimeoutInSeconds() {
		return getAttributeValue(ServerTags.TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the TimeoutInSeconds of the Element health-checker
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the TimeoutInSeconds of the Element health-checker
	* @param v the new value
	*/
	public void setTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of TimeoutInSeconds from dtd
	*/
	public static String getDefaultTimeoutInSeconds() {
		return "10".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "health-checker";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.URL)) return "/".trim();
		if(attr.equals(ServerTags.INTERVAL_IN_SECONDS)) return "30".trim();
		if(attr.equals(ServerTags.TIMEOUT_IN_SECONDS)) return "10".trim();
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
		str.append("HealthChecker\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

