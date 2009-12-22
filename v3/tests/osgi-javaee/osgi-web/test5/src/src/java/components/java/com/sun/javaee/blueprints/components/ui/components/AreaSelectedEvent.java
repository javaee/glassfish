/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: AreaSelectedEvent.java,v 1.2 2005/09/22 00:11:26 inder Exp $ */

package com.sun.javaee.blueprints.components.ui.components;


import javax.faces.event.ActionEvent;


/**
 * <p>An {@link ActionEvent} indicating that the specified {@link AreaComponent}
 * has just become the currently selected hotspot within the source
 * {@link MapComponent}.</p>
 */

public class AreaSelectedEvent extends ActionEvent {

    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new {@link AreaSelectedEvent} from the specified
     * source map.</p>
     *
     * @param map The {@link MapComponent} originating this event
     */
    public AreaSelectedEvent(MapComponent map) {
        super(map);
    }


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the {@link MapComponent} of the map for which an area
     * was selected.</p>
     */
    public MapComponent getMapComponent() {
        return ((MapComponent) getComponent());
    }
}
