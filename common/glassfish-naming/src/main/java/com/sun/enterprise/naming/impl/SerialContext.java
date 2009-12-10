/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.naming.impl;

import com.sun.enterprise.naming.util.LogFacade;
import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.naming.NamingObjectProxy;
import org.jvnet.hk2.component.Habitat;

import com.sun.hk2.component.ExistingSingletonInhabitant;

import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextHelper;
import javax.rmi.PortableRemoteObject;

import org.glassfish.internal.api.*;


import org.omg.CORBA.ORB;


/**
 * This context provides access to the app server naming service. This
 * is the default Context for GlassFish. Lookups of unqualified names (i.e.
 * names not starting with "java:", "corbaname:" etc) are serviced by
 * SerialContext. The namespace is implemented in the
 * SerialContextProviderImpl object,  which is accessed directly in the
 * case that the client is collocated with the naming service impl or
 * remotely via RMI-IIOP if not collocated.
 * <p/>
 * <b>NOT THREAD SAFE: mutable instance variables</b>
 */
public class SerialContext implements Context {


    private static final String JAVA_URL = "java:";

    private static final String JAVA_GLOBAL_URL = "java:global/";


    // Sets unmanaged SerialContext in test mode to prevent attempts to contact server. 
    static final String INITIAL_CONTEXT_TEST_MODE = "com.sun.enterprise.naming.TestMode";

    private static Logger _logger = LogFacade.getLogger();

    private static final NameParser myParser = new SerialNameParser();

    private static Map<ProviderCacheKey, SerialContextProvider> providerCache =
            new HashMap<ProviderCacheKey, SerialContextProvider>();


    private Hashtable myEnv = null; // THREAD UNSAFE

    private SerialContextProvider provider;

    private final String myName;

    private final JavaURLContext javaUrlContext;

    private Habitat habitat;

    private boolean testMode = false;

    private ProcessType processType = ProcessType.Server;

    private String targetHostFromEnv;
    private String targetPortFromEnv;
    private ORB orbFromEnv;

    private String targetHost;
    private String targetPort;


    private ORB orb;
    
    // host/port associated with underlying orb.
    // Used for logging / exception info purposes
    private String orbsInitialHostValue;
    private String orbsInitialPortValue;

    // True if we're running in the server and no orb,host, or port
    // properties have been explicitly set in the properties
    // Allows special optimized intra-server naming service access
    private boolean intraServerLookups;

    // Common Class Loader. It is used as a fallback classloader to locate
    // GlassFish object factories.
    private ClassLoader commonCL;

    /**
     * NOTE: ALL "stickyContext" LOGIC REMOVED FOR INITIAL V3 RELEASE.  WE'LLl
     * REVISIT THE UNDERLYING ISSUE WHEN ADDING LOAD-BALANCING / FAILOVER
     * SUPPORT POST V3.  ORIGINAL COMMENT BLOCK PRESERVED HERE UNTIL THEN.
     *
     * set and get methods for preserving stickiness. This is a temporary
     * solution to store the sticky IC as a thread local variable. This sticky
     * IC will be used by all classes that require a context object to do lookup
     * (if LB is enabled) SerialContext.lookup() sets a value for the thread
     * local variable (stickyContext) before performing th lookup in case LB is
     * enabled. If not, the thread local variable is null. At the end of the
     * SerialContext.lookup() method, the thread local variable gets set to
     * null. So actually speaking, more than being a global variable for the
     * entire thread, its global only during the execution of the
     * SerialContext.lookup() method. bug 5050591 This will be cleaned for the
     * next release.
     *
     */


