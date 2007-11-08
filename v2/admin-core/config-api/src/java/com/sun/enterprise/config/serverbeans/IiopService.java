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
 *	This generated bean class IiopService matches the DTD element iiop-service
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

public class IiopService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ORB = "Orb";
	static public final String SSL_CLIENT_CONFIG = "SslClientConfig";
	static public final String IIOP_LISTENER = "IiopListener";

	public IiopService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public IiopService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("orb", ORB, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Orb.class);
		this.createAttribute(ORB, "use-thread-pool-ids", "UseThreadPoolIds", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ORB, "message-fragment-size", "MessageFragmentSize", 
						AttrProp.CDATA,
						null, "1024");
		this.createAttribute(ORB, "max-connections", "MaxConnections", 
						AttrProp.CDATA,
						null, "1024");
		this.createProperty("ssl-client-config", SSL_CLIENT_CONFIG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SslClientConfig.class);
		this.createProperty("iiop-listener", IIOP_LISTENER, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			IiopListener.class);
		this.createAttribute(IIOP_LISTENER, "id", "Id", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(IIOP_LISTENER, "address", "Address", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(IIOP_LISTENER, "port", "Port", 
						AttrProp.CDATA,
						null, "1072");
		this.createAttribute(IIOP_LISTENER, "security-enabled", "SecurityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(IIOP_LISTENER, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setOrb(Orb value) {
		this.setValue(ORB, value);
	}

	// Get Method
	public Orb getOrb() {
		return (Orb)this.getValue(ORB);
	}

	// This attribute is optional
	public void setSslClientConfig(SslClientConfig value) {
		this.setValue(SSL_CLIENT_CONFIG, value);
	}

	// Get Method
	public SslClientConfig getSslClientConfig() {
		return (SslClientConfig)this.getValue(SSL_CLIENT_CONFIG);
	}

	// Get Method
	public IiopListener getIiopListener(int index) {
		return (IiopListener)this.getValue(IIOP_LISTENER, index);
	}

	// This attribute is an array, possibly empty
	public void setIiopListener(IiopListener[] value) {
		this.setValue(IIOP_LISTENER, value);
	}

	// Getter Method
	public IiopListener[] getIiopListener() {
		return (IiopListener[])this.getValues(IIOP_LISTENER);
	}

	// Return the number of properties
	public int sizeIiopListener() {
		return this.size(IIOP_LISTENER);
	}

	// Add a new element returning its index in the list
	public int addIiopListener(IiopListener value)
			throws ConfigException{
		return addIiopListener(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addIiopListener(IiopListener value, boolean overwrite)
			throws ConfigException{
		IiopListener old = getIiopListenerById(value.getId());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(IiopService.class).getString("cannotAddDuplicate",  "IiopListener"));
		}
		return this.addValue(IIOP_LISTENER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeIiopListener(IiopListener value){
		return this.removeValue(IIOP_LISTENER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeIiopListener(IiopListener value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(IIOP_LISTENER, value, overwrite);
	}

	public IiopListener getIiopListenerById(String id) {
	 if (null != id) { id = id.trim(); }
	IiopListener[] o = getIiopListener();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.ID)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for ClientAuthenticationRequired of the Element iiop-service
	* @return  the ClientAuthenticationRequired of the Element iiop-service
	*/
	public boolean isClientAuthenticationRequired() {
		return toBoolean(getAttributeValue(ServerTags.CLIENT_AUTHENTICATION_REQUIRED));
	}
	/**
	* Modify  the ClientAuthenticationRequired of the Element iiop-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClientAuthenticationRequired(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLIENT_AUTHENTICATION_REQUIRED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ClientAuthenticationRequired of the Element iiop-service
	* @param v the new value
	*/
	public void setClientAuthenticationRequired(boolean v) {
		setAttributeValue(ServerTags.CLIENT_AUTHENTICATION_REQUIRED, ""+(v==true));
	}
	/**
	* Get the default value of ClientAuthenticationRequired from dtd
	*/
	public static String getDefaultClientAuthenticationRequired() {
		return "false".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Orb newOrb() {
		return new Orb();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SslClientConfig newSslClientConfig() {
		return new SslClientConfig();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public IiopListener newIiopListener() {
		return new IiopListener();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "iiop-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.CLIENT_AUTHENTICATION_REQUIRED)) return "false".trim();
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
		str.append("Orb");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getOrb();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ORB, 0, str, indent);

		str.append(indent);
		str.append("SslClientConfig");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getSslClientConfig();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SSL_CLIENT_CONFIG, 0, str, indent);

		str.append(indent);
		str.append("IiopListener["+this.sizeIiopListener()+"]");	// NOI18N
		for(int i=0; i<this.sizeIiopListener(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getIiopListener(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(IIOP_LISTENER, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("IiopService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

