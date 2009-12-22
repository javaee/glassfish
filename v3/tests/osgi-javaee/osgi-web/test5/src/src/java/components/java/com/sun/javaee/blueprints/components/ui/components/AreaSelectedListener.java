/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: AreaSelectedListener.java,v 1.2 2005/09/22 00:11:26 inder Exp $ */

package com.sun.javaee.blueprints.components.ui.components;


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
