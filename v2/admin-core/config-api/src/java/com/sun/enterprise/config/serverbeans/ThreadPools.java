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
 *	This generated bean class ThreadPools matches the DTD element thread-pools
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

public class ThreadPools extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String THREAD_POOL = "ThreadPool";

	public ThreadPools() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ThreadPools(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("thread-pool", THREAD_POOL, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ThreadPool.class);
		this.createAttribute(THREAD_POOL, "thread-pool-id", "ThreadPoolId", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(THREAD_POOL, "min-thread-pool-size", "MinThreadPoolSize", 
						AttrProp.CDATA,
						null, "0");
		this.createAttribute(THREAD_POOL, "max-thread-pool-size", "MaxThreadPoolSize", 
						AttrProp.CDATA,
						null, "200");
		this.createAttribute(THREAD_POOL, "idle-thread-timeout-in-seconds", "IdleThreadTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "120");
		this.createAttribute(THREAD_POOL, "num-work-queues", "NumWorkQueues", 
						AttrProp.CDATA,
						null, "1");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// Get Method
	public ThreadPool getThreadPool(int index) {
		return (ThreadPool)this.getValue(THREAD_POOL, index);
	}

	// This attribute is an array containing at least one element
	public void setThreadPool(ThreadPool[] value) {
		this.setValue(THREAD_POOL, value);
	}

	// Getter Method
	public ThreadPool[] getThreadPool() {
		return (ThreadPool[])this.getValues(THREAD_POOL);
	}

	// Return the number of properties
	public int sizeThreadPool() {
		return this.size(THREAD_POOL);
	}

	// Add a new element returning its index in the list
	public int addThreadPool(ThreadPool value)
			throws ConfigException{
		return addThreadPool(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addThreadPool(ThreadPool value, boolean overwrite)
			throws ConfigException{
		ThreadPool old = getThreadPoolByThreadPoolId(value.getThreadPoolId());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ThreadPools.class).getString("cannotAddDuplicate",  "ThreadPool"));
		}
		return this.addValue(THREAD_POOL, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeThreadPool(ThreadPool value){
		return this.removeValue(THREAD_POOL, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeThreadPool(ThreadPool value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(THREAD_POOL, value, overwrite);
	}

	public ThreadPool getThreadPoolByThreadPoolId(String id) {
	 if (null != id) { id = id.trim(); }
	ThreadPool[] o = getThreadPool();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.THREAD_POOL_ID)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ThreadPool newThreadPool() {
		return new ThreadPool();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "thread-pools";
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
		str.append("ThreadPool["+this.sizeThreadPool()+"]");	// NOI18N
		for(int i=0; i<this.sizeThreadPool(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getThreadPool(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(THREAD_POOL, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ThreadPools\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

