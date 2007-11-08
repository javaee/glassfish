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

import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.impl.EJBQLVisitorImpl;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;

import java.util.ArrayList;

/**
 * The visitor that walks EJBLQ tree to generate SQL. This class is responsible
 * for receiving call backs while visiting a query tree. It extracts information
 * from the parameter of call back methods and delegates the actual work to
 * instance of SelectPlan
 * @author Mitesh Meswani
 * @author Jie Leng
 */

public class SQLVisitor extends EJBQLVisitorImpl {
       
    /**
     * The delegate that constructs the actual query
     */
    private SelectPlan selectPlan;

    /**
     * Result of visiting the tree
     */
    private SelectExecutor executor;
    
    /**
     * Indicates whether we are currently in select clause set in arriveSelectCluase()
     * and reset in leaveSelectCluase()
     */
    private boolean inSelectClause;
    

    public SQLVisitor(RuntimeMappingModel model, DBVendorType dbVendor) {
        selectPlan = new SelectPlan(model, dbVendor);
    }

    /**
     * Gets the result of visiting the tree
     * @return An instance of SelectExecutor
     */
    public SelectExecutor getSelectExecutor() {
        return executor;
    }

    @Override public void leaveSelectStmt(EJBQLAST node, Object from, Object select,
            Object where, Object groupBy, Object having, Object orderBy) {
        executor = selectPlan.getExecutor(
                (StringBuffer) where, (StringBuffer) orderBy,
                (StringBuffer) groupBy, (StringBuffer) having);
    }
  
    /* ------------------ From Clause processing ------------------ */

    /**
     * For query Select .. from Department d....,
     * This method is called for expression Department d.
     * @param node  The node in query tree that corresponds to the experssion.
     * @param schemaName Name of the schema. In above example this is Department.
     * This is return value of method leaveAbstractSchemaName.
     * @param identVariable Data about identifactionVariable. In above example,
     * this is data about d.
     * @return  The data about this variable is added to selectPlan by
     * this method. This method always return null.
     */
    @Override public Object leaveRangeVarDecl(EJBQLAST node, Object schemaName,
            Object identVariable) {
        selectPlan.addRangeVariable(
                (IdentificationVariableData) identVariable);
        return null;
    }

    /**
     * For query Select .. from Department d....,
     * This method is called for expression Department.
     * @param node The node in query tree that corresponds to the experssion.
     * @return fully qualified name of the javaType of this node.
     */
    @Override public String leaveAbstractSchemaName(EJBQLAST node) {
        JavaType javaType = (JavaType) node.getTypeInfo();
        return javaType.getName();
    }

    /**
     * For query Select .. from Department d....,
     * this method is called for expression d
     * @param node The node in query tree that corresponds to the experssion.
     * @return
     */
    @Override public IdentificationVariableData
            leaveIdentificationVar(EJBQLAST node) {
        return getIdentificationVarData(node);
    }

    /* ------------------ Select Clause processing ------------------ */

    
    @Override public void arriveSelectClause(EJBQLAST node) {
        inSelectClause = true;
    }
    
    @Override public Object leaveDistinct(EJBQLAST node) {
        // If this method is called, distinct is specified on  the query.
        // Return a non null object.
        selectPlan.setDistinct();
        return null;
    }

    /**
     * This method corresponds to a projection in select clasuse
     * Thus for query Select Object(d) from .....,
     * This method corresponds to expression Object(d)
     * for query Select d.name from .....,
     * This method corresponds to expression d.name
     * @param pathExpr Data about path expression.
     * @return ResultElement capable of fetching the entity from resultset
     */
    @Override public ResultElement leaveProjection(
            Object pathExpr) {
        Fetcher fetcher = (Fetcher) pathExpr;
        return fetcher.fetch(selectPlan);
    }

    @Override public ArrayList<ResultElement> leaveSelectClause(EJBQLAST node,
            Object distinct, Object selectExpr) {
        //TODO: selectExpr should be a list. For now mock up a list to pass to selectPlan
        //Begin  Mock
        ArrayList<ResultElement> resultElements = new ArrayList<ResultElement>();
        resultElements.add((ResultElement) selectExpr);
        //End Mock
        selectPlan.setResultElements(resultElements);
        inSelectClause = false;
        return resultElements;
    }   
    
    /* ------------------ Where Clause processing ------------------ */

