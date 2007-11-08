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

// Added 20/12/2000 JED. Define the package for the class
header {
    package oracle.toplink.essentials.internal.parsing.ejbql.antlr273;

    import java.util.List;
    import java.util.ArrayList;

    import static oracle.toplink.essentials.internal.parsing.NodeFactory.*;
    import oracle.toplink.essentials.exceptions.EJBQLException;
}

/** */
class EJBQLParser extends Parser("oracle.toplink.essentials.internal.parsing.ejbql.EJBQLParser");
options {
    exportVocab=EJBQL;
    k = 3; // This is the number of tokens to look ahead to
    buildAST = false;
}

tokens {
    ABS="abs";
    ALL="all";
    AND="and";
    ANY="any";
    AS="as";
    ASC="asc";
    AVG="avg";
    BETWEEN="between";
    BOTH="both";
    BY="by";
    CONCAT="concat";
    COUNT="count";
    CURRENT_DATE="current_date";
    CURRENT_TIME="current_time";
    CURRENT_TIMESTAMP="current_timestamp";
    DESC="desc";
    DELETE="delete";
    DISTINCT="distinct";
    EMPTY="empty";
    ESCAPE="escape";
    EXISTS="exists";
    FALSE="false";
    FETCH="fetch";
    FROM="from";
    GROUP="group";
    HAVING="having";
    IN="in";
    INNER="inner";
    IS="is";
    JOIN="join";
    LEADING="leading";
    LEFT="left";
    LENGTH="length";
    LIKE="like";
    LOCATE="locate";
    LOWER="lower";
    MAX="max";
    MEMBER="member";
    MIN="min";
    MOD="mod";
    NEW="new";
    NOT="not";
    NULL="null";
    OBJECT="object";
    OF="of";
    OR="or";
    ORDER="order";
    OUTER="outer";
    SELECT="select";
    SET="set";
    SIZE="size";
    SQRT="sqrt";
    SOME="some";
    SUBSTRING="substring";
    SUM="sum";
    TRAILING="trailing";
    TRIM="trim";
    TRUE="true";
    UNKNOWN="unknown";
    UPDATE="update";
    UPPER="upper";
    WHERE="where";
}

{
    /** The root node of the parsed EJBQL query. */
    private Object root;

    /** Flag indicating whether aggregates are allowed. */
    private boolean aggregatesAllowed = false;

    /** */
    protected void setAggregatesAllowed(boolean allowed) {
        this.aggregatesAllowed = allowed;
    }

    /** */
    protected boolean aggregatesAllowed() {
        return aggregatesAllowed;
    }

    /** */
    protected void validateAbstractSchemaName(Token token) 
        throws RecognitionException {
        String text = token.getText();
        if (!isValidJavaIdentifier(token.getText())) {
            throw new NoViableAltException(token, getFilename());
        }
    }

    /** */
    protected void validateAttributeName(Token token) 
        throws RecognitionException {
        String text = token.getText();
        if (!isValidJavaIdentifier(token.getText())) {
            throw new NoViableAltException(token, getFilename());
        }
    }

    /** */
    protected boolean isValidJavaIdentifier(String text) {
        if ((text == null) || text.equals(""))
            return false;

        // check first char
        if (!Character.isJavaIdentifierStart(text.charAt(0)))
            return false;

        // check remaining characters
        for (int i = 1; i < text.length(); i++) {
            if (!Character.isJavaIdentifierPart(text.charAt(i))) {
                return false;
            }
        }
        
        return true;
    }

    protected String convertStringLiteral(String text) {
        // skip leading and trailing quotes
        String literal = text.substring(1, text.length() - 1);
        
        // convert ''s to 's
        while (true) {
            int index = literal.indexOf("''");
            if (index == -1) {
                break;
            }
            literal = literal.substring(0, index) + 
                      literal.substring(index + 1, literal.length());
        }

        return literal;
    }

    /** */
    public Object getRootNode() {
        return root;
    }

}

document
    : root = selectStatement
    | root = updateStatement
    | root = deleteStatement
    ;

selectStatement returns [Object node]
{ 
    node = null;
    Object select, from;
    Object where = null;
    Object groupBy = null;
    Object having = null;
    Object orderBy = null;
}
    : select  = selectClause
      from    = fromClause
      (where   = whereClause)?
      (groupBy = groupByClause)?
      (having  = havingClause)?
      (orderBy = orderByClause)?
      EOF 
        { 
            node = factory.newSelectStatement(0, 0, select, from, where, 
                                              groupBy, having, orderBy); 
        }
    ;

//================================================

