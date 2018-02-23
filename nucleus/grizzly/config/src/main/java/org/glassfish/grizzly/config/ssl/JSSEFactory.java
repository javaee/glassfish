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

import java.net.Socket;
import javax.net.ssl.SSLEngine;

import org.glassfish.grizzly.ssl.SSLSupport;

/**
 * Factory interface to construct components based on the JSSE version in use.
 *
 * @author Bill Barker
 */
interface JSSEFactory {
    /**
     * Returns the ServerSocketFactory to use.
     */
    ServerSocketFactory getSocketFactory();

    /**
     * returns the SSLSupport attached to this socket.
     */
    SSLSupport getSSLSupport(Socket socket);

    /**
     * returns the SSLSupport attached to this SSLEngine.
     */
    SSLSupport getSSLSupport(SSLEngine sslEngine);
}
