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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.runtime.query.EJBQLVisitor;

/**
 * An EJBQLVisitor that prints arrivals and departures.
 * @author Dave Bristor
 */
public class EJBQLVisitorPrinter implements EJBQLVisitor {
    /** If true, print arrive messages. */
    private final boolean sayArrive;

    /** If true, print leave messages. */
    private final boolean sayLeave;

    /** Stream to which messages are printed. */
    private final PrintWriter writer;

    /** Retains the value of the last thing to have been invoked on method
     * @{link leave}.
     */
    private String last = null;

    /**
     * Prints information about each visited node.
     * @param sayArrive if true, prints arrivals
     * @param sayLeave if true, prints departures
     */
    EJBQLVisitorPrinter(boolean sayArrive, boolean sayLeave) {
        this(sayArrive, sayLeave, null);
    }

    /**
     * Prints informatino about each visited node to given PrintStream.
     * @param sayArrive if true, prints arrivals
     * @param sayLeave if true, prints departures
     * @param out PrintStream to which messages are written
     */
    EJBQLVisitorPrinter(boolean sayArrive, boolean sayLeave, PrintWriter writer) {
        if (writer == null) {
            this.sayArrive = false;
            this.sayLeave = false;
            this.writer = null;
        } else {
            this.sayArrive = sayArrive;
            this.sayLeave = sayLeave;
            this.writer = writer;
        }
    }

    /**
     * @return at any point during the walk, it returns the string resulting
     * from the most recently executed leaveABC method.
     */
    public String getLast() {
        return last;
    }
  
    /*
     * rules: selectStmt
     */
    
    public void arriveSelectStmt(EJBQLAST node) {
        arrive("arriveSelectStmt", node);
    }
    
    public void leaveSelectStmt(
            EJBQLAST node, Object from, Object select, Object where,
            Object groupBy, Object having, Object orderBy) {
        leave(
            "leaveSelectStmt " + node.getText()
            + " from=" + from
            + " select=" + select
            + " where=" + where
            + " groupBy=" + groupBy
            + " having=" + having
            + " orderBy=" + orderBy);
    }    

    /*
     * rules: from clause
     */

    public void arriveFromClause(EJBQLAST node) {
        arrive("arriveFromClause", node);
    }

