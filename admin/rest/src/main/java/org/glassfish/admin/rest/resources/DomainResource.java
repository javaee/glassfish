/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import com.sun.enterprise.config.serverbeans.Domain;
@Path("/domain/")
public class DomainResource extends TemplateResource<Domain> {

@Override public Domain getEntity() {
return org.glassfish.admin.rest.RestService.getDomain();
}
@Path("stop/")
public DomainStopResource getDomainStopResource() {
DomainStopResource resource = resourceContext.getResource(DomainStopResource.class);
return resource;
}

@Path("restart/")
public DomainRestartResource getDomainRestartResource() {
DomainRestartResource resource = resourceContext.getResource(DomainRestartResource.class);
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

@Path("rotate-log/")
public DomainRotateLogResource getDomainRotateLogResource() {
DomainRotateLogResource resource = resourceContext.getResource(DomainRotateLogResource.class);
return resource;
}

@Path("host-port/")
public DomainHostPortResource getDomainHostPortResource() {
DomainHostPortResource resource = resourceContext.getResource(DomainHostPortResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][]{{"stop", "POST"}, {"restart", "POST"}, {"uptime", "GET"}, {"version", "GET"}, {"rotate-log", "POST"}, {"host-port", "GET"}};
}

	@Path("configs/")
	public ConfigsResource getConfigsResource() {
		ConfigsResource resource = resourceContext.getResource(ConfigsResource.class);
		resource.setEntity(getEntity().getConfigs() );
		return resource;
	}
	@Path("resources/")
	public ResourcesResource getResourcesResource() {
		ResourcesResource resource = resourceContext.getResource(ResourcesResource.class);
		resource.setEntity(getEntity().getResources() );
		return resource;
	}
	@Path("lb-configs/")
	public LbConfigsResource getLbConfigsResource() {
		LbConfigsResource resource = resourceContext.getResource(LbConfigsResource.class);
		resource.setEntity(getEntity().getLbConfigs() );
		return resource;
	}
	@Path("load-balancers/")
	public LoadBalancersResource getLoadBalancersResource() {
		LoadBalancersResource resource = resourceContext.getResource(LoadBalancersResource.class);
		resource.setEntity(getEntity().getLoadBalancers() );
		return resource;
	}
	@Path("clusters/")
	public ClustersResource getClustersResource() {
		ClustersResource resource = resourceContext.getResource(ClustersResource.class);
		resource.setEntity(getEntity().getClusters() );
		return resource;
	}
	@Path("servers/")
	public ServersResource getServersResource() {
		ServersResource resource = resourceContext.getResource(ServersResource.class);
		resource.setEntity(getEntity().getServers() );
		return resource;
	}
	@Path("amx-pref/")
	public AmxPrefResource getAmxPrefResource() {
		AmxPrefResource resource = resourceContext.getResource(AmxPrefResource.class);
		resource.setEntity(getEntity().getAmxPref() );
		return resource;
	}
	@Path("node-agents/")
	public NodeAgentsResource getNodeAgentsResource() {
		NodeAgentsResource resource = resourceContext.getResource(NodeAgentsResource.class);
		resource.setEntity(getEntity().getNodeAgents() );
		return resource;
	}
	@Path("applications/")
	public ApplicationsResource getApplicationsResource() {
		ApplicationsResource resource = resourceContext.getResource(ApplicationsResource.class);
		resource.setEntity(getEntity().getApplications() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("system-property/")
	public ListSystemPropertyResource getSystemPropertyResource() {
		ListSystemPropertyResource resource = resourceContext.getResource(ListSystemPropertyResource.class);
		resource.setEntity(getEntity().getSystemProperty() );
		return resource;
	}
	@Path("system-applications/")
	public SystemApplicationsResource getSystemApplicationsResource() {
		SystemApplicationsResource resource = resourceContext.getResource(SystemApplicationsResource.class);
		resource.setEntity(getEntity().getSystemApplications() );
		return resource;
	}
}
