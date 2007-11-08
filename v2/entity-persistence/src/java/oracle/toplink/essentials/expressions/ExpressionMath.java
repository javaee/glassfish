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
package oracle.toplink.essentials.expressions;

import oracle.toplink.essentials.internal.helper.ClassConstants;

/**
 * <p>
 * <b>Purpose</b>: This class mirrors the java.lang.Math class to allow mathimetical function support within expressions.</p>
 * <p>Example:
 * <pre><blockquote>
 *  ExpressionBuilder builder = new ExpressionBuilder();
 *  Expression poorAndRich = ExpressionMath.abs(builder.get("netWorth")).greaterThan(1000000);
 *  session.readAllObjects(Company.class, poorAndRich);
 * </blockquote></pre></p>
 */
public class ExpressionMath {

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     * <p>Example:
     * <pre><blockquote>
     *  Example: ExpressionMath.abs(builder.get("netWorth")).greaterThan(1000000);
     * </blockquote></pre>
     */
    public static Expression abs(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Abs);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression acos(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Acos);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression add(Expression left, int right) {
        return add(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression add(Expression right, Object left) {
        ExpressionOperator anOperator = right.getOperator(ExpressionOperator.Add);
        return anOperator.expressionFor(right, left);
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression asin(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Asin);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression atan(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Atan);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression atan2(Expression expression, int value) {
        return atan2(expression, new Integer(value));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression atan2(Expression expression, Object value) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Atan2);
        return anOperator.expressionFor(expression, value);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression atan2(Expression expression1, Expression expression2) {
        ExpressionOperator anOperator = expression1.getOperator(ExpressionOperator.Atan2);
        return anOperator.expressionFor(expression1, expression2);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression ceil(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Ceil);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression chr(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Chr);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression cos(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Cos);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.

     */
    public static Expression cosh(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Cosh);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression cot(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Cot);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that aplies the function to the given expression.
     */
    public static Expression divide(Expression left, int right) {
        return divide(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression divide(Expression left, Object right) {
        ExpressionOperator anOperator = left.getOperator(ExpressionOperator.Divide);
        return anOperator.expressionFor(left, right);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression exp(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Exp);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression floor(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Floor);
        return anOperator.expressionFor(expression);
    }

    /**
     * INTERNAL:
     * Return the operator.
     */
    public static ExpressionOperator getOperator(int selector) {
        ExpressionOperator result = ExpressionOperator.getOperator(new Integer(selector));
        if (result != null) {
            return result;
        }

        // Make a temporary operator which we expect the platform
        // to supply later.
        result = new ExpressionOperator();
        result.setSelector(selector);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression ln(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Ln);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression log(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Log);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression max(Expression left, int right) {
        return max(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression max(Expression left, Object right) {
        ExpressionOperator anOperator = left.getOperator(ExpressionOperator.Greatest);
        return anOperator.expressionFor(left, right);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression min(Expression left, int right) {
        return min(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression min(Expression left, Object right) {
        ExpressionOperator anOperator = left.getOperator(ExpressionOperator.Least);
        return anOperator.expressionFor(left, right);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression mod(Expression expression, int base) {
        return mod(expression, new Integer(base));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression mod(Expression expression, Object base) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Mod);
        return anOperator.expressionFor(expression, base);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression multiply(Expression left, int right) {
        return multiply(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression multiply(Expression left, Object right) {
        ExpressionOperator anOperator = left.getOperator(ExpressionOperator.Multiply);
        return anOperator.expressionFor(left, right);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression power(Expression expression, int raised) {
        return power(expression, new Integer(raised));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression power(Expression expression, Object raised) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Power);
        return anOperator.expressionFor(expression, raised);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression round(Expression expression, int decimalPlaces) {
        return round(expression, new Integer(decimalPlaces));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression round(Expression expression, Object decimalPlaces) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Round);
        return anOperator.expressionFor(expression, decimalPlaces);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression sign(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Sign);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression sin(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Sin);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression sinh(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Sinh);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression subtract(Expression left, int right) {
        return subtract(left, new Integer(right));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression subtract(Expression left, Object right) {
        ExpressionOperator anOperator = left.getOperator(ExpressionOperator.Subtract);
        return anOperator.expressionFor(left, right);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression tan(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Tan);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression tanh(Expression expression) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Tanh);
        return anOperator.expressionFor(expression);
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression trunc(Expression expression, int decimalPlaces) {
        return trunc(expression, new Integer(decimalPlaces));
    }

    /**
     * PUBLIC:
     * Return a new expression that applies the function to the given expression.
     */
    public static Expression trunc(Expression expression, Object decimalPlaces) {
        ExpressionOperator anOperator = expression.getOperator(ExpressionOperator.Trunc);
        return anOperator.expressionFor(expression, decimalPlaces);
    }
}
