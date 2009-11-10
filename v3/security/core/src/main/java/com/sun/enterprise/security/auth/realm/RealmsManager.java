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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author kumar.jayanti
 */
@Service
@Scoped(Singleton.class)
public class RealmsManager {
    //per domain list of loaded Realms
    private final Hashtable<String, Realm> loadedRealms = new Hashtable();

    // Keep track of name of default realm for this domain. This is updated during startup
    // using value from server.xml
    private volatile String defaultRealmName="default";
    private final RealmsProbeProvider probeProvider = new RealmsProbeProvider();
    
    public RealmsManager() {
        
    }
     /**
     * Checks if the given realm name is loaded/valid.
     * @param String name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public  boolean isValidRealm(String name){
        if(name == null){
            return false;
        } else {
            return loadedRealms.containsKey(name);
        }
    }
    
    /**
     * Returns the names of accessible realms.
     * @return set of realm names
     */
    public  Enumeration	getRealmNames() {
	return loadedRealms.keys();
    }
    

    Realm _getInstance(String name) {
	Realm retval = null;
	retval = (Realm) loadedRealms.get (name);

        // Some tools as well as numerous other locations assume that
        // getInstance("default") always works; keep them from breaking
        // until code can be properly cleaned up. 4628429

        // Also note that for example the appcontainer will actually create
        // a Subject always containing realm='default' so this notion
        // needs to be fixed/handled.
        if ( (retval == null) && (Realm.RI_DEFAULT.equals(name)) ) {
            retval = (Realm) loadedRealms.get (getDefaultRealmName());
        }

        return retval;
    }
    
    void removeFromLoadedRealms(String realmName) {
        loadedRealms.remove(realmName);
        probeProvider.realmRemovedEvent(realmName);
        
    }

    void putIntoLoadedRealms(String realmName, Realm realm) {
        loadedRealms.put(realmName, realm);
        probeProvider.realmAddedEvent(realmName);
    }
    
    public Realm getFromLoadedRealms(String realmName) {
        return loadedRealms.get(realmName);
    }
    public synchronized String getDefaultRealmName() {
        return defaultRealmName;
    }

    public synchronized void setDefaultRealmName(String defaultRealmName) {
        this.defaultRealmName = defaultRealmName;
    }
    
   /**
    * Returns names of predefined AuthRealms' classes supported by security service.
    * @returns array of predefind AuthRealms' classes
    *
    */
   public  List<String> getPredefinedAuthRealmClassNames()
   {
       //!!!!!!!!!!!! (hardcoded for now until ss will implement backemnd support)
      /* return new String[]{
               "com.sun.enterprise.security.auth.realm.file.FileRealm",
               "com.sun.enterprise.security.auth.realm.certificate.CertificateRealm",
               "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm",
               "com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm",
               "com.sun.enterprise.security.auth.realm.solaris.SolarisRealm"};*/
       Habitat habitat = Globals.getDefaultHabitat();
       Collection<Inhabitant<? extends Realm>> collection = habitat.getInhabitants(Realm.class);
       List<String> arr = new ArrayList<String>();
       for (Inhabitant<? extends Realm> it : collection) {
           arr.add(it.typeName());
       }
      
       return arr;
       
   } 

}
