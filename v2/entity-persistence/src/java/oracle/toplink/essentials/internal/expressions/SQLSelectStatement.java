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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.platform.database.DB2MainframePlatform;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: Print SELECT statement.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print SELECT statement.
 * </ul>
 *    @author Dorin Sandu
 *    @since TOPLink/Java 1.0
 */
public class SQLSelectStatement extends SQLStatement {

    /** Fields being selected (can include expressions). */
    protected Vector fields;
    
    /** Fields not being selected (can include expressions). */
    protected Vector nonSelectFields;

    /** Tables being selected from. */
    protected Vector tables;

    /** Used for "Select Distinct" option. */
    protected short distinctState;

    /** Order by clause for read all queries. */
    protected Vector orderByExpressions;

    /** Group by clause for report queries. */
    protected Vector groupByExpressions;

    /** Having clause for report queries. */
    protected Expression havingExpression;

    /** Used for pessimistic locking ie. "For Update". */
    protected ForUpdateClause forUpdateClause;

    /** Used for report query or counts so we know how to treat distincts. */
    protected boolean isAggregateSelect;

    /** Used for DB2 style from clause outer joins. */
    protected Vector outerJoinedExpressions;
    protected Vector outerJoinedMappingCriteria;
    protected Vector outerJoinedAdditionalJoinCriteria;
    // used in case no corresponding outerJoinExpression is provided - 
    // only multitable inheritance should be outer joined
    protected List descriptorsForMultitableInheritanceOnly;

    /** Used for Oracle Hierarchical Queries */
    protected Expression startWithExpression;
    protected Expression connectByExpression;
    protected Vector orderSiblingsByExpressions;

    /** Variables used for aliasing and normalizing. */
    protected boolean requiresAliases;
    protected Hashtable tableAliases;
    protected DatabaseTable lastTable;
    protected DatabaseTable currentAlias;
    protected int currentAliasNumber;

    /** Used for subselects. */
    protected SQLSelectStatement parentStatement;

    public SQLSelectStatement() {
        this.fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        this.tables = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(4);
        this.requiresAliases = false;
        this.isAggregateSelect = false;
        this.distinctState = ObjectLevelReadQuery.UNCOMPUTED_DISTINCT;
        this.currentAliasNumber = 0;
    }

    public void addField(DatabaseField field) {
        getFields().addElement(field);
    }

    /**
     * INTERNAL:  adds an expression to the fields.  set a flag if the expression
     * is for and aggregate function.
     */
    public void addField(Expression expression) {
        if (expression instanceof FunctionExpression) {
            if (((FunctionExpression)expression).getOperator().isAggregateOperator()) {
                setIsAggregateSelect(true);
            }
        }

        getFields().addElement(expression);
    }

    /**
     * When distinct is used with order by the ordered fields must be in the select clause.
     */
    protected void addOrderByExpressionToSelectForDistinct() {
        for (Enumeration orderExpressionsEnum = getOrderByExpressions().elements();
                 orderExpressionsEnum.hasMoreElements();) {
            Expression orderExpression = (Expression)orderExpressionsEnum.nextElement();
            Expression fieldExpression = null;

            if (orderExpression.isFunctionExpression() && (orderExpression.getOperator().isOrderOperator())) {
                fieldExpression = ((FunctionExpression)orderExpression).getBaseExpression();
            } else {
                fieldExpression = orderExpression;
            }

            // Changed to call a method to loop throught the fields vector and check each element
            // individually. Jon D. May 4, 2000 for pr 7811
            if ((fieldExpression.selectIfOrderedBy()) && !fieldsContainField(getFields(), fieldExpression)) {
                addField(fieldExpression);
            }
        }
    }

    /**
     * Add a table to the statement. The table will
     * be used in the FROM part of the SQL statement.
     */
    public void addTable(DatabaseTable table) {
        if (!getTables().contains(table)) {
            getTables().addElement(table);
        }
    }

