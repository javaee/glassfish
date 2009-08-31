/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.web.osgi;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.internal.api.Globals;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It is responsible for starting any registered {@link Extender} service
 * after GlassFish server is started and stopping them when server is shutdown.
 * It does so by listening to GlassFish STARTED and SHUTDOWN event.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ExtenderManager
{
    private static final Logger logger =
            Logger.getLogger(ExtenderManager.class.getPackage().getName());
    private BundleContext context;
    private Events events = Globals.get(Events.class);
    private EventListener listener;
    private Semaphore serverReady = new Semaphore(0);
    private ServiceTracker extenderTracker;

    public ExtenderManager(BundleContext context)
    {
        this.context = context;
    }

    public void start() throws Exception
    {
        // spawn a thread and wait for server to start before proceeding
        waitForServerToStart();
    }

    public void stop() throws Exception
    {
        unregisterGlassFishShutdownHook();
        if (extenderTracker != null) {
            extenderTracker.close();
            extenderTracker = null;
        }
        stopExtenders();
    }

    public void doActualWork() {
        registerGlassFishShutdownHook();
        extenderTracker = new ExtenderTracker(context);
        extenderTracker.open();
    }

    /**
     * This method spawns a new thread, waits for server to start.
     * After being notified of server start, it proceeds by calling
     * {@link #doActualWork()}
     */
    private void waitForServerToStart()
    {
        final EventListener serverStartedListener = new EventListener()
        {
            public void event(Event event)
            {
                if (EventTypes.SERVER_READY.equals(event.type()))
                {
                    logger.logp(Level.INFO, "WebExtender", "event", "Received Server Started Event");
                    serverReady.release();
                    events.unregister(this);
                }
            }
        };
        events.register(serverStartedListener);

        new Thread(new Runnable(){
            public void run()
            {
                // Check again to ensure that we did not miss the event
                // after we checked the status and before we registered
                // the listener. If we don't check and we have indeed
                // missed the event, we will end up waiting for ever.
                if (!isServerStarted()) {
                    logger.logp(Level.INFO, "WebExtender", "run", "Waiting for Server to start");
                    try
                    {
                        serverReady.acquire();
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                doActualWork();
            }
        }).start();
    }

    private void stopExtenders()
    {
        try
        {
            for (ServiceReference ref :
                    context.getServiceReferences(Extender.class.getName(), null)) {
                Extender e = Extender.class.cast(context.getService(ref));
                try {
                    e.stop();
                } finally {
                    context.ungetService(ref);
                }
            }
        }
        catch (InvalidSyntaxException e)
        {
            logger.logp(Level.WARNING, "ExtenderManager", "stopExtenders",
                    "Not able to stop all extenders", e);
        }

    }

    private boolean isServerStarted() {
        ServerEnvironment serverEnv = Globals.get(ServerEnvironment.class);
        return serverEnv.getStatus() == ServerEnvironment.Status.started;
    }

    private void registerGlassFishShutdownHook() {
        listener = new EventListener() {
            public void event(Event event)
            {
                if (EventTypes.PREPARE_SHUTDOWN.equals(event.type())) {
                    stopExtenders();
                }
            }
        };
        events.register(listener);
    }

    private void unregisterGlassFishShutdownHook() {
        if (listener != null) {
            events.unregister(listener);
        }
    }

    private class ExtenderTracker extends ServiceTracker {
        ExtenderTracker(BundleContext context)
        {
            super(context, Extender.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference)
        {
            Extender e = Extender.class.cast(context.getService(reference));
            e.start();
            return e;
        }

    }
}
