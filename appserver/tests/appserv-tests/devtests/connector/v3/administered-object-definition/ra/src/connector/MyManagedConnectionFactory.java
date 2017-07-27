/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package connector;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.security.auth.Subject;

@ConnectionDefinition(
        connectionFactory=ConnectionFactory.class,
        connectionFactoryImpl=MyConnectionFactory.class,
        connection=Connection.class,
        connectionImpl=MyConnection.class)
public class MyManagedConnectionFactory implements ManagedConnectionFactory, TransactionSupport {

    private static final long serialVersionUID = 8394689502759459536L;
    private String testName;
    private ConnectionManager cm;
    private PrintWriter writer;
    private TransactionSupportLevel transactionSupport = TransactionSupportLevel.LocalTransaction;;
    
    public String getTestName() {
        return testName;
    }

    @ConfigProperty(
            defaultValue = "ConfigPropertyForRA",
            type = java.lang.String.class
    )
    public void setTestName(String name) {
        testName = name;
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new MyConnectionFactory(this, null);
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cm)  throws ResourceException {
        this.cm = cm;
        return new MyConnectionFactory(this, cm);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject,  ConnectionRequestInfo reqInfo) 
            throws ResourceException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return writer;
    }

    @Override
    public ManagedConnection matchManagedConnections(Set candidates, Subject sub,  ConnectionRequestInfo reqInfo) 
            throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter writer) throws ResourceException {
        this.writer = writer;
    }

    @Override
    public TransactionSupportLevel getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(TransactionSupportLevel transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

}
