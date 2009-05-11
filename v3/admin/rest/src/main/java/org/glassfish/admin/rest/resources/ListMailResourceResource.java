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
import com.sun.enterprise.config.serverbeans.MailResource;
public class ListMailResourceResource extends TemplateListOfResource<MailResource> {


	@Path("{JndiName}/")
	public MailResourceResource getMailResourceResource(@PathParam("JndiName") String id) {
		MailResourceResource resource = resourceContext.getResource(MailResourceResource.class);
		for (MailResource c: entity){
			if(c.getJndiName().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-javamail-resource ")
@GET
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateJavamailResource(
	 @QueryParam("mailhost")  @DefaultValue("")  String Mailhost 
 ,
	 @QueryParam("mailuser")  @DefaultValue("")  String Mailuser 
 ,
	 @QueryParam("fromaddress")  @DefaultValue("")  String Fromaddress 
 ,
	 @QueryParam("jndi_name")  @DefaultValue("")  String Jndi_name 
 ,
	 @QueryParam("storeprotocol")  @DefaultValue("imap")  String Storeprotocol 
 ,
	 @QueryParam("storeprotocolclass")  @DefaultValue("com.sun.mail.imap.IMAPStore")  String Storeprotocolclass 
 ,
	 @QueryParam("transprotocol")  @DefaultValue("smtp")  String Transprotocol 
 ,
	 @QueryParam("transprotocolclass")  @DefaultValue("com.sun.mail.smtp.SMTPTransport")  String Transprotocolclass 
 ,
	 @QueryParam("enabled")  @DefaultValue("true")  String Enabled 
 ,
	 @QueryParam("debug")  @DefaultValue("false")  String Debug 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("target")  @DefaultValue("server")  String Target 
 ,
	 @QueryParam("description")  @DefaultValue("")  String Description 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("mailhost", Mailhost);
	p.put("mailuser", Mailuser);
	p.put("fromaddress", Fromaddress);
	p.put("jndi_name", Jndi_name);
	p.put("storeprotocol", Storeprotocol);
	p.put("storeprotocolclass", Storeprotocolclass);
	p.put("transprotocol", Transprotocol);
	p.put("transprotocolclass", Transprotocolclass);
	p.put("enabled", Enabled);
	p.put("debug", Debug);
	p.put("property", Property);
	p.put("target", Target);
	p.put("description", Description);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-javamail-resource", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
}
