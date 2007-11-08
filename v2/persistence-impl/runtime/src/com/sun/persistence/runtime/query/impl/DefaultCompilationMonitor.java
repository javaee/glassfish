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

import com.sun.persistence.runtime.query.CompilationMonitor;
import com.sun.persistence.runtime.query.EJBQLAST;
import com.sun.persistence.utility.logging.Logger;

/**
 * 
 * @author Dave Bristor
 */
class DefaultCompilationMonitor extends CompilationMonitorImpl implements CompilationMonitor {
    /** The sole instance of this class. */
    private static final CompilationMonitor instance = new DefaultCompilationMonitor();
    
    /**
     * The logger
     */
    private static Logger logger = LogHelperQueryCompiler.getLogger();
    
    private DefaultCompilationMonitor() {
    }
    
    /** Access the sole instance of this class. */
    public static CompilationMonitor instance() {
        return instance;
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preSyntax(java.lang.String)
     */
    public void preSyntax(String qstr) {
        if (logger.isLoggable(Logger.FINER)) {
            logger.finer("LOG_EJBQLCStartPass", "syntax analysis"); //NOI18N
        }
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#postSyntax(java.lang.String, com.sun.persistence.runtime.query.EJBQLAST)
     */
    public void postSyntax(String qstr, EJBQLAST ast) {
        if (logger.isLoggable(Logger.FINEST)) {
            logger.finest("LOG_EJBQLCDumpTree", ast.getTreeRepr("(AST)")); //NOI18N
        }
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preSemantic(java.lang.String)
     */
    public void preSemantic(String qstr) {
        if (logger.isLoggable(Logger.FINER)) {
            logger.finer("LOG_EJBQLCStartPass", "semantic analysis"); //NOI18N
        }
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#postSemantic(java.lang.String, com.sun.persistence.runtime.query.EJBQLAST)
     */
    public void postSemantic(String qstr, EJBQLAST ast) {
        if (logger.isLoggable(Logger.FINEST)) {
            logger.finest(
                    "LOG_EJBQLCDumpTree", ast.getTreeRepr("(typed AST)")); //NOI18N
        }
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#preOptimize(java.lang.String)
     */
    public void preOptimize(String qstr) {
    }

    /**
     * @see com.sun.persistence.runtime.query.CompilationMonitor#postOptimize(java.lang.String, com.sun.persistence.runtime.query.EJBQLAST)
     */
    public void postOptimize(String qstr, EJBQLAST ast) {
    }

}
