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
import oracle.toplink.essentials.internal.helper.*;

/**
 * INTERNAL:
 * Represents the aliased tables an ObjectExpression will be translated to,
 * along with any of its derived TableExpressions.
 * For bug 2778339 / CR 2456, this Lookup also represents identity.  Two
 * expressions with the same Lookup will be translated to the same table(s).
 */
public class TableAliasLookup implements Serializable {// CR#3718, implements Serializable
    protected DatabaseTable[] keys;
    protected DatabaseTable[] values;
    protected int lastUsed;

    /* Have these aliases already been added to a FROM clause? */
    protected boolean haveBeenAddedToStatement;

    /**
     * TableAliasLookup constructor comment.
     */
    public TableAliasLookup() {
        super();
        keys = new DatabaseTable[5];
        values = new DatabaseTable[5];
        lastUsed = 0;
    }

    /**
     * TableAliasLookup constructor comment.
     */
    public TableAliasLookup(int initialSize) {
        super();
        keys = new DatabaseTable[initialSize];
        values = new DatabaseTable[initialSize];
        lastUsed = 0;
    }

    // Add all of our values to the hashtable
    public void addToHashtable(java.util.Hashtable aHashTable) {
        for (int i = 0; i < lastUsed; i++) {
            aHashTable.put(keys[i], values[i]);
        }
    }

    public DatabaseTable get(DatabaseTable key) {
        int index = lookupIndexOf(key);
        if (index == -1) {
            return null;
        }
        return values[index];
    }

    private void grow() {
        DatabaseTable[] newKeys = new DatabaseTable[(lastUsed * 2)];
        DatabaseTable[] newValues = new DatabaseTable[(lastUsed * 2)];

        for (int i = 0; i < lastUsed; i++) {
            newKeys[i] = keys[i];
            newValues[i] = values[i];
        }
        keys = newKeys;
        values = newValues;
    }

    /**
     * INTERNAL:
     * Answers if the aliases have already been added to a statement.
     * This insures that a subselect will not re-add aliases already
     * in a parent FROM clause.
     * For CR#4223
     */
    public boolean haveBeenAddedToStatement() {
        return haveBeenAddedToStatement;
    }

    /**
     * isEmpty method comment.
     */
    public boolean isEmpty() {
        return keys[0] == null;
    }

    public DatabaseTable keyAtValue(DatabaseTable value) {
        int index = lookupValueIndexOf(value);
        if (index == -1) {
            return null;
        }
        return keys[index];
    }

    public DatabaseTable[] keys() {
        return keys;
    }

    private int lookupIndexOf(DatabaseTable table) {
        for (int i = 0; i < lastUsed; i++) {
            if (keys[i].equals(table)) {
                return i;
            }
        }
        return -1;
    }

    private int lookupValueIndexOf(DatabaseTable table) {
        for (int i = 0; i < lastUsed; i++) {
            if (values[i].equals(table)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * put method comment.
     */
    public DatabaseTable put(DatabaseTable key, DatabaseTable value) {
        int index = lookupIndexOf(key);
        if (index == -1) {
            keys[lastUsed] = key;
            values[lastUsed++] = value;
            if (lastUsed >= keys.length) {
                grow();
            }
        } else {
            values[index] = value;
        }
        return value;
    }

    /**
     * INTERNAL:
     * Called when aliases are added to a statement.
     * This insures that a subselect will not re-add aliases already
     * in a parent FROM clause.
     * For CR#4223
     */
    public void setHaveBeenAddedToStatement(boolean value) {
        haveBeenAddedToStatement = value;
    }

    /**
     * size method comment.
     */
    public int size() {
        return lastUsed;
    }

    public String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        buf.append("{");

        for (int i = 0; i <= max; i++) {
            String s1 = keys[i].toString();
            String s2 = values[i].toString();
            buf.append(s1 + "=" + s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public DatabaseTable[] values() {
        return values;
    }
}
