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
package com.sun.enterprise.repository;

import java.util.*;
import java.io.*;
import javax.naming.*;
import java.security.*;
import com.sun.enterprise.util.FileUtil;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

/**
 * Repository Context
 * @author Harish Prabandham
 */
class RepositoryContext implements Context {
    /** This environment property specifies the path of the file 
	 *  that stores all the contents of the repository. 
	 */
// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END
    public static final String 
	REPOSITORY_NAME = "com.sun.enterprise.repository.name";
    public static final String 
	REPOSITORY_DIR = "com.sun.enterprise.repository.dir";

    Hashtable myEnv;
    private Properties bindings;
    private static final boolean debug = false;
    static NameParser myParser = new RepositoryNameParser();
    
    RepositoryContext(Hashtable environment) {
        myEnv = (environment != null)
	    ? (Hashtable)(environment.clone()) 
	    : null;
        resurrectTable();
    }

    // New API for JNDI 1.2
    public String getNameInNamespace() throws NamingException
    {
	throw new OperationNotSupportedException("Context.getNameInNamespace() not implemented");
    }

    public Object lookup(String name) throws NamingException {
    if (name.equals("")) {
            // Asking to look up this context itself.  Create and return
            // a new instance with its own independent environment.
            return (new RepositoryContext(myEnv));
        }
		// System.out.println("BINDINGS :" + bindings);
// START OF IASRI 4660742
		//_logger.log(Level.FINE,"BINDINGS :" + bindings);
// END OF IASRI 4660742
        Object answer = bindings.get(name);
        if (answer == null) {
            throw new NameNotFoundException(name + " not found");
        }
        return answer;
    }

    public Object lookup(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookup(name.toString()); 
    }

