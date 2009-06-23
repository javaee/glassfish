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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.api.core.ResourceContext;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;


/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
public abstract class TemplateListOfResource<E extends ConfigBeanProxy> {

    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected List<E> entity;

    /** Creates a new instance of xxxResource */
    public TemplateListOfResource() {
    }


    @GET
    @Produces({"application/json", "text/html", "application/xml"})
    public List<Dom> get(@QueryParam("expandLevel")
            @DefaultValue("1") int expandLevel) {

        List<Dom> domList = new ArrayList();
        List<E> entities = getEntity();
        if (entities==null){
            return domList;//empty one
        }
        Iterator iterator = entities.iterator();
        E e;
        while (iterator.hasNext()) {
            e = (E) iterator.next();
            domList.add(Dom.unwrap(e));
        }

        return domList;
    }


    public void setEntity(List<E> p) {
        entity = p;
    }


    public List<E> getEntity() {
        return entity;
    }


    @POST //create
    @Produces(MediaType.TEXT_HTML)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public Response CreateResource(HashMap<String, String> data) {
        try {
            if (data.containsKey("error")) {
                return Response.status(415).entity("Unable to parse the input entity. Please check the syntax.").build();//unsupported media
            }

            //Command to execute
            String commandName = getPostCommand();
            adjustParameters(data);
            String resourceToCreate = uriInfo.getAbsolutePath() +
                "/" + data.get("DEFAULT");

            if (null != commandName) {
                CommandRunner cr = RestService.habitat.getComponent(CommandRunner.class);
                ActionReport ar = RestService.habitat.getComponent(ActionReport.class);
                Properties p = new Properties();
                CommandModel cm = cr.getModel(commandName, RestService.logger);
                java.util.Collection<CommandModel.ParamModel> params = cm.getParameters();
                //print(params);
                p.putAll(data);

                cr.doCommand(commandName, p, ar);

                ActionReport.ExitCode exitCode = ar.getActionExitCode();
                if (exitCode == ActionReport.ExitCode.SUCCESS) {
                    return Response.status(201).entity("\"" + resourceToCreate +  //201 - created
                    "\"" + " created successfully.").build();
                }

                String errorMessage = getErrorMessage(data, ar);
                return Response.status(400).entity(errorMessage).build(); // 400 - bad request
            }
            return Response.status(403).entity("POST on \"" +   // 403 - forbidden
                resourceToCreate + "\" is forbidden.").build();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    private void print(Collection<CommandModel.ParamModel> params) {
        for (CommandModel.ParamModel pm : params) {
            System.out.println("Command Param: " + pm.getName());
            System.out.println("Command Param Type: " + pm.getType());
            System.out.println("Command Param Name: " + pm.getParam().name());
            System.out.println("Command Param Shortname: " + pm.getParam().shortName());
        }
    }


    abstract public String getPostCommand();


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


    private String getErrorMessage(HashMap<String, String> data, ActionReport ar) {
        String message;
        //error info
        message = ar.getMessage();

        if (data.isEmpty()) {
            try {
                //usage info
                message = ar.getTopMessagePart().getChildren().get(0).getMessage();
            } catch (Exception e) {
                message = ar.getMessage();
            }
        }
        return message;
    }
}
