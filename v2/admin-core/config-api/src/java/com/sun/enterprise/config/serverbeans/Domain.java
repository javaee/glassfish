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
 *	This generated bean class Domain matches the DTD element domain
 *
 */

package com.sun.enterprise.config.serverbeans;

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

public class Domain extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String APPLICATIONS = "Applications";
	static public final String RESOURCES = "Resources";
	static public final String CONFIGS = "Configs";
	static public final String SERVERS = "Servers";
	static public final String CLUSTERS = "Clusters";
	static public final String NODE_AGENTS = "NodeAgents";
	static public final String LB_CONFIGS = "LbConfigs";
	static public final String LOAD_BALANCERS = "LoadBalancers";
	static public final String SYSTEM_PROPERTY = "SystemProperty";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public Domain() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public Domain(org.w3c.dom.Node doc, int options) {
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
			doc = GraphManager.createRootElementNode("domain");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "domain"));
		}
		Node n = GraphManager.getElementNode("domain", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "domain", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public Domain(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("domain", "Domain",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, Domain.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(10);
		this.createProperty("applications", APPLICATIONS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Applications.class);
		this.createProperty("resources", RESOURCES, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Resources.class);
		this.createProperty("configs", CONFIGS, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Configs.class);
		this.createProperty("servers", SERVERS, 
			Common.TYPE_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Servers.class);
		this.createProperty("clusters", CLUSTERS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Clusters.class);
		this.createProperty("node-agents", NODE_AGENTS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			NodeAgents.class);
		this.createProperty("lb-configs", LB_CONFIGS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LbConfigs.class);
		this.createProperty("load-balancers", LOAD_BALANCERS, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			LoadBalancers.class);
		this.createProperty("system-property", SYSTEM_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SystemProperty.class);
		this.createAttribute(SYSTEM_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(SYSTEM_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
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
		this.createAttribute("application-root", "ApplicationRoot", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute("log-root", "LogRoot", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute("locale", "Locale", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setApplications(Applications value) {
		this.setValue(APPLICATIONS, value);
	}

	// Get Method
	public Applications getApplications() {
		return (Applications)this.getValue(APPLICATIONS);
	}

	// This attribute is optional
	public void setResources(Resources value) {
		this.setValue(RESOURCES, value);
	}

	// Get Method
	public Resources getResources() {
		return (Resources)this.getValue(RESOURCES);
	}

	// This attribute is mandatory
	public void setConfigs(Configs value) {
		this.setValue(CONFIGS, value);
	}

	// Get Method
	public Configs getConfigs() {
		return (Configs)this.getValue(CONFIGS);
	}

	// This attribute is mandatory
	public void setServers(Servers value) {
		this.setValue(SERVERS, value);
	}

	// Get Method
	public Servers getServers() {
		return (Servers)this.getValue(SERVERS);
	}

	// This attribute is optional
	public void setClusters(Clusters value) {
		this.setValue(CLUSTERS, value);
	}

	// Get Method
	public Clusters getClusters() {
		return (Clusters)this.getValue(CLUSTERS);
	}

	// This attribute is optional
	public void setNodeAgents(NodeAgents value) {
		this.setValue(NODE_AGENTS, value);
	}

	// Get Method
	public NodeAgents getNodeAgents() {
		return (NodeAgents)this.getValue(NODE_AGENTS);
	}

	// This attribute is optional
	public void setLbConfigs(LbConfigs value) {
		this.setValue(LB_CONFIGS, value);
	}

	// Get Method
	public LbConfigs getLbConfigs() {
		return (LbConfigs)this.getValue(LB_CONFIGS);
	}

	// This attribute is optional
	public void setLoadBalancers(LoadBalancers value) {
		this.setValue(LOAD_BALANCERS, value);
	}

	// Get Method
	public LoadBalancers getLoadBalancers() {
		return (LoadBalancers)this.getValue(LOAD_BALANCERS);
	}

	// Get Method
	public SystemProperty getSystemProperty(int index) {
		return (SystemProperty)this.getValue(SYSTEM_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setSystemProperty(SystemProperty[] value) {
		this.setValue(SYSTEM_PROPERTY, value);
	}

	// Getter Method
	public SystemProperty[] getSystemProperty() {
		return (SystemProperty[])this.getValues(SYSTEM_PROPERTY);
	}

	// Return the number of properties
	public int sizeSystemProperty() {
		return this.size(SYSTEM_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addSystemProperty(SystemProperty value)
			throws ConfigException{
		return addSystemProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addSystemProperty(SystemProperty value, boolean overwrite)
			throws ConfigException{
		SystemProperty old = getSystemPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(Domain.class).getString("cannotAddDuplicate",  "SystemProperty"));
		}
		return this.addValue(SYSTEM_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSystemProperty(SystemProperty value){
		return this.removeValue(SYSTEM_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeSystemProperty(SystemProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(SYSTEM_PROPERTY, value, overwrite);
	}

	public SystemProperty getSystemPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	SystemProperty[] o = getSystemProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
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
			throw new ConfigException(StringManager.getManager(Domain.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for ApplicationRoot of the Element domain
	* @return  the ApplicationRoot of the Element domain
	*/
	public String getApplicationRoot() {
			return getAttributeValue(ServerTags.APPLICATION_ROOT);
	}
	/**
	* Modify  the ApplicationRoot of the Element domain
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setApplicationRoot(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.APPLICATION_ROOT, v, overwrite);
	}
	/**
	* Modify  the ApplicationRoot of the Element domain
	* @param v the new value
	*/
	public void setApplicationRoot(String v) {
		setAttributeValue(ServerTags.APPLICATION_ROOT, v);
	}
	/**
	* Getter for LogRoot of the Element domain
	* @return  the LogRoot of the Element domain
	*/
	public String getLogRoot() {
			return getAttributeValue(ServerTags.LOG_ROOT);
	}
	/**
	* Modify  the LogRoot of the Element domain
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLogRoot(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOG_ROOT, v, overwrite);
	}
	/**
	* Modify  the LogRoot of the Element domain
	* @param v the new value
	*/
	public void setLogRoot(String v) {
		setAttributeValue(ServerTags.LOG_ROOT, v);
	}
	/**
	* Getter for Locale of the Element domain
	* @return  the Locale of the Element domain
	*/
	public String getLocale() {
			return getAttributeValue(ServerTags.LOCALE);
	}
	/**
	* Modify  the Locale of the Element domain
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLocale(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LOCALE, v, overwrite);
	}
	/**
	* Modify  the Locale of the Element domain
	* @param v the new value
	*/
	public void setLocale(String v) {
		setAttributeValue(ServerTags.LOCALE, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Applications newApplications() {
		return new Applications();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Resources newResources() {
		return new Resources();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Configs newConfigs() {
		return new Configs();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Servers newServers() {
		return new Servers();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Clusters newClusters() {
		return new Clusters();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public NodeAgents newNodeAgents() {
		return new NodeAgents();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LbConfigs newLbConfigs() {
		return new LbConfigs();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public LoadBalancers newLoadBalancers() {
		return new LoadBalancers();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SystemProperty newSystemProperty() {
		return new SystemProperty();
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
	    ret = "domain";
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
	//
	// This method returns the root of the bean graph
	// Each call creates a new bean graph from the specified DOM graph
	//
	public static Domain createGraph(org.w3c.dom.Node doc) {
		return new Domain(doc, Common.NO_DEFAULT_VALUES);
	}

	public static Domain createGraph(java.io.File f) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static Domain createGraph(java.io.InputStream in) {
		return createGraph(in, false);
	}

	public static Domain createGraph(java.io.InputStream in, boolean validate) {
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
	public static Domain createGraph() {
		return new Domain();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Special serializer: output XML as serialization
	private synchronized void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		out.defaultWriteObject();
		final int MAX_SIZE = 0XFFFF;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		final byte [] array = baos.toByteArray();
		final int numStrings = array.length / MAX_SIZE;
		final int leftover = array.length % MAX_SIZE;
		out.writeInt(numStrings + (0 == leftover ? 0 : 1));
		out.writeInt(MAX_SIZE);
		int offset = 0;
		for (int i = 0; i < numStrings; i++){
			out.writeUTF(new String(array, offset, MAX_SIZE));
			offset += MAX_SIZE;
		}
		if (leftover > 0){
			final int count = array.length - offset;
			out.writeUTF(new String(array, offset, count));
		}
	}


	private synchronized void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		try{
		in.defaultReadObject();
		init(comparators, runtimeVersion);
		//init(comparators, new GenBeans.Version(1, 0, 8));
		final int numStrings = in.readInt();
		final int max_size = in.readInt();
		final StringBuffer sb = new StringBuffer(numStrings * max_size);
		for (int i = 0; i < numStrings; i++){
			sb.append(in.readUTF());
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
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
		str.append("Applications");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getApplications();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(APPLICATIONS, 0, str, indent);

		str.append(indent);
		str.append("Resources");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getResources();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(RESOURCES, 0, str, indent);

		str.append(indent);
		str.append("Configs");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getConfigs();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONFIGS, 0, str, indent);

		str.append(indent);
		str.append("Servers");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getServers();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(SERVERS, 0, str, indent);

		str.append(indent);
		str.append("Clusters");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getClusters();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CLUSTERS, 0, str, indent);

		str.append(indent);
		str.append("NodeAgents");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getNodeAgents();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(NODE_AGENTS, 0, str, indent);

		str.append(indent);
		str.append("LbConfigs");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLbConfigs();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LB_CONFIGS, 0, str, indent);

		str.append(indent);
		str.append("LoadBalancers");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getLoadBalancers();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(LOAD_BALANCERS, 0, str, indent);

		str.append(indent);
		str.append("SystemProperty["+this.sizeSystemProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeSystemProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSystemProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SYSTEM_PROPERTY, i, str, indent);
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
		str.append("Domain\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

