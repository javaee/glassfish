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

/*
 * IIOPListener.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;

import com.sun.enterprise.tools.upgrade.common.*;
import com.sun.enterprise.tools.upgrade.cluster.*;
import java.util.logging.Level;

public class GenericResource extends GenericElement {
	
	private boolean appendingResourceRefToCluster = false;
	private java.util.List insertStructureForResourceRefInCluster = null;
	private boolean appendJdbcResource = false;
	private boolean appendJdbcConnPool = false;
	/** Creates a new instance of Element */
	public GenericResource() {
	}
	/**
	 * element - one of the child of resources
	 * parentSource - resources element of source
	 * parentResult - resources element of result
	 */
	public void transform(Element element, Element parentSource, Element parentResult){
		String resourceTagName = element.getTagName();
		logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), resourceTagName));
		
		if(resourceTagName.equals("jms-resource")) {
			// There is this JMSResource class that handles it.
			this.skipGenericElementTransform(element, parentSource, parentResult);
			return;
		}
		
		if(resourceTagName.equals("jdbc-connection-pool")) {
			if(element.getAttribute("name").equals("__TimerPool") ||
				element.getAttribute("name").equals("PointBasePool"))
				return;
		}
		
		if(resourceTagName.equals("jdbc-resource")) {
			if(element.getAttribute("pool-name").equals("PointBasePool"))
				return;
		}
		super.transform(element, parentSource, parentResult);
	}
	protected void updateResourceRef(Element element, Element parentResult){		
		org.w3c.dom.Attr jndiAttr = element.getAttributeNode("jndi-name");
		if(jndiAttr != null){
			// parentResult is resources, its parent should be domain.  From there get servers and server element.
			NodeList servers = ((Element)parentResult.getParentNode()).getElementsByTagName("servers");
			NodeList serverList = ((Element)servers.item(0)).getElementsByTagName("server");
			// PE there can be only one server.
			if(commonInfoModel.isPlatformEdition(commonInfoModel.getSource().getEdition())){
				Element serverElement = ((Element)serverList.item(0));
				this.addOrUpdateResourceRef(serverElement, element, parentResult);
			}else{
				String serverName = "server";
				for(int lh =0; lh < serverList.getLength(); lh++){
					if(serverName.equals(((Element)serverList.item(lh)).getAttribute("name"))){
						this.addOrUpdateResourceRef((Element)serverList.item(lh), element, parentResult);
						break;
					}
				}
			}
		}
	}
	private void updateResourceRefsForCluster(String clusterName, NodeList serverRefList, Element element, Element parentResult){
		for(java.util.Iterator dItr = ClustersInfoManager.getClusterInfoManager().getClusterInfoList().iterator(); dItr.hasNext();){
			ClusterInfo cInfo = (ClusterInfo)dItr.next();
			if(cInfo.getClusterName().equals(clusterName)){
				for(java.util.Iterator clItr = cInfo.getClusteredInstanceList().iterator(); clItr.hasNext();){
					ClusteredInstance clInstance = (ClusteredInstance)clItr.next();
					String clInstanceName = clInstance.getInstanceName();
					for(int lh =0; lh < serverRefList.getLength(); lh++){
						if(clInstanceName.equals(((Element)serverRefList.item(lh)).getAttribute("name"))){
							this.addOrUpdateResourceRef((Element)serverRefList.item(lh), element, parentResult);
							break;
						}
					}
				}
			}
		}
		NodeList clusters = ((Element)parentResult.getParentNode()).getElementsByTagName("clusters");
		if((clusters != null) && (clusters.getLength() > 0)){
			NodeList clustersList = ((Element)clusters.item(0)).getElementsByTagName("cluster");
			for(int lh =0; lh < clustersList.getLength(); lh++){
				if(clusterName.equals(((Element)clustersList.item(lh)).getAttribute("name"))){
					this.appendingResourceRefToCluster = true;
					this.addOrUpdateResourceRef((Element)clustersList.item(lh), element, parentResult);
					this.appendingResourceRefToCluster = false;
					break;
				}
			}
		}
	}
	private void addOrUpdateResourceRef(Element parentForRef, Element element, Element parentResult){
		// Get resource refs.
		NodeList resourceRefs = parentForRef.getElementsByTagName("resource-ref");
		Element resourceRef = null;
		for(int lh =0; lh < resourceRefs.getLength(); lh++){
			// Compare one key attribute
			if((element.getAttribute("jndi-name")).equals(((Element)resourceRefs.item(lh)).getAttribute("ref"))){
				resourceRef = (Element)resourceRefs.item(lh);
				org.w3c.dom.Attr enAttr = element.getAttributeNode("enabled");
				if(enAttr != null){
					resourceRef.setAttribute("enabled", element.getAttribute("enabled"));
				}
				break;
			}
		}
		if(resourceRef == null){
			resourceRef = parentResult.getOwnerDocument().createElement("resource-ref");
			resourceRef.setAttribute("ref", element.getAttribute("jndi-name"));
			org.w3c.dom.Attr enAttr = element.getAttributeNode("enabled");
			if(enAttr != null){
				resourceRef.setAttribute("enabled", element.getAttribute("enabled"));
			}
			this.appendElementToParent(parentForRef,resourceRef);
		}
	}
	private void skipGenericElementTransform(Element element, Element parentSource, Element parentResult){
		NodeList childNodes = element.getChildNodes();
		for(int index=0; index < childNodes.getLength(); index++){
			Node aNode = childNodes.item(index);
			try{
				if(aNode.getNodeType() == Node.ELEMENT_NODE){
					BaseElement baseElement = ElementToObjectMapper.getMapper().getElementObject(aNode.getNodeName());
					baseElement.transform((Element)aNode, element, parentResult);
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	protected java.util.List getInsertElementStructure(Element element, Element parentEle){
		java.util.List insertStrucure = ElementToObjectMapper.getMapper().getInsertElementStructure(element.getTagName());
		if(element.getTagName().equals("resource-ref") && this.appendingResourceRefToCluster){
			if(this.insertStructureForResourceRefInCluster == null){
				this.insertStructureForResourceRefInCluster = new java.util.ArrayList();
				this.insertStructureForResourceRefInCluster.add("application-ref");
				this.insertStructureForResourceRefInCluster.add("system-property");
				this.insertStructureForResourceRefInCluster.add("property");
			}
			return this.insertStructureForResourceRefInCluster;
		}
		return insertStrucure;
	}
	
	/**
	 * Method to transfer the jdbc-connection-pool
	 *
	 *
	 */
	public void transferJdbcConnPool(Element element,
		Element parentSource, Element parentResult) {
		NodeList nodeList = parentResult.getElementsByTagName("jdbc-connection-pool");
		Element connPool = null;
		if( nodeList != null ) {
			for(int i=0; i<nodeList.getLength(); i++ ) {
				connPool = (Element)nodeList.item(i);
				if(connPool.getAttribute("name").equals("__TimerPool")) {
					modifyConnPoolProps(element,connPool);
					connPool.setAttribute("datasource-classname",
						"org.apache.derby.jdbc.ClientDataSource");
					
				}
			}
		}
	}
	/** This method modified the properties in a connection pool
	 * @param element - the connection pool elememt
	 * @param connPool - the Element representing the connection-pool in the
	 *                   result Document
	 */
	public void modifyConnPoolProps(Element element, Element connPool) {
		NodeList propList = connPool.getElementsByTagName("property");
		if(connPool.getAttribute("name").equals("__TimerPool")) {
			transferTimerPoolProps(element, propList, connPool);
		}
		
	}
	
	/** This method add properties to the connection pool.These properties need to
	 * be added for AS8.1 to 8.2 EE upgrade.
	 * @param element Element being processsed now
	 * @param parent Element in the source
	 * @param parent Element in the result which is the jdbc-connection-pool
	 */
	public void addPropertyToConnPool(Element element, Element parentSource,
		Element parentResult ) {
		
		// Add each propert
		Element serverNameProp =
			parentResult.getOwnerDocument().createElement("property");
		serverNameProp.setAttribute("name", "serverName");
		serverNameProp.setAttribute("value", "localhost");
		parentResult.appendChild(serverNameProp);
		
		Element portNumberProp =
			parentResult.getOwnerDocument().createElement("property");
		portNumberProp.setAttribute("name", "PortNumber");
		portNumberProp.setAttribute("value", "1527");
		parentResult.appendChild(portNumberProp);
		
		Element connSettingsProp =
			parentResult.getOwnerDocument().createElement("property");
		connSettingsProp.setAttribute("name",  "connectionAttributes");
		connSettingsProp.setAttribute("value", ";create=true");
		
		Element userProp =
			parentResult.getOwnerDocument().createElement("property");
		userProp.setAttribute("name", "User");
		userProp.setAttribute("value", "APP");
		parentResult.appendChild(userProp);
		
		Element passwordProp =
			parentResult.getOwnerDocument().createElement("property");
		passwordProp.setAttribute("name", "Password");
		passwordProp.setAttribute("value", "APP");
		parentResult.appendChild(passwordProp);
		
		Element databaseNameProp =
			parentResult.getOwnerDocument().createElement("property");
		databaseNameProp.setAttribute("name", "DatabaseName");
		databaseNameProp.setAttribute("value", "sun-appserv-samples");
		parentResult.appendChild(databaseNameProp);
		
	}
	
	/**
	 * This method updates the properties for the Timer Pool
	 * @param element the Element that represents the property that is being processed.
	 * @param propertyList the NodeList that contains the property elements under this pool
	 * @param parentResult the parent element in the result document     * This method updates the properties for the Derby Pool
	 */
	public void transferTimerPoolProps( Element element,
		NodeList propList, Element parentResult ) {
		for(int i=0; i<propList.getLength(); i++ ) {
			Element property = (Element)propList.item(i);
			if(property.getAttribute("name").equals("DatabaseName")) {
				property.setAttribute("value",
					"${com.sun.aas.instanceRoot}/lib/databases/ejbtimer");
				parentResult.appendChild(property);
			}
			if(property.getAttribute("name").equals("User") ) {
				property.setAttribute("value", "APP");
				parentResult.appendChild(property);
			}
			if(property.getAttribute("name").equals("Password") ){
				property.setAttribute("value", "APP");
				parentResult.appendChild(property);
			}
		}
	}
	
	/**
	 * This method add a JDBC Resource in the domain.xml that corresponds to
	 * the default Derby resource. It also adds a resource-ref in the servers and
	 * clusters
	 * @param The element that i being processed now : jdbc-resource
	 * @param The parent element of the jdbc-resource in the Source document
	 * @param The parent element of the jdbc-resource is the Result document
	 */
	
	public void addJdbcResource(Element element, Element parentSource,
		Element parentResult) {
		NodeList jdbcConnPool = parentResult.getElementsByTagName("jdbc-connection-pool");
		// First add the jdbc-resource
		Element jdbcResource =
			parentResult.getOwnerDocument().createElement("jdbc-resource");
		jdbcResource.setAttribute("enabled", "true");
		jdbcResource.setAttribute("jndi-name", "jdbc/__default");
		jdbcResource.setAttribute("object-type", "user");
		jdbcResource.setAttribute("pool-name", "DerbyPool");
		parentResult.insertBefore(jdbcResource, jdbcConnPool.item(0));		
		
		// Now add the resource ref
		// get the server element
		NodeList serverList =
			((Element)parentResult.getParentNode()).getElementsByTagName("servers");
		if(serverList != null && serverList.getLength() > 0 ) {
			// get the server elements from servers ( which will be a single occurence)
			NodeList servers =
				((Element)serverList.item(0)).getElementsByTagName("server");
			for(int sCount=0; sCount<servers.getLength(); sCount++) {
				Element resourceRef =
					servers.item(sCount).getOwnerDocument().createElement("resource-ref");
				resourceRef.setAttribute("enabled", "true");
				resourceRef.setAttribute("ref", "jdbc/__default");
				servers.item(sCount).appendChild(resourceRef);
			}
		}
		appendJdbcResource = true;
	}
	
	/** This method adds a jdbc-connection-pool corresponding to the DerbyPool
	 * which is the default in SJSAS8.2 EE . This method is used in AS8.1 EE to
	 * AS8.2EE upgrades
	 * @param element The element being worked on now
	 * @param parentSource the parent element in the Source document
	 * @param parentResult the parent element of the element being processed in the
	 *                     Result document
	 */
	
	public void addJdbcConnPool(Element element, Element parentSource,
		Element parentResult ) {
		Element jdbcConnPool =
			(Element)parentResult.getOwnerDocument().createElement("jdbc-connection-pool");
		NodeList connPools = parentResult.getElementsByTagName("jdbc-connection-pool");
		Element firstConnPool = (Element)connPools.item(0);
		
		// set all the attributes
		jdbcConnPool.setAttribute("connection-validation-method", "auto-commit");
		jdbcConnPool.setAttribute("datasource-classname","org.apache.derby.jdbc.ClientDataSource");
		jdbcConnPool.setAttribute("fail-all-connections", "false");
		jdbcConnPool.setAttribute("idle-timeout-in-seconds", "300");
		jdbcConnPool.setAttribute("is-connection-validation-required", "false");
		jdbcConnPool.setAttribute("is-isolation-level-guaranteed", "true");
		jdbcConnPool.setAttribute("max-pool-size", "32");
		jdbcConnPool.setAttribute("max-wait-time-in-millis", "60000");
		jdbcConnPool.setAttribute("name",
			"DerbyPool");
		jdbcConnPool.setAttribute("pool-resize-quantity", "2");
		jdbcConnPool.setAttribute("res-type", "javax.sql.XADataSource");
		jdbcConnPool.setAttribute("steady-pool-size", "8");
		
		// Add properties to the JDBC connection pool
		this.addPropertyToConnPool(element,  parentSource, jdbcConnPool);
		parentResult.insertBefore(jdbcConnPool, firstConnPool);
	}
}
