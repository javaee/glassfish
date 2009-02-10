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

import com.sun.enterprise.security.auth.digest.api.NestedDigestAlgoParam;
import java.security.spec.AlgorithmParameterSpec;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class NestedDigestAlgoParamImpl implements NestedDigestAlgoParam {

    private byte[] delimeter = ":".getBytes();
    private String algorithm = "";
    private AlgorithmParameterSpec[] params = null;
    private String name = "";

    public NestedDigestAlgoParamImpl(String algorithm, String name,AlgorithmParameterSpec[] values) {
        this.algorithm = algorithm;
        this.params = values;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(String name,AlgorithmParameterSpec[] values) {
        this.params = values;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(String algorithm,String name, AlgorithmParameterSpec[] values, byte[] delimiter) {
        this.algorithm = algorithm;
        this.params = values;
        this.delimeter = delimiter;
        this.name = name;
    }

    public NestedDigestAlgoParamImpl(AlgorithmParameterSpec[] values,String name,  byte[] delimiter) {
        this.params = values;
        this.delimeter = delimiter;
        this.name = name;
    }

    public AlgorithmParameterSpec[] getNestedParams() {
        return params;
    }

    public byte[] getDelimiter() {
        return delimeter;
    } 

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        return name;
    }
}
