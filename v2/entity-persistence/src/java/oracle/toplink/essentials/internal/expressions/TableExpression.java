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

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.expressions.*;

public class TableExpression extends DataExpression {
    protected DatabaseTable table;

    /**
     * TableExpression constructor comment.
     */
    public TableExpression() {
        super();
    }

    /**
     * TableExpression constructor comment.
     */
    public TableExpression(DatabaseTable aTable) {
        super();
        table = aTable;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Table";
    }

    /**
     * INTERNAL:
     * Fully-qualify the databaseField if the table is known.
     * CR 3791
     */
    public Expression getField(String fieldName) {
        // we need to check for full table qualification
        DatabaseField field = new DatabaseField(fieldName);
        if (!field.hasTableName()) {
            field.setTable(getTable());
        }
        return getField(field);
    }

    /**
     * INTERNAL:
     */
    public Vector getOwnedTables() {
        Vector result = new Vector(1);
        result.addElement(getTable());
        return result;
    }

    public DatabaseTable getTable() {
        return table;
    }

    public boolean isTableExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Normalize the expression into a printable structure.
     * Any joins must be added to form a new root.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
		//Bug4736461  Only setTableQualifier if getDatasourceLogin().getTableQualifier() is an empty string to make the window much smaller when
		//DatabaseTable.qualifiedName is reset
        if (getTable().getTableQualifier().length() == 0 && (normalizer.getSession().getDatasourceLogin().getTableQualifier().length() != 0)) {
            getTable().setTableQualifier(normalizer.getSession().getDatasourceLogin().getTableQualifier());
        }
        return super.normalize(normalizer);
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        Expression newLocalBase = getBaseExpression().rebuildOn(newBase);
        return newLocalBase.getTable(getTable());

    }

    /**
     * INTERNAL:
     * Added for temporal querying.
     */
    public void setTable(DatabaseTable table) {
        this.table = table;
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        Expression twistedBase = getBaseExpression().twistedForBaseAndContext(newBase, context);
        return twistedBase.getTable(getTable());

    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(getTable().toString());
        writer.write(tableAliasesDescription());
    }
}
