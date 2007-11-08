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
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * Superclass for all expression that have a context.
 * i.e. a base expression.
 */
public abstract class DataExpression extends Expression {
    protected Vector derivedTables;
    protected Vector derivedFields;
    protected Expression baseExpression;
    protected boolean hasBeenNormalized = false;
    protected TableAliasLookup tableAliases;

    /**
     * DataExpression constructor comment.
     */
    public DataExpression() {
        super();
    }

    public void addDerivedField(Expression addThis) {
        if (derivedFields == null) {
            derivedFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(4);
        }
        derivedFields.addElement(addThis);
    }

    public void addDerivedTable(Expression addThis) {
        if (derivedTables == null) {
            derivedTables = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(4);
        }
        derivedTables.addElement(addThis);
    }

    /**
     * INTERNAL:
     * Find the alias for a given table
     */
    public DatabaseTable aliasForTable(DatabaseTable table) {
        if (tableAliases == null) {
            if (getBaseExpression() == null) {
                return null;
            }
            return getBaseExpression().aliasForTable(table);
        }

        return tableAliases.keyAtValue(table);
    }

    /**
     * INTERNAL:
     * Alias a particular table within this node
     */
    protected void assignAlias(String name, DatabaseTable table) {
        assignAlias(new DatabaseTable(name), table);
    }

    /**
     * INTERNAL:
     * Alias a particular table within this node
     */
    protected void assignAlias(DatabaseTable alias, DatabaseTable table) {
        if (tableAliases == null) {
            tableAliases = new TableAliasLookup();
        }
        tableAliases.put(alias, table);
    }

    /**
     * INTERNAL:
     */
    public void clearAliases() {
        tableAliases = null;
    }

    public Vector copyCollection(Vector in, Dictionary alreadyDone) {
        if (in == null) {
            return null;
        }
        Vector result = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(in.size());
        for (Enumeration e = in.elements(); e.hasMoreElements();) {
            Expression exp = (Expression)e.nextElement();
            result.addElement(exp.copiedVersionFrom(alreadyDone));
        }
        return result;
    }

    /**
     * INTERNAL:
     */
    public Expression existingDerivedField(DatabaseField field) {
        if (derivedFields == null) {
            return null;
        }
        for (Enumeration e = derivedFields.elements(); e.hasMoreElements();) {
            FieldExpression exp = (FieldExpression)e.nextElement();
            if (exp.getField().equals(field)) {
                return exp;
            }
        }
        return null;

    }

    /**
     * INTERNAL:
     */
    public Expression existingDerivedTable(DatabaseTable table) {
        if (derivedTables == null) {
            return null;
        }
        for (Enumeration e = derivedTables.elements(); e.hasMoreElements();) {
            TableExpression exp = (TableExpression)e.nextElement();
            if (exp.getTable().equals(table)) {
                return exp;
            }
        }
        return null;

    }

    /**
     * INTERNAL:
     * Return the field appropriately aliased
     */
    public DatabaseField getAliasedField() {
        return null;

    }

    public Expression getBaseExpression() {
        return baseExpression;
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        if (getBaseExpression() == null) {
            return null;
        }
        return getBaseExpression().getBuilder();
    }

    public ClassDescriptor getDescriptor() {
        return null;

    }

    /**
     * INTERNAL:
     */
    public DatabaseField getField() {
        return null;
    }

    public Expression getField(String fieldName) {
        DatabaseField field = new DatabaseField(fieldName);
        return getField(field);

    }

    public Expression getField(DatabaseField field) {
        Expression existing = existingDerivedField(field);
        if (existing != null) {
            return existing;
        }
        return newDerivedField(field);

    }

    public DatabaseMapping getMapping() {
        if (getBaseExpression() == null) {
            return null;
        }
        ClassDescriptor aDescriptor = ((DataExpression)getBaseExpression()).getDescriptor();
        if (aDescriptor == null) {
            return null;
        }
        return aDescriptor.getObjectBuilder().getMappingForAttributeName(getName());
    }

