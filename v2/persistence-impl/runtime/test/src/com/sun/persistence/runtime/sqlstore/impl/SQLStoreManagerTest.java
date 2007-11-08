/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * SQLStoreManagerTest.java
 * JUnit based test
 *
 * Created on April 28, 2005, 2:30 PM
 */

package com.sun.persistence.runtime.sqlstore.impl;

import junit.framework.*;
import com.sun.persistence.runtime.connection.ConnectionFactory;
import com.sun.persistence.runtime.connection.impl.ConnectionFactoryImpl;
import com.sun.persistence.runtime.connection.SQLConnector;
import com.sun.persistence.support.identity.ByteIdentity;
import com.sun.persistence.support.identity.CharIdentity;
import com.sun.persistence.support.identity.IntIdentity;
import com.sun.persistence.support.identity.LongIdentity;
import com.sun.persistence.support.identity.ShortIdentity;
import com.sun.persistence.support.identity.SingleFieldIdentity;
import com.sun.persistence.support.identity.StringIdentity;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import com.sun.org.apache.jdo.tck.pc.company.Department;

/**
 * To test getting database connection, you need to provide
 * connection.propreties for your own database. Otherwise, local pointbase
 * is default database.
 * @author jie leng
 */
public class SQLStoreManagerTest extends TestCase {

    private final String fileName = "connection.properties";

    private final String userName = "username";

    private final String password = "password";

    private final String driverName = "drivername";

    private final String connectionURL = "connectionURL";

    private final String defaultUserName = "pbpublic";

    private final String defaultPassword = "pbpublic";

    private final String defaultDriverName = "com.pointbase.jdbc.jdbcUniversalDriver";

    private final String defaultConnectionURL ="jdbc:pointbase://localhost/sample";

    private Properties prop = null;

    private SQLStoreManager srm = null;

    public SQLStoreManagerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        prop = getDatabaseParameters();

        ConnectionFactory cf = new ConnectionFactoryImpl();

        cf.setDriverName((String)prop.getProperty(driverName, defaultDriverName));
        cf.setURL((String)prop.getProperty(connectionURL, defaultConnectionURL));
        cf.setUserName((String)prop.getProperty(userName, defaultUserName));
        cf.setPassword((String)prop.getProperty(password, defaultPassword));

        srm = new SQLStoreManager(null, cf, null, null);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SQLStoreManagerTest.class);

        return suite;
    }

    private Properties getDatabaseParameters(){
        Properties prop = new Properties();
        try {
            InputStream inStream = new FileInputStream(fileName);
            prop.load(inStream);
        } catch (Exception e) {
            // There is no coonection.properties, use pointbase as
            // the default database.
        }
        return prop;
    }

    /**
     * Test of getConnector method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testGetConnector() {

        Connection c = null;
        SQLConnector cr = null;

        cr = (SQLConnector)srm.getConnector();
        assertNotNull("SQLConnector must not be null", cr);

        try {
            c = cr.getConnection();
            //assertNotNull("connection must not be null", c);
        } catch (Exception e) {
            //assertNotNull("connection must not be null", c);
            //e.printStackTrace();
        }

        if (cr != null) {
            cr.flush();
        }
    }

    /**
     * Test of insert method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testInsert() {
    }

    /**
     * Test of update method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testUpdate() {
    }

    /**
     * Test of verifyFields method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testVerifyFields() {
    }

    /**
     * Test of delete method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testDelete() {
    }

    /**
     * Test of fetch method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testFetch() {
    }

    /**
     * Test of getExtent method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testGetExtent() {
    }

    /**
     * Test of createObjectId method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testCreateObjectId() {
    }

    /**
     * Test of getExternalObjectId method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testGetExternalObjectId() {
        Department d = new Department();

        Object o = null;
        assertNull("Null not returned", srm.getExternalObjectId(o, d));

        o = new Long(1l);
        assertSame(o, srm.getExternalObjectId(o, d));

        o = new ByteIdentity(Department.class, (byte)1);
        assertEquals(new Byte((byte)1), srm.getExternalObjectId(o, d));

        o = new LongIdentity(Department.class, (long)1);
        assertEquals(new Long((long)1), srm.getExternalObjectId(o, d));

        o = new CharIdentity(Department.class, (char)1);
        assertEquals(new Character((char)1), srm.getExternalObjectId(o, d));

        o = new IntIdentity(Department.class, 1);
        assertEquals(new Integer(1), srm.getExternalObjectId(o, d));

        o = new ShortIdentity(Department.class, (short)1);
        assertEquals(new Short((short)1), srm.getExternalObjectId(o, d));

        o = new StringIdentity(Department.class, "A");
        assertEquals(new String("A"), srm.getExternalObjectId(o, d));
    }

    /**
     * Test of getInternalObjectId method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testGetInternalObjectId() {
        Object o = null;
        assertNull("Null not returned", 
                srm.getInternalObjectId(o, null, Department.class));

        o = new LongIdentity(Long.class, (long)1);
        assertSame(o, srm.getInternalObjectId(o, null, Department.class));

        o = new Byte((byte)1);
        assertEquals(new ByteIdentity(Department.class, (byte)1), 
                srm.getInternalObjectId(o, null, Department.class));

        o = new Long((long)1);
        assertEquals(new LongIdentity(Department.class, (long)1), 
                srm.getInternalObjectId(o, null, Department.class));

        o = new Integer(1);
        assertEquals(new IntIdentity(Department.class, 1), 
                srm.getInternalObjectId(o, null, Department.class));

        o = new Short((short)1);
        assertEquals(new ShortIdentity(Department.class, (short)1), 
                srm.getInternalObjectId(o, null, Department.class));

        o = new Character((char)1);
        assertEquals(new CharIdentity(Department.class, (char)1), 
                srm.getInternalObjectId(o, null, Department.class));

        o = new String("A");
        assertEquals(new StringIdentity(Department.class, "A"), 
                srm.getInternalObjectId(o, null, Department.class));
    }

    /**
     * Test of getPCClassForOid method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testGetPCClassForOid() {
    }

    /**
     * Test of hasActualPCClass method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testHasActualPCClass() {
    }

    /**
     * Test of newObjectIdInstance method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testNewObjectIdInstance() {
    }

    /**
     * Test of copyKeyFieldsFromObjectId method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testCopyKeyFieldsFromObjectId() {
    }

    /**
     * Test of flush method, of class com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager.
     */
    public void testFlush() {
    }

}
