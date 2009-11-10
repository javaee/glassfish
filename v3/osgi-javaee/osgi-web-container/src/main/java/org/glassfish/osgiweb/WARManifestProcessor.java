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

import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When a deployer installs a bundle with
 * {@link Constants#WEB_BUNDLE_SCHEME},
 * our registered handler gets a chance to look at the stream and process the
 * MANIFEST.MF. It adds necessary OSGi metadata as specified in section #5.2.1.2
 * of RFC #66. It uses the following information during computation:
 * - WAR manifest entries, i.e., developer supplied data
 * - Properties supplied via URL query parameters
 * - Other information present in the WAR, e.g., existence of any jar in
 * WEB-INF/lib causes that jar to be added as Bundle-ClassPath.
 * For exact details, refer to the spec.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WARManifestProcessor
{
    private static Logger logger =
            Logger.getLogger(WARManifestProcessor.class.getPackage().getName());
    private static final String DEFAULT_MAN_VERSION = "2";
    private static final String DEFAULT_IMPORT_PACKAGE =
            "javax.servlet; javax.servlet.http; version=2.5, " +
                    "javax.servlet.jsp; javax.servlet.jsp.tagext;" +
                    "javax.el; javax.servlet.jsp.el; version=2.1";
    // We always add WEB-INF/classes/, because not adding has the adverse
    // side effect of Bundle-ClassPath defaulting to "." by framework
    // in case there is no lib jar.
    private static final String DEFAULT_BUNDLE_CP = "WEB-INF/classes/";

    static AtomicInteger id = new AtomicInteger();

    /**
     * Reads content of the given URL, uses it to come up with a new Manifest.
     *
     * @param url URL which is used to read the original Manifest and other data
     * @return a new Manifest
     * @throws java.io.IOException
     */
    public static Manifest processManifest(URL url) throws IOException
    {
        final JarInputStream jis = new JarInputStream(url.openStream());
        try
        {
            final List<String> libs = new ArrayList<String>();

            // It is a StringBuilder, as a String is immutable.
            final StringBuilder contextRootFromSunDD = new StringBuilder();
            JarHelper.accept(jis, new JarHelper.Visitor()
            {
                public void visit(JarEntry je)
                {
                    String name = je.getName();
                    String LIB_DIR = "WEB-INF/lib/";
                    String JAR_EXT = ".jar";
                    if (!je.isDirectory() && name.startsWith(LIB_DIR) &&
                            name.endsWith(JAR_EXT))
                    {
                        String jarName = name.substring(LIB_DIR.length());
                        if (!jarName.contains("/"))
                        {
                            // only jar files directly in lib dir are considered
                            // as library jars.
                            libs.add(name);
                        }
                    }
                    String SUN_WEB_XML = "WEB-INF/sun-web.xml";
                    if (!je.isDirectory() && je.getName().equals(SUN_WEB_XML))
                    {
                        String contextRoot1 = parseContextRoot(jis);
                        if (contextRoot1 != null)
                        {
                            contextRootFromSunDD.append(contextRoot1);
                        }
                    }
                }
            });
            Properties queryParams = readQueryParams(url);
            Manifest oldManifest = jis.getManifest();
            Manifest newManifest = new Manifest(oldManifest);
            Attributes attrs = newManifest.getMainAttributes();
            String defaultContextRoot = contextRootFromSunDD.length() == 0 ?
                    generateContextRoot(url) : contextRootFromSunDD.toString();
            process(queryParams, attrs, Constants.WEB_CONTEXT_PATH, defaultContextRoot);
            process(queryParams, attrs, BUNDLE_MANIFESTVERSION,
                    DEFAULT_MAN_VERSION);

            // We derive the symbolic name from context root.
            // We exclude the leading '/' while using contextroot as name
            String actualContextRoot = attrs.getValue(Constants.WEB_CONTEXT_PATH);
            String defaultSymName = actualContextRoot.startsWith("/") ?
                    actualContextRoot.substring(1) : actualContextRoot;
            process(queryParams, attrs, BUNDLE_SYMBOLICNAME,
                    defaultSymName);
            process(queryParams, attrs, BUNDLE_VERSION, null);
            String cp = convertToCP(libs);
            cp = cp.length() > 0 ?
                    DEFAULT_BUNDLE_CP.concat(",").concat(cp) : DEFAULT_BUNDLE_CP;
            process(queryParams, attrs, BUNDLE_CLASSPATH, cp);

            process(queryParams, attrs, Constants.WEB_JSP_EXTRACT_LOCATION, null);
            process(queryParams, attrs, IMPORT_PACKAGE, DEFAULT_IMPORT_PACKAGE);
            process(queryParams, attrs, EXPORT_PACKAGE, null);

            // We add this attribute until we have added support for
            // scanning class bytes to figure out import dependencies.
            attrs.putValue(DYNAMICIMPORT_PACKAGE, "*");
            return newManifest;
        }
        finally
        {
            jis.close();
        }
    }

    private static String generateContextRoot(URL url)
    {
        String contextRoot;
        // take the last part of path as context root.
        String file = url.getPath(); // getPath() excludes query unlike getFile()

        // If it is a directory url, then remove the last /, so that we
        // can get the last component of the path.
        if (file.endsWith("/"))
        {
            file = file.substring(0, file.length());
        }
        int slash = file.lastIndexOf("/");
        int dot = file.lastIndexOf(".");
        if (slash != -1)
        {
            contextRoot = dot != -1 && dot > slash ?
                    file.substring(slash, dot) :
                    file.substring(slash);
        }
        else
        {
            contextRoot = "/osgiwar" + id.getAndIncrement();
        }
        return contextRoot;
    }

    private static String parseContextRoot(JarInputStream jis)
    {
        try
        {
            SunWebXmlParser parser = new SunWebXmlParser(jis);
            return parser.getContextRoot();
        }
        catch (Exception e)
        {
            logger.logp(Level.WARNING, "WARManifestProcessor", "getContextRoot",
                    "e = {0}", new Object[]{e});
        }
        return null;
    }

    private static String convertToCP(List<String> jars)
    {
        StringBuilder cp = new StringBuilder();
        for (int i = 0; i < jars.size(); ++i)
        {
            cp.append(jars.get(i));
            if (i < jars.size() - 1)
            {
                cp.append(",");
            }
        }
        return cp.toString();
    }

    private static Properties readQueryParams(URL url)
    {
        Properties queryParams = new Properties();
        String query = url.getQuery();
        if (query != null)
        {
            // "&" separates query paremeters
            StringTokenizer st = new StringTokenizer(query, "&");
            while (st.hasMoreTokens())
            {
                String next = st.nextToken();
                int eq = next.indexOf("=");
                String name = next, value = null;
                if (eq != -1)
                {
                    name = next.substring(0, eq);
                    if ((eq + 1) < next.length())
                    {
                        value = next.substring(eq + 1);
                    }
                }
                queryParams.put(name, value);
            }
            logger.logp(Level.INFO, "WARManifestProcessor", "readQueryParams",
                    "queryParams = {0}", new Object[]{queryParams});
        }
        return queryParams;
    }

    private static void process(Properties deployerOptions,
                                Attributes developerOptions,
                                String key,
                                String defaultOption)
    {
        String deployerOption = deployerOptions.getProperty(key);
        String developerOption = developerOptions.getValue(key);
        String finalOption = defaultOption;
        if (deployerOption != null)
        {
            finalOption = deployerOption;
        }
        else if (developerOption != null)
        {
            finalOption = developerOption;
        }
        if (finalOption != developerOption)
        {
            developerOptions.putValue(key, finalOption);
        }
    }

}
