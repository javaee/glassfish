/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.enterprise.util.Result;

import java.util.concurrent.Future;

/**
 * Generic interface used by the GrizzlyService to start the tcp/udp/tcl stack.
 * By default, we are starting Grizzly, but we might allow other framework to
 * hook in and drive hk2/v3.
 * 
 * TODO: Allow addition of other types of Container, not only Adapter but
 *       also any extension.
 * 
 * @author Jeanfrancois Arcand
 */
public interface NetworkProxy extends EndpointMapper<com.sun.grizzly.tcp.Adapter>{


    /** 
     * Stop the proxy. 
     */
    public void stop();
    
    
    /** 
     * Start the proxy. 
     */
    public Future<Result<Thread>> start();
    

    public int getPort();


    public void destroy();
}
