/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.components;

import javax.el.MethodExpression;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

import java.io.IOException;
import java.util.Map;

/**
 * This component produces a search engine style scroller that facilitates
 * easy navigation over results that span across several pages. It
 * demonstrates how a component can do decoding and encoding
 * without delegating it to a renderer.
 */
public class ScrollerComponent extends UICommand {

    private static final String NORTH = "NORTH";
    private static final String SOUTH = "SOUTH";
    private static final String EAST = "EAST";
    private static final String WEST = "WEST";   

    private static final byte ACTION_NEXT = -1;
    private static final byte ACTION_PREVIOUS = -2;    

    private static final String FORM_NUMBER_ATTR = "com.sun.faces.FormNumber";
                     

    /**
     * The component attribute that tells where to put the user supplied
     * markup in relation to the "jump to the Nth page of results"
     * widget.
     */
    private static final String FACET_MARKUP_ORIENTATION_ATTR =
        "navFacetOrientation";


    public ScrollerComponent() {
        super();
        this.setRendererType(null);
    }

    @Override
    public void decode(FacesContext context) {

        String clientId = getClientId(context);
        Map<String,String> requestParameterMap =
              context.getExternalContext(). getRequestParameterMap();
        String action = requestParameterMap.get(clientId + "_action");
        if (action == null || action.length() == 0) {
            // nothing to decode
            return;
        }
        MethodExpression me = context.getApplication().getExpressionFactory()
              .createMethodExpression(context.getELContext(),
                                      action,
                                      null,
                                      new Class[]{});        
        this.setActionExpression(me);
        
        String curPage = requestParameterMap.get(clientId + "_curPage");
        int currentPage = Integer.valueOf(curPage);
        int actionInt = Integer.valueOf(action);

        // Assert that action's length is 1.
        switch (actionInt) {
            case ACTION_NEXT:
                currentPage++;
                break;
            case ACTION_PREVIOUS:
                currentPage--;
                // Assert 1 < currentPage
                break;
            default:
                currentPage = actionInt;
                break;
        }
        // from the currentPage, calculate the current row to scroll to.
        int currentRow = (currentPage - 1) * getRowsPerPage(context);
        this.getAttributes().put("currentPage", currentPage);
        this.getAttributes().put("currentRow", currentRow);
        this.queueEvent(new ActionEvent(this));
    }


    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        int currentPage = 1;

        ResponseWriter writer = context.getResponseWriter();

        String clientId = getClientId(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage;
        }
        int totalPages = getTotalPages(context);

        writer.write("<table border=\"0\" cellpadding=\"0\" align=\"center\">");
        writer.write("<tr align=\"center\" valign=\"top\">");
        writer.write(
            "<td><font size=\"-1\">Result&nbsp;Page:&nbsp;</font></td>");

