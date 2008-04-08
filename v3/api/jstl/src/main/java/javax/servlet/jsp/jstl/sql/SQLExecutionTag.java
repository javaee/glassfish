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

/**
 * <p>This interface allows tag handlers implementing it to receive
 * values for parameter markers in their SQL statements.</p>
 *
 * <p>This interface is implemented by both &lt;sql:query&gt; and
 * &lt;sql:update&gt;. Its <code>addSQLParameter()</code> method
 * is called by nested parameter actions (such as &lt;sql:param&gt;)
 * to substitute <code>PreparedStatement</code> parameter values for
 * "?" parameter markers in the SQL statement of the enclosing
 * <code>SQLExecutionTag</code> action.</p>
 *
 * <p>The given parameter values are converted to their corresponding
 * SQL type (following the rules in the JDBC specification) before
 * they are sent to the database.</p>
 *
 * <p>Keeping track of the index of the parameter values being added
 * is the responsibility of the tag handler implementing this
 * interface</p>
 *
 * <p>The <code>SQLExcecutionTag</code> interface is exposed in order
 * to support custom parameter actions which may retrieve their
 * parameters from any source and process them before substituting
 * them for a parameter marker in the SQL statement of the
 * enclosing <code>SQLExecutionTag</code> action</p>
 *
 * @author Justyna Horwat
 */
public interface SQLExecutionTag {

    /**
     * Adds a PreparedStatement parameter value. 
     * Must behave as if it calls <code>PreparedStatement.setObject(int, Object)</code>. 
     * For each tag invocation, the integral index passed logically to <code>setObject()</code> 
     * must begin with 1 and must be incremented by 1 for each subsequent invocation 
     * of <code>addSQLParameter()</code>. The Object logically passed to <code>setObject()</code> must be the 
     * unmodified object received in the value argument.
     *
     * @param value the <code>PreparedStatement</code> parameter value
     */
    public void addSQLParameter(Object value);
}
