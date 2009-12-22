/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: TabbedRenderer.java,v 1.4 2005/11/04 04:40:40 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.renderkit;

import com.sun.javaee.blueprints.components.ui.components.*;

import javax.faces.component.UIComponent;
import javax.faces.context.*;

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


    public void decode(FacesContext context, UIComponent component) {
    }


    public void encodeBegin(FacesContext context, UIComponent component)
         throws IOException {
         
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
    }

    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        

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
