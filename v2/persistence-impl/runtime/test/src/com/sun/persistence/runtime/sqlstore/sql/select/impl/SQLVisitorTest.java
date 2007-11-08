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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.persistence.runtime.model.CompanyMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.query.QueryContext;
import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.persistence.runtime.sqlstore.database.DBVendorType;
import com.sun.persistence.runtime.query.impl.PersistenceQueryContext;
import com.sun.persistence.runtime.query.impl.EJBQLC3;
import com.sun.persistence.runtime.query.impl.QueryFactory;
import com.sun.persistence.runtime.query.impl.EJBQLQueryImpl;
import com.sun.org.apache.jdo.pm.MockPersistenceManagerInternal;

import java.sql.DatabaseMetaData;

import junit.framework.*;

/**
 *
 * @author jie leng
 */
public class SQLVisitorTest extends TestCase { 
 
    private QueryInternal query = null;

    private DBVendorType dbVendor = null;
    private String dbVendorName;
    private static String ORACLE = "ORACLE";
    private static String POINTBASE = "POINTBASE";
    private static final RuntimeMappingModel companyMappingModel
        = CompanyMappingModel.getInstance();
    
    protected void setUp() throws Exception {
        super.setUp();
        
        dbVendorName = ORACLE;
        DatabaseMetaData mockDBMetaData = new MockDatabaseMetaDataImpl(dbVendorName);
        dbVendor = new DBVendorType(mockDBMetaData, null);
        
        String DEPT_CLASSNAME = 
            CompanyMappingModel.COMPANY_PACKAGE + "Department";
        companyMappingModel.getJDOModel().getJDOClass(DEPT_CLASSNAME);

    }
    
    public SQLVisitorTest(String testName) {
        super(testName);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SQLVisitorTest.class);
        
