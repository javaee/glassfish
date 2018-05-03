/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.connector;

import org.apache.catalina.LogFacade;
import org.apache.catalina.security.SecurityUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.*;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;


/**
 * Facade class that wraps a Coyote response object. 
 * All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 * @version $Revision: 1.9 $ $Date: 2007/05/05 05:32:43 $
 */


public class ResponseFacade 
    implements HttpServletResponse {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();



    // ----------------------------------------------------------- DoPrivileged
    
    private final class SetContentTypePrivilegedAction
            implements PrivilegedAction<Void> {

        private String contentType;

        public SetContentTypePrivilegedAction(String contentType){
            this.contentType = contentType;
        }

        @Override
        public Void run() {
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
    public ResponseFacade(Response response) {
        this.response = response;
    }


    // ----------------------------------------------- Class/Instance Variables


    /**
     * The wrapped response.
     */
    protected Response response = null;


    // --------------------------------------------------------- Public Methods

    
    /**
     * Prevent cloning the facade.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
      
    
    /**
     * Clear facade.
     */
    public void clear() {
        response = null;
    }


    public void finish() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        response.setSuspended(true);

    }


    public boolean isFinished() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.isSuspended();
    }


    // ------------------------------------------------ ServletResponse Methods

    @Override
    public String getCharacterEncoding() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.getCharacterEncoding();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        ServletOutputStream sos = response.getOutputStream();
        if (isFinished())
            response.setSuspended(true);
        return (sos);
    }

    @Override
    public PrintWriter getWriter() throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        //        if (isFinished())
        //            throw new IllegalStateException
        //                (/*sm.getString("responseFacade.finished")*/);

        PrintWriter writer = response.getWriter();
        if (isFinished())
            response.setSuspended(true);
        return (writer);
    }

    @Override
    public void setContentLength(int len) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;
        
        if (SecurityUtil.isPackageProtectionEnabled()){
            AccessController.doPrivileged(new SetContentTypePrivilegedAction(type));
        } else {
            response.setContentType(type);            
        }
    }

    @Override
    public void setBufferSize(int size) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isFinished())
            //            throw new IllegalStateException
            //                (/*sm.getString("responseFacade.finished")*/);
            return;
        
        if (SecurityUtil.isPackageProtectionEnabled()){
            try{
                AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>(){

                    public Void run() throws IOException{
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

    @Override
    public void resetBuffer() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.resetBuffer();
    }

    @Override
    public boolean isCommitted() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return (response.isAppCommitted());
    }

    @Override
    public void reset() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.reset();
    }

    @Override
    public void setLocale(Locale loc) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setLocale(loc);
    }

    @Override
    public Locale getLocale() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.getLocale();
    }

    @Override
    public void addCookie(Cookie cookie) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.addCookie(cookie);
    }

    @Override
    public boolean containsHeader(String name) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        response.setAppCommitted(true);

        response.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String msg) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        if (isCommitted())
            return;

        response.setStatus(sc, msg);
    }

    @Override
    public String getContentType() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.getContentType();
    }

    @Override
    public void setCharacterEncoding(String arg0) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        response.setCharacterEncoding(arg0);
    }


    // START SJSAS 6374990
    @Override
    public int getStatus() {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }

        return response.getStatus();
    }

    // END SJSAS 6374990

    @Override
    public String getHeader(String name) {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }
        return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }
        return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }
        return response.getHeaderNames();
    }

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }
        return response.getTrailerFields();
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        if (response == null) {
            throw new IllegalStateException(rb.getString(LogFacade.NULL_RESPONSE_OBJECT));
        }
        response.setTrailerFields(supplier);
    }
}
