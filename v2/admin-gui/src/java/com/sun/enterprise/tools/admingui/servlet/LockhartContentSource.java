/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.tools.admingui.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

//import java.io.*;
import java.util.StringTokenizer;

//import javax.servlet.*;
//import javax.servlet.http.*;

import org.apache.jasper.compiler.JspUtil;


/**
 *
 */
public class LockhartContentSource implements DownloadServlet.ContentSource {

    /**
     *	<p> This method returns a unique string used to identify this
     *	    {@link DownloadServlet#ContentSource}.  This string must be
     *	    specified in order to select the appropriate
     *	    {@link DownloadServlet#ContentSource} when using the
     *	    {@link DownloadServlet}.</p>
     */
    public String getId() {
	return "Lockhart";					// NOI18N
    }

    /**
     *  <p> This method is responsible for generating the content and
     *	    returning an InputStream to that content.  It is also
     *	    responsible for setting any attribute values in the
     *	    {@link DownloadServlet#Context}, such as
     *	    {@link DownloadServlet#EXTENSION} or
     *	    {@link DownloadServlet#CONTENT_TYPE}.</p>
     */
    public InputStream getInputStream(DownloadServlet.Context ctx) {
	HttpServletRequest request =
	    (HttpServletRequest) ctx.getServletRequest();
//This block not needed any more after moving to jsftemplating
//Remove this, and serveJSPPage if everything is working smooth after this checkin.
	/*if (isJSP(request)) {
	    serveJSPPage(ctx);
	    //This attribute is used to decide whether to send error code, or not.
	    ctx.setAttribute("JSP_PAGE_SERVED", "true");

	    return null;
	}*/

	String pathInfo = request.getPathInfo();
	if (pathInfo == null || pathInfo.length() == 0) {
	    return null;
	}

	ServletContext sc = ctx.getServletConfig().getServletContext();

	String servletPath = request.getServletPath();
	String realPath = sc.getRealPath(servletPath);
	File file = new File(realPath+pathInfo);
	InputStream is = null;
	try {
		is = new FileInputStream(file);
	} catch (FileNotFoundException ex) {
		//try classpath
		is = getClass().getClassLoader().getResourceAsStream(servletPath.substring(1)+pathInfo);
	}

	int index = pathInfo.lastIndexOf('.');
	// Set the extension so it can be mapped to a MIME type
	String extnsn = pathInfo.substring(index + 1);
	ctx.setAttribute(DownloadServlet.EXTENSION, extnsn);	// NOI18N

	ctx.setAttribute("filePath", is);
	ctx.setAttribute("JSP_PAGE_SERVED", "false");

	// Return an InputStream to the tmpFile
	return is;
    }

    /**
     *	<p> This method looks at the request information and determines if it
     *	    thinks it is a jsp.  Essentially it thinks anything that ends with
     *	    "jsp" in is a jsp (this is not fool proof).</p>
     */
    public boolean isJSP(HttpServletRequest request) {
	String uri = request.getRequestURI();
	String pathInfo = request.getPathInfo();
	if ((uri.indexOf("jsp") > -1) && pathInfo.endsWith(".jsp")) {
	    return true;
	}
	return false;
    }

    /**
     *	<p> This method may be used to clean up any temporary resources.  It
     *	    will be invoked after the <code>InputStream</code> has been
     *	    completely read.</p>
     */
    public void cleanUp(DownloadServlet.Context ctx) {
	// Get the File information
	InputStream is =
	    (InputStream) ctx.getAttribute("filePath");		// NOI18N
	// Close the InputStream
	if (is != null) {
	    try {
		is.close();
	    } catch (Exception ex) {
		// Ignore...
	    }
	}
	ctx.removeAttribute("filePath");		// NOI18N
    }

    /**
     *	<p> This method is responsible for returning the last modified date of
     *	    the content, or -1 if not applicable.  This information will be
     *	    used for caching.</p>
     */
    public long getLastModified(DownloadServlet.Context context) {
	if (isJSP((HttpServletRequest) context.getServletRequest())) {
	    // Don't cache JSP's
	    return -1;
	}

	// This will enable caching on all files served through this code path
	return DEFAULT_MODIFIED_DATE;
    }

