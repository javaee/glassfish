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


package oracle.toplink.essentials.testing.tests.cmp3.xml.merge;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.advanced.EntityMappingsMergeAdvancedJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.relationships.EntityMappingsMergeRelationshipsJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.incompletemappings.nonowning.EntityMappingsIncompleteNonOwningJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.incompletemappings.owning.EntityMappingsIncompleteOwningJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.inherited.EntityMappingsMergeInheritedJUnitTestCase;
 
/**
 * JUnit test suite for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsMergeJUnitTestSuite extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("Merge Tests");

        suite.addTest(EntityMappingsMergeAdvancedJUnitTestCase.suite());
        suite.addTest(EntityMappingsMergeRelationshipsJUnitTestCase.suite());
        suite.addTest(EntityMappingsIncompleteNonOwningJUnitTestCase.suite());
        suite.addTest(EntityMappingsIncompleteOwningJUnitTestCase.suite());
        suite.addTest(EntityMappingsMergeInheritedJUnitTestCase.suite());

        return suite;
    }
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}

