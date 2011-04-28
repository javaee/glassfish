/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.osgijavaeebase.JarHelper;
import org.osgi.service.url.AbstractURLStreamHandlerService;

import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JDBCDriverURLStreamHandlerService extends AbstractURLStreamHandlerService {

    private static final Logger logger = Logger.getLogger(
            JDBCDriverURLStreamHandlerService.class.getPackage().getName());

    private ClassLoader apiClassLoader;

    public JDBCDriverURLStreamHandlerService(ClassLoader apiClassLoader){
        this.apiClassLoader = apiClassLoader;
    }

    public URLConnection openConnection(URL u) throws IOException {
        assert (Constants.JDBC_DRIVER_SCHEME.equals(u.getProtocol()));
        try {
            debug("jdbc driver openConnection()");
            //final URL[] urls = getURLs(u);
            //final URL uberJarURL = getUberJarURL(urls);
            //final URLConnection con = uberJarURL.openConnection();
            //return new URLConnection(uberJarURL) {


            URI embeddedURI = new URI(u.toURI().getSchemeSpecificPart());
            final URL embeddedURL = embeddedURI.toURL();
            final URLConnection con = embeddedURL.openConnection();
            final URLClassLoader cl = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new URLClassLoader(new URL[]{embeddedURL}, apiClassLoader);
                }
            });
            return new URLConnection(embeddedURL) {
                private Manifest m;

                public void connect() throws IOException {
                    con.connect();
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    connect();
                    m = JDBCJarManifestProcessor.processManifest(url, cl);
                    final PipedOutputStream pos = new PipedOutputStream();
                    final PipedInputStream pis = new PipedInputStream(pos);

                    // It is a common practice to spawn a separate thread
                    // to write to PipedOutputStream so that the reader
                    // and writer are not deadlocked.
                    new Thread() {
                        @Override
                        public void run() {
                            JarHelper.write(con, pos, m);
                        }
                    }.start();

                    return pis;
                }
            };
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setURL(URL u, String protocol, String host, int port, String auth, String user,
                          String path, String query, String ref) {
        super.setURL(u, protocol, host, port, auth, user, path, query, ref);
        debug("jdbc driver setURL()");
    }

    private void debug(String s) {
        if(logger.isLoggable(Level.FINEST)){
            logger.finest("[osgi-jdbc] : " + s);
        }
    }

/*
    private URL getUberJarURL(URL[] urls) throws IOException, URISyntaxException {

        if (urls.length == 1) {
            URL u = urls[0];
            URI embeddedURI = new URI(u.toURI().getSchemeSpecificPart());
            return embeddedURI.toURL();
        }

        if (urls.length > 1) {
            File file = new File(new URI(urls[0].toURI().getSchemeSpecificPart()));
            JarFile jar = new JarFile(file);
            File uberFile = File.createTempFile("jdbc-driver-", ".jar");
            uberFile.deleteOnExit();

            try {
                FileOutputStream fos = new FileOutputStream(uberFile);
                JarOutputStream uberJarOS = new JarOutputStream(fos);

                byte buffer[] = new byte[1024];
                int bytesRead;

                try {
                    Enumeration entries = jar.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        InputStream is = jar.getInputStream(entry);
                        uberJarOS.putNextEntry(entry);
                        while ((bytesRead = is.read(buffer)) != -1) {
                            uberJarOS.write(buffer, 0, bytesRead);
                        }
                    }

                    // Add new file(s) to the end
                    for (int i = 1; i < urls.length; i++) {
                        URL url = urls[i];
                        File f = new File(url.toURI());
                        FileInputStream fis = new FileInputStream(f);

                        try {
                            JarEntry entry = new JarEntry(f.getName());
                            uberJarOS.putNextEntry(entry);

                            while ((bytesRead = fis.read(buffer)) != -1) {
                                uberJarOS.write(buffer, 0, bytesRead);
                            }
                        } finally {
                            fis.close();
                        }
                    }
                } catch (IOException ex) {
                    System.err.println
                            ("Operation aborted due to : " + ex);
                } finally {
                    try {
                        uberJarOS.close();
                    } catch (IOException ioe) {
                    }
                }
            } catch (IOException ex) {
                System.err.println(
                        "Can't access new file : " + ex);
            } finally {
                try {
                    jar.close();
                } catch (IOException ioe) {
                }

            }
            return uberFile.toURI().toURL();
        }
        throw new IllegalStateException("Atleast one jar file need to be specified");
    }



    private URL[] getURLs(URL u) throws MalformedURLException {
        String urls = u.toString();
        StringTokenizer tokenizer = new StringTokenizer(urls, ",");
        ArrayList<URL> urlList = new ArrayList<URL>();
        while (tokenizer.hasMoreElements()) {
            String s = (String) tokenizer.nextElement();
            URL url = new URL(s);
            urlList.add(url);
        }
        return urlList.toArray(new URL[urlList.size()]);
    }
*/

}
