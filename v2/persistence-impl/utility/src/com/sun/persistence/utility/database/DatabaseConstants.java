/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.utility.database;

/**
 * @author Marina Vatkina This interface defines string constants used by the
 *         database generation.
 */
public interface DatabaseConstants {

    // Substrings used to construct file names.
    char NAME_SEPARATOR = '_'; // NOI18N

    String JDBC_FILE_EXTENSION = ".jdbc"; // NOI18N
    String SQL_FILE_EXTENSION = ".sql"; // NOI18N
    String CREATE = "create"; // NOI18N
    String DROP = "drop"; // NOI18N
    String DDL = "DDL"; // NOI18N

    // Known file name suffixes.
    String CREATE_DDL_JDBC_FILE_SUFFIX = NAME_SEPARATOR + CREATE + DDL
            + JDBC_FILE_EXTENSION;
    String DROP_DDL_JDBC_FILE_SUFFIX = NAME_SEPARATOR + DROP + DDL
            + JDBC_FILE_EXTENSION;
    String CREATE_SQL_FILE_SUFFIX = NAME_SEPARATOR + CREATE
            + SQL_FILE_EXTENSION;
    String DROP_SQL_FILE_SUFFIX = NAME_SEPARATOR + DROP + SQL_FILE_EXTENSION;

    // Flag used to indicate a database generation mode.
    public static final String JAVA_TO_DB_FLAG = "java-to-database"; // NOI18N
}
