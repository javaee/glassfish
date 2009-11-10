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

import org.osgi.service.url.AbstractURLStreamHandlerService;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.glassfish.osgiweb.Constants.WEB_CONTEXT_PATH;
import static org.glassfish.osgiweb.Constants.WEB_JSP_EXTRACT_LOCATION;
import static org.glassfish.osgiweb.Constants.WEB_BUNDLE_SCHEME;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

/**
 * A {@link org.osgi.service.url.URLStreamHandlerService} for webbundle scheme.
 * It is responsible for not only adding necessary OSGi headers to transform a plain
 * vanilla web app to a Web App Bundle (WAB), but also setting appropriate
 * parameters in the URL object to meet spec's requirement as described below:
 *
 * The java.net.URL object for a webbundle URL must return the String webbundle
 * when the getProtocol method is called. The embedded URL must be returned in
 * full from the getPath method. The parameters for processing manifest must
 * be returned from the getQuery() method.
 *
 * Some form of embedded URL also contain query parameters and this must be
 * supported. Thus the value returned from getPath may contain a URL query.
 * Any implementation must take care to preserve both the query parameters for
 * the embedded URL as well as the webbundle URL. The following example shows
 * an HTTP URL with some query parameter:
 *
 *      webbundle:https://localhost:1234/some/path/?war=example.war?Bundle-SymbolicName=com.example
 *
 * In this case getPath method of the webbundle URL must return:
 *      https://localhost:1234/some/path/?war=example.war
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class WebBundleURLStreamHandlerService
        extends AbstractURLStreamHandlerService
{
    private static final Logger logger = Logger.getLogger(
            WebBundleURLStreamHandlerService.class.getPackage().getName());

    /**
     * These are the query parameters that are understood by this stream handler
     * and they play a role in determining if a query should be part of
     * embedded URL or not.
     * @see #setURL(java.net.URL, String, String, int, String, String, String, String, String)
     */
    private static String[] supportedQueryParamNames = {
        BUNDLE_SYMBOLICNAME,
            BUNDLE_VERSION,
            BUNDLE_MANIFESTVERSION,
            IMPORT_PACKAGE,
            EXPORT_PACKAGE,
            WEB_CONTEXT_PATH,
            WEB_JSP_EXTRACT_LOCATION
    };
    public URLConnection openConnection(URL u) throws IOException
    {
        assert (WEB_BUNDLE_SCHEME.equals(u.getProtocol()));
        try
        {
            URI embeddedURI = new URI(u.toURI().getSchemeSpecificPart());
            final URL embeddedURL = embeddedURI.toURL();
            final URLConnection con = embeddedURL.openConnection();
            return new URLConnection(embeddedURL)
            {
                private Manifest m;

                public void connect() throws IOException
                {
                    con.connect();
                }

                @Override
                public InputStream getInputStream() throws IOException
                {
                    connect();
                    m = WARManifestProcessor.processManifest(embeddedURL);
                    final PipedOutputStream pos = new PipedOutputStream();
                    final PipedInputStream pis = new PipedInputStream(pos);

                    // It is a common practice to spawn a separate thread
                    // to write to PipedOutputStream so that the reader
                    // and writer are not deadlocked.
                    new Thread()
                    {
                        @Override
                        public void run()
                        {
                            JarHelper.write(con, pos, m);
                        }
                    }.start();
                    return pis;
                }
            };
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setURL(URL u,
                          String proto,
                          String host,
                          int port,
                          String auth,
                          String user,
                          String path,
                          String query,
                          String ref)
    {
        logger.logp(Level.INFO, "WebBundleURLStreamHandlerService",
                "setURL() called with",
                "u = [{0}], proto = [{1}], host = [{2}], port = [{3}], " +
                        "auth = [{4}], user = [{5}], path = [{6}], " +
                        "query = [{7}], ref = [{7}]",
                new Object[]{u, proto, host, port, auth, user, path, query, ref});
       /*
        * Some form of embedded URL also contain query parameters and this must be
        * supported. Thus the value returned from getPath may contain a URL query.
        * Any implementation must take care to preserve both the query parameters for
        * the embedded URL as well as the webbundle URL. The following example shows
        * an HTTP URL with some query parameter:
        *
        *      webbundle:https://localhost:1234/some/path/?war=example.war?Bundle-SymbolicName=com.example
        *
        * In this case getPath method of the webbundle URL must return:
        *      https://localhost:1234/some/path/?war=example.war
        */

        if (query != null) {
            // Let's see if there are two parts in the query.
            int sep = query.indexOf("?");
            if (sep != -1) {
                String query1 = query.substring(0, sep);
                String query2 = (query.length() > sep +1) ?
                        query.substring(sep+1) : null;
                if (query2 != null) {
                    path = path.concat("?").concat(query1);
                    query = query2;
                }
            } else {
                // There is a single query. Let's see if this begins
                // with supported query params. If it does not, then
                // treat this as embedded URL's query and hence add it to path.
                int eq = query.indexOf("=");
                String firstQueryParam = eq != -1 ?
                        query.substring(0, eq) : query;
                if (!Arrays.asList(supportedQueryParamNames).contains(firstQueryParam)) {
                    path = path.concat("?").concat(query);
                    query = null;
                }
            }
            logger.logp(Level.INFO, "WebBundleURLStreamHandlerService", "setURL ",
                    "new path = [{0}], new query = [{1}]",
                    new Object[]{path, query});
        }
        super.setURL(u, proto, host, port, auth, user, path, query, ref);
    }
}
