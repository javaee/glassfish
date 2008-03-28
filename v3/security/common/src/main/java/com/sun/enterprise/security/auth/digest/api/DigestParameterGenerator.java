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

import com.sun.enterprise.security.auth.digest.*;
import com.sun.enterprise.security.auth.digest.impl.HttpDigestParamGenerator;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;

/**
 * This Factory provides DigestParameterGenerator instances to generate 
 * DigestAlgorithmParameter objects from Http and Sip servlet requests. 
 *
 * @author K.Venugopal@sun.com
 */
public abstract class DigestParameterGenerator {

    public static final String HTTP_DIGEST="HttpDigest";
    public static final String SIP_DIGEST="SIPDigest";
    public DigestParameterGenerator() {
    }

    //TODO: Ability to return implementations through services mechanism.
    public static DigestParameterGenerator getInstance(String algorithm) {
        if (HTTP_DIGEST.equals(algorithm)) {
            return new HttpDigestParamGenerator();
        }
        return new HttpDigestParamGenerator();
    }
    
    public abstract DigestAlgorithmParameter[] generateParameters(AlgorithmParameterSpec value)throws InvalidAlgorithmParameterException;
}
