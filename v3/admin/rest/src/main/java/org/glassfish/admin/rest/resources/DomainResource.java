/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.resources;
import javax.ws.rs.Path;
import org.glassfish.admin.rest.TemplateResource;
@Path("/domain/")
public class DomainResource extends org.glassfish.admin.rest.staticresources.GlassFishDomainResource {

@Path("create-instance/")
public DomainCreateInstanceResource getDomainCreateInstanceResource() {
DomainCreateInstanceResource resource = resourceContext.getResource(DomainCreateInstanceResource.class);
return resource;
}

@Path("delete-instance/")
public DomainDeleteInstanceResource getDomainDeleteInstanceResource() {
DomainDeleteInstanceResource resource = resourceContext.getResource(DomainDeleteInstanceResource.class);
return resource;
}

@Path("start-instance/")
public DomainStartInstanceResource getDomainStartInstanceResource() {
DomainStartInstanceResource resource = resourceContext.getResource(DomainStartInstanceResource.class);
return resource;
}

@Path("stop-instance/")
public DomainStopInstanceResource getDomainStopInstanceResource() {
DomainStopInstanceResource resource = resourceContext.getResource(DomainStopInstanceResource.class);
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

@Override
public String[][] getCommandResourcesPaths() {
return new String[][]{{"create-instance", "POST"}, {"delete-instance", "DELETE"}, {"start-instance", "POST"}, {"stop-instance", "POST"}, {"host-port", "GET"}, {"list-logger-levels", "GET"}, {"list-instances", "GET"}, {"restart", "POST"}, {"rotate-log", "POST"}, {"set-log-level", "POST"}, {"stop", "POST"}, {"uptime", "GET"}, {"version", "GET"}};
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
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
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
	public ListSystemPropertyResource getSystemPropertyResource() {
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
