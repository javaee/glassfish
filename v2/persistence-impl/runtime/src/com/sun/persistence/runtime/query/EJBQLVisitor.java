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


package com.sun.persistence.runtime.query;

import java.util.List;


/**
 * An EJBQLVisitor can be walked around an EJBQL tree, visiting each node.
 * The intent is that the walking is done via  EJBQLTreeWalker.g.
 * As each node is reached, a corresponding arriveXXX method is invoked; just
 * before leaving that node, the corresponding leaveXXX method is invoked.
 * Note the difference in parameters between the arriveXXX and leaveXXX: the
 * latter takes arguments representing the items which are matched in the
 * rule.  Furthermore, it returns a value based on its own logic of visiting
 * the tree.
 *
 * @author Dave Bristor
 */
// Note the slightly strange indentation in this file; it is intentional:
// Each arrive method is on a single line; each leave method is on 2 or more
// lines as necessary.  This is for consistency, and to (possibly) the pairing
// more easily seen.
public interface EJBQLVisitor {
    /*
     * rules: selectStmt
     */
    public void arriveSelectStmt(EJBQLAST node);
    
    public void leaveSelectStmt(
            EJBQLAST node, Object from, Object select, Object where,
            Object groupBy, Object having, Object orderBy);

    /*
     * rules: from clause
     */

    public void arriveFromClause(EJBQLAST node);

    public Object leaveFromClause(
            EJBQLAST node, List vars);

    public void arriveCollectionMemberDecl(EJBQLAST node);

    public Object leaveCollectionMemberDecl(
            EJBQLAST node, Object path, Object var);

    public void arriveRangeVarDecl(EJBQLAST node);

