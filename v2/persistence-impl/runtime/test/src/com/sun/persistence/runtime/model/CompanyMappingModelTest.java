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
 * CompanyMappingModelTest.java
 *
 * Created on 14. April 2005, 14:46
 */

package com.sun.persistence.runtime.model;

import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ColumnPairElement;
import com.sun.forte4j.modules.dbmodel.SchemaElement;

import com.sun.org.apache.jdo.impl.model.jdo.util.PrintSupport;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOProperty;
import com.sun.org.apache.jdo.tck.pc.company.Company;

import com.sun.org.apache.jdo.tck.pc.company.Department;
import com.sun.org.apache.jdo.tck.pc.company.Project;

import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.api.model.mapping.MappingTable;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;

/**
 * JUnit test for the CompanyMappingModel.
 *
 * @author Michael Bouschen
 */
public class CompanyMappingModelTest extends TestCase {
    
    private RuntimeMappingModel mappingModel = null;
    private JDOModel jdoModel = null;
    private JavaModel javaModel = null;
    private SchemaElement dbSchema = null;
    
    private boolean verbose = false;
    
    private static final String COMPANY_CLASSNAME = 
        CompanyMappingModel.COMPANY_PACKAGE + "Company";
    private static final String DEPT_CLASSNAME = 
        CompanyMappingModel.COMPANY_PACKAGE + "Department";
    private static final String PROJ_CLASSNAME = 
        CompanyMappingModel.COMPANY_PACKAGE + "Project";

    
    /** */
    public CompanyMappingModelTest(String testName) {
        super(testName);
    }

    /** */
    public void testMappingModel() {
        runTestModels();
        runTestCompany();
        runTestDepartment();
        runTestProject();
        if (verbose) {
            printMappingModel(mappingModel);
        }
    }

    /** */
    public void testJDOModel() {
        runTestShortNames();
        runTestProperties();
        if (verbose) {
            printJDOModel(jdoModel);
        }
    }
    
    // ===== helper methods =====
    
    private void initModels() {
        mappingModel = CompanyMappingModel.getInstance(
            new ClassLoader() { });
        jdoModel = mappingModel.getJDOModel();
        javaModel = jdoModel.getJavaModel(); 
        
        // access Department JDOClass which causes package.jdo to be read
        jdoModel.getJDOClass(DEPT_CLASSNAME);
    }
    
    /** */
    private void runTestModels() {
        assertNotNull("missing JavaModel", javaModel);
        assertNotNull("missing JDOModel", jdoModel);
        assertNotNull("missing MappingModel", mappingModel);
    }

