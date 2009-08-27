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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.api.core.ResourceContext;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import org.glassfish.admin.rest.provider.GetResult;
import org.glassfish.admin.rest.provider.OptionsResult;
import org.glassfish.admin.rest.provider.MethodMetaData;


/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
public class TemplateResource<E extends ConfigBeanProxy> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected E entity;


    /** Creates a new instance of xxxResource */
    public TemplateResource() {
        __resourceUtil = new ResourceUtil();
    }


    public void setEntity(E p) {
        entity = p;
    }


    public E getEntity() {
        return entity;
    }


    @GET
    @Produces({MediaType.APPLICATION_FORM_URLENCODED,
        MediaType.TEXT_HTML,
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML})
    public GetResult get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return new GetResult(Dom.unwrap(getEntity()), getDeleteCommand(),
            getCommandResourcesPaths());
    }


    public ConfigBean getConfigBean() {
        return (ConfigBean) Dom.unwrap(getEntity());
    }


    @POST  //update
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Response updateEntity(HashMap<String, String> data) {
        try {
            data.remove("submit");
            if (data.containsKey("error")) {
             return Response.status(415).entity(
                 "Unable to parse the input entity. Please check the syntax.").build();//unsupported media
            }

            __resourceUtil.purgeEmptyEntries(data);

            //hack-1 : support delete method for html
            //Currently, browsers do not support delete method. For html media,
            //delete operations can be supported through POST. Redirect html
            //client POST request for delete operation to DELETE method.
            if ((data.containsKey("operation")) &&
                (data.get("operation").equals("__deleteoperation"))) {
                data.remove("operation");
                return delete(data);
            }

            Map<ConfigBean, Map<String, String>> mapOfChanges = new HashMap<ConfigBean, Map<String, String>>();
            mapOfChanges.put(getConfigBean(), data);
            RestService.getConfigSupport().apply(mapOfChanges); //throws TransactionFailure
            return Response.ok().entity("\"" + uriInfo.getAbsolutePath() + "\" updated successfully").build();
        } catch (Exception ex) {
            System.out.println("exception" + ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @DELETE
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delete(HashMap<String, String> data) {
        //User can not directly delete the resource. User can only
        //do so implicitly through asadmin command
        try {
            if (data.containsKey("error")) {
                return Response.status(415).entity(
                    "Unable to parse the input entity. Please check the syntax.").build();//unsupported media
            }

            __resourceUtil.purgeEmptyEntries(data);

            __resourceUtil.adjustParameters(data);
            if (data.get("DEFAULT") == null) {
                addDefaultParameter(data);
            }

            String resourceName = getResourceName(uriInfo.getAbsolutePath().getPath(), "/");
            if (!data.get("DEFAULT").equals(resourceName)) {
                return Response.status(403).entity("Resource not deleted. Value of \"name\" should be the name of this resource.").build(); //forbidden
            }

            ActionReport actionReport = 
                processRedirectsAnnotation(RestRedirect.OpType.DELETE, data);

            if (actionReport != null) {
                ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    return Response.status(200).entity("\"" + uriInfo.getAbsolutePath() +  //200 - ok
                    "\"" + " deleted successfully.").build();
                }

                String errorMessage = actionReport.getMessage();
                /*try {
                    String usageMessage = 
                        actionReport.getTopMessagePart().getChildren().get(0).getMessage();
                    errorMessage = errorMessage + "\n" + usageMessage;
                } catch (Exception e) {
                    //ignore
                }*/
                return Response.status(400).entity(errorMessage).build(); // 400 - bad request
            }
            return Response.status(403).entity("DELETE on \"" +   // 403 - forbidden
                uriInfo.getAbsolutePath() + "\" is forbidden.").build();
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

            //POST meta data
            //FIXME -- Get hold of meta-data for config bean attributes and
            //set it in MethodMetaData object. For now, just provide POST method
            //without any message/entity information.
            optionsResult.putMethodMetaData("POST", new MethodMetaData());

            //DELETE meta data
            String command = getDeleteCommand();
            if (command != null) {
                MethodMetaData postMethodMetaData = __resourceUtil.getMethodMetaData(
                        command, RestService.getHabitat(), RestService.logger);
                optionsResult.putMethodMetaData("DELETE", postMethodMetaData);
            }
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return optionsResult;
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


    private String getDeleteCommand() {
         return __resourceUtil.getCommand(
            RestRedirect.OpType.DELETE, getConfigBean());
    }


    private ActionReport processRedirectsAnnotation(RestRedirect.OpType type,
            HashMap<String, String> data) {

        String commandName = __resourceUtil.getCommand(type, getConfigBean());
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
        if(null == absoluteName){
            return absoluteName;
        }
        int index = absoluteName.lastIndexOf(delimiter);
        if( index != -1) {
            index = index + delimiter.length();
            return absoluteName.substring(index);
        } else {
            return absoluteName;
        }
    }


    private ResourceUtil __resourceUtil;

}