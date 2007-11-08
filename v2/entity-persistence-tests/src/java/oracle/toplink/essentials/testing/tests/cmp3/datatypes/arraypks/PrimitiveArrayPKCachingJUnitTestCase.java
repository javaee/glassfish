package oracle.toplink.essentials.testing.tests.cmp3.datatypes.arraypks;

import java.util.UUID;

import javax.persistence.EntityManager;
import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.datatypes.arraypks.PrimByteArrayPKType;
import oracle.toplink.essentials.testing.models.cmp3.datatypes.arraypks.PrimitiveArraysAsPrimaryKeyTableCreator;

/**
 * <p>
 * <b>Purpose</b>: Tests caching of entities that use primitive arrays such as byte arrays 
 * as primary keys in TopLink's JPA implementation.
 * <p>
 * <b>Description</b>: This class creates a test suite and adds tests to the
 * suite. The database gets initialized prior to the test methods.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for caching of Entities with primitive array types for primary keys
 * in TopLink's JPA implementation.
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.datatypes.arraypks.PrimitiveArraysAsPrimaryKeyTableCreator
 */
public class PrimitiveArrayPKCachingJUnitTestCase extends JUnitTestCase{
    public PrimitiveArrayPKCachingJUnitTestCase() {
    }
    public PrimitiveArrayPKCachingJUnitTestCase(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Caching Primitive Array pk types");
        DatabaseSession session = JUnitTestCase.getServerSession();   
        if (!session.getPlatform().isOracle()){
            session.log(AbstractSessionLog.WARNING,null,"Warning: RAW type used for Primary keys only supported on Oracle - tests not run");
            return new TestSetup(suite);
        }

        suite.addTest(new PrimitiveArrayPKCachingJUnitTestCase("testPrimitiveByteArrayPK"));
        
        return new TestSetup(suite) {

            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new PrimitiveArraysAsPrimaryKeyTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    /**
     * Creates a PrimByteArrayPKType instance and then verifies that the same instance
     * is returned from the database.  
     */
    public void testPrimitiveByteArrayPK() {
        EntityManager em = createEntityManager();
        
        java.util.UUID uuid = UUID.randomUUID();
        PrimByteArrayPKType originalEntity = new PrimByteArrayPKType(PrimByteArrayPKType.getBytes(uuid));
        try {
            em.getTransaction().begin();
            em.persist(originalEntity);
            em.flush();
            PrimByteArrayPKType objectReadIn = em.find(PrimByteArrayPKType.class, PrimByteArrayPKType.getBytes(uuid));
            em.getTransaction().rollback();
            assertTrue("Different instances of the same PrimByteArrayPKType object was returned", originalEntity == objectReadIn);
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