    /* All methods corresponding to where clause return a StringBuffer. */

    /**
     * @param node the where node
     * @param expr A StringBuffer representing where conditions
     * @return StringBuffer representing where clause for the query
     */
    @Override public StringBuffer leaveWhereClause(EJBQLAST node, Object expr) {
        // If this method is called, query has a where clause. So, expr should
        // not be null.
        assert expr != null;
        return new StringBuffer("where ").append((StringBuffer) expr); //NOI18N
    }

    /**
     * @param node
     * @param path path is IdentificationVariableData returned by
     * leavePathExprIdentificationVar
     * @param field is name of field as String returned by leaveFieldCMPField
     * @return CMPVariableData for given path and field if the method is called
     * while in selecctClause.
     * A StringBuffer containing sqlText representing this cmp field if the
     * method is called while in where Clause
     *
     */
    @Override public Object leavePathExprCMPField(EJBQLAST node,
            Object path, Object field) {
        IdentificationVariableData pathData = (IdentificationVariableData) path;
        String fieldName = (String) field;

        if(inSelectClause) {
            return selectPlan.getCMPVariableData(pathData, fieldName);
        } else {
            return selectPlan.getStateFieldSQLText(pathData, fieldName);
        }
    }

    /**
     * Process user parameters. User parameters of type PersistenceCapable are
     * treated specially.
     * @param node The node represting pararmeter in query tree
     * @return If the parameter corresponds to a PersistenceCapable type,
     * the text of the node as it is. Please note that by convention, all methods
     * processing where clause rerturn a StringBuffer. This methos deviates
     * from the convention for parameter of PersistenceCapable type. However, for
     * this type of parameter, the return value of this method will always go
     * into method leaveRelationalExpresionEntityEqual() which will always
     * return a Stringbuffer.
     * Else a StringBuffer representing parameter marker.
     */
    @Override public Object leaveParameter(EJBQLAST node) {
        // TODO: As per discussion on conf call on 05/05, it should be possible to
        // extract enough information while in this method to get hold of RunTimeMappingField
        // (That is atleast String entityType and String fieldName).
        // This information is required for embedded objects and object cocomparison
        // for objects with composite pk to construct correct sql text and parameters
        Object retVal;
        JavaType javaType = (JavaType) node.getTypeInfo();
        if(javaType.isPersistenceCapable() ) {
            // If this corresponds to an entity mapped to composite pk, this
            // parameter will translate into multiple parameter markers in the
            // generated sql. Let leaveRelationalExpresionEntityEqual handle this
            // case
            retVal = selectPlan.getParameterData(javaType, node.getText());

        } else {
            // The parameter will have exactly one parameter marker in the
            // generated sql text. Add the parameter to plan.
            retVal = selectPlan.addUserParameter(node.getText());
        }
        return retVal;
    }

    /**
     * This method is called when two entities are compared.
     * We have following combinations plus corresponding case where lhs and rhs
     * are switched possible here
     * 1.  entity = ?1
     * 2.  cmr    = ?1
     * 3.  cmr    = entity  (e.g. d.company = c )
     * @param node
     * @param lhs
     * @param rhs
     * @return StringBuffer representing the sql corresponding to sql comparision
     */
    @Override public StringBuffer leaveRelationalExprEntityEqual(EJBQLAST node,
            Object lhs, Object rhs) {
        StringBuffer retVal;
        boolean lhsIsParam = lhs instanceof SelectPlan.ParameterData;
        boolean rhsIsParam = rhs instanceof SelectPlan.ParameterData;
        if(lhsIsParam || rhsIsParam) {
            // Look for entity = ?1 expression.
            // Need to generate entity.pk1 = ? and entity.pk2 = ? ....
            if (lhsIsParam && rhsIsParam) {
                //Comparision of type entityParam1 = entityParam2.
                //TODO: Check with Dave, the query compiler should catch this
                // and throw an error. Then this can turn into an assert
                throw new UnsupportedOperationException("Expression of type "
                        + "entityParam1 = entityParam2 not supported");
            }
            // At this point it is guaranteed that only one of them is a param
            IdentificationVariableData idVarData;
            SelectPlan.ParameterData paramData;
            if(lhsIsParam) {
                paramData = (SelectPlan.ParameterData) lhs;
                idVarData = (IdentificationVariableData) rhs;
            } else {
                assert rhsIsParam;
                paramData = (SelectPlan.ParameterData) rhs;
                idVarData = (IdentificationVariableData) lhs;
            }
            retVal = selectPlan.generateEntityToParamComparison(idVarData,
                    paramData);


        } else {
            // Both the arguments are of type entity. The expression is of
            // type e.singleValuedCmrPathExpr = rangeVar
            // Need to generate
            // e.fk1 = rangeVar.pk1 and e.fk2 = rangeVar.pk2
            retVal = selectPlan.generateEntityToEntityComparision(
                    (IdentificationVariableData) lhs,
                    (IdentificationVariableData) rhs);
        }
       return retVal;
    }

