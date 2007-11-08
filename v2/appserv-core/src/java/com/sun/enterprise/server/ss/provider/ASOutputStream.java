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
package com.sun.enterprise.server.ss.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;


/**
 * OutputStream implementation for the socket wrappers of Quick startup
 * implementation. Implementation is thread safe while using nio to read
 * and write simultaneusly in the non-blocking mode.
 */

 final class ASOutputStream extends OutputStream{

    private static final Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private SocketChannel sc;
    private final Socket sock;
    private Selector selector;
    private boolean closed = false;
    private ByteBuffer bb = null;
    private byte[] bs = null; 	// Invoker's previous array
    private byte[] b1 = null;

    ASOutputStream(SocketChannel sc, Socket sock) throws IOException {
        this.sc = sc;
        this.sock = sock;
        selector = Selector.open();
        this.sc.register(selector, SelectionKey.OP_WRITE); 
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        try {
            selector.close();
            selector = null;
            sc = null;
        } catch (Exception ie) {
            if ( logger.isLoggable(Level.FINE) ) {
                logger.log(Level.FINE, "" + ie.getMessage(), ie);
            }
        }
    }

    public void flush() throws IOException {
        checkClosed();
    }

    private void waitForSelect() throws IOException {
        java.net.Socket sock = sc.socket();
        if (sock.isClosed()) {
            close();
            throw new IOException("Socket closed");
        }       
        Iterator it;
        SelectionKey selKey;
        
        selectorblock:
            while (true) {
                try {
                    selector.select();
                    if (sock.isOutputShutdown() || sock.isClosed()) {
                         throw new IOException("Output Shutdown");
                    }
                    it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        selKey = (SelectionKey)it.next();
                        if (selKey.isValid() && selKey.isWritable()) {
                            it.remove();
                            break selectorblock;
                        }
                    }
                } catch (Exception e) {
                    throw (IOException) (new IOException()).initCause(e);
                }
            }
    }

    public synchronized void write(int b) throws IOException {
        if (b1 == null)
            b1 = new byte[1];
        b1[0] = (byte)b;
        this.write(b1);
    }

    public synchronized void write(byte[] bs, int off, int len) throws IOException {
        checkClosed();
        if ((off < 0) || (off > bs.length) || (len < 0) ||
           ((off + len) > bs.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        ByteBuffer bb = ((this.bs == bs)
                        ? this.bb
                        : ByteBuffer.wrap(bs));
        bb.limit(Math.min(off + len, bb.capacity()));
        bb.position(off);
        this.bb = bb;
        this.bs = bs;
        waitForSelect();
        while (bb.hasRemaining()) {
            sc.write(bb);
        }
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } catch (Throwable t) {}
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream is closed");
        }
        
        if (sock.isOutputShutdown()) {
            throw new IOException("Output Shutdown");
        }
    }
}

