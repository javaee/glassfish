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
package oracle.toplink.essentials.internal.expressions;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Expression SQL printer.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print an expression in SQL format.
 * <li> Replaces FIELD types with field names from the descriptor.
 * <li> Replaces PARAMETER types with row or object values.
 * <li> Calls accessor to print primitive types.
 * </ul>
 *    @author Dorin Sandu
 *    @since TOPLink/Java 1.0
 */
public class ExpressionSQLPrinter {

    /**
     * Stores the current session. The session accessor
     * is used to print all the primitive types.
     */
    protected AbstractSession session;

    /**
     * Stores the call being created.
     */
    protected SQLCall call;

    /**
     * Stores the row. Used to print PARAMETER nodes.
     */
    protected AbstractRecord translationRow;

    /**
     * Indicates whether fully qualified field names
     * (owner + table) should be used or not.
     */
    protected boolean shouldPrintQualifiedNames;

    // What we write on
    protected Writer writer;

    /** Used for distincts in functions. */
    protected boolean requiresDistinct;

    // Used in figuring out when to print a comma in the select line
    protected boolean isFirstElementPrinted;

    public ExpressionSQLPrinter(AbstractSession session, AbstractRecord translationRow, SQLCall call, boolean printQualifiedNames) {
        this.session = session;
        this.translationRow = translationRow;
        this.call = call;
        this.shouldPrintQualifiedNames = printQualifiedNames;
        this.requiresDistinct = false;
        isFirstElementPrinted = false;
    }

    /**
     * Return the call.
     */
    protected SQLCall getCall() {
        return call;
    }

    /**
     * INTERNAL:
     * Return the database platform specific information.
     */
    public DatabasePlatform getPlatform() {
        return session.getPlatform();
    }

    protected AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Return the row for translation
     */
    protected AbstractRecord getTranslationRow() {
        return translationRow;
    }

    public Writer getWriter() {
        return writer;
    }

    /**
     * INTERNAL:
     * Used in figuring out when to print a comma in the select clause
     */
    public boolean isFirstElementPrinted() {
        return isFirstElementPrinted;
    }

    public void printExpression(Expression expression) {
        translateExpression(expression);
    }

    public void printField(DatabaseField field) {
        if (field == null) {
            return;
        }

        try {
            // Print the field using either short or long notation i.e. owner + table name.
            if (shouldPrintQualifiedNames()) {
                getWriter().write(field.getQualifiedName());
            } else {
                getWriter().write(field.getName());
            }
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    public void printParameter(ParameterExpression expression) {
        try {
            getCall().appendTranslationParameter(getWriter(), expression, getPlatform());

        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    public void printParameter(DatabaseField field) {
        getCall().appendTranslation(getWriter(), field);
    }

    public void printPrimitive(Object value) {
        if (value instanceof Vector) {
            printValuelist((Vector)value);
            return;
        }

        session.getPlatform().appendLiteralToCall(getCall(), getWriter(), value);
    }

    public void printNull(ConstantExpression nullValueExpression) {
        if(session.getPlatform().shouldBindLiterals()) {
            DatabaseField field = null;
            Expression localBase = nullValueExpression.getLocalBase();
            if(localBase.isFieldExpression()) {
                field = ((FieldExpression)localBase).getField();
            } else if(localBase.isQueryKeyExpression()) {
                field = ((QueryKeyExpression)localBase).getField();
            }
            session.getPlatform().appendLiteralToCall(getCall(), getWriter(), field);
        } else {
            session.getPlatform().appendLiteralToCall(getCall(), getWriter(), null);
        }
    }
    
    public void printString(String value) {
        try {
            getWriter().write(value);

        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    public void printValuelist(Vector values) {
        try {
            Enumeration valuesEnum = values.elements();
            while (valuesEnum.hasMoreElements()) {
                Object value = valuesEnum.nextElement();
                session.getPlatform().appendLiteralToCall(getCall(), getWriter(), value);
                if (valuesEnum.hasMoreElements()) {
                    getWriter().write(", ");
                }
            }
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is required for batch reading.
     */
    public boolean requiresDistinct() {
        return requiresDistinct;
    }

    protected void setCall(SQLCall call) {
        this.call = call;
    }

    /**
     * INTERNAL:
     * Used in figuring out when to print a comma in the select clause
     */
    public void setIsFirstElementPrinted(boolean isFirstElementPrinted) {
        this.isFirstElementPrinted = isFirstElementPrinted;
    }

    /**
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is required for batch reading.
     */
    public void setRequiresDistinct(boolean requiresDistinct) {
        this.requiresDistinct = requiresDistinct;
    }

    protected void setSession(AbstractSession theSession) {
        session = theSession;
    }

    protected void setShouldPrintQualifiedNames(boolean shouldPrintQualifiedNames) {
        this.shouldPrintQualifiedNames = shouldPrintQualifiedNames;
    }

    /**
     * INTERNAL:
     * Set the row for translation
     */
    protected void setTranslationRow(AbstractRecord theRow) {
        translationRow = theRow;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public boolean shouldPrintParameterValues() {
        return getTranslationRow() != null;
    }

    protected boolean shouldPrintQualifiedNames() {
        return shouldPrintQualifiedNames;
    }

    /**
     * Translate an expression i.e. call the appropriate
     * translation method for the expression based on its
     * type. The translation method is then responsible
     * for translating the subexpressions.
     */
    protected void translateExpression(Expression theExpression) {
        theExpression.printSQL(this);
    }
}
