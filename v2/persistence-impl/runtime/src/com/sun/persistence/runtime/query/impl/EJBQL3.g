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
 * EJBQL3.g
 *
 * Created on April 25, 2005
 */

header
{
    package com.sun.persistence.runtime.query.impl;

    import antlr.MismatchedTokenException;
    import antlr.MismatchedCharException;
    import antlr.NoViableAltException;
    import antlr.NoViableAltForCharException;
    import antlr.TokenStreamRecognitionException;
    
    import java.util.ResourceBundle;
    import com.sun.persistence.utility.I18NHelper;
}

//===== Lexical Analyzer Class Definitions =====

/**
 * This class defines the lexical analysis for the EJBQL3 compiler.
 *
 * @author  Michael Bouschen
 */
class EJBQL3Lexer extends EJBQLLexer;

options {
    exportVocab = EJBQL3Lexer;
    importVocab = EJBQL;
}

tokens {
    // EJBQL 3 keywords 

    // FROM clause
    LEFT = "left"; //NOI18N
    INNER = "inner"; //NOI18N
    OUTER = "outer"; //NOI18N
    JOIN = "join"; //NOI18N
    FETCH = "fetch"; //NOI18N

    // UPDATE clause
    UPDATE = "update"; //NOI18N
    SET = "set"; //NOI18N

    // DELETE clause
    DELETE = "delete"; //NOI18N

    // SELECT clause
    NEW = "new"; //NOI18N

    // GROUPBY clause
    GROUP = "group"; //NOI18N
    HAVING = "having"; //NOI18N

    // expressions
    EXISTS = "exists"; //NOI18N
    ALL = "all"; //NOI18N
    ANY = "any"; //NOI18N
    SOME = "some"; //NOI18N

    // functions
    TRIM = "trim"; //NOI18N
    LOWER = "lower"; //NOI18N
    UPPER = "upper"; //NOI18N
    LEADING = "leading"; //NOI18N
    TRAILING = "trailing"; //NOI18N
    BOTH = "both"; //NOI18N
    CURRENT_DATE = "current_date"; //NOI18N
    CURRENT_TIME = "current_time"; //NOI18N
    CURRENT_TIMESTAMP = "current_timestamp"; //NOI18N
}

NAMED_PARAMETER
    : ':' IDENT_INTERNAL
    ;

//===== Parser Class Definitions =====

/**
 * This class defines the syntax analysis (parser) of the EJBQL3 compiler.
 *
 * @author  Michael Bouschen
 */
class EJBQL3Parser extends EJBQLParser;

options {
    importVocab = EJBQL3Lexer;
    exportVocab = EJBQL3;
    ASTLabelType = "EJBQLASTImpl"; // NOI18N
}

tokens {
    SUBQUERY;
    NOT_EXISTS;
}

root
    :   selectStmt
    |   updateStmt
    |   deleteStmt
    ;

selectStmt!
    :   s:selectClause f:fromClause w:whereClause 
        g:groupbyClause h:havingClause o:orderbyClause EOF!
        {
            // switch the order of subnodes: the fromClause should come first, 
            // because it declares the identification variables used in the 
            // selectClause and the whereClause
            #selectStmt = #(#[QUERY,"QUERY"], #f, #s, #w); //NOI18N
            if (#g != null) {
                #selectStmt.addChild(#g);
            }
            if (#h != null) {
                #selectStmt.addChild(#h);
            }
            if (#o != null) {
                #selectStmt.addChild(#o);
            }
        }
    ;

// ----------------------------------
// rules: select clause
// ----------------------------------

selectClause
    :   SELECT^ ( DISTINCT )? projection ( COMMA! projection )*
    ;

projection
    :   p:pathExpr
    |   OBJECT! LPAREN! IDENT RPAREN! // Note, skipping keyword OBJECT
    |   aggregateSelectExpr
    ;

// ----------------------------------
// rules: from clause
// ----------------------------------

fromClause
    :   FROM^ identificationVarDecl 
        ( COMMA! ( identificationVarDecl | collectionMemberDecl ) )*
    ;

