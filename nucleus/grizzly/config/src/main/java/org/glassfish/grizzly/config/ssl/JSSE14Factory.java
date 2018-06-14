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
import javax.net.ssl.SSLSocket;
// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
import org.glassfish.grizzly.ssl.SSLSupport;
// END SJSAS 6439313

/**
 * Implementation class for JSSEFactory for JSSE 1.1.x (that ships with the
 * 1.4 JVM).
 *
 * @author Bill Barker
 */
// START SJSAS 6240885
//class JSSE14Factory implements JSSEFactory {
public class JSSE14Factory implements JSSEFactory {
// END SJSAS 6240885

    // START SJSAS 6240885
    // 
    //JSSE14Factory() {
    public JSSE14Factory() {
    // END SJSAS 6240885
    }

    @Override
    public ServerSocketFactory getSocketFactory() {
	return new JSSE14SocketFactory();
    }
    
    
    @Override
    public SSLSupport getSSLSupport(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            throw new IllegalArgumentException("The Socket has to be SSLSocket");
        }
        return new JSSE14Support((SSLSocket)socket);
    }

    // START SJSAS 6439313
    @Override
    public SSLSupport getSSLSupport(SSLEngine sslEngine) {
        return new JSSE14Support(sslEngine);
    }

    // END SJSAS 6439313
}
