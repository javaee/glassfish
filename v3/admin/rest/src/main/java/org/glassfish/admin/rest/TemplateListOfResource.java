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
package org.glassfish.admin.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.core.ResourceContext;

import com.sun.jersey.multipart.FormDataMultiPart;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.api.ActionReport;

import org.glassfish.admin.rest.provider.GetResultList;
import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.MethodMetaData;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
public abstract class TemplateListOfResource {
    @Context
    protected HttpHeaders requestHeaders;

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected List<Dom> entity;
    protected Dom parent;
    protected String tagName;

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TemplateListOfResource.class);

    /** Creates a new instance of xxxResource */
    public TemplateListOfResource() {
    }


    @GET
    @Produces({"text/html;qs=2", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public GetResultList get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {

        List<Dom> domList = new ArrayList();
        List<Dom> entities = getEntity();
        if (entities==null){
            return new GetResultList(domList, getPostCommand(),
               getCommandResourcesPaths(), options());//empty dom list
        }
        Iterator iterator = entities.iterator();
        ConfigBean e;
        while (iterator.hasNext()) {
            e = (ConfigBean) iterator.next();
            domList.add(e);
        }

        return new GetResultList(domList, getPostCommand(), getCommandResourcesPaths(), options());
    }


    public void setEntity(List<Dom> p) {
        entity = p;
    }


    public List<Dom> getEntity() {
        return entity;
    }
    public void setParentAndTagName(Dom parent, String tagName) {
        this.parent = parent;
        this.tagName = tagName;
        entity = parent.nodeElements(tagName);

    }

    @POST //create
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
        MediaType.APPLICATION_FORM_URLENCODED})
    public Response createResource(HashMap<String, String> data) {
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return ResourceUtil.getResponse(400, /*parsing error*/
                        errorMessage, requestHeaders, uriInfo);
            }

            ResourceUtil.purgeEmptyEntries(data);

            //Command to execute
            String commandName = getPostCommand();
            String resourceToCreate = uriInfo.getAbsolutePath() + "/";

            if (null != commandName) {
                // TODO: Not needed anymore?
//                data = __resourceUtil.translateCamelCasedNamesToCommandParamNames(data,commandName, RestService.getHabitat(), RestService.logger);
                ResourceUtil.adjustParameters(data); //adjusting for DEFAULT is required only while executing a CLI command
                resourceToCreate += data.get("DEFAULT");
                String typeOfResult = requestHeaders.getAcceptableMediaTypes().get(0).getSubtype();
                ActionReport actionReport = ResourceUtil.runCommand(commandName, data, RestService.getHabitat(),typeOfResult);

                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    String successMessage =
                        localStrings.getLocalString("rest.resource.create.message",
                            "\"{0}\" created successfully.", resourceToCreate);
                    return ResourceUtil.getResponse(201, /* 201 - created */
                         successMessage, requestHeaders, uriInfo);
                }

                String errorMessage = getErrorMessage(data, actionReport);
                return ResourceUtil.getResponse(400, /*400 - bad request*/
                        errorMessage, requestHeaders, uriInfo);
            } else {
                // create it on the fly without a create CLI command.

                Class<? extends ConfigBeanProxy> proxy = getElementTypeByName(parent, tagName);
                ConfigBean createdBean = ConfigSupport.createAndSet((ConfigBean) parent, proxy, data);
                String successMessage =
                        localStrings.getLocalString("rest.resource.create.message",
                        "\"{0}\" created successfully.", createdBean.getKey());
                return ResourceUtil.getResponse(201, //201 - created
                        successMessage, requestHeaders, uriInfo);

            }
        } catch (Exception e) {
            throw new WebApplicationException(e,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    public static Class<? extends ConfigBeanProxy> getElementTypeByName(Dom parentDom, String elementName)
            throws ClassNotFoundException {

        DomDocument document = parentDom.document;
        ConfigModel.Property a = parentDom.model.getElement(elementName);
        if (a != null) {
            if (a.isLeaf()) {
                //  : I am not too sure, but that should be a String @Element
                return null;
            } else {
                ConfigModel childModel = ((ConfigModel.Node) a).getModel();
                return (Class<? extends ConfigBeanProxy>) childModel.classLoaderHolder.get().loadClass(childModel.targetTypeName);
            }
        }
        // global lookup
        ConfigModel model = document.getModelByElementName(elementName);
        if (model != null) {
            return (Class<? extends ConfigBeanProxy>) model.classLoaderHolder.get().loadClass(model.targetTypeName);
        }

        return null;
    }

    //called in case of POST on application resource (deployment).
    //resourceToCreate is the name attribute if provided.
    private Response createResource(HashMap<String, String> data, String resourceToCreate) {
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return ResourceUtil.getResponse(400, /*parsing error*/
                        errorMessage, requestHeaders, uriInfo);
            }

            ResourceUtil.purgeEmptyEntries(data);

            //Command to execute
            String commandName = getPostCommand();
            ResourceUtil.defineDefaultParameters(data);

            if ((resourceToCreate == null) || (resourceToCreate.equals(""))) {
                String newResourceName = data.get("DEFAULT");
                if (newResourceName.contains("/")) {
                    newResourceName = Util.getName(newResourceName, '/');
                } else {
                    if (newResourceName.contains("\\")) {
                        newResourceName = Util.getName(newResourceName, '\\');
                    }
                }
                resourceToCreate = uriInfo.getAbsolutePath() +
                    "/" + newResourceName;
            } else {
                resourceToCreate = uriInfo.getAbsolutePath() +
                    "/" + resourceToCreate;
            }

            if (null != commandName) {
                            String typeOfResult = requestHeaders.getAcceptableMediaTypes().get(0).getSubtype();

                ActionReport actionReport = ResourceUtil.runCommand(commandName,
                    data, RestService.getHabitat(),typeOfResult);

                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    String successMessage =
                        localStrings.getLocalString("rest.resource.create.message",
                            "\"{0}\" created successfully.", new Object[] {resourceToCreate});
                    return ResourceUtil.getResponse(201, //201 - created
                         successMessage, requestHeaders, uriInfo);
                }

                String errorMessage = getErrorMessage(data, actionReport);
                return ResourceUtil.getResponse(400, /*400 - bad request*/
                    errorMessage, requestHeaders, uriInfo);
            }
            String message =
                localStrings.getLocalString("rest.resource.post.forbidden",
                    "POST on \"{0}\" is forbidden.", new Object[] {resourceToCreate});
            return ResourceUtil.getResponse(403, //403 - forbidden
                 message, requestHeaders, uriInfo);

        } catch (Exception e) {
            throw new WebApplicationException(e,
                Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /*
     * allows for remote files to be put in a tmp area and we pass the
     * local location of this file to the corresponding command instead of the content of the file
     * * Yu need to add  enctype="multipart/form-data" in the form
     * for ex:  <form action="http://localhost:4848/management/domain/applications/application" method="post" enctype="multipart/form-data">
     * then any param of type="file" will be uploaded, stored locally and the param will use the local location
     * on the server side (ie. just the path)

     * */

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA) 
    public Response post(FormDataMultiPart formData) {
            /* data passed to the generic command running
             *
             * */
            HashMap<String, String> data = TemplateResource.createDataBasedOnForm(formData);
            return createResource(data, data.get("name")); //execute the deploy command with a copy of the file locally

    }

    @OPTIONS 
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML})
    public OptionsResult options() {
        OptionsResult optionsResult = 
            new OptionsResult(Util.getResourceName(uriInfo));
        try {
            //GET meta data
            optionsResult.putMethodMetaData("GET", new MethodMetaData());

            //POST meta data
            String command = getPostCommand();
            if (command != null) {
                MethodMetaData postMethodMetaData = ResourceUtil.getMethodMetaData(
                    command, RestService.getHabitat(), RestService.logger);
                postMethodMetaData.setDescription("Create");
                if (Util.getResourceName(uriInfo).equals("Application")) {
                    postMethodMetaData.setIsFileUploadOperation(true);
                }
                optionsResult.putMethodMetaData("POST", postMethodMetaData);
            } else {
                ConfigModel.Node prop = (ConfigModel.Node) parent.model.getElement(tagName);
                if (prop == null) { //maybe null when Element ("*") is used
                    ConfigModel.Node prop2 = (ConfigModel.Node) parent.model.getElement("*");

                    ConfigModel childModel = prop2.getModel();
                    Class<?> subType = childModel.classLoaderHolder.get().loadClass(childModel.targetTypeName); ///  a shoulf be the typename
                    List<ConfigModel> lcm = parent.document.getAllModelsImplementing(subType);
                    if (lcm != null) {
                        for (ConfigModel cmodel : lcm) {
                            if (cmodel.getTagName().equals(tagName)) {
                                MethodMetaData postMethodMetaData = ResourceUtil.getMethodMetaData2(parent,
                                        cmodel, Constants.MESSAGE_PARAMETER);
                                postMethodMetaData.setDescription("Update");
                                optionsResult.putMethodMetaData("POST", postMethodMetaData);
                            }
                        }
                    }
                } else {
                    MethodMetaData postMethodMetaData = ResourceUtil.getMethodMetaData2(parent,
                            prop.getModel(), Constants.MESSAGE_PARAMETER);
                    postMethodMetaData.setDescription("Update");
                    optionsResult.putMethodMetaData("POST", postMethodMetaData);
                }

            }
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return optionsResult;
    }


    abstract public String getPostCommand();


    public String[][] getCommandResourcesPaths() {
        return new String[][] {};
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
}
