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
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
public class ListConnectorConnectionPoolResource extends TemplateListOfResource<ConnectorConnectionPool> {


	@Path("{Name}/")
	public ConnectorConnectionPoolResource getConnectorConnectionPoolResource(@PathParam("Name") String id) {
		ConnectorConnectionPoolResource resource = resourceContext.getResource(ConnectorConnectionPoolResource.class);
		for (ConnectorConnectionPool c: entity){
			if(c.getName().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}

@Path("commands/create-connector-connection-pool")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public List<org.jvnet.hk2.config.Dom> execCreateConnectorConnectionPool(
	 @QueryParam("raname")  @DefaultValue("")  String Raname 
 ,
	 @QueryParam("connectiondefinition")  @DefaultValue("")  String Connectiondefinition 
 ,
	 @QueryParam("steadypoolsize")  @DefaultValue("")  String Steadypoolsize 
 ,
	 @QueryParam("maxpoolsize")  @DefaultValue("")  String Maxpoolsize 
 ,
	 @QueryParam("maxwait")  @DefaultValue("")  String Maxwait 
 ,
	 @QueryParam("poolresize")  @DefaultValue("")  String Poolresize 
 ,
	 @QueryParam("idletimeout")  @DefaultValue("")  String Idletimeout 
 ,
	 @QueryParam("isconnectvalidatereq")  @DefaultValue("false")  String Isconnectvalidatereq 
 ,
	 @QueryParam("failconnection")  @DefaultValue("false")  String Failconnection 
 ,
	 @QueryParam("leaktimeout")  @DefaultValue("")  String Leaktimeout 
 ,
	 @QueryParam("leakreclaim")  @DefaultValue("false")  String Leakreclaim 
 ,
	 @QueryParam("creationretryattempts")  @DefaultValue("")  String Creationretryattempts 
 ,
	 @QueryParam("creationretryinterval")  @DefaultValue("")  String Creationretryinterval 
 ,
	 @QueryParam("lazyconnectionenlistment")  @DefaultValue("false")  String Lazyconnectionenlistment 
 ,
	 @QueryParam("lazyconnectionassociation")  @DefaultValue("false")  String Lazyconnectionassociation 
 ,
	 @QueryParam("associatewiththread")  @DefaultValue("false")  String Associatewiththread 
 ,
	 @QueryParam("matchconnections")  @DefaultValue("false")  String Matchconnections 
 ,
	 @QueryParam("maxconnectionusagecount")  @DefaultValue("")  String Maxconnectionusagecount 
 ,
	 @QueryParam("validateatmostonceperiod")  @DefaultValue("")  String Validateatmostonceperiod 
 ,
	 @QueryParam("transactionsupport")  @DefaultValue("")  String Transactionsupport 
 ,
	 @QueryParam("description")  @DefaultValue("")  String Description 
 ,
	 @QueryParam("property")  @DefaultValue("")  String Property 
 ,
	 @QueryParam("target")  @DefaultValue("")  String Target 
 ,
	 @QueryParam("poolname")  @DefaultValue("")  String Poolname 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("raname", Raname);
	p.put("connectiondefinition", Connectiondefinition);
	p.put("steadypoolsize", Steadypoolsize);
	p.put("maxpoolsize", Maxpoolsize);
	p.put("maxwait", Maxwait);
	p.put("poolresize", Poolresize);
	p.put("idletimeout", Idletimeout);
	p.put("isconnectvalidatereq", Isconnectvalidatereq);
	p.put("failconnection", Failconnection);
	p.put("leaktimeout", Leaktimeout);
	p.put("leakreclaim", Leakreclaim);
	p.put("creationretryattempts", Creationretryattempts);
	p.put("creationretryinterval", Creationretryinterval);
	p.put("lazyconnectionenlistment", Lazyconnectionenlistment);
	p.put("lazyconnectionassociation", Lazyconnectionassociation);
	p.put("associatewiththread", Associatewiththread);
	p.put("matchconnections", Matchconnections);
	p.put("maxconnectionusagecount", Maxconnectionusagecount);
	p.put("validateatmostonceperiod", Validateatmostonceperiod);
	p.put("transactionsupport", Transactionsupport);
	p.put("description", Description);
	p.put("property", Property);
	p.put("target", Target);
	p.put("poolname", Poolname);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("create-connector-connection-pool", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}

public String getPostCommand() {
	return "create-connector-connection-pool";
}
}
