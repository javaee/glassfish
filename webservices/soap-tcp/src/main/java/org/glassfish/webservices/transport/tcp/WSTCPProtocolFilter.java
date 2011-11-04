/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.webservices.transport.tcp;

import com.sun.grizzly.BaseSelectionKeyHandler;
import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.SelectionKeyHandler;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.util.ConnectionCloseHandler;
import com.sun.grizzly.util.WorkerThread;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alexey Stashok
 */
public class WSTCPProtocolFilter implements ProtocolFilter {
    private static Logger logger = LogDomains.getLogger(WSTCPProtocolFilter.class, LogDomains.WEBSERVICES_LOGGER);

    private volatile Connector connector;
    private final ConnectionCloseHandler closeHandler = new WSTCPConnectionCloseHandler();
    
    private final Object sync = new Object();

    private static final V3Module module = new V3Module();
    
    public boolean execute(Context ctx) throws IOException {
        if (connector == null) {
            synchronized(sync) {
                if (connector == null) {
                    final TCPSelectorHandler handler = (TCPSelectorHandler) ctx.getSelectorHandler();
                    final String host = handler.getInet().getHostName();
                    final int port = handler.getPort();

                    logger.log(Level.INFO, "Initialize SOAP/TCP protocol for port: " + port);
                    
                    connector = new Connector(host, port, module.getDelegate());

                    final SelectionKeyHandler keyHandler = handler.getSelectionKeyHandler();
                    if (keyHandler instanceof BaseSelectionKeyHandler) {
                        ((BaseSelectionKeyHandler) keyHandler).setConnectionCloseHandler(closeHandler);
                    }
                }
            }
        }

        final ByteBuffer byteBuffer =
                ((WorkerThread) Thread.currentThread()).getByteBuffer();
        byteBuffer.flip();
        final SocketChannel channel = (SocketChannel) ctx.getSelectionKey().channel();
        connector.process(byteBuffer, channel);

        return false;
    }

    public boolean postExecute(Context ctx) throws IOException {
        return true;
    }

    protected class WSTCPConnectionCloseHandler implements ConnectionCloseHandler {

        public void locallyClosed(SelectionKey key) {
            notifyConnectionClosed(key);
        }

        public void remotelyClosed(SelectionKey key) {
            notifyConnectionClosed(key);
        }

        private void notifyConnectionClosed(SelectionKey key) {
            try {
                if (connector != null) {
                    connector.notifyConnectionClosed((SocketChannel) key.channel());
                } else {
                    synchronized (sync) {
                        if (connector != null) {
                            connector.notifyConnectionClosed((SocketChannel) key.channel());
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

    }
}
