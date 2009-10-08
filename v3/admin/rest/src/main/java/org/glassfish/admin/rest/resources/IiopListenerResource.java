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
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.IiopListener;
public class IiopListenerResource extends TemplateResource<IiopListener> {

@Path("create-ssl/")
public IiopListenerCreateSslResource getIiopListenerCreateSslResource() {
IiopListenerCreateSslResource resource = resourceContext.getResource(IiopListenerCreateSslResource.class);
return resource;
}

@Path("delete-ssl/")
public IiopListenerDeleteSslResource getIiopListenerDeleteSslResource() {
IiopListenerDeleteSslResource resource = resourceContext.getResource(IiopListenerDeleteSslResource.class);
return resource;
}

public String[][] getCommandResourcesPaths() {
return new String[][]{{"create-ssl", "POST"}, {"delete-ssl", "DELETE"}};
}

	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("ssl/")
	public SslResource getSslResource() {
		SslResource resource = resourceContext.getResource(SslResource.class);
		resource.setEntity(getEntity().getSsl() );
		return resource;
	}
}