        // write the Previous link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_PREVIOUS,
                             (1 < currentPage));
        // last arg is true iff we're not the first page
        writer.write("</td>");

        // render the page navigation links       
        int first = 1;
        int last = totalPages;

        if (10 < currentPage) {
            first = currentPage - 10;
        }
        if ((currentPage + 9) < totalPages) {
            last = currentPage + 9;
        }
        for (int i = first; i <= last; i++) {
            writer.write("<td>");
            writeNavWidgetMarkup(context, clientId, i, (i != currentPage));
            writer.write("</td>");
        }

        // write the Next link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_NEXT,
                             (currentPage < totalPages));
        writer.write("</td>");
        writer.write("</tr>");
        writer.write(getHiddenFields(clientId));
        writer.write("</table>");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    @Override
    public String getFamily() {

        return ("Scroller");

    }

    //
    // Helper methods
    // 

    /**
     * Write the markup to render a navigation widget.  Override this to
     * replace the default navigation widget of link with something
     * else.
     */
    protected void writeNavWidgetMarkup(FacesContext context,
                                        String clientId,
                                        int navActionType,
                                        boolean enabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String facetOrientation = NORTH;
        String facetName;
        String linkText;
        UIComponent facet;
        boolean isPageNumber = false;

        // Assign values for local variables based on the navActionType
        switch (navActionType) {
            case ACTION_NEXT:
                facetName = "next";
                linkText = "Next";
                break;
            case ACTION_PREVIOUS:
                facetName = "previous";
                linkText = "Previous";
                break;
            default:
                facetName = "number";
                linkText = "" + navActionType;
                isPageNumber = true;
                // heuristic: if navActionType is number, and we are not
                // enabled, this must be the current page.
                if (!enabled) {
                    facetName = "current";
                }
                break;
        }

        // leverage any navigation facets we have
        writer.write("\n&nbsp;");
        if (enabled) {
            writer.write("<a " + getAnchorAttrs(context, clientId,
                                                navActionType) + ">");
        }

        facet = getFacet(facetName);
        // render the facet pertaining to this widget type in the NORTH
        // and WEST cases.
        if (facet != null) {
            // If we're rendering a "go to the Nth page" link
            if (isPageNumber) {
                // See if the user specified an orientation
                facetOrientation = (String) getAttributes().get(
                      FACET_MARKUP_ORIENTATION_ATTR);
                // verify that the orientation is valid
                if (!(facetOrientation.equalsIgnoreCase(NORTH) ||
                      facetOrientation.equalsIgnoreCase(SOUTH) ||
                      facetOrientation.equalsIgnoreCase(EAST) ||
                      facetOrientation.equalsIgnoreCase(WEST))) {
                    facetOrientation = NORTH;
                }
            }

            // output the facet as specified in facetOrientation
            if (facetOrientation.equalsIgnoreCase(NORTH) ||
                facetOrientation.equalsIgnoreCase(EAST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
            // The difference between NORTH and EAST is that NORTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(NORTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
        }

        // if we have a facet, only output the link text if
        // navActionType is number
        if (null != facet) {
            if (navActionType != ACTION_NEXT &&
                navActionType != ACTION_PREVIOUS) {
                writer.write(linkText);
            }
        } else {
            writer.write(linkText);
        }

        // output the facet in the EAST and SOUTH cases
        if (null != facet) {
            if (facetOrientation.equalsIgnoreCase(SOUTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
            // The difference between SOUTH and WEST is that SOUTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(SOUTH) ||
                facetOrientation.equalsIgnoreCase(WEST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
        }

        if (enabled) {
            writer.write("</a>");
        }

    }


    /**
     * <p>Build and return the string consisting of the attibutes for a
     * result set navigation link anchor.</p>
     *
     * @param context  the FacesContext
     * @param clientId the clientId of the enclosing UIComponent
     * @param action   the value for the rhs of the =
     *
     * @return a String suitable for setting as the value of a navigation
     *         href.
     */
    private String getAnchorAttrs(FacesContext context, String clientId,
                                  int action) {
        int currentPage = 1;
        int formNumber = getFormNumber(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage;
        }
        return
            ("href=\"#\" " +
            "onmousedown=\"" +
            "document.forms[" + formNumber + "]['" + clientId +
            "_action'].value='" +
            action +
            "'; " +
            "document.forms[" + formNumber + "]['" + clientId +
            "_curPage'].value='" +
            currentPage +
            "'; " +
            "document.forms[" + formNumber + "].submit()\"");
        
    }


    private String getHiddenFields(String clientId) {
        
        return  
            ("<input type=\"hidden\" name=\"" + clientId + "_action\"/>\n" +
            "<input type=\"hidden\" name=\"" + clientId + "_curPage\"/>");
        
    }


    // PENDING: avoid doing this each time called.  Perhaps
    // store in our own attr?
    protected UIForm getForm(FacesContext context) {
        UIComponent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }
        return (UIForm) parent;
    }


    protected int getFormNumber(FacesContext context) {
        Map<String,Object> requestMap = 
              context.getExternalContext().getRequestMap();               
        Integer formsInt = (Integer) requestMap.get(FORM_NUMBER_ATTR);
        // find out the current number of forms in the page.
        if (formsInt != null) {
            formsInt--;
        } else {
            formsInt = 0;
        }
        return formsInt;
    }


    /**
     * Returns the total number of pages in the result set based on
     * <code>rows</code> and <code>rowCount</code> of <code>UIData</code>
     * component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getTotalPages(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        int rowsPerPage = uiData.getRows();                
        int totalRows = uiData.getRowCount();
        int result = totalRows / rowsPerPage;
        if (0 != (totalRows % rowsPerPage)) {
            result++;
        }
        return result;
    }


    /**
     * Returns the number of rows to display by looking up the
     * <code>UIData</code> component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getRowsPerPage(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        return uiData.getRows();
    }
} 