identificationVarDecl
    :   strictRangeVarDecl ( t:joinType! ( join[#t] | fetchJoin[#t] ) )*
    ;

strictRangeVarDecl!
    :   a:abstractSchemaName ( AS! )? i:IDENT
        {
            #strictRangeVarDecl = #(#[RANGE,"RANGE"], #a, #i); //NOI18N
        }
    ;
    
rangeVarDecl!
    :   a:abstractSchemaName ( AS! )? ( i:IDENT )?
        {
            #rangeVarDecl = #(#[RANGE,"RANGE"], #a); //NOI18N
            if (#i != null) {
                #rangeVarDecl.addChild(#i); 
            }
        }
    ;

joinType
    :   LEFT! ( o:OUTER )? 
        {
            if (#o == null) {
                #joinType = #[OUTER,"outer"]; //NOI18N
            }
        }
    |   INNER
    |   // empty rule
        {
            // default to INNER
            #joinType = #[INNER,"inner"];
        }
    ;

join [EJBQLASTImpl joinType]
    :   j:JOIN^ p:pathExpr ( AS! )? i:IDENT
        {
            // attach the joinType as first child
            #j.setFirstChild(joinType);
            joinType.setNextSibling(#p);
        }
    ;

fetchJoin [EJBQLASTImpl joinType]
    :   JOIN! f:FETCH^ p:pathExpr
        {
            // attach the joinType as first child
            #f.setFirstChild(joinType);
            joinType.setNextSibling(#p);
        }
    ;


// ----------------------------------
// rules: group by / having clause
// ----------------------------------

groupbyClause
    :   GROUP^ BY! groupbyItem ( COMMA! groupbyItem )*
    |   // empty rule
    ; 

groupbyItem
    :   pathExpr
    ;

havingClause
    :   HAVING^ conditionalExpr
    |   // empty rule
    ;

// ----------------------------------
// rules: update statement
// ----------------------------------

updateStmt
    :   UPDATE^ rangeVarDecl setClause whereClause EOF!
    ;

setClause
    :   SET^ setValue ( COMMA! setValue )*
    ;

setValue
    :   pathExpr EQUAL^ conditionalExpr
    ;

// ----------------------------------
// rules: delete statement
// ----------------------------------

deleteStmt
    :   DELETE^ FROM! rangeVarDecl whereClause EOF!
    ;


// ----------------------------------
// rules: expression
// ----------------------------------

constructorExpr 
    :   NEW^ IDENT 
        LPAREN! constructorArg ( COMMA! constructorArg )* RPAREN!
    ;

constructorArg
    :   pathExpr
    |   aggregateSelectExpr
    ;

subquery!
    :   s:simpleSelectClause f:fromClause w:whereClause 
        g:groupbyClause h:havingClause
        {
            // switch the order of subnodes: the fromClause should come first, 
            // because it declares the identification variables used in the 
            // selectClause and the whereClause
            #subquery = #(#[SUBQUERY,"SUBQUERY"], #f, #s, #w); //NOI18N
            if (#g != null) {
                #subquery.addChild(#g);
            }
            if (#h != null) {
                #subquery.addChild(#h);
            }
        }
    ;

conditionalFactor
    :   ( n:NOT^ )?  
        (   conditionalPrimary
        |   e:existsExpr [#n != null]
            {
                #conditionalFactor = #e;
            }
        )
    ;

existsExpr [boolean not]
    :   EXISTS^ LPAREN! subquery RPAREN!
        {
            if (not) {
                // represent NOT EXISTS as single token NOT_EXISTS
                #EXISTS.setType(NOT_EXISTS); 
            }
        }
    ;

comparisonExpr
    :   arithmeticExpr ( ( EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^ ) 
        ( arithmeticExpr | allOrAnyExpr) )*
    ;

allOrAnyExpr
    :   ( ALL^ | ANY^ | SOME^ ) LPAREN! subquery RPAREN! 
    ;

function
    :   CONCAT^ LPAREN! conditionalExpr COMMA! conditionalExpr RPAREN!
    |   SUBSTRING^ LPAREN! conditionalExpr COMMA! conditionalExpr COMMA! conditionalExpr RPAREN!
    |   TRIM^ LPAREN! trimDef conditionalExpr RPAREN!
    |   LOWER^ LPAREN! conditionalExpr RPAREN!
    |   UPPER^ LPAREN! conditionalExpr RPAREN!
    |   LENGTH^ LPAREN! conditionalExpr RPAREN!
    |   LOCATE^ LPAREN! conditionalExpr COMMA! conditionalExpr ( COMMA! conditionalExpr )? RPAREN!
    |   ABS^ LPAREN! conditionalExpr RPAREN! 
    |   SQRT^ LPAREN! conditionalExpr RPAREN!
    |   MOD^ LPAREN! conditionalExpr COMMA! conditionalExpr RPAREN!
    |   CURRENT_DATE
    |   CURRENT_TIME
    |   CURRENT_TIMESTAMP
    ;

trimDef
    :   trimSpec stringLiteral FROM!
    |   c:stringLiteral FROM!
        {
            // default trimSpec to BOTH
            #trimDef = #(#[BOTH,"both"]);
            #trimDef.setNextSibling(#c);
        } 
    |   ( FROM! )?
        {
            // default trimSpec to BOTH and
            // the trim character to blank
            #trimDef = #(#[BOTH,"both"]);
            #trimDef.setNextSibling(#(#[STRING_LITERAL," "]));
        } 
    ;

trimSpec
    :   LEADING | TRAILING | BOTH
    ;

inputParameter
    :   POSITIONAL_PARAMETER
    |   NAMED_PARAMETER
    ;
