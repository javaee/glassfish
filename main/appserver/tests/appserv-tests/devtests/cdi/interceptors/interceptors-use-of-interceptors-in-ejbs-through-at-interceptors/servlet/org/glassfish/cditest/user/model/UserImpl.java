/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

package org.glassfish.cditest.user.model;

import org.glassfish.cditest.user.api.model.Gender;
import org.glassfish.cditest.user.api.model.User;

/**
 * <p>
 * Client-side implementation of {@link User}.
 * </p>
 * 
 * @author chaoslayer
 */
public class UserImpl implements User
{

    private static final long serialVersionUID = 1L;

    private Long id;
    private String lastName;
    private String firstName;
    private Gender gender;
    private String username;
    private String emailAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>
     * Get the username used for login
     * </p>
     * 
     * @return the value of username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * <p>
     * Set the username
     * </p>
     * 
     * @param username
     *            new value of username
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * <p>
     * Get the gender for the user
     * </p>
     * 
     * <p>
     * A gender is much better suited to our needs here as the pure biological
     * sex is.
     * </p>
     * 
     * @return the value of gender
     */
    public Gender getGender()
    {
        return gender;
    }

    /**
     * <p>
     * Set the gender for the user
     * </p>
     * 
     * <p>
     * A gender is much better suited to our needs here as the pure biological
     * sex is.
     * </p>
     * 
     * @param gender
     *            new value of gender
     */
    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    /**
     * Get the value of firstName
     * 
     * @return the value of firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Set the value of firstName
     * 
     * @param firstName
     *            new value of firstName
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * Get the value of lastName
     * 
     * @return the value of lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Set the value of lastName
     * 
     * @param lastName
     *            new value of lastName
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * Get the value of emailAddress
     * 
     * @return the value of emailAddress
     */
    public String getEmailAddress()
    {
        return emailAddress;
    }

    /**
     * Set the value of emailAddress
     * 
     * @param emailAddress
     *            new value of emailAddress
     */
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    /**
     * String representation of the object data
     * 
     * @return The object data as String
     */
    @Override
    public String toString()
    {
        return "UserImpl [id=" + id +
                ", emailAddress=" + emailAddress +
                ", firstName=" + firstName +
                ", gender=" + gender +
                ", lastName=" + lastName +
                ", username=" + username + "]";
    }

}