    /** */
    private void runTestCompany() {
        
        // access mapping class
        
        RuntimeMappingClass company = 
            mappingModel.getMappingClass(COMPANY_CLASSNAME);
        assertNotNull("missing mapping class for Company", company);
        assertEquals("unexpected class name", COMPANY_CLASSNAME, 
            company.getName());
        assertNotNull("missing SchemaElement for Company mapping", 
            company.getDatabaseSchema());
        assertSame("Mapping class returns unexpected Company class instance",
            Company.class, company.getJavaClass());
        
        // access tables
        
        MappingTable[] companyTables = company.getMappingTables();
        assertNotNull("Company table list must not be null", companyTables);
        assertEquals("Company table list must have 1 element", 1, 
            companyTables.length);
        assertEquals("Unexpected Company table", "COMPANY", 
                companyTables[0].getName());
        assertEquals("Unexpected Company primary table", "COMPANY", 
                company.getPrimaryMappingTable().getName());
        
        // access fields
         
        RuntimeMappingField idField = company.getMappingField("companyid");
        assertNotNull("missing mapping field for Company.companyid", 
            idField);
        assertEquals("unexpected field name", "companyid", idField.getName());
        assertSame("unexpected mapping field instance for field number 1",
            idField, company.getMappingField(1));
        RuntimeMappingField deptField = company.getMappingField("departments");
        assertNotNull("missing mapping field for Company.departments", 
            deptField);
        assertEquals("unexpected field name", "departments", deptField.getName());
        assertSame("unexpected mapping field instance for field number 2",
            deptField, company.getMappingField(2));
        RuntimeMappingField foundedField = company.getMappingField("founded");
        assertNotNull("missing mapping field for Comany.founded", foundedField);
        assertEquals("unexpected field name", "founded", foundedField.getName());
        assertSame("unexpected mapping field instance for field number 3",
            foundedField, company.getMappingField(3));
        RuntimeMappingField nameField = company.getMappingField("name");
        assertNotNull("missing mapping field for Comany.name", nameField);
        assertEquals("unexpected field name", "name", nameField.getName());
        assertSame("unexpected mapping field instance for field number 4",
            nameField, company.getMappingField(4));

        
        MappingField[] pkFields = company.getPrimaryKeyMappingFields();
        assertNotNull("Company pk field list must not be null", pkFields);
        assertEquals("Company pk field list must have 1 element", 1, 
            pkFields.length);
        assertEquals("unexpected Company pk field", "companyid", 
            pkFields[0].getName());
        
        MappingField[] dfgFields = company.getDefaultFetchGroupMappingFields();
        assertNotNull("Company dfg field list must not be null", dfgFields);
        assertEquals("Company dfg field list must have 2 elements", 2, 
            dfgFields.length);
        assertEquals("unexpected Department dfg field", "founded", 
            dfgFields[0].getName());
        assertEquals("unexpected Department dfg field", "name", 
            dfgFields[1].getName());

        // access relationships
         
        MappingRelationship depsRel = deptField.getMappingRelationship();
        assertNotNull("missing mapping relationship for Company.departments", 
            depsRel);

        // access columns
        
        ColumnElement[] idCols = idField.getColumns();
        assertNotNull("companyid column list must not be null", idCols);
        assertEquals("companyid columns list must have 1 element", 1, 
            idCols.length);
        assertEquals("unexpected column of companyid field", "COMPANYID", 
            idCols[0].getName().getName());
        ColumnElement[] nameCols = nameField.getColumns();
        assertNotNull("name column list must not be null", nameCols);
        assertEquals("name columns list must have 1 element", 1, 
            nameCols.length);
        assertEquals("unexpected column of name field", "NAME", 
            nameCols[0].getName().getName());
        ColumnElement[] foundedCols = foundedField.getColumns();
        assertNotNull("founded column list must not be null", foundedCols);
        assertEquals("founded columns list must have 1 element", 1, 
            foundedCols.length);
        assertEquals("unexpected column of founded field", "FOUNDED", 
            foundedCols[0].getName().getName());

        // access column pairs

        MappingReferenceKey depsRefKey = 
            depsRel.getMappingReferenceKey(MappingRelationship.USAGE_ELEMENT);
        ColumnPairElement[] depsColPairs = depsRefKey.getColumnPairs();
        assertNotNull("departments column pair list must not be null", 
            depsColPairs);
        assertEquals("departments column pair list must have 1 element", 1, 
            depsColPairs.length);
        assertEquals("unexpected local column of departments field", "COMPANYID", 
            depsColPairs[0].getLocalColumn().getName().getName());
        assertEquals("unexpected referenced column of departments field", 
            "COMPANYID", 
             depsColPairs[0].getReferencedColumn().getName().getName());
        assertFalse("departments doesn't use join table", 
            depsRel.usesJoinTable());
    }

