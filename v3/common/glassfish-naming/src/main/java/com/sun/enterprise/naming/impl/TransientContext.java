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

import javax.naming.*;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class to implement multiple level of subcontexts in SerialContext. To use this
 * class a new object of class InitialContext (env) should be instantiated.
 * The env i.e the Environment is initialised with SerialInitContextFactory
 * An example for using this is in /test/subcontext
 */
public class TransientContext implements Context, Serializable {
    static Logger _logger = LogFacade.getLogger();

    public static final boolean debug = false;
    Hashtable myEnv;
    private Hashtable bindings = new Hashtable();
    static NameParser myParser = new SerialNameParser();

    public TransientContext() {
    }

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public Context createSubcontext(String name) throws NamingException {
        return drillDownAndCreateSubcontext(name);
    }

    /**
     * Create a subcontext with the specified name.
     *
     * @return the created subcontext.
     * @throws NamingException if there is a naming exception.
     */
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    public void destroySubcontext(String name) throws NamingException {
        drillDownAndDestroySubcontext(name);
    }

    /**
     * Destroy the subcontext with the specified name.
     *
     * @throws NamingException if there is a naming exception.
     */
    public void destroySubcontext(Name name) throws NamingException {
        destroySubcontext(name.toString());
    }

    /**
     * Handles making nested subcontexts
     * i.e. if you want abcd/efg/hij. It will go  subcontext efg in abcd
     * (if not present already - it will create it) and then
     * make subcontext hij
     *
     * @return the created subcontext.
     * @throws NamingException if there is a Naming exception
     */
    public Context drillDownAndCreateSubcontext(String name)
            throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() <= 1) { // bottom
            if (bindings.containsKey(name)) {
                throw new NameAlreadyBoundException("Subcontext " +
                        name + "already present");
            }

            TransientContext ctx = null;
            ctx = new TransientContext();
            bindings.put(name, ctx);
            return ctx;
        } else {
            String suffix = n.getSuffix(1).toString();
            Context retCtx, ctx; // the created context
            try {
                ctx = resolveContext(n.get(0));
            } catch (NameNotFoundException e) {
                ctx = new TransientContext();
            }
            retCtx = ctx.createSubcontext(suffix);
            bindings.put(n.get(0), ctx);
            return retCtx;
        }
    }

    /**
     * Handles deleting nested subcontexts
     * i.e. if you want delete abcd/efg/hij. It will go  subcontext efg in abcd
     * it will delete it) and then delete subcontext hij
     *
     * @throws NamingException if there is a naming exception
     */
    public void drillDownAndDestroySubcontext(String name)
            throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() < 1) {
            throw new InvalidNameException("Cannot destoy empty subcontext");
        }
        if (n.size() == 1) { // bottom
            if (bindings.containsKey(name)) {
                bindings.remove(name);
            } else {
                throw new NameNotFoundException("Subcontext: " + name +
                        " not found");
            }
        } else {
            String suffix = n.getSuffix(1).toString();
            Context ctx; // the context to drill down from
            ctx = resolveContext(n.get(0));
            ctx.destroySubcontext(suffix);
        }
    }

    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException          if there is a naming exception.
     * @throws java.rmi.RemoteException if there is an RMI exception.
     */
    public Object lookup(String name) throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() < 1) {
            throw new InvalidNameException("Cannot bind empty name");
        }

        if (n.size() == 1) { // bottom
            return doLookup(n.toString());
        } else {
            String suffix = n.getSuffix(1).toString();
            TransientContext ctx = resolveContext(n.get(0));
            return ctx.lookup(suffix);
        }
    }

    /**
     * Lookup the specified name.
     *
     * @return the object or context bound to the name.
     * @throws NamingException          if there is a naming exception.
     * @throws java.rmi.RemoteException if there is an RMI exception.
     */
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    /**
     * Lookup the specified name in the current objects hashtable.
     *
     * @return the object or context bound to the name.
     * @throws NamingException          if there is a naming exception.
     * @throws java.rmi.RemoteException if there is an RMI exception.
     */
    private Object doLookup(String name) throws NamingException {
        Object answer = bindings.get(name);
        if (answer == null) {
            throw new NameNotFoundException(name + " not found");
        }
        return answer;
    }

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException          if there is a naming exception.
     * @throws java.rmi.RemoteException if there is an RMI exception.
     */
    public void bind(String name, Object obj) throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() < 1) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (n.size() == 1) { // bottom
            doBindOrRebind(n.toString(), obj, false);
        } else {
            String suffix = n.getSuffix(1).toString();
            Context ctx;
            try {
                ctx = resolveContext(n.get(0));
            } catch (NameNotFoundException e) {
                ctx = createSubcontext(n.get(0));
            }
            ctx.bind(suffix, obj);
        }
    }

    /**
     * Bind the object to the specified name.
     *
     * @throws NamingException          if there is a naming exception.
     * @throws java.rmi.RemoteException if there is an RMI exception.
     */
    public void bind(Name name, Object obj)
            throws NamingException {
        bind(name.toString(), obj);
    }

    /**
     * Finds out if the subcontext specified is present in the current context
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    /* Finds if the name searched for is a type of context or anyother type of 
     * object.
     */
    private TransientContext resolveContext(String s) throws NamingException {
        //TransientContext ctx = (TransientContext) bindings.get(s);
        TransientContext ctx;
        Object obj = bindings.get(s);
        if (obj == null) {
            throw new NameNotFoundException(s);
        }
        if (obj instanceof TransientContext) {
            ctx = (TransientContext) obj;
        } else {
            throw new NameAlreadyBoundException(s);
        }
        return ctx;
    }

    /**
     * Binds or rebinds the object specified by name
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    private void doBindOrRebind(String name, Object obj, boolean rebind)
            throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (!rebind) {
            if (bindings.get(name) != null) {
                throw new NameAlreadyBoundException(
                        "Use rebind to override");
            }
        }
        bindings.put(name, obj);
    }


    /**
     * Rebinds the object specified by name
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void rebind(String name, Object obj)
            throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() < 1) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (n.size() == 1) { // bottom
            doBindOrRebind(n.toString(), obj, true);
        } else {
            String suffix = n.getSuffix(1).toString();
            Context ctx = null;
            try {
                ctx = resolveContext(n.get(0));
                ctx.rebind(suffix, obj);
            } catch (NameNotFoundException e) {
                ctx = createSubcontext(n.get(0));
                ctx.rebind(suffix, obj);
            }
        }
    }

    /**
     * Binds or rebinds the object specified by name
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void rebind(Name name, Object obj)
            throws NamingException {
        rebind(name.toString(), obj);
    }

    /**
     * Unbinds the object specified by name. Traverses down the context tree
     * and unbinds the object if required.
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    private void doUnbind(String name) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        if (bindings.get(name) == null) {
            throw new NameNotFoundException(
                    "Cannot find name to unbind");
        }
        bindings.remove(name);
    }

    /**
     * Unbinds the object specified by name. Calls itself recursively to
     * traverse down the context tree and unbind the object.
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void unbind(String name) throws NamingException {
        Name n = new CompositeName(name);
        if (n.size() < 1) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        if (n.size() == 1) { // bottom
            doUnbind(n.toString());
        } else {
            String suffix = n.getSuffix(1).toString();
            TransientContext ctx = resolveContext(n.get(0));
            ctx.unbind(suffix);
        }
    }

    /**
     * Unbinds the object specified by name
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void unbind(Name name)
            throws NamingException {
        unbind(name.toString());
    }

    /**
     * Rename the object specified by oldname to newname
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void rename(Name oldname, Name newname) throws NamingException {
        rename(oldname.toString(), newname.toString());
    }

    /**
     * Rename the object specified by oldname to newname
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public void rename(String oldname, String newname)
            throws NamingException {
        if (oldname.equals("") || newname.equals("")) {
            throw new InvalidNameException("Cannot rename empty name");
        }

        // Check if new name exists
        if (bindings.get(newname) != null) {
            throw new NameAlreadyBoundException(newname +
                    " is already bound");
        }

        // Check if old name is bound
        Object oldBinding = bindings.remove(oldname);
        if (oldBinding == null) {
            throw new NameNotFoundException(oldname + " not bound");
        }

        bindings.put(newname, oldBinding);
    }

    /**
     * list the objects stored by the current context
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public Hashtable list() {
        return bindings;
    }

    /**
     * List the objects specified by name.
     *
     * @throws NamingException          if there is a naming exception
     * @throws java.rmi.RemoteException if there is a RMI exception
     */
    public Hashtable listContext(String name) throws NamingException {
        if (debug) {
            print(bindings);
        }
        if (name.equals(""))
            return bindings;

        Object target = lookup(name);
        if (target instanceof TransientContext) {
            return ((TransientContext) target).listContext("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * List the objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    /**
     * List the objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NamingEnumeration list(String name) throws NamingException {
        if (debug) {
            print(bindings);
        }
        if (name.equals(""))
            return new RepNames(bindings);

        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * List the bindings of objects present in name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NamingEnumeration listBindings(String name) throws NamingException {
        if (name.equals(""))
            return new RepBindings(bindings);

        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context) target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    /**
     * List the binding of objects specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NamingEnumeration listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    /**
     * Lookup the name.
     *
     * @throws NamingException if there is a naming exception
     */
    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    /**
     * Lookup name.
     *
     * @throws NamingException if there is a naming exception
     */
    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    /**
     * List the NameParser specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NameParser getNameParser(String name) throws NamingException {
        return myParser;
    }

    /**
     * List the NameParser specified by name.
     *
     * @throws NamingException if there is a naming exception
     */
    public NameParser getNameParser(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return getNameParser(name.toString());
    }

    /**
     * Compose a new name specified by name and prefix.
     *
     * @return null
     * @throws NamingException if there is a naming exception
     */
    public String composeName(String name, String prefix)
            throws NamingException {
        return null;
    }

    /**
     * Compose a new name specified by name and prefix.
     *
     * @return Name result of the concatenation
     * @throws NamingException if there is a naming exception
     */
    public Name composeName(Name name, Name prefix)
            throws NamingException {
        Name result = (Name) (prefix.clone());
        result.addAll(name);
        return result;
    }

    /**
     * Add the property name and value to the environment.
     *
     * @throws NamingException if there is a naming exception
     */
    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
        }
        return myEnv.put(propName, propVal);
    }

    /**
     * Remove property from the environment.
     *
     * @throws NamingException if there is a naming exception
     */
    public Object removeFromEnvironment(String propName)
            throws NamingException {
        if (myEnv == null) {
            return null;
        }
        return myEnv.remove(propName);
    }

    /**
     * List the current environment.
     *
     * @throws NamingException if there is a naming exception
     */
    public Hashtable getEnvironment() throws NamingException {
        if (myEnv == null) {
            // Must return non-null
            myEnv = new Hashtable(3, 0.75f);
        }
        return myEnv;
    }

    /**
     * Invalidate the current environment.
     */
    public void close() throws NamingException {
        myEnv = null;
    }

    /**
     * Operation not supported.
     */
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException("getNameInNamespace() " +
                "not implemented");
    }

    /**
     * Print the current hashtable.
     */
    private static void print(Hashtable ht) {
        for (Enumeration en = ht.keys(); en.hasMoreElements();) {
            Object key = en.nextElement();
            Object value = ht.get(key);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "[" + key + ":" +
                        key.getClass().getName() +
                        ", " + value + ":" + value.getClass().getName() + "]");
            }
            // END OF IASRI 4660742
        }
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
            throw new
                    OperationNotSupportedException("close() not implemented");
        }
    }
}










