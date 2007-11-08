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

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Field expressions represent a field of a table.
 * Their base is either a table or object expression.
 * This is used for mapping queries and to allow queries on non-mapped field or tables.
 */
public class FieldExpression extends DataExpression {
    protected DatabaseField field;
    protected transient DatabaseField aliasedField;

    /**
     * FieldExpression constructor comment.
     */
    public FieldExpression() {
        super();
    }

    /**
     * FieldExpression constructor comment.
     */
    public FieldExpression(DatabaseField newField) {
        super();
        field = newField;
    }

    /**
     * FieldExpression constructor comment.
     */
    public FieldExpression(DatabaseField newField, Expression myBase) {
        super();
        field = newField;
        baseExpression = myBase;
    }

    /**
     * INTERNAL:
     */
    public void clearAliases() {
        super.clearAliases();
        aliasedField = null;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Field";
    }

    /**
     * INTERNAL:
     * Return the field appropriately aliased
     */
    public DatabaseField getAliasedField() {
        if (aliasedField == null) {
            initializeAliasedField();
        }
        return aliasedField;

    }

    /**
     * Return the alias for our table
     */
    private DatabaseTable getAliasedTable() {
        DataExpression base = (DataExpression)getBaseExpression();
        if (!getField().hasTableName()) {
            base.getDescriptor().buildField(getField());
        }

        DatabaseTable alias = base.aliasForTable(getField().getTable());
        if (alias == null) {
            return getField().getTable();
        } else {
            return alias;
        }
    }

    /**
     * INTERNAL:
     * If there are any fields associated with this expression, return them
     */
    public DatabaseField getClonedField() {
        return (DatabaseField)getField().clone();
    }

    /**
     * INTERNAL:
     * If there are any fields associated with this expression, return them
     */
    public Vector getClonedFields() {
        Vector result = new Vector(1);
        result.addElement(getField().clone());
        return result;
    }

    /**
     * INTERNAL:
     */
    public DatabaseField getField() {
        return field;
    }

    /**
     * INTERNAL:
     * Return all the fields
     */
    public Vector getFields() {
        Vector result = new Vector(1);
        result.addElement(getField());
        return result;
    }

    /**
     * INTERNAL:
     * Alias the database field for our current environment
     */
    private void initializeAliasedField() {
        DatabaseField tempField = (DatabaseField)getField().clone();
        DatabaseTable aliasedTable = getAliasedTable();

        //  Put in a special check here so that if the aliasing does nothing we don't cache the
        // result because it's invalid. This saves us from caching premature data if e.g. debugging
        // causes us to print too early"
        //	if (aliasedTable.equals(getField().getTable())) {
        //		return;
        //	} else {
        aliasedField = tempField;
        aliasedField.setTable(aliasedTable);
        //	}
    }

    /**
     * INTERNAL:
     */
    public boolean isAttribute() {
        return true;
    }

    public boolean isFieldExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Print SQL onto the stream, using the ExpressionPrinter for context
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        printer.printField(getAliasedField());
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        getBaseExpression().printJava(printer);
        printer.printString(".getField(\"" + getField().getQualifiedName() + "\")");
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        FieldExpression expression = new FieldExpression(getField(), getBaseExpression().rebuildOn(newBase));
        expression.setSelectIfOrderedBy(selectIfOrderedBy());
        return expression;
    }

    /**
     * INTERNAL:
     * Set the field in the mapping.
     */
    public void setField(DatabaseField newField) {
        field = newField;
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        Expression twistedBase = getBaseExpression().twistedForBaseAndContext(newBase, context);
        return twistedBase.getField(getField());
    }

    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     */
    public void validateNode() {
        DataExpression base = (DataExpression)getBaseExpression();
        if (getField().getTable().hasName()) {
            Vector tables = base.getOwnedTables();
            if ((tables != null) && (!tables.contains((getField().getTable())))) {
                throw QueryException.invalidTableForFieldInExpression(getField());
            }
        }
    }

    /**
     * INTERNAL:
     * Return the value for in memory comparison.
     * This is only valid for valueable expressions.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // Joins not supported.
        if (getBuilder() != getBaseExpression()) {
            throw QueryException.cannotConformExpression();
        }

        // For bug 2780817 get the mapping directly from the object.  In EJB 2.0 
        // inheritance, each child must override mappings defined in an abstract 
        // class with its own.
        DatabaseMapping mapping = session.getDescriptor(object.getClass()).getObjectBuilder().getMappingForField(getField());
        if (mapping == null) {
            throw QueryException.cannotConformExpression();
        }

        return mapping.valueFromObject(object, getField(), session);
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(getField().toString());
    }

    /**
     * INTERNAL: called from SQLSelectStatement.writeFieldsFromExpression(...)
     */
    public void writeFields(ExpressionSQLPrinter printer, Vector newFields, SQLSelectStatement statement) {
        DatabaseField field = getField();

        if (field != null) {
            newFields.addElement(field);
            writeField(printer, field, statement);
        }
    }
}
