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
package org.glassfish.api.amx;

import java.util.concurrent.CountDownLatch;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 AMX must be "booted" before use.  Use {@link #bootAMX} to start AMX either in-process
 or remotely.
 */
@org.glassfish.external.arc.Taxonomy(stability = org.glassfish.external.arc.Stability.UNCOMMITTED)
public final class AMXBooter
{
    private AMXBooter()
    {
    }

    /**
        Callback for {@link MBeanListener} that waits for the BootAMXMBean to appear;
        it always will load early in server startup. Once it has loaded, AMX can be booted
        via {@link #bootAMX}.  A client should normally just call {@link #bootAMX}, but
        this callback may be suclassed if desired, and used as a trigger to
        boot AMX and then take other dependent actions.
     */
    public static class BootAMXCallback extends MBeanListener.CallbackImpl
    {
        private final MBeanServerConnection mConn;
        public BootAMXCallback(final MBeanServerConnection conn)
        {
            mConn = conn;
        }

        @Override
        public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener)
        {
            super.mbeanRegistered(objectName, listener);
            mLatch.countDown();
        }
    }

    /**
    Ensure that AMX is loaded and ready to use.  This method returns only when all
    AMX subsystems have been loaded.
    It can be called more than once without ill effect, subsequent calls are ignored.
    @param conn connection to the MBeanServer
    @return the ObjectName of {@link DomainRoot}
     */
    public static ObjectName bootAMX(final MBeanServerConnection conn)
    {
        ObjectName domainRootObjectName = AMXUtil.findDomainRoot(conn);

        if (domainRootObjectName == null)
        {
            // wait for the BootAMXMBean to be available (loads at startup)
            final BootAMXCallback callback = new BootAMXCallback(conn);
            MBeanListener.listenForBootAMX(conn, callback);
            callback.await(); // block until the MBean appears

            AMXUtil.invokeBootAMX(conn);
            domainRootObjectName = AMXUtil.invokeWaitAMXReady(conn);
        }
        else
        {
            AMXUtil.invokeWaitAMXReady(conn);
        }
        return domainRootObjectName;
    }
}




















