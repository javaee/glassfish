/* Copyright 2005 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html  
$Id: PaneSelectedEvent.java,v 1.3 2005/11/01 21:59:08 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.components;


import javax.faces.component.UIComponent;
import javax.faces.event.*;


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
