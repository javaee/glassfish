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

import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

/**
 * SSL over NIO utility to encrypt <code>ByteBuffer</code> and flush them.
 * All the SSLEngine operations are delegated to class <code>SSLUtils</code>
 *
 * @author Jeanfrancois Arcand
 */
public final class SSLOutputWriter{

    /**
     * Encrypt the response and flush it using <code>OutputWriter</code>
     */     
    public static void flushChannel(SocketChannel socketChannel, ByteBuffer bb)
            throws IOException{
      
        SSLWorkerThread workerThread = (SSLWorkerThread)Thread.currentThread();
        SSLEngine sslEngine = workerThread.getSSLEngine();
        ByteBuffer outputBB = workerThread.getOutputBB();
        flushChannel(socketChannel,bb,outputBB,sslEngine);
    }
    
    
    /**
     * Encrypt the response and flush it using <code>OutputWriter</code>
     */     
    public static void flushChannel(SocketChannel socketChannel, ByteBuffer bb,
            ByteBuffer outputBB, SSLEngine sslEngine) throws IOException{   
        
        while (bb.hasRemaining()) {
            SSLEngineResult result = SSLUtils.wrap(bb,outputBB,sslEngine);
            switch (result.getStatus()) {
                case OK:
                    if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                        SSLUtils.executeDelegatedTask(sslEngine);
                    }
                    break;

                default:
                    throw new IOException("SSLOutputWriter: " + result.getStatus());
            }

            if (outputBB.hasRemaining()) {
                OutputWriter.flushChannel(socketChannel,outputBB);
            }
        }
        outputBB.clear();
    }
}
