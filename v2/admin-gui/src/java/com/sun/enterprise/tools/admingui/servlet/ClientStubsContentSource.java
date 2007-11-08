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

import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.admin.common.JMXFileTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.ServletRequest;


/**
 *
 */
public class ClientStubsContentSource implements DownloadServlet.ContentSource {

    /**
     *	<p> This method returns a unique string used to identify this
     *	    {@link DownloadServlet#ContentSource}.  This string must be
     *	    specified in order to select the appropriate
     *	    {@link DownloadServlet#ContentSource} when using the
     *	    {@link DownloadServlet}.</p>
     */
    public String getId() {
	return "clientStubs";					// NOI18N
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

	// Get appName
	ServletRequest request = ctx.getServletRequest();
	String appName = request.getParameter("appName");	// NOI18N
	if ((appName == null) || (appName.trim().length() == 0)) {
	    appName = request.getParameter("appClientName");	// NOI18N
	}

	// Create the tmpFile
	String tmpFilePath = null;
	InputStream tmpFile = null;
	try {
	    tmpFilePath = new JMXFileTransfer(JMXUtil.getMBeanServer())
		.downloadClientStubs(
			appName,
			System.getProperty("java.io.tmpdir"));	// NOI18N
	    tmpFile = new FileInputStream(tmpFilePath);
	} catch (Exception ex) {
	    throw new RuntimeException(ex);
	}

	// Save some important stuff for cleanUp
	ctx.setAttribute("tmpFilePath", tmpFilePath);		// NOI18N
	ctx.setAttribute("tmpFile", tmpFile);			// NOI18N

	// Return an InputStream to the tmpFile
	return tmpFile;
    }

    /**
     *	<p> This method may be used to clean up any temporary resources.  It
     *	    will be invoked after the <code>InputStream</code> has been
     *	    completely read.</p>
     */
    public void cleanUp(DownloadServlet.Context ctx) {
	// Get the File information
	String tmpFilePath =
	    (String) ctx.getAttribute("tmpFilePath");		// NOI18N
	InputStream tmpFile =
	    (InputStream) ctx.getAttribute("tmpFile");		// NOI18N

	// Close the InputStream
	if (tmpFile != null) {
	    try {
		tmpFile.close();
	    } catch (Exception ex) {
		// Ignore...
	    }
	}

	// Delete the Temporary File
	if (tmpFilePath != null) {
	    File file = new File(tmpFilePath);
	    try {
		file.delete();
	    } catch (Exception ex) {
		// Ignore...
	    }
	}

	// Null references...
	ctx.removeAttribute("tmpFilePath");		// NOI18N
	ctx.removeAttribute("tmpFile");			// NOI18N
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
