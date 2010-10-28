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
@Path("/domain/")
public class DomainResource extends org.glassfish.admin.rest.resources.GlassFishDomainResource  {

@Path("create-instance/")
public DomainCreateInstanceResource getDomainCreateInstanceResource() {
DomainCreateInstanceResource resource = resourceContext.getResource(DomainCreateInstanceResource.class);
return resource;
}

@Path("host-port/")
public DomainHostPortResource getDomainHostPortResource() {
DomainHostPortResource resource = resourceContext.getResource(DomainHostPortResource.class);
return resource;
}

@Path("list-logger-levels/")
public DomainListLoggerLevelsResource getDomainListLoggerLevelsResource() {
DomainListLoggerLevelsResource resource = resourceContext.getResource(DomainListLoggerLevelsResource.class);
return resource;
}

@Path("list-instances/")
public DomainListInstancesResource getDomainListInstancesResource() {
DomainListInstancesResource resource = resourceContext.getResource(DomainListInstancesResource.class);
return resource;
}

@Path("restart/")
public DomainRestartResource getDomainRestartResource() {
DomainRestartResource resource = resourceContext.getResource(DomainRestartResource.class);
return resource;
}

@Path("rotate-log/")
public DomainRotateLogResource getDomainRotateLogResource() {
DomainRotateLogResource resource = resourceContext.getResource(DomainRotateLogResource.class);
return resource;
}

@Path("set-log-level/")
public DomainSetLogLevelResource getDomainSetLogLevelResource() {
DomainSetLogLevelResource resource = resourceContext.getResource(DomainSetLogLevelResource.class);
return resource;
}

@Path("stop/")
public DomainStopResource getDomainStopResource() {
DomainStopResource resource = resourceContext.getResource(DomainStopResource.class);
return resource;
}

@Path("uptime/")
public DomainUptimeResource getDomainUptimeResource() {
DomainUptimeResource resource = resourceContext.getResource(DomainUptimeResource.class);
return resource;
}

@Path("version/")
public DomainVersionResource getDomainVersionResource() {
DomainVersionResource resource = resourceContext.getResource(DomainVersionResource.class);
return resource;
}

@Path("get-runtime-info/")
public DomainGetRuntimeInfoResource getDomainGetRuntimeInfoResource() {
DomainGetRuntimeInfoResource resource = resourceContext.getResource(DomainGetRuntimeInfoResource.class);
return resource;
}

@Path("location/")
public DomainLocationResource getDomainLocationResource() {
DomainLocationResource resource = resourceContext.getResource(DomainLocationResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][] {{"create-instance", "POST", "create-instance"} , {"host-port", "GET", "_get-host-and-port"} , {"list-logger-levels", "GET", "list-logger-levels"} , {"list-instances", "GET", "list-instances"} , {"restart", "POST", "restart-domain"} , {"rotate-log", "POST", "rotate-log"} , {"set-log-level", "POST", "set-log-level"} , {"stop", "POST", "stop-domain"} , {"uptime", "GET", "uptime"} , {"version", "GET", "version"} , {"get-runtime-info", "GET", "_get-runtime-info"} , {"location", "GET", "__locations"} };
}

	@Path("resources/")
	public ResourcesResource getResourcesResource() {
		ResourcesResource resource = resourceContext.getResource(ResourcesResource.class);
		resource.setParentAndTagName(getEntity() , "resources");
		return resource;
	}
	@Path("load-balancers/")
	public LoadBalancersResource getLoadBalancersResource() {
		LoadBalancersResource resource = resourceContext.getResource(LoadBalancersResource.class);
		resource.setParentAndTagName(getEntity() , "load-balancers");
		return resource;
	}
	@Path("lb-configs/")
	public LbConfigsResource getLbConfigsResource() {
		LbConfigsResource resource = resourceContext.getResource(LbConfigsResource.class);
		resource.setParentAndTagName(getEntity() , "lb-configs");
		return resource;
	}
	@Path("property/")
	public PropertiesBagResource getPropertiesBagResource() {
		PropertiesBagResource resource = resourceContext.getResource(PropertiesBagResource.class);
		resource.setParentAndTagName(getEntity() , "property");
		return resource;
	}
	@Path("system-applications/")
	public SystemApplicationsResource getSystemApplicationsResource() {
		SystemApplicationsResource resource = resourceContext.getResource(SystemApplicationsResource.class);
		resource.setParentAndTagName(getEntity() , "system-applications");
		return resource;
	}
	@Path("configs/")
	public ConfigsResource getConfigsResource() {
		ConfigsResource resource = resourceContext.getResource(ConfigsResource.class);
		resource.setParentAndTagName(getEntity() , "configs");
		return resource;
	}
	@Path("clusters/")
	public ClustersResource getClustersResource() {
		ClustersResource resource = resourceContext.getResource(ClustersResource.class);
		resource.setParentAndTagName(getEntity() , "clusters");
		return resource;
	}
	@Path("servers/")
	public ServersResource getServersResource() {
		ServersResource resource = resourceContext.getResource(ServersResource.class);
		resource.setParentAndTagName(getEntity() , "servers");
		return resource;
	}
	@Path("nodes/")
	public NodesResource getNodesResource() {
		NodesResource resource = resourceContext.getResource(NodesResource.class);
		resource.setParentAndTagName(getEntity() , "nodes");
		return resource;
	}
	@Path("node-agents/")
	public NodeAgentsResource getNodeAgentsResource() {
		NodeAgentsResource resource = resourceContext.getResource(NodeAgentsResource.class);
		resource.setParentAndTagName(getEntity() , "node-agents");
		return resource;
	}
	@Path("amx-pref/")
	public AmxPrefResource getAmxPrefResource() {
		AmxPrefResource resource = resourceContext.getResource(AmxPrefResource.class);
		resource.setParentAndTagName(getEntity() , "amx-pref");
		return resource;
	}
	@Path("system-property/")
	public ListSystemPropertyResource getListSystemPropertyResource() {
		ListSystemPropertyResource resource = resourceContext.getResource(ListSystemPropertyResource.class);
		resource.setParentAndTagName(getEntity() , "system-property");
		return resource;
	}
	@Path("applications/")
	public ApplicationsResource getApplicationsResource() {
		ApplicationsResource resource = resourceContext.getResource(ApplicationsResource.class);
		resource.setParentAndTagName(getEntity() , "applications");
		return resource;
	}
}
