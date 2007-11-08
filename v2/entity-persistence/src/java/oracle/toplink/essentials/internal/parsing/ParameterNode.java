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
package oracle.toplink.essentials.internal.parsing;

import oracle.toplink.essentials.expressions.*;

/**
 * INTERNAL
 * <p><b>Purpose</b>: This node represnts a Parameter (?1) in an EJBQL
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an AND in EJBQL
 * <li> Maintain a
 * <li>
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class ParameterNode extends Node {

    /** */
    private String name;

    /**
     * Return a new ParameterNode.
     */
    public ParameterNode() {
        super();
    }

    /**
     * INTERNAL
     * Create a new ParameterNode with the passed string.
     * @param newVariableName java.lang.String
     */
    public ParameterNode(String newParameterName) {
        setParameterName(newParameterName);
    }

    /**
     * INTERNAL 
     */
    public void validateParameter(ParseTreeContext context, Object contextType) {
        context.defineParameterType(name, contextType, getLine(), getColumn());
        setType(context.getParameterType(name));
    }

    /** */
    public Expression generateExpression(GenerationContext context) {
        //create builder, and add
        Class baseClass = context.getBaseQueryClass();
        ExpressionBuilder builder = new ExpressionBuilder(baseClass);
        Expression whereClause = builder.getParameter(getParameterName(), getType());
        return whereClause;
    }

    /**
     * INTERNAL
     * Return the parameterName
     *
     */
    public String getAsString() {
        return getParameterName();
    }

    /**
     * INTERNAL
     * Return the parameter name
     */
    public String getParameterName() {
        return name;
    }

    /** */
    public void setParameterName(String name) {
        this.name = name;
    }

    /**
     * INTERNAL
     * Yes this is a Parameter node
     */
    public boolean isParameterNode() {
        return true;
    }

}
