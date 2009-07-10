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
AMX must be "booted" before use.
 */
public final class AMXBooter
{
    private AMXBooter()
    {
    }

    /** wait for the BootAMXMBean to appear; it always will load early in server startup */
    private static final class BootAMXCallback extends MBeanListener.CallbackImpl
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
    Ensure that AMX is loaded and ready to go.  Can be called more than once.
    @param conn connection to the MBeanServer
    @return the ObjectName of {@link DomainRoot}
     */
    public static ObjectName bootAMX(final MBeanServerConnection conn)
    {
        ObjectName domainRootObjectName = findDomainRoot(conn);

        if (domainRootObjectName == null)
        {
            final BootAMXCallback callback = new BootAMXCallback(conn);
            MBeanListener.listenForBootAMX(conn, callback);
            // block until ready
            callback.await();

            // start AMX and wait for it to be ready
            try
            {
                conn.invoke(BootAMXMBean.OBJECT_NAME, BootAMXMBean.BOOT_AMX_OPERATION_NAME, null, null);
                domainRootObjectName = MBeanListener.waitAMXReady(conn);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return domainRootObjectName;
    }

    /**
    @return the ObjectName of DomainRoot if it exists, otherwise null
     */
    public static ObjectName findDomainRoot(final MBeanServerConnection conn)
    {
        final ObjectName objectName = AMXValues.domainRoot();
        try
        {
            if (!conn.isRegistered(objectName))
            {
                return null;
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }

        return objectName;
    }

}



