    /** */
    private void runTestDepartment() {
        
        // access mapping class
        
        RuntimeMappingClass dept = mappingModel.getMappingClass(DEPT_CLASSNAME);
        assertNotNull("missing mapping class for Department", dept);
        assertEquals("unexpected class name", DEPT_CLASSNAME, dept.getName());
        assertNotNull("missing SchemaElement for Department mapping", 
            dept.getDatabaseSchema());
        assertSame("Mapping class returns unexpected Department class instance",
            Department.class, dept.getJavaClass());
        
        // access tables
        
        MappingTable[] deptTables = dept.getMappingTables();
        assertNotNull("Department table list must not be null", deptTables);
        assertEquals("Department table list must have 1 element", 1, 
            deptTables.length);
        assertEquals("Unexpected Department table", "DEPARTMENT", 
            deptTables[0].getName());
        assertEquals("Unexpected Department primary table", "DEPARTMENT", 
            dept.getPrimaryMappingTable().getName());
        
        // access fields
         
        RuntimeMappingField companyField = dept.getMappingField("company");
        assertNotNull("missing mapping field for Department.company", 
            companyField);
        assertEquals("unexpected field name", "company", companyField.getName());
        assertSame("unexpected mapping field instance for field number 0",
            companyField, dept.getMappingField(0));
        RuntimeMappingField deptidField = dept.getMappingField("deptid");
        assertNotNull("missing mapping field for Department.name", deptidField);
        assertEquals("unexpected field name", "deptid", deptidField.getName());
        assertSame("unexpected mapping field instance for field number 1",
            deptidField, dept.getMappingField(1));
        RuntimeMappingField nameField = dept.getMappingField("name");
        assertNotNull("missing mapping field for Department.name", nameField);
        assertEquals("unexpected field name", "name", nameField.getName());
        assertSame("unexpected mapping field instance for field number 5",
            nameField, dept.getMappingField(5));
        
        MappingField[] pkFields = dept.getPrimaryKeyMappingFields();
        assertNotNull("Department pk field list must not be null", pkFields);
        assertEquals("Department pk field list must have 1 element", 1, 
            pkFields.length);
        assertEquals("unexpected Department pk field", "deptid", 
            pkFields[0].getName());
        
        MappingField[] dfgFields = dept.getDefaultFetchGroupMappingFields();
        assertNotNull("Department dfg field list must not be null", dfgFields);
        assertEquals("Department dfg field list must have 1 element", 1, 
            dfgFields.length);
        assertEquals("unexpected Department dfg field", "name", 
            dfgFields[0].getName());
        
        // access relationships
         
        MappingRelationship companyRel = companyField.getMappingRelationship();
        assertNotNull("missing mapping relationship for Department.company", 
            companyRel);

        // access columns
        
        ColumnElement[] deptidCols = deptidField.getColumns();
        assertNotNull("deptid column list must not be null", deptidCols);
        assertEquals("deptid columns list must have 1 element", 1, 
            deptidCols.length);
        assertEquals("unexpected column of deptid field", "DEPTID", 
            deptidCols[0].getName().getName());
        ColumnElement[] nameCols = nameField.getColumns();
        assertNotNull("name column list must not be null", nameCols);
        assertEquals("name columns list must have 1 element", 1, 
            nameCols.length);
        assertEquals("unexpected column of name field", "NAME", 
            nameCols[0].getName().getName());

        // access column pairs

        MappingReferenceKey companyRefKey = 
            companyRel.getMappingReferenceKey(MappingRelationship.USAGE_REFERENCE);
        ColumnPairElement[] companyColPairs = companyRefKey.getColumnPairs();
        assertNotNull("company column pair list must not be null", 
            companyColPairs);
        assertEquals("company column pair list must have 1 element", 1, 
            companyColPairs.length);
        assertEquals("unexpected local column of company field", "COMPANYID", 
            companyColPairs[0].getLocalColumn().getName().getName());
        assertEquals("unexpected referenced column of company field", "COMPANYID", 
            companyColPairs[0].getReferencedColumn().getName().getName());
        assertFalse("company doesn't use join table", 
            companyRel.usesJoinTable());

    }
    
