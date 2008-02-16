

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

import javax.management.ObjectName;
import javax.servlet.ServletException;

import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

/** CR 6411114 (Lifecycle implementation moved to ValveBase)
import com.sun.org.apache.commons.modeler.Registry;
*/


/**
 * Standard implementation of a processing <b>Pipeline</b> that will invoke
 * a series of Valves that have been configured to be called in order.  This
 * implementation can be used for any type of Container.
 *
 * <b>IMPLEMENTATION WARNING</b> - This implementation assumes that no
 * calls to <code>addValve()</code> or <code>removeValve</code> are allowed
 * while a request is currently being processed.  Otherwise, the mechanism
 * by which per-thread state is maintained will need to be modified.
 *
 * @author Craig R. McClanahan
 */

public class StandardPipeline
    // START OF IASRI 4665318
    // ValveContext is not needed as it's use increases stack depth
    // implements Pipeline, Contained, Lifecycle, ValveContext {
    implements Pipeline, Contained, Lifecycle {
    // END OF IASRI 4665318
    private static Log log = LogFactory.getLog(StandardPipeline.class);
   

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new StandardPipeline instance with no associated Container.
     */
    public StandardPipeline() {

        this(null);

    }


    /**
     * Construct a new StandardPipeline instance that is associated with the
     * specified Container.
     *
     * @param container The container we should be associated with
     */
    public StandardPipeline(Container container) {

        super();
        setContainer(container);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The basic Valve (if any) associated with this Pipeline.
     */
    protected Valve basic = null;


    /**
     * The Container with which this Pipeline is associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * Descriptive information about this implementation.
     */
    protected String info = "org.apache.catalina.core.StandardPipeline/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    // START OF IASRI# 4647091
    /*
     * The per-thread execution state for processing through this pipeline.
     * The actual value is a java.lang.Integer object containing the subscript
     * into the <code>values</code> array, or a subscript equal to
     * <code>values.length</code> if the basic Valve is currently being
     * processed.
     */
    // protected ThreadLocal state = new ThreadLocal();
    // END OF IASRI# 4647091

    /**
     * The set of Valves (not including the Basic one, if any) associated with
     * this Pipeline.
     */
    protected Valve valves[] = new Valve[0];


    // --------------------------------------------------------- Public Methods


    /**
     * Return descriptive information about this implementation class.
     */
    public String getInfo() {

        return (this.info);

    }


    // ------------------------------------------------------ Contained Methods


    /**
     * Return the Container with which this Pipeline is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Pipeline is associated.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("standardPipeline.alreadyStarted"));

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        started = true;

        // Start the Valves in our pipeline (including the basic), if any
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof Lifecycle)
                ((Lifecycle) valves[i]).start();
            /** CR 6411114 (MBean registration moved to ValveBase.start())
            registerValve(valves[i]);
            */
        }
        if ((basic != null) && (basic instanceof Lifecycle))
            ((Lifecycle) basic).start();
        
        /** CR 6411114 (MBean registration moved to ValveBase.start())
        if( basic!=null )
            registerValve(basic);
        */

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(START_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardPipeline.notStarted"));

        started = false;

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);

        // Stop the Valves in our pipeline (including the basic), if any
        if ((basic != null) && (basic instanceof Lifecycle)) 
            ((Lifecycle) basic).stop();
        /** CR 6411114 (MBean deregistration moved to ValveBase.stop())
        if( basic!=null ) {
            unregisterValve(basic);
        }
        */
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof Lifecycle)
                ((Lifecycle) valves[i]).stop();
            /** CR 6411114 (MBean deregistration moved to ValveBase.stop())
            unregisterValve(valves[i]);
            */
        
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

    }

    /** CR 6411114 (MBean registration/deregistration moved to ValveBase)
    private void registerValve(Valve valve) {

        if( valve instanceof ValveBase &&
                ((ValveBase)valve).getObjectName()==null ) {
            try {
                
                String domain=((ContainerBase)container).getDomain();
                if( container instanceof StandardContext ) {
                    domain=((StandardContext)container).getEngineName();
                }
                if( container instanceof StandardWrapper) {
                    Container ctx=((StandardWrapper)container).getParent();
                    domain=((StandardContext)ctx).getEngineName();
                }
                ObjectName vname=((ValveBase)valve).createObjectName(
                        domain,
                        ((ContainerBase)container).getJmxName());
                if( vname != null ) {
                    ((ValveBase)valve).setObjectName(vname);
                    Registry.getRegistry().registerComponent(valve, vname, valve.getClass().getName());
                    ((ValveBase)valve).setController(((ContainerBase)container).getJmxName());
                }
            } catch( Throwable t ) {
                log.info( "Can't register valve " + valve , t );
            }
        }
    }
    
    private void unregisterValve(Valve valve) {
        if( valve instanceof ValveBase ) {
            try {
                ValveBase vb=(ValveBase)valve;
                if( vb.getController()!=null &&
                        vb.getController() == 
                        ((ContainerBase)container).getJmxName() ) {
                    
                    ObjectName vname=vb.getObjectName();
                    Registry.getRegistry().getMBeanServer().unregisterMBean(vname);
                    ((ValveBase)valve).setObjectName(null);
                }
            } catch( Throwable t ) {
                log.info( "Can't unregister valve " + valve , t );
            }
        }
    }    
    */

    // ------------------------------------------------------- Pipeline Methods


    /**
     * <p>Return the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).
     */
    public Valve getBasic() {

        return (this.basic);

    }


    /**
     * <p>Set the Valve instance that has been distinguished as the basic
     * Valve for this Pipeline (if any).  Prioer to setting the basic Valve,
     * the Valve's <code>setContainer()</code> will be called, if it
     * implements <code>Contained</code>, with the owning Container as an
     * argument.  The method may throw an <code>IllegalArgumentException</code>
     * if this Valve chooses not to be associated with this Container, or
     * <code>IllegalStateException</code> if it is already associated with
     * a different Container.</p>
     *
     * @param valve Valve to be distinguished as the basic Valve
     */
    public void setBasic(Valve valve) {

        // Change components if necessary
        Valve oldBasic = this.basic;
        if (oldBasic == valve)
            return;

        // Stop the old component if necessary
        if (oldBasic != null) {
            if (started && (oldBasic instanceof Lifecycle)) {
                try {
                    ((Lifecycle) oldBasic).stop();
                } catch (LifecycleException e) {
                    log.error("StandardPipeline.setBasic: stop", e);
                }
            }
            if (oldBasic instanceof Contained) {
                try {
                    ((Contained) oldBasic).setContainer(null);
                } catch (Throwable t) {
                    ;
                }
            }
        }

        // Start the new component if necessary
        if (valve == null)
            return;
        if (valve instanceof Contained) {
            ((Contained) valve).setContainer(this.container);
        }
        /** CR 6411114
        if (valve instanceof Lifecycle) {
        */
        // START CR 6411114
        // Start the valve if the pipeline has already been started
        if (started && (valve instanceof Lifecycle)) {
        // END CR 6411114
            try {
                ((Lifecycle) valve).start();
            } catch (LifecycleException e) {
                log.error("StandardPipeline.setBasic: start", e);
                return;
            }
        }
        this.basic = valve;

    }


    /**
     * <p>Add a new Valve to the end of the pipeline associated with this
     * Container.  Prior to adding the Valve, the Valve's
     * <code>setContainer()</code> method will be called, if it implements
     * <code>Contained</code>, with the owning Container as an argument.
     * The method may throw an
     * <code>IllegalArgumentException</code> if this Valve chooses not to
     * be associated with this Container, or <code>IllegalStateException</code>
     * if it is already associated with a different Container.</p>
     *
     * @param valve Valve to be added
     *
     * @exception IllegalArgumentException if this Container refused to
     *  accept the specified Valve
     * @exception IllegalArgumentException if the specifie Valve refuses to be
     *  associated with this Container
     * @exception IllegalStateException if the specified Valve is already
     *  associated with a different Container
     */
    public void addValve(Valve valve) {
    
        // Validate that we can add this Valve
        if (valve instanceof Contained)
            ((Contained) valve).setContainer(this.container);

        // Start the new component if necessary
        if (started) {
            if (valve instanceof Lifecycle) {
                try {
                    ((Lifecycle) valve).start();
                } catch (LifecycleException e) {
                    log.error("StandardPipeline.addValve: start: ", e);
                }
            }
            /** CR 6411114 (MBean registration moved to ValveBase.start())
            // Register the newly added valve
            registerValve(valve);
            */
        }

        // Add this Valve to the set associated with this Pipeline
        synchronized (valves) {
            Valve results[] = new Valve[valves.length +1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = valve;
            valves = results;
        }

    }


    /**
     * Return the set of Valves in the pipeline associated with this
     * Container, including the basic Valve (if any).  If there are no
     * such Valves, a zero-length array is returned.
     */
    public Valve[] getValves() {

        if (basic == null)
            return (valves);
        synchronized (valves) {
            Valve results[] = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = basic;
            return (results);
        }

    }

    public ObjectName[] getValveObjectNames() {
        ObjectName oname[]=new ObjectName[valves.length + 1];
        for( int i=0; i<valves.length; i++ ) {
            if( valves[i] instanceof ValveBase )
                oname[i]=((ValveBase)valves[i]).getObjectName();
        }
        if( basic instanceof ValveBase )
            oname[valves.length]=((ValveBase)basic).getObjectName();
        return oname;
    }

    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        // START PWC 4665318
        doInvoke(request, response);
    }

    protected void doInvoke(Request request, Response response)
        throws IOException, ServletException {

        doInvoke(request, response, false);
    }

    protected void doChainInvoke(Request request, Response response)
        throws IOException, ServletException {

        doInvoke(request, response, true);
    }

    private void doInvoke(Request request, Response response,
        boolean chaining) throws IOException, ServletException {
        // END PWC 4665318

        // START OF IASRI 4665318
        if ((valves.length > 0) || (basic != null)) {
            // Set the status so that if there are no valves (other than the
            // basic one), the basic valve's request processing logic will
            // be invoked
            int status = Valve.INVOKE_NEXT;

            // Iterate over all the valves in the pipeline and invoke
            // each valve's processing logic and then move onto to the
            // next valve in the pipeline only if the previous valve indicated
            // that the pipeline should proceed.
            int i;
            for (i = 0; i < valves.length; i++) {
                Request req = request;
                Response resp = response;
                if (chaining) {
                    req = getRequest(request);
                    resp = getResponse(request, response);
                }
                status = valves[i].invoke(req, resp);
                if (status != Valve.INVOKE_NEXT)
                    break;
            }

            // Save a reference to the valve[], to ensure that postInvoke()
            // is invoked on the original valve[], in case a valve gets added
            // or removed during the invocation of the basic valve (e.g.,
            // in case access logging is enabled or disabled by some kind of
            // admin servlet), in which case the indices used for postInvoke
            // invocations below would be off
            Valve[] savedValves = valves;

            // Invoke the basic valve's request processing and post-request
            // logic only if the pipeline was not aborted (i.e. no valve
            // returned END_PIPELINE)
            if ((status == Valve.INVOKE_NEXT) && (basic != null)) {
                Request req = request;
                Response resp = response;
                if (chaining) {
                    req = getRequest(request);
                    resp = getResponse(request, response);
                }
                basic.invoke(req, resp);
                basic.postInvoke(req, resp);
            }

            // Invoke the post-request processing logic only on those valves
            // that returned a status of INVOKE_NEXT
            for (int j = i - 1; j >= 0; j--) {
                Request req = request;
                Response resp = response;
                if (chaining) {
                    req = getRequest(request);
                    resp = getResponse(request, response);
                }
                savedValves[j].postInvoke(req, resp);
            }

            savedValves = null;

        } else {
            throw new ServletException
                (sm.getString("standardPipeline.noValve"));
        }
        // END OF IASRI 4665318

    }


    /**
     * Remove the specified Valve from the pipeline associated with this
     * Container, if it is found; otherwise, do nothing.  If the Valve is
     * found and removed, the Valve's <code>setContainer(null)</code> method
     * will be called if it implements <code>Contained</code>.
     *
     * @param valve Valve to be removed
     */
    public void removeValve(Valve valve) {

        synchronized (valves) {

            // Locate this Valve in our list
            int j = -1;
            for (int i = 0; i < valves.length; i++) {
                if (valve == valves[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;

            // Remove this valve from our list
            Valve results[] = new Valve[valves.length - 1];
            int n = 0;
            for (int i = 0; i < valves.length; i++) {
                if (i == j)
                    continue;
                results[n++] = valves[i];
            }
            valves = results;
            try {
                if (valve instanceof Contained)
                    ((Contained) valve).setContainer(null);
            } catch (Throwable t) {
                ;
            }

        }

        // Stop this valve if necessary
        if (started) {
            if (valve instanceof Lifecycle) {
                try {
                    ((Lifecycle) valve).stop();
                } catch (LifecycleException e) {
                    log.error("StandardPipeline.removeValve: stop: ", e);
                }
            }
            /** CR 6411114 (MBean deregistration moved to ValveBase.stop())
            // Unregister the removed valave
            unregisterValve(valve);
            */
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardPipeline[" + container.getName() + "]: " +
                       message);
        else
            System.out.println("StandardPipeline[" + container.getName() +
                               "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {

        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardPipeline[" + container.getName() + "]: " +
                       message, throwable);
        else {
            System.out.println("StandardPipeline[" + container.getName() +
                               "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }

    // ------------------------------------------------------ Private Methods
    private Request getRequest(Request request) {
	Request r = (Request) 
	    request.getNote(Globals.WRAPPED_REQUEST); 
	if (r == null) {
	    r = request;
	}
	return r;
    }

    private Response getResponse(Request request, Response response) {
	Response r = (Response) 
	    request.getNote(Globals.WRAPPED_RESPONSE); 
	if (r == null) {
	    r = response;
	}
	return r;
    }
}
