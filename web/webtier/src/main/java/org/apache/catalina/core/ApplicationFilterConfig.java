

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


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.io.Serializable;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Context;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.security.SecurityUtil;
import org.apache.tomcat.util.log.SystemLogHandler;


/**
 * Implementation of a <code>javax.servlet.FilterConfig</code> useful in
 * managing the filter instances instantiated when a web application
 * is first started.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2007/03/22 18:04:04 $
 */

final class ApplicationFilterConfig implements FilterConfig, Serializable {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( ApplicationFilterConfig.class );
 
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ApplicationFilterConfig for the specified filter
     * definition.
     *
     * @param context The context with which we are associated
     * @param filterDef Filter definition for which a FilterConfig is to be
     *  constructed
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     */
    public ApplicationFilterConfig(StandardContext context, FilterDef filterDef)
        throws ClassCastException, ClassNotFoundException,
               IllegalAccessException, InstantiationException,
               ServletException {

        super();
        this.context = context;
        setFilterDef(filterDef);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The Context with which we are associated.
     */
    private StandardContext context = null;


    /**
     * The application Filter we are configured for.
     */
    private transient Filter filter = null;


    /**
     * The <code>FilterDef</code> that defines our associated Filter.
     */
    private FilterDef filterDef = null;


    // --------------------------------------------------- FilterConfig Methods


    /**
     * Return the name of the filter we are configuring.
     */
    public String getFilterName() {

        return (filterDef.getFilterName());

    }


    /**
     * Return a <code>String</code> containing the value of the named
     * initialization parameter, or <code>null</code> if the parameter
     * does not exist.
     *
     * @param name Name of the requested initialization parameter
     */
    public String getInitParameter(String name) {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (null);
        else
            return ((String) map.get(name));

    }


    /**
     * Return an <code>Enumeration</code> of the names of the initialization
     * parameters for this Filter.
     */
    public Enumeration getInitParameterNames() {

        Map map = filterDef.getParameterMap();
        if (map == null)
            return (new Enumerator(new ArrayList()));
        else
            return (new Enumerator(map.keySet()));

    }


    /**
     * Return the ServletContext of our associated web application.
     */
    public ServletContext getServletContext() {

        return (this.context.getServletContext());

    }


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ApplicationFilterConfig[");
        sb.append("name=");
        sb.append(filterDef.getFilterName());
        sb.append(", filterClass=");
        sb.append(filterDef.getFilterClass());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return the application Filter we are configured for.
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     */
    Filter getFilter() throws ClassCastException, ClassNotFoundException,
        IllegalAccessException, InstantiationException, ServletException {

        // Return the existing filter instance, if any
        if (this.filter != null)
            return (this.filter);

        // Identify the class loader we will be using
        String filterClass = filterDef.getFilterClass();
        ClassLoader classLoader = null;
        if (filterClass.startsWith("org.apache.catalina."))
            classLoader = this.getClass().getClassLoader();
        else
            classLoader = context.getLoader().getClassLoader();

        ClassLoader oldCtxClassLoader =
            Thread.currentThread().getContextClassLoader();

        // Instantiate a new instance of this filter and return it
        Class clazz = classLoader.loadClass(filterClass);
        this.filter = (Filter) clazz.newInstance();

        // START PWC 1.2
        if (context != null) {
            context.fireContainerEvent(
                ContainerEvent.BEFORE_FILTER_INITIALIZED,
                filter);
        }
        // END PWC 1.2

        if (context.getSwallowOutput()) {
            try {
                SystemLogHandler.startCapture();
                filter.init(this);
            } finally {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    getServletContext().log(log);
                }
            }
        } else {
            filter.init(this);
        }

        // START PWC 1.2
        if (context != null) {
            context.fireContainerEvent(ContainerEvent.AFTER_FILTER_INITIALIZED,
                                       filter);
        }
        // END PWC 1.2

        return (this.filter);

    }


    /**
     * Return the filter definition we are configured for.
     */
    FilterDef getFilterDef() {

        return (this.filterDef);

    }


    /**
     * Release the Filter instance associated with this FilterConfig,
     * if there is one.
     */
    void release() {

        if (this.filter != null){

            if (context != null) {
                context.fireContainerEvent(
                    ContainerEvent.BEFORE_FILTER_DESTROYED,
                    filter);
            }

            // START SJS WS 7.0 6236329
            //if( System.getSecurityManager() != null) {
            if ( SecurityUtil.executeUnderSubjectDoAs() ){
            // END OF SJS WS 7.0 6236329
                try{
                    SecurityUtil.doAsPrivilege("destroy",
                                               filter); 
                    SecurityUtil.remove(filter);
                } catch(java.lang.Exception ex){                    
                    log.error("ApplicationFilterConfig.doAsPrivilege", ex);
                }
            } else { 
                filter.destroy();
            }

            if (context != null) {
                context.fireContainerEvent(
                    ContainerEvent.AFTER_FILTER_DESTROYED,
                    filter);
            }

        }

        this.filter = null;

     }


    /**
     * Set the filter definition we are configured for.  This has the side
     * effect of instantiating an instance of the corresponding filter class.
     *
     * @param filterDef The new filter definition
     *
     * @exception ClassCastException if the specified class does not implement
     *  the <code>javax.servlet.Filter</code> interface
     * @exception ClassNotFoundException if the filter class cannot be found
     * @exception IllegalAccessException if the filter class cannot be
     *  publicly instantiated
     * @exception InstantiationException if an exception occurs while
     *  instantiating the filter object
     * @exception ServletException if thrown by the filter's init() method
     */
    void setFilterDef(FilterDef filterDef)
        throws ClassCastException, ClassNotFoundException,
               IllegalAccessException, InstantiationException,
               ServletException {

        this.filterDef = filterDef;
        if (filterDef == null) {

            // Release any previously allocated filter instance
            if (this.filter != null){
                // START SJS WS 7.0 6236329
                //if( System.getSecurityManager() != null) {
                if ( SecurityUtil.executeUnderSubjectDoAs() ){
                // END OF SJS WS 7.0 6236329
                    try{
                        SecurityUtil.doAsPrivilege("destroy",
                                                   filter);  
                        SecurityUtil.remove(filter);
                    } catch(java.lang.Exception ex){    
                        log.error("ApplicationFilterConfig.doAsPrivilege", ex);
                    }
                } else { 
                    filter.destroy();
                }
            }
            this.filter = null;

        } else {

            // Allocate a new filter instance
            Filter filter = getFilter();

        }

    }


    // -------------------------------------------------------- Private Methods


}
