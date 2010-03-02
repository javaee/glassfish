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

import com.sun.enterprise.web.WebModuleDecorator;
import com.sun.enterprise.web.WebModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.glassfish.osgijavaeebase.BundleResource;

import javax.servlet.ServletContext;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.ArrayList;

/**
 * This class is responsible for setting
 * a) an attribute called {@link Constants.BUNDLE_CONTEXT_ATTR} in ServletContext of the web app
 * associated with the current OSGi bundle.
 * b) discovering JSF faces config resources and setting them in an attribute called
 * {@link Constants.FACES_CONFIG_ATTR}.
 * b) discovering JSF facelet config resources and setting them in an attribute called
 * {@link Constants.FACELET_CONFIG_ATTR}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWebModuleDecorator implements WebModuleDecorator
{
    private volatile OSGiWebContainer wc;

    public OSGiWebModuleDecorator(OSGiWebContainer wc)
    {
        this.wc = wc;
    }

    public void decorate(WebModule module)
    {
        if (wc != null) {
            BundleContext bctx = wc.getCurrentBundleContext();
            if (bctx != null) {
                final ServletContext sc = module.getServletContext();
                sc.setAttribute(Constants.BUNDLE_CONTEXT_ATTR, bctx);
                Collection<URL> facesConfigs = new ArrayList<URL>();
                Collection<URL> faceletConfigs = new ArrayList<URL>();
                discoverJSFConfigs(bctx.getBundle(), facesConfigs, faceletConfigs);
                sc.setAttribute(Constants.FACES_CONFIG_ATTR, facesConfigs);
                sc.setAttribute(Constants.FACELET_CONFIG_ATTR, faceletConfigs);
            }
        }
    }

    /* package */ void setWc(OSGiWebContainer wc)
    {
        this.wc = wc;
    }

    /**
     * This method discovers JSF resources packaged in a bundle.
     * It iterates over resources in the bundle classpath of the bundle. It is searching for two kinds of
     * resources: viz: faces configs and facelet configs.
     * Faces configs are identified by a file name faces-config.xml or a file ending with .faces-config.xml in META-INF/.
     * Facelet configs are identified by files in META-INF/ having suffix .taglib.xml. It returns the results in
     * the two collections passed to this method.
     */
    private void discoverJSFConfigs(Bundle b, Collection<URL> facesConfigs, Collection<URL> faceletConfigs) {
        OSGiBundleArchive archive = new OSGiBundleArchive(b);
        for (BundleResource r : archive) {
            final String path = r.getPath();
            if (path.startsWith("META-INF/")) {
                try {
                    final URL url = r.getUri().toURL();
                    if (path.endsWith(".taglib.xml")) {
                        faceletConfigs.add(url);
                    } else if ("META-INF/faces-config.xml".equals(path) || path.endsWith(".faces-config.xml")) {
                        facesConfigs.add(url);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace(); // ignore and continue
                }
            }
        }
    }

}
