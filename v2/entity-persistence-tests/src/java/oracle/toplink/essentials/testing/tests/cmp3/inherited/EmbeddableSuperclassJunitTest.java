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

import java.util.Date;
import java.util.Vector;
import java.util.Calendar;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Alpine;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Canadian;
import oracle.toplink.essentials.testing.models.cmp3.inherited.BeerConsumer;
import oracle.toplink.essentials.testing.models.cmp3.inherited.SerialNumber;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Certification;
import oracle.toplink.essentials.testing.models.cmp3.inherited.TelephoneNumber;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inherited.InheritedTableManager;
 
public class EmbeddableSuperclassJunitTest extends JUnitTestCase {
    private static Date m_savedDate;
    private static Integer m_alpineId;
    private static Integer m_canadianId;
    private static Integer m_beerConsumerId1, m_beerConsumerId2;
    private static Integer m_certId1, m_certId2, m_certId3, m_certId4;
    private static String m_canadianProperty1 = "string1";
    private static String m_canadianProperty2 = "string2";
    
    public EmbeddableSuperclassJunitTest() {
        super();
    }
    
    public EmbeddableSuperclassJunitTest(String name) {
        super(name);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("EmbeddableSuperclassJunitTest");
        suite.addTest(new EmbeddableSuperclassJunitTest("testCreateBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testCreateAlpine"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testCreateCanadian"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testCreateCertifications"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testCreateTelephoneNumbers"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testReadBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testReadAlpine"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testReadCanadian"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testInsertNewAlpine"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testInsertNewAlpineAndModifyOrderOfExistingAlpines"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateAlpine"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateAlpineThroughBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateCanadian"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateCanadianThroughBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateCertifications"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testUpdateTelephoneNumberThroughBeerConsumer"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testShuffleTelephoneNumbersOnBeerConsumers"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testDeleteAlpine"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testDeleteCanadian"));
        suite.addTest(new EmbeddableSuperclassJunitTest("testDeleteBeerConsumer"));

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
    
    public void testCreateAlpine() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);

            Alpine alpine1 = new Alpine();
            alpine1.setBestBeforeDate(new Date(2005, 8, 21));
            alpine1.setAlcoholContent(5.0);
            alpine1.setBeerConsumer(beerConsumer);
            alpine1.setClassification(Alpine.Classification.BITTER);
            alpine1.addInspectionDate(new Date(System.currentTimeMillis()));
            em.persist(alpine1);
            m_alpineId = alpine1.getId();
            
            SerialNumber serialNumber1 = new SerialNumber();
            serialNumber1.setAlpine(alpine1);
            em.persist(serialNumber1);

            Alpine alpine2 = new Alpine();
            alpine2.setBestBeforeDate(new Date(2005, 8, 17));
            alpine2.setAlcoholContent(5.5);
            alpine2.setBeerConsumer(beerConsumer);
            alpine2.setClassification(Alpine.Classification.STRONG);
            em.persist(alpine2);

            SerialNumber serialNumber2 = new SerialNumber();
            serialNumber2.setAlpine(alpine2);
            em.persist(serialNumber2);
            
            Alpine alpine3 = new Alpine();
            alpine3.setBestBeforeDate(new Date(2005, 8, 22));
            alpine3.setAlcoholContent(4.5);
            alpine3.setBeerConsumer(beerConsumer);
            alpine3.setClassification(Alpine.Classification.SWEET);
            em.persist(alpine3);
            
            SerialNumber serialNumber3 = new SerialNumber();
            serialNumber3.setAlpine(alpine3);
            em.persist(serialNumber3);
            
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
    
