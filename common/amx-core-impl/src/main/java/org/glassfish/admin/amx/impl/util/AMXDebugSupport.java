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
package org.glassfish.admin.amx.impl.util;


import org.glassfish.admin.amx.util.AMXDebug;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.FileUtils;
import org.glassfish.admin.amx.impl.loader.BootUtil;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;

import org.glassfish.admin.amx.base.AMXDebugSupportMBean;

/**
 */
public final class AMXDebugSupport
    implements AMXDebugSupportMBean
{
    private final MBeanServer   mServer;
    
    public static final String  NAME    = "debug";
    
        public static ObjectName
    getObjectName()
    {
        final String    domain  =
            BootUtil.getInstance().getAMXSupportJMXDomain();
        
        final String    props   = Util.makeNameProp(NAME);
        
        final ObjectName    objectName  = Util.newObjectName( domain, props );
        
        return objectName;
    }
    
        public
    AMXDebugSupport( final MBeanServer server )
    {
        mServer = server;
    }
    
        private AMXDebug
    getAMXDebug()
    {
        return AMXDebug.getInstance();
    }
    
        public String[]
    getOutputIDs()
    {
        return AMXDebug.getInstance().getOutputIDs();
    }
    
        public boolean
    getDefaultDebug()
    {
        return getAMXDebug().getDefaultDebug();
    }
     
        public void
    setDefaultDebug( boolean debug )
    {
        getAMXDebug().setDefaultDebug( debug );
    } 
    
        public boolean
    getDebug( final String id )
    {
        return getAMXDebug().getDebug( id );
    }
    
        public void
    setDebug( final String id, final boolean debug )
    {
        getAMXDebug().setDebug( id, debug );
    } 
    
        public void
    setAll( final boolean debug )
    {
        getAMXDebug().setAll( debug );
    }
    
        public void
    cleanup()
    {
        getAMXDebug().cleanup();
    }
    
        public String
    getOutputFrom( final String id )
    {
        String  output  = "";
        
        final File  f   = getAMXDebug().getOutputFile( id );
        if ( f != null && f.exists() )
        {
            try
            {
                output  = FileUtils.fileToString( f );
            }
            catch( Exception e )
            {
                output  = "";
            }
        }
        
        return output;
    }
}

























