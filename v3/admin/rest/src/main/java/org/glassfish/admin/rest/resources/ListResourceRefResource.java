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
import com.sun.enterprise.config.serverbeans.ResourceRef;
public class ListResourceRefResource extends TemplateListOfResource<ResourceRef> {


	@Path("{Ref}/")
	public ResourceRefResource getResourceRefResource(@PathParam("Ref") String id) {
		ResourceRefResource resource = resourceContext.getResource(ResourceRefResource.class);
		for (ResourceRef c: entity){
			if(c.getRef().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-resource-ref")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateResourceRef(
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 ,
	 @QueryParam("reference_name")  @DefaultValue("")  String Reference_name 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("enabled", Enabled);
	p.put("target", Target);
	p.put("reference_name", Reference_name);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-resource-ref", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}

public String getPostCommand() {
	return "create-resource-ref";
}
}
