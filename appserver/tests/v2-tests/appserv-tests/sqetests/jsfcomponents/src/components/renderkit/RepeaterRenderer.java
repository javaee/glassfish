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
 * $Id: RepeaterRenderer.java,v 1.4 2004/11/14 07:33:15 tcfujii Exp $
 */

package components.renderkit;


import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import java.io.IOException;
import java.util.Iterator;


/**
 * <p><code>Renderer</code> that supports generating markup for the per-row data
 * associated with a <code>UIData</code> component.  You can easily specialize
 * the behavior of the <code>Renderer</code> by subclassing and overriding the
 * <code>tableBegin()</code>, <code>rowBegin()</code>,
 * <code>rowBody()</code>, <code>rowEnd()</code>, and <code>tableEnd()</code>
 * methods.  The default implementation renders an HTML table with
 * headers and footers.</p>
 */

public class RepeaterRenderer extends BaseRenderer {


    // -------------------------------------------------------- Renderer Methods


    /**
     * <p>Render the beginning of the table for our associated data.</p>
     *
     * @param context   <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> being rendered
     *
     * @throws IOException if an input/output error occurs
     */
    public void encodeBegin(FacesContext context, UIComponent component)
        throws IOException {

        super.encodeBegin(context, component);
        ResponseWriter writer = context.getResponseWriter();
        UIData data = (UIData) component;

        // Render the beginning of this table
        data.setRowIndex(-1);
        tableBegin(context, data, writer);

    }


    /**
     * <p>Render the body rows of the table for our associated data.</p>
     *
     * @param context   <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> being rendered
     *
     * @throws IOException if an input/output error occurs
     */
    public void encodeChildren(FacesContext context, UIComponent component)
        throws IOException {

        super.encodeChildren(context, component);
        ResponseWriter writer = context.getResponseWriter();
        UIData data = (UIData) component;

        int processed = 0;
        int rowIndex = data.getFirst() - 1;
        int rows = data.getRows();

        // Iterate over the specified rows of data
        while (true) {

            // Have we displayed the requested number of rows?
            if ((rows > 0) && (++processed > rows)) {
                break;
            }

            // Select the next row (if there is one)
            data.setRowIndex(++rowIndex);
            if (!data.isRowAvailable()) {
                break;
            }

            // Render the beginning, body, and ending of this row
            rowBegin(context, data, writer);
            rowBody(context, data, writer);
            rowEnd(context, data, writer);

        }

    }


    /**
     * <p>Render the ending of the table for our associated data.</p>
     *
     * @param context   <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> being rendered
     *
     * @throws IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
        throws IOException {

        super.encodeEnd(context, component);
        ResponseWriter writer = context.getResponseWriter();
        UIData data = (UIData) component;

        // Render the ending of this table
        data.setRowIndex(-1);
        tableEnd(context, data, writer);

    }


    /**
     * <p>Return <code>true</code> to indicate that we do indeed wish to be
     * responsible for rendering the children of the associated component.</p>
     */
    public boolean getRendersChildren() {

        return (true);

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the number of child components of type <code>UIColumn</code>
     * are registered with the specified <code>UIData</code> component.</p>
     *
     * @param data <code>UIData</code> component for which to count
     */
    protected int getColumnCount(UIData data) {

        int n = 0;
        Iterator kids = data.getChildren().iterator();
        while (kids.hasNext()) {
            if (kids.next() instanceof UIColumn) {
                n++;
            }
        }
        return (n);

    }


    /**
     * <p>Return the number of child components of type <code>UIColumn</code>
     * are registered with the specified <code>UIData</code> component
     * and have a facet named <code>footer</code>.</p>
     *
     * @param data <code>UIData</code> component for which to count
     */
    protected int getColumnFooterCount(UIData data) {

        int n = 0;
        Iterator kids = data.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if ((kid instanceof UIColumn) &&
                (kid.getFacet("footer") != null)) {
                n++;
            }
        }
        return (n);

    }


