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
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * Used for parameterized expressions, such as expression defined in mapping queries.
 */
public class ParameterExpression extends Expression {

    /** The parameter field or name. */
    protected DatabaseField field;

    /** The opposite side of the relation, this is used for conversion of the parameter using the others mapping. */
    protected Expression localBase;

    /** The base expression is what the parameter was derived from, used for nested parameters. */
    protected Expression baseExpression;

    /** The infered type of the parameter.
     * Please note that the type might not be always initialized to correct value.
     * It might be null if not initialized correctly.
     */
    Object type;

    public ParameterExpression() {
        super();
    }

    public ParameterExpression(String fieldName) {
        this(new DatabaseField(fieldName));
    }

    public ParameterExpression(DatabaseField field) {
        super();
        this.field = field;
    }

    // For bug 3107049 ParameterExpression will now be built with a
    // default localBase, same as with ConstantExpression.
    public ParameterExpression(String fieldName, Expression baseExpression,  Object type) {
        this(new DatabaseField(fieldName), baseExpression);
        this.type = type;
    }

    public ParameterExpression(DatabaseField field, Expression baseExpression) {
        super();
        this.field = field;
        localBase = baseExpression;
    }

    /**
     * Return description.
     * Used for toString.
     */
    public String basicDescription() {
        return String.valueOf(getField());
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Parameter";
    }

    /**
     * This allows for nesting of parametrized expression.
     * This is used for parameterizing object comparisons.
     */
    public Expression get(String attributeOrQueryKey) {
        ParameterExpression expression = new ParameterExpression(attributeOrQueryKey);
        expression.setBaseExpression(this);

        return expression;
    }

    /**
     * The base expression is what the parameter was derived from.
     * This is used for nested parameters.
     */
    public Expression getBaseExpression() {
        return baseExpression;
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        if (localBase == null) {
            //Bug#5097278 Need to return the builder from the base expression if nested.
            if (getBaseExpression() != null) {
                return ((ParameterExpression)getBaseExpression()).getBuilder();
            } else {
                return null;
            }
        }
        return localBase.getBuilder();
    }

    public DatabaseField getField() {
        return field;
    }

    /**
     * This allows for nesting of parametrized expression.
     * This is used for parameterizing object comparisons.
     */
    public Expression getField(DatabaseField field) {
        ParameterExpression expression = new ParameterExpression(field);
        expression.setBaseExpression(this);

        return expression;
    }

    /**
     * The opposite side of the relation, this is used for conversion of the parameter using the others mapping.
     */
    public Expression getLocalBase() {
        return localBase;
    }

    /**
     * The infered type of this parameter.
     * Please note that the type might not be always initialized to correct value.
     * It might be null if not initialized correctly
     */
    public Object getType() { return type; }

    /**
     * Extract the value from the row.
     * This may require recusion if it is a nested parameter.
     */
    public Object getValue(AbstractRecord translationRow, AbstractSession session) {
        if (getField() == null) {
            return null;
        }

        Object value = null;

        // Check for nested parameters.
        if (getBaseExpression() != null) {
            value = ((ParameterExpression)getBaseExpression()).getValue(translationRow, session);
            if (value == null) {
                return null;
            }

            ClassDescriptor descriptor = session.getDescriptor(value);
            //Bug4924639  Aggregate descriptors have to be acquired from their mapping as they are cloned and initialized by each mapping
            if (descriptor.isAggregateDescriptor() && ((ParameterExpression)getBaseExpression()).getLocalBase().isObjectExpression()) {
                descriptor = ((ObjectExpression)((ParameterExpression)getBaseExpression()).getLocalBase()).getDescriptor();
            }
            if (descriptor != null) {
                // For bug 2990493 must unwrap for EJBQL "Select Person(p) where p = ?1"
                //if we had to unwrap it make sure we replace the argument with this value
                //incase it is needed again, say in conforming.
                //bug 3750793
                value = descriptor.getObjectBuilder().unwrapObject(value, session);
                translationRow.put(((ParameterExpression)getBaseExpression()).getField(), value);

                // The local parameter is either a field or attribute of the object.
                DatabaseMapping mapping = descriptor.getObjectBuilder().getMappingForField(getField());
                if (mapping != null) {
                    value = mapping.valueFromObject(value, getField(), session);
                } else {
                    mapping = descriptor.getObjectBuilder().getMappingForAttributeName(getField().getName());
                    if (mapping != null) {
                        value = mapping.getRealAttributeValueFromObject(value, session);
                    } else {
                        DatabaseField queryKeyField = descriptor.getObjectBuilder().getFieldForQueryKeyName(getField().getName());
                        if (queryKeyField != null) {
                            mapping = descriptor.getObjectBuilder().getMappingForField(getField());
                            if (mapping != null) {
                                value = mapping.valueFromObject(value, getField(), session);
                            }
                        }
                    }
                }
            }
        } else {
            value = translationRow.getIndicatingNoEntry(getField());
            //Throw an exception if the field is not mapped
            if (value == oracle.toplink.essentials.internal.sessions.AbstractRecord.noEntry) {
                throw QueryException.parameterNameMismatch(getField().getName());
            }
        }

        // Convert the value to the correct type, i.e. object type mappings.
        if (getLocalBase() != null) {
            value = getLocalBase().getFieldValue(value);
        }

        return value;
    }

