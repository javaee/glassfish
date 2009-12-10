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


package org.glassfish.osgijpa;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import org.glassfish.osgijpa.dd.PersistenceXMLParser;
import org.glassfish.osgijpa.dd.Persistence;
import org.glassfish.osgijpa.dd.PersistenceUnit;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
*/
class JPABundleProcessor
{
    private static final Logger logger =
            Logger.getLogger(JPABundleProcessor.class.getPackage().getName());

    public static final String PXML_PATH = "META-INF/persistence.xml";

    private static final String ECLIPSELINK_JPA_PROVIDER =
            "org.eclipselink.jpa.PersistenceProvider";

    // A marker header to indicate that a bundle has been statically weaved
    // This is used to avoid updating infinitely
    public static final String STATICALLY_WEAVED = "GlassFish-StaticallyWeaved";

    private Bundle b;

    private List<URL> pxmlURLs;

    /**
     * A PURoot specifies the relative path from the root of the bundle to
     * to the root of this persistence unit. e.g.
     * WEB-INF/classes -- if persistence.xml is in WEB-INF/classes/META-INF,
     * WEB-INF/lib/foo.jar -- if persistence.xml is in WEB-INF/lib/foo.jar/META-INF,
     * "" -- if persistence.xml is in META-INF directory of the bundle,
     * util/bar.jar -- if persistence.xml is in bundle.jar/util/bar.jar
     */
    private List<String> puRoots = new ArrayList<String>();

    JPABundleProcessor(Bundle b)
    {
        this.b = b;
    }

    boolean isJPABundle() {
        if (pxmlURLs == null) {
            discoverPxmls();
        }
        return !pxmlURLs.isEmpty();
    }

    void discoverPxmls() {
        assert(pxmlURLs == null);
        pxmlURLs = new ArrayList<URL>();
        String bcp = String.class.cast(b.getHeaders().get(BUNDLE_CLASSPATH));
        if (bcp == null || bcp.isEmpty()) {
            bcp = "."; // this is the default
        }
        //Bundle-ClassPath entries are separated by ; or ,
        StringTokenizer entries = new StringTokenizer(bcp, ",;");
        String entry;
        while (entries.hasMoreTokens()) {
            entry = entries.nextToken().trim();
            if (entry.startsWith("/")) entry = entry.substring(1);
            // We need to prefix "/" while calling bundle.getEntry() because Felix does not like something like
            // ./META-INF/persistence.xml.
            URL entryURL = b.getEntry(URI.create("/" + entry).normalize().toString());
            if (entryURL != null) {
                URL pxmlURL = null;
                try {
                    pxmlURL = new URL(entryURL, PXML_PATH);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
                try {
                    InputStream is = pxmlURL.openStream();
                    is.close();
                    logger.logp(Level.INFO, "JPABundleProcessor", "discoverPxmls",
                            "pxmlURL = {0}", new Object[]{pxmlURL});
                    pxmlURLs.add(pxmlURL);
                    puRoots.add(entry);
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    void processPXmls() {
        List<Persistence> persistenceList = new ArrayList<Persistence>();
        for (URL url : pxmlURLs) {
            Persistence persistence = PersistenceXMLParser.parse(url);
            persistenceList.add(persistence);
        }
    }

    boolean validate(List<Persistence> persistenceList)
    {
        for (Persistence persistence : persistenceList) {
            for (PersistenceUnit pu : persistence.getPUs()) {
                if (pu.provider != null && !pu.provider.equals(ECLIPSELINK_JPA_PROVIDER)) {
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

    void enhance() throws BundleException, IOException
    {
        JPAEnhancer enhancer = new EclipseLinkEnhancer();
        InputStream enhancedStream = enhancer.enhance(b, puRoots);
        b.update(enhancedStream);
    }

    public boolean isEnhanced(Bundle b) {
        return b.getHeaders().get(STATICALLY_WEAVED)!=null;
    }
}
