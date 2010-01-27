/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.security.util;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.spec.*;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.GFBase64Decoder;
import com.sun.enterprise.util.GFBase64Encoder;
import org.glassfish.internal.api.SharedSecureRandom;

/**
 * Util class for salted SHA processing.
 *
 * <P>Salted SHA (aka SSHA) is computed as follows:
 * <br> result = {SSHA}BASE64(SHA(password,salt),salt)
 *
 * <P>Methods are also provided to return partial results, such as
 * SHA( password , salt) without Base64 encoding.
 *
 */
public class SSHA
{
    private static final String SSHA_TAG = "{SSHA}";
    //TODO V3 need to check if second arg is correct
    private static StringManager sm =
        StringManager.getManager(SSHA.class);
    private static MessageDigest md = null;

    
    /**
     * Compute a salted SHA hash.
     *
     * @param salt Salt bytes.
     * @param password Password bytes.
     * @return Byte array of length 20 bytes containing hash result.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static byte[] compute(byte[] salt, byte[] password)
        throws IASSecurityException
    {
        byte[] buff = new byte[password.length + salt.length];
        System.arraycopy(password, 0, buff, 0, password.length);
        System.arraycopy(salt, 0, buff, password.length, salt.length);

        byte[] hash = null;

        synchronized (SSHA.class) {
            
            if (md == null) {
                try {
                    md = MessageDigest.getInstance("SHA");
                } catch (Exception e) {
                    throw new IASSecurityException(e);
                }    
            }

            assert (md != null);
            md.reset();
            hash = md.digest(buff);
        }

        assert (hash.length==20); // SHA output is 160 bits

        return hash;
    }


    /**
     * Compute a salted SHA hash.
     *
     * <P>Salt bytes are obtained using SecureRandom.
     *
     * @param saltBytes Number of bytes of random salt value to generate.
     * @param password Password bytes.
     * @return Byte array of length 20 bytes containing hash result.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static byte[] compute(int saltBytes, byte[] password)
        throws IASSecurityException
    {
        SecureRandom rng=SharedSecureRandom.get();
        byte[] salt=new byte[saltBytes];
        rng.nextBytes(salt);

        return compute(salt, password);
    }

    
    /**
     * Perform encoding of salt and computed hash.
     *
     * @param salt Salt bytes.
     * @param hash Result of prior compute() operation.
     * @return String Encoded string, as described in class documentation.
     *
     */
    public static String encode(byte[] salt, byte[] hash)
    {
        assert (hash.length==20);
        byte[] res = new byte[20+salt.length];
        System.arraycopy(hash, 0, res, 0, 20);
        System.arraycopy(salt, 0, res, 20, salt.length);

        GFBase64Encoder encoder = new GFBase64Encoder();
        String encoded = encoder.encode(res);

        String out = SSHA_TAG + encoded;
        return out;
    }


    /**
     * Compute a salted SHA hash and return the encoded result. This is
     * a convenience method combining compute() and encode().
     *
     * @param salt Salt bytes.
     * @param password Password bytes.
     * @return String Encoded string, as described in class documentation.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static String computeAndEncode(byte[] salt, byte[] password)
        throws IASSecurityException
    {
        byte[] hash = compute(salt, password);
        return encode(salt, hash);
    }


    /**
     * Compute a salted SHA hash and return the encoded result. This is
     * a convenience method combining compute() and encode().
     *
     * @param saltBytes Number of bytes of random salt value to generate.
     * @param password Password bytes.
     * @return String Encoded string, as described in class documentation.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static String computeAndEncode(int saltBytes, byte[] password)
        throws IASSecurityException
    {
        SecureRandom rng=SharedSecureRandom.get();
        byte[] salt=new byte[saltBytes];
        rng.nextBytes(salt);

        byte[] hash = compute(salt, password);
        return encode(salt, hash);
    }


    /**
     * Verifies a password.
     *
     * <P>The given password is verified against the provided encoded SSHA
     * result string.
     *
     * @param encoded Encoded SSHA value (e.g. output of computeAndEncode())
     * @param password Password bytes of the password to verify.
     * @returns True if given password matches encoded SSHA.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static boolean verify(String encoded, byte[] password)
        throws IASSecurityException
    {
        byte[] hash = new byte[20];
        byte[] salt = decode(encoded, hash);
        return verify(salt, hash, password);
    }


    /**
     * Verifies a password.
     *
     * <P>The given password is verified against the provided salt and hash
     * buffers.
     *
     * @param salt Salt bytes used in the hash result.
     * @param hash Hash result to compare against.
     * @param password Password bytes of the password to verify.
     * @returns True if given password matches encoded SSHA.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static boolean verify(byte[] salt, byte[] hash, byte[] password)
        throws IASSecurityException
    {
        byte[] newHash = compute(salt, password);
        return Arrays.equals(hash, newHash);
    }


    /**
     * Decodes an encoded SSHA string.
     *
     * @param encoded Encoded SSHA value (e.g. output of computeAndEncode())
     * @param hashResult A byte array which must contain 20 elements. Upon
     *      succesful return from method, it will be filled by the hash
     *      value decoded from the given SSHA string. Existing values are
     *      not used and will be overwritten.
     * @returns Byte array containing the salt obtained from the encoded SSHA
     *      string.
     * @throws IASSecurityExeption Thrown if there is an error.
     *
     */
    public static byte[] decode(String encoded, byte[] hashResult)
        throws IASSecurityException
    {
        assert (hashResult.length==20);
        if (!encoded.startsWith(SSHA_TAG)) {
            String msg = sm.getString("ssha.badformat", encoded);
            throw new IASSecurityException(msg);
        }

        String ssha = encoded.substring(SSHA_TAG.length());
        
        GFBase64Decoder decoder = new GFBase64Decoder();
        byte[] result = null;
      
        try {
            result = decoder.decodeBuffer(ssha);
        } catch (IOException e) {
            throw new IASSecurityException(e);
        }
        assert (result.length > 20);
        
        byte[] salt = new byte[result.length - 20];

        System.arraycopy(result, 0, hashResult, 0, 20);
        System.arraycopy(result, 20, salt, 0, result.length-20);

        return salt;
    }




}
