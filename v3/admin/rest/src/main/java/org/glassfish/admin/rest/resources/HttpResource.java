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
import com.sun.grizzly.config.dom.Http;
public class HttpResource extends TemplateResource<Http> {

	@Path("file-cache/")
	public FileCacheResource getFileCacheResource() {
		FileCacheResource resource = resourceContext.getResource(FileCacheResource.class);
		resource.setEntity(getEntity().getFileCache() );
		return resource;
	}
}
