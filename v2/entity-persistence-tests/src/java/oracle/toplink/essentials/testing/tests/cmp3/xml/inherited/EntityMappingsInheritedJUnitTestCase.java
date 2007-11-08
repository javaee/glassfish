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


package oracle.toplink.essentials.testing.tests.cmp3.xml.inherited;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.Alpine;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.BeerConsumer;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.Canadian;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.Certification;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.InheritedTableManager;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.xml.inherited.TelephoneNumber;
 
/**
 * JUnit test case(s) for the TopLink EntityMappingsXMLProcessor.
 */
public class EntityMappingsInheritedJUnitTestCase extends JUnitTestCase {
    private static Integer beerConsumerId;
   
    public EntityMappingsInheritedJUnitTestCase() {
        super();
    }
    
    public EntityMappingsInheritedJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Inherited Model");
		suite.addTest(new EntityMappingsInheritedJUnitTestCase("testOneToManyRelationships"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testVerifyOneToManyRelationships"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testCreateBeerConsumer"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testReadBeerConsumer"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testNamedNativeQueryBeerConsumers"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testNamedNativeQueryCertifications"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testUpdateBeerConsumer"));
        suite.addTest(new EntityMappingsInheritedJUnitTestCase("testDeleteBeerConsumer"));
        
        return new TestSetup(suite) {
            
            protected void setUp(){               
                DatabaseSession session = JUnitTestCase.getServerSession();   
                new InheritedTableManager().replaceTables(session);
            }
        
            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreateBeerConsumer() {
        boolean exceptionCaught = false;
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();

            BeerConsumer consumer = new BeerConsumer();
            consumer.setName("Joe Black");

            em.persist(consumer);
            beerConsumerId = consumer.getId();
            
            Alpine alpine1 = new Alpine();
            alpine1.setAlcoholContent(5.0);
            alpine1.setBeerConsumer(consumer);
            alpine1.setBestBeforeDate(new Date(System.currentTimeMillis()+10000000));
            em.persist(alpine1);

            Canadian canadian1 = new Canadian();
            canadian1.setAlcoholContent(5.5);
            canadian1.setBeerConsumer(consumer);
            canadian1.setBornOnDate(new Date(System.currentTimeMillis()-30000000));
            em.persist(canadian1);
            
            Canadian canadian2 = new Canadian();
            canadian2.setAlcoholContent(5.0);
            canadian2.setBeerConsumer(consumer);
            canadian2.setBornOnDate(new Date(System.currentTimeMillis()-23000000));
            em.persist(canadian2);

            /*
            TelephoneNumber homeNumber = new TelephoneNumber();
            homeNumber.setAreaCode("555");
            homeNumber.setType("Home");
            homeNumber.setNumber("123-1234");
            getEntityManager().persist(homeNumber);
            
            TelephoneNumber workNumber = new TelephoneNumber();
            workNumber.setAreaCode("555");
            workNumber.setType("Work");
            workNumber.setNumber("987-9876");
            getEntityManager().persist(workNumber);
            */
            
            Certification cert1 = new Certification();
            cert1.setDescription("Value brand beer consumption certified");
            cert1.setBeerConsumer(consumer);
            em.persist(cert1);

            Certification cert2 = new Certification();
            cert2.setDescription("Premium brand beer consumption certified");
            cert2.setBeerConsumer(consumer);
            em.persist(cert2);

            em.persist(consumer);
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        
    }

    public void testNamedNativeQueryBeerConsumers() {
        List consumers = createEntityManager().createNamedQuery("findAnySQLBeerConsumer").getResultList();
        assertTrue("Error executing native query 'findAnySQLBeerConsumer'", consumers != null);
    }

    public void testNamedNativeQueryCertifications() {
        List certifications = createEntityManager().createNamedQuery("findAllSQLCertifications").getResultList();
        assertTrue("Error executing native query 'findAllSQLCertifications'", certifications != null);
    }

    public void testDeleteBeerConsumer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.remove(em.find(BeerConsumer.class, beerConsumerId));
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }
        assertTrue("Error deleting BeerConsumer", em.find(BeerConsumer.class, beerConsumerId) == null);
    }

    public void testReadBeerConsumer() {
        BeerConsumer consumer = (BeerConsumer) createEntityManager().find(BeerConsumer.class, beerConsumerId);
        assertTrue("Error reading BeerConsumer", consumer.getId() == beerConsumerId);
    }

    public void testUpdateBeerConsumer() {
        EntityManager em = createEntityManager();
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
        clearCache();

        BeerConsumer newBeerConsumer = (BeerConsumer) em.find(BeerConsumer.class, beerConsumerId);
        em.close();
        assertTrue("Error updating BeerConsumer name", newBeerConsumer.getName().equals("Joe White"));
    }
	
	//Merge Test:Have a class(TelephoneNumber) that uses a composite primary key (defined in annotations) and define a 1-M (BeerConsumer->TelephoneNumber) for it in XML
	//Setup Relationship
	public void testOneToManyRelationships() {
		EntityManager em = createEntityManager();
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
        EntityManager em = createEntityManager();
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

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
