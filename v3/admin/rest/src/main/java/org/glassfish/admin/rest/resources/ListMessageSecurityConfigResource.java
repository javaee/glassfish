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
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
public class ListMessageSecurityConfigResource extends TemplateListOfResource<MessageSecurityConfig> {


	@Path("{AuthLayer}/")
	public MessageSecurityConfigResource getMessageSecurityConfigResource(@PathParam("AuthLayer") String id) {
		MessageSecurityConfigResource resource = resourceContext.getResource(MessageSecurityConfigResource.class);
		for (MessageSecurityConfig c: entity){
//THIS KEY IS THE FIRST Attribute ONE ludo
			if(c.getAuthLayer().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-message-security-provider ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateMessageSecurityProvider(
	 @QueryParam("layer")  @DefaultValue("SOAP")  String Layer 
 ,
	 @QueryParam("providertype")  @DefaultValue("client-server")  String Providertype 
 ,
	 @QueryParam("requestauthsource")  @DefaultValue("")  String Requestauthsource 
 ,
	 @QueryParam("requestauthrecipient")  @DefaultValue("")  String Requestauthrecipient 
 ,
	 @QueryParam("responseauthsource")  @DefaultValue("")  String Responseauthsource 
 ,
	 @QueryParam("responseauthrecipient")  @DefaultValue("")  String Responseauthrecipient 
 ,
	 @QueryParam("isdefaultprovider")  @DefaultValue("false")  String Isdefaultprovider 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("classname")  @DefaultValue("")  String Classname 
 ,
	 @QueryParam("providername")  @DefaultValue("")  String Providername 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("layer", Layer);
	p.put("providertype", Providertype);
	p.put("requestauthsource", Requestauthsource);
	p.put("requestauthrecipient", Requestauthrecipient);
	p.put("responseauthsource", Responseauthsource);
	p.put("responseauthrecipient", Responseauthrecipient);
	p.put("isdefaultprovider", Isdefaultprovider);
	p.put("property", Property);
	p.put("classname", Classname);
	p.put("providername", Providername);
	p.put("target", Target);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-message-security-provider", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
}
