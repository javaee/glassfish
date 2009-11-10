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


package org.glassfish.osgihttp;

import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebModule;
import com.sun.enterprise.web.WebModuleConfig;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.Container;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.core.StandardContext;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;

/**
 * This is the entry point to our implementation of OSGi/HTTP service.
 * Based on user input (for now it uses configuration property called
 * org.glassfish.web.osgihttp.VirtualHostId), it selects the virtual host
 * under which it creates a new context. The context path can be defined
 * by user using configuration property org.glassfish.web.osgihttp.ContextPath.
 * If it is absent, we use a default value of "/osgi." After initializing
 * the HttpService factory with necessary details, we register the factory
 * OSGi service registry.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Activator implements BundleActivator {

    private BundleContext bctx;
    private Host vs;
    private String contextPath;
    private ServiceRegistration registration;

    // configuration property used to select virtual host under which
    // this service is deployed.
    private static final String VS_ID_PROP =
            Activator.class.getPackage().getName() + ".VirtualServerId";

    // configuration property used to select context root under which
    // this service is deployed.
    private static final String CONTEXT_PATH_PROP =
            Activator.class.getPackage().getName() + ".ContextPath";

    public void start(BundleContext context) throws Exception {
        bctx = context;
        StandardContext standardContext = getStandardContext(context);
        GlassFishHttpService httpService = new GlassFishHttpService(standardContext);
        registration = context.registerService(HttpService.class.getName(),
                new HttpServiceWrapper.HttpServiceFactory(httpService),
                null);
    }

    private StandardContext getStandardContext(BundleContext context) throws Exception {
        WebContainer webContainer =
                Globals.get(WebContainer.class);
        Engine engine = webContainer.getEngine();
        String vsId = context.getProperty(VS_ID_PROP);
        if (vsId == null) {
            vsId = "server";
//            throw new Exception("You must specify which virtual server to use using property called " + VS_ID_PROP);
        }
        vs = (Host) engine.findChild(vsId);
        if (vs == null) {
            throw new Exception("No virtual host by name : " + vsId +
                    ". Please specify virtual server name using property called " + VS_ID_PROP);
        }
        contextPath = context.getProperty(CONTEXT_PATH_PROP);
        if (contextPath == null) {
            contextPath = "/osgi"; // default value
        }
        // create a new context under which all OSGi HTTP wrappers
        // will be registered.
        WebModule standardContext = new WebModule();
        standardContext.setWebContainer(webContainer);
        standardContext.setName(contextPath);
        standardContext.setPath(contextPath);
        // TODO(Sahoo): Need to set proper values for these directories
        standardContext.setDocBase(System.getProperty("java.io.tmpdir"));
        standardContext.setWorkDir(System.getProperty("java.io.tmpdir"));
        // standardContext.setJ2EEServer(System.getProperty("com.sun.aas.instanceName"));
        standardContext.setJ2EEServer("server");
        standardContext.addLifecycleListener(new ContextConfig());
        Realm realm = Globals.getDefaultHabitat().getByContract(Realm.class);
        standardContext.setRealm(realm);
        WebModuleConfig wmConfig = new WebModuleConfig();
        wmConfig.setWorkDirBase(System.getProperty("java.io.tmpdir"));
        wmConfig.setVirtualServers(vsId);

        // Setting it in WebModuleConfig does not work, Ceck with Jan.
//        wmConfig.setAppClassLoader(getCommonClassLoader());
        standardContext.setParentClassLoader(getCommonClassLoader());
        standardContext.setWebModuleConfig(wmConfig);

        // Since there is issue about locating user classes that are part
        // of some OSGi bundle while deserializing, we switch off session
        // persistence.
        switchOffSessionPersistence(standardContext);
        vs.addChild(standardContext);
//        StandardContext standardContext =
//                StandardContext.class.cast(vs.findChild(contextPath));
        return standardContext;
    }

    private ClassLoader getCommonClassLoader()
    {
        ClassLoaderHierarchy clh =
                Globals.getDefaultHabitat().getComponent(ClassLoaderHierarchy.class);
        return clh.getAPIClassLoader();
    }

    public void stop(BundleContext context) throws Exception {
        registration.unregister();
        StandardContext standardContext =
                StandardContext.class.cast(vs.findChild(contextPath));
        for (Container child : standardContext.findChildren()) {
            standardContext.removeChild(child);
        }
        vs.removeChild(standardContext);
        // TODO(Sahoo): Need to call stop on all wrappers if they are not
        // automatically stopped when removed from context.
    }

    private void switchOffSessionPersistence(StandardContext ctx) {
        // See Jan's blog about how to switch off
        // Session persistence:
        // http://blogs.sun.com/jluehe/entry/how_to_disable_persisting_of
        Manager mgr = ctx.getManager();
        if (mgr == null) {
            mgr = new StandardManager();
            StandardManager.class.cast(mgr).setPathname(null);
            ctx.setManager(mgr);
        } else {
            try {
                StandardManager.class.cast(mgr).setPathname(null);
            } catch (ClassCastException cce) {
                System.out.println(mgr +
                        " does not allow path name of session store to be configured.");
            }
        }
    }
}
