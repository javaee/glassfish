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
 * $Id: MenuTreeRenderer.java,v 1.3 2004/11/14 07:33:15 tcfujii Exp $
 */

package components.renderkit;


import components.components.GraphComponent;
import components.model.Graph;
import components.model.Node;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a tree control, with individual nodes expanded or contracted based on
 * their current status.</p>
 */

public class MenuTreeRenderer extends MenuBarRenderer {

    /**
     * The names of tree state images that we need.
     */
    public static final String IMAGE_HANDLE_DOWN_LAST = "handledownlast.gif";
    public static final String IMAGE_HANDLE_DOWN_MIDDLE = "handledownmiddle.gif";
    public static final String IMAGE_HANDLE_RIGHT_LAST = "handlerightlast.gif";
    public static final String IMAGE_HANDLE_RIGHT_MIDDLE = "handlerightmiddle.gif";
    public static final String IMAGE_LINE_LAST = "linelastnode.gif";
    public static final String IMAGE_LINE_MIDDLE = "linemiddlenode.gif";
    public static final String IMAGE_LINE_VERTICAL = "linevertical.gif";

    String imageLocation = null;


    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {
        Graph graph = null;
        // Acquire the root node of the graph representing the menu
        graph = (Graph) ((GraphComponent) component).getValue();
        if (graph == null) {
            throw new FacesException("Graph could not be located");
        }
        Node root = graph.getRoot();
        if (root == null) {
            throw new FacesException("Graph has no root node");
        }
        if (root.getChildCount() < 1) {
            return; // Nothing to render
        }
        this.component = component;
        this.context = context;
        clientId = component.getClientId(context);
        imageLocation = getImagesLocation(context);

        treeClass = (String) component.getAttributes().get("graphClass");
        selectedClass = (String) component.getAttributes().get("selectedClass");
        unselectedClass =
            (String) component.getAttributes().get("unselectedClass");

        ResponseWriter writer = context.getResponseWriter();

        writer.write("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"");
        if (treeClass != null) {
            writer.write(" class=\"");
            writer.write(treeClass);
            writer.write("\"");
        }
        writer.write(">");
        writer.write("\n");

        int level = 0;
        encodeNode(writer, root, level, root.getDepth(), true);
        writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
        writer.write("</table>");
        writer.write("\n");
    }


    protected void encodeNode(ResponseWriter writer, Node node,
                              int level, int width, boolean last)
        throws IOException {

        // Render the beginning of this node
        writer.write("  <tr valign=\"middle\">");
      
        // Create the appropriate number of indents
        for (int i = 0; i < level; i++) {
            int levels = level - i;
            Node parent = node;
            for (int j = 1; j <= levels; j++)
                parent = parent.getParent();
            if (parent.isLast())
                writer.write("    <td></td>");
            else {
                writer.write("    <td><img src=\"");
                writer.write(imageLocation);
                writer.write("/");
                writer.write(IMAGE_LINE_VERTICAL);
                writer.write("\" border=\"0\"></td>");
            }
            writer.write("\n");
        }

        // Render the tree state image for this node. use the "onmousedown" event 
        // handler to track which node was clicked. The images are rendered
        // as links.
        writer.write("    <td>");
        if (!node.isLeaf()) {
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            writer.write("<a href=\"");
            writer.write(getSubmitScript(node.getPath(), context));
            writer.write(" >");
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLast()) {
                writer.write(IMAGE_HANDLE_RIGHT_LAST);
            } else {
                writer.write(IMAGE_HANDLE_RIGHT_MIDDLE);
            }
            writer.write("\" border=\"0\">");
            writer.write("</a>");
            writer.write("</td>");
        } else {
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            if (node.isLeaf()) {
                if (node.isLast()) {
                    writer.write(IMAGE_LINE_LAST);
                } else {
                    writer.write(IMAGE_LINE_MIDDLE);
                }
            } else if (node.isExpanded()) {
                if (node.isLast()) {
                    writer.write(IMAGE_HANDLE_DOWN_LAST);
                } else {
                    writer.write(IMAGE_HANDLE_DOWN_MIDDLE);
                }
            }
            writer.write("\" border=\"0\">");
            writer.write("</td>");
        }

        // Render the icon for this node (if any)
        writer.write("    <td colspan=\"");
        writer.write(String.valueOf(width - level + 1));
        writer.write("\">");
        if (node.getIcon() != null) {
            // Label and action link
            // Note: we assume that the links do not act as command button,
            // meaning they do not cause the form to be submitted.
            if (node.getAction() != null) {
                writer.write("<a href=\"");
                writer.write(href(node.getAction()));
                writer.write("\">");
            }
            writer.write("<img src=\"");
            writer.write(imageLocation);
            writer.write("/");
            writer.write(node.getIcon());
            writer.write("\" border=\"0\">");
            if (node.getAction() != null) {
                writer.write("</a>");
            }
        }
        // Render the label for this node (if any) as link.
        if (node.getLabel() != null) {
            writer.write("   ");
            String labelStyle = null;
            if (node.isSelected() && (selectedClass != null)) {
                labelStyle = selectedClass;
            } else if (!node.isSelected() && (unselectedClass != null)) {
                labelStyle = unselectedClass;
            }
            if (node.isEnabled()) {
                // Note: we assume that the links do not act as command button,
                // meaning they do not cause the form to be submitted.
                writer.write("<a href=\"");
                writer.write(href(node.getAction()));
                writer.write("\"");
                if (labelStyle != null) {
                    writer.write(" class=\"");
                    writer.write(labelStyle);
                    writer.write("\"");
                }
                writer.write(">");
            } else if (labelStyle != null) {
                writer.write("<span class=\"");
                writer.write(labelStyle);
                writer.write("\">");
            }
            writer.write(node.getLabel());
            if (node.getLabel() != null) {
                writer.write("</a>");
            } else if (labelStyle != null) {
                writer.write("</span>");
            }
        }
        writer.write("</td>");
        writer.write("\n");

        // Render the end of this node
        writer.write("  </tr>");
        writer.write("\n");

        // Render the children of this node
        if (node.isExpanded()) {
            Iterator children = node.getChildren();
            int lastIndex = (node.getChildCount()) - 1;
            int newLevel = level + 1;
            while (children.hasNext()) {
                Node nextChild = (Node) children.next();
                boolean lastNode = nextChild.isLast();
                encodeNode(writer, nextChild, newLevel, width, lastNode);
            }
        }
    }


    /**
     * Returns the location of images by looking up the servlet context
     * init parameter. If parameter is not found, default to "/images".
     * Image location can be configured by setting this property.
     */
    protected String getImagesLocation(FacesContext context) {
        StringBuffer sb = new StringBuffer();

        // First, add the context path
        String contextPath = context.getExternalContext()
            .getRequestContextPath();
        sb.append(contextPath);

        // Next, add the images directory path
        Map initParameterMap = context.getExternalContext()
            .getInitParameterMap();
        String images = (String) initParameterMap.get("tree.control.images");
        if (images == null) {
            images = "/images";
        }
        sb.append(images);
        return (sb.toString());
    }


}
