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

package com.sun.enterprise.ee.admin.dottedname;

import com.sun.enterprise.admin.dottedname.DottedNameServerInfoImpl;
import com.sun.enterprise.admin.dottedname.DottedNameServerInfo;
import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.util.ArrayConversion;

import javax.management.*;
import java.util.Set;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 7, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameServerInfoImplEE extends DottedNameServerInfoImpl implements DottedNameServerInfoEE{
    MBeanServerConnection mConn;
    public DottedNameServerInfoImplEE(final MBeanServerConnection conn) {
        super(conn);
        mConn=conn;
    }

    protected Set _getServerNames()
    		throws ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException{

        final MyController controller = getServersController();
        final String []	names	= controller.listServerInstancesAsString("domain", false);
        return( ArrayConversion.toSet( names ) );
    }

    private MyController getServersController() {
        final String [] domains = new String[] {MBeanRegistryFactory.getAdminContext().getDomainName()};
        final MBeanRegistry reg = MBeanRegistryFactory.getAdminMBeanRegistry();
        return (MyController)
            MBeanServerInvocationHandler.newProxyInstance(
                        mConn, reg.getMbeanObjectName("servers", domains) ,
                                MyController.class, false );
    }

    private interface MyController{
        String[]    listServerInstancesAsString(String domain, boolean andStatus );
        String[]    listResourceReferencesAsString();
        String[]    listApplicationReferencesAsString();
        ObjectName[]    listUnclusteredServerInstances(boolean excludeDAS);
    }

    public Set getUnclusteredServerNames(){
        final MyController c = getServersController();
        final ObjectName[] names = c.listUnclusteredServerInstances(false);
        final String[] sNames = new String[names.length];
        for(int i=0; i<names.length;i++){
            sNames[i] = names[i].getKeyProperty("name");
        }
        return ArrayConversion.toSet(sNames);
    }

    public Set getResourceNamesForServer(final String serverName)
            throws ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException,
            DottedNameServerInfo.UnavailableException,
            MalformedObjectNameException
    {
        final MyController server	= getServerController(serverName);
        final String[]	resourceNames = server.listResourceReferencesAsString();
        return ( ArrayConversion.toSet( resourceNames ) );
    }

    /**
     * collection of applications referenced by the standalone server
     */

    public Set getApplicationNamesForServer(final String serverName)
            throws ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException,
            DottedNameServerInfo.UnavailableException,
            MalformedObjectNameException
    {

        final MyController server	= getServerController(serverName);
        final String[]	appNames = server.listApplicationReferencesAsString();
        return ( ArrayConversion.toSet( appNames ) );
    }

    private MyController getServerController(final String serverName)
            throws MalformedObjectNameException
    {
        final ObjectName server = new ObjectName("com.sun.appserv:type=server,name="+
                                serverName+",category=config");
        return (MyController)
            MBeanServerInvocationHandler.newProxyInstance(
                        mConn, server,MyController.class, false );
    }
}
