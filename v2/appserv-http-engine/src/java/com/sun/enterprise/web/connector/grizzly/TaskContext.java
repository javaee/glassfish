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
package com.sun.enterprise.web.connector.grizzly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.ByteBuffer;


/**
 * This class encapsulates the logic required to synchronized
 * unblocking socket request with the blocked stream architecture of Tomcat.
 *
 * @author Jean-Francois Arcand
 */
public class TaskContext{

    
    /**
     * Use a PipeInputStream since we are using a non blocking byteChannel.
     */
    private InputStream inputStream;
    
    
    /**
     * Use a PipeInputStream since we are using a non blocking byteChannel.
     */
    private ByteBufferStream outputStream;   
    

    // ------------------------------------------------------Constructor -----//
    
    /**
     * Create a instance of this object. 
     */
    public TaskContext(){
    }    


    //------------------------------------------------------------------------//
     
        
    /**
     * Return the input stream used by this request. The default stream is an 
     * instance of <code>NonBlockinginputStream</code>
     */
    public InputStream getInputStream(){
        return inputStream;
    }
    
    public OutputStream getOutputStream(){
        return (OutputStream)outputStream;
    }
    
    public void setInputStream(InputStream inputStream){
        this.inputStream = inputStream;
    }
    
    public void setOutputStream(ByteBufferStream outputStream){
        this.outputStream = outputStream;
    }
    
    // ---------------------------------------------------------------------//

        
    /**
     * Fill the current output stream with the available bytes
     */
    public void write(ByteBuffer byteBuffer) throws IOException {  
        outputStream.write(byteBuffer);
    }
        
    
    /**
     * Flush bytes to the <code>NonBlockinginputStream</code>
     */
    public void flush() throws IOException {   
        if (outputStream != null) {
            outputStream.flush();
        }
    }
    
    
    /**
     * Recycle all streams used by this object.
     */
    public void recycle() throws IOException{
        if (inputStream != null){
            flush();
            inputStream.close();
        }
    }
    
}





