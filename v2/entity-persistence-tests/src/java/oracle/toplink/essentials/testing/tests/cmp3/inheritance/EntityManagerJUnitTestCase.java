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
package oracle.toplink.essentials.testing.tests.cmp3.inheritance;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.InheritanceTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.SportsCar;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Car;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Person;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Engineer;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.ComputerPK;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Desktop;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Laptop;
import oracle.toplink.essentials.sessions.DatabaseSession;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import javax.persistence.EntityManager;

public class EntityManagerJUnitTestCase extends JUnitTestCase {

    public EntityManagerJUnitTestCase() {
        super();
    }

    public EntityManagerJUnitTestCase(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EntityManagerJUnitTestCase.class);

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

    // gf issue 1356 - persisting a polymorphic relationship throws a NPE.
    // The order of persist operations is important for this test.
    public void testPersistPolymorphicRelationship() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Person p = new Person();
        p.setName("Evil Knievel");
        
        Car c = new SportsCar();
        c.setDescription("Ferrari");
        ((SportsCar) c).setMaxSpeed(200);
        p.setCar(c);
        
        try {
            em.persist(c);
            em.persist(p);
            em.getTransaction().commit();
        
        } catch (Exception exception ) {
            fail("Error persisting polymorphic relationship: " + exception.getMessage());
        } finally {
            em.close();
        }
    }

    // test if we can associate with a subclass entity 
    // whose root entity has EmbeddedId in Joined inheritance strategy
    // Issue: GF#1153 && GF#1586 (desktop amendment)
    public void testAssociationWithEmbeddedIdSubclassEntityInJoinedStrategy() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        try {
            Engineer engineer = new Engineer();
            em.persist(engineer);
        
            ComputerPK laptopPK = new ComputerPK("Dell", 10001);    
            Laptop laptop = em.find(Laptop.class, laptopPK);
            if (laptop == null){
                laptop = new Laptop(laptopPK);
                em.persist(laptop);
            }
        
            ComputerPK desktopPK = new ComputerPK("IBM", 10002);    
            Desktop desktop = em.find(Desktop.class, desktopPK);
            if (desktop == null){
                desktop = new Desktop(desktopPK);
                em.persist(desktop);
            }
            
            // associate many-to-many relationships
            engineer.getLaptops().add(laptop);
            engineer.getDesktops().add(desktop);
            
            em.getTransaction().commit();
        } catch(RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            throw ex;
        } finally {
            em.close();
        }
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
