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

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Car;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.Bus;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.SportsCar;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.AbstractBus;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.InheritanceTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.BusListener;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.VehicleListener;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.ListenerSuperclass;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.listeners.FueledVehicleListener;
import oracle.toplink.essentials.testing.models.cmp3.xml.inheritance.listeners.DefaultListener;

public class LifecycleCallbackJunitTest extends JUnitTestCase {
    private static Number m_busID;
     
    public LifecycleCallbackJunitTest() {
        super();
    }
    
    public LifecycleCallbackJunitTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("LifecycleCallbackJunitTest");
        suite.addTest(new LifecycleCallbackJunitTest("testPrePersistBusOverrideAndAbstractInheritAndDefault"));
        suite.addTest(new LifecycleCallbackJunitTest("testPostPersistBusInheritAndDefault"));
        suite.addTest(new LifecycleCallbackJunitTest("testPostLoadBusInheritAndDefault"));
        suite.addTest(new LifecycleCallbackJunitTest("testPrePersistSportsCarInheritAndExcludeDefault"));
        suite.addTest(new LifecycleCallbackJunitTest("testPostPersistSportsCarInheritAndExcludeDefault"));
        suite.addTest(new LifecycleCallbackJunitTest("testPrePersistSportsCarOverride"));

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
    
    public void testPostLoadBusInheritAndDefault() {
        int vehiclePostLoadCountBefore = VehicleListener.POST_LOAD_COUNT;
        int defaultListenerPostLoadCountBefore = DefaultListener.POST_LOAD_COUNT;
        
        Bus bus = (Bus) createEntityManager().find(Bus.class, m_busID);
        
        int vehiclePostLoadCountAfter = VehicleListener.POST_LOAD_COUNT;
        int defaultListenerPostLoadCountAfter = DefaultListener.POST_LOAD_COUNT;
        
        assertFalse("The PostLoad callback method for Vehicle was not called.", vehiclePostLoadCountBefore == vehiclePostLoadCountAfter);
        assertFalse("The PostLoad callback method for DefaultListener was not called.", defaultListenerPostLoadCountBefore == defaultListenerPostLoadCountAfter);
    }
    
    public void testPostPersistBusInheritAndDefault() {
        int busListenerPostPersistCountBefore = BusListener.POST_PERSIST_COUNT;
        int fueledVehiclePostPersistCountBefore = FueledVehicleListener.POST_PERSIST_COUNT;
        int defaultListenerPostPersistCountBefore = DefaultListener.POST_PERSIST_COUNT;
        int defaultListenerPostLoadCountBefore = DefaultListener.POST_LOAD_COUNT;
        int defaultListenerPostLoadCountIntermidiate;
        
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        try {
            Bus bus = new Bus();
            bus.setPassengerCapacity(new Integer(50));
            bus.setFuelCapacity(new Integer(175));
            bus.setDescription("OC Transpo Bus");
            bus.setFuelType("Diesel");
            em.persist(bus);
            em.flush();
            
            // This should fire a postLoad event ...
            em.refresh(bus);
            defaultListenerPostLoadCountIntermidiate = DefaultListener.POST_LOAD_COUNT;
                
        	javax.persistence.Query q = em.createQuery("select distinct b from Bus b where b.id = " + bus.getId());
            // This should not fire a postLoad event ...
            q.getResultList();
            
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw ex;
        }
        
        int busListenerPostPersistCountAfter = BusListener.POST_PERSIST_COUNT;
        int fueledVehiclePostPersistCountAfter = FueledVehicleListener.POST_PERSIST_COUNT;
        int defaultListenerPostPersistCountAfter = DefaultListener.POST_PERSIST_COUNT;
        int defaultListenerPostLoadCountAfter = DefaultListener.POST_LOAD_COUNT;
        
        assertFalse("The PostPersist callback method on BusListener was not called.", busListenerPostPersistCountBefore == busListenerPostPersistCountAfter);
        assertFalse("The PostPersist callback method on FueledVehicleListener was not called.", fueledVehiclePostPersistCountBefore == fueledVehiclePostPersistCountAfter);
        assertFalse("The PostPersist callback method on DefaultListener was not called.", defaultListenerPostPersistCountBefore == defaultListenerPostPersistCountAfter);
        assertTrue("The PostLoad callback method on DefaultListener was called more than once, possibly on the refresh.", (defaultListenerPostLoadCountIntermidiate - defaultListenerPostLoadCountBefore) == 1);
        assertTrue("The PostLoad callback method on DefaultListener was called on the getQueryResult().", defaultListenerPostLoadCountIntermidiate == defaultListenerPostLoadCountAfter);
    }
    
