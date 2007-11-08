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
 *	This generated bean class KeepAlive matches the DTD element keep-alive
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

public class KeepAlive extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public KeepAlive() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public KeepAlive(int options)
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
	* Getter for ThreadCount of the Element keep-alive
	* @return  the ThreadCount of the Element keep-alive
	*/
	public String getThreadCount() {
		return getAttributeValue(ServerTags.THREAD_COUNT);
	}
	/**
	* Modify  the ThreadCount of the Element keep-alive
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setThreadCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.THREAD_COUNT, v, overwrite);
	}
	/**
	* Modify  the ThreadCount of the Element keep-alive
	* @param v the new value
	*/
	public void setThreadCount(String v) {
		setAttributeValue(ServerTags.THREAD_COUNT, v);
	}
	/**
	* Get the default value of ThreadCount from dtd
	*/
	public static String getDefaultThreadCount() {
		return "1".trim();
	}
	/**
	* Getter for MaxConnections of the Element keep-alive
	* @return  the MaxConnections of the Element keep-alive
	*/
	public String getMaxConnections() {
		return getAttributeValue(ServerTags.MAX_CONNECTIONS);
	}
	/**
	* Modify  the MaxConnections of the Element keep-alive
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxConnections(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_CONNECTIONS, v, overwrite);
	}
	/**
	* Modify  the MaxConnections of the Element keep-alive
	* @param v the new value
	*/
	public void setMaxConnections(String v) {
		setAttributeValue(ServerTags.MAX_CONNECTIONS, v);
	}
	/**
	* Get the default value of MaxConnections from dtd
	*/
	public static String getDefaultMaxConnections() {
		return "256".trim();
	}
	/**
	* Getter for TimeoutInSeconds of the Element keep-alive
	* @return  the TimeoutInSeconds of the Element keep-alive
	*/
	public String getTimeoutInSeconds() {
		return getAttributeValue(ServerTags.TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the TimeoutInSeconds of the Element keep-alive
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the TimeoutInSeconds of the Element keep-alive
	* @param v the new value
	*/
	public void setTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of TimeoutInSeconds from dtd
	*/
	public static String getDefaultTimeoutInSeconds() {
		return "30".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "keep-alive";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.THREAD_COUNT)) return "1".trim();
		if(attr.equals(ServerTags.MAX_CONNECTIONS)) return "256".trim();
		if(attr.equals(ServerTags.TIMEOUT_IN_SECONDS)) return "30".trim();
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
		str.append("KeepAlive\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

