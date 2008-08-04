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
package org.glassfish.admin.amx.mbean;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import javax.management.ObjectName;
import javax.management.MBeanException;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.ext.realm.RealmsMgr;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.AuthRealmConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.PropertyConfig;

import org.glassfish.internal.api.Globals;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.RealmConfig;
import com.sun.enterprise.security.auth.realm.User;


import com.sun.enterprise.security.auth.realm.file.FileRealm;


/**
    AMX RealmsMgr implementation.
 */
public final class RealmsMgrImpl extends AMXNonConfigImplBase implements RealmsMgr
{
    private final RealmsManager mRealmsManager;
    
		public
	RealmsMgrImpl( final ObjectName containerObjectName )
	{
        super( RealmsMgr.J2EE_TYPE, RealmsMgr.J2EE_TYPE, containerObjectName, RealmsMgr.class, null);
        
        mRealmsManager = Globals.getDefaultHabitat().getComponent(RealmsManager.class);
	}
    
        private void 
    loadReams()
    {
        // this is ugly, the underlying API doesn't understand that there is more than one <security-service>,
        // each with one or more <auth-realm>
        final ConfigConfig config = getDomainRoot().getDomainConfig().getConfigsConfig().getConfigConfigMap().values().iterator().next();
        final SecurityServiceConfig ss = config.getSecurityServiceConfig();
        
        final Map<String,AuthRealmConfig> authRealmConfigs = ss.getAuthRealmConfigMap();
        
        final List<String> goodRealms = new ArrayList<String>();
        for( final AuthRealmConfig authRealm : authRealmConfigs.values() )
        {
            final Map<String,PropertyConfig> propConfigs = authRealm.getPropertyConfigMap();
            final Properties props = new Properties();
            for (final PropertyConfig p : propConfigs.values() )
            {
                final String value = p.resolveAttribute( "Value" );
                props.setProperty( p.getName(), value );
            }
            try
            {
                Realm.instantiate( authRealm.getName(), authRealm.getClassname(), props );
                goodRealms.add( authRealm.getName() );
            }
            catch( final Exception e )
            {
                e.printStackTrace();
            }
        }
        
        if ( goodRealms.size() != 0 )
        {
            final String goodRealm = goodRealms.iterator().next();
            try
            {
                final String defaultRealm = ss.getDefaultRealm();
                final Realm r = Realm.getInstance(defaultRealm);
                Realm.setDefaultRealm(defaultRealm);
            }
            catch (Exception e)
            {
                Realm.setDefaultRealm(goodRealms.iterator().next());
            }
        }
  }
    
    private static String[] toArray( final List<String> l )
    {
        return (String[])l.toArray( new String[l.size()] );
    }
    
    
    public String[]
    getRealmNames()
    {
        loadReams();
        
        final List<String> items = ListUtil.newList( mRealmsManager.getRealmNames() );
        return toArray(items);
    }
    
    public String[]
    getPredefinedAuthRealmClassNames()
    {
        final List<String> items = mRealmsManager.getPredefinedAuthRealmClassNames();
        return toArray(items);
    }
    
    
    public String getDefaultRealmName()
    {
        return mRealmsManager.getDefaultRealmName();
    }
    
    
    public void setDefaultRealmName(final String realmName)
    {
        mRealmsManager.setDefaultRealmName(realmName);
    }
        
        private Realm
    getRealm(final String realmName)
    {
        final Realm realm = mRealmsManager.getFromLoadedRealms(realmName);
        if ( realm == null )
        {
            throw new IllegalArgumentException( "No such realm: " + realmName );
        }
        return realm;
    }
    private void persist( final Realm realm )
    {
       // realm.persist();
    }
    
