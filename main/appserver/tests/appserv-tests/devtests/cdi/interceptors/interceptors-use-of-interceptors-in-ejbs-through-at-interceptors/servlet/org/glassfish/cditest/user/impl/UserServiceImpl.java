/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.cditest.user.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.glassfish.cditest.security.api.Secure;
import org.glassfish.cditest.user.api.UserService;
import org.glassfish.cditest.user.api.model.Gender;
import org.glassfish.cditest.user.api.model.User;
import org.glassfish.cditest.user.model.UserImpl;
/**
 * <p>
 * Implementation of the UserService.
 * </p>
 * 
 * @author chaoslayer
 */
@Stateless
@Local
@javax.interceptor.Interceptors(org.glassfish.cditest.security.interceptor.SecurityInterceptor.class)
public class UserServiceImpl implements UserService {
    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    /**
     * <p>
     * Add a new user
     * </p>
     * 
     * <p>
     * The implementation must ensure that the provided user object is persisted
     * before returning the assigned persistent user ID.
     * </p>
     * 
     * @param user
     *            The user object to persist
     * @return The newly created persistent ID of the user object
     * 
     * @see UserImpl
     */
    @Override
    public Long addUser(final User user) throws EJBException
    {
        LOG.log(Level.INFO, "Storing user {0}", user);

        return new Long(123);
    }

    /**
     * <p>
     * Get a {@link User} by the user id
     * </p>
     * 
     * @param userid
     *            The userid to search for
     * @return A {@link User} object or <code>null</code> if no user was found
     */
    @Override
    public User findById(long userId) throws EJBException
    {
        UserImpl u = new UserImpl();

        u.setId(userId);
        u.setEmailAddress("test@test.org");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setGender(Gender.UNISEX);
        u.setUsername("john-123");

        LOG.log(Level.INFO, "Returning user {0}", u);

        return u;
    }

}
