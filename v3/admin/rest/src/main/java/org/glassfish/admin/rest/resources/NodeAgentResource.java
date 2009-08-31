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
import com.sun.enterprise.config.serverbeans.NodeAgent;
public class NodeAgentResource extends TemplateResource<NodeAgent> {

	@Path("auth-realm/")
	public AuthRealmResource getAuthRealmResource() {
		AuthRealmResource resource = resourceContext.getResource(AuthRealmResource.class);
		resource.setEntity(getEntity().getAuthRealm() );
		return resource;
	}
	@Path("jmx-connector/")
	public JmxConnectorResource getJmxConnectorResource() {
		JmxConnectorResource resource = resourceContext.getResource(JmxConnectorResource.class);
		resource.setEntity(getEntity().getJmxConnector() );
		return resource;
	}
	@Path("log-service/")
	public LogServiceResource getLogServiceResource() {
		LogServiceResource resource = resourceContext.getResource(LogServiceResource.class);
		resource.setEntity(getEntity().getLogService() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
}
