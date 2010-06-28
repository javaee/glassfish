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
public class ResourcesResource extends TemplateResource {

@Path("work-security-map/")
public ListWorkSecurityMapResource getWorkSecurityMapResource() {
	ListWorkSecurityMapResource resource = resourceContext.getResource(ListWorkSecurityMapResource.class);
	resource.setParentAndTagName(getEntity() , "work-security-map");
	return resource;
}
@Path("connector-connection-pool/")
public ListConnectorConnectionPoolResource getConnectorConnectionPoolResource() {
	ListConnectorConnectionPoolResource resource = resourceContext.getResource(ListConnectorConnectionPoolResource.class);
	resource.setParentAndTagName(getEntity() , "connector-connection-pool");
	return resource;
}
@Path("persistence-manager-factory-resource/")
public ListPersistenceManagerFactoryResourceResource getPersistenceManagerFactoryResourceResource() {
	ListPersistenceManagerFactoryResourceResource resource = resourceContext.getResource(ListPersistenceManagerFactoryResourceResource.class);
	resource.setParentAndTagName(getEntity() , "persistence-manager-factory-resource");
	return resource;
}
@Path("external-jndi-resource/")
public ListExternalJndiResourceResource getExternalJndiResourceResource() {
	ListExternalJndiResourceResource resource = resourceContext.getResource(ListExternalJndiResourceResource.class);
	resource.setParentAndTagName(getEntity() , "external-jndi-resource");
	return resource;
}
@Path("custom-resource/")
public ListCustomResourceResource getCustomResourceResource() {
	ListCustomResourceResource resource = resourceContext.getResource(ListCustomResourceResource.class);
	resource.setParentAndTagName(getEntity() , "custom-resource");
	return resource;
}
@Path("connector-resource/")
public ListConnectorResourceResource getConnectorResourceResource() {
	ListConnectorResourceResource resource = resourceContext.getResource(ListConnectorResourceResource.class);
	resource.setParentAndTagName(getEntity() , "connector-resource");
	return resource;
}
@Path("jdbc-connection-pool/")
public ListJdbcConnectionPoolResource getJdbcConnectionPoolResource() {
	ListJdbcConnectionPoolResource resource = resourceContext.getResource(ListJdbcConnectionPoolResource.class);
	resource.setParentAndTagName(getEntity() , "jdbc-connection-pool");
	return resource;
}
@Path("mail-resource/")
public ListMailResourceResource getMailResourceResource() {
	ListMailResourceResource resource = resourceContext.getResource(ListMailResourceResource.class);
	resource.setParentAndTagName(getEntity() , "mail-resource");
	return resource;
}
@Path("admin-object-resource/")
public ListAdminObjectResourceResource getAdminObjectResourceResource() {
	ListAdminObjectResourceResource resource = resourceContext.getResource(ListAdminObjectResourceResource.class);
	resource.setParentAndTagName(getEntity() , "admin-object-resource");
	return resource;
}
@Path("jdbc-resource/")
public ListJdbcResourceResource getJdbcResourceResource() {
	ListJdbcResourceResource resource = resourceContext.getResource(ListJdbcResourceResource.class);
	resource.setParentAndTagName(getEntity() , "jdbc-resource");
	return resource;
}
@Path("resource-adapter-config/")
public ListResourceAdapterConfigResource getResourceAdapterConfigResource() {
	ListResourceAdapterConfigResource resource = resourceContext.getResource(ListResourceAdapterConfigResource.class);
	resource.setParentAndTagName(getEntity() , "resource-adapter-config");
	return resource;
}
}
