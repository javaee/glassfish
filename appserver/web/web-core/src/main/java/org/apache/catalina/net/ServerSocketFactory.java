/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.net;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;


/**
 * Interface that describes the common characteristics of factory classes
 * that create server sockets which may be required by a Connector.  A concrete
 * implementation of this interface will be assigned to a Connector
 * via the <code>setFactory()</code> method.
 *
 * @author db@eng.sun.com
 * @author Harish Prabandham
 * @author Craig R. McClanahan
 */
public interface ServerSocketFactory {


    // --------------------------------------------------------- Public Methods


    /**
     * Returns a server socket which uses all network interfaces on
     * the host, and is bound to a the specified port.  The socket is
     * configured with the socket options (such as accept timeout)
     * given to this factory.
     *
     * @param port the port to listen to
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file (SSL only)
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider (SSL only)
     * @exception CertificateException       general certificate error (SSL only)
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate (SSL only)
     * @exception KeyManagementException     problem in the key management
     *                                       layer (SSL only)
     */
    public ServerSocket createSocket (int port)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException;


    /**
     * Returns a server socket which uses all network interfaces on
     * the host, is bound to a the specified port, and uses the
     * specified connection backlog.  The socket is configured with
     * the socket options (such as accept timeout) given to this factory.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file (SSL only)
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider (SSL only)
     * @exception CertificateException       general certificate error (SSL only)
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate (SSL only)
     * @exception KeyManagementException     problem in the key management
     *                                       layer (SSL only)
     */
    public ServerSocket createSocket (int port, int backlog)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException;


    /**
     * Returns a server socket which uses only the specified network
     * interface on the local host, is bound to a the specified port,
     * and uses the specified connection backlog.  The socket is configured
     * with the socket options (such as accept timeout) given to this factory.
     *
     * @param port the port to listen to
     * @param backlog how many connections are queued
     * @param ifAddress the network interface address to use
     *
     * @exception IOException                input/output or network error
     * @exception KeyStoreException          error instantiating the
     *                                       KeyStore from file (SSL only)
     * @exception NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider (SSL only)
     * @exception CertificateException       general certificate error (SSL only)
     * @exception UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate (SSL only)
     * @exception KeyManagementException     problem in the key management
     *                                       layer (SSL only)
     */
    public ServerSocket createSocket (int port, int backlog,
                                      InetAddress ifAddress)
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException;


}
