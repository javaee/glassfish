/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.resources.generated;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.glassfish.admin.rest.resources.*;
import org.glassfish.admin.rest.resources.custom.*;
public class ConfigResource extends TemplateResource  {

@Path("delete-config/")
public ConfigDeleteConfigResource getConfigDeleteConfigResource() {
ConfigDeleteConfigResource resource = resourceContext.getResource(ConfigDeleteConfigResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][] {{"delete-config", "POST", "delete-config"} };
}

	@Path("admin-service/")
	public AdminServiceResource getAdminServiceResource() {
		AdminServiceResource resource = resourceContext.getResource(AdminServiceResource.class);
		resource.setParentAndTagName(getEntity() , "admin-service");
		return resource;
	}
	@Path("ejb-container/")
	public EjbContainerResource getEjbContainerResource() {
		EjbContainerResource resource = resourceContext.getResource(EjbContainerResource.class);
		resource.setParentAndTagName(getEntity() , "ejb-container");
		return resource;
	}
	@Path("transaction-service/")
	public TransactionServiceResource getTransactionServiceResource() {
		TransactionServiceResource resource = resourceContext.getResource(TransactionServiceResource.class);
		resource.setParentAndTagName(getEntity() , "transaction-service");
		return resource;
	}
	@Path("management-rules/")
	public ManagementRulesResource getManagementRulesResource() {
		ManagementRulesResource resource = resourceContext.getResource(ManagementRulesResource.class);
		resource.setParentAndTagName(getEntity() , "management-rules");
		return resource;
	}
	@Path("java-config/")
	public JavaConfigResource getJavaConfigResource() {
		JavaConfigResource resource = resourceContext.getResource(JavaConfigResource.class);
		resource.setParentAndTagName(getEntity() , "java-config");
		return resource;
	}
	@Path("config-extension/")
	public ListConfigExtensionResource getListConfigExtensionResource() {
		ListConfigExtensionResource resource = resourceContext.getResource(ListConfigExtensionResource.class);
		resource.setParentAndTagName(getEntity() , "config-extension");
		return resource;
	}
	@Path("jruby-container/")
	public ListJrubyContainerResource getListJrubyContainerResource() {
		ListJrubyContainerResource resource = resourceContext.getResource(ListJrubyContainerResource.class);
		resource.setParentAndTagName(getEntity() , "jruby-container");
		return resource;
	}
	@Path("monitoring-service/")
	public MonitoringServiceResource getMonitoringServiceResource() {
		MonitoringServiceResource resource = resourceContext.getResource(MonitoringServiceResource.class);
		resource.setParentAndTagName(getEntity() , "monitoring-service");
		return resource;
	}
	@Path("group-management-service/")
	public GroupManagementServiceResource getGroupManagementServiceResource() {
		GroupManagementServiceResource resource = resourceContext.getResource(GroupManagementServiceResource.class);
		resource.setParentAndTagName(getEntity() , "group-management-service");
		return resource;
	}
	@Path("property/")
	public PropertiesBagResource getPropertiesBagResource() {
		PropertiesBagResource resource = resourceContext.getResource(PropertiesBagResource.class);
		resource.setParentAndTagName(getEntity() , "property");
		return resource;
	}
	@Path("availability-service/")
	public AvailabilityServiceResource getAvailabilityServiceResource() {
		AvailabilityServiceResource resource = resourceContext.getResource(AvailabilityServiceResource.class);
		resource.setParentAndTagName(getEntity() , "availability-service");
		return resource;
	}
	@Path("thread-pools/")
	public ThreadPoolsResource getThreadPoolsResource() {
		ThreadPoolsResource resource = resourceContext.getResource(ThreadPoolsResource.class);
		resource.setParentAndTagName(getEntity() , "thread-pools");
		return resource;
	}
	@Path("web-container/")
	public WebContainerResource getWebContainerResource() {
		WebContainerResource resource = resourceContext.getResource(WebContainerResource.class);
		resource.setParentAndTagName(getEntity() , "web-container");
		return resource;
	}
	@Path("mdb-container/")
	public MdbContainerResource getMdbContainerResource() {
		MdbContainerResource resource = resourceContext.getResource(MdbContainerResource.class);
		resource.setParentAndTagName(getEntity() , "mdb-container");
		return resource;
	}
	@Path("log-service/")
	public LogServiceResource getLogServiceResource() {
		LogServiceResource resource = resourceContext.getResource(LogServiceResource.class);
		resource.setParentAndTagName(getEntity() , "log-service");
		return resource;
	}
	@Path("http-service/")
	public HttpServiceResource getHttpServiceResource() {
		HttpServiceResource resource = resourceContext.getResource(HttpServiceResource.class);
		resource.setParentAndTagName(getEntity() , "http-service");
		return resource;
	}
	@Path("jms-service/")
	public JmsServiceResource getJmsServiceResource() {
		JmsServiceResource resource = resourceContext.getResource(JmsServiceResource.class);
		resource.setParentAndTagName(getEntity() , "jms-service");
		return resource;
	}
	@Path("alert-service/")
	public AlertServiceResource getAlertServiceResource() {
		AlertServiceResource resource = resourceContext.getResource(AlertServiceResource.class);
		resource.setParentAndTagName(getEntity() , "alert-service");
		return resource;
	}
	@Path("connector-service/")
	public ConnectorServiceResource getConnectorServiceResource() {
		ConnectorServiceResource resource = resourceContext.getResource(ConnectorServiceResource.class);
		resource.setParentAndTagName(getEntity() , "connector-service");
		return resource;
	}
	@Path("network-config/")
	public NetworkConfigResource getNetworkConfigResource() {
		NetworkConfigResource resource = resourceContext.getResource(NetworkConfigResource.class);
		resource.setParentAndTagName(getEntity() , "network-config");
		return resource;
	}
	@Path("system-property/")
	public ListSystemPropertyResource getListSystemPropertyResource() {
		ListSystemPropertyResource resource = resourceContext.getResource(ListSystemPropertyResource.class);
		resource.setParentAndTagName(getEntity() , "system-property");
		return resource;
	}
	@Path("iiop-service/")
	public IiopServiceResource getIiopServiceResource() {
		IiopServiceResource resource = resourceContext.getResource(IiopServiceResource.class);
		resource.setParentAndTagName(getEntity() , "iiop-service");
		return resource;
	}
	@Path("diagnostic-service/")
	public DiagnosticServiceResource getDiagnosticServiceResource() {
		DiagnosticServiceResource resource = resourceContext.getResource(DiagnosticServiceResource.class);
		resource.setParentAndTagName(getEntity() , "diagnostic-service");
		return resource;
	}
	@Path("security-service/")
	public SecurityServiceResource getSecurityServiceResource() {
		SecurityServiceResource resource = resourceContext.getResource(SecurityServiceResource.class);
		resource.setParentAndTagName(getEntity() , "security-service");
		return resource;
	}
}
