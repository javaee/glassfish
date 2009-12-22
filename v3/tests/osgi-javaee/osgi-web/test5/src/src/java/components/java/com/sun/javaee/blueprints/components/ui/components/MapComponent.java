/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: MapComponent.java,v 1.3 2005/11/01 21:59:08 jenniferb Exp $ */

package com.sun.javaee.blueprints.components.ui.components;


import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import java.io.IOException;

/**
 * <p>{@link MapComponent} is a JavaServer Faces component that corresponds
 * to a client-side image map.  It can have one or more children of type
 * {@link AreaComponent}, each representing hot spots, which a user can
 * click on and mouse over.</p>
 *
 * <p>This component is a source of {@link AreaSelectedEvent} events,
 * which are fired whenever the current area is changed.</p>
 */

public class MapComponent extends UICommand {


    // ------------------------------------------------------ Instance Variables


    private String current = null;



    // --------------------------------------------------------------Constructors 

    public MapComponent() {
        super();
    }

    
    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the alternate text label for the currently selected
     * child {@link AreaComponent}.</p>
     */
    public String getCurrent() {
        return (this.current);
    }


    /**
     * <p>Set the alternate text label for the currently selected child.
     * If this is different from the previous value, fire an
     * {@link AreaSelectedEvent} to interested listeners.</p>
     *
     * @param current The new alternate text label
     */
    public void setCurrent(String current) {

        String previous = this.current;
        this.current = current;

        // Fire an {@link AreaSelectedEvent} if appropriate
        if ((previous == null) && (current == null)) {
            return;
        } else if ((previous != null) && (current != null) &&
            (previous.equals(current))) {
            return;
        } else {
            this.queueEvent(new AreaSelectedEvent(this));
        }

    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Map");

    }
  

    // ----------------------------------------------------- StateHolder Methods


    /**
     * <p>Return the state to be saved for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[6];
        values[0] = super.saveState(context);
        values[1] = current;
        return (values);
    }


    /**
     * <p>Restore the state for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param state   State to be restored
     *
     * @throws IOException if an input/output error occurs
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        current = (String) values[1];
    }

}
