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
package oracle.toplink.essentials.queryframework;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;

/**
 * <p><b>Purpose</b>:
 * Concrete class used for executing non selecting SQL strings.
 *
 * <p><b>Responsibilities</b>:
 * <ul>
 * <li> Execute a non selecting raw SQL string.
 * </ul>
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class DataModifyQuery extends ModifyQuery {
    public DataModifyQuery() {
        super();
    }

    public DataModifyQuery(String sqlString) {
        this();

        setSQLString(sqlString);
    }

    public DataModifyQuery(Call call) {
        this();
        setCall(call);
    }

    /**
     * INTERNAL:
     * Perform the work to execute the SQL call.
     * Return the row count of the number of rows effected by the SQL call.
     */
    public Object executeDatabaseQuery() throws DatabaseException {

        /* Fix to allow executing non-selecting SQL in a UnitOfWork. - RB */
        if (getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();
            /* bug:4211104 for DataModifyQueries executed during an event, while transaction was started by the uow*/
            if ( !unitOfWork.getCommitManager().isActive() && !unitOfWork.isInTransaction()) {
                unitOfWork.beginEarlyTransaction();
            }
            unitOfWork.setWasNonObjectLevelModifyQueryExecuted(true);
        }
        return getQueryMechanism().executeNoSelect();
    }

    /**
     * PUBLIC:
     * Return if this is a data modify query.
     */
    public boolean isDataModifyQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     */
    protected void prepare() {
        super.prepare();

        getQueryMechanism().prepareExecuteNoSelect();
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session. In particular,
     * set the descriptor of the receiver to the Descriptor for the
     * appropriate class for the receiver's object.
     */
    public void prepareForExecution() throws QueryException {
        super.prepareForExecution();

        setModifyRow(getTranslationRow());
    }
}
