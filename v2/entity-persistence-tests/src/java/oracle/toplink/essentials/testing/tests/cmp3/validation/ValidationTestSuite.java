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
// Copyright (c) 1998, 2007, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.tests.cmp3.validation;

import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Map;
import javax.persistence.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import oracle.toplink.essentials.config.TopLinkProperties;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;

public class ValidationTestSuite extends JUnitTestCase {
    public ValidationTestSuite() {
    }
    
    public ValidationTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        // Don't clear the cache here since it bring the default pu into play.
    }
    
    public static Test suite() {
        return new TestSuite(ValidationTestSuite.class) {
        
            protected void setUp(){
            }

            protected void tearDown() {
            }
        };
    }
    
    public void testCacheIsolation_PropertiesDefault_Config() throws Exception {
        // Be sure not to modify the global properties.
        Map properties = new HashMap((HashMap) getDatabaseProperties());
        properties.put(TopLinkProperties.CACHE_SHARED_DEFAULT, Boolean.FALSE.toString());
        
        EntityManager em = createEntityManager("isolated1053", properties);
        assertTrue("Item descriptor did not have an isolated cache setting from a TopLink properties setting.", ((EntityManagerImpl)em).getServerSession().getClassDescriptorForAlias("Item").isIsolated());
        em.close();
        
        // Ensure this is done to avoid consecutive tests picking up our
        // very specific isolated persistence unit.
        this.closeEntityManagerFactoryNamedPersistenceUnit("isolated1053");
    }
    
    /**
     * This test assumes the persistence unit has the following property set:
     *       <property name="toplink.cache.shared.default" value="false"/>
     * @throws Exception
     */
     
    public void testCacheIsolation_XMLDefault_Config() throws Exception {
        EntityManager em = createEntityManager("isolated1053");
        assertTrue("Item descriptor did not have an isolated cache setting from an XML setting.", ((EntityManagerImpl)em).getServerSession().getClassDescriptorForAlias("Item").isIsolated());
        em.close();
        
        // Ensure this is done to avoid consecutive tests picking up our
        // very specific isolated persistence unit.
        this.closeEntityManagerFactoryNamedPersistenceUnit("isolated1053");
    }
    
    /**
     * This tests fix for gf bug 2492, specifically testing 
     * javax.persistence.jtaDataSource property.  There is no easy way outside a container
     * to ensure the look up fails, but this test ensures that the datasource passed in
     * is used to acquire a connection on login.  
     */
    public void testJTADatasource_Config_Override() throws Exception {
        boolean pass=false;
        Map properties = new HashMap();
        tmpDataSourceImp jtadatasourece = new tmpDataSourceImp();
        properties.put(TopLinkProperties.JTA_DATASOURCE, jtadatasourece);
        EntityManager em =null;
        try{
            em = createEntityManager("ignore", properties);
        }catch(RuntimeException expected){
            pass= "tmpDataSourceImp getConnection called".equals(expected.getMessage());
        }finally{
            if (em!=null){
                em.close();
            }
            // Ensure this is done to avoid consecutive tests picking up our
            // very specific isolated persistence unit.
            this.closeEntityManagerFactoryNamedPersistenceUnit("ignore");
        } 
        assertTrue("JTA datasource was not set or accessed as expected through map of properties", pass);
    }
    
    /**
     * This tests fix for gf bug 2492, specifically testing 
     * javax.persistence.jtaDataSource property.  There is no easy way outside a container
     * to ensure the look up fails, but this test ensures that the datasource passed in
     * is used to acquire a connection on login.  
     */
    public void testNonJTADatasource_Config_Override() throws Exception {    
        boolean pass=false;
        Map properties = new HashMap();
        tmpDataSourceImp nonJTADatasourece = new tmpDataSourceImp();
        properties.put(TopLinkProperties.NON_JTA_DATASOURCE, nonJTADatasourece);
        EntityManager em =null;
        try{
            em = createEntityManager("ignore", properties);
        }catch(RuntimeException expected){
            pass= "tmpDataSourceImp getConnection called".equals(expected.getMessage());
        }finally{
            if (em!=null){
                em.close();
            }
            // Ensure this is done to avoid consecutive tests picking up our
            // very specific isolated persistence unit.
            this.closeEntityManagerFactoryNamedPersistenceUnit("ignore");
        } 
        assertTrue("Non JTA datasource was not set or accessed as expected through map of properties", pass);
    }
    
    public void testPKClassTypeValidation(){
        try{
            createEntityManager().find(Employee.class, new Employee());
        }catch (IllegalArgumentException ex){
            return;
        }
        fail("Failed to throw expected IllegalArgumentException, when incorrect PKClass is used in find call");
    }
    
    public class tmpDataSourceImp implements DataSource{
        public tmpDataSourceImp(){
            super();
        }
        public Connection getConnection() throws SQLException{
            RuntimeException exception = 
                    new RuntimeException("tmpDataSourceImp getConnection called");
            throw exception;
        }
        public Connection getConnection(String username, String password) throws SQLException{
            return getConnection();
        }
        //rest are ignored
        public java.io.PrintWriter getLogWriter() throws SQLException{
            return null;
        }
        public void setLogWriter(java.io.PrintWriter out) throws SQLException{}
        public void setLoginTimeout(int seconds) throws SQLException{}
        public int getLoginTimeout() throws SQLException{return 1;}
        public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
    }
}