    /** */
    private void runTestProject() {
                
        // access mapping class
        
        RuntimeMappingClass proj = mappingModel.getMappingClass(PROJ_CLASSNAME);
        assertNotNull("missing mapping class for Project", proj);
        assertEquals("unexpected class name", PROJ_CLASSNAME, proj.getName());
        assertNotNull("missing SchemaElement for Project mapping", 
            proj.getDatabaseSchema());
        assertSame("Mapping class returns unexpected Project class instance",
            Project.class, proj.getJavaClass());
        
        // access tables
        
        MappingTable[] projTables = proj.getMappingTables();
        assertNotNull("Project table list must not be null", projTables);
        assertEquals("Project table list must have 1 element", 1, 
            projTables.length);
        assertEquals("Unexpected Project table", "PROJECT", 
                projTables[0].getName());
        assertEquals("Unexpected Project primary table", "PROJECT", 
                proj.getPrimaryMappingTable().getName());
        
        // access fields
         
        RuntimeMappingField projidField = proj.getMappingField("projid");
        assertNotNull("missing mapping field for Project.name", projidField);
        assertEquals("unexpected field name", "projid", projidField.getName());
        assertSame("unexpected mapping field instance for field number 3",
            projidField, proj.getMappingField(3));
        RuntimeMappingField nameField = proj.getMappingField("name");
        assertNotNull("missing mapping field for Project.name", nameField);
        assertEquals("unexpected field name", "name", nameField.getName());
        assertSame("unexpected mapping field instance for field number 2",
            nameField, proj.getMappingField(2));
        RuntimeMappingField budgetField = proj.getMappingField("budget");
        assertNotNull("missing mapping field for Project.budget", budgetField);
        assertEquals("unexpected field budget", "budget", budgetField.getName());
        assertSame("unexpected mapping field instance for field number 0",
            budgetField, proj.getMappingField(0));
        
        MappingField[] pkFields = proj.getPrimaryKeyMappingFields();
        assertNotNull("Project pk field list must not be null", pkFields);
        assertEquals("Project pk field list must have 1 element", 1, 
            pkFields.length);
        assertEquals("unexpected Project pk field", "projid", 
            pkFields[0].getName());
        
        MappingField[] dfgFields = proj.getDefaultFetchGroupMappingFields();
        assertNotNull("Project dfg field list must not be null", dfgFields);
        assertEquals("Project dfg field list must have 2 elements", 2, 
            dfgFields.length);
        List dfgNames = Arrays.asList(new String[] {"name", "budget"}); 
        if (!dfgNames.contains(dfgFields[0].getName()))
            fail("unexpected Project dfg field " + dfgFields[0].getName());
        if (!dfgNames.contains(dfgFields[1].getName()))
            fail("unexpected Project dfg field " + dfgFields[0].getName());
         
        // access columns
        
        ColumnElement[] projidCols = projidField.getColumns();
        assertNotNull("projid column list must not be null", projidCols);
        assertEquals("projid columns list must have 1 element", 1, 
            projidCols.length);
        assertEquals("unexpected column of projid field", "PROJID", 
            projidCols[0].getName().getName());
        
        ColumnElement[] nameCols = nameField.getColumns();
        assertNotNull("name column list must not be null", nameCols);
        assertEquals("name columns list must have 1 element", 1, 
            nameCols.length);
        assertEquals("unexpected column of name field", "NAME", 
            nameCols[0].getName().getName());
        
        ColumnElement[] budgetCols = budgetField.getColumns();
        assertNotNull("budget column list must not be null", budgetCols);
        assertEquals("budget columns list must have 1 element", 1, 
            budgetCols.length);
        assertEquals("unexpected column of budget field", "BUDGET", 
            budgetCols[0].getName().getName());
    
    }

    /** */
    private void runTestShortNames() {
        String msg = "Methods getJDOClassForShortName and getJDOClass should return same instance";
        
        assertSame(msg, jdoModel.getJDOClassForShortName("Company"), 
                   jdoModel.getJDOClass(COMPANY_CLASSNAME));
        assertSame(msg, jdoModel.getJDOClassForShortName("Department"), 
                   jdoModel.getJDOClass(DEPT_CLASSNAME));
        assertSame(msg, jdoModel.getJDOClassForShortName("Project"), 
                   jdoModel.getJDOClass(PROJ_CLASSNAME));
    }

    /** */
    private void runTestProperties() {
        // access Department JDOClass which causes package.jdo to be read
        JDOClass dept = jdoModel.getJDOClass(DEPT_CLASSNAME);
        
        // Department.name
        JDOField nameField = dept.getField("name");
        try {
            dept.removeDeclaredMember(nameField);
            assertNull("removeDeclaredMember did not remove field name",
                       dept.getField("name"));
            JDOProperty nameProp = dept.createJDOProperty("name");
            assertNotNull("createJDOProperty should not return null", nameProp);
            assertEquals("unexpected name of property", "name", nameProp.getName());
            assertTrue("expected managed property", nameProp.isManaged());
            assertEquals("unexpected field number", 5, nameProp.getRelativeFieldNumber());
        }
        catch(ModelException ex) {
            throw new RuntimeException("Problem while patching JDOModel", ex);
        }
    }
    
    // ===== print support =====
    
