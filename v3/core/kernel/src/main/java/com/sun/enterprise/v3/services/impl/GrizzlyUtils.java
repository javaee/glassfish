/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.util.SSLUtils;
import com.sun.grizzly.util.ThreadAttachment;
import com.sun.grizzly.util.Utils;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import javax.net.ssl.SSLEngine;

/**
 * Set of Grizzly network utilities
 * 
 * @author Alexey Stashok
 */
public class GrizzlyUtils {
    /**
     * Reads bytes to the <code>WorkerThread</code> associated <code>ByteBuffer</code>s.
     * Could be used to read both raw and secured data.
     * 
     * @param key <code>SelectionKey</code>
     * @param timeout read timeout
     * @return number of read bytes
     * @throws java.io.IOException
     */
    public static int readToWorkerThreadBuffers(SelectionKey key, int timeout) throws IOException {
        Object attachment = key.attachment();
        SSLEngine sslEngine = null;
        if (attachment instanceof ThreadAttachment) {
            sslEngine = ((ThreadAttachment) attachment).getSSLEngine();
        }
        
        WorkerThread thread = (WorkerThread) Thread.currentThread();
        
        if (sslEngine == null) {
            return Utils.readWithTemporarySelector(key.channel(), 
                    thread.getByteBuffer(), timeout).bytesRead;
        } else {
            // if ssl - try to unwrap secured buffer first
            ByteBuffer byteBuffer = thread.getByteBuffer();
            ByteBuffer securedBuffer = thread.getInputBB();
            
            if (securedBuffer.position() > 0) {
                int initialPosition = byteBuffer.position();
                byteBuffer = 
                        SSLUtils.unwrapAll(byteBuffer, securedBuffer, sslEngine);
                int producedBytes = byteBuffer.position() - initialPosition;
                if (producedBytes > 0) {
                    return producedBytes;
                }
            }
            
            // if no bytes were unwrapped - read more
            return SSLUtils.doSecureRead(key.channel(), sslEngine, byteBuffer,
                    securedBuffer).bytesRead;
        }
    }
}
