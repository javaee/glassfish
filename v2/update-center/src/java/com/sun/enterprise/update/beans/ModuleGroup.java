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
 *	This generated bean class ModuleGroup matches the schema element 'module_group'.
 *  The root bean class is ModuleUpdates
 *
 *	Generated on Tue Oct 03 15:15:44 PDT 2006
 * @Generated
 */

package com.sun.enterprise.update.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;

// BEGIN_NOI18N

public class ModuleGroup extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(5, 0, 0);

	static public final String NAME = "Name";	// NOI18N
	static public final String MODULE_GROUP = "ModuleGroup";	// NOI18N
	static public final String MODULE = "Module";	// NOI18N

	public ModuleGroup() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ModuleGroup(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
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
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setName(int index, java.lang.String value) {
		// Make sure we've got a place to put this attribute.
		if (size(MODULE_GROUP) == 0) {
			addValue(MODULE_GROUP, "");
		}
		setAttributeValue(MODULE_GROUP, index, "Name", value);
	}

	//
	public java.lang.String getName(int index) {
		// If our element does not exist, then the attribute does not exist.
		if (size(MODULE_GROUP) == 0) {
			return null;
		} else {
			return getAttributeValue(MODULE_GROUP, index, "Name");
		}
	}

	// Return the number of properties
	public int sizeName() {
		return this.size(MODULE_GROUP);
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
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
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

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ModuleGroup\n");	// NOI18N
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
