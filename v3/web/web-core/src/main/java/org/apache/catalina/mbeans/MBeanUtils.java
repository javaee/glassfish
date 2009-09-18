/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.apache.catalina.mbeans;


import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.logging.*;

import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

import org.apache.catalina.Connector;
import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;

import org.glassfish.web.valve.GlassFishValve;

/**
 * Public utility methods in support of the server side MBeans implementation.
 *
 * @author Craig R. McClanahan
 * @author Amy Roh
 * @version $Revision: 1.5 $ $Date: 2006/11/10 18:12:35 $
 */

public class MBeanUtils {

    private static Logger log = Logger.getLogger(MBeanUtils.class.getName());

    private static final ClassLoader serverLifecycleListenerClassLoader =
        ServerLifecycleListener.class.getClassLoader();

    // ------------------------------------------------------- Static Variables


    /**
     * The set of exceptions to the normal rules used by
     * <code>createManagedBean()</code>.  The first element of each pair
     * is a class name, and the second element is the managed bean name.
     */
    private static String exceptions[][] = {
        { "org.apache.ajp.tomcat4.Ajp13Connector",
          "Ajp13Connector" },
        { "com.sun.grizzly.tcp.tomcat4.Ajp13Connector",
          "CoyoteConnector" },
        { "org.apache.catalina.core.StandardDefaultContext",
          "DefaultContext" },
        { "org.apache.catalina.users.JDBCGroup",
          "Group" },
        { "org.apache.catalina.users.JDBCRole",
          "Role" },
        { "org.apache.catalina.users.JDBCUser",
          "User" },
        { "org.apache.catalina.users.MemoryGroup",
          "Group" },
        { "org.apache.catalina.users.MemoryRole",
          "Role" },
        { "org.apache.catalina.users.MemoryUser",
          "User" },
    };


    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = createRegistry();


    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = createServer();


    // --------------------------------------------------------- Static Methods

    /**
     * Translates a string into x-www-form-urlencoded format
     *
     * @param t string to be encoded
     * @return encoded string
     */
    private static final String encodeStr(String t) {
   
        return URLEncoder.encode(t);

    }


