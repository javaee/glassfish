/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Wed Aug 26 14:38:43 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.SecurityService;
public class SecurityServiceResource extends TemplateResource<SecurityService> {

	@Path("auth-realm/")
	public ListAuthRealmResource getAuthRealmResource() {
		ListAuthRealmResource resource = resourceContext.getResource(ListAuthRealmResource.class);
		resource.setEntity(getEntity().getAuthRealm() );
		return resource;
	}
	@Path("audit-module/")
	public ListAuditModuleResource getAuditModuleResource() {
		ListAuditModuleResource resource = resourceContext.getResource(ListAuditModuleResource.class);
		resource.setEntity(getEntity().getAuditModule() );
		return resource;
	}
	@Path("message-security-config/")
	public ListMessageSecurityConfigResource getMessageSecurityConfigResource() {
		ListMessageSecurityConfigResource resource = resourceContext.getResource(ListMessageSecurityConfigResource.class);
		resource.setEntity(getEntity().getMessageSecurityConfig() );
		return resource;
	}
	@Path("jacc-provider/")
	public ListJaccProviderResource getJaccProviderResource() {
		ListJaccProviderResource resource = resourceContext.getResource(ListJaccProviderResource.class);
		resource.setEntity(getEntity().getJaccProvider() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