    /**
     * @return StringBuffer representing the expression
     */
    @Override public StringBuffer leaveRelationalExprEqual(EJBQLAST node,
            Object lhs, Object rhs) {
        return selectPlan.getEqualExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    @Override public StringBuffer leaveRelationalExprNotEqual(EJBQLAST node,
            Object lhs, Object rhs) {
        return selectPlan.getNotEqualExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    /** arithmetic functions **/

    @Override public StringBuffer leaveMod(EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getModExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    @Override public StringBuffer leaveAbs(EJBQLAST node, Object expr) {
        return selectPlan.getAbsExpr((StringBuffer)expr);
    }

    @Override public StringBuffer leaveSqrt(EJBQLAST node, Object expr) {
        return selectPlan.getSqrtExpr((StringBuffer)expr);
    }

    /** string functions **/

    @Override public StringBuffer leaveConcat(EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getConcatExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    @Override public StringBuffer leaveSubstring(EJBQLAST node, Object str,
            Object start, Object length) {
        return selectPlan.getSubstringExpr((StringBuffer)str, (StringBuffer)start,
                (StringBuffer)length);
    }

    @Override public StringBuffer leaveLength(EJBQLAST node, Object expr) {
        return selectPlan.getLengthExpr((StringBuffer)expr);
    }

    @Override public StringBuffer leaveLower(EJBQLAST node, Object expr) {
        return selectPlan.getLowerExpr((StringBuffer)expr);
    }

    @Override public StringBuffer leaveUpper(EJBQLAST node, Object expr) {
        return selectPlan.getUpperExpr((StringBuffer)expr);
    }

    @Override public StringBuffer leaveTrim(EJBQLAST node, Object trimSpec,
            Object trimChar, Object str) {
        return selectPlan.getTrimExpr((String)trimSpec,
                (String)trimChar, (StringBuffer)str);
    }

    @Override public StringBuffer leaveLocate(EJBQLAST node, Object pattern,
            Object str, Object start) {
        return selectPlan.getLocateExpr((StringBuffer)pattern,
                (StringBuffer)str, (StringBuffer)start);
    }

    @Override public StringBuffer leaveLiteralTrue(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralFalse(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralLong(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralInt(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralFloat(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralDouble(EJBQLAST node) {
        return new StringBuffer(node.getText());
    }

    @Override public StringBuffer leaveLiteralString(EJBQLAST node) {
        return selectPlan.getStringExpr(node.getText());
    }
    
    @Override public StringBuffer leaveWildcard(EJBQLAST node) {
        return new StringBuffer (node.getText());
    }

    /* LESS, GREATER, expressions */

    @Override public StringBuffer leaveRelationalExprLE(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getLEExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    @Override public StringBuffer leaveRelationalExprLT(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getLTExpr((StringBuffer)lhs, (StringBuffer)rhs);
    }

    @Override public StringBuffer leaveRelationalExprGE(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getGEExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    @Override public StringBuffer leaveRelationalExprGT(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getGTExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

     /* Binary arithmetic expressions */

    @Override public StringBuffer leaveBinaryArithmeticExprPlus(
            EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getPlusExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    @Override public StringBuffer leaveBinaryArithmeticExprMinus(
            EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getMinusExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    @Override public StringBuffer leaveBinaryArithmeticExprStar(
            EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getStarExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    @Override public StringBuffer leaveBinaryArithmeticExprDiv(
            EJBQLAST node, Object lhs, Object rhs) {
        return selectPlan.getDivExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    /* AND, OR expressions */

    @Override public StringBuffer leaveConditionalExprAnd(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getAndExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    @Override public StringBuffer leaveConditionalExprOr(EJBQLAST node, Object lhs,
            Object rhs) {
        return selectPlan.getOrExpr((StringBuffer) lhs, (StringBuffer) rhs);
    }

    /* Unary arithmetic expresions */

    @Override public StringBuffer leaveUnaryExprPlus(EJBQLAST node, Object expr) {
        return selectPlan.getUnaryPlusExpr((StringBuffer) expr);
    }

    @Override public StringBuffer leaveUnaryExprMinus(EJBQLAST node, Object expr) {
        return selectPlan.getUnaryMinusExpr((StringBuffer) expr);
    }

    @Override public StringBuffer leaveUnaryExprNot(
            EJBQLAST node, Object expr) {
        return selectPlan.getUnaryNotExpr((StringBuffer) expr);
    }

    /* LIKE (and NOT) expression and related */

    @Override public StringBuffer leaveLikeExpr(
            EJBQLAST node,
            Object expr, Object pattern, Object escape, boolean sense) {
        return selectPlan.getLikeExpr((StringBuffer) expr,
                (StringBuffer) pattern, (StringBuffer) escape, sense);
    }

    @Override public StringBuffer leaveEscape(EJBQLAST node, Object escape) {
        return (StringBuffer) escape;
    }

    @Override public StringBuffer leaveSingleCharStringLiteral(EJBQLAST node) {
        return selectPlan.getSingleCharExpr(node.getText());
    }

    /* NULL (and not) comparison expression */

    @Override public StringBuffer leaveNullComparisonExpr(
            EJBQLAST node, Object expr, boolean sense) {
        return selectPlan.getNullExpr((StringBuffer) expr, sense);
    }

    @Override public StringBuffer leaveBetweenExpr(EJBQLAST node,
            Object expr, Object lower, Object upper, boolean sense) {
        return selectPlan.getBetweenExpr((StringBuffer) expr,
                (StringBuffer) lower, (StringBuffer) upper, sense);
    }

    /* ------------------   Generic Methods ------------------ */

    /**
     * For query Select Obect(d) from Department d where d.id = ?1 or d.name = ?2,
     * this metod is called for all occurance of expression d in where clause.
     * Please note that this method is not called for expression d in "Object(d)"
     * or for expression d in from clause
     * @return IdentificationVariableData for this node
     */
    @Override public IdentificationVariableData leavePathExprIdentificationVar(
            EJBQLAST node) {
        return getIdentificationVarData(node);
    }

    /**
     * @return name of the field as String
     */
    @Override public String leaveFieldCMPField(EJBQLAST node) {
        return node.getText();
    }

    /**
     * @return name of the field as String
     */
    @Override public Object leaveFieldCMRField(EJBQLAST node) {
        return node.getText();
    }

    /**
     * This method is called for occurance of cmr expression in where clause.
     * For query Select Obect(d) from Department d where d.company.name = ?1
     * this method is called for expression "d.company"
     * @param node The node in query tree that corresponds to the experssion.
     * @param path Data about path expression. In the above example this is
     * data about expression d This is return value of method
     * leavePathExprIdentificationVar
     * Please note that for expression of type e.department.company.name = ?1
     * value of path for cmr company will be as returned by leavePathExprCMRField
     * for expression * e.deparmtnet
     * @param field the cmr field. This is as returned by leaveFieldCMRField
     * @return
     */
    public Object leavePathExprCMRField(EJBQLAST node, Object path,
            Object field) {
        IdentificationVariableData pathData = (IdentificationVariableData) path;
        String fieldName = (String) field;
        return selectPlan.getCMRVariableData(pathData, fieldName);
    }


    /* ------------------   Helper Methods ------------------ */

    /**
     * Get Data for identification variable.
     * @param idVarNode A node that corresponds to
     * @return
     */
    private IdentificationVariableData getIdentificationVarData(
            EJBQLAST idVarNode) {
        JavaType entityJavaType = (JavaType) idVarNode.getTypeInfo();
        assert entityJavaType != null : "This method is to be called only for" //NOI18N
                + "node that corresponds to identification variable."; //NOI18N
        String navigationId = idVarNode.getNavigationId();
        return selectPlan.getIdentificationVariableData(
                entityJavaType, navigationId);
    }      
}
