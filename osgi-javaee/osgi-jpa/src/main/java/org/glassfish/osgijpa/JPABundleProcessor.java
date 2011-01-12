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

package org.glassfish.osgijpa;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.glassfish.osgijpa.dd.PersistenceXMLReaderWriter;
import org.glassfish.osgijpa.dd.Persistence;
import org.glassfish.osgijavaeebase.BundleResource;
import org.glassfish.osgijavaeebase.OSGiBundleArchive;
import org.osgi.framework.BundleReference;

import java.io.Serializable;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
*/
class JPABundleProcessor implements Serializable // we write it to a file, so it must be serializable
{
    private static final Logger logger =
            Logger.getLogger(JPABundleProcessor.class.getPackage().getName());

    public static final String PXML_PATH = "META-INF/persistence.xml";

    private static final String ECLIPSELINK_JPA_PROVIDER =
            "org.eclipselink.jpa.PersistenceProvider";

    // A marker header to indicate that a bundle has been statically weaved
    // This is used to avoid updating infinitely
    public static final String STATICALLY_WEAVED = "GlassFish-StaticallyWeaved";

    private long bundleId; // store the id so that we don't have a hard reference to bundle

    private List<Persistence> persistenceXMLs;
    private static final long serialVersionUID = -1293408086392301220L;

    JPABundleProcessor(Bundle b)
    {
        this.bundleId = b.getBundleId();
    }

    boolean isJPABundle() {
        if (persistenceXMLs == null) {
            discoverPxmls();
        }
        return !persistenceXMLs.isEmpty();
    }

    void discoverPxmls() {
        assert(persistenceXMLs == null);
        persistenceXMLs = new ArrayList<Persistence>();
        if (isFragment()) return;
        for (BundleResource r : new OSGiBundleArchive(getBundle())) {
            if (PXML_PATH.equals(r.getPath())) {
                URL pxmlURL;
                try {
                    pxmlURL = r.getUri().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
                InputStream is = null;
                try {
                    is = pxmlURL.openStream();
                    Persistence persistence = new PersistenceXMLReaderWriter().read(is);
                    persistence.setUrl(pxmlURL);
                    persistence.setPURoot(r.getArchivePath());
                    persistenceXMLs.add(persistence);
                } catch (IOException ioe) {
                    logger.logp(Level.WARNING, "JPABundleProcessor", "discoverPxmls", "Exception occurred while processing " + pxmlURL, ioe);
                } finally {
                    if (is != null) try {is.close();} catch (IOException ioe) {}
                }
            }
        }
    }

    boolean validate(List<Persistence> persistenceList)
    {
        for (Persistence persistence : persistenceList) {
            for (Persistence.PersistenceUnit pu : persistence.getPersistenceUnit()) {
                if (pu.getProvider() == null) continue;
                if (ECLIPSELINK_JPA_PROVIDER.equals(pu.getProvider())) {
                    return false;
                } else {
                    logger.logp(Level.INFO, "JPABundleProcessor", "validate",
                            "{0} has a persistence-unit which does not use {1} as provider",
                            new Object[]{persistence, ECLIPSELINK_JPA_PROVIDER});
                }

            }
        }
        return true;
    }

    InputStream enhance() throws BundleException, IOException
    {
        JPAEnhancer enhancer = new EclipseLinkEnhancer();
        InputStream enhancedStream = enhancer.enhance(getBundle(), persistenceXMLs);
        return enhancedStream;
    }

    public boolean isEnhanced() {
        return getBundle().getHeaders().get(STATICALLY_WEAVED)!=null;
    }

    private boolean isFragment() {
        return getBundle().getHeaders().get(org.osgi.framework.Constants.FRAGMENT_HOST) != null;
    }

    public Bundle getBundle() {
        Bundle b = getBundleContext().getBundle(bundleId);
        if (b == null) {
            throw new RuntimeException("Bundle with id " + bundleId + " has already been uninstalled");
        }
        return b;
    }

    private BundleContext getBundleContext() {
        return BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
    }

    /* package */ long getBundleId() {
        return bundleId;
    }
}
