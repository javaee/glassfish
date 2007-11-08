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


header
{
    package com.sun.persistence.runtime.query.impl;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.ResourceBundle;
    import com.sun.persistence.runtime.query.EJBQLVisitor;
    import com.sun.persistence.utility.I18NHelper;
    import com.sun.persistence.utility.StringHelper;
}

/**
 * This parser offers a way to traverse an EJBQL query tree by providing a
 * Visitor that can do so.
 *
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 * @author  Dave Bristor
 */
class EJBQLTreeWalker extends TreeParser;

options
{
    importVocab = EJBQL3;
    defaultErrorHandler = false;
    ASTLabelType = "EJBQLASTImpl"; //NOI18N
}

{
    /** I18N support. */
    protected final static ResourceBundle msgs =
        I18NHelper.loadBundle(EJBQLTreeWalker.class);

    /**
     * The Visitor which is walked around the tree.
     */
    private EJBQLVisitor v;

    /**
     * Sets this tree walker's visitor.
     */
    public void init(EJBQLVisitor v) {
        this.v = v;
    }


    public void reportError(RecognitionException ex) {
        ErrorMsg.fatal(I18NHelper.getMessage(msgs,
                "ERR_EJBQLTreeWalkerError"), ex); //NOI18N
    }

    /** */
    public void reportError(String s) {
        ErrorMsg.fatal(I18NHelper.getMessage(msgs,
                "ERR_EJBQLTreeWalkerError") + s); //NOI18N
    }
}

//---------------------------------
//query
//---------------------------------

query
    :   selectStmt
    |   updateStmt
    |   deleteStmt
    ; 

// ----------------------------------
// rules: selectStmt
// ----------------------------------