updateStatement returns [Object node]
{ 
    node = null; 
    Object update, set, where = null;
}
    : update = updateClause
      set    = setClause
      (where  = whereClause)?
      EOF { node = factory.newUpdateStatement(0, 0, update, set, where); }
    ;

updateClause returns [Object node]
{ 
    node = null; 
    String schema, variable = null;
}
    : u:UPDATE schema = abstractSchemaName 
        ((AS)? ident:IDENT { variable = ident.getText(); })?
        { 
            node = factory.newUpdateClause(u.getLine(), u.getColumn(), 
                                           schema, variable); 
        } 
    ;  

setClause returns [Object node]
{ 
    node = null; 
    List assignments = new ArrayList();
}
    : t:SET node = setAssignmentClause { assignments.add(node); }
        (COMMA node = setAssignmentClause { assignments.add(node); } )*
        { node = factory.newSetClause(t.getLine(), t.getColumn(), assignments); }
     ;

setAssignmentClause returns [Object node]
{ 
    node = null;
    Object target, value; 
}
    : target = setAssignmentTarget t:EQUALS value = newValue
        { 
            node = factory.newSetAssignmentClause(t.getLine(), t.getColumn(), 
                                                  target, value); 
        }
    ;

setAssignmentTarget returns [Object node]
{ 
    node = null;
    Object left = null;
}
    : node = attribute
    | node = pathExpression
    ;

newValue returns [Object node]
{ node = null; }
    : node = simpleArithmeticExpression
    | n:NULL 
        { node = factory.newNullLiteral(n.getLine(), n.getColumn()); } 
    ;

//================================================

deleteStatement returns [Object node]
{ 
    node = null; 
    Object delete, where = null;
}
    : delete = deleteClause
      (where = whereClause)?
      EOF { node = factory.newDeleteStatement(0, 0, delete, where); }
    ;

deleteClause returns [Object node]
{ 
    node = null; 
    String schema, variable = null;
}
    : t:DELETE FROM schema = abstractSchemaName 
        ((AS)? ident:IDENT { variable = ident.getText(); })?
        { 
            node = factory.newDeleteClause(t.getLine(), t.getColumn(), 
                                           schema, variable); 
        }
    ;

//================================================

selectClause returns [Object node]
{ 
    node = null;
    boolean distinct = false;
    List exprs = new ArrayList();
}
    : t:SELECT (DISTINCT { distinct = true; })?
      node = selectExpression { exprs.add(node); }
      ( COMMA node = selectExpression  { exprs.add(node); } )*
        { 
            node = factory.newSelectClause(t.getLine(), t.getColumn(), 
                                           distinct, exprs); 
        }
    ;

selectExpression returns [Object node]
{ node = null; }
    : node = pathExprOrVariableAccess 
    | node = aggregateExpression
    | OBJECT LEFT_ROUND_BRACKET node = variableAccess RIGHT_ROUND_BRACKET
    | node = constructorExpression
    ;

pathExprOrVariableAccess returns [Object node]
{
    node = null;
    Object right;
}
    : node = variableAccess
        (d:DOT right = attribute
            { node = factory.newDot(d.getLine(), d.getColumn(), node, right); }
        )*
    ;

aggregateExpression returns [Object node]
{ 
    node = null; 
    boolean distinct = false;
}
    : t1:AVG LEFT_ROUND_BRACKET (DISTINCT { distinct = true; })?
        node = stateFieldPathExpression RIGHT_ROUND_BRACKET 
        { node = factory.newAvg(t1.getLine(), t1.getColumn(), distinct, node); }
    | t2:MAX LEFT_ROUND_BRACKET (DISTINCT { distinct = true; })? 
        node = stateFieldPathExpression RIGHT_ROUND_BRACKET
        { node = factory.newMax(t2.getLine(), t2.getColumn(), distinct, node); }
    | t3:MIN LEFT_ROUND_BRACKET (DISTINCT { distinct = true; })?
        node = stateFieldPathExpression RIGHT_ROUND_BRACKET
        { node = factory.newMin(t3.getLine(), t3.getColumn(), distinct, node); }
    | t4:SUM LEFT_ROUND_BRACKET (DISTINCT { distinct = true; })?
        node = stateFieldPathExpression RIGHT_ROUND_BRACKET
        { node = factory.newSum(t4.getLine(), t4.getColumn(), distinct, node); }
    | t5:COUNT LEFT_ROUND_BRACKET (DISTINCT { distinct = true; })?
        node = pathExprOrVariableAccess RIGHT_ROUND_BRACKET
        { node = factory.newCount(t5.getLine(), t5.getColumn(), distinct, node); }
    ;