    public void addUser(
        final String realmName,
        final String user,
        final String password,
        final String[] groupList )
    {
        checkSupportsUserManagement(realmName);
        
        try
        {
            final Realm realm = getRealm(realmName);
            realm.addUser(user, password, groupList);
            persist(realm);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public void updateUser(
        final String realmName,
        final String existingUser,
        final String newUser,
        final String password,
        final String[] groupList )
    {
        checkSupportsUserManagement(realmName);
        
        try
        {
            final Realm realm = getRealm(realmName);
            realm.updateUser(existingUser, newUser, password, groupList);
            persist(realm);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public void removeUser(final String realmName, final String user)
    {
        checkSupportsUserManagement(realmName);
        
        try
        {
            final Realm realm = getRealm(realmName);
            realm.removeUser(user);
            persist(realm);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public boolean supportsUserManagement(final String realmName)
    {
        return getRealm(realmName).supportsUserManagement();
    }
    
        private void
    checkSupportsUserManagement(final String realmName)
    {
        if ( ! supportsUserManagement(realmName) )
        {
            throw new IllegalStateException( "Realm " + realmName + " does not support user management" );
        }
    }

    
    
    public String[] getUserNames(final String realmName)
    {
        try
        {
            final List<String> names = ListUtil.newList( getRealm(realmName).getUserNames() );
            return toArray( names );
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public String[] getGroupNames(final String realmName)
    {
        try
        {
            final List<String> names = ListUtil.newList( getRealm(realmName).getGroupNames() );
            return toArray(names);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public String[] getGroupNames(final String realmName, final String user)
    {
        try
        {
            return toArray( ListUtil.newList( getRealm(realmName).getGroupNames(user) ) );
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    
    public Map<String,Object> getUserAttributes(final String realmName, final String username)
    {
        try
        {
            final User user = getRealm(realmName).getUser(username);
            final Map<String,Object> m = new HashMap<String,Object>();
            final List<String> attrNames = ListUtil.newList(user.getAttributeNames());
            for( final String attrName : attrNames ) 
            {
                m.put( attrName, user.getAttribute(attrName) );
            }
            return m;
        }
        catch( final Exception e )
        {
        throw new RuntimeException(e);
        }
    }
    
    private static void debug( final String s ) { System.out.println("##### " + s); }
    
    private static final String ADMIN_REALM = "admin-realm";
    private static final String ANONYMOUS_USER = "anonymous";
    private static final String FILE_REALM_CLASSNAME = "com.sun.enterprise.security.auth.realm.file.FileRealm";
            
    public boolean getAnonymousLogin() {
        final DomainRoot domainRoot = getDomainRoot();
        
        final Map<String,ConfigConfig> configs = domainRoot.getDomainConfig().getConfigsConfig().getConfigConfigMap();

        // find the ADMIN_REALM
        AuthRealmConfig adminFileAuthRealm = null;
        for( final ConfigConfig config : configs.values() )
        {
            for( final AuthRealmConfig auth : config.getSecurityServiceConfig().getAuthRealmConfigMap().values() )
            {
                if ( auth.getName().equals(ADMIN_REALM) )
                {
                    adminFileAuthRealm = auth;
                    break;
                }
            } 
        }
        if (adminFileAuthRealm == null) {
            // There must always be an admin realm
            throw new IllegalStateException( "Cannot find admin realm" );
        }

        // Get FileRealm class name
        final String fileRealmClassName = adminFileAuthRealm.getClassname();
        if (fileRealmClassName != null && ! fileRealmClassName.equals(FILE_REALM_CLASSNAME)) {
            // This condition can arise if admin-realm is not a File realm. Then the API to extract
            // the anonymous user should be integrated for the logic below this line of code. for now,
            // we treat this as an error and instead of throwing exception return false;
            return false;
        }

        final Map<String,PropertyConfig>  props = adminFileAuthRealm.getPropertyConfigMap();
        final PropertyConfig keyfileProp = props.get("file");
        if ( keyfileProp == null ) {
            throw new IllegalStateException( "Cannot find property 'file'" );
        }
        //System.out.println( "############### keyFileProp: " + keyfileProp.getName() + " = " + keyfileProp.getValue() );
        final String keyFile = keyfileProp.resolveAttribute( "Value" );
        //System.out.println( "############### keyFile: " + keyfileProp.getValue() + " ===> " + keyFile);
        if (keyFile == null) {
            throw new IllegalStateException( "Cannot find key file" );
        }
        
        //System.out.println( "############### keyFile: " + keyFile);

        /* doesn't work! 
        final String[] usernames = getUserNames(adminFileAuthRealm.getName());
        return usernames.length == 1 && usernames[0].equals(ANONYMOUS_USER);
        */
        FileRealm fr = null;
        try {
            fr = new FileRealm(keyFile);
        } catch( final Exception e) {
            throw new RuntimeException(e);
        }

        // Check if the realm has only one user named annonymous.
        // Head off to the landing page if so; else to the regular
        // login page.
        try {
            final List<String>  usernames = ListUtil.newList( fr.getUserNames() );
            return usernames.size() == 1  && usernames.get(0).equals(ANONYMOUS_USER);
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
}























