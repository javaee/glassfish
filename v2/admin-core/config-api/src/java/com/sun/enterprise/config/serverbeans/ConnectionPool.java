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
 *	This generated bean class ConnectionPool matches the DTD element connection-pool
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

public class ConnectionPool extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public ConnectionPool() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ConnectionPool(int options)
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
	* Getter for QueueSizeInBytes of the Element connection-pool
	* @return  the QueueSizeInBytes of the Element connection-pool
	*/
	public String getQueueSizeInBytes() {
		return getAttributeValue(ServerTags.QUEUE_SIZE_IN_BYTES);
	}
	/**
	* Modify  the QueueSizeInBytes of the Element connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setQueueSizeInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.QUEUE_SIZE_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the QueueSizeInBytes of the Element connection-pool
	* @param v the new value
	*/
	public void setQueueSizeInBytes(String v) {
		setAttributeValue(ServerTags.QUEUE_SIZE_IN_BYTES, v);
	}
	/**
	* Get the default value of QueueSizeInBytes from dtd
	*/
	public static String getDefaultQueueSizeInBytes() {
		return "4096".trim();
	}
	/**
	* Getter for MaxPendingCount of the Element connection-pool
	* @return  the MaxPendingCount of the Element connection-pool
	*/
	public String getMaxPendingCount() {
		return getAttributeValue(ServerTags.MAX_PENDING_COUNT);
	}
	/**
	* Modify  the MaxPendingCount of the Element connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxPendingCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_PENDING_COUNT, v, overwrite);
	}
	/**
	* Modify  the MaxPendingCount of the Element connection-pool
	* @param v the new value
	*/
	public void setMaxPendingCount(String v) {
		setAttributeValue(ServerTags.MAX_PENDING_COUNT, v);
	}
	/**
	* Get the default value of MaxPendingCount from dtd
	*/
	public static String getDefaultMaxPendingCount() {
		return "4096".trim();
	}
	/**
	* Getter for ReceiveBufferSizeInBytes of the Element connection-pool
	* @return  the ReceiveBufferSizeInBytes of the Element connection-pool
	*/
	public String getReceiveBufferSizeInBytes() {
		return getAttributeValue(ServerTags.RECEIVE_BUFFER_SIZE_IN_BYTES);
	}
	/**
	* Modify  the ReceiveBufferSizeInBytes of the Element connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setReceiveBufferSizeInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RECEIVE_BUFFER_SIZE_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the ReceiveBufferSizeInBytes of the Element connection-pool
	* @param v the new value
	*/
	public void setReceiveBufferSizeInBytes(String v) {
		setAttributeValue(ServerTags.RECEIVE_BUFFER_SIZE_IN_BYTES, v);
	}
	/**
	* Get the default value of ReceiveBufferSizeInBytes from dtd
	*/
	public static String getDefaultReceiveBufferSizeInBytes() {
		return "4096".trim();
	}
	/**
	* Getter for SendBufferSizeInBytes of the Element connection-pool
	* @return  the SendBufferSizeInBytes of the Element connection-pool
	*/
	public String getSendBufferSizeInBytes() {
		return getAttributeValue(ServerTags.SEND_BUFFER_SIZE_IN_BYTES);
	}
	/**
	* Modify  the SendBufferSizeInBytes of the Element connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSendBufferSizeInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SEND_BUFFER_SIZE_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the SendBufferSizeInBytes of the Element connection-pool
	* @param v the new value
	*/
	public void setSendBufferSizeInBytes(String v) {
		setAttributeValue(ServerTags.SEND_BUFFER_SIZE_IN_BYTES, v);
	}
	/**
	* Get the default value of SendBufferSizeInBytes from dtd
	*/
	public static String getDefaultSendBufferSizeInBytes() {
		return "8192".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "connection-pool";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.QUEUE_SIZE_IN_BYTES)) return "4096".trim();
		if(attr.equals(ServerTags.MAX_PENDING_COUNT)) return "4096".trim();
		if(attr.equals(ServerTags.RECEIVE_BUFFER_SIZE_IN_BYTES)) return "4096".trim();
		if(attr.equals(ServerTags.SEND_BUFFER_SIZE_IN_BYTES)) return "8192".trim();
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
		str.append("ConnectionPool\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

