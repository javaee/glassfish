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
 *	This generated bean class ServerRef matches the DTD element server-ref
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

public class ServerRef extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String HEALTH_CHECKER = "HealthChecker";

	public ServerRef() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ServerRef(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("health-checker", HEALTH_CHECKER, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HealthChecker.class);
		this.createAttribute(HEALTH_CHECKER, "url", "Url", 
						AttrProp.CDATA,
						null, "/");
		this.createAttribute(HEALTH_CHECKER, "interval-in-seconds", "IntervalInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(HEALTH_CHECKER, "timeout-in-seconds", "TimeoutInSeconds", 
						AttrProp.CDATA,
						null, "10");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setHealthChecker(HealthChecker value) {
		this.setValue(HEALTH_CHECKER, value);
	}

	// Get Method
	public HealthChecker getHealthChecker() {
		return (HealthChecker)this.getValue(HEALTH_CHECKER);
	}

	/**
	* Getter for Ref of the Element server-ref
	* @return  the Ref of the Element server-ref
	*/
	public String getRef() {
		return getAttributeValue(ServerTags.REF);
	}
	/**
	* Modify  the Ref of the Element server-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRef(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.REF, v, overwrite);
	}
	/**
	* Modify  the Ref of the Element server-ref
	* @param v the new value
	*/
	public void setRef(String v) {
		setAttributeValue(ServerTags.REF, v);
	}
	/**
	* Getter for DisableTimeoutInMinutes of the Element server-ref
	* @return  the DisableTimeoutInMinutes of the Element server-ref
	*/
	public String getDisableTimeoutInMinutes() {
		return getAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES);
	}
	/**
	* Modify  the DisableTimeoutInMinutes of the Element server-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDisableTimeoutInMinutes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES, v, overwrite);
	}
	/**
	* Modify  the DisableTimeoutInMinutes of the Element server-ref
	* @param v the new value
	*/
	public void setDisableTimeoutInMinutes(String v) {
		setAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES, v);
	}
	/**
	* Get the default value of DisableTimeoutInMinutes from dtd
	*/
	public static String getDefaultDisableTimeoutInMinutes() {
		return "30".trim();
	}
	/**
	* Getter for LbEnabled of the Element server-ref
	* @return  the LbEnabled of the Element server-ref
	*/
	public boolean isLbEnabled() {
		return toBoolean(getAttributeValue(ServerTags.LB_ENABLED));
	}
	/**
	* Modify  the LbEnabled of the Element server-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLbEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LB_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the LbEnabled of the Element server-ref
	* @param v the new value
	*/
	public void setLbEnabled(boolean v) {
		setAttributeValue(ServerTags.LB_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of LbEnabled from dtd
	*/
	public static String getDefaultLbEnabled() {
		return "false".trim();
	}
	/**
	* Getter for Enabled of the Element server-ref
	* @return  the Enabled of the Element server-ref
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element server-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element server-ref
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
	public HealthChecker newHealthChecker() {
		return new HealthChecker();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "server-ref" + (canHaveSiblings() ? "[@ref='" + getAttributeValue("ref") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.DISABLE_TIMEOUT_IN_MINUTES)) return "30".trim();
		if(attr.equals(ServerTags.LB_ENABLED)) return "false".trim();
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
		str.append("HealthChecker");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getHealthChecker();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(HEALTH_CHECKER, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ServerRef\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

