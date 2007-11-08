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
package oracle.toplink.essentials.internal.ejb.cmp3.transaction.base;

import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.*;
import oracle.toplink.essentials.internal.ejb.cmp3.base.ExceptionFactory;
import oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base.DataSourceImpl;

/**
 * Implementation of JTA Transaction manager class.
 *
 * Currently support is limited to enlisting a single tx data source
 */
public class TransactionManagerImpl implements TransactionManager, UserTransaction {
    // Not null when a transaction is active
    TransactionImpl tx;

    /************************/
    /***** Internal API *****/
    /************************/
    private void debug(String s) {
        System.out.println(s);
    }

    /*
     * Used to create the single instance
     */
    public TransactionManagerImpl() {
        this.tx = null;
    }

    /*
     * Return true if a transaction has been explicitly begun
     */
    public boolean isTransactionActive() {
        return tx != null;
    }

    /*
     * Return a Connection if a transaction is active, otherwise return null
     */
    public Connection getConnection(DataSourceImpl ds, String user, String password) throws SQLException {
        return (tx == null) ? null : tx.getConnection(ds, user, password);
    }

    /************************************************************/
    /***** Supported TransactionManager/UserTransaction API *****/
    /************************************************************/
    public void begin() throws NotSupportedException, SystemException {
        debug("Tx - begin");

        if (isTransactionActive()) {
            throw new ExceptionFactory().txActiveException();
        }

        // New transaction created by virtue of Transaction existence
        tx = new TransactionImpl();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        debug("Tx - commit");

        if (!isTransactionActive()) {
            throw new ExceptionFactory().txNotActiveException();
        }
        try{
            tx.commit();
        }finally{
            tx = null;
        }
    }

    public int getStatus() throws SystemException {
        return (!isTransactionActive()) ? Status.STATUS_NO_TRANSACTION : tx.getStatus();
    }

    public Transaction getTransaction() throws SystemException {
        return tx;

    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        debug("Tx - rollback");

        if (!isTransactionActive()) {
            throw new ExceptionFactory().txNotActiveException();
        }
        try{
            tx.rollback();
        }finally{
            tx = null;
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        debug("Tx - rollback");

        if (!isTransactionActive()) {
            throw new ExceptionFactory().txNotActiveException();
        }
        tx.setRollbackOnly();
    }

    /****************************************************************/
    /***** NOT supported TransactionManager/UserTransaction API *****/
    /****************************************************************/
    public Transaction suspend() throws SystemException {
        return null;
    }

    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        // Do nothing
    }

    public void setTransactionTimeout(int i) throws SystemException {
        // Do nothing
    }
}
