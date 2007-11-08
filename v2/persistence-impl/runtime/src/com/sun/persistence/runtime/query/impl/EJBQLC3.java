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
 * EJBQLC3.java
 *
 * Created on May 9, 2005 
 */


package com.sun.persistence.runtime.query.impl;

import antlr.TokenBuffer;

import com.sun.persistence.runtime.query.CompilationMonitor;
import com.sun.persistence.runtime.query.ParameterSupport;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.persistence.utility.I18NHelper;
import com.sun.persistence.utility.generator.JavaClassWriterHelper;
import com.sun.persistence.utility.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

/**
 * This class is the driver of the EJBQL compiler. It controls the compiler
 * passes: syntax analysis, semantic analysis and pass monitoring.
 * <p>
 * An EJBQLC instance is able to compile multiple EJBQL queries as long as
 * they come from the same deployement descriptor. The class uses the model
 * instance passed to the constructor to access any meta data from the
 * deployment descriptor. Method {@link #compile} compiles a single EJBQL query
 * string together with the java.lang.reflect.Method instance of the
 * corresponding finder/selector method.
 *
 * XXX Update this comment.
 *
 * @author Michael Bouschen
 * @author Shing Wai Chan
 * @author Dave Bristor
 */
public class EJBQLC3 extends EJBQLC {

    /** The compiler instance. */
    private static EJBQLC3 ejbqlc = new EJBQLC3();

    /** Returns the singleton EJBQLC driver instance. */
    public static EJBQLC3 getInstance() {
        return ejbqlc;
    }

    /** Constructor. */
    EJBQLC3() {
    }

    /** 
     * Redefine method compile used in the CMP environment to throw
     * UnsupportedOperationException. 
     */
    public void compile(QueryInternal query, Method method,
            QueryContext queryContext, CompilationMonitor cm,
            int resultTypeMapping, boolean finderNotSelector, String ejbName)
            throws EJBQLException {
        throw new UnsupportedOperationException();
    }

    /** */
    public void compile(String ejbqlQuery, 
        QueryContext queryContext, Environment env, CompilationMonitor cm)
        throws EJBQLException, Throwable {
        
        boolean finer = logger.isLoggable(Logger.FINER);
        boolean finest = logger.isLoggable(Logger.FINEST);
        
        String pass = null;
        
        // syntax analysis
        cm.preSyntax(ejbqlQuery);
        EJBQL3Parser parser = createEJBQL3StringParser(ejbqlQuery);
        parser.query();
        EJBQLASTImpl ast = (EJBQLASTImpl) parser.getAST();
        // MBO: print AST
        //System.out.println(ast.getTreeRepr("(AST)"));
        if (finest) {
            logger.finest("LOG_EJBQLCDumpTree", ast.getTreeRepr("(AST)")); //NOI18N
        }
        cm.postSyntax(ejbqlQuery, ast);

        // semantic analysis
        cm.preSemantic(ejbqlQuery);
        Semantic3 semantic = new Semantic3();
        semantic.init(queryContext, env);
        semantic.setASTFactory(EJBQLASTFactory.getInstance());
        semantic.query(ast);
        ast = (EJBQLASTImpl) semantic.getAST();
        // MBO: print AST
        //System.out.println(ast.getTreeRepr("(typed AST)"));
        cm.postSemantic(ejbqlQuery, ast);
    }
    
    //========= Internal helper methods ==========

    /**
     * Creates an ANTLR EJBQL parser reading a string.
     */
    private EJBQL3Parser createEJBQL3StringParser(String text) {
        Reader in = new StringReader(text);
        EJBQL3Lexer lexer = new EJBQL3Lexer(in);
        TokenBuffer buffer = new TokenBuffer(lexer);
        EJBQL3Parser parser = new EJBQL3Parser(buffer);
        parser.setASTFactory(EJBQLASTFactory.getInstance());
        return parser;
    }    

}

