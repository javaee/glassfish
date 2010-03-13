/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.osgijdbc;


import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A Proxy to <i>javax.sql.DataSource</i> that will be
 * initialized upon first time usage by OSGi applications.<br>
 * Delegates the calls to the DataSource object obtained from
 * GlassFish's runtime.<br>
 *
 * @author Jagadish Ramu
 */
public class DataSourceProxy implements DataSource {

    private DataSource ds;
    //TODO should we provide __nontx resource ?
    private String jndiName; // resource's identity
    private boolean invalidated = false;

    public DataSourceProxy(String jndiName){
        this.jndiName = jndiName;
    }

    private DataSource getDS() {
        if (!invalidated) {
            if (ds == null) {
                try {
                    ds = (DataSource) new InitialContext().lookup(jndiName);
                } catch (NamingException e) {
                    System.err.println(e);
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        } else {
            throw new RuntimeException("Resource [" + jndiName + "] is either undeployed or redeployed");
        }
        return ds;
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return getDS().getConnection();
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection(String username, String password) throws SQLException {
        return getDS().getConnection(username, password);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDS().unwrap(iface);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDS().isWrapperFor(iface);
    }

    /**
     * {@inheritDoc}
     */
    public PrintWriter getLogWriter() throws SQLException {
        return getDS().getLogWriter();
    }

    /**
     * {@inheritDoc}
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDS().setLogWriter(out);
    }

    /**
     * {@inheritDoc}
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        getDS().setLoginTimeout(seconds);
    }

    /**
     * {@inheritDoc}
     */
    public int getLoginTimeout() throws SQLException {
        return getDS().getLoginTimeout();
    }

    /**
     * sets the state of the <i>data-source</i> as invalid<br>
     */
    public void invalidate(){
        invalidated = true;
    }
}
