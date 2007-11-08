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
 *	This generated bean class Ssl matches the DTD element ssl
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

public class Ssl extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public Ssl() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Ssl(int options)
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
	* Getter for CertNickname of the Element ssl
	* @return  the CertNickname of the Element ssl
	*/
	public String getCertNickname() {
		return getAttributeValue(ServerTags.CERT_NICKNAME);
	}
	/**
	* Modify  the CertNickname of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCertNickname(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CERT_NICKNAME, v, overwrite);
	}
	/**
	* Modify  the CertNickname of the Element ssl
	* @param v the new value
	*/
	public void setCertNickname(String v) {
		setAttributeValue(ServerTags.CERT_NICKNAME, v);
	}
	/**
	* Getter for Ssl2Enabled of the Element ssl
	* @return  the Ssl2Enabled of the Element ssl
	*/
	public boolean isSsl2Enabled() {
		return toBoolean(getAttributeValue(ServerTags.SSL2_ENABLED));
	}
	/**
	* Modify  the Ssl2Enabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl2Enabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SSL2_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Ssl2Enabled of the Element ssl
	* @param v the new value
	*/
	public void setSsl2Enabled(boolean v) {
		setAttributeValue(ServerTags.SSL2_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Ssl2Enabled from dtd
	*/
	public static String getDefaultSsl2Enabled() {
		return "false".trim();
	}
	/**
	* Getter for Ssl2Ciphers of the Element ssl
	* @return  the Ssl2Ciphers of the Element ssl
	*/
	public String getSsl2Ciphers() {
			return getAttributeValue(ServerTags.SSL2_CIPHERS);
	}
	/**
	* Modify  the Ssl2Ciphers of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl2Ciphers(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SSL2_CIPHERS, v, overwrite);
	}
	/**
	* Modify  the Ssl2Ciphers of the Element ssl
	* @param v the new value
	*/
	public void setSsl2Ciphers(String v) {
		setAttributeValue(ServerTags.SSL2_CIPHERS, v);
	}
	/**
	* Getter for Ssl3Enabled of the Element ssl
	* @return  the Ssl3Enabled of the Element ssl
	*/
	public boolean isSsl3Enabled() {
		return toBoolean(getAttributeValue(ServerTags.SSL3_ENABLED));
	}
	/**
	* Modify  the Ssl3Enabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl3Enabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SSL3_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Ssl3Enabled of the Element ssl
	* @param v the new value
	*/
	public void setSsl3Enabled(boolean v) {
		setAttributeValue(ServerTags.SSL3_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Ssl3Enabled from dtd
	*/
	public static String getDefaultSsl3Enabled() {
		return "true".trim();
	}
	/**
	* Getter for Ssl3TlsCiphers of the Element ssl
	* @return  the Ssl3TlsCiphers of the Element ssl
	*/
	public String getSsl3TlsCiphers() {
			return getAttributeValue(ServerTags.SSL3_TLS_CIPHERS);
	}
	/**
	* Modify  the Ssl3TlsCiphers of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl3TlsCiphers(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SSL3_TLS_CIPHERS, v, overwrite);
	}
	/**
	* Modify  the Ssl3TlsCiphers of the Element ssl
	* @param v the new value
	*/
	public void setSsl3TlsCiphers(String v) {
		setAttributeValue(ServerTags.SSL3_TLS_CIPHERS, v);
	}
	/**
	* Getter for TlsEnabled of the Element ssl
	* @return  the TlsEnabled of the Element ssl
	*/
	public boolean isTlsEnabled() {
		return toBoolean(getAttributeValue(ServerTags.TLS_ENABLED));
	}
	/**
	* Modify  the TlsEnabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTlsEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TLS_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the TlsEnabled of the Element ssl
	* @param v the new value
	*/
	public void setTlsEnabled(boolean v) {
		setAttributeValue(ServerTags.TLS_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of TlsEnabled from dtd
	*/
	public static String getDefaultTlsEnabled() {
		return "true".trim();
	}
	/**
	* Getter for TlsRollbackEnabled of the Element ssl
	* @return  the TlsRollbackEnabled of the Element ssl
	*/
	public boolean isTlsRollbackEnabled() {
		return toBoolean(getAttributeValue(ServerTags.TLS_ROLLBACK_ENABLED));
	}
	/**
	* Modify  the TlsRollbackEnabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTlsRollbackEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TLS_ROLLBACK_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the TlsRollbackEnabled of the Element ssl
	* @param v the new value
	*/
	public void setTlsRollbackEnabled(boolean v) {
		setAttributeValue(ServerTags.TLS_ROLLBACK_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of TlsRollbackEnabled from dtd
	*/
	public static String getDefaultTlsRollbackEnabled() {
		return "true".trim();
	}
	/**
	* Getter for ClientAuthEnabled of the Element ssl
	* @return  the ClientAuthEnabled of the Element ssl
	*/
	public boolean isClientAuthEnabled() {
		return toBoolean(getAttributeValue(ServerTags.CLIENT_AUTH_ENABLED));
	}
	/**
	* Modify  the ClientAuthEnabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClientAuthEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLIENT_AUTH_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ClientAuthEnabled of the Element ssl
	* @param v the new value
	*/
	public void setClientAuthEnabled(boolean v) {
		setAttributeValue(ServerTags.CLIENT_AUTH_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of ClientAuthEnabled from dtd
	*/
	public static String getDefaultClientAuthEnabled() {
		return "false".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "ssl";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.SSL2_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.SSL3_ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.TLS_ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.TLS_ROLLBACK_ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.CLIENT_AUTH_ENABLED)) return "false".trim();
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
		str.append("Ssl\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

