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


package org.glassfish.osgiweb;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.jvnet.hk2.component.Habitat;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.module.bootstrap.ModuleStartup;

/**
 * It is responsible for starting any registered {@link Extender} service
 * after GlassFish server is started and stopping them when server is shutdown.
 * As much as we would like to use GlassFish STARTED event to be notified
 * of server startup, we can't, because the order in which bundles are started
 * is undefined. Fortunately, HK2 OSGi bundle registers Habitat in service
 * registry after ModuleStartup has been called. That's a sufficient
 * indication that server has been started.
 * For shutdown, we don't have any such issue. We use SHUTDOWN event.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class ExtenderManager
{
    private static final Logger logger =
            Logger.getLogger(ExtenderManager.class.getPackage().getName());
    private BundleContext context;
    private Habitat habitat; // handle to HK2 service registry
    private Events events;
    private EventListener listener;
    private ServiceTracker extenderTracker;
    private GlassFishServerTracker glassFishServerTracker; // used to track starting of GlassFish

    public ExtenderManager(BundleContext context)
    {
        this.context = context;
    }

    public void start() throws Exception
    {
        glassFishServerTracker = new GlassFishServerTracker(context);
        glassFishServerTracker.open();
    }

    public void stop() throws Exception
    {
        unregisterGlassFishShutdownHook();
        if (glassFishServerTracker != null) {
            glassFishServerTracker.close();
            glassFishServerTracker = null;
        }
        if (extenderTracker != null) {
            extenderTracker.close();
            extenderTracker = null;
        }
        stopExtenders();
    }

    public void doActualWork() {
        events = habitat.getComponent(Events.class);
        registerGlassFishShutdownHook();
        extenderTracker = new ExtenderTracker(context);
        extenderTracker.open();
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

    /**
     * HK2 OSGi bundle registers ModuleStartup in service
     * registry after ModuleStartup has been called.
     */
    private class GlassFishServerTracker extends ServiceTracker {
        public GlassFishServerTracker(BundleContext context)
        {
            super(context, ModuleStartup.class.getName(), null);
        }

        @Override
        public Object addingService(ServiceReference reference)
        {
            logger.logp(Level.FINE, "ExtenderManager$GlassFishServerTracker", "addingService", "GlassFish has been started");
            ServiceReference habitatServiceRef = context.getServiceReference(Habitat.class.getName());
            habitat = Habitat.class.cast(context.getService(habitatServiceRef));
            doActualWork();
            return super.addingService(reference);
        }
    }
}
