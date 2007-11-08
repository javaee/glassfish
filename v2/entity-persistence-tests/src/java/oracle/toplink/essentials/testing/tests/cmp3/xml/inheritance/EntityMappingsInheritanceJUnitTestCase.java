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
package oracle.toplink.essentials.testing.tests.cmp3.xml.inheritance;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;

import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;

import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.Boat;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.Bus;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.Company;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.SportsCar;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.InheritanceTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.InheritanceModelExamples;

import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener2;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.DefaultListener;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
 
/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsInheritanceJUnitTestCase extends JUnitTestCase {
    private static Number busId;
    private static Number boatId;
    private static Number sportsCarId;
    
    public EntityMappingsInheritanceJUnitTestCase() {
        super();
    }
    
    public EntityMappingsInheritanceJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Inheritance Model");
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testCreateFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testCreateBusFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testCreateNonFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testReadFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testReadNonFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testNamedNativeQueryOnSportsCar"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testUpdateBusFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testUpdateFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testUpdateNonFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testDeleteBusFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testDeleteFueledVehicle"));
        suite.addTest(new EntityMappingsInheritanceJUnitTestCase("testDeleteNonFueledVehicle"));
        
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
    
    public void testCreateBusFueledVehicle() {
        int prePersistBusCountBefore = Bus.PRE_PERSIST_COUNT;
        int postPersistBusCountBefore = Bus.POST_PERSIST_COUNT;
        
        int prePersistBusListenerCountBefore = BusListener.PRE_PERSIST_COUNT;
        int postPersistBusListenerCountBefore = BusListener.POST_PERSIST_COUNT;
        
        int prePersistBusListener2CountBefore = BusListener2.PRE_PERSIST_COUNT;
        int postPersistBusListener2CountBefore = BusListener2.POST_PERSIST_COUNT;
        
        int prePersistDefaultListenerCountBefore = DefaultListener.PRE_PERSIST_COUNT;
        int postPersistDefaultListenerCountBefore = DefaultListener.POST_PERSIST_COUNT;
        
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        Bus bus = new Bus();
        bus.setPassengerCapacity(new Integer(50));
        bus.setFuelCapacity(new Integer(175));
        bus.setDescription("OC Transpo Bus");
        bus.setFuelType("Diesel");
            
        try {
            em.persist(bus);
            busId = bus.getId();
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw ex;
        }
        
        assertFalse("The PrePersist callback method on Bus was not called.", prePersistBusCountBefore == Bus.PRE_PERSIST_COUNT);
        assertFalse("The PostPersist callback method on Bus was not called.", postPersistBusCountBefore == Bus.POST_PERSIST_COUNT);
        assertFalse("The PrePersist callback method on BusListener was not called.", prePersistBusListenerCountBefore == BusListener.PRE_PERSIST_COUNT);
        assertFalse("The PostPersist callback method on BusListener was not called.", postPersistBusListenerCountBefore == BusListener.POST_PERSIST_COUNT);
        assertFalse("The PrePersist callback method on BusListener2 was not called.", prePersistBusListener2CountBefore == BusListener2.PRE_PERSIST_COUNT);
        assertFalse("The PostPersist callback method on BusListener2 was not called.", postPersistBusListener2CountBefore == BusListener2.POST_PERSIST_COUNT);
        assertFalse("The PrePersist callback method on DefaultListener was not called.", prePersistDefaultListenerCountBefore == DefaultListener.PRE_PERSIST_COUNT);
        assertFalse("The PostPersist callback method on DefaultListener was not called.", postPersistDefaultListenerCountBefore == DefaultListener.POST_PERSIST_COUNT);

        assertTrue("An incorrect number of PrePersist notifications where made for the Bus object.", bus.prePersistCalledListenerCount() == 3);
        assertTrue("An incorrect number of PostPersist notifications where made for the Bus object.", bus.postPersistCalledListenerCount() == 3);
        
        assertTrue("The PrePersist events were not fired in the correct order.", bus.getPrePersistCalledListenerAt(0) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener2.class);
        assertTrue("The PrePersist events were not fired in the correct order.", bus.getPrePersistCalledListenerAt(1) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener3.class);
        assertTrue("The PrePersist events were not fired in the correct order.", bus.getPrePersistCalledListenerAt(2) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener.class);
        
        assertTrue("The PostPersist events were not fired in the correct order.", bus.getPostPersistCalledListenerAt(0) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener2.class);
        assertTrue("The PostPersist events were not fired in the correct order.", bus.getPostPersistCalledListenerAt(1) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener3.class);
        assertTrue("The PostPersist events were not fired in the correct order.", bus.getPostPersistCalledListenerAt(2) == oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.BusListener.class);
    }
    
    public void testCreateFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            SportsCar car = (SportsCar) InheritanceModelExamples.sportsCarExample1();
            car.setDescription("Ferrari");
            car.setMaxSpeed(300);
            em.persist(car);
            sportsCarId = car.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            fail("An exception was caught during create FueledVehicle [SportsCar] operation : " + e.getMessage());
        }finally{
            em.close();
        }
        
    }

    public void testCreateNonFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            Company co = InheritanceModelExamples.companyExample2();
            Boat boat = InheritanceModelExamples.boatExample1(co);
            em.persist(boat);
            boatId = boat.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            fail("An exception was caught during create NonFueledVehicle [Boat] operation : " + e.getMessage());
        }finally{
            em.close();
        }
    }    
    
    public void testDeleteBusFueledVehicle() {
        int postLoadBusCountBefore = Bus.POST_LOAD_COUNT;
        int preRemoveBusCountBefore = Bus.PRE_REMOVE_COUNT;
        int postRemoveBusCountBefore = Bus.POST_REMOVE_COUNT;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            em.remove(em.find(Bus.class, busId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw e;
        }
    
        assertTrue("Error deleting FueledVehicle [Bus]", em.find(Bus.class, busId) == null);
        assertFalse("The PostLoad callback method on Bus was not called.", postLoadBusCountBefore == Bus.POST_LOAD_COUNT);
        assertFalse("The PreRemove callback method on Bus was not called.", preRemoveBusCountBefore == Bus.PRE_REMOVE_COUNT);
        assertFalse("The PostRemove callback method on Bus was not called.", postRemoveBusCountBefore == Bus.POST_REMOVE_COUNT);
    }
    
    public void testDeleteFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(SportsCar.class, sportsCarId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting FueledVehicle [SportsCar]", em.find(SportsCar.class, sportsCarId) == null);
    }

    public void testDeleteNonFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Boat.class, boatId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting NonFueledVehicle [Boat]", em.find(Boat.class, boatId) == null);
    }

    public void testNamedNativeQueryOnSportsCar() {
        EJBQueryImpl query = (EJBQueryImpl) createEntityManager().createNamedQuery("findSQLMaxSpeedForFerrari");
        List results = query.getResultList();
        assertTrue("Failed to return 1 item", (results.size() == 1));
        
        for (Iterator iterator = results.iterator(); iterator.hasNext(); ){
            Object maxSpeed = iterator.next();
            assertTrue("Failed to return column",(maxSpeed instanceof Number));
            assertTrue("Failed to return correct speed of 300",(((Number)maxSpeed).intValue() == 300));
        }
    }

    public void testReadFueledVehicle() {
        SportsCar car = (SportsCar) createEntityManager().find(SportsCar.class, sportsCarId);
        assertTrue("Error reading FueledVehicle [SportsCar]", car.getId() == sportsCarId);
    }

    public void testReadNonFueledVehicle() {
        Boat boat = (Boat) createEntityManager().find(Boat.class, boatId);
        assertTrue("Error reading NonFueledVehicle [Boat]", boat.getId() == boatId);
    }

    public void testUpdateBusFueledVehicle() {
        int preUpdateBusCountBefore = Bus.PRE_UPDATE_COUNT;
        int postUpdateBusCountBefore = Bus.POST_UPDATE_COUNT;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        Bus bus;
        
        try {
            bus = (Bus) em.find(Bus.class, busId);
            bus.setDescription("A crappy bus");
            em.merge(bus);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw e;
        }
        
        assertFalse("The PreUpdate callback method on Bus was not called.", preUpdateBusCountBefore == Bus.PRE_UPDATE_COUNT);
        assertFalse("The PostUpdate callback method on Bus was not called.", postUpdateBusCountBefore == Bus.POST_UPDATE_COUNT);
        
        int postLoadBusCountBefore = Bus.POST_LOAD_COUNT;
        int postLoadBusCountAfter1;
        
        try {
            // Clear the cache and check that we get a post load (post build internally).
            clearCache();
            em.refresh(bus);
            postLoadBusCountAfter1 = Bus.POST_LOAD_COUNT;
            
            // Don't clear the cache and check that we get a post load (post refresh internally).
            em.refresh(bus);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw e;
        }
            
        assertTrue("Error updating FueledVehicle [Bus]", bus.getDescription().equals("A crappy bus"));
        assertFalse("The PostLoad (on refresh without object in cache) callback method on Bus was not called.", postLoadBusCountBefore == postLoadBusCountAfter1);
        assertFalse("The PostLoad (on refresh with object in cache) callback method on Bus was not called.", postLoadBusCountAfter1 == Bus.POST_LOAD_COUNT);
    }
    
    public void testUpdateFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            SportsCar car = (SportsCar) em.find(SportsCar.class, sportsCarId);
            car.setDescription("Corvette");
            em.merge(car);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        SportsCar newCar = (SportsCar) em.find(SportsCar.class, sportsCarId);
        assertTrue("Error updating FueledVehicle [SportsCar]", newCar.getDescription().equals("Corvette"));
    }

    public void testUpdateNonFueledVehicle() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Boat boat = (Boat) em.find(Boat.class, boatId);
            Company co = boat.getOwner();
            co.setName("XYZ");
            em.merge(boat);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        Boat newBoat = (Boat) em.find(Boat.class, boatId);
        assertTrue("Error updating NonFueledVehicle [Boat]", newBoat.getOwner().getName().equals("XYZ"));
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
