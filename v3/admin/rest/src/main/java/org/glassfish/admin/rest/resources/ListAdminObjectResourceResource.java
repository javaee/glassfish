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
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateListOfResource;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
public class ListAdminObjectResourceResource extends TemplateListOfResource<AdminObjectResource> {


	@Path("{JndiName}/")
	public AdminObjectResourceResource getAdminObjectResourceResource(@PathParam("JndiName") String id) {
		AdminObjectResourceResource resource = resourceContext.getResource(AdminObjectResourceResource.class);
		for (AdminObjectResource c: entity){
			//Using '-' for back-slash in resource names
			//For example, jndi names has back-slash in it.
			if(c.getJndiName().replace('/', '-').equals(id)){
				resource.setEntity(c);
			}
		}
		return resource;
	}


@Override
public String getPostCommand() {
	return "create-admin-object";
}
}
