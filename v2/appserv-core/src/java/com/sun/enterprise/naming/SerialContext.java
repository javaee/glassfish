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
package com.sun.enterprise.naming;

import java.util.*;
import java.io.*;
import javax.naming.*;
import org.omg.CORBA.ORB;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.naming.java.javaURLContext;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextHelper;
import com.sun.enterprise.server.ondemand.entry.*;

import java.util.logging.*;
import com.sun.logging.*;


/**
 * This is a JNDI Context implementation for storing serializable objects.
 * This is the default Context for the J2EE RI. Lookups of unqualified
 * names (i.e. names not starting with "java:", "corbaname:" etc)
 * are serviced by SerialContext. The namespace is implemented in
 * the J2EE server in the SerialContextProviderImpl object, which is
 * accessed over RMI-IIOP from clients.
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 */
public class SerialContext implements Context {

    private static final Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);

    private static final NameParser myParser = new SerialNameParser();

    private Hashtable myEnv=null;  // THREAD UNSAFE
    
    private SerialContextProvider provider;  // THREAD UNSAFE
    private static Hashtable providerCache = new Hashtable();
    private final String myName;
    private final javaURLContext javaUrlContext;

    private static final String JAVA_URL = "java:";
    // private InitialContext cosContext;   
    private static final Boolean threadlock = new Boolean(true);

    private static final ThreadLocal stickyContext = new ThreadLocal();

    private final boolean isEE;
    private ORB orb ;
    private String host ;
    private String port ;


    /**
     * set and get methods for preserving stickiness.
     * This is a temporary solution
     * to store the sticky IC as a thread local variable.
     * This sticky IC will be used by all classes that require a context
     * object to do lookup (if LB is enabled)
     * SerialContext.lookup() sets a value for the thread local variable 
     * (stickyContext) before performing th lookup in case LB is enabled.
     * If not, the thread local variable is null. 
     * At the end of the SerialContext.lookup() method, the thread local
     * variable gets set to null.
     * So actually speaking, more than being a global variable for the 
     * entire thread, its global only during the execution of the 
     * SerialContext.lookup() method. bug 5050591
     * This will be cleaned for the next release.
     *
     */
    public static void setSticky(ThreadLocalIC var) {
        stickyContext.set(var);
    }

    public static ThreadLocalIC getSticky() {
        return (ThreadLocalIC) stickyContext.get();
    }
  
  /**
   * This method is called from SerialInitContextFactory & S1ASCtxFactory
   * to check if sticky context is set.
   */
    public static Context getStickyContext() {
        return getSticky().getStickyContext();
    }


    /**
     * method to narrow down the provider. 
     * common code that is called from getProvider()
     */
    private SerialContextProvider narrowProvider(org.omg.CORBA.Object ref) 
                       throws Exception {

        NamingContext nctx = NamingContextHelper.narrow(ref);
	NameComponent[] path = 
	{ new NameComponent("SerialContextProvider", "") };
	synchronized (threadlock) {	
	    provider = (SerialContextProvider)
	      PortableRemoteObject.narrow(nctx.resolve(path),
					  SerialContextProvider.class);
	}
	return provider;
    }


    private SerialContextProvider getProvider() 
      throws NamingException {
	try {
	    if (provider == null) {
	        // Get orb to use for connecting to NameService
	      
	        if (Switch.getSwitch().getContainerType() ==
                    Switch.EJBWEB_CONTAINER) {
		    if (_logger.isLoggable(Level.FINE)) {
		        _logger.fine("lookup call inside the server VM...");
		    }
		    if (orb == null && host == null && port == null) {
		        return Switch.getSwitch().getProviderManager().getLocalProvider();		
		    } else
		        return getRemoteProvider();         
		} else {	
		    _logger.fine("lookup call outside the server VM...");	
		    return getRemoteProvider();
		}
	    }  else return provider;     
	} catch (Exception ex) {
	    setSticky(null);
	    CommunicationException ce = new CommunicationException
		("Can't find SerialContextProvider");
	    ce.initCause(ex);
	    throw ce;
	}		  
	

    }

    private SerialContextProvider getRemoteProvider() 
      throws Exception {         
	_logger.fine(" Inside getRemoteProvider()");
	org.omg.CORBA.Object ref = null;

	//if the following is not null, then we are in EE world
	//else PE

	if (isEE == true) {
	    orb = ORBManager.getORB();
	    ref = orb.string_to_object((String)(myEnv.get(
							  "com.sun.appserv.ee.iiop.endpointslist")));
	    return narrowProvider(ref);
	    
	} else {
	    // in PE mode
	    if ( orb == null ) {
	      _logger.fine("orb == null");
	        if (host != null) {
		    if (port == null) {		     
		        //assume default port of 3700
		        port = ORBManager.DEFAULT_ORB_INIT_PORT;
		    }
		} else {
		    if (port != null) {
		        host = ORBManager.DEFAULT_ORB_INIT_HOST;
		    } else {
		        //both host and port are null and so is the orb
		        //this can happen from appclient or standalone client 
		        //but for within the ejb container its already taken care of in getProvider()
		        _logger.fine("host,port and orb are null");
		        return getCachedProvider(ORBManager.getORB());
		    }	    
		}
	
		orb = ORBManager.getORB();
	
		if (_logger.isLoggable(Level.FINE))
		    print(orb);


		provider = (SerialContextProvider) providerCache.get(host + ":" + port);
	
		if (provider == null) {
		    ref = orb.string_to_object("corbaloc:iiop:1.2@" + 
					       host + ":" + port + "/NameService");
		    provider = narrowProvider(ref);

		    providerCache.put(host + ":" + port, provider);
		    _logger.fine("created the provider.. " +host + ":" + port + " provider = " + provider);		   
		} else  _logger.fine("provider for " + host + ":" + port + "already exists");
		return provider;
	    } else {
	      return getCachedProvider(orb);	  
	    }
	}
    }

    private SerialContextProvider getCachedProvider(ORB orb) 
      throws Exception {
	org.omg.CORBA.Object ref = null;
	_logger.fine("inside getCachedProvider..." + orb);

	String orbHost = ((com.sun.corba.ee.impl.orb.ORBImpl)orb).
	  getORBData().getORBInitialHost();

	int orbPort = ((com.sun.corba.ee.impl.orb.ORBImpl)orb).
	  getORBData().getORBInitialPort();  

	provider = (SerialContextProvider) providerCache.get(orbHost + ":" + new Integer(orbPort).toString());
	// thread-safety: race condition
	if (provider == null) {
	  ref = orb.resolve_initial_references("NameService");
	  provider = narrowProvider(ref);
	  providerCache.put(orbHost + ":" + new Integer(orbPort).toString(), provider);
	}
	_logger.fine("returning provider..." + provider + orbHost + ":" + new Integer(orbPort).toString());
	return provider;
    }
    
    //internal API for logging
    public void print(ORB orb) {
        _logger.fine("SerialContext ==> SerialContext instance created : " + this);

	if (orb != null) {	
	    _logger.fine("SerialContext ==> ORB instance : " + orb);

	    String host = ((com.sun.corba.ee.impl.orb.ORBImpl)orb).
	      getORBData().getORBInitialHost();
	    int port = ((com.sun.corba.ee.impl.orb.ORBImpl)orb).
	      getORBData().getORBInitialPort();                    	
	    
	    _logger.fine("SerialContext ==> ORB HOST : " + host +
			       ", ORB PORT : " + port);
	} else _logger.fine("print() => orb is null");
    }

    /**
     * Constructor for the context. Initializes the object reference
     * to the remote provider object.
     */
    public SerialContext(String name, Hashtable environment) throws NamingException {
     
        myEnv = (environment != null)
	    ? (Hashtable)(environment.clone()) 
	    : null;
      
        // Dont initialize provider now, this throws an exception
        // if J2EEServer is not yet started. Get it lazily when needed.
        //provider = SerialContext.getProvider(myEnv);
        
        this.myName = name;
        if (_logger.isLoggable(Level.FINE))
            _logger.fine("SerialContext ==> SerialContext instance created : " + this);	

        // using these two temp variables allows instance variables
        // to be 'final'.
        javaURLContext  urlContextTemp = null;
        boolean         isEETemp    = false;
        if (myEnv.get("com.sun.appserv.ee.iiop.endpointslist") != null) {
            isEETemp = true;
            urlContextTemp = new javaURLContext(myEnv, this);
        }
        else {
            urlContextTemp = new javaURLContext(myEnv, null);
        }
        javaUrlContext  = urlContextTemp;
        isEE    = isEETemp;
        orb = (ORB) myEnv.get(ORBManager.JNDI_CORBA_ORB_PROPERTY);
        host = (String)myEnv.get(ORBManager.OMG_ORB_INIT_HOST_PROPERTY);
        port = (String)myEnv.get(ORBManager.OMG_ORB_INIT_PORT_PROPERTY);

    }
    
    /**
     * This constructor takes the component id as an argument. All 
     * name arguments to operations are prepended by the component id.
     */
    public SerialContext(Hashtable env) throws NamingException {
        this("", env);
    }


    /**
     * The getNameInNamespace API is not supported in this context.
     * @exception NamingException if there is a naming exception.
     */
    public String getNameInNamespace() throws NamingException {
	return myName;
    }

    /**
     * method to check if the name to look up starts with "java:"
     */
    private boolean isjavaURL(String name) {
     
        if (name.startsWith(JAVA_URL)) {	    	    
	    return true;
	} else return false;
    }

    /**
     * Generates an entry context and give it to ondemand initialization framework.
     */
    public void generateEntryContext(Object context) {
        ServerEntryHelper.generateJndiEntryContext((String) context);
    }

    /**
     * method for checking the count and decrementing it
     * also resets the sticky context to null if count is 0
     */
    private void resetSticky() {
        if (getSticky() != null) {
	    getSticky().decrementCount();
	
	    if (getSticky().getStickyCount() == 0) {
	        setSticky(null);
	    }
	}
    }
    
    /**
     * Lookup the specified name in the context.
     * Returns the resolved object. 
     * @return the resolved object.
     * @exception NamingException if there is a naming exception.
     */
    public Object lookup(String name) throws NamingException {

        //Before enything call ondemand initialization framework.
        generateEntryContext(name);

        /**
	 * In case a user is creating an IC with env passed
	 * in constructor; env specifies endpoints in some form
	 * in that case, the sticky IC should be stores as a thread local 
	 * variable. 
	 *
	 */
        if (myEnv.get("com.sun.appserv.ee.iiop.endpointslist") != null) {
	    if (getSticky() == null) {
	        ThreadLocalIC threadLocal = new ThreadLocalIC(this,1);	
		setSticky(threadLocal);
	    } else getSticky().incrementCount();
	}

        if (_logger.isLoggable(Level.FINE))
	    _logger.fine("SerialContext ==> doing lookup with " + this);
        if (name.equals("")) {
	    resetSticky();
            // Asking to look up this context itself.  Create and return
            // a new instance with its own independent environment.
            return (new SerialContext(myName, myEnv));
        } 
	name = getRelativeName(name);
        if (_logger.isLoggable(Level.FINE))
	    _logger.fine("SerialContext ==> looking up : " + name);

        try {
	    if (isjavaURL(name)) {
	        resetSticky();
		return javaUrlContext.lookup(name);
	    } else {	        
		Object obj = getProvider().lookup(name);
		if(obj instanceof Context) {
		    resetSticky();
		    return new SerialContext(name, myEnv);
		}
		Object retObj = 
		    javax.naming.spi.NamingManager.getObjectInstance(obj, 
								     new CompositeName(name),
								     null, myEnv);
		resetSticky();

		return retObj;
	    }
	} catch (NamingException nnfe) {
	    setSticky(null);
	    throw nnfe;
	} catch (Exception ex) {
	    setSticky(null);
            _logger.log(Level.SEVERE,
                        "enterprise_naming.serialctx_communication_exception",
                        ex);
	    //temp fix for 6320008
	    //this should be removed once we change the transient NS implementation to persistent
	    if (ex instanceof java.rmi.MarshalException && 
		ex.getCause() instanceof org.omg.CORBA.COMM_FAILURE) {
	        provider = null; 
		_logger.fine("Resetting provider to NULL. Will get new obj ref for provider since previous obj ref was stale...");
		return lookup(name);		
	    } else {
	        CommunicationException ce = 
		  new CommunicationException("serial context communication ex");
		ce.initCause(ex);
		throw ce;
	    }
	}
	
    }

    /**

     * Lookup the specifed name in the context.
     * Returns the resolved object.
     * @return the resolved object.
     * @exception NamingException if there is a naming exception.
     */
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString()); 
    }

    /**
     * Bind the object to the specified name.
     * @param the name that the object is being bound to.
     * @param the object that is being bound.
     * @exception NamingException if there is a naming exception.
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
     * @param the name that the object is being bound to.
     * @param the object that is being bound.
     * @exception NamingException if there is a naming exception.
     */
    public void bind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        bind(name.toString(), obj);
    }

    /**
     * Rebind the object to the specified name.
     * @param the name that the object is being bound to.
     * @param the object that is being bound.
     * @exception NamingException if there is a naming exception.
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
     * @param the name that the object is being bound to.
     * @param the object that is being bound.
     * @exception NamingException if there is a naming exception.
     */
    public void rebind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        rebind(name.toString(), obj);
    }

    /**
     * Unbind the object with the specified name.
     * @param the name that is being unbound.
     * @exception NamingException if there is a naming exception.
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
     * @param the name that is being unbound.
     * @exception NamingException if there is a naming exception.
     */
    public void unbind(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        unbind(name.toString());
    }

    /**
     * Rename the bound object.
     * @param the old name that the object is bound as.
     * @param the new name that the object will be bound as.
     * @exception NamingException if there is a naming exception.
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
     * @param the old name that the object is bound as.
     * @param the new name that the object will be bound as.
     * @exception NamingException if there is a naming exception.
     */
    public void rename(Name oldname, Name newname)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        rename(oldname.toString(), newname.toString());
    }

    /**
     * List the contents of the specified context.
     * @return an enumeration of the contents.
     * @param the context name.
     * @exception NamingException if there is a naming exception.
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
	        return ((Context)target).list("");
	    }
	    throw new NotContextException(name + " cannot be listed");
	}
    }

    /**
     * List the contents of the specified context.
     * @return an enumeration of the contents.
     * @param the context name.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration list(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }

    /**
     * List the bindings in the specified context.
     * @return an enumeration of the bindings.
     * @param the context name.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(String name)
            throws NamingException {
        if (name.equals("")) {
            // listing this context
            try {
                Hashtable bindings = getProvider().list(myName);
                return new RepBindings(bindings);
            } catch (RemoteException ex) {
                CommunicationException ce = 
                    new CommunicationException(ex.toString());
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
	        return ((Context)target).listBindings("");
	    }
	    throw new NotContextException(name + " cannot be listed");
	}
    }

    /**
     * List the bindings in the specified context.
     * @return an enumeration of the bindings.
     * @param the context name.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

    /**
     * Destroy the specified subcontext.
     * @param the name of the subcontext.
     * @exception NamingException if there is a naming exception.
     */
    public void destroySubcontext(String name) throws NamingException {
	name = getRelativeName (name);
	if (isjavaURL(name)) {	    
	    javaUrlContext.destroySubcontext(name);
	} else {
	    try {
	        getProvider().destroySubcontext(name);
	    } catch(RemoteException e) {
	        CommunicationException ce = 
		  new CommunicationException(e.toString());
		ce.initCause(e);
		throw ce;
	    }
	}
    }

    /**
     * Destroy the specified subcontext.
     * @param the name of the subcontext.
     * @exception NamingException if there is a naming exception.
     */
    public void destroySubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        destroySubcontext(name.toString());
    }

    /**
     * Create the specified subcontext.
     * @return the created subcontext.
     * @param the name of the subcontext.
     * @exception NamingException if there is a naming exception.
     */
    public Context createSubcontext(String name)
            throws NamingException {
	Context c = null;
	name = getRelativeName (name);
	if (isjavaURL(name)) {	    
	    return javaUrlContext.createSubcontext(name);
	} else {
	    try {
	        c = getProvider().createSubcontext(name);
		/* this simulates the transient context structure on the
		 * client side. Have to do this - as reference to
		 * Transient Context is not resolved properly due to rmi
		 */
		if (c instanceof Context){
		    c = new SerialContext (name, myEnv);
		}
	    } catch(RemoteException e) {
	        CommunicationException ce = 
		  new CommunicationException(e.toString());
		ce.initCause(e);
		throw ce;
	    }
	    return c;
	}
    }

    /**
     * Create the specified subcontext.
     * @return the created subcontext.
     * @param the name of the subcontext.
     * @exception NamingException if there is a naming exception.
     */
    public Context createSubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return createSubcontext(name.toString());
    }

    /**
     * Links are not treated specially.
     * @param the name of the link.
     * @return the resolved object.
     * @exception NamingException if there is a naming exception.
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
     * @param the name of the link.
     * @return the resolved object.
     * @exception NamingException if there is a naming exception.
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * Allow access to the name parser object.
     * @param String JNDI name, is ignored since there is only one Name
     * Parser object.
     * @exception NamingException
     * @return NameParser object
     */
    public NameParser getNameParser(String name)
            throws NamingException {
        return myParser;
    }

    /**
     * Allow access to the name parser object.
     * @param String JNDI name, is ignored since there is only one Name
     * Parser object.
     * @exception NamingException
     * @return NameParser object
     */
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }

    public String composeName(String name, String prefix)
            throws NamingException {
        Name result = composeName(new CompositeName(name),
                                  new CompositeName(prefix));
        return result.toString();
    }

    public Name composeName(Name name, Name prefix)
            throws NamingException {
        Name result = (Name)(prefix.clone());
        result.addAll(name);
        return result;
    }

    /**
     * Add to the environment for the current context.
     * @exception NamingException if there is a naming exception.
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
     * @exception NamingException if there is a naming exception.
     */
    public Object removeFromEnvironment(String propName) 
		throws NamingException {
        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    /**
     * Return the environment for the current context.
     * @exception NamingException if there is a naming exception.
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            myEnv = new Hashtable(3, 0.75f);
        }
        return myEnv;
    }

    /**
     * Set the environment for the current context to null when close is
     * called.
     * @exception NamingException if there is a naming exception.
     */
    public void close() throws NamingException {
        myEnv = null;
    }

    private String getRelativeName(String name) {
	if(!myName.equals("")) {
	    name = myName + "/" + name;
	} 
	return name;
    }

    // Class for enumerating name/class pairs
    class RepNames implements NamingEnumeration {
        Hashtable bindings;
        Enumeration names;

        RepNames (Hashtable bindings) {
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
            if(names.hasMoreElements())
            {
                String name = (String)names.nextElement();
                String className = bindings.get(name).getClass().getName();
                return new NameClassPair(name, className);
            }
            else
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

        RepBindings (Hashtable bindings) {
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
            if(hasMoreElements())
            {
                String name = (String)names.nextElement();
                return new Binding(name, bindings.get(name));
            }
            else
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


  /**
   * This is a temporary solution to store 
   * the sticky context as a threadlocal variable. bug 5050591
   * context is the sticky context
   * count is needed to know how many times the lookup method is being called
   * from within the user code's ic.lookup(). e.g. JMS resource lookups (via ConnectorObjectFactory)
   */
   class  ThreadLocalIC {

        Context ctx;
        int count = 0;

        public ThreadLocalIC(Context ctxIn, int countIn) {
            ctx = ctxIn;
            count = countIn;
        }

        public void setStickyContext(Context ctxIn) {
            ctx = ctxIn; 
        }
    
        public Context getStickyContext() {
            return ctx;
        }
    
        public void setStickyCount(int countIn) {
            count = countIn;
        }

        public int getStickyCount() {
            return count;      
        }

        public void incrementCount() {
            count++;
        }

        public void decrementCount() {
            count--;
        }
    }
};


