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
import com.sun.enterprise.config.serverbeans.Resources;
public class ResourcesResource extends TemplateResource<Resources> {

@Path("persistence-manager-factory-resource/")
public ListPersistenceManagerFactoryResourceResource getPersistenceManagerFactoryResourceResource() {
ListPersistenceManagerFactoryResourceResource resource = resourceContext.getResource(ListPersistenceManagerFactoryResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource) {
newList.add((com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("admin-object-resource/")
public ListAdminObjectResourceResource getAdminObjectResourceResource() {
ListAdminObjectResourceResource resource = resourceContext.getResource(ListAdminObjectResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.AdminObjectResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.AdminObjectResource) {
newList.add((com.sun.enterprise.config.serverbeans.AdminObjectResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("work-security-map/")
public ListWorkSecurityMapResource getWorkSecurityMapResource() {
ListWorkSecurityMapResource resource = resourceContext.getResource(ListWorkSecurityMapResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.WorkSecurityMap> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.WorkSecurityMap) {
newList.add((com.sun.enterprise.config.serverbeans.WorkSecurityMap)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("jdbc-resource/")
public ListJdbcResourceResource getJdbcResourceResource() {
ListJdbcResourceResource resource = resourceContext.getResource(ListJdbcResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.JdbcResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.JdbcResource) {
newList.add((com.sun.enterprise.config.serverbeans.JdbcResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("resource-adapter-config/")
public ListResourceAdapterConfigResource getResourceAdapterConfigResource() {
ListResourceAdapterConfigResource resource = resourceContext.getResource(ListResourceAdapterConfigResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ResourceAdapterConfig> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.ResourceAdapterConfig) {
newList.add((com.sun.enterprise.config.serverbeans.ResourceAdapterConfig)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("external-jndi-resource/")
public ListExternalJndiResourceResource getExternalJndiResourceResource() {
ListExternalJndiResourceResource resource = resourceContext.getResource(ListExternalJndiResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ExternalJndiResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.ExternalJndiResource) {
newList.add((com.sun.enterprise.config.serverbeans.ExternalJndiResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("jdbc-connection-pool/")
public ListJdbcConnectionPoolResource getJdbcConnectionPoolResource() {
ListJdbcConnectionPoolResource resource = resourceContext.getResource(ListJdbcConnectionPoolResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.JdbcConnectionPool> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.JdbcConnectionPool) {
newList.add((com.sun.enterprise.config.serverbeans.JdbcConnectionPool)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("custom-resource/")
public ListCustomResourceResource getCustomResourceResource() {
ListCustomResourceResource resource = resourceContext.getResource(ListCustomResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.CustomResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.CustomResource) {
newList.add((com.sun.enterprise.config.serverbeans.CustomResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("mail-resource/")
public ListMailResourceResource getMailResourceResource() {
ListMailResourceResource resource = resourceContext.getResource(ListMailResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.MailResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.MailResource) {
newList.add((com.sun.enterprise.config.serverbeans.MailResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("connector-resource/")
public ListConnectorResourceResource getConnectorResourceResource() {
ListConnectorResourceResource resource = resourceContext.getResource(ListConnectorResourceResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ConnectorResource> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.ConnectorResource) {
newList.add((com.sun.enterprise.config.serverbeans.ConnectorResource)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("connector-connection-pool/")
public ListConnectorConnectionPoolResource getConnectorConnectionPoolResource() {
ListConnectorConnectionPoolResource resource = resourceContext.getResource(ListConnectorConnectionPoolResource.class);
java.util.List<com.sun.enterprise.config.serverbeans.Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ConnectorConnectionPool> newList = new java.util.ArrayList();
for (com.sun.enterprise.config.serverbeans.Resource r: l){
try {
if (r instanceof com.sun.enterprise.config.serverbeans.ConnectorConnectionPool) {
newList.add((com.sun.enterprise.config.serverbeans.ConnectorConnectionPool)r);
}
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


}
