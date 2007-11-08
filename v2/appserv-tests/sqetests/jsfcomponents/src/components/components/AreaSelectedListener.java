/*
 * $Id: AreaSelectedListener.java,v 1.4 2004/11/14 07:33:12 tcfujii Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import javax.faces.event.FacesListener;


/**
 * <p>{@link AreaSelectedListener} defines an event listener interested in
 * {@link AreaSelectedEvent}s from a {@link MapComponent}.</p>
 */

public interface AreaSelectedListener extends FacesListener {


    /**
     * <p>Process the specified event.</p>
     *
     * @param event The event to be processed
     */
    public void processAreaSelected(AreaSelectedEvent event);


}
