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
package com.sun.enterprise.security.auth.realm;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.security.Principal;
import java.security.acl.Group;
import com.sun.logging.*;
import com.sun.enterprise.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.security.auth.realm.IASRealm;


/**
 * javadoc
 *
 * @see java.security.Principal
 *
 * @author Harish Prabandham
 * @author Harpreet Singh
 * @author Jyri Virkki
 * @author Shing Wai Chan
 *
 */
public abstract class Realm implements Comparable {

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(Realm.class);

    private static Hashtable loadedRealms = new Hashtable();
    private String myName;

    // Keep track of name of default realm. This is updated during startup
    // using value from server.xml
    private static String defaultRealmName="default";
    
    // Keep a mapping from "default" to default realm (if no such named
    // realm is present) for the sake of all the hardcoded accesses to it.
    // This needs to be removed as part of RI security service cleanup.
    private final static String RI_DEFAULT="default";

    // All realms have a set of properties from config file, consolidate.
    private Properties ctxProps;

    // for assign-groups
    private static final String PARAM_GROUPS = "assign-groups";
    private static final String GROUPS_SEP = ",";
    private List<String> assignGroups = null;

    
    /**
     * Returns the name of this realm.
     *
     * @return realm name.
     */
    public final String	getName() { 
	return myName; 
    }

    
    /**
     * Assigns the name of this realm, and stores it in the cache
     * of realms.  Used when initializing a newly created in-memory
     * realm object; if the realm already has a name, there is no
     * effect.
     *
     * @param name name to be assigned to this realm.
     */
    protected final void setName(String name) {
	if (myName != null) {
	    return;
	}
	myName = name;
    }

    
    /**
     * Returns the name of this realm.
     *
     * @return name of realm.
     */
    public String  toString() { 
	return myName; 
    }

    
    /**
     * Compares a realm to another.  The comparison first considers the
     * authentication type, so that realms supporting the same kind of
     * user authentication are grouped together.  Then it compares realm
     * realm names.  Realms compare "before" other kinds of objects (i.e.
     * there's only a partial order defined, in the case that those other
     * objects compare themselves "before" a realm object).
     */
    public int compareTo (Object realm) {
	if (!(realm instanceof Realm)) {
	    return 1;
	}
	
	Realm 	r = (Realm) realm;
	String	str = r.getAuthType ();
	int	temp;
	
	if ((temp = getAuthType ().compareTo (str)) != 0) {
	    return temp;
	}

	str = r.getName ();
	return getName ().compareTo (str);
    }


    /**
     * Instantiate a Realm with the given name and properties using the
     * Class name given. This method is used by iAS and not RI.
     *
     * @param name Name of the new realm.
     * @param className Java Class name of the realm to create.
     * @param props Properties containing values of the Property element
     *     from server.xml
     * @returns Reference to the new Realm. The Realm class keeps an internal
     *     list of all instantiated realms.
     * @throws BadRealmException If the requested realm cannot be instantiated.
     *
     */
    public static Realm instantiate(String name, String className,
                                    Properties props)
        throws BadRealmException
    {
        return doInstantiate(name, className, props);
    }


    /**
     * Instantiate a Realm with the given name, loading properties from
     * the given file. This method is only used by RI and is not called
     * anywhere in iAS.
     *
     * @param realmName Name of the new realm.
     * @param f File containing Properties for the new realm.
     */
    public static Realm instantiate(String realmName, File f)
    throws NoSuchRealmException, BadRealmException, FileNotFoundException
    {
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException ();
        }
        
        if(_getInstance(realmName) != null) {
            throw new BadRealmException(
                localStrings.getLocalString("realm.already_exists", 
                                            "This Realm already exists."));
        } 
        
        //
        // First load the description from properties.
        //
        InputStream in = null;
        Properties  props = new Properties();
        
