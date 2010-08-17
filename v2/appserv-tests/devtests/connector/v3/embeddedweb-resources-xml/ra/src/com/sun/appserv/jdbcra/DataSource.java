/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.appserv.jdbcra;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The <code>javax.sql.DataSource</code> implementation of SunONE application 
 * server will implement this interface. An application program would be able
 * to use this interface to do the extended functionality exposed by SunONE 
 * application server.
 * <p>A sample code for getting driver's connection implementation would like 
 * the following.
 * <pre>
     InitialContext ic = new InitialContext();
     com.sun.appserv.DataSource ds = (com.sun.appserv.DataSOurce) ic.lookup("jdbc/PointBase"); 
     Connection con = ds.getConnection();
     Connection drivercon = ds.getConnection(con);

     // Do db operations.

     con.close();
   </pre>
 * 
 * @author Binod P.G
 */
public interface DataSource extends javax.sql.DataSource {

    /**
     * Retrieves the actual SQLConnection from the Connection wrapper 
     * implementation of SunONE application server. If an actual connection is
     * supplied as argument, then it will be just returned.
     *
     * @param con Connection obtained from <code>Datasource.getConnection()</code>
     * @return <code>java.sql.Connection</code> implementation of the driver.
     * @throws <code>java.sql.SQLException</code> If connection cannot be obtained.
     */
    public Connection getConnection(Connection con) throws SQLException;

}
