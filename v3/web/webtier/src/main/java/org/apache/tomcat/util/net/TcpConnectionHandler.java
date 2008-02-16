

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.net;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.util.*;

/**
 * This interface will be implemented by any object that
 * uses TcpConnections. It is supported by the pool tcp
 * connection manager and should be supported by future
 * managers.
 * The goal is to decouple the connection handler from
 * the thread, socket and pooling complexity.
 */
public interface TcpConnectionHandler {
    
    /** Add informations about the a "controler" object
     *  specific to the server. In tomcat it will be a
     *  ContextManager.
     *  @deprecated This has nothing to do with TcpHandling,
     *  was used as a workaround
     */
    public void setServer(Object manager);

    
    /** Used to pass config informations to the handler
     *  @deprecated. This has nothing to do with Tcp,
     *  was used as a workaround 
     */
    public void setAttribute(String name, Object value );
    
    /** Called before the call to processConnection.
     *  If the thread is reused, init() should be called once per thread.
     *
     *  It may look strange, but it's a _very_ good way to avoid synchronized
     *  methods and keep per thread data.
     *
     *  Assert: the object returned from init() will be passed to
     *  all processConnection() methods happening in the same thread.
     * 
     */
    public Object[] init( );

    /**
     *  Assert: connection!=null
     *  Assert: connection.getSocket() != null
     *  Assert: thData != null and is the result of calling init()
     *  Assert: thData is preserved per Thread.
     */
    public void processConnection(TcpConnection connection, Object thData[]);    
}
