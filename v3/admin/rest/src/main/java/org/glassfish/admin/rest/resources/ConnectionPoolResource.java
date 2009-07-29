/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Tue Jul 28 17:11:42 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.ConnectionPool;
public class ConnectionPoolResource extends TemplateResource<ConnectionPool> {

@Path("commands/ping-connection-pool")
@POST
@Produces({javax.ws.rs.core.MediaType.TEXT_HTML, javax.ws.rs.core.MediaType.APPLICATION_JSON, javax.ws.rs.core.MediaType.APPLICATION_XML})
public org.glassfish.admin.rest.provider.GetResult execPingConnectionPool(
	 @QueryParam("pool_name")  @DefaultValue("")  String Pool_name 
 	) {
	java.util.Properties p = new java.util.Properties();
	p.put("pool_name", Pool_name);
	org.glassfish.api.ActionReport ar = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.ActionReport.class);
	org.glassfish.api.admin.CommandRunner cr = org.glassfish.admin.rest.RestService.habitat.getComponent(org.glassfish.api.admin.CommandRunner.class);
	cr.doCommand("ping-connection-pool", p, ar);
	System.out.println("exec command =" + ar.getActionExitCode());
	return get(1);
}
@Path("ping/")
public ConnectionPoolPingResource getConnectionPoolPingResource() {
ConnectionPoolPingResource resource = resourceContext.getResource(ConnectionPoolPingResource.class);
return resource;
}

public String[][] getCommandResourcesPaths() {
return new String[][]{{"ping", "GET"}};
}

}
