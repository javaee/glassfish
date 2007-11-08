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
 *	This generated bean class DiagnosticService matches the DTD element diagnostic-service
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

public class DiagnosticService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public DiagnosticService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public DiagnosticService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(1);
		this.createProperty("property", ELEMENT_PROPERTY, 
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
			throw new ConfigException(StringManager.getManager(DiagnosticService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for ComputeChecksum of the Element diagnostic-service
	* @return  the ComputeChecksum of the Element diagnostic-service
	*/
	public boolean isComputeChecksum() {
		return toBoolean(getAttributeValue(ServerTags.COMPUTE_CHECKSUM));
	}
	/**
	* Modify  the ComputeChecksum of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setComputeChecksum(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.COMPUTE_CHECKSUM, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ComputeChecksum of the Element diagnostic-service
	* @param v the new value
	*/
	public void setComputeChecksum(boolean v) {
		setAttributeValue(ServerTags.COMPUTE_CHECKSUM, ""+(v==true));
	}
	/**
	* Get the default value of ComputeChecksum from dtd
	*/
	public static String getDefaultComputeChecksum() {
		return "true".trim();
	}
	/**
	* Getter for VerifyConfig of the Element diagnostic-service
	* @return  the VerifyConfig of the Element diagnostic-service
	*/
	public boolean isVerifyConfig() {
		return toBoolean(getAttributeValue(ServerTags.VERIFY_CONFIG));
	}
	/**
	* Modify  the VerifyConfig of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setVerifyConfig(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VERIFY_CONFIG, ""+(v==true), overwrite);
	}
	/**
	* Modify  the VerifyConfig of the Element diagnostic-service
	* @param v the new value
	*/
	public void setVerifyConfig(boolean v) {
		setAttributeValue(ServerTags.VERIFY_CONFIG, ""+(v==true));
	}
	/**
	* Get the default value of VerifyConfig from dtd
	*/
	public static String getDefaultVerifyConfig() {
		return "true".trim();
	}
	/**
	* Getter for CaptureInstallLog of the Element diagnostic-service
	* @return  the CaptureInstallLog of the Element diagnostic-service
	*/
	public boolean isCaptureInstallLog() {
		return toBoolean(getAttributeValue(ServerTags.CAPTURE_INSTALL_LOG));
	}
	/**
	* Modify  the CaptureInstallLog of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCaptureInstallLog(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CAPTURE_INSTALL_LOG, ""+(v==true), overwrite);
	}
	/**
	* Modify  the CaptureInstallLog of the Element diagnostic-service
	* @param v the new value
	*/
	public void setCaptureInstallLog(boolean v) {
		setAttributeValue(ServerTags.CAPTURE_INSTALL_LOG, ""+(v==true));
	}
	/**
	* Get the default value of CaptureInstallLog from dtd
	*/
	public static String getDefaultCaptureInstallLog() {
		return "true".trim();
	}
	/**
	* Getter for CaptureSystemInfo of the Element diagnostic-service
	* @return  the CaptureSystemInfo of the Element diagnostic-service
	*/
	public boolean isCaptureSystemInfo() {
		return toBoolean(getAttributeValue(ServerTags.CAPTURE_SYSTEM_INFO));
	}
	/**
	* Modify  the CaptureSystemInfo of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCaptureSystemInfo(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CAPTURE_SYSTEM_INFO, ""+(v==true), overwrite);
	}
	/**
	* Modify  the CaptureSystemInfo of the Element diagnostic-service
	* @param v the new value
	*/
	public void setCaptureSystemInfo(boolean v) {
		setAttributeValue(ServerTags.CAPTURE_SYSTEM_INFO, ""+(v==true));
	}
	/**
	* Get the default value of CaptureSystemInfo from dtd
	*/
	public static String getDefaultCaptureSystemInfo() {
		return "true".trim();
	}
	/**
	* Getter for CaptureHadbInfo of the Element diagnostic-service
	* @return  the CaptureHadbInfo of the Element diagnostic-service
	*/
	public boolean isCaptureHadbInfo() {
		return toBoolean(getAttributeValue(ServerTags.CAPTURE_HADB_INFO));
	}
	/**
	* Modify  the CaptureHadbInfo of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCaptureHadbInfo(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CAPTURE_HADB_INFO, ""+(v==true), overwrite);
	}
	/**
	* Modify  the CaptureHadbInfo of the Element diagnostic-service
	* @param v the new value
	*/
	public void setCaptureHadbInfo(boolean v) {
		setAttributeValue(ServerTags.CAPTURE_HADB_INFO, ""+(v==true));
	}
	/**
	* Get the default value of CaptureHadbInfo from dtd
	*/
	public static String getDefaultCaptureHadbInfo() {
		return "true".trim();
	}
	/**
	* Getter for CaptureAppDd of the Element diagnostic-service
	* @return  the CaptureAppDd of the Element diagnostic-service
	*/
	public boolean isCaptureAppDd() {
		return toBoolean(getAttributeValue(ServerTags.CAPTURE_APP_DD));
	}
	/**
	* Modify  the CaptureAppDd of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setCaptureAppDd(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CAPTURE_APP_DD, ""+(v==true), overwrite);
	}
	/**
	* Modify  the CaptureAppDd of the Element diagnostic-service
	* @param v the new value
	*/
	public void setCaptureAppDd(boolean v) {
		setAttributeValue(ServerTags.CAPTURE_APP_DD, ""+(v==true));
	}
	/**
	* Get the default value of CaptureAppDd from dtd
	*/
	public static String getDefaultCaptureAppDd() {
		return "true".trim();
	}
	/**
	* Getter for MinLogLevel of the Element diagnostic-service
	* @return  the MinLogLevel of the Element diagnostic-service
	*/
	public String getMinLogLevel() {
		return getAttributeValue(ServerTags.MIN_LOG_LEVEL);
	}
	/**
	* Modify  the MinLogLevel of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMinLogLevel(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MIN_LOG_LEVEL, v, overwrite);
	}
	/**
	* Modify  the MinLogLevel of the Element diagnostic-service
	* @param v the new value
	*/
	public void setMinLogLevel(String v) {
		setAttributeValue(ServerTags.MIN_LOG_LEVEL, v);
	}
	/**
	* Get the default value of MinLogLevel from dtd
	*/
	public static String getDefaultMinLogLevel() {
		return "INFO".trim();
	}
	/**
	* Getter for MaxLogEntries of the Element diagnostic-service
	* @return  the MaxLogEntries of the Element diagnostic-service
	*/
	public String getMaxLogEntries() {
		return getAttributeValue(ServerTags.MAX_LOG_ENTRIES);
	}
	/**
	* Modify  the MaxLogEntries of the Element diagnostic-service
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxLogEntries(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_LOG_ENTRIES, v, overwrite);
	}
	/**
	* Modify  the MaxLogEntries of the Element diagnostic-service
	* @param v the new value
	*/
	public void setMaxLogEntries(String v) {
		setAttributeValue(ServerTags.MAX_LOG_ENTRIES, v);
	}
	/**
	* Get the default value of MaxLogEntries from dtd
	*/
	public static String getDefaultMaxLogEntries() {
		return "500".trim();
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
	    ret = "diagnostic-service";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.COMPUTE_CHECKSUM)) return "true".trim();
		if(attr.equals(ServerTags.VERIFY_CONFIG)) return "true".trim();
		if(attr.equals(ServerTags.CAPTURE_INSTALL_LOG)) return "true".trim();
		if(attr.equals(ServerTags.CAPTURE_SYSTEM_INFO)) return "true".trim();
		if(attr.equals(ServerTags.CAPTURE_HADB_INFO)) return "true".trim();
		if(attr.equals(ServerTags.CAPTURE_APP_DD)) return "true".trim();
		if(attr.equals(ServerTags.MIN_LOG_LEVEL)) return "INFO".trim();
		if(attr.equals(ServerTags.MAX_LOG_ENTRIES)) return "500".trim();
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
		str.append("DiagnosticService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

