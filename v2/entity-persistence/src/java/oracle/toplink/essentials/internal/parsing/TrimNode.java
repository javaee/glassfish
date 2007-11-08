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
 * <p><b>Purpose</b>: Represent a TRIM
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for TRIM
 * </ul>
 */
public class TrimNode extends StringFunctionNode {

    private Node trimChar;
    private boolean leading;
    private boolean trailing;
    private boolean both;

    /**
     * TrimNode constructor.
     */
    public TrimNode() {
        super();
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        if (left != null) {
            left.validate(context);
            left.validateParameter(context, typeHelper.getStringType());
        }
        if (trimChar != null) {
            trimChar.validate(context);
            trimChar.validateParameter(context, typeHelper.getCharType());
        }
        setType(typeHelper.getStringType());
    }

    /**
     * INTERNAL
     * Generate the TopLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        Expression whereClause = getLeft().generateExpression(context);
        if (leading) {
            // use leftTrim
            if (trimChar != null) {
                Expression trimCharExpr = trimChar.generateExpression(context);
                whereClause = whereClause.leftTrim(trimCharExpr);
            } else {
                whereClause = whereClause.leftTrim();
            }
        } else if (trailing) {
            if (trimChar != null) {
                Expression trimCharExpr = trimChar.generateExpression(context);
                whereClause = whereClause.rightTrim(trimCharExpr);
            } else {
                whereClause = whereClause.rightTrim();
            }
        } else {
            if (trimChar != null) {
                Expression trimCharExpr = trimChar.generateExpression(context);
                whereClause = whereClause.leftTrim(trimCharExpr).rightTrim(trimCharExpr);
            } else {
                whereClause = whereClause.leftTrim().rightTrim();
            }
        }
        return whereClause;
    }

    /** */
    public void setTrimChar(Node trimChar) {
        this.trimChar = trimChar;
    }

    /** */
    public boolean isLeading() {
        return leading;
    }

    /** */
    public void setLeading(boolean newLeading) {
        this.leading = newLeading;
    }
    
    /** */
    public boolean isTrailing() {
        return trailing;
    }
    
    /** */
    public void setTrailing(boolean newTrailing) {
        this.trailing = newTrailing;
    }
    
    /** */
    public boolean isBoth() {
        return both;
    }
    
    /** */
    public void setBoth(boolean newBoth) {
        this.both = newBoth;
    }
    
}
