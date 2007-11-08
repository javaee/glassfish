/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.util;

import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.resource.spi.security.PasswordCredential;
import javax.resource.ResourceException;
import javax.resource.spi.*;
import com.sun.jdbcra.spi.ConnectionRequestInfo;
import java.util.Set;
import java.util.Iterator;

/**
 * SecurityUtils for Generic JDBC Connector.
 *
 * @version	1.0, 02/07/22
 * @author	Evani Sai Surya Kiran
 */
public class SecurityUtils {

    /**
     * This method returns the <code>PasswordCredential</code> object, given 
     * the <code>ManagedConnectionFactory</code>, subject and the 
     * <code>ConnectionRequestInfo</code>. It first checks if the 
     * <code>ConnectionRequestInfo</code> is null or not. If it is not null, 
     * it constructs a <code>PasswordCredential</code> object with
     * the user and password fields from the <code>ConnectionRequestInfo</code> and returns this
     * <code>PasswordCredential</code> object. If the <code>ConnectionRequestInfo</code> 
     * is null, it retrieves the <code>PasswordCredential</code> objects from 
     * the <code>Subject</code> parameter and returns the first
     * <code>PasswordCredential</code> object which contains a 
     * <code>ManagedConnectionFactory</code>, instance equivalent
     * to the <code>ManagedConnectionFactory</code>, parameter.
     *
     * @param	mcf	<code>ManagedConnectionFactory</code>
     * @param	subject	<code>Subject</code>
     * @param	info	<code>ConnectionRequestInfo</code>
     * @return	<code>PasswordCredential</code>
     * @throws	<code>ResourceException</code>	generic exception if operation fails
     * @throws	<code>SecurityException</code>	if access to the <code>Subject</code> instance is denied
     */
    public static PasswordCredential getPasswordCredential(final ManagedConnectionFactory mcf,
         final Subject subject, javax.resource.spi.ConnectionRequestInfo info) throws ResourceException {

	if (info == null) {
            if (subject == null) {
                return null;
            } else {
                PasswordCredential pc = (PasswordCredential) AccessController.doPrivileged
                    (new PrivilegedAction() {
                        public Object run() {
                            Set passwdCredentialSet = subject.getPrivateCredentials(PasswordCredential.class);
                            Iterator iter = passwdCredentialSet.iterator();
                            while (iter.hasNext()) {
                                PasswordCredential temp = (PasswordCredential) iter.next();
                                if (temp.getManagedConnectionFactory().equals(mcf)) {
                                    return temp;
                                }
                            }
                            return null;
                        }
                    });
                if (pc == null) {
                    throw new javax.resource.spi.SecurityException("No PasswordCredential found");
                } else {
                    return pc;
                }
            }
        } else {
            com.sun.jdbcra.spi.ConnectionRequestInfo cxReqInfo = (com.sun.jdbcra.spi.ConnectionRequestInfo) info;
            PasswordCredential pc = new PasswordCredential(cxReqInfo.getUser(), cxReqInfo.getPassword().toCharArray());
            pc.setManagedConnectionFactory(mcf);
            return pc;
        }
    }
    
    /**
     * Returns true if two strings are equal; false otherwise
     * 
     * @param	str1	<code>String</code>
     * @param	str2	<code>String</code>
     * @return	true	if the two strings are equal
     *        	false	otherwise
     */
    static private boolean isEqual(String str1, String str2) {
        if (str1 == null) {
            return (str2 == null);
        } else {
            return str1.equals(str2);
        }
    }

    /**
     * Returns true if two <code>PasswordCredential</code> objects are equal; false otherwise
     *
     * @param	pC1	<code>PasswordCredential</code>
     * @param	pC2	<code>PasswordCredential</code>
     * @return	true	if the two PasswordCredentials are equal
     *        	false	otherwise
     */
    static public boolean isPasswordCredentialEqual(PasswordCredential pC1, PasswordCredential pC2) {
        if (pC1 == pC2)
            return true;
        if(pC1 == null || pC2 == null)
            return (pC1 == pC2);
        if (!isEqual(pC1.getUserName(), pC2.getUserName())) {
            return false;
        }
        String p1 = null;
        String p2 = null;
        if (pC1.getPassword() != null) {
            p1 = new String(pC1.getPassword());
        }
        if (pC2.getPassword() != null) {
            p2 = new String(pC2.getPassword());
        }
        return (isEqual(p1, p2));
    }
}
