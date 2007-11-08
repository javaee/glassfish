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
 *	This generated bean class Profiler matches the DTD element profiler
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

public class Profiler extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String JVM_OPTIONS = "JvmOptions";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public Profiler() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Profiler(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("jvm-options", JVM_OPTIONS, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("property", ELEMENT_PROPERTY, Common.SEQUENCE_OR | 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is an array, possibly empty
	public void setJvmOptions(String[] value) {
		this.setValue(JVM_OPTIONS, value);
	}

	// Getter Method
	public String[] getJvmOptions() {
		return (String[])this.getValues(JVM_OPTIONS);
	}

	// Return the number of properties
	public int sizeJvmOptions() {
		return this.size(JVM_OPTIONS);
	}

	// Add a new element returning its index in the list
	public int addJvmOptions(String value)
			throws ConfigException{
		return addJvmOptions(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addJvmOptions(String value, boolean overwrite)
			throws ConfigException{
		return this.addValue(JVM_OPTIONS, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeJvmOptions(String value){
		return this.removeValue(JVM_OPTIONS, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeJvmOptions(String value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(JVM_OPTIONS, value, overwrite);
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
			throw new ConfigException(StringManager.getManager(Profiler.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element profiler
	* @return  the Name of the Element profiler
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element profiler
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element profiler
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for Classpath of the Element profiler
	* @return  the Classpath of the Element profiler
	*/
	public String getClasspath() {
			return getAttributeValue(ServerTags.CLASSPATH);
	}
	/**
	* Modify  the Classpath of the Element profiler
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClasspath(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLASSPATH, v, overwrite);
	}
	/**
	* Modify  the Classpath of the Element profiler
	* @param v the new value
	*/
	public void setClasspath(String v) {
		setAttributeValue(ServerTags.CLASSPATH, v);
	}
	/**
	* Getter for NativeLibraryPath of the Element profiler
	* @return  the NativeLibraryPath of the Element profiler
	*/
	public String getNativeLibraryPath() {
			return getAttributeValue(ServerTags.NATIVE_LIBRARY_PATH);
	}
	/**
	* Modify  the NativeLibraryPath of the Element profiler
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNativeLibraryPath(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH, v, overwrite);
	}
	/**
	* Modify  the NativeLibraryPath of the Element profiler
	* @param v the new value
	*/
	public void setNativeLibraryPath(String v) {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH, v);
	}
	/**
	* Getter for Enabled of the Element profiler
	* @return  the Enabled of the Element profiler
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element profiler
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element profiler
	* @param v the new value
	*/
	public void setEnabled(boolean v) {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Enabled from dtd
	*/
	public static String getDefaultEnabled() {
		return "true".trim();
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
	    ret = "profiler" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
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
		str.append("JvmOptions["+this.sizeJvmOptions()+"]");	// NOI18N
		for(int i=0; i<this.sizeJvmOptions(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getValue(JVM_OPTIONS, i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(JVM_OPTIONS, i, str, indent);
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
		str.append("Profiler\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

