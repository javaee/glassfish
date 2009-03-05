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
import org.glassfish.api.naming.NamingObjectProxy;
import org.jvnet.hk2.component.Habitat;

import javax.naming.*;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    private static final String JAVA_GLOBAL_URL = "java:global";


    private static Logger _logger = LogFacade.getLogger();

    private static final NameParser myParser = new SerialNameParser();

    private static Hashtable providerCache = new Hashtable();


    private Hashtable myEnv = null; // THREAD UNSAFE

    private SerialContextProvider provider; // THREAD UNSAFE

    private final String myName;

    private final JavaURLContext javaUrlContext;


    // private InitialContext cosContext;

    private final Habitat habitat;


    /**
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
     * NOTE: all stickyContext logic removed for initial V3 release.  We'll
     * revisit the underlying issue when adding load-balancing support post-V3.
     */




    private SerialContextProvider getProvider() throws NamingException {

        if (provider == null) {
            provider = ProviderManager.getProviderManager().getLocalProvider();
        }

        return provider;
    }

    /**
     * Constructor for the context. Initializes the object reference to the
     * remote provider object.
     */
    public SerialContext(String name, Hashtable environment, Habitat habitat)
            throws NamingException {

        this.habitat = habitat;
        
        myEnv = (environment != null) ? (Hashtable) (environment.clone())
                : null;

        this.myName = name;
        if (_logger.isLoggable(Level.FINE))
            _logger.fine("SerialContext ==> SerialContext instance created : "
                    + this);

        // using these two temp variables allows instance variables
        // to be 'final'.
        JavaURLContext urlContextTemp = null;

        if (myEnv.get("com.sun.appserv.ee.iiop.endpointslist") != null) {

            urlContextTemp = new JavaURLContext(myEnv, this);
        } else {
            urlContextTemp = new JavaURLContext(myEnv, null);
        }
        javaUrlContext = urlContextTemp;

    }

    /**
     * This constructor takes the component id as an argument. All name
     * arguments to operations are prepended by the component id.
     */
    public SerialContext(Hashtable env, Habitat habitat) throws NamingException {
        this("", env, habitat);
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
                Object retObj = javax.naming.spi.NamingManager
                        .getObjectInstance(obj, new CompositeName(name), null,
                                myEnv);

                return retObj;
            }
        } catch (NamingException nnfe) {
            throw nnfe;
        } catch (Exception ex) {
            _logger.log(Level.SEVERE,
                    "enterprise_naming.serialctx_communication_exception", ex);
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
                        "serial context communication ex");
                ce.initCause(ex);
                throw ce;
            }
        }

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

};
