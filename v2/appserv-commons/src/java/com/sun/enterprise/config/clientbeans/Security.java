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
 *	This generated bean class Security matches the DTD element security
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

public class Security extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String SSL = "Ssl";
	static public final String CERT_DB = "CertDb";

	public Security() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Security(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("ssl", SSL, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Ssl.class);
		this.createAttribute(SSL, "cert-nickname", "CertNickname", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SSL, "ssl2-enabled", "Ssl2Enabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(SSL, "ssl2-ciphers", "Ssl2Ciphers", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SSL, "ssl3-enabled", "Ssl3Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(SSL, "ssl3-tls-ciphers", "Ssl3TlsCiphers", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(SSL, "tls-enabled", "TlsEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(SSL, "tls-rollback-enabled", "TlsRollbackEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("cert-db", CERT_DB, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			CertDb.class);
		this.createAttribute(CERT_DB, "path", "Path", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CERT_DB, "password", "Password", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setSsl(Ssl value) {
		this.setValue(SSL, value);
	}

	// Get Method
	public Ssl getSsl() {
		return (Ssl)this.getValue(SSL);
	}

	// This attribute is mandatory
	public void setCertDb(CertDb value) {
		this.setValue(CERT_DB, value);
	}

	// Get Method
	public CertDb getCertDb() {
		return (CertDb)this.getValue(CERT_DB);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Ssl newSsl() {
		return new Ssl();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public CertDb newCertDb() {
		return new CertDb();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "security";
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
		str.append("Ssl");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSsl();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SSL, 0, str, indent);

		str.append(indent);
		str.append("CertDb");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getCertDb();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CERT_DB, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Security\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

