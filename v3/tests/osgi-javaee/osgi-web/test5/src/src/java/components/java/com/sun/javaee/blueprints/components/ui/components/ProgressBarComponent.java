/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
