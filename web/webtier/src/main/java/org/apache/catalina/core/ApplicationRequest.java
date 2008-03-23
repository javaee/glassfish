

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.ServletRequest</code>
 * that transforms an application request object (which might be the original
 * one passed to a servlet, or might be based on the 2.3
 * <code>javax.servlet.ServletRequestWrapper</code> class)
 * back into an internal <code>org.apache.catalina.Request</code>.
 * <p>
 * <strong>WARNING</strong>:  Due to Java's lack of support for multiple
 * inheritance, all of the logic in <code>ApplicationRequest</code> is
 * duplicated in <code>ApplicationHttpRequest</code>.  Make sure that you
 * keep these two classes in synchronization when making changes!
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2006/11/02 17:40:12 $
 */

public class ApplicationRequest extends ServletRequestWrapper {


    // ------------------------------------------------------- Static Variables


    /**
     * The set of attribute names that are special for request dispatchers.
     */
    private static final HashSet specials = new HashSet(10);


    static {
        specials.add(Globals.INCLUDE_REQUEST_URI_ATTR);
        specials.add(Globals.INCLUDE_CONTEXT_PATH_ATTR);
        specials.add(Globals.INCLUDE_SERVLET_PATH_ATTR);
        specials.add(Globals.INCLUDE_PATH_INFO_ATTR);
        specials.add(Globals.INCLUDE_QUERY_STRING_ATTR);
        specials.add(Globals.FORWARD_REQUEST_URI_ATTR);
        specials.add(Globals.FORWARD_CONTEXT_PATH_ATTR);
        specials.add(Globals.FORWARD_SERVLET_PATH_ATTR);
        specials.add(Globals.FORWARD_PATH_INFO_ATTR);
        specials.add(Globals.FORWARD_QUERY_STRING_ATTR);
    }


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped request around the specified servlet request.
     *
     * @param request The servlet request being wrapped
     */
    public ApplicationRequest(ServletRequest request) {

        super(request);
        setRequest(request);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The request attributes for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Override the <code>getAttribute()</code> method of the wrapped request.
     *
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    public Enumeration getAttributeNames() {

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Override the <code>removeAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
            if (!isSpecial(name))
                getRequest().removeAttribute(name);
        }

    }


    /**
     * Override the <code>setAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {

        synchronized (attributes) {
            attributes.put(name, value);
            if (!isSpecial(name))
                getRequest().setAttribute(name, value);
        }

    }


    // ------------------------------------------ ServletRequestWrapper Methods


    /**
     * Set the request that we are wrapping.
     *
     * @param request The new wrapped request
     */
    public void setRequest(ServletRequest request) {

        super.setRequest(request);

        // Initialize the attributes for this request
        synchronized (attributes) {
            attributes.clear();
            Enumeration names = request.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                Object value = request.getAttribute(name);
                attributes.put(name, value);
            }
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Is this attribute name one of the special ones that is added only for
     * included servlets?
     *
     * @param name Attribute name to be tested
     */
    protected static boolean isSpecial(String name) {

        return specials.contains(name);
    }

}
