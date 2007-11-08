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
 *	This generated bean class JavaConfig matches the DTD element java-config
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

public class JavaConfig extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String PROFILER = "Profiler";
	static public final String JVM_OPTIONS = "JvmOptions";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public JavaConfig() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public JavaConfig(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("profiler", PROFILER, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Profiler.class);
		this.createAttribute(PROFILER, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(PROFILER, "classpath", "Classpath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PROFILER, "native-library-path", "NativeLibraryPath", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(PROFILER, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
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

	// This attribute is optional
	public void setProfiler(Profiler value) {
		this.setValue(PROFILER, value);
	}

	// Get Method
	public Profiler getProfiler() {
		return (Profiler)this.getValue(PROFILER);
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
			throw new ConfigException(StringManager.getManager(JavaConfig.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for JavaHome of the Element java-config
	* @return  the JavaHome of the Element java-config
	*/
	public String getJavaHome() {
		return getAttributeValue(ServerTags.JAVA_HOME);
	}
	/**
	* Modify  the JavaHome of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJavaHome(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JAVA_HOME, v, overwrite);
	}
	/**
	* Modify  the JavaHome of the Element java-config
	* @param v the new value
	*/
	public void setJavaHome(String v) {
		setAttributeValue(ServerTags.JAVA_HOME, v);
	}
	/**
	* Get the default value of JavaHome from dtd
	*/
	public static String getDefaultJavaHome() {
		return "${com.sun.aas.javaRoot}".trim();
	}
	/**
	* Getter for DebugEnabled of the Element java-config
	* @return  the DebugEnabled of the Element java-config
	*/
	public boolean isDebugEnabled() {
		return toBoolean(getAttributeValue(ServerTags.DEBUG_ENABLED));
	}
	/**
	* Modify  the DebugEnabled of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDebugEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEBUG_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the DebugEnabled of the Element java-config
	* @param v the new value
	*/
	public void setDebugEnabled(boolean v) {
		setAttributeValue(ServerTags.DEBUG_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of DebugEnabled from dtd
	*/
	public static String getDefaultDebugEnabled() {
		return "false".trim();
	}
	/**
	* Getter for DebugOptions of the Element java-config
	* @return  the DebugOptions of the Element java-config
	*/
	public String getDebugOptions() {
		return getAttributeValue(ServerTags.DEBUG_OPTIONS);
	}
	/**
	* Modify  the DebugOptions of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDebugOptions(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DEBUG_OPTIONS, v, overwrite);
	}
	/**
	* Modify  the DebugOptions of the Element java-config
	* @param v the new value
	*/
	public void setDebugOptions(String v) {
		setAttributeValue(ServerTags.DEBUG_OPTIONS, v);
	}
	/**
	* Get the default value of DebugOptions from dtd
	*/
	public static String getDefaultDebugOptions() {
		return "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n".trim();
	}
	/**
	* Getter for RmicOptions of the Element java-config
	* @return  the RmicOptions of the Element java-config
	*/
	public String getRmicOptions() {
		return getAttributeValue(ServerTags.RMIC_OPTIONS);
	}
	/**
	* Modify  the RmicOptions of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRmicOptions(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RMIC_OPTIONS, v, overwrite);
	}
	/**
	* Modify  the RmicOptions of the Element java-config
	* @param v the new value
	*/
	public void setRmicOptions(String v) {
		setAttributeValue(ServerTags.RMIC_OPTIONS, v);
	}
	/**
	* Get the default value of RmicOptions from dtd
	*/
	public static String getDefaultRmicOptions() {
		return "-iiop -poa -alwaysgenerate -keepgenerated -g".trim();
	}
	/**
	* Getter for JavacOptions of the Element java-config
	* @return  the JavacOptions of the Element java-config
	*/
	public String getJavacOptions() {
		return getAttributeValue(ServerTags.JAVAC_OPTIONS);
	}
	/**
	* Modify  the JavacOptions of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setJavacOptions(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.JAVAC_OPTIONS, v, overwrite);
	}
	/**
	* Modify  the JavacOptions of the Element java-config
	* @param v the new value
	*/
	public void setJavacOptions(String v) {
		setAttributeValue(ServerTags.JAVAC_OPTIONS, v);
	}
	/**
	* Get the default value of JavacOptions from dtd
	*/
	public static String getDefaultJavacOptions() {
		return "-g".trim();
	}
	/**
	* Getter for ClasspathPrefix of the Element java-config
	* @return  the ClasspathPrefix of the Element java-config
	*/
	public String getClasspathPrefix() {
			return getAttributeValue(ServerTags.CLASSPATH_PREFIX);
	}
	/**
	* Modify  the ClasspathPrefix of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClasspathPrefix(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLASSPATH_PREFIX, v, overwrite);
	}
	/**
	* Modify  the ClasspathPrefix of the Element java-config
	* @param v the new value
	*/
	public void setClasspathPrefix(String v) {
		setAttributeValue(ServerTags.CLASSPATH_PREFIX, v);
	}
	/**
	* Getter for ClasspathSuffix of the Element java-config
	* @return  the ClasspathSuffix of the Element java-config
	*/
	public String getClasspathSuffix() {
			return getAttributeValue(ServerTags.CLASSPATH_SUFFIX);
	}
	/**
	* Modify  the ClasspathSuffix of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClasspathSuffix(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLASSPATH_SUFFIX, v, overwrite);
	}
	/**
	* Modify  the ClasspathSuffix of the Element java-config
	* @param v the new value
	*/
	public void setClasspathSuffix(String v) {
		setAttributeValue(ServerTags.CLASSPATH_SUFFIX, v);
	}
	/**
	* Getter for ServerClasspath of the Element java-config
	* @return  the ServerClasspath of the Element java-config
	*/
	public String getServerClasspath() {
			return getAttributeValue(ServerTags.SERVER_CLASSPATH);
	}
	/**
	* Modify  the ServerClasspath of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setServerClasspath(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SERVER_CLASSPATH, v, overwrite);
	}
	/**
	* Modify  the ServerClasspath of the Element java-config
	* @param v the new value
	*/
	public void setServerClasspath(String v) {
		setAttributeValue(ServerTags.SERVER_CLASSPATH, v);
	}
	/**
	* Getter for SystemClasspath of the Element java-config
	* @return  the SystemClasspath of the Element java-config
	*/
	public String getSystemClasspath() {
			return getAttributeValue(ServerTags.SYSTEM_CLASSPATH);
	}
	/**
	* Modify  the SystemClasspath of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSystemClasspath(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SYSTEM_CLASSPATH, v, overwrite);
	}
	/**
	* Modify  the SystemClasspath of the Element java-config
	* @param v the new value
	*/
	public void setSystemClasspath(String v) {
		setAttributeValue(ServerTags.SYSTEM_CLASSPATH, v);
	}
	/**
	* Getter for NativeLibraryPathPrefix of the Element java-config
	* @return  the NativeLibraryPathPrefix of the Element java-config
	*/
	public String getNativeLibraryPathPrefix() {
			return getAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_PREFIX);
	}
	/**
	* Modify  the NativeLibraryPathPrefix of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNativeLibraryPathPrefix(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_PREFIX, v, overwrite);
	}
	/**
	* Modify  the NativeLibraryPathPrefix of the Element java-config
	* @param v the new value
	*/
	public void setNativeLibraryPathPrefix(String v) {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_PREFIX, v);
	}
	/**
	* Getter for NativeLibraryPathSuffix of the Element java-config
	* @return  the NativeLibraryPathSuffix of the Element java-config
	*/
	public String getNativeLibraryPathSuffix() {
			return getAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_SUFFIX);
	}
	/**
	* Modify  the NativeLibraryPathSuffix of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setNativeLibraryPathSuffix(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_SUFFIX, v, overwrite);
	}
	/**
	* Modify  the NativeLibraryPathSuffix of the Element java-config
	* @param v the new value
	*/
	public void setNativeLibraryPathSuffix(String v) {
		setAttributeValue(ServerTags.NATIVE_LIBRARY_PATH_SUFFIX, v);
	}
	/**
	* Getter for BytecodePreprocessors of the Element java-config
	* @return  the BytecodePreprocessors of the Element java-config
	*/
	public String getBytecodePreprocessors() {
			return getAttributeValue(ServerTags.BYTECODE_PREPROCESSORS);
	}
	/**
	* Modify  the BytecodePreprocessors of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setBytecodePreprocessors(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.BYTECODE_PREPROCESSORS, v, overwrite);
	}
	/**
	* Modify  the BytecodePreprocessors of the Element java-config
	* @param v the new value
	*/
	public void setBytecodePreprocessors(String v) {
		setAttributeValue(ServerTags.BYTECODE_PREPROCESSORS, v);
	}
	/**
	* Getter for EnvClasspathIgnored of the Element java-config
	* @return  the EnvClasspathIgnored of the Element java-config
	*/
	public boolean isEnvClasspathIgnored() {
		return toBoolean(getAttributeValue(ServerTags.ENV_CLASSPATH_IGNORED));
	}
	/**
	* Modify  the EnvClasspathIgnored of the Element java-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnvClasspathIgnored(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENV_CLASSPATH_IGNORED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the EnvClasspathIgnored of the Element java-config
	* @param v the new value
	*/
	public void setEnvClasspathIgnored(boolean v) {
		setAttributeValue(ServerTags.ENV_CLASSPATH_IGNORED, ""+(v==true));
	}
	/**
	* Get the default value of EnvClasspathIgnored from dtd
	*/
	public static String getDefaultEnvClasspathIgnored() {
		return "true".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Profiler newProfiler() {
		return new Profiler();
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
	    ret = "java-config";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.JAVA_HOME)) return "${com.sun.aas.javaRoot}".trim();
		if(attr.equals(ServerTags.DEBUG_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.DEBUG_OPTIONS)) return "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n".trim();
		if(attr.equals(ServerTags.RMIC_OPTIONS)) return "-iiop -poa -alwaysgenerate -keepgenerated -g".trim();
		if(attr.equals(ServerTags.JAVAC_OPTIONS)) return "-g".trim();
		if(attr.equals(ServerTags.ENV_CLASSPATH_IGNORED)) return "true".trim();
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
		str.append("Profiler");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getProfiler();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(PROFILER, 0, str, indent);

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
		str.append("JavaConfig\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