    private void serveJSPPage(DownloadServlet.Context ctx) {
	HttpServletRequest request = (HttpServletRequest)ctx.getServletRequest();
	HttpServletResponse response = (HttpServletResponse)ctx.getServletResponse();
	String pathInfo = (String)request.getAttribute(PATH_INFO_ATTRIBUTE);
	String servletPath = (String)request.getAttribute(SERVLET_PATH_ATTRIBUTE);

	if(servletPath == null) {
	    servletPath = request.getServletPath();
	    pathInfo = request.getPathInfo();
	}
	String className = getClassName(servletPath, pathInfo);

	try {
	    Class cls = Class.forName(className);
	    Object obj = cls.newInstance();
	    ((HttpServlet)obj).init(ctx.getServletConfig());
	    ((HttpServlet)obj).service(request, response);
	} catch(Exception ex) {
		//throw an exception here
	}
    }

    /**
     *	This method takes the servlet path and the pathInfo (which should be
     *	the jspName) and generates the class name of the generated JSP.  Both
     *	of these Strings are expected to have a leading '/'.
     */
    protected String getClassName(String servletPath, String jspName) {
	return getClassName(servletPath+jspName);
    }


    /**
     *	This method takes the full jsp name and generates the class name for
     *	the generated JSP.
     */
    protected String getClassName(String fullPath) {
	fullPath = fullPath.trim();
	int lastSlash = fullPath.lastIndexOf('/');
	if (lastSlash == -1) {
	    // Probably an error, but just in case we'll do this and let it
	    // fail later.
	    return JSP_CLASS_PREFIX+JspUtil.makeJavaIdentifier(fullPath);
	}

	// The packageName is the full path minus everything after last '/'
	String packageName = fullPath.substring(0, ++lastSlash);

	// The jspName is everything after the last '/'
	String jspName = fullPath.substring(lastSlash);

	// Make sure to get rid of any double slashes  "//"
	//This may never happen to our application.
	for (int loc=packageName.indexOf("//"); loc != -1;
		loc=packageName.indexOf("//")) {
	    packageName = packageName.replaceAll("//", "/");
	}

	// Get rid of leading '/'
	if (packageName.startsWith("/")) {
	    packageName = packageName.substring(1);
	}

	// Iterate through each part of path and call makeJavaIdentifier
	StringTokenizer tok = new StringTokenizer(packageName, "/");
	StringBuffer className = new StringBuffer(JSP_CLASS_PREFIX);
	while (tok.hasMoreTokens()) {
	    // Convert .'s to _'s + other conversions
	    className.append(JspUtil.makeJavaIdentifier(tok.nextToken()));
	    className.append('.');
	}

	// Add on the jsp name
	className.append(JspUtil.makeJavaIdentifier(jspName));
/* Commenting out for now, log later
	if (Util.isLoggableFINER()) {
	    Util.logFINER("CLASSNAME = "+className);
	}
*/
	// Return the classname
	return className.toString();
    }

    /**
     *	When an include() is called the PATH_INFO is set via a Request
     *	attribute named "javax.servlet.include.path_info".  This constant is
     *	set to this name.
     */
    private static final String PATH_INFO_ATTRIBUTE =
	"javax.servlet.include.path_info";

    /**
     *	When an include() is called the SERVLET_PATH is set via a Request
     *	attribute named "javax.servlet.include.servlet_path".  This constant is
     *	set to this name.
     */
    private static final String SERVLET_PATH_ATTRIBUTE =
	"javax.servlet.include.servlet_path";

    /**
     *	The base package where compiled JSP class files are located.
     */
    private static final String JSP_CLASS_PREFIX =	"org.apache.jsp.";

    /**
     *	This is the default "Last-Modified" Date.  Its value is the
     *	<code>Long</code> representing the initialization time of this class.
     */
    static final long DEFAULT_MODIFIED_DATE = (new Date()).getTime();
}
