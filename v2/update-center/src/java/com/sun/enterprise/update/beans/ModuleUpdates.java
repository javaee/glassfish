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
 *	This generated bean class ModuleUpdates matches the schema element 'module_updates'.
 *
 *	Generated on Tue Oct 03 15:15:44 PDT 2006
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	moduleUpdates <module_updates> : ModuleUpdates
 *		[attr: timestamp CDATA #REQUIRED ]
 *		(
 *		  | moduleGroup <module_group> : ModuleGroup
 *		  | 	[attr: name CDATA #REQUIRED ]
 *		  | 	(
 *		  | 	  | moduleGroup <module_group> : ModuleGroup...
 *		  | 	  | 	[attr: name CDATA #REQUIRED ]
 *		  | 	  | module <module> : Module
 *		  | 	  | 	[attr: name CDATA #REQUIRED ]
 *		  | 	  | 	[attr: homepage CDATA #IMPLIED ]
 *		  | 	  | 	[attr: distribution CDATA #REQUIRED ]
 *		  | 	  | 	[attr: license CDATA #IMPLIED ]
 *		  | 	  | 	[attr: downloadsize CDATA #REQUIRED ]
 *		  | 	  | 	[attr: needsrestart CDATA #IMPLIED ]
 *		  | 	  | 	[attr: moduleauthor CDATA #IMPLIED ]
 *		  | 	  | 	[attr: releasedate CDATA #IMPLIED ]
 *		  | 	  | 	description <description> : String[0,1]
 *		  | 	  | 	| manifest <manifest> : boolean
 *		  | 	  | 	| 	[attr: Module CDATA #REQUIRED ]
 *		  | 	  | 	| 	[attr: Module-Name CDATA #REQUIRED ]
 *		  | 	  | 	| 	[attr: Module-Type CDATA #REQUIRED ]
 *		  | 	  | 	| 	[attr: Module-Specification-Version CDATA #REQUIRED ]
 *		  | 	  | 	| 	[attr: Module-Implementation-Version CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: Module-Module-Dependencies CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: Module-Short-Description CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: Module-Long-Description CDATA #IMPLIED ]
 *		  | 	  | 	| 	EMPTY : String
 *		  | 	  | 	| l10n <l10n> : boolean
 *		  | 	  | 	| 	[attr: langcode CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: module_spec_version CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: module_major_version CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: Module-Name CDATA #IMPLIED ]
 *		  | 	  | 	| 	[attr: Module-Long-Description CDATA #IMPLIED ]
 *		  | 	  | 	| 	EMPTY : String
 *		  | 	)[0,n]
 *		  | module <module> : Module
 *		  | 	[attr: name CDATA #REQUIRED ]
 *		  | 	[attr: homepage CDATA #IMPLIED ]
 *		  | 	[attr: distribution CDATA #REQUIRED ]
 *		  | 	[attr: license CDATA #IMPLIED ]
 *		  | 	[attr: downloadsize CDATA #REQUIRED ]
 *		  | 	[attr: needsrestart CDATA #IMPLIED ]
 *		  | 	[attr: moduleauthor CDATA #IMPLIED ]
 *		  | 	[attr: releasedate CDATA #IMPLIED ]
 *		  | 	description <description> : String[0,1]
 *		  | 	| manifest <manifest> : boolean
 *		  | 	| 	[attr: Module CDATA #REQUIRED ]
 *		  | 	| 	[attr: Module-Name CDATA #REQUIRED ]
 *		  | 	| 	[attr: Module-Type CDATA #REQUIRED ]
 *		  | 	| 	[attr: Module-Specification-Version CDATA #REQUIRED ]
 *		  | 	| 	[attr: Module-Implementation-Version CDATA #IMPLIED ]
 *		  | 	| 	[attr: Module-Module-Dependencies CDATA #IMPLIED ]
 *		  | 	| 	[attr: Module-Short-Description CDATA #IMPLIED ]
 *		  | 	| 	[attr: Module-Long-Description CDATA #IMPLIED ]
 *		  | 	| 	EMPTY : String
 *		  | 	| l10n <l10n> : boolean
 *		  | 	| 	[attr: langcode CDATA #IMPLIED ]
 *		  | 	| 	[attr: module_spec_version CDATA #IMPLIED ]
 *		  | 	| 	[attr: module_major_version CDATA #IMPLIED ]
 *		  | 	| 	[attr: Module-Name CDATA #IMPLIED ]
 *		  | 	| 	[attr: Module-Long-Description CDATA #IMPLIED ]
 *		  | 	| 	EMPTY : String
 *		)[0,n]
 *		license <license> : String[0,n]
 *			[attr: name CDATA #REQUIRED ]
 *
 * @Generated
 */

package com.sun.enterprise.update.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.*;

// BEGIN_NOI18N

public class ModuleUpdates extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String TIMESTAMP = "Timestamp";	// NOI18N
	static public final String MODULE_GROUP = "ModuleGroup";	// NOI18N
	static public final String MODULE = "Module";	// NOI18N
	static public final String LICENSE = "License";	// NOI18N
	static public final String LICENSENAME = "LicenseName";	// NOI18N

	public ModuleUpdates() throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public ModuleUpdates(org.w3c.dom.Node doc, int options) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		this(Common.NO_DEFAULT_VALUES);
		initFromNode(doc, options);
	}
	protected void initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("module_updates");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "module_updates"));
		}
		Node n = GraphManager.getElementNode("module_updates", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "module_updates", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public ModuleUpdates(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("module_updates", "ModuleUpdates",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, ModuleUpdates.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("module_group", 	// NOI18N
			MODULE_GROUP, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ModuleGroup.class);
		this.createAttribute(MODULE_GROUP, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createProperty("module", 	// NOI18N
			MODULE, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Module.class);
		this.createAttribute(MODULE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MODULE, "homepage", "Homepage", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MODULE, "distribution", "Distribution", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MODULE, "license", "License", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MODULE, "downloadsize", "Downloadsize", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(MODULE, "needsrestart", "Needsrestart", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MODULE, "moduleauthor", "Moduleauthor", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(MODULE, "releasedate", "Releasedate", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("license", 	// NOI18N
			LICENSE, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createAttribute(LICENSE, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute("timestamp", "Timestamp", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setTimestamp(java.lang.String value) {
		setAttributeValue(TIMESTAMP, value);
	}

	//
	public java.lang.String getTimestamp() {
		return getAttributeValue(TIMESTAMP);
	}

	// This attribute is an array, possibly empty
	public void setModuleGroup(int index, ModuleGroup value) {
		this.setValue(MODULE_GROUP, index, value);
	}

	//
	public ModuleGroup getModuleGroup(int index) {
		return (ModuleGroup)this.getValue(MODULE_GROUP, index);
	}

	// Return the number of properties
	public int sizeModuleGroup() {
		return this.size(MODULE_GROUP);
	}

	// This attribute is an array, possibly empty
	public void setModuleGroup(ModuleGroup[] value) {
		this.setValue(MODULE_GROUP, value);
	}

	//
	public ModuleGroup[] getModuleGroup() {
		return (ModuleGroup[])this.getValues(MODULE_GROUP);
	}

	// Add a new element returning its index in the list
	public int addModuleGroup(com.sun.enterprise.update.beans.ModuleGroup value) {
		int positionOfNewItem = this.addValue(MODULE_GROUP, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModuleGroup(com.sun.enterprise.update.beans.ModuleGroup value) {
		return this.removeValue(MODULE_GROUP, value);
	}

	// This attribute is an array, possibly empty
	public void setModule(int index, Module value) {
		this.setValue(MODULE, index, value);
	}

	//
	public Module getModule(int index) {
		return (Module)this.getValue(MODULE, index);
	}

	// Return the number of properties
	public int sizeModule() {
		return this.size(MODULE);
	}

	// This attribute is an array, possibly empty
	public void setModule(Module[] value) {
		this.setValue(MODULE, value);
	}

	//
	public Module[] getModule() {
		return (Module[])this.getValues(MODULE);
	}

	// Add a new element returning its index in the list
	public int addModule(com.sun.enterprise.update.beans.Module value) {
		int positionOfNewItem = this.addValue(MODULE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeModule(com.sun.enterprise.update.beans.Module value) {
		return this.removeValue(MODULE, value);
	}

	// This attribute is an array, possibly empty
	public void setLicense(int index, String value) {
		this.setValue(LICENSE, index, value);
	}

	//
	public String getLicense(int index) {
		return (String)this.getValue(LICENSE, index);
	}

	// Return the number of properties
	public int sizeLicense() {
		return this.size(LICENSE);
	}

	// This attribute is an array, possibly empty
	public void setLicense(String[] value) {
		this.setValue(LICENSE, value);
	}

	//
	public String[] getLicense() {
		return (String[])this.getValues(LICENSE);
	}

	// Add a new element returning its index in the list
	public int addLicense(String value) {
		int positionOfNewItem = this.addValue(LICENSE, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeLicense(String value) {
		return this.removeValue(LICENSE, value);
	}

	// This attribute is an array, possibly empty
	public void setLicenseName(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(LICENSE) == 0) {
			addValue(LICENSE, "");
		}
		setAttributeValue(LICENSE, index, "Name", value);
	}

	//
	public java.lang.String getLicenseName(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(LICENSE) == 0) {
			return null;
		} else {
			return getAttributeValue(LICENSE, index, "Name");
		}
	}

	// Return the number of properties
	public int sizeLicenseName() {
		return this.size(LICENSE);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ModuleGroup newModuleGroup() {
		return new ModuleGroup();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Module newModule() {
		return new Module();
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
	public static ModuleUpdates createGraph(org.w3c.dom.Node doc) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return new ModuleUpdates(doc, Common.NO_DEFAULT_VALUES);
	}

	public static ModuleUpdates createGraph(java.io.File f) throws org.netbeans.modules.schema2beans.Schema2BeansException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return createGraph(in, false);
		} finally {
			in.close();
		}
	}

	public static ModuleUpdates createGraph(java.io.InputStream in) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		return createGraph(in, false);
	}

	public static ModuleUpdates createGraph(java.io.InputStream in, boolean validate) throws org.netbeans.modules.schema2beans.Schema2BeansException {
		Document doc = GraphManager.createXmlDocument(in, validate);
		return createGraph(doc);
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static ModuleUpdates createGraph() {
		try {
			return new ModuleUpdates();
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
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
		str.append("ModuleGroup["+this.sizeModuleGroup()+"]");	// NOI18N
		for(int i=0; i<this.sizeModuleGroup(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModuleGroup(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE_GROUP, i, str, indent);
		}

		str.append(indent);
		str.append("Module["+this.sizeModule()+"]");	// NOI18N
		for(int i=0; i<this.sizeModule(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getModule(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(MODULE, i, str, indent);
		}

		str.append(indent);
		str.append("License["+this.sizeLicense()+"]");	// NOI18N
		for(int i=0; i<this.sizeLicense(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getLicense(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(LICENSE, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ModuleUpdates\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
		The following schema file has been used for generation:

<!-- -//Glassfish//DTD Update Center Catalog 1.0//EN -->
<!-- XML representation of Update Center Modules/Updates Catalog -->

<!ELEMENT module_updates ((module_group|module)*, license*)>
<!ATTLIST module_updates timestamp CDATA #REQUIRED>

<!ELEMENT module_group ((module_group|module)*)>
<!ATTLIST module_group name CDATA #REQUIRED>

<!ELEMENT module (description?, (manifest | l10n) )>
<!ATTLIST module name CDATA #REQUIRED
                 homepage     CDATA #IMPLIED
                 distribution CDATA #REQUIRED
                 license      CDATA #IMPLIED
                 downloadsize CDATA #REQUIRED
                 needsrestart CDATA #IMPLIED
                 moduleauthor CDATA #IMPLIED
                 releasedate  CDATA #IMPLIED>

<!ELEMENT description (#PCDATA)>

<!ELEMENT manifest EMPTY>
<!ATTLIST manifest Module CDATA #REQUIRED
                   Module-Name CDATA #REQUIRED
                   Module-Type CDATA #REQUIRED
                   Module-Specification-Version CDATA #REQUIRED
                   Module-Implementation-Version CDATA #IMPLIED
                   Module-Module-Dependencies CDATA #IMPLIED
                   Module-Short-Description CDATA #IMPLIED
                   Module-Long-Description CDATA #IMPLIED>

<!ELEMENT l10n EMPTY>
<!ATTLIST l10n   langcode             CDATA #IMPLIED
                 module_spec_version  CDATA #IMPLIED
                 module_major_version CDATA #IMPLIED
                 Module-Name  CDATA #IMPLIED
                 Module-Long-Description CDATA #IMPLIED>

<!ELEMENT license (#PCDATA)>
<!ATTLIST license name CDATA #REQUIRED>

*/
