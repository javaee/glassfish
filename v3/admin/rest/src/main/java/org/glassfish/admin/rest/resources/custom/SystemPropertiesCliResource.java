/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.resources.custom;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.container.ContainerRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import org.glassfish.admin.rest.CliFailureException;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.resources.TemplateExecCommand;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ValidationException;

/**
 *
 * @author jasonlee
 */
@Produces({"text/html;qs=2", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
public class SystemPropertiesCliResource extends TemplateExecCommand {
    protected static final String TAG_SYSTEM_PROPERTY = "system-property";

    @Context
    protected ResourceContext resourceContext;

    @Context
    protected Habitat habitat;
    protected Dom entity;
//    protected Dom parent;

    public SystemPropertiesCliResource() {
        super(SystemPropertiesCliResource.class.getSimpleName(), "", "", "", "", true);
    }

    public void setEntity(Dom p) {
        entity = p;
    }

    public Dom getEntity() {
        return entity;
    }

    @GET
    public ActionReportResult get() {
        Dom server = getEntity();
        ParameterMap data = new ParameterMap();
        processCommandParams(data);
        addQueryString(((ContainerRequest) requestHeaders).getQueryParameters(), data);
        adjustParameters(data);
        List<Map<String, String>> properties = new ArrayList<Map<String, String>>();

        RestActionReporter actionReport = new RestActionReporter();
        for (Dom child : server.nodeElements("system-property")) {
            Map<String, String> property = new HashMap<String, String>();
            property.put("name", child.getKey());
            property.put("value", child.attribute("value"));
            properties.add(property);
        }
        actionReport.getExtraProperties().put("systemProperties", properties);
        if (properties.isEmpty()) {
            actionReport.getTopMessagePart().setMessage("Nothing to list."); // i18n
        }
        ActionReportResult results = new ActionReportResult(commandName, actionReport, new OptionsResult());

        return results;
    }

    @POST
    public ActionReportResult create(HashMap<String, String> data) {
        try {
            deleteExistingProperties();
            return saveProperties(data);
        } catch (Exception ex) {
            if (ex.getCause() instanceof ValidationException) {
                return ResourceUtil.getActionReportResult(400, ex.getMessage(), requestHeaders, uriInfo);
            } else {
                throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @PUT
    public ActionReportResult update(HashMap<String, String> data) {
        return create(data);
    }

    @Path("{Name}/")
    @POST
    public ActionReportResult getSystemPropertyResource(@PathParam("Name") String id, HashMap<String, String> data) {
        data.put(id, data.get("value"));
        data.remove("value");
        List<PathSegment> segments = uriInfo.getPathSegments(true);
        String grandParent = segments.get(segments.size()-3).getPath();

        return saveProperties(grandParent, data);
//        Dom parent = getEntity();
//        for (Dom child : parent.nodeElements(TAG_SYSTEM_PROPERTY)) {
//            if (child.getKey().equals(id)) {
//                SystemPropertyResource resource = resourceContext.getResource(SystemPropertyResource.class);
//                resource.setEntity(child);
//
//                return resource;
//            }
//        }
//
//        throw new WebApplicationException(Status.NOT_FOUND);
    }

    protected String convertPropertyMapToString(HashMap<String, String> data) {
        StringBuilder options = new StringBuilder();
        String sep = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            final String value = entry.getValue();
            if ((value != null) && !value.isEmpty()) {
                options.append(sep).append(entry.getKey()).append("=").append(value.replaceAll(":", "\\\\:"));
                sep = ":";
            }
        }

        return options.toString();
    }

    protected ActionReportResult saveProperties(HashMap<String, String> data) {
        return saveProperties(null, data);
    }
    
    protected ActionReportResult saveProperties(String parent, HashMap<String, String> data) {
        String propertiesString = convertPropertyMapToString(data);

        data = new HashMap<String, String>();
        data.put("DEFAULT", propertiesString);
        data.put("target", (parent == null) ? getParent(uriInfo) : parent);

        RestActionReporter actionReport = ResourceUtil.runCommand("create-system-properties", data, habitat, "");
        ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
        ActionReportResult results = new ActionReportResult(commandName, actionReport, new OptionsResult());

        if (exitCode != ActionReport.ExitCode.FAILURE) {
            results.setStatusCode(200); /*200 - ok*/
        } else {
            Throwable ex = actionReport.getFailureCause();
            throw (ex == null)
                    ? new CliFailureException(actionReport.getMessage())
                    : new CliFailureException(actionReport.getMessage(), ex);
        }

        return results;
    }

    protected void deleteExistingProperties() throws TransactionFailure {
        Dom parent = getEntity();
        for (Dom existingProp : parent.nodeElements(TAG_SYSTEM_PROPERTY)) {
            ConfigSupport.deleteChild((ConfigBean) parent, (ConfigBean) existingProp);
        }
    }

}