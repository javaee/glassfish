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

package org.glassfish.osgijdbc;

import org.osgi.service.jdbc.DataSourceFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.osgi.framework.Constants.*;

public class JDBCJarManifestProcessor {
    private static Logger logger =
            Logger.getLogger(JDBCJarManifestProcessor.class.getPackage().getName());
    private static final String DEFAULT_MAN_VERSION = "2";
    private static final String DEFAULT_IMPORT_PACKAGE = "";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    public static final String OSGI_RFC_122="OSGI_RFC_122";

    /**
     * Reads content of the given URL, uses it to come up with a new Manifest.
     *
     * @param url URL which is used to read the original Manifest and other data
     * @return a new Manifest
     * @throws java.io.IOException
     */
    public static Manifest processManifest(URL url, ClassLoader cl) throws IOException {
        final JarInputStream jis = new JarInputStream(url.openStream());

        try {

            File file = new File(url.toURI());

            List<String> embeddedJars = getEmbeddedJarsList(file);
            StringBuffer bundleClassPath = deriveBundleClassPath(embeddedJars);
            
            JDBCDriverLoader loader = new JDBCDriverLoader(cl);
            Properties properties = loader.loadDriverInformation(file);

            Properties queryParams = readQueryParams(url);
            Manifest oldManifest = jis.getManifest();
            Manifest newManifest = new Manifest(oldManifest);
            Attributes attrs = newManifest.getMainAttributes();

            Set keys = properties.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                String value = (String) properties.get(key);
                key = key.replace('.', '_');
                attrs.putValue(key, value);
            }

            attrs.putValue((DataSourceFactory.OSGI_JDBC_DRIVER_CLASS.replace('.', '_')),
                    (String) properties.get(Constants.DRIVER));

            attrs.putValue(OSGI_RFC_122, "TRUE");

            process(queryParams, attrs, BUNDLE_MANIFESTVERSION,
                    DEFAULT_MAN_VERSION);

            String defaultSymName = properties.getProperty(Constants.DRIVER);
            process(queryParams, attrs, BUNDLE_SYMBOLICNAME,
                    defaultSymName);

            String version = oldManifest.getMainAttributes().getValue(IMPLEMENTATION_VERSION);
            if(isOSGiCompatibleVersion(version)){
                process(queryParams, attrs, BUNDLE_VERSION, version);
            }

            process(queryParams, attrs, BUNDLE_CLASSPATH, bundleClassPath.toString());

            //process(queryParams, attrs, IMPORT_PACKAGE, DEFAULT_IMPORT_PACKAGE);
            process(queryParams, attrs, EXPORT_PACKAGE, "*");

            // We add this attribute until we have added support for
            // scanning class bytes to figure out import dependencies.
            attrs.putValue(DYNAMICIMPORT_PACKAGE, "*");
            return newManifest;
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            jis.close();
        }
    }

    private static boolean isOSGiCompatibleVersion(String version) {
        boolean isCompatible = false;
        try{
            if(version != null){
                Double.parseDouble(version);
                isCompatible = true;
            }
        }catch(NumberFormatException nfe){
            if(logger.isLoggable(Level.FINEST)){
                logger.finest("Not a OSGi compatible bundle-version ["+version+"] : " + nfe);
            }
        }
        return isCompatible;
    }

    private static StringBuffer deriveBundleClassPath(List<String> embeddedJars) {
        StringBuffer bundleClasspath = new StringBuffer(".");
        for(int i = 0; i<embeddedJars.size() ; i++){
            bundleClasspath = bundleClasspath.append(",");
            bundleClasspath = bundleClasspath.append(embeddedJars.get(i));
        }
        return bundleClasspath;
    }

    private static List<String> getEmbeddedJarsList(File file) throws IOException {
        List<String> jarsList = new ArrayList<String>();
        JarFile f = new JarFile(file);
        Enumeration<JarEntry> entries =  f.entries();
        while(entries.hasMoreElements()){
            JarEntry entry = entries.nextElement();
            if(!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".jar")){
                jarsList.add(entry.getName());
            }
        }
        return jarsList;
    }

    private static Properties readQueryParams(URL url) {
        Properties queryParams = new Properties();
        String query = url.getQuery();
        if (query != null) {
            // "&" separates query paremeters
            StringTokenizer st = new StringTokenizer(query, "&");
            while (st.hasMoreTokens()) {
                String next = st.nextToken();
                int eq = next.indexOf("=");
                String name = next, value = null;
                if (eq != -1) {
                    name = next.substring(0, eq);
                    if ((eq + 1) < next.length()) {
                        value = next.substring(eq + 1);
                    }
                }
                queryParams.put(name, value);
            }
            logger.logp(Level.INFO, "JDBCJarManifestProcessor", "readQueryParams",
                    "queryParams = {0}", new Object[]{queryParams});
        }
        return queryParams;
    }

    private static void process(Properties deployerOptions,
                                Attributes developerOptions,
                                String key,
                                String defaultOption) {
        String deployerOption = deployerOptions.getProperty(key);
        String developerOption = developerOptions.getValue(key);
        String finalOption = defaultOption;
        if (deployerOption != null) {
            finalOption = deployerOption;
        } else if (developerOption != null) {
            finalOption = developerOption;
        }
        if (finalOption != developerOption) {
            developerOptions.putValue(key, finalOption);
        }
    }
}