    /**
     * ADVANCED:
     * If a platform is Informix, then the outer join must be in the FROM clause.
     * This is used internally by TopLink for building Informix outer join syntax which differs from
     * other platforms(Oracle,Sybase) that print the outer join in the WHERE clause and from DB2 which prints
     * the.
     * OuterJoinedAliases passed in to keep track of tables used for outer join so no normal join is given
     */
    public void appendFromClauseForInformixOuterJoin(ExpressionSQLPrinter printer, Vector outerJoinedAliases) throws IOException {
        Writer writer = printer.getWriter();
        AbstractSession session = printer.getSession();

        // Print outer joins
        boolean firstTable = true;

        for (int index = 0; index < getOuterJoinExpressions().size(); index++) {
            QueryKeyExpression outerExpression = (QueryKeyExpression)getOuterJoinExpressions().elementAt(index);
            CompoundExpression relationExpression = (CompoundExpression)getOuterJoinedMappingCriteria().elementAt(index);// get expression for multiple table case

            // CR#3083929 direct collection/map mappings do not have reference descriptor.
            DatabaseTable targetTable = null;
            if (outerExpression.getMapping().isDirectCollectionMapping()) {
                targetTable = ((DirectCollectionMapping)outerExpression.getMapping()).getReferenceTable();
            } else {
                targetTable = (DatabaseTable)outerExpression.getMapping().getReferenceDescriptor().getTables().firstElement();
            }

            // Grab the source table from the mapping not just the first table 
            // from the descriptor. In an joined inheritance hierarchy, the
            // fk used in the outer join may be from a subclasses's table .
            DatabaseTable sourceTable;
            if (outerExpression.getMapping().isObjectReferenceMapping() && ((ObjectReferenceMapping) outerExpression.getMapping()).isForeignKeyRelationship()) {
                sourceTable = (DatabaseTable)((DatabaseField) outerExpression.getMapping().getFields().firstElement()).getTable();
            } else {
                sourceTable = (DatabaseTable)((ObjectExpression)outerExpression.getBaseExpression()).getDescriptor().getTables().firstElement();    
            }

            DatabaseTable sourceAlias = outerExpression.getBaseExpression().aliasForTable(sourceTable);
            DatabaseTable targetAlias = outerExpression.aliasForTable(targetTable);

            if (!(outerJoinedAliases.contains(sourceAlias) || outerJoinedAliases.contains(targetAlias))) {
                if (!firstTable) {
                    writer.write(", ");
                }

                firstTable = false;
                writer.write(sourceTable.getQualifiedName());
                outerJoinedAliases.addElement(sourceAlias);
                writer.write(" ");
                writer.write(sourceAlias.getQualifiedName());

                if (outerExpression.getMapping().isManyToManyMapping()) {// for many to many mappings, you need to do some funky stuff to get the relation table's alias
                    DatabaseTable newTarget = ((ManyToManyMapping)outerExpression.getMapping()).getRelationTable();
                    DatabaseTable newAlias = relationExpression.aliasForTable(newTarget);
                    writer.write(", OUTER ");// need to outer join only to relation table for many-to-many case in Informix
                    writer.write(newTarget.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(newAlias);
                    writer.write(newAlias.getQualifiedName());
                } else if (outerExpression.getMapping().isDirectCollectionMapping()) {// for many to many mappings, you need to do some funky stuff to get the relation table's alias
                    DatabaseTable newTarget = ((DirectCollectionMapping)outerExpression.getMapping()).getReferenceTable();
                    DatabaseTable newAlias = relationExpression.aliasForTable(newTarget);
                    writer.write(", OUTER ");
                    writer.write(newTarget.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(newAlias);
                    writer.write(newAlias.getQualifiedName());
                } else {// do normal outer stuff for Informix
                    for (Enumeration target = outerExpression.getMapping().getReferenceDescriptor().getTables().elements();
                             target.hasMoreElements();) {
                        DatabaseTable newTarget = (DatabaseTable)target.nextElement();
                        Expression onExpression = outerExpression;
                        DatabaseTable newAlias = outerExpression.aliasForTable(newTarget);
                        writer.write(", OUTER ");
                        writer.write(newTarget.getQualifiedName());
                        writer.write(" ");
                        outerJoinedAliases.addElement(newAlias);
                        writer.write(newAlias.getQualifiedName());
                    }
                }
            }
        }
    }

    /**
     * ADVANCED:
     * If a platform is DB2 or MySQL, then the outer join must be in the FROM clause.
     * This is used internally by TopLink for building DB2 outer join syntax which differs from
     * other platforms(Oracle,Sybase) that print the outer join in the WHERE clause.
     * OuterJoinedAliases passed in to keep track of tables used for outer join so no normal join is given
     */
    public void appendFromClauseForOuterJoin(ExpressionSQLPrinter printer, Vector outerJoinedAliases) throws IOException {
        Writer writer = printer.getWriter();
        AbstractSession session = printer.getSession();

        // Print outer joins
        boolean firstTable = true;
        boolean requiresEscape = false;//checks if the jdbc closing escape syntax is needed

        OuterJoinExpressionHolders outerJoinExpressionHolders = new OuterJoinExpressionHolders();
        for (int index = 0; index < getOuterJoinExpressions().size(); index++) {
            QueryKeyExpression outerExpression = (QueryKeyExpression)getOuterJoinExpressions().elementAt(index);

            // CR#3083929 direct collection/map mappings do not have reference descriptor.
            DatabaseTable targetTable = null;
            DatabaseTable sourceTable = null;
            DatabaseTable sourceAlias = null;
            DatabaseTable targetAlias = null;
            if(outerExpression != null) {
                if (outerExpression.getMapping().isDirectCollectionMapping()) {
                    targetTable = ((DirectCollectionMapping)outerExpression.getMapping()).getReferenceTable();
                } else {
                    targetTable = (DatabaseTable)outerExpression.getMapping().getReferenceDescriptor().getTables().firstElement();
                }
                
                // Grab the source table from the mapping not just the first table 
                // from the descriptor. In an joined inheritance hierarchy, the
                // fk used in the outer join may be from a subclasses's table.
                if (outerExpression.getMapping().isObjectReferenceMapping() && ((ObjectReferenceMapping) outerExpression.getMapping()).isForeignKeyRelationship()) {
                    sourceTable = (DatabaseTable)((DatabaseField) outerExpression.getMapping().getFields().firstElement()).getTable();
                } else {
                    sourceTable = (DatabaseTable)((ObjectExpression)outerExpression.getBaseExpression()).getDescriptor().getTables().firstElement();    
                }
                
                sourceAlias = outerExpression.getBaseExpression().aliasForTable(sourceTable);
                targetAlias = outerExpression.aliasForTable(targetTable);
            } else {
                sourceTable = (DatabaseTable)((ClassDescriptor)getDescriptorsForMultitableInheritanceOnly().get(index)).getTables().firstElement();
                targetTable = (DatabaseTable)((ClassDescriptor)getDescriptorsForMultitableInheritanceOnly().get(index)).getInheritancePolicy().getChildrenTables().get(0);
                Expression exp = (Expression)((Map)getOuterJoinedAdditionalJoinCriteria().elementAt(index)).get(targetTable);
                sourceAlias = exp.aliasForTable(sourceTable);
                targetAlias = exp.aliasForTable(targetTable);
            }

            outerJoinExpressionHolders.add(
                new OuterJoinExpressionHolder(outerExpression, index, targetTable, 
                                              sourceTable, targetAlias, sourceAlias));
        }
        
        for (Iterator i = outerJoinExpressionHolders.linearize().iterator(); i.hasNext();) {
            OuterJoinExpressionHolder holder = (OuterJoinExpressionHolder)i.next();
            QueryKeyExpression outerExpression = holder.joinExpression;
            int index = holder.index;
            DatabaseTable targetTable = holder.targetTable;
            DatabaseTable sourceTable = holder.sourceTable;
            DatabaseTable sourceAlias = holder.sourceAlias;
            DatabaseTable targetAlias = holder.targetAlias;

            if (!outerJoinedAliases.contains(targetAlias)) {
                if (!outerJoinedAliases.contains(sourceAlias)) {
                    if (requiresEscape && session.getPlatform().shouldUseJDBCOuterJoinSyntax()) {
                        writer.write("}");
                    }

                    if (!firstTable) {
                        writer.write(",");
                    }

                    if (session.getPlatform().shouldUseJDBCOuterJoinSyntax()) {
                        writer.write(session.getPlatform().getJDBCOuterJoinString());
                    }

                    requiresEscape = true;
                    firstTable = false;
                    writer.write(sourceTable.getQualifiedName());
                    outerJoinedAliases.addElement(sourceAlias);
                    writer.write(" ");
                    writer.write(sourceAlias.getQualifiedName());
                }

                if(outerExpression == null) {
                    printAdditionalJoins(printer, outerJoinedAliases, (ClassDescriptor)getDescriptorsForMultitableInheritanceOnly().get(index), (Map)getOuterJoinedAdditionalJoinCriteria().elementAt(index));
                } else if (outerExpression.getMapping().isDirectCollectionMapping()) {
                    // Append the join clause,
                    // If this is a direct collection, join to direct table.
                    Expression onExpression = (Expression)getOuterJoinedMappingCriteria().elementAt(index);

                    DatabaseTable newAlias = onExpression.aliasForTable(targetTable);
                    writer.write(" LEFT OUTER JOIN ");
                    writer.write(targetTable.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(newAlias);
                    writer.write(newAlias.getQualifiedName());
                    writer.write(" ON ");

                    if (session.getPlatform() instanceof DB2MainframePlatform) {
                        ((RelationExpression)onExpression).printSQLNoParens(printer);
                    } else {
                        onExpression.printSQL(printer);
                    }

                    //Bug#4240751 Treat ManyToManyMapping separately for out join
                } else if (outerExpression.getMapping().isManyToManyMapping()) {
                    // Must outerjoin each of the targets tables.
                    // The first table is joined with the mapping join criteria,
                    // the rest of the tables are joined with the additional join criteria.
                    // For example: EMPLOYEE t1 LEFT OUTER JOIN (PROJ_EMP t3 LEFT OUTER JOIN PROJECT t0 ON (t0.PROJ_ID = t3.PROJ_ID)) ON (t3.EMP_ID = t1.EMP_ID)
                    DatabaseTable relationTable = ((ManyToManyMapping)outerExpression.getMapping()).getRelationTable();
                    DatabaseTable relationAlias = ((Expression)getOuterJoinedMappingCriteria().elementAt(index)).aliasForTable(relationTable);
                    writer.write(" LEFT OUTER JOIN (");
                    
                    writer.write(relationTable.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(relationAlias);
                    writer.write(relationAlias.getQualifiedName());
                    
                    Vector tablesInOrder = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
                    // glassfish issue 2440: store aliases instead of tables
                    // in the tablesInOrder. This allows to distinguish source
                    // and target table in case of an self referencing relationship.
                    tablesInOrder.add(sourceAlias);
                    tablesInOrder.add(relationAlias);
                    tablesInOrder.add(targetAlias);
                    TreeMap indexToExpressionMap = new TreeMap();
                    mapTableIndexToExpression((Expression)getOuterJoinedMappingCriteria().elementAt(index), indexToExpressionMap, tablesInOrder);
                    Expression sourceToRelationJoin = (Expression)indexToExpressionMap.get(new Integer(1));
                    Expression relationToTargetJoin = (Expression)indexToExpressionMap.get(new Integer(2));
                    
                    writer.write(" JOIN ");
                    writer.write(targetTable.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(targetAlias);
                    writer.write(targetAlias.getQualifiedName());
                    writer.write(" ON ");
                    if (session.getPlatform() instanceof DB2MainframePlatform) {
                        ((RelationExpression)relationToTargetJoin).printSQLNoParens(printer);
                    } else {
                        relationToTargetJoin.printSQL(printer);
                    }
                    
                    Map tablesJoinExpression = (Map)getOuterJoinedAdditionalJoinCriteria().elementAt(index);
                    if(tablesJoinExpression != null && !tablesJoinExpression.isEmpty()) {
                        printAdditionalJoins(printer, outerJoinedAliases, outerExpression.getMapping().getReferenceDescriptor(), tablesJoinExpression);
                    }
                    writer.write(") ON ");
                    if (session.getPlatform() instanceof DB2MainframePlatform) {
                        ((RelationExpression)sourceToRelationJoin).printSQLNoParens(printer);
                    } else {
                        sourceToRelationJoin.printSQL(printer);
                    }
                } else {
                    // Must outerjoin each of the targets tables.
                    // The first table is joined with the mapping join criteria,
                    // the rest of the tables are joined with the additional join criteria.
                    writer.write(" LEFT OUTER JOIN ");
                    Map tablesJoinExpression = (Map)getOuterJoinedAdditionalJoinCriteria().elementAt(index);
                    boolean hasAdditionalJoinExpressions = tablesJoinExpression != null && !tablesJoinExpression.isEmpty();
                    if(hasAdditionalJoinExpressions) {
                        writer.write("(");
                    }
                    writer.write(targetTable.getQualifiedName());
                    writer.write(" ");
                    outerJoinedAliases.addElement(targetAlias);
                    writer.write(targetAlias.getQualifiedName());
                    if(hasAdditionalJoinExpressions) {
                        printAdditionalJoins(printer, outerJoinedAliases, outerExpression.getMapping().getReferenceDescriptor(), tablesJoinExpression);
                        writer.write(")");
                    }
                    writer.write(" ON ");
                    Expression sourceToTargetJoin = (Expression)getOuterJoinedMappingCriteria().elementAt(index);
                    if (session.getPlatform() instanceof DB2MainframePlatform) {
                        ((RelationExpression)sourceToTargetJoin).printSQLNoParens(printer);
                    } else {
                        sourceToTargetJoin.printSQL(printer);
                    }
                }
            }
        }

        if (requiresEscape && session.getPlatform().shouldUseJDBCOuterJoinSyntax()) {
            writer.write("}");
        }
    }

    protected void printAdditionalJoins(ExpressionSQLPrinter printer, Vector outerJoinedAliases, ClassDescriptor desc, Map tablesJoinExpressions)  throws IOException {
        Writer writer = printer.getWriter();
        AbstractSession session = printer.getSession();
        Vector descriptorTables = desc.getTables();
        int nDescriptorTables = descriptorTables.size();
        Vector tables;
        if(desc.hasInheritance()) {
            tables = desc.getInheritancePolicy().getAllTables();
        } else {
            tables = descriptorTables;
        }

        // skip main table - start with i=1
        int tablesSize = tables.size();
        for(int i=1; i < tablesSize; i++) {
            DatabaseTable table = (DatabaseTable)tables.elementAt(i);
            Expression onExpression = (Expression)tablesJoinExpressions.get(table);
            if (onExpression != null) {
                if(i < nDescriptorTables) {
                    // it's descriptor's own table
                    writer.write(" JOIN ");
                } else {
                    // it's child's table
                    writer.write(" LEFT OUTER JOIN ");
                }
                writer.write(table.getQualifiedName());
                writer.write(" ");
                DatabaseTable alias = onExpression.aliasForTable(table);
                outerJoinedAliases.addElement(alias);
                writer.write(alias.getQualifiedName());
                writer.write(" ON ");
                if (session.getPlatform() instanceof DB2MainframePlatform) {
                    ((RelationExpression)onExpression).printSQLNoParens(printer);
                } else {
                    onExpression.printSQL(printer);
                }
            }
        }
    }

    /**
     * Print the from clause.
     * This includes outer joins, these must be printed before the normal join to ensure that the source tables are not joined again.
     * Outer joins are not printed in the FROM clause on Oracle or Sybase.
     */
    public void appendFromClauseToWriter(ExpressionSQLPrinter printer) throws IOException {
        Writer writer = printer.getWriter();
        AbstractSession session = printer.getSession();
        writer.write(" FROM ");

        // Print outer joins
        boolean firstTable = true;
        Vector outerJoinedAliases = new Vector(1);// Must keep track of tables used for outer join so no normal join is given

        if (hasOuterJoinExpressions()) {
            if (session.getPlatform().isInformixOuterJoin()) {
                appendFromClauseForInformixOuterJoin(printer, outerJoinedAliases);
            } else if (!session.getPlatform().shouldPrintOuterJoinInWhereClause()) {
                appendFromClauseForOuterJoin(printer, outerJoinedAliases);
            }

            firstTable = false;
        }

        // If there are no table aliases it means the query was malformed,
        // most likely the wrong builder was used, or wrong builder on the left in a sub-query.
        if (getTableAliases().isEmpty()) {
            throw QueryException.invalidBuilderInQuery(null);// Query is set in execute.
        }

        // Print tables for normal join
        for (Enumeration aliasesEnum = getTableAliases().keys(); aliasesEnum.hasMoreElements();) {
            DatabaseTable alias = (DatabaseTable)aliasesEnum.nextElement();

            if (!outerJoinedAliases.contains(alias)) {
                DatabaseTable table = (DatabaseTable)getTableAliases().get(alias);

                if (requiresAliases()) {
                    if (!firstTable) {
                        writer.write(", ");
                    }

                    firstTable = false;
                    writer.write(table.getQualifiedName());
                    writer.write(" ");
                    writer.write(alias.getQualifiedName());
                } else {
                    writer.write(table.getQualifiedName());
                }
            }
        }
    }

    /**
     * This method will append the group by clause to the end of the
     * select statement.
     */
    public void appendGroupByClauseToWriter(ExpressionSQLPrinter printer) throws IOException {
        if (getGroupByExpressions().isEmpty()) {
            return;
        }

        printer.getWriter().write(" GROUP BY ");

        Vector newFields = new Vector();
        // to avoid printing a comma before the first field
        printer.setIsFirstElementPrinted(false);
        for (Enumeration expressionsEnum = getGroupByExpressions().elements();
                 expressionsEnum.hasMoreElements();) {
            Expression expression = (Expression)expressionsEnum.nextElement();            
            writeFieldsFromExpression(printer, expression, newFields);
        }
    }

    /**
     * This method will append the Hierarchical Query Clause to the end of the
     * select statement
     */
    public void appendHierarchicalQueryClauseToWriter(ExpressionSQLPrinter printer) throws IOException {
        Expression startWith = getStartWithExpression();
        Expression connectBy = getConnectByExpression();
        Vector orderSiblingsBy = getOrderSiblingsByExpressions();

        //Create the START WITH CLAUSE
        if (startWith != null) {
            printer.getWriter().write(" START WITH ");
            startWith.printSQL(printer);
        }

        if (connectBy != null) {
            if (!connectBy.isQueryKeyExpression()) {
                throw QueryException.illFormedExpression(connectBy);
            }

            printer.getWriter().write(" CONNECT BY ");

            DatabaseMapping mapping = ((QueryKeyExpression)connectBy).getMapping();
            ClassDescriptor descriptor = mapping.getDescriptor();

            //only works for these kinds of mappings. The data isn't hierarchical otherwise
            //Should also check that the source class and target class are the same.
            Map foreignKeys = null;

            if (mapping.isOneToManyMapping()) {
                OneToManyMapping otm = (OneToManyMapping)mapping;
                foreignKeys = otm.getTargetForeignKeyToSourceKeys();
            } else if (mapping.isOneToOneMapping()) {
                OneToOneMapping oto = (OneToOneMapping)mapping;
                foreignKeys = oto.getSourceToTargetKeyFields();
            } else if (mapping.isAggregateCollectionMapping()) {
                AggregateCollectionMapping acm = (AggregateCollectionMapping)mapping;
                foreignKeys = acm.getTargetForeignKeyToSourceKeys();
            } else {
                throw QueryException.invalidQueryKeyInExpression(connectBy);
            }

            DatabaseTable defaultTable = descriptor.getDefaultTable();
            String tableName = "";

            //determine which table name to use
            if (requiresAliases()) {
                tableName = getBuilder().aliasForTable(defaultTable).getName();
            } else {
                tableName = defaultTable.getName();
            }

            if ((foreignKeys != null) && !foreignKeys.isEmpty()) {
                //get the source and target fields. 
                Iterator sourceKeys = foreignKeys.keySet().iterator();

                //for each source field, get the target field and create the link. If there's
                //only one, use the simplest version without ugly bracets
                if (foreignKeys.size() > 1) {
                    printer.getWriter().write("((");
                }

                DatabaseField source = (DatabaseField)sourceKeys.next();
                DatabaseField target = (DatabaseField)foreignKeys.get(source);

                //OneToOneMappings -> Source in parent object goes to target in child object 
                if (mapping.isOneToOneMapping()) {
                    printer.getWriter().write("PRIOR " + tableName + "." + source.getName());
                    printer.getWriter().write(" = " + tableName + "." + target.getName());
                } else {//collections are the opposite way
                    printer.getWriter().write(tableName + "." + source.getName());
                    printer.getWriter().write(" = PRIOR " + tableName + "." + target.getName());
                }

                while (sourceKeys.hasNext()) {
                    printer.getWriter().write(") AND (");
                    source = (DatabaseField)sourceKeys.next();
                    target = (DatabaseField)foreignKeys.get(source);

                    if (mapping.isOneToOneMapping()) {
                        printer.getWriter().write("PRIOR " + tableName + "." + source.getName());
                        printer.getWriter().write(" = " + tableName + "." + target.getName());
                    } else {//collections are the opposite way
                        printer.getWriter().write(tableName + "." + source.getName());
                        printer.getWriter().write(" = PRIOR " + tableName + "." + target.getName());
                    }
                }

                if (foreignKeys.size() > 1) {
                    printer.getWriter().write("))");
                }
            }
        }

        if (orderSiblingsBy != null) {
            printer.getWriter().write(" ORDER SIBLINGS BY ");

            for (Enumeration expressionEnum = orderSiblingsBy.elements();
                     expressionEnum.hasMoreElements();) {
                Expression ex = (Expression)expressionEnum.nextElement();
                ex.printSQL(printer);

                if (expressionEnum.hasMoreElements()) {
                    printer.getWriter().write(", ");
                }
            }
        }
    }

    /**
     * This method will append the order clause to the end of the
     * select statement.
     */
    public void appendOrderClauseToWriter(ExpressionSQLPrinter printer) throws IOException {
        if (!hasOrderByExpressions()) {
            return;
        }

        printer.getWriter().write(" ORDER BY ");

        for (Enumeration expressionsEnum = getOrderByExpressions().elements();
                 expressionsEnum.hasMoreElements();) {
            Expression expression = (Expression)expressionsEnum.nextElement();
            expression.printSQL(printer);

            if (expressionsEnum.hasMoreElements()) {
                printer.getWriter().write(", ");
            }
        }
    }

    /**
     * INTERNAL: Alias the tables in all of our nodes.
     */
    public void assignAliases(Vector allExpressions) {
        // For sub-selects all statements must share aliasing information.
        // For  CR#2627019
        currentAliasNumber = getCurrentAliasNumber();

        ExpressionIterator iterator = new ExpressionIterator() {
            public void iterate(Expression each) {
                currentAliasNumber = each.assignTableAliasesStartingAt(currentAliasNumber);
            }
        };

        if (allExpressions.isEmpty()) {
            // bug 3878553 - ensure aliases are always assigned for when required .
            if ((getBuilder() != null) && requiresAliases()) {
                getBuilder().assignTableAliasesStartingAt(currentAliasNumber);
            }
        } else {
            for (Enumeration expressionEnum = allExpressions.elements();
                     expressionEnum.hasMoreElements();) {
                Expression expression = (Expression)expressionEnum.nextElement();
                iterator.iterateOn(expression);
            }
        }

        // For sub-selects update aliasing information of all statements.
        // For  CR#2627019
        setCurrentAliasNumber(currentAliasNumber);
    }

    /**
     * Print the SQL representation of the statement on a stream.
     */
    public DatabaseCall buildCall(AbstractSession session) {
        SQLCall call = new SQLCall();
        call.returnManyRows();

        Writer writer = new CharArrayWriter(200);

        ExpressionSQLPrinter printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, requiresAliases());
        printer.setWriter(writer);

        call.setFields(printSQL(printer));
        call.setSQLString(writer.toString());

        return call;
    }

    /**
     * INTERNAL:
     * This is used by cursored stream to determine if an expression used distinct as the size must account for this.
     */
    public void computeDistinct() {
        ExpressionIterator iterator = new ExpressionIterator() {
            public void iterate(Expression expression) {
                if (expression.isQueryKeyExpression() && ((QueryKeyExpression)expression).shouldQueryToManyRelationship()) {
                    // Aggregate should only use distinct as specified by the user.
                    if (!isDistinctComputed()) {
                        useDistinct();
                    }
                }
            }
        };

        if (getWhereClause() != null) {
            iterator.iterateOn(getWhereClause());
        }
    }

    public boolean isSubSelect() {
        return (getParentStatement() != null);
    }

    /**
     * INTERNAL:
     * Computes all aliases which will appear in the FROM clause.
     */
    public void computeTables() {
        // Compute tables should never defer to computeTablesFromTables
        // This iterator will pull all the table aliases out of an expression, and
        // put them in a hashtable.
        ExpressionIterator iterator = new ExpressionIterator() {
            public void iterate(Expression each) {
                TableAliasLookup aliases = each.getTableAliases();

                if (aliases != null) {
                    // Insure that an aliased table is only added to a single
                    // FROM clause.
                    if (!aliases.haveBeenAddedToStatement()) {
                        aliases.addToHashtable((Hashtable)getResult());
                        aliases.setHaveBeenAddedToStatement(true);
                    }
                }
            }
        };

        iterator.setResult(new Hashtable(5));

        if (getWhereClause() != null) {
            iterator.iterateOn(getWhereClause());
        } else if (hasOuterJoinExpressions()) {
            Expression outerJoinCriteria = (Expression)getOuterJoinedMappingCriteria().firstElement();
            if (outerJoinCriteria != null){
                iterator.iterateOn(outerJoinCriteria);
            }
        }

        //Iterate on fields as well in that rare case where the select is not in the where clause
        for (Iterator fields = getFields().iterator(); fields.hasNext();) {
            Object field = fields.next();
            if (field instanceof Expression) {
                iterator.iterateOn((Expression)field);
            }
        }

        // Always iterator on the builder, as the where clause may not contain the builder, i.e. value=value.
        iterator.iterateOn(getBuilder());

        Hashtable allTables = (Hashtable)iterator.getResult();
        setTableAliases(allTables);

        for (Enumeration e = allTables.elements(); e.hasMoreElements();) {
            addTable((DatabaseTable)e.nextElement());
        }
    }

    /**
     * If there is no where clause, alias the tables from the tables list directly. Assume there's
     * no ambiguity
     */
    public void computeTablesFromTables() {
        Hashtable allTables = new Hashtable();

        for (int index = 0; index < getTables().size(); index++) {
            DatabaseTable next = (DatabaseTable)getTables().elementAt(index);
            DatabaseTable alias = new DatabaseTable("t" + (index));
            allTables.put(alias, next);
        }

        setTableAliases(allTables);
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void dontUseDistinct() {
        setDistinctState(ObjectLevelReadQuery.DONT_USE_DISTINCT);
    }

    /**
     * Check if the field from the field expression is already contained in the select clause of the statement.
     * This is used on order by expression when the field being ordered by must be in the select,
     * but cannot be in the select twice.
     */
    protected boolean fieldsContainField(Vector fields, Expression expression) {
        DatabaseField orderByField;

        if (expression instanceof DataExpression) {
            orderByField = ((DataExpression)expression).getField();
        } else {
            return false;
        }

        //check all fields for a match
        for (Enumeration fieldsEnum = fields.elements(); fieldsEnum.hasMoreElements();) {
            Object fieldOrExpression = fieldsEnum.nextElement();

            if (fieldOrExpression instanceof DatabaseField) {
                DatabaseField field = (DatabaseField)fieldOrExpression;
                DataExpression exp = (DataExpression)expression;

                if (field.equals(orderByField) && (exp.getBaseExpression() == getBuilder())) {
                    // found a match
                    return true;
                }
            }
            // For CR#2589.  This method was not getting the fields in the same way that
            // printSQL does (i.e. using getFields() instead of getField()).
            // The problem was that getField() on an expression builder led to a null pointer
            // exception.
            else {
                Expression exp = (Expression)fieldOrExpression;
                DatabaseTable table = orderByField.getTable();

                if (exp.getFields().contains(orderByField) && (expression.aliasForTable(table).equals(exp.aliasForTable(table)))) {
                    //found a match
                    return true;
                }
            }
        }

        // no matches
        return false;
    }

    /**
     * Gets a unique id that will be used to alias the next table.
     * For sub-selects all must use this same aliasing information, maintained
     * in the root enclosing statement.  For CR#2627019
     */
    public int getCurrentAliasNumber() {
        if (getParentStatement() != null) {
            return getParentStatement().getCurrentAliasNumber();
        } else {
            return currentAliasNumber;
        }
    }

    /**
     * INTERNAL:
     * Return all the fields
     */
    public Vector getFields() {
        return fields;
    }

    protected ForUpdateClause getForUpdateClause() {
        return forUpdateClause;
    }

    /**
     * INTERNAL:
     * Return the group bys.
     */
    public Vector getGroupByExpressions() {
        if (groupByExpressions == null) {
            groupByExpressions = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }

        return groupByExpressions;
    }

    /**
     * INTERNAL:
     * Return the having expression.
     */
    public Expression getHavingExpression() {
        return havingExpression;
    }

    /**
     * INTERNAL:
     * Return the StartWith expression
     */
    public Expression getStartWithExpression() {
        return startWithExpression;
    }

    /**
     * INTERNAL:
     * Return the CONNECT BY expression
     */
    public Expression getConnectByExpression() {
        return connectByExpression;
    }

    /**
     * INTERNAL:
     * Return the ORDER SIBLINGS BY expression
     */
    public Vector getOrderSiblingsByExpressions() {
        return orderSiblingsByExpressions;
    }

    /**
     * Return the fields we don't want to select but want to join on.
     */
    public Vector getNonSelectFields() {
        return nonSelectFields;
    }
    
    /**
     * INTERNAL:
     * Return the order expressions for the query.
     */
    public Vector getOrderByExpressions() {
        if (orderByExpressions == null) {
            orderByExpressions = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }

        return orderByExpressions;
    }

    /**
     * INTERNAL:
     * Each Vector's element is a map of tables join expressions keyed by the tables
     */
    public Vector getOuterJoinedAdditionalJoinCriteria() {
        if (outerJoinedAdditionalJoinCriteria == null) {
            outerJoinedAdditionalJoinCriteria = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }

        return outerJoinedAdditionalJoinCriteria;
    }

    public Vector getOuterJoinedMappingCriteria() {
        if (outerJoinedMappingCriteria == null) {
            outerJoinedMappingCriteria = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }

        return outerJoinedMappingCriteria;
    }

    public Vector getOuterJoinExpressions() {
        if (outerJoinedExpressions == null) {
            outerJoinedExpressions = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }

        return outerJoinedExpressions;
    }

    public List getDescriptorsForMultitableInheritanceOnly() {
        if (descriptorsForMultitableInheritanceOnly == null) {
            descriptorsForMultitableInheritanceOnly = new ArrayList(3);
        }

        return descriptorsForMultitableInheritanceOnly;
    }

    /**
     * Return the parent statement if using subselects.
     * This is used to normalize correctly with subselects.
     */
    public SQLSelectStatement getParentStatement() {
        return parentStatement;
    }

    /**
     * INTERNAL:
     * Return the aliases used.
     */
    public Hashtable getTableAliases() {
        return tableAliases;
    }

    /**
     * INTERNAL:
     * Return all the tables.
     */
    public Vector getTables() {
        return tables;
    }

    protected boolean hasAliasForTable(DatabaseTable table) {
        if (tableAliases != null) {
            return getTableAliases().containsKey(table);
        }

        return false;
    }

    public boolean hasGroupByExpressions() {
        return (groupByExpressions != null) && (!groupByExpressions.isEmpty());
    }

    public boolean hasHavingExpression() {
        return (havingExpression != null);
    }

    public boolean hasStartWithExpression() {
        return startWithExpression != null;
    }

    public boolean hasConnectByExpression() {
        return connectByExpression != null;
    }

    public boolean hasOrderSiblingsByExpressions() {
        return orderSiblingsByExpressions != null;
    }

    public boolean hasHierarchicalQueryExpressions() {
        return ((startWithExpression != null) || (connectByExpression != null) || (orderSiblingsByExpressions != null));
    }

    public boolean hasOrderByExpressions() {
        return (orderByExpressions != null) && (!orderByExpressions.isEmpty());
    }
    
    public boolean hasNonSelectFields() {
        return (nonSelectFields != null) && (!nonSelectFields.isEmpty());
    }

    public boolean hasOuterJoinedAdditionalJoinCriteria() {
        return (outerJoinedAdditionalJoinCriteria != null) && (!outerJoinedAdditionalJoinCriteria.isEmpty());
    }

    public boolean hasOuterJoinExpressions() {
        return (outerJoinedExpressions != null) && (!outerJoinedExpressions.isEmpty());
    }

    /**
     * INTERNAL:
     */
    public boolean isAggregateSelect() {
        return isAggregateSelect;
    }

    /**
     * INTERNAL:
     * return true if this query has computed its distinct value already
     */
    public boolean isDistinctComputed() {
        return distinctState != ObjectLevelReadQuery.UNCOMPUTED_DISTINCT;
    }

    /**
     * INTERNAL:
     * Normalize an expression into a printable structure.
     * i.e. merge the expression with the join expressions.
     * Also replace table names with corresponding aliases.
     * @param clonedExpressions
     */
    public final void normalize(AbstractSession session, ClassDescriptor descriptor) {
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        normalize(session, descriptor, new IdentityHashtable());
    }

    /**
     * INTERNAL:
     * Normalize an expression into a printable structure.
     * i.e. merge the expression with the join expressions.
     * Also replace table names with corresponding aliases.
     * @param clonedExpressions With 2612185 allows additional expressions
     * from multiple bases to be rebuilt on the correct cloned base.
     */
    public void normalize(AbstractSession session, ClassDescriptor descriptor, Dictionary clonedExpressions) {
        // Initialize the builder.
        if (getBuilder() == null) {
            if (getWhereClause() == null) {
                setBuilder(new ExpressionBuilder());
            } else {
                setBuilder(getWhereClause().getBuilder());
            }
        }

        ExpressionBuilder builder = getBuilder();

        // For flashback: The builder is increasingly important.  It can store
        // an AsOfClause, needs to be normalized for history, and aliased for
        // pessimistic locking to work.  Hence everything that would have
        // been applied to the where clause will be applied to the builder if
        // the former is null.  In the past though if there was no where
        // clause just threw away the builder (to get to this point we had to
        // pass it in via the vacated where clause), and neither normalized nor
        // aliased it directly.
        if (getWhereClause() == builder) {
            setWhereClause(null);
        }

        builder.setSession(session.getRootSession(null));

        // Some queries are not on objects but for data, thus no descriptor.
        if (!builder.doesNotRepresentAnObjectInTheQuery()) {
            if (descriptor != null) {
                Class queryClass = builder.getQueryClass();
                // GF 2333 Only change the descriptor class if it is not set, or if this is an inheritance query
                if ((queryClass == null) || descriptor.isChildDescriptor()) {
                    builder.setQueryClass(descriptor.getJavaClass());
                }
            }
        }

        // Compute all other expressions used other than the where clause, i.e. select, order by, group by.
        // Also must ensure that all expression use a unique builder.
        Vector allExpressions = new Vector();

        // Process select expressions.
        rebuildAndAddExpressions(getFields(), allExpressions, builder, clonedExpressions);

        // Process non-select expressions
        if (hasNonSelectFields()) {
            rebuildAndAddExpressions(getNonSelectFields(), allExpressions, builder, clonedExpressions);
        }
        
        // Process group by expressions.
        if (hasGroupByExpressions()) {
            rebuildAndAddExpressions(getGroupByExpressions(), allExpressions, builder, clonedExpressions);
        }

        if (hasHavingExpression()) {
            //rebuildAndAddExpressions(getHavingExpression(), allExpressions, builder, clonedExpressions);
            Expression expression = getHavingExpression();
            ExpressionBuilder originalBuilder = expression.getBuilder();
            if (originalBuilder != builder) {
                // For bug 2612185 avoid rebuildOn if possible as it rebuilds all on a single base.
                // i.e. Report query items could be from parallel expressions.
                if (clonedExpressions.get(originalBuilder) != null) {
                    expression = expression.copiedVersionFrom(clonedExpressions);
                } else {
                    // Possibly the expression was built with the wrong builder.
                    expression = expression.rebuildOn(builder);
                }

                setHavingExpression(expression);
            }

            allExpressions.addElement(expression);
        }

        // Process order by expressions.
        if (hasOrderByExpressions()) {
            rebuildAndAddExpressions(getOrderByExpressions(), allExpressions, builder, clonedExpressions);
        }

        // Process outer join by expressions.
        if (hasOuterJoinExpressions()) {
            rebuildAndAddExpressions(getOuterJoinedMappingCriteria(), allExpressions, builder, clonedExpressions);
            for (Iterator criterias = getOuterJoinedAdditionalJoinCriteria().iterator();
                     criterias.hasNext();) {
                rebuildAndAddExpressions((Map)criterias.next(), allExpressions, builder, clonedExpressions);
            }
        }

        //Process hierarchical query expressions.
        if (hasStartWithExpression()) {
            startWithExpression = getStartWithExpression().rebuildOn(builder);
            allExpressions.addElement(startWithExpression);
        }

        if (hasConnectByExpression()) {
            connectByExpression = getConnectByExpression().rebuildOn(builder);
        }

        if (hasOrderSiblingsByExpressions()) {
            rebuildAndAddExpressions(getOrderSiblingsByExpressions(), allExpressions, builder, clonedExpressions);
        }

        // We have to handle the cases where the where
        // clause is initially empty but might have clauses forced into it because the class
        // has multiple tables, order by forces a join, etc.  So we have to create a builder
        // and add expressions for it and the extras, but throw it away if they didn't force anything
        Expression oldRoot = getWhereClause();
        ExpressionNormalizer normalizer = new ExpressionNormalizer(this);
        normalizer.setSession(session);

        Expression newRoot = null;

        if (oldRoot != null) {
            newRoot = oldRoot.normalize(normalizer);
        }

        // CR#3166542 always ensure that the builder has been normalized,
        // there may be an expression that does not refer to the builder, i.e. value=value.
        if (descriptor != null) {
            builder.normalize(normalizer);
        }

        for (int index = 0; index < allExpressions.size(); index++) {
            Expression expression = (Expression)allExpressions.elementAt(index);
            expression.normalize(normalizer);
        }

        // Sets the where clause and AND's it with the additional Expression
        // setNormalizedWhereClause must be called to avoid the builder side-effects
        if (newRoot == null) {
            setNormalizedWhereClause(normalizer.getAdditionalExpression());
        } else {
            setNormalizedWhereClause(newRoot.and(normalizer.getAdditionalExpression()));
        }

        if (getWhereClause() != null) {
            allExpressions.addElement(getWhereClause());
        }

        // CR#3166542 always ensure that the builder has been normalized,
        // there may be an expression that does not refer to the builder, i.e. value=value.
        if (descriptor != null) {
            allExpressions.addElement(builder);
        }

        // Must also assign aliases to outer joined mapping criterias.
        if (hasOuterJoinExpressions()) {
            // Check for null on criterias.
            for (Iterator criterias = getOuterJoinedMappingCriteria().iterator();
                     criterias.hasNext();) {
                Expression criteria = (Expression)criterias.next();
                if (criteria != null) {
                    allExpressions.add(criteria);
                }
            }

            // Check for null on criterias.
            for (Iterator criterias = getOuterJoinedAdditionalJoinCriteria().iterator();
                     criterias.hasNext();) {
                Map map = (Map)criterias.next();
                if (map != null) {
                    Iterator it = map.values().iterator();
                    while(it.hasNext()) {
                        Expression criteria = (Expression)it.next();
                        if(criteria != null) {
                            allExpressions.add(criteria);
                        }
                    }
                }
            }
        }

        // Bug 2956674 Remove validate call as validation will be completed as the expression was normalized
        // Assign all table aliases.
        assignAliases(allExpressions);

        // If this is data level then the tables must be set manually.
        if (descriptor == null) {
            computeTablesFromTables();
        } else {
            computeTables();
        }

        // Now that the parent statement has been normalized, aliased, etc.,
        // normalize the subselect expressions.  For CR#4223.
        if (normalizer.encounteredSubSelectExpressions()) {
            normalizer.normalizeSubSelects(clonedExpressions);
        }

        // CR2114; If this is data level then we don't have a descriptor.
        // We don't have a target class so we must use the root platform. PWK
        // We are not fixing the informix.
        Class aClass = null;

        if (descriptor != null) {
            aClass = descriptor.getJavaClass();
        }

        // When a distinct is used the order bys must be in the select clause, so this forces them into the select.
        if ((session.getPlatform(aClass).isInformix()) || (shouldDistinctBeUsed() && hasOrderByExpressions())) {
            addOrderByExpressionToSelectForDistinct();
        }
    }

    /**
     * INTERNAL:
     * Normalize an expression mapping all of the descriptor's tables to the view.
     * This is used to allow a descriptor to read from a view, but write to tables.
     * This is used in the multiple table and subclasses read so all of the descriptor's
     * possible tables must be mapped to the view.
     */
    public void normalizeForView(AbstractSession theSession, ClassDescriptor theDescriptor, Dictionary clonedExpressions) {
        ExpressionBuilder builder;

        // bug 3878553 - alias all view selects. 
        setRequiresAliases(true);

        if (getWhereClause() != null) {
            builder = getWhereClause().getBuilder();
        } else {
            builder = new ExpressionBuilder();
            setBuilder(builder);
        }

        builder.setViewTable((DatabaseTable)getTables().firstElement());

        normalize(theSession, theDescriptor, clonedExpressions);
    }

    /**
     * Print the SQL representation of the statement on a stream.
     */
    public Vector printSQL(ExpressionSQLPrinter printer) {
        try {
            Vector selectFields = null;
            printer.setRequiresDistinct(shouldDistinctBeUsed());
            printer.printString("SELECT ");

            if (shouldDistinctBeUsed()) {
                printer.printString("DISTINCT ");
            }

            selectFields = writeFieldsIn(printer);

            appendFromClauseToWriter(printer);

            if (!(getWhereClause() == null)) {
                printer.printString(" WHERE ");
                printer.printExpression(getWhereClause());
            }

            if (hasHierarchicalQueryExpressions()) {
                appendHierarchicalQueryClauseToWriter(printer);
            }

            if (hasGroupByExpressions()) {
                appendGroupByClauseToWriter(printer);
            }
            if (hasHavingExpression()) {
                //appendHavingClauseToWriter(printer);
                printer.printString(" HAVING ");
                printer.printExpression(getHavingExpression());
            }

            if (hasOrderByExpressions()) {
                appendOrderClauseToWriter(printer);
            }

            // For pessimistic locking.
            if (getForUpdateClause() != null) {
                getForUpdateClause().printSQL(printer, this);
            }

            return selectFields;
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * Rebuild the expressions with the correct expression builder if using a different one.
     */
    public void rebuildAndAddExpressions(Vector expressions, Vector allExpressions, ExpressionBuilder primaryBuilder, Dictionary clonedExpressions) {
        for (int index = 0; index < expressions.size(); index++) {
            Object fieldOrExpression = expressions.elementAt(index);

            if (fieldOrExpression instanceof Expression) {
                Expression expression = (Expression)fieldOrExpression;
                ExpressionBuilder originalBuilder = expression.getBuilder();

                if (originalBuilder != primaryBuilder) {
                    // For bug 2612185 avoid rebuildOn if possible as it rebuilds all on a single base.
                    // i.e. Report query items could be from parallel expressions.
                    if (clonedExpressions.get(originalBuilder) != null) {
                        expression = expression.copiedVersionFrom(clonedExpressions);
                        //if there is no builder or it is a copy of the base builder then rebuild otherwise it is a parallel expression not joined
                    } 
                    if (originalBuilder.wasQueryClassSetInternally()) {
                        // Possibly the expression was built with the wrong builder.
                        expression = expression.rebuildOn(primaryBuilder);
                    }
                    expressions.setElementAt(expression, index);
                }

                allExpressions.addElement(expression);
            }
        }
    }

    /**
     * Rebuild the expressions with the correct expression builder if using a different one.
     * Exact copy of the another rebuildAndAddExpressions adopted to a Map with Expression values
     * as the first parameter (instead of Vector in the original method)
     */
    public void rebuildAndAddExpressions(Map expressions, Vector allExpressions, ExpressionBuilder primaryBuilder, Dictionary clonedExpressions) {
        Iterator it = expressions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            Object fieldOrExpression = entry.getValue();

            if (fieldOrExpression instanceof Expression) {
                Expression expression = (Expression)fieldOrExpression;
                ExpressionBuilder originalBuilder = expression.getBuilder();

                if (originalBuilder != primaryBuilder) {
                    // For bug 2612185 avoid rebuildOn if possible as it rebuilds all on a single base.
                    // i.e. Report query items could be from parallel expressions.
                    if (clonedExpressions.get(originalBuilder) != null) {
                        expression = expression.copiedVersionFrom(clonedExpressions);
                        //if there is no builder or it is a copy of the base builder then rebuild otherwise it is a parallel expression not joined
                    } 
                    if (originalBuilder.wasQueryClassSetInternally()) {
                        // Possibly the expression was built with the wrong builder.
                        expression = expression.rebuildOn(primaryBuilder);
                    }

                    entry.setValue(expression);
                }

                allExpressions.addElement(expression);
            }
        }
    }

    /**
     * INTERNAL:
     */
    public void removeField(DatabaseField field) {
        getFields().removeElement(field);
    }

    /**
     * Remove a table from the statement. The table will
     * be dropped from the FROM part of the SQL statement.
     */
    public void removeTable(DatabaseTable table) {
        getTables().removeElement(table);
    }

    /**
     * INTERNAL: Returns true if aliases are required, false otherwise.
     * If requiresAliases is set then force aliasing, this is required for object-rel.
     */
    public boolean requiresAliases() {
        if (requiresAliases || hasOuterJoinExpressions()) {
            return true;
        }

        if (tableAliases != null) {
            return getTableAliases().size() > 1;
        }

        // tableAliases is null
        return false;
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void resetDistinct() {
        setDistinctState(ObjectLevelReadQuery.UNCOMPUTED_DISTINCT);
    }

    /**
     * Sets a unique id that will be used to alias the next table.
     * For sub-selects all must use this same aliasing information, maintained
     * in the root enclosing statement.  For CR#2627019
     */
    public void setCurrentAliasNumber(int currentAliasNumber) {
        if (getParentStatement() != null) {
            getParentStatement().setCurrentAliasNumber(currentAliasNumber);
        } else {
            this.currentAliasNumber = currentAliasNumber;
        }
    }

    /**
     * Set the non select fields. The fields are used only on joining.
     */
    public void setNonSelectFields(Vector nonSelectFields) {
        this.nonSelectFields = nonSelectFields;
    }

    /**
     * Set the where clause expression.
     * This must be used during normalization as the normal setWhereClause has the side effect
     * of setting the builder, which must not occur during normalize.
     */
    public void setNormalizedWhereClause(Expression whereClause) {
        this.whereClause = whereClause;
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void setDistinctState(short distinctState) {
        this.distinctState = distinctState;
    }

    /**
     * INTERNAL:
     * Set the fields, if any are aggregate selects then record this so that the distinct is not printed through anyOfs.
     */
    public void setFields(Vector fields) {
        for (Enumeration fieldsEnum = fields.elements(); fieldsEnum.hasMoreElements();) {
            Object fieldOrExpression = fieldsEnum.nextElement();

            if (fieldOrExpression instanceof FunctionExpression) {
                if (((FunctionExpression)fieldOrExpression).getOperator().isAggregateOperator()) {
                    setIsAggregateSelect(true);

                    break;
                }
            }
        }

        this.fields = fields;
    }

    public void setGroupByExpressions(Vector expressions) {
        this.groupByExpressions = expressions;
    }

    public void setHavingExpression(Expression expressions) {
        this.havingExpression = expressions;
    }

    /**
     * INTERNAL:
     * takes the hierarchical query expression which have been set on the query and sets them here
     * used to generate the Hierarchical Query Clause in the SQL
     */
    public void setHierarchicalQueryExpressions(Expression startWith, Expression connectBy, Vector orderSiblingsExpressions) {
        this.startWithExpression = startWith;
        this.connectByExpression = connectBy;
        this.orderSiblingsByExpressions = orderSiblingsExpressions;
    }

    public void setIsAggregateSelect(boolean isAggregateSelect) {
        this.isAggregateSelect = isAggregateSelect;
    }

    protected void setForUpdateClause(ForUpdateClause clause) {
        this.forUpdateClause = clause;
    }

    public void setLockingClause(ForUpdateClause lockingClause) {
        this.forUpdateClause = lockingClause;
    }

    public void setOrderByExpressions(Vector orderByExpressions) {
        this.orderByExpressions = orderByExpressions;
    }

    public void setOuterJoinedAdditionalJoinCriteria(Vector outerJoinedAdditionalJoinCriteria) {
        this.outerJoinedAdditionalJoinCriteria = outerJoinedAdditionalJoinCriteria;
    }

    public void setOuterJoinedMappingCriteria(Vector outerJoinedMappingCriteria) {
        this.outerJoinedMappingCriteria = outerJoinedMappingCriteria;
    }

    public void setOuterJoinExpressions(Vector outerJoinedExpressions) {
        this.outerJoinedExpressions = outerJoinedExpressions;
    }

    /**
     * Set the parent statement if using subselects.
     * This is used to normalize correctly with subselects.
     */
    public void setParentStatement(SQLSelectStatement parentStatement) {
        this.parentStatement = parentStatement;
    }

    public void setRequiresAliases(boolean requiresAliases) {
        this.requiresAliases = requiresAliases;
    }

    protected void setTableAliases(Hashtable theTableAliases) {
        tableAliases = theTableAliases;
    }

    public void setTables(Vector theTables) {
        tables = theTables;
    }

    /**
     * INTERNAL:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is required for batch reading.
     */
    public boolean shouldDistinctBeUsed() {
        return distinctState == ObjectLevelReadQuery.USE_DISTINCT;
    }

    /**
     * ADVANCED:
     * If a distinct has been set the DISTINCT clause will be printed.
     * This is used internally by TopLink for batch reading but may also be
     * used directly for advanced queries or report queries.
     */
    public void useDistinct() {
        setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
    }

    /**
     * INTERNAL:
     */
    protected void writeField(ExpressionSQLPrinter printer, DatabaseField field) {
        //print ", " before each selected field except the first one
        if (printer.isFirstElementPrinted()) {
            printer.printString(", ");
        } else {
            printer.setIsFirstElementPrinted(true);
        }

        if (printer.shouldPrintQualifiedNames()) {
            if (field.getTable() != lastTable) {
                lastTable = field.getTable();
                currentAlias = getBuilder().aliasForTable(lastTable);

                // This is really for the special case where things were pre-aliased
                if (currentAlias == null) {
                    currentAlias = lastTable;
                }
            }

            printer.printString(currentAlias.getQualifiedName());
            printer.printString(".");
            printer.printString(field.getName());
        } else {
            printer.printString(field.getName());
        }
    }

    /**
     * INTERNAL:
     */
    protected void writeFieldsFromExpression(ExpressionSQLPrinter printer, Expression expression, Vector newFields) {
        expression.writeFields(printer, newFields, this);
    }

    /**
     * INTERNAL:
     */
    protected Vector writeFieldsIn(ExpressionSQLPrinter printer) {
        this.lastTable = null;

        Vector newFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();

        for (Enumeration fieldsEnum = getFields().elements(); fieldsEnum.hasMoreElements();) {
            Object next = fieldsEnum.nextElement();

            if (next instanceof Expression) {
                writeFieldsFromExpression(printer, (Expression)next, newFields);
            } else {
                writeField(printer, (DatabaseField)next);
                newFields.addElement(next);
            }
        }

        return newFields;
    }

    /**
     * INTERNAL:
     * The method searches for expressions that join two tables each in a given expression.
     * Given expression and tablesInOrder and an empty SortedMap (TreeMap with no Comparator), this method
     *   populates the map with expressions corresponding to two tables 
     *     keyed by an index (in tablesInOrder) of the table with the highest (of two) index;
     *   returns all the participating in at least one of the expressions.
     * Example:
     *   expression (joining Employee to Project through m-m mapping "projects"):
     *     (employee.emp_id = proj_emp.emp_id) and (proj_emp.proj_id = project.proj_id)
     *   tablesInOrder:
     *     employee, proj_emp, project
     *     
     *   results:
     *     map: 
     *          1 -> (employee.emp_id = proj_emp.emp_id)
     *          2 -> (proj_emp.proj_id = project.proj_id)
     *     returned SortedSet: {0, 1, 2}.
     *     
     *     Note that tablesInOrder must contain all tables used by expression
     */
    protected static SortedSet mapTableIndexToExpression(Expression expression, SortedMap map, Vector tablesInOrder) {
        // glassfish issue 2440: 
        // - Use DataExpression.getAliasedField instead of getField. This
        // allows to distinguish source and target tables in case of a self
        // referencing relationship.
        // - Removed the block handling ParameterExpressions, because it is
        // not possible to get into that method with a ParameterExpression.
        TreeSet tables = new TreeSet();
        if(expression instanceof DataExpression) {
            DataExpression de = (DataExpression)expression;
            if(de.getAliasedField() != null) {
                tables.add(new Integer(tablesInOrder.indexOf(de.getAliasedField().getTable())));
            }
        } else if(expression instanceof CompoundExpression) {
            CompoundExpression ce = (CompoundExpression)expression;
            tables.addAll(mapTableIndexToExpression(ce.getFirstChild(), map, tablesInOrder));
            tables.addAll(mapTableIndexToExpression(ce.getSecondChild(), map, tablesInOrder));
        } else if(expression instanceof FunctionExpression) {
            FunctionExpression fe = (FunctionExpression)expression;
            Iterator it = fe.getChildren().iterator();
            while(it.hasNext()) {
                tables.addAll(mapTableIndexToExpression((Expression)it.next(), map, tablesInOrder));
            }
        }
        
        if(tables.size() == 2) {
            map.put(tables.last(), expression);
        }
        
        return tables;
    }

    /**
     * INTERNAL:
     * The method searches for expressions that join two tables each in a given expression.
     * Given expression and tablesInOrder, this method
     *   returns the map with expressions corresponding to two tables 
     *     keyed by tables (from tablesInOrder) with the highest (of two) index;
     * Example:
     *   expression (joining Employee to Project through m-m mapping "projects"):
     *     (employee.emp_id = proj_emp.emp_id) and (proj_emp.proj_id = project.proj_id)
     *   tablesInOrder:
     *     employee, proj_emp, project
     *     
     *   results:
     *     returned map: 
     *          proj_emp -> (employee.emp_id = proj_emp.emp_id)
     *          project -> (proj_emp.proj_id = project.proj_id)
     *     
     *     Note that tablesInOrder must contain all tables used by expression
     */
    public static Map mapTableToExpression(Expression expression, Vector tablesInOrder) {
        TreeMap indexToExpressionMap = new TreeMap();
        mapTableIndexToExpression(expression, indexToExpressionMap, tablesInOrder);
        HashMap map = new HashMap(indexToExpressionMap.size());
        Iterator it = indexToExpressionMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            int index = ((Integer)entry.getKey()).intValue();
            map.put(tablesInOrder.elementAt(index), entry.getValue());
        }
        return map;
    }

    // Outer join support methods / classes
        
    /**
     * This class manages outer join expressions. It stores them per source
     * aliases, resolves nested joins and provides a method retuning a list of
     * outer join expressions in the correct order for code generation.
     */
    static class OuterJoinExpressionHolders {
        
        /** 
         * key: sourceAlias name 
         * value: list of OuterJoinExpressionHolder instances
         */
        Map sourceAlias2HoldersMap = new HashMap();

        /** 
         * Adds the specified outer join expression holder to the 
         * internal map. 
         */ 
        void add(OuterJoinExpressionHolder holder) {
            Object key = holder.sourceAlias.getName();
            List holders = (List)sourceAlias2HoldersMap.get(key);
            if (holders == null) {
                holders = new ArrayList();
                sourceAlias2HoldersMap.put(key, holders);
            }
            holders.add(holder);
        }
        
        /** 
         * Returns a list of OuterJoinExpressionHolder instances in the correct order.
         */
        List linearize() {
            // Handle nested joins:
            // Iterate the lists of OuterJoinExpressionHolder instances stored as
            // values in the map. For each OuterJoinExpressionHolder check whether
            // the target alias has an entry in the map. If so, this is a
            // nested join. Get the list of OuterJoinExpressionHolder instance from
            // the map and store it as nested joins in the current
            // OuterJoinExpressionHolder instance being processed.
            List remove = new ArrayList();
            for (Iterator i = sourceAlias2HoldersMap.values().iterator(); 
                 i.hasNext(); ) {
                List holders = (List)i.next();
                for (Iterator j = holders.iterator(); j.hasNext();) {
                    OuterJoinExpressionHolder holder = (OuterJoinExpressionHolder)j.next();
                    Object key = holder.targetAlias.getName();
                    List nested = (List)sourceAlias2HoldersMap.get(key);
                    if (nested != null) {
                        remove.add(key);
                        holder.nested = nested;
                    }
                }
            }

            // Remove the entries from the map that are handled as nested joins.
            for (Iterator r = remove.iterator(); r.hasNext();) {
                sourceAlias2HoldersMap.remove(r.next());
            }
        
            // Linearize the map:
            // Iterate the remaining lists of OuterJoinExpressionHolder instances
            // stored as values in the map. Add the OuterJoinExpressionHolder to the
            // result list plus its nested joins in depth first order.
            List outerJoinExprHolders = new ArrayList();
            for (Iterator i = sourceAlias2HoldersMap.values().iterator(); 
                 i.hasNext();) {
                List holders = (List)i.next();
                addHolders(outerJoinExprHolders, holders);
            }
            
            return outerJoinExprHolders;
        }

        /**
         * Add all elements from the specified holders list to the specified
         * result list. If a holder has nested join add them to the result
         * list before processing its sibling (depth first order). 
         */
        void addHolders(List result, List holders) {
            if ((holders == null) || holders.isEmpty()) {
                // nothing to be done
                return;
            }
            for (Iterator i = holders.iterator(); i.hasNext();) {
                OuterJoinExpressionHolder holder = (OuterJoinExpressionHolder)i.next();
                // add current holder
                result.add(holder);
                // add nested holders, if any
                addHolders(result, holder.nested);
            }
        }
    }
    

    /**
     * Holder class storing a QueryKeyExpression representing an outer join
     * plus some data calculated by method appendFromClauseForOuterJoin.
     */
    static class OuterJoinExpressionHolder
    {
        final QueryKeyExpression joinExpression;
        final int index;
        final DatabaseTable targetTable;
        final DatabaseTable sourceTable;
        final DatabaseTable targetAlias;
        final DatabaseTable sourceAlias;
        List nested;
        
        public OuterJoinExpressionHolder(QueryKeyExpression joinExpression, 
                                         int index,
                                         DatabaseTable targetTable, 
                                         DatabaseTable sourceTable,
                                         DatabaseTable targetAlias, 
                                         DatabaseTable sourceAlias) {
            this.joinExpression = joinExpression;
            this.index = index;
            this.targetTable = targetTable;
            this.sourceTable = sourceTable;
            this.targetAlias = targetAlias;
            this.sourceAlias = sourceAlias;
        }
    }
}
