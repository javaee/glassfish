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
import com.sun.enterprise.config.serverbeans.Cluster;
public class ClusterResource extends TemplateResource<Cluster> {

	@Path("application-ref/")
	public ListApplicationRefResource getApplicationRefResource() {
		ListApplicationRefResource resource = resourceContext.getResource(ListApplicationRefResource.class);
		resource.setEntity(getEntity().getApplicationRef() );
		return resource;
	}
	@Path("server-ref/")
	public ListServerRefResource getServerRefResource() {
		ListServerRefResource resource = resourceContext.getResource(ListServerRefResource.class);
		resource.setEntity(getEntity().getServerRef() );
		return resource;
	}
	@Path("resource-ref/")
	public ListResourceRefResource getResourceRefResource() {
		ListResourceRefResource resource = resourceContext.getResource(ListResourceRefResource.class);
		resource.setEntity(getEntity().getResourceRef() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("system-property/")
	public ListSystemPropertyResource getSystemPropertyResource() {
		ListSystemPropertyResource resource = resourceContext.getResource(ListSystemPropertyResource.class);
		resource.setEntity(getEntity().getSystemProperty() );
		return resource;
	}
}
