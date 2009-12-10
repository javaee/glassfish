/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.mbeanserver;

import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Factory;
import org.jvnet.hk2.component.ComponentException;

import javax.management.MBeanServer;

import org.glassfish.api.Startup;
import org.glassfish.api.Async;
import org.glassfish.internal.api.*;

/**
    Factory for the MBeanServer.  Required so that HK2 can find an MBeanServer
    for modules doing @Inject MBeanServer.
 */
@Service
@FactoryFor(MBeanServer.class)
public final class MBeanServerFactory implements Factory, PostStartup {
    private static void debug( final String s ) { System.out.println(s); }
    
    private final MBeanServer     mMBeanServer;
    
    public MBeanServerFactory()
    {
        // initialize eagerly; ~20ms
        mMBeanServer = java.lang.management.ManagementFactory.getPlatformMBeanServer();
    }
    
    public void postConstruct()
    {
    }
    
    /**
     * The system calls this method to obtain a reference
     * to the component.
     *
     * @return null is a valid return value. This is useful
     *         when a factory primarily does a look-up and it fails
     *         to find the specified component, yet you don't want that
     *         by itself to be an error. If the injection wants
     *         a non-null value (i.e., <tt>@Inject(optional=false)</tt>).
     * @throws org.jvnet.hk2.component.ComponentException
     *          If the factory failed to get/create an instance
     *          and would like to propagate the error to the caller.
     */
    public Object getObject() throws ComponentException {
        return mMBeanServer;
    }
    
}











