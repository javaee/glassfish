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

import org.glassfish.admin.rest.provider.CommandResourceGetResult;
import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.api.ActionReport;

public class DomainRotateLogResource {

public DomainRotateLogResource() {
__resourceUtil = new ResourceUtil();
}
@POST
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
public Response executeCommand(HashMap<String, String> data) {
try {
if (data.containsKey("error")) {
String errorMessage = localStrings.getLocalString("rest.request.parsing.error", "Unable to parse the input entity. Please check the syntax.");
return __resourceUtil.getResponse(400, /*parsing error*/
 errorMessage, requestHeaders, uriInfo);
}

/*formulate id attribute for this command resource*/
String parent = __resourceUtil.getParentName(uriInfo);
if (parent != null) {
data.put("id", parent);
}

__resourceUtil.adjustParameters(data);

__resourceUtil.purgeEmptyEntries(data);

ActionReport actionReport = __resourceUtil.runCommand(commandName, data, RestService.getHabitat());

ActionReport.ExitCode exitCode = actionReport.getActionExitCode();

if (exitCode == ActionReport.ExitCode.SUCCESS) {
String successMessage = localStrings.getLocalString("rest.request.success.message",
"{0} of {1} executed successfully.", new Object[] {commandMethod, uriInfo.getAbsolutePath()});
return __resourceUtil.getResponse(200, /*200 - ok*/
 successMessage, requestHeaders, uriInfo);
}

String errorMessage = actionReport.getMessage();
return __resourceUtil.getResponse(400, /*400 - bad request*/
 errorMessage, requestHeaders, uriInfo);
} catch (Exception e) {
throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
}
}
//Handle POST request without any entity(input).
//Do not care what the Content-Type is.
@POST
public Response executeCommand() {
try {
return executeCommand(new HashMap<String, String>());
} catch (Exception e) {
throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
}
}
@GET
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public CommandResourceGetResult get() {
try {
return new CommandResourceGetResult(resourceName, commandName, commandDisplayName, commandMethod, options());
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
commandName, Constants.MESSAGE_PARAMETER, RestService.getHabitat(), RestService.logger);
//GET meta data
optionsResult.putMethodMetaData("GET", new MethodMetaData());
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

private static final String resourceName = "DomainRotateLog";
private static final String commandName = "rotate-log";
private static final String commandDisplayName = "rotate-log";
private static final String commandMethod = "POST";
private ResourceUtil __resourceUtil;
}
