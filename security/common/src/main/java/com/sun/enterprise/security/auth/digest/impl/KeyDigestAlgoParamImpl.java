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
package com.sun.enterprise.security.auth.digest.impl;

import com.sun.enterprise.security.auth.digest.api.Key;
import com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter;

/**
 *
 *  @author K.Venugopal@sun.com
 */
public class KeyDigestAlgoParamImpl implements DigestAlgorithmParameter, Key {

    private String userName;
    private String realmName;
    private String algorithm = null;
    private String name = "A1";
    private static byte[] delimeter = ":".getBytes();

    public KeyDigestAlgoParamImpl(String user, String realm) {
        this.userName = user;
        this.realmName = realm;
    }

    public KeyDigestAlgoParamImpl(String algorithm, String user, String realm) {
        this.userName = user;
        this.realmName = realm;
        this.algorithm = algorithm;
    }

    public String getUsername() {
        return userName;
    }

    public String getRealmName() {
        return realmName;
    }

    public byte[] getValue() {
        throw new UnsupportedOperationException();
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getDelimiter() {
        return delimeter;
    }

    public String getName() {
        return name;
    }
}
