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
 * EJBQLC.java
 *
 * Created on November 12, 2001
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
public class EJBQLC {

    /**
     * The intermediate form of the EJBQL query string.
     */
    protected EJBQLASTImpl ast;

    /**
     * The logger
     */
    protected static Logger logger = LogHelperQueryCompiler.getLogger();

    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs = I18NHelper.loadBundle(
            EJBQLC.class);

    /**
     * Signature with CVS keyword substitution for identifying the generated
     * code
     */
    // XXX Update this.
    public static final String SIGNATURE = "$RCSfile: EJBQLC.java,v $ $Revision: 1.1.1.1 $"; //NOI18N

    /** The compiler instance. */
    private static EJBQLC ejbqlc = new EJBQLC();

    /** */
    public static EJBQLC getInstance() {
        return ejbqlc;
    }

    /** */
    EJBQLC() {
    }

    public void compile(QueryInternal q,
            QueryContext queryContext, CompilationMonitor cm)
            throws EJBQLException {

        QueryImpl query = (QueryImpl) q;
        String ejbqlQuery = query.getQuery();
        cm = (cm == null) ? DefaultCompilationMonitor.instance() : cm;

        ParameterSupport paramSupport = new PersistenceParameterSupport();
        Environment env = new PersistenceEnvironment(queryContext, paramSupport);
        query.setParameterSupport(paramSupport);

        try {
            compile(ejbqlQuery, queryContext, env, cm);
        } catch (EJBQLException ex) {
            Object[] msgArgs = {ejbqlQuery, ex.getMessage()};
            ErrorMsg.error(
                    I18NHelper.getMessage(
                    msgs, "EXC_InvalidEJBQL_3_Query", msgArgs)); //NOI18N
        } catch (Throwable t) {
            Object[] msgArgs = {ejbqlQuery, t.toString()};
            ErrorMsg.log(
                    Logger.SEVERE, I18NHelper.getMessage(
                    msgs, "EXC_EJBQLQueryInternal_3_Error", msgArgs), t); //NOI18N
        }
    }
    
    /**
     * Compiles the specified query string for the specified finder/selector
     * method.
     * @param q the EJBQL query text
     * @param method the Method instance of the finder or selector
     * @param resultTypeMapping result-type-mapping element from the DD
     * @param finderNotSelector <code>true</code> indicates a finder,
     * <code>false</code> a selector
     * @param ejbName the ejb name of the entity bean
     */
    public void compile(QueryInternal q, Method method,
            QueryContext queryContext, CompilationMonitor cm,
            int resultTypeMapping, boolean finderNotSelector, String ejbName)
            throws EJBQLException {
        
        QueryImpl query = (QueryImpl) q;
        String ejbqlQuery = query.getQuery();

        if (method == null) {
            ErrorMsg.fatal(
                    I18NHelper.getMessage(msgs, "ERR_MissingMethodInstance")); //NOI18N
        }
        if ((ejbqlQuery == null) || ejbqlQuery.trim().length() == 0) {
            ErrorMsg.error(
                    I18NHelper.getMessage(
                    msgs, "EXC_MissingEjbqlQueryText", ejbName, //NOI18N
                    getMethodSignature(method)));
        }
        if (logger.isLoggable(Logger.FINER)) {
            logger.finer(
                    "LOG_EJBQLCCompile", ejbName, //NOI18N
                    getMethodSignature(method), ejbqlQuery);
        }

        cm = (cm == null) ? DefaultCompilationMonitor.instance() : cm;
        CMPParameterSupport paramSupport = new CMPParameterSupport(method);
        Environment env = new CMPEnvironment(
                queryContext, paramSupport, method, resultTypeMapping,
                finderNotSelector, ejbName);
        query.setParameterSupport(paramSupport);

        try {
            compile(ejbqlQuery, queryContext, env, cm);
        } catch (EJBQLException ex) {
            Object[] msgArgs = {ejbName, getMethodSignature(method),
                    ejbqlQuery, ex.getMessage()};
                    ErrorMsg.error(
                            I18NHelper.getMessage(
                            msgs, "EXC_InvalidEJBQLQuery", msgArgs)); //NOI18N
        } catch (Throwable t) {
            Object[] msgArgs = {ejbName, getMethodSignature(method),
                    ejbqlQuery, t.toString()};
                    ErrorMsg.log(
                            Logger.SEVERE, I18NHelper.getMessage(
                            msgs, "EXC_EJBQLQueryInternalError", msgArgs), t); //NOI18N
        }
    }

    public void compile(String ejbqlQuery,
            QueryContext queryContext, Environment env, CompilationMonitor cm)
            throws EJBQLException, Throwable {

        boolean finer = logger.isLoggable(Logger.FINER);
        boolean finest = logger.isLoggable(Logger.FINEST);

        String pass = null;

        // syntax analysis
        cm.preSyntax(ejbqlQuery);
        EJBQLParser parser = createStringParser(ejbqlQuery);
        parser.query();
        EJBQLASTImpl ast = (EJBQLASTImpl) parser.getAST();
        if (finest) {
            logger.finest("LOG_EJBQLCDumpTree", ast.getTreeRepr("(AST)")); //NOI18N
        }
        cm.postSyntax(ejbqlQuery, ast);

        // semantic analysis
        cm.preSemantic(ejbqlQuery);
        Semantic semantic = new Semantic();
        semantic.init(queryContext, env);
        semantic.setASTFactory(EJBQLASTFactory.getInstance());
        semantic.query(ast);
        ast = (EJBQLASTImpl) semantic.getAST();
        cm.postSemantic(ejbqlQuery, ast);
    }

    //========= Internal helper methods ==========

    /**
     * Creates an ANTLR EJBQL parser reading a string.
     */
    private EJBQLParser createStringParser(String text) {
        Reader in = new StringReader(text);
        EJBQLLexer lexer = new EJBQLLexer(in);
        TokenBuffer buffer = new TokenBuffer(lexer);
        EJBQLParser parser = new EJBQLParser(buffer);
        parser.setASTFactory(EJBQLASTFactory.getInstance());
        return parser;
    }

    /**
     * Returns the signature of a method w/o exceptions and modifiers as a
     * string.
     */
    private String getMethodSignature(Method m) {
        if (m == null) {
            return ""; //NOI18N
        }

        return m.getReturnType().getName() + ' ' + m.getName()
        + JavaClassWriterHelper.parenleft_
                + JavaClassWriterHelper.getParameterTypesList(m)
                + JavaClassWriterHelper.parenright_;
    }
}

