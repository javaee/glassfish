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


package oracle.toplink.essentials.testing.tests.cmp3.xml.merge.inherited;

import java.sql.Date;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.Alpine;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.BeerConsumer;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.Canadian;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.Certification;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inherited.Beer;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.BeerListener;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.EmbeddedSerialNumber;
import oracle.toplink.essentials.testing.models.cmp3.xml.merge.inherited.TelephoneNumber;
 
/**
 * JUnit test case(s) for model using a mix of annotations, XML, and XML
 * overrides, with entities defined in separate XML mapping files.
 */
public class EntityMappingsMergeInheritedJUnitTestCase extends JUnitTestCase {
    private static Integer beerConsumerId;
    private static Integer canadianId;
    private static Integer alpineId;
    private static EmbeddedSerialNumber embeddedSerialNumber;
    
    public EntityMappingsMergeInheritedJUnitTestCase() {
        super();
    }
    
    public EntityMappingsMergeInheritedJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Inherited Model");
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testOneToManyRelationships"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testVerifyOneToManyRelationships"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testCreateBeerConsumer"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testReadBeerConsumer"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testNamedNativeQueryBeerConsumers"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testNamedNativeQueryCertifications"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testMappedSuperclassTransientField"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testTransientField"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testUpdateBeerConsumer"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testDeleteBeerConsumer"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testBeerLifeCycleMethodAnnotationIgnored"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testMappedSuperclassEntityListener"));
        suite.addTest(new EntityMappingsMergeInheritedJUnitTestCase("testMappedSuperclassEmbeddedXMLElement"));
        return new TestSetup(suite) {
            
            protected void setUp(){               
                DatabaseSession session = JUnitTestCase.getServerSession();   
            }
        
            protected void tearDown() {
                clearCache("ddlGeneration");
            }
        };
    }
    
    public void testCreateBeerConsumer() {
        EntityManager em = createEntityManager("ddlGeneration");
        try {
            em.getTransaction().begin();

            BeerConsumer consumer = new BeerConsumer();
            consumer.setName("Joe Black");
            em.persist(consumer);
            beerConsumerId = consumer.getId();
            
            Alpine alpine1 = new Alpine();
            alpine1.setAlcoholContent(5.0);
            alpine1.setBestBeforeDate(new Date(System.currentTimeMillis()+10000000));
            alpine1.setLocalTransientString("This should never be persisted");
            em.persist(alpine1);
            alpineId=alpine1.getId();
            consumer.addAlpineBeerToConsume(alpine1);

            embeddedSerialNumber = new EmbeddedSerialNumber();
            embeddedSerialNumber.setNumber(123456);
            embeddedSerialNumber.setBreweryCode("MOLSON");
             
            Canadian canadian1 = new Canadian();
            canadian1.setAlcoholContent(5.5);
            canadian1.setBeerConsumer(consumer);
            canadian1.setBornOnDate(new Date(System.currentTimeMillis()-30000000));
            canadian1.setTransientString("This should never be persisted");
            canadian1.setEmbeddedSerialNumber(embeddedSerialNumber);
            em.persist(canadian1);
            canadianId=canadian1.getId();
            consumer.getCanadianBeersToConsume().put(canadian1.getId(), canadian1);
            
            Canadian canadian2 = new Canadian();
            canadian2.setAlcoholContent(5.0);
            canadian2.setBeerConsumer(consumer);
            canadian2.setBornOnDate(new Date(System.currentTimeMillis()-23000000));
            em.persist(canadian2);
            consumer.getCanadianBeersToConsume().put(canadian2.getId(), canadian2);

            Certification cert1 = new Certification();
            cert1.setDescription("Value brand beer consumption certified");
            cert1.setBeerConsumer(consumer);
            em.persist(cert1);
            consumer.getCertifications().put(cert1.getId(), cert1);

            Certification cert2 = new Certification();
            cert2.setDescription("Premium brand beer consumption certified");
            cert2.setBeerConsumer(consumer);
            em.persist(cert2);
            consumer.getCertifications().put(cert2.getId(), cert2);

            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void testNamedNativeQueryBeerConsumers() {
        List consumers = createEntityManager("ddlGeneration").createNamedQuery("findAnyMergeSQLBeerConsumer").getResultList();
        assertTrue("Error executing native query 'findAnyMergeSQLBeerConsumer'", consumers != null);
    }

    public void testNamedNativeQueryCertifications() {
        List certifications = createEntityManager("ddlGeneration").createNamedQuery("findAllMergeSQLCertifications").getResultList();
        assertTrue("Error executing native query 'findAllMergeSQLCertifications'", certifications != null);
    }

    public void testDeleteBeerConsumer() {
        EntityManager em = createEntityManager("ddlGeneration");
        em.getTransaction().begin();
        try{
            em.remove(em.find(BeerConsumer.class, beerConsumerId));
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        assertTrue("Error deleting BeerConsumer", em.find(BeerConsumer.class, beerConsumerId) == null);
    }

    public void testReadBeerConsumer() {
        BeerConsumer consumer = (BeerConsumer) createEntityManager("ddlGeneration").find(BeerConsumer.class, beerConsumerId);
        assertTrue("Error reading BeerConsumer", consumer.getId() == beerConsumerId);
    }

    public void testUpdateBeerConsumer() {
        EntityManager em = createEntityManager("ddlGeneration");
        em.getTransaction().begin();
        try{
        
            BeerConsumer beerConsumer = (BeerConsumer) em.find(BeerConsumer.class, beerConsumerId);
            beerConsumer.setName("Joe White");
            
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }
        clearCache("ddlGeneration");

        BeerConsumer newBeerConsumer = (BeerConsumer) em.find(BeerConsumer.class, beerConsumerId);
        em.close();
        assertTrue("Error updating BeerConsumer name", newBeerConsumer.getName().equals("Joe White"));
    }
	
        /** 
         * Merge Test:Have a class(TelephoneNumber) that uses a composite primary
         * key (defined partially in annotations and XML) and define a 1-M 
         * (BeerConsumer->TelephoneNumber) for it in XML 
         */
	public void testOneToManyRelationships() {
		EntityManager em = createEntityManager("ddlGeneration");
		try {
			em.getTransaction().begin();
			
			BeerConsumer consumer = new BeerConsumer();
			consumer.setName("Joe Black");
			
			TelephoneNumber homeNumber = new TelephoneNumber();
			homeNumber.setAreaCode("555");
			homeNumber.setType("Home");
			homeNumber.setNumber("123-1234");
			
			TelephoneNumber workNumber = new TelephoneNumber();
			workNumber.setAreaCode("555");
			workNumber.setType("Work");
			workNumber.setNumber("987-9876");
			 
			consumer.addTelephoneNumber(homeNumber);
			consumer.addTelephoneNumber(workNumber);
			em.persist(consumer);
			beerConsumerId = consumer.getId();
			
			em.getTransaction().commit();    
		} catch (RuntimeException e) {
			if (em.getTransaction().isActive()){
                            em.getTransaction().rollback();
                        }
			throw e;
		}
        
    }
    //Verify Relationship
    public void testVerifyOneToManyRelationships() {
        EntityManager em = createEntityManager("ddlGeneration");
        try {
            em.getTransaction().begin();
            
            BeerConsumer cm = em.find(BeerConsumer.class, beerConsumerId);
            java.util.Collection phones = cm.getTelephoneNumbers().values();
            assertTrue("Wrong phonenumbers associated with BeerConsumer", phones.size() == 2);
            for (Iterator iterator = phones.iterator(); iterator.hasNext();){
                    TelephoneNumber phone = (TelephoneNumber)(iterator.next());
                    assertTrue("Wrong owner of the telephone",phone.getBeerConsumer().getId() == beerConsumerId);
            }
            
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    // Verify transient property from mapped superclass is not persisted
    public void testMappedSuperclassTransientField() {
        Canadian canadianBeer = (Canadian) createEntityManager("ddlGeneration").find(Canadian.class, canadianId);
        assertTrue("Error reading Canadian", canadianBeer.getId() == canadianId);
        assertTrue("Mapped superclass transientString was persisted to the database", canadianBeer.getTransientString() == null);
    }

    // Verify transient property is not persisted
    public void testTransientField() {
        Alpine alpineBeer = (Alpine) createEntityManager("ddlGeneration").find(Alpine.class, alpineId);
        assertTrue("Error reading Alpine", alpineBeer.getId() == alpineId);
        assertTrue("localTransientString was persisted to the database", alpineBeer.getLocalTransientString() == null);
    }

    public void testBeerLifeCycleMethodAnnotationIgnored() {
        // Since metadata-complete specified on Beer superclass, all annotations
        // including lifecycle methods should be ignored.
        int beerPrePersistCount = Beer.BEER_PRE_PERSIST_COUNT;
        EntityManager em = createEntityManager("ddlGeneration");
        BeerConsumer consumer = (BeerConsumer) createEntityManager("ddlGeneration").find(BeerConsumer.class, beerConsumerId);

        try {
            em.getTransaction().begin();

            Canadian canadian1 = new Canadian();
            canadian1.setAlcoholContent(5.5);
            canadian1.setBeerConsumer(consumer);
            canadian1.setBornOnDate(new Date(System.currentTimeMillis()-30000000));
            em.persist(canadian1);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        assertTrue("The callback method [PrePersist] was called.", beerPrePersistCount == Beer.BEER_PRE_PERSIST_COUNT);
    }

    public void testMappedSuperclassEntityListener() {
        int listenerPostPersistCount = BeerListener.POST_PERSIST_COUNT;
        EntityManager em = createEntityManager("ddlGeneration");
        BeerConsumer consumer = (BeerConsumer) createEntityManager("ddlGeneration").find(BeerConsumer.class, beerConsumerId);

        try {
            em.getTransaction().begin();
            Canadian canadian1 = new Canadian();
            canadian1.setAlcoholContent(5.5);
            canadian1.setBeerConsumer(consumer);
            canadian1.setBornOnDate(new Date(System.currentTimeMillis()-30000000));
            em.persist(canadian1);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        assertFalse("The listener callback method [PostPersist] was not called.", listenerPostPersistCount == BeerListener.POST_PERSIST_COUNT);
    }
    
    public void testMappedSuperclassEmbeddedXMLElement() {
        /**
         * Canadian canadianBeer = (Canadian) createEntityManager("ddlGeneration").find(Canadian.class, canadianId);
         * assertTrue("Error reading Canadian", canadianBeer.getId() == canadianId);
         * assertTrue("Mapped superclass embedded element was not processed correctly", (canadianBeer.getEmbeddedSerialNumber().getNumber() == embeddedSerialNumber.getNumber())
         *    &&(canadianBeer.getEmbeddedSerialNumber().getBreweryCode().equals(embeddedSerialNumber.getBreweryCode())));        
         */
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
