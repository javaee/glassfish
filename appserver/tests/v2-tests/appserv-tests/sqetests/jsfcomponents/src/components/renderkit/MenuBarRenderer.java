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
 * $Id: MenuBarRenderer.java,v 1.1 2005/11/03 02:59:58 SherryShen Exp $
 */

package components.renderkit;


import components.components.GraphComponent;
import components.model.Graph;
import components.model.Node;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Render our current value (which must be a <code>Graph</code>)
 * as a menu bar, where the children of the root node are treated as individual
 * menus, and grandchildren of the root node are the items on the main menus.
 * A real application would display things as links to expand and contract
 * items, including recursive submenus.</p>
 */

public class MenuBarRenderer extends BaseRenderer {

    public static final String URL_PREFIX = "/faces";
    public final static String FORM_NUMBER_ATTR =
        "com.sun.faces.FormNumber";

    private static Log log = LogFactory.getLog(MenuBarRenderer.class);

    protected String treeClass = null;
    protected String selectedClass = null;
    protected String unselectedClass = null;
    protected String clientId = null;
    protected UIComponent component = null;
    protected FacesContext context = null;


    public void decode(FacesContext context, UIComponent component) {

        Graph graph = null;
  
        // if a node was clicked queue an ActionEvent.
        Map requestParameterMap = (Map) context.getExternalContext().
            getRequestParameterMap();
        String path = (String) requestParameterMap.
            get(component.getClientId(context));
        if (path != null && path.length() != 0) {
            component.getAttributes().put("path", path);
            component.queueEvent(new ActionEvent(component));
            if (log.isTraceEnabled()) {
                log.trace("ActionEvent queued on Graph component for " + path);
            }

        }
    }


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

        treeClass = (String) component.getAttributes().get("menuClass");
        selectedClass = (String) component.getAttributes().get("selectedClass");
        unselectedClass =
            (String) component.getAttributes().get("unselectedClass");
        
        // Render the menu bar for this graph
        Iterator menus = null;
        ResponseWriter writer = context.getResponseWriter();
        writer.write("<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\"");
        if (treeClass != null) {
            writer.write(" class=\"");
            writer.write(treeClass);
            writer.write("\"");
        }
        writer.write(">");
        writer.write("\n");
        writer.write("<tr>"); // For top level menu bar
        menus = root.getChildren();
        while (menus.hasNext()) {
            Node menu = (Node) menus.next();
            writer.write("<th bgcolor=\"silver\" align=\"left\">");
            // The image links of the nodes that have children behave like
            // command buttons causing the form to be submitted so the state of 
            // node can be toggled
            if (menu.isEnabled()) {
                writer.write("<a href=\"");
                writer.write(getSubmitScript(menu.getPath(), context));
                writer.write(" >");
                writer.write(menu.getLabel());
                writer.write("</a>");
            } else {
                writer.write(menu.getLabel());
            }
            writer.write("</th>");
        }
        writer.write("</tr>");

        writer.write("<tr>"); // For any expanded menu(s)
        menus = root.getChildren();
        while (menus.hasNext()) {
            Node menu = (Node) menus.next();
            writer.write(
                "<td bgcolor=\"silver\" align=\"left\" valign=\"top\">");

            if (menu.isExpanded()) {
                writer.write("<ul>");
                Iterator items = menu.getChildren();
                while (items.hasNext()) {
                    Node node = (Node) items.next();
                    writer.write("<li>");
                    // Render the label for this node (if any) as a
                    // link is the node is enabled. 
                    if (node.getLabel() != null) {
                        writer.write("   ");
                        String labelStyle = null;
                        if (node.isSelected() && (selectedClass != null)) {
                            labelStyle = selectedClass;
                        } else if (!node.isSelected() &&
                            (unselectedClass != null)) {
                            labelStyle = unselectedClass;
                        }
                        if (node.isEnabled()) {
                            writer.write("<a href=\"");
                            // Note: we assume that the links do not act as
                            // command button, meaning they do not cause the
                            // form to be submitted.
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
                    writer.write("</li>");
                    // PENDING - marker for submenu
                    // PENDING - expanded submenu?
                }
                writer.write("</ul>");
            } else {
                writer.write("&nbsp;");
            }
            writer.write("</td>");
        }
        writer.write("<input type=\"hidden\" name=\"" + clientId + "\" />");
        writer.write("</table>");

    }


    /**
     * <p> Returns a string that is rendered as the value of
     * <code>onmousedown</code> attribute. <code>onmousedown</code> event 
     * handler is used the track the node that was clicked using a hidden field, 
     * then submits the form so that we have the state information to 
     * reconstitute the tree.
     */
    protected String getSubmitScript(String path, FacesContext context) {
        UIForm uiform = getMyForm();
        String formClientId = uiform.getClientId(context);
        StringBuffer sb = new StringBuffer();
        sb.append("#\" onclick=\"document.forms['" + formClientId + "']['" +
                  clientId + "'].value='" + path +
                  "';document.forms['" + formClientId + "'].submit()\"");
        return sb.toString();
    }


    /**
     * Returns the parent form of graph component.
     */
    protected UIForm getMyForm() {
        UIComponent parent = component.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }
        return (UIForm) parent;
    }


    /**
     * Returns a string that is rendered as the value of
     * href attribute after prepending the contextPath if necessary.
     */
    protected String href(String action) {
        // if action does not start with a "/", it is considered an absolute
        // URL and hence don't prepend contextPath.
        if (action != null && !(action.startsWith("/"))) {
            return action;
        }

        StringBuffer sb = new StringBuffer();
        if (action.startsWith("/")) {
            sb.append(context.getExternalContext().getRequestContextPath());
        }
        sb.append(action);
        return (sb.toString());
    }


}
