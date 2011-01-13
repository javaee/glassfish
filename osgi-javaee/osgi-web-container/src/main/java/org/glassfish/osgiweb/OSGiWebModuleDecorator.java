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

import com.sun.enterprise.web.WebModuleDecorator;
import com.sun.enterprise.web.WebModule;
import com.sun.faces.spi.ConfigurationResourceProvider;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.web.loader.WebappClassLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.glassfish.osgijavaeebase.BundleResource;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for setting
 * a) an attribute called {@link Constants#BUNDLE_CONTEXT_ATTR} in ServletContext of the web app
 * associated with the current OSGi bundle.
 * b) discovering JSF faces config resources and setting them in an attribute called
 * {@link Constants#FACES_CONFIG_ATTR}.
 * c) discovering JSF facelet config resources and setting them in an attribute called
 * {@link Constants#FACELET_CONFIG_ATTR}.
 * d) discovering faces annotations in a WAB and setting them in an attribute called
 * {@@link Constants#FACES_ANNOTATED_CLASSES}
 *
 * This class is looked up by mojarra using JDK SPI mechanism.
 *
 * @see org.glassfish.osgiweb.OSGiFacesConfigResourceProvider
 * @see org.glassfish.osgiweb.OSGiFaceletConfigResourceProvider
 * @see org.glassfish.osgiweb.OSGiWebDeploymentContext.WABClassLoader#getResources(String) 
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebModuleDecorator implements WebModuleDecorator
{
    private Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private boolean active = true;

    public void decorate(WebModule module)
    {
        if (isActive()) {
            BundleContext bctx = OSGiWebDeploymentRequest.getCurrentBundleContext();
            if (bctx != null) {
                final ServletContext sc = module.getServletContext();
                sc.setAttribute(Constants.BUNDLE_CONTEXT_ATTR, bctx);
                if (isMojarraPresent()) {
                    populateFacesInformation(module, bctx, sc);
                }
            }
        }
    }

    private boolean isMojarraPresent() {
        // We don't have a hard dependency on JSF or mojarra in our Import-Package. So, we need to test
        // if mojarra is available or not.
        try {
            return Class.forName(ConfigurationResourceProvider.class.getName()) != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void populateFacesInformation(WebModule module, BundleContext bctx, ServletContext sc) {
        Collection<URI> facesConfigs = new ArrayList<URI>();
        Collection<URI> faceletConfigs = new ArrayList<URI>();
        discoverJSFConfigs(bctx.getBundle(), facesConfigs, faceletConfigs);
        sc.setAttribute(Constants.FACES_CONFIG_ATTR, facesConfigs);
        sc.setAttribute(Constants.FACELET_CONFIG_ATTR, faceletConfigs);
        Map<Class<? extends Annotation>, Set<Class<? extends Object>>> facesAnnotatedClasses =
                scanFacesAnnotations(module);
        sc.setAttribute(Constants.FACES_ANNOTATED_CLASSES, facesAnnotatedClasses);
    }

    private synchronized boolean isActive() {
        return active;
    }

    /* package */ synchronized void deActivate()
    {
        this.active = false;
    }

    /**
     * JSF has two kinds of configuration files, viz: faces configs and facelet configs.
     * While faces configs are identified by a file name faces-config.xml or a file ending with .faces-config.xml in META-INF/,
     * facelet configs are identified by files in META-INF/ having suffix .taglib.xml. Note that facelet configs
     * are never named simply taglib.xml, they must end with .taglib.xml, where as faces configs can be named as
     * faces-config.xml as well as ending with .faces-config.xml.
     *
     * As you can see from the above description, it is a pattern based search.
     * The default config resource providers in mojarra (our JSF implementation layer) is not OSGi aware, so
     * it does not know how to iterate over bundle entries. More over, it does not even know about Archive abstraction
     * that GlassFish deployment backend uses. It relies on web app classloader to return jar or file type urls for
     * resources so that they can walk through the resource hierarchy to find matching resource files.
     * Since, {@link org.glassfish.osgiweb.OSGiWebDeploymentContext.WABClassLoader} does not provide
     * jar or file type URLs for resources, the default providers of mojarra are insufficient for our needs
     * as mentioned in https://glassfish.dev.java.net/issues/show_bug.cgi?id=11606.
     * So, we need to augment the providers discovered by mojarra providers. This method discovers JSF resources
     * packaged in a bundle. It returns the results in the two collections passed to this method.
     * These two collections are then set as ServletContext attributes which are used by
     * {@link org.glassfish.osgiweb.OSGiFacesConfigResourceProvider} and {@link org.glassfish.osgiweb.OSGiFaceletConfigResourceProvider}.
     *
     * Since mojarra can discover faces-config.xmls, in order to avoid duplicate resource situation as
     * reported in https://glassfish.dev.java.net/issues/show_bug.cgi?id=12914, we only find faces config resources
     * that ends with .faces-config.xml.
     */
    private void discoverJSFConfigs(Bundle b, Collection<URI> facesConfigs, Collection<URI> faceletConfigs) {
        OSGiBundleArchive archive = new OSGiBundleArchive(b);
        for (BundleResource r : archive) {
            final String path = r.getPath();
            if (path.startsWith("META-INF/")) {
                final URI uri = r.getUri();
                if (path.endsWith(".taglib.xml")) {
                    faceletConfigs.add(uri);
                } else if (path.endsWith(".faces-config.xml")) { // this check automatically excludes META-INF/faces-config.xml
                    facesConfigs.add(uri);
                }
            }
        }
    }

    private Map<Class<? extends Annotation>, Set<Class<? extends Object>>> scanFacesAnnotations(WebModule wm) {
        final DeploymentContext dc = wm.getWebModuleConfig().getDeploymentContext();
        if (dc == null) {
            // We are likely to be here when there are no web apps deployed and the first webapp that gets deployed
            // is a WAB. In that case, the default_web_app gets loaded in the same thread that's trying to load
            // the WAB and we end up getting here, because our thread local object contains the WAB's bundle context
            // at this point of time. That's one of the many ugly side effects of using thread locals.
            logger.fine("Can't process annotations as deployment context is not set.");
            return Collections.emptyMap();
        }
        final Types types = dc.getTransientAppMetaData(Types.class.getName(), Types.class);
        return OSGiFacesAnnotationScanner.scan(getURIs(wm), types, getClassLoader(wm));
    }

    private Collection<URI> getURIs(WebModule wm) {
        WebappClassLoader cl = getClassLoader(wm);
        Collection<URI> uris = new ArrayList<URI>();
        for (URL url : cl.getURLs()) {
            try {
                uris.add(url.toURI());
            } catch (URISyntaxException e) {
                logger.log(Level.WARNING, "Unable to process " + url, e);
            }
        }
        return uris;
    }

    private WebappClassLoader getClassLoader(WebModule wm) {
        WebappClassLoader cl = WebappClassLoader.class.cast(wm.getWebModuleConfig().getDeploymentContext().getClassLoader());
        return cl;
    }

}
