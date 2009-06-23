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
import java.util.Properties;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * Rajeshwar Patil
 */
public class TemplateResource<E extends ConfigBeanProxy> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected E entity;

    /** Creates a new instance of xxxResource */
    public TemplateResource() {
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
    public Dom get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {
        if (getEntity() == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return Dom.unwrap(getEntity());
   }


    public ConfigBean getConfigBean() {
        return (ConfigBean) Dom.unwrap(getEntity());
    }


    @POST //create
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Dom updateDataItem(HashMap<String, String> data) {
        data.remove("submit");
        for (String name : data.keySet()) {
            System.out.println("updateDataItem name=" + name);
            System.out.println("value=" + data.get(name));
        }

        ActionReport actionReport = processRedirectsAnnotation(RestRedirect.OpType.POST, data);
        if (actionReport != null){
        return Dom.unwrap(getEntity());
        }
        //data.get("submit");
        Map<ConfigBean, Map<String, String>> mapOfChanges = new HashMap<ConfigBean, Map<String, String>>();
        mapOfChanges.put(getConfigBean(), data);

        try {
            RestService.configSupport.apply(mapOfChanges); //throws TransactionFailure
        } catch (TransactionFailure ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Dom.unwrap(getEntity());
    }


    @PUT  //update
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateEntity(HashMap<String, String> data) {
        try {
            if (data.containsKey("error")) {
             return Response.status(415).entity(
                 "Unable to parse the input entity. Please check the syntax.").build();//unsupported media
            }

            Map<ConfigBean, Map<String, String>> mapOfChanges = new HashMap<ConfigBean, Map<String, String>>();
            mapOfChanges.put(getConfigBean(), data);
            RestService.configSupport.apply(mapOfChanges); //throws TransactionFailure
            return Response.ok().entity("\"" + uriInfo.getAbsolutePath() + "\" updated successfully").build();
        } catch (TransactionFailure ex) {
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

            adjustParameters(data);
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
                try {
                    String usageMessage = 
                        actionReport.getTopMessagePart().getChildren().get(0).getMessage();
                    errorMessage = errorMessage + "\n" + usageMessage;
                } catch (Exception e) {
                    //ignore
                }
                return Response.status(400).entity(errorMessage).build(); // 400 - bad request
            }
            return Response.status(403).entity("DELETE on \"" +   // 403 - forbidden
                uriInfo.getAbsolutePath() + "\" is forbidden.").build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
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

    private ActionReport processRedirectsAnnotation(RestRedirect.OpType type , HashMap<String, String> data) {

        Class<? extends ConfigBeanProxy> cbp = null;
        try {
            cbp = (Class<? extends ConfigBeanProxy>) getConfigBean().model.classLoaderHolder.get().loadClass(getConfigBean().model.targetTypeName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        RestRedirects restRedirects = cbp.getAnnotation(RestRedirects.class);
        if (restRedirects != null) {
            System.out.println("Annotation restRedirects are not null:===" + restRedirects);

            RestRedirect[] values = restRedirects.value();
            for (RestRedirect r : values) {
                //System.out.println("command name=" + r.commandName());
                //System.out.println("command type=" + r.opType());
                if (r.opType().equals(type)) {
                    CommandRunner cr = RestService.habitat.getComponent(CommandRunner.class);
                    ActionReport ar = RestService.habitat.getComponent(ActionReport.class);
                    Properties p = new Properties();
                    CommandModel cm = cr.getModel(r.commandName(), RestService.logger);
                    java.util.Collection<CommandModel.ParamModel> params = cm.getParameters();
                    p.putAll(data);

                    cr.doCommand(r.commandName(), p, ar);
                    return ar;//processed
                }
            }
        }
        return null;//not processed
    }


    private void adjustParameters(HashMap<String, String> data) {
        if (data != null) {
            if (!(data.containsKey("DEFAULT"))) {
                boolean isRenamed = renameParameter(data, "name", "DEFAULT");
                if (!isRenamed) {
                    renameParameter(data, "id", "DEFAULT");
                }
            }
        }
    }


    private boolean renameParameter(HashMap<String, String> data,
        String parameterToRename, String newName) {
        if ((data.containsKey(parameterToRename))) {
            String value = data.get(parameterToRename);
            data.remove(parameterToRename);
            data.put(newName, value);
            return true;
        }
        return false;
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
}
