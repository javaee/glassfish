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
package oracle.toplink.essentials.internal.expressions;

import java.util.*;
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * This class represents a "query key" that isn't really there
 * in the descriptors. For example, I could use this to create
 * an 'employee' query key from an 'address' node even if addresses
 * don't know their employee. It's called manual, because I will
 * have to provide the relevant join criteria myself (normally based
 * on a reverse relationship. Motivated by batch reading.
 */
public class ManualQueryKeyExpression extends QueryKeyExpression {
    public ManualQueryKeyExpression() {
        super();
    }

    public ManualQueryKeyExpression(String impliedRelationshipName, Expression base) {
        super(impliedRelationshipName, base);
    }

    public ManualQueryKeyExpression(String impliedRelationshipName, Expression base, ClassDescriptor descriptor) {
        super(impliedRelationshipName, base);
        this.descriptor = descriptor;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Manual Query Key";
    }

    /**
     * INTERNAL:
     * If we ever get in the circumstance of a manual query key
     * to an aggregate, then we can assume that the owner of that
     * aggregate isn't participating (and even if it is, we can't
     * know which node it is, so *DO* use the aggregate's parents tables
     */
    public Vector getOwnedTables() {
        if (getDescriptor() == null) {
            return null;
        } else {
            return getDescriptor().getTables();
        }
    }

    public QueryKey getQueryKeyOrNull() {
        return null;
    }

    /**
     * INTERNAL:
     * We can never be an attribute, we're always a join
     */
    public boolean isAttribute() {
        return false;
    }

    public Expression mappingCriteria() {
        return null;
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        ObjectExpression newLocalBase = (ObjectExpression)getBaseExpression().rebuildOn(newBase);
        return newLocalBase.getManualQueryKey(getName(), getDescriptor());
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        ObjectExpression twistedBase = (ObjectExpression)getBaseExpression().twistedForBaseAndContext(newBase, context);
        return twistedBase.getManualQueryKey(getName(), getDescriptor());

    }

    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     */
    public void validateNode() {
        // Override super.validateNode() because those criteria don't apply to us
    }
}
