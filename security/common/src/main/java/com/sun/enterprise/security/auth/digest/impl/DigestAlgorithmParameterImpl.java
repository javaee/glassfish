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




/**
 * Implementation class for Digest algorithm parameters. 
 * @author K.Venugopal@sun.com
 */

public class DigestAlgorithmParameterImpl implements  com.sun.enterprise.security.auth.digest.api.DigestAlgorithmParameter {
    
    private static byte[] delimeter = ":".getBytes();
    private String algorithm = "";
    private byte[] data = null;          
    private String name = "";

    public DigestAlgorithmParameterImpl(String name,byte[] data) {        
        this.data = data;
        this.name = name;
    }
    
    public DigestAlgorithmParameterImpl(String name,byte[] data,byte delimiter) {        
        this.data = data;
        this.delimeter = delimeter;
        this.name = name;
    }
    
    
    public DigestAlgorithmParameterImpl(String name,String algorithm, byte[] data) {
        this.algorithm = algorithm;
        this.data = data;
        this.name = name;
    }
    
    public DigestAlgorithmParameterImpl(String name,String algorithm, byte[] data,byte [] delimiter) {
        this.algorithm = algorithm;
        this.data = data;
        this.delimeter = delimeter;
        this.name = name;
    }
    
    public String getAlgorithm() {
        return this.algorithm;
    }
    
    public  byte[] getValue() {
        return data;
    }    
   
    public byte[] getDelimiter() {
        return delimeter;
    }

    public String getName() {
        return this.name;
    }
}