    /**
     * INTERNAL:
     */
    public Vector getOwnedTables() {
        if (getDescriptor() == null) {
            return null;
        } else {
            if (getDescriptor().isAggregateDescriptor()) {
                return null;
            } else {
                return getDescriptor().getTables();
            }
        }
    }

    public QueryKey getQueryKeyOrNull() {
        return null;

    }

    public Expression getTable(String tableName) {
        DatabaseTable table = new DatabaseTable(tableName);
        return getTable(table);
    }

    public Expression getTable(DatabaseTable table) {
        Expression existing = existingDerivedTable(table);
        if (existing != null) {
            return existing;
        }
        return newDerivedTable(table);

    }

    /**
     * INTERNAL:
     * Return the aliases used.  For CR#2456 must never lazily initialize as also used for Expression identity.
     */
    public TableAliasLookup getTableAliases() {
        return tableAliases;

    }

    /**
     * INTERNAL:
     * Did the normalizer already add additional joins to the where clause due to
     * this query key representing a foreign reference mapping?
     * This insures that join criteria (for any query key expression) is not
     * added twice.
     * <p>
     * New meaning: DataExpressions are often iterated on multiple times during
     * normalize, but Function/Relation expressions only once.  Adding a has
     * been normalized flag improves performance and is required in some
     * applications, such as have temporal query criteria been added.
     */
    public boolean hasBeenNormalized() {
        return hasBeenNormalized;
    }

    public boolean hasBeenAliased() {
        return ((tableAliases != null) && (tableAliases.size() != 0));

    }

    protected boolean hasDerivedFields() {
        return derivedFields != null;
    }

    protected boolean hasDerivedTables() {
        return derivedTables != null;
    }

    /**
     * INTERNAL:
     */
    public boolean isAttribute() {
        return false;
    }

    public boolean isDataExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * For iterating using an inner class
     */
    public void iterateOn(ExpressionIterator iterator) {
        super.iterateOn(iterator);
        if (baseExpression != null) {
            baseExpression.iterateOn(iterator);
        }
    }

    public Expression mappingCriteria() {
        return null;
    }

    /**
     * INTERNAL:
     */
    public Expression newDerivedField(DatabaseField field) {
        FieldExpression result = new FieldExpression(field, this);
        addDerivedField(result);
        return result;

    }

    /**
     * INTERNAL:
     */
    public Expression newDerivedTable(DatabaseTable table) {
        TableExpression result = new TableExpression(table);
        result.setBaseExpression(this);
        addDerivedTable(result);
        return result;

    }

    /**
     * INTERNAL:
     * Normalize the expression into a printable structure.
     * Any joins must be added to form a new root.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        if (getBaseExpression() != null) {
            // First normalize the base.
            setBaseExpression(getBaseExpression().normalize(normalizer));
        }

        return super.normalize(normalizer);
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        clearAliases();
        if (baseExpression != null) {
            baseExpression = baseExpression.copiedVersionFrom(alreadyDone);
        }
        derivedFields = copyCollection(derivedFields, alreadyDone);
        derivedTables = copyCollection(derivedTables, alreadyDone);
    }

    /**
     * INTERNAL:
     * Print SQL onto the stream, using the ExpressionPrinter for context
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        printer.printField(getAliasedField());
    }

    protected void setBaseExpression(Expression e) {
        baseExpression = e;
    }

    public void setHasBeenNormalized(boolean value) {
        hasBeenNormalized = value;
    }

    /**
     * INTERNAL:
     * For CR#2456, Table identity involves having two tables sharing the same
     * aliasing table.
     */
    public void setTableAliases(TableAliasLookup tableAliases) {
        if (this.tableAliases == null) {
            this.tableAliases = tableAliases;
        }
    }

    public String tableAliasesDescription() {
        if (tableAliases == null) {
            return "";
        }
        return tableAliases.toString();
    }

    /**
     * Print the base for debuggin purposes.
     */
    public void writeSubexpressionsTo(BufferedWriter writer, int indent) throws IOException {
        if (getBaseExpression() != null) {
            getBaseExpression().toString(writer, indent);
        }
    }
}
