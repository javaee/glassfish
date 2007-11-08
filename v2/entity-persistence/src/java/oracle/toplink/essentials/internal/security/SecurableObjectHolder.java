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

import java.security.AccessController;
import java.security.PrivilegedActionException;

import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.helper.ConversionManager;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;

/**
 * Holder of a SecurableObject. Securable objects should not be held onto
 * directly, instead they should be accessed via this holder.
 *
 * @author Guy Pelletier
 * @date June 26, 2003
 */
public class SecurableObjectHolder {

    /** The JCE encryption class name */
    private final static String JCE_ENCRYPTION_CLASS_NAME = "oracle.toplink.essentials.internal.security.JCEEncryptor";

    /** The encryption class name **/
    private String m_securableClassName;

    /** The actual encryption object **/
    private Securable m_securableObject;

    public SecurableObjectHolder() {
        this(null);
    }

    public SecurableObjectHolder(String securableClassName) {
        m_securableObject = null;
        m_securableClassName = securableClassName;
    }

    public void setEncryptionClassName(String securableClassName) {
        m_securableClassName = securableClassName;
    }

    public Securable getSecurableObject() {
        if (m_securableObject == null) {
            initSecurableObject();
        }

        return m_securableObject;
    }

    public boolean hasSecurableObject() {
        return m_securableObject != null;
    }

    /**
       * Convert a String into a Securable object
       * Class name must be fully qualified, eg. oracle.toplink.essentials.internal.security.JCEEncryptor
     * Default is the JCEEncryptor
       */
    private void initSecurableObject() {
        boolean initPassThroughEncryptor = false;

        if (m_securableClassName == null) {
            // Since we are defaulting, hence, assuming they can initialize the JCE
            // libraries, if the init fails, this flag tells us to assume no encryption.
            // However, if the JCE init does work, the JCEEncryptor will need to 
            // determine that a password was not encrypted by it, therefore, assume 
            // clear text. See JCEEncryptor.
            initPassThroughEncryptor = true;
            m_securableClassName = JCE_ENCRYPTION_CLASS_NAME;
        }

        try {
            ConversionManager cm = ConversionManager.getDefaultManager();
            Class securableClass = (Class)cm.convertObject(m_securableClassName, Class.class);
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    m_securableObject = (Securable)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(securableClass));
                } catch (PrivilegedActionException exception) {
                    throw exception.getException();
                }
            } else {
                m_securableObject = (Securable)PrivilegedAccessHelper.newInstanceFromClass(securableClass);
            }
        } catch (Throwable e) {
            if (initPassThroughEncryptor) {// default failed, so perform no encryption.
                m_securableObject = new PassThroughEncryptor();
            } else {
                throw ValidationException.invalidEncryptionClass(m_securableClassName, e);
            }
        }
    }

    /*
     * If we default to JCE and the initialization fails, our fall back is to do
     * no encryption. This covers the case where the user is running against JDK 1.3
     * At runtime, no encryption will be made and the passwords will be assummed to
     * be clear text.
     */
    private class PassThroughEncryptor implements Securable {
        public String encryptPassword(String pswd) {
            return pswd;
        }

        public String decryptPassword(String encryptedPswd) {
            return encryptedPswd;
        }
    }
}