constructorExpression returns [Object node]
{ 
    node = null;
    String className = null; 
    List args = new ArrayList();
}
    : t:NEW className = constructorName
        LEFT_ROUND_BRACKET 
        node = constructorItem { args.add(node); } 
        ( COMMA node = constructorItem { args.add(node); } )*
        RIGHT_ROUND_BRACKET
        { 
            node = factory.newConstructor(t.getLine(), t.getColumn(), 
                                          className, args); 
        }
    ;

constructorName returns [String className]
{ 
    className = null;
    StringBuffer buf = new StringBuffer(); 
}
    : i1:IDENT { buf.append(i1.getText()); }
        ( DOT i2:IDENT { buf.append('.').append(i2.getText()); })*
        { className = buf.toString(); }
    ;

constructorItem returns [Object node]
{ node = null; }
    : node = pathExprOrVariableAccess 
    | node = aggregateExpression
    ;

fromClause returns [Object node]
{ 
    node = null; 
    List varDecls = new ArrayList();
}
    : t:FROM identificationVariableDeclaration[varDecls]
        (COMMA  ( identificationVariableDeclaration[varDecls]
                | node = collectionMemberDeclaration  { varDecls.add(node); }
                ) 
        )*
        { node = factory.newFromClause(t.getLine(), t.getColumn(), varDecls); }
    ;

identificationVariableDeclaration [List varDecls]
{ Object node = null; }
    : node = rangeVariableDeclaration { varDecls.add(node); } 
        ( node = join { varDecls.add(node); } )*
    ;

rangeVariableDeclaration returns [Object node]
{ 
    node = null; 
    String schema;
}
    : schema = abstractSchemaName (AS)? i:IDENT
        { 
            node = factory.newRangeVariableDecl(i.getLine(), i.getColumn(), 
                                                schema, i.getText()); 
        }
    ;

// Non-terminal abstractSchemaName first matches any token to allow abstract 
// schema names that are keywords (such as order, etc.). 
// Method validateAbstractSchemaName throws an exception if the text of the 
// token is not a valid Java identifier.
abstractSchemaName returns [String schema]
{ schema = null; }
    : ident:. 
        {
            schema = ident.getText();
            validateAbstractSchemaName(ident); 
        }
    ;

join returns [Object node]
{ 
    node = null;
    boolean outerJoin; 
}
    : outerJoin = joinSpec
      ( node = joinAssociationPathExpression (AS)? i:IDENT
        {
            node = factory.newJoinVariableDecl(i.getLine(), i.getColumn(), 
                                               outerJoin, node, i.getText()); 
        }
      | t:FETCH node = joinAssociationPathExpression 
        { 
            node = factory.newFetchJoin(t.getLine(), t.getColumn(), 
                                        outerJoin, node); }
      )
    ;

joinSpec returns [boolean outer]
{ outer = false; }
    : (LEFT (OUTER)? { outer = true; }  | INNER  )? JOIN
    ;

collectionMemberDeclaration returns [Object node]
{ node = null; }
    : t:IN LEFT_ROUND_BRACKET node = collectionValuedPathExpression RIGHT_ROUND_BRACKET 
      (AS)? i:IDENT
      { 
          node = factory.newCollectionMemberVariableDecl(
                t.getLine(), t.getColumn(), node, i.getText()); 
        }
    ;

collectionValuedPathExpression returns [Object node]
{ node = null; }
    : node = pathExpression
    ;

associationPathExpression returns [Object node]
{ node = null; }
    : node = pathExpression
    ;

joinAssociationPathExpression returns [Object node]
{
    node = null; 
    Object left, right;
}
    : left = variableAccess d:DOT right = attribute
        { node = factory.newDot(d.getLine(), d.getColumn(), left, right); }
    ;

singleValuedPathExpression returns [Object node]
{ node = null; }
    : node = pathExpression
    ;

stateFieldPathExpression returns [Object node]
{ node = null; }
    : node = pathExpression
    ;

pathExpression returns [Object node]
{ 
    node = null; 
    Object right;
}
    : node = variableAccess
        (d:DOT right = attribute
            { node = factory.newDot(d.getLine(), d.getColumn(), node, right); }
        )+
    ;

// Non-terminal attribute first matches any token to allow abstract 
// schema names that are keywords (such as order, etc.). 
// Method validateAttributeName throws an exception if the text of the 
// token is not a valid Java identifier.
attribute returns [Object node]
{ node = null; }

    : i:.
        { 
            validateAttributeName(i);
            node = factory.newAttribute(i.getLine(), i.getColumn(), i.getText()); 
        }
    ;

variableAccess returns [Object node]
{ node = null; }
    : i:IDENT
        { node = factory.newVariableAccess(i.getLine(), i.getColumn(), i.getText()); }
    ;

