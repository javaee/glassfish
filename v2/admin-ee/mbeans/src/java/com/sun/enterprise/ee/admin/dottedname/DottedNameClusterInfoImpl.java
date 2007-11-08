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

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;
import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.admin.util.ArrayConversion;
import com.sun.enterprise.admin.dottedname.DottedNameServerInfo;
import com.sun.enterprise.admin.dottedname.DottedNameLogger;

import javax.management.*;
import java.util.*;
import java.io.IOException;


/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 2, 2004
 * @version $Revision: 1.2 $
 */
public class DottedNameClusterInfoImpl implements DottedNameClusterInfo{
	final MBeanServerConnection	mConn;
    public DottedNameClusterInfoImpl( final MBeanServerConnection conn ){
        mConn	= conn;
    }

    ObjectName getConfigsObjectName() throws MalformedObjectNameException {
        return( new ObjectName( "com.sun.appserv:type=configs,category=config" )  );
    }

    Set _getConfigNames() throws ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException,
        MalformedObjectNameException, AttributeNotFoundException {
        // we can't use a proxy; it won't work when the method name starts with "get", apparently
        // thinking it's an Attribute
        final ObjectName []	configObjectNames	=
            (ObjectName [])mConn.invoke( getConfigsObjectName(), "getConfig", null, null );

        final HashSet	configNames	= new HashSet();
        for( int i = 0; i < configObjectNames.length; ++i ){
            final String	name	= (String)mConn.getAttribute( configObjectNames[ i ], "name" );
            configNames.add( name );
        }

        return( configNames );
    }

    public Set getConfigNames() throws DottedNameServerInfo.UnavailableException {
        final Set	namesSet;
        try{
            namesSet	= _getConfigNames();
        } catch( Exception e ){
            throw new DottedNameServerInfo.UnavailableException( e.getCause() );
        }

        return( namesSet );
    }

    // used to create a proxy to clusters mbean
    private interface MyClusters {
        String []	listClustersAsString(String targetName, boolean withStatus);
    };

    Set _getClusterNames() throws
            ReflectionException, InstanceNotFoundException, MBeanException, java.io.IOException {

        final String [] domains = new String[] {MBeanRegistryFactory.getAdminContext().getDomainName()};
        final MBeanRegistry reg = MBeanRegistryFactory.getAdminMBeanRegistry();
        final MyClusters clusters	= (MyClusters)
            MBeanServerInvocationHandler.newProxyInstance( mConn, reg.getMbeanObjectName("clusters", domains) , MyClusters.class, false );
        final String []	names	= clusters.listClustersAsString("domain", false);
        return( ArrayConversion.toSet( names ) );
    }

    public Set getClusterNames() throws DottedNameServerInfo.UnavailableException {
        final Set	namesSet;
        try{
            namesSet	= _getClusterNames();
        }
        catch( Exception e ){
            throw new DottedNameServerInfo.UnavailableException( e.getCause() );
        }
        return( namesSet );
    }

    private interface MyCluster{
        String []   listServerInstancesAsString(boolean andStatus);
        String[]    listResourceReferencesAsString();
        String[]    listApplicationReferencesAsString();
    }

    String[] _getServerNamesForCluster(final String clusterName)
            throws ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException{

        final ObjectName clusterObjectName = getClusterObjectName(clusterName);
        final MyCluster cluster	= (MyCluster)
                    MBeanServerInvocationHandler.newProxyInstance( mConn, clusterObjectName, MyCluster.class, false );
        return cluster.listServerInstancesAsString(false);
    }

    public String[] getServerNamesForCluster(final String clusterName) throws Exception {
        final String[]	namesSet;
        try{
            namesSet	= _getServerNamesForCluster(clusterName);
        }
        catch( Exception e ){
            throw new DottedNameServerInfo.UnavailableException( e.getCause() );
        }
        return( namesSet );
    }

