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

package com.sun.enterprise.management.agent;

import java.security.*;
import java.rmi.RemoteException;
import java.util.*;
import java.io.ObjectInputStream;
//import javax.ejb.*; //
import javax.management.*;
import javax.management.j2ee.*;
import javax.naming.*;
import com.sun.enterprise.management.util.J2EEModuleUtil;

/**
 * @ejbHome <{com.sun.enterprise.management.agent.MEJBHome}>
 * @ejbRemote <{com.sun.enterprise.management.agent.MEJB}>
 *
 *  @author Hans Hrasna
 */
public class MEJBUtility  {
    private MBeanServer server;
    private ListenerRegistry listenerRegistry = null;
    //private com.sun.enterprise.Switch theSwitch = 
//					com.sun.enterprise.Switch.getSwitch(); //TBD SRI
                                        
    private static MEJBUtility mejbUtility = null;
    
    private MEJBUtility(){
        ArrayList mbservers = (ArrayList) AccessController.doPrivileged(
		new PrivilegedAction() {
                    public java.lang.Object run() {
                        return MBeanServerFactory.findMBeanServer(null);
                    }
                });
        if (mbservers.isEmpty()) {
            server = null;
        }
        else {
            server = (MBeanServer)mbservers.get(0);
        }
    }
    public static synchronized MEJBUtility getMEJBUtility(){
        if(mejbUtility == null){
            mejbUtility = new MEJBUtility();
        }
        return mejbUtility;
    }
    /*
    Set getRemoteMBeanServers() {
        HashSet hs = new HashSet();
        InitialContext ic = null;
        Binding b = null;
        try {
            ic = new InitialContext();
            NamingEnumeration ne = ic.listBindings("ejb/mgmt/rmbs");
            while (ne.hasMore()) {
                b = (Binding)ne.next();
                //test binding, check if this IIOPMBeanServer is still valid before adding to the set
                IIOPMBeanServer ios = (IIOPMBeanServer)b.getObject();
                ios.getDefaultDomain();
                hs.add(ios);
            }
        } catch (java.rmi.MarshalException me) {
            try {
                javax.naming.CompositeName cn = new javax.naming.CompositeName("ejb/mgmt/rmbs/" + b.getName());
                ic.unbind(cn);
            }
        	catch (Exception e1) {
            	System.out.println(e1);
        	}
        } catch (Exception e) {
        }
        return hs;
    }

     */
    
    /**
     * Find the MBean server a managed object is registered with.
     * @param name The OBJECT_NAME of the managed object
     * @return  An instance of MBeanServer or IIOPMBeanServer.
     * @exception InstanceNotFoundException Thrown if the managed object is not found on
     * any of the known MBeanServers.
     * /
    Object findServer(ObjectName name) throws InstanceNotFoundException {
        Iterator servers = getRemoteMBeanServers().iterator();
        //check local server
        if (server.isRegistered(name)) {
            return server;
        }
        //check remote servers
        while (servers.hasNext()) {
            IIOPMBeanServer iiopserver = (IIOPMBeanServer)servers.next();
            try {
            	if (iiopserver.isRegistered(name)) {
                	return iiopserver;
            	}
            } catch (Exception e) {
            }
        }
        throw new InstanceNotFoundException(name.toString());
    }

    // javax.management.j2ee.Management implementation starts here

    /**
     * Gets the names of managed objects controlled by the MEJB. This method
     * enables any of the following to be obtained: The names of all managed objects,
     * the names of a set of managed objects specified by pattern matching on the
     * <CODE>ObjectName</CODE> and/or a Query expression, a specific managed object name (equivalent to
     * testing whether an managed object is registered). When the object name is
     * null or no domain and key properties are specified, all objects are selected (and filtered if a
     * query is specified). It returns the set of ObjectNames for the managed objects selected.
     * @param name The object name pattern identifying the managed objects to be retrieved. If
     * null or no domain and key properties are specified, all the managed objects registered will be retrieved.
     * @param query The query expression to be applied for selecting managed objects. If null
     * no query expression will be applied for selecting managed objects.
     * @return  A set containing the ObjectNames for the managed objects selected.
     * If no managed object satisfies the query, an empty list is returned.
     */
    
