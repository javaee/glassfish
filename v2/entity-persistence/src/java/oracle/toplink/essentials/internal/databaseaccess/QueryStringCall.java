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
package oracle.toplink.essentials.internal.databaseaccess;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 * <b>Purpose<b>: Used to define query string calls.
 * These include SQLCall, XQueryInteraction which reuse translation behavoir through
 * this interface.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public interface QueryStringCall extends Call {

    /**
     * The parameters are the values in order of occurance in the SQL statement.
     * This is lazy initialized to conserv space on calls that have no parameters.
     */
    public Vector getParameters();

    /**
     * The parameter types determine if the parameter is a modify, translation or litteral type.
     */
    public Vector getParameterTypes();

    /**
     * The parameters are the values in order of occurance in call.
     * This is lazy initialized to conserv space on calls that have no parameters.
     */
    public boolean hasParameters();

    /**
     * Allow pre-printing of the query/SQL string for fully bound calls, to save from reprinting.
     * This should call translateCustomQuery() in the call implementation.
     */
    public void prepare(AbstractSession session);

    /**
     * Allow the call to translate from the translation for predefined calls.
     * This should call translateQueryString() in the call implementation.
     */
    public void translate(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session);

    /**
     * Return the query string of the call.
     * This must be overwritten by subclasses that support query language translation (SQLCall, XQueryCall).
     */
    public String getQueryString();

    /**
     * Set the query string of the call.
     * This must be overwritten by subclasses that support query language translation (SQLCall, XQueryCall).
     */
    public void setQueryString(String queryString);

    /**
     * Parse the query string for # markers for custom query based on a query language.
     * This is used by SQLCall and XQuery call, but can be reused by other query languages.
     */
    public void translateCustomQuery();

    /**
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendLiteral(Writer writer, Object literal);

    /**
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendTranslation(Writer writer, DatabaseField modifyField);

    /**
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendModify(Writer writer, DatabaseField modifyField);

    /**
     * Add the parameter.
     * If using binding bind the parameter otherwise let the platform print it.
     * The platform may also decide to bind the value.
     */
    public void appendParameter(Writer writer, Object parameter, AbstractSession session);

    /**
     * Allow the call to translate from the translation for predefined calls.
     */
    public void translateQueryString(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session);

    /**
     * Should return true.
     */
    public boolean isQueryStringCall();
}