        try{    
            in = new FileInputStream(f);
            props.load(in);
            //
            // Then instantiate and initialize, using the single mandatory
            // property ("classname").
            //
            String classname = props.getProperty("classname");
            assert (classname != null);

            return doInstantiate(realmName, classname, props);
        } catch (IOException e) {
            throw new BadRealmException(e.toString());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch(Exception ex) {
                }
            }
        }
    }

    
    /**
     * Instantiates a Realm class of the given type and invokes its init()
     *
     */
    private static Realm doInstantiate(String name, String className,
                                       Properties props)
        throws BadRealmException
    {
        try {            
            Class realmClass = Class.forName(className);
            Object obj = realmClass.newInstance();
            Realm r = (Realm) obj;
            r.setName(name);
            r.init(props);

            loadedRealms.put(name, r);            
            return r;

        } catch(Exception e) {
            throw new BadRealmException(e);
        }
    }


    /**
     * Replace a Realm instance. Can be used by a Realm subclass to
     * replace a previously initialized instance of itself. Future
     * getInstance requests will then obtain the new instance.
     *
     * <P>Minimal error checking is done. The realm being replaced must
     * already exist (instantiate() was previously called), the new
     * instance must be fully initialized properly and it must of course
     * be of the same class as the previous instance.
     *
     * @param realm The new realm instance.
     * @param name The (previously instantiated) name for this realm.
     *
     */
    protected static void updateInstance(Realm realm, String name)
    {
        Realm oldRealm = (Realm)loadedRealms.get(name);
        if (!oldRealm.getClass().equals(realm.getClass())) {
            // would never happen unless bug in realm subclass
            throw new Error("Incompatible class "+realm.getClass()+
                            " in replacement realm "+name);
        }
        realm.setName(oldRealm.getName());
        loadedRealms.put(name, realm);
    }

    
    /**
     * Convenience method which returns the Realm object representing
     * the current default realm. Equivalent to
     * getInstance(getDefaultRealm()).
     * 
     * @return Realm representing default realm.
     * @exception NoSuchRealmException if default realm does not exist
     */
    public static Realm getDefaultInstance() throws NoSuchRealmException
    {
        return getInstance(defaultRealmName);
    }

    
    /**
     * Returns the name of the default realm.
     *
     * @return Default realm name.
     *
     */
    public static String getDefaultRealm() {
        return defaultRealmName;
    }

    
    /**
     * Sets the name of the default realm.
     *
     * @param realmName Name of realm to set as default.
     *
     */
    public static void setDefaultRealm(String realmName) {
        defaultRealmName = realmName;
    }

    /**
     * Remove realm with given name from cache.
     * @param realmName
     * @exception NoSuchRealmException
     */
    static void unloadInstance(String realmName) throws NoSuchRealmException {
        //make sure instance exist
        getInstance(realmName);
        loadedRealms.remove(realmName);
    }


    /**
     * Set a realm property.
     *
     * @param name property name.
     * @param value property value.
     *
     */
    public void setProperty(String name, String value)
    {
        ctxProps.setProperty(name, value);
    }


    /**
     * Get a realm property.
     *
     * @param name property name.
     * @returns value.
     *
     */
    public String getProperty(String name)
    {
        return ctxProps.getProperty(name);
    }

    /**
     * Return properties of the realm.
     */
    protected Properties getProperties() {
        return ctxProps;
    }


    /**
     * Returns name of JAAS context used by this realm.
     *
     * <P>The JAAS context is defined in server.xml auth-realm element
     * associated with this realm. 
     *
     * @return String containing JAAS context name.
     *
     */
    public String getJAASContext()
    {
        return ctxProps.getProperty(IASRealm.JAAS_CONTEXT_PARAM);
    }
    
    
    /**
     * Returns the realm identified by the name which is passed
     * as a parameter.  This function knows about all the realms
     * which exist; it is not possible to store (or create) one
     * which is not accessible through this routine.
     *
     * @param name identifies the realm
     * @return the requested realm
     * @exception NoSuchRealmException if the realm is invalid
     * @exception BadRealmException if realm data structures are bad
     */
    public static Realm	getInstance(String name) throws NoSuchRealmException
    {
	Realm retval = _getInstance(name);

        if (retval == null) {
            throw new NoSuchRealmException(
                localStrings.getLocalString("realm.no_such_realm", 
                name + " realm does not exist.",
                new Object[] { name }));
        }
     
	return retval;
    }

    /**
     * This is a private method for getting realm instance.
     * If realm does not exist, then it will not return null rather than 
     * throw exception.
     * @param name identifies the realm
     * @return the requested realm
     */
    private static Realm _getInstance(String name) {
	Realm retval = null;
	retval = (Realm) loadedRealms.get (name);

        // Some tools as well as numerous other locations assume that
        // getInstance("default") always works; keep them from breaking
        // until code can be properly cleaned up. 4628429

        // Also note that for example the appcontainer will actually create
        // a Subject always containing realm='default' so this notion
        // needs to be fixed/handled.
        if ( (retval == null) && (RI_DEFAULT.equals(name)) ) {
            retval = (Realm) loadedRealms.get (defaultRealmName);
        }

        return retval;
    }


    /**
     * Returns the names of accessible realms.
     * @return set of realm names
     */
    public static Enumeration	getRealmNames() {
	return loadedRealms.keys();
    }


    /**
     * The default constructor creates a realm which will later
     * be initialized, either from properties or by deserializing.
     */
    protected Realm() {
        ctxProps = new Properties();
    }


    /**
     * Initialize a realm with some properties.  This can be used
     * when instantiating realms from their descriptions.  This
     * method may only be called a single time.  
     *
     * @param props initialization parameters used by this realm.
     * @exception BadRealmException if the configuration parameters
     *	identify a corrupt realm
     * @exception NoSuchRealmException if the configuration parameters
     *	specify a realm which doesn't exist
     */
    protected void init(Properties props)
            throws BadRealmException, NoSuchRealmException {
        String groupList = props.getProperty(PARAM_GROUPS);
        if (groupList != null && groupList.length() > 0) {
            this.setProperty(PARAM_GROUPS, groupList);
            assignGroups = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(groupList, GROUPS_SEP);
            while (st.hasMoreTokens()) {
                String grp = (String)st.nextToken();
                if (!assignGroups.contains(grp)) {
                    assignGroups.add(grp);
                }
            }
        }
    }

    /**
     * Checks if the given realm name is loaded/valid.
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public static boolean isValidRealm(String name){
        if(name == null){
            return false;
        } else {
            return loadedRealms.containsKey(name);
        }
    }

    /**
     * Add assign groups to given Vector of groups.
     * To be used by getGroupNames.
     * @param grps
     */
    protected String[] addAssignGroups(String[] grps) {
        String[] resultGroups = grps;
        if (assignGroups != null && assignGroups.size() > 0) {
            List<String> groupList = new ArrayList<String>();
            if (grps != null &&  grps.length > 0) {
                for (String grp : grps) {
                    groupList.add(grp);
                }
            }
            
            for (String agrp : assignGroups) {
                if (!groupList.contains(agrp)) {
                    groupList.add(agrp);
                }
            }
            resultGroups = groupList.toArray(new String[groupList.size()]);
        }
        return resultGroups;
    }
    
    //---[ Abstract methods ]------------------------------------------------

    
    /**
     * Returns a short (preferably less than fifteen characters) description
     * of the kind of authentication which is supported by this realm.
     *
     * @return description of the kind of authentication that is directly
     *	supported by this realm.
     */
    public abstract String getAuthType ();

    /**
     * Returns an AuthenticationHandler object which can be used to 
     * authenticate within this realm.
     *
     * @return An AuthenticationHandler object for this realm.
     */
    public abstract AuthenticationHandler getAuthenticationHandler ();

    /**
     * Returns names of all the users in this particular realm.
     *
     * @return enumeration of user names (strings)
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract Enumeration	getUserNames() throws BadRealmException;

    /**
     * Returns the information recorded about a particular named user.
     *
     * @param name name of the user whose information is desired
     * @return the user object
     * @exception NoSuchUserException if the user doesn't exist
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract User getUser(String name)
	throws NoSuchUserException, BadRealmException;

    /**
     * Returns names of all the groups in this particular realm.
     *
     * @return enumeration of group names (strings)
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract Enumeration	getGroupNames()
	throws BadRealmException;

    /**
     * Returns the name of all the groups that this user belongs to
     * @param username name of the user in this realm whose group listing
     * is needed.
     * @return enumeration of group names (strings)
     * @exception InvalidOperationException thrown if the realm does not
     * support this operation - e.g. Certificate realm does not support this
     * operation
     */
    public abstract Enumeration getGroupNames (String username)
	throws InvalidOperationException, NoSuchUserException;
    
    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * @exception BadRealmException if realm data structures are bad
     */
    public abstract void  refresh() throws BadRealmException;
    
}





