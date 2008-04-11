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
package com.sun.appserv.management.config;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ProxyFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.reflect.Constructor;



/**
	<b>Private interface, DO NOT USE</b>
*/
public final class OfflineConfigIniter 
{
    private final MBeanServer   mServer;
    private final File          mDomainXML;
    private final ObjectName    mLoader;
    private final DomainRoot    mDomainRoot;
    
    private static final String LOADER_CLASSNAME    =
        "com.sun.enterprise.management.offline.OfflineLoader";
    
    private final Class[]   LOADER_CONSTRUCTOR_SIG    = new Class[]
    {
        MBeanServer.class,
        File.class,
    };
    
        public
    OfflineConfigIniter(
        final MBeanServer server,
        final File        domainXML )
    {
        mServer     = server;
        mDomainXML  = domainXML;
        mLoader     = initLoader();
        
        mDomainRoot = ProxyFactory.getInstance( server ).getDomainRoot();
    }
    
        public DomainRoot
    getDomainRoot()
    {
        return mDomainRoot;
    }
    
    public static final String  NAME    = "offline-mbean-loader";
    
        private ObjectName
    initLoader()
    {
        ObjectName  loaderObjectName    = null;
        
        try
        {
            // can't import it because it's this code is in client API.
            // We want this feature to work only if the requisite classes are present,
            // but we can't include them.
            //
            final Class         loaderClass = Class.forName( LOADER_CLASSNAME );
            final Constructor   constructor =
                loaderClass.getConstructor( LOADER_CONSTRUCTOR_SIG );
            
            final Object    loader      = constructor.newInstance( mServer, mDomainXML );
            
            final String domain = AMX.JMX_DOMAIN + "-support";
            loaderObjectName  = Util.newObjectName( domain, Util.makeNameProp( NAME ) );
            loaderObjectName  =
                mServer.registerMBean( loader, loaderObjectName ).getObjectName();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            
            throw new RuntimeException( t );
        }
        
        return loaderObjectName;
    }
}

















