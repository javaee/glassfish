

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


package listeners;


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 * Example listener for context-related application events, which were
 * introduced in the 2.3 version of the Servlet API.  This listener
 * merely documents the occurrence of such events in the application log
 * associated with our servlet context.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:13:37 $
 */

public final class SessionListener
    implements ServletContextListener,
	       HttpSessionAttributeListener, HttpSessionListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The servlet context with which we are associated.
     */
    private ServletContext context = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Record the fact that a servlet context attribute was added.
     *
     * @param event The session attribute event
     */
    public void attributeAdded(HttpSessionBindingEvent event) {

	log("attributeAdded('" + event.getSession().getId() + "', '" +
	    event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was removed.
     *
     * @param event The session attribute event
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {

	log("attributeRemoved('" + event.getSession().getId() + "', '" +
	    event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that a servlet context attribute was replaced.
     *
     * @param event The session attribute event
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {

	log("attributeReplaced('" + event.getSession().getId() + "', '" +
	    event.getName() + "', '" + event.getValue() + "')");

    }


    /**
     * Record the fact that this web application has been destroyed.
     *
     * @param event The servlet context event
     */
    public void contextDestroyed(ServletContextEvent event) {

	log("contextDestroyed()");
	this.context = null;

    }


    /**
     * Record the fact that this web application has been initialized.
     *
     * @param event The servlet context event
     */
    public void contextInitialized(ServletContextEvent event) {

	this.context = event.getServletContext();
	log("contextInitialized()");

    }


    /**
     * Record the fact that a session has been created.
     *
     * @param event The session event
     */
    public void sessionCreated(HttpSessionEvent event) {

	log("sessionCreated('" + event.getSession().getId() + "')");

    }


    /**
     * Record the fact that a session has been destroyed.
     *
     * @param event The session event
     */
    public void sessionDestroyed(HttpSessionEvent event) {

	log("sessionDestroyed('" + event.getSession().getId() + "')");

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message to the servlet context application log.
     *
     * @param message Message to be logged
     */
    private void log(String message) {

	if (context != null)
	    context.log("SessionListener: " + message);
	else
	    System.out.println("SessionListener: " + message);

    }


    /**
     * Log a message and associated exception to the servlet context
     * application log.
     *
     * @param message Message to be logged
     * @param throwable Exception to be logged
     */
    private void log(String message, Throwable throwable) {

	if (context != null)
	    context.log("SessionListener: " + message, throwable);
	else {
	    System.out.println("SessionListener: " + message);
	    throwable.printStackTrace(System.out);
	}

    }


}
