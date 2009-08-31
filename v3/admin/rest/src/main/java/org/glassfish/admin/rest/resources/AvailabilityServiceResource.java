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
import com.sun.enterprise.config.serverbeans.AvailabilityService;
public class AvailabilityServiceResource extends TemplateResource<AvailabilityService> {

	@Path("jms-availability/")
	public JmsAvailabilityResource getJmsAvailabilityResource() {
		JmsAvailabilityResource resource = resourceContext.getResource(JmsAvailabilityResource.class);
		resource.setEntity(getEntity().getJmsAvailability() );
		return resource;
	}
	@Path("web-container-availability/")
	public WebContainerAvailabilityResource getWebContainerAvailabilityResource() {
		WebContainerAvailabilityResource resource = resourceContext.getResource(WebContainerAvailabilityResource.class);
		resource.setEntity(getEntity().getWebContainerAvailability() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("ejb-container-availability/")
	public EjbContainerAvailabilityResource getEjbContainerAvailabilityResource() {
		EjbContainerAvailabilityResource resource = resourceContext.getResource(EjbContainerAvailabilityResource.class);
		resource.setEntity(getEntity().getEjbContainerAvailability() );
		return resource;
	}
}
