/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.sqlstore.sql.impl.InputParameter;
import com.sun.persistence.runtime.sqlstore.sql.impl.ListText;
import com.sun.persistence.runtime.sqlstore.sql.impl.UserInputParameter;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to SQLVisitor for SQL generation
 * @author Mitesh Meswani
 * @author Jie Leng
 */
public class SelectPlan {
    
    /** Constants for query */
    private final static String DISTINCT = "DISTINCT";
    private final static String SELECT = "SELECT";
    private final static String WHERE = "WHERE";
    private final static String FROM = "FROM";
    private final static String PARAM_MARKER = "?";
    private final static String DOT = ".";
    private final static String SPACE = " ";
    private final static String LEFT_PAREN = "(";
    private final static String RIGHT_PAREN = ")";
    private final static String SINGLE_QUOTE = "\'";
    private final static String PLUS = "+";
    private final static String MINUS = "-";
    private final static String STAR = "*";
    private final static String DIV = "/";
    private final static String AND = "AND";
    private final static String OR = "OR";
    private final static String NOT = "NOT";
    private final static String EQUAL = "=";
    private final static String NOT_EQUAL = "<>";
    private final static String LE = "<=";
    private final static String LT = "<";
    private final static String GE = ">=";
    private final static String GT = ">";
    
    /* Tables for this query */
    private QueryTables tables = new QueryTables();

    /**
     * Indicates whether distinct was specified on the query
     */
    private boolean distinct;

    /**
     * ResultElement for this query
     */
    private List<ResultElement> resultElements;

    /**
     * Parameters for this query. Each InputParameter corresponds to a '?' in
     * the query string. The constants used by user in the query are also
     * converted to parameters. For params that correspond to embedded field or
     * composite pk, there will be 'n' InputParams. One each for its primitive
     * field.
     */
    private List<InputParameter> inputParams = new ArrayList<InputParameter>();

    /**
     * Keeps track of current column index
     */
    private int columnIndex = 1;

    private RuntimeMappingModel model;

    private DBVendorType dbVendor = null;

    public SelectPlan(RuntimeMappingModel model, DBVendorType dbVendor) {
        this.model = model;
        this.dbVendor = dbVendor;

    }

    /**
     * Gets a <code>SelectExecutor</code> that can execute this query
     * @param whereClause the where clause text
     * @param orderByClause the order by  clause text
     * @param groupByClause the group  by  clause text
     * @param havingClause the having clause text This method add text for
     * select clause and from clause to prepare the final statement text
     * @return
     */
    public SelectExecutor getExecutor(StringBuffer whereClause,
            StringBuffer orderByClause, StringBuffer groupByClause,
            StringBuffer havingClause) {
        String statementText = getStatementText(
                whereClause, orderByClause, groupByClause, havingClause);
        return new SelectExecutorImpl(
                statementText, resultElements, inputParams);
    }

    /* ------------------ From Clause processing ------------------ */

    /**
     * Add range variable with given <code>navigationId</code> and
     * <code>model information</code>
     * @param idVarData Data about identification variable corresponding
     * to the range
     */
    public void addRangeVariable(IdentificationVariableData idVarData) {
        addRangeVariable(idVarData.getNavigationId(),
                idVarData.getMappingClass(), false);
    }

    /**
     * Add an alias for given navigationId. The information for this
     * navigationId can be retrieved using the given <code>aliasName</code>
     * after this method completes.
     * @param navigationId The given navigationId
     * @param aliasName The
     */
    public void addAlias(String navigationId, String aliasName) {
        tables.addAlias(navigationId, aliasName);
    }

    /* ------------------ Select Clause processing ------------------ */

