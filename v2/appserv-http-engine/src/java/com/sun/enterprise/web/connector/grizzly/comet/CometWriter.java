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

import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.apache.tomcat.util.buf.HexUtils;


/**
 * Non blocking Writer.  This class can be used from a 
 * <code>CometHandler</code> to execute non blocking write. If the write was
 * incomplete, the CometHandler must register itself using 
 * <code>CometContext.registerAsyncWrite()</code>
 *
 * @author Jeanfrancois Arcand
 */
public class CometWriter{

    
    private boolean isComplete = true;
    
    
    private SocketChannel socketChannel;

    
    /**
     * Buffer used for chunk length conversion.
     */
    protected byte[] chunkLength;   
    
    
    private final static ByteBuffer end = ByteBuffer.wrap("0\r\n\r\n".getBytes());
    
    
    /**
     * Is this CometWriter ready
     */
    private boolean ready = true;  
    
    
    public CometWriter() {
        chunkLength = new byte[10];
        chunkLength[8] = (byte) '\r';
        chunkLength[9] = (byte) '\n';
    }
    
    
    protected void setChannel(SocketChannel socketChannel){      
        this.socketChannel = socketChannel;
    }    
    
    
    public int write(byte[] buf) throws IOException {
        return write(buf,0,buf.length);
    }

    
    public int write(byte[] buf, int off, int len) throws IOException {
        if (!ready){
            throw new IllegalStateException("This CometWriter is no longer usable.");
        }
        int pos = 7;
        int current = len;
                
        // Force a blocking read.
        if (isComplete) {
            while (current > 0) {
                int digit = current % 16;
                current = current / 16;
                chunkLength[pos--] = HexUtils.HEX[digit];
            }     

            OutputWriter.flushChannel(socketChannel, 
                    ByteBuffer.wrap(chunkLength, pos + 1, 9 - pos));
        }
        
        int nWrite = socketChannel.write(ByteBuffer.wrap(buf,off,len));    
        if (nWrite == len) {
            isComplete = true;
            OutputWriter.flushChannel(socketChannel,
                    ByteBuffer.wrap(chunkLength, 8, 2));
            OutputWriter.flushChannel(socketChannel,end.slice());
        } else {
            isComplete = false;
        }
        return nWrite;
    }
    
    
    public boolean isComplete(){
        return isComplete;
    }
    
    
    public void recycle(){
        isComplete = true;
        socketChannel = null;
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
