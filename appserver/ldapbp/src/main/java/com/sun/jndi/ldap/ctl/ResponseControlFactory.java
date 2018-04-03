/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jndi.ldap.ctl;

import java.io.IOException;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;

/**
 * This class represents a factory for creating LDAP response controls.
 * The following response controls are supported:
 * <ul>
 * <li>
 * Paged results, as defined in
 * <a href="http://www.ietf.org/rfc/rfc2696.txt">RFC 2696</a>.
 * <li>
 * Server-side sorting, as defined in
 * <a href="http://www.ietf.org/rfc/rfc2891.txt">RFC 2891</a>.
 * <li>
 * Virtual list view, as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ldapext-ldapv3-vlv-09.txt">draft-ietf-ldapext-ldapv3-vlv-09</a>.
 * <li>
 * Directory synchronization, as defined in <tt>draft-armijo-ldap-dirsync-01.txt</tt>
 * <li>
 * Get effective rights, as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-ietf-ldapext-acl-model-08.txt">draft-ietf-ldapext-acl-model-08</a>.
 * <li>
 * Password expiring notification and password expired notification,
 * as defined in <tt>draft-vchu-ldap-pwd-policy-00.txt</tt>
 * <li>
 * Authorization identity, as defined in
 * <a href="http://www.ietf.org/internet-drafts/draft-weltman-ldapv3-auth-response-08.txt">draft-weltman-ldapv3-auth-response-08</a>.
 * </ul>
 *
 * @see PagedResultsResponseControl
 * @see SortResponseControl
 * @see VirtualListViewResponseControl
 * @see DirSyncResponseControl
 * @see PasswordExpiredResponseControl
 * @see PasswordExpiringResponseControl
 * @see AuthorizationIDResponseControl
 * @author Vincent Ryan
 */

public class ResponseControlFactory extends ControlFactory {

    /**
     * Constructs a new instance of the response control factory.
     */
    public ResponseControlFactory() {
    }

    /**
     * Creates an instance of a response control class from a more
     * generic control class (BasicControl).
     *
     * @param ctl A non-null control.
     * @return    The LDAP control created or null if it cannot be created.
     *            Null indicates that another factory should be attempted.
     * @exception NamingException if this control factory encountered an
     *            error condition while attempting to create the LDAP control,
     *            and no other control factories are to be tried.
     */
    public Control getControlInstance(Control ctl) 
	throws NamingException {

	String id = ctl.getID();
	Control newCtl = null;

	try {
	    if (id.equals(SortResponseControl.OID)) {
		newCtl = new SortResponseControl(id, ctl.isCritical(),
		    ctl.getEncodedValue());

	    } else if (id.equals(VirtualListViewResponseControl.OID)) {
                newCtl = new VirtualListViewResponseControl(id,
		    ctl.isCritical(), ctl.getEncodedValue());

	    } else if (id.equals(PagedResultsResponseControl.OID)) {
		newCtl = new PagedResultsResponseControl(id, ctl.isCritical(),
		    ctl.getEncodedValue());

	    } else if (id.equals(DirSyncResponseControl.OID)) {
		newCtl = new DirSyncResponseControl(id, ctl.isCritical(),
		    ctl.getEncodedValue());

	    } else if (id.equals(PasswordExpiredResponseControl.OID)) {
                newCtl = new PasswordExpiredResponseControl(id,
		    ctl.isCritical(), ctl.getEncodedValue());

            } else if (id.equals(PasswordExpiringResponseControl.OID)) {
                newCtl = new PasswordExpiringResponseControl(id,
		    ctl.isCritical(), ctl.getEncodedValue());

            } else if (id.equals(AuthorizationIDResponseControl.OID)) {
		newCtl = new AuthorizationIDResponseControl(id,
		    ctl.isCritical(), ctl.getEncodedValue());
            }
	} catch (IOException e) {
	    NamingException ne = new NamingException();
	    ne.setRootCause(e);
	    throw ne;
	}
	return newCtl;
    }
}
