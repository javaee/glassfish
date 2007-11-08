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
 *	This generated bean class RequestProcessing matches the DTD element request-processing
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

public class RequestProcessing extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public RequestProcessing() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public RequestProcessing(int options)
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
	* Getter for ThreadCount of the Element request-processing
	* @return  the ThreadCount of the Element request-processing
	*/
	public String getThreadCount() {
		return getAttributeValue(ServerTags.THREAD_COUNT);
	}
	/**
	* Modify  the ThreadCount of the Element request-processing
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setThreadCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.THREAD_COUNT, v, overwrite);
	}
	/**
	* Modify  the ThreadCount of the Element request-processing
	* @param v the new value
	*/
	public void setThreadCount(String v) {
		setAttributeValue(ServerTags.THREAD_COUNT, v);
	}
	/**
	* Get the default value of ThreadCount from dtd
	*/
	public static String getDefaultThreadCount() {
		return "128".trim();
	}
	/**
	* Getter for InitialThreadCount of the Element request-processing
	* @return  the InitialThreadCount of the Element request-processing
	*/
	public String getInitialThreadCount() {
		return getAttributeValue(ServerTags.INITIAL_THREAD_COUNT);
	}
	/**
	* Modify  the InitialThreadCount of the Element request-processing
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setInitialThreadCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.INITIAL_THREAD_COUNT, v, overwrite);
	}
	/**
	* Modify  the InitialThreadCount of the Element request-processing
	* @param v the new value
	*/
	public void setInitialThreadCount(String v) {
		setAttributeValue(ServerTags.INITIAL_THREAD_COUNT, v);
	}
	/**
	* Get the default value of InitialThreadCount from dtd
	*/
	public static String getDefaultInitialThreadCount() {
		return "48".trim();
	}
	/**
	* Getter for ThreadIncrement of the Element request-processing
	* @return  the ThreadIncrement of the Element request-processing
	*/
	public String getThreadIncrement() {
		return getAttributeValue(ServerTags.THREAD_INCREMENT);
	}
	/**
	* Modify  the ThreadIncrement of the Element request-processing
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setThreadIncrement(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.THREAD_INCREMENT, v, overwrite);
	}
	/**
	* Modify  the ThreadIncrement of the Element request-processing
	* @param v the new value
	*/
	public void setThreadIncrement(String v) {
		setAttributeValue(ServerTags.THREAD_INCREMENT, v);
	}
	/**
	* Get the default value of ThreadIncrement from dtd
	*/
	public static String getDefaultThreadIncrement() {
		return "10".trim();
	}
	/**
	* Getter for RequestTimeoutInSeconds of the Element request-processing
	* @return  the RequestTimeoutInSeconds of the Element request-processing
	*/
	public String getRequestTimeoutInSeconds() {
		return getAttributeValue(ServerTags.REQUEST_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the RequestTimeoutInSeconds of the Element request-processing
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRequestTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.REQUEST_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the RequestTimeoutInSeconds of the Element request-processing
	* @param v the new value
	*/
	public void setRequestTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.REQUEST_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of RequestTimeoutInSeconds from dtd
	*/
	public static String getDefaultRequestTimeoutInSeconds() {
		return "30".trim();
	}
	/**
	* Getter for HeaderBufferLengthInBytes of the Element request-processing
	* @return  the HeaderBufferLengthInBytes of the Element request-processing
	*/
	public String getHeaderBufferLengthInBytes() {
		return getAttributeValue(ServerTags.HEADER_BUFFER_LENGTH_IN_BYTES);
	}
	/**
	* Modify  the HeaderBufferLengthInBytes of the Element request-processing
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHeaderBufferLengthInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HEADER_BUFFER_LENGTH_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the HeaderBufferLengthInBytes of the Element request-processing
	* @param v the new value
	*/
	public void setHeaderBufferLengthInBytes(String v) {
		setAttributeValue(ServerTags.HEADER_BUFFER_LENGTH_IN_BYTES, v);
	}
	/**
	* Get the default value of HeaderBufferLengthInBytes from dtd
	*/
	public static String getDefaultHeaderBufferLengthInBytes() {
		return "4096".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "request-processing";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.THREAD_COUNT)) return "128".trim();
		if(attr.equals(ServerTags.INITIAL_THREAD_COUNT)) return "48".trim();
		if(attr.equals(ServerTags.THREAD_INCREMENT)) return "10".trim();
		if(attr.equals(ServerTags.REQUEST_TIMEOUT_IN_SECONDS)) return "30".trim();
		if(attr.equals(ServerTags.HEADER_BUFFER_LENGTH_IN_BYTES)) return "4096".trim();
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
		str.append("RequestProcessing\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