    public void testPostPersistSportsCarInheritAndExcludeDefault() {
        int fueledVehiclePostPersistCountBefore = FueledVehicleListener.POST_PERSIST_COUNT;
        int defaultListenerPostPersistCountBefore = DefaultListener.POST_PERSIST_COUNT;
        
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        try {
            SportsCar sportsCar = new SportsCar();
            sportsCar.setPassengerCapacity(new Integer(4));
            sportsCar.setFuelCapacity(new Integer(55));
            sportsCar.setDescription("Porshe");
            sportsCar.setFuelType("Gas");
            em.persist(sportsCar);    
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw ex;
        }
        
        int fueledVehiclePostPersistCountAfter = FueledVehicleListener.POST_PERSIST_COUNT;
        int defaultListenerPostPersistCountAfter = DefaultListener.POST_PERSIST_COUNT;
        
        assertFalse("The PostPersist callback method on FueledVehicleListener was not called.", fueledVehiclePostPersistCountBefore == fueledVehiclePostPersistCountAfter);
        assertTrue("The PostPersist callback method on DefaultListener was called.", defaultListenerPostPersistCountBefore == defaultListenerPostPersistCountAfter);
    }
    
    public void testPrePersistBusOverrideAndAbstractInheritAndDefault() {
        int busListenerPrePersistCountBefore = BusListener.PRE_PERSIST_COUNT;
        int listenerSuperclassPrePersistCountBefore = ListenerSuperclass.COMMON_PRE_PERSIST_COUNT;
        int abstractBusPrePeristCountBefore = AbstractBus.PRE_PERSIST_COUNT;
        int defaultListenerPrePersistCountBefore = DefaultListener.PRE_PERSIST_COUNT;
       
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        try {
            Bus bus = new Bus();
            bus.setPassengerCapacity(new Integer(30));
            bus.setFuelCapacity(new Integer(100));
            bus.setDescription("School Bus");
            bus.setFuelType("Diesel");
            em.persist(bus);
            m_busID = bus.getId();
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            throw ex;
        }
        
        em.close();
        
        int busListenerPrePersistCountAfter = BusListener.PRE_PERSIST_COUNT;
        int listenerSuperclassPrePersistCountAfter = ListenerSuperclass.COMMON_PRE_PERSIST_COUNT;
        int abstractBusPrePeristCountAfter = AbstractBus.PRE_PERSIST_COUNT;
        int defaultListenerPrePersistCountAfter = DefaultListener.PRE_PERSIST_COUNT;
        
        assertFalse("The PrePersist callback method on BusListener was not called.", busListenerPrePersistCountBefore == busListenerPrePersistCountAfter);
        assertTrue("The PrePersist callback method on ListenerSuperclass was called.", listenerSuperclassPrePersistCountBefore == listenerSuperclassPrePersistCountAfter);
        assertFalse("The PrePersist callback method on AbstractBus was not called.", abstractBusPrePeristCountBefore == abstractBusPrePeristCountAfter);
        assertFalse("The PrePersist callback method on DefaultListener was not called.", defaultListenerPrePersistCountBefore == defaultListenerPrePersistCountAfter);
        assertFalse("The PrePersist callback method on DefaultListener was called more than once.", defaultListenerPrePersistCountAfter - defaultListenerPrePersistCountBefore >1 );
    }
    
    public void testPrePersistSportsCarInheritAndExcludeDefault() {
        int listenerSuperclassPrePersistCountBefore = ListenerSuperclass.COMMON_PRE_PERSIST_COUNT;
        int defaultListenerPrePersistCountBefore = DefaultListener.PRE_PERSIST_COUNT;
        
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        try {
            SportsCar sportsCar = new SportsCar();
            sportsCar.setPassengerCapacity(new Integer(2));
            sportsCar.setFuelCapacity(new Integer(60));
            sportsCar.setDescription("Corvette");
            sportsCar.setFuelType("Gas");
            em.persist(sportsCar);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
        
            em.close();
            throw ex;
        }
        
        em.close();
        
        int listenerSuperclassPrePersistCountAfter = ListenerSuperclass.COMMON_PRE_PERSIST_COUNT;
        int defaultListenerPrePersistCountAfter = DefaultListener.PRE_PERSIST_COUNT;
        
        assertFalse("The PrePersist callback method on ListenerSuperclass was not called.", listenerSuperclassPrePersistCountBefore == listenerSuperclassPrePersistCountAfter);
        assertTrue("The PrePersist callback method on DefaultListener was called.", defaultListenerPrePersistCountBefore == defaultListenerPrePersistCountAfter);
    }
    
    public void testPrePersistSportsCarOverride() {
        int carPrePersistCountBefore = Car.PRE_PERSIST_COUNT;
        int sportsCarPrePersistCountBefore = SportsCar.PRE_PERSIST_COUNT;
        
        EntityManager em = createEntityManager();        
        em.getTransaction().begin();
        
        try {
            SportsCar sportsCar = new SportsCar();
            sportsCar.setPassengerCapacity(new Integer(2));
            sportsCar.setFuelCapacity(new Integer(90));
            sportsCar.setDescription("Viper");
            sportsCar.setFuelType("Gas");
            em.persist(sportsCar);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
        
            em.close();
            throw ex;
        }
        
        em.close();
        
        int carPrePersistCountAfter = Car.PRE_PERSIST_COUNT;
        int sportsCarPrePersistCountAfter = SportsCar.PRE_PERSIST_COUNT;
        
        assertTrue("The PrePersist callback method on Car was called.", carPrePersistCountBefore == carPrePersistCountAfter);
        assertFalse("The PrePersist callback method on Sports car was not called.", sportsCarPrePersistCountBefore == sportsCarPrePersistCountAfter);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
