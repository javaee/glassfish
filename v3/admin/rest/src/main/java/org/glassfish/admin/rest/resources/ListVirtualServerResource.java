/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Sat Jun 20 16:10:03 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import java.util.List;
import org.glassfish.admin.rest.TemplateListOfResource;
import com.sun.enterprise.config.serverbeans.VirtualServer;
public class ListVirtualServerResource extends TemplateListOfResource<VirtualServer> {


	@Path("{Id}/")
	public VirtualServerResource getVirtualServerResource(@PathParam("Id") String id) {
		VirtualServerResource resource = resourceContext.getResource(VirtualServerResource.class);
		for (VirtualServer c: entity){
			if(c.getId().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-virtual-server")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateVirtualServer(
	 @QueryParam("hosts")  @DefaultValue("")  String Hosts 
 ,
	 @QueryParam("httplisteners")  @DefaultValue("")  String Httplisteners 
 ,
	 @QueryParam("networklisteners")  @DefaultValue("")  String Networklisteners 
 ,
	 @QueryParam("defaultwebmodule")  @DefaultValue("")  String Defaultwebmodule 
 ,
	 @QueryParam("state")  @DefaultValue("")  String State 
 ,
	 @QueryParam("logfile")  @DefaultValue("")  String Logfile 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("virtual_server_id")  @DefaultValue("")  String Virtual_server_id 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("hosts", Hosts);
	p.put("httplisteners", Httplisteners);
	p.put("networklisteners", Networklisteners);
	p.put("defaultwebmodule", Defaultwebmodule);
	p.put("state", State);
	p.put("logfile", Logfile);
	p.put("property", Property);
	p.put("virtual_server_id", Virtual_server_id);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-virtual-server", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}

public String getPostCommand() {
	return "create-virtual-server";
}
}
