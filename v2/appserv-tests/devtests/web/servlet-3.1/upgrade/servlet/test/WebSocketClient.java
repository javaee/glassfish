/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Processor;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpClientFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOConnectorHandler;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.websockets.WebSocketEngine.WebSocketHolder;
import org.glassfish.grizzly.websockets.*;

public class WebSocketClient extends DefaultWebSocket {
    private static final Logger logger = Logger.getLogger(WebSocketEngine.WEBSOCKET);
    private Version version;
    private final URI address;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    protected TCPNIOTransport transport;

    public WebSocketClient(String uri, WebSocketListener... listeners) {
        this(uri, WebSocketEngine.DEFAULT_VERSION, listeners);
    }

    public WebSocketClient(String uri, Version version, WebSocketListener... listeners) {
        super(version.createHandler(true), null, listeners);
        this.version = version;
        try {
            address = new URI(uri);
        } catch (URISyntaxException e) {
            throw new WebSocketException(e.getMessage(), e);
        }
        add(new WebSocketCloseAdapter());
    }

    public URI getAddress() {
        return address;
    }

    public void execute(Runnable runnable) {
        executorService.submit(runnable);
    }

    /**
     * @return this on successful connection
     */
    public WebSocket connect() {
        return connect(WebSocketEngine.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * @param timeout number of seconds to timeout trying to connect
     * @param unit time unit to use
     *
     * @return this on successful connection
     */
    public WebSocket connect(long timeout, TimeUnit unit) {
        try {
            buildTransport();
            transport.start();
            final TCPNIOConnectorHandler connectorHandler = new TCPNIOConnectorHandler(transport) {
                @Override
                protected void preConfigure(Connection conn) {
                    super.preConfigure(conn);
//                    final ProtocolHandler handler = version.createHandler(true);
                    /*
                    holder.handshake = handshake;
                     */
                    protocolHandler.setConnection(conn);
                    final WebSocketHolder holder = WebSocketEngine.getEngine().setWebSocketHolder(conn, protocolHandler,
                        WebSocketClient.this);
                    holder.handshake = protocolHandler.createHandShake(address);
                }
            };
            final CountDownLatch latch = new CountDownLatch(1);
            add(new WebSocketAdapter() {
                @Override
                public void onConnect(final WebSocket socket) {
                    super.onConnect(socket);
                    latch.countDown();
                }
            });
            connectorHandler.setProcessor(createFilterChain());
            // start connect
            connectorHandler.connect(new InetSocketAddress(
                    address.getHost(), address.getPort()), (CompletionHandler<Connection>) null);
            latch.await(timeout, unit);

            return this;
        } catch (Exception e) {
            e.printStackTrace();
            throw new HandshakeException(e.getMessage());
        }
    }

    protected void buildTransport() {
        transport = TCPNIOTransportBuilder.newInstance().build();
    }

    private static Processor createFilterChain() {
        FilterChainBuilder clientFilterChainBuilder = FilterChainBuilder.stateless();
        clientFilterChainBuilder.add(new TransportFilter());
        clientFilterChainBuilder.add(new HttpClientFilter());
        clientFilterChainBuilder.add(new WebSocketFilter());

        return clientFilterChainBuilder.build();
    }

    public class WebSocketCloseAdapter extends WebSocketAdapter {
        @Override
        public void onClose(WebSocket socket, DataFrame frame) {
            super.onClose(socket, frame);
            if (transport != null) {
                try {
                    transport.stop();
                } catch (IOException e) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
            }
        }
    }
}
