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
 * CompanyMappingModel.java
 *
 * Created on 15. April 2005, 13:58
 */

package com.sun.persistence.runtime.model;

import com.sun.persistence.api.model.mapping.MappingReferenceKey;
import java.util.HashMap;
import java.util.Map;

import com.sun.forte4j.modules.dbmodel.DBIdentifier;
import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.forte4j.modules.dbmodel.util.NameUtil;

import com.sun.org.apache.jdo.impl.model.java.runtime.jdk5.JDK5RuntimeJavaModelFactory;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOModel;

import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingRelationship;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModelFactory;
import com.sun.persistence.runtime.model.mapping.impl.RuntimeMappingModelImpl;
import com.sun.persistence.runtime.model.mapping.impl.RuntimeMappingModelFactoryImpl;

/**
 *
 * @author Michael Bouschen
 */
public class CompanyMappingModel extends RuntimeMappingModelImpl {
    
    /** The JavaModelFactory. */
    private static final JavaModelFactory javaModelFactory =
        JDK5RuntimeJavaModelFactory.getInstance();

    /** The MappingModelFactory. */
    private final static RuntimeMappingModelFactory mappingModelFactory =
        new CompanyMappingModelFactory();
    
    /** Company model package name. */
    public static final String COMPANY_PACKAGE = 
            "com.sun.org.apache.jdo.tck.pc.company.";
    
    public static final String DATABASE_ROOT;
    
    static {
        DATABASE_ROOT = COMPANY_PACKAGE.replace('.', '/') + "company";
    }

    /**
     * Tests can provide their own ClassLoader.  This map is our way of
     * ensuring that for a given ClassLoader, a model is initialized at most
     * once.
     */
    private static Map<ClassLoader, CompanyMappingModel> companyModels
        = new HashMap<ClassLoader, CompanyMappingModel>();
    
    /**
     * The company MappingModel instance corresponding to ClassLoader
     * most recently used in a call to getInstance.
     */
    private static CompanyMappingModel companyModel = null; 
    
    /** The dbschema */
    private SchemaElement dbSchema;
    
    /** */
    public static RuntimeMappingModel getInstance() {
        return getInstance(CompanyMappingModel.class.getClassLoader());
    }
    
    public static RuntimeMappingModel getInstance(ClassLoader classLoader) {
        companyModel = companyModels.get(classLoader);
        if (companyModel == null) {
            JavaModel javaModel = javaModelFactory.getJavaModel(classLoader);
            SchemaElement dbSchema
                = SchemaElement.forName(DATABASE_ROOT, classLoader);
            companyModel = (CompanyMappingModel) mappingModelFactory.
            getMappingModel(javaModel.getJDOModel(), null);
            companyModel.createMapping(dbSchema);
            companyModels.put(classLoader, companyModel);
        }
        return companyModel;
    }

    /** */
    public CompanyMappingModel(JDOModel jdoModel) {
        super(jdoModel);
    }
    
    /** */
    protected void createMapping(SchemaElement schemaElement) {
        this.dbSchema = schemaElement;
        createCompanyMapping();
        createDepartmentMapping();
        createProjectMapping();
    }

    /** */
    protected void createCompanyMapping() {
        try {
            MappingClass company = 
                createMappingClass(COMPANY_PACKAGE + "Company");
            // schema handling
            company.setDatabaseSchema(dbSchema);
            // table handling
            TableElement companyTable = 
                dbSchema.getTable(DBIdentifier.create("COMPANY"));    
            company.createPrimaryMappingTable(companyTable);
            // field handling
            MappingField idField = company.createMappingField("companyid");
            idField.addColumn(
                companyTable.getColumn(DBIdentifier.create("COMPANYID")));
            MappingField nameField = company.createMappingField("name");
            nameField.addColumn(
                companyTable.getColumn(DBIdentifier.create("NAME")));
            MappingField foundedField = company.createMappingField("founded");
            foundedField.addColumn(
                companyTable.getColumn(DBIdentifier.create("FOUNDED")));

            // relationship handling
            MappingField deptsField = company.createMappingField("departments");
            MappingRelationship depsRel = 
                deptsField.createMappingRelationship();
            MappingReferenceKey depsRefKey = depsRel.createMappingReferenceKey(
                MappingRelationship.USAGE_ELEMENT);
            String fullSchemaName = dbSchema.getName().getFullName();

            depsRefKey.addColumnPair(
                companyTable.getColumnPair(DBIdentifier.create(
                    NameUtil.getAbsoluteMemberName(fullSchemaName, 
                    "COMPANY.COMPANYID;DEPARTMENT.COMPANYID"))));
        }
        catch (ModelException ex) {
            throw new ModelFatalException(
                "Problem during CompanyMappingModel createCompanyMapping", ex);
        }
    }

