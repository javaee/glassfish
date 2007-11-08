/*
 * $Id: PaneComponent.java,v 1.1 2005/11/03 02:59:50 SherryShen Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

import java.util.Iterator;

/**
 * <p>Component designed to contain child components (and possibly other
 * layout in a JSP environment) for things like a tabbed pane control.
 */
public class PaneComponent extends UIComponentBase {


    private static Log log = LogFactory.getLog(PaneComponent.class);


    // creates and adds a listener;
    public PaneComponent() {
        PaneSelectedListener listener = new PaneSelectedListener();
        addFacesListener(listener);
    }


    /**
     * <p>Return the component family for this component.
     */
    public String getFamily() {

        return ("Pane");

    }


    // Does this component render its own children?
    public boolean getRendersChildren() {
        return (true);
    }


    public void processDecodes(FacesContext context) {
        // Process all facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            kid.processDecodes(context);
        }

        // Process this component itself
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }
    }


    // Ignore update model requests
    public void updateModel(FacesContext context) {
    }


    /**
     * <p>Faces Listener implementation which sets the selected tab
     * component
     */
    public class PaneSelectedListener implements FacesListener, StateHolder {

        public PaneSelectedListener() {
        }

        // process the event..

        public void processPaneSelectedEvent(FacesEvent event) {
            UIComponent source = event.getComponent();
            PaneSelectedEvent pevent = (PaneSelectedEvent) event;
            String id = pevent.getId();

            boolean paneSelected = false;

            // Find the parent tab control so we can set all tabs
            // to "unselected";
            UIComponent tabControl = findParentForRendererType(source,
                                                               "Tabbed");
            int n = tabControl.getChildCount();
            for (int i = 0; i < n; i++) {
                PaneComponent pane = (PaneComponent) tabControl.getChildren()
                    .get(i);
                if (pane.getId().equals(id)) {
                    pane.setRendered(true);
                    paneSelected = true;
                } else {
                    pane.setRendered(false);
                }
            }

            if (!paneSelected) {
                log.warn("Cannot select pane for id=" + id + "," +
                         ", selecting first pane");
                ((PaneComponent) tabControl.getChildren().get(0)).setRendered(
                    true);
            }
        }


        // methods from StateHolder
        public Object saveState(FacesContext context) {
            return null;
        }


        public void restoreState(FacesContext context, Object state) {
        }


        public void setTransient(boolean newTransientValue) {
        }


        public boolean isTransient() {
            return true;
        }
    }


    private UIComponent findParentForRendererType(UIComponent component, String rendererType) {
        Object facetParent = null;
        UIComponent currentComponent = component;
        
        // Search for an ancestor that is the specified renderer type;
        // search includes the facets.
        while (null != (currentComponent = currentComponent.getParent())) {
            if (currentComponent.getRendererType().equals(rendererType)) {
                break;
            }
        }
        return currentComponent;
    }


}
