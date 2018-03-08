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

package org.apache.catalina.connector;

import java.io.IOException;

/**
 * Wrap an IOException identifying it as being caused by an abort
 * of a request by a remote client.
 *
 * @author Glenn L. Nielsen
 * @version $Revision: 1.3 $ $Date: 2005/12/08 01:27:28 $
 */

public final class ClientAbortException extends IOException {


    //------------------------------------------------------------ Constructors


    /**
     * Construct a new ClientAbortException with no other information.
     */
    public ClientAbortException() {
        super();
    }


    /**
     * Construct a new ClientAbortException for the specified message.
     *
     * @param message Message describing this exception
     */
    public ClientAbortException(String message) {
        super(message);
    }


    /**
     * Construct a new ClientAbortException for the specified throwable.
     *
     * @param throwable Throwable that caused this exception
     */
    public ClientAbortException(Throwable throwable) {
        super(throwable);
    }


    /**
     * Construct a new ClientAbortException for the specified message
     * and throwable.
     *
     * @param message Message describing this exception
     * @param throwable Throwable that caused this exception
     */
    public ClientAbortException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
