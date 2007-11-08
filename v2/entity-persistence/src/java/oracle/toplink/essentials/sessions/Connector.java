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
package oracle.toplink.essentials.sessions;

import java.util.*;
import java.sql.*;
import java.io.*;

/**
 * <b>Purpose</b>:
 * Define an interface for supplying TopLink with a <code>Connection</code> to
 * a JDBC database.
 * <p>
 * <b>Description</b>:
 * This interface defines the methods to be implemented that allow TopLink to
 * acquire a <code>Connection</code> to a JDBC database. There are only 2
 * methods that need to be implemented:
 * <blockquote><code>
 * java.sql.Connection connect(java.util.Properties properties)<br>
 * void toString(java.io.PrintWriter writer)
 * </code></blockquote>
 * Once these methods have been implemented, an instance of the new
 * <code>Connector</code> can be  passed
 * to a <code>JDBCLogin</code> at startup. For example:
 * <blockquote><code>
 * session.getLogin().setConnector(new FooConnector());<br>
 * session.login();
 * </code></blockquote>
 *
 * @see DatabaseLogin
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public interface Connector extends Serializable, Cloneable {

    /**
     * PUBLIC:
     * Must be cloneable.
     */
    Object clone();

    /**
     * PUBLIC:
     * Connect with the specified properties and return the <code>Connection</code>.
     * The properties are driver-specific; but usually contain the <code>"user"</code>
     * and <code>"password"</code>. Additional
     * properties can be built by using <code>JDBCLogin.setProperty(String propertyName,
     * Object propertyValue)</code>.
     * @return java.sql.Connection
     */
    Connection connect(Properties properties);

    /**
     * PUBLIC:
     * Print something useful on the log. This information will be displayed
     * on the TopLink log (by default <code>System.out</code>) at login.
     * See the other implementations of this method for examples.
     */
    void toString(PrintWriter writer);

    /**
     * PUBLIC:
     * Provide the details of my connection information. This is primarily for JMX runtime services.
     * @return java.lang.String
     */
    String getConnectionDetails();
}