    public Set queryNames(ObjectName name, QueryExp query) throws Exception {
        /*
        HashSet names = new HashSet();
        Iterator servers = getRemoteMBeanServers().iterator();
        if ((name == null) || (name.isPattern())) {
        	//check remote servers
        	while (servers.hasNext()) {
            	IIOPMBeanServer iiopserver = (IIOPMBeanServer)servers.next();
            	try {
            		names.addAll(iiopserver.queryNames(name, query));
            	} catch (Exception e) {
                	System.out.println(this + ": "+ e);
            	}
        	}
        	//check local server
        	names.addAll(server.queryNames(name, query));
        } else {
        	try {
            	Object svr = findServer(name);
        		if (svr instanceof MBeanServer) {
            		names.addAll(((MBeanServer)svr).queryNames(name, query));
        		} else {
            		names.addAll(((IIOPMBeanServer)svr).queryNames(name, query));
        		}
        	} catch (InstanceNotFoundException e) {
            	//System.out.println(this + ": "+ e);
        	} catch (Exception e) {
                    throw e;
            	//throw new RemoteException(this.toString() +"::queryNames", e);
        	}
        }
        return names;
         */
        return server.queryNames(name, query);
    }

    
    /**
     * Checks whether an MBean, identified by its object name, is already registered with the MBean server.
     * @param name The object name of the MBean to be checked.
     * @return  True if the MBean is already registered in the MBean server, false otherwise.
     */
    
    
    public boolean isRegistered(ObjectName name) throws Exception {
        //check remote servers
        //DON'T EVER call findServer from here!!
        /*Iterator servers = getRemoteMBeanServers().iterator();
        while (servers.hasNext()) {
            IIOPMBeanServer iiopserver = (IIOPMBeanServer)servers.next();
            if (iiopserver.isRegistered(name)) {
                return true;
            }
        }*/
        //check local server
        return server.isRegistered(name);
    }

    
    
    /** Returns the number of MBeans registered in the MBean server. */
    
    public Integer getMBeanCount() throws Exception {
        /*int i = 0;
        Iterator servers = getRemoteMBeanServers().iterator();
        while (servers.hasNext()) {
            IIOPMBeanServer iiopserver = (IIOPMBeanServer)servers.next();
            i = i + iiopserver.getMBeanCount().intValue();
        }
        //check local server
        i = i + server.getMBeanCount().intValue();
        return new Integer(i);
         */
        return server.getMBeanCount();
    }

     
    
    /**
     * This method discovers the attributes and operations that an MBean exposes for management.
     * @param name The name of the MBean to analyze
     * @return  An instance of <CODE>MBeanInfo</CODE> allowing the retrieval of all attributes and operations of this MBean.
     * @exception IntrospectionException An exception occurs during introspection.
     * @exception InstanceNotFoundException The MBean specified is not found.
     * @exception ReflectionException An exception occurred when trying to invoke the getMBeanInfo of a Dynamic MBean.
     */
    
    public MBeanInfo getMBeanInfo(ObjectName name) throws javax.management.InstanceNotFoundException,
        javax.management.IntrospectionException, javax.management.ReflectionException, RemoteException {
        /* MBeanInfo mbi = null;
        Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            mbi = ((MBeanServer)svr).getMBeanInfo(name);
        } else {
            mbi = ((IIOPMBeanServer)svr).getMBeanInfo(name);
        }
        return mbi;
         **/
        return server.getMBeanInfo(name);
    }
     

    /**
     * Gets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean from which the attribute is to be retrieved.
     * @param attribute A String specifying the name of the attribute to be retrieved.
     * @return  The value of the retrieved attribute.
     * @exception AttributeNotFoundException The attribute specified is not accessible in the MBean.
     * @exception MBeanException  Wraps an exception thrown by the MBean's getter.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown when trying to invoke the setter.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or the attribute in parameter is null.
     */
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException,
        javax.management.AttributeNotFoundException, javax.management.InstanceNotFoundException,
        javax.management.ReflectionException, RemoteException {
            /*
        Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            return ((MBeanServer)svr).getAttribute(name, attribute);
        }
        return ((IIOPMBeanServer)svr).getAttribute(name, attribute);
             */
            return server.getAttribute(name, attribute);
    }

