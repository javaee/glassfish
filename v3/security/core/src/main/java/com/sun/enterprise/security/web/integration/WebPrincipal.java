/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.security.web.integration;

import com.sun.enterprise.security.*;
import java.security.Principal;
import java.security.cert.X509Certificate;
import org.glassfish.security.common.PrincipalImpl;
import com.sun.enterprise.security.SecurityContext;

public class WebPrincipal extends PrincipalImpl {


    private String password;

    private X509Certificate[] certs;

    private boolean useCertificate;

    private SecurityContext secCtx;

    private Principal customPrincipal;

    public WebPrincipal(Principal p, SecurityContext context) {
	super(p.getName());
	if (!(p instanceof PrincipalImpl)) {
	    customPrincipal = p;
	}
        this.useCertificate = false;
        this.secCtx = context;
    }

    public WebPrincipal(String user, String password,
                        SecurityContext context) {
        super(user);
        this.password = password;
        this.useCertificate = false;
        this.secCtx = context;
    }

    public WebPrincipal(X509Certificate[] certs,
                        SecurityContext context) {
        super(certs[0].getSubjectDN().getName());
        this.certs = certs;
        this.useCertificate = true;
        this.secCtx = context;
    }

    public String getPassword() {
        return password;
    }

    public X509Certificate[] getCertificates() {
        return certs;
    }

    public boolean isUsingCertificate() {
        return useCertificate;
    }

    public SecurityContext getSecurityContext() {
        return secCtx;
    }

    public String getName() {
	if (customPrincipal == null) {
	    return super.getName();
	} else {
	    return customPrincipal.getName();
	}
    }

    public boolean equals(Object another) {

	if (customPrincipal == null) {
	    return super.equals(another);
	} 
	return customPrincipal.equals(another);
    }

    public int hashCode() {
	if (customPrincipal == null) {
	    return super.hashCode();
	} 
	return customPrincipal.hashCode();
    }

    public String toString() {
	if (customPrincipal == null) {
	    return super.toString();
	} 
	return customPrincipal.toString();
    }

}

