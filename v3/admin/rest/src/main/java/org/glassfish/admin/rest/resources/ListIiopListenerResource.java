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
import com.sun.enterprise.config.serverbeans.IiopListener;
public class ListIiopListenerResource extends TemplateListOfResource<IiopListener> {


	@Path("{Id}/")
	public IiopListenerResource getIiopListenerResource(@PathParam("Id") String id) {
		IiopListenerResource resource = resourceContext.getResource(IiopListenerResource.class);
		for (IiopListener c: entity){
			if(c.getId().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-iiop-listener ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateIiopListener(
	 @QueryParam("listeneraddress")  @DefaultValue("")  String Listeneraddress 
 ,
	 @QueryParam("iiopport")  @DefaultValue("1072")  String Iiopport 
 ,
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("securityenabled")  @DefaultValue("false")  String Securityenabled 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 ,
	 @QueryParam("listener_id")  @DefaultValue("")  String Listener_id 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("listeneraddress", Listeneraddress);
	p.put("iiopport", Iiopport);
	p.put("enabled", Enabled);
	p.put("securityenabled", Securityenabled);
	p.put("property", Property);
	p.put("target", Target);
	p.put("listener_id", Listener_id);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-iiop-listener", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
}
