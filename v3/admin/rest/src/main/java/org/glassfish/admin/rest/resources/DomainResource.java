/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Mon May 04 14:01:01 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import com.sun.enterprise.config.serverbeans.Domain;
@Path("/domain/")
public class DomainResource extends TemplateResource<Domain> {

public Domain getEntity() {
return org.glassfish.admin.rest.RestService.theDomain;
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
