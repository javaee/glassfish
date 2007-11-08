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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.internal.parsing.ejbql;


/**
 * This class provides for versioning of the grammar
 */
public class GrammarSpecial {
    //This shows the grammar (in comment form), copied directly from "EJBQLParser.g"
    public static void grammar() {

        /*
        // Added 20/12/2000 JED. Define the package for the class
        header {
        package oracle.toplink.essentials.internal.parsing.ejbql;
        }

        class EJBQLParser extends Parser;
        options {
        exportVocab=EJBQL;
        k = 2; // This is the number of tokens to look ahead to
        buildAST = true;
        }

        tokens {
        FROM="FROM";
        WHERE="WHERE";
        OR="OR";
        AND="AND";
        TRUE="TRUE";
        FALSE="FALSE";
        BETWEEN="BETWEEN";
        CONCAT="CONCAT";
        SUBSTRING="SUBSTRING";
        LENGTH="LENGTH";
        LOCATE="LOCATE";
        ABS="ABS";
        SQRT="SQRT";
        IS="IS";
        UNKNOWN="UNKNOWN";
        LIKE="LIKE";
        NOT="NOT";
        PERCENT="%";
        UNDERSCORE="_";
        IN="IN";
        NULL="NULL";
        EMPTY="EMPTY";
        AS="AS";
        }

        document
        : (fromClause) (whereClause)?
        ;

        //================================================
        fromClause
        : from identificationVariableDeclaration (COMMA  identificationVariableDeclaration)*
        ;

        from
        : FROM {matchedFrom();}
        ;

        identificationVariableDeclaration
        : collectionMemberDeclaration
        | rangeVariableDeclaration
        ;

        collectionMemberDeclaration
        : identifier IN singleValuedNavigation
        ;

        rangeVariableDeclaration
        : abstractSchemaName (AS)? abstractSchemaIdentifier
        ;

        singleValuedPathExpression
        //    : (singleValuedNavigation | identifier) DOT^ identifier
        : singleValuedNavigation
        ;

        singleValuedNavigation
        : identifier dot (identifier dot)* identifier
        ;

        //collectionValuedPathExpression
        //    : identifier DOT^ (identifier DOT^)* identifier

        //================================================

        //from
        //    : (FROM) {matchedFrom();} abstractSchemaClause (whereClause)?
        //    ;

        //Abstract Schema
        //abstractSchemaClause
        //    : abstractSchemaName (abstractSchemaVariableClause)?
        //    ;

        abstractSchemaName
        : TEXTCHAR {matchedAbstractSchemaName();}
        ;

        abstractSchemaIdentifier
        : identifier {matchedAbstractSchemaIdentifier();}
        ;

        dot
        : DOT^ {matchedDot();}
        ;

        identifier
        : TEXTCHAR {matchedIdentifier();}
        ;

        whereClause
        : WHERE {matchedWhere();} conditionalExpression
        ;

        conditionalExpression
        : conditionalTerm (OR{matchedOr();} conditionalTerm {finishedOr();})*
        ;

        conditionalTerm
        : {conditionalTermFound();} conditionalFactor (AND{matchedAnd();} conditionalFactor {finishedAnd();})*
        ;

        conditionalFactor
        : conditionalTest
        ;

        conditionalTest
        : conditionalPrimary (isExpression (NOT)? (literalBoolean | UNKNOWN))?
        ;

        conditionalPrimary
        : simpleConditionalExpression
        | (LEFT_ROUND_BRACKET {matchedLeftRoundBracket();} conditionalExpression RIGHT_ROUND_BRACKET {matchedRightRoundBracket();})
        ;

        simpleConditionalExpression
        : comparisonLeftOperand comparisonRemainder
        ;

        comparisonLeftOperand
        : singleValuedPathExpression
        ;

        comparisonRemainder
        : equalsRemainder
        | betweenRemainder
        | likeRemainder
        | inRemainder
        | nullRemainder
        ;

        comparisonExpression
        : expressionOperandNotMagnitude equals expressionOperandNotMagnitude {finishedEquals();}
        | expressionOperandMagnitude comparisonOperator expressionOperandMagnitude {finishedComparisonExpression();}
        ;

        equalsRemainder
        : equals (stringExpression | literalNumeric | singleValuedPathExpression) {finishedEquals();}
        ;

        betweenRemainder
        : (NOT {matchedNot();})? ((BETWEEN {matchedBetween();}) literalNumeric) AND
            {matchedAndAfterBetween();} literalNumeric {finishedBetweenAnd();}
        ;

        likeRemainder
        : (NOT {matchedNot();})? (LIKE {matchedLike();}) literalString {finishedLike();}
        ;

        inRemainder
        : (NOT {matchedNot();})? (IN {matchedIn();})
            (LEFT_ROUND_BRACKET
                (literalString|literalNumeric)
                    (COMMA (literalString|literalNumeric))*
            RIGHT_ROUND_BRACKET)
            {finishedIn();}
        ;

        emptyCollectionRemainder
        : IS (NOT {matchedNot();})? EMPTY {matchedEmpty();} {finishedEmpty();}
        ;

        nullRemainder
        : IS (NOT {matchedNot();})? NULL {matchedNull();} {finishedNull();}
        ;

        arithmeticExpression
        : literal
        ;

        stringExpression
        : literalString
        ;

        expressionOperandNotMagnitude
        : literalString
        | singleValuedPathExpression
        ;

        expressionOperandMagnitude
        : literalNumeric
        //    | singleValuedReferenceExpression
        ;

        singleValueDesignator
        : singleValuedPathExpression
        ;

        singleValuedReferenceExpression
        : variableName (DOT^ variableName)?
        ;

        singleValuedDesignator
        : scalarExpression
        ;

        scalarExpression
        : arithmeticExpression
        ;

        variableName
        : TEXTCHAR {matchedVariableName();}
        ;

        isExpression
        : (IS {matchedIs();})
        ;

        //Literals and Low level stuff

        literal
        : literalNumeric
        | literalBoolean
        | literalString
        ;

        literalNumeric
        : NUM_INT {matchedInteger();}
        | NUM_FLOAT^ {matchedFloat();}
        ;

        literalBoolean
        : TRUE {matchedTRUE();}
        | FALSE {matchedFALSE();}
        ;

        // Added Jan 9, 2001 JED
        literalString
        : STRING_LITERAL {matchedString();}
        ;

        // Added 20/12/2000 JED
        comparisonOperator
        : (equals|greaterThan|greaterThanEqualTo|lessThan|lessThanEqualTo|notEqualTo)
        ;

        equals
        : (EQUALS) {matchedEquals();}
        ;

        greaterThan
        : (GREATER_THAN) {matchedGreaterThan();}
        ;

        greaterThanEqualTo
        : GREATER_THAN_EQUAL_TO {matchedGreaterThanEqualTo();}
        ;

        lessThan
        : (LESS_THAN) {matchedLessThan();}
        ;

        lessThanEqualTo
        : LESS_THAN_EQUAL_TO {matchedLessThanEqualTo();}
        ;

        notEqualTo
        : (NOT_EQUAL_TO) {matchedNotEqualTo();}
        ;

        // End of addition 20/12/2000 JED

        class EJBQLLexer extends Lexer;
        options {
        k = 4;
        exportVocab=EJBQL;
        charVocabulary = '\3'..'\377';
        caseSensitive=true;
        }

        // hexadecimal digit (again, note it's protected!)
        protected
        HEX_DIGIT
        :    ('0'..'9'|'A'..'F'|'a'..'f')
        ;

        WS    : (' ' | '\t' | '\n' | '\r')+
        { $setType(Token.SKIP); } ;

        LEFT_ROUND_BRACKET
        : '('
        ;

        RIGHT_ROUND_BRACKET
        : ')'
        ;

        COMMA
        : ','
        ;

        TEXTCHAR
        : ('a'..'z' | 'A'..'Z' | '_')+
        ;

        // a numeric literal
        NUM_INT
        {boolean isDecimal=false;}
        :    '.' {_ttype = DOT;}
                (('0'..'9')+ (EXPONENT)? (FLOAT_SUFFIX)? { _ttype = NUM_FLOAT; })?
        |    (    '0' {isDecimal = true;} // special case for just '0'
                (    ('x'|'X')
                    (                                            // hex
                        // the 'e'|'E' and float suffix stuff look
                        // like hex digits, hence the (...)+ doesn't
                        // know when to stop: ambig.  ANTLR resolves
                        // it correctly by matching immediately.  It
                        // is therefor ok to hush warning.
                        options {
                            warnWhenFollowAmbig=false;
                        }
                    :    HEX_DIGIT
                    )+
                |    ('0'..'7')+                                    // octal
                )?
            |    ('1'..'9') ('0'..'9')*  {isDecimal=true;}        // non-zero decimal
            )
            (    ('l'|'L')

            // only check to see if it's a float if looks like decimal so far
            |    {isDecimal}?
                (    '.' ('0'..'9')* (EXPONENT)? (FLOAT_SUFFIX)?
                |    EXPONENT (FLOAT_SUFFIX)?
                |    FLOAT_SUFFIX
                )
                { _ttype = NUM_FLOAT; }
            )?
        ;

        // a couple protected methods to assist in matching floating point numbers
        protected
        EXPONENT
        :    ('e'|'E') ('+'|'-')? ('0'..'9')+
        ;


        protected
        FLOAT_SUFFIX
        :    'f'|'F'|'d'|'D'
        ;

        EQUALS
        : '='
        ;

        GREATER_THAN
        : '>'
        ;

        GREATER_THAN_EQUAL_TO
        : ">="
        ;

        LESS_THAN
        : '<'
        ;

        LESS_THAN_EQUAL_TO
        : "<="
        ;

        NOT_EQUAL_TO
        : "<>"
        ;

        // Added Jan 9, 2001 JED
        // string literals
        STRING_LITERAL
        : '"' (ESC|~('"'|'\\'))* '"'
        ;

        // Added Jan 9, 2001 JED
        // escape sequence -- note that this is protected; it can only be called
        //   from another lexer rule -- it will not ever directly return a token to
        //   the parser
        // There are various ambiguities hushed in this rule.  The optional
        // '0'...'9' digit matches should be matched here rather than letting
        // them go back to STRING_LITERAL to be matched.  ANTLR does the
        // right thing by matching immediately; hence, it's ok to shut off
        // the FOLLOW ambig warnings.
        protected
        ESC
        :    '\\'
            (    'n'
            |    'r'
            |    't'
            |    'b'
            |    'f'
            |    '"'
            |    '\''
            |    '\\'
            |    ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            |    ('0'..'3')
                (
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :    ('0'..'7')
                    (
                        options {
                            warnWhenFollowAmbig = false;
                        }
                    :    '0'..'7'
                    )?
                )?
            |    ('4'..'7')
                (
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :    ('0'..'9')
                )?
            )
        ;


        */
    }
}
