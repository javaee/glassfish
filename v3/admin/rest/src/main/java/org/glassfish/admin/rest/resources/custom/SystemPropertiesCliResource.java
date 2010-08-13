/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.glassfish.admin.rest.CliFailureException;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.resources.TemplateExecCommand;
import org.glassfish.admin.rest.resources.generated.SystemPropertyResource;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.config.Dom;

/**
 *
 * @author jasonlee
 */
@Produces({"text/html;qs=2", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
public class SystemPropertiesCliResource extends TemplateExecCommand {

    @Context
    protected HttpHeaders requestHeaders;
    @Context
    protected UriInfo uriInfo;
    @Context
    protected ResourceContext resourceContext;
    protected Dom entity;
    protected Dom parent;

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
//        processCommandParams(data);
//        addQueryString(((ContainerRequest) requestHeaders).getQueryParameters(), data);
//        adjustParameters(data);

        String propertiesString = convertPropertyMapToString(data);
        data = new HashMap<String, String>();
        data.put("DEFAULT", propertiesString);

        RestActionReporter actionReport = ResourceUtil.runCommand("create-system-properties", data, RestService.getHabitat(), "");
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

    @Path("{Name}/")
    public SystemPropertyResource getSystemPropertyResource(@PathParam("Name") String id) {
        Dom server = getEntity();
        for (Dom child : server.nodeElements("system-property")) {
            if (child.getKey().equals(id)) {
                SystemPropertyResource resource = resourceContext.getResource(SystemPropertyResource.class);
                resource.setEntity(child);

                return resource;
            }
        }

        throw new WebApplicationException(Status.NOT_FOUND);
    }

    protected String convertPropertyMapToString(HashMap<String, String> data) {
        StringBuilder options = new StringBuilder();
        String sep = "";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            options.append(sep).append(entry.getKey()).append("=").append(entry.getValue());
            sep = ":";
        }

        return options.toString();
    }
}
