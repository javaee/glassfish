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


package com.sun.persistence.runtime.query.impl;

import java.util.List;

import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.EJBQLVisitor;

/**
 * This EJBQLVisitor does nothing except implement the interface.
 *
 * @author Dave Bristor
 */
// Note the slightly strange indentation in this file; it is intentional:
// Each arrive method is on a single line; each leave method is on 2 or more
// lines as necessary.  This is for consistency, and to (possibly) the pairing
// more easily seen.
public class EJBQLVisitorImpl implements EJBQLVisitor {
    public EJBQLVisitorImpl() {
    }
    
    /*
     * rules: selectStmt
     */
    public void arriveSelectStmt(EJBQLAST node) {
    }

    public void leaveSelectStmt (
            EJBQLAST node, Object from, Object select,Object where,
            Object groupBy, Object having, Object orderBy) {
    }   

    /*
     * rules: from clause
     */

    public void arriveFromClause(EJBQLAST node) {
    }

    public Object leaveFromClause(
            EJBQLAST node, List vars) {
        return null;
    }

    public void arriveCollectionMemberDecl(EJBQLAST node) {
    }

    public Object leaveCollectionMemberDecl(
            EJBQLAST node, Object path, Object var) {
        return null;
    }

    public void arriveRangeVarDecl(EJBQLAST node) {
    }

