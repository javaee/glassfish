/*
 * $Id: ProgressBarComponent.java,v 1.1 2005/09/27 18:53:18 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * [Name of File] [ver.__] [Date]
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.javaee.blueprints.components.ui.components;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;


/**
 */

public class ProgressBarComponent extends UICommand {

    /**
     * <p>The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "ProgressBar";


    /**
     * <p>The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "ProgressBar";

    /**
     * <p>The standard renderer type for this component.</p>
     */
    public static final String RENDERER_TYPE = "ProgressBar";
    
    /**
     * <p>Name of the servlet that renders the image.</p>
     */
    public static final String PROGRESSBAR_SERVLET_NAME = "ProgressBarServlet";
    
    // ------------------------------------------------------ Instance Variables
    private int interval = 1000;

    // --------------------------------------------------------------Constructors 

    public ProgressBarComponent() {
        super();
        setRendererType(RENDERER_TYPE);
    }

    
    // -------------------------------------------------------------- Properties

    public int getInterval() {
	return interval;
    }

    public void setInterval(int newInterval) {
	interval = newInterval;
    }

    /**
     * <p>Return the component family for this component.
     */
    public String getFamily() {

        return (COMPONENT_FAMILY);

    }

    // ---------------------------------------------------- Action method

    public String startPolling() {
	
	return null;
    }
   
    // ----------------------------------------------------- StateHolder Methods
    /**
     * <p>Return the state to be saved for this component.
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[2];
        values[0] = super.saveState(context);
        values[1] = new Integer(interval);
        return (values);
    }

    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
	interval = ((Integer)values[1]).intValue();
    }

    
}
