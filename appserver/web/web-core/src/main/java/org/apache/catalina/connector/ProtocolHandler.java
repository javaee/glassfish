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

package org.apache.catalina.connector;

import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 * <p/>
 * This is the main interface to be implemented by a coyote connector.
 * (In contrast, Adapter is the main interface to be implemented by a
 * coyote servlet container.)
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 * @see HttpHandler
 */
public interface ProtocolHandler {
    /**
     * Pass config info.
     */
    void setAttribute(String name, Object value);

    Object getAttribute(String name);

    /**
     * The adapter, used to call the connector.
     */
    void setHandler(HttpHandler handler);

    HttpHandler getHandler();

    /**
     * Init the protocol.
     */
    void init() throws Exception;

    /**
     * Start the protocol.
     */
    void start() throws Exception;

    void destroy() throws Exception;
}
