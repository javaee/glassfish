/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.rest.resources;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
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
