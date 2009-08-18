/**
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
* Copyright 2009 Sun Microsystems, Inc. All rights reserved.
* Generated code from the com.sun.enterprise.config.serverbeans.*
* config beans, based on  HK2 meta model for these beans
* see generator at org.admin.admin.rest.GeneratorResource
* date=Tue Aug 11 16:09:06 PDT 2009
* Very soon, this generated code will be replace by asm or even better...more dynamic logic.
* Ludovic Champenois ludo@dev.java.net
*
**/
package org.glassfish.admin.rest.resources;
import com.sun.enterprise.config.serverbeans.*;
import javax.ws.rs.*;
import org.glassfish.admin.rest.TemplateResource;
import org.glassfish.admin.rest.provider.GetResult;
import com.sun.enterprise.config.serverbeans.Resources;
public class ResourcesResource extends TemplateResource<Resources> {

@Path("admin-object-resource/")
public ListAdminObjectResourceResource getAdminObjectResourceResource() {
ListAdminObjectResourceResource resource = resourceContext.getResource(ListAdminObjectResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.AdminObjectResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.AdminObjectResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("mail-resource/")
public ListMailResourceResource getMailResourceResource() {
ListMailResourceResource resource = resourceContext.getResource(ListMailResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.MailResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.MailResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("connector-connection-pool/")
public ListConnectorConnectionPoolResource getConnectorConnectionPoolResource() {
ListConnectorConnectionPoolResource resource = resourceContext.getResource(ListConnectorConnectionPoolResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ConnectorConnectionPool> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.ConnectorConnectionPool)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("work-security-map/")
public ListWorkSecurityMapResource getWorkSecurityMapResource() {
ListWorkSecurityMapResource resource = resourceContext.getResource(ListWorkSecurityMapResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.WorkSecurityMap> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.WorkSecurityMap)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("connector-resource/")
public ListConnectorResourceResource getConnectorResourceResource() {
ListConnectorResourceResource resource = resourceContext.getResource(ListConnectorResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ConnectorResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.ConnectorResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("jdbc-resource/")
public ListJdbcResourceResource getJdbcResourceResource() {
ListJdbcResourceResource resource = resourceContext.getResource(ListJdbcResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.JdbcResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.JdbcResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("external-jndi-resource/")
public ListExternalJndiResourceResource getExternalJndiResourceResource() {
ListExternalJndiResourceResource resource = resourceContext.getResource(ListExternalJndiResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ExternalJndiResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.ExternalJndiResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("resource-adapter-config/")
public ListResourceAdapterConfigResource getResourceAdapterConfigResource() {
ListResourceAdapterConfigResource resource = resourceContext.getResource(ListResourceAdapterConfigResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.ResourceAdapterConfig> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.ResourceAdapterConfig)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("custom-resource/")
public ListCustomResourceResource getCustomResourceResource() {
ListCustomResourceResource resource = resourceContext.getResource(ListCustomResourceResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.CustomResource> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.CustomResource)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


@Path("jdbc-connection-pool/")
public ListJdbcConnectionPoolResource getJdbcConnectionPoolResource() {
ListJdbcConnectionPoolResource resource = resourceContext.getResource(ListJdbcConnectionPoolResource.class);
java.util.List<Resource> l = entity.getResources();
java.util.List<com.sun.enterprise.config.serverbeans.JdbcConnectionPool> newList = new java.util.ArrayList();
for (Resource r: l){
try {
newList.add((com.sun.enterprise.config.serverbeans.JdbcConnectionPool)r);
} catch (Exception e){
}
}
resource.setEntity(newList );
return resource;
}


}
