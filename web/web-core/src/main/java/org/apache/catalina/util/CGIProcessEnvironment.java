/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.util;

import java.lang.Process;
import java.io.File;
import java.io.Writer;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.Date;
import java.util.logging.*;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;



// import org.apache.catalina.util.StringManager;



/**
 * Encapsulates the CGI Process' environment and rules to derive
 * that environment from the servlet container and request information.
 * @author   Martin Dengler [root@martindengler.com]
 * @version  $Revision: 1.4 $, $Date: 2006/11/06 21:13:39 $
 * @since    Tomcat 4.0
 */

public class CGIProcessEnvironment extends ProcessEnvironment {


    private static Logger log = Logger.getLogger(CGIProcessEnvironment.class.getName());

    /** cgi command's query parameters */
    private Hashtable queryParameters = null;

    /**
     *  The CGI search path will start at
     *    webAppRootDir + File.separator + cgiPathPrefix
     *    (or webAppRootDir alone if cgiPathPrefix is
     *    null)
     */
    private String cgiPathPrefix = null;


    /**
     * Creates a ProcessEnvironment and derives the necessary environment,
     * working directory, command, etc.  The cgi path prefix is initialized
     * to "" (the empty string).
     *
     * @param  req       HttpServletRequest for information provided by
     *                   the Servlet API
     * @param  context   ServletContext for information provided by
     *                   the Servlet API
     */
    public CGIProcessEnvironment(HttpServletRequest req,
        ServletContext context) {
            this(req, context, "");
    }



    /**
     * Creates a ProcessEnvironment and derives the necessary environment,
     * working directory, command, etc.
     * @param req             HttpServletRequest for information provided by
     *                        the Servlet API
     * @param context         ServletContext for information provided by
     *                        the Servlet API
     * @param cgiPathPrefix   subdirectory of webAppRootDir below which the
     *                        web app's CGIs may be stored; can be null or "".
     */
    public CGIProcessEnvironment(HttpServletRequest req,
        ServletContext context, String cgiPathPrefix) {
            this(req, context, cgiPathPrefix, 0);
    }



    /**
     * Creates a ProcessEnvironment and derives the necessary environment,
     * working directory, command, etc.
     * @param req             HttpServletRequest for information provided by
     *                        the Servlet API
     * @param context         ServletContext for information provided by
     *                        the Servlet API
     * @param  debug          int debug level (0 == none, 6 == lots)
     */
    public CGIProcessEnvironment(HttpServletRequest req,
        ServletContext context, int debug) {
            this(req, context, "", 0);
    }




    /**
     * Creates a ProcessEnvironment and derives the necessary environment,
     * working directory, command, etc.
     * @param req             HttpServletRequest for information provided by
     *                        the Servlet API
     * @param context         ServletContext for information provided by
     *                        the Servlet API
     * @param cgiPathPrefix   subdirectory of webAppRootDir below which the
     *                        web app's CGIs may be stored; can be null or "".
     * @param  debug          int debug level (0 == none, 6 == lots)
     */
    public CGIProcessEnvironment(HttpServletRequest req,
        ServletContext context, String cgiPathPrefix, int debug) {
            super(req, context, debug);
            this.cgiPathPrefix = cgiPathPrefix;
            queryParameters = new Hashtable();
            Enumeration paramNames = req.getParameterNames();
            while (paramNames != null && paramNames.hasMoreElements()) {
                String param = paramNames.nextElement().toString();
                if (param != null) {
                    queryParameters.put(param,
                        URLEncoder.encode(req.getParameter(param)));
                }
            }
            this.valid = deriveProcessEnvironment(req);
    }



