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
 *	This generated bean class HttpProtocol matches the DTD element http-protocol
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

public class HttpProtocol extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public HttpProtocol() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public HttpProtocol(int options)
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
	* Getter for Version of the Element http-protocol
	* @return  the Version of the Element http-protocol
	*/
	public String getVersion() {
		return getAttributeValue(ServerTags.VERSION);
	}
	/**
	* Modify  the Version of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setVersion(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VERSION, v, overwrite);
	}
	/**
	* Modify  the Version of the Element http-protocol
	* @param v the new value
	*/
	public void setVersion(String v) {
		setAttributeValue(ServerTags.VERSION, v);
	}
	/**
	* Get the default value of Version from dtd
	*/
	public static String getDefaultVersion() {
		return "HTTP/1.1".trim();
	}
	/**
	* Getter for DnsLookupEnabled of the Element http-protocol
	* @return  the DnsLookupEnabled of the Element http-protocol
	*/
	public boolean isDnsLookupEnabled() {
		return toBoolean(getAttributeValue(ServerTags.DNS_LOOKUP_ENABLED));
	}
	/**
	* Modify  the DnsLookupEnabled of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDnsLookupEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DNS_LOOKUP_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the DnsLookupEnabled of the Element http-protocol
	* @param v the new value
	*/
	public void setDnsLookupEnabled(boolean v) {
		setAttributeValue(ServerTags.DNS_LOOKUP_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of DnsLookupEnabled from dtd
	*/
	public static String getDefaultDnsLookupEnabled() {
		return "false".trim();
	}
	/**
	* Getter for ForcedType of the Element http-protocol
	* @return  the ForcedType of the Element http-protocol
	*/
	public String getForcedType() {
		return getAttributeValue(ServerTags.FORCED_TYPE);
	}
	/**
	* Modify  the ForcedType of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setForcedType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FORCED_TYPE, v, overwrite);
	}
	/**
	* Modify  the ForcedType of the Element http-protocol
	* @param v the new value
	*/
	public void setForcedType(String v) {
		setAttributeValue(ServerTags.FORCED_TYPE, v);
	}
	/**
	* Get the default value of ForcedType from dtd
	*/
	public static String getDefaultForcedType() {
		return "text/html; charset=iso-8859-1".trim();
	}
	/**
	* Getter for DefaultType of the Element http-protocol
	* @return  the DefaultType of the Element http-protocol
	*/
	public String getDefaultType() {
		return getAttributeValue(ServerTags.DEFAULT_TYPE);
	}
	/**
	* Modify  the DefaultType of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_TYPE, v, overwrite);
	}
	/**
	* Modify  the DefaultType of the Element http-protocol
	* @param v the new value
	*/
	public void setDefaultType(String v) {
		setAttributeValue(ServerTags.DEFAULT_TYPE, v);
	}
	/**
	* Get the default value of DefaultType from dtd
	*/
	public static String getDefaultDefaultType() {
		return "text/html; charset=iso-8859-1".trim();
	}
	/**
	* Getter for ForcedResponseType of the Element http-protocol
	* @return  the ForcedResponseType of the Element http-protocol
	*/
	public String getForcedResponseType() {
		return getAttributeValue(ServerTags.FORCED_RESPONSE_TYPE);
	}
	/**
	* Modify  the ForcedResponseType of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setForcedResponseType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FORCED_RESPONSE_TYPE, v, overwrite);
	}
	/**
	* Modify  the ForcedResponseType of the Element http-protocol
	* @param v the new value
	*/
	public void setForcedResponseType(String v) {
		setAttributeValue(ServerTags.FORCED_RESPONSE_TYPE, v);
	}
	/**
	* Get the default value of ForcedResponseType from dtd
	*/
	public static String getDefaultForcedResponseType() {
		return "AttributeDeprecated".trim();
	}
	/**
	* Getter for DefaultResponseType of the Element http-protocol
	* @return  the DefaultResponseType of the Element http-protocol
	*/
	public String getDefaultResponseType() {
		return getAttributeValue(ServerTags.DEFAULT_RESPONSE_TYPE);
	}
	/**
	* Modify  the DefaultResponseType of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultResponseType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEFAULT_RESPONSE_TYPE, v, overwrite);
	}
	/**
	* Modify  the DefaultResponseType of the Element http-protocol
	* @param v the new value
	*/
	public void setDefaultResponseType(String v) {
		setAttributeValue(ServerTags.DEFAULT_RESPONSE_TYPE, v);
	}
	/**
	* Get the default value of DefaultResponseType from dtd
	*/
	public static String getDefaultDefaultResponseType() {
		return "AttributeDeprecated".trim();
	}
	/**
	* Getter for SslEnabled of the Element http-protocol
	* @return  the SslEnabled of the Element http-protocol
	*/
	public boolean isSslEnabled() {
		return toBoolean(getAttributeValue(ServerTags.SSL_ENABLED));
	}
	/**
	* Modify  the SslEnabled of the Element http-protocol
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSslEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SSL_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the SslEnabled of the Element http-protocol
	* @param v the new value
	*/
	public void setSslEnabled(boolean v) {
		setAttributeValue(ServerTags.SSL_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of SslEnabled from dtd
	*/
	public static String getDefaultSslEnabled() {
		return "true".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "http-protocol";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.VERSION)) return "HTTP/1.1".trim();
		if(attr.equals(ServerTags.DNS_LOOKUP_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.FORCED_TYPE)) return "text/html; charset=iso-8859-1".trim();
		if(attr.equals(ServerTags.DEFAULT_TYPE)) return "text/html; charset=iso-8859-1".trim();
		if(attr.equals(ServerTags.FORCED_RESPONSE_TYPE)) return "AttributeDeprecated".trim();
		if(attr.equals(ServerTags.DEFAULT_RESPONSE_TYPE)) return "AttributeDeprecated".trim();
		if(attr.equals(ServerTags.SSL_ENABLED)) return "true".trim();
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
		str.append("HttpProtocol\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

