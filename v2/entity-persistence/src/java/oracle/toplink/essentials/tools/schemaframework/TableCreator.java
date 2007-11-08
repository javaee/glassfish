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

import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.logging.SessionLog;

import java.util.*;

/**
 * <b>Purpose</b>: This class is reponsible for creating the tables defined in the project.
 * A specific subclass of this class is created for each project.  The specific table information
 * is defined in the subclass.
 *
 * @since TopLink 2.0
 * @author Peter Krogh
 */
public class TableCreator {
    protected Vector tableDefinitions;
    protected String name;
    protected boolean ignoreDatabaseException; //if true, DDL generation will continue even if exceptions occur

    public TableCreator() {
        this(new Vector());
    }

    public TableCreator(Vector tableDefinitions) {
        super();
        this.tableDefinitions = tableDefinitions;
    }

    /**
     * Add the table.
     */
    public void addTableDefinition(TableDefinition tableDefinition) {
        tableDefinitions.addElement(tableDefinition);
    }
    
    /**
     * Add a set of tables.
     */
    public void addTableDefinitions(Collection tableDefs) {
        tableDefinitions.addAll(tableDefs);
    }


    /**
     * Create constraints.
     */
    public void createConstraints(oracle.toplink.essentials.sessions.DatabaseSession session) {
        //CR2612669
        createConstraints(session, new SchemaManager(session));
    }

    /**
     * Create constraints.
     */
    public void createConstraints(oracle.toplink.essentials.sessions.DatabaseSession session, SchemaManager schemaManager) {
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            schemaManager.buildFieldTypes((TableDefinition)enumtr.nextElement());
        }

