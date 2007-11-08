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
package oracle.toplink.essentials.testing.tests;

import junit.framework.TestSuite;
import junit.framework.Test;

import oracle.toplink.essentials.testing.tests.cmp3.advanced.AdvancedJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.NamedNativeQueryJUnitTest;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.CallbackEventJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.EntityManagerJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.SQLResultSetMappingTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.JoinedAttributeAdvancedJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.ReportQueryMultipleReturnTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.ReportQueryAdvancedJUnitTest;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.ExtendedPersistenceContextJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.ReportQueryConstructorExpressionTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.OptimisticConcurrencyJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.advanced.UpdateAllQueryAdvancedJunitTest;

import oracle.toplink.essentials.testing.tests.cmp3.advanced.compositepk.AdvancedCompositePKJunitTest;

import oracle.toplink.essentials.testing.tests.cmp3.inheritance.LifecycleCallbackJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inheritance.DeleteAllQueryInheritanceJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inheritance.JoinedAttributeInheritanceJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inheritance.EntityManagerJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.inheritance.MixedInheritanceJUnitTestCase;

import oracle.toplink.essentials.testing.tests.cmp3.inherited.OrderedListJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inherited.InheritedModelJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inherited.InheritedCallbacksJunitTest;
import oracle.toplink.essentials.testing.tests.cmp3.inherited.EmbeddableSuperclassJunitTest;

import oracle.toplink.essentials.testing.tests.cmp3.relationships.EMQueryJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.relationships.ExpressionJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.relationships.UniAndBiDirectionalMappingTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.relationships.VirtualAttributeTestSuite;

import oracle.toplink.essentials.testing.tests.cmp3.validation.ValidationTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.validation.QueryParameterValidationTestSuite;

import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLUnitTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLSimpleTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLValidationTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLComplexAggregateTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLDateTimeTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLParameterTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLExamplesTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLModifyTestSuite;

import oracle.toplink.essentials.testing.tests.cmp3.xml.EntityMappingsJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.cmp3.ddlgeneration.DDLGenerationJUnitTestSuite;
import oracle.toplink.essentials.testing.tests.ejb.ejbqltesting.JUnitEJBQLInheritanceTestSuite;

public class FullRegressionTestSuite extends TestSuite{
    
    public static Test suite() {
        TestSuite fullSuite = new TestSuite();
        fullSuite.setName("FullRegressionTestSuite");
        
        // Advanced model
        fullSuite.addTest(NamedNativeQueryJUnitTest.suite());
        fullSuite.addTest(CallbackEventJUnitTestSuite.suite());
        fullSuite.addTest(EntityManagerJUnitTestSuite.suite());
        fullSuite.addTest(SQLResultSetMappingTestSuite.suite());
        fullSuite.addTest(JoinedAttributeAdvancedJunitTest.suite());
        fullSuite.addTest(ReportQueryMultipleReturnTestSuite.suite());
        fullSuite.addTest(ReportQueryAdvancedJUnitTest.suite());
        fullSuite.addTest(ExtendedPersistenceContextJUnitTestSuite.suite());
        fullSuite.addTest(ReportQueryConstructorExpressionTestSuite.suite());
        fullSuite.addTest(OptimisticConcurrencyJUnitTestSuite.suite());
        fullSuite.addTest(AdvancedJunitTest.suite());
        fullSuite.addTest(UpdateAllQueryAdvancedJunitTest.suite());
        
        // Advanced - compositepk model
        fullSuite.addTest(AdvancedCompositePKJunitTest.suite());

        // DataTypes model
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.datatypes.NullBindingJUnitTestCase.suite());
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.datatypes.arraypks.PrimitiveArrayPKCachingJUnitTestCase.suite());

        // DateTime model
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.datetime.NullBindingJUnitTestCase.suite());

        // Lob model
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.lob.LobJUnitTestCase.suite());

        // Inheritance model
        fullSuite.addTest(LifecycleCallbackJunitTest.suite());
        fullSuite.addTest(DeleteAllQueryInheritanceJunitTest.suite());
        fullSuite.addTest(EntityManagerJUnitTestCase.suite());
        fullSuite.addTest(MixedInheritanceJUnitTestCase.suite());
        fullSuite.addTest(JoinedAttributeInheritanceJunitTest.suite());
        
        // Inherited model
        fullSuite.addTest(OrderedListJunitTest.suite());
        fullSuite.addTest(InheritedModelJunitTest.suite());
        fullSuite.addTest(InheritedCallbacksJunitTest.suite());
        fullSuite.addTest(EmbeddableSuperclassJunitTest.suite());
        
        // Relationship model
        fullSuite.addTestSuite(EMQueryJUnitTestSuite.class);
        fullSuite.addTestSuite(ExpressionJUnitTestSuite.class);
        fullSuite.addTest(VirtualAttributeTestSuite.suite());
        fullSuite.addTest(ValidationTestSuite.suite());
        fullSuite.addTest(QueryParameterValidationTestSuite.suite());
        fullSuite.addTest(UniAndBiDirectionalMappingTestSuite.suite());

        // FieldAccess relationship model
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.fieldaccess.relationships.UniAndBiDirectionalMappingTestSuite.suite());
        fullSuite.addTestSuite(oracle.toplink.essentials.testing.tests.cmp3.fieldaccess.relationships.ExpressionJUnitTestSuite.class);
        fullSuite.addTest(oracle.toplink.essentials.testing.tests.cmp3.fieldaccess.relationships.VirtualAttributeTestSuite.suite());
        
        // EJBQL testing model
        fullSuite.addTest(JUnitEJBQLUnitTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLSimpleTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLComplexTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLInheritanceTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLValidationTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLComplexAggregateTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLDateTimeTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLParameterTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLExamplesTestSuite.suite());
        fullSuite.addTest(JUnitEJBQLModifyTestSuite.suite());
	        
        // XML model
        fullSuite.addTest(EntityMappingsJUnitTestSuite.suite());

        // DDL model
        fullSuite.addTest(DDLGenerationJUnitTestSuite.suite());

        return fullSuite;
    }
    

}
