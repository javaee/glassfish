package oracle.toplink.essentials.testing.tests.cmp3.xml.merge.advanced;

import java.util.Map;
import java.util.Vector;

import junit.framework.*;
import junit.extensions.TestSetup;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.descriptors.InheritancePolicy;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.advanced.*;

/**
 * JUnit test case(s) for merging xml with metadata-complete="true" and annotations.
 * See orm-annotation-merge-advanced-entity-mappings.xml
 * Values defined in xml often (but not always) prefixed with "XML_MERGE_"
 * Values defined in annotations often (but not always) prefixed with "ANN_MERGE_"
 * Currently no tables are created, so the testing is limited to examining descriptors.
 */
public class EntityMappingsMergeAdvancedJUnitTestCase extends JUnitTestCase {
    
    static String packageName = "oracle.toplink.essentials.testing.models.cmp3.xml.merge.advanced.";
    static String packageToCompareName = "oracle.toplink.essentials.testing.models.cmp3.advanced.";
    static String[] classNames = {"Address", "Employee", "EmploymentPeriod", "LargeProject", "PhoneNumber", "Project", "SmallProject"};
    
    public EntityMappingsMergeAdvancedJUnitTestCase() {
        super();
    }
    
    public EntityMappingsMergeAdvancedJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EntityMappingsMergeAdvancedJUnitTestCase.class, "Advanced Model");

        return new TestSetup(suite) {
            
            protected void setUp(){               
            }
        
            protected void tearDown() {
            }
        };
    }
    
	public void testInheritanceDiscriminatorFieldValue() {
        InheritancePolicy projectInheritancePolicy = getServerSession().getDescriptor(Project.class).getInheritancePolicy();
        
        // defined in xml
        DatabaseField classIndicatorField = projectInheritancePolicy.getClassIndicatorField();
        String classIndicatorFieldName = classIndicatorField.getName();
        if(!classIndicatorFieldName.equals("XML_MERGE_PROJ_TYPE")) {
            fail("Wrong classIndicatorField name '"+classIndicatorFieldName+"'");
        }
        Class classIndicatorFieldType = classIndicatorField.getType();
        if(!classIndicatorFieldType.equals(String.class)) {
            fail("Wrong classIndicatorField type '"+classIndicatorFieldType.getName()+"'");
        }
        // defaulted in xml
        String classIndicatorFieldTableName = classIndicatorField.getTableName();
        if(!classIndicatorFieldTableName.equals("XMLMERGEPROJECT")) {
            fail("Wrong classIndicatorField table '"+classIndicatorFieldTableName+"'");
        }
                
        Map classNameIndicators = projectInheritancePolicy.getClassNameIndicatorMapping();
        // defined in xml
        String projectIndicator = (String)classNameIndicators.get(packageName + "Project");
        if(!projectIndicator.equals("XML_MERGE_P")) {
            fail("Wrong classIndicatorField value for Project '"+projectIndicator+"'");
        }
        // defaulted in annotations
        String smallProjectIndicator = (String)classNameIndicators.get(packageName + "SmallProject");
        if(!smallProjectIndicator.equals("SmallProject")) {
            fail("Wrong classIndicatorField value for SmallProject '"+smallProjectIndicator+"'");
        }
        String largeProjectIndicator = (String)classNameIndicators.get(packageName + "LargeProject");
        if(!largeProjectIndicator.equals("LargeProject")) {
            fail("Wrong classIndicatorField value for LargeProject '"+largeProjectIndicator+"'");
        }
    }

	// The test compares the mappings's types for each class with the corresponding class from packageToCompare.
    // The test should be altered accordingly in case the two classes are no longer use the same mappings types.
    public void testMappingsTypes() throws ClassNotFoundException {
        Map descriptors = getServerSession().getDescriptors();
        String errorMsg = "";
        for (int i=0; i<classNames.length; i++) {
            String classErrorMsg = "";
            String className = packageName + classNames[i];
            String classToCompareName = packageToCompareName + classNames[i];
            Class cls = Class.forName(className);
            Class clsToCompare = Class.forName(classToCompareName);
            ClassDescriptor desc = (ClassDescriptor)descriptors.get(cls);
            ClassDescriptor descToCompare = (ClassDescriptor)descriptors.get(clsToCompare);
            Vector mappings = desc.getMappings();
            Vector mappingsToCompare = descToCompare.getMappings();
            if(mappings.size() != mappingsToCompare.size()) {
                classErrorMsg = classErrorMsg +  "Number of mappings is different; ";
                continue;
            }
            for(int j=0; j<mappings.size(); j++) {
                DatabaseMapping mapping = (DatabaseMapping)mappings.elementAt(j);
                String attributeName = mapping.getAttributeName();
                DatabaseMapping mappingToCompare = descToCompare.getMappingForAttributeName(attributeName);
                if(!mapping.getClass().equals(mappingToCompare.getClass())) {
                    classErrorMsg = classErrorMsg + "attribute "+attributeName+" - mappings of different types; ";
                }
            }
            if(classErrorMsg.length() > 0) {
                errorMsg = errorMsg + "class " + classNames[i] +": " + classErrorMsg;
            }
        }
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

	public static void main(String[] args) {
         junit.textui.TestRunner.run(EntityMappingsMergeAdvancedJUnitTestCase.suite());
    }
}
