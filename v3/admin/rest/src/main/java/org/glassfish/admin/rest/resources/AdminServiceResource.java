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
import com.sun.enterprise.config.serverbeans.AdminService;
public class AdminServiceResource extends TemplateResource<AdminService> {

	@Path("jmx-connector/")
	public ListJmxConnectorResource getJmxConnectorResource() {
		ListJmxConnectorResource resource = resourceContext.getResource(ListJmxConnectorResource.class);
		resource.setEntity(getEntity().getJmxConnector() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("das-config/")
	public DasConfigResource getDasConfigResource() {
		DasConfigResource resource = resourceContext.getResource(DasConfigResource.class);
		resource.setEntity(getEntity().getDasConfig() );
		return resource;
	}
}
