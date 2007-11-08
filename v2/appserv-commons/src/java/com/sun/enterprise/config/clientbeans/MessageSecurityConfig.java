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
 *	This generated bean class MessageSecurityConfig matches the DTD element message-security-config
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

public class MessageSecurityConfig extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String PROVIDER_CONFIG = "ProviderConfig";

	public MessageSecurityConfig() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public MessageSecurityConfig(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("provider-config", PROVIDER_CONFIG, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ProviderConfig.class);
		this.createAttribute(PROVIDER_CONFIG, "provider-id", "ProviderId", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(PROVIDER_CONFIG, "provider-type", "ProviderType", 
						AttrProp.ENUM | AttrProp.REQUIRED,
						new String[] {
							"client",
							"server",
							"client-server"
						}, null);
		this.createAttribute(PROVIDER_CONFIG, "class-name", "ClassName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array containing at least one element
	public void setProviderConfig(int index, ProviderConfig value) {
		this.setValue(PROVIDER_CONFIG, index, value);
	}

	// Get Method
	public ProviderConfig getProviderConfig(int index) {
		return (ProviderConfig)this.getValue(PROVIDER_CONFIG, index);
	}

	// This attribute is an array containing at least one element
	public void setProviderConfig(ProviderConfig[] value) {
		this.setValue(PROVIDER_CONFIG, value);
	}

	// Getter Method
	public ProviderConfig[] getProviderConfig() {
		return (ProviderConfig[])this.getValues(PROVIDER_CONFIG);
	}

	// Return the number of properties
	public int sizeProviderConfig() {
		return this.size(PROVIDER_CONFIG);
	}

	// Add a new element returning its index in the list
	public int addProviderConfig(ProviderConfig value)
			throws ConfigException{
		return addProviderConfig(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addProviderConfig(ProviderConfig value, boolean overwrite)
			throws ConfigException{
		ProviderConfig old = getProviderConfigByProviderId(value.getProviderId());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(MessageSecurityConfig.class).getString("cannotAddDuplicate",  "ProviderConfig"));
		}
		return this.addValue(PROVIDER_CONFIG, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeProviderConfig(ProviderConfig value){
		return this.removeValue(PROVIDER_CONFIG, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeProviderConfig(ProviderConfig value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(PROVIDER_CONFIG, value, overwrite);
	}

	public ProviderConfig getProviderConfigByProviderId(String id) {
	 if (null != id) { id = id.trim(); }
	ProviderConfig[] o = getProviderConfig();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ClientTags.PROVIDER_ID)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for AuthLayer of the Element message-security-config
	* @return  the AuthLayer of the Element message-security-config
	*/
	public String getAuthLayer() {
		return getAttributeValue(ClientTags.AUTH_LAYER);
	}
	/**
	* Modify  the AuthLayer of the Element message-security-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAuthLayer(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.AUTH_LAYER, v, overwrite);
	}
	/**
	* Modify  the AuthLayer of the Element message-security-config
	* @param v the new value
	*/
	public void setAuthLayer(String v) {
		setAttributeValue(ClientTags.AUTH_LAYER, v);
	}
	/**
	* Getter for DefaultProvider of the Element message-security-config
	* @return  the DefaultProvider of the Element message-security-config
	*/
	public String getDefaultProvider() {
			return getAttributeValue(ClientTags.DEFAULT_PROVIDER);
	}
	/**
	* Modify  the DefaultProvider of the Element message-security-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultProvider(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.DEFAULT_PROVIDER, v, overwrite);
	}
	/**
	* Modify  the DefaultProvider of the Element message-security-config
	* @param v the new value
	*/
	public void setDefaultProvider(String v) {
		setAttributeValue(ClientTags.DEFAULT_PROVIDER, v);
	}
	/**
	* Getter for DefaultClientProvider of the Element message-security-config
	* @return  the DefaultClientProvider of the Element message-security-config
	*/
	public String getDefaultClientProvider() {
			return getAttributeValue(ClientTags.DEFAULT_CLIENT_PROVIDER);
	}
	/**
	* Modify  the DefaultClientProvider of the Element message-security-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDefaultClientProvider(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.DEFAULT_CLIENT_PROVIDER, v, overwrite);
	}
	/**
	* Modify  the DefaultClientProvider of the Element message-security-config
	* @param v the new value
	*/
	public void setDefaultClientProvider(String v) {
		setAttributeValue(ClientTags.DEFAULT_CLIENT_PROVIDER, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ProviderConfig newProviderConfig() {
		return new ProviderConfig();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "message-security-config" + (canHaveSiblings() ? "[@auth-layer='" + getAttributeValue("auth-layer") +"']" : "") ;
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
		str.append("ProviderConfig["+this.sizeProviderConfig()+"]");	// NOI18N
		for(int i=0; i<this.sizeProviderConfig(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getProviderConfig(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(PROVIDER_CONFIG, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("MessageSecurityConfig\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

