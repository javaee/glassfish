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
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
public class WebServiceEndpointResource extends TemplateResource<WebServiceEndpoint> {

	@Path("registry-location/")
	public ListRegistryLocationResource getRegistryLocationResource() {
		ListRegistryLocationResource resource = resourceContext.getResource(ListRegistryLocationResource.class);
		resource.setEntity(getEntity().getRegistryLocation() );
		return resource;
	}
	@Path("transformation-rule/")
	public ListTransformationRuleResource getTransformationRuleResource() {
		ListTransformationRuleResource resource = resourceContext.getResource(ListTransformationRuleResource.class);
		resource.setEntity(getEntity().getTransformationRule() );
		return resource;
	}
}