    /**
     * Constructs the CGI environment to be supplied to the invoked CGI
     * script; relies heavliy on Servlet API methods and findCGI
     * @param    HttpServletRequest request associated with the CGI invokation
     * @return   true if environment was set OK, false if there was a problem
     *           and no environment was set
     */
    protected boolean deriveProcessEnvironment(HttpServletRequest req) {
        /*
         * This method is slightly ugly; c'est la vie.
         * "You cannot stop [ugliness], you can only hope to contain [it]"
         * (apologies to Marv Albert regarding MJ)
         */

        Hashtable envp;
        super.deriveProcessEnvironment(req);
        envp = getEnvironment();

        String sPathInfoOrig = null;
        String sPathTranslatedOrig = null;
        String sPathInfoCGI = null;
        String sPathTranslatedCGI = null;
        String sCGIFullPath = null;
        String sCGIScriptName = null;
        String sCGIFullName = null;
        String sCGIName = null;
        String[] sCGINames;
        sPathInfoOrig = this.pathInfo;
        sPathInfoOrig = sPathInfoOrig == null ? "" : sPathInfoOrig;
        sPathTranslatedOrig = req.getPathTranslated();
        sPathTranslatedOrig = sPathTranslatedOrig == null ? "" :
            sPathTranslatedOrig;
            sCGINames =
                findCGI(sPathInfoOrig, getWebAppRootDir(), getContextPath(),
                getServletPath(), cgiPathPrefix);
        sCGIFullPath = sCGINames[0];
        sCGIScriptName = sCGINames[1];
        sCGIFullName = sCGINames[2];
        sCGIName = sCGINames[3];
        if (sCGIFullPath == null || sCGIScriptName == null
            || sCGIFullName == null || sCGIName == null) {
                return false;
        }
        envp.put("SERVER_SOFTWARE", "TOMCAT");
        envp.put("SERVER_NAME", nullsToBlanks(req.getServerName()));
        envp.put("GATEWAY_INTERFACE", "CGI/1.1");
        envp.put("SERVER_PROTOCOL", nullsToBlanks(req.getProtocol()));
        int port = req.getServerPort();
        Integer iPort = (port == 0 ? Integer.valueOf(-1) : Integer.valueOf(port));
        envp.put("SERVER_PORT", iPort.toString());
        envp.put("REQUEST_METHOD", nullsToBlanks(req.getMethod()));

        /*-
        * PATH_INFO should be determined by using sCGIFullName:
        * 1) Let sCGIFullName not end in a "/" (see method findCGI)
        * 2) Let sCGIFullName equal the pathInfo fragment which
        *    corresponds to the actual cgi script.
        * 3) Thus, PATH_INFO = request.getPathInfo().substring(
        *                      sCGIFullName.length())
        *
        * (see method findCGI, where the real work is done)
        *
        */

        if (pathInfo == null ||
            (pathInfo.substring(sCGIFullName.length()).length() <= 0)) {
                sPathInfoCGI = "";
        } else {
            sPathInfoCGI = pathInfo.substring(sCGIFullName.length());
        }
        envp.put("PATH_INFO", sPathInfoCGI);

        /*-
        * PATH_TRANSLATED must be determined after PATH_INFO (and the
        * implied real cgi-script) has been taken into account.
        *
        * The following example demonstrates:
        *
        * servlet info   = /servlet/cgigw/dir1/dir2/cgi1/trans1/trans2
        * cgifullpath    = /servlet/cgigw/dir1/dir2/cgi1
        * path_info      = /trans1/trans2
        * webAppRootDir  = servletContext.getRealPath("/")
        *
        * path_translated = servletContext.getRealPath("/trans1/trans2")
        *
        * That is, PATH_TRANSLATED = webAppRootDir + sPathInfoCGI
        * (unless sPathInfoCGI is null or blank, then the CGI
        * specification dictates that the PATH_TRANSLATED metavariable
        * SHOULD NOT be defined.
        *
        */

        if (sPathInfoCGI != null && !("".equals(sPathInfoCGI))) {
            sPathTranslatedCGI = getContext().getRealPath(sPathInfoCGI);
        } else {
            sPathTranslatedCGI = null;
        }
        if (sPathTranslatedCGI == null || "".equals(sPathTranslatedCGI)) {
            //NOOP
        } else {
            envp.put("PATH_TRANSLATED", nullsToBlanks(sPathTranslatedCGI));
        }
        envp.put("SCRIPT_NAME", nullsToBlanks(sCGIScriptName));
        envp.put("QUERY_STRING", nullsToBlanks(req.getQueryString()));
        envp.put("REMOTE_HOST", nullsToBlanks(req.getRemoteHost()));
        envp.put("REMOTE_ADDR", nullsToBlanks(req.getRemoteAddr()));
        envp.put("AUTH_TYPE", nullsToBlanks(req.getAuthType()));
        envp.put("REMOTE_USER", nullsToBlanks(req.getRemoteUser()));
        envp.put("REMOTE_IDENT", ""); //not necessary for full compliance
        envp.put("CONTENT_TYPE", nullsToBlanks(req.getContentType()));

        /* Note CGI spec says CONTENT_LENGTH must be NULL ("") or undefined
        * if there is no content, so we cannot put 0 or -1 in as per the
        * Servlet API spec.
        */

        int contentLength = req.getContentLength();
        String sContentLength = (contentLength <= 0 ? "" : (Integer.valueOf(contentLength)).toString());
        envp.put("CONTENT_LENGTH", sContentLength);
        Enumeration<String> headers = req.getHeaderNames();
        String header = null;
        while (headers.hasMoreElements()) {
            header = null;
            header = (headers.nextElement()).toUpperCase();
            //REMIND: rewrite multiple headers as if received as single
            //REMIND: change character set
            //REMIND: I forgot what the previous REMIND means
            if ("AUTHORIZATION".equalsIgnoreCase(header)
                || "PROXY_AUTHORIZATION".equalsIgnoreCase(header)) {
                    //NOOP per CGI specification section 11.2
            } else if ("HOST".equalsIgnoreCase(header)) {
                String host = req.getHeader(header);
                envp.put("HTTP_" + header.replace('-', '_'),
                    host.substring(0, host.indexOf(":")));
            } else {
                envp.put("HTTP_" + header.replace('-', '_'),
                    req.getHeader(header));
            }
        }
        command = sCGIFullPath;
        workingDirectory = new File(command.substring(0,
            command.lastIndexOf(File.separator)));
        envp.put("X_TOMCAT_COMMAND_PATH", command); //for kicks
        this.setEnvironment(envp);
        return true;
    }


