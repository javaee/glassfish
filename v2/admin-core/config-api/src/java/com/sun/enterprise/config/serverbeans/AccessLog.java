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
 *	This generated bean class AccessLog matches the DTD element access-log
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

public class AccessLog extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public AccessLog() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public AccessLog(int options)
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
	* Getter for Format of the Element access-log
	* @return  the Format of the Element access-log
	*/
	public String getFormat() {
		return getAttributeValue(ServerTags.FORMAT);
	}
	/**
	* Modify  the Format of the Element access-log
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFormat(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FORMAT, v, overwrite);
	}
	/**
	* Modify  the Format of the Element access-log
	* @param v the new value
	*/
	public void setFormat(String v) {
		setAttributeValue(ServerTags.FORMAT, v);
	}
	/**
	* Get the default value of Format from dtd
	*/
	public static String getDefaultFormat() {
		return "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%".trim();
	}
	/**
	* Getter for RotationPolicy of the Element access-log
	* @return  the RotationPolicy of the Element access-log
	*/
	public String getRotationPolicy() {
		return getAttributeValue(ServerTags.ROTATION_POLICY);
	}
	/**
	* Modify  the RotationPolicy of the Element access-log
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRotationPolicy(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROTATION_POLICY, v, overwrite);
	}
	/**
	* Modify  the RotationPolicy of the Element access-log
	* @param v the new value
	*/
	public void setRotationPolicy(String v) {
		setAttributeValue(ServerTags.ROTATION_POLICY, v);
	}
	/**
	* Get the default value of RotationPolicy from dtd
	*/
	public static String getDefaultRotationPolicy() {
		return "time".trim();
	}
	/**
	* Getter for RotationIntervalInMinutes of the Element access-log
	* @return  the RotationIntervalInMinutes of the Element access-log
	*/
	public String getRotationIntervalInMinutes() {
		return getAttributeValue(ServerTags.ROTATION_INTERVAL_IN_MINUTES);
	}
	/**
	* Modify  the RotationIntervalInMinutes of the Element access-log
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRotationIntervalInMinutes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROTATION_INTERVAL_IN_MINUTES, v, overwrite);
	}
	/**
	* Modify  the RotationIntervalInMinutes of the Element access-log
	* @param v the new value
	*/
	public void setRotationIntervalInMinutes(String v) {
		setAttributeValue(ServerTags.ROTATION_INTERVAL_IN_MINUTES, v);
	}
	/**
	* Get the default value of RotationIntervalInMinutes from dtd
	*/
	public static String getDefaultRotationIntervalInMinutes() {
		return "1440".trim();
	}
	/**
	* Getter for RotationSuffix of the Element access-log
	* @return  the RotationSuffix of the Element access-log
	*/
	public String getRotationSuffix() {
		return getAttributeValue(ServerTags.ROTATION_SUFFIX);
	}
	/**
	* Modify  the RotationSuffix of the Element access-log
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRotationSuffix(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROTATION_SUFFIX, v, overwrite);
	}
	/**
	* Modify  the RotationSuffix of the Element access-log
	* @param v the new value
	*/
	public void setRotationSuffix(String v) {
		setAttributeValue(ServerTags.ROTATION_SUFFIX, v);
	}
	/**
	* Get the default value of RotationSuffix from dtd
	*/
	public static String getDefaultRotationSuffix() {
		return "yyyyMMdd-HH'h'mm'm'ss's'".trim();
	}
	/**
	* Getter for RotationEnabled of the Element access-log
	* @return  the RotationEnabled of the Element access-log
	*/
	public boolean isRotationEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ROTATION_ENABLED));
	}
	/**
	* Modify  the RotationEnabled of the Element access-log
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRotationEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ROTATION_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the RotationEnabled of the Element access-log
	* @param v the new value
	*/
	public void setRotationEnabled(boolean v) {
		setAttributeValue(ServerTags.ROTATION_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of RotationEnabled from dtd
	*/
	public static String getDefaultRotationEnabled() {
		return "true".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "access-log";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.FORMAT)) return "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%".trim();
		if(attr.equals(ServerTags.ROTATION_POLICY)) return "time".trim();
		if(attr.equals(ServerTags.ROTATION_INTERVAL_IN_MINUTES)) return "1440".trim();
		if(attr.equals(ServerTags.ROTATION_SUFFIX)) return "yyyyMMdd-HH'h'mm'm'ss's'".trim();
		if(attr.equals(ServerTags.ROTATION_ENABLED)) return "true".trim();
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
		str.append("AccessLog\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