    /**
     * Enables the values of several attributes of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean from which the attributes are retrieved.
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of the retrieved attributes.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException An exception occurred when trying to invoke the getAttributes method of a Dynamic MBean.
     * @exception RuntimeOperationsException Wrap a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or attributes in parameter is null.
     */
    public javax.management.AttributeList getAttributes(ObjectName name, String[] attributes)
        throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, RemoteException {
        /*Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            return ((MBeanServer)svr).getAttributes(name, attributes);
        }
        return ((IIOPMBeanServer)svr).getAttributes(name, attributes);
        // return server.getAttributes(name, attributes);
         **/
       return server.getAttributes(name, attributes);     
    }

    /**
     * Sets the value of a specific attribute of a named MBean. The MBean is identified by its object name.
     * @param name The name of the MBean within which the attribute is to be set.
     * @param attribute The identification of the attribute to be set and the value it is to be set to.
     * @return  The value of the attribute that has been set.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception AttributeNotFoundException The attribute specified is not accessible in the MBean.
     * @exception InvalidAttributeValueException The value specified for the attribute is not valid.
     * @exception MBeanException Wraps an exception thrown by the MBean's setter.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown when trying to invoke the setter.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or the attribute in parameter is null.
     */
    public void setAttribute(ObjectName name, javax.management.Attribute attribute)
        throws javax.management.InstanceNotFoundException, javax.management.AttributeNotFoundException,
        javax.management.InvalidAttributeValueException, MBeanException,
        javax.management.ReflectionException, RemoteException {
		server.setAttribute(name, attribute);
        /*Object svr = findServer(name);
       	if (svr instanceof MBeanServer) {
            ((MBeanServer)svr).setAttribute(name, attribute);
        }else {
            ((IIOPMBeanServer)svr).setAttribute(name, attribute);
        }*/
    }

    /**
     * Sets the values of several attributes of a named MBean. The MBean is identified by its object name.
     * @param name The object name of the MBean within which the attributes are to be set.
     * @param attributes A list of attributes: The identification of the
     * attributes to be set and  the values they are to be set to.
     * @return  The list of attributes that were set, with their new values.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception ReflectionException An exception occurred when trying to invoke the getAttributes method of a Dynamic MBean.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or attributes in parameter is null.
     */
    public javax.management.AttributeList setAttributes(ObjectName name, javax.management.AttributeList attributes)
        throws javax.management.InstanceNotFoundException, javax.management.ReflectionException, RemoteException {
        return server.setAttributes(name, attributes);
        /*Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            return ((MBeanServer)svr).setAttributes(name, attributes);
        }
        return ((IIOPMBeanServer)svr).setAttributes(name, attributes);
         **/
        
    }

    /**
     * Invokes an operation on an MBean.
     * @param name The object name of the MBean on which the method is to be invoked.
     * @param operationName The name of the operation to be invoked.
     * @param params An array containing the parameters to be set when the operation is invoked
     * @param signature An array containing the signature of the operation. The class objects will
     * be loaded using the same class loader as the one used for loading the MBean on which the operation was invoked.
     * @return  The object returned by the operation, which represents the result ofinvoking the operation
     * on the MBean specified.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception MBeanException  Wraps an exception thrown by the MBean's invoked method.
     * @exception ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method.
     */
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
        throws javax.management.InstanceNotFoundException, MBeanException,
        javax.management.ReflectionException, RemoteException {
        return server.invoke(name, operationName, params, signature);
        /*Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            return ((MBeanServer)svr).invoke(name, operationName, params, signature);
        }
        return ((IIOPMBeanServer)svr).invoke(name, operationName, params, signature);
         */
    }

    /**
     * Returns the default domain used for naming the managed object.
     * The default domain name is used as the domain part in the ObjectName
     * of managed objects if no domain is specified by the user.
     */
    public String getDefaultDomain() {
        return server.getDefaultDomain();
    }

    /*
     * returns the ListenerRegistry implementation for this MEJB
     */

    public ListenerRegistration getListenerRegistry() {
        if (listenerRegistry == null) {
            try {
                listenerRegistry = new ListenerRegistry(java.net.InetAddress.getLocalHost().getHostAddress());
            } catch (java.net.UnknownHostException e) {
                listenerRegistry = new ListenerRegistry(J2EEModuleUtil.getDomainName());
            }
        }
        return listenerRegistry;
    }

    /* TBD SRI
    //called by AppContainer to register an AppClientModule MO if it wasn't registered during deployment
    public void registerAppClient(com.sun.enterprise.deployment.ApplicationClientDescriptor d) throws RemoteException, java.io.IOException {
        theSwitch.getManagementObjectManager().registerAppClient(d);
    }

     */
    