    /**
     * Resolves core information about the cgi script. <p> Example URI:
     * <PRE> /servlet/cgigateway/dir1/realCGIscript/pathinfo1 </PRE> <ul>
     * <LI><b>path</b> = $CATALINA_HOME/mywebapp/dir1/realCGIscript
     * <LI><b>scriptName</b> = /servlet/cgigateway/dir1/realCGIscript</LI>
     * <LI><b>cgiName</b> = /dir1/realCGIscript
     * <LI><b>name</b> = realCGIscript
     * </ul>
     * </p>
     * <p>
     * CGI search algorithm: search the real path below
     * &lt;my-webapp-root&gt; and find the first non-directory in
     * the getPathTranslated("/"), reading/searching from left-to-right.
     * </p>
     * <p>
     * The CGI search path will start at
     * webAppRootDir + File.separator + cgiPathPrefix (or webAppRootDir
     * alone if cgiPathPrefix is null).
     * </p>
     * <p>
     * cgiPathPrefix is usually set by the calling servlet to the servlet's
     * cgiPathPrefix init parameter
     * </p>
     *
     * @param pathInfo       String from HttpServletRequest.getPathInfo()
     * @param webAppRootDir  String from context.getRealPath("/")
     * @param contextPath    String as from HttpServletRequest.getContextPath()
     * @param servletPath    String as from HttpServletRequest.getServletPath()
     * @param cgiPathPrefix  subdirectory of webAppRootDir below which the
     *                       web app's CGIs may be stored; can be null.
     * @return
     * <ul> <li> <code>path</code>  -    full file-system path to valid cgi
     *                                   script, or null if no cgi was found
     * <li> <code>scriptName</code> -    CGI variable SCRIPT_NAME; the full
     *                                   URL path to valid cgi script or
     *                                   null if no cgi was found
     * <li> <code>cgiName</code>    -    servlet pathInfo fragment
     *                                   corresponding to the cgi script
     *                                   itself, or null if not found
     * <li> <code>name</code>       -    simple name (no directories) of
     *                                   the cgi script, or null if no cgi
     *                                   was found
     * </ul>
     * @author Martin Dengler [root@martindengler.com]
     * @since Tomcat 4.0
     */
    protected String[] findCGI(String pathInfo, String webAppRootDir,
        String contextPath, String servletPath, String cgiPathPrefix) {
            String path = null;
            String name = null;
            String scriptname = null;
            String cginame = null;
            if ((webAppRootDir != null)
                && (webAppRootDir.lastIndexOf("/")
                == (webAppRootDir.length() - 1))) {
                    //strip the trailing "/" from the webAppRootDir
                    webAppRootDir =
                        webAppRootDir.substring(0,
                        (webAppRootDir.length() - 1));
            }
            if (cgiPathPrefix != null) {
                webAppRootDir = webAppRootDir + File.separator
                    + cgiPathPrefix;
            }
            if (debug >= 2) {
                log("findCGI: start = [" + webAppRootDir
                    + "], pathInfo = [" + pathInfo + "] ");
            }
            File currentLocation = new File(webAppRootDir);
            StringTokenizer dirWalker = new StringTokenizer(pathInfo, "/");
            while (!currentLocation.isFile() && dirWalker.hasMoreElements()) {
                currentLocation = new
                    File(currentLocation, (String) dirWalker.nextElement());
                if (debug >= 3) {
                    log("findCGI: traversing to [" + currentLocation + "]");
                }
            }
            if (!currentLocation.isFile()) {
                return new String[] { null, null, null, null };
            } else {
                if (debug >= 2) {
                    log("findCGI: FOUND cgi at [" + currentLocation + "]");
                }
                path = currentLocation.getAbsolutePath();
                name = currentLocation.getName();
                cginame = currentLocation.getParent()
                    .substring(webAppRootDir.length())
                    + File.separator + name;
                    if (".".equals(contextPath)) {
                        scriptname = servletPath + cginame;
                } else {
                    scriptname = contextPath + servletPath + cginame;
                }
            }
            if (debug >= 1) {
                log("findCGI calc: name=" + name + ", path=" + path
                    + ", scriptname=" + scriptname + ", cginame=" + cginame);
            }
            return new String[] { path, scriptname, cginame, name };
    }


