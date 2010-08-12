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
package org.glassfish.admin.rest.provider;

import com.sun.enterprise.v3.common.ActionReporter;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.GetResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.ActionReport;
import org.jvnet.hk2.config.ConfigBean;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.*;

import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlForComponent;
import static org.glassfish.admin.rest.provider.ProviderUtil.getHtmlRespresentationsForCommand;

/**
 * @author Ludovic Champenois
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class ActionReportResultHtmlProvider extends BaseProvider<ActionReportResult> {
    public ActionReportResultHtmlProvider() {
        super(ActionReportResult.class, MediaType.TEXT_HTML_TYPE);
    }

    @Override
    public String getContent(ActionReportResult proxy) {
        RestActionReporter ar = (RestActionReporter) proxy.getActionReport();
        StringBuilder result = new StringBuilder(ProviderUtil.getHtmlHeader());

        result.append("<h1>")
                .append(ar.getActionDescription())
                .append("</h1><div>");

        if (proxy.isError()) {
            result.append("<h2>Error:</h2>")
                    .append(proxy.getErrorMessage());
        } else {
            final ConfigBean entity = proxy.getEntity();
            final Map<String, String> childResources = (Map<String, String>) ar.getExtraProperties().get("childResources");
            final List<Map<String, String>> commands = (List<Map<String, String>>) ar.getExtraProperties().get("commands");
            final MethodMetaData postMetaData = proxy.getMetaData().getMethodMetaData("POST");

            if ((postMetaData != null) && (entity == null)) {
                String postCommand = getHtmlRespresentationsForCommand(postMetaData, "POST", "Create", uriInfo);
                result.append(getHtmlForComponent(postCommand, "Create " + ar.getActionDescription(), ""));
            }

            if (entity != null) {
                String attributes = ProviderUtil.getHtmlRepresentationForAttributes(proxy.getEntity(), uriInfo);
                result.append(ProviderUtil.getHtmlForComponent(attributes, "Attributes", ""));

                String deleteCommand = ProviderUtil.getHtmlRespresentationsForCommand(proxy.getMetaData().getMethodMetaData("DELETE"), "DELETE", "Delete", uriInfo);
                result.append(ProviderUtil.getHtmlForComponent(deleteCommand, "Delete " + entity.model.getTagName(), ""));
            }

            if (childResources != null) {
                String childResourceLinks = getResourcesLinks(childResources);
                result.append(ProviderUtil.getHtmlForComponent(childResourceLinks, "Child Resources", ""));
            }

            if (commands != null) {
                String commandLinks = getCommandLinks(commands);
                result.append(ProviderUtil.getHtmlForComponent(commandLinks, "Commands", ""));
            }

            result.append("<h2>Raw Output</h2>");
            result.append(processReport(ar));
        }
        return result.append("</div></body></html>").toString();
    }

    protected String getResourcesLinks(Map<String, String> childResources) {
        StringBuilder links = new StringBuilder("<div>");
        for (Map.Entry<String, String> link : childResources.entrySet()) {
            links.append("<a href=\"")
                .append(link.getValue())
                .append("\">")
                .append(link.getKey())
                .append("</a><br>");

        }

        return links.append("</div><br/>").toString();
    }

    protected String getCommandLinks(List<Map<String, String>> commands) {
        StringBuilder result = new StringBuilder("<div>");

        for (Map<String, String> commandList : commands) {
            String command = commandList.get("command");
            String path = commandList.get("path");
            if (path.startsWith("_")) {//hidden cli command name
                result.append("<!--");//hide the link in a comment
            }
            result.append("<a href=\"")
                    .append(ProviderUtil.getElementLink(uriInfo, command))
                    .append("\">")
                    .append(command)
                    .append("</a><br>");
            if (path.startsWith("_")) {//hidden cli
                result.append("-->");
            }
        }

        return result.append("</div><br>").toString();
    }

    protected String processReport(ActionReporter ar) {
        StringBuilder result = new StringBuilder();
        result.append("<h1>GlassFish ")
                .append(ar.getActionDescription())
                .append(" command report</h1><h2>")
                .append(ar.getMessage() != null ? ar.getMessage() : "")
                .append("</h2><h2>Exit Code: ")
                .append(ar.getActionExitCode().toString())
                .append("</h2><hr>");

        Properties properties = ar.getTopMessagePart().getProps();
        if (!properties.isEmpty()) {
            result.append(processProperties(properties));
        }

        Properties extraProperties = ar.getExtraProperties();
        if ((extraProperties != null) && (!extraProperties.isEmpty())) {
            result.append(getExtraProperties(extraProperties));
        }

        List<ActionReport.MessagePart> children = ar.getTopMessagePart().getChildren();
        if (children.size() > 0) {
            result.append(processChildren(children));
        }

        List<ActionReporter> subReports = ar.getSubActionsReport();
        if (subReports.size() > 0) {
            result.append(processSubReports(subReports));
        }

        return result.toString();
    }

    protected String processProperties(Properties props) {
        StringBuilder result = new StringBuilder("<h3>Properties</h3>");
        result.append(processMap(props));

        return result.append("</table>").toString();
    }

    protected String getExtraProperties(Properties props) {
        StringBuilder result = new StringBuilder("<h3>Extra Properties</h3>");
        result.append(processMap(props));

        return result.toString();
    }

    protected String processChildren(List<ActionReport.MessagePart> parts) {
        StringBuilder result = new StringBuilder("<h3>Children</h3><ul>");

        for (ActionReport.MessagePart part : parts) {
            result.append("<li><table border=\"1\" style=\"border-collapse: collapse\">")
                    .append("<tr><td>Message</td>")
                    .append("<td>")
                    .append(part.getMessage())
                    .append("</td></tr><td>Properites</td><td>")
                    .append(processMap(part.getProps()))
                    .append("</td></tr>");
            List<ActionReport.MessagePart> children = part.getChildren();
            if (children.size() > 0) {
                result.append("<tr><td>Children</td><td>")
                        .append(processChildren(part.getChildren()))
                        .append("</td></tr>");
            }
            result.append("</table></li>");
        }

        return result.append("</ul>").toString();
    }

    protected String processSubReports(List<ActionReporter> subReports) {
        StringBuilder result = new StringBuilder("<h3>Sub Reports</h3><ul>");

        for (ActionReporter subReport : subReports) {
            result.append(processReport(subReport));
        }

        return result.append("</ul>").toString();
    }

    protected String getHtmlRepresentation(Object object) {
        String result = null;
        if (object == null) {
            return "";
        } else if (object instanceof Collection) {
            result = processCollection((Collection) object);
        } else if (object instanceof Map) {
            result = processMap((Map) object);
        } else {
            result = object.toString();
        }

        return result;
    }

    protected String processCollection(Collection c) {
        StringBuilder result = new StringBuilder("<ul>");
        Iterator i = c.iterator();
        while (i.hasNext()) {
            result.append("<li>")
                    .append(getHtmlRepresentation(i.next()))
                    .append("</li>");
        }

        return result.append("</li></ul>").toString();
    }

    protected String processMap(Map map) {
        StringBuilder result = new StringBuilder("<table border=\"1\" style=\"border-collapse: collapse\">");
        result.append("<tr><th>key</th><th>value</th></tr>");

        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            result.append("<tr><td>")
                    .append(entry.getKey())
                    .append("</td><td>")
                    .append(getHtmlRepresentation(entry.getValue()))
                    .append("</td></tr>");
        }

        return result.append("</table>").toString();
    }
}