    public Object leaveRangeVarDecl(
            EJBQLAST node, Object schema, Object var) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAbstractSchemaName(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveIdentificationVar(
            EJBQLAST node) {
        return null;
    }

    /*
     * rules: select clause
     */
    
    public void arriveSelectClause(EJBQLAST node) {
    }

// XXX TODO: EJB3 allows multiple projections
//    public Object leaveSelectClause(
//            EJBQLAST node, Object distinct, List selectExprs) {
//        return null;
//    }

    public Object leaveSelectClause(
            EJBQLAST node, Object distinct, Object selectExpr) {
        return null;
    }

    public Object leaveDistinct(
            EJBQLAST node) {
        return null;
    }

    /**
     * @see com.sun.persistence.runtime.query.EJBQLVisitor#leaveProjection(java.lang.Object)
     */
    public Object leaveProjection(
            Object pathExpr) {
        return null;
    }

    
    public void arriveAggregateSelectExprAvg(EJBQLAST node) {
    }

    public Object leaveAggregateSelectExprAvg(
            EJBQLAST node,Object aggregateDistinct, Object pathExpr) {
        return null;
    }

    public void arriveAggregateSelectExprMax(EJBQLAST node) {
    }

    public Object leaveAggregateSelectExprMax(
            EJBQLAST node, Object aggregateDistinct, Object pathExpr) {
        return null;
    }

    public void arriveAggregateSelectExprMin(EJBQLAST node) {
    }

    public Object leaveAggregateSelectExprMin(
            EJBQLAST node, Object aggregateDistinct, Object pathExpr) {
        return null;
    }

    public void arriveAggregateSelectExprSum(EJBQLAST node) {
    }

    public Object leaveAggregateSelectExprSum(
            EJBQLAST node, Object aggregateDistinct, Object pathExpr) {
        return null;
    }

    public void arriveAggregateSelectExprCount(EJBQLAST node) {
    }

    public Object leaveAggregateSelectExprCount(
            EJBQLAST node, Object aggregateDistinct, Object pathExpr) {
        return null;
    }


    /*
     * rules: where clause
     */

    public void arriveWhereClause(EJBQLAST node) {
    }

    public Object leaveWhereClause(
            EJBQLAST node, Object expr) {
        return null;
    }

    
    /*
     * rules: groupby clause
     */
    
    public void arriveGroupByClause(EJBQLAST node) {
             
    }
    
    public Object  leaveGroupByClause(
            EJBQLAST node, List groupings) {
        return null;
    }
    
    /*
     * rules: having clause
     */
   
    public void arriveHavingClause(EJBQLAST node) {
    }
    
    public Object leaveHavingClause(
            EJBQLAST node, Object expr) {
        return null;
    }
    
    /*
     * rules: orderby clause
     */

    public void arriveOrderByClause(EJBQLAST node) {
    }

    public Object leaveOrderByClause(
            EJBQLAST node, List orderings) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAsc(
            EJBQLAST node, Object pathExpr) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveDesc(
            EJBQLAST node, Object pathExpr) {
        return null;
    }
    
    /*
     * rules: updateStmt
     */
    public void arriveUpdateStmt(EJBQLAST node) {
    }
    
    public void leaveUpdateStmt(EJBQLAST node, Object rvd,
             Object set, Object where) {
    }
    
    /* 
     * rules: setClause
     */
    public void arriveSetClause(EJBQLAST node) {
    }
    
    public Object leaveSetClause(EJBQLAST node, List assignments) {
       return null;
    }

    
    /*
     * rules: setValue
     */    
    public void arriveSetValue(EJBQLAST node) {
    }
    
    public Object leaveSetValue(EJBQLAST node, Object path, Object value) {
        return null;
    }

   
    /*
     * rules: deleteStmt
     */
    public void arriveDeleteStmt(EJBQLAST node) {
    }
    
    public void leaveDeleteStmt(EJBQLAST node, Object rvd, Object expr) {
    }

    /*
     * rules: expression
     */
    
    /* AND, OR expressions */
    
    public void arriveConditionalExprAnd(EJBQLAST node) {
    }

    public Object leaveConditionalExprAnd(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveConditionalExprOr(EJBQLAST node) {
    }

    public Object leaveConditionalExprOr(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    
    /* EQUAL expressions
     * Use one method pair, taking an additional boolean, instead of two
     * method pairs, to handle both XYZ and NOT-XYZ cases.
     */

    public void arriveRelationalExprEqual(EJBQLAST node) {
    }

    public Object leaveRelationalExprEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprEntityEqual(EJBQLAST node) {
    }

    public Object leaveRelationalExprEntityEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprNotEqual(EJBQLAST node) {
    }

    public Object leaveRelationalExprNotEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprEntityNotEqual(EJBQLAST node) {
    }

    public Object leaveRelationalExprEntityNotEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    
    /* LESS, GREATER, expressions */

    public void arriveRelationalExprLE(EJBQLAST node) {
    }

    public Object leaveRelationalExprLE(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprLT(EJBQLAST node) {
    }

    public Object leaveRelationalExprLT(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprGE(EJBQLAST node) {
    }

    public Object leaveRelationalExprGE(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveRelationalExprGT(EJBQLAST node) {
    }

    public Object leaveRelationalExprGT(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }


    /* Binary arithmetic expressions */

    public void arriveBinaryArithmeticExprPlus(EJBQLAST node) {
    }

    public Object leaveBinaryArithmeticExprPlus(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveBinaryArithmeticExprMinus(EJBQLAST node) {
    }

    public Object leaveBinaryArithmeticExprMinus(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveBinaryArithmeticExprStar(EJBQLAST node) {
    }

    public Object leaveBinaryArithmeticExprStar(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveBinaryArithmeticExprDiv(EJBQLAST node) {
    }

    public Object leaveBinaryArithmeticExprDiv(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    
    /* Unary arithmetic expresions */

    public void arriveUnaryExprPlus(EJBQLAST node) {
    }

    public Object leaveUnaryExprPlus(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveUnaryExprMinus(EJBQLAST node) {
    }

    public Object leaveUnaryExprMinus(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveUnaryExprNot(EJBQLAST node) {
    }

    public Object leaveUnaryExprNot(
            EJBQLAST node, Object expr) {
        return null;
    }

    
    /* BETWEEN (and NOT) expression */
    
    public void arriveBetweenExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveBetweenExpr(
            EJBQLAST node,
            Object expr, Object lower, Object upper, boolean sense) {
        return null;
    }
    

    /* LIKE (and NOT) expression and related */

    public void arriveLikeExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveLikeExpr(
            EJBQLAST node,
            Object expr, Object pattern, Object escape, boolean sense) {
        return null;
    }

    public void arriveEscape(EJBQLAST node) {
    }

    public Object leaveEscape(
            EJBQLAST node, Object escape) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveSingleCharStringLiteral(
            EJBQLAST node) {
        return null;
    }

    
    /* IN (and NOT) expression */
    
    public void arriveInExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveInExpr(
            EJBQLAST node, Object expr, List primaries, boolean sense) {
        return null;
    }
    
    /* NULL (and not) comparison expression */

    public void arriveNullComparisonExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveNullComparisonExpr(
            EJBQLAST node, Object expr, boolean sense) {
        return null;
    }

    
    /* EMPTY (and NOT) expression */
    
    public void arriveEmptyCollectionComparisonExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveEmptyCollectionComparisonExpr(
            EJBQLAST node, Object expr, boolean sense) {
        return null;
    }

    
    /* MEMBER OF (and NOT) expression */
    
    public void arriveCollectionMemberExpr(EJBQLAST node, boolean sense) {
    }

    public Object leaveCollectionMemberExpr(
            EJBQLAST node, Object expr, Object cmrAccess, boolean sense) {
        return null;
    }

    /*
     * rules: functions
     */
    
    /* String functions */

    public void arriveConcat(EJBQLAST node) {
    }

    public Object leaveConcat(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    public void arriveSubstring(EJBQLAST node) {
    }

    public Object leaveSubstring(
            EJBQLAST node, Object str, Object start, Object length) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveWildcard(
            EJBQLAST node) {
        return null;
    }

    public void arriveLength(EJBQLAST node) {
    }

    public Object leaveLength(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveLocate(EJBQLAST node) {
    }

    public Object leaveLocate(
            EJBQLAST node, Object pattern, Object str, Object start) {
        return null;
    }

    public void arriveLower(EJBQLAST node) {
    }

    public Object leaveLower(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveUpper(EJBQLAST node) {
    }

    public Object leaveUpper(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveTrim(EJBQLAST node) {
    }

    public Object leaveTrim(
            EJBQLAST node, Object trimSpec, Object trimChar, Object str) {
        return null;
    }

    /* Arithmetic functions */

    public void arriveAbs(EJBQLAST node) {
    }

    public Object leaveAbs(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveSqrt(EJBQLAST node) {
    }

    public Object leaveSqrt(
            EJBQLAST node, Object expr) {
        return null;
    }

    public void arriveMod(EJBQLAST node) {
    }

    public Object leaveMod(
            EJBQLAST node, Object lhs, Object rhs) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralTrue(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFalse(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralString(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralInt(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralLong(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFloat(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralDouble(
            EJBQLAST node) {
        return null;
    }

    
    /*
     * rules: path expressions
     */
    
    public void arrivePathExprCMPField(EJBQLAST node) {
    }

    public Object leavePathExprCMPField(
            EJBQLAST node, Object path, Object field) {
        return null;
    }
    
    public void arrivePathExprCMRField(EJBQLAST node) {
    }

    public Object leavePathExprCMRField(
            EJBQLAST node, Object path, Object field) {
        return null;
    }
    
    public void arrivePathExprCollectionCMRField(EJBQLAST node) {
    }

    public Object leavePathExprCollectionCMRField(
            EJBQLAST node, Object path, Object field) {
        return null;
    }
    
    /** Note the intentional omission of a corresponding arrive method. */
    public Object leavePathExprIdentificationVar(
            EJBQLAST node) {
        return null;
    }
    
    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMPField(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMRField(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCollectionField(
            EJBQLAST node) {
        return null;
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveParameter(
            EJBQLAST node) {
        return null;
    }
}