    /** */
    protected void createDepartmentMapping() {
        try {
            MappingClass dept = 
                createMappingClass(COMPANY_PACKAGE + "Department");
            // schema handling
            dept.setDatabaseSchema(dbSchema);
            // table handling
            TableElement deptTable = 
                dbSchema.getTable(DBIdentifier.create("DEPARTMENT"));    
            dept.createPrimaryMappingTable(deptTable);
            // field handling
            MappingField deptidField = dept.createMappingField("deptid");
            deptidField.addColumn(
                deptTable.getColumn(DBIdentifier.create("DEPTID")));
            
            MappingField nameField = dept.createMappingField("name");
            nameField.addColumn(
                deptTable.getColumn(DBIdentifier.create("NAME")));

            // relationship handling
            MappingField companyField = dept.createMappingField("company");
            MappingRelationship companyRel = 
                companyField.createMappingRelationship();

            // this really should be have already been done by the 
            // jdo model set up but is not yet handled by the 
            // xml reader/properties implementation
            companyRel.getJDORelationship().setMappedBy(
                    companyRel.getJDORelationship().getRelatedJDOClass().
                    getField("departments").getRelationship());
            // end jdo model setup
            
            // comment the next 2 statements out to test that 
            // inverse mapping will be returned by getMappingReferenceKey
            // methods even without setting it explicitly
            MappingReferenceKey companyRefKey = 
                companyRel.createMappingReferenceKey(
                MappingRelationship.USAGE_REFERENCE);
            companyRefKey.setColumnPairs(
                companyModel.getMappingClass(COMPANY_PACKAGE + "Company").
                getMappingField("departments").getMappingRelationship().
                getMappingReferenceKey(MappingRelationship.USAGE_ELEMENT));
            // end inverse mapping code to comment
        }
        catch (ModelException ex) {
            throw new ModelFatalException(
                "Problem during CompanyMappingModel createDepartmentMapping", ex);
        }
    }
 
        /** */
    protected void createProjectMapping() {
        try {
            MappingClass project = 
                createMappingClass(COMPANY_PACKAGE + "Project");
            // schema handling
            project.setDatabaseSchema(dbSchema);
            // table handling
            TableElement projectTable = 
                dbSchema.getTable(DBIdentifier.create("PROJECT"));
            project.createPrimaryMappingTable(projectTable);
            // field handling
            MappingField projectidField = project.createMappingField("projid");
            projectidField.addColumn(
                projectTable.getColumn(DBIdentifier.create("PROJID")));
            
            MappingField nameField = project.createMappingField("name");
            nameField.addColumn(
                projectTable.getColumn(DBIdentifier.create("NAME")));
            
            MappingField budgetField = project.createMappingField("budget");
            budgetField.addColumn(
                projectTable.getColumn(DBIdentifier.create("BUDGET")));
        }
        catch (ModelException ex) {
            throw new ModelFatalException(
                "Problem during CompanyMappingModel createProjectMapping", ex);
        }
    }

    /** MappingModelFactory implementation for the company model. */
    static class CompanyMappingModelFactory extends RuntimeMappingModelFactoryImpl {
        /** */
        public RuntimeMappingModel createMappingModel(JDOModel jdoModel, Object key) {
            return new CompanyMappingModel(jdoModel);
        }
    }
}
