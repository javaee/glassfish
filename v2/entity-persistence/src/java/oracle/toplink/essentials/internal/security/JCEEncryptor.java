/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.exceptions.ConversionException;

/**
 * TopLink reference implementation for password encryption.
 *
 * @author Guy Pelletier
 */
public class JCEEncryptor implements Securable {
    private Cipher m_cipher;
    private final String m_algorithm = "DES";
    private final String m_padding = "DES/ECB/PKCS5Padding";

    public JCEEncryptor() throws Exception {

        /*
         * We want to force the initialization of the cipher here. This is a fix
         * for bug #2696486.
         * JDev with JDK 1.3 in some cases will allow a JCE object to be created
         * when it shouldn't. That is, JDev includes an incompletely configured JCE
         * library for JDK 1.3, meaning JCE will not run properly in the VM. So, JDev
         * allows you to create a JCEEncryptor object, but eventually throw's
         * errors when trying to make JCE library calls from encryptPassword.
         *
         * Confusing??? Well, don't move this code before talking to Guy first!
         */
        m_cipher = Cipher.getInstance(m_padding);
    }

    /**
     * Encrypts a string. Will throw a validation exception.
     */
    public synchronized String encryptPassword(String password) {
        try {
            m_cipher.init(Cipher.ENCRYPT_MODE, Synergizer.getMultitasker(m_algorithm));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CipherOutputStream cos = new CipherOutputStream(baos, m_cipher);
            ObjectOutputStream oos = new ObjectOutputStream(cos);

            oos.writeObject(password);
            oos.flush();
            oos.close();

            return Helper.buildHexStringFromBytes(baos.toByteArray());
        } catch (Exception e) {
            throw ValidationException.errorEncryptingPassword(e);
        }
    }

    /**
     * Decrypts a string. Will throw a validation exception.
     * Handles backwards compatability for older encrypted strings.
     */
    public synchronized String decryptPassword(String encryptedPswd) {
        String password = "";

        try {
            m_cipher.init(Cipher.DECRYPT_MODE, Synergizer.getMultitasker(m_algorithm));

            byte[] bytePassword = Helper.buildBytesFromHexString(encryptedPswd);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytePassword);
            CipherInputStream cis = new CipherInputStream(bais, m_cipher);
            ObjectInputStream ois = new ObjectInputStream(cis);

            password = (String)ois.readObject();
            ois.close();
        } catch (IOException e) {
            // JCE 1.2.2 couldn't decrypt it, assume clear text
            password = encryptedPswd;
        } catch (ArrayIndexOutOfBoundsException e) {
            // JCE 1.2.1 couldn't decrypt it, assume clear text
            password = encryptedPswd;
        } catch (ConversionException e) {
            // Never prepared (buildBytesFromHexString failed), assume clear text
            password = encryptedPswd;
        } catch (Exception e) {
            throw ValidationException.errorDecryptingPassword(e);
        }

        return password;
    }

    private static class Synergizer {
        private static String multitasker = "E60B80C7AEC78038";

        static public SecretKey getMultitasker(String algorithm) throws Exception {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(new DESKeySpec(Helper.buildBytesFromHexString(multitasker)));
        }
    }
}
