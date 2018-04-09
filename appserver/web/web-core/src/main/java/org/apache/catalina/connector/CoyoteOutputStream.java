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

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Coyote implementation of the servlet output stream.
 * 
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public class CoyoteOutputStream 
    extends ServletOutputStream {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();


    // ----------------------------------------------------- Instance Variables


    protected OutputBuffer ob;


    // ----------------------------------------------------------- Constructors


    // BEGIN S1AS 6175642
    /*
    protected CoyoteOutputStream(OutputBuffer ob) {
    */
    public CoyoteOutputStream(OutputBuffer ob) {
    // END S1AS 6175642
        this.ob = ob;
    }
    
    
    // --------------------------------------------------------- Public Methods


    /**
    * Prevent cloning the facade.
    */
    protected Object clone()
        throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

  
    // -------------------------------------------------------- Package Methods


    /**
     * Clear facade.
     */
    void clear() {
        ob = null;
    }


    // --------------------------------------------------- OutputStream Methods


    public void write(int i)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ob.writeByte(i);
    }


    public void write(byte[] b)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        write(b, 0, b.length);
    }


    public void write(byte[] b, int off, int len)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ob.write(b, off, len);
    }


    /**
     * Will send the buffer to the client.
     */
    public void flush()
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ob.flush();
    }


    public void close()
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ob.close();
    }


    // -------------------------------------------- ServletOutputStream Methods


    public void print(String s)
        throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ob.write(s);
    }


    public boolean isReady() {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }
        return ob.isReady();
    }


    public void setWriteListener(WriteListener writeListener) {
        // Disallow operation if the object has gone out of scope
        if (ob == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        if (writeListener == null) {
            throw new NullPointerException(rb.getString(LogFacade.NULL_WRITE_LISTENER_EXCEPTION));
        }

        ob.setWriteListener(writeListener);
    }
}