whereClause returns [Object node]
{ node = null; }
    : t:WHERE node = conditionalExpression 
        { node = factory.newWhereClause(t.getLine(), t.getColumn(), node); } 
    ;

conditionalExpression returns [Object node]
{ 
    node = null; 
    Object right;
}
    : node = conditionalTerm 
        (t:OR right = conditionalTerm
            { node = factory.newOr(t.getLine(), t.getColumn(), node, right); }
        )*
    ;

conditionalTerm returns [Object node]
{ 
    node = null; 
    Object right;
}
    : node = conditionalFactor 
        (t:AND right = conditionalFactor
            { node = factory.newAnd(t.getLine(), t.getColumn(), node, right); }
        )*
    ;

conditionalFactor returns [Object node]
{ node = null; }
    : (n:NOT)? 
        ( node = conditionalPrimary 
          { 
              if (n != null) {
                  node = factory.newNot(n.getLine(), n.getColumn(), node); 
              }
          }
        | node = existsExpression[(n!=null)] 
        )
    ;

conditionalPrimary  returns [Object node]
{ node = null; }
    : (LEFT_ROUND_BRACKET conditionalExpression) =>
        LEFT_ROUND_BRACKET node = conditionalExpression RIGHT_ROUND_BRACKET
    | node = simpleConditionalExpression 
    ;

simpleConditionalExpression returns [Object node]
{ 
    node = null; 
    Object left = null;
}
    : left = arithmeticExpression 
        node = simpleConditionalExpressionRemainder[left]
    ;

simpleConditionalExpressionRemainder [Object left] returns [Object node]
{ node = null; }
    : node = comparisonExpression[left]
    | (n1:NOT)? node = conditionWithNotExpression[(n1!=null), left]
    | IS (n2:NOT)? node = isExpression[(n2!=null), left]
    ;

conditionWithNotExpression [boolean not, Object left] returns [Object node]
{ node = null; }
    : node = betweenExpression[not, left]
    | node = likeExpression[not, left]
    | node = inExpression[not, left]
    | node = collectionMemberExpression[not, left]
    ;

isExpression [boolean not, Object left] returns [Object node]
{ node = null; }
    : node = nullComparisonExpression[not, left]
    | node = emptyCollectionComparisonExpression[not, left]
    ;

betweenExpression [boolean not, Object left] returns [Object node]
{
    node = null;
    Object lower, upper;
}
    : t:BETWEEN
        lower = arithmeticExpression AND upper = arithmeticExpression
        {
            node = factory.newBetween(t.getLine(), t.getColumn(),
                                      not, left, lower, upper);
        }
    ;

inExpression [boolean not, Object left] returns [Object node]
{
    node = null;
    List items = new ArrayList();
    Object subqueryNode, itemNode;
}
    : t:IN
        LEFT_ROUND_BRACKET
        ( itemNode = inItem { items.add(itemNode); }
            ( COMMA itemNode = inItem { items.add(itemNode); } )*
            {
                node = factory.newIn(t.getLine(), t.getColumn(),
                                     not, left, items);
            }
        | subqueryNode = subquery
            {
                node = factory.newIn(t.getLine(), t.getColumn(),
                                     not, left, subqueryNode);
            }
        )
        RIGHT_ROUND_BRACKET
    ;

inItem returns [Object node]
{ node = null; }
    : node = literalString
    | node = literalNumeric
    | node = inputParameter
    ;

likeExpression [boolean not, Object left] returns [Object node]
{
    node = null;
    Object pattern, escape = null;
}
    : t:LIKE pattern = likeValue
        (escape = escape)?
        {
            node = factory.newLike(t.getLine(), t.getColumn(), not,
                                   left, pattern, escape);
        }
    ;

escape returns [Object node]
{ 
    node = null; 
    Object escape = null;

}
    : t:ESCAPE escape = likeValue
        { node = factory.newEscape(t.getLine(), t.getColumn(), escape); }
    ;

likeValue returns [Object node]
{ node = null; }
    : node = literalString 
    | node = inputParameter
    ;

nullComparisonExpression [boolean not, Object left] returns [Object node]
{ node = null; }
    : t: NULL
        { node = factory.newIsNull(t.getLine(), t.getColumn(), not, left); }
    ;

emptyCollectionComparisonExpression [boolean not, Object left] returns [Object node]
{ node = null; }
    : t: EMPTY
        { node = factory.newIsEmpty(t.getLine(), t.getColumn(), not, left); }
    ;

collectionMemberExpression [boolean not, Object left] returns [Object node]
{ node = null; }
    : t: MEMBER (OF)? node = collectionValuedPathExpression
        { 
            node = factory.newMemberOf(t.getLine(), t.getColumn(), 
                                       not, left, node); 
        }
    ;

