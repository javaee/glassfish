package connector;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;

public class MyConnectionFactory implements ConnectionFactory {

    private static final long serialVersionUID = -8947169718238922386L;
    private ConnectionManager cm;
    private MyManagedConnectionFactory mcf;
    private Reference ref;
    
    public MyConnectionFactory(MyManagedConnectionFactory mcf, ConnectionManager cm) {
        super();
        this.mcf = mcf;
        this.cm = cm;
    }

    @Override
    public void setReference(Reference ref) {

    }

    @Override
    public Reference getReference() throws NamingException {
        return ref;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        return new MyConnection();
    }

    @Override
    public Connection getConnection(ConnectionSpec spec)  throws ResourceException {
        return new MyConnection();
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

}
