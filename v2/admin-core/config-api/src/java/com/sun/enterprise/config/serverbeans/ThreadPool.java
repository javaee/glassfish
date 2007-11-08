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
 *	This generated bean class ThreadPool matches the DTD element thread-pool
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

public class ThreadPool extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public ThreadPool() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ThreadPool(int options)
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
	* Getter for ThreadPoolId of the Element thread-pool
	* @return  the ThreadPoolId of the Element thread-pool
	*/
	public String getThreadPoolId() {
		return getAttributeValue(ServerTags.THREAD_POOL_ID);
	}
	/**
	* Modify  the ThreadPoolId of the Element thread-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setThreadPoolId(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.THREAD_POOL_ID, v, overwrite);
	}
	/**
	* Modify  the ThreadPoolId of the Element thread-pool
	* @param v the new value
	*/
	public void setThreadPoolId(String v) {
		setAttributeValue(ServerTags.THREAD_POOL_ID, v);
	}
	/**
	* Getter for MinThreadPoolSize of the Element thread-pool
	* @return  the MinThreadPoolSize of the Element thread-pool
	*/
	public String getMinThreadPoolSize() {
		return getAttributeValue(ServerTags.MIN_THREAD_POOL_SIZE);
	}
	/**
	* Modify  the MinThreadPoolSize of the Element thread-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMinThreadPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MIN_THREAD_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the MinThreadPoolSize of the Element thread-pool
	* @param v the new value
	*/
	public void setMinThreadPoolSize(String v) {
		setAttributeValue(ServerTags.MIN_THREAD_POOL_SIZE, v);
	}
	/**
	* Get the default value of MinThreadPoolSize from dtd
	*/
	public static String getDefaultMinThreadPoolSize() {
		return "0".trim();
	}
	/**
	* Getter for MaxThreadPoolSize of the Element thread-pool
	* @return  the MaxThreadPoolSize of the Element thread-pool
	*/
	public String getMaxThreadPoolSize() {
		return getAttributeValue(ServerTags.MAX_THREAD_POOL_SIZE);
	}
	/**
	* Modify  the MaxThreadPoolSize of the Element thread-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxThreadPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_THREAD_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxThreadPoolSize of the Element thread-pool
	* @param v the new value
	*/
	public void setMaxThreadPoolSize(String v) {
		setAttributeValue(ServerTags.MAX_THREAD_POOL_SIZE, v);
	}
	/**
	* Get the default value of MaxThreadPoolSize from dtd
	*/
	public static String getDefaultMaxThreadPoolSize() {
		return "200".trim();
	}
	/**
	* Getter for IdleThreadTimeoutInSeconds of the Element thread-pool
	* @return  the IdleThreadTimeoutInSeconds of the Element thread-pool
	*/
	public String getIdleThreadTimeoutInSeconds() {
		return getAttributeValue(ServerTags.IDLE_THREAD_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the IdleThreadTimeoutInSeconds of the Element thread-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setIdleThreadTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.IDLE_THREAD_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the IdleThreadTimeoutInSeconds of the Element thread-pool
	* @param v the new value
	*/
	public void setIdleThreadTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.IDLE_THREAD_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of IdleThreadTimeoutInSeconds from dtd
	*/
	public static String getDefaultIdleThreadTimeoutInSeconds() {
		return "120".trim();
	}
	/**
	* Getter for NumWorkQueues of the Element thread-pool
	* @return  the NumWorkQueues of the Element thread-pool
	*/
	public String getNumWorkQueues() {
		return getAttributeValue(ServerTags.NUM_WORK_QUEUES);
	}
	/**
	* Modify  the NumWorkQueues of the Element thread-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNumWorkQueues(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NUM_WORK_QUEUES, v, overwrite);
	}
	/**
	* Modify  the NumWorkQueues of the Element thread-pool
	* @param v the new value
	*/
	public void setNumWorkQueues(String v) {
		setAttributeValue(ServerTags.NUM_WORK_QUEUES, v);
	}
	/**
	* Get the default value of NumWorkQueues from dtd
	*/
	public static String getDefaultNumWorkQueues() {
		return "1".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "thread-pool" + (canHaveSiblings() ? "[@thread-pool-id='" + getAttributeValue("thread-pool-id") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.MIN_THREAD_POOL_SIZE)) return "0".trim();
		if(attr.equals(ServerTags.MAX_THREAD_POOL_SIZE)) return "200".trim();
		if(attr.equals(ServerTags.IDLE_THREAD_TIMEOUT_IN_SECONDS)) return "120".trim();
		if(attr.equals(ServerTags.NUM_WORK_QUEUES)) return "1".trim();
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
		str.append("ThreadPool\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