    /**
     * Add columns to select clause for the given entity and prepare
     * EntityResultElement that can parse the ResultSet for the given entity
     * @param mappingClass RuntimeMappingClass of the entity
     * @param navigationId navigationId of the entity
     * @return EntityResultElement that can parse the ResultSet for the given
     *         entity
     */
    public EntityResultElement fetchEntity(RuntimeMappingClass mappingClass,
            String navigationId) {

        ListText sqlText = new ListText();
        //---Inheritance handling---/

        //---Key field handling---/
        RuntimeMappingField[] idFields = mappingClass.getPrimaryKeyMappingFields();
        StateFieldResultElement[] idResultElements = fetchStateFields(
                idFields, navigationId, sqlText);

        //---dfg state fields handling --//
        RuntimeMappingField[] dfgStateFields =
                mappingClass.getDefaultFetchGroupMappingFields();
        // Filter out relationship fields from dfg fields.
        // Need to allocate an array of exact size
        // TODO; Discuss with Michael about providing a convinience method
        // in model that just gives out dfg-primitve-state fields
        ArrayList nonRelationshipDfgStateFields =
                new ArrayList<RuntimeMappingField>(dfgStateFields.length);
        for (int i = 0; i < dfgStateFields.length; i++) {
            RuntimeMappingField field = dfgStateFields[i];
            if (field.getMappingRelationship() == null) {
                nonRelationshipDfgStateFields.add(field);
            }
        }

        StateFieldResultElement[] eagerFetchedStateFieldResultElment =
                fetchStateFields( (RuntimeMappingField[] )
                    nonRelationshipDfgStateFields.toArray(
                    new RuntimeMappingField[0]),navigationId, sqlText);

        EntityResultElement retVal = new EntityResultElement(
                mappingClass, idResultElements,
                eagerFetchedStateFieldResultElment, sqlText.toString());

        return retVal;
    }

    /**
     * Sets the ResultElements that can parse the ResultSet that corresponds to
     * this query.
     * @param resultElements ResultElements that can parse the ResultSet that
     * corresponds to this query.
     */
    public void setResultElements(List<ResultElement> resultElements) {
        this.resultElements = resultElements;
    }

    /**
     * Mark this query to fetch distinct results
     */
    public void setDistinct() {
        distinct = true;
    }
    
    /* ------------------ Where Clause processing ------------------ */

    /**
     * @param idVarData The Identification variable data for the entity
     * @param paramData The parameter data for the parameter
     * @return StrinBuffer representing the sql for the expression
     */
    public StringBuffer generateEntityToParamComparison(
            IdentificationVariableData idVarData, ParameterData paramData) {
        StringBuffer sqlText = new StringBuffer();
        InputParameter[] inputParams =
                idVarData.generateToParamComparison(this, paramData, sqlText);
        for (InputParameter inputParam : inputParams) {
            this.inputParams.add(inputParam);
        }
        return sqlText;
    }

