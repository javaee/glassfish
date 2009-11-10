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

import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiResourceServlet extends HttpServlet {

    private String alias;
    private String name;
    private HttpContext httpContext;

    public OSGiResourceServlet(String alias, String name, HttpContext httpContext) {
        this.alias = alias;
        this.name = name;
        this.httpContext = httpContext;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String resPath = getResourcePath(req);
        URL url = httpContext.getResource(resPath);
        if (url == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // contentType must be set before writing anything to the stream
        // as for long data,m stream gets flushed before we have finished
        // writing everything.
        String mimeType = httpContext.getMimeType(resPath);
        if (mimeType == null) {
            mimeType = getServletConfig().getServletContext().getMimeType(resPath);
        }
        resp.setContentType(mimeType);
        URLConnection conn = url.openConnection();
        int writeCount = writeToStream(conn, resp.getOutputStream());
        resp.setContentLength(writeCount);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private String getResourcePath(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        assert(servletPath == alias);
        String contextPath = req.getContextPath();
        final String requestURI;
        try {
            requestURI = new URI(req.getRequestURI()).normalize().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
        String requestedPath = requestURI.substring(contextPath.length());
        StringBuilder mappedPath = new StringBuilder(requestedPath);
        String internalName = name == "/" ? "" : name;
        mappedPath.replace(0, servletPath.length(), internalName);
        System.out.println("Mapped [" + requestedPath + "] to [" + mappedPath + "]");
        return mappedPath.toString();
    }

    private int writeToStream(URLConnection connection, OutputStream os) throws IOException {
        InputStream is = connection.getInputStream();
        try {
            byte[] buf = new byte[8192];
            int readCount = is.read(buf);
            int writeCount = 0;
            while (readCount!=-1) {
                os.write(buf, 0, readCount);
                writeCount += readCount;
                readCount = is.read(buf);
            }
            return writeCount;
        } finally {
            if (is != null) is.close();
        }
    }
}
