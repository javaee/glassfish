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

import antlr.collections.AST;

import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.utility.I18NHelper;
import java.util.ResourceBundle;

/**
 * Helper class to support semantic analysis.
 * 
 * @author Michael Bouschen
 * @author Dave Bristor
 */
public class SemanticHelper {

    /** Type info access helper. */
    protected QueryContext queryContext;
    
    /** The environment in which the query is being compiled. */
    protected Environment env;

    /** Symbol table handling names of variables and parameters. */
    protected SymbolTable symtab;

    /** I18N support. */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
        SemanticHelper.class);

    /** */
    protected SemanticHelper(QueryContext queryContext, Environment env) {
        this.queryContext = queryContext;
        this.env = env;
        this.symtab = new SymbolTable();
    }

    /**
     * Checks that select clause and orderby clause are compatible.
     * <p>
     * The method checks the following error cases:
     * <ul>
     * <li>if the select clause is an identification variable or
     * a single valued cmr path expression, then the orderby item
     * must be a cmp field of the entity bean abstract schema
     * type value returned by the SELECT clause
     * <li>if the select clause is a cmp field, then
     * orderby item must be empty or the same cmp field.
     * </ul>
     * @param select the select clause of the query
     * @param orderby the orderby clause of the query
     */
    public void checkSelectOrderbyClause(EJBQLASTImpl select, EJBQLASTImpl orderby)
    {
        // nothing to check if no orderby clause
        if (orderby == null) {
            return;
        }

        AST selectReturnAST = select.getFirstChild();
        // skip DISTINCT node, so selectReturnAST should be one of the following:
        //     Object(x), cmr-field, cmp-field
        // it is illegal to be an aggregate function node
        if (selectReturnAST.getType() == EJBQLTokenTypes.DISTINCT) {
            selectReturnAST = selectReturnAST.getNextSibling();
        }

        if (selectReturnAST.getType() == EJBQLTokenTypes.CMP_FIELD_ACCESS) {
            StringBuffer buf = new StringBuffer();
            genPathExpression(selectReturnAST, buf);
            String selectReturnPathExpr = buf.toString();
            for (AST sibling = orderby.getFirstChild();
                    sibling != null;
                    sibling = sibling.getNextSibling().getNextSibling()) {

                // share buf
                buf.setLength(0);
                genPathExpression(sibling, buf);
                String siblingPathExpr = buf.toString();
                if (!selectReturnPathExpr.equals(siblingPathExpr)) {
                    ErrorMsg.error(I18NHelper.getMessage(msgs, 
                    "EXC_InvalidOrderbyItemForCMPSelect", //NOI18N
                    siblingPathExpr)); 
                }
            }
        } else {
            AST abstractSchemaAST = null;
            if (selectReturnAST.getType() == EJBQLTokenTypes.IDENTIFICATION_VAR
                    || selectReturnAST.getType() == EJBQLTokenTypes.SINGLE_CMR_FIELD_ACCESS
                    || selectReturnAST.getType() == EJBQLTokenTypes.CMP_FIELD_ACCESS) {
                abstractSchemaAST = selectReturnAST;
            } else { // it must be an aggregate function node
                ErrorMsg.error(I18NHelper.getMessage(msgs,
                "EXC_InvalidAggregateOrderby" //NOI18N
                ));
            }

            StringBuffer buf = new StringBuffer();
            genPathExpression(abstractSchemaAST, buf);
            String abstractSchemaExpr = buf.toString();
            for (AST sibling = orderby.getFirstChild();
                    sibling != null;
                    sibling = sibling.getNextSibling().getNextSibling()) {

                // share  buf
                buf.setLength(0);
                genPathExpression(sibling.getFirstChild(), buf);
                String siblingRootExpr = buf.toString();
                if (!abstractSchemaExpr.equals(siblingRootExpr)) {
                    buf.setLength(0);
                    genPathExpression(sibling, buf);
                    ErrorMsg.error(I18NHelper.getMessage(msgs, 
                    "EXC_InvalidOrderbyItem", //NOI18N
                    buf.toString())); 
                }
            }
        } 
    }

    /**
     * Form a string representation of a dot expression and append to given
     * StringBuffer.
     * @param ast the AST node representing the root the of the expression
     * @param buf the StringBuffer that will have result of path expression
     * append
     */
    //SW: We can write this method without recursion. Michael suggests to use
    //recursion for readability.
    public void genPathExpression(AST ast, StringBuffer buf) {
        if (ast == null) {
            return;
        }
        switch (ast.getType()) {
            case EJBQLTokenTypes.CMP_FIELD_ACCESS:
            case EJBQLTokenTypes.COLLECTION_CMR_FIELD_ACCESS:
            case EJBQLTokenTypes.SINGLE_CMR_FIELD_ACCESS:
                AST left = ast.getFirstChild();
                AST right = left.getNextSibling();
                genPathExpression(left, buf);
                buf.append('.');
                genPathExpression(right, buf);
                break;
            default:
                buf.append(ast.getText());
                break;
        }
    }

    /**
     * Analyses a logical operation AND, OR
     * @param op the logical operator
     * @param leftAST left operand 
     * @param rightAST right operand
     * @return the type info of the operator 
     */
    public Object analyseConditionalExpr(EJBQLASTImpl op, EJBQLASTImpl leftAST, EJBQLASTImpl rightAST)
    {
        Object leftTypeInfo = leftAST.getTypeInfo();
        Object rightTypeInfo = rightAST.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(leftTypeInfo)
                || queryContext.isErrorType(rightTypeInfo)) {
            return queryContext.getErrorType();
        }
        
        if (queryContext.isBooleanType(leftTypeInfo)
                && queryContext.isBooleanType(rightTypeInfo)) {
            Object common = queryContext.getBooleanType();
            return common;
        }

        // if this code is reached a bitwise operator was used with invalid arguments
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_InvalidArguments",  op.getText())); //NOI18N
        return queryContext.getErrorType();
    }
    
    /** 
     * Analyses a equality operation (==, <>)
     * @param op the relational operator
     * @param leftAST left operand 
     * @param rightAST right operand
     * @return the type info of the operator
     */
    public Object analyseEqualityExpr(EJBQLASTImpl op, EJBQLASTImpl leftAST, EJBQLASTImpl rightAST)
    {
        Object leftTypeInfo = leftAST.getTypeInfo();
        Object rightTypeInfo = rightAST.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(leftTypeInfo)
                || queryContext.isErrorType(rightTypeInfo)) {
            return queryContext.getErrorType();
        }

        // check right hand side for input param which might have unknown type
        if (isInputParameter(rightAST)) {
            // Note: If rightAST's typeInfo is unknown, it will change to that
            // given by leftTypeInfo.
            rightTypeInfo = env.analyseParameter(rightAST, leftTypeInfo);
        }

        // check left hand side for literals and input params 
        if (isLiteral(leftAST)) {
            ErrorMsg.error(leftAST.getLine(), leftAST.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_InvalidLHSLiteral", //NOI18N 
                    leftAST.getText(), op.getText()));
            return queryContext.getErrorType();
        }
        else if (isInputParameter(leftAST)) {
            ErrorMsg.error(leftAST.getLine(), leftAST.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_InvalidLHSParameter", //NOI18N 
                    leftAST.getText(), op.getText()));
            return queryContext.getErrorType();
        }
        
        // check operand types 
        if (queryContext.isNumberType(leftTypeInfo)
                && queryContext.isNumberType(rightTypeInfo)) {
            return queryContext.getBooleanType();
            
        } else if (queryContext.isStringType(leftTypeInfo)
                && queryContext.isStringType(rightTypeInfo)) {
            return queryContext.getBooleanType();
            
        } else if (queryContext.isDateTimeType(leftTypeInfo)
                && queryContext.isDateTimeType(rightTypeInfo)) {
            return queryContext.getBooleanType();
            
        } else if (isEntityBeanValue(leftAST)
                       && isEntityBeanValue(rightAST)
                       && (queryContext.isCompatibleWith(leftTypeInfo, rightTypeInfo)
                               || queryContext.isCompatibleWith(rightTypeInfo, leftTypeInfo))) {
            return queryContext.getBooleanType();
            
        } else if (queryContext.isBooleanType(leftTypeInfo)
                && queryContext.isBooleanType(rightTypeInfo)) {
            return queryContext.getBooleanType();
        }

        // if this code is reached a conditional operator was used with invalid arguments
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_InvalidArguments",  op.getText())); //NOI18N 
        return queryContext.getErrorType();
    }
    
    /**
     * Analyses a relational operation (<, <=, >, >=)
     * @param op the relational operator
     * @param leftAST left operand 
     * @param rightAST right operand
     * @return the type info of the operator
     */
    public Object analyseRelationalExpr(EJBQLASTImpl op, EJBQLASTImpl leftAST, EJBQLASTImpl rightAST)
    {
        Object leftTypeInfo = leftAST.getTypeInfo();
        Object rightTypeInfo = rightAST.getTypeInfo();

        // handle error type
        if (queryContext.isErrorType(leftTypeInfo)
                || queryContext.isErrorType(rightTypeInfo)) {
            return queryContext.getErrorType();
        }
        
        // check right hand side for input param which might have unknown type
        if (isInputParameter(rightAST)) {
            // Note: If rightAST's typeInfo is unknown, it will change to that
            // given by leftTypeInfo.
            env.analyseParameter(rightAST, leftTypeInfo);
            return queryContext.getBooleanType();
        }

        // check left hand side for literals and input params 
        if (isLiteral(leftAST)) {
            ErrorMsg.error(leftAST.getLine(), leftAST.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_InvalidLHSLiteral", //NOI18N 
                    leftAST.getText(), op.getText()));
            return queryContext.getErrorType();
        }
        else if (isInputParameter(leftAST)) {
            ErrorMsg.error(leftAST.getLine(), leftAST.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_InvalidLHSParameter", //NOI18N 
                    leftAST.getText(), op.getText()));
            return queryContext.getErrorType();
        }
        
        // check operand types
        if ((queryContext.isNumberType(leftTypeInfo) 
                    && queryContext.isNumberType(rightTypeInfo)) 
                ||
                (queryContext.isDateTimeType(leftTypeInfo) 
                    && queryContext.isDateTimeType(rightTypeInfo)) 
                ||
                (queryContext.isStringType(leftTypeInfo)
                    && queryContext.isStringType(rightTypeInfo))) {
            return queryContext.getBooleanType();
        }

        // if this code is reached a conditional operator was used with invalid arguments
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_InvalidArguments",  op.getText())); //NOI18N 
        return queryContext.getErrorType();
    }
    
    /**
     * Analyses a binary arithmetic expression +, -, *, /.
     * @param op the  operator
     * @param leftAST left operand 
     * @param rightAST right operand
     * @return the type info of the operator
     */
    public Object analyseBinaryArithmeticExpr(EJBQLASTImpl op, EJBQLASTImpl leftAST, EJBQLASTImpl rightAST)
    {
        Object leftTypeInfo = leftAST.getTypeInfo();
        Object rightTypeInfo = rightAST.getTypeInfo();

        // handle error type
        if (queryContext.isErrorType(leftTypeInfo)
                || queryContext.isErrorType(rightTypeInfo)) {
            return queryContext.getErrorType();
        }

        // check right hand side for input param which might have unknown type
        if (isInputParameter(rightAST)) {
            // Note: If rightAST's typeInfo is unknown, it will change to that
            // given by leftTypeInfo.
            env.analyseParameter(rightAST, leftTypeInfo);
            return queryContext.getBooleanType();
        }

        if (queryContext.isNumberType(leftTypeInfo)
                && queryContext.isNumberType(rightTypeInfo)) {
            Object common = queryContext.getCommonOperandType(leftTypeInfo, rightTypeInfo);
            if (!queryContext.isErrorType(common)) {
                return common;
            }
        }

        // if this code is reached a conditional operator was used with invalid arguments
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_InvalidArguments",  op.getText())); //NOI18N
        return queryContext.getErrorType();
    }

    /**
     * Analyses a unary expression (+ and -).
     * @param op the operator
     * @param argASTleftAST left operand 
     * @param rightAST right operand
     * @return the type info of the operator 
     */
    public Object analyseUnaryArithmeticExpr(EJBQLASTImpl op, EJBQLASTImpl argAST)
    {
        Object arg = argAST.getTypeInfo();

        // handle error type
        if (queryContext.isErrorType(arg)) {
            return arg;
        }
        
        if (queryContext.isNumberType(arg)) {
            boolean wrapper = false;
            if (queryContext.isNumericWrapperType(arg)) {
                arg = queryContext.getPrimitiveType(arg);
                wrapper = true;
            }

            Object promotedType = queryContext.unaryNumericPromotion(arg);
            if (wrapper)
                promotedType = queryContext.getWrapperType(promotedType);
            return promotedType;
        }
        
        // if this code is reached a conditional operator was used with invalid arguments
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_InvalidArguments",  op.getText())); //NOI18N
        return queryContext.getErrorType();
    }
    
    /** 
     * Analyses a expression node that is expected to access a collection 
     * valued CMR field. It returns the element type of the collection valued 
     * CMR field. 
     * @param fieldAccess the field access node
     * @return the type info of the operator 
     */
    public Object analyseCollectionValuedCMRField(EJBQLASTImpl fieldAccess)
    {
        if (fieldAccess.getType() != EJBQLTokenTypes.COLLECTION_CMR_FIELD_ACCESS) {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_InvalidPathExpr")); //NOI18N
            return queryContext.getErrorType();
        }

        EJBQLASTImpl classExpr = (EJBQLASTImpl)fieldAccess.getFirstChild();
        EJBQLASTImpl field = (EJBQLASTImpl)classExpr.getNextSibling();
        Object fieldInfo = 
            queryContext.getFieldInfo(classExpr.getTypeInfo(), field.getText());
        return queryContext.getElementType(fieldInfo);
    }

    /**
     * Analyses a MEMBER OF operation. 
     * @param op the MEMBER OF operator
     * @param value node representing the value to be tested
     * @param col the collection
     * @return the type info of the operator
     */
    public Object analyseMemberExpr(EJBQLASTImpl op, EJBQLASTImpl value, EJBQLASTImpl col)
    {
        Object valueTypeInfo = value.getTypeInfo();
        Object elementTypeInfo = analyseCollectionValuedCMRField(col);

        // handle error type
        if (queryContext.isErrorType(valueTypeInfo)
                || queryContext.isErrorType(elementTypeInfo)) {
            return queryContext.getErrorType();
        }
        
        // check value for input param which might have unknown type
        if (isInputParameter(value)) {
            // Note: If rightAST's typeInfo is unknown, it will change to that
            // given by elementTypeInfo.
            valueTypeInfo = env.analyseParameter(value, elementTypeInfo);
        }

        // check compatibility
        if (queryContext.isCompatibleWith(valueTypeInfo, elementTypeInfo)
                || queryContext.isCompatibleWith(elementTypeInfo, valueTypeInfo)) {
            return queryContext.getBooleanType();
        }
        
        // if this code is reached there is a compatibility problem 
        // with the value and the collection expr
        ErrorMsg.error(op.getLine(), op.getColumn(), 
            I18NHelper.getMessage(msgs, "EXC_CollectionElementTypeMismatch", //NOI18N
                queryContext.getTypeName(elementTypeInfo), 
                queryContext.getTypeName(valueTypeInfo)));
        return queryContext.getErrorType();
    }

    /**
     * Analyses the type of the element to be compatible with the type of the
     * value expression in the sense that element type can be cast into value
     * type without losing precision.
     * For instance, element type can be a double and value type can be an
     * integer.
     * @param elementAST given element
     * @param valueTypeInfo the type to be check for compatibility
     * @return the type info of the elementAST or queryContext.errorType
     */
    public Object analyseInCollectionElement(EJBQLASTImpl elementAST,
            Object valueTypeInfo)
    {
        Object elementTypeInfo = elementAST.getTypeInfo();

        // handle error type
        if (queryContext.isErrorType(valueTypeInfo)
                || queryContext.isErrorType(elementTypeInfo)) {
            return queryContext.getErrorType();
        }

        // if elementTypeInfo is Unknown, make it same as given valueTypeInfo.
        if (queryContext.isUnknownType(elementTypeInfo)) {
            return valueTypeInfo;
        }

        Object common = queryContext.getCommonOperandType(elementTypeInfo, valueTypeInfo);
        if (!queryContext.isErrorType(common)
                && elementTypeInfo.equals(common)) {
            return common;
        }

        // if this code is reached there is a compatibility problem
        // with the value and the collection expr
        ErrorMsg.error(elementAST.getLine(), elementAST.getColumn(),
            I18NHelper.getMessage(msgs, "EXC_CollectionElementTypeMismatch", //NOI18N
            queryContext.getTypeName(valueTypeInfo),
            queryContext.getTypeName(elementTypeInfo)));
        return queryContext.getErrorType();
    }


    
    /** 
     * Returns <code>true</code> if ast denotes a entity bean value.
     */
    public boolean isEntityBeanValue(EJBQLASTImpl ast)
    {
        boolean ret = false;
        if (isInputParameter(ast) ) {
            Object typeInfo = ast.getTypeInfo();
            ret = queryContext.isEjbOrInterfaceName(typeInfo); 
        } else {
            switch(ast.getType()) {
            case EJBQLTokenTypes.SINGLE_CMR_FIELD_ACCESS:
            case EJBQLTokenTypes.IDENTIFICATION_VAR:
                ret = true;
            }
        }
        return ret;
    }

    /** 
     * Returns <code>true</code> if ast denotes a literal.
     */
    public boolean isLiteral(EJBQLASTImpl ast)
    {
        int tokenType = ast.getType();
        return ((tokenType == EJBQLTokenTypes.INT_LITERAL)
                     || (tokenType == EJBQLTokenTypes.LONG_LITERAL)
                     || (tokenType == EJBQLTokenTypes.STRING_LITERAL)
                     || (tokenType == EJBQLTokenTypes.FLOAT_LITERAL)
                     || (tokenType == EJBQLTokenTypes.DOUBLE_LITERAL)
                     || (tokenType == EJBQLTokenTypes.TRUE)
                     || (tokenType == EJBQLTokenTypes.FALSE));
    }

    /** 
     * Returns <code>true</code> if ast denotes a input parameter access.
     */
    public boolean isInputParameter(EJBQLASTImpl ast)
    {
        return ast.getType() == EJBQLTokenTypes.POSITIONAL_PARAMETER;
    }
    
    /**
     * The method checks the specified node being an expression of type Char.
     * <em>Note:</em> If the expr's typeInfo is <code>UnknownType</code>, it
     * will be changed to Char.
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has the type Char.
     */
    public boolean analyseCharExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;

        // if unknown, make it Char
        if (queryContext.isUnknownType(exprType)) {
            exprType= queryContext.getCharType();
            expr.setTypeInfo(exprType);
            return true;
        }

        // expr must have the type Char
        if (!queryContext.isCharType(exprType)) {
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }    

    /**
     * The method checks the specified node being an expression of type String.
     * <em>Note:</em> If the expr's typeInfo is <code>UnknownType</code>, it
     * will be changed to String.
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has the type String.
     */
    public boolean analyseStringExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;

        // if unknown, make it String
        if (queryContext.isUnknownType(exprType)) {
            expr.setTypeInfo(queryContext.getStringType());
            return true;
        }
        
        // expr must have the type String
        if (!queryContext.isStringType(exprType)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_StringExprExpected", //NOI18N
                    queryContext.getTypeName(exprType)));
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }

    /**
     * The method checks the specified node being an expression of 
     * type int or java.lang.Integer.
     * <em>Note:</em> If the expr's typeInfo is <code>UnknownType</code>, it
     * will be changed to Int.
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has the type 
     * int or java.lang.Integer.
     */
    public boolean analyseIntExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;

        // if unknown, make it Int
        if (queryContext.isUnknownType(exprType)) {
            expr.setTypeInfo(queryContext.getIntType());
            return true;
        }

        // expr must have the type int or Integer
        if (!queryContext.isIntType(exprType)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_IntExprExpected", //NOI18N
                    queryContext.getTypeName(exprType)));
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }

    /**
     * The method checks the specified node being an expression of 
     * type double or java.lang.Double.
     * <em>Note:</em> If the expr's typeInfo is <code>UnknownType</code>, it
     * will be changed to Double.
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has the type 
     * double or java.lang.Double.
     */
    public boolean analyseDoubleExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;

        // if unknown, make it Double
        if (queryContext.isUnknownType(exprType)) {
            expr.setTypeInfo(queryContext.getDoubleType());
            return true;
        }

        // expr must have the type double or Double
        if (!queryContext.isDoubleType(exprType)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_DoubleExprExpected", //NOI18N
                    queryContext.getTypeName(exprType)));
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }

    /**
     * The method checks the specified node being an expression of a number type
     * (a numeric type or a number wrapper class).
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has a number type.
     */
    public boolean isNumberExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;
        
        // expr must have a number type
        if (!queryContext.isNumberType(exprType)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_NumberExprExpected", //NOI18N
                    queryContext.getTypeName(exprType)));
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }

    /**
     * The method checks the specified node being an expression of a number type
     * (a numeric type or a number wrapper class).
     * @param expr the expression to be checked
     * @return <code>true</code> if the specified expression has a number or
     * String type 
     */
    public boolean isNumberOrStringExpr(EJBQLASTImpl expr)
    {
        Object exprType = expr.getTypeInfo();
        
        // handle error type
        if (queryContext.isErrorType(exprType))
            return true;
        
        // expr must have a number type
        if (!queryContext.isNumberType(exprType)
                && !queryContext.isStringType(exprType)) {
            ErrorMsg.error(expr.getLine(), expr.getColumn(), 
                I18NHelper.getMessage(msgs,
                    "EXC_NumberOrStringExprExpected", //NOI18N
                    queryContext.getTypeName(exprType)));
            return false;
        }
        
        // everything is ok => return true;
        return true;
    }

    /** 
     * The method checks whether the specified node denotes a valid abstract 
     * schema type.
     * @param ident the node to be checked
     * @return the type info for the abstract bean class of the specified 
     * abstract schema type.
     */
    public Object checkAbstractSchemaType(EJBQLASTImpl ident)
    {
        String name = ident.getText();
        Object typeInfo = 
            queryContext.getTypeInfoForAbstractSchema(name);
        if (typeInfo == null) {
            ErrorMsg.error(ident.getLine(), ident.getColumn(), 
                I18NHelper.getMessage(msgs, 
                    "EXC_AbstractSchemNameExpected", name)); //NOI18N
            typeInfo = queryContext.getErrorType();
        }
        return typeInfo;
    }

    /**
     * Returns true if the specified text is a string literal consisting of a
     * single char. Escaped chars are counted as a single char such as \ uxxxx.
     */
    public boolean isSingleCharacterStringLiteral(String text)
    {
        int i = 0;
        int length = text.length();
        if (length == 0) {
            // empty string
            return false;
        }
        if (text.charAt(i) == '\\')
        {
            i++;
            if (i == length) {
                // string literal was '\'
                return true;
            }
            // escaped char => check the next char
            if (text.charAt(i) == 'u') {
                // unicode
                i +=5;
            }
            else if (('0' <= text.charAt(i)) && (text.charAt(i) <= '3')) {
                i++;
                if ((i < length) && isOctalDigit(text.charAt(i))) {
                    i++;
                    if ((i < length) && isOctalDigit(text.charAt(i))) {
                        i++;
                    }
                }
            }
            else if (isOctalDigit(text.charAt(i))) {
                i++;
                if ((i < length) && isOctalDigit(text.charAt(i))) {
                    i++;
                }
            }
            else {
                i++;
            }
        }
        else if (text.charAt(i) == '\''){
            // check special EJBQL single quote char
            i++;
            if ((i < length) && (text.charAt(i) == '\'')) {
                i++;
            }
        }
        else {
            i++;
        }
        // reached end of text?
        return (i == length);
    }

    /** Returns true if the specified char is an octal digit */
    public boolean isOctalDigit(char c)
    {
        return ('0' <= c && c <= '7');
    }

    //===== symbol table support 

    /**
     *
     */
    public Object declareIdent(String ident, Object decl) 
    {
        return symtab.declare(ident, decl);
    }
    
    /**
     *
     */
    public Object getDeclaration(String ident)
    {
        return symtab.getDeclaration(ident);
    }
}