    /** */
    private void printJDOModel(JDOModel jdoModel) {

        JDOClass[] declaredClasses = jdoModel.getDeclaredClasses();
        System.out.println("JDOModel declares ");
        for (int i = 0; i < declaredClasses.length; i++)
            System.out.println("    " + declaredClasses[i]);
        System.out.println();

        JDOClass company = jdoModel.getJDOClass(COMPANY_CLASSNAME);
        System.out.println("Company JDOClass = " + company);
        PrintSupport.printJDOClass(company);
        JDOClass department = jdoModel.getJDOClass(DEPT_CLASSNAME);
        System.out.println("Department JDOClass = " + department);
        PrintSupport.printJDOClass(department);
        JDOClass project = jdoModel.getJDOClass(PROJ_CLASSNAME);
        System.out.println("Project JDOClass = " + project);
        PrintSupport.printJDOClass(project);
    }
        
    /** Prints the specified RuntimeMappingModel to System.out. */
    private void printMappingModel(RuntimeMappingModel mappingModel) {
        RuntimeMappingClass[] declaredClasses = 
            mappingModel.getMappingClasses();
        System.out.println("MappingModel declares ");
        for (int i = 0; i < declaredClasses.length; i++)
            System.out.println("    " + declaredClasses[i]);
        System.out.println();
        
        for (int i = 0; i < declaredClasses.length; i++)
            printMappingClass(declaredClasses[i]);
        
    }
    
    /** Prints the specified RuntimeMappingClass to System.out. */
    private void printMappingClass(RuntimeMappingClass mappingClass) {
        System.out.println("--> MappingClass");
        System.out.println("    name          = " + mappingClass.getName());
        System.out.println("    pk fields     = " + 
            Arrays.asList(mappingClass.getPrimaryKeyMappingFields()));
        System.out.println("    dfg fields    = " + 
            Arrays.asList(mappingClass.getDefaultFetchGroupMappingFields()));
        System.out.println("    tables        = " + 
            Arrays.asList(mappingClass.getMappingTables()));
        System.out.println("    primary table = " + 
            mappingClass.getPrimaryMappingTable());
        
        RuntimeMappingField[] mappingFields = mappingClass.getMappingFields();
        for (int i = 0; i < mappingFields.length; i++)
            printMappingField(mappingFields[i]);
        
        System.out.println("<-- MappingClass");
    }
    
    /** Prints the specified RuntimeMappingField to System.out. */
    private void printMappingField(RuntimeMappingField mappingField) {
        System.out.println("    --> MappingField");
        System.out.println("        name    = " + mappingField.getName());
        System.out.println("        columns = " + 
            Arrays.asList(mappingField.getColumns()));

        MappingRelationship rel = mappingField.getMappingRelationship();
        if (rel != null)
            printMappingRelationship(rel);

        System.out.println("    <-- MappingField");
    }
 
    /** Prints the specified MappingRelationship to System.out. */
    private void printMappingRelationship(MappingRelationship mappingRel) {
        System.out.println("    --> MappingRelationship");
        System.out.println("        name    = " + mappingRel.getName());
        System.out.println("        join key = ");
        printMappingReferenceKey(mappingRel.getMappingReferenceKey(
            MappingRelationship.USAGE_JOIN));
        System.out.println("        reference key = ");
        printMappingReferenceKey(mappingRel.getMappingReferenceKey(
            MappingRelationship.USAGE_REFERENCE));
        System.out.println("        element key = ");
        printMappingReferenceKey(mappingRel.getMappingReferenceKey(
            MappingRelationship.USAGE_ELEMENT));

        System.out.println("    <-- MappingRelationship");
    }

    /** Prints the specified MappingReferenceKey to System.out. */
    private void printMappingReferenceKey(MappingReferenceKey mappingRefKey) {
        if (mappingRefKey != null) {
            System.out.println("    --> MappingReferenceKey");
            System.out.println("        name    = " + mappingRefKey.getName());
            System.out.println("        column pairs = " + 
                Arrays.asList(mappingRefKey.getColumnPairs()));

            System.out.println("    <-- MappingReferenceKey");
        } else System.out.println("null");
    }

    // ============== JUnit methods ===================
    
    /** */
    protected void setUp() throws Exception { 
        initModels();
    }
    
    /** */
    protected void tearDown() throws Exception { }
    
    /** */
    public static Test suite() {
        return new TestSuite(CompanyMappingModelTest.class);
    }
}

