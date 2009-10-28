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
import org.glassfish.admin.rest.TemplateResource;
import com.sun.enterprise.config.serverbeans.AuthRealm;
public class AuthRealmResource extends TemplateResource<AuthRealm> {

@Path("create-user/")
public AuthRealmCreateUserResource getAuthRealmCreateUserResource() {
AuthRealmCreateUserResource resource = resourceContext.getResource(AuthRealmCreateUserResource.class);
return resource;
}

@Path("delete-user/")
public AuthRealmDeleteUserResource getAuthRealmDeleteUserResource() {
AuthRealmDeleteUserResource resource = resourceContext.getResource(AuthRealmDeleteUserResource.class);
return resource;
}

@Path("list-users/")
public AuthRealmListUsersResource getAuthRealmListUsersResource() {
AuthRealmListUsersResource resource = resourceContext.getResource(AuthRealmListUsersResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][]{{"create-user", "POST"}, {"delete-user", "DELETE"}, {"list-users", "GET"}};
}

	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
