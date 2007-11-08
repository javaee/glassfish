/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
 
/**
 *	This generated bean class Ssl matches the DTD element ssl
 *
 */

package com.sun.enterprise.config.clientbeans;

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
			return getAttributeValue(ClientTags.CERT_NICKNAME);
	}
	/**
	* Modify  the CertNickname of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCertNickname(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.CERT_NICKNAME, v, overwrite);
	}
	/**
	* Modify  the CertNickname of the Element ssl
	* @param v the new value
	*/
	public void setCertNickname(String v) {
		setAttributeValue(ClientTags.CERT_NICKNAME, v);
	}
	/**
	* Getter for Ssl2Enabled of the Element ssl
	* @return  the Ssl2Enabled of the Element ssl
	*/
	public boolean isSsl2Enabled() {
		return toBoolean(getAttributeValue(ClientTags.SSL2_ENABLED));
	}
	/**
	* Modify  the Ssl2Enabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl2Enabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.SSL2_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Ssl2Enabled of the Element ssl
	* @param v the new value
	*/
	public void setSsl2Enabled(boolean v) {
		setAttributeValue(ClientTags.SSL2_ENABLED, ""+(v==true));
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
			return getAttributeValue(ClientTags.SSL2_CIPHERS);
	}
	/**
	* Modify  the Ssl2Ciphers of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl2Ciphers(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.SSL2_CIPHERS, v, overwrite);
	}
	/**
	* Modify  the Ssl2Ciphers of the Element ssl
	* @param v the new value
	*/
	public void setSsl2Ciphers(String v) {
		setAttributeValue(ClientTags.SSL2_CIPHERS, v);
	}
	/**
	* Getter for Ssl3Enabled of the Element ssl
	* @return  the Ssl3Enabled of the Element ssl
	*/
	public boolean isSsl3Enabled() {
		return toBoolean(getAttributeValue(ClientTags.SSL3_ENABLED));
	}
	/**
	* Modify  the Ssl3Enabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl3Enabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.SSL3_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Ssl3Enabled of the Element ssl
	* @param v the new value
	*/
	public void setSsl3Enabled(boolean v) {
		setAttributeValue(ClientTags.SSL3_ENABLED, ""+(v==true));
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
			return getAttributeValue(ClientTags.SSL3_TLS_CIPHERS);
	}
	/**
	* Modify  the Ssl3TlsCiphers of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSsl3TlsCiphers(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.SSL3_TLS_CIPHERS, v, overwrite);
	}
	/**
	* Modify  the Ssl3TlsCiphers of the Element ssl
	* @param v the new value
	*/
	public void setSsl3TlsCiphers(String v) {
		setAttributeValue(ClientTags.SSL3_TLS_CIPHERS, v);
	}
	/**
	* Getter for TlsEnabled of the Element ssl
	* @return  the TlsEnabled of the Element ssl
	*/
	public boolean isTlsEnabled() {
		return toBoolean(getAttributeValue(ClientTags.TLS_ENABLED));
	}
	/**
	* Modify  the TlsEnabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTlsEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.TLS_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the TlsEnabled of the Element ssl
	* @param v the new value
	*/
	public void setTlsEnabled(boolean v) {
		setAttributeValue(ClientTags.TLS_ENABLED, ""+(v==true));
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
		return toBoolean(getAttributeValue(ClientTags.TLS_ROLLBACK_ENABLED));
	}
	/**
	* Modify  the TlsRollbackEnabled of the Element ssl
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTlsRollbackEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.TLS_ROLLBACK_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the TlsRollbackEnabled of the Element ssl
	* @param v the new value
	*/
	public void setTlsRollbackEnabled(boolean v) {
		setAttributeValue(ClientTags.TLS_ROLLBACK_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of TlsRollbackEnabled from dtd
	*/
	public static String getDefaultTlsRollbackEnabled() {
		return "true".trim();
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
		if(attr.equals(ClientTags.SSL2_ENABLED)) return "false".trim();
		if(attr.equals(ClientTags.SSL3_ENABLED)) return "true".trim();
		if(attr.equals(ClientTags.TLS_ENABLED)) return "true".trim();
		if(attr.equals(ClientTags.TLS_ROLLBACK_ENABLED)) return "true".trim();
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

