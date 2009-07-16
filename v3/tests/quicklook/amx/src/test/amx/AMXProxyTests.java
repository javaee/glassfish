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
package amxtest;

import org.testng.annotations.*;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.glassfish.admin.amx.intf.config.*;
import org.glassfish.admin.amx.core.*;
import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.config.*;
//import org.glassfish.admin.amx.j2ee.*;
import org.glassfish.admin.amx.monitoring.*;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.logging.Logging;


/** 
    These tests are designed to exercise the AMXProxyHandler code.
 */
//@Test(groups={"amx"}, description="AMXProxy tests", sequential=false, threadPoolSize=5)
@Test(
    sequential=false, threadPoolSize=5,
    groups =
    {
        "amx"
    },
    description = "AMXProxyHandler tests"
)
public final class AMXProxyTests extends AMXTestBase
{
    public AMXProxyTests()
    {
    }

    private boolean isGetter( final Method m )
    {
        return m.getName().startsWith("get") && m.getParameterTypes().length == 0;
    }
    
    
    private <T extends AMXProxy> void testProxyInterface(final AMXProxy amx, Class<T> clazz)
    {
        final List<String> problems = _testProxyInterface(amx, clazz);
        assert problems.size() == 0 : CollectionUtil.toString( problems, "\n" );
    }
    
    
    private <T extends AMXProxy> List<String> _testProxyInterface(final AMXProxy amxIn, Class<T> clazz)
    {
        final List<String> problems = new ArrayList<String>();
        
        final T amx = amxIn.as(clazz);
        
        final String nameProp = amx.nameProp();
        assert Util.getNameProp(amx.objectName()) == nameProp;
        
        assert amx.parentPath() != null;
        assert amx.type() != null;
        assert amx.valid();
        assert amx.childrenSet() != null;
        assert amx.childrenMaps() != null;
        assert amx.attributesMap() != null;
        assert amx.attributeNames() != null;
        assert amx.objectName() != null;
        assert amx.extra() != null;
        
        final Extra extra = amx.extra();
        //assert extra.mbeanServerConnection() == getMBeanServerConnection();
        assert extra.proxyFactory() == getProxyFactory();
        assert extra.java().length() >= 100;
        
        assert extra.mbeanInfo() != null;
        assert extra.interfaceName() != null;
        assert extra.genericInterface() != null;
        assert extra.group() != null;
        assert extra.descriptor() != null;
        extra.isInvariantMBeanInfo();   // just call it
        extra.subTypes();   // just call it
        extra.supportsAdoption();   // just call it
        if ( extra.globalSingleton() )
        {
            assert extra.singleton();
        }
        
        final Method[] methods = clazz.getMethods();
        for( final Method m : methods )
        {
            if ( isGetter(m) )
            {
                try
                {
                    final Object result = m.invoke( amx, (Object[])null);
                }
                catch( final Exception e )
                {
                    problems.add( "Error invoking " + m.getName() + "() on " + amx.objectName() + " = " + e );
                }
            }
        }
        return problems;
    }

    @Test
    public void testDomainRoot()
    {
        final DomainRoot dr = getDomainRootProxy();
        testProxyInterface( dr, DomainRoot.class );
        
        // sanity check:  see that the various attributes are reachable through its proxy
        assert dr.getAMXReady();
        assert dr.getDebugPort() != null;
        assert dr.getApplicationServerFullVersion() != null;
        assert dr.getInstanceRoot() != null;
        assert dr.getDomainDir() != null;
        assert dr.getConfigDir() != null;
        assert dr.getInstallDir() != null;
        assert dr.getUptimeMillis() != null;

    }

    @Test
    public void testExt()
    {
        testProxyInterface( getExt(), Ext.class );
    }

    @Test
    public void testQuery()
    {
        testProxyInterface( getQueryMgr(), Query.class );
    }

    @Test
    public void testBulkAccess()
    {
        testProxyInterface( getDomainRootProxy().getBulkAccess(), BulkAccess.class );
    }

    @Test
    public void testTools()
    {
        testProxyInterface( getDomainRootProxy().getTools(), Tools.class );
    }

    @Test
    public void testMonitoringRoot()
    {
        testProxyInterface( getDomainRootProxy().getMonitoringRoot(), MonitoringRoot.class );
    }

    @Test
    public void testSystemInfo()
    {
        testProxyInterface( getDomainRootProxy().getSystemInfo(), SystemInfo.class );
    }

    @Test
    public void testLogging()
    {
        testProxyInterface( getDomainRootProxy().getLogging(), Logging.class );
    }

    @Test
    public void testPathnames()
    {
        testProxyInterface( getDomainRootProxy().getPathnames(), Pathnames.class );
    }

    @Test
    public void testDomainConfig()
    {
        final Domain dc = getDomainConfig();
        testProxyInterface( dc, Domain.class );
        
        assert dc.getApplicationRoot() != null;
        dc.getLocale(); // can be null
        assert dc.getLogRoot() != null;
    }

    @Test
    public void testApplications()
    {
        testProxyInterface( getDomainConfig().getApplications(), Applications.class );
    }

    @Test
    public void testResources()
    {
        testProxyInterface( getDomainConfig().getResources(), Resources.class );
    }

    @Test
    public void testConfigs()
    {
        testProxyInterface( getDomainConfig().getConfigs(), Configs.class );
    }

    @Test
    public void testSystemApplications()
    {
        testProxyInterface( getDomainConfig().getSystemApplications(), SystemApplications.class );
    }

    @Test
    public void testServers()
    {
        testProxyInterface( getDomainConfig().getServers(), Servers.class );
    }

    /** subclass can override to add more */
    protected Interfaces getInterfaces()
    {
        return new Interfaces();
    }
    
    @Test
    public void testAllGenerically()
    {
        final Interfaces interfaces = getInterfaces();
        final List<String> problems = new ArrayList<String>();

        for( final AMXProxy amx : getQueryMgr().queryAll() )
        {
            try
            {
                final List<String> p = _testProxyInterface( amx, interfaces.get(amx.type()) );
                problems.addAll(p);
            }
            catch( final Throwable t )
            {
                final Throwable rootCause = ExceptionUtil.getRootCause(t);
                problems.add( rootCause.getMessage() );
            }
        }

        if ( problems.size() != 0 )
        {
            System.out.println( "\nPROBLEMS:\n" + CollectionUtil.toString(problems, "\n\n") );
        }
        // don't mark this as a failure just yet
        // assert problems.size() == 0;
    }
    
    
/*
where to put this, won't run on web distribution
    @Test
    public void testJ2EEDomain()
    {
        testProxyInterface( getDomainRootProxy().getJ2EEDomain(), J2EEDomain.class );
    }
*/
}




































