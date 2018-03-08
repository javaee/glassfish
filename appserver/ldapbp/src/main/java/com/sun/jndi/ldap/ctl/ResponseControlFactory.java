/*
 * @(#)ResponseControlFactory.java	1.6 03/04/13
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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
