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

// Java imports
import java.sql.*;
import java.io.*;

// TopLink imports
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.parsing.ejbql.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <b>Purpose</b>: Used as an abstraction of a database invocation.
 * A call is an EJBQL string.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Parse the EJBQL String
 * <li> Populate the contained query's selection criteria. Add attributes to ReportQuery (if required).
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
/**
 * <b>Purpose</b>: Used as an abstraction of a database invocation.
 * A call is an EJBQL string.
 */
public class EJBQLCall implements Serializable, Call {
    // Back reference to query, unfortunately required for events.
    protected DatabaseQuery query;
    protected String ejbqlString;

    // Check that we aren't parsing more than once
    protected boolean isParsed;

    /**
     * PUBLIC
     * Create a new EJBQLCall.
     */
    public EJBQLCall() {
        super();
    }

    /**
     * PUBLIC
     * Create a new EJBQLCall with an ejbqlString
     */
    public EJBQLCall(String ejbqlString) {
        this();
        this.ejbqlString = ejbqlString;
    }

    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildNewQueryMechanism(DatabaseQuery query) {
        return new EJBQLCallQueryMechanism(query, this);
    }

	//bug4324564: removed buildParserFor to remove Antlr.jar compile dependency -CD
	//   See class oracle.toplink.essentials.internal.parsing.ejbql.EJBQLParserFactory.buildParserFor


    /**
     * INTERNAL:
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildQueryMechanism(DatabaseQuery query, DatabaseQueryMechanism mechanism) {
        return buildNewQueryMechanism(query);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Return the string for the call
     */
    public String getCallString() {
        return getEjbqlString();
    }

    /**
     * INTERNAL:
     * Return the EJBQL string for this call
     */
    public String getEjbqlString() {
        return ejbqlString;
    }

    /**
     * INTERNAL
     * Return the isParsed state
     */
    private boolean getIsParsed() {
        return isParsed;
    }

    /**
     * Back reference to query, unfortunately required for events.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     * INTERNAL:
     * Return the SQL string for this call. Always return null
     * since this is an EJBQL call
     */
    public String getLogString(Accessor accessor) {
        return getSQLString();
    }

    /**
     * INTERNAL:
     * Return the SQL string for this call. Always return null
     * since this is an EJBQL call
     */
    public String getSQLString() {
        return null;
    }

    /**
     * INTERNAL:
     * Yes this is an EJBQLCall
     */
    public boolean isEJBQLCall() {
        return true;
    }

    /**
     * Return whether all the results of the call have been returned.
     */
    public boolean isFinished() {
        //never used, but required for implementing Call.
        return true;
    }

    /**
     * INTERNAL
     * Is this query Parsed
     */
    public boolean isParsed() {
        return getIsParsed();
    }

	//bug4324564: removed parseEJBQLString to remove Antlr.jar compile dependency -CD
	//  See class oracle.toplink.essentials.internal.parsing.ejbql.EJBQLParserFactory.parseEJBQLString

    /**
     * Populate the query using the information retrieved from parsing the EJBQL.
     */
    public void populateQuery(AbstractSession session) {
        if (!isParsed()) {
			//bug4324564: moved code to EJBQLParserFactory.populateQuery -CD
            (new EJBQLParserFactory()).populateQuery(getEjbqlString(), (ObjectLevelReadQuery)getQuery(), session);
            // Make sure we don't parse and prepare again.
            this.setIsParsed(true);
        }
    }

    /**
     * INTERNAL:
     * Prepare the JDBC statement, this may be parameterize or a call statement.
     * If caching statements this must check for the pre-prepared statement and re-bind to it.
     */
    public PreparedStatement prepareStatement(DatabaseAccessor accessor, AbstractRecord translationRow, AbstractSession session) throws SQLException {
        return null;
    }

    /**
     * INTERNAL:
     * Set the EJBQL string for this call
     */
    public void setEjbqlString(java.lang.String newEjbqlString) {
        ejbqlString = newEjbqlString;
    }

    /**
     * INTERNAL
     * Set the isParsed state
     */
    public void setIsParsed(boolean newIsParsed) {
        isParsed = newIsParsed;
    }

    /**
     * INTERNAL:
     * Back reference to query, unfortunately required for events.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * INTERNAL:
     * translate method comment.
     */
    public void translate(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
    }
}
