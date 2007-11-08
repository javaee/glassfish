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


/*
 * Semantic3.g
 *
 * Created on April 25, 2005
 */

header
{
    package com.sun.persistence.runtime.query.impl;
    
    import java.util.ResourceBundle;
    import com.sun.persistence.utility.I18NHelper;
    import com.sun.persistence.runtime.query.QueryContext;
}

/**
 * This class defines the semantic analysis of the EJBQL3 compiler.
 * Input of this pass is the AST as produced by the parser,
 * that consists of EJBQLAST nodes.
 * The result is a typed EJBQLAST tree.
 *
 * @author  Michael Bouschen
 */
class Semantic3 extends Semantic;

options
{
    importVocab = EJBQL3;
}

{
    /** Type info access helper. */
    protected QueryContext queryContext;
    
    /** The environment in which the query is being compiled. */
    protected Environment env;

    /** The helper. */
    protected Semantic3Helper helper;

    /** I18N support. */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
        Semantic3.class);
    
    /**
     * Initializes the semantic analysis.
     * @param queryContext type info access helper.
     * @param paramSupport parameter info helper.
     * @param env environment in which compilation is taking place
     * @param ejbName the ejb name of the finder/selector method.
     */    
    public void init(QueryContext queryContext, Environment env) {
        this.queryContext = queryContext;
        this.env = env;
        this.helper = new Semantic3Helper(queryContext, env);
        env.setSemanticHelper(helper);
    }

    /** */
    public void reportError(RecognitionException ex) {
        ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_SemanticError"), ex); //NOI18N
    }

    /** */
    public void reportError(String s) {
        ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_SemanticError") + s); //NOI18N
    }
    
}

query
    :   selectStmt
    |   updateStmt
    |   deleteStmt
    ;

selectStmt
    :   #(QUERY fromClause s:selectClause whereClause 
                groupbyClause havingClause o:orderbyClause)
        {
            helper.checkSelectOrderbyClause(#s, #o);
        }
    ;

subquery
    :   #(sub:SUBQUERY fromClause s:simpleSelectClause whereClause 
                   groupbyClause havingClause)
        {
            // use the select clause typoe as the type of the subquery, FIXME
            #sub.setTypeInfo(#s.getTypeInfo());
        }           
    ;

// ----------------------------------
// rules: select clause
// ----------------------------------

selectClause 
    :   #(SELECT distinct ( projection )+ )
    ;

// ----------------------------------
// rules: from clause
// ----------------------------------

fromClause
    :   #(FROM ( identificationVarDecl | collectionMemberDecl )+ )
    ;

// TBD: check oder of join and fetchJoin
identificationVarDecl
    :   rangeVarDecl ( join | fetchJoin )*
    ;

