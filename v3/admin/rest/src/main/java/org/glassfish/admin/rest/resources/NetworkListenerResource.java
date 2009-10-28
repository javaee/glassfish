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
import com.sun.grizzly.config.dom.NetworkListener;
public class NetworkListenerResource extends TemplateResource<NetworkListener> {

@Path("create-ssl/")
public NetworkListenerCreateSslResource getNetworkListenerCreateSslResource() {
NetworkListenerCreateSslResource resource = resourceContext.getResource(NetworkListenerCreateSslResource.class);
return resource;
}

@Path("delete-ssl/")
public NetworkListenerDeleteSslResource getNetworkListenerDeleteSslResource() {
NetworkListenerDeleteSslResource resource = resourceContext.getResource(NetworkListenerDeleteSslResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][]{{"create-ssl", "POST"}, {"delete-ssl", "DELETE"}};
}

@Override
public String getDeleteCommand() {
	return "delete-network-listener";
}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
