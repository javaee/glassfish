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
 *	This generated bean class HttpFileCache matches the DTD element http-file-cache
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

public class HttpFileCache extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public HttpFileCache() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public HttpFileCache(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(0);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	/**
	* Getter for GloballyEnabled of the Element http-file-cache
	* @return  the GloballyEnabled of the Element http-file-cache
	*/
	public boolean isGloballyEnabled() {
		return toBoolean(getAttributeValue(ServerTags.GLOBALLY_ENABLED));
	}
	/**
	* Modify  the GloballyEnabled of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setGloballyEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.GLOBALLY_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the GloballyEnabled of the Element http-file-cache
	* @param v the new value
	*/
	public void setGloballyEnabled(boolean v) {
		setAttributeValue(ServerTags.GLOBALLY_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of GloballyEnabled from dtd
	*/
	public static String getDefaultGloballyEnabled() {
		return "true".trim();
	}
	/**
	* Getter for FileCachingEnabled of the Element http-file-cache
	* @return  the FileCachingEnabled of the Element http-file-cache
	*/
	public String getFileCachingEnabled() {
		return getAttributeValue(ServerTags.FILE_CACHING_ENABLED);
	}
	/**
	* Modify  the FileCachingEnabled of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFileCachingEnabled(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FILE_CACHING_ENABLED, v, overwrite);
	}
	/**
	* Modify  the FileCachingEnabled of the Element http-file-cache
	* @param v the new value
	*/
	public void setFileCachingEnabled(String v) {
		setAttributeValue(ServerTags.FILE_CACHING_ENABLED, v);
	}
	/**
	* Get the default value of FileCachingEnabled from dtd
	*/
	public static String getDefaultFileCachingEnabled() {
		return "on".trim();
	}
	/**
	* Getter for MaxAgeInSeconds of the Element http-file-cache
	* @return  the MaxAgeInSeconds of the Element http-file-cache
	*/
	public String getMaxAgeInSeconds() {
		return getAttributeValue(ServerTags.MAX_AGE_IN_SECONDS);
	}
	/**
	* Modify  the MaxAgeInSeconds of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxAgeInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_AGE_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the MaxAgeInSeconds of the Element http-file-cache
	* @param v the new value
	*/
	public void setMaxAgeInSeconds(String v) {
		setAttributeValue(ServerTags.MAX_AGE_IN_SECONDS, v);
	}
	/**
	* Get the default value of MaxAgeInSeconds from dtd
	*/
	public static String getDefaultMaxAgeInSeconds() {
		return "30".trim();
	}
	/**
	* Getter for MediumFileSizeLimitInBytes of the Element http-file-cache
	* @return  the MediumFileSizeLimitInBytes of the Element http-file-cache
	*/
	public String getMediumFileSizeLimitInBytes() {
		return getAttributeValue(ServerTags.MEDIUM_FILE_SIZE_LIMIT_IN_BYTES);
	}
	/**
	* Modify  the MediumFileSizeLimitInBytes of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMediumFileSizeLimitInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MEDIUM_FILE_SIZE_LIMIT_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the MediumFileSizeLimitInBytes of the Element http-file-cache
	* @param v the new value
	*/
	public void setMediumFileSizeLimitInBytes(String v) {
		setAttributeValue(ServerTags.MEDIUM_FILE_SIZE_LIMIT_IN_BYTES, v);
	}
	/**
	* Get the default value of MediumFileSizeLimitInBytes from dtd
	*/
	public static String getDefaultMediumFileSizeLimitInBytes() {
		return "537600".trim();
	}
	/**
	* Getter for MediumFileSpaceInBytes of the Element http-file-cache
	* @return  the MediumFileSpaceInBytes of the Element http-file-cache
	*/
	public String getMediumFileSpaceInBytes() {
		return getAttributeValue(ServerTags.MEDIUM_FILE_SPACE_IN_BYTES);
	}
	/**
	* Modify  the MediumFileSpaceInBytes of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMediumFileSpaceInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MEDIUM_FILE_SPACE_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the MediumFileSpaceInBytes of the Element http-file-cache
	* @param v the new value
	*/
	public void setMediumFileSpaceInBytes(String v) {
		setAttributeValue(ServerTags.MEDIUM_FILE_SPACE_IN_BYTES, v);
	}
	/**
	* Get the default value of MediumFileSpaceInBytes from dtd
	*/
	public static String getDefaultMediumFileSpaceInBytes() {
		return "10485760".trim();
	}
	/**
	* Getter for SmallFileSizeLimitInBytes of the Element http-file-cache
	* @return  the SmallFileSizeLimitInBytes of the Element http-file-cache
	*/
	public String getSmallFileSizeLimitInBytes() {
		return getAttributeValue(ServerTags.SMALL_FILE_SIZE_LIMIT_IN_BYTES);
	}
	/**
	* Modify  the SmallFileSizeLimitInBytes of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSmallFileSizeLimitInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SMALL_FILE_SIZE_LIMIT_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the SmallFileSizeLimitInBytes of the Element http-file-cache
	* @param v the new value
	*/
	public void setSmallFileSizeLimitInBytes(String v) {
		setAttributeValue(ServerTags.SMALL_FILE_SIZE_LIMIT_IN_BYTES, v);
	}
	/**
	* Get the default value of SmallFileSizeLimitInBytes from dtd
	*/
	public static String getDefaultSmallFileSizeLimitInBytes() {
		return "2048".trim();
	}
	/**
	* Getter for SmallFileSpaceInBytes of the Element http-file-cache
	* @return  the SmallFileSpaceInBytes of the Element http-file-cache
	*/
	public String getSmallFileSpaceInBytes() {
		return getAttributeValue(ServerTags.SMALL_FILE_SPACE_IN_BYTES);
	}
	/**
	* Modify  the SmallFileSpaceInBytes of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSmallFileSpaceInBytes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.SMALL_FILE_SPACE_IN_BYTES, v, overwrite);
	}
	/**
	* Modify  the SmallFileSpaceInBytes of the Element http-file-cache
	* @param v the new value
	*/
	public void setSmallFileSpaceInBytes(String v) {
		setAttributeValue(ServerTags.SMALL_FILE_SPACE_IN_BYTES, v);
	}
	/**
	* Get the default value of SmallFileSpaceInBytes from dtd
	*/
	public static String getDefaultSmallFileSpaceInBytes() {
		return "1048576".trim();
	}
	/**
	* Getter for FileTransmissionEnabled of the Element http-file-cache
	* @return  the FileTransmissionEnabled of the Element http-file-cache
	*/
	public boolean isFileTransmissionEnabled() {
		return toBoolean(getAttributeValue(ServerTags.FILE_TRANSMISSION_ENABLED));
	}
	/**
	* Modify  the FileTransmissionEnabled of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFileTransmissionEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FILE_TRANSMISSION_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the FileTransmissionEnabled of the Element http-file-cache
	* @param v the new value
	*/
	public void setFileTransmissionEnabled(boolean v) {
		setAttributeValue(ServerTags.FILE_TRANSMISSION_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of FileTransmissionEnabled from dtd
	*/
	public static String getDefaultFileTransmissionEnabled() {
		return "false".trim();
	}
	/**
	* Getter for MaxFilesCount of the Element http-file-cache
	* @return  the MaxFilesCount of the Element http-file-cache
	*/
	public String getMaxFilesCount() {
		return getAttributeValue(ServerTags.MAX_FILES_COUNT);
	}
	/**
	* Modify  the MaxFilesCount of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxFilesCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_FILES_COUNT, v, overwrite);
	}
	/**
	* Modify  the MaxFilesCount of the Element http-file-cache
	* @param v the new value
	*/
	public void setMaxFilesCount(String v) {
		setAttributeValue(ServerTags.MAX_FILES_COUNT, v);
	}
	/**
	* Get the default value of MaxFilesCount from dtd
	*/
	public static String getDefaultMaxFilesCount() {
		return "1024".trim();
	}
	/**
	* Getter for HashInitSize of the Element http-file-cache
	* @return  the HashInitSize of the Element http-file-cache
	*/
	public String getHashInitSize() {
		return getAttributeValue(ServerTags.HASH_INIT_SIZE);
	}
	/**
	* Modify  the HashInitSize of the Element http-file-cache
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setHashInitSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.HASH_INIT_SIZE, v, overwrite);
	}
	/**
	* Modify  the HashInitSize of the Element http-file-cache
	* @param v the new value
	*/
	public void setHashInitSize(String v) {
		setAttributeValue(ServerTags.HASH_INIT_SIZE, v);
	}
	/**
	* Get the default value of HashInitSize from dtd
	*/
	public static String getDefaultHashInitSize() {
		return "0".trim();
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "http-file-cache";
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.GLOBALLY_ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.FILE_CACHING_ENABLED)) return "on".trim();
		if(attr.equals(ServerTags.MAX_AGE_IN_SECONDS)) return "30".trim();
		if(attr.equals(ServerTags.MEDIUM_FILE_SIZE_LIMIT_IN_BYTES)) return "537600".trim();
		if(attr.equals(ServerTags.MEDIUM_FILE_SPACE_IN_BYTES)) return "10485760".trim();
		if(attr.equals(ServerTags.SMALL_FILE_SIZE_LIMIT_IN_BYTES)) return "2048".trim();
		if(attr.equals(ServerTags.SMALL_FILE_SPACE_IN_BYTES)) return "1048576".trim();
		if(attr.equals(ServerTags.FILE_TRANSMISSION_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.MAX_FILES_COUNT)) return "1024".trim();
		if(attr.equals(ServerTags.HASH_INIT_SIZE)) return "0".trim();
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
	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("HttpFileCache\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

