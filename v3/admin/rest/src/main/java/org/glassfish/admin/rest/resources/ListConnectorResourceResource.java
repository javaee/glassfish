/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Mon May 11 13:27:46 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import java.util.List;
import org.glassfish.admin.rest.TemplateListOfResource;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
public class ListConnectorResourceResource extends TemplateListOfResource<ConnectorResource> {


	@Path("{JndiName}/")
	public ConnectorResourceResource getConnectorResourceResource(@PathParam("JndiName") String id) {
		ConnectorResourceResource resource = resourceContext.getResource(ConnectorResourceResource.class);
		for (ConnectorResource c: entity){
			if(c.getJndiName().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-connector-resource ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateConnectorResource(
	 @QueryParam("poolname")  @DefaultValue("")  String Poolname 
 ,
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("description")  @DefaultValue("")  String Description 
 ,
	 @QueryParam("objecttype")  @DefaultValue("user")  String Objecttype 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 ,
	 @QueryParam("jndi_name")  @DefaultValue("")  String Jndi_name 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("poolname", Poolname);
	p.put("enabled", Enabled);
	p.put("description", Description);
	p.put("objecttype", Objecttype);
	p.put("property", Property);
	p.put("target", Target);
	p.put("jndi_name", Jndi_name);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-connector-resource", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
}
