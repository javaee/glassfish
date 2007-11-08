/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */



/*
 * EJBQL.g
 *
 * Created on November 12, 2001
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
 * This class defines the lexical analysis for the EJBQL compiler.
 *
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 */
class EJBQLLexer extends Lexer;
options
{
    k = 2;
    exportVocab = EJBQL;
    charVocabulary = '\u0000'..'\uFFFE'; //NOI18N
    caseSensitiveLiterals = false;
}

tokens {

    // keywords 
    SELECT = "select"; //NOI18N
    FROM = "from"; //NOI18N
    WHERE = "where"; //NOI18N
    DISTINCT = "distinct"; //NOI18N
    OBJECT = "object"; //NOI18N
    NULL = "null"; //NOI18N
    TRUE = "true"; //NOI18N
    FALSE = "false"; //NOI18N
    NOT = "not"; //NOI18N
    AND = "and"; //NOI18N
    OR = "or"; //NOI18N
    BETWEEN = "between"; //NOI18N
    LIKE = "like"; //NOI18N
    IN = "in"; //NOI18N
    AS = "as"; //NOI18N
    UNKNOWN = "unknown"; //NOI18N
    EMPTY = "empty"; //NOI18N
    MEMBER = "member"; //NOI18N
    OF = "of"; //NOI18N
    IS = "is"; //NOI18N

    // function/operator names treated as keywords
    ESCAPE = "escape"; //NOI18N
    CONCAT = "concat"; //NOI18N
    SUBSTRING = "substring"; //NOI18N
    LOCATE = "locate"; //NOI18N
    LENGTH = "length"; //NOI18N
    ABS = "abs"; //NOI18N
    SQRT = "sqrt"; //NOI18N
    MOD = "mod"; //NOI18N

    // aggregate functions
    AVG = "avg"; //NOI18N
    MAX = "max"; //NOI18N
    MIN = "min"; //NOI18N
    SUM = "sum"; //NOI18N
    COUNT = "count"; //NOI18N

    // order by
    ORDER = "order"; //NOI18N
    BY = "by"; //NOI18N
    ASC = "asc"; //NOI18N
    DESC = "desc"; //NOI18N

    // relational operators
    EQUAL;
    ENTITY_EQUAL;
    NOT_EQUAL;
    ENTITY_NOT_EQUAL;
    GE;
    GT;
    LE;
    LT;

    // arithmetic operators
    PLUS;
    MINUS;
    STAR;
    DIV;

    // literals
    STRING_LITERAL;
    INT_LITERAL;
    LONG_LITERAL;
    FLOAT_LITERAL;
    DOUBLE_LITERAL;

    // other token types
    IDENT;
    DOT;
    POSITIONAL_PARAMETER;

    // lexer internal token types
    LPAREN;
    RPAREN;
    COMMA;
    WS;
    HEX_DIGIT;
    EXPONENT;
    FLOAT_SUFFIX;
    UNICODE_DIGIT;
    UNICODE_STR;
}

