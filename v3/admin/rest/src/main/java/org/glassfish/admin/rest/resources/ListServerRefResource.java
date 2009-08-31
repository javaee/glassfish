/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import java.util.List;
import org.glassfish.admin.rest.TemplateListOfResource;
import org.glassfish.admin.rest.provider.GetResultList;
import com.sun.enterprise.config.serverbeans.ServerRef;
public class ListServerRefResource extends TemplateListOfResource<ServerRef> {


	@Path("{Ref}/")
	public ServerRefResource getServerRefResource(@PathParam("Ref") String id) {
		ServerRefResource resource = resourceContext.getResource(ServerRefResource.class);
		for (ServerRef c: entity){
//THIS KEY IS THE FIRST Attribute ONE ludo
			if(c.getRef().equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}


public String getPostCommand() {
	return null;
}
}