    public Object leaveFromClause(
            EJBQLAST node, List vars) {
        String rc = "leaveFromClause " + node.getText();
        if (vars.size() > 0) {
            rc += " vars=";
            for (Iterator i = vars.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o == null) {
                    rc += "null ";
                } else {
                    rc += o.toString() + " ";
                }
            }
        }
        return leave(rc);
    }

    public void arriveCollectionMemberDecl(EJBQLAST node) {
        arrive("arriveCollectionMemberDecl", node);
    }

    public Object leaveCollectionMemberDecl(
            EJBQLAST node, Object path, Object var) {
        return leave(
            "leaveCollectionMemberDecl " + node.getText()
            + " path=" + path + " var=" + var);
    }

    public void arriveRangeVarDecl(EJBQLAST node) {
        arrive("arriveRangeVarDecl", node);
    }

    public Object leaveRangeVarDecl(
            EJBQLAST node, Object schema, Object var) {
        return leave(
            "leaveRangeVarDecl " + node.getText()
            + " schema=" + schema + " var=" + var);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAbstractSchemaName(
            EJBQLAST node) {
        return leave(
            "leaveAbstractSchemaName " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveIdentificationVar(
            EJBQLAST node) {
        return leave(
            "leaveIdentificationVar " + node.getText());
    }

    /*
     * rules: select clause
     */

    public void arriveSelectClause(EJBQLAST node) {
        arrive("arriveSelectClause", node);
    }

// XXX TODO: EJB3 allows multiple projections
//    public Object leaveSelectClause(
//            EJBQLAST node, Object distinct, List selectExprs);

    public Object leaveSelectClause(
            EJBQLAST node, Object distinct, Object selectExpr) {
        return leave(
            "leaveSelectClause " + node.getText()
            + " distinct=" + distinct + " selectExpr=" + selectExpr);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveDistinct(
            EJBQLAST node) {
        return leave((node != null) ? node.getText() : null);
    }

    /**
     * @see com.sun.persistence.runtime.query.EJBQLVisitor#leaveProjection(java.lang.Object)
     */
    public Object leaveProjection(
            Object pathExpr) {
        return leave(
            "leaveProjection"
            + " pathExpr=" + pathExpr);
    }


    public void arriveAggregateSelectExprAvg(EJBQLAST node) {
        arrive("arriveAggregateSelectExprAvg", node);
    }

    public Object leaveAggregateSelectExprAvg(
            EJBQLAST node, Object distinct, Object pathExpr) {
        return leaveAggregateSelectExpr(node, distinct, pathExpr, "Avg");
    }

    public void arriveAggregateSelectExprMax(EJBQLAST node) {
        arrive("arriveAggregateSelectExprMax", node);
    }

    public Object leaveAggregateSelectExprMax(
            EJBQLAST node, Object distinct, Object pathExpr) {
        return leaveAggregateSelectExpr(node, distinct, pathExpr, "Max");
    }

    public void arriveAggregateSelectExprMin(EJBQLAST node) {
        arrive("arriveAggregateSelectExprMin", node);
    }

    public Object leaveAggregateSelectExprMin(
            EJBQLAST node, Object distinct, Object pathExpr) {
        return leaveAggregateSelectExpr(node, distinct, pathExpr, "Min");
    }

    public void arriveAggregateSelectExprSum(EJBQLAST node) {
        arrive("arriveAggregateSelectExprSum", node);
    }

    public Object leaveAggregateSelectExprSum(
            EJBQLAST node, Object distinct, Object pathExpr) {
        return leaveAggregateSelectExpr(node, distinct, pathExpr, "Sum");
    }

    public void arriveAggregateSelectExprCount(EJBQLAST node) {
        arrive("arriveAggregateSelectExprCount", node);
    }

    public Object leaveAggregateSelectExprCount(
            EJBQLAST node, Object distinct, Object pathExpr) {
        return leaveAggregateSelectExpr(node, distinct, pathExpr, "Count");
    }

    /*
     * rules: where clause
     */

    public void arriveWhereClause(EJBQLAST node) {
        arrive("arriveWhereClause", node);
    }

    public Object leaveWhereClause(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveWhereClause " + node.getText() + " expr=" + expr);
    }

    /*
     * rules: groupby clause
     */
    
    public void arriveGroupByClause(EJBQLAST node) {
        arrive("arriveGroupByClause", node);
    }
    
    public Object leaveGroupByClause(
            EJBQLAST node, List groupings) {
        return leave(
            "leaveGroupByClause " + node.getText() + " groupings=" + groupings);
    }
    
    /*
     * rules: having clause
     */
    
    public void arriveHavingClause(EJBQLAST node) {
        arrive("arriveHavingClause", node);
    }
    
    public Object leaveHavingClause(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveHavingClause " + node.getText() + " expr=" + expr);
    }
    
    /*
     * rules: orderby clause
     */

    public void arriveOrderByClause(EJBQLAST node) {
        arrive("arriveOrderByClause", node);
    }

    public Object leaveOrderByClause(
            EJBQLAST node, List orderings) {
        return leave(
            "leaveOrderByClause " + node.getText()
            + " orderings=" + orderings);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveAsc(
            EJBQLAST node, Object pathExpr) {
        return leave(
            "leaveAsc " + node.getText()
            + " pathExpr=" + pathExpr);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveDesc(
            EJBQLAST node, Object pathExpr) {
        return leave(
            "leaveDesc " + node.getText()
            + " pathExpr=" + pathExpr);
    }

    /*
     * rules: updateStmt
     */
    
    public void arriveUpdateStmt(EJBQLAST node) {
        arrive("arriveUpdateStmt", node);
    }
    
    public void leaveUpdateStmt(
            EJBQLAST node, Object rvd, Object set, Object where) {
        leave(
            "leaveUpdateStmt " + node.getText()
            + " set=" + set + " where=" + where);
    }
    
    /* 
     * rules: setClause
     */
    
    public void arriveSetClause(EJBQLAST node) {
        arrive("arriveSetClause", node);
    }
    
    public Object leaveSetClause(EJBQLAST node, List assignments) {
        return leave(
            "leaveSetClause" + node.getText()
            + " assignments=" + assignments);
    }

    
    /*
     * rules: setValue
     */    
    
    public void arriveSetValue(EJBQLAST node) {
        arrive("arriveSetValue", node);
    }
    
    public Object leaveSetValue(EJBQLAST node, Object path, Object value) {
        return leave(
            "leaveSetValue" + node.getText() 
            + " path=" + path + " value=" + value);
    }
   
    /*
     * rules: deleteStmt
     */
    
    public void arriveDeleteStmt(EJBQLAST node) {
        arrive("arriveDeleteStmt ", node);
    }
    
    public void leaveDeleteStmt(EJBQLAST node, Object rvd, Object expr) {
        leave(
            "leaveDeleteStmt " + node.getText()
            + " rvd=" + rvd + " expr=" + expr);
    }                
   
    /*
     * rules: expression
     */


    /* AND, OR expressions */

    public void arriveConditionalExprAnd(EJBQLAST node) {
        arrive("arriveConditionalExprAnd", node);
    }

    public Object leaveConditionalExprAnd(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveConditionalExprAnd " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveConditionalExprOr(EJBQLAST node) {
        arrive("arriveConditionalExprOr", node);
    }

    public Object leaveConditionalExprOr(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveConditionalExprOr " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }


    /* EQUAL expressions
     * Use one method pair, taking an additional boolean, instead of two
     * method pairs, to handle both XYZ and NOT-XYZ cases.
     */

    public void arriveRelationalExprEqual(EJBQLAST node) {
        arrive("arriveRelationalExprEqual", node);
    }

    public Object leaveRelationalExprEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprEqual " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprEntityEqual(EJBQLAST node) {
        arrive("arriveRelationalExprEntityEqual", node);
    }

    public Object leaveRelationalExprEntityEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprEntityEqual " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprNotEqual(EJBQLAST node) {
        arrive("arriveRelationalExprNotEqual", node);
    }

    public Object leaveRelationalExprNotEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprNotEqual " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprEntityNotEqual(EJBQLAST node) {
        arrive("arriveRelationalExprEntityNotEqual", node);
    }

    public Object leaveRelationalExprEntityNotEqual(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprEntityNotEqual " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }


    /* LESS, GREATER, expressions */

    public void arriveRelationalExprLE(EJBQLAST node) {
        arrive("arriveRelationalExprLE", node);
    }

    public Object leaveRelationalExprLE(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprLE " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprLT(EJBQLAST node) {
        arrive("arriveRelationalExprLT", node);
    }

    public Object leaveRelationalExprLT(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprLT " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprGE(EJBQLAST node) {
        arrive("arriveRelationalExprGE", node);
    }

    public Object leaveRelationalExprGE(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprGE " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveRelationalExprGT(EJBQLAST node) {
        arrive("arriveRelationalExprGT", node);
    }

    public Object leaveRelationalExprGT(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveRelationalExprGT " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }


    /* Binary arithmetic expressions */

    public void arriveBinaryArithmeticExprPlus(EJBQLAST node) {
        arrive("arriveBinaryArithmeticExprPlus", node);
    }

    public Object leaveBinaryArithmeticExprPlus(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveBinaryArithmeticExprPlus " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveBinaryArithmeticExprMinus(EJBQLAST node) {
        arrive("arriveBinaryArithmeticExprMinus", node);
    }

    public Object leaveBinaryArithmeticExprMinus(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveBinaryArithmeticExprMinus " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveBinaryArithmeticExprStar(EJBQLAST node) {
        arrive("arriveBinaryArithmeticExprStar", node);
    }

    public Object leaveBinaryArithmeticExprStar(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveBinaryArithmeticExprStar " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    public void arriveBinaryArithmeticExprDiv(EJBQLAST node) {
        arrive("arriveBinaryArithmeticExprDiv", node);
    }

    public Object leaveBinaryArithmeticExprDiv(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveBinaryArithmeticExprDiv " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }


    /* Unary arithmetic expresions */

    public void arriveUnaryExprPlus(EJBQLAST node) {
        arrive("arriveUnaryExprPlus", node);
    }

    public Object leaveUnaryExprPlus(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveUnaryExprPlus " + node.getText() + " expr=" + expr);
    }

    public void arriveUnaryExprMinus(EJBQLAST node) {
        arrive("arriveUnaryExprMinus", node);
    }

    public Object leaveUnaryExprMinus(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveUnaryExprMinus " + node.getText() + " expr=" + expr);
    }

    public void arriveUnaryExprNot(EJBQLAST node) {
        arrive("arriveUnaryExprNot", node);
    }

    public Object leaveUnaryExprNot(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveUnaryExprNot " + node.getText() + " expr=" + expr);
    }


    /* BETWEEN (and NOT) expression */

    public void arriveBetweenExpr(EJBQLAST node, boolean sense) {
        arrive("arriveBetweenExpr", node, sense);
    }

    public Object leaveBetweenExpr(
            EJBQLAST node,
            Object expr, Object lower, Object upper, boolean sense) {
        return leave(
            "leaveBetweenExpr " + node.getText()
            + " lower=" + lower + " upper=" + upper + " sense=" + sense);
    }


    /* LIKE (and NOT) expression and related */

    public void arriveLikeExpr(EJBQLAST node, boolean sense) {
        arrive("arriveLikeExpr", node, sense);
    }

    public Object leaveLikeExpr(
            EJBQLAST node,
            Object expr, Object pattern, Object escape, boolean sense) {
        return leave(
            "leaveLikeExpr " + node.getText()
            + " expr=" + expr + " pattern=" + pattern
            + " escape=" + escape + " sense=" + sense);
    }

    public void arriveEscape(EJBQLAST node) {
        arrive("arriveEscape", node);
    }

    public Object leaveEscape(
            EJBQLAST node, Object escape) {
        return leave(
            "leaveEscape " + node.getText() + " escape=" + escape);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveSingleCharStringLiteral(
            EJBQLAST node) {
        return leave(
            "leaveSingleCharStringLiteral " + node.getText());
    }


    /* IN (and NOT) expression */

    public void arriveInExpr(EJBQLAST node, boolean sense) {
        arrive("arriveInExpr", node, sense);
    }

    public Object leaveInExpr(
            EJBQLAST node, Object expr, List primaries, boolean sense) {
        return leave(
            "leaveInExpr " + node.getText()
            + " expr=" + expr + " primaries=" + primaries
            + " sense=" + sense);
    }


    /* NULL (and not) comparison expression */

    public void arriveNullComparisonExpr(EJBQLAST node, boolean sense) {
        arrive("arriveNullComparisonExpr", node, sense);
    }

    public Object leaveNullComparisonExpr(
            EJBQLAST node, Object expr, boolean sense) {
        return leave(
                "leaveNullComparisonExpr " + node.getText()
                + " expr=" + expr + " sense=" + sense);
    }


    /* EMPTY (and NOT) expression */

    public void arriveEmptyCollectionComparisonExpr(EJBQLAST node, boolean sense) {
        arrive("arriveEmptyCollectionComparisonExpr", node, sense);
    }

    public Object leaveEmptyCollectionComparisonExpr(
            EJBQLAST node, Object expr, boolean sense) {
        return leave(
            "leaveEmptyCollectionComparisonExpr " + node.getText()
            + " expr=" + expr + " sense=" + sense);
    }


    /* MEMBER OF (and NOT) expression */

    public void arriveCollectionMemberExpr(EJBQLAST node, boolean sense) {
        arrive("arriveCollectionMemberExpr", node, sense);
    }

    public Object leaveCollectionMemberExpr(
            EJBQLAST node, Object expr, Object cmr, boolean sense) {
        return leave(
            "leaveCollectionMemberExpr " + node.getText()
            + " expr=" + expr + " cmr=" + cmr + " sense=" + sense);
    }

    /*
     * rules: functions
     */

    /* String functions */

    public void arriveConcat(EJBQLAST node) {
        arrive("arriveConcat", node);
    }

    public Object leaveConcat(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveConcat " + node.getText()
            + " lhs=" + lhs + " rhs=");
    }

    public void arriveSubstring(EJBQLAST node) {
        arrive("arriveSubstring", node);
    }

    public Object leaveSubstring(
            EJBQLAST node, Object str, Object start, Object length) {
        return leave(
            "leaveSubstring " + node.getText()
            + " str=" + str + " start=" + start + " length=" + length);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveWildcard(
            EJBQLAST node) {
        return leave("leaveWildcard " + node.getText());
    }

    public void arriveLength(EJBQLAST node) {
        arrive("arriveLength", node);
    }

    public Object leaveLength(
            EJBQLAST node, Object length) {
        return leave(
            "leaveLength " + node.getText() + " length=" + length);
    }

    public void arriveLocate(EJBQLAST node) {
        arrive("arriveLocate", node);
    }

    public Object leaveLocate(
            EJBQLAST node, Object pattern, Object str, Object start) {
        return leave(
            "leaveSubstring " + node.getText()
            + " pattern=" + pattern + " str=" + str + " start=" + start);
    }

    public void arriveLower(EJBQLAST node) {
        arrive("arriveLower", node);
    }

    public Object leaveLower(
            EJBQLAST node, Object str) {
        return leave(
            "leaveLower " + node.getText() + " lower=" + str);
    }

    public void arriveUpper(EJBQLAST node) {
        arrive("arriveUpper", node);
    }

    public Object leaveUpper(
            EJBQLAST node, Object str) {
        return leave(
            "leaveUpper " + node.getText() + " upper=" + str);
    }

    public void arriveTrim(EJBQLAST node) {
        arrive("arriveTrim", node);
    }

    public Object leaveTrim(
            EJBQLAST node, Object trimSpec, Object trimChar, Object str) {
        return leave(
            "leaveTrim " + node.getText()
            + " trimSpec=" + trimSpec + " trimChar=" + trimChar + " str=" + str);
    }

    /* Arithmetic functions */

    public void arriveAbs(EJBQLAST node) {
        arrive("arriveAbs", node);
    }

    public Object leaveAbs(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveAbs " + node.getText() + " expr=" + expr );
    }

    public void arriveSqrt(EJBQLAST node) {
        arrive("arriveSqrt", node);
    }

    public Object leaveSqrt(
            EJBQLAST node, Object expr) {
        return leave(
            "leaveSqrt " + node.getText() + " expr=" + expr);
    }

    public void arriveMod(EJBQLAST node) {
        arrive("arriveMod", node);
    }

    public Object leaveMod(
            EJBQLAST node, Object lhs, Object rhs) {
        return leave(
            "leaveMod " + node.getText()
            + " lhs=" + lhs + " rhs=" + rhs);
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralTrue(
            EJBQLAST node) {
        return leave("leaveLiteralTrue " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFalse(
            EJBQLAST node) {
        return leave("leaveLiteralFalse " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralString(
            EJBQLAST node) {
        return leave("leaveLiteralString " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralInt(
            EJBQLAST node) {
        return leave("leaveLiteralInt " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralLong(
            EJBQLAST node) {
        return leave("leaveLiteralLong " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralFloat(
            EJBQLAST node) {
        return leave("leaveLiteralFloat " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveLiteralDouble(
            EJBQLAST node) {
        return leave("leaveLiteralDouble " + node.getText());
    }


    /*
     * rules: path expressions
     */

    public void arrivePathExprCMPField(EJBQLAST node) {
        arrive("arrivePathExprCMPField", node);
    }

    public Object leavePathExprCMPField(
            EJBQLAST node, Object path, Object field) {
        return leavePathExpr(node, path, field, "CMPField");
    }

    public void arrivePathExprCMRField(EJBQLAST node) {
        arrive("arrivePathExprCMRField", node);
    }

    public Object leavePathExprCMRField(
            EJBQLAST node, Object path, Object field) {
        return leavePathExpr(node, path, field, "CMRField");
    }

    public void arrivePathExprCollectionCMRField(EJBQLAST node) {
        arrive("arrivePathExprCollectionCMRField", node);
    }

    public Object leavePathExprCollectionCMRField(
            EJBQLAST node, Object path, Object field) {
        return leavePathExpr(node, path, field, "CollectionCMRField");
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leavePathExprIdentificationVar(
            EJBQLAST node) {
        return leave(
            "leavePathExprIdentificationVar " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMPField(
            EJBQLAST node) {
        return leave(
            "leaveFieldCMPField " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCMRField(
            EJBQLAST node) {
        return leave(
            "leaveFieldCMRField " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveFieldCollectionField(
            EJBQLAST node) {
        return leave(
            "leaveFieldCollectionField " + node.getText());
    }

    /** Note the intentional omission of a corresponding arrive method. */
    public Object leaveParameter(
            EJBQLAST node) {
        return leave("leaveParameter " + node.getText());
    }


    /*
     * Private utility methods.
     */

    private void arrive(String s, EJBQLAST node) {
        if (sayArrive) {
            writer.println(s + " " + node.getText());
        }
    }

    private void arrive(String s, EJBQLAST node, boolean sense) {
        if (sayArrive) {
            writer.println(
                s + " " + node.getText()
                + " sense=" + sense);
        }
    }

    private Object leave(String s) {
        s = "(" + s + ")";
        if (sayLeave) {
            writer.print(s);
            writer.flush();
        }
        last = s;
        return s;
    }

    private Object leaveAggregateSelectExpr(
            EJBQLAST node, Object distinct, Object pathExpr, String name) {
        return leave(
            "leaveAggregateSelectExpr" + name + " " + node.getText()
            + " distinct=" + distinct
            + " pathExpr=" + pathExpr);
    }

    private Object leavePathExpr(
            EJBQLAST node, Object path, Object field, String name) {
        return leave(
            "leavePathExpr" + name +" " + node.getText()
            + " path=" + path
            + " field=" + field);
    }
}
