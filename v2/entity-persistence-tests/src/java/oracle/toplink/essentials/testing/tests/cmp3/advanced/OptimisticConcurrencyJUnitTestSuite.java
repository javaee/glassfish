package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.*;
import oracle.toplink.essentials.sessions.DatabaseSession;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.PersistenceException;

/**
 * <p>
 * <b>Purpose</b>: Test TopLink's EJB 3.0 optimistic concurrency functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite and adds tests to the
 * suite. The database gets initialized prior to the test methods.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for TopLink's EJB 3.0 optimistic concurrency functionality.
 * </ul>
 * @see oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator
 */
 public class OptimisticConcurrencyJUnitTestSuite extends JUnitTestCase {

    public OptimisticConcurrencyJUnitTestSuite() {
        super();
    }

    public OptimisticConcurrencyJUnitTestSuite(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Optimistic Concurrency");
        suite.addTest(new OptimisticConcurrencyJUnitTestSuite("testCreateProjects"));
        suite.addTest(new OptimisticConcurrencyJUnitTestSuite("testCreateEmployeeWithFlush"));
        suite.addTest(new OptimisticConcurrencyJUnitTestSuite("testVersionUpdateWithCorrectValue"));
        suite.addTest(new OptimisticConcurrencyJUnitTestSuite("testVersionUpdateWithIncorrectValue"));
        suite.addTest(new OptimisticConcurrencyJUnitTestSuite("testVersionUpdateWithNullValue"));

        return new TestSetup(suite) {

            protected void setUp(){
                DatabaseSession session = JUnitTestCase.getServerSession();
                new AdvancedTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }

    /**
     * Creates two projects used in later tests.
     */
    public void testCreateProjects() {
        EntityManager em = createEntityManager();
        Project project1, project2;

        em.getTransaction().begin();
        project1 = ModelExamples.projectExample1();
        project2 = ModelExamples.projectExample2();
        em.persist(project1);
        em.persist(project2);
        em.getTransaction().commit();
    }

    /**
     * test for issue 635: NullPointerException occuring for Object typed version fields.
     * Employee has a write lock (version) field of type Integer
     * The NullPointerException is thrown when comparing versions in
     * ObjectChangeSet#compareWriteLockValues
     */
    public void testCreateEmployeeWithFlush() {
        EntityManager em = createEntityManager();
        Project project1, project2;
        Employee employee;

        try {
            em.getTransaction().begin();
            employee = ModelExamples.employeeExample1();
            em.persist(employee);

            // first flush: Employee is written to the database
            Query query = em.createNamedQuery("findProjectByName");
            query.setParameter("name", "Farmer effecency evaluations");
            project1 = (Project) query.getSingleResult();
            employee.getProjects().add(project1);

            // second flush: Employee is modified, but
            // no update to EMPLOYEE table; only join table entry is written
            query = em.createNamedQuery("findProjectByName");
            query.setParameter("name", "Feline Demographics Assesment");
            project2 = (Project) query.getSingleResult();
            employee.getProjects().add(project2);

            // third flush: Employee is modified, but
            // no update to EMPLOYEE table; only join table entry is written
            // A NullPointerException in ObjectChangeSet#compareWriteLockValues
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }

    /**
     * test: updating the version field with the in-memory value.
     * This should be allowed; there's no change for TopLink to detect this.
     */
    public void testVersionUpdateWithCorrectValue() {
        EntityManager em = createEntityManager();
        Employee employee;

        try {
            em.getTransaction().begin();
            employee = ModelExamples.employeeExample1();
            em.persist(employee);
            em.getTransaction().commit();

            em.getTransaction().begin();
            employee.setVersion(1);
            em.getTransaction().commit();
        } catch (RuntimeException re) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw re;
        }
    }

    /**
     * test: updating the version field with value != in-memory value.
     * This should throw an OptimisticLockException
     */
    public void testVersionUpdateWithIncorrectValue() {
        EntityManager em = createEntityManager();
        Employee employee;

        try {
            em.getTransaction().begin();
            employee = ModelExamples.employeeExample1();
            em.persist(employee);
            em.getTransaction().commit();

            em.getTransaction().begin();
            employee.setVersion(2);
            em.getTransaction().commit();
            fail("updating object version with wrong value didn't throw exception");
        } catch (PersistenceException pe) {
            // expected behaviour
        } catch (Exception e) {
            fail("updating object version with wrong value threw a wrong exception: " + e.getMessage());
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    /**
     * test: updating the version field with null value.
     * This should throw an exception
     */
    public void testVersionUpdateWithNullValue() {
        EntityManager em = createEntityManager();
        Employee employee;

        try {
            em.getTransaction().begin();
            employee = ModelExamples.employeeExample1();
            em.persist(employee);
            em.getTransaction().commit();

            em.getTransaction().begin();
            employee.setVersion(null);
            em.getTransaction().commit();
            fail("employee.setVersion(null) didn't throw exception");
        } catch (PersistenceException pe) {
            // expected behaviour
        } catch (Exception e) {
            fail("employee.setVersion(null) threw a wrong exception: " + e.getMessage());
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
}
