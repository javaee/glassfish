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
 *	This generated bean class ClientContainer matches the DTD element client-container
 *
 */

package com.sun.enterprise.config.clientbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.io.Serializable;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.util.i18n.StringManager;

// BEGIN_NOI18N

public class ClientContainer extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String TARGET_SERVER = "TargetServer";
	static public final String AUTH_REALM = "AuthRealm";
	static public final String CLIENT_CREDENTIAL = "ClientCredential";
	static public final String LOG_SERVICE = "LogService";
	static public final String MESSAGE_SECURITY_CONFIG = "MessageSecurityConfig";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ClientContainer() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public ClientContainer(org.w3c.dom.Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("client-container");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "client-container"));
		}
		Node n = GraphManager.getElementNode("client-container", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "client-container", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public ClientContainer(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("client-container", "ClientContainer",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, ClientContainer.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(6);
		this.createProperty("target-server", TARGET_SERVER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			TargetServer.class);
		this.createAttribute(TARGET_SERVER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(TARGET_SERVER, "address", "Address", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(TARGET_SERVER, "port", "Port", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("auth-realm", AUTH_REALM, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AuthRealm.class);
		this.createAttribute(AUTH_REALM, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(AUTH_REALM, "classname", "Classname", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("client-credential", CLIENT_CREDENTIAL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ClientCredential.class);
		this.createAttribute(CLIENT_CREDENTIAL, "user-name", "UserName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CLIENT_CREDENTIAL, "password", "Password", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(CLIENT_CREDENTIAL, "realm", "Realm", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("log-service", LOG_SERVICE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LogService.class);
		this.createAttribute(LOG_SERVICE, "file", "File", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(LOG_SERVICE, "level", "Level", 
						AttrProp.CDATA,
						null, "SEVERE");
		this.createProperty("message-security-config", MESSAGE_SECURITY_CONFIG, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			MessageSecurityConfig.class);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "auth-layer", "AuthLayer", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "default-provider", "DefaultProvider", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MESSAGE_SECURITY_CONFIG, "default-client-provider", "DefaultClientProvider", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("property", ELEMENT_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute("send-password", "SendPassword", 
						AttrProp.CDATA,
						null, "true");
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array containing at least one element
	public void setTargetServer(int index, TargetServer value) {
		this.setValue(TARGET_SERVER, index, value);
	}

	// Get Method
	public TargetServer getTargetServer(int index) {
		return (TargetServer)this.getValue(TARGET_SERVER, index);
	}

	// This attribute is an array containing at least one element
	public void setTargetServer(TargetServer[] value) {
		this.setValue(TARGET_SERVER, value);
	}

	// Getter Method
	public TargetServer[] getTargetServer() {
		return (TargetServer[])this.getValues(TARGET_SERVER);
	}

	// Return the number of properties
	public int sizeTargetServer() {
		return this.size(TARGET_SERVER);
	}

	// Add a new element returning its index in the list
	public int addTargetServer(TargetServer value)
			throws ConfigException{
		return addTargetServer(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addTargetServer(TargetServer value, boolean overwrite)
			throws ConfigException{
		TargetServer old = getTargetServerByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ClientContainer.class).getString("cannotAddDuplicate",  "TargetServer"));
		}
		return this.addValue(TARGET_SERVER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeTargetServer(TargetServer value){
		return this.removeValue(TARGET_SERVER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeTargetServer(TargetServer value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(TARGET_SERVER, value, overwrite);
	}

	public TargetServer getTargetServerByName(String id) {
	 if (null != id) { id = id.trim(); }
	TargetServer[] o = getTargetServer();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ClientTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// This attribute is optional
	public void setAuthRealm(AuthRealm value) {
		this.setValue(AUTH_REALM, value);
	}

	// Get Method
	public AuthRealm getAuthRealm() {
		return (AuthRealm)this.getValue(AUTH_REALM);
	}

	// This attribute is optional
	public void setClientCredential(ClientCredential value) {
		this.setValue(CLIENT_CREDENTIAL, value);
	}

	// Get Method
	public ClientCredential getClientCredential() {
		return (ClientCredential)this.getValue(CLIENT_CREDENTIAL);
	}

	// This attribute is optional
	public void setLogService(LogService value) {
		this.setValue(LOG_SERVICE, value);
	}

	// Get Method
	public LogService getLogService() {
		return (LogService)this.getValue(LOG_SERVICE);
	}

	// This attribute is an array, possibly empty
	public void setMessageSecurityConfig(int index, MessageSecurityConfig value) {
		this.setValue(MESSAGE_SECURITY_CONFIG, index, value);
	}

	// Get Method
	public MessageSecurityConfig getMessageSecurityConfig(int index) {
		return (MessageSecurityConfig)this.getValue(MESSAGE_SECURITY_CONFIG, index);
	}

	// This attribute is an array, possibly empty
	public void setMessageSecurityConfig(MessageSecurityConfig[] value) {
		this.setValue(MESSAGE_SECURITY_CONFIG, value);
	}

	// Getter Method
	public MessageSecurityConfig[] getMessageSecurityConfig() {
		return (MessageSecurityConfig[])this.getValues(MESSAGE_SECURITY_CONFIG);
	}

	// Return the number of properties
	public int sizeMessageSecurityConfig() {
		return this.size(MESSAGE_SECURITY_CONFIG);
	}

	// Add a new element returning its index in the list
	public int addMessageSecurityConfig(MessageSecurityConfig value)
			throws ConfigException{
		return addMessageSecurityConfig(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addMessageSecurityConfig(MessageSecurityConfig value, boolean overwrite)
			throws ConfigException{
		MessageSecurityConfig old = getMessageSecurityConfigByAuthLayer(value.getAuthLayer());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ClientContainer.class).getString("cannotAddDuplicate",  "MessageSecurityConfig"));
		}
		return this.addValue(MESSAGE_SECURITY_CONFIG, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeMessageSecurityConfig(MessageSecurityConfig value){
		return this.removeValue(MESSAGE_SECURITY_CONFIG, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeMessageSecurityConfig(MessageSecurityConfig value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(MESSAGE_SECURITY_CONFIG, value, overwrite);
	}

	public MessageSecurityConfig getMessageSecurityConfigByAuthLayer(String id) {
	 if (null != id) { id = id.trim(); }
	MessageSecurityConfig[] o = getMessageSecurityConfig();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ClientTags.AUTH_LAYER)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// This attribute is an array, possibly empty
	public void setElementProperty(int index, ElementProperty value) {
		this.setValue(ELEMENT_PROPERTY, index, value);
	}

	// Get Method
	public ElementProperty getElementProperty(int index) {
		return (ElementProperty)this.getValue(ELEMENT_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setElementProperty(ElementProperty[] value) {
		this.setValue(ELEMENT_PROPERTY, value);
	}

	// Getter Method
	public ElementProperty[] getElementProperty() {
		return (ElementProperty[])this.getValues(ELEMENT_PROPERTY);
	}

	// Return the number of properties
	public int sizeElementProperty() {
		return this.size(ELEMENT_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addElementProperty(ElementProperty value)
			throws ConfigException{
		return addElementProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addElementProperty(ElementProperty value, boolean overwrite)
			throws ConfigException{
		ElementProperty old = getElementPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ClientContainer.class).getString("cannotAddDuplicate",  "ElementProperty"));
		}
		return this.addValue(ELEMENT_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeElementProperty(ElementProperty value){
		return this.removeValue(ELEMENT_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeElementProperty(ElementProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ELEMENT_PROPERTY, value, overwrite);
	}

	public ElementProperty getElementPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	ElementProperty[] o = getElementProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ClientTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for SendPassword of the Element client-container
	* @return  the SendPassword of the Element client-container
	*/
	public boolean isSendPassword() {
		return toBoolean(getAttributeValue(ClientTags.SEND_PASSWORD));
	}
	/**
	* Modify  the SendPassword of the Element client-container
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSendPassword(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ClientTags.SEND_PASSWORD, ""+(v==true), overwrite);
	}
	/**
	* Modify  the SendPassword of the Element client-container
	* @param v the new value
	*/
	public void setSendPassword(boolean v) {
		setAttributeValue(ClientTags.SEND_PASSWORD, ""+(v==true));
	}
	/**
	* Get the default value of SendPassword from dtd
	*/
	public static String getDefaultSendPassword() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public TargetServer newTargetServer() {
		return new TargetServer();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AuthRealm newAuthRealm() {
		return new AuthRealm();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ClientCredential newClientCredential() {
		return new ClientCredential();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LogService newLogService() {
		return new LogService();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public MessageSecurityConfig newMessageSecurityConfig() {
		return new MessageSecurityConfig();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ElementProperty newElementProperty() {
		return new ElementProperty();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "client-container";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ClientTags.SEND_PASSWORD)) return "true".trim();
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
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static ClientContainer createGraph(org.w3c.dom.Node doc) {
		return new ClientContainer(doc, Common.NO_DEFAULT_VALUES);
	}

	public static ClientContainer createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static ClientContainer createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static ClientContainer createGraph(java.io.InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static ClientContainer createGraph() {
		return new ClientContainer();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Special serializer: output XML as serialization
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
			init(comparators, runtimeVersion);
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}

	public void _setSchemaLocation(String location) {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
		}
		setAttributeValue("xsi:schemaLocation", location);
	}

	public String _getSchemaLocation() {
		if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
			createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
			setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
		}
		return getAttributeValue("xsi:schemaLocation");
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("TargetServer["+this.sizeTargetServer()+"]");	// NOI18N
		for(int i=0; i<this.sizeTargetServer(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getTargetServer(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(TARGET_SERVER, i, str, indent);
		}

		str.append(indent);
		str.append("AuthRealm");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAuthRealm();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(AUTH_REALM, 0, str, indent);

		str.append(indent);
		str.append("ClientCredential");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getClientCredential();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CLIENT_CREDENTIAL, 0, str, indent);

		str.append(indent);
		str.append("LogService");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLogService();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOG_SERVICE, 0, str, indent);

		str.append(indent);
		str.append("MessageSecurityConfig["+this.sizeMessageSecurityConfig()+"]");	// NOI18N
		for(int i=0; i<this.sizeMessageSecurityConfig(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getMessageSecurityConfig(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MESSAGE_SECURITY_CONFIG, i, str, indent);
		}

		str.append(indent);
		str.append("ElementProperty["+this.sizeElementProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeElementProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getElementProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ELEMENT_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ClientContainer\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

