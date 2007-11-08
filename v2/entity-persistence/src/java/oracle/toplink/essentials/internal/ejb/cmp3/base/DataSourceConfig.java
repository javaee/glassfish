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
package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.io.StringWriter;

/**
 * Simplest of all possible holder objects for all of the data source 
 * info required by the Entity test environment. Use the constructor to 
 * simultaneously create the instance and set the fields. 
 * <p>
 * At least one (transactional) data source must be specified and 
 * potentially a non-transactional if such a data source is appropriate 
 * (e.g. for non-transactional operations).
 * <p>
 * @see ContainerConfig
 * @see EntityContainer
 */
public class DataSourceConfig {

    /** Identifier to name this data source (must be Container-unique) */
    public String dsName;

    /** JNDI name that data source should be bound to */
    public String jndiName;

    /** URL that is passed to the driver to determine db */
    public String url;

    /** Driver class name string */
    public String driver;

    /** User name to use when connecting to the db */
    public String user;

    /** Password to use when connecting to the db */
    public String password;
    
    /**
     * Constructor used to create a DataSourceConfig
     *
     * @param dsName Data source identifier 
     * @param jndiName Name that the data source should be bound to in JNDI
     * @param url Passed to the driver to determine db
     * @param driver The class name for the db driver
     * @param user User name to use when connecting to the db
     * @param password Password to use when connecting to the db
     */
    public DataSourceConfig(String dsName, String jndiName, String url, String driver, String user, String password) {
        this.dsName = dsName;
        this.jndiName = jndiName;
        this.url = url;
        this.driver = driver;
        this.user = user;
        this.password = password;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        StringWriter writer = new StringWriter();
        if(dsName != null) {
            writer.write("dsName = " + dsName + '\n');
        }
        if(jndiName != null) {
            writer.write("jndiName = " + jndiName + '\n');
        }
        if(url != null) {
            writer.write("url = " + url + '\n');
        }
        if(driver != null) {
            writer.write("driver = " + driver + '\n');
        }
        if(user != null) {
            writer.write("user = " + user + '\n');
        }
        return writer.toString();
    }
}
