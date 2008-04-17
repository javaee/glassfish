/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.lang.support;

import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.lang.jstl.Coercions;
import org.apache.taglibs.standard.lang.jstl.ELException;
import org.apache.taglibs.standard.lang.jstl.Logger;

/**
 * <p>A conduit to the JSTL EL.  Based on...</p>
 * 
 * <p>An implementation of the ExpressionEvaluatorManager called for by
 * the JSTL rev1 draft.  This class is responsible for delegating a
 * request for expression evaluating to the particular, "active"
 * ExpressionEvaluator for the given point in the PageContext object
 * passed in.</p>
 *
 * @author Shawn Bayern
 */
public class ExpressionEvaluatorManager { 

    //*********************************************************************
    // Constants

    public static final String EVALUATOR_CLASS =
        "org.apache.taglibs.standard.lang.jstl.Evaluator";
    // private static final String EVALUATOR_PARAMETER =
    //    "javax.servlet.jsp.jstl.temp.ExpressionEvaluatorClass";

    //*********************************************************************
    // Internal, static state

    private static HashMap nameMap = new HashMap();
    private static Logger logger = new Logger(System.out);

    //*********************************************************************
    // Public static methods

    /** 
     * Invokes the evaluate() method on the "active" ExpressionEvaluator
     * for the given pageContext.
     */ 
    public static Object evaluate(String attributeName, 
                                  String expression, 
                                  Class expectedType, 
                                  Tag tag, 
                                  PageContext pageContext) 
           throws JspException
    {

        // the evaluator we'll use
        ExpressionEvaluator target = getEvaluatorByName(EVALUATOR_CLASS);

        // delegate the call
        return (target.evaluate(
            attributeName, expression, expectedType, tag, pageContext));
    }

    /** 
     * Invokes the evaluate() method on the "active" ExpressionEvaluator
     * for the given pageContext.
     */ 
    public static Object evaluate(String attributeName, 
                                  String expression, 
                                  Class expectedType, 
                                  PageContext pageContext) 
           throws JspException
    {

        // the evaluator we'll use
        ExpressionEvaluator target = getEvaluatorByName(EVALUATOR_CLASS);

        // delegate the call
        return (target.evaluate(
            attributeName, expression, expectedType, null, pageContext));
    }

    /**
     * Gets an ExpressionEvaluator from the cache, or seeds the cache
     * if we haven't seen a particular ExpressionEvaluator before.
     */
    public static
	    ExpressionEvaluator getEvaluatorByName(String name)
            throws JspException {

        Object oEvaluator = nameMap.get(name);
        if (oEvaluator != null) {
            return ((ExpressionEvaluator) oEvaluator);
        }
        try {
            synchronized (nameMap) {
                oEvaluator = nameMap.get(name);
                if (oEvaluator != null) {
                    return ((ExpressionEvaluator) oEvaluator);
                }
                ExpressionEvaluator e = (ExpressionEvaluator)
                    Class.forName(name).newInstance();
                nameMap.put(name, e);
                return (e);
            }
        } catch (ClassCastException ex) {
            // just to display a better error message
            throw new JspException("invalid ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (ClassNotFoundException ex) {
            throw new JspException("couldn't find ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (IllegalAccessException ex) {
            throw new JspException("couldn't access ExpressionEvaluator: " +
                ex.toString(), ex);
        } catch (InstantiationException ex) {
            throw new JspException(
                "couldn't instantiate ExpressionEvaluator: " +
                ex.toString(), ex);
        }
    }

    /** Performs a type conversion according to the EL's rules. */
    public static Object coerce(Object value, Class classe)
            throws JspException {
	try {
	    // just delegate the call
	    return Coercions.coerce(value, classe, logger);
	} catch (ELException ex) {
	    throw new JspException(ex);
	}
    }

} 
