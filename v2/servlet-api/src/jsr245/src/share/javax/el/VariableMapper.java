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
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */

package javax.el;

/**
 * The interface to a map between EL variables and the EL expressions
 * they are associated with.
 *
 * @since JSP 2.1
 */

public abstract class VariableMapper {
    
    /**
     * @param variable The variable name
     * @return the ValueExpression assigned to the variable,
     *         null if there is no previous assignment to this variable.
     */
    public abstract ValueExpression resolveVariable(
            String variable);
    
    /**
     * Assign a ValueExpression to an EL variable, replacing
     * any previously assignment to the same variable.
     * The assignment for the variable is removed if
     * the expression is <code>null</code>.
     *
     * @param variable The variable name
     * @param expression The ValueExpression to be assigned
     *        to the variable.
     * @return The previous ValueExpression assigned to this variable,
     *         null if there is no previouse assignment to this variable.
     */
    public abstract ValueExpression setVariable(
            String variable,
            ValueExpression expression);
}
