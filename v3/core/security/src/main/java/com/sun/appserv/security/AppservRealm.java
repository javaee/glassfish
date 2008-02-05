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

package com.sun.appserv.security;

import java.util.*;

import com.sun.enterprise.security.auth.realm.*;

import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Parent class for iAS Realm classes.
 *
 * <P>This class provides default implementation for most of the abstract
 * methods in com.sun.enterprise.security.auth.realm.Realm. Since most
 * of these abstract methods are not supported by Realms there is
 * no need for the subclasses to implement them. The default implementations
 * provided here generally throw an exception if invoked.
 *
 *  @author Harpreet Singh
 */
public abstract class AppservRealm extends Realm
{
    public static final String JAAS_CONTEXT_PARAM="jaas-context";
    
    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.SECURITY_LOGGER);
    protected static final StringManager sm =
        StringManager.getManager("com.sun.enterprise.security.auth.realm", Thread.currentThread().getContextClassLoader());

    
    /**
     * Returns an AuthenticationHandler object which can be used to 
     * authenticate within this realm.
     *
     * <P>This method return null always, since AuthenticationHandlers
     * are generally not supported by iAS realms. Subclass can override
     * if necessary.
     *
     * @return An AuthenticationHandler object for this realm (always null)
     *
     */
    public AuthenticationHandler getAuthenticationHandler()
    {
        _logger.warning("iasrealm.noauth");
        return null;
    }


    /**
     * Returns names of all the users in this particular realm.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @return enumeration of user names (strings)
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public Enumeration getUserNames() throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Returns the information recorded about a particular named user.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @param name name of the user whose information is desired
     * @return the user object
     * @exception com.sun.enterprise.security.auth.realm.NoSuchUserException if the user doesn't exist
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public User getUser(String name)
        throws NoSuchUserException, BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Returns names of all the groups in this particular realm.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @return enumeration of group names (strings)
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public Enumeration getGroupNames()
        throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }


    /**
     * Refreshes the realm data so that new users/groups are visible.
     *
     * <P>This method always throws a BadRealmException since by default
     * this operation is not supported. Subclasses which support this
     * method can override.
     *
     * @exception com.sun.enterprise.security.auth.realm.BadRealmException if realm data structures are bad
     *
     */
    public void refresh() throws BadRealmException
    {
        String msg = sm.getString("iasrealm.notsupported");
        throw new BadRealmException(msg);
    }
}