    // Additional MBeanServer interface implementation starts here

    /**
     * Instantiates an object using the list of all class loaders registered
     * in the MBean server ({@link javax.management.loading.DefaultLoaderRepository Default Loader Repository}).
     * The object's class should have a public constructor. It returns a reference to the newly created object.
     * The newly created object is not registered in the MBean server.
     * @param className The class name of the object to be instantiated.
     * @return The newly instantiated object.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the object's constructor.
     * @exception MBeanException The constructor of the object has thrown an exception
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className) throws javax.management.ReflectionException, MBeanException, RemoteException {
        return server.instantiate(className);
    }

    /**
     * Instantiates an object using the class Loader specified by its <CODE>ObjectName</CODE>.
     * If the loader name is null, the ClassLoader that loaded the MBean Server will be used.
     * The object's class should have a public constructor. It returns a reference to the newly created object.
     * The newly created object is not registered in the MBean server.
     * @param className The class name of the MBean to be instantiated.
     * @param loaderName The object name of the class loader to be used.
     * @return The newly instantiated object.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the object's constructor.
     * @exception MBeanException The constructor of the object has thrown an exception.
     * @exception InstanceNotFoundException The specified class loader is not registered in the MBaenServer.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className, ObjectName loaderName) throws javax.management.ReflectionException,
        MBeanException, javax.management.InstanceNotFoundException, RemoteException {
            return server.instantiate(className, loaderName);
    }

    /**
     * Instantiates an object using the list of all class loaders registered
     * in the MBean server ({@link javax.management.loading.DefaultLoaderRepository Default Loader Repository}).
     * The object's class should have a public constructor. The call returns a reference to the newly created object.
     * The newly created object is not registered in the MBean server.
     * @param className The class name of the object to be instantiated.
     * @param params An array containing the parameters of the constructor to be invoked.
     * @param signature An array containing the signature of the constructor to be invoked.
     * @return The newly instantiated object.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the object's constructor.
     * @exception MBeanException The constructor of the object has thrown an exception
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className, Object[] params, String[] signature) throws javax.management.ReflectionException,
        MBeanException, RemoteException {
            return server.instantiate(className, params, signature);
    }

    /**
     * Instantiates an object. The class loader to be used is identified by its object
     * name. If the object name of the loader is null, the ClassLoader that loaded the MBean server will be used.
     * The object's class should have a public constructor. The call returns a reference to the newly created object.
     * The newly created object is not registered in the MBean server.
     * @param className The class name of the object to be instantiated.
     * @param params An array containing the parameters of the constructor to be invoked.
     * @param signature An array containing the signature of the constructor to be invoked.
     * @param loaderName The object name of the class loader to be used.
     * @return The newly instantiated object.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or the
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the object's constructor.
     * @exception MBeanException The constructor of the object has thrown an exception
     * @exception InstanceNotFoundException The specified class loader is not registered in the MBean server.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className
     * passed in parameter is null.
     */
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
        throws javax.management.ReflectionException, MBeanException,
        javax.management.InstanceNotFoundException, RemoteException {
            return server.instantiate(className, loaderName, params, signature);
    }

    /**
     * Instantiates and registers an MBean in the MBean server. The MBean server will use the {@link
     * javax.management.loading.DefaultLoaderRepository Default Loader Repository} to load the class of the MBean.
     * An object name is associated to the MBean. If the object name given is null, the MBean can automatically provide its
     * own name by implementing the {@link javax.management.MBeanRegistration MBeanRegistration} interface. The call returns
     * an <CODE>ObjectInstance</CODE> object representing the newly created MBean.
     * @param className The class name of the MBean to be instantiated.
     * @param name The object name of the MBean. May be null.
     * @return  An <CODE>ObjectInstance</CODE>, containing the <CODE>ObjectName</CODE> and the Java class name
     * of the newly instantiated MBean.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or a
     * <CODE><CODE>java.lang.Exception</CODE></CODE> that occurred when trying to invoke the MBean's constructor.
     * @exception InstanceAlreadyExistsException The MBean is already under the control of the MBean server.
     * @exception MBeanRegistrationException The <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE> interface) method of
     * the MBean has thrown an exception. The MBean will not be registered.
     * @exception MBeanException The constructor of the MBean has thrown an exception
     * @exception NotCompliantMBeanException This class is not a JMX compliant MBean
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className passed in
     * parameter is null, the <CODE>ObjectName</CODE> passed in parameter contains a pattern or no <CODE>ObjectName</CODE> is
     * specified for the MBean.
     */
    public ObjectInstance createMBean(String className, ObjectName name) throws javax.management.ReflectionException,
        javax.management.InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,
        NotCompliantMBeanException, RemoteException {
            return server.createMBean(className, name);
    }