{
    /**
     * The width of a tab stop.
     * This value is used to calculate the correct column in a line
     * conatining a tab character.
     */
    protected static int TABSIZE = 4;

    /** */
    protected static final int EOF_CHAR = 65535; // = (char) -1 = EOF

    /** I18N support. */
    protected final static ResourceBundle msgs = 
        I18NHelper.loadBundle(EJBQLLexer.class);
    
    /**
     *
     */
    public void tab() 
    {
        int column = getColumn();
        int newColumn = (((column-1)/TABSIZE)+1)*TABSIZE+1;
        setColumn(newColumn);
    }

    /** */
    public void reportError(int line, int column, String s)
    {
        ErrorMsg.error(line, column, s);
    }

    /** Report lexer exception errors caught in nextToken(). */
    public void reportError(RecognitionException e)
    {
        handleANTLRException(e);
    }

    /** Lexer error-reporting function. */
    public void reportError(String s)
    {
        ErrorMsg.error(0, 0, s);
    }

    /** Lexer warning-reporting function. */
    public void reportWarning(String s)
    {
        throw new EJBQLException(s);
    }

    /**
     *
     */
    public static void handleANTLRException(ANTLRException ex)
    {
        if (ex instanceof MismatchedCharException) {
            MismatchedCharException mismatched = (MismatchedCharException)ex;
            if (mismatched.mismatchType == MismatchedCharException.CHAR) {
                if (mismatched.foundChar == EOF_CHAR) {
                    ErrorMsg.error(mismatched.getLine(), mismatched.getColumn(),
                        //TBD: bundle key
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedEOF")); //NOI18N
                }
                else {
                    ErrorMsg.error(mismatched.getLine(), mismatched.getColumn(), 
                        I18NHelper.getMessage(msgs, "EXC_ExpectedCharFound", //NOI18N
                            String.valueOf((char)mismatched.expecting), 
                            String.valueOf((char)mismatched.foundChar)));
                }
                return;
            }
        }
        else if (ex instanceof MismatchedTokenException) {
            MismatchedTokenException mismatched = (MismatchedTokenException)ex;
            Token token = mismatched.token;
            if ((mismatched.mismatchType == MismatchedTokenException.TOKEN) &&
                (token != null)) {
                if (token.getType() == Token.EOF_TYPE) {
                    ErrorMsg.error(token.getLine(), token.getColumn(),
                        //TBD: bundle key
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedEOF")); //NOI18N
                }
                else {
                    ErrorMsg.error(token.getLine(), token.getColumn(), 
                        I18NHelper.getMessage(msgs, "EXC_SyntaxErrorAt", token.getText())); //NOI18N
                }
                return;
            }
        }
        else if (ex instanceof NoViableAltException) {
            Token token = ((NoViableAltException)ex).token;
            if (token != null) {
                if (token.getType() == Token.EOF_TYPE) {
                    ErrorMsg.error(token.getLine(), token.getColumn(), 
                        //TBD: bundle key
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedEOF")); //NOI18N
                }
                else {
                    ErrorMsg.error(token.getLine(), token.getColumn(), 
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedToken", token.getText())); //NOI18N
                }
                return;
            }
        }
        else if (ex instanceof NoViableAltForCharException) {
            NoViableAltForCharException noViableAlt = (NoViableAltForCharException)ex;
            ErrorMsg.error(noViableAlt.getLine(), noViableAlt.getColumn(), 
                I18NHelper.getMessage(msgs, "EXC_UnexpectedChar", new Character(noViableAlt.foundChar)));//NOI18N
        }
        else if (ex instanceof TokenStreamRecognitionException) {
            handleANTLRException(((TokenStreamRecognitionException)ex).recog);
        }

        // no special handling from aboves matches the exception if this line is reached =>
        // make it a syntax error
        int line = 0;
        int column = 0;
        if (ex instanceof RecognitionException) {
            line = ((RecognitionException)ex).getLine();
            column = ((RecognitionException)ex).getColumn();
        }
        ErrorMsg.error(line, column, I18NHelper.getMessage(msgs, "EXC_SyntaxError")); //NOI18N
    }
}

// OPERATORS

LPAREN          :   '('     ;
RPAREN          :   ')'     ;
COMMA           :   ','     ;
//DOT           :   '.'     ;
EQUAL           :   '='     ; 
NOT_EQUAL       :   "<>"    ; //NOI18N
GE              :   ">="    ; //NOI18N
GT              :   ">"     ; //NOI18N
LE              :   "<="    ; //NOI18N
LT              :   '<'     ;
PLUS            :   '+'     ;
DIV             :   '/'     ;
MINUS           :   '-'     ;
STAR            :   '*'     ;

// Whitespace -- ignored
WS  
    :   (   ' '
        |   '\t'
        |   '\f'
        )
        { _ttype = Token.SKIP; }
    ;

NEWLINE
    :   (   "\r\n"  //NOI18N
        |   '\r'
        |   '\n'
        )
        { 
            newline(); 
            _ttype = Token.SKIP; 
        }
    ;

// input parameter
POSITIONAL_PARAMETER
    :  '?' ('1'..'9') ('0'..'9')*
    ;

// string literals
STRING_LITERAL
    :  '\'' ( "''" | ESC | ~'\'' )* '\'' //NOI18N
    ;

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
    :   '\\'
        (   options { warnWhenFollowAmbig = false; }
        :   'n'
        |   'r'
        |   't'
        |   'b'
        |   'f'
        |   '"' //NOI18N
            // Note, EJBQL uses a quote to escape a quote
            // |   '\'' 
        |   '\\'
        |   ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT 
        |   ('0'..'3')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   ('0'..'7')
                (   
                    options {
                        warnWhenFollowAmbig = false;
                    }
                :   '0'..'7'
                )?
            )?
        |   ('4'..'7')
            (
                options {
                    warnWhenFollowAmbig = false;
                }
            :   ('0'..'7')
            )?
        )?
    ;

// hexadecimal digit
protected
HEX_DIGIT
    :   ('0'..'9'|'A'..'F'|'a'..'f')
    ;

// a numeric literal
INT_LITERAL
    {   
        boolean isDecimal=false;
        int tokenType = DOUBLE_LITERAL; 
    }
    :   '.' {_ttype = DOT;}
            (('0'..'9')+ {tokenType = DOUBLE_LITERAL;}
             (EXPONENT)? 
             (tokenType = FLOATINGPOINT_SUFFIX)?
            {_ttype = tokenType;} )?
    |   (   '0' {isDecimal = true;} // special case for just '0'
            (   ('x'|'X')
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
        (   ('l'|'L') { _ttype = LONG_LITERAL; }
        
        // only check to see if it's a float if looks like decimal so far
        |   {isDecimal}?
            {tokenType = DOUBLE_LITERAL;} 
            (   '.' ('0'..'9')* (EXPONENT)? 
                ( tokenType = FLOATINGPOINT_SUFFIX)?
            |   EXPONENT ( tokenType = FLOATINGPOINT_SUFFIX)?
            |   tokenType = FLOATINGPOINT_SUFFIX
            )
            { _ttype = tokenType; }
        )?
    ;

// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
    :   ('e'|'E') ('+'|'-')? ('0'..'9')+
    ;

protected
FLOATINGPOINT_SUFFIX returns [int tokenType]
    : 'f' { tokenType = FLOAT_LITERAL; } 
    | 'F' { tokenType = FLOAT_LITERAL; } 
    | 'd' { tokenType = DOUBLE_LITERAL; } 
    | 'D' { tokenType = DOUBLE_LITERAL; } 
    ;

// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer

IDENT : IDENT_INTERNAL ;

protected 
IDENT_INTERNAL
    options {testLiterals=true;} //NOI18N
    :   (   'a'..'z'
        |   'A'..'Z'
        |   '_'
        |   '$'
        |   UNICODE_ESCAPE
        |   c1:'\u0080'..'\uFFFE'
            { 
                if (!Character.isJavaIdentifierStart(c1)) {
                    ErrorMsg.error(getLine(), getColumn(), 
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedChar", String.valueOf(c1)));//NOI18N
                }
            }
        ) 
        (   'a'..'z'
        |   'A'..'Z'
        |   '_'
        |   '$'
        |   '0'..'9'
        |   UNICODE_ESCAPE
        |   c2:'\u0080'..'\uFFFE'
            {   
                if (!Character.isJavaIdentifierPart(c2)) {
                    ErrorMsg.error(getLine(), getColumn(), 
                        I18NHelper.getMessage(msgs, "EXC_UnexpectedChar", String.valueOf(c2)));//NOI18N
                }
            }
        )*
    ;

protected
UNICODE_ESCAPE
    : '\\' ('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
        {
            try {
                String tmp = text.toString();
                char c  = (char)Integer.parseInt(tmp.substring(tmp.length() - 4,
                        tmp.length()), 16);
                // problems using ANTLR feature $setText => use generated code
                text.setLength(_begin); 
                text.append(new Character(c).toString());
            }
            catch (NumberFormatException ex) {
                ErrorMsg.fatal(I18NHelper.getMessage(msgs, 
                        "ERR_UnexpectedExceptionUnicode"), ex); //NOI18N
            }
        }
    ;

//===== Parser Class Definitions =====

/**
 * This class defines the syntax analysis (parser) of the EJBQL compiler.
 *
 * @author  Michael Bouschen
 */
class EJBQLParser extends Parser;

options {
    k = 2;                   // two token lookahead
    exportVocab = EJBQL;
    buildAST = true;
    ASTLabelType = "EJBQLASTImpl"; // NOI18N
}

tokens
{
    // root
    QUERY;

    // special from clause tokenes
    RANGE;
    COLLECTION_MEMBER_DECL;

    // special dot expresssion
    CMP_FIELD_ACCESS;
    SINGLE_CMR_FIELD_ACCESS;
    COLLECTION_CMR_FIELD_ACCESS;

    // identifier 
    IDENTIFICATION_VAR;
    IDENTIFICATION_VAR_DECL;
    ABSTRACT_SCHEMA_NAME;
    CMP_FIELD;
    SINGLE_CMR_FIELD;
    COLLECTION_CMR_FIELD;

    // operators
    UNARY_MINUS;
    UNARY_PLUS;
    NOT_BETWEEN;
    NOT_LIKE;
    NOT_IN;
    NOT_NULL;
    NOT_EMPTY;
    NOT_MEMBER;
}

{
    /** I18N support. */
    protected final static ResourceBundle msgs = 
        I18NHelper.loadBundle(EJBQLParser.class);
    
    /** ANTLR method called when an error was detected. */
    public void reportError(RecognitionException ex)
    {
        EJBQLLexer.handleANTLRException(ex);
    }

    /** ANTLR method called when an error was detected. */
    public void reportError(String s)
    {
        ErrorMsg.error(0, 0, s);
    }

    /** */
    public void reportError(int line, int column, String s)
    {
        ErrorMsg.error(line, column, s);
    }

    /** ANTLR method called when a warning was detected. */
    public void reportWarning(String s)
    {
        throw new EJBQLException(s);
    }

    /** 
     * This method wraps the root rule in order to handle 
     * ANTLRExceptions thrown during parsing.
     */
    public void query ()
    {
        try {
            root();
        }
        catch (ANTLRException ex) {
            EJBQLLexer.handleANTLRException(ex);
        }
    }
}

// ----------------------------------
// rules
// ----------------------------------

root!
    :   s:simpleSelectClause f:fromClause w:whereClause o:orderbyClause EOF!
        {
            // switch the order of subnodes: the fromClause should come first, 
            // because it declares the identification variables used in the 
            // selectClause and the whereClause
            #root = #(#[QUERY,"QUERY"], #f, #s, #w); //NOI18N
            if (#o != null) {
                #root.addChild(#o);
            }
        }
    ;

simpleSelectClause
    :   SELECT^ ( DISTINCT )? projection
    ;

projection
    :   p:pathExpr
        {
            if (#p.getType() != DOT) {
                ErrorMsg.error(#p.getLine(), #p.getColumn(), 
                    I18NHelper.getMessage(msgs, "EXC_SyntaxErrorAt", //NOI18N
                        #p.getText())); 
            }
        }
    |   OBJECT! LPAREN! IDENT RPAREN! // Note, skipping keyword OBJECT
    |   aggregateSelectExpr
    ;

aggregateSelectExpr
    :   ( AVG^ | MAX^ | MIN^ | SUM^ | COUNT^ ) LPAREN! (DISTINCT)? pathExpr RPAREN!
    ;

fromClause
    :   FROM^ identificationVarDecl ( COMMA! identificationVarDecl )*
    ;

identificationVarDecl
    :   collectionMemberDecl
    |   rangeVarDecl
    ;

collectionMemberDecl
    :   IN^ LPAREN! pathExpr RPAREN! ( AS! )? IDENT
    ;

rangeVarDecl!
    :   a:abstractSchemaName ( AS! )? i:IDENT
        {
            #rangeVarDecl = #(#[RANGE,"RANGE"], #a, #i); //NOI18N
        }
    ;

abstractSchemaName
    {   String name = null; }
    :   name = c:className
        {
            #c.setType(ABSTRACT_SCHEMA_NAME);
            #c.setText(name);
            // the top level node represents the entire abstract schema name, 
            // so it should not have child nodes anymore
            #c.setFirstChild(null);
        }
    ;

className returns [String fullyQualifiedName]
    {   
        StringBuffer name = new StringBuffer(); 
        fullyQualifiedName = null;
    }
    :   i1:IDENT 
        { name.append(#i1.getText()); }
        (   d:DOT^ 
            { name.append(#d.getText()); }
            i2:IDENT 
            { name.append(#i2.getText()); }
        )*
        {
            fullyQualifiedName = name.toString();
        }
    ;

whereClause
    :   WHERE^ conditionalExpr
    |   // empty rule
        {
            // Add where true in the case the where clause is omitted
            #whereClause = #(#[WHERE,"WHERE"], #[TRUE,"TRUE"]); //NOI18N
        }
    ;

pathExpr
    :   IDENT ( DOT^ IDENT )*
    ;

conditionalExpr
    :   conditionalTerm ( OR^ conditionalTerm )*
    ;

conditionalTerm
    :   conditionalFactor ( AND^ conditionalFactor )*
    ;

conditionalFactor
    :   ( NOT^ )? conditionalPrimary
    ;

conditionalPrimary
    :   ( betweenExpr )=> betweenExpr
    |   ( likeExpr )=> likeExpr
    |   ( inExpr )=> inExpr
    |   ( nullComparisonExpr )=> nullComparisonExpr
    |   ( emptyCollectionComparisonExpr )=> emptyCollectionComparisonExpr
    |   ( collectionMemberExpr )=> collectionMemberExpr 
    |   comparisonExpr
    ;

betweenExpr
    :   arithmeticExpr ( n:NOT! )? BETWEEN^ arithmeticExpr AND! arithmeticExpr
        {
            // map NOT BETWEEN to single operator NOT_BETWEEN
            if (#n != null)
                #BETWEEN.setType(NOT_BETWEEN);
        }
    ;

likeExpr
    :   pathExpr ( n:NOT! )? LIKE^ ( stringLiteral | inputParameter ) escape
        {
            // map NOT LIKE to single operator NOT_LIKE
            if(#n != null)
                #LIKE.setType(NOT_LIKE);
        }
    ;

escape
    :   ESCAPE^ ( stringLiteral | inputParameter )
    |   // empty rule
    ;

inExpr
    :   pathExpr ( n:NOT! )? IN^ LPAREN! inCollectionElement ( COMMA! inCollectionElement )* RPAREN!
        {
            // map NOT BETWEEN to single operator NOT_IN
            if (#n != null)
                #IN.setType(NOT_IN);
        }
    ;

nullComparisonExpr
    :   ( pathExpr | inputParameter ) IS! ( n:NOT! )? NULL^
        {
            // map NOT NULL to single operator NOT_NULL
            if (#n != null)
                #NULL.setType(NOT_NULL);
        }
    ;

emptyCollectionComparisonExpr
    :   pathExpr IS! ( n:NOT! )? EMPTY^
        {
            // map IS NOT EMPTY to single operator NOT_EMPTY
            if (#n != null)
                #EMPTY.setType(NOT_EMPTY);
        }
    ;

collectionMemberExpr
    :   ( pathExpr | inputParameter ) 
        ( n:NOT! )? MEMBER^ ( OF! )? pathExpr
        {
            // map NOT MEMBER to single operator NOT_MEMBER
            if (#n != null)
                #MEMBER.setType(NOT_MEMBER);
        }
    ;

comparisonExpr
    :   arithmeticExpr ( ( EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^ ) arithmeticExpr )*
    ;

arithmeticExpr
    :   arithmeticTerm ( ( PLUS^ | MINUS^ ) arithmeticTerm )*
    ;

arithmeticTerm 
    :   arithmeticFactor ( (STAR^ | DIV^ ) arithmeticFactor )*
    ;

arithmeticFactor
    :   MINUS^ {#MINUS.setType(UNARY_MINUS);} arithmeticFactor
    |   PLUS^  {#PLUS.setType(UNARY_PLUS);} arithmeticFactor
    |   arithmeticPrimary
    ;

arithmeticPrimary
    :   pathExpr 
    |   literal
    |   LPAREN! conditionalExpr RPAREN!
    |   inputParameter
    |   function
    ;

function
    :   CONCAT^ LPAREN! conditionalExpr COMMA! conditionalExpr RPAREN!
    |   SUBSTRING^ LPAREN! conditionalExpr COMMA! conditionalExpr COMMA! conditionalExpr RPAREN!
    |   LENGTH^ LPAREN! conditionalExpr RPAREN!
    |   LOCATE^ LPAREN! conditionalExpr COMMA! conditionalExpr ( COMMA! conditionalExpr )? RPAREN!
    |   ABS^ LPAREN! conditionalExpr RPAREN! 
    |   SQRT^ LPAREN! conditionalExpr RPAREN!
    |   MOD^ LPAREN! conditionalExpr COMMA! conditionalExpr RPAREN!
    ;

inputParameter
    :   POSITIONAL_PARAMETER
    ;

orderbyClause
    :   ORDER^ BY! orderbyItem ( COMMA! orderbyItem )*
    |   // empty rule
    ;

orderbyItem
    :   pathExpr direction
    ;

direction
    :   ASC
    |   DESC
    |   // empty rule
        {
            // ASC is added as default
            #direction = #[ASC, "ASC"]; //NOI18N
        }
    ;

inCollectionElement
    :   literal
    |   inputParameter
    ;

literal
    :   TRUE
    |   FALSE
    |   stringLiteral
    |   INT_LITERAL
    |   LONG_LITERAL
    |   FLOAT_LITERAL
    |   DOUBLE_LITERAL
    ;

stringLiteral
    :   s:STRING_LITERAL
        {
            // strip quotes from the token text
            String text = #s.getText();
            #s.setText(text.substring(1,text.length()-1));
        }
    ;
