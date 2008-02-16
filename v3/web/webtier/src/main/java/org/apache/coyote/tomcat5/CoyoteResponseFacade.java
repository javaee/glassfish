

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


package org.apache.coyote.tomcat5;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.StringManager;


/**
 * Facade class that wraps a Coyote response object. 
 * All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision: 1.9 $ $Date: 2007/05/05 05:32:43 $
 */


public class CoyoteResponseFacade 
    extends ResponseFacade
    implements HttpServletResponse {

    // ----------------------------------------------------------- DoPrivileged
    
    private final class SetContentTypePrivilegedAction
            implements PrivilegedAction {

        private String contentType;

        public SetContentTypePrivilegedAction(String contentType){
            this.contentType = contentType;
        }
        
        public Object run() {
            response.setContentType(contentType);
            return null;
        }            
    }
     
    
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public CoyoteResponseFacade(CoyoteResponse response) {

        super(response);
        this.response = response;

    }


    // ----------------------------------------------- Class/Instance Variables


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The wrapped response.
     */
    protected CoyoteResponse response = null;


    // --------------------------------------------------------- Public Methods

    
    /**
    * Prevent cloning the facade.
    */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
      
    
    /**
     * Clear facade.
     */
    public void clear() {
        response = null;
    }


    public void finish() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        response.setSuspended(true);

    }


    public boolean isFinished() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.isSuspended();

    }


    // ------------------------------------------------ ServletResponse Methods


    public String getCharacterEncoding() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.getCharacterEncoding();
    }


    public ServletOutputStream getOutputStream()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        ServletOutputStream sos = response.getOutputStream();
        if (isFinished())
            response.setSuspended(true);
        return (sos);

    }


    public PrintWriter getWriter()
        throws IOException {

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        PrintWriter writer = response.getWriter();
        if (isFinished())
            response.setSuspended(true);
        return (writer);

    }


    public void setContentLength(int len) {

        if (isCommitted())
            return;

        response.setContentLength(len);

    }


    public void setContentType(String type) {

        if (isCommitted())
            return;
        
        if (SecurityUtil.isPackageProtectionEnabled()){
            AccessController.doPrivileged(new SetContentTypePrivilegedAction(type));
        } else {
            response.setContentType(type);            
        }
    }


    public void setBufferSize(int size) {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setBufferSize(size);

    }


    public int getBufferSize() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.getBufferSize();
    }


    public void flushBuffer()
        throws IOException {

        if (isFinished())
            //            throw new IllegalStateException
            //                (/*sm.getString("responseFacade.finished")*/);
            return;
        
        if (SecurityUtil.isPackageProtectionEnabled()){
            try{
                AccessController.doPrivileged(new PrivilegedExceptionAction(){

                    public Object run() throws IOException{
                        response.setAppCommitted(true);

                        response.flushBuffer();
                        return null;
                    }
                });
            } catch(PrivilegedActionException e){
                Exception ex = e.getException();
                if (ex instanceof IOException){
                    throw (IOException)ex;
                }
            }
        } else {
            response.setAppCommitted(true);

            response.flushBuffer();            
        }

    }


    public void resetBuffer() {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.resetBuffer();

    }


    public boolean isCommitted() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return (response.isAppCommitted());
    }


    public void reset() {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.reset();

    }


    public void setLocale(Locale loc) {

        if (isCommitted())
            return;

        response.setLocale(loc);
    }


    public Locale getLocale() {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.getLocale();
    }


    public void addCookie(Cookie cookie) {

        if (isCommitted())
            return;

        response.addCookie(cookie);

    }


    public boolean containsHeader(String name) {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.containsHeader(name);
    }


    public String encodeURL(String url) {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.encodeURL(url);
    }


    public String encodeRedirectURL(String url) {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.encodeRedirectURL(url);
    }


    public String encodeUrl(String url) {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.encodeURL(url);
    }


    public String encodeRedirectUrl(String url) {

        if (response == null) {
            throw new IllegalStateException(
                            sm.getString("responseFacade.nullResponse"));
        }

        return response.encodeRedirectURL(url);
    }


    public void sendError(int sc, String msg)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendError(sc, msg);

    }


    public void sendError(int sc)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendError(sc);

    }


    public void sendRedirect(String location)
        throws IOException {

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendRedirect(location);

    }


    public void setDateHeader(String name, long date) {

        if (isCommitted())
            return;

        response.setDateHeader(name, date);

    }


    public void addDateHeader(String name, long date) {

        if (isCommitted())
            return;

        response.addDateHeader(name, date);

    }


    public void setHeader(String name, String value) {

        if (isCommitted())
            return;

        response.setHeader(name, value);

    }


    public void addHeader(String name, String value) {

        if (isCommitted())
            return;

        response.addHeader(name, value);

    }


    public void setIntHeader(String name, int value) {

        if (isCommitted())
            return;

        response.setIntHeader(name, value);

    }


    public void addIntHeader(String name, int value) {

        if (isCommitted())
            return;

        response.addIntHeader(name, value);

    }


    public void setStatus(int sc) {

        if (isCommitted())
            return;

        response.setStatus(sc);

    }


    public void setStatus(int sc, String sm) {

        if (isCommitted())
            return;

        response.setStatus(sc, sm);

    }


    // START SJSAS 6374990
    public int getStatus() {
        return response.getStatus();
    }

    public String getMessage() {
        return response.getMessage();
    }

    public void setSuspended(boolean suspended) {
        response.setSuspended(suspended);
    }

    public void setAppCommitted(boolean appCommitted) {
        response.setAppCommitted(appCommitted);
    }

    public int getContentCount() {
        return response.getContentCount();
    }

    public boolean isError() {
        return response.isError();
    }
    // END SJSAS 6374990


    //START S1AS 4703023
    /**
     * Return the original <code>CoyoteRequest</code> object.
     */
    public CoyoteResponse getUnwrappedCoyoteResponse()
        throws AccessControlException {

        // tomcat does not have any Permission types so instead of
        // creating a TomcatPermission for this, use SecurityPermission.
        if (Globals.IS_SECURITY_ENABLED) {
            Permission perm =
                new SecurityPermission("getUnwrappedCoyoteResponse");
            AccessController.checkPermission(perm);
        }

        return response;
    }
    //START S1AS 4703023
}
