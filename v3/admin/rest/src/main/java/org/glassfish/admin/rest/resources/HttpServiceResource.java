/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Wed Aug 26 14:38:42 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.HttpService;
public class HttpServiceResource extends TemplateResource<HttpService> {

	@Path("http-protocol/")
	public HttpProtocolResource getHttpProtocolResource() {
		HttpProtocolResource resource = resourceContext.getResource(HttpProtocolResource.class);
		resource.setEntity(getEntity().getHttpProtocol() );
		return resource;
	}
	@Path("connection-pool/")
	public ConnectionPoolResource getConnectionPoolResource() {
		ConnectionPoolResource resource = resourceContext.getResource(ConnectionPoolResource.class);
		resource.setEntity(getEntity().getConnectionPool() );
		return resource;
	}
	@Path("http-file-cache/")
	public HttpFileCacheResource getHttpFileCacheResource() {
		HttpFileCacheResource resource = resourceContext.getResource(HttpFileCacheResource.class);
		resource.setEntity(getEntity().getHttpFileCache() );
		return resource;
	}
	@Path("http-listener/")
	public ListHttpListenerResource getHttpListenerResource() {
		ListHttpListenerResource resource = resourceContext.getResource(ListHttpListenerResource.class);
		resource.setEntity(getEntity().getHttpListener() );
		return resource;
	}
	@Path("request-processing/")
	public RequestProcessingResource getRequestProcessingResource() {
		RequestProcessingResource resource = resourceContext.getResource(RequestProcessingResource.class);
		resource.setEntity(getEntity().getRequestProcessing() );
		return resource;
	}
	@Path("virtual-server/")
	public ListVirtualServerResource getVirtualServerResource() {
		ListVirtualServerResource resource = resourceContext.getResource(ListVirtualServerResource.class);
		resource.setEntity(getEntity().getVirtualServer() );
		return resource;
	}
	@Path("access-log/")
	public AccessLogResource getAccessLogResource() {
		AccessLogResource resource = resourceContext.getResource(AccessLogResource.class);
		resource.setEntity(getEntity().getAccessLog() );
		return resource;
	}
	@Path("property/")
	public ListPropertyResource getPropertyResource() {
		ListPropertyResource resource = resourceContext.getResource(ListPropertyResource.class);
		resource.setEntity(getEntity().getProperty() );
		return resource;
	}
	@Path("keep-alive/")
	public KeepAliveResource getKeepAliveResource() {
		KeepAliveResource resource = resourceContext.getResource(KeepAliveResource.class);
		resource.setEntity(getEntity().getKeepAlive() );
		return resource;
	}
}
