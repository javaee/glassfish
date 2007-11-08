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

import java.util.Vector;
import java.io.*;
import java.math.BigDecimal;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.sequencing.Sequence;
import oracle.toplink.essentials.sequencing.DefaultSequence;
import oracle.toplink.essentials.sequencing.TableSequence;

/**
 * <p>
 * <b>Purpose</b>: Allow a generic way of creating sequences on the different platforms,
 * and allow optional parameters to be specified.
 * <p>
 */
public class TableSequenceDefinition extends SequenceDefinition {

    /**
     * INTERNAL:
     * Should be a sequence defining table sequence in the db:
     * either TableSequence
     * DefaultSequence (only if case platform.getDefaultSequence() is a TableSequence).
     */
    public TableSequenceDefinition(Sequence sequence) {
        super(sequence);
    }

    /**
     * INTERNAL:
     * Return the SQL required to insert the sequence row into the sequence table.
     * Assume that the sequence table exists.
     */
    public Writer buildCreationWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("INSERT INTO ");
            writer.write(getSequenceTableName());
            writer.write("(" + getSequenceNameFieldName());
            writer.write(", " + getSequenceCounterFieldName());
            writer.write(") values (");
            writer.write("'" + getName() + "', "  + Integer.toString(sequence.getInitialValue() - 1) + ")");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERNAL:
     * Return the SQL to delete the row from the sequence table.
     */
    public Writer buildDeletionWriter(AbstractSession session, Writer writer) throws ValidationException {
        try {
            writer.write("DELETE FROM ");
            writer.write(getSequenceTableName());
            writer.write(" WHERE " + getSequenceNameFieldName());
            writer.write(" = '" + getName() + "'");
        } catch (IOException ioException) {
            throw ValidationException.fileError(ioException);
        }
        return writer;
    }

    /**
     * INTERAL:
     * Execute the SQL required to insert the sequence row into the sequence table.
     * Assume that the sequence table exists.
     */
    public boolean checkIfExist(AbstractSession session) throws DatabaseException {
        Vector results = session.executeSelectingCall(new oracle.toplink.essentials.queryframework.SQLCall("SELECT * FROM " + getSequenceTableName() + " WHERE " + getSequenceNameFieldName() + " = '" + getName() + "'"));
        return !results.isEmpty();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceTableName() {
        return getTableSequence().getTableName();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceCounterFieldName() {
        return getTableSequence().getCounterFieldName();
    }

    /**
     * PUBLIC:
     */
    public String getSequenceNameFieldName() {
        return getTableSequence().getNameFieldName();
    }

    /**
     * INTERNAL:
     * Return a TableDefinition specifying sequence table.
     */
    public TableDefinition buildTableDefinition() {
        TableDefinition definition = new TableDefinition();
        definition.setName(getSequenceTableName());
        definition.addPrimaryKeyField(getSequenceNameFieldName(), String.class, 50);
        definition.addField(getSequenceCounterFieldName(), BigDecimal.class);
        return definition;
    }
    
    protected TableSequence getTableSequence() {
        if(sequence instanceof TableSequence) {
            return (TableSequence)sequence;
        } else {
            return (TableSequence)((DefaultSequence)sequence).getDefaultSequence();
        }
    }
}