        // Unique constraints should be generated before foreign key constraints,
        // because foreign key constraints can reference unique constraints
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createUniqueConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }
        
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createForeignConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }
    }

    /**
     * This creates the tables on the database.
     * If the table already exists this will fail.
     */
    public void createTables(oracle.toplink.essentials.sessions.DatabaseSession session) {
        //CR2612669
        createTables(session, new SchemaManager(session));
    }

    /**
     * This creates the tables on the database.
     * If the table already exists this will fail.
     */
    public void createTables(oracle.toplink.essentials.sessions.DatabaseSession session, SchemaManager schemaManager) {
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            schemaManager.buildFieldTypes((TableDefinition)enumtr.nextElement());
        }

        String sequenceTableName = getSequenceTableName(session);
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            // Must not create sequence table as done in createSequences.
            TableDefinition table = (TableDefinition)enumtr.nextElement();
            if (!table.getName().equals(sequenceTableName)) {
                try {
                    schemaManager.createObject(table);
                    session.getSessionLog().log(SessionLog.FINEST, "default_tables_created", table.getFullName());
                } catch (DatabaseException ex) {
                    session.getSessionLog().log(SessionLog.FINEST, "default_tables_already_existed", table.getFullName());
                    if (!shouldIgnoreDatabaseException()) {
                        throw ex;
                    }
                }
            }
        }

        // Unique constraints should be generated before foreign key constraints,
        // because foreign key constraints can reference unique constraints
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createUniqueConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }
        
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createForeignConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }

        schemaManager.createSequences();
    }

    /**
     * Drop the table constraints from the database.
     */
    public void dropConstraints(oracle.toplink.essentials.sessions.DatabaseSession session) {
        //CR2612669
        dropConstraints(session, new SchemaManager(session));
    }

    /**
     * Drop the table constraints from the database.
     */
    public void dropConstraints(oracle.toplink.essentials.sessions.DatabaseSession session, SchemaManager schemaManager) {
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            schemaManager.buildFieldTypes((TableDefinition)enumtr.nextElement());
        }

        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.dropConstraints((TableDefinition)enumtr.nextElement());
            } catch (oracle.toplink.essentials.exceptions.DatabaseException dbE) {
                //ignore
            }
        }
    }

    /**
     * Drop the tables from the database.
     */
    public void dropTables(oracle.toplink.essentials.sessions.DatabaseSession session) {
        //CR2612669
        dropTables(session, new SchemaManager(session));
    }

    /**
     * Drop the tables from the database.
     */
    public void dropTables(oracle.toplink.essentials.sessions.DatabaseSession session, SchemaManager schemaManager) {
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            schemaManager.buildFieldTypes((TableDefinition)enumtr.nextElement());
        }

        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.dropConstraints((TableDefinition)enumtr.nextElement());
            } catch (oracle.toplink.essentials.exceptions.DatabaseException dbE) {
                //ignore
            }
        }

        String sequenceTableName = getSequenceTableName(session);
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            // Must not create sequence table as done in createSequences.
            TableDefinition table = (TableDefinition)enumtr.nextElement();
            if (!table.getName().equals(sequenceTableName)) {
                try {
                    schemaManager.dropObject(table);
                } catch (DatabaseException ex) {
                    if (!shouldIgnoreDatabaseException()) {
                        throw ex;
                    }
                }
            }
        }
    }

    /**
     * Return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the tables.
     */
    public Vector getTableDefinitions() {
        return tableDefinitions;
    }

    /**
     * Recreate the tables on the database.
     * This will drop the tables if they exist and recreate them.
     */
    public void replaceTables(oracle.toplink.essentials.sessions.DatabaseSession session) {
        replaceTables(session, new SchemaManager(session));
    }

    /**
     * Recreate the tables on the database.
     * This will drop the tables if they exist and recreate them.
     */
    public void replaceTables(oracle.toplink.essentials.sessions.DatabaseSession session, SchemaManager schemaManager) {
        replaceTablesAndConstraints(schemaManager, session);

        schemaManager.createSequences();

    }
    
    /**
     * Recreate the tables on the database.
     * This will drop the tables if they exist and recreate them.
     */
    public void replaceTables(oracle.toplink.essentials.sessions.DatabaseSession session, 
            SchemaManager schemaManager, boolean keepSequenceTable) {
        replaceTablesAndConstraints(schemaManager, session);

        schemaManager.createOrReplaceSequences(keepSequenceTable, false);
    }    
    

    private void replaceTablesAndConstraints(final SchemaManager schemaManager, 
            final oracle.toplink.essentials.sessions.DatabaseSession session) {
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            schemaManager.buildFieldTypes((TableDefinition)enumtr.nextElement());
        }
        
        // CR 3870467, do not log stack
        boolean shouldLogExceptionStackTrace = session.getSessionLog().shouldLogExceptionStackTrace();
        if (shouldLogExceptionStackTrace) {
            session.getSessionLog().setShouldLogExceptionStackTrace(false);
        }
        try {
            for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
                try {
                    schemaManager.dropConstraints((TableDefinition)enumtr.nextElement());
                } catch (oracle.toplink.essentials.exceptions.DatabaseException dbE) {
                    //ignore
                }
            }
        } finally {
            if (shouldLogExceptionStackTrace) {
                session.getSessionLog().setShouldLogExceptionStackTrace(true);
            }
        }

        String sequenceTableName = getSequenceTableName(session);
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            // Must not create sequence table as done in createSequences.
            TableDefinition table = (TableDefinition)enumtr.nextElement();
            if (!table.getName().equals(sequenceTableName)) {
                try {
                    schemaManager.replaceObject(table);
                } catch (DatabaseException ex) {
                    if (!shouldIgnoreDatabaseException()) {
                        throw ex;
                    }
                }
            }
        }

        // Unique constraints should be generated before foreign key constraints,
        // because foreign key constraints can reference unique constraints
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createUniqueConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }
        
        for (Enumeration enumtr = getTableDefinitions().elements(); enumtr.hasMoreElements();) {
            try {
                schemaManager.createForeignConstraints((TableDefinition)enumtr.nextElement());
            } catch (DatabaseException ex) {
                if (!shouldIgnoreDatabaseException()) {
                    throw ex;
                }
            }
        }
    }

    /**
     * Set the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the tables.
     */
    public void setTableDefinitions(Vector tableDefinitions) {
        this.tableDefinitions = tableDefinitions;
    }

    /**
     * Return true if DatabaseException is to be ignored.
     */
    boolean shouldIgnoreDatabaseException() {
        return ignoreDatabaseException;
    }

    /**
     * Set flag whether DatabaseException should be ignored. 
     */
    void setIgnoreDatabaseException(boolean ignoreDatabaseException) {
        this.ignoreDatabaseException = ignoreDatabaseException;
    }

    protected String getSequenceTableName(oracle.toplink.essentials.sessions.Session session) {
        String sequenceTableName = null;
        if (session.getProject().usesSequencing()) {
            oracle.toplink.essentials.sequencing.Sequence sequence = session.getLogin().getDefaultSequence();
            if (sequence instanceof oracle.toplink.essentials.sequencing.TableSequence) {
                sequenceTableName = ((oracle.toplink.essentials.sequencing.TableSequence)sequence).getTableName();
            }
        }
        return sequenceTableName;
    }
}
