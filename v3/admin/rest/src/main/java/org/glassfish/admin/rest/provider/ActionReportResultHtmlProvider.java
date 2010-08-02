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
import org.glassfish.admin.rest.utils.xml.XmlArray;
import org.glassfish.admin.rest.utils.xml.XmlMap;
import org.glassfish.api.ActionReport;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.*;

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
        ActionReporter ar = (ActionReporter) proxy.getActionReport();
        StringBuilder result = new StringBuilder(ProviderUtil.getHtmlHeader());
//        String uri = uriInfo.getAbsolutePath().toString();
//        String name = upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));
//        String parentName =
//                upperCaseFirstLetter(eleminateHypen(getParentName(uri)));

        result.append("<h1>")
                .append(ar.getActionDescription())
                .append("</h1><div>");

        if (proxy.isError()) {
            result.append("<h2>Error:</h2>")
                    .append(proxy.getErrorMessage())
                    .append("<br>");
        } else {
            result
//                    .append("<h2>")
//                    .append(parentName)
//                    .append(" - ")
//                    .append(name)
//                    .append("</h2>")
                    .append(processReport(ar));
        }
        return result.append("</div><br></body></html>").toString();
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
//        result.append("<table border=\"1\" style=\"border-collapse: collapse\">");
//        for (Map.Entry entry : props.entrySet()) {
//            result.append("<tr><td>")
//                    .append(entry.getKey())
//                    .append("</td><td>")
//                    .append(entry.getValue())
//                    .append("</td></tr>");
//        }
        result.append(processMap(props));

        return result.append("</table>").toString();
    }

    protected String getExtraProperties(Properties props) {
        StringBuilder result = new StringBuilder("<h3>Extra Properties</h3>");
//        result.append("<table border=\"1\" style=\"border-collapse: collapse\">");
//        for (Map.Entry<Object, Object> entry : props.entrySet()) {
//            String key = entry.getKey().toString();
//            String value = getHtmlRepresentation(entry.getValue());
//            result.append("<tr><td>")
//                    .append(key)
//                    .append("</td><td>")
//                    .append(value)
//                    .append("</td></tr>");
//        }
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
        if (object instanceof Collection) {
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

        return result.append("</li>").toString();
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