    /**
     * Create and return the name of the <code>ManagedBean</code> that
     * corresponds to this Catalina component.
     *
     * @param component The component for which to create a name
     */
    static String createManagedName(Object component) {

        // Deal with exceptions to the standard rule
        String className = component.getClass().getName();
        for (int i = 0; i < exceptions.length; i++) {
            if (className.equals(exceptions[i][0])) {
                return (exceptions[i][1]);
            }
        }

        // Perform the standard transformation
        int period = className.lastIndexOf('.');
        if (period >= 0)
            className = className.substring(period + 1);
        return (className);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Context</code> object.
     *
     * @param context The Context to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Context context)
        throws Exception {

        String mname = createManagedName(context);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(context);
        ObjectName oname = createObjectName(domain, context);
        if( mserver.isRegistered(oname)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Already registered " + oname);
            }
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }

    
    /**
     * Create, register, and return an MBean for this
     * <code>ContextEnvironment</code> object.
     *
     * @param environment The ContextEnvironment to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(ContextEnvironment environment)
        throws Exception {

        String mname = createManagedName(environment);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(environment);
        ObjectName oname = createObjectName(domain, environment);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>ContextResource</code> object.
     *
     * @param resource The ContextResource to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(ContextResource resource)
        throws Exception {

        String mname = createManagedName(resource);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(resource);
        ObjectName oname = createObjectName(domain, resource);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>ContextResourceLink</code> object.
     *
     * @param resourceLink The ContextResourceLink to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(ContextResourceLink resourceLink)
        throws Exception {

        String mname = createManagedName(resourceLink);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(resourceLink);
        ObjectName oname = createObjectName(domain, resourceLink);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }    
    
    
    /**
     * Create, register, and return an MBean for this
     * <code>DefaultContext</code> object.
     *
     * @param context The DefaultContext to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(DefaultContext context)
        throws Exception {

        String mname = createManagedName(context);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(context);
        ObjectName oname = createObjectName(domain, context);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Engine</code> object.
     *
     * @param engine The Engine to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Engine engine)
        throws Exception {

        String mname = createManagedName(engine);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(engine);
        ObjectName oname = createObjectName(domain, engine);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Host</code> object.
     *
     * @param host The Host to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Host host)
        throws Exception {

        String mname = createManagedName(host);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(host);
        ObjectName oname = createObjectName(domain, host);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Loader</code> object.
     *
     * @param loader The Loader to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Loader loader)
        throws Exception {

        String mname = createManagedName(loader);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(loader);
        ObjectName oname = createObjectName(domain, loader);
        if( mserver.isRegistered( oname ))  {
            // side effect: stop it
            mserver.unregisterMBean( oname );
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }

    /**
     * Create, register, and return an MBean for this
     * <code>Logger</code> object.
     *
     * @param logger The Logger to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(org.apache.catalina.Logger logger)
        throws Exception {

        String mname = createManagedName(logger);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(logger);
        ObjectName oname = createObjectName(domain, logger);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Manager</code> object.
     *
     * @param manager The Manager to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Manager manager)
        throws Exception {

        String mname = createManagedName(manager);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(manager);
        ObjectName oname = createObjectName(domain, manager);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>MBeanFactory</code> object.
     *
     * @param factory The MBeanFactory to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(MBeanFactory factory)
        throws Exception {

        String mname = createManagedName(factory);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(factory);
        ObjectName oname = createObjectName(domain, factory);
        if( mserver.isRegistered(oname )) {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>NamingResources</code> object.
     *
     * @param resource The NamingResources to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(NamingResources resource)
        throws Exception {

        String mname = createManagedName(resource);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(resource);
        ObjectName oname = createObjectName(domain, resource);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }

    
    /**
     * Create, register, and return an MBean for this
     * <code>Realm</code> object.
     *
     * @param realm The Realm to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Realm realm)
        throws Exception {

        String mname = createManagedName(realm);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(realm);
        ObjectName oname = createObjectName(domain, realm);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }



    /**
     * Create, register, and return an MBean for this
     * <code>Server</code> object.
     *
     * @param server The Server to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Server server)
        throws Exception {

        String mname = createManagedName(server);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(server);
        ObjectName oname = createObjectName(domain, server);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Service</code> object.
     *
     * @param service The Service to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(Service service)
        throws Exception {

        String mname = createManagedName(service);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(service);
        ObjectName oname = createObjectName(domain, service);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create, register, and return an MBean for this
     * <code>Valve</code> object.
     *
     * @param valve The Valve to be managed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    static ModelMBean createMBean(GlassFishValve valve)
        throws Exception {

        String mname = createManagedName(valve);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception("ManagedBean is not found with "+mname);
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ModelMBean mbean = managed.createMBean(valve);
        ObjectName oname = createObjectName(domain, valve);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return (mbean);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Context</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param context The Context to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Context context)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Host host = (Host)context.getParent();
        Service service = ((Engine)host.getParent()).getService();
        String path = context.getPath();
        if (path.length() < 1)
            path = "/";
        // FIXME 
        name = new ObjectName(domain + ":j2eeType=WebModule,name=//" +
                              host.getName()+ path +
                              ",J2EEApplication=none,J2EEServer=none");
    
        return (name);

    }

    
    /**
     * Create an <code>ObjectName</code> for this
     * <code>Service</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param context The ContextEnvironment to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextEnvironment environment)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Object container = 
                environment.getNamingResources().getContainer();
        if (container instanceof Server) {
            name = new ObjectName(domain + ":type=Environment" + 
                        ",resourcetype=Global,name=" + environment.getName());
        } else if (container instanceof Context) {        
            String path = ((Context)container).getPath();
            if (path.length() < 1)
                path = "/";
            Host host = (Host) ((Context)container).getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Environment" + 
                        ",resourcetype=Context,path=" + path + 
                        ",host=" + host.getName() +
                        ",name=" + environment.getName());
        } else if (container instanceof DefaultContext) {
            container = ((DefaultContext)container).getParent();
            if (container instanceof Host) {
                Host host = (Host) container;
                Service service = ((Engine)host.getParent()).getService();
                name = new ObjectName(domain + ":type=Environment" + 
                        ",resourcetype=HostDefaultContext,host=" + host.getName() +
                        ",name=" + environment.getName());
            } else if (container instanceof Engine) {
                Engine engine = (Engine) container;
                Service service = engine.getService();
                name = new ObjectName(domain + ":type=Environment" + 
                        ",resourcetype=ServiceDefaultContext,name=" + environment.getName());
            }
        }
        
        return (name);

    }
    
    
    /**
     * Create an <code>ObjectName</code> for this
     * <code>ContextResource</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param resource The ContextResource to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextResource resource)
        throws MalformedObjectNameException {

        ObjectName name = null;
        String encodedResourceName = encodeStr(resource.getName());
        Object container = 
                resource.getNamingResources().getContainer();
        if (container instanceof Server) {        
            name = new ObjectName(domain + ":type=Resource" +
                        ",resourcetype=Global,class=" + resource.getType() + 
                        ",name=" + encodedResourceName);
        } else if (container instanceof Context) {                    
            String path = ((Context)container).getPath();
            if (path.length() < 1)
                path = "/";
            Host host = (Host) ((Context)container).getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Resource" +
                        ",resourcetype=Context,path=" + path + 
                        ",host=" + host.getName() +
                        ",class=" + resource.getType() +
                        ",name=" + encodedResourceName);
        } else if (container instanceof DefaultContext) {            
            container = ((DefaultContext)container).getParent();
            if (container instanceof Host) {
                Host host = (Host) container;
                Service service = ((Engine)host.getParent()).getService();
                name = new ObjectName(domain + ":type=Resource" + 
                        ",resourcetype=HostDefaultContext,host=" + host.getName() +
                        ",class=" + resource.getType() +
                        ",name=" + encodedResourceName);
            } else if (container instanceof Engine) {
                Engine engine = (Engine) container;
                Service service = engine.getService();
                name = new ObjectName(domain + ":type=Resource" + 
                        ",resourcetype=ServiceDefaultContext,class=" + resource.getType() +
                        ",name=" + encodedResourceName);
            }
        }
        
        return (name);

    }
  
    
     /**
     * Create an <code>ObjectName</code> for this
     * <code>ContextResourceLink</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param resourceLink The ContextResourceLink to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextResourceLink resourceLink)
        throws MalformedObjectNameException {

        ObjectName name = null;
        String encodedResourceLinkName = encodeStr(resourceLink.getName());        
        Object container = 
                resourceLink.getNamingResources().getContainer();
        if (container instanceof Server) {        
            name = new ObjectName(domain + ":type=ResourceLink" +
                        ",resourcetype=Global" + 
                        ",name=" + encodedResourceLinkName);
        } else if (container instanceof Context) {                    
            String path = ((Context)container).getPath();
            if (path.length() < 1)
                path = "/";
            Host host = (Host) ((Context)container).getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=ResourceLink" +
                        ",resourcetype=Context,path=" + path + 
                        ",host=" + host.getName() +
                        ",name=" + encodedResourceLinkName);
        } else if (container instanceof DefaultContext) {            
            container = ((DefaultContext)container).getParent();
            if (container instanceof Host) {
                Host host = (Host) container;
                Service service = ((Engine)host.getParent()).getService();
                name = new ObjectName(domain + ":type=ResourceLink" + 
                        ",resourcetype=HostDefaultContext,host=" + host.getName() +
                        ",name=" + encodedResourceLinkName);
            } else if (container instanceof Engine) {
                Engine engine = (Engine) container;
                Service service = engine.getService();
                name = new ObjectName(domain + ":type=ResourceLink" + 
                        ",resourcetype=ServiceDefaultContext,name=" + encodedResourceLinkName);
            }
        }
        
        return (name);

    }
    
    
    /**
     * Create an <code>ObjectName</code> for this
     * <code>DefaultContext</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param context The DefaultContext to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              DefaultContext context)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Container container = context.getParent();
        if (container instanceof Host) {
            Host host = (Host) container;
            Service service = ((Engine)host.getParent()).getService();
            name = new ObjectName(domain + ":type=DefaultContext,host=" +
                              host.getName());
        } else if (container instanceof Engine) {
            Engine engine = (Engine) container;
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=DefaultContext");
        }

        return (name);

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>Engine</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param engine The Engine to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Engine engine)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Engine");
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Host</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param host The Host to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       Host host)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Engine engine = (Engine)host.getParent();
        Service service = engine.getService();
        name = new ObjectName(domain + ":type=Host,host=" +
                              host.getName());
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Loader</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param loader The Loader to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       Loader loader)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Container container = loader.getContainer();

        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            name = new ObjectName(domain + ":type=Loader");
        } else if (container instanceof Host) {
            Engine engine = (Engine) container.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Loader,host=" +
                              container.getName());
        } else if (container instanceof Context) {
            String path = ((Context)container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) container.getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Loader,path=" + path +
                              ",host=" + host.getName());
        } else if (container == null) {
            // What is that ???
            DefaultContext defaultContext = loader.getDefaultContext();
            if (defaultContext != null) {
                Container parent = defaultContext.getParent();
                if (parent instanceof Engine) {
                    Service service = ((Engine)parent).getService();
                    name = new ObjectName(domain + ":type=DefaultLoader");
                } else if (parent instanceof Host) {
                    Engine engine = (Engine) parent.getParent();
                    Service service = engine.getService();
                    name = new ObjectName(domain + ":type=DefaultLoader,host=" +
                            parent.getName());
                }
            }
        }

        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Logger</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param logger The Logger to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       org.apache.catalina.Logger logger)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Container container = logger.getContainer();

        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            name = new ObjectName(domain + ":type=Logger");
        } else if (container instanceof Host) {
            Engine engine = (Engine) container.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Logger,host=" +
                              container.getName());
        } else if (container instanceof Context) {
            String path = ((Context)container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) container.getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Logger,path=" + path +
                              ",host=" + host.getName());
        }

        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Manager</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param manager The Manager to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Manager manager)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Container container = manager.getContainer();

        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            name = new ObjectName(domain + ":type=Manager");
        } else if (container instanceof Host) {
            Engine engine = (Engine) container.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Manager,host=" +
                              container.getName());
        } else if (container instanceof Context) {
            String path = ((Context)container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) container.getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Manager,path=" + path +
                              ",host=" + host.getName());
        } else if (container == null) {
            DefaultContext defaultContext = manager.getDefaultContext();
            if (defaultContext != null) {
                Container parent = defaultContext.getParent();
                if (parent instanceof Engine) {
                    Service service = ((Engine)parent).getService();
                    name = new ObjectName(domain + ":type=DefaultManager");
                } else if (parent instanceof Host) {
                    Engine engine = (Engine) parent.getParent();
                    Service service = engine.getService();
                    name = new ObjectName(domain + ":type=DefaultManager,host=" +
                            parent.getName());
                }
            }
        }

        return (name);

    }
    
    
    /**
     * Create an <code>ObjectName</code> for this
     * <code>Server</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param resources The NamingResources to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              NamingResources resources)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Object container = resources.getContainer();        
        if (container instanceof Server) {        
            name = new ObjectName(domain + ":type=NamingResources" + 
                        ",resourcetype=Global");
        } else if (container instanceof Context) {        
            String path = ((Context)container).getPath();
            if (path.length() < 1)
                path = "/";
            Host host = (Host) ((Context)container).getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=NamingResources" + 
                        ",resourcetype=Context,path=" + path + 
                        ",host=" + host.getName());
        } else if (container instanceof DefaultContext) {
            container = ((DefaultContext)container).getParent();
            if (container instanceof Host) {
                Host host = (Host) container;
                Service service = ((Engine)host.getParent()).getService();
                name = new ObjectName(domain + ":type=NamingResources" + 
                        ",resourcetype=HostDefaultContext,host=" + host.getName());
            } else if (container instanceof Engine) {
                Engine engine = (Engine) container;
                Service service = engine.getService();
                name = new ObjectName(domain + ":type=NamingResources" + 
                        ",resourcetype=ServiceDefaultContext");
            }
        }
        
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>MBeanFactory</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param factory The MBeanFactory to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              MBeanFactory factory)
        throws MalformedObjectNameException {

        ObjectName name = new ObjectName(domain + ":type=MBeanFactory");

        return (name);

    }

    
    /**
     * Create an <code>ObjectName</code> for this
     * <code>Realm</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param realm The Realm to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Realm realm)
        throws MalformedObjectNameException {

        ObjectName name = null;
        Container container = realm.getContainer();

        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            name = new ObjectName(domain + ":type=Realm");
        } else if (container instanceof Host) {
            Engine engine = (Engine) container.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Realm,host=" +
                              container.getName());
        } else if (container instanceof Context) {
            String path = ((Context)container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) container.getParent();
            Engine engine = (Engine) host.getParent();
            Service service = engine.getService();
            name = new ObjectName(domain + ":type=Realm,path=" + path +
                              ",host=" + host.getName());
        }

        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Server</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param server The Server to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Server server)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Server");
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Service</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param service The Service to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                              Service service)
        throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Service,serviceName=" + 
                            service.getName());
        return (name);

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Valve</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param valve The Valve to be named
     *
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       GlassFishValve valve)
        throws MalformedObjectNameException {
        if( valve instanceof ValveBase ) {
            ObjectName name=((ValveBase)valve).getObjectName();
            if( name != null )
                return name;
        }

        ObjectName name = null;
        Container container = null;
        String className=valve.getClass().getName();
        int period = className.lastIndexOf('.');
        if (period >= 0)
            className = className.substring(period + 1);
        if( valve instanceof Contained ) {
            container = ((Contained)valve).getContainer();
        }
        if( container == null ) {
            throw new MalformedObjectNameException(
                               "Cannot create mbean for non-contained valve " +
                               valve);
        }        
        if (container instanceof Engine) {
            Service service = ((Engine)container).getService();
            String local="";
            int seq = getSeq(local);
            String ext="";
            if( seq > 0 ) {
                ext=",seq=" + seq;
            }
            name = new ObjectName(domain + ":type=Valve,name=" + className + 
                                    ext + local );
        } else if (container instanceof Host) {
            Service service = ((Engine)container.getParent()).getService();
            String local=",host=" +container.getName();
            int seq = getSeq(local);
            String ext="";
            if( seq > 0 ) {
                ext=",seq=" + seq;
            }
            name = new ObjectName(domain + ":type=Valve,name=" + className + 
                                    ext + local );
        } else if (container instanceof Context) {
            String path = ((Context)container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) container.getParent();
            Service service = ((Engine) host.getParent()).getService();
            String local=",path=" + path + ",host=" +
                    host.getName();
            int seq = getSeq(local);
            String ext="";
            if( seq > 0 ) {
                ext=",seq=" + seq;
            }
            name = new ObjectName(domain + ":type=Valve,name=" + className + 
                                    ext + local );
        }

        return (name);

    }

    static Hashtable seq=new Hashtable();
    static int getSeq( String key ) {
        int i[]=(int [])seq.get( key );
        if (i == null ) {
            i=new int[1];
            i[0]=0;
            seq.put( key, i);
        } else {
            i[0]++;
        }
        return i[0];
    }

    /**
     * Create and configure (if necessary) and return the registry of
     * managed object descriptions.
     */
    public synchronized static Registry createRegistry() {

        if (registry == null) {
            registry = Registry.getRegistry(null, null);
            ClassLoader cl = serverLifecycleListenerClassLoader;

            registry.loadDescriptors("org.apache.catalina.mbeans",  cl);
            registry.loadDescriptors("org.apache.catalina.authenticator", cl);
            registry.loadDescriptors("org.apache.catalina.core", cl);
            registry.loadDescriptors("org.apache.catalina", cl);
            registry.loadDescriptors("org.apache.catalina.deploy", cl);
            registry.loadDescriptors("org.apache.catalina.loader", cl);
            registry.loadDescriptors("org.apache.catalina.logger", cl);
            registry.loadDescriptors("org.apache.catalina.realm", cl);
            registry.loadDescriptors("org.apache.catalina.session", cl);
            registry.loadDescriptors("org.apache.catalina.startup", cl);
            registry.loadDescriptors("org.apache.catalina.users", cl);
            
            registry.loadDescriptors("org.apache.catalina.valves",  cl);
            registry.loadDescriptors("org.apache.catalina.connector", cl);
        }
        return (registry);

    }


