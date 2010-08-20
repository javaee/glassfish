/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.resources.generated;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.glassfish.admin.rest.resources.*;
import org.glassfish.admin.rest.resources.custom.*;
public class ProtocolResource extends TemplateResource  {

@Path("create-http/")
public ProtocolCreateHttpResource getProtocolCreateHttpResource() {
ProtocolCreateHttpResource resource = resourceContext.getResource(ProtocolCreateHttpResource.class);
return resource;
}

@Path("create-protocol-filter/")
public ProtocolCreateProtocolFilterResource getProtocolCreateProtocolFilterResource() {
ProtocolCreateProtocolFilterResource resource = resourceContext.getResource(ProtocolCreateProtocolFilterResource.class);
return resource;
}

@Path("delete-protocol-filter/")
public ProtocolDeleteProtocolFilterResource getProtocolDeleteProtocolFilterResource() {
ProtocolDeleteProtocolFilterResource resource = resourceContext.getResource(ProtocolDeleteProtocolFilterResource.class);
return resource;
}

@Path("create-protocol-finder/")
public ProtocolCreateProtocolFinderResource getProtocolCreateProtocolFinderResource() {
ProtocolCreateProtocolFinderResource resource = resourceContext.getResource(ProtocolCreateProtocolFinderResource.class);
return resource;
}

@Path("delete-protocol-finder/")
public ProtocolDeleteProtocolFinderResource getProtocolDeleteProtocolFinderResource() {
ProtocolDeleteProtocolFinderResource resource = resourceContext.getResource(ProtocolDeleteProtocolFinderResource.class);
return resource;
}

@Path("delete-http/")
public ProtocolDeleteHttpResource getProtocolDeleteHttpResource() {
ProtocolDeleteHttpResource resource = resourceContext.getResource(ProtocolDeleteHttpResource.class);
return resource;
}

@Override
public String[][] getCommandResourcesPaths() {
return new String[][] {{"create-http", "POST", "create-http"} , {"create-protocol-filter", "POST", "create-protocol-filter"} , {"delete-protocol-filter", "DELETE", "delete-protocol-filter"} , {"create-protocol-finder", "POST", "create-protocol-finder"} , {"delete-protocol-finder", "DELETE", "delete-protocol-finder"} , {"delete-http", "DELETE", "delete-http"} };
}

@Override
public String getDeleteCommand() {
	return "delete-protocol";
}
	@Path("http-redirect/")
	public HttpRedirectResource getHttpRedirectResource() {
		HttpRedirectResource resource = resourceContext.getResource(HttpRedirectResource.class);
		resource.setParentAndTagName(getEntity() , "http-redirect");
		return resource;
	}
	@Path("port-unification/")
	public PortUnificationResource getPortUnificationResource() {
		PortUnificationResource resource = resourceContext.getResource(PortUnificationResource.class);
		resource.setParentAndTagName(getEntity() , "port-unification");
		return resource;
	}
	@Path("http/")
	public HttpResource getHttpResource() {
		HttpResource resource = resourceContext.getResource(HttpResource.class);
		resource.setParentAndTagName(getEntity() , "http");
		return resource;
	}
	@Path("property/")
	public PropertiesBagResource getPropertiesBagResource() {
		PropertiesBagResource resource = resourceContext.getResource(PropertiesBagResource.class);
		resource.setParentAndTagName(getEntity() , "property");
		return resource;
	}
	@Path("ssl/")
	public SslResource getSslResource() {
		SslResource resource = resourceContext.getResource(SslResource.class);
		resource.setParentAndTagName(getEntity() , "ssl");
		return resource;
	}
	@Path("protocol-chain-instance-handler/")
	public ProtocolChainInstanceHandlerResource getProtocolChainInstanceHandlerResource() {
		ProtocolChainInstanceHandlerResource resource = resourceContext.getResource(ProtocolChainInstanceHandlerResource.class);
		resource.setParentAndTagName(getEntity() , "protocol-chain-instance-handler");
		return resource;
	}
}
