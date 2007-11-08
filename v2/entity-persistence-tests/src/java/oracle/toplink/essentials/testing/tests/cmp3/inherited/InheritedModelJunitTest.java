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
package oracle.toplink.essentials.testing.tests.cmp3.inherited;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Blue;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inherited.InheritedTableManager;
 
public class InheritedModelJunitTest extends JUnitTestCase {
    private static Integer m_blueId;
    
    public InheritedModelJunitTest() {
        super();
    }
    
    public InheritedModelJunitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("InheritedModelJunitTest");
        suite.addTest(new InheritedModelJunitTest("testCreateBlue"));
        suite.addTest(new InheritedModelJunitTest("testReadBlue"));

        return new TestSetup(suite) {
        
            protected void setUp() {               
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                new InheritedTableManager().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreateBlue() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Blue blue = new Blue();
            blue.setAlcoholContent(5.3);
            em.persist(blue);
            m_blueId = blue.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            fail("An exception was caught during create operation: [" + e.getMessage() + "]");
        }
        
        em.close();
    }
    
    public void testReadBlue() {
        Blue blue = (Blue) createEntityManager().find(Blue.class, m_blueId);
        
        assertTrue("Error on reading back a Blue beer", blue != null);
    }
    
    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
