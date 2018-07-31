/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/*
 * $Id: TabbedRenderer.java,v 1.1 2005/11/03 03:00:02 SherryShen Exp $
 */

package components.renderkit;


import components.components.PaneComponent;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;

/**
 * <p>Render our associated {@link PaneComponent} as a tabbed control, with
 * each of its immediate child {@link PaneComponent}s representing a single
 * tab.  Measures are taken to ensure that exactly one of the child tabs is
 * selected, and only the selected child pane's contents will be rendered.
 * </p>
 */

public class TabbedRenderer extends BaseRenderer {


    private static Log log = LogFactory.getLog(TabbedRenderer.class);


    public void decode(FacesContext context, UIComponent component) {
    }


    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeBegin(" + component.getId() + ")");
        }

        // Render the outer border and tabs of our owning table
        String paneClass = (String) component.getAttributes().get("paneClass");
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<table");
        if (paneClass != null) {
            writer.write(" class=\"");
            writer.write(paneClass);
            writer.write("\"");
        }
        writer.write(">\n");

    }


    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeChildren(" + component.getId() + ")");
        }

    }


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("encodeEnd(" + component.getId() + ")");
        }

        // Ensure that exactly one of our child PaneComponents is selected
        Iterator kids = component.getChildren().iterator();
        PaneComponent firstPane = null;
        PaneComponent selectedPane = null;
        int n = 0;
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (!(kid instanceof PaneComponent)) {
                continue;
            }
            PaneComponent pane = (PaneComponent) kid;
            n++;
            if (firstPane == null) {
                firstPane = pane;
            }
            if (pane.isRendered()) {
                if (selectedPane == null) {
                    selectedPane = pane;
                } else {
                    pane.setRendered(false);
                }
            }
        }
        if ((selectedPane == null) && (firstPane != null)) {
            firstPane.setRendered(true);
            selectedPane = firstPane;
        }

        // Render the labels for our tabs
        String selectedClass =
            (String) component.getAttributes().get("selectedClass");
        String unselectedClass =
            (String) component.getAttributes().get("unselectedClass");
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<tr>\n");
        int percent;
        if (n > 0) {
            percent = 100 / n;
        } else {
            percent = 100;
        }

        kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (!(kid instanceof PaneComponent)) {
                continue;
            }
            PaneComponent pane = (PaneComponent) kid;
            writer.write("<td width=\"");
            writer.write("" + percent);
            writer.write("%\"");
            if (pane.isRendered() && (selectedClass != null)) {
                writer.write(" class=\"");
                writer.write(selectedClass);
                writer.write("\"");
            } else if (!pane.isRendered() && (unselectedClass != null)) {
                writer.write(" class=\"");
                writer.write(unselectedClass);
                writer.write("\"");
            }
            writer.write(">");

            UIComponent facet = (UIComponent) pane.getFacet("label");
            if (facet != null) {
                if (pane.isRendered() && (selectedClass != null)) {
                    facet.getAttributes().put("paneTabLabelClass",
                                              selectedClass);
                } else if (!pane.isRendered() && (unselectedClass != null)) {
                    facet.getAttributes().put("paneTabLabelClass",
                                              unselectedClass);
                }
                facet.encodeBegin(context);
            }
            writer.write("</td>\n");
        }
        writer.write("</tr>\n");

        // Begin the containing element for the selected child pane
        String contentClass = (String) component.getAttributes().get(
            "contentClass");
        writer.write("<tr><td width=\"100%\" colspan=\"");
        writer.write("" + n);
        writer.write("\"");
        if (contentClass != null) {
            writer.write(" class=\"");
            writer.write(contentClass);
            writer.write("\"");
        }
        writer.write(">\n");

        // Render the selected child pane
        selectedPane.encodeBegin(context);
        if (selectedPane.getRendersChildren()) {
            selectedPane.encodeChildren(context); // We know Pane does this
        }
        selectedPane.encodeEnd(context);

        // End the containing element for the selected child pane
        writer.write("\n</td></tr>\n");

        // Render the ending of our owning element and table
        writer.write("</table>\n");
    }
}
