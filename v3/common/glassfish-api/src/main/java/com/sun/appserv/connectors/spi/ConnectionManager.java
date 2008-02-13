package com.sun.appserv.connectors.spi;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;


public interface ConnectionManager extends javax.resource.spi.ConnectionManager {

    Object allocateNonTxConnection( ManagedConnectionFactory mcf,
        ConnectionRequestInfo cxRequestInfo ) throws ResourceException;
    String getJndiName() ;
}
