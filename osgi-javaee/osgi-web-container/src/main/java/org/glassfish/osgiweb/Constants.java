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

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Constants
{
    public static final String WEB_BUNDLE_SCHEME = "webbundle";
    public static final String WEB_CONTEXT_PATH = "Web-ContextPath";
    public static final String WEB_JSP_EXTRACT_LOCATION = "Web-JSPExtractLocation";
    public static final String BUNDLE_CONTEXT_ATTR = "osgi-bundlecontext";
    public static final String OSGI_WEB_SYMBOLIC_NAME = "osgi.web.symbolicname";
    public static final String OSGI_WEB_VERSION = "osgi.web.version";
    public static final String OSGI_WEB_CONTEXTPATH= "osgi.web.contextpath";

    // constants related to integration with event admin service
    public static final String EVENT_TOPIC_DEPLOYING = "org/osgi/service/web/DEPLOYING";
    public static final String EVENT_TOPIC_DEPLOYED = "org/osgi/service/web/DEPLOYED";
    public static final String EVENT_TOPIC_UNDEPLOYING = "org/osgi/service/web/UNDEPLOYING";
    public static final String EVENT_TOPIC_UNDEPLOYED = "org/osgi/service/web/UNDEPLOYED";
    public static final String EVENT_TOPIC_FAILED = "org/osgi/service/web/FAILED";

    // various properties published as part of the event data
    public static final String EVENT_PROPERTY_BUNDLE_SYMBOLICNAME = "bundle.symbolicName";
    public static final String EVENT_PROPERTY_BUNDLE_ID = "bundle.id";
    public static final String EVENT_PROPERTY_BUNDLE = "bundle";
    public static final String EVENT_PROPERTY_BUNDLE_VERSION = "bundle.version";
    public static final String EVENT_PROPERTY_CONTEXT_PATH = "context.path";
    public static final String EVENT_PROPERTY_TIMESTAMP = "timestamp";
    public static final String EVENT_PROPERTY_EXTENDER_BUNDLE = "extender.bundle";
    public static final String EVENT_PROPERTY_EXTENDER_BUNDLE_ID = "extender.bundle.id";
    public static final String EVENT_PROPERTY_EXTENDER_BUNDLE_NAME = "extender.bundle.symbolicName";
    public static final String EVENT_PROPERTY_EXTENDER_BUNDLE_VERSION = "extender.bundle.version";
    public static final String EVENT_PROPERTY_EXCEPTION = "exception";
    public static final String EVENT_PROPERTY_COLLISION = "collision";
    public static final String EVENT_PROPERTY_COLLISION_BUNDLES = "collision.bundles";


    // Below are GlassFish specific constants
    public static final String FACES_CONFIG_ATTR = "glassfish.osgi.web.facesconfigs";
    public static final String FACELET_CONFIG_ATTR = "glassfish.osgi.web.faceletconfigs";
    public static final String FACES_ANNOTATED_CLASSES = "glassfish.osgi.web.facesannotatedclasses";
    public static final String VIRTUAL_SERVERS = "Virtual-Servers";
}
