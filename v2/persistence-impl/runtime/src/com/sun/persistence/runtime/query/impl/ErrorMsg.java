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
 * ErrorMsg.java
 *
 * Created on November 12, 2001
 */


package com.sun.persistence.runtime.query.impl;

import com.sun.persistence.utility.I18NHelper;
import com.sun.persistence.utility.logging.Logger;

import java.util.ResourceBundle;

/**
 * This is a helper class to report error messages from the EJBQL compiler.
 * @author Michael Bouschen
 * @author Shing Wai Chan
 */
public class ErrorMsg {
    /**
     * I18N support.
     */
    private final static ResourceBundle msgs = I18NHelper.loadBundle(
            ErrorMsg.class);

    /**
     * The logger
     */
    private static Logger logger = LogHelperQueryCompiler.getLogger();

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param line line number
     * @param col column number
     * @param text error message
     * @throws EJBQLException describes the user error.
     */
    public static void error(int line, int col, String text)
            throws EJBQLException {
        EJBQLException ex = null;
        if (line > 1) {
            // include line and column info
            Object args[] = {new Integer(line), new Integer(col), text};
            ex = new EJBQLException(
                    I18NHelper.getMessage(
                            msgs, "EXC_PositionInfoMsgLineColumn", args)); //NOI18N
        } else if (col > 0) {
            // include column info
            Object args[] = {new Integer(col), text};
            ex = new EJBQLException(
                    I18NHelper.getMessage(
                            msgs, "EXC_PositionInfoMsgColumn", args)); //NOI18N
        } else {
            ex = new EJBQLException(
                    I18NHelper.getMessage(msgs, "EXC_PositionInfoMsg", text)); //NOI18N
        }
        throw ex;
    }

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param text error message
     * @param cause the cause of the error
     * @throws EJBQLException describes the user error.
     */
    public static void error(String text, Throwable cause)
            throws EJBQLException {
        throw new EJBQLException(text, cause);
    }

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param text error message
     * @throws EJBQLException describes the user error.
     */
    public static void error(String text) throws EJBQLException {
        throw new EJBQLException(text);
    }

    /**
     * This method throws an UnsupportedOperationException indicating an
     * unsupported feature.
     * @param line line number
     * @param col column number
     * @param text message
     * @throws UnsupportedOperationException describes the unsupported feature.
     */
    public static void unsupported(int line, int col, String text)
            throws UnsupportedOperationException {
        UnsupportedOperationException ex;
        if (line > 1) {
            // include line and column info
            Object args[] = {new Integer(line), new Integer(col), text};
            ex = new UnsupportedOperationException(
                    I18NHelper.getMessage(
                            msgs, "EXC_PositionInfoMsgLineColumn", args)); //NOI18N
        } else if (col > 0) {
            // include column info
            Object args[] = {new Integer(col), text};
            ex = new UnsupportedOperationException(
                    I18NHelper.getMessage(
                            msgs, "EXC_PositionInfoMsgColumn", args)); //NOI18N
        } else {
            Object args[] = {text};
            ex = new UnsupportedOperationException(
                    I18NHelper.getMessage(msgs, "EXC_PositionInfoMsg", args)); //NOI18N
        }
        throw ex;
    }

    /**
     * This method is called in the case of an fatal internal error.
     * @param text error message
     * @throws EJBQLException describes the fatal internal error.
     */
    public static void fatal(String text) throws EJBQLException {
        throw new EJBQLException(
                I18NHelper.getMessage(msgs, "ERR_FatalInternalError", text)); //NOI18N
    }

    /**
     * This method is called in the case of an fatal internal error.
     * @param text error message
     * @param nested the cause of the error
     * @throws EJBQLException describes the fatal internal error.
     */
    public static void fatal(String text, Throwable nested)
            throws EJBQLException {
        throw new EJBQLException(
                I18NHelper.getMessage(msgs, "ERR_FatalInternalError", text), nested); //NOI18N
    }

    /**
     * This method is called when we want to log an exception in a given level.
     * Note that all other methods in this class do not log a stack trace.
     * @param level log level
     * @param text error message
     * @param nested the cause of the error
     * @throws EJBQLException describes the fatal internal error.
     */
    public static void log(int level, String text, Throwable nested)
            throws EJBQLException {
        logger.log(level, text, nested);
        throw new EJBQLException(text, nested);
    }
}
