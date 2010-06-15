/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
import javax.ws.rs.Path;
import org.glassfish.admin.rest.TemplateResource;
public class SystemApplicationsResource extends TemplateResource {

@Path("j2ee-application/")
public ListJ2eeApplicationResource getJ2eeApplicationResource() {
	ListJ2eeApplicationResource resource = resourceContext.getResource(ListJ2eeApplicationResource.class);
	resource.setParentAndTagName(getEntity() , "j2ee-application");
	return resource;
}
@Path("web-module/")
public ListWebModuleResource getWebModuleResource() {
	ListWebModuleResource resource = resourceContext.getResource(ListWebModuleResource.class);
	resource.setParentAndTagName(getEntity() , "web-module");
	return resource;
}
@Path("extension-module/")
public ListExtensionModuleResource getExtensionModuleResource() {
	ListExtensionModuleResource resource = resourceContext.getResource(ListExtensionModuleResource.class);
	resource.setParentAndTagName(getEntity() , "extension-module");
	return resource;
}
@Path("appclient-module/")
public ListAppclientModuleResource getAppclientModuleResource() {
	ListAppclientModuleResource resource = resourceContext.getResource(ListAppclientModuleResource.class);
	resource.setParentAndTagName(getEntity() , "appclient-module");
	return resource;
}
@Path("lifecycle-module/")
public ListLifecycleModuleResource getLifecycleModuleResource() {
	ListLifecycleModuleResource resource = resourceContext.getResource(ListLifecycleModuleResource.class);
	resource.setParentAndTagName(getEntity() , "lifecycle-module");
	return resource;
}
@Path("connector-module/")
public ListConnectorModuleResource getConnectorModuleResource() {
	ListConnectorModuleResource resource = resourceContext.getResource(ListConnectorModuleResource.class);
	resource.setParentAndTagName(getEntity() , "connector-module");
	return resource;
}
@Path("application/")
public ListApplicationResource getApplicationResource() {
	ListApplicationResource resource = resourceContext.getResource(ListApplicationResource.class);
	resource.setParentAndTagName(getEntity() , "application");
	return resource;
}
@Path("ejb-module/")
public ListEjbModuleResource getEjbModuleResource() {
	ListEjbModuleResource resource = resourceContext.getResource(ListEjbModuleResource.class);
	resource.setParentAndTagName(getEntity() , "ejb-module");
	return resource;
}
}
