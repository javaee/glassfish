/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
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

    private MethodBinding action = null;
    private MethodBinding actionListener = null;
    private boolean immediate = false;
    private boolean immediateSet = false;



    // --------------------------------------------------------------Constructors 

    public MapComponent() {
        super();
        addDefaultActionListener(getFacesContext());
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
   
    // ----------------------------------------------------- Event Methods

    private static Class signature[] = {AreaSelectedEvent.class};


    /**
     * <p>In addition to to the default <code>UIComponentBase#broadcast</code>
     * processing, pass the {@link ActionEvent} being broadcast to the
     * method referenced by <code>actionListener</code> (if any).</p>
     *
     * @param event   {@link FacesEvent} to be broadcast
     *
     * @throws AbortProcessingException Signal the JavaServer Faces
     *                                  implementation that no further processing on the current event
     *                                  should be performed
     * @throws IllegalArgumentException if the implementation class
     *                                  of this {@link FacesEvent} is not supported by this component
     * @throws IllegalStateException    if PhaseId.ANY_PHASE is passed
     *                                  for the phase identifier
     * @throws NullPointerException     if <code>event</code> is
     *                                  <code>null</code>
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {

        // Perform standard superclass processing
        super.broadcast(event);

        // Notify the specified action listener method (if any)
        MethodBinding mb = getActionListener();
        if (mb != null) {
            if ((isImmediate() &&
                event.getPhaseId().equals(PhaseId.APPLY_REQUEST_VALUES)) ||
                (!isImmediate() &&
                event.getPhaseId().equals(PhaseId.INVOKE_APPLICATION))) {
                FacesContext context = getFacesContext();
                mb.invoke(context, new Object[]{event});
            }
        }

    }


    /**
     * <p>Intercept <code>queueEvent</code> and mark the phaseId for the
     * event to be <code>PhaseId.APPLY_REQUEST_VALUES</code> if the
     * <code>immediate</code> flag is true,
     * <code>PhaseId.INVOKE_APPLICATION</code> otherwise.</p>
     */

    public void queueEvent(FacesEvent e) {
        if (e instanceof ActionEvent) {
            if (isImmediate()) {
                e.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
            } else {
                e.setPhaseId(PhaseId.INVOKE_APPLICATION);
            }
        }
        super.queueEvent(e);
    }

    // ----------------------------------------------------- StateHolder Methods


    /**
     * <p>Return the state to be saved for this component.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {
        removeDefaultActionListener(context);
        Object values[] = new Object[6];
        values[0] = super.saveState(context);
        values[1] = current;
        values[2] = saveAttachedState(context, action);
        values[3] = saveAttachedState(context, actionListener);
        values[4] = immediate ? Boolean.TRUE : Boolean.FALSE;
        values[5] = immediateSet ? Boolean.TRUE : Boolean.FALSE;
        addDefaultActionListener(context);
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
        removeDefaultActionListener(context);
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        current = (String) values[1];
        action = (MethodBinding) restoreAttachedState(context, values[2]);
        actionListener = (MethodBinding) restoreAttachedState(context,
                                                              values[3]);
        immediate = ((Boolean) values[4]).booleanValue();
        immediateSet = ((Boolean) values[5]).booleanValue();
        addDefaultActionListener(context);
    }
    
    // ----------------------------------------------------- Private Methods

    // Add the default action listener
    private void addDefaultActionListener(FacesContext context) {
        ActionListener listener =
            context.getApplication().getActionListener();
        addActionListener(listener);
    }


    // Remove the default action listener
    private void removeDefaultActionListener(FacesContext context) {
        removeActionListener(context.getApplication().getActionListener());
    }

}