    /**
     * Instantiates and registers an MBean in the MBean server. The class loader to be used is identified by its object  name.
     * An object name is associated to the MBean. If the object name  of the loader is null, the ClassLoader that loaded the
     * MBean server will be used. If the MBean's object name given is null, the MBean can automatically provide its
     * own name by implementing the {@link javax.management.MBeanRegistration MBeanRegistration} interface. The call returns
     * an <CODE>ObjectInstance</CODE> object representing the newly created MBean.
     * @param className The class name of the MBean to be instantiated.
     * @param name The object name of the MBean. May be null.
     * @param loaderName The object name of the class loader to be used.
     * @return  An <CODE>ObjectInstance</CODE>, containing the <CODE>ObjectName</CODE> and the Java class name
     * of the newly instantiated MBean.
     * @exception ReflectionException  Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or a
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the MBean's constructor.
     * @exception InstanceAlreadyExistsException The MBean is already under the control of the MBean server.
     * @exception MBeanRegistrationException The <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE>  interface) method
     * of the MBean has thrown an exception. The MBean will not be registered.
     * @exception MBeanException The constructor of the MBean has thrown an exception
     * @exception NotCompliantMBeanException This class is not a JMX compliant MBean
     * @exception InstanceNotFoundException The specified class loader is not registered in the MBean server.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className passed in
     * parameter is null, the <CODE>ObjectName</CODE> passed in parameter contains a pattern or no <CODE>ObjectName</CODE> is
     * specified for the MBean.
     */
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
        throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException,
        MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
        javax.management.InstanceNotFoundException, RemoteException {
            return server.createMBean(className, name, loaderName);
    }

    /**
     * Instantiates and registers an MBean in the MBean server.
     * The MBean server will use the {@link javax.management.loading.DefaultLoaderRepository Default Loader Repository}
     * to load the class of the MBean. An object name is associated to the MBean. If the object name given is null, the MBean
     * can automatically provide its own name by implementing the {@link javax.management.MBeanRegistration MBeanRegistration}
     * interface. The call returns an <CODE>ObjectInstance</CODE> object representing the newly created MBean.
     * @param className The class name of the MBean to be instantiated.
     * @param name The object name of the MBean. May be null.
     * @param params An array containing the parameters of the constructor to be invoked.
     * @param signature An array containing the signature of the constructor to be invoked.
     * @return  An <CODE>ObjectInstance</CODE>, containing the <CODE>ObjectName</CODE> and the Java class name
     * of the newly instantiated MBean.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or a
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the MBean's constructor.
     * @exception InstanceAlreadyExistsException The MBean is already under the control of the MBean server.
     * @exception MBeanRegistrationException The <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE>  interface) method
     * of the MBean has thrown an exception. The MBean will not be registered.
     * @exception MBeanException The constructor of the MBean has thrown an exception
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className passed in
     * parameter is null, the <CODE>ObjectName</CODE> passed in parameter contains a pattern or no <CODE>ObjectName</CODE> is
     * specified for the MBean.
     */
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
        throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException,
        MBeanRegistrationException, MBeanException, NotCompliantMBeanException, RemoteException {
            return server.createMBean(className, name, params, signature);
    }

