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
package oracle.toplink.essentials.exceptions;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 *    <p><b>Purpose</b>: IntegrityChecker is used for catching all the descriptor exceptions,
 *    and checking database tables. It gives the user options if he/she wants to
 *    catch descriptor exceptions, check database, and check InstantiationPolicy or not.
 */
public class IntegrityChecker implements Serializable {

    /** To add all the Descriptor exceptions */
    protected Vector caughtExceptions = null;

    /** To load the tables from database    */
    protected Vector tables = null;

    /** To know that should we catch all the descriptors exceptions or not */
    protected boolean shouldCatchExceptions;

    /** To know that should we check database tables or not */
    protected boolean shouldCheckDatabase;

    /** To know that should we check InstantiationPolicy or not */
    protected boolean shouldCheckInstantiationPolicy;

    /**
     * PUBLIC:
     * IntegrityChecker is used for catching all the Descriptor Exceptions,
     * and check database tables. IntegrityChecker gives the option to the user that does he wants to
     * catch all the descriptor exceptions,check database, and check InstantiationPolicy or not.
     */
    public IntegrityChecker() {
        super();
        this.shouldCatchExceptions = true;
        this.shouldCheckDatabase = false;
        this.shouldCheckInstantiationPolicy = true;
    }

    /**
     * PUBLIC:
     * This method is used for catching all the Descriptor Exceptions
     */
    public void catchExceptions() {
        setShouldCatchExceptions(true);
    }

    /**
     * PUBLIC:
     * This method is used to check the database tables.
     */
    public void checkDatabase() {
        setShouldCheckDatabase(true);
    }

    /**
     * PUBLIC:
     * This method is used to check the InstantiationPolicy.
     */
    public void checkInstantiationPolicy() {
        setShouldCheckInstantiationPolicy(true);
    }

    /**
     * INTERNAL:
     * This method checks that tables are present in the database.
     */
    public boolean checkTable(DatabaseTable table, AbstractSession session) {
        if (getTables().size() == 0) {
            // load the tables from the session
            initializeTables(session);
        }
        //MySQL converts all the table names to lower case.
        if (session.getPlatform().isMySQL()) {
            return getTables().contains(table.getName().toLowerCase());
        }
        return getTables().contains(table.getName());
    }

    /**
     * PUBLIC:
     * This method is used for don't catching all the Descriptor Exceptions
     */
    public void dontCatchExceptions() {
        setShouldCatchExceptions(false);
    }

    /**
     * PUBLIC:
     * This method is used for don't checking the database tables and fields.
     */
    public void dontCheckDatabase() {
        setShouldCheckDatabase(false);
    }

    /**
     * PUBLIC:
     * This method is used for don't checking the InstantiationPolicy.
     */
    public void dontCheckInstantiationPolicy() {
        setShouldCheckInstantiationPolicy(false);
    }

    /**
     * PUBLIC:
     * This method returns the vecotr which adds all the Descriptors Exceptions.
     */
    public Vector getCaughtExceptions() {
        if (caughtExceptions == null) {
            caughtExceptions = new Vector();
        }
        return caughtExceptions;
    }

    /**
     * INTERNAL:
     * This method returns a vector which holds all the tables of database
     */
    public Vector getTables() {
        if (tables == null) {
            tables = new Vector();
        }
        return tables;
    }

    /**
     * INTERNAL:
     * This method handle all the Descriptor Exceptions.
     * This method throw the exception or add the exceptions into a vector depand on the value of shouldCatchExceptions.
     */
    public void handleError(RuntimeException runtimeException) {
        if (!shouldCatchExceptions()) {
            throw runtimeException;
        }
        getCaughtExceptions().addElement(runtimeException);
    }

    /**
     * INTERNAL:
     * Return if any errors occured.
     */
    public boolean hasErrors() {
        if ((caughtExceptions != null) && (caughtExceptions.size() > 0)) {
            return true;
        }
        return false;
    }

    /**
     * INTERNAL:
     * Return if any runtime errors occured.
     */
    public boolean hasRuntimeExceptions() {
        if (hasErrors()) {
            for (Enumeration exceptionsEnum = getCaughtExceptions().elements();
                     exceptionsEnum.hasMoreElements();) {
                if (exceptionsEnum.nextElement() instanceof RuntimeException) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * This method is used to get all the database tables and add them into a vector.
     */
    public void initializeTables(AbstractSession session) {
        Vector result = session.getAccessor().getTableInfo(null, null, null, null, session);
        for (Enumeration resultEnum = result.elements(); resultEnum.hasMoreElements();) {
            AbstractRecord row = (AbstractRecord)resultEnum.nextElement();
            tables.addElement(row.get("TABLE_NAME"));
        }
    }

    /**
     * INTERNAL:
     */
    public void setCaughtExceptions(Vector exceptions) {
        this.caughtExceptions = exceptions;
    }

    /**
     * PUBLIC:
     * This method assigns the value to the variable (shouldCatchExceptions)
     * that we should catch all Descriptor Exceptions or not.
     */
    public void setShouldCatchExceptions(boolean answer) {
        shouldCatchExceptions = answer;
    }

    /**
     * PUBLIC:
     * This method assigns the value to the variable (shouldCheckDatabase)
     * that we should check database or not.
     */
    public void setShouldCheckDatabase(boolean answer) {
        shouldCheckDatabase = answer;
    }

    /**
     * PUBLIC:
     * This method assigns the value to the variable (shouldCheckInstantiationPolicy)
     * that we should check InstantiationPolicy or not.
     */
    public void setShouldCheckInstantiationPolicy(boolean answer) {
        shouldCheckInstantiationPolicy = answer;
    }

    /**
     * PUBLIC:
     * This method is used to know that all the Descriptor Exceptions should be thrown or not.
     */
    public boolean shouldCatchExceptions() {
        return shouldCatchExceptions;
    }

    /**
     * PUBLIC:
     * This method is used to know that database tables and fields should be checked or not.
     */
    public boolean shouldCheckDatabase() {
        return shouldCheckDatabase;
    }

    /**
     * PUBLIC:
     * This method tells us that we should check InstantiationPolicy or not.
     */
    public boolean shouldCheckInstantiationPolicy() {
        return shouldCheckInstantiationPolicy;
    }
}
