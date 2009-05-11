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
import com.sun.enterprise.config.serverbeans.HttpListener;
public class ListHttpListenerResource extends TemplateListOfResource<HttpListener> {


	@Path("{Id}/")
	public HttpListenerResource getHttpListenerResource(@PathParam("Id") String id) {
		HttpListenerResource resource = resourceContext.getResource(HttpListenerResource.class);
		for (HttpListener c: entity){
			if(c.getId().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-http-listener ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateHttpListener(
	 @QueryParam("listeneraddress")  @DefaultValue("")  String Listeneraddress 
 ,
	 @QueryParam("listenerport")  @DefaultValue("")  String Listenerport 
 ,
	 @QueryParam("defaultvs")  @DefaultValue("")  String Defaultvs 
 ,
	 @QueryParam("servername")  @DefaultValue("")  String Servername 
 ,
	 @QueryParam("acceptorthreads")  @DefaultValue("")  String Acceptorthreads 
 ,
	 @QueryParam("xpowered")  @DefaultValue("true")  String Xpowered 
 ,
	 @QueryParam("redirectport")  @DefaultValue("")  String Redirectport 
 ,
	 @QueryParam("externalport")  @DefaultValue("")  String Externalport 
 ,
	 @QueryParam("securityenabled")  @DefaultValue("false")  String Securityenabled 
 ,
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("secure")  @DefaultValue("false")  String Secure 
 ,
	 @QueryParam("family")  @DefaultValue("")  String Family 
 ,
	 @QueryParam("blockingenabled")  @DefaultValue("false")  String Blockingenabled 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("listener_id")  @DefaultValue("")  String Listener_id 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("listeneraddress", Listeneraddress);
	p.put("listenerport", Listenerport);
	p.put("defaultvs", Defaultvs);
	p.put("servername", Servername);
	p.put("acceptorthreads", Acceptorthreads);
	p.put("xpowered", Xpowered);
	p.put("redirectport", Redirectport);
	p.put("externalport", Externalport);
	p.put("securityenabled", Securityenabled);
	p.put("enabled", Enabled);
	p.put("secure", Secure);
	p.put("family", Family);
	p.put("blockingenabled", Blockingenabled);
	p.put("property", Property);
	p.put("listener_id", Listener_id);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-http-listener", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
}
