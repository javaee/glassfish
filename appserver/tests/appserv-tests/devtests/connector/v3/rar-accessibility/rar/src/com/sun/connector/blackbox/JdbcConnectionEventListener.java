/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import java.util.Vector;

/**
 * @author Tony Ng
 */
public class JdbcConnectionEventListener
        implements javax.sql.ConnectionEventListener {

    private Vector listeners;
    private ManagedConnection mcon;

    public JdbcConnectionEventListener(ManagedConnection mcon) {
        listeners = new Vector();
        this.mcon = mcon;
    }

    public void sendEvent(int eventType, Exception ex,
                          Object connectionHandle) {
        Vector list = (Vector) listeners.clone();
        ConnectionEvent ce = null;
        if (ex == null) {
            ce = new ConnectionEvent(mcon, eventType);
        } else {
            ce = new ConnectionEvent(mcon, eventType, ex);
        }
        if (connectionHandle != null) {
            ce.setConnectionHandle(connectionHandle);
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ConnectionEventListener l =
                    (ConnectionEventListener) list.elementAt(i);
            switch (eventType) {
                case ConnectionEvent.CONNECTION_CLOSED:
                    l.connectionClosed(ce);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    l.localTransactionStarted(ce);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    l.localTransactionCommitted(ce);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    l.localTransactionRolledback(ce);
                    break;
                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    l.connectionErrorOccurred(ce);
                    System.out.println(" Received CONNECTION_ERROR_OCCURRED in listener");
                    break;
                default:
                    throw new IllegalArgumentException("Illegal eventType: " +
                            eventType);
            }
        }
    }


    public void addConnectorListener(ConnectionEventListener l) {
        listeners.addElement(l);
    }

    public void removeConnectorListener(ConnectionEventListener l) {
        listeners.removeElement(l);
    }

    public void connectionClosed(javax.sql.ConnectionEvent event) {
        // do nothing. The event is sent by the JdbcConnection wrapper
    }

    public void connectionErrorOccurred(javax.sql.ConnectionEvent event) {
        sendEvent(ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                event.getSQLException(), null);
    }
}