    /**
     * Load an MBean descriptor resource.
     */
    public synchronized static void loadMBeanDescriptors(String resource) {
        try {
            URL url = ServerLifecycleListener.class.getResource(resource);
            if (url != null) {
                InputStream stream = url.openStream();
                Registry.getRegistry(null, null).loadMetadata(stream);
                stream.close();
            } else {
                // XXX: i18n
                log.warning("MBean descriptors not found:" + resource);
            }
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error during loadMBeanDescriptors", t);
        }
    }


    /**
     * Create and configure (if necessary) and return the
     * <code>MBeanServer</code> with which we will be
     * registering our <code>ModelMBean</code> implementations.
     */
    public synchronized static MBeanServer createServer() {
        if (mserver == null) {
            try {
                //Trace.parseTraceProperties();
                //mserver = MBeanServerFactory.createMBeanServer();
                mserver = Registry.getRegistry(null, null).getMBeanServer();
            } catch (Throwable t) {
                log.log(Level.SEVERE, "Error during createServer", t);
                System.exit(1);
            }
        }
        return (mserver);

    }


    /**
     * Create a RMI adapter [MX4J specific].
     */
    public static void createRMIAdaptor(String adaptorType, String host, int port)
        throws Exception {

        String namingProviderObjectName = null;
        String namingProviderClassName = null;
        String adaptorObjectName = null;
        String adaptorClassName = null;
        String adaptorMbeanClassName = null;
        boolean delay = false;
        String jndiName = "jrmp";
        String contextFactory = null;
        String providerUrl = null;

		if ((host == null) || (host.trim().length() == 0))
			host = "localhost";

        if (adaptorType.equals("jrmp")) {
            namingProviderObjectName = "Naming:type=rmiregistry";
            namingProviderClassName = "mx4j.tools.naming.NamingService";
            adaptorObjectName = "Adaptor:protocol=JRMP";
            adaptorClassName = "mx4j.adaptor.rmi.jrmp.JRMPAdaptor";
            adaptorMbeanClassName = "mx4j.adaptor.rmi.jrmp.JRMPAdaptorMBean";
            contextFactory = 
                "com.sun.jndi.rmi.registry.RegistryContextFactory";
                
			if (port == -1)
				port = 1099;
				            	    
            providerUrl = "rmi://" + host + ":" + Integer.toString(port);
            
        } else if (adaptorType.equals("iiop")) {
            namingProviderObjectName = "Naming:type=tnameserv";
            namingProviderClassName = "mx4j.tools.naming.CosNamingService";
            delay = true;
            adaptorObjectName = "Adaptor:protocol=IIOP";
            adaptorClassName = "mx4j.adaptor.rmi.iiop.IIOPAdaptor";
            adaptorMbeanClassName = "mx4j.adaptor.rmi.iiop.IIOPAdaptorMBean";
            contextFactory = "com.sun.jndi.cosnaming.CNCtxFactory";

			if (port == -1)
				port = 900;
				            	    
            providerUrl = "iiop://" + host + ":" + Integer.toString(port);
        } else {
            throw new IllegalArgumentException("Unknown adaptor type");
        }

        // Create and start the naming service
        ObjectName naming = new ObjectName(namingProviderObjectName);
        mserver.createMBean(namingProviderClassName, naming, null);
        if (delay) {
            mserver.setAttribute(naming, new Attribute
                                 ("Delay", Integer.valueOf(5000)));
        }
        mserver.invoke(naming, "start", null, null);

        // Create the JRMP adaptor
        ObjectName adaptor = new ObjectName(adaptorObjectName);
        mserver.createMBean(adaptorClassName, adaptor, null);

        Class proxyClass = Class.forName("mx4j.util.StandardMBeanProxy");

        Object args[] = null;
        Class types[] = null;
        Method method = null;

        types = new Class[3];
        types[0] = Class.class;
        types[1] = MBeanServer.class;
        types[2] = ObjectName.class;
        args = new Object[3];
        args[0] = Class.forName(adaptorMbeanClassName);
        args[1] = mserver;
        args[2] = adaptor;
        method = proxyClass.getMethod("create", types);
        Object bean = method.invoke(null, args);

        Class beanClass = bean.getClass();

        args = new Object[1];
        args[0] = jndiName;
        types = new Class[1];
        types[0] = String.class;
        method = beanClass.getMethod("setJNDIName", types);
        method.invoke(bean, args);

        args = new Object[2];
        types = new Class[2];
        types[0] = Object.class;
        types[1] = Object.class;
        method = beanClass.getMethod("putJNDIProperty", types);

        args[0] = javax.naming.Context.INITIAL_CONTEXT_FACTORY;
        args[1] = contextFactory;
        method.invoke(bean, args);

        args[0] = javax.naming.Context.PROVIDER_URL;
        args[1] = providerUrl;
        method.invoke(bean, args);

        method = beanClass.getMethod("start", (Class[])null);
        method.invoke(bean, (Object[])null);

    }


