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
package com.sun.enterprise.management.offline;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import com.sun.appserv.management.util.misc.CollectionUtil;

import com.sun.enterprise.admin.config.MBeanConfigException;

import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchUserException;


/**
    <b>CAUTION: this code was COPIED from 
       com.sun.enterprise.admin.mbeans.AuthRealmMBean.  This
       class initially was a subclass of AuthRealmMBean, but
       it is impossible to instantiate the superclass, due to its
       hooks into MBeanRegistry, which throws an Exception when
       running "offline". </b>
    @see com.sun.enterprise.admin.mbeans.AuthRealmMBean
 */
final class AuthRealmMBeanX
{
    private final String mFile;
    
        public
    AuthRealmMBeanX( final String file )
    {
        mFile = file;
    }

        private FileRealm
    getRealmKeyFile()
        throws MBeanConfigException
    {
        try
        {
            return new FileRealm( mFile );
        }
        catch(Exception e)
        {
            throw new MBeanConfigException( e.getMessage() );
        }
    }

        private static String[]
    toStringArray(final Enumeration e)
    {
        final List<String>  list = new ArrayList<String>();
        
        while( e.hasMoreElements() )
        {
            list.add( "" + e.nextElement() );
         }
        return CollectionUtil.toStringArray( list );
    }
    
        public String[]
    getUserNames()
        throws MBeanConfigException
    {
        final FileRealm realm = getRealmKeyFile();
        try
        {
            return toStringArray(realm.getUserNames());
        }
        catch(BadRealmException bre)
        {
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns names of all the groups from the instance realm keyfile
     */
    public String[] getGroupNames() throws MBeanConfigException
    {
        final FileRealm realm = getRealmKeyFile();
        try
        {
            return toStringArray(realm.getGroupNames());
        }
        catch(BadRealmException bre)
        {
            throw new MBeanConfigException(bre.getMessage());
        }
    }

    /**
     * Returns the name of all the groups that this user belongs to from the instance realm keyfile
     */
    public String[] getUserGroupNames(String userName) throws MBeanConfigException
    {
        if( userName == null )
        {
            throw new IllegalArgumentException( "" + null );
        }

        final FileRealm realm = getRealmKeyFile();
        try
        {
            return toStringArray(realm.getGroupNames(userName));
        }
        catch(NoSuchUserException nse)
        {
            throw new MBeanConfigException(nse.getMessage());
        }
    }

    /**
     * Adds new user to file realm. User cannot exist already.
     */
        public void
    addUser(
        final String userName,
        final String password, 
        final String[] groupList)
        throws MBeanConfigException
    {
        final FileRealm realm = getRealmKeyFile();
        try
        {
            realm.addUser(userName, password, groupList);
            saveInstanceRealmKeyFile(realm);
        }
        catch(Exception e)
        {
            throw new MBeanConfigException( e.getMessage() );
        }
    }

    /**
     * Remove user from file realm. User must exist.
     */
    public void removeUser( final String userName)
        throws MBeanConfigException
    {
        final FileRealm realm = getRealmKeyFile();
        try
        {
            realm.removeUser(userName);
            saveInstanceRealmKeyFile(realm);
        }
        catch(NoSuchUserException nse)
        {
            throw new MBeanConfigException(nse.getMessage());
        }
    }

    /**
     * Update data for an existing user. User must exist.
     This is equivalent to calling removeUser() followed by addUser().
     */
    public void updateUser(
        final String userName,
        final String password,
        final String[] groupList)
        throws MBeanConfigException
    {
        final FileRealm realm = getRealmKeyFile();
        try
        {
            realm.updateUser(userName, userName, password, groupList);
            saveInstanceRealmKeyFile( realm );
        }
        catch( Exception e )
        {
            throw new MBeanConfigException( e .getMessage() );
        }
    }

    // ****************************************************************************
        private void
    saveInstanceRealmKeyFile(final FileRealm realm)
        throws MBeanConfigException
    {
        try
        {
            realm.writeKeyFile( mFile );
        }
        catch(IOException e)
        {
            throw new MBeanConfigException( e.getMessage() );
        }
 }


}

