    /**
     * Instantiates and registers an MBean in the MBean server. The class loader to be used is identified by its object
     * name. An object name is associated to the MBean. If the object name
     * of the loader is not specified, the ClassLoader that loaded the MBean server will be used.
     * If  the MBean object name given is null, the MBean can automatically provide its
     * own name by implementing the {@link javax.management.MBeanRegistration MBeanRegistration} interface. The call returns
     * an <CODE>ObjectInstance</CODE> object representing the newly created MBean.
     * @param className The class name of the MBean to be instantiated.
     * @param name The object name of the MBean. May be null.
     * @param params An array containing the parameters of the constructor to be invoked.
     * @param signature An array containing the signature of the constructor to be invoked.
     * @param loaderName The object name of the class loader to be used.
     * @return  An <CODE>ObjectInstance</CODE>, containing the <CODE>ObjectName</CODE> and the Java class name
     * of the newly instantiated MBean.
     * @exception ReflectionException Wraps a <CODE>java.lang.ClassNotFoundException</CODE> or a
     * <CODE>java.lang.Exception</CODE> that occurred when trying to invoke the MBean's constructor.
     * @exception InstanceAlreadyExistsException The MBean is already under the control of the MBean server.
     * @exception MBeanRegistrationException The <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE>  interface) method
     * of the MBean has thrown an exception. The MBean will not be registered.
     * @exception MBeanException The constructor of the MBean has thrown an exception
     * @exception InstanceNotFoundException The specified class loader is not registered in the MBean server.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The className passed in
     * parameter is null, the <CODE>ObjectName</CODE> passed in parameter contains a pattern or no <CODE>ObjectName</CODE> is
     * specified for the MBean.
     */
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
        throws javax.management.ReflectionException, javax.management.InstanceAlreadyExistsException,
        MBeanRegistrationException, MBeanException, NotCompliantMBeanException,
        javax.management.InstanceNotFoundException, RemoteException {
            return server.createMBean(className, name, loaderName, params, signature);
    }

    /**
     * Registers a pre-existing object as an MBean with the MBean server. If the object name given is
     * null, the MBean may automatically provide its own name by implementing the
     * {@link javax.management.MBeanRegistration MBeanRegistration}  interface.
     * The call returns an <CODE>ObjectInstance</CODE> object representing the registered MBean.
     * @param object The  MBean to be registered as an MBean.
     * @param name The object name of the MBean. May be null.
     * @return  The <CODE>ObjectInstance</CODE> for the MBean that has been registered.
     * @exception InstanceAlreadyExistsException The MBean is already under the control of the MBean server.
     * @exception MBeanRegistrationException The <CODE>preRegister</CODE> (<CODE>MBeanRegistration</CODE>  interface) method
     * of the MBean has thrown an exception. The MBean will not be registered.
     * @exception NotCompliantMBeanException This object is not a JMX compliant MBean
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object passed in
     * parameter is null or no object name is specified.
     */
    public ObjectInstance registerMBean(Object object, ObjectName name) throws javax.management.InstanceAlreadyExistsException,
        MBeanRegistrationException, NotCompliantMBeanException, RemoteException {
            return server.registerMBean(object, name);
    }

    /**
     * De-registers an MBean from the MBean server. The MBean is identified by
     * its object name. Once the method has been invoked, the MBean may no longer be accessed by its object name.
     * @param name The object name of the MBean to be de-registered.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     * @exception MBeanRegistrationException The preDeregister ((<CODE>MBeanRegistration</CODE>  interface) method of the MBean
     * has thrown an exception.
     * @exception RuntimeOperationsException Wraps a <CODE>java.lang.IllegalArgumentException</CODE>: The object name in
     * parameter is null or the MBean you are when trying to de-register is the {@link javax.management.MBeanServerDelegate
     * MBeanServerDelegate} MBean.
     */
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException,
        MBeanRegistrationException, RemoteException {
        /*Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            ((MBeanServer)svr).unregisterMBean(name);
            return;
        }
        ((IIOPMBeanServer)svr).unregisterMBean(name);*/
        server.unregisterMBean(name);
    }

    /**
     * Gets the <CODE>ObjectInstance</CODE> for a given MBean registered with the MBean server.
     * @param name The object name of the MBean.
     * @return The <CODE>ObjectInstance</CODE> associated to the MBean specified by <VAR>name</VAR>.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     */
    public ObjectInstance getObjectInstance(ObjectName name)
        throws javax.management.InstanceNotFoundException, RemoteException {
            return server.getObjectInstance(name);
    }