selectStmt 
{
    Object from = null;
    Object select = null;
    Object where = null;
    Object groupBy = null;
    Object having = null;
    Object orderBy = null;
}
    :   #(  q:QUERY
            { v.arriveSelectStmt(#q); }
            from = fromClause
            select = selectClause
            where = whereClause
            groupBy = groupbyClause
            having = havingClause
            orderBy = orderbyClause
            // Note: The result of walking the tree can be obtained from the
            // visitor.
            { v.leaveSelectStmt(#q, from, select, where,  groupBy, having, orderBy); }
        )
    ;

// ----------------------------------
// rules: from clause
// ----------------------------------

fromClause returns [Object rc]
{
  List vars = new ArrayList();
}
    :   #(  from:FROM
            { v.arriveFromClause(#from); }
            ( identificationVarDecl[vars] )+
            { rc = v.leaveFromClause(#from, vars); }
        )
    ;

identificationVarDecl[List vars]
{
    Object obj = null;
}
    :
        obj = collectionMemberDecl
        { vars.add(obj); }
    |
        obj = rangeVarDecl
        { vars.add(obj); }
    ;

collectionMemberDecl returns [Object rc]
{
    Object path = null;
    Object var = null;
}
    :   #(  c:COLLECTION_MEMBER_DECL
            { v.arriveCollectionMemberDecl(#c); }
            path = pathExpr
            var = identificationVar
            { rc = v.leaveCollectionMemberDecl(#c, path, var); }
         )
    ;

rangeVarDecl returns [Object rc]
{
    Object schema = null;
    Object var = null;
}
    :   #(  r: RANGE
            { v.arriveRangeVarDecl(#r); }
            schema = abstractSchemaName
            ( var = identificationVar )?
            { rc = v.leaveRangeVarDecl(#r, schema, var); }
        )
    ;

abstractSchemaName returns [Object rc]
    : asn:ABSTRACT_SCHEMA_NAME
       // arrive intentionally omitted
      { rc = v.leaveAbstractSchemaName(#asn); }
	;

identificationVar returns [Object rc]
    : var:IDENTIFICATION_VAR_DECL
       // arrive intentionally omitted
      { rc = v.leaveIdentificationVar(#var); }
	;

// ----------------------------------
// rules: select clause
// ----------------------------------

selectClause returns [Object rc]
{
    Object d = null;
    Object p = null;
}
    :   #(  s:SELECT
            { v.arriveSelectClause(#s); }
            d = distinct
            p = projection
            { rc = v.leaveSelectClause(#s, d, p); }
        )
    ;

distinct returns [Object rc]
    :   d:DISTINCT
        { rc = v.leaveDistinct(#d); }
    |   // empty rule
        { rc = null; }
    ;

projection returns [Object rc]
{
    Object a = null;
    Object p = null;
}
    :   a = aggregateSelectExpr
        // arrive intentionally omitted
        { rc = v.leaveProjection(p); }
    |   p = pathExpr
        // arrive intentionally omitted
        { rc = v.leaveProjection(p); }
    ;

aggregateSelectExpr returns [Object rc]
{
    Object p = null;
    Object d = null;
}    :   #( avg:AVG
           { v.arriveAggregateSelectExprAvg(#avg); }
           d = distinct
           p = pathExpr
           { rc = v.leaveAggregateSelectExprAvg(#avg, d, p); }
        )
    |   #( max:MAX
           { v.arriveAggregateSelectExprMax(#max); }
           d = distinct
           p = pathExpr
           { rc = v.leaveAggregateSelectExprMax(#max, d, p); }
        )
    |   #( min:MIN
           { v.arriveAggregateSelectExprMin(#min); }
           d = distinct
           p = pathExpr
           { rc = v.leaveAggregateSelectExprMin(#min, d, p); }
        )
    |   #( sum:SUM
           { v.arriveAggregateSelectExprSum(#sum); }
           d = distinct
           p = pathExpr
           { rc = v.leaveAggregateSelectExprMin(#sum, d, p); }
        )
    |   #( count:COUNT
           { v.arriveAggregateSelectExprCount(#count); }
           d = distinct
           p = pathExpr
           { rc = v.leaveAggregateSelectExprCount(#count, d, p); }
        )
    ;

// ----------------------------------
// rules: where clause
// ----------------------------------

whereClause returns [Object rc]
{
    Object e = null;
}
    :   #(  w:WHERE
            { v.arriveWhereClause(#w); }
            e = expression
            { rc = v.leaveWhereClause(#w, e); }
        )
    ;

//-----------------------------------
// rules: groupBy/having clause
//-----------------------------------

groupbyClause returns [Object rc]
{
    Object p = null;
    List groupings = new ArrayList();
}
    :   #(  group:GROUP
            { v.arriveGroupByClause(#group); }
                ( 
                     p = pathExpr
                     { groupings.add(p);}
                 )+
            { rc = v.leaveGroupByClause(#group, groupings); }
        )
    |
            // empty rule
            { rc = null ; }
    ;

havingClause returns [Object rc]
{
    Object expr = null;
}
    :   #(  having:HAVING
            { v.arriveHavingClause(#having); }
                expr = expression
            { rc = v.leaveHavingClause(#having, expr); }
         )
        |
            // empty rule
            { rc = null; }
    ;

// ----------------------------------
// rules: orderby clause
// ----------------------------------

orderbyClause returns [Object rc]
{
    Object p = null;
    Object o = null;
    List orderings = new ArrayList();
}
    :   #(  order:ORDER
            { v.arriveOrderByClause(#order); }
                (
                    p = pathExpr
                    (
                        o = asc[p]
                        |
                        o = desc[p]
                    )
                    { orderings.add(o); }
                )+
            { rc = v.leaveOrderByClause(#order, orderings); }
        )
    |   // empty rule
        { rc = null;}
    ;

asc [Object pathExpr] returns [Object rc]
    :   a:ASC
         // arrive intentionally omitted
        { rc = v.leaveAsc(#a, pathExpr); }
    ;

desc [Object pathExpr] returns [Object rc]
    :   d:DESC
         // arrive intentionally omitted
        { rc = v.leaveDesc(#d, pathExpr); }
    ;

// ----------------------------------
// rules: update statement
// ----------------------------------

updateStmt
{
    Object rvd = null;
    Object set = null;
    Object where = null;
}
    :   #(   u:UPDATE
            { v.arriveUpdateStmt(#u); }
            rvd = rangeVarDecl
            set = setClause
            where = whereClause
            // Note: The result of walking the tree can be obtained from the
            // visitor.
            { v.leaveUpdateStmt(#u, rvd, set, where); }
          )
      ;
setClause returns [Object rc]
{
    Object assignment = null;
    List assignments = new ArrayList();
}
    :   #(  set:SET
            { v.arriveSetClause(#set); }
            (
                assignment = setValue
                { assignments.add(assignment); }
            )+
            { rc = v.leaveSetClause(#set, assignments); }
        )
    ;

setValue returns [Object rc ]
{
    Object path = null;
    Object value = null;
}
    :   #(  equal:EQUAL
            { v.arriveSetValue(#equal); }
            (
                path = pathExpr
                value = literal
            )   
            { rc =  v.leaveSetValue(#equal, path, value); }
        )
    ;

// ----------------------------------
// rules: delete statement
// ----------------------------------

deleteStmt
{
    Object rvd = null;
    Object where = null;
}
    :   #(  d:DELETE
            { v.arriveDeleteStmt(#d); }
            (
                rvd = rangeVarDecl
                where = whereClause
            )
            // Note: The result of walking the tree can be obtained from the
            // visitor.
            { v.leaveDeleteStmt(#d, rvd, where); }
        )
    ;

// ----------------------------------
// rules: expression
// ----------------------------------

expression returns [Object rc]
    :   rc = conditionalExpr
    |   rc = relationalExpr
    |   rc = binaryArithmeticExpr
    |   rc = unaryExpr
    |   rc = betweenExpr
    |   rc = likeExpr
    |   rc = inExpr
    |   rc = nullComparisonExpr
    |   rc = emptyCollectionComparisonExpr
    |   rc = collectionMemberExpr
    |   rc = function
    |   rc = primary
    ;

conditionalExpr returns [Object rc]
{
    Object lhs = null;
    Object rhs = null;
}
    :   #(  a:AND
            { v.arriveConditionalExprAnd(#a); }
            lhs = expression
            rhs = expression
            { rc = v.leaveConditionalExprAnd(#a, lhs, rhs); }
        )
    |   #(  o:OR
            { v.arriveConditionalExprOr(#o); }
            lhs = expression
            rhs = expression
            { rc = v.leaveConditionalExprOr(#o, lhs, rhs); }
        )
    ;

relationalExpr returns [Object rc]
{
    Object lhs = null;
    Object rhs = null;
}
    :   #(  eq:EQUAL
            { v.arriveRelationalExprEqual(#eq); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprEqual(#eq, lhs, rhs); }
        )
    |   #(  ee:ENTITY_EQUAL
            { v.arriveRelationalExprEntityEqual(#ee); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprEntityEqual(#ee, lhs, rhs); }
        )
    |   #(  ne:NOT_EQUAL
            { v.arriveRelationalExprNotEqual(#ne); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprNotEqual(#ne, lhs, rhs); }
        )
    |   #(  ene:ENTITY_NOT_EQUAL
            { v.arriveRelationalExprEntityNotEqual(#ene); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprEntityNotEqual(#ene, lhs, rhs); }
        )
    |   #(  lt:LT
            { v.arriveRelationalExprLT(#lt); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprLT(#lt, lhs, rhs); }
        )
    |   #(  le:LE
            { v.arriveRelationalExprLE(#le); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprLE(#le, lhs, rhs); }
        )
    |   #(  gt:GT
            { v.arriveRelationalExprGT(#gt); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprGT(#gt, lhs, rhs); }
        )
    |   #(  ge:GE
            { v.arriveRelationalExprGE(#ge); }
            lhs = expression
            rhs = expression
            { rc = v.leaveRelationalExprGE(#ge, lhs, rhs); }
        )
    ;

binaryArithmeticExpr returns [Object rc]
{
    Object lhs = null;
    Object rhs = null;
}
    :   #(  plus:PLUS
            { v.arriveBinaryArithmeticExprPlus(#plus); }
            lhs = expression
            rhs = expression
            { rc = v.leaveBinaryArithmeticExprPlus(#plus, lhs, rhs); }
        )
    |   #(  minus:MINUS
            { v.arriveBinaryArithmeticExprMinus(#minus); }
            lhs = expression
            rhs = expression
            { rc = v.leaveBinaryArithmeticExprMinus(#minus, lhs, rhs); }
        )
    |   #(  star:STAR
            { v.arriveBinaryArithmeticExprStar(#star); }
            lhs = expression
            rhs = expression
            { rc = v.leaveBinaryArithmeticExprStar(#star, lhs, rhs); }
        )
    |   #(  div:DIV
            { v.arriveBinaryArithmeticExprDiv(#div); }
            lhs = expression
            rhs = expression
            { rc = v.leaveBinaryArithmeticExprDiv(#div, lhs, rhs); }
        )
    ;

unaryExpr returns [Object rc]
{
    Object expr = null;
}
    :   #(  up:UNARY_PLUS
            { v.arriveUnaryExprPlus(#up); }
            expr = expression
            { rc = v.leaveUnaryExprPlus(#up, expr); }
        )
    |   #(  um:UNARY_MINUS
            { v.arriveUnaryExprMinus(#um); }
            expr = expression
            { rc = v.leaveUnaryExprMinus(#um, expr); }
        )
    |   #(  not:NOT
            { v.arriveUnaryExprNot(#not); }
            expr = expression
            { rc = v.leaveUnaryExprNot(#not, expr); }
        )
    ;

betweenExpr returns [Object rc]
{
    Object expr = null;
    Object lower = null;
    Object upper = null;
}
    :   #(  b:BETWEEN
            { v.arriveBetweenExpr(#b, true); }
            expr = expression
            lower = expression
            upper = expression
            { rc = v.leaveBetweenExpr(#b, expr, lower, upper, true); }
        )
    |   #(  nb:NOT_BETWEEN
            { v.arriveBetweenExpr(#nb, false); }
            expr = expression
            lower = expression
            upper = expression
            { rc = v.leaveBetweenExpr(#nb, expr, lower, upper, false); }
        )
    ;

likeExpr returns [Object rc]
{
    Object expr = null;
    Object pattern = null;
    Object esc = null;
}
    :   #(  l:LIKE
            { v.arriveLikeExpr(#l, true); }
            expr = expression
            (
                pattern = stringLiteral
                |
                pattern = parameter
            )
            esc = escape
            { rc = v.leaveLikeExpr(#l, expr, pattern, esc, true); }
        )
    |   #(  nl:NOT_LIKE
            { v.arriveLikeExpr(#nl, false); }
            expr = expression
            (
                pattern = stringLiteral
                |
                pattern = parameter
            )
            esc = escape
            { rc = v.leaveLikeExpr(#nl, expr, pattern, esc, false); }
        )
    ;

escape returns [Object rc]
{
    Object o = null;
}
    :   #(  e:ESCAPE
            { v.arriveEscape(#e); }
            (
                o = singleCharStringLiteral
                |
                o = parameter
            )
            { rc = v.leaveEscape(#e, o); }
        )
    |   // empty rule
        { rc = null; }
    ;

singleCharStringLiteral returns [Object rc]
    :   s:STRING_LITERAL
        // arrive intentionally omitted
        { rc = v.leaveSingleCharStringLiteral(#s); }
    ;

inExpr returns [Object rc]
{
    Object expr = null;
    Object p = null;
    List primaries = new ArrayList();
}
    :   #(  i:IN
            { v.arriveInExpr(#i, true); }
            expr = expression
            (
                p = primary
                { primaries.add(p); }
            )+
            { rc = v.leaveInExpr(#i, expr, primaries, true); }
        )
    |   #(  ni:NOT_IN
            { v.arriveInExpr(#ni, false); }
            expr = expression
            (
                p = primary
                { primaries.add(p); }
            )+
            { rc = v.leaveInExpr(#ni, expr, primaries, false); }
        )
    ;


nullComparisonExpr returns [Object rc]
{
    Object expr = null;
}
    :   #(  n:NULL
            { v.arriveNullComparisonExpr(#n, true); }
            expr = expression
            { rc = v.leaveNullComparisonExpr(#n, expr, true); }
        )
    |   #(  nn:NOT_NULL
            { v.arriveNullComparisonExpr(#nn, false); }
            expr = expression
            { rc = v.leaveNullComparisonExpr(#nn, expr, false); }
        )
    ;

emptyCollectionComparisonExpr returns [Object rc]
{
    Object expr = null;
}
    :   #(  e:EMPTY
            { v.arriveEmptyCollectionComparisonExpr(#e, true); }
            expr = expression
            { rc = v.leaveEmptyCollectionComparisonExpr(#e, expr, true); }
        )
    |   #(  ne:NOT_EMPTY
            { v.arriveEmptyCollectionComparisonExpr(#ne, false); }
            expr = expression
            { rc = v.leaveEmptyCollectionComparisonExpr(#ne, expr, false); }
        )
    ;

collectionMemberExpr returns [Object rc]
{
    Object expr = null;
    Object cmr = null;
}
    :   #(  m:MEMBER
            { v.arriveCollectionMemberExpr(#m, true); }
            expr = expression
            cmr = expression
            { rc = v.leaveCollectionMemberExpr(#m, expr, cmr, true); }
        )
    |   #(  nm: NOT_MEMBER
            { v.arriveCollectionMemberExpr(#nm, false); }
            expr = expression
            cmr = expression
            { rc = v.leaveCollectionMemberExpr(#nm, expr, cmr, false); }
        )
    ;

function returns [Object rc]
    :   rc = concat
    |   rc = substring
    |   rc = trim
    |   rc = lower
    |   rc = upper
    |   rc = length
    |   rc = locate
    |   rc = abs
    |   rc = sqrt
    |   rc = mod
    ;

concat returns [Object rc]
{
    Object lhs = null;
    Object rhs = null;
}
    :   #(  c:CONCAT
            { v.arriveConcat(#c); }
            lhs = expression
            rhs = expression
            { rc = v.leaveConcat(#c, lhs, rhs); }
        )
    ;

substring returns [Object rc]
{
    Object expr = null;
    Object start = null;
    Object length = null;
}
    :   #(  s:SUBSTRING
            { v.arriveSubstring(#s); }
            expr = expression
            start = wildcard
            length = wildcard
            { rc = v.leaveSubstring(#s, expr, start, length); }
        )
    ;

wildcard returns [Object rc]
    :   w:.
        // arrive intentionally omitted
        { rc = v.leaveWildcard(#w); }
    ;

lower returns [Object rc]
{
    Object expr = null;
}
    :   #(  lower:LOWER
            { v.arriveLower(#lower); }
            expr = expression
            { rc = v.leaveLower(#lower, expr); }
        )
    ;

upper returns [Object rc]
{
    Object expr = null;
}
    :   #(  upper:UPPER
            { v.arriveUpper(#upper); }
            expr = expression
            { rc = v.leaveUpper(#upper, expr); }
        )
    ;

trim returns [Object rc]
{
    List stuff = new ArrayList();
    Object str = null;
}
    :   #(  trim:TRIM
            { v.arriveTrim(#trim); }
            trimDef[stuff]
            str = expression
            { rc = v.leaveTrim(#trim, stuff.get(0), stuff.get(1), str); }
        )
    ;

trimDef [List stuff]
{
    Object t = null;
}
    :   t = trimSpec
        s:STRING_LITERAL
        { stuff.add(t); stuff.add(s.getText()); }
    ;

trimSpec returns [Object rc]
    :   l:LEADING { rc = l.getText(); } 
    |   t:TRAILING { rc = t.getText(); } 
    |   b:BOTH { rc = b.getText(); } 
    ;

length returns [Object rc]
{
    Object expr = null;
}
    :   #(  length:LENGTH
            { v.arriveLength(#length); }
            expr = expression
            { rc = v.leaveLength(#length, expr); }
        )
    ;

locate returns [Object rc]
{
    Object pattern = null;
    Object str = null;
    Object start = null;
}
    :   // EJBQL: LOCATE(pattern, string) ->
        // JDOQL: (string.indexOf(pattern) + 1)
        // EJBQL: LOCATE(pattern, string, start) ->
        // JDOQL: (string.indexOf(pattern, start - 1) + 1)
        #(  loc:LOCATE
            { v.arriveLocate(#loc); }
            pattern = expression
            str = expression
            start = locateStartPos
            { rc = v.leaveLocate(#loc, pattern, str, start); }
        )
    ;

locateStartPos returns [Object rc]
    :   rc = wildcard
    |   // empty rule
        { rc = null; }
    ;

abs returns [Object rc]
{
    Object expr = null;
}
    :   #(  abs:ABS
            { v.arriveAbs(#abs); }
            expr = expression
            { rc = v.leaveAbs(#abs, expr); }
        )
    ;

sqrt returns [Object rc]
{
    Object expr = null;
}
    :   #(  sqrt:SQRT
            { v.arriveSqrt(#sqrt); }
            expr = expression
            { rc = v.leaveSqrt(#sqrt, expr); }
        )
    ;

mod returns [Object rc]
{
    Object lhs = null;
    Object rhs = null;
}
    :   #(  mod:MOD
            { v.arriveMod(#mod); }
            lhs = expression
            rhs = expression
            { rc = v.leaveMod(#mod, lhs, rhs); }
        )
    ;

primary returns [Object rc]
    :   rc = literal
    |   rc = pathExpr
    |   rc = parameter
    ;

literal returns [Object rc]
    :   tr:TRUE { rc = v.leaveLiteralTrue(#tr); }
    |   fa:FALSE { rc = v.leaveLiteralFalse(#fa); }
    |   s:STRING_LITERAL { rc = v.leaveLiteralString(#s); }
    |   i:INT_LITERAL { rc = v.leaveLiteralInt(#i); }
    |   l:LONG_LITERAL { rc = v.leaveLiteralLong(#l); }
    |   f:FLOAT_LITERAL { rc = v.leaveLiteralFloat(#f); }
    |   d:DOUBLE_LITERAL { rc = v.leaveLiteralDouble(#d); }
    ;

stringLiteral returns [Object rc]
    :   s:STRING_LITERAL
        // arrive intentionally omitted
        // Note: method name intentionally does *not* match rule,
        // but does match that of all other literals.
        { rc = v.leaveLiteralString(#s); }
    ;

pathExpr returns [Object rc]
{
    Object p = null;
    Object f = null;
}
    :   #(  cmp:CMP_FIELD_ACCESS
            { v.arrivePathExprCMPField(#cmp); }
            p = pathExpr
            f = field
            { rc = v.leavePathExprCMPField(#cmp, p, f); }
        )
    |   #(  cmr:SINGLE_CMR_FIELD_ACCESS
            { v.arrivePathExprCMRField(#cmr); }
            p = pathExpr
            f = field
            { rc = v.leavePathExprCMRField(#cmr, p, f); }
        )
    |   #(  coll:COLLECTION_CMR_FIELD_ACCESS
            { v.arrivePathExprCollectionCMRField(#coll); }
            p = pathExpr
            f = field
            { rc = v.leavePathExprCollectionCMRField(#coll, p, f); }
        )
    |   var:IDENTIFICATION_VAR
        { rc = v.leavePathExprIdentificationVar(#var); }
    |   #( dot:DOT
           p = expression // value ignored; assignment silences antlr warning
        )
        {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_UnexpectedNode", //NOI18N
                    dot.getText(), String.valueOf(dot.getType())));
            rc = null; // Silence compiler warning
        }
    |   i:IDENT
        {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_UnexpectedNode", //NOI18N
                    i.getText(), String.valueOf(i.getType())));
            rc = null; // Silence compiler warning
        }
    ;

field returns [Object rc]
    :   cmp:CMP_FIELD
        { rc = v.leaveFieldCMPField(#cmp); }
    |   cmr:SINGLE_CMR_FIELD
        { rc = v.leaveFieldCMRField(#cmr); }
    |   coll:COLLECTION_CMR_FIELD
        { rc = v.leaveFieldCollectionField(#coll); }
    ;

parameter returns [Object rc]
    :   param:POSITIONAL_PARAMETER
        { rc = v.leaveParameter(#param); }
    |   name:NAMED_PARAMETER
        { rc = v.leaveParameter(#name); }
    ;