existsExpression [boolean not] returns [Object node]
{ 
    Object subqueryNode = null; 
    node = null;
}
    : t:EXISTS LEFT_ROUND_BRACKET subqueryNode = subquery RIGHT_ROUND_BRACKET
        { 
            node = factory.newExists(t.getLine(), t.getColumn(), 
                                     not, subqueryNode); 
        }
    ;

comparisonExpression [Object left] returns [Object node]
{ node = null; }
    : t1:EQUALS node = comparisonExpressionRightOperand 
        { node = factory.newEquals(t1.getLine(), t1.getColumn(), left, node); }
    | t2:NOT_EQUAL_TO node = comparisonExpressionRightOperand 
        { node = factory.newNotEquals(t2.getLine(), t2.getColumn(), left, node); }
    | t3:GREATER_THAN node = comparisonExpressionRightOperand 
        { node = factory.newGreaterThan(t3.getLine(), t3.getColumn(), left, node); }
    | t4:GREATER_THAN_EQUAL_TO node = comparisonExpressionRightOperand 
        { node = factory.newGreaterThanEqual(t4.getLine(), t4.getColumn(), left, node); }
    | t5:LESS_THAN node = comparisonExpressionRightOperand 
        { node = factory.newLessThan(t5.getLine(), t5.getColumn(), left, node); }
    | t6:LESS_THAN_EQUAL_TO node = comparisonExpressionRightOperand 
        { node = factory.newLessThanEqual(t6.getLine(), t6.getColumn(), left, node); }
    ;

comparisonExpressionRightOperand returns [Object node]
{ node = null; }
    : node = arithmeticExpression 
    | node = anyOrAllExpression
    ;

arithmeticExpression returns [Object node]
{ node = null; }
    : node = simpleArithmeticExpression
    | LEFT_ROUND_BRACKET node = subquery RIGHT_ROUND_BRACKET
    ;

simpleArithmeticExpression returns [Object node]
{ 
    node = null; 
    Object right;
}
    : node = arithmeticTerm 
        ( p:PLUS right = arithmeticTerm 
            { node = factory.newPlus(p.getLine(), p.getColumn(), node, right); }
        | m:MINUS right = arithmeticTerm
            { node = factory.newMinus(m.getLine(), m.getColumn(), node, right); }
        )* 
    ;

arithmeticTerm  returns [Object node]
{ 
    node = null; 
    Object right;
}
    : node = arithmeticFactor 
        ( m:MULTIPLY right = arithmeticFactor 
            { node = factory.newMultiply(m.getLine(), m.getColumn(), node, right); }
        | d:DIVIDE right = arithmeticFactor
            { node = factory.newDivide(d.getLine(), d.getColumn(), node, right); }
        )* 
    ;

arithmeticFactor returns [Object node]
{ node = null; }
    : p:PLUS  node = arithmeticPrimary 
        { node = factory.newUnaryPlus(p.getLine(), p.getColumn(), node); } 
    | m:MINUS node = arithmeticPrimary  
        { node = factory.newUnaryMinus(m.getLine(), m.getColumn(), node); }
    | node = arithmeticPrimary 
    ;

arithmeticPrimary returns [Object node]
{ node = null; }
    : { aggregatesAllowed() }? node = aggregateExpression
    | node = variableAccess
    | node = stateFieldPathExpression
    | node = functionsReturningNumerics
    | node = functionsReturningDatetime
    | node = functionsReturningStrings
    | node = inputParameter
    | node = literalNumeric
    | node = literalString
    | node = literalBoolean
    | LEFT_ROUND_BRACKET node = simpleArithmeticExpression RIGHT_ROUND_BRACKET
    ;

anyOrAllExpression returns [Object node]
{ node = null; }
    : a:ALL LEFT_ROUND_BRACKET node = subquery RIGHT_ROUND_BRACKET
        { node = factory.newAll(a.getLine(), a.getColumn(), node); }
    | y:ANY LEFT_ROUND_BRACKET node = subquery RIGHT_ROUND_BRACKET
        { node = factory.newAny(y.getLine(), y.getColumn(), node); }
    | s:SOME LEFT_ROUND_BRACKET node = subquery RIGHT_ROUND_BRACKET
        { node = factory.newSome(s.getLine(), s.getColumn(), node); }
    ;

stringPrimary returns [Object node]
{ node = null; }
    : node = literalString 
    | node = functionsReturningStrings
    | node = inputParameter
    | node = stateFieldPathExpression
    ;

// Literals and Low level stuff

literal returns [Object node]
{ node = null; }
    : node = literalNumeric
    | node = literalBoolean
    | node = literalString
    ;

