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
 *	This generated bean class HttpService matches the DTD element http-service
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

public class HttpService extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String ACCESS_LOG = "AccessLog";
	static public final String HTTP_LISTENER = "HttpListener";
	static public final String VIRTUAL_SERVER = "VirtualServer";
	static public final String REQUEST_PROCESSING = "RequestProcessing";
	static public final String KEEP_ALIVE = "KeepAlive";
	static public final String CONNECTION_POOL = "ConnectionPool";
	static public final String HTTP_PROTOCOL = "HttpProtocol";
	static public final String HTTP_FILE_CACHE = "HttpFileCache";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public HttpService() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public HttpService(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(9);
		this.createProperty("access-log", ACCESS_LOG, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			AccessLog.class);
		this.createAttribute(ACCESS_LOG, "format", "Format", 
						AttrProp.CDATA,
						null, "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%");
		this.createAttribute(ACCESS_LOG, "rotation-policy", "RotationPolicy", 
						AttrProp.CDATA,
						null, "time");
		this.createAttribute(ACCESS_LOG, "rotation-interval-in-minutes", "RotationIntervalInMinutes", 
						AttrProp.CDATA,
						null, "1440");
		this.createAttribute(ACCESS_LOG, "rotation-suffix", "RotationSuffix", 
						AttrProp.CDATA,
						null, "yyyyMMdd-HH'h'mm'm'ss's'");
		this.createAttribute(ACCESS_LOG, "rotation-enabled", "RotationEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("http-listener", HTTP_LISTENER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HttpListener.class);
		this.createAttribute(HTTP_LISTENER, "id", "Id", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "address", "Address", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "port", "Port", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "external-port", "ExternalPort", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "family", "Family", 
						AttrProp.CDATA,
						null, "inet");
		this.createAttribute(HTTP_LISTENER, "blocking-enabled", "BlockingEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(HTTP_LISTENER, "acceptor-threads", "AcceptorThreads", 
						AttrProp.CDATA,
						null, "1");
		this.createAttribute(HTTP_LISTENER, "security-enabled", "SecurityEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(HTTP_LISTENER, "default-virtual-server", "DefaultVirtualServer", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "server-name", "ServerName", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "redirect-port", "RedirectPort", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(HTTP_LISTENER, "xpowered-by", "XpoweredBy", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(HTTP_LISTENER, "enabled", "Enabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("virtual-server", VIRTUAL_SERVER, 
			Common.TYPE_1_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			VirtualServer.class);
		this.createAttribute(VIRTUAL_SERVER, "id", "Id", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(VIRTUAL_SERVER, "http-listeners", "HttpListeners", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(VIRTUAL_SERVER, "default-web-module", "DefaultWebModule", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(VIRTUAL_SERVER, "hosts", "Hosts", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(VIRTUAL_SERVER, "state", "State", 
						AttrProp.CDATA,
						null, "on");
		this.createAttribute(VIRTUAL_SERVER, "docroot", "Docroot", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(VIRTUAL_SERVER, "log-file", "LogFile", 
						AttrProp.CDATA,
						null, "${com.sun.aas.instanceRoot}/logs/server.log");
		this.createProperty("request-processing", REQUEST_PROCESSING, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RequestProcessing.class);
		this.createAttribute(REQUEST_PROCESSING, "thread-count", "ThreadCount", 
						AttrProp.CDATA,
						null, "128");
		this.createAttribute(REQUEST_PROCESSING, "initial-thread-count", "InitialThreadCount", 
						AttrProp.CDATA,
						null, "48");
		this.createAttribute(REQUEST_PROCESSING, "thread-increment", "ThreadIncrement", 
						AttrProp.CDATA,
						null, "10");
		this.createAttribute(REQUEST_PROCESSING, "request-timeout-in-seconds", "RequestTimeoutInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(REQUEST_PROCESSING, "header-buffer-length-in-bytes", "HeaderBufferLengthInBytes", 
						AttrProp.CDATA,
						null, "4096");
		this.createProperty("keep-alive", KEEP_ALIVE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			KeepAlive.class);
		this.createAttribute(KEEP_ALIVE, "thread-count", "ThreadCount", 
						AttrProp.CDATA,
						null, "1");
		this.createAttribute(KEEP_ALIVE, "max-connections", "MaxConnections", 
						AttrProp.CDATA,
						null, "256");
		this.createAttribute(KEEP_ALIVE, "timeout-in-seconds", "TimeoutInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createProperty("connection-pool", CONNECTION_POOL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ConnectionPool.class);
		this.createAttribute(CONNECTION_POOL, "queue-size-in-bytes", "QueueSizeInBytes", 
						AttrProp.CDATA,
						null, "4096");
		this.createAttribute(CONNECTION_POOL, "max-pending-count", "MaxPendingCount", 
						AttrProp.CDATA,
						null, "4096");
		this.createAttribute(CONNECTION_POOL, "receive-buffer-size-in-bytes", "ReceiveBufferSizeInBytes", 
						AttrProp.CDATA,
						null, "4096");
		this.createAttribute(CONNECTION_POOL, "send-buffer-size-in-bytes", "SendBufferSizeInBytes", 
						AttrProp.CDATA,
						null, "8192");
		this.createProperty("http-protocol", HTTP_PROTOCOL, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HttpProtocol.class);
		this.createAttribute(HTTP_PROTOCOL, "version", "Version", 
						AttrProp.CDATA,
						null, "HTTP/1.1");
		this.createAttribute(HTTP_PROTOCOL, "dns-lookup-enabled", "DnsLookupEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(HTTP_PROTOCOL, "forced-type", "ForcedType", 
						AttrProp.CDATA,
						null, "text/html; charset=iso-8859-1");
		this.createAttribute(HTTP_PROTOCOL, "default-type", "DefaultType", 
						AttrProp.CDATA,
						null, "text/html; charset=iso-8859-1");
		this.createAttribute(HTTP_PROTOCOL, "forced-response-type", "ForcedResponseType", 
						AttrProp.CDATA,
						null, "AttributeDeprecated");
		this.createAttribute(HTTP_PROTOCOL, "default-response-type", "DefaultResponseType", 
						AttrProp.CDATA,
						null, "AttributeDeprecated");
		this.createAttribute(HTTP_PROTOCOL, "ssl-enabled", "SslEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createProperty("http-file-cache", HTTP_FILE_CACHE, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			HttpFileCache.class);
		this.createAttribute(HTTP_FILE_CACHE, "globally-enabled", "GloballyEnabled", 
						AttrProp.CDATA,
						null, "true");
		this.createAttribute(HTTP_FILE_CACHE, "file-caching-enabled", "FileCachingEnabled", 
						AttrProp.CDATA,
						null, "on");
		this.createAttribute(HTTP_FILE_CACHE, "max-age-in-seconds", "MaxAgeInSeconds", 
						AttrProp.CDATA,
						null, "30");
		this.createAttribute(HTTP_FILE_CACHE, "medium-file-size-limit-in-bytes", "MediumFileSizeLimitInBytes", 
						AttrProp.CDATA,
						null, "537600");
		this.createAttribute(HTTP_FILE_CACHE, "medium-file-space-in-bytes", "MediumFileSpaceInBytes", 
						AttrProp.CDATA,
						null, "10485760");
		this.createAttribute(HTTP_FILE_CACHE, "small-file-size-limit-in-bytes", "SmallFileSizeLimitInBytes", 
						AttrProp.CDATA,
						null, "2048");
		this.createAttribute(HTTP_FILE_CACHE, "small-file-space-in-bytes", "SmallFileSpaceInBytes", 
						AttrProp.CDATA,
						null, "1048576");
		this.createAttribute(HTTP_FILE_CACHE, "file-transmission-enabled", "FileTransmissionEnabled", 
						AttrProp.CDATA,
						null, "false");
		this.createAttribute(HTTP_FILE_CACHE, "max-files-count", "MaxFilesCount", 
						AttrProp.CDATA,
						null, "1024");
		this.createAttribute(HTTP_FILE_CACHE, "hash-init-size", "HashInitSize", 
						AttrProp.CDATA,
						null, "0");
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

	// This attribute is optional
	public void setAccessLog(AccessLog value) {
		this.setValue(ACCESS_LOG, value);
	}

	// Get Method
	public AccessLog getAccessLog() {
		return (AccessLog)this.getValue(ACCESS_LOG);
	}

	// Get Method
	public HttpListener getHttpListener(int index) {
		return (HttpListener)this.getValue(HTTP_LISTENER, index);
	}

	// This attribute is an array containing at least one element
	public void setHttpListener(HttpListener[] value) {
		this.setValue(HTTP_LISTENER, value);
	}

	// Getter Method
	public HttpListener[] getHttpListener() {
		return (HttpListener[])this.getValues(HTTP_LISTENER);
	}

	// Return the number of properties
	public int sizeHttpListener() {
		return this.size(HTTP_LISTENER);
	}

	// Add a new element returning its index in the list
	public int addHttpListener(HttpListener value)
			throws ConfigException{
		return addHttpListener(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addHttpListener(HttpListener value, boolean overwrite)
			throws ConfigException{
		HttpListener old = getHttpListenerById(value.getId());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(HttpService.class).getString("cannotAddDuplicate",  "HttpListener"));
		}
		return this.addValue(HTTP_LISTENER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeHttpListener(HttpListener value){
		return this.removeValue(HTTP_LISTENER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeHttpListener(HttpListener value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(HTTP_LISTENER, value, overwrite);
	}

	public HttpListener getHttpListenerById(String id) {
	 if (null != id) { id = id.trim(); }
	HttpListener[] o = getHttpListener();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.ID)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// Get Method
	public VirtualServer getVirtualServer(int index) {
		return (VirtualServer)this.getValue(VIRTUAL_SERVER, index);
	}

	// This attribute is an array containing at least one element
	public void setVirtualServer(VirtualServer[] value) {
		this.setValue(VIRTUAL_SERVER, value);
	}

	// Getter Method
	public VirtualServer[] getVirtualServer() {
		return (VirtualServer[])this.getValues(VIRTUAL_SERVER);
	}

	// Return the number of properties
	public int sizeVirtualServer() {
		return this.size(VIRTUAL_SERVER);
	}

	// Add a new element returning its index in the list
	public int addVirtualServer(VirtualServer value)
			throws ConfigException{
		return addVirtualServer(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addVirtualServer(VirtualServer value, boolean overwrite)
			throws ConfigException{
		VirtualServer old = getVirtualServerById(value.getId());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(HttpService.class).getString("cannotAddDuplicate",  "VirtualServer"));
		}
		return this.addValue(VIRTUAL_SERVER, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeVirtualServer(VirtualServer value){
		return this.removeValue(VIRTUAL_SERVER, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeVirtualServer(VirtualServer value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(VIRTUAL_SERVER, value, overwrite);
	}

	public VirtualServer getVirtualServerById(String id) {
	 if (null != id) { id = id.trim(); }
	VirtualServer[] o = getVirtualServer();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.ID)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	// This attribute is optional
	public void setRequestProcessing(RequestProcessing value) {
		this.setValue(REQUEST_PROCESSING, value);
	}

	// Get Method
	public RequestProcessing getRequestProcessing() {
		return (RequestProcessing)this.getValue(REQUEST_PROCESSING);
	}

	// This attribute is optional
	public void setKeepAlive(KeepAlive value) {
		this.setValue(KEEP_ALIVE, value);
	}

	// Get Method
	public KeepAlive getKeepAlive() {
		return (KeepAlive)this.getValue(KEEP_ALIVE);
	}

	// This attribute is optional
	public void setConnectionPool(ConnectionPool value) {
		this.setValue(CONNECTION_POOL, value);
	}

	// Get Method
	public ConnectionPool getConnectionPool() {
		return (ConnectionPool)this.getValue(CONNECTION_POOL);
	}

	// This attribute is optional
	public void setHttpProtocol(HttpProtocol value) {
		this.setValue(HTTP_PROTOCOL, value);
	}

	// Get Method
	public HttpProtocol getHttpProtocol() {
		return (HttpProtocol)this.getValue(HTTP_PROTOCOL);
	}

	// This attribute is optional
	public void setHttpFileCache(HttpFileCache value) {
		this.setValue(HTTP_FILE_CACHE, value);
	}

	// Get Method
	public HttpFileCache getHttpFileCache() {
		return (HttpFileCache)this.getValue(HTTP_FILE_CACHE);
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
			throw new ConfigException(StringManager.getManager(HttpService.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public AccessLog newAccessLog() {
		return new AccessLog();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public HttpListener newHttpListener() {
		return new HttpListener();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public VirtualServer newVirtualServer() {
		return new VirtualServer();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RequestProcessing newRequestProcessing() {
		return new RequestProcessing();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public KeepAlive newKeepAlive() {
		return new KeepAlive();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ConnectionPool newConnectionPool() {
		return new ConnectionPool();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public HttpProtocol newHttpProtocol() {
		return new HttpProtocol();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public HttpFileCache newHttpFileCache() {
		return new HttpFileCache();
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
	    ret = "http-service";
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
		str.append("AccessLog");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getAccessLog();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(ACCESS_LOG, 0, str, indent);

		str.append(indent);
		str.append("HttpListener["+this.sizeHttpListener()+"]");	// NOI18N
		for(int i=0; i<this.sizeHttpListener(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getHttpListener(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(HTTP_LISTENER, i, str, indent);
		}

		str.append(indent);
		str.append("VirtualServer["+this.sizeVirtualServer()+"]");	// NOI18N
		for(int i=0; i<this.sizeVirtualServer(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getVirtualServer(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(VIRTUAL_SERVER, i, str, indent);
		}

		str.append(indent);
		str.append("RequestProcessing");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getRequestProcessing();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(REQUEST_PROCESSING, 0, str, indent);

		str.append(indent);
		str.append("KeepAlive");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getKeepAlive();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(KEEP_ALIVE, 0, str, indent);

		str.append(indent);
		str.append("ConnectionPool");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getConnectionPool();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(CONNECTION_POOL, 0, str, indent);

		str.append(indent);
		str.append("HttpProtocol");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getHttpProtocol();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(HTTP_PROTOCOL, 0, str, indent);

		str.append(indent);
		str.append("HttpFileCache");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getHttpFileCache();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(HTTP_FILE_CACHE, 0, str, indent);

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
		str.append("HttpService\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N

