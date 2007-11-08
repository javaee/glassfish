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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.internal.parsing.ejbql;

import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL
 * <p><b>Purpose</b>:
 * Mechanism used for EJBQL.
 * <p>
 * <p><b>Responsibilities</b>:
 * Executes the appropriate call.
 *
 * @author Jon Driscoll, Joel Lucuik
 * @since TopLink 4.0
 */
public class EJBQLCallQueryMechanism extends ExpressionQueryMechanism {
    //EJBQLCall gets its own variable, rather than inheriting
    //call (because an EJBQLCall is out on its own)
    protected EJBQLCall ejbqlCall;

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     */
    public EJBQLCallQueryMechanism(DatabaseQuery query) {
        super(query);
    }

    /**
     * INTERNAL
     * Initialize the state of the query
     * @param query - owner of mechanism
     * @param call - Database call
     */
    public EJBQLCallQueryMechanism(DatabaseQuery query, EJBQLCall call) {
        this(query);
        this.ejbqlCall = call;
        call.setQuery(query);
    }

    public Object clone() {
        EJBQLCallQueryMechanism copyOfMyself = (EJBQLCallQueryMechanism)super.clone();
        copyOfMyself.ejbqlCall = (EJBQLCall)ejbqlCall.clone();
        return copyOfMyself;

    }

    /**
     * Internal:
     * In the case of EJBQL, an expression needs to be generated, and the query populated.
     */
    public void buildSelectionCriteria(AbstractSession newSession) {
        getEJBQLCall().setQuery(getQuery());
        getEJBQLCall().populateQuery(newSession);
    }

    public EJBQLCall getEJBQLCall() {
        return ejbqlCall;
    }

    public boolean isEJBQLCallQueryMechanism() {
        return true;
    }

    /**
     * All the query mechanism related things are initialized here.
     * This method is called on the *clone* of the query with
     * every execution.
     */
    public void prepareForExecution() throws QueryException {
    }

    public void setEJBQLCall(EJBQLCall newEJBQLCall) {
        ejbqlCall = newEJBQLCall;
    }
}