    public void testCreateBeerConsumer() {
        String exceptionStr = null;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            BeerConsumer beerConsumer1 = new BeerConsumer();
            beerConsumer1.setName("Guy Pelletier");
            em.persist(beerConsumer1);
            m_beerConsumerId1 = beerConsumer1.getId();
            
            BeerConsumer beerConsumer2 = new BeerConsumer();
            beerConsumer2.setName("Tom Ware");
            em.persist(beerConsumer2);
            m_beerConsumerId2 = beerConsumer2.getId();
            
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
    
    public void testCreateCanadian() {
        String exceptionStr = null;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            
            Canadian canadian1 = new Canadian();
            m_savedDate = Calendar.getInstance().getTime();
            canadian1.setBornOnDate(m_savedDate);
            canadian1.setAlcoholContent(5.0);
            canadian1.setBeerConsumer(beerConsumer);
            canadian1.setFlavor(Canadian.Flavor.LAGER);
            canadian1.getProperties().put( m_canadianProperty1, new Date(System.currentTimeMillis()) );
            em.persist(canadian1);
            m_canadianId = canadian1.getId();

            Canadian canadian2 = new Canadian();
            canadian2.setBornOnDate(Calendar.getInstance().getTime());
            canadian2.setAlcoholContent(5.5);
            canadian2.setFlavor(Canadian.Flavor.ICE);
            canadian2.setBeerConsumer(beerConsumer);
            em.persist(canadian2);
            
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
    
    public void testCreateCertifications() {
        String exceptionStr = null;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            Certification cert1 = new Certification();
            cert1.setDescription("Certification 1");
            em.persist(cert1);
            m_certId1 = cert1.getId();
            
            Certification cert2 = new Certification();
            cert2.setDescription("Certification 2");
            em.persist(cert2);
            m_certId2 = cert2.getId();
           
            Certification cert3 = new Certification();
            cert3.setDescription("Certification 3");
            em.persist(cert3);
            m_certId3 = cert3.getId();
            
            Certification cert4 = new Certification();
            cert4.setDescription("Certification 4");
            em.persist(cert4);
            m_certId4 = cert4.getId();
            
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
    
    public void testCreateTelephoneNumbers() {
        String exceptionStr = null;
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            BeerConsumer beerConsumer1 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
           
            TelephoneNumber telephoneNumber1 = new TelephoneNumber();
            telephoneNumber1.setType("Home");
            telephoneNumber1.setAreaCode("975");
            telephoneNumber1.setNumber("1234567");
            beerConsumer1.addTelephoneNumber(telephoneNumber1);
            
            TelephoneNumber telephoneNumber2 = new TelephoneNumber();
            telephoneNumber2.setType("Cell");
            telephoneNumber2.setAreaCode("975");
            telephoneNumber2.setNumber("7654321");
            beerConsumer1.addTelephoneNumber(telephoneNumber2);
            
            BeerConsumer beerConsumer2 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
            
            TelephoneNumber telephoneNumber3 = new TelephoneNumber();
            telephoneNumber3.setType("Home");
            telephoneNumber3.setAreaCode("555");
            telephoneNumber3.setNumber("5555555");
            beerConsumer2.addTelephoneNumber(telephoneNumber3);
            
            TelephoneNumber telephoneNumber4 = new TelephoneNumber();
            telephoneNumber4.setType("Cell");
            telephoneNumber4.setAreaCode("555");
            telephoneNumber4.setNumber("3331010");
            beerConsumer2.addTelephoneNumber(telephoneNumber4);
            
            TelephoneNumber telephoneNumber5 = new TelephoneNumber();
            telephoneNumber5.setType("Work");
            telephoneNumber5.setAreaCode("999");
            telephoneNumber5.setNumber("8648363");
            beerConsumer2.addTelephoneNumber(telephoneNumber5);
            
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
    
    public void testDeleteAlpine() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            
            em.remove(em.find(Alpine.class, m_alpineId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting an Alpine beer", em.find(Alpine.class, m_alpineId) == null);
    }
    
    public void testDeleteBeerConsumer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(BeerConsumer.class, m_beerConsumerId1));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting a BeerConsumer", em.find(BeerConsumer.class, m_beerConsumerId1) == null);
    }
    
    public void testDeleteCanadian() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.remove(em.find(Canadian.class, m_canadianId));
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        assertTrue("Error deleting a Canadian beer", em.find(Canadian.class, m_canadianId) == null);
    }
    
    public void testInsertNewAlpine() {
        Alpine alpine = null;
        EntityManager em = createEntityManager();
        // Part 1 ... add an alpine beer to the collection.
        BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        em.getTransaction().begin();
        try {
            beerConsumer = (BeerConsumer) em.merge(beerConsumer);

            alpine = new Alpine();
            alpine.setBestBeforeDate(new Date(2005, 8, 18));
            alpine.setAlcoholContent(5.4);
            alpine.setClassification(Alpine.Classification.BITTER);
            beerConsumer.addAlpineBeerToConsume(alpine, 0);
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
            
        // Part 2 ... read from cache.
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        
        // Read the beerConsumer back from the cache, check the ordering.
        Vector alpinesFromCache = (Vector) beerConsumer.getAlpineBeersToConsume();
        assertTrue("The new alpine was not added at the correct index in the cache.", alpinesFromCache.indexOf(alpine) == 0);
        
        // Part 3 ... read from database.
        clearCache();
        em.clear();
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        
        // Read the beerConsumer back from the database, check the ordering.
        Vector alpinesFromDB = (Vector) beerConsumer.getAlpineBeersToConsume();
        assertTrue("The new alpine was not added at the correct index when retrieving from the database.", alpinesFromDB.indexOf(alpine) == 1);
    }
    
    public void testInsertNewAlpineAndModifyOrderOfExistingAlpines() {
        Alpine alpine1 = null, alpine2 = null;
        EntityManager em = createEntityManager();
        // Part 1 ... add an alpine beer to the collection.
        BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        em.getTransaction().begin();
        try {
        
            beerConsumer = (BeerConsumer) em.merge(beerConsumer);

            alpine1 = new Alpine();
            alpine1.setBestBeforeDate(new Date(2005, 8, 16));
            alpine1.setAlcoholContent(5.6);
            alpine1.setClassification(Alpine.Classification.STRONG);
            beerConsumer.addAlpineBeerToConsume(alpine1, 0);
            
            // Part 2 ... change the date, hence the index, of an alpine.
            alpine2 = beerConsumer.getAlpineBeerToConsume(4);
            alpine2.setBestBeforeDate(new Date(2005, 8, 20));
            beerConsumer.moveAlpineBeerToConsume(4, 3);
            
            // Part 3 ... remove 2 alpines ...
            beerConsumer.removeAlpineBeerToConsume(4);
            beerConsumer.removeAlpineBeerToConsume(2);
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
            
            // Part 3 ... read from cache.
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        
        // Read the beerConsumer back from the cache, check the ordering.
        Vector alpinesFromCache = (Vector) beerConsumer.getAlpineBeersToConsume();
        assertTrue("The new alpine was not added at the correct index in the cache.", alpinesFromCache.indexOf(alpine1) == 0);
        assertTrue("The alpine was not moved to the correct index in the cache.", alpinesFromCache.indexOf(alpine2) == 2);
        
        // Part 4 ... read from database.
        clearCache();
        em.clear();
        beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        
        // Read the beerConsumer back from the database, check the ordering.
        Vector alpinesFromDB = (Vector) beerConsumer.getAlpineBeersToConsume();
        assertTrue("The new alpine was not added at the correct index when retrieving from the database.", alpinesFromDB.indexOf(alpine1) == 0);
        assertTrue("The alpine was not moved to the correct index when retrieving from the database.", alpinesFromDB.indexOf(alpine2) == 2);
    }
    
    public void testReadAlpine() {
        Alpine alpine = (Alpine) createEntityManager().find(Alpine.class, m_alpineId);
        assertTrue("Error on reading back an Alpine beer", alpine != null);
        assertTrue("The enum was not read back in properly.", alpine.getClassification() == Alpine.Classification.BITTER);
    }
    
    public void testReadBeerConsumer() {
        BeerConsumer beerConsumer = (BeerConsumer) createEntityManager().find(BeerConsumer.class, m_beerConsumerId1);
        assertTrue("Error on reading back a BeerConsumer", beerConsumer != null);
    }
    
    public void testReadCanadian() {
        Canadian canadian = (Canadian) createEntityManager().find(Canadian.class, m_canadianId);
        assertTrue("Error on reading back a Canadian beer", canadian != null);
        assertTrue("The enum was not read back in properly.", canadian.getFlavor() == Canadian.Flavor.LAGER);
    }
    
    public void testShuffleTelephoneNumbersOnBeerConsumers() {
        int beerConsumer1TelephoneCountStart = 0, beerConsumer2TelephoneCountStart = 0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            BeerConsumer beerConsumer1 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            beerConsumer1TelephoneCountStart = beerConsumer1.getTelephoneNumbers().size();
            
            BeerConsumer beerConsumer2 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
            beerConsumer2TelephoneCountStart = beerConsumer2.getTelephoneNumbers().size();
            
            TelephoneNumber phone = beerConsumer1.getTelephoneNumbers().values().iterator().next();
            phone.setBeerConsumer(beerConsumer2);
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        em.clear();
        BeerConsumer newBeerConsumer1 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
        int beerConsumer1TelephoneCountEnd = newBeerConsumer1.getTelephoneNumbers().size();
        
        BeerConsumer newBeerConsumer2 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
        int beerConsumer2TelephoneCountEnd = newBeerConsumer2.getTelephoneNumbers().size();
        
        assertTrue("Error updating a TelephoneNumber's beer consumer", beerConsumer1TelephoneCountEnd + 1 == beerConsumer1TelephoneCountStart);
        assertTrue("Error updating a TelephoneNumber's beer consumer", beerConsumer2TelephoneCountEnd - 1 == beerConsumer2TelephoneCountStart);
    }
    
    public void testUpdateAlpine() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            Alpine alpine = (Alpine) em.find(Alpine.class, m_alpineId);
            alpine.setBestBeforeDate(new Date(2005, 8, 19));
            em.merge(alpine);    
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
        clearCache();
        em.clear();
        Alpine newAlpine = (Alpine) em.find(Alpine.class, m_alpineId);
        assertTrue("Error updating an Alpine beer.", newAlpine.getBestBeforeDate().equals(new Date(2005, 8, 19)));
    }
    
    public void testUpdateAlpineThroughBeerConsumer() {
        int id = 0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
            Alpine alpine = (Alpine) beerConsumer.getAlpineBeersToConsume().iterator().next();
            alpine.setBestBeforeDate(new Date(2005, 9, 19));
            id = alpine.getId();
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        em.clear();
        Alpine newAlpine = (Alpine) em.find(Alpine.class, id);
        em.close();
        assertTrue("Error updating an Alpine beer.", newAlpine.getBestBeforeDate().equals(new Date(2005, 9, 19)));
    }
    
    public void testUpdateBeerConsumer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            beerConsumer.setName("Big beer gut");
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    
        clearCache();
        em.clear();
        BeerConsumer newBeerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);        
        assertTrue("Error updating a BeerConsumer", newBeerConsumer.getName().equals("Big beer gut"));
    }
        
    public void testUpdateCanadian() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Canadian canadian = (Canadian) em.find(Canadian.class, m_canadianId);
            canadian.getBornOnDate().setTime((new Date(2005, 8, 19)).getTime());
            canadian.getProperties().put( m_canadianProperty2, new Date(System.currentTimeMillis()) );
            em.merge(canadian);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        
        clearCache();
        em.clear();
        Canadian newCanadian = (Canadian) em.find(Canadian.class, m_canadianId);
        assertTrue("Error updating a Canadian beer's BornOnDate", newCanadian.getBornOnDate().equals(new Date(2005, 8, 19)));
        assertTrue("Error updating a Canadian beer's Properties", newCanadian.getProperties().size()==2);
    }
    
