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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.api.core.ResourceContext;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.ArrayList;
import javax.ws.rs.PathParam;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.RestRedirect;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ValidationException;

import org.glassfish.admin.rest.provider.GetResult;
import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.MethodMetaData;
import org.jvnet.hk2.config.ConfigModel;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
public class TemplateResource {
    @Context
    protected HttpHeaders requestHeaders;

    @Context
    protected UriInfo uriInfo;

    @Context
    protected ResourceContext resourceContext;
    protected Dom entity;
    protected Dom parent;
    protected String tagName;

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(TemplateResource.class);

    final private static List<String> attributesToSkip = new ArrayList<String>() {{
        add("parent");
        add("name");
        add("children");
        add("submit");
    }};

    /** Creates a new instance of xxxResource */
    public TemplateResource() {
        __resourceUtil = new ResourceUtil();
    }

    public void setEntity(Dom p) {
        entity = p;
    }

    public Dom getEntity() {
        return entity;
    }
    
    public void setParentAndTagName(Dom parent, String tagName) {
        this.parent = parent;
        this.tagName = tagName;
        entity = parent.nodeElement(tagName);

    }

    @GET
    @Produces({MediaType.TEXT_HTML,
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public GetResult get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return new GetResult(getEntity(), getDeleteCommand(),
                getCommandResourcesPaths(), options());
    }

//    public ConfigBean getConfigBean() {
//        return (ConfigBean) Dom.unwrap(getEntity());
//    }

    // TODO: This is wrong. Updates are done via PUT
    @POST  //update
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Response updateEntity(HashMap<String, String> data) {
        try {
            //data.remove("submit");
            removeAttributesToBeSkipped(data);
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return __resourceUtil.getResponse(400, /*parsing error*/
                        errorMessage, requestHeaders, uriInfo);
            }

            __resourceUtil.purgeEmptyEntries(data);

            //hack-1 : support delete method for html
            //Currently, browsers do not support delete method. For html media,
            //delete operations can be supported through POST. Redirect html
            //client POST request for delete operation to DELETE method.
            if ((data.containsKey("operation")) &&
                    (data.get("operation").equals("__deleteoperation"))) {
                data.remove("operation");
                return delete(data, "true");
            }

            Map<ConfigBean, Map<String, String>> mapOfChanges = new HashMap<ConfigBean, Map<String, String>>();
            data = ResourceUtil.translateCamelCasedNamesToXMLNames(data);
            mapOfChanges.put((ConfigBean)getEntity(), data);
            RestService.getConfigSupport().apply(mapOfChanges); //throws TransactionFailure

            String successMessage = localStrings.getLocalString("rest.resource.update.message",
                    "\"{0}\" updated successfully.", new Object[] {uriInfo.getAbsolutePath()});
           return __resourceUtil.getResponse(200, successMessage, requestHeaders, uriInfo);
        } catch (Exception ex) {
            if (ex.getCause() instanceof ValidationException) {
                return __resourceUtil.getResponse(400, /*400 - bad request*/
                    ex.getMessage(), requestHeaders, uriInfo);
            } else {
                throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    protected void removeAttributesToBeSkipped(Map<String, String> data) {
        for (String item : attributesToSkip) {
            data.remove(item);
        }
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_OCTET_STREAM})
    public Response delete(HashMap<String, String> data, @DefaultValue("false") @QueryParam("cascade") String cascade) {
        //User can not directly delete the resource. User can only
        //do so implicitly through asadmin command
        try {
            if (data.containsKey("error")) {
                String errorMessage = localStrings.getLocalString("rest.request.parsing.error",
                        "Unable to parse the input entity. Please check the syntax.");
                return __resourceUtil.getResponse(400, /*parsing error*/
                        errorMessage, requestHeaders, uriInfo);
            }

            data.put("cascade", cascade);
            __resourceUtil.purgeEmptyEntries(data);

            __resourceUtil.adjustParameters(data);
            if (data.get("DEFAULT") == null) {
                addDefaultParameter(data);
            }

            String resourceName = getResourceName(uriInfo.getAbsolutePath().getPath(), "/");
            if (!data.get("DEFAULT").equals(resourceName)) {
                String errorMessage = localStrings.getLocalString("rest.resource.not.deleted",
                        "Resource not deleted. Value of \"name\" should be the name of this resource.");
                return __resourceUtil.getResponse(403, /*forbidden*/
                        errorMessage, requestHeaders, uriInfo);
            }

            ActionReport actionReport = runCommand(getDeleteCommand(), data);

            if (actionReport != null) {
                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    String successMessage = localStrings.getLocalString("rest.resource.delete.message",
                        "\"{0}\" deleted successfully.", new Object[] {uriInfo.getAbsolutePath()});
                    return __resourceUtil.getResponse(200, successMessage, requestHeaders, uriInfo); //200 - ok
                }

                String errorMessage = actionReport.getMessage();
                /*try {
                String usageMessage =
                actionReport.getTopMessagePart().getChildren().get(0).getMessage();
                errorMessage = errorMessage + "\n" + usageMessage;
                } catch (Exception e) {
                //ignore
                }*/
                return __resourceUtil.getResponse(400, errorMessage, requestHeaders, uriInfo); //400 - bad request
            }

            String message = localStrings.getLocalString("rest.resource.delete.forbidden",
                "DELETE on \"{0}\" is forbidden.", new Object[] {uriInfo.getAbsolutePath()});
            return __resourceUtil.getResponse(403, message, requestHeaders, uriInfo); //403 - forbidden
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @OPTIONS
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.APPLICATION_XML})
    public OptionsResult options() {
        OptionsResult optionsResult =
                new OptionsResult(__resourceUtil.getResourceName(uriInfo));

        try {
            //GET meta data
            optionsResult.putMethodMetaData("GET", new MethodMetaData());

            /////optionsResult.putMethodMetaData("POST", new MethodMetaData());
            MethodMetaData postMethodMetaData = __resourceUtil.getMethodMetaData(
                (ConfigBean)getEntity());
            postMethodMetaData.setDescription("Update");
            optionsResult.putMethodMetaData("POST", postMethodMetaData);


            //DELETE meta data
            String command = getDeleteCommand();
            if (command != null) {
                MethodMetaData deleteMethodMetaData = __resourceUtil.getMethodMetaData(
                        command, RestService.getHabitat(), RestService.logger);
                //In case of delete operation(command), do not  display/provide id attribute.
                deleteMethodMetaData.removeParamMetaData("id");
                optionsResult.putMethodMetaData("DELETE", deleteMethodMetaData);
            }
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return optionsResult;
    }
    /*
     * allows for remote files to be put in a tmp area and we pass the
     * local location of this file to the corresponding command instead of the content of the file
     * * Yu need to add  enctype="multipart/form-data" in the form
     * for ex:  <form action="http://localhost:4848/management/domain/applications/application" method="post" enctype="multipart/form-data">
     * then any param of type="file" will be uploaded, stored locally and the param will use the local location
     * on the server side (ie. just the path)

     **/

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void post(FormDataMultiPart formData) {
        /* data passed to the generic command running
         *
         * */
        HashMap<String, String> data = createDataBasedOnForm(formData);
        updateEntity(data); //execute the deploy command with a copy of the file locally

    }
    /*
     * allows for remote files to be put in a tmp area and we pass the
     * local location of this file to the corresponding command instead of the content of the file
     * * Yu need to add  enctype="multipart/form-data" in the form
     * for ex:  <form action="http://localhost:4848/management/domain/applications/application" method="post" enctype="multipart/form-data">
     * then any param of type="file" will be uploaded, stored locally and the param will use the local location
     * on the server side (ie. just the path)
    
     **/

    public static HashMap<String, String> createDataBasedOnForm(FormDataMultiPart formData) {
        HashMap<String, String> data = new HashMap<String, String>();
        try {
            /* data passed to the generic command running
             *
             * */

            Map<String, List<FormDataBodyPart>> m1 = formData.getFields();

            Set<String> ss = m1.keySet();
            for (String fieldName : ss) {
                FormDataBodyPart n = formData.getField(fieldName);
                Logger.getLogger(TemplateResource.class.getName()).log(Level.INFO, "fieldName=" + fieldName);


                if (n.getContentDisposition().getFileName() != null) {//we have a file
                    //save it and mark it as delete on exit.
                    InputStream fileStream = n.getValueAs(InputStream.class);
                    String mimeType = n.getMediaType().toString();

                    //Use just the filename without complete path. File creation
                    //in case of remote deployment failing because fo this.
                    String fileName = n.getContentDisposition().getFileName();
                    ResourceUtil resourceUtil = new ResourceUtil();
                    if (fileName.contains("/")) {
                        fileName = resourceUtil.getName(fileName, '/');
                    } else {
                        if (fileName.contains("\\")) {
                            fileName = resourceUtil.getName(fileName, '\\');
                        }
                    }

                    File f = saveFile(fileName, mimeType, fileStream);
                    f.deleteOnExit();
                    //put only the local path of the file in the same field.
                    data.put(fieldName, f.getAbsolutePath());

                } else {
                    try {
                        Logger.getLogger(TemplateResource.class.getName()).log(Level.INFO, "Values=" + fieldName + " === " + n.getValue());

                        data.put(fieldName, n.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(TemplateResource.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            formData.cleanup();
        }
        return data;

    }

    private static File saveFile(String fileName, String mimeType, InputStream fileStream) {


        BufferedOutputStream out = null;
        File f = null;
        try {

            if (fileName.contains(".")) {
                String prefix = fileName.substring(0, fileName.indexOf("."));
                String suffix = fileName.substring(fileName.indexOf("."), fileName.length());
                if (prefix.length() < 3) {
                    prefix = "glassfish" + prefix;
                }
                f = File.createTempFile(prefix, suffix);
            }


            out = new BufferedOutputStream(new FileOutputStream(f));
            byte[] buffer = new byte[32 * 1024];
            int bytesRead = 0;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return f;
        } catch (IOException ex) {
            Logger.getLogger(TemplateResource.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(TemplateResource.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
        return null;
    }

    /*
     * see if we can understand the configbeans annotations like:
     * @RestRedirects(
    {
    @RestRedirect(opType= RestRedirect.OpType.DELETE, commandName="undeploy"),
    @RestRedirect(opType= RestRedirect.OpType.POST, commandName = "redeploy")
    }
     *
     * */
    public String[][] getCommandResourcesPaths() {
        return new String[][]{};
    }

    public String getDeleteCommand() {
        return __resourceUtil.getCommand(
                RestRedirect.OpType.DELETE, (ConfigBean)getEntity());
    }

    private ActionReport runCommand(String commandName,
            HashMap<String, String> data) {

        if (commandName != null) {
            return __resourceUtil.runCommand(commandName,
                    data, RestService.getHabitat());//processed
        }

        return null;//not processed
    }


    private void addDefaultParameter(HashMap<String, String> data) {
        int index = uriInfo.getAbsolutePath().getPath().lastIndexOf('/');
        String defaultParameterValue = uriInfo.getAbsolutePath().getPath().substring(index + 1);
        data.put("DEFAULT", defaultParameterValue);
    }

    private String getResourceName(String absoluteName, String delimiter) {
        if (null == absoluteName) {
            return absoluteName;
        }
        int index = absoluteName.lastIndexOf(delimiter);
        if (index != -1) {
            index = index + delimiter.length();
            return absoluteName.substring(index);
        } else {
            return absoluteName;
        }
    }
    private ResourceUtil __resourceUtil;


    public void setBeanByKey(List<Dom> parentList,String id) {
        for (Dom c : parentList) {


            String keyAttributeName = null;
            ConfigModel model = c.model;
            if (model.key == null) {
                try {
                    for (String s : model.getAttributeNames()) {//no key, by default use the name attr
                        if (s.equals("name")) {
                            keyAttributeName = s;
                        }
                    }
                    if (keyAttributeName == null)//nothing, so pick the first one
                    {
                        keyAttributeName = model.getAttributeNames().iterator().next();
                    }
                } catch (Exception e) {
                    keyAttributeName = "ThisIsAModelBug:NoKeyAttr"; //no attr choice fo a key!!! Error!!!
                } //firstone
            } else {
                keyAttributeName = model.key.substring(1, model.key.length());
            }



            //Using '-' for back-slash in resource names
            //For example, jndi names has back-slash in it.
            String keyvalue = c.attribute(keyAttributeName.toLowerCase());
            if (keyvalue.equals(id)) {
                setEntity((ConfigBean) c);
            }
        }
    }
}
