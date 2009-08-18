/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Tue Aug 11 16:09:05 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
public class ProviderConfigResource extends TemplateResource<ProviderConfig> {

	@Path("request-policy/")
	public RequestPolicyResource getRequestPolicyResource() {
		RequestPolicyResource resource = resourceContext.getResource(RequestPolicyResource.class);
		resource.setEntity(getEntity().getRequestPolicy() );
		return resource;
	}
	@Path("response-policy/")
	public ResponsePolicyResource getResponsePolicyResource() {
		ResponsePolicyResource resource = resourceContext.getResource(ResponsePolicyResource.class);
		resource.setEntity(getEntity().getResponsePolicy() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
