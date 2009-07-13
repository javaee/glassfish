/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Mon Jul 13 13:06:35 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.Domain;
@Path("/domain/")
public class DomainResource extends TemplateResource<Domain> {

public Domain getEntity() {
return org.glassfish.admin.rest.RestService.theDomain;
}
@Path("commands/stop-domain")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execStopDomain(
	 @QueryParam("force")  @DefaultValue("true")  String Force 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("force", Force);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("stop-domain", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("commands/restart-domain")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execRestartDomain(
 	) {
	java.util.Properties p = new java.util.Properties();
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("restart-domain", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("commands/uptime")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execUptime(
 	) {
	java.util.Properties p = new java.util.Properties();
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("uptime", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("commands/version")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execVersion(
	 @QueryParam("verbose")  @DefaultValue("false")  String Verbose 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("verbose", Verbose);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("version", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("commands/rotate-log")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execRotateLog(
 	) {
	java.util.Properties p = new java.util.Properties();
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("rotate-log", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("commands/get-host-and-port")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execGetHostAndPort(
	 @QueryParam("target")  @DefaultValue("")  String Target 
 ,
	 @QueryParam("virtualServer")  @DefaultValue("")  String VirtualServer 
 ,
	 @QueryParam("securityEnabled")  @DefaultValue("false")  String SecurityEnabled 
 ,
	 @QueryParam("moduleId")  @DefaultValue("")  String ModuleId 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("target", Target);
	p.put("virtualServer", VirtualServer);
	p.put("securityEnabled", SecurityEnabled);
	p.put("moduleId", ModuleId);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("get-host-and-port", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
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

public String[] getCommandResourcesPaths() {
return new String[]{"stop", "restart", "uptime", "version", "rotate-log", "host-port"};
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