    public void bind(String name, Object obj) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        if (bindings.get(name) != null) {
            throw new NameAlreadyBoundException(
                    "Use rebind to override");
        }
        bindings.put(name, obj);
		store();
    }

    public void bind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        bind(name.toString(), obj);
    }

    public void rebind(String name, Object obj) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot bind empty name");
        }
        bindings.put(name, obj);
		store();
    }

    public void rebind(Name name, Object obj) throws NamingException {
        // Flat namespace; no federation; just call string version
        rebind(name.toString(), obj);
    }

    public void unbind(String name) throws NamingException {
        if (name.equals("")) {
            throw new InvalidNameException("Cannot unbind empty name");
        }
        bindings.remove(name);
		store();
    }

    public void unbind(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        unbind(name.toString());
    }

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
		store();
    }

    public void rename(Name oldname, Name newname)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        rename(oldname.toString(), newname.toString());
    }

    public NamingEnumeration list(String name)
            throws NamingException {
        if (name.equals("")) {
            // listing this context
            return new RepNames(bindings.keys());
        } 

        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context)target).list("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    public NamingEnumeration list(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return list(name.toString());
    }

    public NamingEnumeration listBindings(String name)
            throws NamingException {
        if (name.equals("")) {
            // listing this context
            return new RepBindings(bindings.keys());
        } 

        // Perhaps 'name' names a context
        Object target = lookup(name);
        if (target instanceof Context) {
            return ((Context)target).listBindings("");
        }
        throw new NotContextException(name + " cannot be listed");
    }

    public NamingEnumeration listBindings(Name name)
            throws NamingException {
        // Flat namespace; no federation; just call string version
        return listBindings(name.toString());
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException(
                "RepositoryContext does not support subcontexts");
    }

    public void destroySubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        destroySubcontext(name.toString());
    }

    public Context createSubcontext(String name)
            throws NamingException {
        throw new OperationNotSupportedException(
                "RepositoryContext does not support subcontexts");
    }

    public Context createSubcontext(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return createSubcontext(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        // This flat context does not treat links specially
        return lookup(name);
    }

    public Object lookupLink(Name name) throws NamingException {
        // Flat namespace; no federation; just call string version
        return lookupLink(name.toString());
    }

    public NameParser getNameParser(String name)
            throws NamingException {
        return myParser;
    }

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

    public Object addToEnvironment(String propName, Object propVal)
            throws NamingException {
        if (myEnv == null) {
            myEnv = new Hashtable(5, 0.75f);
	} 
	return myEnv.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) 
            throws NamingException {
        if (myEnv == null)
            return null;

	return myEnv.remove(propName);
    }

    public Hashtable getEnvironment() throws NamingException {
	if (myEnv == null) {
	    // Must return non-null
	    myEnv = new Hashtable(3, 0.75f);
	}
        return myEnv;
    }

    public void close() throws NamingException {
    store();
 	myEnv = null;
	bindings = null;
    }

	private void store()
	{
        try {
        if (bindings != null) {
            FileOutputStream fos = new FileOutputStream(getStoragePath());
            bindings.store(fos, "Repository resource mapping");
            fos.close();
        }
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace();
// START OF IASRI 4660742
		_logger.log(Level.SEVERE,"enterprise.store_exception" ,e);
// END OF IASRI 4660742
        }
	}

    static String getFilePath(final String dirName, final String filename) {

        return (String)
        AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            String path = filename + FILE_EXT;
	
            if(path != null) {
                // Check if this file exists...
                String fname = FileUtil.getAbsolutePath(dirName + path);
                File f = new File(fname);
                
                if(!f.exists())
                    path = DEFAULT_NAME + FILE_EXT;
            }
            else
                path = DEFAULT_NAME + FILE_EXT;
            
            return FileUtil.getAbsolutePath(dirName + path);
        }});
    }
      
    private String getStoragePath() {
	String dirName = (String) myEnv.get(REPOSITORY_DIR);
	if (dirName == null) {
	    dirName = DEFAULT_NAMETABLE_DIR;
	}

	// System.out.println("REPOSITORY: " + dirName);
// START OF IASRI 4660742
		//_logger.log(Level.FINE,"REPOSITORY: " + dirName);
// END OF IASRI 4660742
        return getFilePath(dirName, (String) myEnv.get(REPOSITORY_NAME));
    }

    public static String getRepositoryName(String fname) {
	String filepath = getFilePath(DEFAULT_NAMETABLE_DIR, fname);
	int fileIndex = filepath.lastIndexOf(File.separator); 
	String filename = null;
	
	if(fileIndex > 0)
	    filename = filepath.substring(fileIndex + 1);	
	else
	    filename = filepath;
	
	int extIndex = filename.lastIndexOf(FILE_EXT);
	String fwithoutext;

	if(extIndex > 0)
	    fwithoutext = filename.substring(0, extIndex);
	else
	    fwithoutext = filename;
	
	return fwithoutext;
    }

    private void resurrectTable() {
        AccessController.doPrivileged(new PrivilegedAction() {
        public Object run() {
            bindings = new Properties();
            try {
                File f = new File(getStoragePath());
                if(debug)
// IASRI 4660742                    System.out.println("Loaded File: " + f.getAbsolutePath()); 
// START OF IASRI 4660742
		_logger.log(Level.FINE,"Loaded File: " + f.getAbsolutePath());
// END OF IASRI 4660742
                if(f.exists())
                    bindings.load(new FileInputStream(f));
            } catch (Exception e) {
// IASRI 4660742                e.printStackTrace();
// START OF IASRI 4660742
		_logger.log(Level.SEVERE,"enterprise.load_exception" ,e);
// END OF IASRI 4660742
            }
            return null;
        }});
    }

    private static void print(Hashtable ht) {
        for (Enumeration en = ht.keys(); en.hasMoreElements(); ) {
            Object key = en.nextElement();
            Object value = ht.get(key);
	    if (debug)
// IASRI 4660742	      System.out.println("[" + key + ":" + key.getClass().getName() + 
// IASRI 4660742                               ", " + value + ":" + value.getClass().getName()
// IASRI 4660742                               + "]");
// START OF IASRI 4660742
		{
			if(_logger.isLoggable(Level.FINE))
				_logger.log(Level.FINE,"[" + key + ":" + key.getClass().getName() +", " + value + ":" + value.getClass().getName()+ "]");
		}
// END OF IASRI 4660742
        }
    }
    
    // Class for enumerating name/class pairs
    class RepNames implements NamingEnumeration {
        Enumeration names;

        RepNames (Enumeration names) {
            this.names = names;
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
	    throw new OperationNotSupportedException("NamingEnumeration.close() not implemented");
	}
    }

    // Class for enumerating bindings
    class RepBindings implements NamingEnumeration {
        Enumeration names;

        RepBindings (Enumeration names) {
            this.names = names;
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

	public void close() throws NamingException {
	    throw new OperationNotSupportedException("NamingEnumeration.close() not implemented");
	}
    }
    
    private static final String 
		DEFAULT_NAMETABLE_DIR = "config/";
    private static final String DEFAULT_NAME = "default";
    private static final String FILE_EXT = ".properties";
};


