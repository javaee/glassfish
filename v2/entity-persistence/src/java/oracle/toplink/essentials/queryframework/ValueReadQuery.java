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

import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p><b>Purpose</b>:
 * Concrete class to perform a read of a single data value.
 * <p>
 * <p><b>Responsibilities</b>:
 * Used in conjunction with CursoredStream size and Platform getSequence.
 * This can be used to read a single data value (i.e. one field).
 * A single data value is returned, or null if no rows are returned.
 *
 * @author James Sutherland
 * @since TOPLink/Java 1.2
 */
public class ValueReadQuery extends DirectReadQuery {

    /**
     * PUBLIC:
     * Initialize the state of the query.
     */
    public ValueReadQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified SQL string.
     */
    public ValueReadQuery(String sqlString) {
        super(sqlString);
    }

    /**
     * PUBLIC:
     * Initialize the query to use the specified call.
     */
    public ValueReadQuery(Call call) {
        super(call);
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Perform the work to execute the SQL string.
     * @exception  DatabaseException an error has occurred on the database
     * @return Object the data value or null.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        Object values = super.executeDatabaseQuery();
        ContainerPolicy cp = getContainerPolicy();
        if (cp.sizeFor(values) == 0) {
            return null;
        } else {
            return cp.next(cp.iteratorFor(values), getSession());
        }
    }
}
