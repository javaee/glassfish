

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

import java.lang.reflect.Method;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
// START GlassFish 1343
import org.apache.catalina.Valve;
// END GlassFish 1343
import org.apache.catalina.Wrapper;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.InstanceSupport;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.log.SystemLogHandler;
import com.sun.org.apache.commons.modeler.Registry;

/**
 * Standard implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition.  No child Containers are allowed, and
 * the parent Container must be a Context.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.11 $ $Date: 2007/05/05 05:31:54 $
 */
public class StandardWrapper
    extends ContainerBase
    implements ServletConfig, Wrapper {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( StandardWrapper.class );

    private static final String[] DEFAULT_SERVLET_METHODS = new String[] {
                                                    "GET", "HEAD", "POST" };

    // ----------------------------------------------------------- Constructors


    /**
     * Create a new StandardWrapper component with the default basic Valve.
     */
    public StandardWrapper() {

        super();
        swValve=new StandardWrapperValve();
        pipeline.setBasic(swValve);
        broadcaster = new NotificationBroadcasterSupport();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The date and time at which this servlet will become available (in
     * milliseconds since the epoch), or zero if the servlet is available.
     * If this value equals Long.MAX_VALUE, the unavailability of this
     * servlet is considered permanent.
     */
    private long available = 0L;
    
    /**
     * The broadcaster that sends j2ee notifications. 
     */
    private NotificationBroadcasterSupport broadcaster = null;
    
    /**
     * The count of allocations that are currently active (even if they
     * are for the same instance, as will be true on a non-STM servlet).
     */
    private int countAllocated = 0;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The facade associated with this wrapper.
     */
    private StandardWrapperFacade facade =
        new StandardWrapperFacade(this);


    /**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardWrapper/1.0";


    /**
     * The (single) initialized instance of this servlet.
     */
    private Servlet instance = null;


    /**
     * The support object for our instance listeners.
     */
    private InstanceSupport instanceSupport = new InstanceSupport(this);


    /**
     * The context-relative URI of the JSP file for this servlet.
     */
    private String jspFile = null;


    /**
     * The load-on-startup order value (negative value means load on
     * first call) for this servlet.
     */
    private int loadOnStartup = -1;


    /**
     * Mappings associated with the wrapper.
     */
    private ArrayList mappings = new ArrayList();


    /**
     * The initialization parameters for this servlet, keyed by
     * parameter name.
     */
    private HashMap parameters = new HashMap();


    /**
     * The security role references for this servlet, keyed by role name
     * used in the servlet.  The corresponding value is the role name of
     * the web application itself.
     */
    private HashMap references = new HashMap();


    /**
     * The run-as identity for this servlet.
     */
    private String runAs = null;

    /**
     * The notification sequence number.
     */
    private long sequenceNumber = 0;

    /**
     * The fully qualified servlet class name for this servlet.
     */
    private String servletClass = null;


    /**
     * Does this servlet implement the SingleThreadModel interface?
     */
    private boolean singleThreadModel = false;


    /**
     * Are we unloading our servlet instance at the moment?
     */
    private boolean unloading = false;


    /**
     * Maximum number of STM instances.
     */
    private int maxInstances = 20;


    /**
     * Number of instances currently loaded for a STM servlet.
     */
    private int nInstances = 0;


    /**
     * Stack containing the STM instances.
     */
    private Stack instancePool = null;


    /**
     * True if this StandardWrapper is for the JspServlet
     */
    private boolean isJspServlet;


    /**
     * The ObjectName of the JSP monitoring mbean
     */
    private ObjectName jspMonitorON;


    /**
     * Should we swallow System.out
     */
    private boolean swallowOutput = false;

    // To support jmx attributes
    private StandardWrapperValve swValve;
    private long loadTime=0;
    private int classLoadTime=0;

    /**
     * Static class array used when the SecurityManager is turned on and 
     * <code>Servlet.init</code> is invoked.
     */
    private static Class[] classType = new Class[]{ServletConfig.class};
    
    
    /**
     * Static class array used when the SecurityManager is turned on and 
     * <code>Servlet.service</code>  is invoked.
     */                                                 
    private static Class[] classTypeUsedInService = new Class[]{
                                                         ServletRequest.class,
                                                         ServletResponse.class};

    // ------------------------------------------------------------- Properties


    /**
     * Return the available date/time for this servlet, in milliseconds since
     * the epoch.  If this date/time is Long.MAX_VALUE, it is considered to mean
     * that unavailability is permanent and any request for this servlet will return
     * an SC_NOT_FOUND error.  If this date/time is in the future, any request for
     * this servlet will return an SC_SERVICE_UNAVAILABLE error.  If it is zero,
     * the servlet is currently available.
     */
    public long getAvailable() {

        return (this.available);

    }


    /**
     * Set the available date/time for this servlet, in milliseconds since the
     * epoch.  If this date/time is Long.MAX_VALUE, it is considered to mean
     * that unavailability is permanent and any request for this servlet will return
     * an SC_NOT_FOUND error. If this date/time is in the future, any request for
     * this servlet will return an SC_SERVICE_UNAVAILABLE error.
     *
     * @param available The new available date/time
     */
    public void setAvailable(long available) {

        long oldAvailable = this.available;
        if (available > System.currentTimeMillis())
            this.available = available;
        else
            this.available = 0L;
        support.firePropertyChange("available", Long.valueOf(oldAvailable),
                                   Long.valueOf(this.available));

    }


    /**
     * Return the number of active allocations of this servlet, even if they
     * are all for the same instance (as will be true for servlets that do
     * not implement <code>SingleThreadModel</code>.
     */
    public int getCountAllocated() {

        return (this.countAllocated);

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        int oldDebug = this.debug;
        this.debug = debug;
        support.firePropertyChange("debug", Integer.valueOf(oldDebug),
                                   Long.valueOf(this.debug));

    }


    public String getEngineName() {
        return ((StandardContext)getParent()).getEngineName();
    }


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the InstanceSupport object for this Wrapper instance.
     */
    public InstanceSupport getInstanceSupport() {

        return (this.instanceSupport);

    }


    /**
     * Return the context-relative URI of the JSP file for this servlet.
     */
    public String getJspFile() {

        return (this.jspFile);

    }


    /**
     * Set the context-relative URI of the JSP file for this servlet.
     *
     * @param jspFile JSP file URI
     */
    public void setJspFile(String jspFile) {

        String oldJspFile = this.jspFile;
        this.jspFile = jspFile;
        support.firePropertyChange("jspFile", oldJspFile, this.jspFile);

        // Each jsp-file needs to be represented by its own JspServlet and
        // corresponding JspMonitoring mbean, because it may be initialized
        // with its own init params
        isJspServlet = true;
    }


    /**
     * Return the load-on-startup order value (negative value means
     * load on first call).
     */
    public int getLoadOnStartup() {

        if (isJspServlet && loadOnStartup < 0) {
            /*
             * JspServlet must always be preloaded, because its instance is
             * used during registerJMX (when registering the JSP
             * monitoring mbean)
             */
             return Integer.MAX_VALUE;
        } else {
            return (this.loadOnStartup);
        }
    }


    /**
     * Set the load-on-startup order value (negative value means
     * load on first call).
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartup(int value) {

        int oldLoadOnStartup = this.loadOnStartup;
        this.loadOnStartup = value;
        support.firePropertyChange("loadOnStartup",
                                   Integer.valueOf(oldLoadOnStartup),
                                   Integer.valueOf(this.loadOnStartup));

    }



    /**
     * Set the load-on-startup order value from a (possibly null) string.
     * Per the specification, any missing or non-numeric value is converted
     * to a zero, so that this servlet will still be loaded at startup
     * time, but in an arbitrary order.
     *
     * @param value New load-on-startup value
     */
    public void setLoadOnStartupString(String value) {

        try {
            setLoadOnStartup(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            setLoadOnStartup(0);
        }
    }

    public String getLoadOnStartupString() {
        return Integer.toString( getLoadOnStartup());
    }


    /**
     * Return maximum number of instances that will be allocated when a single
     * thread model servlet is used.
     */
    public int getMaxInstances() {

        return (this.maxInstances);

    }


    /**
     * Set the maximum number of instances that will be allocated when a single
     * thread model servlet is used.
     *
     * @param maxInstances New value of maxInstances
     */
    public void setMaxInstances(int maxInstances) {

        int oldMaxInstances = this.maxInstances;
        this.maxInstances = maxInstances;
        support.firePropertyChange("maxInstances", oldMaxInstances,
                                   this.maxInstances);

    }


    /**
     * Set the parent Container of this Wrapper, but only if it is a Context.
     *
     * @param container Proposed parent Container
     */
    public void setParent(Container container) {

        if ((container != null) &&
            !(container instanceof Context))
            throw new IllegalArgumentException
                (sm.getString("standardWrapper.notContext"));
        if (container instanceof StandardContext) {
            swallowOutput = ((StandardContext)container).getSwallowOutput();
            notifyContainerListeners =
                ((StandardContext)container).isNotifyContainerListeners();
        }
        super.setParent(container);

    }


    /**
     * Return the run-as identity for this servlet.
     */
    public String getRunAs() {

        return (this.runAs);

    }


    /**
     * Set the run-as identity for this servlet.
     *
     * @param runAs New run-as identity value
     */
    public void setRunAs(String runAs) {

        String oldRunAs = this.runAs;
        this.runAs = runAs;
        support.firePropertyChange("runAs", oldRunAs, this.runAs);

    }


    /**
     * Return the fully qualified servlet class name for this servlet.
     */
    public String getServletClass() {

        return (this.servletClass);

    }


    /**
     * Set the fully qualified servlet class name for this servlet.
     *
     * @param servletClass Servlet class name
     */
    public void setServletClass(String servletClass) {

        String oldServletClass = this.servletClass;
        this.servletClass = servletClass;
        support.firePropertyChange("servletClass", oldServletClass,
                                   this.servletClass);
        if (Constants.JSP_SERVLET_CLASS.equals(servletClass)) {
            isJspServlet = true;
        }
    }



    /**
     * Set the name of this servlet.  This is an alias for the normal
     * <code>Container.setName()</code> method, and complements the
     * <code>getServletName()</code> method required by the
     * <code>ServletConfig</code> interface.
     *
     * @param name The new name of this servlet
     */
    public void setServletName(String name) {

        setName(name);

    }


    /**
     * Return <code>true</code> if the servlet class represented by this
     * component implements the <code>SingleThreadModel</code> interface.
     */
    public boolean isSingleThreadModel() {

        try {
            loadServlet();
        } catch (Throwable t) {
            ;
        }
        return (singleThreadModel);

    }


    /**
     * Is this servlet currently unavailable?
     */
    public boolean isUnavailable() {

        if (available == 0L)
            return (false);
        else if (available <= System.currentTimeMillis()) {
            available = 0L;
            return (false);
        } else
            return (true);

    }


    /**
     * Gets the names of the methods supported by the underlying servlet.
     *
     * This is the same set of methods included in the Allow response header
     * in response to an OPTIONS request method processed by the underlying
     * servlet.
     *
     * @return Array of names of the methods supported by the underlying
     * servlet
     */
    public String[] getServletMethods() throws ServletException {
	
        Class servletClazz = loadServlet().getClass();
        if (!javax.servlet.http.HttpServlet.class.isAssignableFrom(
                                                        servletClazz)) {
            return DEFAULT_SERVLET_METHODS;
        }

        HashSet allow = new HashSet();
        allow.add("TRACE");
        allow.add("OPTIONS");
	
        Method[] methods = getAllDeclaredMethods(servletClazz);
        for (int i=0; methods != null && i<methods.length; i++) {
            Method m = methods[i];
	    
            if (m.getName().equals("doGet")) {
                allow.add("GET");
                allow.add("HEAD");
            } else if (m.getName().equals("doPost")) {
                allow.add("POST");
            } else if (m.getName().equals("doPut")) {
                allow.add("PUT");
            } else if (m.getName().equals("doDelete")) {
                allow.add("DELETE");
            }
        }

        String[] methodNames = new String[allow.size()];
        return (String[]) allow.toArray(methodNames);

    }


    // --------------------------------------------------------- Public Methods


    // START GlassFish 1343
    public synchronized void addValve(Valve valve) {
        /*
         * This exception should never be thrown in reality, because we never
         * add any valves to a StandardWrapper. 
         * This exception is added here as an alert mechanism only, should
         * there ever be a need to add valves to a StandardWrapper in the
         * future.
         * In that case, the optimization in StandardContextValve related to
         * GlassFish 1343 will need to be adjusted, by calling
         * pipeline.getValves() and checking the pipeline's length to
         * determine whether the basic valve may be invoked directly. The
         * optimization currently avoids a call to pipeline.getValves(),
         * because it is expensive.
         */
        throw new UnsupportedOperationException(
            "Adding valves to wrappers not supported");
    }
    // END GlassFish 1343


    /**
     * Extract the root cause from a servlet exception.
     * 
     * @param e The servlet exception
     */
    public static Throwable getRootCause(ServletException e) {
        Throwable rootCause = e;
        Throwable rootCauseCheck = null;
        // Extra aggressive rootCause finding
        do {
            try {
                rootCauseCheck = (Throwable)IntrospectionUtils.getProperty
                                            (rootCause, "rootCause");
                if (rootCauseCheck!=null)
                    rootCause = rootCauseCheck;

            } catch (ClassCastException ex) {
                rootCauseCheck = null;
            }
        } while (rootCauseCheck != null);
        return rootCause;
    }


    /**
     * Refuse to add a child Container, because Wrappers are the lowest level
     * of the Container hierarchy.
     *
     * @param child Child container to be added
     */
    public void addChild(Container child) {

        throw new IllegalStateException
            (sm.getString("standardWrapper.notChild"));

    }


    /**
     * Add a new servlet initialization parameter for this servlet.
     *
     * @param name Name of this initialization parameter to add
     * @param value Value of this initialization parameter to add
     */
    public void addInitParameter(String name, String value) {

        synchronized (parameters) {
            parameters.put(name, value);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addInitParameter", name);
        }
    }


    /**
     * Add a new listener interested in InstanceEvents.
     *
     * @param listener The new listener
     */
    public void addInstanceListener(InstanceListener listener) {

        instanceSupport.addInstanceListener(listener);

    }


    /**
     * Add a mapping associated with the Wrapper.
     *
     * @param pattern The new wrapper mapping
     */
    public void addMapping(String mapping) {

        synchronized (mappings) {
            mappings.add(mapping);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addMapping", mapping);
        }
    }


    /**
     * Add a new security role reference record to the set of records for
     * this servlet.
     *
     * @param name Role name used within this servlet
     * @param link Role name used within the web application
     */
    public void addSecurityReference(String name, String link) {

        synchronized (references) {
            references.put(name, link);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addSecurityReference", name);
        }
    }


    /**
     * Allocate an initialized instance of this Servlet that is ready to have
     * its <code>service()</code> method called.  If the servlet class does
     * not implement <code>SingleThreadModel</code>, the (only) initialized
     * instance may be returned immediately.  If the servlet class implements
     * <code>SingleThreadModel</code>, the Wrapper implementation must ensure
     * that this instance is not allocated again until it is deallocated by a
     * call to <code>deallocate()</code>.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if a loading error occurs
     */
    public Servlet allocate() throws ServletException {

        // If we are currently unloading this servlet, throw an exception
        if (unloading)
            throw new ServletException
              (sm.getString("standardWrapper.unloading", getName()));

        // If not SingleThreadedModel, return the same instance every time
        if (!singleThreadModel) {

            // Load and initialize our instance if necessary
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) {
                        try {
                            if (log.isTraceEnabled())
                                log.trace("Allocating non-STM instance");

                            instance = loadServlet();
                        } catch (ServletException e) {
                            throw e;
                        } catch (Throwable e) {
                            throw new ServletException
                                (sm.getString("standardWrapper.allocate"), e);
                        }
                    }
                }
            }

            if (!singleThreadModel) {
                if (log.isTraceEnabled())
                    log.trace("  Returning non-STM instance");
                countAllocated++;
                return (instance);
            }

        }

        synchronized (instancePool) {

            while (countAllocated >= nInstances) {
                // Allocate a new instance if possible, or else wait
                if (nInstances < maxInstances) {
                    try {
                        instancePool.push(loadServlet());
                        nInstances++;
                    } catch (ServletException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new ServletException
                            (sm.getString("standardWrapper.allocate"), e);
                    }
                } else {
                    try {
                        instancePool.wait();
                    } catch (InterruptedException e) {
                        ;
                    }
                }
            }
            if (log.isTraceEnabled())
                log.trace("  Returning allocated STM instance");
            countAllocated++;
            return (Servlet) instancePool.pop();

        }

    }


    /**
     * Return this previously allocated servlet to the pool of available
     * instances.  If this servlet class does not implement SingleThreadModel,
     * no action is actually required.
     *
     * @param servlet The servlet to be returned
     *
     * @exception ServletException if a deallocation error occurs
     */
    public void deallocate(Servlet servlet) throws ServletException {

        // If not SingleThreadModel, no action is required
        if (!singleThreadModel) {
            countAllocated--;
            return;
        }

        // Unlock and free this instance
        synchronized (instancePool) {
            countAllocated--;
            instancePool.push(servlet);
            instancePool.notify();
        }

    }


    /**
     * Return the value for the specified initialization parameter name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Name of the requested initialization parameter
     */
    public String findInitParameter(String name) {

        synchronized (parameters) {
            return ((String) parameters.get(name));
        }

    }


    /**
     * Return the names of all defined initialization parameters for this
     * servlet.
     */
    public String[] findInitParameters() {

        synchronized (parameters) {
            String results[] = new String[parameters.size()];
            return ((String[]) parameters.keySet().toArray(results));
        }

    }


    /**
     * Return the mappings associated with this wrapper.
     */
    public String[] findMappings() {

        synchronized (mappings) {
            return (String[]) mappings.toArray(new String[mappings.size()]);
        }

    }


    /**
     * Return the security role link for the specified security role
     * reference name, if any; otherwise return <code>null</code>.
     *
     * @param name Security role reference used within this servlet
     */
    public String findSecurityReference(String name) {

        synchronized (references) {
            return ((String) references.get(name));
        }

    }


    /**
     * Return the set of security role reference names associated with
     * this servlet, if any; otherwise return a zero-length array.
     */
    public String[] findSecurityReferences() {

        synchronized (references) {
            String results[] = new String[references.size()];
            return ((String[]) references.keySet().toArray(results));
        }

    }


    /**
     * FIXME: Fooling introspection ...
     */
    public Wrapper findMappingObject() {
        return (Wrapper) getMappingObject();
    }


    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance.  This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Servlets whose classnames begin with
     * <code>org.apache.catalina.</code> (so-called "container" servlets)
     * are loaded by the same classloader that loaded this class, rather than
     * the classloader for the current web application.
     * This gives such classes access to Catalina internals, which are
     * prevented for classes loaded for web applications.
     *
     * @exception ServletException if the servlet init() method threw
     *  an exception
     * @exception ServletException if some other loading problem occurs
     */
    public synchronized void load() throws ServletException {
        instance = loadServlet();
    }


    /**
     * Load and initialize an instance of this servlet, if there is not already
     * at least one initialized instance.  This can be used, for example, to
     * load servlets that are marked in the deployment descriptor to be loaded
     * at server startup time.
     */
    public synchronized Servlet loadServlet() throws ServletException {

        // Nothing to do if we already have an instance or an instance pool
        if (!singleThreadModel && (instance != null))
            return instance;

        PrintStream out = System.out;
        if (swallowOutput) {
            SystemLogHandler.startCapture();
        }

        Servlet servlet;
        try {
            long t1=System.currentTimeMillis();
            // If this "servlet" is really a JSP file, get the right class.
            // HOLD YOUR NOSE - this is a kludge that avoids having to do special
            // case Catalina-specific code in Jasper - it also requires that the
            // servlet path be replaced by the <jsp-file> element content in
            // order to be completely effective
            String actualClass = servletClass;
            if ((actualClass == null) && (jspFile != null)) {
                Wrapper jspWrapper = (Wrapper)
                    ((Context) getParent()).findChild(Constants.JSP_SERVLET_NAME);
                if (jspWrapper != null) {
                    actualClass = jspWrapper.getServletClass();
                    // Merge init parameters
                    String paramNames[] = jspWrapper.findInitParameters();
                    for (int i = 0; i < paramNames.length; i++) {
                        if (parameters.get(paramNames[i]) == null) {
                            parameters.put
                                (paramNames[i], 
                                 jspWrapper.findInitParameter(paramNames[i]));
                        }
                    }
                }
            }

            // Complain if no servlet class has been specified
            if (actualClass == null) {
                unavailable(null);
                throw new ServletException
                    (sm.getString("standardWrapper.notClass", getName()));
            }

            // Acquire an instance of the class loader to be used
            Loader loader = getLoader();
            if (loader == null) {
                unavailable(null);
                throw new ServletException
                    (sm.getString("standardWrapper.missingLoader", getName()));
            }

            ClassLoader classLoader = loader.getClassLoader();

            // Special case class loader for a container provided servlet
            //  
            if (isContainerProvidedServlet(actualClass) && 
                    ! ((Context)getParent()).getPrivileged() ) {
                // If it is a priviledged context - using its own
                // class loader will work, since it's a child of the container
                // loader
                classLoader = this.getClass().getClassLoader();
            }

            // Load the specified servlet class from the appropriate class loader
            Class classClass = null;
            try {
                if (SecurityUtil.isPackageProtectionEnabled()){
                    final ClassLoader fclassLoader = classLoader;
                    final String factualClass = actualClass;
                    try{
                        classClass = (Class)AccessController.doPrivileged(
                                new PrivilegedExceptionAction(){
                                    public Object run() throws Exception{
                                        if (fclassLoader != null) {
                                            return fclassLoader.loadClass(factualClass);
                                        } else {
                                            return Class.forName(factualClass);
                                        }
                                    }
                        });
                    } catch(PrivilegedActionException pax){
                        Exception ex = pax.getException();
                        if (ex instanceof ClassNotFoundException){
                            throw (ClassNotFoundException)ex;
                        } else {
                            getServletContext().log( "Error loading "
                                + fclassLoader + " " + factualClass, ex );
                        }
                    }
                } else {
                    if (classLoader != null) {
                        classClass = classLoader.loadClass(actualClass);
                    } else {
                        classClass = Class.forName(actualClass);
                    }
                }
            } catch (ClassNotFoundException e) {
                unavailable(null);


                getServletContext().log( "Error loading " + classLoader + " " + actualClass, e );
                throw new ServletException
                    (sm.getString("standardWrapper.missingClass", actualClass),
                     e);
            }

            if (classClass == null) {
                unavailable(null);
                throw new ServletException
                    (sm.getString("standardWrapper.missingClass", actualClass));
            }

            // Instantiate and initialize an instance of the servlet class itself
            try {
                servlet = (Servlet) classClass.newInstance();
            } catch (ClassCastException e) {
                unavailable(null);
                // Restore the context ClassLoader
                throw new ServletException
                    (sm.getString("standardWrapper.notServlet", actualClass), e);
            } catch (Throwable e) {
                unavailable(null);
                // Restore the context ClassLoader
                throw new ServletException
                    (sm.getString("standardWrapper.instantiate", actualClass), e);
            }

            // Check if loading the servlet in this web application should be
            // allowed
            if (!isServletAllowed(servlet)) {
                throw new SecurityException
                    (sm.getString("standardWrapper.privilegedServlet",
                                  actualClass));
            }

            // Special handling for ContainerServlet instances
            if ((servlet instanceof ContainerServlet) &&
                  (isContainerProvidedServlet(actualClass) ||
                    ((Context)getParent()).getPrivileged() )) {
                ((ContainerServlet) servlet).setWrapper(this);
            }

            classLoadTime=(int) (System.currentTimeMillis() -t1);
            // Call the initialization method of this servlet
            try {
                instanceSupport.fireInstanceEvent(InstanceEvent.BEFORE_INIT_EVENT,
                                                  servlet);

                // START SJS WS 7.0 6236329
                //if( System.getSecurityManager() != null) {
                if ( SecurityUtil.executeUnderSubjectDoAs() ){
                // END OF SJS WS 7.0 6236329
                    Object[] initType = new Object[1];
                    initType[0] = facade;
                    SecurityUtil.doAsPrivilege("init",
                                               servlet,
                                               classType,
                                               initType);
                    initType = null;
                } else {
                    servlet.init(facade);
                }

                // Invoke jspInit on JSP pages
                if ((loadOnStartup >= 0) && (jspFile != null)) {
                    // Invoking jspInit
                    DummyRequest req = new DummyRequest();
                    req.setServletPath(jspFile);
                    req.setQueryString("jsp_precompile=true");

                    // START PWC 4707989
                    String allowedMethods = (String) parameters.get("httpMethods");
                    if (allowedMethods != null
                            && allowedMethods.length() > 0) {
                        String[] s = allowedMethods.split(",");
                        if (s != null && s.length > 0) {
                            req.setMethod(s[0].trim());
                        }
                    }
                    // END PWC 4707989

                    DummyResponse res = new DummyResponse();

                    // START SJS WS 7.0 6236329
                    //if( System.getSecurityManager() != null) {
                    if ( SecurityUtil.executeUnderSubjectDoAs() ){
                    // END OF SJS WS 7.0 6236329
                        Object[] serviceType = new Object[2];
                        serviceType[0] = req;
                        serviceType[1] = res;                
                        SecurityUtil.doAsPrivilege("service",
                                                   servlet,
                                                   classTypeUsedInService,
                                                   serviceType);
                        serviceType = null;
                    } else {
                        servlet.service(req, res);
                    }
                }
                instanceSupport.fireInstanceEvent(InstanceEvent.AFTER_INIT_EVENT,
                                                  servlet);
            } catch (UnavailableException f) {
                instanceSupport.fireInstanceEvent(InstanceEvent.AFTER_INIT_EVENT,
                                                  servlet, f);
                unavailable(f);
                throw f;
            } catch (ServletException f) {
                instanceSupport.fireInstanceEvent(InstanceEvent.AFTER_INIT_EVENT,
                                                  servlet, f);
                // If the servlet wanted to be unavailable it would have
                // said so, so do not call unavailable(null).
                throw f;
            } catch (Throwable f) {
                getServletContext().log("StandardWrapper.Throwable", f );
                instanceSupport.fireInstanceEvent(InstanceEvent.AFTER_INIT_EVENT,
                                                  servlet, f);
                // If the servlet wanted to be unavailable it would have
                // said so, so do not call unavailable(null).
                throw new ServletException
                    (sm.getString("standardWrapper.initException", getName()), f);
            }

            // Register our newly initialized instance
            singleThreadModel = servlet instanceof SingleThreadModel;
            if (singleThreadModel) {
                if (instancePool == null)
                    instancePool = new Stack();
            }

            if (notifyContainerListeners) {
                fireContainerEvent("load", this);
            }

            loadTime=System.currentTimeMillis() -t1;
        } finally {
            if (swallowOutput) {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    if (getServletContext() != null) {
                        getServletContext().log(log);
                    } else {
                        out.println(log);
                    }
                }
            }
        }
        return servlet;

    }


    /**
     * Remove the specified initialization parameter from this servlet.
     *
     * @param name Name of the initialization parameter to remove
     */
    public void removeInitParameter(String name) {

        synchronized (parameters) {
            parameters.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeInitParameter", name);
        }
    }


    /**
     * Remove a listener no longer interested in InstanceEvents.
     *
     * @param listener The listener to remove
     */
    public void removeInstanceListener(InstanceListener listener) {

        instanceSupport.removeInstanceListener(listener);

    }


    /**
     * Remove a mapping associated with the wrapper.
     *
     * @param mapping The pattern to remove
     */
    public void removeMapping(String mapping) {

        synchronized (mappings) {
            mappings.remove(mapping);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeMapping", mapping);
        }
    }


    /**
     * Remove any security role reference for the specified role name.
     *
     * @param name Security role used within this servlet to be removed
     */
    public void removeSecurityReference(String name) {

        synchronized (references) {
            references.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeSecurityReference", name);
        }
    }


    /**
     * Return a String representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardWrapper[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }


    /**
     * Process an UnavailableException, marking this servlet as unavailable
     * for the specified amount of time.
     *
     * @param unavailable The exception that occurred, or <code>null</code>
     *  to mark this servlet as permanently unavailable
     */
    public void unavailable(UnavailableException unavailable) {
        getServletContext().log(sm.getString("standardWrapper.unavailable", getName()));
        if (unavailable == null)
            setAvailable(Long.MAX_VALUE);
        else if (unavailable.isPermanent())
            setAvailable(Long.MAX_VALUE);
        else {
            int unavailableSeconds = unavailable.getUnavailableSeconds();
            if (unavailableSeconds <= 0)
                unavailableSeconds = 60;        // Arbitrary default
            setAvailable(System.currentTimeMillis() +
                         (unavailableSeconds * 1000L));
        }

    }


    /**
     * Unload all initialized instances of this servlet, after calling the
     * <code>destroy()</code> method for each instance.  This can be used,
     * for example, prior to shutting down the entire servlet engine, or
     * prior to reloading all of the classes from the Loader associated with
     * our Loader's repository.
     *
     * @exception ServletException if an exception is thrown by the
     *  destroy() method
     */
    public synchronized void unload() throws ServletException {

        // Nothing to do if we have never loaded the instance
        if (!singleThreadModel && (instance == null))
            return;
        unloading = true;

        // Loaf a while if the current instance is allocated
        // (possibly more than once if non-STM)
        if (countAllocated > 0) {
            int nRetries = 0;
            while ((nRetries < 21) && (countAllocated > 0)) {
                if ((nRetries % 10) == 0) {
                    log.debug(sm.getString("standardWrapper.waiting",
                                           Integer.valueOf(countAllocated),
                                           instance.getClass().getName()));
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ;
                }
                nRetries++;
            }
        }

        ClassLoader oldCtxClassLoader =
            Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = instance.getClass().getClassLoader();

        PrintStream out = System.out;
        if (swallowOutput) {
            SystemLogHandler.startCapture();
        }

        // Call the servlet destroy() method
        try {
            instanceSupport.fireInstanceEvent
              (InstanceEvent.BEFORE_DESTROY_EVENT, instance);

            Thread.currentThread().setContextClassLoader(classLoader);
            // START SJS WS 7.0 6236329
            //if( System.getSecurityManager() != null) {
            if ( SecurityUtil.executeUnderSubjectDoAs() ){
            // END OF SJS WS 7.0 6236329
                SecurityUtil.doAsPrivilege("destroy",
                                           instance);
                SecurityUtil.remove(instance);                           
            } else {
                instance.destroy();
            }

            instanceSupport.fireInstanceEvent
              (InstanceEvent.AFTER_DESTROY_EVENT, instance);
        } catch (Throwable t) {
            instanceSupport.fireInstanceEvent
              (InstanceEvent.AFTER_DESTROY_EVENT, instance, t);
            instance = null;
            instancePool = null;
            nInstances = 0;
            if (notifyContainerListeners) {
                fireContainerEvent("unload", this);
            }
            unloading = false;
            throw new ServletException
                (sm.getString("standardWrapper.destroyException", getName()),
                 t);
        } finally {
            // restore the context ClassLoader
            Thread.currentThread().setContextClassLoader(oldCtxClassLoader);
            // Write captured output
            if (swallowOutput) {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    if (getServletContext() != null) {
                        getServletContext().log(log);
                    } else {
                        out.println(log);
                    }
                }
            }
        }

        // Deregister the destroyed instance
        instance = null;

        if (singleThreadModel && (instancePool != null)) {
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                while (!instancePool.isEmpty()) {
                    // START SJS WS 7.0 6236329
                    //if( System.getSecurityManager() != null) {
                    if ( SecurityUtil.executeUnderSubjectDoAs() ){
                    // END OF SJS WS 7.0 6236329
                        SecurityUtil.doAsPrivilege("destroy",
                                                   ((Servlet) instancePool.pop()));
                        SecurityUtil.remove(instance);                           
                    } else {
                        ((Servlet) instancePool.pop()).destroy();
                    }
                }
            } catch (Throwable t) {
                instancePool = null;
                nInstances = 0;
                unloading = false;
                if (notifyContainerListeners) {
                    fireContainerEvent("unload", this);
                }
                throw new ServletException
                    (sm.getString("standardWrapper.destroyException",
                                  getName()), t);
            } finally {
                // restore the context ClassLoader
                Thread.currentThread().setContextClassLoader
                    (oldCtxClassLoader);
            }
            instancePool = null;
            nInstances = 0;
        }

        singleThreadModel = false;

        unloading = false;
   
        if (notifyContainerListeners) {
            fireContainerEvent("unload", this);
        }
    }


    // -------------------------------------------------- ServletConfig Methods


    /**
     * Return the initialization parameter value for the specified name,
     * if any; otherwise return <code>null</code>.
     *
     * @param name Name of the initialization parameter to retrieve
     */
    public String getInitParameter(String name) {

        return (findInitParameter(name));

    }


    /**
     * Return the set of initialization parameter names defined for this
     * servlet.  If none are defined, an empty Enumeration is returned.
     */
    public Enumeration getInitParameterNames() {

        synchronized (parameters) {
            return (new Enumerator(parameters.keySet()));
        }

    }


    /**
     * Return the servlet context with which this servlet is associated.
     */
    public ServletContext getServletContext() {

        if (parent == null)
            return (null);
        else if (!(parent instanceof Context))
            return (null);
        else
            return (((Context) parent).getServletContext());

    }


    /**
     * Return the name of this servlet.
     */
    public String getServletName() {

        return (getName());

    }

    public long getProcessingTimeMillis() {
        return swValve.getProcessingTimeMillis();
    }

    public void setProcessingTimeMillis(long processingTimeMillis) {
        swValve.setProcessingTimeMillis(processingTimeMillis);
    }

    public long getMaxTimeMillis() {
        return swValve.getMaxTimeMillis();
    }

    public void setMaxTimeMillis(long maxTimeMillis) {
        swValve.setMaxTimeMillis(maxTimeMillis);
    }

    public long getMinTimeMillis() {
        return swValve.getMinTimeMillis();
    }

    public void setMinTimeMillis(long minTimeMillis) {
        swValve.setMinTimeMillis(minTimeMillis);
    }

    public int getRequestCount() {
        return swValve.getRequestCount();
    }

    public void setRequestCount(int requestCount) {
        swValve.setRequestCount(requestCount);
    }

    public int getErrorCount() {
        return swValve.getErrorCount();
    }

    public void setErrorCount(int errorCount) {
           swValve.setErrorCount(errorCount);
    }


    /**
     * Increment the error count used for monitoring.
     */
    public void incrementErrorCount(){
        swValve.setErrorCount(swValve.getErrorCount() + 1);
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public int getClassLoadTime() {
        return classLoadTime;
    }

    // -------------------------------------------------------- Package Methods


    // -------------------------------------------------------- Private Methods


    /**
     * Add a default Mapper implementation if none have been configured
     * explicitly.
     *
     * @param mapperClass Java class name of the default Mapper
     */
    protected void addDefaultMapper(String mapperClass) {

        ;       // No need for a default Mapper on a Wrapper

    }


    /**
     * Return <code>true</code> if the specified class name represents a
     * container provided servlet class that should be loaded by the
     * server class loader.
     *
     * @param classname Name of the class to be checked
     */
    private boolean isContainerProvidedServlet(String classname) {

        if (classname.startsWith("org.apache.catalina.")) {
            return (true);
        }
        try {
            Class clazz =
                this.getClass().getClassLoader().loadClass(classname);
            return (ContainerServlet.class.isAssignableFrom(clazz));
        } catch (Throwable t) {
            return (false);
        }

    }


    /**
     * Return <code>true</code> if loading this servlet is allowed.
     */
    private boolean isServletAllowed(Object servlet) {

        if (servlet instanceof ContainerServlet) {
            if (((Context) getParent()).getPrivileged()
                || (servlet.getClass().getName().equals
                    ("org.apache.catalina.servlets.InvokerServlet"))) {
                return (true);
            } else {
                return (false);
            }
        }

        return (true);

    }


    /**
     * Log the abbreviated name of this Container for logging messages.
     */
    protected String logName() {

        StringBuffer sb = new StringBuffer("StandardWrapper[");
        if (getParent() != null)
            sb.append(getParent().getName());
        else
            sb.append("null");
        sb.append(':');
        sb.append(getName());
        sb.append(']');
        return (sb.toString());

    }


    private Method[] getAllDeclaredMethods(Class c) {

        if (c.equals(javax.servlet.http.HttpServlet.class)) {
            return null;
        }

        Method[] parentMethods = getAllDeclaredMethods(c.getSuperclass());

        Method[] thisMethods = c.getDeclaredMethods();
        if (thisMethods == null) {
            return parentMethods;
        }

        if ((parentMethods != null) && (parentMethods.length > 0)) {
            Method[] allMethods =
                new Method[parentMethods.length + thisMethods.length];
	    System.arraycopy(parentMethods, 0, allMethods, 0,
                             parentMethods.length);
	    System.arraycopy(thisMethods, 0, allMethods, parentMethods.length,
                             thisMethods.length);

	    thisMethods = allMethods;
	}

	return thisMethods;
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Start this component, pre-loading the servlet if the load-on-startup
     * value is set appropriately.
     *
     * @exception LifecycleException if a fatal error occurs during startup
     */
    public void start() throws LifecycleException {
    
        // Send j2ee.state.starting notification 
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting", 
                                                        this.getObjectName(), 
                                                        sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
        // Start up this component
        super.start();

        if( oname != null )
            registerJMX((StandardContext)getParent());
        
        // Load and initialize an instance of this servlet if requested
        // MOVED TO StandardContext START() METHOD

        setAvailable(0L);
        
        // Send j2ee.state.running notification 
        if (this.getObjectName() != null) {
            Notification notification = 
                new Notification("j2ee.state.running", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }

    }


    /**
     * Stop this component, gracefully shutting down the servlet if it has
     * been initialized.
     *
     * @exception LifecycleException if a fatal error occurs during shutdown
     */
    public void stop() throws LifecycleException {

        setAvailable(Long.MAX_VALUE);
        
        // Send j2ee.state.stopping notification 
        if (this.getObjectName() != null) {
            Notification notification = 
                new Notification("j2ee.state.stopping", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
        // Shut down our servlet instance (if it has been initialized)
        try {
            unload();
        } catch (ServletException e) {
            getServletContext().log(sm.getString
                      ("standardWrapper.unloadException", getName()), e);
        }

        // Shut down this component
        super.stop();

        // Send j2ee.state.stoppped notification 
        if (this.getObjectName() != null) {
            Notification notification = 
                new Notification("j2ee.state.stopped", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }
        
        if( oname != null ) {
            Registry.getRegistry().unregisterComponent(oname);
            
            // Send j2ee.object.deleted notification 
            Notification notification = 
                new Notification("j2ee.object.deleted", this.getObjectName(), 
                                sequenceNumber++);
            broadcaster.sendNotification(notification);
        }

        if (isJspServlet && jspMonitorON != null ) {
            Registry.getRegistry(null, null).unregisterComponent(jspMonitorON);
        }

    }

    protected void registerJMX(StandardContext ctx) {

        String parentName = ctx.getEncodedPath();
        parentName = ("".equals(parentName)) ? "/" : parentName;

        String hostName = ctx.getParent().getName();
        hostName = (hostName==null) ? "DEFAULT" : hostName;

        String domain = ctx.getDomain();

        String webMod= "//" + hostName + parentName;
        String onameStr = domain + ":j2eeType=Servlet,name=" + getName() +
                          ",WebModule=" + webMod + ",J2EEApplication=" +
                          ctx.getJ2EEApplication() + ",J2EEServer=" +
                          ctx.getJ2EEServer();

        try {
            oname=new ObjectName(onameStr);
            controller=oname;
            Registry.getRegistry().registerComponent(this, oname, null );
            
            // Send j2ee.object.created notification 
            if (this.getObjectName() != null) {
                Notification notification = new Notification(
                                                "j2ee.object.created", 
                                                this.getObjectName(), 
                                                sequenceNumber++);
                broadcaster.sendNotification(notification);
            }
        } catch( Exception ex ) {
            log.info("Error registering servlet with jmx " + this);
        }

        if (isJspServlet) {
            // Register JSP monitoring mbean
            onameStr = domain + ":type=JspMonitor,name=" + getName()
                       + ",WebModule=" + webMod
                       + ",J2EEApplication=" + ctx.getJ2EEApplication()
                       + ",J2EEServer=" + ctx.getJ2EEServer();
            try {
                jspMonitorON = new ObjectName(onameStr);
                Registry.getRegistry(null, null)
                    .registerComponent(instance, jspMonitorON, null);
            } catch( Exception ex ) {
                log.info("Error registering JSP monitoring with jmx " +
                         instance);
            }
        }

    }
    

    // ------------------------------------------------------------- Attributes
        
        
    public boolean isEventProvider() {
        return false;
    }
    
    public boolean isStateManageable() {
        return false;
    }
    
    public boolean isStatisticsProvider() {
        return false;
    }
        
        
}