    public boolean isClusteredInstance(final String serverName) {
        try {
            final Iterator iter = getClusterNames().iterator();
            while(iter.hasNext()){
                final String[] serverNames = getServerNamesForCluster((String)iter.next());
                if(serverNames != null){
                    for(int i = 0; i<serverNames.length; i++){
                        if(serverNames[i].equals(serverName)){
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            DottedNameLogger.logException(e);
        }
        return false;
    }

    public String getConfigNameForCluster(final String clusterName) throws DottedNameServerInfo.UnavailableException {
        final ObjectName clusterObjectName	= getClusterObjectName(clusterName);
        final String	configName;
        try {
            configName	= (String)mConn.getAttribute( clusterObjectName, "config_ref" );
        }catch( Exception e ){
            throw new DottedNameServerInfo.UnavailableException( e.getCause() );
        }
        return( configName );
    }

    private ObjectName getClusterObjectName(final String clusterName){
        final String [] domains = new String[] {
                MBeanRegistryFactory.getAdminContext().getDomainName(),
                clusterName,
                "config"
                };
        final MBeanRegistry reg = MBeanRegistryFactory.getAdminMBeanRegistry();
        return reg.getMbeanObjectName("cluster", domains);
    }

    public String[] getClusterNamesForConfig(final String configName) throws DottedNameServerInfo.UnavailableException {
        final java.util.Iterator iter			= getClusterNames().iterator();
        final java.util.ArrayList	namesOut	= new java.util.ArrayList();

        while ( iter.hasNext() ) {
            final String	clusterName	= (String)iter.next();
            if ( configName.equals( getConfigNameForCluster( clusterName ) ) ) {
                namesOut.add( clusterName );
            }
        }

        final String []	namesOutArray	= new String [ namesOut.size() ];
        namesOut.toArray( namesOutArray );

        return( namesOutArray );
    }

    public Set getResourceNamesForCluster(final String clusterName)
                throws  ReflectionException,
                InstanceNotFoundException,
                MBeanException,
                java.io.IOException,
                DottedNameServerInfo.UnavailableException{

        final ObjectName clusterObjectName	= getClusterObjectName(clusterName);
        final MyCluster cluster	= (MyCluster)
                    MBeanServerInvocationHandler.newProxyInstance( mConn, clusterObjectName, MyCluster.class, false );
        final String[]	resourceNames = cluster.listResourceReferencesAsString();
        return (ArrayConversion.toSet(resourceNames )) ;
    }

    public Set getApplicationNamesForCluster(final String clusterName)
            throws  ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException,
            DottedNameServerInfo.UnavailableException{


        final ObjectName clusterObjectName	= getClusterObjectName(clusterName);
        final MyCluster cluster	= (MyCluster)
                    MBeanServerInvocationHandler.newProxyInstance( mConn, clusterObjectName, MyCluster.class, false );
        final String[]	appNames = cluster.listApplicationReferencesAsString();
        return (ArrayConversion.toSet(appNames ) );
    }

    private interface MyResources {
        ObjectName[]    listReferencees(String resName);
        String[]        listResourceReferencesAsString(String target);
    }

    public String[] getTargetsSharingResource(final String resourceName)
            throws ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException,
            AttributeNotFoundException,
            DottedNameServerInfo.UnavailableException, MalformedObjectNameException {

        final MyResources resources	= getResourcesProxy();
        final ObjectName[] targetObjectNames = resources.listReferencees(resourceName);
        return getTargets(targetObjectNames);
    }

    private interface MyConfig{
        ObjectName[] listReferencees();
    }
    public String[] getTargetsSharingConfig(final String configName)
            throws Exception
    {
        final MyConfig config = getConfigProxy(configName);
        final ObjectName[] targetObjectNames = config.listReferencees();

        return getTargets(targetObjectNames);
    }

    private interface MyApplications {
        ObjectName[] listReferencees(String appName);
        String[]     listApplicationReferencesAsString(String target);
    }

    public String[] getTargetsSharingApplication(final String applicationName)
            throws ReflectionException,
            InstanceNotFoundException,
            MBeanException,
            java.io.IOException,
            AttributeNotFoundException,
            DottedNameServerInfo.UnavailableException, MalformedObjectNameException
    {

        final MyApplications apps	= getApplicationsProxy();
        final ObjectName[] targetObjectNames = apps.listReferencees(applicationName);

        return getTargets(targetObjectNames);
    }

    public String[] getAllResourceNames() {
        Set<String> resNames = new HashSet<String>();
        
        try {
            String[] res;
            final Iterator i= getAllTargets().iterator();
            while(i.hasNext()){
                res = getResourcesProxy().listResourceReferencesAsString((String)i.next());
                
                if(res == null)
                    continue;
                
                for(String s : res)
                    resNames.add(s);
            }
        } catch (Exception e) {
            DottedNameLogger.logException(e);
        }
        return (String[])resNames.toArray(new String[resNames.size()]);
    }

    private Collection getAllTargets() throws DottedNameServerInfo.UnavailableException, MalformedObjectNameException {
        final Vector names = new Vector();
        Iterator iter = getClusterNames().iterator();
        while(iter.hasNext()){
            names.add(iter.next());
        }

        iter = getUnclusteredInstancesNames().iterator();
        while(iter.hasNext()){
            names.add(iter.next());
        }
        return names;
    }

    private  interface myServers{
        String[] listUnclusteredServerInstancesAsString(boolean andStatus);
    }

    private Collection getUnclusteredInstancesNames() throws MalformedObjectNameException {
        final ObjectName obj = new ObjectName("com.sun.appserv:type=servers,category=config");
        final myServers servers = (myServers) MBeanServerInvocationHandler.newProxyInstance(
                                mConn, obj, myServers.class, false );
        return ArrayConversion.toSet(servers.listUnclusteredServerInstancesAsString(false));
    }


    public String[] getAllApplicationNames() throws MalformedObjectNameException {
       Set<String> appsNames = new HashSet<String>();
       try {
            String[] apps;
            final Iterator i= getAllTargets().iterator();
            while(i.hasNext()){
                apps = getApplicationsProxy().listApplicationReferencesAsString((String)i.next());

                if(apps == null)
                    continue;
                
                for(String s : apps)
                    appsNames.add(s);
            }
       } catch (Exception e) {
            DottedNameLogger.logException(e);
        }
        return (String[])appsNames.toArray(new String[appsNames.size()]);
    }

    private MyResources getResourcesProxy() throws MalformedObjectNameException {
        final ObjectName obj = new ObjectName("com.sun.appserv:type=resources,category=config");
        return (MyResources)
            MBeanServerInvocationHandler.newProxyInstance(
                    mConn, obj, MyResources.class, false );
    }

    private MyApplications getApplicationsProxy() throws MalformedObjectNameException {
        final ObjectName obj = new ObjectName("com.sun.appserv:type=applications,category=config");
        return (MyApplications)
            MBeanServerInvocationHandler.newProxyInstance(
                    mConn, obj , MyApplications.class, false );
    }

    private MyConfig getConfigProxy(final String configName) throws MalformedObjectNameException {
        final ObjectName obj = new ObjectName("com.sun.appserv:type=config,name="+configName+",category=config");
        return (MyConfig)
                MBeanServerInvocationHandler.newProxyInstance(
                     mConn, obj , MyConfig.class, false );
    }

    private String[] getTargets(final ObjectName[] targetObjectNames)
            throws ReflectionException, IOException,
            InstanceNotFoundException, MBeanException,
            AttributeNotFoundException
    {
        final Vector targets = new Vector();
        for(int i =0; i<targetObjectNames.length; i++){
            targets.add( mConn.getAttribute(targetObjectNames[i],"name") );
        }
        return (String[]) targets.toArray(new String[targets.size()]);
    }
}