literalNumeric returns [Object node]
{ node = null; }
    : i:NUM_INT 
        { 
            node = factory.newIntegerLiteral(i.getLine(), i.getColumn(), 
                                             Integer.valueOf(i.getText())); 
        }
    | l:NUM_LONG 
        { 
            String text = l.getText();
            // skip the tailing 'l'
            text = text.substring(0, text.length() - 1);
            node = factory.newLongLiteral(l.getLine(), l.getColumn(), 
                                          Long.valueOf(text)); 
        }
    | f:NUM_FLOAT
        { 
            node = factory.newFloatLiteral(f.getLine(), f.getColumn(),
                                           Float.valueOf(f.getText()));
        }
    | d:NUM_DOUBLE
        { 
            node = factory.newDoubleLiteral(d.getLine(), d.getColumn(),
                                            Double.valueOf(d.getText()));
        }
    ;

literalBoolean returns [Object node]
{ node = null; }
    : t:TRUE  
        { node = factory.newBooleanLiteral(t.getLine(), t.getColumn(), Boolean.TRUE); }
    | f:FALSE 
        { node = factory.newBooleanLiteral(f.getLine(), f.getColumn(), Boolean.FALSE); }
    ;

literalString returns [Object node]
{ node = null; }
    : d:STRING_LITERAL_DOUBLE_QUOTED 
        { 
            node = factory.newStringLiteral(d.getLine(), d.getColumn(), 
                                            convertStringLiteral(d.getText())); 
        }
    | s:STRING_LITERAL_SINGLE_QUOTED
        { 
            node = factory.newStringLiteral(s.getLine(), s.getColumn(), 
                                            convertStringLiteral(s.getText())); 
        }
    ;

inputParameter returns [Object node]
{ node = null; }
    : p:POSITIONAL_PARAM
        { 
            // skip the leading ?
            String text = p.getText().substring(1);
            node = factory.newPositionalParameter(p.getLine(), p.getColumn(), text); 
        }
    | n:NAMED_PARAM
        { 
            // skip the leading :
            String text = n.getText().substring(1);
            node = factory.newNamedParameter(n.getLine(), n.getColumn(), text); 
        }
    ;

functionsReturningNumerics returns [Object node]
{ node = null; }
    : node = abs
    | node = length
    | node = mod
    | node = sqrt
    | node = locate
    | node = size
    ;

functionsReturningDatetime returns [Object node]
{ node = null; }
    : d:CURRENT_DATE 
        { node = factory.newCurrentDate(d.getLine(), d.getColumn()); }
    | t:CURRENT_TIME
        { node = factory.newCurrentTime(t.getLine(), t.getColumn()); }
    | ts:CURRENT_TIMESTAMP
        { node = factory.newCurrentTimestamp(ts.getLine(), ts.getColumn()); }
    ;

functionsReturningStrings returns [Object node]
{ node = null; }
    : node = concat
    | node = substring
    | node = trim
    | node = upper
    | node = lower
    ;

// Functions returning strings
concat returns [Object node]
{ 
    node = null;
    Object firstArg, secondArg;
}
    : c:CONCAT 
        LEFT_ROUND_BRACKET 
        firstArg = stringPrimary COMMA secondArg = stringPrimary
        RIGHT_ROUND_BRACKET
        { node = factory.newConcat(c.getLine(), c.getColumn(), firstArg, secondArg); }
    ;

substring returns [Object node]
{ 
    node = null;
    Object string, start, length;
}
    : s:SUBSTRING   
        LEFT_ROUND_BRACKET
        string = stringPrimary COMMA
        start = simpleArithmeticExpression COMMA
        length = simpleArithmeticExpression
        RIGHT_ROUND_BRACKET
        { 
            node = factory.newSubstring(s.getLine(), s.getColumn(), 
                                        string, start, length); 
        }
    ;

trim returns [Object node]
{ 
    node = null; 
    TrimSpecification trimSpec = TrimSpecification.BOTH;
    Object trimChar = null;
}
    : t:TRIM
        LEFT_ROUND_BRACKET 
        ( ( trimSpec trimChar FROM )=> trimSpec = trimSpec trimChar = trimChar FROM )? 
        node = stringPrimary
        RIGHT_ROUND_BRACKET
        { 
            node = factory.newTrim(t.getLine(), t.getColumn(), 
                                   trimSpec, trimChar, node); 
        }
    ;

trimSpec returns [TrimSpecification trimSpec]
{ trimSpec = TrimSpecification.BOTH; }
    : LEADING
        { trimSpec = TrimSpecification.LEADING; }
    | TRAILING
        { trimSpec = TrimSpecification.TRAILING; }
    | BOTH
        { trimSpec = TrimSpecification.BOTH; }
    | // empty rule
    ;

