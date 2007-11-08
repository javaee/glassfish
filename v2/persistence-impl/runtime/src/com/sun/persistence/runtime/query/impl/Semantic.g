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
 * Semantic.g
 *
 * Created on November 19, 2001
 */

header
{
    package com.sun.persistence.runtime.query.impl;
    
    import java.util.ResourceBundle;
    import com.sun.persistence.utility.I18NHelper;
    import com.sun.persistence.runtime.query.QueryContext;
}

/**
 * This class defines the semantic analysis of the EJBQL compiler.
 * Input of this pass is the AST as produced by the parser,
 * that consists of EJBQLASTImpl nodes.
 * The result is a typed EJBQLASTImpl tree.
 *
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 */
class Semantic extends TreeParser;

options
{
    importVocab = EJBQL;
    buildAST = true;
    defaultErrorHandler = false;
    ASTLabelType = "EJBQLASTImpl"; //NOI18N
}

{
    /** Type info access helper. */
    protected QueryContext queryContext;
    
    /** The environment in which the query is being compiled. */
    protected Environment env;

    /** The helper. */
    protected SemanticHelper helper;

   /** I18N support. */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
        Semantic.class);
    
    /**
     * Initializes the semantic analysis.
     * @param queryContext type info access helper.
     * @param paramSupport parameter info helper.
     * @param env environment in which compilation is taking place
     */    
    public void init(QueryContext queryContext, Environment env) {
        this.queryContext = queryContext;
        this.env = env;
        this.helper = new SemanticHelper(queryContext, env);
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

// rules

query
    :   #(QUERY fromClause s:simpleSelectClause whereClause o:orderbyClause)
        {
            helper.checkSelectOrderbyClause(#s, #o);
        }
    ;

// ----------------------------------
// rules: from clause
// ----------------------------------

fromClause
    :   #( FROM ( identificationVarDecl )+ )
    ;

identificationVarDecl
    :   collectionMemberDecl
    |   rangeVarDecl
    ;

collectionMemberDecl
    :   #(in:IN p:collectionValuedPathExpression var:IDENT)
        {
            Object typeInfo = helper.analyseCollectionValuedCMRField(#p);
            String name = #var.getText();
            Object identVar = new IdentificationVariable(name, typeInfo);
            if (helper.declareIdent(name, identVar) != null) {
                ErrorMsg.error(#var.getLine(), #var.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_MultipleDeclaration", name)); //NOI18N
            }
            #var.setType(IDENTIFICATION_VAR_DECL);
            #var.setTypeInfo(typeInfo);
            #var.setNavigationId(name);
            #in.setType(COLLECTION_MEMBER_DECL);
        }
    ;

rangeVarDecl
    :   #(RANGE abstractSchemaName:ABSTRACT_SCHEMA_NAME var:IDENT)
        {
            // check abstract schema name
            Object typeInfo = 
                helper.checkAbstractSchemaType(#abstractSchemaName);
            #abstractSchemaName.setTypeInfo(typeInfo);

            // check identification variable
            String name = #var.getText();
            Object identVar = new IdentificationVariable(name, typeInfo);
            if (helper.declareIdent(name, identVar) != null) {
                ErrorMsg.error(#var.getLine(), #var.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_MultipleDeclaration", name)); //NOI18N
            }
            #var.setType(IDENTIFICATION_VAR_DECL);
            #var.setTypeInfo(typeInfo);
            #var.setNavigationId(name);
        }
    ;

// ----------------------------------
// rules: select clause
// ----------------------------------

simpleSelectClause
    :   #( s:SELECT distinct p:projection )
        {
            env.checkQueryResultType(#p.getTypeInfo());
            #s.setTypeInfo(#p.getTypeInfo());
        }
    ;

distinct
    :   DISTINCT
    |   // empty rule
        {
            // Insert DISTINCT keyword, in the case of a multi-object selector 
            // having java.util.Set as return type
            if (env.requiresDistinct()) {
                #distinct = #[DISTINCT,"distinct"];
            }
        }
    ; 

projection
    :   singleValuedPathExpression
    |   var:IDENT
        {
            String name = #var.getText();
            Object decl = helper.getDeclaration(name);
            Object typeInfo = null;
            if ((decl != null) && 
                (decl instanceof IdentificationVariable)) {
                #var.setType(IDENTIFICATION_VAR);
                #var.setNavigationId(name);
                typeInfo = ((IdentificationVariable)decl).getTypeInfo();
            }
            else {
                ErrorMsg.error(#var.getLine(), #var.getColumn(), 
                    I18NHelper.getMessage(msgs, 
                        "EXC_IdentificationVariableExcepted", name)); //NOI18N
            }
            #var.setTypeInfo(typeInfo);
        }
    |   aggregateSelectExpr
    ;
    
aggregateSelectExpr
    :   #( sum:SUM ( DISTINCT )? sumExpr:cmpPathExpression )
        {
            // check numeric type
            Object typeInfo = #sumExpr.getTypeInfo();
            if (!queryContext.isNumberType(typeInfo) ||
                    queryContext.isCharType(typeInfo)) {
                ErrorMsg.error(#sumExpr.getLine(), #sumExpr.getColumn(),
                    I18NHelper.getMessage(msgs,
                        "EXC_NumberExprExpected", //NO18N
                        queryContext.getTypeName(typeInfo)));
            }
            #sum.setTypeInfo(queryContext.getSumReturnType(typeInfo));
            env.setAggregate(true);
        }
    |   #( avg:AVG ( DISTINCT )? avgExpr:cmpPathExpression )
        {
            // check numeric type
            Object typeInfo = #avgExpr.getTypeInfo();
            if (!queryContext.isNumberType(typeInfo) ||
                    queryContext.isCharType(typeInfo)) {
                ErrorMsg.error(#avgExpr.getLine(), #avgExpr.getColumn(),
                    I18NHelper.getMessage(msgs,
                        "EXC_NumberExprExpected", //NO18N
                        queryContext.getTypeName(typeInfo)));
            }
            #avg.setTypeInfo(queryContext.getAvgReturnType(typeInfo));
            env.setAggregate(true);
        }
    |   #( min:MIN ( DISTINCT )? minExpr:cmpPathExpression )
        {
            // check orderable type
            Object typeInfo = #minExpr.getTypeInfo();
            if (!queryContext.isOrderableType(typeInfo)) {
                ErrorMsg.error(#minExpr.getLine(), #minExpr.getColumn(),
                    I18NHelper.getMessage(msgs,
                        "EXC_OrderableExpected", //NO18N
                        queryContext.getTypeName(typeInfo)));
            }
            #min.setTypeInfo(queryContext.getMinMaxReturnType(typeInfo));
            env.setAggregate(true);
        }
    |   #( max:MAX ( DISTINCT )? maxExpr:cmpPathExpression )
        {
            // check orderable type
            Object typeInfo = #maxExpr.getTypeInfo();
            if (!queryContext.isOrderableType(typeInfo)) {
                ErrorMsg.error(#maxExpr.getLine(), #maxExpr.getColumn(),
                    I18NHelper.getMessage(msgs,
                        "EXC_OrderableExpected", //NO18N
                        queryContext.getTypeName(typeInfo)));
            }
            #max.setTypeInfo(queryContext.getMinMaxReturnType(typeInfo));
            env.setAggregate(true);
        }
    |   #( c:COUNT ( DISTINCT )? countExpr )
        {
            #c.setTypeInfo(queryContext.getLongClassType());
            env.setAggregate(true);
        }
    ;

countExpr
    :   v:IDENT
        {
            String name = #v.getText();
            Object decl = helper.getDeclaration(name);
            Object typeInfo = null;
            if ((decl != null) && 
                (decl instanceof IdentificationVariable)) {
                #v.setType(IDENTIFICATION_VAR);
                #v.setNavigationId(name);
                typeInfo = ((IdentificationVariable)decl).getTypeInfo();
            }
            else {
                ErrorMsg.error(#v.getLine(), #v.getColumn(), 
                    I18NHelper.getMessage(msgs, 
                    "EXC_IdentificationVariableExcepted", name)); //NOI18N
            }
            #v.setTypeInfo(typeInfo);
        }
    |   singleValuedPathExpression
    ;

// ----------------------------------
// rules: where clause
// ----------------------------------

whereClause
    :   #( WHERE e:expression )
        {
            Object typeInfo = #e.getTypeInfo();
            if (!queryContext.isBooleanType(typeInfo)) {
                ErrorMsg.error(#e.getLine(), #e.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_BooleanWhereClauseExpected",  //NOI18N
                        queryContext.getTypeName(typeInfo)));
            }
        }
    ;

// ----------------------------------
// rules: order by clause
// ----------------------------------

orderbyClause
    :   #( ORDER ( orderbyItem )+ )
    |   // empty rule
    ;

orderbyItem
    :   expr:cmpPathExpression ( ASC | DESC )
        {
            // check orderable type
            Object typeInfo = #expr.getTypeInfo();
            if (!queryContext.isOrderableType(typeInfo)) {
                ErrorMsg.error(#expr.getLine(), #expr.getColumn(),
                    I18NHelper.getMessage(msgs,
                        "EXC_OrderableOrderbyClauseExpected", //NO18N
                        queryContext.getTypeName(typeInfo)));
            }
        }
    ;

// ----------------------------------
// rules: expression
// ----------------------------------

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
    |   function
    |   primary
    ;

conditionalExpr
    :   #( op1:AND left1:expression right1:expression )
        {
            #op1.setTypeInfo(helper.analyseConditionalExpr(#op1, #left1, #right1));
        }
    |   #( op2:OR  left2:expression right2:expression )
        {
            #op2.setTypeInfo(helper.analyseConditionalExpr(#op2, #left2, #right2));
        }
    ;

relationalExpr
    :   #( op1:EQUAL left1:expression right1:expression )
        {
            #op1.setTypeInfo(helper.analyseEqualityExpr(#op1, #left1, #right1));
            if (helper.isEntityBeanValue(#left1)
                && helper.isEntityBeanValue(#right1)) {
                #op1.setType(ENTITY_EQUAL);
            }
        }
    |   #( op2:NOT_EQUAL left2:expression right2:expression )
        {
            #op2.setTypeInfo(helper.analyseEqualityExpr(#op2, #left2, #right2));
            if (helper.isEntityBeanValue(#left2)
                && helper.isEntityBeanValue(#right2)) {
                #op2.setType(ENTITY_NOT_EQUAL);
            }
        }
    |   #( op3:LT left3:expression right3:expression )
        {
            #op3.setTypeInfo(helper.analyseRelationalExpr(#op3, #left3, #right3));
        }
    |   #( op4:LE left4:expression right4:expression )
        {
            #op4.setTypeInfo(helper.analyseRelationalExpr(#op4, #left4, #right4));
        }
    |   #( op5:GT left5:expression right5:expression )
        {
            #op5.setTypeInfo(helper.analyseRelationalExpr(#op5, #left5, #right5));
        }
    |   #( op6:GE left6:expression right6:expression )
        {
            #op6.setTypeInfo(helper.analyseRelationalExpr(#op6, #left6, #right6));
        }
    ;

binaryArithmeticExpr
    :   #( op1:PLUS left1:expression right1:expression )
        {
            #op1.setTypeInfo(helper.analyseBinaryArithmeticExpr(#op1, #left1, #right1));
        }
    |   #( op2:MINUS left2:expression right2:expression )
        {
            #op2.setTypeInfo(helper.analyseBinaryArithmeticExpr(#op2, #left2, #right2));
        }
    |   #( op3:STAR left3:expression right3:expression )
        {
            #op3.setTypeInfo(helper.analyseBinaryArithmeticExpr(#op3, #left3, #right3));
        }
    |   #( op4:DIV left4:expression right4:expression )
        {
            #op4.setTypeInfo(helper.analyseBinaryArithmeticExpr(#op4, #left4, #right4));
        }
    ;

unaryExpr
    :   #( op1:UNARY_PLUS arg1:expression )
        {
            #op1.setTypeInfo(helper.analyseUnaryArithmeticExpr(#op1, #arg1));
        }
    |   #( op2:UNARY_MINUS arg2:expression )
        {
            #op2.setTypeInfo(helper.analyseUnaryArithmeticExpr(#op2, #arg2));
        }
    |   #( op3:NOT arg3:expression )
        {
            Object typeInfo = queryContext.getErrorType();
            Object arg = #arg3.getTypeInfo();
            if (queryContext.isErrorType(arg))
                typeInfo = queryContext.getErrorType();
            else if (queryContext.isBooleanType(arg))
                typeInfo = arg;
            else {
                ErrorMsg.error(#op3.getLine(), #op3.getColumn(), 
                    I18NHelper.getMessage(msgs, "EXC_InvalidArguments", //NOI18N
                        #op3.getText())); 
            }
            #op3.setTypeInfo(typeInfo);
        }
    ;

betweenExpr 
    :   #( op1:BETWEEN expr1:expression lower1:expression upper1:expression )
        {
            #op1.setTypeInfo((helper.isNumberExpr(#expr1) && helper.isNumberExpr(#lower1) && helper.isNumberExpr(#upper1)) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    |   #( op2:NOT_BETWEEN expr2:expression lower2:expression upper2:expression )
        {
            #op2.setTypeInfo((helper.isNumberExpr(#expr2) && helper.isNumberExpr(#lower2) && helper.isNumberExpr(#upper2)) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    ;

likeExpr
    :   #( op1:LIKE expr1:cmpPathExpression pattern escape )
        {
            #op1.setTypeInfo(helper.analyseStringExpr(#expr1) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    |   #( op2:NOT_LIKE expr2:cmpPathExpression pattern escape )
        {
            #op2.setTypeInfo(helper.analyseStringExpr(#expr2) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    ;

pattern
    :   STRING_LITERAL 
    |   p:inputParameter
        {
            if (!helper.analyseStringExpr(#p)) {
                ErrorMsg.error(#p.getLine(), #p.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_InvalidPatternDefinition",
                        #p.getText())); //NOI18N
            }
        }
    ;

escape
    :   #( ESCAPE escapeCharacter )
    |   // empty rule
    ;

escapeCharacter
    :   s:STRING_LITERAL
        {
            String literal = #s.getText();
            // String must be single charater string literal =>
            // either '<char>' or ''''
            if (!helper.isSingleCharacterStringLiteral(#s.getText())) {
                ErrorMsg.error(#s.getLine(), #s.getColumn(),
                    I18NHelper.getMessage(msgs, 
                        "EXC_InvalidEscapeDefinition", #s.getText())); //NOI18N
            }
        }
    |   p:inputParameter
        {
            if (!helper.analyseCharExpr(#p)) {
                ErrorMsg.error(#p.getLine(), #p.getColumn(),
                    I18NHelper.getMessage(msgs, 
                        "EXC_InvalidEscapeParameterDefinition", #p.getText())); //NOI18N
            }
        }
    ;

inExpr
    :   #( op1:IN expr1:cmpPathExpression inCollection[#expr1.getTypeInfo()] )
        {
            #op1.setTypeInfo(helper.isNumberOrStringExpr(#expr1) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    |   #( op2:NOT_IN expr2:cmpPathExpression inCollection[#expr2.getTypeInfo()] )
        {
            #op2.setTypeInfo(helper.isNumberOrStringExpr(#expr2) ? 
                queryContext.getBooleanType() : queryContext.getErrorType());
        }
    ;

nullComparisonExpr
    :   #( op1:NULL ( singleValuedPathExpression | inputParameter ) )
        {
            #op1.setTypeInfo(queryContext.getBooleanType());
        }
    |   #( op2:NOT_NULL ( singleValuedPathExpression | inputParameter ) )
        {
            #op2.setTypeInfo(queryContext.getBooleanType());
        }
    ;

emptyCollectionComparisonExpr
{ 
    Object elementTypeInfo = null; 
}
    :   #( op1:EMPTY col1:collectionValuedPathExpression )
        {
            elementTypeInfo = helper.analyseCollectionValuedCMRField(#col1);
            #op1.setTypeInfo(queryContext.isErrorType(elementTypeInfo) ? 
                queryContext.getErrorType() : queryContext.getBooleanType());
        }
    |   #( op2:NOT_EMPTY col2:collectionValuedPathExpression )
        {
            elementTypeInfo = helper.analyseCollectionValuedCMRField(#col2);
            #op2.setTypeInfo(queryContext.isErrorType(elementTypeInfo) ? 
                queryContext.getErrorType() : queryContext.getBooleanType());
        }
    ;

collectionMemberExpr
    :   #( op1:MEMBER value1:member col1:collectionValuedPathExpression )
        {
            #op1.setTypeInfo(helper.analyseMemberExpr(#op1, #value1, #col1));
        }
    |   #( op2:NOT_MEMBER value2:member col2:collectionValuedPathExpression )
        {
            #op2.setTypeInfo(helper.analyseMemberExpr(#op2, #value2, #col2));
        }
    ;

member
    :   identificationVariable
    |   inputParameter
    |   singleValuedCmrPathExpression
    ;

function
    :   concat
    |   substring
    |   length
    |   locate
    |   abs
    |   sqrt
    |   mod
    ;

concat
    :   #( op:CONCAT arg1:expression arg2:expression )
        {
            #op.setTypeInfo((helper.analyseStringExpr(#arg1)
                    && helper.analyseStringExpr(#arg2)) ?
                queryContext.getStringType() : queryContext.getErrorType());
        }
    ;

substring
    :   #( op:SUBSTRING arg1:expression arg2:expression arg3:expression )
        {
            #op.setTypeInfo((helper.analyseStringExpr(#arg1)
                    && helper.analyseIntExpr(#arg2)
                    && helper.analyseIntExpr(#arg3)) ? 
                queryContext.getStringType() : queryContext.getErrorType());
        }
    ;

length
    :   #( op:LENGTH arg:expression )
        {
            #op.setTypeInfo(helper.analyseStringExpr(#arg) ? 
                queryContext.getIntType() : queryContext.getErrorType());
        }
    ;

locate
    :   #( op:LOCATE arg1:expression arg2:expression ( arg3:expression )? )
        {
            #op.setTypeInfo((helper.analyseStringExpr(#arg1)
                    && helper.analyseStringExpr(#arg2)
                    && ((#arg3 == null) || helper.analyseIntExpr(#arg3))) ?
                queryContext.getIntType() : queryContext.getErrorType());
        }
    ;

abs
    :   #( op:ABS expr:expression )
        {
            #op.setTypeInfo(helper.isNumberExpr(#expr) ? 
                #expr.getTypeInfo() : queryContext.getErrorType());
        }
    ;

sqrt
    :   #( op:SQRT expr:expression )
        {
            #op.setTypeInfo(helper.analyseDoubleExpr(#expr) ? 
                #expr.getTypeInfo() : queryContext.getErrorType());
        }
    ;

mod
    :   #( op:MOD arg1:expression arg2:expression )
        {
            #op.setTypeInfo((helper.analyseIntExpr(#arg1)
                    && helper.analyseIntExpr(#arg2)) ? 
                queryContext.getIntType() : queryContext.getErrorType());
        }
    ;

primary
    :   literal
    |   singleValuedPathExpression
    |   identificationVariable
    |   inputParameter
    ;

literal
    :   b1:TRUE          { #b1.setTypeInfo(queryContext.getBooleanType()); }
    |   b2:FALSE         { #b2.setTypeInfo(queryContext.getBooleanType()); }
    |   s:STRING_LITERAL { #s.setTypeInfo(queryContext.getStringType()); }
    |   i:INT_LITERAL    { #i.setTypeInfo(queryContext.getIntType()); }
    |   l:LONG_LITERAL   { #l.setTypeInfo(queryContext.getLongType()); }
    |   f:FLOAT_LITERAL  { #f.setTypeInfo(queryContext.getFloatType()); }
    |   d:DOUBLE_LITERAL { #d.setTypeInfo(queryContext.getDoubleType()); }
    ;

pathExpression
    :   #(  dot:DOT  o:objectDenoter i:IDENT )
        {
            String fieldName = #i.getText();
            Object typeInfo = #o.getTypeInfo();
            Object fieldTypeInfo = 
                queryContext.getFieldType(typeInfo, fieldName);
            if (fieldTypeInfo == null) {
                // field is not known
                ErrorMsg.error(#i.getLine(), #i.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_UnknownField", fieldName, //NOI18N
                        queryContext.getAbstractSchemaForTypeInfo(typeInfo)));
                fieldTypeInfo = queryContext.getErrorType();
            }
            else {
                Object fieldInfo = queryContext.getFieldInfo(typeInfo, fieldName);
                if (fieldInfo == null) {
                    ErrorMsg.fatal(I18NHelper.getMessage(msgs, 
                            "ERR_MissingFieldInfo",  //NOI18N
                            fieldName, queryContext.getTypeName(typeInfo)));
                }
                if (!queryContext.isRelationship(fieldInfo)) {
                    // field is not a relationship => cmp field
                    #i.setType(CMP_FIELD);
                    #dot.setType(CMP_FIELD_ACCESS);
                }
                else if (queryContext.isCollectionType(fieldTypeInfo)) {
                    // field is a relationship of a collection type =>
                    // collection valued cmr field
                    #i.setType(COLLECTION_CMR_FIELD);
                    #dot.setType(COLLECTION_CMR_FIELD_ACCESS);
                }
                else {
                    // field is a relationship of a non collection type =>
                    // single valued cmr field
                    #i.setType(SINGLE_CMR_FIELD);
                    #dot.setType(SINGLE_CMR_FIELD_ACCESS);
                }
            }
            String navigationId = #o.getNavigationId() + "." + fieldName;
            #dot.setNavigationId(navigationId);
            #dot.setTypeInfo(fieldTypeInfo);
            #i.setTypeInfo(fieldTypeInfo);
        }
    ;

objectDenoter
    :   identificationVariable
    |   singleValuedCmrPathExpression
    ;

identificationVariable
    :   i:IDENT 
        {
            String name = #i.getText();
            Object decl = helper.getDeclaration(name);
            // check for identification variables
            if ((decl != null) && (decl instanceof IdentificationVariable)) {
                #i.setType(IDENTIFICATION_VAR);
                #i.setTypeInfo(((IdentificationVariable)decl).getTypeInfo());
                #i.setNavigationId(name);
            }
            else {
                #i.setTypeInfo(queryContext.getErrorType());
                ErrorMsg.error(#i.getLine(), #i.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_UndefinedIdentifier", name)); //NOI18N
                        
            }
        }
    ;

singleValuedPathExpression
    :   p:pathExpression
        {
            int fieldTokenType = #p.getType();
            if ((fieldTokenType != SINGLE_CMR_FIELD_ACCESS) && 
                (fieldTokenType != CMP_FIELD_ACCESS)) {
                EJBQLASTImpl classExpr = (EJBQLASTImpl)#p.getFirstChild();
                EJBQLASTImpl field = (EJBQLASTImpl)classExpr.getNextSibling();
                ErrorMsg.error(field.getLine(), field.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_SingleValuedCMROrCMPFieldExpected", //NOI18N
                        field.getText(), queryContext.getTypeName(field.getTypeInfo())));
                #p.setType(SINGLE_CMR_FIELD_ACCESS);
            }
        }
    ;

cmpPathExpression
    :   p:pathExpression
        {
            int fieldTokenType = #p.getType();
            if ((fieldTokenType != CMP_FIELD_ACCESS)) {
                EJBQLASTImpl classExpr = (EJBQLASTImpl)#p.getFirstChild();
                EJBQLASTImpl field = (EJBQLASTImpl)classExpr.getNextSibling();
                ErrorMsg.error(field.getLine(), field.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_CMPFieldExpected", //NOI18N
                        field.getText(), queryContext.getTypeName(field.getTypeInfo())));
                #p.setType(CMP_FIELD_ACCESS);
            }
        }
    ;

singleValuedCmrPathExpression
    :   p:pathExpression
        {
            int fieldTokenType = #p.getType();
            if (fieldTokenType != SINGLE_CMR_FIELD_ACCESS) {
                EJBQLASTImpl classExpr = (EJBQLASTImpl)#p.getFirstChild();
                EJBQLASTImpl field = (EJBQLASTImpl)classExpr.getNextSibling();
                ErrorMsg.error(field.getLine(), field.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_SingleValuedCMRFieldExpected", //NOI18N
                        field.getText(), queryContext.getTypeName(field.getTypeInfo())));
                #p.setType(COLLECTION_CMR_FIELD_ACCESS);
            }
        }
    ;

collectionValuedPathExpression
    :   p:pathExpression
        {
            int fieldTokenType = #p.getType();
            if (fieldTokenType != COLLECTION_CMR_FIELD_ACCESS) {
                EJBQLASTImpl classExpr = (EJBQLASTImpl)#p.getFirstChild();
                EJBQLASTImpl field = (EJBQLASTImpl)classExpr.getNextSibling();
                ErrorMsg.error(field.getLine(), field.getColumn(),
                    I18NHelper.getMessage(msgs, "EXC_CollectionValuedCMRFieldExpected", //NOI18N
                        field.getText(), queryContext.getTypeName(field.getTypeInfo())));
                #p.setType(COLLECTION_CMR_FIELD_ACCESS);
            }
        }
    ;

inCollection [Object valueExprTypeInfo]
    :   ( inCollectionElement[valueExprTypeInfo] )+
    ;

inCollectionElement [Object valueExprTypeInfo]
    :   l:literal
        {
            l.setTypeInfo(helper.analyseInCollectionElement(#l, valueExprTypeInfo));
        }
    |   i:inputParameter
        {
            i.setTypeInfo(helper.analyseInCollectionElement(#i, valueExprTypeInfo));
        }
    ;

inputParameter
    :   param:POSITIONAL_PARAMETER
        {
            Object typeInfo = queryContext.getTypeInfo(
                env.getParameterType(#param.getText()));

            #param.setTypeInfo(typeInfo);
        }
    ;

