/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.StringListResult;
import org.glassfish.api.ActionReport;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.jersey.api.core.ResourceContext;


/**
 * @author Rajeshwar Patil
 */
public abstract class CollectionLeafResource {
    @Context
    protected HttpHeaders requestHeaders;

    @Context
    protected UriInfo uriInfo;

    @Context
    protected ResourceContext resourceContext;
    protected List<String> entity;

    public final static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CollectionLeafResource.class);

    /** Creates a new instance of xxxResource */
    public CollectionLeafResource() {
        __resourceUtil = new ResourceUtil();
    }


    public void setEntity(List<String> p) {
        entity = p;
    }


    public List<String> getEntity() {
        return entity;
    }


    @GET
    @Produces({MediaType.TEXT_HTML,
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public StringListResult get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return new StringListResult(getName(), getEntity(), getPostCommand(),
                getDeleteCommand(), options());
    }


    @POST //create
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_FORM_URLENCODED})
    public Response create(HashMap<String, String> data) {
        //hack-1 : support delete method for html
        //Currently, browsers do not support delete method. For html media,
        //delete operations can be supported through POST. Redirect html
        //client POST request for delete operation to DELETE method.
        if ((data.containsKey("operation")) &&
                (data.get("operation").equals("__deleteoperation"))) {
            data.remove("operation");
            return delete(data);
        }

        return runCommand(getPostCommand(), data, "rest.resource.create.message",
            "\"{0}\" created successfully.", "rest.resource.post.forbidden",
                 "POST on \"{0}\" is forbidden.");
    }


    @DELETE //delete
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_FORM_URLENCODED})
    public Response delete(HashMap<String, String> data) {
        return runCommand(getDeleteCommand(), data, "rest.resource.delete.message",
            "\"{0}\" deleted successfully.", "rest.resource.delete.forbidden",
                 "DELETE on \"{0}\" is forbidden.");
    }


    @OPTIONS
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML})
    public OptionsResult options() {
        OptionsResult optionsResult =
                new OptionsResult(__resourceUtil.getResourceName(uriInfo));

        try {
            //GET meta data
            optionsResult.putMethodMetaData("GET", new MethodMetaData());

            //POST meta data
            String postCommand = getPostCommand();
            if (postCommand != null) {
                MethodMetaData postMethodMetaData = __resourceUtil.getMethodMetaData(
                    postCommand, RestService.getHabitat(), RestService.logger);
                postMethodMetaData.setDescription("Create");
                optionsResult.putMethodMetaData("POST", postMethodMetaData);
            }

            //DELETE meta data
            String deleteCommand = getDeleteCommand();
            if (deleteCommand != null) {
                MethodMetaData deleteMethodMetaData = __resourceUtil.getMethodMetaData(
                        deleteCommand, RestService.getHabitat(), RestService.logger);
                deleteMethodMetaData.setDescription("Delete");
                optionsResult.putMethodMetaData("DELETE", deleteMethodMetaData);
            }
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return optionsResult;
    }


    private void addDefaultParameter(HashMap<String, String> data) {
        int index = uriInfo.getAbsolutePath().getPath().lastIndexOf('/');
        String defaultParameterValue = uriInfo.getAbsolutePath().getPath().substring(index + 1);
        data.put("DEFAULT", defaultParameterValue);
    }


    protected String getPostCommand(){
        return null;
    }


    protected String getDeleteCommand() {
        return null;
    }


    protected String getName() {
        ResourceUtil resourceUtil = new ResourceUtil();
        return resourceUtil.getResourceName(uriInfo);
    }


    private Response runCommand(String commandName, HashMap<String, String> data,
        String successMsgKey, String successMsg, String operationForbiddenMsgKey,
            String operationForbiddenMsg ) {
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return __resourceUtil.getResponse(400, /*parsing error*/
                        errorMessage, requestHeaders, uriInfo);
            }

            __resourceUtil.purgeEmptyEntries(data);
            __resourceUtil.adjustParameters(data);

            String attributeName = data.get("DEFAULT");

            if (null != commandName) {
                ActionReport actionReport = __resourceUtil.runCommand(commandName,
                    data, RestService.getHabitat());

                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    String successMessage =
                        localStrings.getLocalString(successMsgKey,
                            successMsg, new Object[] {attributeName});
                    return __resourceUtil.getResponse(200, /*200 - success*/
                         successMessage, requestHeaders, uriInfo);
                }

                String errorMessage = getErrorMessage(data, actionReport);
                return __resourceUtil.getResponse(400, /*400 - bad request*/
                    errorMessage, requestHeaders, uriInfo);
            }
            String message =
                localStrings.getLocalString(operationForbiddenMsgKey, 
                    operationForbiddenMsg, new Object[] {uriInfo.getAbsolutePath()});
            return __resourceUtil.getResponse(403, /*403 - forbidden*/
                 message, requestHeaders, uriInfo);

        } catch (Exception e) {
            throw new WebApplicationException(e,
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    private String getErrorMessage(HashMap<String, String> data, ActionReport ar) {
        String message;
        //error info
        message = ar.getMessage();

        /*if (data.isEmpty()) {
            try {
                //usage info
                message = ar.getTopMessagePart().getChildren().get(0).getMessage();
            } catch (Exception e) {
                message = ar.getMessage();
            }
        }*/
        return message;
    }

    private ResourceUtil __resourceUtil;
}
