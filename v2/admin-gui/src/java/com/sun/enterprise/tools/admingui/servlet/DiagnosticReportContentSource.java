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
import java.io.InputStream;

import javax.servlet.ServletRequest;


/**
 *
 */
public class DiagnosticReportContentSource implements DownloadServlet.ContentSource {

    /**
     *	<p> This method returns a unique string used to identify this
     *	    {@link DownloadServlet#ContentSource}.  This string must be
     *	    specified in order to select the appropriate
     *	    {@link DownloadServlet#ContentSource} when using the
     *	    {@link DownloadServlet}.</p>
     */
    public String getId() {
	return "diagReport";					// NOI18N
    }

    /**
     *  <p> This method is responsible for generating the content and
     *	    returning an InputStream to that content.  It is also
     *	    responsible for setting any attribute values in the
     *	    {@link DownloadServlet#Context}, such as {@link DownloadServlet#EXTENSION} or
     *	    {@link DownloadServlet#CONTENT_TYPE}.</p>
     */
    public InputStream getInputStream(DownloadServlet.Context ctx) {
	// Set the extension so it can be mapped to a MIME type
	ctx.setAttribute(DownloadServlet.EXTENSION, "jar");	// NOI18N

	// Get file name
	ServletRequest request = ctx.getServletRequest();
	String filename = request.getParameter("propertyForm:propertySheet:propertySectionTextField:reportLocationProp:reportLocationHidden");
	if ((filename == null) || (filename.trim().length() == 0)) {
	    throw new IllegalArgumentException(
		    "Report name not specified!");		// NOI18N
	}

	// Open a Stream
	InputStream stream = null;
	try {
	    stream = new FileInputStream(filename);
	} catch (Exception ex) {
	    throw new RuntimeException(ex);
	}

	// Save some important stuff for cleanUp
	ctx.setAttribute("stream", stream);			// NOI18N

	// Return the InputStream
	return stream;
    }

    /**
     *	<p> This method may be used to clean up any temporary resources.  It
     *	    will be invoked after the <code>InputStream</code> has been
     *	    completely read.</p>
     */
    public void cleanUp(DownloadServlet.Context ctx) {
	// Get the File information
	InputStream stream =
	    (InputStream) ctx.getAttribute("stream");		// NOI18N

	// Close the InputStream
	if (stream != null) {
	    try {
		stream.close();
	    } catch (Exception ex) {
		// Ignore...
	    }
	}

	// Null references...
	ctx.removeAttribute("stream");			// NOI18N
    }

    /**
     *	<p> This method is responsible for returning the last modified date of
     *	    the content, or -1 if not applicable.  This information will be
     *	    used for caching.  This implementation always returns -1.</p>
     *
     *	@return	-1
     */
    public long getLastModified(DownloadServlet.Context context) {
	return -1;
    }
}
