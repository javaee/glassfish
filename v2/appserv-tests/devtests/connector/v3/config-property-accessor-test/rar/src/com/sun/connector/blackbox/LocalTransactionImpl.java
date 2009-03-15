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
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransaction;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Tony Ng
 */
public class LocalTransactionImpl implements LocalTransaction {

    // XXX TODO: call sequence, tx.begin

    private JdbcManagedConnection mc;

    public LocalTransactionImpl(JdbcManagedConnection mc) {
        this.mc = mc;
    }

    public void begin() throws ResourceException {
        try {
            Connection con = mc.getJdbcConnection();
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }
    }

    public void commit() throws ResourceException {
        Connection con = null;
        try {
            con = mc.getJdbcConnection();
            con.commit();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    public void rollback() throws ResourceException {
        Connection con = null;
        try {
            con = mc.getJdbcConnection();
            con.rollback();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }
}
