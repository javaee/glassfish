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
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Define a database object for the purpose of creation and deletion.
 * A database object is an entity such as a table, view, proc, sequence...
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Be able to create and drop the object from the database.
 * </ul>
 */
public abstract class DatabaseObjectDefinition implements Cloneable, Serializable {
    public String name;
    public String qualifier;

    public DatabaseObjectDefinition() {
        this.name = "";
        this.qualifier = "";
    }

    /**
     * INTERNAL:
     * Returns the writer used for creation of this object.
     */
    public abstract Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException;

    /**
     * INTERNAL:
     * Returns the writer used for creation of this object.
     */
    public abstract Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException;

    /**
     * PUBLIC:
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException impossible) {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Either drop from the database directly or write the statement to a file.
     * Database objects are root level entities such as tables, views, procs, sequences...
     */
    public void createObject(AbstractSession session, Writer schemaWriter) throws TopLinkException {
        if (schemaWriter == null) {
            this.createOnDatabase(session);
        } else {
            this.buildCreationWriter(session, schemaWriter);
        }
    }

    /**
     * INTERNAL:
     * Execute the DDL to create the varray.
     */
    public void createOnDatabase(AbstractSession session) throws TopLinkException {
        session.executeNonSelectingCall(new SQLCall(buildCreationWriter(session, new StringWriter()).toString()));
    }

    /**
     * INTERNAL:
     * Execute the DDL to drop the varray.
     */
    public void dropFromDatabase(AbstractSession session) throws TopLinkException {
        session.executeNonSelectingCall(new SQLCall(buildDeletionWriter(session, new StringWriter()).toString()));
    }

    /**
     * INTERNAL:
     * Execute the DDL to drop the varray.  Either directly from the database
     * of write out the statement to a file.
     */
    public void dropObject(AbstractSession session, Writer schemaWriter) throws TopLinkException {
        if (schemaWriter == null) {
            this.dropFromDatabase(session);
        } else {
            buildDeletionWriter(session, schemaWriter);
        }
    }

    /**
     * INTERNAL:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public String getFullName() {
        if (getQualifier().equals("")) {
            return getName();
        } else {
            return getQualifier() + "." + getName();
        }
    }

    /**
     * PUBLIC:
     * Return the name of the object.
     * i.e. the table name or the sequence name.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * PUBLIC:
     * Set the name of the object.
     * i.e. the table name or the sequence name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Most major databases support a creator name scope.
     * This means whenever the database object is referecned, it must be qualified.
     */
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getFullName() + ")";
    }
}