    public Object leaveRangeVarDecl(
            EJBQLAST node, Object schema, Object var);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAbstractSchemaName(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveIdentificationVar(
            EJBQLAST node);


    /*
     * rules: select clause
     */
    
    public void arriveSelectClause(EJBQLAST node);

// XXX TODO: EJB3 allows multiple projections
//    public Object leaveSelectClause(
//            EJBQLAST node, Object distinct, List selectExprs);

    public Object leaveSelectClause(
            EJBQLAST node, Object distinct, Object selectExpr);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveDistinct(
            EJBQLAST node);

    /**
     * Note that (a) there is no corresponding arrive method, and (b) the
     * leave method does NOT take an EJBQLAST node, just the pathExpr,
     * because there is no node.
     */
    public Object leaveProjection(
            Object pathExpr);


    public void arriveAggregateSelectExprAvg(EJBQLAST node);

    public Object leaveAggregateSelectExprAvg(
            EJBQLAST node, Object distinct, Object pathExpr);

    public void arriveAggregateSelectExprMax(EJBQLAST node);

    public Object leaveAggregateSelectExprMax(
            EJBQLAST node, Object distinct, Object pathExpr);

    public void arriveAggregateSelectExprMin(EJBQLAST node);

    public Object leaveAggregateSelectExprMin(
            EJBQLAST node, Object distinct, Object pathExpr);

    public void arriveAggregateSelectExprSum(EJBQLAST node);

    public Object leaveAggregateSelectExprSum(
            EJBQLAST node, Object distinct, Object pathExpr);

    public void arriveAggregateSelectExprCount(EJBQLAST node);

    public Object leaveAggregateSelectExprCount(
            EJBQLAST node, Object distinct, Object pathExpr);


    /*
     * rules: where clause
     */

    public void arriveWhereClause(EJBQLAST node);

    public Object leaveWhereClause(
            EJBQLAST node, Object expr);
    
   /*
    * rules: groupby clause
    */
    
    public void arriveGroupByClause(EJBQLAST node);
    
    public Object  leaveGroupByClause(
            EJBQLAST node, List groupings);
  
    
    /*
     * rules: having clause
     */
   
    public void arriveHavingClause(EJBQLAST node); 
        
    public Object leaveHavingClause(
            EJBQLAST node, Object expr) ;


    /*
     * rules: orderby clause
     */

    public void arriveOrderByClause(EJBQLAST node);

    public Object leaveOrderByClause(
            EJBQLAST node, List orderings);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAsc(
            EJBQLAST node, Object pathExpr);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveDesc(
            EJBQLAST node, Object pathExpr);
    
    /*
     * rules: updateStmt
     */
    public void arriveUpdateStmt(EJBQLAST node);
    
    public void leaveUpdateStmt(EJBQLAST node, Object rvd,
             Object set, Object where);
    
    /* 
     * rules: setClause
     */
    public void arriveSetClause(EJBQLAST node);
    
    public Object leaveSetClause(EJBQLAST node, List assignments);

    
    /*
     * rules: setValue
     */    
    public void arriveSetValue(EJBQLAST node);
    
    public Object leaveSetValue(EJBQLAST node, Object path, Object value);

   
    /*
     * rules: deleteStmt
     */
    public void arriveDeleteStmt(EJBQLAST node);
    
    public void leaveDeleteStmt(EJBQLAST node, Object rvd, Object expr);
    
    /*
     * rules: expression
     */

    /* Note: {arrive,leave}Expression are intentionally omitted.
     */
    
    /* AND, OR expressions */
    
    public void arriveConditionalExprAnd(EJBQLAST node);

    public Object leaveConditionalExprAnd(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveConditionalExprOr(EJBQLAST node);

    public Object leaveConditionalExprOr(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprEqual(EJBQLAST node);

    public Object leaveRelationalExprEqual(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprEntityEqual(EJBQLAST node);

    public Object leaveRelationalExprEntityEqual(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprNotEqual(EJBQLAST node);

    public Object leaveRelationalExprNotEqual(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprEntityNotEqual(EJBQLAST node);

    public Object leaveRelationalExprEntityNotEqual(
            EJBQLAST node, Object lhs, Object rhs);

    /* LESS, GREATER, expressions */
    
    public void arriveRelationalExprLE(EJBQLAST node);

    public Object leaveRelationalExprLE(
            EJBQLAST node, Object lhs, Object rhs);
    public void arriveRelationalExprLT(EJBQLAST node);

    public Object leaveRelationalExprLT(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprGE(EJBQLAST node);

    public Object leaveRelationalExprGE(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveRelationalExprGT(EJBQLAST node);

    public Object leaveRelationalExprGT(
            EJBQLAST node, Object lhs, Object rhs);


    /* Binary arithmetic expressions */

    public void arriveBinaryArithmeticExprPlus(EJBQLAST node);

    public Object leaveBinaryArithmeticExprPlus(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveBinaryArithmeticExprMinus(EJBQLAST node);

    public Object leaveBinaryArithmeticExprMinus(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveBinaryArithmeticExprStar(EJBQLAST node);

    public Object leaveBinaryArithmeticExprStar(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveBinaryArithmeticExprDiv(EJBQLAST node);

    public Object leaveBinaryArithmeticExprDiv(
            EJBQLAST node, Object lhs, Object rhs);

    
    /* Unary arithmetic expresions */

    public void arriveUnaryExprPlus(EJBQLAST node);

    public Object leaveUnaryExprPlus(
            EJBQLAST node, Object expr);

    public void arriveUnaryExprMinus(EJBQLAST node);

    public Object leaveUnaryExprMinus(
            EJBQLAST node, Object expr);

    public void arriveUnaryExprNot(EJBQLAST node);

    public Object leaveUnaryExprNot(
            EJBQLAST node, Object expr);

    
    /* BETWEEN (and NOT) expression */
    
    public void arriveBetweenExpr(EJBQLAST node, boolean sense);

    public Object leaveBetweenExpr(
            EJBQLAST node,
            Object expr, Object lower, Object upper, boolean sense);

    
    /* LIKE (and NOT) expression and related */

    public void arriveLikeExpr(EJBQLAST node, boolean sense);

    public Object leaveLikeExpr(
            EJBQLAST node,
            Object expr, Object pattern, Object escape, boolean sense);

    public void arriveEscape(EJBQLAST node);

    public Object leaveEscape(
            EJBQLAST node, Object escape);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveSingleCharStringLiteral(
            EJBQLAST node);

    
    /* IN (and NOT) expression */
    
    public void arriveInExpr(EJBQLAST node, boolean sense);

    public Object leaveInExpr(
            EJBQLAST node, Object expr, List primaries, boolean sense);

    
    /* NULL (and not) comparison expression */

    public void arriveNullComparisonExpr(EJBQLAST node, boolean sense);

    public Object leaveNullComparisonExpr(
            EJBQLAST node, Object expr, boolean sense);

    
    /* EMPTY (and NOT) expression */
    
    public void arriveEmptyCollectionComparisonExpr(EJBQLAST node, boolean sense);

    public Object leaveEmptyCollectionComparisonExpr(
            EJBQLAST node, Object expr, boolean sense);

    
    /* MEMBER OF (and NOT) expression */
    
    public void arriveCollectionMemberExpr(EJBQLAST node, boolean sense);

    public Object leaveCollectionMemberExpr(
            EJBQLAST node, Object expr, Object cmrAccess, boolean sense);

    /*
     * rules: functions
     */

    /* Note: {arrive,leave}Function are intentionally omitted.
     */

    /* String functions */

    public void arriveConcat(EJBQLAST node);

    public Object leaveConcat(
            EJBQLAST node, Object lhs, Object rhs);

    public void arriveSubstring(EJBQLAST node);

    public Object leaveSubstring(
            EJBQLAST node, Object str, Object start, Object length);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveWildcard(
            EJBQLAST node);

    public void arriveLength(EJBQLAST node);

    public Object leaveLength(
            EJBQLAST node, Object expr);

    public void arriveLocate(EJBQLAST node);

    public Object leaveLocate(
            EJBQLAST node, Object pattern, Object str, Object start);

    public void arriveLower(EJBQLAST node);
 
    public Object leaveLower(
            EJBQLAST node, Object expr); 
 
    public void arriveUpper(EJBQLAST node);

    public Object leaveUpper(
            EJBQLAST node, Object expr);
 
    public void arriveTrim(EJBQLAST node);
 
    public Object leaveTrim(
            EJBQLAST node, Object trimSpec, Object trimChar, Object str);


    /* Aritmetic functions */

    public void arriveAbs(EJBQLAST node);

    public Object leaveAbs(
            EJBQLAST node, Object expr);

    public void arriveSqrt(EJBQLAST node);

    public Object leaveSqrt(
            EJBQLAST node, Object expr);

    public void arriveMod(EJBQLAST node);

    public Object leaveMod(
            EJBQLAST node, Object lhs, Object rhs);

    /* Note: {arrive,leave}Primary are intentionally omitted.
     */

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralTrue(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFalse(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralString(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralInt(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralLong(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFloat(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralDouble(
            EJBQLAST node);

    
    /*
     * rules: path expression
     */
    
    public void arrivePathExprCMPField(EJBQLAST node);

    public Object leavePathExprCMPField(
            EJBQLAST node, Object path, Object field);
    
    public void arrivePathExprCMRField(EJBQLAST node);

    public Object leavePathExprCMRField(
            EJBQLAST node, Object path, Object field);
    
    public void arrivePathExprCollectionCMRField(EJBQLAST node);

    public Object leavePathExprCollectionCMRField(
            EJBQLAST node, Object path, Object field);
    
    /** Note the intentional omission of a corresponding arrive method. */
    public Object leavePathExprIdentificationVar(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMPField(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMRField(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCollectionField(
            EJBQLAST node);

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveParameter(
            EJBQLAST node);
}