trimChar returns [Object node]
{ node = null; }
    : node = literalString
    | node = inputParameter
    | // empty rule
    ;

upper returns [Object node]
{ node = null; }
    : u:UPPER LEFT_ROUND_BRACKET node = stringPrimary RIGHT_ROUND_BRACKET
        { node = factory.newUpper(u.getLine(), u.getColumn(), node); }
    ;

lower returns [Object node]
{ node = null; }
    : l:LOWER LEFT_ROUND_BRACKET node = stringPrimary RIGHT_ROUND_BRACKET
        { node = factory.newLower(l.getLine(), l.getColumn(), node); }
    ;

// Functions returning numerics
abs returns [Object node]
{ node = null; }
    : a:ABS LEFT_ROUND_BRACKET node = simpleArithmeticExpression RIGHT_ROUND_BRACKET
        { node = factory.newAbs(a.getLine(), a.getColumn(), node); }
    ;

length returns [Object node]
{ node = null; }
    : l:LENGTH LEFT_ROUND_BRACKET node = stringPrimary RIGHT_ROUND_BRACKET
        { node = factory.newLength(l.getLine(), l.getColumn(), node); }
    ;

locate returns [Object node]
{ 
    node = null; 
    Object pattern, startPos = null;
}
    : l:LOCATE
        LEFT_ROUND_BRACKET 
        pattern = stringPrimary COMMA node = stringPrimary
        ( COMMA startPos = simpleArithmeticExpression )?
        RIGHT_ROUND_BRACKET
        { 
            node = factory.newLocate(l.getLine(), l.getColumn(), 
                                     pattern, node, startPos); 
        }
    ;

size returns [Object node]
{ node = null; }
    : s:SIZE 
        LEFT_ROUND_BRACKET node = collectionValuedPathExpression RIGHT_ROUND_BRACKET
        { node = factory.newSize(s.getLine(), s.getColumn(), node);}
    ;

mod returns [Object node]
{ 
    node = null; 
    Object left, right;
}
    : m:MOD LEFT_ROUND_BRACKET
        left = simpleArithmeticExpression COMMA 
        right = simpleArithmeticExpression
        RIGHT_ROUND_BRACKET
        { node = factory.newMod(m.getLine(), m.getColumn(), left, right); }
    ;

sqrt returns [Object node]
{ node = null; }
    : s:SQRT 
        LEFT_ROUND_BRACKET node = simpleArithmeticExpression RIGHT_ROUND_BRACKET
        { node = factory.newSqrt(s.getLine(), s.getColumn(), node); }
    ;

subquery returns [Object node]
{ 
    node = null; 
    Object select, from;
    Object where = null;
    Object groupBy = null;
    Object having = null;
}
    : select  = simpleSelectClause
      from    = subqueryFromClause
      (where   = whereClause)?
      (groupBy = groupByClause)?
      (having  = havingClause)?
        { 
            node = factory.newSubquery(0, 0, select, from, 
                                       where, groupBy, having); 
        }
    ;

simpleSelectClause returns [Object node]
{ 
    node = null; 
    boolean distinct = false;
}
    : s:SELECT (DISTINCT { distinct = true; })?
      node = simpleSelectExpression
        {
            List exprs = new ArrayList();
            exprs.add(node);
            node = factory.newSelectClause(s.getLine(), s.getColumn(), 
                                           distinct, exprs);
        }
    ;

simpleSelectExpression returns [Object node]
{ node = null; }
    : node = singleValuedPathExpression
    | node = aggregateExpression
    | node = variableAccess
    ;

subqueryFromClause returns [Object node]
{ 
    node = null; 
    List varDecls = new ArrayList();
}
    : f:FROM subselectIdentificationVariableDeclaration[varDecls] 
        ( COMMA subselectIdentificationVariableDeclaration[varDecls] )*
        { node = factory.newFromClause(f.getLine(), f.getColumn(), varDecls); }
    ;

subselectIdentificationVariableDeclaration [List varDecls]
{ Object node; }
    : identificationVariableDeclaration[varDecls]
    | node = associationPathExpression (AS)? i:IDENT
        { 
            varDecls.add(factory.newVariableDecl(i.getLine(), i.getColumn(), 
                                                 node, i.getText())); 
        }
    | node = collectionMemberDeclaration { varDecls.add(node); }
    ;

orderByClause returns [Object node]
{ 
    node = null; 
    List items = new ArrayList();
}
    : o:ORDER BY
        node = orderByItem  { items.add(node); } 
        (COMMA node = orderByItem  { items.add(node); })*
        { node = factory.newOrderByClause(o.getLine(), o.getColumn(), items); }
    ; 