    public StringBuffer generateEntityToEntityComparision(
            IdentificationVariableData lhs, IdentificationVariableData rhs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Gets SQL text for given fieldName of given mappingClass.
     * @param path IdentificationVariableData for path to the given fieldName
     * @param fieldName The given fieldName
     * @return SQL text for given fieldName of given mappingClass.
     */
    public StringBuffer getStateFieldSQLText(IdentificationVariableData path, 
            String fieldName) {
        String navigationId = path.getNavigationId();
        RuntimeMappingClass mappingClass = path.getMappingClass();
        RuntimeMappingField mappingField = mappingClass.getMappingField(
                fieldName);
        // The query compiler should have made sure that we do not get a field
        // that does not belong to this entity
        assert mappingField != null;
        MappingTable table = mappingField.getFirstMappingTable();
        QueryTable queryTable = tables.getTable(navigationId, table);
        // QueryTable for a field should have been added while processing the from
        // clause or Select clause.
        assert queryTable != null;

        return getColumnText(navigationId, table, getPrimaryColumn(mappingField));
    }

    /**
     * Adds a user parameter to the query.
     * @param position position of the parameter as defined by user.
     * @return The sql string representing this parameter
     */
    public StringBuffer addUserParameter(String position) {
        InputParameter inputParam = new UserInputParameter(position);
        inputParams.add(inputParam);

        return new StringBuffer(PARAM_MARKER); //NOI18N
    }

    /**
     * Gets SQL text for "mod".
     * @param lhs represents arithmetic expression
     * @param rhs represents arithmetic expression
     * @return SQL text for "mod"
     */
    public StringBuffer getModExpr(StringBuffer lhs, StringBuffer rhs) {
        return new StringBuffer(dbVendor.getMod(lhs.toString(), rhs.toString()))
;
    }

    /**
     * Gets SQL text for "abs".
     * @param expr represents arithmetic expression
     * @return SQL text for "abs"
     */
    public StringBuffer getAbsExpr(StringBuffer expr) {
        return new StringBuffer(dbVendor.getAbs(expr.toString()));
    }

    /**
     * Gets SQL text for "sqrt".
     * @param expr represents arithmetic expression
     * @return SQL text for "sqrt"
     */
    public StringBuffer getSqrtExpr(StringBuffer expr) {
        return new StringBuffer(dbVendor.getSqrt(expr.toString()));
    }

    /**
     * Gets SQL text for "concat".
     * @param lhs the given string expression
     * @param rhs the given string expression
     * @return SQL text for "concat"
     */
    public StringBuffer getConcatExpr(StringBuffer lhs, StringBuffer rhs) {
        return new StringBuffer(dbVendor.getConcat(lhs.toString(), rhs.toString(
)));
    }

    /**
     * Gets SQL text for "substring".
     * @param str the given string expression
     * @param start the numeric number expression for start position
     * @param length the numeric number expression for substring length
     * @return SQL text for "substring"
     */
    public StringBuffer getSubstringExpr(StringBuffer str, StringBuffer start,
            StringBuffer length) {
        return new StringBuffer(dbVendor.getSubstring(str.toString(),
                start.toString(), length.toString()));
    }

    /**
     * Gets SQL text for "length".
     * @param expr the given string expression
     * @return SQL text for "length"
     */
    public StringBuffer getLengthExpr(StringBuffer expr) {
        return new StringBuffer(dbVendor.getLength(expr.toString()));
    }

    /**
     * Gets SQL text for "lower".
     * @param expr the given string expression
     * @return SQL text for "lower"
     */
    public StringBuffer getLowerExpr(StringBuffer expr) {
        return new StringBuffer(dbVendor.getLower(expr.toString()));
    }

    /**
     * Gets SQL text for "upper".
     * @param expr the given string expression
     * @return SQL text for "upper"
     */
    public StringBuffer getUpperExpr(StringBuffer expr) {
        return new StringBuffer(dbVendor.getUpper(expr.toString()));
    }

    /**
     * Gets SQL text for "trim".
     * @param trimSpec represents how to trim the string
     * @param trimChar the given char to be trimed off
     * @param str the string to be trimed
     * @return SQL text for "trim"
     */
    public StringBuffer getTrimExpr(String trimSpec,
            String trimChar, StringBuffer str) {
        return new StringBuffer(dbVendor.getTrim(trimSpec,
                trimChar, str.toString()));
    }

    /**
     * Gets SQL text for "locate".
     * @param pattern the given string expression to be searched
     * @param str the given string
     * @param start the numeric number expression for start position
     * @return SQL text for "sqrt"
     */
    public StringBuffer getLocateExpr(StringBuffer pattern, StringBuffer str,
            StringBuffer start) {
        return new StringBuffer(dbVendor.getLocate(pattern.toString(),
                str.toString(), (start != null)?start.toString():null));
    }

    /**
     * Gets SQL text for null comparision.
     * @param expr the given string expression
     * @param sense if it is <code>true</code>, then "is null" sql text is
     * constructed. if it is <code>false</code>, then "is no null" sql text
     * is constructed.
     * @return SQL text for null comparision
     */
    public StringBuffer getNullExpr(StringBuffer expr, boolean sense) {
        return new StringBuffer(dbVendor.getNull(expr.toString(), sense));
    }

    /**
     * Gets SQL text for like expression.
     * @param expr represents state field
     * @param pattern represents like pattern
     * @param escape represents esacape character
     * @param sense if it is <code>true</code>, "like" sql text is constructed.
     * if it is <code>false</code>, "not like" sql text is constructed.
     * @return SQL text for like expression
     */
    public StringBuffer getLikeExpr(StringBuffer expr, StringBuffer pattern,
                StringBuffer escape, boolean sense) {
        return new StringBuffer(dbVendor.getLike(expr.toString(), pattern.toString(),
                (escape != null)?escape.toString():null, sense));
    }

    /**
     * Gets SQL text for "between".
     * @param expr the given string expression
     * @param lower the given lower bound
     * @param upper the given upper bound
     * @param sense if it is <code>true</code>, "between" sql text is constructed.
     * if it is <code>false</code>, "not between" sql text is constructed.
     * @return SQL text for "between"
     */
    public StringBuffer getBetweenExpr(StringBuffer expr,
                StringBuffer lower, StringBuffer upper, boolean sense) {
        return new StringBuffer(dbVendor.getBetween(expr.toString(), 
                lower.toString(), upper.toString(), sense));       
    }

    /**
     * Gets SQL text for "=".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for "="
     */
    public StringBuffer getEqualExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(EQUAL).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "<>".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for "<>"
     */    
    public StringBuffer getNotEqualExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(NOT_EQUAL).append(SPACE).append(rhs);
    }