        return suite;
    }
     
    protected void executeQuery(String qstr, String expectedText) {
        
        query = (QueryInternal) QueryFactory.getInstance()
                .createQuery(qstr, new MockPersistenceManagerInternal());
        
        SQLTestCompilationMonitor cm =
                new SQLTestCompilationMonitor(
                companyMappingModel, dbVendor);   

        QueryContext qc = new PersistenceQueryContext(
                CompanyMappingModel.getInstance());
        
            try {
                EJBQLC3.getInstance().compile(query, qc, cm);
            } catch(Exception ex) {
                System.out.println("Got an exception: " + ex);
                fail();
            }
        
        String statementText = cm.getStatementText();
        assertTrue(expectedText.equals(statementText.trim()));        
    }

    // Since we don't have int and double data ready in company model, testMod
    // and testSqrt are comments out.
    /*
    public void testMod() {
        
        String qstr = 
            "select object(d) from Department d where mod(d.deptid, 10) = 5";
        String expectedText = "";
        executeQuery(qstr, expectedText);
    }
 
    public void testSqrt(){
        String qstr = 
            "select object(d) from Department d where sqrt(d.deptid) = 5.0";
        String expectedText = "";
       executeQuery(qstr, expectedText);
    } 
    */
    
    public void testAbs() {
        String qstr = 
            "select object(d) from Department d where abs(d.deptid) = 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where ABS(t0.DEPTID) = 5";
        executeQuery(qstr, expectedText);
    }
   
    public void testConcat() {
        String qstr = 
            "select object(d) from Department d where concat(d.name, d.name) = 'test'";
        
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where concat(t0.NAME, t0.NAME) = 'test'";
        executeQuery(qstr, expectedText);
    }
    
    public void testSubstring() {
        String qstr =
            "select object(d) from Department d where substring(d.name, 1, 4) = 'test'";
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where SUBSTR(t0.NAME, 1, 4) = 'test'";
        executeQuery(qstr, expectedText);
    }
    
    public void testLength() {
        String qstr =
            "select object(d) from Department d where length(d.name) = 3";
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where LENGTH(t0.NAME) = 3";
        executeQuery(qstr, expectedText);
        
    }
    
    public void testLower() {
        String qstr =
            "select object(d) from Department d where lower(d.name) = 'test'";
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where LOWER(t0.NAME) = 'test'";
        executeQuery(qstr, expectedText);
        
    }
    
    public void testUpper() {
        String qstr =
            "select object(d) from Department d where upper(d.name) = 'test'";
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where UPPER(t0.NAME) = 'test'";
        executeQuery(qstr, expectedText);
        
    }
    
    public void testTrim() {
        String qstr =
            "select object(d) from Department d where trim(BOTH 'C' from d.name) = 'test'";
        String expectedText = 
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where trim(BOTH 'C' from t0.NAME) = 'test'";
        executeQuery(qstr, expectedText);
        
    }
    
    public void testLocate() {
        String qstr =
            "select object(d) from Department d where locate('test', d.name, 1) = 3";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where INSTR('test', t0.NAME, 1) = 3";
        executeQuery(qstr, expectedText);
    } 
    
    public void testIsNull() {
        String qstr =
            "select object(d) from Department d where d.name is null";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.NAME is NULL";
        executeQuery(qstr, expectedText);       
    }
    
    public void testIsNotNull() {
        String qstr =
            "select object(d) from Department d where d.name is not null";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.NAME is not NULL"; 
        executeQuery(qstr, expectedText);       
    }
    
    public void testLike() {
        String qstr =
            "select object(d) from Department d where d.name like 'F_\\_test%' ESCAPE '\\'";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.NAME LIKE 'F_\\_test%' ESCAPE '\\'"; 
        executeQuery(qstr, expectedText);       
    }
    
    public void testNotLike() {
        String qstr =
            "select object(d) from Department d where d.name not like 'F_\\_test%' ESCAPE '\\'";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.NAME NOT LIKE 'F_\\_test%' ESCAPE '\\'"; 
        executeQuery(qstr, expectedText);       
    }    
    
    public void testGT() {
        String qstr =
            "select object(d) from Department d where d.deptid > 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID > 5"; 
        executeQuery(qstr, expectedText);       
    }
    
    public void testGE() {
        String qstr =
            "select object(d) from Department d where d.deptid >= 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID >= 5"; 
        executeQuery(qstr, expectedText);       
    } 
    
    public void testLT() {
        String qstr =
            "select object(d) from Department d where d.deptid < 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID < 5"; 
        executeQuery(qstr, expectedText);       
    } 
    
    public void testLE() {
        String qstr =
            "select object(d) from Department d where d.deptid <= 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID <= 5"; 
        executeQuery(qstr, expectedText);       
    }    
    
    public void testNotEqual() {
        String qstr =
            "select object(d) from Department d where d.deptid <> 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID <> 5"; 
        executeQuery(qstr, expectedText);      
    }
    
    public void testPlus() {
        String qstr =
            "select object(d) from Department d where d.deptid + 5 = 10";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID + 5 = 10"; 
        executeQuery(qstr, expectedText);       
    }   
        
    public void testMinus() {
        String qstr =
            "select object(d) from Department d where d.deptid - 5 = 10";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID - 5 = 10"; 
        executeQuery(qstr, expectedText);       
    } 
        
    public void testStar() {
        String qstr =
            "select object(d) from Department d where d.deptid * 5 = 10";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID * 5 = 10"; 
        executeQuery(qstr, expectedText);       
    }    
        
    public void testDiv() {
        String qstr =
            "select object(d) from Department d where d.deptid / 5 = 10";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID / 5 = 10"; 
        executeQuery(qstr, expectedText);       
    }    
           
    public void testOr() {
        String qstr =
            "select object(d) from Department d where d.deptid = 5 or d.name = 'test'";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID = 5 OR t0.NAME = 'test'"; 
        executeQuery(qstr, expectedText);       
    }    

     public void testNot() {
        String qstr =
            "select object(d) from Department d where not d.deptid = 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where NOT t0.DEPTID = 5"; 
        executeQuery(qstr, expectedText);       
    }    
    
     public void testBetween() {
        String qstr =
            "select object(d) from Department d where d.deptid between 3 and 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID BETWEEN 3 AND 5"; 
        executeQuery(qstr, expectedText);       
    }    
     
     public void testNotBetween() {
        String qstr =
            "select object(d) from Department d where d.deptid not between 3 and 5";
        String expectedText =
            "SELECT t0.DEPTID, t0.NAME FROM DEPARTMENT t0 where t0.DEPTID NOT BETWEEN 3 AND 5"; 
        executeQuery(qstr, expectedText);       
    }
   
    public void testSingleProjection() {
        String qstr =
            "select d.name from Department d where d.deptid between 3 and 5";
        String expectedText =
            "SELECT t0.NAME FROM DEPARTMENT t0 where t0.DEPTID BETWEEN 3 AND 5"; 
        executeQuery(qstr, expectedText);       
    }   
     
}

