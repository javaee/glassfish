

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.connector;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import org.apache.catalina.Response;
import org.apache.catalina.util.StringManager;


/**
 * Facade class that wraps a Catalina-internal <b>Response</b>
 * object.  All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.3 $ $Date: 2006/08/14 20:45:38 $
 */

public class ResponseFacade implements ServletResponse {


    // ----------------------------------------------------------- Constants


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public ResponseFacade(Response response) {
        this.resp = response;
        this.response = (ServletResponse) response;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The wrapped response.
     */
    protected ServletResponse response = null;


    /**
     * The wrapped response.
     */
    protected Response resp = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Clear facade.
     */
    public void clear() {
        response = null;
        resp = null;
    }


    public void finish() {

        resp.setSuspended(true);

    }


    public boolean isFinished() {

        return resp.isSuspended();

    }


    // ------------------------------------------------ ServletResponse Methods


    public String getCharacterEncoding() {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return response.getCharacterEncoding();
    }


    public ServletOutputStream getOutputStream()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        ServletOutputStream sos = response.getOutputStream();
        if (isFinished())
            resp.setSuspended(true);
        return (sos);

    }


    public PrintWriter getWriter()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        PrintWriter writer = response.getWriter();
        if (isFinished())
            resp.setSuspended(true);
        return (writer);

    }


    public void setContentLength(int len) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        response.setContentLength(len);

    }

    public void setCharacterEncoding(String charset) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        response.setCharacterEncoding(charset);

    }


    public void setContentType(String type) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        response.setContentType(type);

    }

    public String getContentType() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        return response.getContentType();

    }

    public void setBufferSize(int size) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setBufferSize(size);

    }


    public int getBufferSize() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        return response.getBufferSize();
    }


    public void flushBuffer()
        throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isFinished())
            //            throw new IllegalStateException
            //                (/*sm.getString("responseFacade.finished")*/);
            return;

        resp.setAppCommitted(true);

        try {
            response.flushBuffer();
        } catch(IOException ioe) {
            // An IOException on a write is almost always due to
            // the remote client aborting the request.  Wrap this
            // so that it can be handled better by the error dispatcher.
            throw new ClientAbortException(ioe);
        }
    }


    public void resetBuffer() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.resetBuffer();

    }


    public boolean isCommitted() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        return (resp.isAppCommitted());
    }


    public void reset() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.reset();

    }


    public void setLocale(Locale loc) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        response.setLocale(loc);
    }


    public Locale getLocale() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        return response.getLocale();
    }


}
