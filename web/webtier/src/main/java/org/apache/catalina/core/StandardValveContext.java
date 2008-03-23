

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.core;


import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.ValveContext;
import org.apache.catalina.util.StringManager;


/**
 * Standard implementation of a <code>ValveContext</code>.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 *
 * <IMPLEMENTATION-NOTE>
???* This class is no longer used in PE 8.0. See bug 4665318
 * @author Jean-Francois Arcand
 * </IMPLEMENTATION-NOTE>
 */

public final class StandardValveContext
    implements ValveContext {


    // ----------------------------------------------------- Instance Variables


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    protected String info = 
        "org.apache.catalina.core.StandardValveContext/1.0";
    protected int stage = 0;
    protected Valve basic = null;
    protected Valve valves[] = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this ValveContext 
     * implementation.
     */
    public String getInfo() {
        return info;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Cause the <code>invoke()</code> method of the next Valve that is 
     * part of the Pipeline currently being processed (if any) to be 
     * executed, passing on the specified request and response objects 
     * plus this <code>ValveContext</code> instance.  Exceptions thrown by
     * a subsequently executed Valve (or a Filter or Servlet at the 
     * application level) will be passed on to our caller.
     *
     * If there are no more Valves to be executed, an appropriate
     * ServletException will be thrown by this ValveContext.
     *
     * @param request The request currently being processed
     * @param response The response currently being created
     *
     * @exception IOException if thrown by a subsequent Valve, Filter, or
     *  Servlet
     * @exception ServletException if thrown by a subsequent Valve, Filter,
     *  or Servlet
     * @exception ServletException if there are no further Valves 
     *  configured in the Pipeline currently being processed
     */
    public final void invokeNext(Request request, Response response)
        throws IOException, ServletException {

        /** STARTS OF PE 4665318
        int subscript = stage;
        stage = stage + 1;

        // Invoke the requested Valve for the current request thread
        if (subscript < valves.length) {
            valves[subscript].invoke(request, response, this);
        } else if ((subscript == valves.length) && (basic != null)) {
            basic.invoke(request, response, this);
        } else {
            throw new ServletException
                (sm.getString("standardPipeline.noValve"));
        }
        */
        // END OF PE 4665318
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Reset state.
     */
    void set(Valve basic, Valve valves[]) {
        stage = 0;
        this.basic = basic;
        this.valves = valves;
    }


}

