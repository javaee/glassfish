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
package com.sun.enterprise.web.connector.grizzly.comet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Non blocking IO reader. This class can be used from a 
 * <code>CometHandler</code> to execute non blocking read. This is
 * usefull when the client is pipelining data. The CometHandler
 * will be notified as soon as bytes are arriving. CometHandler
 * who wants to be notified just need to register themself
 * by calling <code>CometContext.registerAsyncRead()</code>
 *
 * @author Jeanfrancois Arcand
 */
public class CometReader {
        
    /**
     * The non blocking channel.
     */
    private SocketChannel socketChannel;
    
    
    /**
     * The ByteBuffer used to execute the first read in CometTask.
     */
    private ByteBuffer byteBuffer;
    
    
    /**
     * How many bytes have we read.
     */
    private int nRead = 0;
    
    
    /**
     * Is this CometReader ready
     */
    private boolean ready = true;
            
    
    public CometReader() {
    }


    /**
     * Set the underlying SocketChannel.
     */
    protected void setChannel(SocketChannel socketChannel){      
        this.socketChannel = socketChannel;
    }    
    
    
    /**
     *  Read bytes without blocking.
     */ 
    public int read(byte[] buf) throws IOException {
        return read(buf,0,buf.length);
    }

    
    /**
     * Read bytes without blocking.
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        if (!ready){
            throw new IllegalStateException("This CometReader is no longer usable");
        }
        // The CometTask has already read the first bytes for us.
        if (byteBuffer != null){           
            byteBuffer.get(buf,off,len);
            return nRead > len ? len : nRead;
        }
        return socketChannel.read(ByteBuffer.wrap(buf,off,len));
    }
   
    
    /**
     * Recycle this object.
     */
    public void recycle(){
        nRead = 0;
        byteBuffer = null;
        socketChannel = null;
    }    


    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    
    public void setNRead(int nRead) {
        this.nRead = nRead;
    }

    
    /**
     * Return true if this instance is ready to read.
     */
    public boolean isReady() {
        return ready;
    }

    
    /**
     * <tt>false</tt> if this instance is no longer ready to read.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
