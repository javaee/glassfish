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

package com.sun.enterprise.web.connector.grizzly.ssl;

import com.sun.enterprise.web.connector.grizzly.SocketChannelOutputBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLEngine;

import org.apache.coyote.Response;

/**
 * Buffer the bytes until the <code>ByteChunk</code> is full or the request
 * is completed, and then delegate the SSL encryption to class 
 * <code>SSLOutputBuffer</code>
 * 
 * @author Jean-Francois Arcand
 */
public class SSLAsyncOutputBuffer extends SocketChannelOutputBuffer{
    
    /**
     * Encrypted Output <code>ByteBuffer</code>
     */
    protected ByteBuffer outputBB;
    
    
    /**
     * The <code>SSLEngine</code> used to write SSL data.
     */
    protected SSLEngine sslEngine;
    
    
    /**
     * Alternate constructor.
     */
    public SSLAsyncOutputBuffer(Response response, int headerBufferSize,
                                boolean useSocketBuffer) {
        super(response,headerBufferSize,useSocketBuffer);     
    }    
        
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * using <code>SSLOutputBuffer</code>
     * @param bb the ByteBuffer to write.
     */   
    public void flushChannel(ByteBuffer bb) throws IOException{
        SSLOutputWriter.flushChannel(socketChannel, bb, outputBB, sslEngine);
    }   
    
    /**
     * Return the encrypted <code>ByteBuffer</code> used to handle response.
     */    
    public ByteBuffer getOutputBB(){
        return outputBB;
    }
    
    
    /**
     * Set the encrypted <code>ByteBuffer</code> used to handle response.
     */   
    public void setOutputBB(ByteBuffer outputBB){
        this.outputBB = outputBB;
    }
    
         
    /**
     * Set the <code>SSLEngine</code>.
     */
    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

        
    /**
     * Get the <code>SSLEngine</code>.
     */
    public void setSSLEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }    
}
