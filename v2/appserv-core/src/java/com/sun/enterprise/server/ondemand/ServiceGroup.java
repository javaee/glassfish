/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.server.ondemand;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import com.sun.logging.LogDomains;
import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.PEMain;
import com.sun.enterprise.server.ondemand.entry.*;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.Switch;

/**
 * Super class of all servicegroups. Service group is a collection
 * of services in application server. Service group design follows
 * a variant of composite design pattern. Thus each servicegroup
 * is a composite of other servicegroup.
 * 
 * This super class basically has the logic to handle the servicegroup
 * state and a general logic to handle the children of the servicegroup.
 *
 * @see ServiceGroupBuilder
 * @see MainServiceGroup
 * @see EjbServiceGroup
 * @see WebServiceGroup
 * @see ResourcesServiceGroup
 */
public abstract class ServiceGroup {

    protected static final Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private final ArrayList sgList = new ArrayList();

    public static final int NOTSTARTED = 0;
    public static final int STARTING = 1;
    public static final int STARTED = 2;
    public static final int STOPPING = 8;
    public static final int STOPPED = 9;

    private int state = NOTSTARTED;

    private ServerLifecycle[] services = {};
    private static final ArrayList<ServiceGroupListener> listeners;

    static {
        listeners = new ArrayList();
        String extListener = 
        System.getProperty("com.sun.enterprise.server.ondemand.ExternalListener");
        if(extListener != null) {
            try {
                ServiceGroupListener servicegrouplistener = 
                (ServiceGroupListener)Class.forName(extListener).newInstance();
                listeners.add(servicegrouplistener);
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void notifyListener (int state, ServiceGroup sg) {
        for (ServiceGroupListener l : listeners) {
            try {
                switch (state) {
                    case STARTING :
                        l.beforeStart(sg.getClass().getName());
                        break;
                    case STARTED :
                        l.afterStart(sg.getClass().getName());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void addServiceGroupListener(ServiceGroupListener listener) {
        listeners.add(listener);
    }

    public static void removeServiceGroupListener(ServiceGroupListener listener) {
        listeners.remove(listener);
    }

    /**
     * Add a servicegroup as child of this servicegroup.
     */
    public void addServiceGroup(ServiceGroup group) {
        sgList.add(group);
    }
                                                                                                                             
    /**
     * Remove a servicegroup from its children.
     */
    public void removeServiceGroup(ServiceGroup group) {
        sgList.remove(group);
    }

    /**
     * Iterator of children.
     */
    public Iterator serviceGroupIterator() {
        return sgList.iterator();
    }

    /**
     * If atleast one of the servicegroup that need to be started
     * is interested in this context, then the servicegroup is 
     * not notified.
     */
    public boolean isNotified(EntryContext context) {
        Iterator it = serviceGroupIterator();
        while (it.hasNext()) {
            final ServiceGroup sg = (ServiceGroup) it.next();
            if (sg.getState() != STARTED) {
                if (sg.analyseEntryContext(context)) {
                     return false;
                }
            }
        }
        return true;
    }

    /**
     * Logic to start all children based on entrycontext, If any of the child
     * recognises the entrycontext, then this will attempt to start that
     * servicegroup.
     */
    public void startChildren(final EntryContext context) throws ServiceGroupException {
        Iterator it = serviceGroupIterator();
        while (it.hasNext()) {
            final ServiceGroup sg = (ServiceGroup) it.next();

            if (_logger.isLoggable(Level.FINER)) {
                _logger.finer("Trying " + sg + " servicegroup");
            }

            if (sg.getState() != STARTED) {
                if (sg.analyseEntryContext(context)) {
                    synchronized (sg) {
                        if (sg.getState() == NOTSTARTED) {
                            sg.setState(STARTING);
                            if (_logger.isLoggable(Level.FINE)) {
                               _logger.fine("Starting " + sg + " servicegroup with context :" + context);
                            }
                            notifyListener(STARTING, sg);
                            ComponentInvocation dummy = preInvoke();
                            try {
                                AccessController.doPrivileged
                                    (new PrivilegedExceptionAction() {
                                    public Object run() throws Exception {
                                        sg.start(context);
                                        return null;
                                    }
                                });
                            } catch (PrivilegedActionException pae) {
                                throw new ServiceGroupException(
                                      pae.getException());
                            } finally {
                                postInvoke(dummy);
                            }
                            notifyListener(STARTED, sg);
                            sg.setState(STARTED);
                        }
                    }
                }
            }
        }
    }

    /**
     * The preInvoke is used to insert a dummy invocation context to the 
     * invocation chain for everything that happens within the service
     * startup. This is detected by the InvocationManager and the 
     * threads created from within this context will not inherit the 
     * context of the parent.
     * 
     * This is required since an application thread might end up in starting 
     * a container. Thread pools managed by that container should not
     * inherit the properties by the application, since both are unrelated.
     */
    private ComponentInvocation preInvoke() {
        InvocationManager im = Switch.getSwitch().getInvocationManager();
        ComponentInvocation dummy = 
        new ComponentInvocation(ComponentInvocation.SERVICE_STARTUP);
        im.preInvoke(dummy);
        return dummy;
    }

    /**
     * See the comment on preInvoke.
     */
    private void postInvoke(ComponentInvocation dummy) {
        InvocationManager im = Switch.getSwitch().getInvocationManager();
        im.postInvoke(dummy);
    }

    // Stop all children.
    public void stopChildren(EntryContext context) throws ServiceGroupException {
        Iterator it = serviceGroupIterator();
        while (it.hasNext()) {
            ServiceGroup sg = (ServiceGroup) it.next();
            if (sg.getState() != STOPPED && sg.getState() != NOTSTARTED) {
                sg.stop(context);
            }
        }
    }

    // Abort all children.
    public void abortChildren(EntryContext context) {
        Iterator it = serviceGroupIterator();
        while (it.hasNext()) {
            ServiceGroup sg = (ServiceGroup) it.next();
            if (sg.getState() != STOPPED) {
                sg.abort(context);
            }
        }
    }

    // Start lifecycle services. Concrete servicegroup implementations
    // may use this method.
    protected void startLifecycleServices(String[][] s, ServerContext sc) {
        services = new ServerLifecycle[s.length];
        for (int i =0; i < s.length; i ++) {
            try {
                String service = s[i][1];
                ServerLifecycle slc = (ServerLifecycle) 
                Class.forName(service).newInstance();
                services[i] = slc;
                slc.onInitialization(sc);
            } catch (Exception e) {
                _logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        for (ServerLifecycle slc : services) {
            try {
                slc.onStartup(sc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (ServerLifecycle slc : services) {
            try {
                slc.onReady(sc);
            } catch (Exception e) {
                _logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    // Stop lifecycle services. concrete servicegroup implementations
    // may use this method.
    protected void stopLifecycleServices() {
        for (ServerLifecycle slc : services) {
            try {
                slc.onShutdown();
            } catch (Exception e) {
                _logger.log(Level.WARNING, e.getMessage(), e);
            }
        }

        for (ServerLifecycle slc : services) {
            try {
                slc.onTermination();
            } catch (Exception e) {
                _logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Triggers the start of the servicegroup. The entry context
     * that caused this startup is used by the servicegroup to obtain
     * any startup information it require.
     * 
     * @param context EntryContext object.
     * @see EntryContext.
     */
    public abstract void start(EntryContext context) throws ServiceGroupException ;

    /**
     * Analyse the entrycontext and specifies whether this servicegroup
     * can be started or not.
     *
     * @return boolean If true is returned, this servicegroup can be started
     * If false is returned, the entrycontext  is not recognized by the 
     * servicegroup.
     */
    public abstract boolean analyseEntryContext(EntryContext context);

    /**
     * Stop the servicegroup.
     */
    public abstract void stop(EntryContext context) throws ServiceGroupException;

    /**
     * Abort the servicegroup. Unused. For future!
     */
    public abstract void abort(EntryContext context);

    /**
     * Set the state of the servicegroup.
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Return the state of the servicegroup.
     */
    public int getState() {
        return this.state;
    }
}
