/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth.realm;

import java.security.Principal;
import com.sun.enterprise.security.auth.*;

/**
 * This interface is used by the Authentication Service to have the 
 * Principal authenticated by the realm. A realm provides an 
 * implementation of this interface.
 * @author Harish Prabandham
 * @author Harpreet Singh
 */
public interface AuthenticationHandler {
    /**
     * Returns the Realm that this Authentication Handler is authenticating
     * in.
     * @return The Realm object in which this handler is authenticating in.
     */
    public Realm getRealm();

    /**
     * This method authenticates the given principal using the specified 
     * authentication data and the Principal's Credentials. The result of
     * the authentication is returned back.
     * @param The principal (user) being authenticated.
     * @param The data needed for authentication.
     * @return boolean denoting true for success and false for failure
     * authentication.
     */
    public boolean doAuthentication(String principalName,
				    byte[] authData);
}
