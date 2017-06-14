/*
 * Use of this J2EE Connectors Sample Source Code file is governed by
 * the following modified BSD license:
 * 
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA,
 * OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.connector.blackbox;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Set;

public class Util {
    static public PasswordCredential getPasswordCredential
            (final ManagedConnectionFactory mcf,
             final Subject subject, ConnectionRequestInfo info)
            throws ResourceException {

        if (subject == null) {
            if (info == null) {
                return null;
            } else {
                JdbcConnectionRequestInfo myinfo =
                        (JdbcConnectionRequestInfo) info;
                PasswordCredential pc =
                        new PasswordCredential(myinfo.getUser(),
                                myinfo.getPassword().toCharArray());
                pc.setManagedConnectionFactory(mcf);
                return pc;
            }
        } else {
            PasswordCredential pc =
                    (PasswordCredential) AccessController.doPrivileged
                            (new PrivilegedAction() {
                                public Object run() {
                                    Set creds = subject.getPrivateCredentials
                                            (PasswordCredential.class);
                                    Iterator iter = creds.iterator();
                                    while (iter.hasNext()) {
                                        PasswordCredential temp =
                                                (PasswordCredential) iter.next();
                                        if (temp.getManagedConnectionFactory().
                                                equals(mcf)) {
                                            return temp;
                                        }
                                    }
                                    return null;
                                }
                            });
            if (pc == null) {
                throw new SecurityException("No PasswordCredential found");
            } else {
                return pc;
            }
        }
    }

    static public boolean isEqual(String a, String b) {
        if (a == null) {
            return (b == null);
        } else {
            return a.equals(b);
        }
    }

    static public boolean isPasswordCredentialEqual(PasswordCredential a,
                                                    PasswordCredential b) {
        if (a == b) return true;
        if ((a == null) && (b != null)) return false;
        if ((a != null) && (b == null)) return false;
        if (!isEqual(a.getUserName(), b.getUserName())) return false;
        String p1 = null;
        String p2 = null;
        if (a.getPassword() != null) {
            p1 = new String(a.getPassword());
        }
        if (b.getPassword() != null) {
            p2 = new String(b.getPassword());
        }
        return (isEqual(p1, p2));
    }

}