    public void testUpdateCanadianThroughBeerConsumer() {
        int id =  0;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            Canadian canadian = (Canadian) beerConsumer.getCanadianBeersToConsume().values().iterator().next();
            canadian.setBornOnDate(new Date(2005, 9, 19));
            id = canadian.getId();
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();
        
        Canadian newCanadian = (Canadian) em.find(Canadian.class, id);
        assertTrue("Error updating a Canadian beer.", newCanadian.getBornOnDate().equals(new Date(2005, 9, 19)));
    }
    
    public void testUpdateTelephoneNumberThroughBeerConsumer() {
        TelephoneNumber oldPhone = null, newPhone = null;
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            
            oldPhone = beerConsumer.getTelephoneNumbers().values().iterator().next();
            beerConsumer.removePhoneNumber(oldPhone);
            em.remove(oldPhone);
            
            newPhone = new TelephoneNumber();
            newPhone.setAreaCode("XXX");
            newPhone.setNumber(oldPhone.getNumber());
            newPhone.setType(oldPhone.getType());
            beerConsumer.addTelephoneNumber(newPhone);
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
        clearCache();

        BeerConsumer bc = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
        
        assertTrue("The new/updated phone was not persisted.", bc.hasTelephoneNumber(newPhone));
        assertFalse("The old phone was not removed.", bc.hasTelephoneNumber(oldPhone));
    }
    
    public void testUpdateCertifications() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
           
            BeerConsumer beerConsumer1 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId1);
            
            Certification cert1 = (Certification) em.find(Certification.class, m_certId1);
            cert1.setBeerConsumer(beerConsumer1);
            
            Certification cert2 = (Certification) em.find(Certification.class, m_certId2);
            cert2.setBeerConsumer(beerConsumer1);
    
            BeerConsumer beerConsumer2 = (BeerConsumer) em.find(BeerConsumer.class, m_beerConsumerId2);
            
            Certification cert3 = (Certification) em.find(Certification.class, m_certId3);
            cert3.setBeerConsumer(beerConsumer2);
            
            Certification cert4 = (Certification) em.find(Certification.class, m_certId4);
            cert4.setBeerConsumer(beerConsumer2);
            
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }
    
    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