orderByItem returns [Object node]
{ node = null; }
    : node = stateFieldPathExpression
        ( a:ASC 
            { node = factory.newAscOrdering(a.getLine(), a.getColumn(), node); }
        | d:DESC
            { node = factory.newDescOrdering(d.getLine(), d.getColumn(), node); }
        | // empty rule
            { node = factory.newAscOrdering(0, 0, node); }
        )
    ;

groupByClause returns [Object node]
{ 
    node = null; 
    List items = new ArrayList();
}
    : g:GROUP BY
        node = groupByItem { items.add(node); }
        (COMMA node = groupByItem  { items.add(node); } )*
        { node = factory.newGroupByClause(g.getLine(), g.getColumn(), items); }
    ;

groupByItem returns [Object node]
{ node = null; }
    : node = stateFieldPathExpression
    | node = variableAccess
    ;

havingClause returns [Object node]
{ node = null; }
    : h:HAVING { setAggregatesAllowed(true); } 
        node = conditionalExpression 
        { 
            setAggregatesAllowed(false); 
            node = factory.newHavingClause(h.getLine(), h.getColumn(), node);
        }
    ;

/** */
class EJBQLLexer extends Lexer;
options {   
    caseSensitive=false;
    caseSensitiveLiterals=false;
    k = 4;
    exportVocab=EJBQL;
    charVocabulary = '\u0000'..'\uFFFE';
}

// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
    :   ('0'..'9'|'a'..'f')
    ;

WS  : (' ' | '\t' | '\n' | '\r')+
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

IDENT 
    : TEXTCHAR
    ;

protected
TEXTCHAR
    : ('a'..'z' | '_' | '$' | 
       c1:'\u0080'..'\uFFFE' 
       {
           if (!Character.isJavaIdentifierStart(c1)) {
               throw new NoViableAltForCharException(
                   c1, getFilename(), getLine(), getColumn());
           }
       }
      )
      ('a'..'z' | '_' | '$' | '0'..'9' | 
       c2:'\u0080'..'\uFFFE'
       {
           if (!Character.isJavaIdentifierPart(c2)) {
               throw new NoViableAltForCharException(
                   c2, getFilename(), getLine(), getColumn());
           }
       }
      )*
    ;

// a numeric literal
NUM_INT
{ 
    boolean isDecimal=false; 
    int tokenType = NUM_DOUBLE;
}
    :   '.' {_ttype = DOT;}
            (('0'..'9')+ { tokenType = NUM_DOUBLE; } (EXPONENT)? (tokenType = FLOAT_SUFFIX)?
            {_ttype = tokenType; })?
    |   (   '0' {isDecimal = true;} // special case for just '0'
            (   ('x')
                (                                           // hex
                    // the 'e'|'E' and float suffix stuff look
                    // like hex digits, hence the (...)+ doesn't
                    // know when to stop: ambig.  ANTLR resolves
                    // it correctly by matching immediately.  It
                    // is therefor ok to hush warning.
                    options {
                        warnWhenFollowAmbig=false;
                    }
                :   HEX_DIGIT
                )+
            |   ('0'..'7')+                                 // octal
            )?
        |   ('1'..'9') ('0'..'9')*  {isDecimal=true;}       // non-zero decimal
        )
        (   ('l') { _ttype = NUM_LONG; }
        
        // only check to see if it's a float if looks like decimal so far
        |   {isDecimal}?
            { tokenType = NUM_DOUBLE; }
            (   '.' ('0'..'9')* (EXPONENT)? (tokenType = FLOAT_SUFFIX)?
            |   EXPONENT (tokenType = FLOAT_SUFFIX)?
            |   tokenType = FLOAT_SUFFIX
            )
            { _ttype = tokenType; }
        )?
    ;

// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
    :   ('e') ('+'|'-')? ('0'..'9')+
    ;


protected
FLOAT_SUFFIX returns [int tokenType]
{ tokenType = NUM_DOUBLE; }
    :   'f' { tokenType = NUM_FLOAT; }
    |   'd' { tokenType = NUM_DOUBLE; }
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

MULTIPLY
    : "*"
    ;

DIVIDE
    : "/"
    ;

PLUS
    : "+"
    ;

MINUS
    : "-"
    ;

POSITIONAL_PARAM
    : "?" ('1'..'9') ('0'..'9')*
    ;

NAMED_PARAM
    : ":" TEXTCHAR
    ;

// Added Jan 9, 2001 JED
// string literals
STRING_LITERAL_DOUBLE_QUOTED
    : '"' (~ ('"'))* '"'
    ;

STRING_LITERAL_SINGLE_QUOTED
    : '\'' (~ ('\'') | ("''"))* '\'' 
    ;