    /**
     * Gets MBeans controlled by the MBean server. This method allows any
     * of the following to be obtained: All MBeans, a set of MBeans specified
     * by pattern matching on the <CODE>ObjectName</CODE> and/or a Query expression, a
     * specific MBean. When the object name is null or no domain and key properties are specified, all objects are to be
     * selected (and filtered if a query is specified). It returns the
     * set of <CODE>ObjectInstance</CODE> objects (containing the <CODE>ObjectName</CODE> and the Java Class name)
     * for the selected MBeans.
     * @param name The object name pattern identifying the MBeans to be retrieved. If
     * null or no domain and key properties are specified, all the MBeans registered will be retrieved.
     * @param query The query expression to be applied for selecting MBeans. If null
     * no query expression will be applied for selecting MBeans.
     * @return  A set containing the <CODE>ObjectInstance</CODE> objects for the selected MBeans.
     * If no MBean satisfies the query an empty list is returned.
     */
    public Set queryMBeans(ObjectName name, QueryExp query) throws RemoteException {
        return server.queryMBeans(name, query);
    }

    /**
     * Returns the default domain used for naming the MBean. The default domain name is used as the domain part in the
     * ObjectName of MBeans if no domain is specified by the user.
     */
    //public String getDefaultDomain() throws RemoteException;

    /**
     * Enables to add a listener to a registered MBean.
     * @param name The name of the MBean on which the listener should be added.
     * @param listener The listener object which will handle the notifications emitted by the registered MBean.
     * @param filter The filter object. If filter is null, no filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a notification is emitted.
     * @exception InstanceNotFoundException The MBean name provided does not match any of the registered MBeans.
     */
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
        throws javax.management.InstanceNotFoundException, RemoteException {
            /*
        Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
            if (listener instanceof RemoteListenerConnector) {
            	((RemoteListenerConnector)listener).setMBeanServer((MBeanServer)svr);
        	}
        	((MBeanServer)svr).addNotificationListener(name, listener, filter, handback);;
            return;
        }
        //not yet supported, need to add to IIOPMBeanServer interface/impl
        //((IIOPMBeanServer)svr).addNotificationListener(name, listener, filter, handback);
             */
            server.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * Enables to add a listener to a registered MBean.
     * @param name The name of the MBean on which the listener should be added.
     * @param listener The object name of the listener which will handle the notifications emitted by the registered MBean.
     * @param filter The filter object. If filter is null, no filtering will be performed before handling notifications.
     * @param handback The context to be sent to the listener when a notification is emitted.
     * @exception InstanceNotFoundException The MBean name of the notification listener or of the notification broadcaster
     * does not match any of the registered MBeans.
     */
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
        throws javax.management.InstanceNotFoundException, RemoteException {
            server.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * Enables to remove a listener from a registered MBean.
     * @param name The name of the MBean on which the listener should be removed.
     * @param listener The listener object which will handle the notifications emitted by the registered MBean.
     * This method will remove all the information related to this listener.
     * @exception InstanceNotFoundException The MBean name provided does not match any of the registered MBeans.
     * @exception ListenerNotFoundException The listener is not registered in the MBean.
     */
    public void removeNotificationListener(ObjectName name, NotificationListener listener)
        throws javax.management.InstanceNotFoundException, javax.management.ListenerNotFoundException, RemoteException {
            /*
        Object svr = findServer(name);
        if (svr instanceof MBeanServer) {
        	((MBeanServer)svr).removeNotificationListener(name, listener);
            return;
        }
        //not yet supported, need to add to IIOPMBeanServer interface/impl
        //((IIOPMBeanServer)svr).removeNotificationListener(name, listener);
        //server.removeNotificationListener(name, listener);
             */
            
            server.removeNotificationListener(name, listener);
    }

    /**
     * Enables to remove a listener from a registered MBean.
     * @param name The name of the MBean on which the listener should be removed.
     * @param listener The object name of the listener which will handle the notifications emitted by the registered MBean.
     * This method will remove all the information related to this listener.
     * @exception InstanceNotFoundException The MBean name provided does not match any of the registered MBeans.
     * @exception ListenerNotFoundException The listener is not registered in the MBean.
     */
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws javax.management.InstanceNotFoundException,
        javax.management.ListenerNotFoundException, RemoteException {
            server.removeNotificationListener(name, listener);
    }

    /**
     * Returns true if the MBean specified is an instance of the specified class, false otherwise.
     * @param name The <CODE>ObjectName</CODE> of the MBean.
     * @param className The name of the class.
     * @return true if the MBean specified is an instance of the specified class, false otherwise.
     * @exception InstanceNotFoundException The MBean specified is not registered in the MBean server.
     */
    public boolean isInstanceOf(ObjectName name, String className)
        throws javax.management.InstanceNotFoundException, RemoteException {
            return server.isInstanceOf(name, className);
    }     
}
