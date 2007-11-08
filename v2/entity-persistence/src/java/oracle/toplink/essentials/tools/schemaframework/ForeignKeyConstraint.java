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
package oracle.toplink.essentials.tools.schemaframework;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Define a foreign key from one table to another.
 * This support composite foreign keys can constraint options.
 */
public class ForeignKeyConstraint implements Serializable {
    protected String name;
    protected Vector sourceFields; //source(foreign key) field names
    protected Vector targetFields; //target(primary key) field names
    protected String targetTable; //fully-qualified target table name
    protected boolean shouldCascadeOnDelete;

    public ForeignKeyConstraint() {
        this.name = "";
        this.sourceFields = new Vector();
        this.targetFields = new Vector();
        this.targetTable = "";
        this.shouldCascadeOnDelete = false;
    }

    public ForeignKeyConstraint(String name, String sourceField, String targetField, String targetTable) {
        this();
        this.name = name;
        sourceFields.addElement(sourceField);
        targetFields.addElement(targetField);
        this.targetTable = targetTable;
    }

    public void addSourceField(String sourceField) {
        getSourceFields().addElement(sourceField);
    }

    public void addTargetField(String targetField) {
        getTargetFields().addElement(targetField);
    }

    /**
     * INTERNAL:
     * Append the database field definition string to the table creation statement.
     */
    public void appendDBString(Writer writer, AbstractSession session) {
        try {
            writer.write("FOREIGN KEY (");
            for (Enumeration sourceEnum = getSourceFields().elements();
                     sourceEnum.hasMoreElements();) {
                writer.write((String)sourceEnum.nextElement());
                if (sourceEnum.hasMoreElements()) {
                    writer.write(", ");
                }
            }
            writer.write(") REFERENCES ");
            writer.write(getTargetTable());
            writer.write(" (");
            for (Enumeration targetEnum = getTargetFields().elements();
                     targetEnum.hasMoreElements();) {
                writer.write((String)targetEnum.nextElement());
                if (targetEnum.hasMoreElements()) {
                    writer.write(", ");
                }
            }
            writer.write(")");
            if (shouldCascadeOnDelete()) {
                writer.write(" ON DELETE CASCADE");
            }
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
    }

    /**
     * PUBLIC:
     * Enables delete cascading on the database.
     * This must be used carefully, i.e. only private relationships.
     */
    public void cascadeOnDelete() {
        setShouldCascadeOnDelete(true);
    }

    /**
     * PUBLIC:
     * Disables delete cascading on the database, this is the default.
     */
    public void dontCascadeOnDelete() {
        setShouldCascadeOnDelete(false);
    }

    public String getName() {
        return name;
    }

    public Vector getSourceFields() {
        return sourceFields;
    }

    public Vector getTargetFields() {
        return targetFields;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Enables delete cascading on the database.
     * This must be used carefully, i.e. only private relationships.
     */
    public void setShouldCascadeOnDelete(boolean shouldCascadeOnDelete) {
        this.shouldCascadeOnDelete = shouldCascadeOnDelete;
    }

    public void setSourceFields(Vector sourceFields) {
        this.sourceFields = sourceFields;
    }

    public void setTargetFields(Vector targetFields) {
        this.targetFields = targetFields;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public boolean shouldCascadeOnDelete() {
        return shouldCascadeOnDelete;
    }
}
