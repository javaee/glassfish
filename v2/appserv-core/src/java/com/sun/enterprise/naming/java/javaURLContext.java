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
package com.sun.enterprise.naming.java;

import java.util.*;
import java.io.*;
import javax.naming.*;

import com.sun.ejb.containers.BaseContainer;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.naming.*;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.distributedtx.UserTransactionImpl;
import com.sun.enterprise.distributedtx.TransactionSynchronizationRegistryImpl;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * This class is a context implementation for the java:comp namespace.
 * The context determines the component id from the invocation manager
 * of the component that is invoking the method and then looks up the
 * object in that component's local namespace.
 */

public final class javaURLContext implements Context, Cloneable {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);
    // END OF IASRI 4660742

    // Global objects in component namespace
    private static final String ORB_STRING = "java:comp/ORB";
    private static final String HANDLE_DELEGATE = "java:comp/HandleDelegate";
    private static final String USER_TX = "java:comp/UserTransaction";
    private static final String EJB_CONTEXT = "java:comp/EJBContext";
    private static final String EJB_TIMER_SERVICE = "java:comp/TimerService";
    private static final String TRANSACTION_SYNC_REGISTRY = 
                                "java:comp/TransactionSynchronizationRegistry";
    private static final String TRANSACTION_MGR = "java:pm/TransactionManager";
    public static final String APPSERVER_TRANSACTION_MGR = "java:appserver/TransactionManager";


    private static final boolean debug = false;

    private NamingManagerImpl namingManager;
    private Hashtable myEnv;
    //private Context ctx; XXX not needed ?
    private String myName="";

    private SerialContext serialContext = null;
    
    /**
     * Create a context with the specified environment. 
     */
    public javaURLContext(Hashtable environment) 
	throws NamingException 
    {
        myEnv = (environment != null) ? (Hashtable)(environment.clone()) : null;
	if (namingManager == null ) {
	  namingManager = (NamingManagerImpl)
	    Switch.getSwitch().getNamingManager();
	}	
    }

    /**
     * Create a context with the specified name+environment. 
     * Called only from NamingManagerImpl.
     */
    public javaURLContext(String name, Hashtable env) 
	throws NamingException 
    {
	this(env);
	this.myName = name;
    }

    /**
     * this constructor is called from SerialContext class
    */
    public javaURLContext(Hashtable env, SerialContext serialContext) 
	throws NamingException 
    {
	this(env);
	this.serialContext = serialContext;
    }
  
  /**
   * add SerialContext to preserve stickiness to cloned instance 
   * why clone() : to avoid the case of multiple threads modifying 
   * the context returned for ctx.lookup("java:com/env/ejb")
   */
    public javaURLContext addStickyContext(SerialContext serialContext) 
      throws NamingException {
	try {
	    javaURLContext jCtx = (javaURLContext)this.clone();
	    jCtx.serialContext = serialContext;
	    return jCtx;
	} catch (java.lang.CloneNotSupportedException ex) {	   
	     NamingException ne = new NamingException("problem with cloning javaURLContext instance");
	     ne.initCause(ex);
	     throw ne;
	}
    }

    /**
     * Lookup an object in the serial context.
     * @return the object that is being looked up.
     * @exception NamingException if there is a naming exception.
     */
    public Object lookup(String name) throws NamingException {
        if (_logger.isLoggable(Level.FINE))
	    _logger.log(Level.FINE,"In javaURLContext.lookup, name = "+name + " serialcontext..." + serialContext);
	    		
	if ( name.equals("") ) {
	    /** javadocs for Context.lookup:
	     * If name is empty, returns a new instance of this context 
	     * (which represents the same naming context as this context, 
	     * but its environment may be modified independently and it may
	     * be accessed concurrently).
	     */
	    return new javaURLContext(myName, myEnv);
	}

	String fullName = name;
	if ( !myName.equals("") ) {
	    if ( myName.equals("java:") ) 
		fullName = myName + name;
	    else 
		fullName = myName + "/" + name;
	}

	try {
	    if ( fullName.startsWith("java:comp/env") ) {
		// name is in component specific namespace	     
	        return namingManager.lookup(fullName, serialContext);
	    }
	    else {
		// name is for a global object
		if( fullName.equals(ORB_STRING) ) {
		    // return the singleton ORB instance
		    return ORBManager.getORB();
		}
		else if ( fullName.equals(USER_TX) ) {
		    InvocationManager invMgr = Switch.getSwitch().getInvocationManager();
		    ComponentInvocation inv = null;
		    if (invMgr != null) { 
		        inv = invMgr.getCurrentInvocation();
		    }
		    if ((inv != null) && (inv.container != null)) {
		        if (inv.container instanceof BaseContainer) {
			    BaseContainer container = (BaseContainer) inv.container;
			    container.checkUserTransactionLookup(inv);
			}
		    }
		    // UserTransactionImpl is mutable so return new instance 
		    return new UserTransactionImpl(); 
		}
		else if ( fullName.equals(EJB_TIMER_SERVICE) ) {
		    // return the EJB Timer Service.  Only works for ejbs.
		    return Switch.getSwitch().getContainerFactory().
                        getEJBContextObject("javax.ejb.TimerService");
		} else if ( fullName.equals(EJB_CONTEXT) ) {
                    // return the EJB Timer Service.  Only works for ejbs.
		    return Switch.getSwitch().getContainerFactory().
                        getEJBContextObject("javax.ejb.EJBContext");
		} else if ( fullName.equals(HANDLE_DELEGATE) ) {
		    return Switch.getSwitch().getHandleDelegate();
		}
		else if ( fullName.equals(TRANSACTION_MGR) ) {
		    return Switch.getSwitch().getContainerFactory().getTransactionMgr();
		}
		else if ( fullName.equals(APPSERVER_TRANSACTION_MGR) ) {
		    return com.sun.enterprise.transaction.TransactionManagerHelper.getTransactionManager();
		}
		else if ( fullName.equals(TRANSACTION_SYNC_REGISTRY) ) {
		    return TransactionSynchronizationRegistryImpl.getInstance();
		}
		else {
		    // try NamingManager		  
		  return namingManager.lookup(fullName, serialContext);
		}
	    }
	} catch ( NamingException ex ) {
	    throw ex;
	} catch ( Exception ex ) {
	    throw (NamingException)(new NameNotFoundException("No object bound for "+fullName)).initCause(ex);
	}
    }

    /**
     * Lookup a name in either the cosnaming or serial context.
     * @return the object that is being looked up.
     * @exception NamingException if there is a naming exception.
     */
    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString()); 
    }

    /**
     * Bind an object in the namespace. Binds the reference to the
     * actual object in either the cosnaming or serial context.
     * @exception NamingException if there is a naming exception.
     */
    public void bind(String name, Object obj) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * Bind an object in the namespace. Binds the reference to the
     * actual object in either the cosnaming or serial context.
     * @exception NamingException if there is a naming exception.
     */
    public void bind(Name name, Object obj) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * Rebind an object in the namespace. Rebinds the reference to the
     * actual object in either the cosnaming or serial context.
     * @exception NamingException if there is a naming exception.
     */
    public void rebind(String name, Object obj) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * Rebind an object in the namespace. Rebinds the reference to the
     * actual object in either the cosnaming or serial context.
     * @exception NamingException if there is a naming exception.
     */
    public void rebind(Name name, Object obj) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * Unbind an object from the namespace. 
     * @exception NamingException if there is a naming exception.
     */
    public void unbind(String name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * Unbind an object from the namespace. 
     * @exception NamingException if there is a naming exception.
     */
    public void unbind(Name name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * The rename operation is not supported by this context. It throws
     * an OperationNotSupportedException.
     */
    public void rename(String oldname, String newname) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * The rename operation is not supported by this context. It throws
     * an OperationNotSupportedException.
     */
    public void rename(Name oldname, Name newname)
            throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * The destroySubcontext operation is not supported by this context. 
     * It throws an OperationNotSupportedException.
     */
    public void destroySubcontext(String name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    /**
     * The destroySubcontext operation is not supported by this context. 
     * It throws an OperationNotSupportedException.
     */
    public void destroySubcontext(Name name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    public Context createSubcontext(String name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }

    public Context createSubcontext(Name name) throws NamingException {
	throw new NamingException("java:comp namespace cannot be modified");
    }


    /**
     * Lists the contents of a context or subcontext. The operation is
     * delegated to the serial context.
     * @return an enumeration of the contents of the context.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration list(String name)
            throws NamingException {
        if (name.equals("")) {
            // listing this context
	    if ( namingManager == null )
		throw new NamingException();
	    return namingManager.list(myName);
        } 

        // Check if 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context)target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * Lists the contents of a context or subcontext. The operation is
     * delegated to the serial context.
     * @return an enumeration of the contents of the context.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration list(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }

    /**
     * Lists the bindings of a context or subcontext. The operation is
     * delegated to the serial context.
     * @return an enumeration of the bindings of the context.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(String name)
            throws NamingException {
        if (name.equals("")) {
            // listing this context
	    if ( namingManager == null )
		throw new NamingException();
	    return namingManager.listBindings(myName);
        } 

        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context)target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * Lists the bindings of a context or subcontext. The operation is
     * delegated to the serial context.
     * @return an enumeration of the bindings of the context.
     * @exception NamingException if there is a naming exception.
     */
    public NamingEnumeration listBindings(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

    /**
     * This context does not treat links specially. A lookup operation is
     * performed.
     */
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    /**
     * This context does not treat links specially. A lookup operation is
     * performed.
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * Return the name parser for the specified name.
     * @return the NameParser instance.
     * @exception NamingException if there is an exception.
     */
    public NameParser getNameParser(String name)
            throws NamingException {
	if ( namingManager == null )
	    throw new NamingException();
        return namingManager.getNameParser();
    }

    /**
     * Return the name parser for the specified name.
     * @return the NameParser instance.
     * @exception NamingException if there is an exception.
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
     * Add a property to the environment.
     */
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
        } 
        return myEnv.put(propName, propVal);
    }

    /**
     * Remove a property from the environment.
     */
    public Object removeFromEnvironment(String propName) 
	    throws NamingException 
    {
        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    /**
     * Get the context's environment.
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            myEnv = new Hashtable(3, 0.75f);
        }
        return myEnv;
    }

    /**
     * New JNDI 1.2 operation.
     */
    public void close() throws NamingException {
        myEnv = null;
    }

    /**
     * Return the name of this context within the namespace.  The name
     * can be passed as an argument to (new InitialContext()).lookup()
     * to reproduce this context.
     */
    public String getNameInNamespace() throws NamingException {
        return myName;
    }
}