    /**
     * Print important CGI environment information in an
     * easy-to-read HTML table
     * @return  HTML string containing CGI environment info
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<TABLE border=2>");
        sb.append("<tr><th colspan=2 bgcolor=grey>");
        sb.append("ProcessEnvironment Info</th></tr>");
        sb.append("<tr><td>Debug Level</td><td>");
        sb.append(debug);
        sb.append("</td></tr>");
        sb.append("<tr><td>Validity:</td><td>");
        sb.append(isValid());
        sb.append("</td></tr>");
        if (isValid()) {
            Enumeration envk = env.keys();
            while (envk.hasMoreElements()) {
                String s = (String)envk.nextElement();
                sb.append("<tr><td>");
                sb.append(s);
                sb.append("</td><td>");
                sb.append(blanksToString((String)env.get(s),
                    "[will be set to blank]"));
                    sb.append("</td></tr>");
            }
        }
        sb.append("<tr><td colspan=2><HR></td></tr>");
        sb.append("<tr><td>Derived Command</td><td>");
        sb.append(nullsToBlanks(command));
        sb.append("</td></tr>");
        sb.append("<tr><td>Working Directory</td><td>");
        if (workingDirectory != null) {
            sb.append(workingDirectory.toString());
        }
        sb.append("</td></tr>");
        sb.append("<tr><td colspan=2>Query Params</td></tr>");
        Enumeration paramk = queryParameters.keys();
        while (paramk.hasMoreElements()) {
            String s = paramk.nextElement().toString();
            sb.append("<tr><td>");
            sb.append(s);
            sb.append("</td><td>");
            sb.append(queryParameters.get(s).toString());
            sb.append("</td></tr>");
        }

        sb.append("</TABLE><p>end.");
        return sb.toString();
    }


    /**
     * Gets process' derived query parameters
     * @return   process' query parameters
     */
    public Hashtable getParameters() {
        return queryParameters;
    }

}