    /**
     * Gets SQL text for string.
     * @param node represents string expression
     * @return quoted SQL text for string
     */
    public StringBuffer getStringExpr(String node) {
        return new StringBuffer(SINGLE_QUOTE + node + SINGLE_QUOTE);
    }
    
    /**
     * Gets SQL text for "<=".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for "<="
     */        
    public StringBuffer getLEExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(LE).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "<".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for "<"
     */         
    public StringBuffer getLTExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(LT).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for ">=".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for ">="
     */            
    public StringBuffer getGEExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(GE).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for ">".
     * @param lhs represents comparison expression
     * @param rhs represents comparison expression
     * @return SQL text for ">"
     */            
    public StringBuffer getGTExpr(StringBuffer lhs, StringBuffer rhs) {    
        return lhs.append(SPACE).append(GT).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "+".
     * @param lhs represents arithmetic expression
     * @param rhs represents arithmetic expression
     * @return SQL text for "+"
     */            
    public StringBuffer getPlusExpr(StringBuffer lhs, StringBuffer rhs) {         
        return lhs.append(SPACE).append(PLUS).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "-".
     * @param lhs represents arithmetic expression
     * @param rhs represents arithmetic expression
     * @return SQL text for "-"
     */         
    public StringBuffer getMinusExpr(StringBuffer lhs, StringBuffer rhs) {           
        return lhs.append(SPACE).append(MINUS).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "*".
     * @param lhs represents arithmetic expression
     * @param rhs represents arithmetic expression
     * @return SQL text for "*"
     */         
    public StringBuffer getStarExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(STAR).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "/".
     * @param lhs represents arithmetic expression
     * @param rhs represents arithmetic expression
     * @return SQL text for "/"
     */         
    public StringBuffer getDivExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(DIV).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "and".
     * @param lhs represents condition expression
     * @param rhs represents condition expression
     * @return SQL text for "and"
     */             
    public StringBuffer getAndExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(AND).append(SPACE).append(rhs);
    }
    
    /**
     * Gets SQL text for "or".
     * @param lhs represents condition expression
     * @param rhs represents condition expression
     * @return SQL text for "or"
     */         
    public StringBuffer getOrExpr(StringBuffer lhs, StringBuffer rhs) {
        return lhs.append(SPACE).append(OR).append(SPACE).append(rhs);
    }
    
    /* Unary arithmetic expresions */
    
    /**
     * Gets SQL text for unary plus.
     * @param expr represents arithmetic expression
     * @return SQL text for unary plus
     */         
    public StringBuffer getUnaryPlusExpr(StringBuffer expr) {
        return (new StringBuffer(PLUS)).append(SPACE).append(expr);
    }
    
    /**
     * Gets SQL text for unary minus.
     * @param expr represents arithmetic expression
     * @return SQL text for unary minus
     */         
    public StringBuffer getUnaryMinusExpr(StringBuffer expr) {
        return (new StringBuffer(MINUS)).append(SPACE).
                append(expr); 
    }
    
    /**
     * Gets SQL text for unary not.
     * @param expr represents condition expression
     * @return SQL text for unary not
     */       
    public StringBuffer getUnaryNotExpr(StringBuffer expr) {
        return (new StringBuffer(NOT)).append(SPACE).
                append(expr);
    }
    
    /**
     * Gets SQL text for single char.
     * @param node represents character expression
     * @return SQL text for single char
     */           
    public StringBuffer getSingleCharExpr(String node) {
        return new StringBuffer(SINGLE_QUOTE + node + SINGLE_QUOTE);
    }
    
    /* ------------------ Helpers for SQLVisitor  ------------------ */

    /**
     * Gets <code>IdentificationVariableData</code> for given
     * <code>entityJavaType</code> and <code>navigationId</code>
     * @param entityJavaType The java type of an entity
     * @param navigationId Navigation id of an entity
     * @return <code>IdentificationVariableData</code> for given
     *         <code>entityJavaType</code> and <code>navigationId</code>
     */
    public IdentificationVariableData getIdentificationVariableData(
            JavaType entityJavaType, String navigationId) {
        String entityType = entityJavaType.getName();
        return new IdentificationVariableData(model.getMappingClass(entityType),
                navigationId);
    }

    /**
     *
     * @param pathData
     * @param fieldName
     * @return
     */
    public AssociationVariableData getCMRVariableData(
            IdentificationVariableData pathData, String fieldName) {
        RuntimeMappingClass parentClass = pathData.getMappingClass();
        RuntimeMappingField parentField = parentClass.getMappingField(fieldName);
        JavaType parentFieldJavaType = parentField.getJDOField().getJavaField().getType();
        RuntimeMappingClass parentFieldMappingClass =
                model.getMappingClass(parentFieldJavaType.getName());
        String navigationId = pathData.getNavigationId() + DOT + fieldName;
        return new AssociationVariableData(parentFieldMappingClass, navigationId,
                parentField, pathData);
    }

    /**
     * Gets <code>ParameterData</code> for given <code>entityJavaType</code>
     * @param entityJavaType The java type of an entity
     * @param positionMarker The posistion marker of the parameter
     * @return <code>ParameterData</code> for given <code>entityJavaType</code>
     */
    public ParameterData getParameterData(JavaType entityJavaType,
            String positionMarker) {
        ParameterData retVal = new ParameterData();
        String entityType = entityJavaType.getName();
        retVal.mappingClass    = model.getMappingClass(entityType);
        retVal.positionMarker = positionMarker;
        return retVal;
    }
    
    /**
     * Gets <code>CMPVariableData</code> for a given field.
     * @param path The object which contains navigationId
     * @param fieldName The name for the field
     * @return CMPVariableData which contains mapping field and navigationId.
     */
    public CMPVariableData getCMPVariableData(
            IdentificationVariableData path, String fieldName) {
        
        String navigationId = path.getNavigationId();
        RuntimeMappingClass mappingClass = path.getMappingClass();
        RuntimeMappingField mappingField = mappingClass.getMappingField(
                fieldName);
        
        // The query compiler should have made sure that we do not get a field
        // that does not belong to this entity
        assert mappingField != null;
        
        return new CMPVariableData(mappingField, navigationId);
    }
    
    /* ------------------ Helpers for other classes  ------------------ */

    /**
     * get sql text for given <code>column</code> of given <code>table</code>
     * for given <code>navigationId</code>
     * @param navigationId the navigation id.
     * @param table the table
     * @param column the column
     * @return sql text.
     */
    public StringBuffer getColumnText(String navigationId, MappingTable table,
            ColumnElement column) {
        //Get the query table corresponding to this field
        QueryTable queryTable = tables.getTable(navigationId, table);
        StringBuffer sqlText = new StringBuffer();
        return sqlText.append(queryTable.getCorrelationId()).append('.')
                .append(column.getName().getName());
    }


    /*----------- private Helper Methods -----------*/
    /**
     * Adds a range variable to tableList
     */
    private void addRangeVariable(String navigationId,
            RuntimeMappingClass mappingClass, boolean joinTarget) {
        //add the primary table for this range variable
        MappingTable primaryTable = mappingClass.getPrimaryMappingTable();
        tables.add(navigationId, primaryTable, joinTarget);
    }

    private StateFieldResultElement[] fetchStateFields(
            RuntimeMappingField[] fields, String navigationId,
            ListText sqlText) {
        StateFieldResultElement[] resultElements = new StateFieldResultElement[fields.length];
        for (int i = 0; i < fields.length; i++) {
            RuntimeMappingField f = fields[i];
            StateFieldResultElement id = fetchStateField(
                    f, navigationId);
            resultElements[i] = id;
            sqlText.append(id.getSQLText());
        }
        return resultElements;
    }

    /** 
     * fetech result element for a state field
     * @param mappingField the given runtime mapping field
     * @param navigationId the given navigationId
     * @return StateFieldResultElement that represents result element
     * for a state field
     */
    public StateFieldResultElement fetchStateField(
            RuntimeMappingField mappingField, String navigationId) {
        MappingTable table = mappingField.getFirstMappingTable();
        StringBuffer columnText = new StringBuffer();
        //Only primary column fetched for select
        ColumnElement primaryColumn = mappingField.getColumns()[0];
        ResultColumnElement resultColumn = addColumn(
                navigationId, table, primaryColumn, columnText);
        return new StateFieldResultElement(
                resultColumn, columnText.toString(), mappingField);
    }

    /**
     * TODO: ModelIssue: Need to add a method to RuntimeMappingField to get
     * primary column Once the issue is solved, this method would go away
     */
    private static ColumnElement getPrimaryColumn(RuntimeMappingField mappingField) {
        return mappingField.getColumns()[0];
    }

    /**
     * Adds given <code>column</code> to select clause of this query for given
     * <code>navigationId<code>. columnIndex is incremented in preparation for
     * next column to be added.
     * @param navigationId the navigationId
     * @param table the table
     * @param column the column
     * @param sqlText empty StringBuffer. The sql text for added column is
     * returned in this parameter.
     * @return An IndexedResultColumnElement that remembers the index of the
     *         added column is returned. The sql text for the added column is
     *         also returned in <code>sqlText</code>
     */
    private ResultColumnElement addColumn(String navigationId,
            MappingTable table, ColumnElement column, StringBuffer sqlText) {
        assert sqlText.length() == 0; //sqlText is assumed to be empty
        sqlText.append(getColumnText(navigationId, table, column));
        return new IndexedResultColumnElement(columnIndex++);
    }

    private String getStatementText(StringBuffer whereClause,
            StringBuffer orderByClause, StringBuffer groupByClause,
            StringBuffer havingClause) {
        assert groupByClause == null : "Not used for Now"; //NOI18N
        assert havingClause == null  : "Not used for Now"; //NOI18N

        StringBuffer text = new StringBuffer();
        text.append(getSelectText()).append(SPACE).append(getFromText())
                .append(SPACE);
        if (whereClause != null) {
            text.append((StringBuffer) whereClause).append(SPACE);
        }
        if (orderByClause != null) {
            text.append((StringBuffer) orderByClause).append(SPACE);
        }

        return text.toString();
    }

    /**
     * Gets sql text for select clause. The method aggregates sqlText from all
     * the resultElements and constructs sql text for select clause
     * @return sql text for select clause
     */
    private StringBuffer getSelectText() {
        ListText columnList = new ListText(); //NOI18N
        //There should be atleast one resultElement else something is broken
        assert resultElements.size() > 0;
        for (int i = 0; i < resultElements.size(); i++) {
            ResultElement resultElement = resultElements.get(i);
            columnList.append(resultElement.getSQLText());
        }
        return new StringBuffer(SELECT).append(SPACE).
                append(distinct ? DISTINCT + SPACE : "").//NOI18N
                append(columnList.toString());
    }

    private StringBuffer getFromText() {
        return new StringBuffer(FROM).append(SPACE).append(tables.getSQLText());
    }

    static class ParameterData {
        RuntimeMappingClass mappingClass;
        String positionMarker;
    }

}