    /**
     * Deregister the MBean for this
     * <code>Context</code> object.
     *
     * @param context The Context to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Context context)
        throws Exception {

        String mname = createManagedName(context);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, context);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }

    
    /**
     * Deregister the MBean for this
     * <code>ContextEnvironment</code> object.
     *
     * @param environment The ContextEnvironment to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(ContextEnvironment environment)
        throws Exception {

        String mname = createManagedName(environment);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, environment);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }
    
    
    /**
     * Deregister the MBean for this
     * <code>ContextResource</code> object.
     *
     * @param resource The ContextResource to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(ContextResource resource)
        throws Exception {

        String mname = createManagedName(resource);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, resource);
        if( mserver.isRegistered(oname ))
            mserver.unregisterMBean(oname);

    }
     
    
    /**
     * Deregister the MBean for this
     * <code>ContextResourceLink</code> object.
     *
     * @param resourceLink The ContextResourceLink to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(ContextResourceLink resourceLink)
        throws Exception {

        String mname = createManagedName(resourceLink);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, resourceLink);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }   
    
    
    /**
     * Deregister the MBean for this
     * <code>DefaultContext</code> object.
     *
     * @param context The DefaultContext to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(DefaultContext context)
        throws Exception {

        String mname = createManagedName(context);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, context);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Engine</code> object.
     *
     * @param engine The Engine to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Engine engine)
        throws Exception {

        String mname = createManagedName(engine);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, engine);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Host</code> object.
     *
     * @param host The Host to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Host host)
        throws Exception {

        String mname = createManagedName(host);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, host);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Loader</code> object.
     *
     * @param loader The Loader to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Loader loader)
        throws Exception {

        String mname = createManagedName(loader);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, loader);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Logger</code> object.
     *
     * @param logger The Logger to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(org.apache.catalina.Logger logger)
        throws Exception {

        String mname = createManagedName(logger);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, logger);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Manager</code> object.
     *
     * @param manager The Manager to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Manager manager)
        throws Exception {

        String mname = createManagedName(manager);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, manager);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }
    
    
   /**
     * Deregister the MBean for this
     * <code>NamingResources</code> object.
     *
     * @param resources The NamingResources to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(NamingResources resources)
        throws Exception {

        String mname = createManagedName(resources);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, resources);
       if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }
    
    
    /**
     * Deregister the MBean for this
     * <code>Realm</code> object.
     *
     * @param realm The Realm to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Realm realm)
        throws Exception {

        String mname = createManagedName(realm);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, realm);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Server</code> object.
     *
     * @param server The Server to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Server server)
        throws Exception {

        String mname = createManagedName(server);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, server);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Service</code> object.
     *
     * @param service The Service to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(Service service)
        throws Exception {

        String mname = createManagedName(service);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, service);
        if( mserver.isRegistered(oname) )
            mserver.unregisterMBean(oname);

    }


    /**
     * Deregister the MBean for this
     * <code>Valve</code> object.
     *
     * @param valve The Valve to be managed
     *
     * @exception Exception if an MBean cannot be deregistered
     */
    static void destroyMBean(GlassFishValve valve, Container container)
        throws Exception {

        ((Contained)valve).setContainer(container);
        String mname = createManagedName(valve);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            return;
        }
        String domain = managed.getDomain();
        if (domain == null)
            domain = mserver.getDefaultDomain();
        ObjectName oname = createObjectName(domain, valve);
        try {
            ((Contained)valve).setContainer(null);
        } catch (Throwable t) {
        ;
        }
        if( mserver.isRegistered(oname) ) {
            mserver.unregisterMBean(oname);
        }

    }

}
