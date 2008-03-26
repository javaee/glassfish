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
 
/*
 */

package org.glassfish.admin.amx.loader;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;

import org.glassfish.admin.amx.util.Issues;

import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.util.misc.TimingDelta;

/**
	Used internally to work around problems with cascaded MBeans.
 */
public final class LoadAMX
{
    private LoadAMX() {}
    private static ObjectName LOADER_OBJECTNAME = null;
    
    private static final String AMX_LOADER_DEFAULT_OBJECTNAME    =
        "amx-support:name=mbean-loader";

        public static synchronized ObjectName
    loadAMX( final MBeanServer mbeanServer )
    {
        if ( LOADER_OBJECTNAME == null )
        {
            final boolean inDAS = true;
            Issues.getAMXIssues().notDone( "LoadAMX.loadAMX(): determine if this is the DAS" );
            
            if ( inDAS )
            {
                final Loader loader = new Loader();
                
                final ObjectName tempObjectName  = JMXUtil.newObjectName( AMX_LOADER_DEFAULT_OBJECTNAME );
                
                try
                {
                    LOADER_OBJECTNAME  =
                        mbeanServer.registerMBean( loader, tempObjectName ).getObjectName();
                }
                catch( JMException e )
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return LOADER_OBJECTNAME;
    }
}

