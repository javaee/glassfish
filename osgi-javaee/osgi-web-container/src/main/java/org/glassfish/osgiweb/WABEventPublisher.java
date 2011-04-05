/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import org.glassfish.osgijavaeebase.AbstractOSGiDeployer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.HashMap;
import java.util.Map;

import static org.glassfish.osgiweb.Constants.*;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

/**
 * This class is responsible for publishing events using EventAdmin service for a WAB deployment lifecycle.
 * During deployment, various events are raised as described below (taken verbatim from spec):
 * The Web Extender must track all WABs in the OSGi service platform in which the Web Extender is
 * installed. The Web Extender must post Event Admin events, which is asynchronous, at crucial points
 * in its processing. The topic of the event must be one of the following values:
 * •   org/osgi/service/web/DEPLOYING – The Web Extender has accepted a WAB and started the
 * process of deploying a Web Application.
 * •   org/osgi/service/web/DEPLOYED – The Web Extender has finished deploying a Web Application, and
 * the Web Application is now available for web requests on its Context Path.
 * •   org/osgi/service/web/UNDEPLOYING – The web extender started undeploying the Web Application
 * in response to its corresponding WAB being stopped or the Web Extender is stopped.
 * •   org/osgi/service/web/UNDEPLOYED – The Web Extender has undeployed the Web Application.
 * The application is no longer available for web requests.
 * •   org/osgi/service/web/FAILED – The Web Extender has failed to deploy the Web Application, this
 * event can be fired after the DEPLOYING event has fired and indicates that no DEPLOYED event will be fired.
 * <p/>
 * For each event topic above, the following properties must be published:
 * •   bundle.symbolicName – (String) The bundle symbolic name of the WAB.
 * •   bundle.id – (Long) The bundle id of the WAB.
 * •   bundle – (Bundle) The Bundle object of the WAB.
 * •   bundle.version – (Version) The version of the WAB.
 * •   context.path – (String) The Context Path of the Web Application.
 * •   timestamp – (Long) The time when the event occurred
 * •   extender.bundle – (Bundle) The Bundle object of the Web Extender Bundle
 * •   extender.bundle.id – (Long) The id of the Web Extender Bundle.
 * •   extender.bundle.symbolicName – (String) The symbolic name of the Web Extender Bundle.
 * •   extender.bundle.version – (Version) The version of the Web Extender Bundle.
 * <p/>
 * In addition, the org/osgi/service/web/FAILED event must also have the following property:
 * •   exception – (Throwable) If an exception caused the failure, an exception detailing the error that
 * occurred during the deployment of the WAB.
 * •   collision – (String) If a name collision occurred, the Web-ContextPath that had a collision
 * •   collision.bundles – (Long) If a name collision occurred, a list of bundle ids that all have the same
 * value for the Web-ContextPath manifest header.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class WABEventPublisher {
    void raiseEvent(AbstractOSGiDeployer.State state, Bundle appBundle, Bundle extenderBundle, Throwable e) {
        Event event = prepareEvent(state, appBundle, extenderBundle, e);
        if (event != null) {
            postEvent(event, extenderBundle.getBundleContext());
        }
    }

    private Event prepareEvent(AbstractOSGiDeployer.State state, Bundle appBundle, Bundle extenderBundle, Throwable e) {
        String topic;
        Map<Object, Object> props = new HashMap<Object, Object>();
        props.put(EVENT_PROPERTY_BUNDLE_SYMBOLICNAME, appBundle.getHeaders().get(BUNDLE_SYMBOLICNAME));
        props.put(EVENT_PROPERTY_BUNDLE_ID, appBundle.getBundleId());
        props.put(EVENT_PROPERTY_BUNDLE_VERSION, appBundle.getHeaders().get(BUNDLE_VERSION));
        props.put(EVENT_PROPERTY_CONTEXT_PATH, appBundle.getHeaders().get(WEB_CONTEXT_PATH));
        props.put(EVENT_PROPERTY_TIMESTAMP, System.currentTimeMillis());
        props.put(EVENT_PROPERTY_BUNDLE_ID, appBundle);

        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE, extenderBundle);
        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_ID, extenderBundle.getBundleId());
        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_NAME, extenderBundle.getHeaders().get(BUNDLE_SYMBOLICNAME));
        props.put(EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION, extenderBundle.getHeaders().get(BUNDLE_VERSION));

        switch (state) {
            case DEPLOYING:
                topic = EVENT_TOPIC_DEPLOYING;
                break;
            case DEPLOYED:
                topic = EVENT_TOPIC_DEPLOYING;
                break;
            case FAILED:
                topic = EVENT_TOPIC_FAILED;
                props.put(EVENT_PROPERTY_EXCEPTION, e);
                if (e instanceof ContextPathCollisionException) {
                    final ContextPathCollisionException ce = ContextPathCollisionException.class.cast(e);
                    Long ids[] = ce.getExistingWabIds();
                    props.put(EVENT_PROPERTY_COLLISION_BUNDLES, ids);
                }
                break;
            case UNDEPLOYING:
                topic = EVENT_TOPIC_UNDEPLOYING;
                break;
            case UNDEPLOYED:
                topic = EVENT_TOPIC_UNDEPLOYED;
                break;
            default:
                return null;
        }
        Event event = new Event(topic, props);
        return event;
    }

    private void postEvent(Event event, BundleContext bc) {
        ServiceReference ref = ServiceReference.class.cast(
                bc.getServiceReference(EventAdmin.class.getName()));
        if (ref != null) {
            EventAdmin ea = (EventAdmin) bc.getService(ref);
            if (ea != null) {
                ea.postEvent(event); // asynchronous
            }
            bc.ungetService(ref);
        }
    }

}