    public boolean isParameterExpression() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public boolean isValueExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        if (getLocalBase() != null) {
            setLocalBase(getLocalBase().copiedVersionFrom(alreadyDone));
        }
        if (getBaseExpression() != null) {
            setBaseExpression(getBaseExpression().copiedVersionFrom(alreadyDone));
        }
    }

    /**
     * INTERNAL:
     * Print SQL onto the stream, using the ExpressionPrinter for context
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        if (printer.shouldPrintParameterValues()) {
            Object value = getValue(printer.getTranslationRow(), printer.getSession());
            if(getField() == null) {
                printer.printPrimitive(value);
            } else {
                if (value instanceof Vector) {
                    printer.printValuelist((Vector)value);
                } else {
                    printer.printParameter(this);
                }
            }
        } else {
            if (getField() != null) {
                printer.printParameter(this);
            }
        }
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        ((DataExpression)getLocalBase()).getBaseExpression().printJava(printer);
        printer.printString(".getParameter(\"" + getField().getQualifiedName() + "\")");        
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        ParameterExpression result = (ParameterExpression)clone();
        result.setLocalBase(localBase.rebuildOn(newBase));
        return result;
    }

    /**
     * The base expression is what the parameter was derived from.
     * This is used for nested parameters.
     */
    protected void setBaseExpression(Expression baseExpression) {
        this.baseExpression = baseExpression;
    }

    /**
     * The opposite side of the relation, this is used for conversion of the parameter using the others mapping.
     */
    public void setLocalBase(Expression localBase) {
        this.localBase = localBase;
    }

    /**
     * INTERNAL:
     * Rebuild against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist,
     * (see the comment there for more details).
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        return context.getField(getField());
    }

    /**
     * INTERNAL:
     * Return the value for in memory comparison.
     * This is only valid for valueable expressions.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // Run ourselves through the translation row to find the desired value
        if (getField() != null) {
            return getValue(translationRow, session);
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(basicDescription());
    }

    /**
     * INTERNAL:
     * Append the parameter into the printer.
     * "Normal" ReadQuery never has ParameterExpression in it's select clause hence for a "normal" ReadQuery this method is never called.
     * The reason this method was added is that UpdateAllQuery (in case temporary storage is required)
     * creates a "helper" ReportQuery with ReportItem corresponding to each update expression - and update expression
     * may be a ParameterExpression. The call created by "helper" ReportQuery is never executed - 
     * it's used during construction of insert call into temporary storage.
     */
    public void writeFields(ExpressionSQLPrinter printer, Vector newFields, SQLSelectStatement statement) {
        //print ", " before each selected field except the first one
        if (printer.isFirstElementPrinted()) {
            printer.printString(", ");
        } else {
            printer.setIsFirstElementPrinted(true);
        }

        // This field is a parameter value, so any name can be used.
        newFields.addElement(new DatabaseField("*"));
        printSQL(printer);
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
