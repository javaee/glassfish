/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * SQLConnector.java
 *
 * Created on April 25, 2005, 2:14 PM
 */


package com.sun.persistence.runtime.connection;

import com.sun.org.apache.jdo.store.Connector;

import java.sql.Connection;

/**
 * A SQLConnector interface is representing a sql database connection.
 * It extends <code>com.sun.org.apache.jdo.store.Connector</code> interface. And
 * has more APIs related to database connection.
 *
 * @author jie leng
 */
public interface SQLConnector extends Connector {

    /**
     * Returns a Connection. If there is no existing one, asks ConnectionFactory
     * for a new Connection.
     */
    public Connection getConnection();

    /**
     * Replace a connection. Used in a managed environment only.
     */
    public void replaceConnection();

    /**
     * A connection releases if it doesn't need to hold it by the connector
     * logic.
     * Connection cannot be released if it is part of the
     * commit/rollback operation or inside a pessimistic transaction
     */
    public void releaseConnection();
}