    /**
     * Constructor for the context. Initializes the object reference to the
     * remote provider object.
     */
    public SerialContext(String name, Hashtable environment, Habitat h)
            throws NamingException {

        habitat = h;

        myEnv = (environment != null) ? (Hashtable) (environment.clone())
                : null;

        // TODO REMOVE when property stuff is figured out
        myEnv.put("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
        myEnv.put("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl" );

        this.myName = name;
        if (_logger.isLoggable(Level.FINE))
            _logger.fine("SerialContext ==> SerialContext instance created : "
                    + this);

        if( myEnv.get(INITIAL_CONTEXT_TEST_MODE) != null ) {
            testMode = true;
            System.out.println("SerialContext in test mode");
        }

        if( (habitat == null) && !testMode ) {

            synchronized(SerialContext.class) {

                if( SerialInitContextFactory.getDefaultHabitat() == null ) {

                    // Bootstrap a hk2 environment.
                    // TODO This will need to be moved somewhere else.  Potentially any
                    // piece of glassfish code that can be an initial entry point from a
                    // Java SE client will need to make this happen.

                    habitat = Globals.getStaticHabitat();

                    SerialInitContextFactory.setDefaultHabitat(habitat);
                } else {
                    habitat = SerialInitContextFactory.getDefaultHabitat();
                }

            }
        }
        

        if( testMode ) {
            processType = ProcessType.Server;
        } else {
            ProcessEnvironment processEnv = habitat.getComponent(ProcessEnvironment.class);
            processType = processEnv.getProcessType();
            _logger.fine("Serial Context initializing with process environment " + processEnv);

        }



        // using these two temp variables allows instance variables
        // to be 'final'.
        JavaURLContext urlContextTemp = null;

        if (myEnv.get("com.sun.appserv.ee.iiop.endpointslist") != null) {

            urlContextTemp = new JavaURLContext(myEnv, this);
        } else {
            urlContextTemp = new JavaURLContext(myEnv, null);
        }
        javaUrlContext = urlContextTemp;


        orbFromEnv  = (ORB) myEnv.get(ORBLocator.JNDI_CORBA_ORB_PROPERTY);
        targetHostFromEnv = (String)myEnv.get(ORBLocator.OMG_ORB_INIT_HOST_PROPERTY);
        targetPortFromEnv = (String)myEnv.get(ORBLocator.OMG_ORB_INIT_PORT_PROPERTY);

        intraServerLookups = (processType.isServer()) && (orbFromEnv == null) &&
                        (targetHostFromEnv == null) && (targetPortFromEnv == null);

        // Set target host / port from env.  If only one of the two is set, fill in the
        // other with the default.  
        if( targetHostFromEnv != null ) {
            targetHost = targetHostFromEnv;
            if( targetPortFromEnv == null ) {
                targetPort = ORBLocator.DEFAULT_ORB_INIT_PORT;
            }
        }

        if( targetPortFromEnv != null ) {
            targetPort = targetPortFromEnv;
            if( targetHostFromEnv == null ) {
                targetHost = ORBLocator.DEFAULT_ORB_INIT_HOST;
            }
        }

        orb = orbFromEnv;
        if (habitat != null) { // can happen in test mode
            ServerContext sc = habitat.getByContract(ServerContext.class);
            if (sc != null) commonCL = sc.getCommonClassLoader();
        }
    }

    /**
     * This constructor takes the component id as an argument. All name
     * arguments to operations are prepended by the component id.
     */
    public SerialContext(Hashtable env, Habitat habitat) throws NamingException {
        this("", env, habitat);
    }


    private SerialContextProvider getProvider() throws NamingException {

        SerialContextProvider returnValue = provider;

        if (provider == null) {

            try {

                if( intraServerLookups ) {

                    returnValue = ProviderManager.getProviderManager().getLocalProvider();

                }  else {

                    returnValue = getRemoteProvider();
                }

            } catch(Exception e) {
                e.printStackTrace();
                NamingException ne =
                        new NamingException("Unable to acquire SerialContextProvider for " + this);
                ne.initCause(e);
                throw ne;
            }
        }

        return returnValue;
    }

    private SerialContextProvider getRemoteProvider() throws Exception {


        if( provider == null ) {

            ORBLocator orbHelper = habitat.getComponent(ORBLocator.class);

            ProviderCacheKey key;
            if( orb != null) {
                key = new ProviderCacheKey(orb);
            } else {
                orb = orbHelper.getORB();

                key = (targetHost == null) ? new ProviderCacheKey(orb) :
                    new ProviderCacheKey(targetHost, targetPort);
            }

            // For logging / exception info purposes, keep track of what the
            // orb has as its host/port
            orbsInitialHostValue = orbHelper.getORBHost(orb);
            orbsInitialPortValue = orbHelper.getORBPort(orb) + "";

            SerialContextProvider cachedProvider;

            synchronized(SerialContext.class) {
                cachedProvider = providerCache.get(key);
            }

            if( cachedProvider == null) {

                org.omg.CORBA.Object cosNamingServiceRef = null;

                if( targetHost != null ) {

                    cosNamingServiceRef = orb.string_to_object("corbaloc:iiop:1.2@" +
					           targetHost + ":" + targetPort + "/NameService");
                } else {

                    cosNamingServiceRef = orb.resolve_initial_references("NameService");
                }

                SerialContextProvider tmpProvider = narrowProvider(cosNamingServiceRef);

                synchronized(SerialContext.class) {
                    cachedProvider = providerCache.get(key);
                    if( cachedProvider == null ) {
                        providerCache.put(key, tmpProvider);
                        provider = tmpProvider;
                    } else {
                        provider = cachedProvider;
                    }
                }
            } else {
                provider = cachedProvider;
            }
                       
        }

		return provider;

    }

    /**
     * Lookup object within server's CosNaming service that provides remote
     * access to the app server's SerialContext naming service.
     */
    private SerialContextProvider narrowProvider(org.omg.CORBA.Object ref)
                       throws Exception {

        NamingContext nctx = NamingContextHelper.narrow(ref);
	    NameComponent[] path =
	        { new NameComponent("SerialContextProvider", "") };

	    return (SerialContextProvider)
	        PortableRemoteObject.narrow(nctx.resolve(path),
					            SerialContextProvider.class);
    }


    /**
     * The getNameInNamespace API is not supported in this context.
     *
     * @throws NamingException if there is a naming exception.
     */
    public String getNameInNamespace() throws NamingException {
        return myName;
    }

    /**
     * method to check if the name to look up starts with "java:"
     */
    private boolean isjavaURL(String name) {

        if ((name.startsWith(JAVA_URL)) && (! name.startsWith(JAVA_GLOBAL_URL))) {
            return true;
        } else
            return false;
    }

    /**
     * Lookup the specified name in the context. Returns the resolved object.
     *
     * @return the resolved object.
     * @throws NamingException if there is a naming exception.
     */
    public Object lookup(String name) throws NamingException {

        // Before any lookup bind any NamedNamingObjectProxy
        // Skip if in plain Java SE client
        // TODO this should really be moved somewhere else

        NamedNamingObjectManager.checkAndLoadProxies(habitat);


        /**
         * In case a user is creating an IC with env passed in constructor; env
         * specifies endpoints in some form in that case, the sticky IC should
         * be stores as a thread local variable.
         *
         */
        if (myEnv.get("com.sun.appserv.ee.iiop.endpointslist") != null) {
           // TODO post V3
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("SerialContext ==> lookup( " + name +")");
        }

        if (name.equals("")) {
            // Asking to look up this context itself. Create and return
            // a new instance with its own independent environment.
            return (new SerialContext(myName, myEnv, habitat));
        }

        name = getRelativeName(name);

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("SerialContext ==> lookup relative name : " + name);
        }

        try {
            if (isjavaURL(name)) {
                return javaUrlContext.lookup(name);
            } else {
                Object obj = getProvider().lookup(name);
                if (obj instanceof NamingObjectProxy) {
                    return ((NamingObjectProxy) obj).create(this);
                }
                if (obj instanceof Context) {
                    return new SerialContext(name, myEnv, habitat);
                }
                Object retObj = getObjectInstance(name, obj);

                return retObj;
            }
        } catch (NamingException nnfe) {
            NamingException ne = new NamingException
                    ("Lookup failed for '" + name + "' in " + this);
            ne.initCause(nnfe);
            throw ne;
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,
                    "enterprise_naming.serialctx_communication_exception", name);
            _logger.log(Level.SEVERE, "", ex);

            // temp fix for 6320008
            // this should be removed once we change the transient NS
            // implementation to persistent
            if (ex instanceof java.rmi.MarshalException
                    && ex.getCause() instanceof org.omg.CORBA.COMM_FAILURE) {
                provider = null;
                _logger.fine("Resetting provider to NULL. Will get new obj ref " +
                             "for provider since previous obj ref was stale...");
                return lookup(name);
            } else {
                CommunicationException ce = new CommunicationException(
                        "Communication exception for " + this);
                ce.initCause(ex);
                throw ce;
            }
        }

    }

    private Object getObjectInstance(String name, Object obj) throws Exception
    {
        Object retObj = javax.naming.spi.NamingManager
                .getObjectInstance(obj, new CompositeName(name), null,
                        myEnv);
        if (retObj == obj) {
            // NamingManager.getObjectInstance() returns the same object
            // when it can't find the factory class. Since NamingManager
            // uses Thread's context class loader to locate factory classes,
            // it may not be able to locate the various GlassFish factories
            // when lookup is performed outside of a Java EE context like
            // inside an OSGi bundle's activator.
            // So, let's see if using CommonClassLoader helps or not.
            // We will only try with CommonClassLoader when the passed object
            // reference has a factory class name set.

            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != commonCL) {
                Reference ref = getReference(obj);
                if (ref != null) {
                    _logger.logp(Level.FINE, "SerialContext",
                            "getObjectInstance",
                            "Trying with CommonClassLoader for name {0} ",
                            new Object[]{name});
                    ObjectFactory factory = getObjectFactory(ref, commonCL);
                    if (factory != null) {
                        retObj = factory.getObjectInstance(
                                ref, new CompositeName(name), null, myEnv);
                    }
                    if (retObj != obj) {
                        _logger.logp(Level.FINE, "SerialContext",
                                "getObjectInstance",
                                "Found with CommonClassLoader");
                    }
                }
            }
        }
        return retObj;
    }

    /**
     * This method tries to check if the passed object is a Reference or
     * Refenciable. If it is a Reference, it just casts it to a Reference and
     * returns, else if it is a Referenceable, it tries to get a Reference from
     * the Referenceable and returns that, otherwise, it returns null.
     *
     * @param obj
     * @return
     * @throws NamingException
     */
    private Reference getReference(Object obj) throws NamingException
    {
        Reference ref = null;
        if (obj instanceof Reference) {
            ref = (Reference) obj;
        } else if (obj instanceof Referenceable) {
            ref = ((Referenceable)(obj)).getReference();
        }

        return ref;
    }

    /**
     * It tries to load the factory class for the given reference using the
     * given class loader and return an instance of the same. Returns null
     * if it can't load the class.
     *
     * @param ref
     * @param cl
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private ObjectFactory getObjectFactory(Reference ref, ClassLoader cl)
            throws IllegalAccessException, InstantiationException
    {
        String factoryName = ref.getFactoryClassName();
        if (factoryName != null) {
            try
            {
                Class c = Class.forName(factoryName, false, cl);
                return (ObjectFactory)c.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                // ignore only CNFE, all other exceptions are considered errors
            }
        }
        return null;
    }

    /**
     * Lookup the specifed name in the context. Returns the resolved object.
     *
     * @return the resolved object.
     * @throws NamingException if there is a naming exception.
     */
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString());
    }

    /**
     * Bind the object to the specified name.
     *
     * @param name name that the object is being bound to.
     * @param name object that is being bound.
     * @throws NamingException if there is a naming exception.
     */
    public void bind(String name, Object obj) throws NamingException {

        name = getRelativeName(name);
        if (isjavaURL(name)) {
            javaUrlContext.bind(name, obj);
        } else {
            try {
                getProvider().bind(name, obj);
            } catch (RemoteException ex) {
                throw new CommunicationException(ex.toString());
            }
        }
    }

    /**
     * Bind the object to the specified name.
     *
     * @param name name that the object is being bound to.
     * @param obj  object that is being bound.
     * @throws NamingException if there is a naming exception.
     */
    public void bind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        bind(name.toString(), obj);
    }

    /**
     * Rebind the object to the specified name.
     *
     * @param name name that the object is being bound to.
     * @param obj  object that is being bound.
     * @throws NamingException if there is a naming exception.
     */
    public void rebind(String name, Object obj) throws NamingException {

        name = getRelativeName(name);
        if (isjavaURL(name)) {
            javaUrlContext.rebind(name, obj);
        } else {
            try {
                getProvider().rebind(name, obj);
            } catch (RemoteException ex) {
                throw new CommunicationException(ex.toString());
            }
        }
    }

    /**
     * Rebind the object to the specified name.
     *
     * @param name name that the object is being bound to.
     * @param obj  object that is being bound.
     * @throws NamingException if there is a naming exception.
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        rebind(name.toString(), obj);
    }

    /**
     * Unbind the object with the specified name.
     *
     * @param name that is being unbound.
     * @throws NamingException if there is a naming exception.
     */
    public void unbind(String name) throws NamingException {
        name = getRelativeName(name);
        if (isjavaURL(name)) {
            javaUrlContext.unbind(name);
        } else {
            try {
                getProvider().unbind(name);
            } catch (RemoteException ex) {
                throw new CommunicationException(ex.toString());
            }
        }
    }

    /**
     * Unbind the object with the specified name.
     *
     * @param name name that is being unbound.
     * @throws NamingException if there is a naming exception.
     */
    public void unbind(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        unbind(name.toString());
    }

    /**
     * Rename the bound object.
     *
     * @param oldname old name that the object is bound as.
     * @param newname new name that the object will be bound as.
     * @throws NamingException if there is a naming exception.
     */
    public void rename(String oldname, String newname) throws NamingException {
        oldname = getRelativeName(oldname);
        newname = getRelativeName(newname);
        if (isjavaURL(oldname)) {
            javaUrlContext.rename(oldname, newname);
        } else {
            try {
                getProvider().rename(oldname, newname);
            } catch (RemoteException ex) {
                throw new CommunicationException(ex.toString());
            }
        }
    }

    /**
     * Rename the bound object.
     *
     * @param oldname old name that the object is bound as.
     * @param newname new name that the object will be bound as.
     * @throws NamingException if there is a naming exception.
     */
    public void rename(Name oldname, Name newname) throws NamingException {
        // Flat namespace; no federation; just call string version
        rename(oldname.toString(), newname.toString());
    }

    /**
     * List the contents of the specified context.
     *
     * @param name context name.
     * @return an enumeration of the contents.
     * @throws NamingException if there is a naming exception.
     */
    public NamingEnumeration list(String name) throws NamingException {
        if (name.equals("")) {
            // listing this context
            try {
                Hashtable bindings = getProvider().list(myName);
                return new RepNames(bindings);
            } catch (RemoteException ex) {
                throw new CommunicationException(ex.toString());
            }
        }

        name = getRelativeName(name);
        if (isjavaURL(name)) {
            return javaUrlContext.list(name);
        } else {
            // Perhaps 'name' names a context
            Object target = lookup(name);
            if (target instanceof Context) {
                return ((Context) target).list("");
            }
            throw new NotContextException(name + " cannot be listed");
        }
    }

    /**
     * List the contents of the specified context.
     *
     * @param name context name.
     * @return an enumeration of the contents.
     * @throws NamingException if there is a naming exception.
     */
    public NamingEnumeration list(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }

    /**
     * List the bindings in the specified context.
     *
     * @param name context name.
     * @return an enumeration of the bindings.
     * @throws NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        if (name.equals("")) {
            // listing this context
            try {
                Hashtable bindings = getProvider().list(myName);
                return new RepBindings(bindings);
            } catch (RemoteException ex) {
                CommunicationException ce = new CommunicationException(ex
                        .toString());
                ce.initCause(ex);
                throw ce;
            }
        }

        name = getRelativeName(name);
        if (isjavaURL(name)) {
            return javaUrlContext.listBindings(name);
        } else {
            // Perhaps 'name' names a context
            Object target = lookup(name);
            if (target instanceof Context) {
                return ((Context) target).listBindings("");
            }
            throw new NotContextException(name + " cannot be listed");
        }
    }

    /**
     * List the bindings in the specified context.
     *
     * @param name context name.
     * @return an enumeration of the bindings.
     * @throws NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

    /**
     * Destroy the specified subcontext.
     *
     * @param name name of the subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public void destroySubcontext(String name) throws NamingException {
        name = getRelativeName(name);
        if (isjavaURL(name)) {
            javaUrlContext.destroySubcontext(name);
        } else {
            try {
                getProvider().destroySubcontext(name);
            } catch (RemoteException e) {
                CommunicationException ce = new CommunicationException(e
                        .toString());
                ce.initCause(e);
                throw ce;
            }
        }
    }

    /**
     * Destroy the specified subcontext.
     *
     * @param name name of the subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public void destroySubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        destroySubcontext(name.toString());
    }

    /**
     * Create the specified subcontext.
     *
     * @param name name of the subcontext.
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public Context createSubcontext(String name) throws NamingException {
        Context c = null;
        name = getRelativeName(name);
        if (isjavaURL(name)) {
            return javaUrlContext.createSubcontext(name);
        } else {
            try {
                c = getProvider().createSubcontext(name);
                /*
                 * this simulates the transient context structure on the client
                 * side. Have to do this - as reference to Transient Context is
                 * not resolved properly due to rmi
                 */
                if (c instanceof Context) {
                    c = new SerialContext(name, myEnv, habitat);
                }
            } catch (RemoteException e) {
                CommunicationException ce = new CommunicationException(e
                        .toString());
                ce.initCause(e);
                throw ce;
            }
            return c;
        }
    }

    /**
     * Create the specified subcontext.
     *
     * @param name name of the subcontext.
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public Context createSubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return createSubcontext(name.toString());
    }

    /**
     * Links are not treated specially.
     *
     * @param name name of the link.
     * @return the resolved object.
     * @throws NamingException if there is a naming exception.
     */
    public Object lookupLink(String name) throws NamingException {
        name = getRelativeName(name);
        if (isjavaURL(name)) {
            return javaUrlContext.lookupLink(name);
        } else {
            // This flat context does not treat links specially
            return lookup(name);
        }
    }

    /**
     * Links are not treated specially.
     *
     * @param name name of the link.
     * @return the resolved object.
     * @throws NamingException if there is a naming exception.
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * Allow access to the name parser object.
     *
     * @param name JNDI name, is ignored since there is only one Name Parser
     *             object.
     * @return NameParser object
     * @throws NamingException
     */
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    /**
     * Allow access to the name parser object.
     *
     * @param name JNDI name, is ignored since there is only one Name Parser
     *             object.
     * @return NameParser object
     * @throws NamingException
     */
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix)
            throws NamingException {
        Name result = composeName(new CompositeName(name), new CompositeName(
                prefix));
        return result.toString();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }

    /**
     * Add to the environment for the current context.
     *
     * @throws NamingException if there is a naming exception.
     */
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
        }
        return myEnv.put(propName, propVal);
    }

    /**
     * Remove from the environment for the current context.
     *
     * @throws NamingException if there is a naming exception.
     */
    public Object removeFromEnvironment(String propName) throws NamingException {
        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    /**
     * Return the environment for the current context.
     *
     * @throws NamingException if there is a naming exception.
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            myEnv = new Hashtable(3, 0.75f);
        }
        return myEnv;
    }

    /**
     * Set the environment for the current context to null when close is called.
     *
     * @throws NamingException if there is a naming exception.
     */
    public void close() throws NamingException {
        myEnv = null;
    }

    private String getRelativeName(String name) {
        if (!myName.equals("")) {
            name = myName + "/" + name;
        }
        return name;
    }

    // Class for enumerating name/class pairs
    class RepNames implements NamingEnumeration {
        Hashtable bindings;

        Enumeration names;

        RepNames(Hashtable bindings) {
            this.bindings = bindings;
            this.names = bindings.keys();
        }

        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public Object nextElement() {
            if (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String className = bindings.get(name).getClass().getName();
                return new NameClassPair(name, className);
            } else
                return null;
        }

        public Object next() throws NamingException {
            return nextElement();
        }

        // New API for JNDI 1.2
        public void close() throws NamingException {
            throw new OperationNotSupportedException("close() not implemented");
        }
    }

    // Class for enumerating bindings
    class RepBindings implements NamingEnumeration {
        Enumeration names;

        Hashtable bindings;

        RepBindings(Hashtable bindings) {
            this.bindings = bindings;
            this.names = bindings.keys();
        }

        public boolean hasMoreElements() {
            return names.hasMoreElements();
        }

        public boolean hasMore() throws NamingException {
            return hasMoreElements();
        }

        public Object nextElement() {
            if (hasMoreElements()) {
                String name = (String) names.nextElement();
                return new Binding(name, bindings.get(name));
            } else
                return null;
        }

        public Object next() throws NamingException {
            return nextElement();
        }

        // New API for JNDI 1.2
        public void close() throws NamingException {
            throw new OperationNotSupportedException("close() not implemented");
        }
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("SerialContext ");
        if(testMode) {
            sb.append("( IN TEST MODE ) ");
        }
        if( targetHost != null) {
            sb.append("targetHost="+targetHost);
        }
        if( targetPort != null) {
            sb.append(",targetPort="+targetPort);
        }

        if( orb != null ) {
            sb.append(",orb'sInitialHost="+orbsInitialHostValue);
            sb.append(",orb'sInitialPort="+orbsInitialPortValue);
        }

        return sb.toString();

    }

    private class ProviderCacheKey {

        // Key is either orb OR host/port combo.
        private ORB orb;

        private String host;
        private String port;

        public ProviderCacheKey(ORB orb) {
            this.orb = orb;
        }

        // Host and Port must both be non-null
        public ProviderCacheKey(String host, String port) {
            this.host = host;
            this.port = port;
        }

        public int hashCode() {
            return (orb != null) ? orb.hashCode() : host.hashCode();
        }

        public boolean equals(Object other) {
            boolean equal = false;

            if( (other != null) && (other instanceof ProviderCacheKey) ) {
                ProviderCacheKey otherKey = (ProviderCacheKey) other;
                if( orb != null ) {
                    equal = (orb == otherKey.orb);
                } else {
                    if( (otherKey.host != null) && host.equals(otherKey.host)
                        && port.equals(otherKey.port) ) {
                        equal = true;
                    }
                }

            }

            return equal;
        }
    }

}