    /**
     * <p>Return the number of child components of type <code>UIColumn</code>
     * are registered with the specified <code>UIData</code> component
     * and have a facet named <code>header</code>.</p>
     *
     * @param data <code>UIData</code> component for which to count
     */
    protected int getColumnHeaderCount(UIData data) {

        int n = 0;
        Iterator kids = data.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if ((kid instanceof UIColumn) &&
                (kid.getFacet("header") != null)) {
                n++;
            }
        }
        return (n);

    }


    /**
     * <p>Render the markup for the beginning of the current body row.  The
     * default implementation renders <code>&lt;tr&gt;</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param data    <code>UIData</code> being rendered
     * @param writer  <code>ResponseWriter</code> to render to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void rowBegin(FacesContext context, UIData data,
                            ResponseWriter writer) throws IOException {

        writer.startElement("tr", data);
        writer.writeText("\n", null);

    }


    /**
     * <p>Render the markup for the content of the current body row.  The
     * default implementation renders the descendant components of each
     * child <code>UIColumn</code>, surrounded by <code>&lt;td&gt;</code>
     * and <code>&lt;/td&gt;</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param data    <code>UIData</code> being rendered
     * @param writer  <code>ResponseWriter</code> to render to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void rowBody(FacesContext context, UIData data,
                           ResponseWriter writer) throws IOException {

        // Iterate over the UIColumn children of this UIData component
        Iterator columns = data.getChildren().iterator();
        while (columns.hasNext()) {

            // Only process UIColumn children
            UIComponent column = (UIComponent) columns.next();
            if (!(column instanceof UIColumn)) {
                continue;
            }

            // Create the markup for this column
            writer.startElement("td", column);
            Iterator contents = column.getChildren().iterator();
            while (contents.hasNext()) {
                encodeRecursive(context, (UIComponent) contents.next());
            }
            writer.endElement("td");
            writer.writeText("\n", null);

        }

    }


    /**
     * <p>Render the markup for the ending of the current body row.  The
     * default implementation renders <code>&lt;/tr&gt;</code>.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param data    <code>UIData</code> being rendered
     * @param writer  <code>ResponseWriter</code> to render to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void rowEnd(FacesContext context, UIData data,
                          ResponseWriter writer) throws IOException {

        writer.endElement("tr");
        writer.writeText("\n", null);

    }


    /**
     * <p>Render the markup for the beginnning of an entire table.  The
     * default implementation renders:</p>
     * <ul>
     * <li>A <code>&lt;table&gt;</code> element.</li>
     * <li>If the <code>UIData</code> component has a facet named
     * <code>header</code>, render it in a table row with a
     * <code>colspan</code> set to span all the columns in the table.</li>
     * <li>If any of the child <code>UIColumn</code> components has a facet
     * named <code>header</code>, render them in a table row with a
     * each header in a <code>&lt;th&gt;</code> element.</li>
     * </ul>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param data    <code>UIData</code> being rendered
     * @param writer  <code>ResponseWriter</code> to render to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void tableBegin(FacesContext context, UIData data,
                              ResponseWriter writer) throws IOException {

        // Render the outermost table element
        writer.startElement("table", data);
        String styleClass = (String) data.getAttributes().get("styleClass");
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        } else {
            writer.writeAttribute("border", "0", null);
            writer.writeAttribute("cellspacing", "5", null);
        }
        writer.writeText("\n", null);

        // Render the table and column headers (if any)
        UIComponent header = data.getFacet("header");
        int n = getColumnHeaderCount(data);
        if ((header != null) || (n > 0)) {
            writer.startElement("thead", header);
        }
        if (header != null) {
            writer.startElement("tr", header);
            writer.startElement("th", header);
            writer.writeAttribute("colspan", "" + getColumnCount(data), null);
            writer.writeText("\n", null);
            encodeRecursive(context, header);
            writer.writeText("\n", null);
            writer.endElement("th");
            writer.endElement("tr");
            writer.writeText("\n", null);
        }
        if (n > 0) {
            writer.startElement("tr", data);
            writer.writeText("\n", null);
            Iterator columns = data.getChildren().iterator();
            while (columns.hasNext()) {
                UIComponent column = (UIComponent) columns.next();
                if (!(column instanceof UIColumn)) {
                    continue;
                }
                writer.startElement("th", column);
                UIComponent facet = column.getFacet("header");
                if (facet != null) {
                    encodeRecursive(context, facet);
                }
                writer.endElement("th");
                writer.writeText("\n", null);
            }
            writer.endElement("tr");
            writer.writeText("\n", null);
        }
        if ((header != null) || (n > 0)) {
            writer.endElement("thead");
            writer.writeText("\n", null);
        }

        // Render the beginning of the table body
        writer.startElement("tbody", data);
        writer.writeText("\n", null);

    }


    /**
     * <p>Render the markup for the ending of an entire table.  The
     * default implementation renders:</p>
     * <ul>
     * <li>If any of the child <code>UIColumn</code> components has a facet
     * named <code>footer</code>, render them in a table row with a
     * each footer in a <code>&lt;th&gt;</code> element.</li>
     * <li>If the <code>UIData</code> component has a facet named
     * <code>footer</code>, render it in a table row with a
     * <code>colspan</code> set to span all the columns in the table.</li>
     * <li>A <code>&lt;/table&gt;</code> element.</li>
     * </ul>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param data    <code>UIData</code> being rendered
     * @param writer  <code>ResponseWriter</code> to render to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void tableEnd(FacesContext context, UIData data,
                            ResponseWriter writer) throws IOException {

        // Render the end of the table body
        writer.endElement("tbody");
        writer.writeText("\n", null);

        // Render the table and column footers (if any)
        UIComponent footer = data.getFacet("footer");
        int n = getColumnFooterCount(data);
        if ((footer != null) || (n > 0)) {
            writer.startElement("tfoot", footer);
        }
        if (n > 0) {
            writer.startElement("tr", data);
            writer.writeText("\n", null);
            Iterator columns = data.getChildren().iterator();
            while (columns.hasNext()) {
                UIComponent column = (UIComponent) columns.next();
                if (!(column instanceof UIColumn)) {
                    continue;
                }
                writer.startElement("th", column);
                UIComponent facet = column.getFacet("footer");
                if (facet != null) {
                    encodeRecursive(context, facet);
                }
                writer.endElement("th");
                writer.writeText("\n", null);
            }
            writer.endElement("tr");
            writer.writeText("\n", null);
        }
        if (footer != null) {
            writer.startElement("tr", footer);
            writer.startElement("th", footer);
            writer.writeAttribute("colspan", "" + getColumnCount(data), null);
            writer.writeText("\n", null);
            encodeRecursive(context, footer);
            writer.writeText("\n", null);
            writer.endElement("th");
            writer.endElement("tr");
            writer.writeText("\n", null);
        }
        if ((footer != null) || (n > 0)) {
            writer.endElement("tfoot");
            writer.writeText("\n", null);
        }

        // Render the ending of the outermost table element
        writer.endElement("table");
        writer.writeText("\n", null);

    }


}
