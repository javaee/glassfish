/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.security.auth.digest.api;

/**
 * represents plain text password and pre hashed(username+realmname+password) password.
 * @author K.Venuopal@sun.com
 */
public interface Password {
    public static final int PLAIN_TEXT= 0;
    public static final int HASHED = 1;
    /**
     * returns PLAIN_TEXT or HASHED.
     * @returns int
     */
    public int getType();
    /**
     * returns password.
     * @returns byte[] 
     */
    public byte[] getValue();
}
