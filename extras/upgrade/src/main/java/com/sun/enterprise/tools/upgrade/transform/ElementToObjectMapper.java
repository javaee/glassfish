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
 * ElementToObjectMapper.java
 *
 * Created on August 4, 2003, 1:52 PM
 */

package com.sun.enterprise.tools.upgrade.transform;


import java.util.HashMap;
import com.sun.enterprise.tools.upgrade.transform.elements.BaseElement;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.LogService;

/**
 *
 * @author  prakash
 */
public class ElementToObjectMapper {
	
	private static ElementToObjectMapper mapper;
	private HashMap elementMap;
	private HashMap keyMap;
	private HashMap elementStructureMap;
	////// StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.transform");
        StringManager stringManager = StringManager.getManager(ElementToObjectMapper.class);
	/** Creates a new instance of ElementToObjectMapper */
	private ElementToObjectMapper() {
		buildMapping();
	}
	public static ElementToObjectMapper getMapper(){
		if(mapper == null)
			mapper = new ElementToObjectMapper();
		return mapper;
	}
	public BaseElement getElementObject(String element) throws ElementNotFoundException, ClassNotFoundException,
		InstantiationException, IllegalAccessException{
		String eleClassName = (String)(elementMap.get(element));
		
		if (eleClassName == null)
			throw new ElementNotFoundException(element+" "+stringManager.getString("upgrade.transform.elementObjectMapper.elementNotFound"));
		return (BaseElement)((Class.forName(eleClassName)).newInstance());
	}
	public String getKeyForElement(String element) {
		return (String)(keyMap.get(element));
	}
	public java.util.List getInsertElementStructure(String element){
		return (java.util.List)(this.elementStructureMap.get(element));
	}
	private void buildMapping(){
		if(elementMap == null){
			elementMap = new HashMap();
		}
		buildKeyMapping();
		buildElementStructureMap();
		elementMap.put("server", "com.sun.enterprise.tools.upgrade.transform.elements.Server");
		elementMap.put("http-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("admin-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("web-container", "com.sun.enterprise.tools.upgrade.transform.elements.GenericContainer");
		elementMap.put("ejb-container", "com.sun.enterprise.tools.upgrade.transform.elements.GenericContainer");
		elementMap.put("mdb-container", "com.sun.enterprise.tools.upgrade.transform.elements.GenericContainer");
		elementMap.put("jms-service", "com.sun.enterprise.tools.upgrade.transform.elements.JMSService");
		elementMap.put("log-service", "com.sun.enterprise.tools.upgrade.transform.elements.LogService");
		elementMap.put("security-service", "com.sun.enterprise.tools.upgrade.transform.elements.SecurityService");
		elementMap.put("transaction-service", "com.sun.enterprise.tools.upgrade.transform.elements.TransactionService");
		elementMap.put("java-config", "com.sun.enterprise.tools.upgrade.transform.elements.JavaConfig");
		elementMap.put("resources", "com.sun.enterprise.tools.upgrade.transform.elements.Resources");
		elementMap.put("applications", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("lifecycle-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("j2ee-application", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("ejb-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("web-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("connector-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("http-listener", "com.sun.enterprise.tools.upgrade.transform.elements.HttpListener");
		elementMap.put("mime", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("acl", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("virtual-server-class", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("ssl", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("property", "com.sun.enterprise.tools.upgrade.transform.elements.Property");
		elementMap.put("description", "com.sun.enterprise.tools.upgrade.transform.elements.Description");
		elementMap.put("virtual-server", "com.sun.enterprise.tools.upgrade.transform.elements.VirtualServer");
		elementMap.put("http-qos", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("auth-db", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("iiop-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("orb", "com.sun.enterprise.tools.upgrade.transform.elements.ORB");
		elementMap.put("iiop-listener", "com.sun.enterprise.tools.upgrade.transform.elements.IIOPListener");
		elementMap.put("ssl-client-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("custom-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("external-jndi-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("jdbc-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("jdbc-connection-pool", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("mail-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("persistence-manager-factory-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("jms-resource", "com.sun.enterprise.tools.upgrade.transform.elements.JMSResource");
		elementMap.put("auth-realm", "com.sun.enterprise.tools.upgrade.transform.elements.AuthRealm");
		elementMap.put("profiler", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("jvm-options", "com.sun.enterprise.tools.upgrade.transform.elements.JVMOptions");
		elementMap.put("server-instance", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		
		// added for 8.0 PE
		elementMap.put("domain", "com.sun.enterprise.tools.upgrade.transform.elements.Domain");
		elementMap.put("configs", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("admin-object-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("connector-resource", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("resource-adapter-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("connector-connection-pool", "com.sun.enterprise.tools.upgrade.transform.elements.GenericResource");
		elementMap.put("jms-host", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("session-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("session-manager", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("manager-properties", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("session-properties", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("store-properties", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("jmx-connector", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("das-config", "com.sun.enterprise.tools.upgrade.transform.elements.DasConfig");
		elementMap.put("monitoring-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("module-monitoring-levels", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("availability-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("web-container-availability", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("ejb-container-availability", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("ejb-timer-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("quorum-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("thread-pools", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("thread-pool", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("servers", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("module-log-levels", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("jacc-provider", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("audit-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		
		elementMap.put("message-security-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("provider-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("request-policy", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("response-policy", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		
		// application-ref is updated by application deploy module.  resources-ref updated by generic resources
		elementMap.put("resource-ref", "com.sun.enterprise.tools.upgrade.transform.elements.ResourceRef");
		elementMap.put("application-ref", "com.sun.enterprise.tools.upgrade.transform.elements.ApplicationRef");
		
		elementMap.put("clusters", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("cluster", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("server-ref", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		
		elementMap.put("system-property", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		
		// For the time being will set the following elements to unsuported
		elementMap.put("iiop-cluster", "com.sun.enterprise.tools.upgrade.transform.elements.BaseElement");
		elementMap.put("persistence-store", "com.sun.enterprise.tools.upgrade.transform.elements.PersistenceStore");
		elementMap.put("iiop-server-instance", "com.sun.enterprise.tools.upgrade.transform.elements.IIOPServerInstance");
		// iiop-endpoint is handled in iiop-server-instance itself.  One less element class to worry about.
		elementMap.put("iiop-endpoint", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		
		// Additions for AS9 upgrade
		elementMap.put("node-agents", "com.sun.enterprise.tools.upgrade.transform.elements.NodeAgentsElement");
		elementMap.put("node-agent", "com.sun.enterprise.tools.upgrade.transform.elements.UnSupportedElement");
		elementMap.put("lb-configs", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("lb-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("cluster-ref", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("health-checker", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("appclient-module", "com.sun.enterprise.tools.upgrade.transform.elements.GenericComponent");
		elementMap.put("alert-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("connector-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("alert-subscription", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("listener-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("filter-config", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("access-log", "com.sun.enterprise.tools.upgrade.transform.elements.AccessLog");
		// Added for AS8.2 EE
		elementMap.put("access-log", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("http-access-log", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("request-processing", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("keep-alive", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("http-protocol", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("http-file-cache", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("security-map", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("user-group", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("principal", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("backend-principal", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("diagnostic-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("group-management-service", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("management-rules", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("management-rule", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("event", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("load-balancers", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("load-balancer", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		//Will be supported when domain is created afresh. CR - 6480041
		elementMap.put("jms-availability", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
		elementMap.put("connection-pool", "com.sun.enterprise.tools.upgrade.transform.elements.GenericElement");
	}
	/*
	 * This method builds key map.  The keyMap holds key if any for an element for comparison puprose
	 * If no key is mapped then that element has no key to comapre
	 * Usually if there are multiple elements (*) then key is needed for comparison.
	 * If there are ? (1 or zero) or mandatory element then key is not needed.
	 */
	private void buildKeyMapping(){
		if(keyMap == null){
			keyMap = new HashMap();
		}
		// all resources
		keyMap.put("custom-resource", "jndi-name");
		keyMap.put("jdbc-resource","jndi-name");
		keyMap.put("external-jndi-resource", "jndi-name");
		keyMap.put("jdbc-connection-pool", "name");
		keyMap.put("mail-resource", "jndi-name");
		keyMap.put("persistence-manager-factory-resource", "jndi-name");
		keyMap.put("jms-resource", "jndi-name");
		keyMap.put("admin-object-resource", "jndi-name");
		keyMap.put("connector-resource", "jndi-name");
		keyMap.put("resource-adapter-config", "name");
		keyMap.put("connector-connection-pool", "name");
		
		keyMap.put("jms-host", "name");
		keyMap.put("server-instance", "name");
		keyMap.put("jmx-connector", "name");
		keyMap.put("iiop-listener", "id");
		keyMap.put("config", "name");
		keyMap.put("thread-pool","thread-pool-id");
		keyMap.put("cluster","name");
		keyMap.put("server-ref","ref");
		keyMap.put("resource-ref","ref");
		keyMap.put("application-ref","ref");
		keyMap.put("jacc-provider","name");
		keyMap.put("audit-module","name");
		
		keyMap.put("message-security-config","auth-layer");
		keyMap.put("provider-config","provider-id");
		
		// Added for AS9 upgrade.
		keyMap.put("node-agent","name");
		keyMap.put("lb-config","name");
		keyMap.put("cluster-ref","ref");
		keyMap.put("alert-subscription","name");
		keyMap.put("listener-config","listener-class-name");
		keyMap.put("filter-config","filter-class-name");
		keyMap.put("security-map","name");
		keyMap.put("load-balancer","name");
		keyMap.put("management-rule","name");
		keyMap.put("system-property","name");
	}
	private void buildElementStructureMap(){
		
		if(elementStructureMap == null){
			this.elementStructureMap = new HashMap();
		}
		// List is empty if this element to be inserted as the first element.
		// List contains all element names that appear in the tree after this element appears. (Include names untill the next mandatory element is found)
		// If the element to be inserted at the end do not add any mapping.  if null, then it assumed to be put at the end.
		// If the element is mandatory and need not be added to the tree, then this structure need not be built
		
		elementStructureMap.put("http-service", getListWithElementNames(null));
		elementStructureMap.put("iiop-service", getListWithElementNames(new String[]{"admin-service"}));
		
		elementStructureMap.put("http-listener", getListWithElementNames(new String[]{"virtual-server"}));
		elementStructureMap.put("virtual-server", getListWithElementNames(new String[]{"request-processing","keep-alive","connection-pool","http-protocol","http-file-cache","property"}));
		elementStructureMap.put("request-processing", getListWithElementNames(new String[]{"keep-alive","connection-pool","http-protocol","http-file-cache","property"}));
		elementStructureMap.put("keep-alive", getListWithElementNames(new String[]{"connection-pool","http-protocol","http-file-cache","property"}));
		elementStructureMap.put("connection-pool", getListWithElementNames(new String[]{"http-protocol","http-file-cache","property"}));
		elementStructureMap.put("http-protocol", getListWithElementNames(new String[]{"http-file-cache","property"}));
		elementStructureMap.put("http-file-cache", getListWithElementNames(new String[]{"property"}));
		
		elementStructureMap.put("custom-resource", getListWithElementNames(new String[]{}));
		elementStructureMap.put("external-jndi-resource", getListWithElementNames(new String[]{"jdbc-resource","mail-resource","persistence-manager-factory-resource","admin-object-resource","connector-resource","resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("jdbc-resource", getListWithElementNames(new String[]{"mail-resource","persistence-manager-factory-resource","admin-object-resource","connector-resource","resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("mail-resource", getListWithElementNames(new String[]{"persistence-manager-factory-resource","admin-object-resource","connector-resource","resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("persistence-manager-factory-resource", getListWithElementNames(new String[]{"admin-object-resource","connector-resource","resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("admin-object-resource", getListWithElementNames(new String[]{"connector-resource","resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("connector-resource", getListWithElementNames(new String[]{"resource-adapter-config","jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("resource-adapter-config", getListWithElementNames(new String[]{"jdbc-connection-pool","connector-connection-pool"}));
		elementStructureMap.put("jdbc-connection-pool", getListWithElementNames(new String[]{"connector-connection-pool"}));
		
		// description is always the first element
		elementStructureMap.put("description", getListWithElementNames(new String[]{}));
		elementStructureMap.put("jms-service", getListWithElementNames(new String[]{"log-service"}));
		elementStructureMap.put("availability-service", getListWithElementNames(new String[]{"thread-pools"}));
		
		// ssl element always appear first
		elementStructureMap.put("ssl", getListWithElementNames(new String[]{}));
		elementStructureMap.put("orb", getListWithElementNames(new String[]{}));
		elementStructureMap.put("ssl-client-config", getListWithElementNames(new String[]{"iiop-listener"}));
		
		elementStructureMap.put("jmx-connector", getListWithElementNames(new String[]{}));
		elementStructureMap.put("das-config", getListWithElementNames(new String[]{"property"}));
		
		elementStructureMap.put("session-config", getListWithElementNames(new String[]{}));
		elementStructureMap.put("session-manager", getListWithElementNames(new String[]{}));
		elementStructureMap.put("manager-properties", getListWithElementNames(new String[]{}));
		elementStructureMap.put("ejb-timer-service", getListWithElementNames(new String[]{}));
		elementStructureMap.put("jms-host", getListWithElementNames(new String[]{}));
		elementStructureMap.put("module-log-levels", getListWithElementNames(new String[]{}));
		
		elementStructureMap.put("auth-realm", getListWithElementNames(new String[]{}));
		elementStructureMap.put("jacc-provider", getListWithElementNames(new String[]{"audit-module","message-security-config","property"}));
		elementStructureMap.put("audit-module", getListWithElementNames(new String[]{"message-security-config","property"}));
		elementStructureMap.put("message-security-config",getListWithElementNames(new String[]{"property"}));
		elementStructureMap.put("module-monitoring-levels", getListWithElementNames(new String[]{}));
		
		elementStructureMap.put("profiler", getListWithElementNames(new String[]{}));
		elementStructureMap.put("jvm-options", getListWithElementNames(new String[]{"property"}));
		
		elementStructureMap.put("web-container-availability", getListWithElementNames(new String[]{}));
		elementStructureMap.put("ejb-container-availability", getListWithElementNames(new String[]{"jms-availability","property"}));
		elementStructureMap.put("security-map", getListWithElementNames(new String[]{"property"}));
		elementStructureMap.put("principal", getListWithElementNames(new String[]{}));
		elementStructureMap.put("user-group", getListWithElementNames(new String[]{"backend-principal"}));
		elementStructureMap.put("application-ref", getListWithElementNames(new String[]{"system-property" , "property"}));
		elementStructureMap.put("resource-ref", getListWithElementNames(new String[]{"system-property" , "property"}));
		elementStructureMap.put("system-property" , getListWithElementNames(new String[]{ "property"}));
		elementStructureMap.put("principal", getListWithElementNames(new String[]{}));
		
		// AS90 related
		elementStructureMap.put("alert-service", getListWithElementNames(new String[]{"group-management-service","management-rules","system-property","property"}));
		elementStructureMap.put("group-management-service", getListWithElementNames(new String[]{"management-rules","system-property","property"}));
		elementStructureMap.put("management-rules", getListWithElementNames(new String[]{"system-property","property"}));
		elementStructureMap.put("lb-configs", getListWithElementNames(new String[]{"system-property","property"}));
		elementStructureMap.put("load-balancers", getListWithElementNames(new String[]{"","system-property","property"}));
		elementStructureMap.put("connector-service", getListWithElementNames(new String[]{"web-container"}));
		elementStructureMap.put("alert-subscription", getListWithElementNames(new String[]{}));
		elementStructureMap.put("listener-config", getListWithElementNames(new String[]{}));
		elementStructureMap.put("access-log", getListWithElementNames(new String[]{}));
		elementStructureMap.put("http-access-log", getListWithElementNames(new String[]{}));
		elementStructureMap.put("server-ref", getListWithElementNames(new String[]{}));
		elementStructureMap.put("clusters", getListWithElementNames(new String[]{"system-property","property"}));
	}
	private java.util.List getListWithElementNames(String[] succeedingElements){
		java.util.ArrayList eleList = new java.util.ArrayList();
		if(succeedingElements != null){
			for(int i=0; i<succeedingElements.length; i++){
				eleList.add(succeedingElements[i]);
			}
		}
		return eleList;
	}
}
