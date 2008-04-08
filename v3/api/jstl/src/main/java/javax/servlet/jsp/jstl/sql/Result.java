/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.jsp.jstl.sql;

import java.util.SortedMap;

/**
 * <p>This interface represents the result of a &lt;sql:query&gt;
 * action. It provides access to the following information in the
 * query result:</p>
 *
 * <ul>
 * <li> The result rows (<tt>getRows()</tt> and <tt>getRowsByIndex()</tt>)
 * <li> The column names (<tt>getColumnNames()</tt>)
 * <li> The number of rows in the result (<tt>getRowCount()</tt>)
 * <li> An indication whether the rows returned represent the complete result 
 *      or just a subset that is limited by a maximum row setting
 *      (<tt>isLimitedByMaxRows()</tt>)
 * </ul>
 *
 * <p>An implementation of the <tt>Result</tt> interface provides a
 * <i>disconnected</i> view into the result of a query.
 *
 * @author Justyna Horwat
 *
 */
public interface Result {

    /**
     * <p>Returns the result of the query as an array of <code>SortedMap</code> objects. 
     * Each item of the array represents a specific row in the query result.</p>
     *
     * <p>A row is structured as a <code>SortedMap</code> object where the key is the column name, 
     * and where the value is the value associated with the column identified by 
     * the key. The column value is an Object of the Java type corresponding 
     * to the mapping between column types and Java types defined by the JDBC 
     * specification when the <code>ResultSet.getObject()</code> method is used.</p>
     *
     * <p>The <code>SortedMap</code> must use the <code>Comparator</code> 
     * <code>java.util.String.CASE_INSENSITIVE_ORDER</code>. 
     * This makes it possible to access the key as a case insensitive representation 
     * of a column name. This method will therefore work regardless of the case of 
     * the column name returned by the database.</p>
     *
     * @return The result rows as an array of <code>SortedMap</code> objects
     */
    public SortedMap[] getRows();

    /**
     * Returns the result of the query as an array of arrays. 
     * The first array dimension represents a specific row in the query result. 
     * The array elements for each row are Object instances of the Java type 
     * corresponding to the mapping between column types and Java types defined 
     * by the JDBC specification when the <code>ResultSet.getObject()</code> method is used.
     *
     * @return the result rows as an array of <code>Object[]</code> objects
     */
    public Object[][] getRowsByIndex();

    /**
     * Returns the names of the columns in the result. The order of the names in the array 
     * matches the order in which columns are returned in method getRowsByIndex().
     *
     * @return the column names as an array of <code>String</code> objects
     */
    public String[] getColumnNames();

    /**
     * Returns the number of rows in the cached ResultSet
     *
     * @return the number of rows in the result
     */
    public int getRowCount();

    /**
     * Returns true if the query was limited by a maximum row setting
     *
     * @return <tt>true</tt> if the query was limited by a maximum
     * row setting
     */
    public boolean isLimitedByMaxRows();
}
