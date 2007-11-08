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
// Copyright (c) 1998, 2006, Oracle. All rights reserved.  
package oracle.toplink.essentials.testing.tests.cmp3.inheritance;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.InheritanceTableCreator;

import oracle.toplink.essentials.testing.models.cmp3.inheritance.TireRating;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.MudTireInfo;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.RockTireInfo;

import oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.TireInfoListener;

import oracle.toplink.essentials.sessions.DatabaseSession;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import javax.persistence.EntityManager;

public class MixedInheritanceJUnitTestCase extends JUnitTestCase {
    private static int mudTireId;
    private static int rockTireId;
    
    public MixedInheritanceJUnitTestCase() {
        super();
    }

    public MixedInheritanceJUnitTestCase(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("MixedInheritanceJUnitTestCase");
        
        suite.addTest(new MixedInheritanceJUnitTestCase("testCreateNewMudTire"));
        suite.addTest(new MixedInheritanceJUnitTestCase("testCreateNewRockTire"));
        
        suite.addTest(new MixedInheritanceJUnitTestCase("testReadNewMudTire"));
        suite.addTest(new MixedInheritanceJUnitTestCase("testReadNewRockTire"));

        return new TestSetup(suite) {

            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new InheritanceTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }

    public void testCreateNewMudTire() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        MudTireInfo mudTire = new MudTireInfo();
        mudTire.setName("Goodyear Mud Tracks");
        mudTire.setCode("MT-674-A4");
        mudTire.setPressure(new Integer(100));
        mudTire.setTreadDepth(3);
        
        TireRating tireRating = new TireRating();
        tireRating.setRating("Excellent");
        tireRating.setComments("Tire outperformed all others in adverse conditions");
        
        mudTire.setTireRating(tireRating);
        
        try {
            int prePersistCountBefore = TireInfoListener.PRE_PERSIST_COUNT;
            em.persist(mudTire);
            mudTireId = mudTire.getId();
            em.getTransaction().commit();
            int prePersistCountAfter = TireInfoListener.PRE_PERSIST_COUNT;
            
            int perPersistCountTotal = prePersistCountAfter - prePersistCountBefore;
            assertTrue("The pre persist method was called more than once (" + perPersistCountTotal + ")", perPersistCountTotal == 1);
        } catch (Exception exception ) {
            fail("Error persisting mud tire: " + exception.getMessage());
        } finally {
            em.close();
        }
    }
    
    public void testCreateNewRockTire() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        RockTireInfo rockTire = new RockTireInfo();
        rockTire.setName("Goodyear Mud Tracks");
        rockTire.setCode("AE-678");
        rockTire.setPressure(new Integer(100));
        rockTire.setGrip(RockTireInfo.Grip.SUPER);
        
        try {
            em.persist(rockTire);
            rockTireId = rockTire.getId();
            em.getTransaction().commit();
        } catch (Exception exception ) {
            fail("Error persisting rock tire: " + exception.getMessage());
        } finally {
            em.close();
        }
    }
    
    public void testReadNewMudTire() {
        assertNotNull("The new mud tire info could not be read back.", createEntityManager().find(MudTireInfo.class, mudTireId));
    }
    
    public void testReadNewRockTire() {
        assertNotNull("The new rock tire info could not be read back.", createEntityManager().find(RockTireInfo.class, rockTireId));
    }
}
