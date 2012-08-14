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
    private String resourceAdapterName;
    private TransactionSupportLevel transactionSupport = TransactionSupportLevel.LocalTransaction;;
    
    public String getResourceAdapterName() {
        return resourceAdapterName;
    }

    public void setResourceAdapterName(String resourceAdapterName) {
        this.resourceAdapterName = resourceAdapterName;
    }

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
