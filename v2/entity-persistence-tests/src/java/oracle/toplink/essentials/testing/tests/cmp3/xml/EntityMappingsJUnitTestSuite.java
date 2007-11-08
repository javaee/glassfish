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


package oracle.toplink.essentials.testing.tests.cmp3.xml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import oracle.toplink.essentials.testing.tests.cmp3.xml.advanced.EntityMappingsAdvancedJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.inheritance.EntityMappingsInheritanceJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.inherited.EntityMappingsInheritedJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.relationships.EntityMappingsRelationshipsJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.relationships.unidirectional.EntityMappingsUnidirectionalRelationshipsJUnitTestCase;
import oracle.toplink.essentials.testing.tests.cmp3.xml.merge.EntityMappingsMergeJUnitTestSuite;
 
/**
 * JUnit test suite for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsJUnitTestSuite extends TestCase {
    public static Test suite() {
        TestSuite suite = new TestSuite("Entity Mappings JUnit Test Suite");

        suite.addTest(EntityMappingsAdvancedJUnitTestCase.suite());
        suite.addTest(EntityMappingsRelationshipsJUnitTestCase.suite());
        suite.addTest(EntityMappingsUnidirectionalRelationshipsJUnitTestCase.suite());
        suite.addTest(EntityMappingsInheritanceJUnitTestCase.suite());
        suite.addTest(EntityMappingsInheritedJUnitTestCase.suite());
        suite.addTest(EntityMappingsMergeJUnitTestSuite.suite());
        return suite;
    }
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}

