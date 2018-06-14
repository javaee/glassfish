/*
 * Copyright (c) 2007-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.net.ssl.SSLContext;

/**
 * This class creates server sockets.  It may be subclassed by other factories, which create particular types of server
 * sockets.  This provides a general framework for the addition of public socket-level functionality.  It it is the
 * server side analogue of a socket factory, and similarly provides a way to capture a variety of policies related to
 * the sockets being constructed.
 *
 * <P> Like socket factories, Server Socket factory instances have two categories of methods.  First are methods used to
 * create sockets. Second are methods which set properties used in the production of sockets, such as networking
 * options.  There is also an environment specific default server socket factory; frameworks will often use their own
 * customized factory.
 *
 * <P><hr><em> It may be desirable to move this interface into the <b>java.net</b> package, so that is not an extension
 * but the preferred interface.  Should this be serializable, making it a JavaBean which can be saved along with its
 * networking configuration? </em>
 *
 * @author db@eng.sun.com
 * @author Harish Prabandham
 */
public abstract class ServerSocketFactory implements Cloneable {
    private static ServerSocketFactory theFactory;
    protected SSLContext context;
    protected final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Constructor is used only by subclasses.
     */
    protected ServerSocketFactory() {
    }

    /**
     * Returns a server socket which uses all network interfaces on the host, and is bound to a the specified port.  The
     * socket is configured with the socket options (such as accept timeout) given to this factory.
     *
     * @param port the port to listen to
     *
     * @throws IOException for networking errors
     * @throws InstantiationException for construction errors
     */
    public abstract ServerSocket createSocket(int port) throws IOException, InstantiationException;

    /**
     * Returns a server socket which uses all network interfaces on the host, is bound to a the specified port, and uses
     * the specified connection backlog.  The socket is configured with the socket options (such as accept timeout)
     * given to this factory.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     *
     * @throws IOException for networking errors
     * @throws InstantiationException for construction errors
     */
    public abstract ServerSocket createSocket(int port, int backlog) throws IOException, InstantiationException;

    /**
     * Returns a server socket which uses only the specified network interface on the local host, is bound to a the
     * specified port, and uses the specified connection backlog.  The socket is configured with the socket options
     * (such as accept timeout) given to this factory.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     * @param ifAddress the network interface address to use
     *
     * @throws IOException for networking errors
     * @throws InstantiationException for construction errors
     */
    public abstract ServerSocket createSocket(int port, int backlog, InetAddress ifAddress)
    throws IOException, InstantiationException;

    /**
     * Wrapper function for accept(). This allows us to trap and translate exceptions if necessary
     *
     * @throws IOException;
     */
    public abstract Socket acceptSocket(ServerSocket socket) throws IOException;

    /**
     * Extra function to initiate the handshake. Sometimes necessary for SSL
     *
     * @throws IOException;
     */
    public abstract void handshake(Socket sock) throws IOException;

    public abstract void init() throws IOException;

    public void initSocket(Socket s) {
    }

    /**
     * Return the {@link SSLContext} required when implementing SSL over NIO non-blocking.
     *
     * @return SSLContext
     */
    public SSLContext getSSLContext() {
        return context;
    }

    /**
     * General mechanism to pass attributes from the ServerConnector to the socket factory.
     *
     * Note that the "preferred" mechanism is to use bean setters and explicit methods, but this allows easy
     * configuration via server.xml or simple Properties
     */
    public void setAttribute(String name, Object value) {
        if (name != null && value != null) {
            attributes.put(name, value);
        }
    }

    /**
     * Returns a copy of the environment's default socket factory.
     */
    public static synchronized ServerSocketFactory getDefault() {
        if (theFactory == null) {
            theFactory = new DefaultServerSocketFactory();
        }
        try {
            return (ServerSocketFactory) theFactory.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

