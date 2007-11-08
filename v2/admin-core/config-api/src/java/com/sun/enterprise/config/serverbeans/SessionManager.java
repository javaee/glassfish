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
 *	This generated bean class SessionManager matches the DTD element session-manager
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

public class SessionManager extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String MANAGER_PROPERTIES = "ManagerProperties";
	static public final String STORE_PROPERTIES = "StoreProperties";

	public SessionManager() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public SessionManager(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("manager-properties", MANAGER_PROPERTIES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ManagerProperties.class);
		this.createAttribute(MANAGER_PROPERTIES, "session-file-name", "SessionFileName", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANAGER_PROPERTIES, "reap-interval-in-seconds", "ReapIntervalInSeconds", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANAGER_PROPERTIES, "max-sessions", "MaxSessions", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MANAGER_PROPERTIES, "session-id-generator-classname", "SessionIdGeneratorClassname", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("store-properties", STORE_PROPERTIES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			StoreProperties.class);
		this.createAttribute(STORE_PROPERTIES, "directory", "Directory", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(STORE_PROPERTIES, "reap-interval-in-seconds", "ReapIntervalInSeconds", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setManagerProperties(ManagerProperties value) {
		this.setValue(MANAGER_PROPERTIES, value);
	}

	// Get Method
	public ManagerProperties getManagerProperties() {
		return (ManagerProperties)this.getValue(MANAGER_PROPERTIES);
	}

	// This attribute is optional
	public void setStoreProperties(StoreProperties value) {
		this.setValue(STORE_PROPERTIES, value);
	}

	// Get Method
	public StoreProperties getStoreProperties() {
		return (StoreProperties)this.getValue(STORE_PROPERTIES);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ManagerProperties newManagerProperties() {
		return new ManagerProperties();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public StoreProperties newStoreProperties() {
		return new StoreProperties();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "session-manager";
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
		str.append("ManagerProperties");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getManagerProperties();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(MANAGER_PROPERTIES, 0, str, indent);

		str.append(indent);
		str.append("StoreProperties");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getStoreProperties();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(STORE_PROPERTIES, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("SessionManager\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

