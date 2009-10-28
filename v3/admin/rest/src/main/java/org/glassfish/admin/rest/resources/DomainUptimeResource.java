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

import java.util.HashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.provider.StringResult;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.api.ActionReport;

public class DomainUptimeResource {

public DomainUptimeResource() {
__resourceUtil = new ResourceUtil();
}
@GET
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
public StringResult executeCommand(
 	) {
try {
	java.util.Properties properties = new java.util.Properties();
if (commandParams != null) {
//formulate parent-link attribute for this command resource
//Parent link attribute may or may not be the id/target attribute
if (isLinkedToParent) {
__resourceUtil.resolveParentParamValue(commandParams, uriInfo);
}
properties.putAll(commandParams);
}

ActionReport actionReport = __resourceUtil.runCommand(commandName, properties, RestService.getHabitat());

ActionReport.ExitCode exitCode = actionReport.getActionExitCode();

StringResult results = new StringResult(commandName, __resourceUtil.getMessage(actionReport), options());
if (exitCode == ActionReport.ExitCode.SUCCESS) {
results.setStatusCode(200); /*200 - ok*/
} else {
results.setStatusCode(400); /*400 - bad request*/
results.setIsError(true);
results.setErrorMessage(actionReport.getMessage());
}

return results;

} catch (Exception e) {
throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
}
}
@OPTIONS
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML})
public OptionsResult options() {
OptionsResult optionsResult = new OptionsResult(resourceName);
try {
//command method metadata
MethodMetaData methodMetaData = __resourceUtil.getMethodMetaData(
commandName, commandParams, Constants.QUERY_PARAMETER, RestService.getHabitat(), RestService.logger);
optionsResult.putMethodMetaData(commandMethod, methodMetaData);
} catch (Exception e) {
throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
}

return optionsResult;
}

public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ResourceUtil.class);
@Context
protected HttpHeaders requestHeaders;
@Context
protected UriInfo uriInfo;

private static final String resourceName = "DomainUptime";
private static final String commandName = "uptime";
private static final String commandDisplayName = "uptime";
private static final String commandMethod = "GET";
private static final String commandAction = "Uptime";
private HashMap<String, String> commandParams = null;
private static final boolean isLinkedToParent = false;
private ResourceUtil __resourceUtil;
}
