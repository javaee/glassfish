/*
 * $Id: PaneSelectedEvent.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;


/**
 * A custom event which indicates the currently selected pane
 * in a tabbed pane control.
 */
public class PaneSelectedEvent extends FacesEvent {


    public PaneSelectedEvent(UIComponent component, String id) {
        super(component);
        this.id = id;
    }


    // The component id of the newly selected child pane
    private String id = null;


    public String getId() {
        return (this.id);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer("PaneSelectedEvent[id=");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
    }


    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof PaneComponent.PaneSelectedListener);
    }


    public void processListener(FacesListener listener) {
        ((PaneComponent.PaneSelectedListener) listener).processPaneSelectedEvent(
            this);
    }

}