rangeVarDecl
    :   #(RANGE a:abstractSchemaName (var:IDENT)? ) {
            // check abstract schema name
            Object typeInfo = 
                helper.checkAbstractSchemaType(#a);
            #a.setTypeInfo(typeInfo);

            // check identification variable
            if (#var != null) {
                String name = #var.getText();
                Object identVar = new IdentificationVariable(name, typeInfo);
                if (helper.declareIdent(name, identVar) != null) {
                    String text = I18NHelper.getMessage(msgs, 
                        "EXC_MultipleDeclaration", name); //NOI18N
                    ErrorMsg.error(#var.getLine(), #var.getColumn(), text);
                }
                #var.setType(IDENTIFICATION_VAR_DECL);
                #var.setTypeInfo(typeInfo);
            }
        }
    ;

abstractSchemaName
    :   ABSTRACT_SCHEMA_NAME
    ;

join
    :   #(JOIN joinType p:associationPathExpression var:IDENT)  
        {
            Object typeInfo = #p.getTypeInfo();
 
            // check identification variable
            String name = #var.getText();
            Object identVar = new IdentificationVariable(name, typeInfo);
            if (helper.declareIdent(name, identVar) != null) {
                String text = I18NHelper.getMessage(msgs, 
                    "EXC_MultipleDeclaration", name); //NOI18N
                ErrorMsg.error(#var.getLine(), #var.getColumn(), text);
            }
            #var.setType(IDENTIFICATION_VAR_DECL);
            #var.setTypeInfo(typeInfo);
        }
    ;

joinType
    :   INNER | OUTER
    ;

fetchJoin
    :   #(FETCH joinType associationPathExpression) 
    ;

// ----------------------------------
// rules: group by / having clause
// ----------------------------------

groupbyClause
    :   #(GROUP ( groupbyItem )+ )
    |   // empty rule
    ; 

groupbyItem
    :   stateFieldPathExpression
    ;

havingClause
    :   #(HAVING expression)
    |   // empty rule
    ;

// ----------------------------------
// rules: update statement
// ----------------------------------

updateStmt
    :   #(UPDATE rangeVarDecl setClause whereClause )
    ;

setClause
    :   #(SET ( setValue )+ )
    ;

setValue
    :   #(EQUAL qualifieldStateFieldExpression expression)
    ;

// ----------------------------------
// rules: delete statement
// ----------------------------------

deleteStmt
    :   #(DELETE rangeVarDecl whereClause )
    ;

// ----------------------------------
// rules: expression
// ----------------------------------

constructorExpr 
    :   #(NEW IDENT ( constructorArg )* )
    ;

constructorArg
    :   singleValuedPathExpression
    |   aggregateSelectExpr
    ;

expression
    :   conditionalExpr
    |   relationalExpr
    |   binaryArithmeticExpr
    |   unaryExpr
    |   betweenExpr
    |   likeExpr
    |   inExpr
    |   nullComparisonExpr
    |   emptyCollectionComparisonExpr
    |   collectionMemberExpr
    |   exists
    |   function
    |   primary
    ;

relationalExpr
    :   #( op1:EQUAL left1:expression right1:relationalExprRHS )
        {
            #op1.setTypeInfo(helper.analyseEqualityExpr(#op1, #left1, #right1));
            if (helper.isEntityBeanValue(#left1)
                && helper.isEntityBeanValue(#right1)) {
                #op1.setType(ENTITY_EQUAL);
            }
        }
    |   #( op2:NOT_EQUAL left2:expression right2:relationalExprRHS )
        {
            #op2.setTypeInfo(helper.analyseEqualityExpr(#op2, #left2, #right2));
            if (helper.isEntityBeanValue(#left2)
                && helper.isEntityBeanValue(#right2)) {
                #op2.setType(ENTITY_NOT_EQUAL);
            }
        }
    |   #( op3:LT left3:expression right3:relationalExprRHS )
        {
            #op3.setTypeInfo(helper.analyseRelationalExpr(#op3, #left3, #right3));
        }
    |   #( op4:LE left4:expression right4:relationalExprRHS )
        {
            #op4.setTypeInfo(helper.analyseRelationalExpr(#op4, #left4, #right4));
        }
    |   #( op5:GT left5:expression right5:relationalExprRHS )
        {
            #op5.setTypeInfo(helper.analyseRelationalExpr(#op5, #left5, #right5));
        }
    |   #( op6:GE left6:expression right6:relationalExprRHS )
        {
            #op6.setTypeInfo(helper.analyseRelationalExpr(#op6, #left6, #right6));
        }
    ;

relationalExprRHS
    :   expression
    |   allOrAnyExpr
    ;

allOrAnyExpr
    :   #(op1:ALL sub1:subquery)
        {
            #op1.setTypeInfo(#sub1.getTypeInfo());
        }
    |   #(op2:ANY sub2:subquery)
        {
            #op2.setTypeInfo(#sub2.getTypeInfo());
        }
    |   #(op3:SOME sub3:subquery)
        {
            #op3.setTypeInfo(#sub3.getTypeInfo());
        }
    ;

exists
    :   #(op1:EXISTS sub1:subquery)
        {
            // TBD: check subquery
            #op1.setTypeInfo(queryContext.getBooleanType());
        }
    |   #(op2:NOT_EXISTS subquery)
        {
            // TBD: check sub2:subquery
            #op2.setTypeInfo(queryContext.getBooleanType());
        }
    ;

function
    :   concat
    |   substring
    |   trim
    |   upper
    |   lower
    |   length
    |   locate
    |   abs
    |   sqrt
    |   mod
    |   dateFunctions
    ;

trim
    :   #(op:TRIM t:trimDef e:expression)
        {
            #op.setTypeInfo(helper.analyseTrimFunction(#t, #e));
        }
    ;

trimDef
    :   trimSpec STRING_LITERAL
    ;

trimSpec
    :   LEADING | TRAILING | BOTH
    ;

upper
    :   #(op:UPPER e:expression)
        {
            #op.setTypeInfo(helper.analyseStringExpr(#e)
                ? queryContext.getStringType() : queryContext.getErrorType());
        }
    ;

lower
    :   #(op:LOWER e:expression)
        {
            #op.setTypeInfo(helper.analyseStringExpr(#e)
                ? queryContext.getStringType() : queryContext.getErrorType());
        }
    ;

dateFunctions
    :   CURRENT_DATE
    |   CURRENT_TIME
    |   CURRENT_TIMESTAMP
    ; 

associationPathExpression
    // this is either collectionValuedPathExpression or 
    // singleValuedAssocationPathExpression, so a access expression 
    // ending in a relationship field
    :   pathExpression 
    ;

stateFieldPathExpression
    :   cmpPathExpression
    ;

qualifieldStateFieldExpression
    :   IDENT
    |   stateFieldPathExpression
    ;

inputParameter
    :   pos:POSITIONAL_PARAMETER
        {
            Object typeInfo = queryContext.getTypeInfo(
                env.getParameterType(#pos.getText()));
            #pos.setTypeInfo(typeInfo);
        }
    |   name:NAMED_PARAMETER
        {
            #name.setTypeInfo(queryContext.getUnknownType());
        }
    ;

