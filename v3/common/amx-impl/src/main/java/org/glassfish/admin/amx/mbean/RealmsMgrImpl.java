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
import javax.management.ObjectName;
import javax.management.MBeanException;

import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.ext.realm.RealmsMgr;

import org.glassfish.internal.api.Globals;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.User;




/**
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
    

    private static List<String>
    toList( final Enumeration<String> e )
    {
        final List<String> items = new ArrayList<String>();
        while ( e.hasMoreElements() )
        {
            items.add( e.nextElement() );
        }
        return items;
    }
    
    private static String[] toArray( final List<String> l )
    {
        return (String[])l.toArray( new String[l.size()] );
    }
    
    
    public String[]
    getRealmNames()
    {
        final List<String> items = toList( mRealmsManager.getRealmNames() );
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
    
    public void addUser(
        final String realmName,
        final String user,
        final String password,
        final String[] groupList )
    {
        try
        {
            final Realm realm = getRealm(realmName);
            realm.addUser(user, password, groupList);
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
        try
        {
            final Realm realm = getRealm(realmName);
            realm.updateUser(existingUser, newUser, password, groupList);
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    
    public String[] getUserNames(final String realmName)
    {
        try
        {
            final List<String> names = toList( getRealm(realmName).getUserNames() );
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
            final List<String> names = toList( getRealm(realmName).getGroupNames() );
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
            return toArray( toList( getRealm(realmName).getGroupNames(user) ) );
        }
        catch( final Exception e )
        {
            throw new RuntimeException(e);
        }
    }
    
    public void removeUser(final String realmName, final String user)
    {
        try
        {
            getRealm(realmName).removeUser(user);
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
    
    public Map<String,Object> getUserAttributes(final String realmName, final String username)
    {
        try
        {
            final User user = getRealm(realmName).getUser(username);
            final Map<String,Object> m = new HashMap<String,Object>();
            final List<String> attrNames = toList(user.getAttributeNames());
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
}


















