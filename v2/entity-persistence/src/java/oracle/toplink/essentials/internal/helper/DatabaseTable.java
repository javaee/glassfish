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
package oracle.toplink.essentials.internal.helper;

import java.io.*;
import java.util.Vector;
import java.util.Collection;
import oracle.toplink.essentials.internal.databaseaccess.*;

/**
 * INTERNAL:
 * <p> <b>Purpose</b>:
 * Define a fully qualified table name.<p>
 * <b>Responsibilities</b>:    <ul>
 *    <li> Allow specification of a qualifier to the table, i.e. creator or database.
 *    </ul>
 *@see DatabaseField
 */
public class DatabaseTable implements Cloneable, Serializable { 
    protected String name;
    protected String tableQualifier;
    protected String qualifiedName;
    protected Vector<String[]> uniqueConstraints; //Element is columnNames

    /** 
     * Initialize the newly allocated instance of this class.
     * By default their is no qualifier.
     */
    public DatabaseTable() {
        this("", "");
    }

    public DatabaseTable(String possiblyQualifiedName) {
        setPossiblyQualifiedName(possiblyQualifiedName);
        uniqueConstraints = new Vector<String[]>();
    }

    public DatabaseTable(String tableName, String qualifier) {
        this.name = tableName;
        this.tableQualifier = qualifier;
        uniqueConstraints = new Vector<String[]>();
    }

    /**
     * Add the unique constraint for the columns names. Used for DDL generation.
     */
    public void addUniqueConstraints(String[] columnNames) {
        uniqueConstraints.add(columnNames);
    }
    
    /** 
     * Return a shallow copy of the receiver.
     * @return Object An Object must be returned or the signature of this method
     * will conflict with the signature in Object.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
        }

        return null;
    }

    /** 
     * Two tables are equal if their names and tables are equal,
     * or their names are equal and one does not have a qualifier assigned.
     * This allows an unqualified table to equal the same fully qualified one.
     */
    public boolean equals(Object object) {
        if (object instanceof DatabaseTable) {
            return equals((DatabaseTable)object);
        }
        return false;
    }

    /**
     * Two tables are equal if their names and tables are equal,
     * or their names are equal and one does not have a qualifier assigned.
     * This allows an unqualified table to equal the same fully qualified one.
     */
    public boolean equals(DatabaseTable table) {
        if (this == table) {
            return true;
        }
        if (DatabasePlatform.shouldIgnoreCaseOnFieldComparisons()) {
            if (getName().equalsIgnoreCase(table.getName())) {
                if ((getTableQualifier().length() == 0) || (table.getTableQualifier().length() == 0) || (getTableQualifier().equalsIgnoreCase(table.getTableQualifier()))) {
                    return true;
                }
            }
        } else {
            if (getName().equals(table.getName())) {
                if ((getTableQualifier().length() == 0) || (table.getTableQualifier().length() == 0) || (getTableQualifier().equals(table.getTableQualifier()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /** 
     * Get method for table name.
     */
    public String getName() {
        return name;
    }

    public String getQualifiedName() {
        if (qualifiedName == null) {
            if (tableQualifier.equals("")) {
                qualifiedName = getName();
            } else {
                qualifiedName = getTableQualifier() + "." + getName();
            }
        }

        return qualifiedName;
    }

    public String getTableQualifier() {
        return tableQualifier;
    }
    
    /**
     * Return a vector of the unique constraints for this table.
     * Used for DDL generation.
     */
    public Vector<String[]> getUniqueConstraints() {
        return uniqueConstraints;
    }

    /** 
     * Return the hashcode of the name, because it is fairly unqiue.
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Determine whether the receiver has any identification information.
     * Return true if the name or qualifier of the receiver are nonempty.
     */
    public boolean hasName() {
        if ((getName().length() == 0) && (getTableQualifier().length() == 0)) {
            return false;
        }

        return true;
    }

    /**
     * INTERNAL:
     * Is this decorated / has an AS OF (some past time) clause.
     * <b>Example:</b>
     * SELECT ... FROM EMPLOYEE AS OF TIMESTAMP (exp) t0 ...
     */
    public boolean isDecorated() {
        return false;
    }

    protected void resetQualifiedName() {
        this.qualifiedName = null;
    }

    /**
     * This method will set the table name regardless if the name has
     * a qualifier. Used when aliasing table names.
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
        resetQualifiedName();
    }
    
    /**
     * Used to map the project xml. Anytime a string name is read from the
     * project xml, we must check if it is fully qualified and split the
     * actual name from the qualifier.
     * 
     * @param possiblyQualifiedName 
     */
    public void setPossiblyQualifiedName(String possiblyQualifiedName) {
        resetQualifiedName();
        
        int index = possiblyQualifiedName.lastIndexOf('.');

        if (index == -1) {
            this.name = possiblyQualifiedName;
            this.tableQualifier = "";
        } else {
            this.name = possiblyQualifiedName.substring(index + 1, possiblyQualifiedName.length());
            this.tableQualifier = possiblyQualifiedName.substring(0, index);
        }
    }

    public void setTableQualifier(String qualifier) {
        this.tableQualifier = qualifier;
        resetQualifiedName();
    }

    public String toString() {
        return "DatabaseTable(" + getQualifiedName() + ")";
    }
}
