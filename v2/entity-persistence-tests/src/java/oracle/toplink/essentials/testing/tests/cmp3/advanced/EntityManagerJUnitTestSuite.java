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


package oracle.toplink.essentials.testing.tests.cmp3.advanced;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;
import javax.persistence.spi.PersistenceUnitInfo;

import junit.extensions.TestSetup;
import junit.framework.*;

import oracle.toplink.essentials.config.CacheUsage;
import oracle.toplink.essentials.config.CascadePolicy;
import oracle.toplink.essentials.config.FlushClearCache;
import oracle.toplink.essentials.config.PessimisticLock;
import oracle.toplink.essentials.config.TopLinkProperties;
import oracle.toplink.essentials.config.TopLinkQueryHints;
import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.internal.helper.Helper;
import oracle.toplink.essentials.queryframework.DatabaseQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.tools.schemaframework.SchemaManager;
import oracle.toplink.essentials.sequencing.NativeSequence;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Customizer;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Employee;
import oracle.toplink.essentials.testing.models.cmp3.advanced.AdvancedTableCreator;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Address;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Department;
import java.util.Iterator;

import oracle.toplink.essentials.testing.models.cmp3.advanced.LargeProject;
import oracle.toplink.essentials.testing.models.cmp3.advanced.PhoneNumber;
import oracle.toplink.essentials.testing.models.cmp3.advanced.SmallProject;
import oracle.toplink.essentials.testing.models.cmp3.advanced.Project;
import oracle.toplink.essentials.testing.models.cmp3.advanced.SuperLargeProject;
import oracle.toplink.essentials.threetier.ReadConnectionPool;
import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.tools.schemaframework.SequenceObjectDefinition;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;

public class EntityManagerJUnitTestSuite extends JUnitTestCase {
        
    public EntityManagerJUnitTestSuite() {
        super();
    }
    
    public EntityManagerJUnitTestSuite(String name) {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EntityManagerJUnitTestSuite.class);

        return new TestSetup(suite) {
            protected void setUp(){
                SchemaManager schemaManager = new SchemaManager(JUnitTestCase.getServerSession());
                new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession(), schemaManager);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    // JUnit framework will automatically execute all methods starting with test...    
    public void testRefreshNotManaged() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Employee emp = new Employee();
        emp.setFirstName("testRefreshNotManaged");
        try {
            em.refresh(emp);
            fail("entityManager.refresh(notManagedObject) didn't throw exception");
        } catch (IllegalArgumentException illegalArgumentException) {
            // expected behaviour
        } catch (Exception exception ) {
            fail("entityManager.refresh(notManagedObject) threw a wrong exception: " + exception.getMessage());
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    public void testRefreshRemoved() {
        // find an existing or create a new Employee
        String firstName = "testRefreshRemoved";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            try{
                em.getTransaction().begin();
                em.persist(emp);
                em.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw ex;
            }
        }
        
        try{
            em.getTransaction().begin();
            emp = em.find(Employee.class, emp.getId());
            
            // delete the Employee from the db
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();

            // refresh the Employee - should fail with EntityNotFoundException
            em.refresh(emp);
            fail("entityManager.refresh(removedObject) didn't throw exception");
        } catch (EntityNotFoundException entityNotFoundException) {
            em.getTransaction().rollback();
            // expected behaviour
        } catch (Exception exception ) {
            em.getTransaction().rollback();
            fail("entityManager.refresh(removedObject) threw a wrong exception: " + exception.getMessage());
        }
    }
    
    //Bug3323, EM refresh process should invalidate the shared cached object that not exists in DB.
    public void testRefreshInvalidateDeletedObject(){
        Object obj;
        EntityManager em1 = createEntityManager();
        EntityManager em2 = createEntityManager();
        Address address = new Address();
        address.setCity("Kanata");
        // persist the Address
        try {
            
            //Ensure shared cache being used.
            boolean isIsolated = ((EntityManagerImpl)em1).getServerSession().getClassDescriptorForAlias("Address").isIsolated();
            if(isIsolated){
                throw new Exception("This test should use non-isolated cache setting class descriptor for test.");
            }
            
            em1.getTransaction().begin();
            em1.persist(address);
            em1.getTransaction().commit();
            
            //Cache the Address
            em1 = createEntityManager();
            em1.getTransaction().begin();
            address = em1.find(Address.class, address.getId());

            // Delete Address outside of JPA so that the object still stored in the cache.
            em2 = createEntityManager();
            em2.getTransaction().begin();
            em2.createNativeQuery("DELETE FROM CMP3_ADDRESS a where a.address_id = ?1").setParameter(1, address.getId()).executeUpdate();
            em2.getTransaction().commit();
            
            //Call refresh to invalidate the object
            em1.refresh(address);
        }catch (Exception e){
            //expected exception
        } finally{
            if (em1.getTransaction().isActive()) {
                em1.getTransaction().rollback();
            }
        }
        
        //Verify
        em1.getTransaction().begin();
        address=em1.find(Address.class, address.getId());
        em1.getTransaction().commit();
        
        assertNull("The deleted object is still valid in share cache", address);
        
    }


    public void testCacheUsage(){
        EntityManager em = createEntityManager();
        Employee emp = new Employee();
        emp.setFirstName("Mark");
        // persist the Employee
        try{
            em.getTransaction().begin();
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }
    	clearCache();
        em.getTransaction().begin();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e").getResultList();
        em.getTransaction().commit();
        Object obj = ((oracle.toplink.essentials.ejb.cmp3.EntityManager)em).getServerSession().getIdentityMapAccessor().getFromIdentityMap(result.get(0));
        assertTrue("Failed to load the object into the shared cache when there were no changes in the UOW", obj != null);
        try{
            em.getTransaction().begin();
            emp = em.find(Employee.class, emp.getId());
            em.remove(emp);
            em.getTransaction().commit();
        }catch (RuntimeException t){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw t;
        }
    }
    
    public void testContainsRemoved() {
        // find an existing or create a new Employee
        String firstName = "testContainsRemoved";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            try{
                em.getTransaction().begin();
                em.persist(emp);
                em.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw ex;
            }
        }
        
        boolean containsRemoved = true;
        try{
            em.getTransaction().begin();
            emp = em.find(Employee.class, emp.getId());
            em.remove(emp);
            containsRemoved = em.contains(emp);
            em.getTransaction().commit();
        }catch (RuntimeException t){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw t;
        }
        
        assertFalse("entityManager.contains(removedObject)==true ", containsRemoved);
    }

    public void testFlushModeEmAutoQueryCommit() {
        internalTestFlushMode(FlushModeType.AUTO, FlushModeType.COMMIT);
    }
    
    public void testFlushModeEmAuto() {
        internalTestFlushMode(FlushModeType.AUTO, null);
    }
    
    public void testFlushModeEmAutoQueryAuto() {
        internalTestFlushMode(FlushModeType.AUTO, FlushModeType.AUTO);
    }
    
    public void testFlushModeEmCommitQueryCommit() {
        internalTestFlushMode(FlushModeType.COMMIT, FlushModeType.COMMIT);
    }
    
    public void testFlushModeEmCommit() {
        internalTestFlushMode(FlushModeType.COMMIT, null);
    }
    
    public void testFlushModeEmCommitQueryAuto() {
        internalTestFlushMode(FlushModeType.COMMIT, FlushModeType.AUTO);
    }
    
    public void internalTestFlushMode(FlushModeType emFlushMode, FlushModeType queryFlushMode) {
        // create a new Employee
        String firstName = "testFlushMode";

        // make sure no Employee with the specified firstName exists.
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }
        clearCache();
        
        Employee emp;
        Query query = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName like '"+firstName+"'");
        if(queryFlushMode != null) {
            query.setFlushMode(queryFlushMode);
        }
        FlushModeType emFlushModeOriginal = em.getFlushMode();
        em.setFlushMode(emFlushMode);

        // create a new Employee
        emp = new Employee();
        emp.setFirstName(firstName);
        boolean flushed = true;
        Employee result = null;
        try{
            em.getTransaction().begin();
            em.persist(emp);
            result = (Employee) query.getSingleResult();
        } catch (javax.persistence.NoResultException ex) {
            // failed to flush to database
            flushed = false;
        } finally {
            em.getTransaction().rollback();
            em.setFlushMode(emFlushModeOriginal);
        }
        
        boolean shouldHaveFlushed;
        if(queryFlushMode != null) {
            shouldHaveFlushed = queryFlushMode == FlushModeType.AUTO;
        } else {
            shouldHaveFlushed = emFlushMode == FlushModeType.AUTO;
        }
        if(shouldHaveFlushed != flushed) {
            if(flushed) {
                fail("Flushed to database");
            } else {
                fail("Failed to flush to database");
            }
        }
        
    }

    public void testFlushModeOnUpdateQuery() {
        // find an existing or create a new Employee
        String firstName = "testFlushModeOnUpdateQuery";
        Employee emp;
        EntityManager em = createEntityManager();
        Query readQuery = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.phoneNumbers IS EMPTY and e.firstName like '"+firstName+"'");
        Query updateQuery = em.createQuery("UPDATE Employee e set e.salary = 100 where e.firstName like '" + firstName + "'");
        updateQuery.setFlushMode(FlushModeType.AUTO);
        emp = new Employee();
        emp.setFirstName(firstName);
        try{
            try{
                em.getTransaction().begin();
                em.persist(emp);
                updateQuery.executeUpdate();
                Employee result = (Employee) readQuery.getSingleResult();
            }catch (javax.persistence.EntityNotFoundException ex){
                em.getTransaction().rollback();
                fail("Failed to flush to database");
            }
            em.refresh(emp);
            assertTrue("Failed to flush to Database", emp.getSalary() == 100);
            em.remove(emp);
            em.getTransaction().commit();
        }catch(RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }
    }

    public void testSetRollbackOnly(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            Employee emp = new Employee();
            emp.setFirstName("Bob");
            emp.setLastName("Fisher");
            em.persist(emp);
            emp = new Employee();
            emp.setFirstName("Anthony");
            emp.setLastName("Walace");
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        EntityManager localEm = createEntityManager();
        localEm.getTransaction().begin();
        List result = localEm.createQuery("SELECT e from Employee e").getResultList();
        Employee emp = (Employee)result.get(0);
        Employee emp2 = (Employee)result.get(1);
        String newName = ""+System.currentTimeMillis();
        emp2.setFirstName(newName);
        localEm.flush();
        emp2.setLastName("Whatever");
        emp2.setVersion(0);
        try{
            localEm.flush();
        }catch (Exception ex){
            localEm.clear(); //prevent the flush again
            String eName = (String)localEm.createQuery("SELECT e.firstName from Employee e where e.id = " + emp2.getId()).getSingleResult();
            assertTrue("Failed to keep txn open for set RollbackOnly", eName.equals(newName));
        }
        try{
            assertTrue("Failed to mark txn rollback only", localEm.getTransaction().getRollbackOnly());
        }finally{
            try{
                localEm.getTransaction().commit();
            }catch (RollbackException ex){
                return;    
            }catch (RuntimeException ex){
                localEm.getTransaction().rollback();
                throw ex;
                
            }
        }
        fail("Failed to throw rollback exception");
    }
    
    public void testSubString() {
        // find an existing or create a new Employee
        String firstName = "testSubString";
        Employee emp;
        EntityManager em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
        if(!result.isEmpty()) {
            emp = (Employee)result.get(0);
        } else {
            emp = new Employee();
            emp.setFirstName(firstName);
            // persist the Employee
            try{
                em.getTransaction().begin();
                em.persist(emp);
                em.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw ex;
            }
        }
        
        int firstIndex = 1;
        int lastIndex = firstName.length();
        List employees = em.createQuery("SELECT object(e) FROM Employee e where e.firstName = substring(:p1, :p2, :p3)").
            setParameter("p1", firstName).
            setParameter("p2", new Integer(firstIndex)).
            setParameter("p3", new Integer(lastIndex)).
            getResultList();
            
        // clean up
        try{
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        }

        assertFalse("employees.isEmpty()==true ", employees.isEmpty());
    }
    
    public void testDatabaseSyncNewObject() {
        EntityManager em = createEntityManager();

        em.getTransaction().begin();

        try{
            Project project = new LargeProject();
            em.persist(project);
            project.setName("Blah");
            project.setTeamLeader(new Employee());
            project.getTeamLeader().addProject(project);
            em.flush();
        }catch (IllegalStateException ex){
            em.getTransaction().rollback();
            return;
        }
        
        fail("Failed to throw illegal argument when finding unregistered new object cascading on database sync");

    }

    public void testTransactionRequired() {
        String firstName = "testTransactionRequired";
        Employee emp = new Employee();
        emp.setFirstName(firstName);
        
        String noException = "";
        String wrongException = "";
        
        try {
            createEntityManager().flush();
            noException = noException + " flush;";
        } catch (TransactionRequiredException transactionRequiredException) {
            // expected behaviour
        } catch (RuntimeException ex) {
            wrongException = wrongException + " flush: " + ex.getMessage() +";";
        }

        String errorMsg = "";
        if(noException.length() > 0) {
            errorMsg = "No exception thrown: " + noException;
        }
        if(wrongException.length() > 0) {
            if(errorMsg.length() > 0) {
                errorMsg = errorMsg + " ";
            }
            errorMsg = errorMsg + "Wrong exception thrown: " + wrongException;
        }
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testIdentityInsideTransaction() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        Query query = em.createQuery("SELECT e FROM PhoneNumber e");
        List<PhoneNumber> phoneNumbers = query.getResultList();
        for (PhoneNumber phoneNumber : phoneNumbers) {
            Employee emp = phoneNumber.getOwner();
            Collection<PhoneNumber> numbers = emp.getPhoneNumbers();
            assertTrue(numbers.contains(phoneNumber));
        }
        
        em.getTransaction().commit();
        em.close();
    }

    public void testIdentityOutsideTransaction() {
        EntityManager em = createEntityManager();
        
        Query query = em.createQuery("SELECT e FROM PhoneNumber e");
        List<PhoneNumber> phoneNumbers = query.getResultList();
        for (PhoneNumber phoneNumber : phoneNumbers) {
            Employee emp = phoneNumber.getOwner();
            Collection<PhoneNumber> numbers = emp.getPhoneNumbers();
            assertTrue(numbers.contains(phoneNumber));
        }
        
        em.close();
    } 
    
    public void testIgnoreRemovedObjectsOnDatabaseSync() {
        EntityManager em = createEntityManager();
        Query phoneQuery = em.createQuery("Select p from PhoneNumber p where p.owner.lastName like 'Dow%'");
        Query empQuery = em.createQuery("Select e from Employee e where e.lastName like 'Dow%'");

        em.getTransaction().begin();
        //--setup
        try{
            Employee emp = new Employee();
            emp.setLastName("Dowder");
            PhoneNumber phone = new PhoneNumber("work", "613", "5555555");
            emp.addPhoneNumber(phone);
            phone = new PhoneNumber("home", "613", "4444444");
            emp.addPhoneNumber(phone);
            Address address = new Address("SomeStreet", "somecity", "province", "country", "postalcode");
            emp.setAddress(address);
            em.persist(emp);
            em.flush();
    
            emp = new Employee();
            emp.setLastName("Dows");
            phone = new PhoneNumber("work", "613", "2222222");
            emp.addPhoneNumber(phone);
            phone = new PhoneNumber("home", "613", "1111111");
            emp.addPhoneNumber(phone);
            address = new Address("street1", "city1", "province1", "country1", "postalcode1");
            emp.setAddress(address);
            em.persist(emp);
            em.flush();
            //--end setup
    
            List<Employee> emps = empQuery.getResultList();
    
            List phones = phoneQuery.getResultList();
            for (Iterator iterator = phones.iterator(); iterator.hasNext();){
                em.remove(iterator.next());
            }
            em.flush();
            
            for (Iterator<Employee> iterator = emps.iterator(); iterator.hasNext();){
                em.remove(iterator.next());
            }
        }catch (RuntimeException ex){
            em.getTransaction().rollback();
            throw ex;
        }
        try{
            em.flush();
        }catch (IllegalStateException ex){
            em.getTransaction().rollback();
            em.close();
            em = createEntityManager();
            em.getTransaction().begin();
            try{
                phoneQuery = em.createQuery("Select p from PhoneNumber p where p.owner.lastName like 'Dow%'");
                empQuery = em.createQuery("Select e from Employee e where e.lastName like 'Dow%'");
                List<Employee> emps =  empQuery.getResultList();
                List phones = phoneQuery.getResultList();
                for (Iterator iterator = phones.iterator(); iterator.hasNext();){
                    em.remove(iterator.next());
                }
                for (Iterator<Employee> iterator = emps.iterator(); iterator.hasNext();){
                    em.remove(iterator.next());
                }
                em.getTransaction().commit();
            }catch (RuntimeException re){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw re;
            }
            fail("Failed to ignore the removedobject when cascading on database sync");
        }
        
        em.getTransaction().commit();
    }
    
    public void testREADLock(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Employee employee = null;
        try{
            employee = new Employee();
            employee.setFirstName("Mark");
            employee.setLastName("Madsen");
            em.persist(employee);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        EntityManager em2 = createEntityManager();
        Exception optimisticLockException = null;
       
        em.getTransaction().begin();
        try{
            employee = em.find(Employee.class, employee.getId());
            em.lock(employee, LockModeType.READ);
            em2.getTransaction().begin();
            try{
                Employee employee2 = em2.find(Employee.class, employee.getId());
                employee2.setFirstName("Michael");
                em2.getTransaction().commit();
                em2.close();
            }catch (RuntimeException ex){
                em2.getTransaction().rollback();
                em2.close();
                throw ex;
            }
        
            try{
                em.flush();
            } catch (PersistenceException exception) {
                if (exception instanceof OptimisticLockException){
                    optimisticLockException = exception;
                }else{
                    throw exception;
                }
            }
            em.getTransaction().rollback();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        em.getTransaction().begin();
        try{
            employee = em.find(Employee.class, employee.getId());
            em.remove(employee);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        if (optimisticLockException == null){
            fail("Proper exception not thrown when EntityManager.lock(object, READ) is used.");
        }
    }
    
    // test for bug 4676587: 
    // CTS: AFTER A REMOVE THEN A PERSIST ON THE SAME ENTITY, CONTAINS RETURNS FALSE
    // The test performs persist, remove, persist sequence on a single object
    // in different "flavours":
    // doTransaction - the first persist happens in a separate transaction;
    // doFirstFlush - perform flush after the first persist;
    // doSecondFlush - perform flush after the remove;
    // doThirdFlush - perform flush after the second persist;
    // doRollback - rollbacks transaction that contains remove and the second persist.
    public void testPersistRemoved() {
        // create an Employee
        String firstName = "testPesistRemoved";
        Employee emp = new Employee();
        emp.setFirstName(firstName);

        // make sure no Employee with the specified firstName exists.
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        String errorMsg = "";
        for (int i=0; i < 32; i++) {
            int j = i;
            boolean doRollback = j % 2 == 0;
            j = j/2;
            boolean doThirdFlush = j % 2 == 0;
            j = j/2;
            boolean doSecondFlush = j % 2 == 0;
            j = j/2;
            boolean doFirstFlush = j % 2 == 0;
            j = j/2;
            boolean doTransaction = j % 2 == 0;
            if(doTransaction && doFirstFlush) {
                continue;
            }
            String msg = "";
            if(doTransaction) {
                msg = "Transaction ";
            }
            if(doFirstFlush) {
                msg = msg + "firstFlush ";
            }
            if(doSecondFlush) {
                msg = msg + "secondFlush ";
            }
            if(doThirdFlush) {
                msg = msg + "thirdFlush ";
            }
            if(doRollback) {
                msg = msg + "RolledBack ";
            }

            String localErrorMsg = msg;
            boolean exceptionWasThrown = false;
            Integer empId = null;
            em.getTransaction().begin();            
            try {
                emp = new Employee();
                emp.setFirstName(firstName);

                // persist the Employee
                em.persist(emp);
                if(doTransaction) {
                    em.getTransaction().commit();
                    empId = emp.getId();
                    em.getTransaction().begin();
                } else {
                    if(doFirstFlush) {
                        em.flush();
                    }
                }
        
                if(doTransaction) {
                    emp = em.find(Employee.class, empId);
                }
                // remove the Employee
                em.remove(emp);
                if(doSecondFlush) {
                    em.flush();
                }
        
                // persist the Employee
                em.persist(emp);
                if(doThirdFlush) {
                    em.flush();
                }
            } catch (RuntimeException ex) {
                em.getTransaction().rollback();
                localErrorMsg = localErrorMsg + " " + ex.getMessage() + ";";
                exceptionWasThrown = true;
            }
        
            boolean employeeShouldExist = doTransaction || !doRollback;
            boolean employeeExists = false;
            try{
                if(!exceptionWasThrown) {
                    if(doRollback) {
                        em.getTransaction().rollback();
                    } else {
                        em.getTransaction().commit();
                    }
                    
                    if(doTransaction) {
                        Employee employeeReadFromCache = (Employee)em.find(Employee.class, empId);
                        if(employeeReadFromCache == null) {
                            localErrorMsg = localErrorMsg + " employeeReadFromCache == null;";
                        }
                    }
                    
                    List resultList = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
                    employeeExists = resultList.size() > 0;
                    
                    if(employeeShouldExist) {
                        if(resultList.size() > 1) {
                            localErrorMsg = localErrorMsg + " resultList.size() > 1";
                        }
                        if(!employeeExists) {
                            localErrorMsg = localErrorMsg + " employeeReadFromDB == null;";
                        }
                    } else {
                        if(resultList.size() > 0) {
                            localErrorMsg = localErrorMsg + " employeeReadFromDB != null;";
                        }
                    }
                }
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                throw ex;
            }
            
            // clean up
            if(employeeExists || exceptionWasThrown) {
                em = createEntityManager();
                em.getTransaction().begin();
                try{
                    em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
                    em.getTransaction().commit();
                }catch (RuntimeException ex){
                    em.getTransaction().rollback();
                    throw ex;
                }
            }
            
            if(!msg.equals(localErrorMsg)) {
                errorMsg = errorMsg + "i="+Integer.toString(i)+": "+ localErrorMsg + " ";
            }
        }
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }

    public void testPersistManagedException(){       
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Employee emp = new Employee();
        em.persist(emp);
        em.flush();
        Integer id = emp.getId();
        emp = new Employee();
        emp.setId(id);
        boolean caughtException = false;
        try{
            em.persist(emp);
        } catch (EntityExistsException e){
            caughtException = true;
        }
        emp = em.find(Employee.class, id);
        em.remove(emp);
        em.getTransaction().rollback();
        assertTrue("EntityExistsException was not thrown for an existing Employee.", caughtException);
    }
    
    public void testPersistManagedNoException(){       
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        Employee emp = new Employee();
        em.persist(emp);
        em.flush();
        Integer id = emp.getId();
        Address address = new Address();
        emp.setAddress(address);
        boolean caughtException = false;
        try{
            em.persist(emp);
        } catch (EntityExistsException e){
            caughtException = true;
        }
        emp = em.find(Employee.class, id);
        em.remove(emp);
        em.getTransaction().commit();
        assertFalse("EntityExistsException was thrown for a registered Employee.", caughtException);
    }

    // test for bug 4676587: 
    // CTS: AFTER A REMOVE THEN A PERSIST ON THE SAME ENTITY, CONTAINS RETURNS FALSE
    public void testRemoveFlushPersistContains() {
        // create an Employee
        String firstName = "testRemoveFlushPersistContains";
        Employee emp = new Employee();
        emp.setFirstName(firstName);

        // make sure no Employee with the specified firstName exists.
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // persist
        em.getTransaction().begin();
        try{
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        // remove, flush, persist, contains
        boolean contains = false;
        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());
            em.remove(emp); 
            em.flush(); 
            em.persist(emp); 
            contains = em.contains(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        // clean up
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        assertTrue("contains==false", contains);
    }
    
    // test for bug 4742161: 
    // CTS: OBJECTS REMOVED AND THEN FLUSHED ARE RETURNED BY QUERIES AFTER THE FLUSH
    public void testRemoveFlushFind() {
        // create an Employee
        String firstName = "testRemoveFlushFind";
        Employee emp = new Employee();
        emp.setFirstName(firstName);

        // make sure no Employee with the specified firstName exists.
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // persist
        em.getTransaction().begin();
        try{
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        // remove, flush, persist, contains
        boolean foundAfterFlush = true;
        boolean foundBeforeFlush = true;
        em.getTransaction().begin();
        try{
            emp = em.find(Employee.class, emp.getId());
            em.remove(emp); 
            Employee empFound = em.find(Employee.class, emp.getId());
            foundBeforeFlush = empFound != null;
            em.flush(); 
            empFound = em.find(Employee.class, emp.getId());
             foundAfterFlush = empFound != null;
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        // clean up
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        assertFalse("removed object found", foundBeforeFlush);
        assertFalse("removed object found after flush", foundAfterFlush);
    }
    
    // test for bug 4681287: 
    // CTS: EXCEPTION EXPECTED ON FIND() IF PK PASSED IN != ATTRIBUTE TYPE
    public void testFindWithWrongTypePk() {
        EntityManager em = createEntityManager();
        try {
            em.find(Employee.class, "1");
        } catch (IllegalArgumentException ilEx) {
            return;
        } catch (Exception ex) {
            fail("Wrong exception thrown: " + ex.getMessage());
            return;
        }finally{
            em.close();
        }
        fail("No exception thrown");
    }
    
    //test for gf721 - IllegalArgumentException expected for null PK
    public void testFindWithNullPk() {
        EntityManager em = createEntityManager();
        try {
            em.find(Employee.class, null);
        } catch (IllegalArgumentException iae) {
            return;
        } catch (Exception e) {
            fail("Wrong exception type thrown: " + e.getClass());
        }finally{
            em.close();
        }
        fail("No exception thrown when null PK used in find operation.");
    }

    public void testCheckVersionOnMerge() {
        Employee employee = new Employee();
        employee.setFirstName("Marc");
        
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(employee);
            em.getTransaction().commit();
            em.clear();
            em.getTransaction().begin();
            Employee empClone = (Employee) em.find(Employee.class, employee.getId());
            empClone.setFirstName("Guy");
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("Exception caught during test setup " + ex);
        }
        
        try {
            em.getTransaction().begin();
            em.merge(employee);
            em.getTransaction().commit();
        } catch (OptimisticLockException e) {
            em.getTransaction().rollback();
            em.close();
            return;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            fail("Wrong exception thrown: " + ex.getMessage());
        }
            
        fail("No exception thrown");
    }
    
    public void testClear(){
        Employee employee = new Employee();
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.persist(employee);
            em.getTransaction().commit();
            em.clear();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        boolean cleared = !em.contains(employee);
        em.close();
        assertTrue("EntityManager not properly cleared", cleared);
    }
    
    public void testClearWithFlush(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            Employee emp = new Employee();
            emp.setFirstName("Douglas");
            emp.setLastName("McRae");
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        EntityManager localEm = createEntityManager();
        localEm.getTransaction().begin();
        Employee emp = null;
        String originalName = "";
        boolean cleared, updated, reset = false;
        try{
            Query query = localEm.createQuery("Select e from Employee e where e.firstName is not null");
            emp = (Employee)query.getResultList().get(0);
            originalName = emp.getFirstName();
            emp.setFirstName("Bobster");
            localEm.flush();
            localEm.clear();
            //this test is testing the cache not the database
            localEm.getTransaction().commit();
            cleared = !localEm.contains(emp);
            emp = localEm.find(Employee.class, emp.getId());
            updated = emp.getFirstName().equals("Bobster");
            localEm.close();
        }catch (RuntimeException ex){
            localEm.getTransaction().rollback();
            localEm.close();
            throw ex;
        }finally{
            //now clean up
            localEm = createEntityManager();
            localEm.getTransaction().begin();
            emp = localEm.find(Employee.class, emp.getId());
            emp.setFirstName(originalName);
            localEm.getTransaction().commit();
            emp = localEm.find(Employee.class, emp.getId());
            reset = emp.getFirstName().equals(originalName);
            localEm.close();
        }
        assertTrue("EntityManager not properly cleared", cleared);
        assertTrue("flushed data not merged", updated);
        assertTrue("unable to reset", reset);
    }
    
    // gf3596: transactions never release memory until commit, so JVM eventually crashes
    // The test verifies that there's no stale data read after transaction.
    // Because there were no TopLinkProperties.FLUSH_CLEAR_CACHE property passed
    // while creating either EM or EMF the tested behaviour corresponds to
    // the default property value FlushClearCache.DropInvalidate.
    // Note that the same test would pass with FlushClearCache.Merge
    // (in that case all changes are merges into the shared cache after transaction committed),
    // but the test would fail with FlushClearCache.Drop - that mode just drops em cache
    // without doing any invalidation of the shared cache.
    public void testClearWithFlush2() {
        String firstName = "testClearWithFlush2";
        
        // setup
        // create employee and manager - and then remove them from the shared cache
        EntityManager em = createEntityManager();
        int employee_1_NotInCache_id = 0;
        int employee_2_NotInCache_id = 0;
        int manager_NotInCache_id = 0;
        em.getTransaction().begin();
        try {
            Employee employee_1_NotInCache = new Employee();
            employee_1_NotInCache.setFirstName(firstName);
            employee_1_NotInCache.setLastName("Employee_1_NotInCache");
            
            Employee employee_2_NotInCache = new Employee();
            employee_2_NotInCache.setFirstName(firstName);
            employee_2_NotInCache.setLastName("Employee_2_NotInCache");
            
            Employee manager_NotInCache = new Employee();
            manager_NotInCache.setFirstName(firstName);
            manager_NotInCache.setLastName("Manager_NotInCache");
            // employee_1 is manager, employee_2 is not
            manager_NotInCache.addManagedEmployee(employee_1_NotInCache);
            
            // persist
            em.persist(manager_NotInCache);
            em.persist(employee_1_NotInCache);
            em.persist(employee_2_NotInCache);
            em.getTransaction().commit();
            
            employee_1_NotInCache_id = employee_1_NotInCache.getId();
            employee_2_NotInCache_id = employee_2_NotInCache.getId();
            manager_NotInCache_id = manager_NotInCache.getId();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        // remove both manager_NotInCache and employee_NotInCache from the shared cache
        clearCache();
        
        // setup
        // create employee and manager - and keep them in the shared cache
        em = createEntityManager();
        int employee_1_InCache_id = 0;
        int employee_2_InCache_id = 0;
        int manager_InCache_id = 0;
        em.getTransaction().begin();
        try {
            Employee employee_1_InCache = new Employee();
            employee_1_InCache.setFirstName(firstName);
            employee_1_InCache.setLastName("Employee_1_InCache");
            
            Employee employee_2_InCache = new Employee();
            employee_2_InCache.setFirstName(firstName);
            employee_2_InCache.setLastName("Employee_2_InCache");
            
            Employee manager_InCache = new Employee();
            manager_InCache.setFirstName(firstName);
            manager_InCache.setLastName("Manager_InCache");
            // employee_1 is manager, employee_2 is not
            manager_InCache.addManagedEmployee(employee_1_InCache);
            
            // persist
            em.persist(manager_InCache);
            em.persist(employee_1_InCache);
            em.persist(employee_2_InCache);
            em.getTransaction().commit();
            
            employee_1_InCache_id = employee_1_InCache.getId();
            employee_2_InCache_id = employee_2_InCache.getId();
            manager_InCache_id = manager_InCache.getId();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // test
        // create new employee and manager, change existing ones, flush, clear
        em = createEntityManager();
        int employee_1_New_id = 0;
        int employee_2_New_id = 0;
        int employee_3_New_id = 0;
        int employee_4_New_id = 0;
        int manager_New_id = 0;
        em.getTransaction().begin();
        try {
            Employee employee_1_New = new Employee();
            employee_1_New.setFirstName(firstName);
            employee_1_New.setLastName("Employee_1_New");
            em.persist(employee_1_New);
            employee_1_New_id = employee_1_New.getId();
            
            Employee employee_2_New = new Employee();
            employee_2_New.setFirstName(firstName);
            employee_2_New.setLastName("Employee_2_New");
            em.persist(employee_2_New);
            employee_2_New_id = employee_2_New.getId();
            
            Employee employee_3_New = new Employee();
            employee_3_New.setFirstName(firstName);
            employee_3_New.setLastName("Employee_3_New");
            em.persist(employee_3_New);
            employee_3_New_id = employee_3_New.getId();
            
            Employee employee_4_New = new Employee();
            employee_4_New.setFirstName(firstName);
            employee_4_New.setLastName("Employee_4_New");
            em.persist(employee_4_New);
            employee_4_New_id = employee_4_New.getId();
            
            Employee manager_New = new Employee();
            manager_New.setFirstName(firstName);
            manager_New.setLastName("Manager_New");
            em.persist(manager_New);
            manager_New_id = manager_New.getId();

            // find and update all objects created during setup
            Employee employee_1_NotInCache = em.find(Employee.class, employee_1_NotInCache_id);
            employee_1_NotInCache.setLastName(employee_1_NotInCache.getLastName() + "_Updated");
            Employee employee_2_NotInCache = em.find(Employee.class, employee_2_NotInCache_id);
            employee_2_NotInCache.setLastName(employee_2_NotInCache.getLastName() + "_Updated");
            Employee manager_NotInCache = em.find(Employee.class, manager_NotInCache_id);
            manager_NotInCache.setLastName(manager_NotInCache.getLastName() + "_Updated");

            Employee employee_1_InCache = em.find(Employee.class, employee_1_InCache_id);
            employee_1_InCache.setLastName(employee_1_InCache.getLastName() + "_Updated");
            Employee employee_2_InCache = em.find(Employee.class, employee_2_InCache_id);
            employee_2_InCache.setLastName(employee_2_InCache.getLastName() + "_Updated");
            Employee manager_InCache = em.find(Employee.class, manager_InCache_id);
            manager_InCache.setLastName(manager_InCache.getLastName() + "_Updated");

            manager_NotInCache.addManagedEmployee(employee_1_New);
            manager_InCache.addManagedEmployee(employee_2_New);
            
            manager_New.addManagedEmployee(employee_3_New);
            manager_New.addManagedEmployee(employee_2_NotInCache);
            manager_New.addManagedEmployee(employee_2_InCache);

            // flush
            em.flush();
            
            // clear and commit
            em.clear();
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // verify
        String errorMsg = "";
        em = createEntityManager();

        // find and verify all objects created during setup and test

        Employee manager_NotInCache = em.find(Employee.class, manager_NotInCache_id);
        if(!manager_NotInCache.getLastName().endsWith("_Updated")) {
            errorMsg = errorMsg + "manager_NotInCache lastName NOT updated; ";
        }
        Iterator it = manager_NotInCache.getManagedEmployees().iterator();
        while(it.hasNext()) {
            Employee emp = (Employee)it.next();
            if(emp.getId() == employee_1_NotInCache_id) {
                if(!emp.getLastName().endsWith("_Updated")) {
                    errorMsg = errorMsg + "employee_1_NotInCache lastName NOT updated; ";
                }
            } else if(emp.getId() == employee_1_New_id) {
                if(!emp.getLastName().endsWith("_New")) {
                    errorMsg = errorMsg + "employee_1_New lastName wrong; ";
                }
            } else {
                errorMsg = errorMsg + "manager_NotInCache has unexpected employee: lastName = " + emp.getLastName();
            }
        }
        if(manager_NotInCache.getManagedEmployees().size() != 2) {
            errorMsg = errorMsg + "manager_NotInCache.getManagedEmployees().size() != 2; size = " + manager_NotInCache.getManagedEmployees().size();
        }

        Employee manager_InCache = em.find(Employee.class, manager_InCache_id);
        if(!manager_InCache.getLastName().endsWith("_Updated")) {
            errorMsg = errorMsg + "manager_InCache lastName NOT updated; ";
        }
        it = manager_InCache.getManagedEmployees().iterator();
        while(it.hasNext()) {
            Employee emp = (Employee)it.next();
            if(emp.getId() == employee_1_InCache_id) {
                if(!emp.getLastName().endsWith("_Updated")) {
                    errorMsg = errorMsg + "employee_1_InCache lastName NOT updated; ";
                }
            } else if(emp.getId() == employee_2_New_id) {
                if(!emp.getLastName().endsWith("_New")) {
                    errorMsg = errorMsg + "employee_2_New lastName wrong; ";
                }
            } else {
                errorMsg = errorMsg + "manager_InCache has unexpected employee: lastName = " + emp.getLastName();
            }
        }
        if(manager_InCache.getManagedEmployees().size() != 2) {
            errorMsg = errorMsg + "manager_InCache.getManagedEmployees().size() != 2; size = " + manager_InCache.getManagedEmployees().size();
        }

        Employee manager_New = em.find(Employee.class, manager_New_id);
        if(!manager_New.getLastName().endsWith("_New")) {
            errorMsg = errorMsg + "manager_New lastName wrong; ";
        }
        it = manager_New.getManagedEmployees().iterator();
        while(it.hasNext()) {
            Employee emp = (Employee)it.next();
            if(emp.getId() == employee_2_NotInCache_id) {
                if(!emp.getLastName().endsWith("_Updated")) {
                    errorMsg = errorMsg + "employee_2_NotInCache_id lastName NOT updated; ";
                }
            } else if(emp.getId() == employee_2_InCache_id) {
                if(!emp.getLastName().endsWith("_Updated")) {
                    errorMsg = errorMsg + "employee_2_InCache_id lastName NOT updated; ";
                }
            } else if(emp.getId() == employee_3_New_id) {
                if(!emp.getLastName().endsWith("_New")) {
                    errorMsg = errorMsg + "employee_3_New lastName wrong; ";
                }
            } else {
                errorMsg = errorMsg + "manager_New has unexpected employee: lastName = " + emp.getLastName();
            }
        }
        if(manager_New.getManagedEmployees().size() != 3) {
            errorMsg = errorMsg + "manager_InCache.getManagedEmployees().size() != 3; size = " + manager_InCache.getManagedEmployees().size();
        }
        Employee employee_4_New = em.find(Employee.class, employee_4_New_id);
        if(!employee_4_New.getLastName().endsWith("_New")) {
            errorMsg = errorMsg + "employee_4_New lastName wrong; ";
        }
        em.close();
        
        // clean up
        // remove all objects created during this test and clear the cache.
        em = createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            // ignore exception in clean up in case there's an error in test
            if(errorMsg.length() == 0) {
                throw ex;
            }
        }
        clearCache();
        
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    // gf3596: transactions never release memory until commit, so JVM eventually crashes.
    // Attempts to compare memory consumption between the two FlushClearCache modes.
    // If the values changed to be big enough (in TopLink I tried nFlashes = 30 , nInsertsPerFlush = 10000)
    // internalMassInsertFlushClear(FlushClearCache.Merge, 30, 10000) will run out of memory,
    // but internalMassInsertFlushClear(null, 30, 10000) will still be ok.
    public void testMassInsertFlushClear() {
        int nFlushes = 20;
        int nPersistsPerFlush = 50;
        long[] defaultFreeMemoryDelta = internalMassInsertFlushClear(null, nFlushes, nPersistsPerFlush);
        long[] mergeFreeMemoryDelta = internalMassInsertFlushClear(FlushClearCache.Merge, nFlushes, nPersistsPerFlush);
        // disregard the flush if any of the two FreeMemoryDeltas is negative - clearly that's gc artefact.
        int nEligibleFlushes = 0;
        long diff = 0;
        for(int nFlush = 0; nFlush < nFlushes; nFlush++) {
            if(defaultFreeMemoryDelta[nFlush] >= 0 && mergeFreeMemoryDelta[nFlush] >= 0) {
                nEligibleFlushes++;
                diff = diff + mergeFreeMemoryDelta[nFlush] - defaultFreeMemoryDelta[nFlush];
            }
        }
        long lowEstimateOfBytesPerObject = 200;
        long diffPerObject = diff / (nEligibleFlushes * nPersistsPerFlush);
        if(diffPerObject < lowEstimateOfBytesPerObject) {
            fail("difference in freememory per object persisted " + diffPerObject + " is lower than lowEstimateOfBytesPerObject " + lowEstimateOfBytesPerObject);
        }
    }
    
    // memory usage with different FlushClearCache modes.
    protected long[] internalMassInsertFlushClear(String propValue, int nFlushes, int nPersistsPerFlush) {
        // set logDebug to true to output the freeMemory values after each flush/clear
        boolean logDebug = false;
        String firstName = "testMassInsertFlushClear";
        EntityManager em;
        if(propValue == null) {
            // default value FlushClearCache.DropInvalidate will be used
            em = createEntityManager();
            if(logDebug) {
                System.out.println(FlushClearCache.DEFAULT);
            }
        } else {
            HashMap map = new HashMap(1);
            map.put(TopLinkProperties.FLUSH_CLEAR_CACHE, propValue);
            em = getEntityManagerFactory().createEntityManager(map);
            if(logDebug) {
                System.out.println(propValue);
            }
        }
        // For enhance accuracy of memory measuring allocate everything first:
        // make a first run and completely disregard its results - somehow
        // that get freeMemory function to report more accurate results in the second run -
        // which is the only one used to calculate results.
        if(logDebug) {
            System.out.println("The first run is ignored");
        }
        long freeMemoryOld;
        long freeMemoryNew;
        long[] freeMemoryDelta = new long[nFlushes];
        em.getTransaction().begin();
        try {
            // Try to force garbage collection NOW.
            System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
            freeMemoryOld = Runtime.getRuntime().freeMemory();
            if(logDebug) {
                System.out.println("initial freeMemory = " + freeMemoryOld);
            }
            for(int nFlush = 0; nFlush < nFlushes; nFlush++) {
                for(int nPersist = 0; nPersist < nPersistsPerFlush; nPersist++) {
                    Employee emp = new Employee();
                    emp.setFirstName(firstName);
                    int nEmployee = nFlush * nPersistsPerFlush + nPersist;
                    emp.setLastName("lastName_" + Integer.toString(nEmployee));
                    em.persist(emp);
                }
                em.flush();
                em.clear();
                // Try to force garbage collection NOW.
                System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
                freeMemoryNew = Runtime.getRuntime().freeMemory();
                freeMemoryDelta[nFlush] = freeMemoryOld - freeMemoryNew;
                freeMemoryOld = freeMemoryNew;
                if(logDebug) {
                    System.out.println(nFlush +": after flush/clear freeMemory = " + freeMemoryNew);
                }
            }
        } finally {
            em.getTransaction().rollback();
            em = null;
        }

        if(logDebug) {
            System.out.println("The second run");
        }
        // now allocate again - with gc and memory measuring
        if(propValue == null) {
            // default value FlushClearCache.DropInvalidate will be used
            em = createEntityManager();
        } else {
            HashMap map = new HashMap(1);
            map.put(TopLinkProperties.FLUSH_CLEAR_CACHE, propValue);
            em = getEntityManagerFactory().createEntityManager(map);
        }
        em.getTransaction().begin();
        try {
            // Try to force garbage collection NOW.
            System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
            freeMemoryOld = Runtime.getRuntime().freeMemory();
            if(logDebug) {
                System.out.println("initial freeMemory = " + freeMemoryOld);
            }
            for(int nFlush = 0; nFlush < nFlushes; nFlush++) {
                for(int nPersist = 0; nPersist < nPersistsPerFlush; nPersist++) {
                    Employee emp = new Employee();
                    emp.setFirstName(firstName);
                    int nEmployee = nFlush * nPersistsPerFlush + nPersist;
                    emp.setLastName("lastName_" + Integer.toString(nEmployee));
                    em.persist(emp);
                }
                em.flush();
                em.clear();
                // Try to force garbage collection NOW.
                System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
                freeMemoryNew = Runtime.getRuntime().freeMemory();
                freeMemoryDelta[nFlush] = freeMemoryOld - freeMemoryNew;
                freeMemoryOld = freeMemoryNew;
                if(logDebug) {
                    System.out.println(nFlush +": after flush/clear freeMemory = " + freeMemoryNew);
                }
            }
            return freeMemoryDelta;
        } finally {
            em.getTransaction().rollback();
        }
    }
    
    public void testClearInTransaction(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            Employee emp = new Employee();
            emp.setFirstName("Tommy");
            emp.setLastName("Marsh");
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        EntityManager localEm = createEntityManager();
        localEm.getTransaction().begin();
        Employee emp = null;
        String originalName = "";
        try{
            Query query = localEm.createQuery("Select e from Employee e where e.firstName is not null");
            emp = (Employee)query.getResultList().get(0);
            originalName = emp.getFirstName();
            emp.setFirstName("Bobster");
            localEm.clear();
            localEm.getTransaction().commit();
        }catch (RuntimeException ex){
            localEm.getTransaction().rollback();
            localEm.close();
            throw ex;
        }
        boolean cleared = !localEm.contains(emp);
        emp = localEm.find(Employee.class, emp.getId());
        localEm.close();
        assertTrue("EntityManager not properly cleared", cleared);
        assertTrue("Employee was updated although EM was cleared", emp.getFirstName().equals(originalName));
    }
    
    public void testExtendedPersistenceContext() {
        String firstName = "testExtendedPersistenceContext";
        int originalSalary = 0;

        Employee empNew = new Employee();
        empNew.setFirstName(firstName);
        empNew.setLastName("new");
        empNew.setSalary(originalSalary);
        
        Employee empToBeRemoved = new Employee();
        empToBeRemoved.setFirstName(firstName);
        empToBeRemoved.setLastName("toBeRemoved");
        empToBeRemoved.setSalary(originalSalary);
        
        Employee empToBeRefreshed = new Employee();
        empToBeRefreshed.setFirstName(firstName);
        empToBeRefreshed.setLastName("toBeRefreshed");
        empToBeRefreshed.setSalary(originalSalary);
        
        Employee empToBeMerged = new Employee();
        empToBeMerged.setFirstName(firstName);
        empToBeMerged.setLastName("toBeMerged");
        empToBeMerged.setSalary(originalSalary);
        
        // setup: make sure no Employee with the specified firstName exists and create the existing employees.
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
            em.getTransaction().begin();
            em.persist(empToBeRemoved);
            em.persist(empToBeRefreshed);
            em.persist(empToBeMerged);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        em.close();
        clearCache();
        
        // create entityManager with extended Persistence Context.
        em = createEntityManager();
        
        try {
            // first test
            // without starting transaction persist, remove, refresh, merge
    
            em.persist(empNew);
            
            Employee empToBeRemovedExtended = em.find(Employee.class, empToBeRemoved.getId());
            em.remove(empToBeRemovedExtended);
            
            Employee empToBeRefreshedExtended = em.find(Employee.class, empToBeRefreshed.getId());
            int newSalary = 100;
            // Use another EntityManager to alter empToBeRefreshed in the db
            em.getTransaction().begin();
            empToBeRefreshed = em.find(Employee.class, empToBeRefreshed.getId());
            empToBeRefreshed.setSalary(newSalary);
            em.getTransaction().commit();
            // now refesh
            em.refresh(empToBeRefreshedExtended);
    
            Employee empToBeMergedExtended = em.find(Employee.class, empToBeMerged.getId());
            // alter empToBeRefreshed
            empToBeMerged.setSalary(newSalary);
            // now merge
            em.merge(empToBeMerged);
    
            // begin and commit transaction
            em.getTransaction().begin();
            em.getTransaction().commit();
            
            // verify objects are correct in the PersistenceContext after transaction
            if(!em.contains(empNew)) {
                fail("empNew gone from extended PersistenceContext after transaction committed");
            }
            if(em.contains(empToBeRemovedExtended)) {
                fail("empToBeRemovedExtended still in extended PersistenceContext after transaction committed");
            }
            if(!em.contains(empToBeRefreshedExtended)) {
                fail("empToBeRefreshedExtended gone from extended PersistenceContext after transaction committed");
            } else if(empToBeRefreshedExtended.getSalary() != newSalary) {
                fail("empToBeRefreshedExtended still has the original salary after transaction committed");
            }
            if(!em.contains(empToBeMergedExtended)) {
                fail("empToBeMergedExtended gone from extended PersistenceContext after transaction committed");
            } else if(empToBeMergedExtended.getSalary() != newSalary) {
                fail("empToBeMergedExtended still has the original salary after transaction committed");
            }
    
            // verify objects are correct in the db after transaction
            clearCache();
            Employee empNewFound = em.find(Employee.class, empNew.getId());
            if(empNewFound == null) {
                fail("empNew not in the db after transaction committed");
            }
            Employee empToBeRemovedFound = em.find(Employee.class, empToBeRemoved.getId());
            if(empToBeRemovedFound != null) {
                fail("empToBeRemoved is still in the db after transaction committed");
            }
            Employee empToBeRefreshedFound = em.find(Employee.class, empToBeRefreshed.getId());
            if(empToBeRefreshedFound == null) {
                fail("empToBeRefreshed not in the db after transaction committed");
            } else if(empToBeRefreshedFound.getSalary() != newSalary) {
                fail("empToBeRefreshed still has the original salary in the db after transaction committed");
            }
            Employee empToBeMergedFound = em.find(Employee.class, empToBeMerged.getId());
            if(empToBeMergedFound == null) {
                fail("empToBeMerged not in the db after transaction committed");
            } else if(empToBeMergedFound.getSalary() != newSalary) {
                fail("empToBeMerged still has the original salary in the db after transaction committed");
            }
    
            // second test
            // without starting transaction persist, remove, refresh, merge for the second time:
            // now return to the original state of the objects:
            // remove empNew, persist empToBeRemoved, set empToBeRefreshed and empToBeMerged the original salary.
            
            em.persist(empToBeRemoved);
            em.remove(empNew);
            
            // Use another EntityManager to alter empToBeRefreshed in the db
            em.getTransaction().begin();
            empToBeRefreshed = em.find(Employee.class, empToBeRefreshed.getId());
            empToBeRefreshed.setSalary(originalSalary);
            em.getTransaction().commit();
            // now refesh
            em.refresh(empToBeRefreshedExtended);
    
            // alter empToBeRefreshedFound - can't use empToBeRefreshed here because of its older version().
            empToBeMergedFound.setSalary(originalSalary);
            // now merge
            em.merge(empToBeMergedFound);
    
            // begin and commit the second transaction
            em.getTransaction().begin();
            em.getTransaction().commit();
            
            // verify objects are correct in the PersistenceContext
            if(em.contains(empNew)) {
                fail("empNew not gone from extended PersistenceContext after the second transaction committed");
            }
            if(!em.contains(empToBeRemoved)) {
                fail("empToBeRemoved is not in extended PersistenceContext after the second transaction committed");
            }
            if(!em.contains(empToBeRefreshedExtended)) {
                fail("empToBeRefreshedExtended gone from extended PersistenceContext after the second transaction committed");
            } else if(empToBeRefreshedExtended.getSalary() != originalSalary) {
                fail("empToBeRefreshedExtended still doesn't have the original salary after the second transaction committed");
            }
            if(!em.contains(empToBeMergedExtended)) {
                fail("empToBeMergedExtended gone from extended PersistenceContext after the second transaction committed");
            } else if(empToBeMergedExtended.getSalary() != originalSalary) {
                fail("empToBeMergedExtended doesn't have the original salary after the second transaction committed");
            }
    
            // verify objects are correct in the db
            clearCache();
            Employee empNewFound2 = em.find(Employee.class, empNew.getId());
            if(empNewFound2 != null) {
                fail("empNew still in the db after the second transaction committed");
            }
            Employee empToBeRemovedFound2 = em.find(Employee.class, empToBeRemoved.getId());
            if(empToBeRemovedFound2 == null) {
                fail("empToBeRemoved is not in the db after the second transaction committed");
            }
            Employee empToBeRefreshedFound2 = em.find(Employee.class, empToBeRefreshed.getId());
            if(empToBeRefreshedFound2 == null) {
                fail("empToBeRefreshed not in the db after the second transaction committed");
            } else if(empToBeRefreshedFound2.getSalary() != originalSalary) {
                fail("empToBeRefreshed doesn't have the original salary in the db after the second transaction committed");
            }
            Employee empToBeMergedFound2 = em.find(Employee.class, empToBeMerged.getId());
            if(empToBeMergedFound2 == null) {
                fail("empToBeMerged not in the db after the second transaction committed");
            } else if(empToBeMergedFound2.getSalary() != originalSalary) {
                fail("empToBeMerged doesn't have the original salary in the db after the second transaction committed");
            }
            
            // third test
            // without starting transaction persist, remove, refresh, merge
            // The same as the first test - but now we'll rollback.
            // The objects should be detached.
    
            em.getTransaction().begin();
            em.persist(empNew);
            em.remove(empToBeRemoved);
            
            // Use another EntityManager to alter empToBeRefreshed in the db
            EntityManager em2 = createEntityManager();
            em2.getTransaction().begin();
            try{
                empToBeRefreshed = em2.find(Employee.class, empToBeRefreshed.getId());
                empToBeRefreshed.setSalary(newSalary);
                em2.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em2.getTransaction().isActive()){
                    em2.getTransaction().rollback();
                }
                throw ex;
            }finally{
                em2.close();
            }
            // now refesh
            em.refresh(empToBeRefreshedExtended);
    
            // alter empToBeRefreshed
            empToBeMergedFound2.setSalary(newSalary);
            // now merge
            em.merge(empToBeMergedFound2);
    
            // flush and ROLLBACK the third transaction
            em.flush();
            em.getTransaction().rollback();
            
            // verify objects are correct in the PersistenceContext after the third transaction rolled back
            if(em.contains(empNew)) {
                fail("empNew is still in extended PersistenceContext after the third transaction rolled back");
            }
            if(em.contains(empToBeRemoved)) {
                fail("empToBeRemoved is still in extended PersistenceContext after the third transaction rolled back");
            }
            if(em.contains(empToBeRefreshedExtended)) {
                fail("empToBeRefreshedExtended is still in extended PersistenceContext after the third transaction rolled back");
            } else if(empToBeRefreshedExtended.getSalary() != newSalary) {
                fail("empToBeRefreshedExtended still has the original salary after third transaction rolled back");
            }
            if(em.contains(empToBeMergedExtended)) {
                fail("empToBeMergedExtended is still in extended PersistenceContext after the third transaction rolled back");
            } else if(empToBeMergedExtended.getSalary() != newSalary) {
                fail("empToBeMergedExtended still has the original salary after third transaction rolled back");
            }
    
            // verify objects are correct in the db after the third transaction rolled back
            clearCache();
            Employee empNewFound3 = em.find(Employee.class, empNew.getId());
            if(empNewFound3 != null) {
                fail("empNew is in the db after the third transaction rolled back");
            }
            Employee empToBeRemovedFound3 = em.find(Employee.class, empToBeRemoved.getId());
            if(empToBeRemovedFound3 == null) {
                fail("empToBeRemoved not in the db after the third transaction rolled back");
            }
            Employee empToBeRefreshedFound3 = em.find(Employee.class, empToBeRefreshed.getId());
            if(empToBeRefreshedFound3 == null) {
                fail("empToBeRefreshed not in the db after the third transaction rolled back");
            } else if(empToBeRefreshedFound3.getSalary() != newSalary) {
                fail("empToBeRefreshed has the original salary in the db after the third transaction rolled back");
            }
            Employee empToBeMergedFound3 = em.find(Employee.class, empToBeMerged.getId());
            if(empToBeMergedFound3 == null) {
                fail("empToBeMerged not in the db after the third transaction rolled back");
            } else if(empToBeMergedFound3.getSalary() != originalSalary) {
                fail("empToBeMerged still doesn't have the original salary in the db after the third transaction rolled back");
            }
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
    
    public void testReadTransactionIsolation_CustomUpdate() {
        internalTestReadTransactionIsolation(false, false, false, false);
    }
    public void testReadTransactionIsolation_CustomUpdate_Flush() {
        internalTestReadTransactionIsolation(false, false, false, true);
    }
    public void testReadTransactionIsolation_CustomUpdate_Refresh() {
        internalTestReadTransactionIsolation(false, false, true, false);
    }
    public void testReadTransactionIsolation_CustomUpdate_Refresh_Flush() {
        internalTestReadTransactionIsolation(false, false, true, true);
    }
    public void testReadTransactionIsolation_UpdateAll() {
        internalTestReadTransactionIsolation(false, true, false, false);
    }
    public void testReadTransactionIsolation_UpdateAll_Flush() {
        internalTestReadTransactionIsolation(false, true, false, true);
    }
    public void testReadTransactionIsolation_UpdateAll_Refresh() {
        internalTestReadTransactionIsolation(false, true, true, false);
    }
    public void testReadTransactionIsolation_UpdateAll_Refresh_Flush() {
        internalTestReadTransactionIsolation(false, true, true, true);
    }
    public void testReadTransactionIsolation_OriginalInCache_CustomUpdate() {
        internalTestReadTransactionIsolation(true, false, false, false);
    }
    public void testReadTransactionIsolation_OriginalInCache_CustomUpdate_Flush() {
        internalTestReadTransactionIsolation(true, false, false, true);
    }
    public void testReadTransactionIsolation_OriginalInCache_CustomUpdate_Refresh() {
        internalTestReadTransactionIsolation(true, false, true, false);
    }
    public void testReadTransactionIsolation_OriginalInCache_CustomUpdate_Refresh_Flush() {
        internalTestReadTransactionIsolation(true, false, true, true);
    }
    public void testReadTransactionIsolation_OriginalInCache_UpdateAll() {
        internalTestReadTransactionIsolation(true, true, false, false);
    }
    public void testReadTransactionIsolation_OriginalInCache_UpdateAll_Flush() {
        internalTestReadTransactionIsolation(true, true, false, true);
    }
    public void testReadTransactionIsolation_OriginalInCache_UpdateAll_Refresh() {
        internalTestReadTransactionIsolation(true, true, true, false);
    }
    public void testReadTransactionIsolation_OriginalInCache_UpdateAll_Refresh_Flush() {
        internalTestReadTransactionIsolation(true, true, true, true);
    }
    
    protected void internalTestReadTransactionIsolation(boolean shouldOriginalBeInParentCache, boolean shouldUpdateAll, boolean shouldRefresh, boolean shouldFlush) {
        //setup
        String firstName = "testReadTransactionIsolation";
        
        // make sure no Employee with the specified firstName exists.
        EntityManager em = createEntityManager();
        Query deleteQuery = em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'");        
        em.getTransaction().begin();
        try{
            deleteQuery.executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        em.clear();
        
        // create and persist the object
        String lastNameOriginal = "Original";
        int salaryOriginal = 0;
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastNameOriginal);
        employee.setSalary(salaryOriginal);
        em.getTransaction().begin();
        try{
            em.persist(employee);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        if(!shouldOriginalBeInParentCache) {
            clearCache();
        }
        em.clear();
        
        Query selectQuery = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'");
        Employee employeeUOW = null;

        int salaryNew = 100;
        String lastNameNew = "New";

        em.getTransaction().begin();
        
        try{
            if(shouldRefresh) {
                String lastNameAlternative = "Alternative";
                int salaryAlternative = 50;
                employeeUOW = (Employee)selectQuery.getSingleResult();
                employeeUOW.setLastName(lastNameAlternative);
                employeeUOW.setSalary(salaryAlternative);
            }
        
            int nUpdated;
            if(shouldUpdateAll) {
                nUpdated = em.createQuery("UPDATE Employee e set e.lastName = '" + lastNameNew + "' where e.firstName like '" + firstName + "'").setFlushMode(FlushModeType.AUTO).executeUpdate();
            } else {
                nUpdated = em.createNativeQuery("UPDATE CMP3_EMPLOYEE SET L_NAME = '" + lastNameNew + "', VERSION = VERSION + 1 WHERE F_NAME LIKE '" + firstName + "'").setFlushMode(FlushModeType.AUTO).executeUpdate();
            }
            assertTrue("nUpdated=="+ nUpdated +"; 1 was expected", nUpdated == 1);
    
            if(shouldFlush) {
                selectQuery.setFlushMode(FlushModeType.AUTO);
            } else {
                selectQuery.setFlushMode(FlushModeType.COMMIT);
            }
    
            if(shouldRefresh) {
                selectQuery.setHint("toplink.refresh", Boolean.TRUE);
                employeeUOW = (Employee)selectQuery.getSingleResult();
                selectQuery.setHint("toplink.refresh", Boolean.FALSE);
            } else {
                employeeUOW = (Employee)selectQuery.getSingleResult();
            }
            assertTrue("employeeUOW.getLastName()=="+ employeeUOW.getLastName() +"; " + lastNameNew + " was expected", employeeUOW.getLastName().equals(lastNameNew));
    
            employeeUOW.setSalary(salaryNew);
    
            employeeUOW = (Employee)selectQuery.getSingleResult();
            assertTrue("employeeUOW.getSalary()=="+ employeeUOW.getSalary() +"; " + salaryNew + " was expected", employeeUOW.getSalary() == salaryNew);
                    
            em.getTransaction().commit();
        }catch (Throwable ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            if (Error.class.isAssignableFrom(ex.getClass())){
                throw (Error)ex;
            }else{
                throw (RuntimeException)ex;
            }
        }
        
        Employee employeeFoundAfterTransaction = em.find(Employee.class, employeeUOW.getId());
        assertTrue("employeeFoundAfterTransaction().getLastName()=="+ employeeFoundAfterTransaction.getLastName() +"; " + lastNameNew + " was expected", employeeFoundAfterTransaction.getLastName().equals(lastNameNew));
        assertTrue("employeeFoundAfterTransaction().getSalary()=="+ employeeFoundAfterTransaction.getSalary() +"; " + salaryNew + " was expected", employeeFoundAfterTransaction.getSalary() == salaryNew);
    
        // clean up
        em.getTransaction().begin();
        try{
            deleteQuery.executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        clearCache();
        em.close();
    }

    // test for bug 4755392: 
    // AFTER DELETEALL OBJECT STILL DEEMED EXISTING
    public void testFindDeleteAllPersist() {
        String firstName = "testFindDeleteAllPersist";

        // create Employees        
        Employee empWithAddress = new Employee();
        empWithAddress.setFirstName(firstName);
        empWithAddress.setLastName("WithAddress");
        empWithAddress.setAddress(new Address());

        Employee empWithoutAddress = new Employee();
        empWithoutAddress.setFirstName(firstName);
        empWithoutAddress.setLastName("WithoutAddress");

        EntityManager em = createEntityManager();
        Query deleteQuery = em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'");

        // make sure no Employee with the specified firstName exists.
        em.getTransaction().begin();
        try{
            deleteQuery.executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // persist
        em.getTransaction().begin();
        try{
            em.persist(empWithAddress);
            em.persist(empWithoutAddress);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        // clear cache
        clearCache();
        em.clear();
        
        // Find both to bring into the cache, delete empWithoutAddress.
        // Because the address VH is not triggered both objects should be invalidated.
        em.getTransaction().begin();
        try{
            Employee empWithAddressFound = em.find(Employee.class, empWithAddress.getId());
            Employee empWithoutAddressFound = em.find(Employee.class, empWithoutAddress.getId());
            int nDeleted = em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"' and e.address IS NULL").executeUpdate();
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        // we can no longer rely on the query above to clear the Employee from the persistence context.
        // Clearling the context to allow us to proceed.
        em.clear();
        // persist new empWithoutAddress - the one that has been deleted from the db.
        em.getTransaction().begin();
        try{
            Employee newEmpWithoutAddress = new Employee();
            newEmpWithoutAddress.setFirstName(firstName);
            newEmpWithoutAddress.setLastName("newWithoutAddress");
            newEmpWithoutAddress.setId(empWithoutAddress.getId());
            em.persist(newEmpWithoutAddress);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        // persist new empWithAddress - the one still in the db.
        em.getTransaction().begin();
        try{
            Employee newEmpWithAddress = new Employee();
            newEmpWithAddress.setFirstName(firstName);
            newEmpWithAddress.setLastName("newWithAddress");
            newEmpWithAddress.setId(empWithAddress.getId());
            em.persist(newEmpWithAddress);
            fail("EntityExistsException was expected");
        } catch (EntityExistsException ex) {
            // "cant_persist_detatched_object" - ignore the expected exception
        } finally {
            em.getTransaction().rollback();
        }

        // clean up
        em.getTransaction().begin();
        deleteQuery.executeUpdate();
        em.getTransaction().commit();
    }
    
    public void testWRITELock(){
        EntityManager em = createEntityManager();
        Employee employee = new Employee();
        employee.setFirstName("Mark");
        employee.setLastName("Madsen");
        em.getTransaction().begin();
        try{
            em.persist(employee);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }

        EntityManager em2 = createEntityManager();
        Exception optimisticLockException = null;
        
        em.getTransaction().begin();
        try{
            employee = em.find(Employee.class, employee.getId());
            em.lock(employee, LockModeType.WRITE);

            em2.getTransaction().begin();
            try{
                Employee employee2 = em2.find(Employee.class, employee.getId());
                employee2.setFirstName("Michael");
                em2.getTransaction().commit();
            }catch (RuntimeException ex){
                em2.getTransaction().rollback();
                em2.close();
                throw ex;
            }
            
            em.getTransaction().commit();
        } catch (RollbackException exception) {
            if (exception.getCause() instanceof OptimisticLockException){
                optimisticLockException = exception;
            }
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        em.getTransaction().begin();
        try{
            employee = em.find(Employee.class, employee.getId());
            em.remove(employee);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
        
        if (optimisticLockException == null){
            fail("Proper exception not thrown when EntityManager.lock(object, WRITE) is used.");
        }
    }
    /*This test case uses the "default2" PU defined in the persistence.xml 
    located at tltest/resource/essentials/broken-testmodels/META-INF 
    and included in essentials_testmodels_broken.jar */
    
    public void testEMFWrapValidationException() 
    {
        EntityManagerFactory factory = null;
        try {
            factory = Persistence.createEntityManagerFactory("broken-PU", getDatabaseProperties());
            EntityManager em = factory.createEntityManager();
        } catch (javax.persistence.PersistenceException e)  {
            // Ignore - it's expected exception type
        } finally {
            factory.close();
        }
    }
    
    /**
     * At the time this test case was added, the problem it was designed to test for would cause a failure during deployement 
     * and therefore this tests case would likely always pass if there is a successful deployment.
     * But it is anticipated that that may not always be the case and therefore we are adding a test case 
     */
    public void testEMDefaultTxType() 
    {
        EntityManagerFactory factory = null;
        try {
            factory = Persistence.createEntityManagerFactory("default1", getDatabaseProperties());
            EntityManager em = factory.createEntityManager();
        } catch (Exception e)  {   
            fail("Exception caught while creating EM with no \"transaction-type\" specified in persistence.xml");        
        } finally {
            factory.close();
        }
        Assert.assertTrue(true);        
    }
    
    public void testPersistOnNonEntity()
    {
        boolean testPass = false;
        Object nonEntity = new Object();
        EntityManager em = createEntityManager();
        
        try {
            em.persist(nonEntity);
        } catch (IllegalArgumentException e) {
            testPass = true;
        }
        Assert.assertTrue(testPass);
    }

    public void testClose() {
        EntityManager em = createEntityManager();
        if(!em.isOpen()) {
            fail("Created EntityManager is not open");
        }
        em.close();
        if(em.isOpen()) {
            fail("Closed EntityManager is still open");
        }
    }

    public void testBeginTransactionClose() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.close();
            if(em.isOpen()) {
                fail("Closed EntityManager is still open before transaction complete");
            }
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            if(em.isOpen()) {
                em.close();
            }
            throw ex;
        }

        em.getTransaction().rollback();
        if(em.isOpen()) {
            fail("Closed EntityManager is still open after transaction rollback");
        }
    }

    public void testBeginTransactionCloseCommitTransaction() {
        String firstName = "testBeginTrCloseCommitTr";
        EntityManager em = createEntityManager();

        // make sure there is no employees with this firstName
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
        em.getTransaction().commit();
        
        // create a new Employee
        Employee employee = new Employee();
        employee.setFirstName(firstName);        
        
        // persist the new Employee and close the entity manager
        em.getTransaction().begin();
        try{
            em.persist(employee);
            em.close();
            if(em.isOpen()) {
                fail("Closed EntityManager is still open before transaction complete");
            }
        }catch (RuntimeException ex){
            em.getTransaction().rollback();
            if(em.isOpen()) {
                em.close();
            }
            throw ex;
        }
        em.getTransaction().commit();
        
        if(em.isOpen()) {
            fail("Closed EntityManager is still open after transaction commit");
        }
        
        // verify that the employee has been persisted
        em = createEntityManager();
        RuntimeException exception = null;
        try {
            Employee persistedEmployee = (Employee)em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getSingleResult();
        } catch (RuntimeException runtimeException) {
            exception = runtimeException;
        }

        // clean up
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
        em.getTransaction().commit();
        
        if(exception != null) {
            if(exception instanceof EntityNotFoundException) {
                fail("object has not been persisted");
            } else {
                // unexpected exception - rethrow.
                throw exception;
            }
        }
    }

    // The test removed because we moved back to binding literals 
    // on platforms other than DB2 and Derby
/*    public void testDontBindLiteral() {
        EntityManager em = createEntityManager();
        
        Query controlQuery = em.createQuery("SELECT OBJECT(p) FROM SmallProject p WHERE p.name = CONCAT(:param1, :param2)");
        controlQuery.setParameter("param1", "A").setParameter("param2", "B");
        List controlResults = controlQuery.getResultList();
        int nControlParams = ((ExpressionQueryMechanism)((EJBQueryImpl)controlQuery).getDatabaseQuery().getQueryMechanism()).getCall().getParameters().size();
        if(nControlParams != 2) {
            fail("controlQuery has wrong number of parameters = "+nControlParams+"; 2 is expected");
        }

        Query query = em.createQuery("SELECT OBJECT(p) FROM SmallProject p WHERE p.name = CONCAT('A', 'B')");
        List results = query.getResultList();
        int nParams = ((ExpressionQueryMechanism)((EJBQueryImpl)query).getDatabaseQuery().getQueryMechanism()).getCall().getParameters().size();
        if(nParams > 0) {
            fail("Query processed literals as parameters");
        }
        
        em.close();
    }*/
    
    public void testPersistenceProperties() {
        EntityManager em = createEntityManager();
        ServerSession ss = ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)em).getServerSession();
        
        // these properties were set in persistence unit
        // and overridden in CMP3TestModel.setup - the values should be overridden.
        
        boolean isReadShared = (ss.getReadConnectionPool() instanceof ReadConnectionPool);
        if(isReadShared != Boolean.parseBoolean((String)propertiesMap.get(TopLinkProperties.JDBC_READ_CONNECTIONS_SHARED))) {
            fail("isReadShared is wrong");
        }
        
        int writeMin = ss.getDefaultConnectionPool().getMinNumberOfConnections();
        if(writeMin != Integer.parseInt((String)propertiesMap.get(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MIN))) {
            fail("writeMin is wrong");
        }
        
        int writeMax = ss.getDefaultConnectionPool().getMaxNumberOfConnections();
        if(writeMax != Integer.parseInt((String)propertiesMap.get(TopLinkProperties.JDBC_WRITE_CONNECTIONS_MAX))) {
            fail("writeMax is wrong");
        }

        int readMin = ss.getReadConnectionPool().getMinNumberOfConnections();
        if(readMin != Integer.parseInt((String)propertiesMap.get(TopLinkProperties.JDBC_READ_CONNECTIONS_MIN))) {
            fail("readMin is wrong");
        }

        int readMax = ss.getReadConnectionPool().getMaxNumberOfConnections();
        if(readMax != Integer.parseInt((String)propertiesMap.get(TopLinkProperties.JDBC_READ_CONNECTIONS_MAX))) {
            fail("readMax is wrong");
        }
        
        // these properties were set in persistence unit - the values should be the same as in persistence.xml
        /*
			<property name="toplink.session-name" value="default-session"/>
			<property name="toplink.cache.size.default" value="500"/>
			<property name="toplink.cache.size.Employee" value="550"/>
			<property name="toplink.cache.size.oracle.toplink.essentials.testing.models.cmp3.advanced.Address" value="555"/>
			<property name="toplink.cache.type.default" value="Full"/>
			<property name="toplink.cache.type.Employee" value="Weak"/>
			<property name="toplink.cache.type.oracle.toplink.essentials.testing.models.cmp3.advanced.Address" value="HardWeak"/>
			<property name="toplink.session.customizer" value="oracle.toplink.essentials.testing.models.cmp3.advanced.Customizer"/>
			<property name="toplink.descriptor.customizer.Employee" value="oracle.toplink.essentials.testing.models.cmp3.advanced.Customizer"/>
			<property name="toplink.descriptor.customizer.oracle.toplink.essentials.testing.models.cmp3.advanced.Address" value="oracle.toplink.essentials.testing.models.cmp3.advanced.Customizer"/>
        */
        
        String sessionName = ss.getName();
        if(!sessionName.equals("default-session")) {
            fail("sessionName is wrong");
        }
        
        int defaultCacheSize = ss.getDescriptor(Project.class).getIdentityMapSize();
        if(defaultCacheSize != 500) {
            fail("defaultCacheSize is wrong");
        }
        
        int employeeCacheSize = ss.getDescriptor(Employee.class).getIdentityMapSize();
        if(employeeCacheSize != 550) {
            fail("employeeCacheSize is wrong");
        }
        
        int addressCacheSize = ss.getDescriptor(Address.class).getIdentityMapSize();
        if(addressCacheSize != 555) {
            fail("addressCacheSize is wrong");
        }
        
        Class defaultCacheType = ss.getDescriptor(Project.class).getIdentityMapClass();
        if(! Helper.getShortClassName(defaultCacheType).equals("FullIdentityMap")) {
            fail("defaultCacheType is wrong");
        }
        
        Class employeeCacheType = ss.getDescriptor(Employee.class).getIdentityMapClass();
        if(! Helper.getShortClassName(employeeCacheType).equals("WeakIdentityMap")) {
            fail("employeeCacheType is wrong");
        }
        
        Class addressCacheType = ss.getDescriptor(Address.class).getIdentityMapClass();
        if(! Helper.getShortClassName(addressCacheType).equals("HardCacheWeakIdentityMap")) {
            fail("addressCacheType is wrong");
        }
        
        int numSessionCalls = Customizer.getNumberOfCallsForSession(ss.getName());
        if(numSessionCalls == 0) {
            fail("session customizer hasn't been called");
        }
        
        int numProjectCalls = Customizer.getNumberOfCallsForClass(Project.class.getName());
        if(numProjectCalls > 0) {
            fail("Project customizer has been called");
        }
        
        int numEmployeeCalls = Customizer.getNumberOfCallsForClass(Employee.class.getName());
        if(numEmployeeCalls == 0) {
            fail("Employee customizer hasn't been called");
        }
        
        int numAddressCalls = Customizer.getNumberOfCallsForClass(Address.class.getName());
        if(numAddressCalls == 0) {
            fail("Address customizer hasn't been called");
        }
        
        em.close();
    }

    public void testMultipleFactories() {
        getEntityManagerFactory();
        closeEntityManagerFactory();
        boolean isOpen = getEntityManagerFactory().isOpen();
        if(!isOpen) {
            fail("Close factory 1; open factory 2 - it's not open");
        } else {
            // Get entity manager just to login back the session, then close em
            getEntityManagerFactory().createEntityManager().close();
        }
    }
    
    public void testParallelMultipleFactories() {
        EntityManagerFactory factory1 =  Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        factory1.createEntityManager();
        EntityManagerFactory factory2 =  Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        factory2.createEntityManager();
        factory1.close();
        if(factory1.isOpen()) {
            fail("after factory1.close() factory1 is not closed");
        }
        if(!factory2.isOpen()) {
            fail("after factory1.close() factory2 is closed");
        }
        factory2.close();
        if(factory2.isOpen()) {
            fail("after factory2.close() factory2 is not closed");
        }
        EntityManagerFactory factory3 =  Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        if(!factory3.isOpen()) {
            fail("factory3 is closed");
        }
        factory3.createEntityManager();
        factory3.close();
        if(factory3.isOpen()) {
            fail("after factory3.close() factory3 is open");
        }
    }
    
    public void testQueryHints() {
        EntityManager em = createEntityManager();
        Query query = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = 'testQueryHints'");
        ObjectLevelReadQuery olrQuery = (ObjectLevelReadQuery)((EJBQueryImpl)query).getDatabaseQuery();
        
        // binding
        // original state = default state
        assertTrue(olrQuery.shouldIgnoreBindAllParameters());
        // set boolean true
        query.setHint(TopLinkQueryHints.BIND_PARAMETERS, true);
        assertTrue(olrQuery.shouldBindAllParameters());
        // reset to original state
        query.setHint(TopLinkQueryHints.BIND_PARAMETERS, "");
        assertTrue(olrQuery.shouldIgnoreBindAllParameters());
        // set "false"
        query.setHint(TopLinkQueryHints.BIND_PARAMETERS, "false");
        assertFalse(olrQuery.shouldBindAllParameters());
        // reset to the original state
        query.setHint(TopLinkQueryHints.BIND_PARAMETERS, "");
        assertTrue(olrQuery.shouldIgnoreBindAllParameters());
        
        // cache usage
        query.setHint(TopLinkQueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
        assertTrue(olrQuery.getCacheUsage()==ObjectLevelReadQuery.DoNotCheckCache);
        query.setHint(TopLinkQueryHints.CACHE_USAGE, CacheUsage.CheckCacheOnly);
        assertTrue(olrQuery.shouldCheckCacheOnly());
        query.setHint(TopLinkQueryHints.CACHE_USAGE, CacheUsage.ConformResultsInUnitOfWork);
        assertTrue(olrQuery.shouldConformResultsInUnitOfWork());
        // reset to the original state
        query.setHint(TopLinkQueryHints.CACHE_USAGE, "");
        assertTrue(olrQuery.shouldCheckDescriptorForCacheUsage());
        
        // pessimistic lock
        query.setHint(TopLinkQueryHints.PESSIMISTIC_LOCK, PessimisticLock.Lock);
        assertTrue(olrQuery.getLockMode()==ObjectLevelReadQuery.LOCK);
        query.setHint(TopLinkQueryHints.PESSIMISTIC_LOCK, PessimisticLock.NoLock);
        assertTrue(olrQuery.getLockMode()==ObjectLevelReadQuery.NO_LOCK);
        query.setHint(TopLinkQueryHints.PESSIMISTIC_LOCK, PessimisticLock.LockNoWait);
        assertTrue(olrQuery.getLockMode()==ObjectLevelReadQuery.LOCK_NOWAIT);
        // default state
        query.setHint(TopLinkQueryHints.PESSIMISTIC_LOCK, "");
        assertTrue(olrQuery.getLockMode()==ObjectLevelReadQuery.NO_LOCK);
        
        //refresh
        // set to original state - don't refresh.
        // the previously run LOCK and LOCK_NOWAIT have swithed it to true
        query.setHint(TopLinkQueryHints.REFRESH, false);
        assertFalse(olrQuery.shouldRefreshIdentityMapResult());
        // set boolean true
        query.setHint(TopLinkQueryHints.REFRESH, true);
        assertTrue(olrQuery.shouldRefreshIdentityMapResult());
        assertTrue(olrQuery.shouldCascadeByMapping()); // check if cascade refresh is enabled 
        // set "false"
        query.setHint(TopLinkQueryHints.REFRESH, "false");
        assertFalse(olrQuery.shouldRefreshIdentityMapResult());
        // set Boolean.TRUE
        query.setHint(TopLinkQueryHints.REFRESH, Boolean.TRUE);
        assertTrue(olrQuery.shouldRefreshIdentityMapResult());
        assertTrue(olrQuery.shouldCascadeByMapping()); // check if cascade refresh is enabled 
        // reset to original state
        query.setHint(TopLinkQueryHints.REFRESH, "");
        assertFalse(olrQuery.shouldRefreshIdentityMapResult());
        
        //cascade policy
        query.setHint(TopLinkQueryHints.REFRESH_CASCADE, CascadePolicy.NoCascading);
        assertTrue(olrQuery.getCascadePolicy()==DatabaseQuery.NoCascading);
        query.setHint(TopLinkQueryHints.REFRESH_CASCADE, CascadePolicy.CascadeByMapping);
        assertTrue(olrQuery.getCascadePolicy()==DatabaseQuery.CascadeByMapping);
        query.setHint(TopLinkQueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts);
        assertTrue(olrQuery.getCascadePolicy()==DatabaseQuery.CascadeAllParts);
        query.setHint(TopLinkQueryHints.REFRESH_CASCADE, CascadePolicy.CascadePrivateParts);
        assertTrue(olrQuery.getCascadePolicy()==DatabaseQuery.CascadePrivateParts);
        // reset to the original state
        query.setHint(TopLinkQueryHints.REFRESH_CASCADE, "");
        assertTrue(olrQuery.getCascadePolicy()==DatabaseQuery.CascadeByMapping);
        
        em.close();
    }
    
    /*
     * Bug51411440: need to throw IllegalStateException if query executed on closed em
     */
    public void testQueryOnClosedEM() {
        boolean exceptionWasThrown = false;
        EntityManager em = createEntityManager();
        Query q =  em.createQuery("SELECT e FROM Employee e ");
        em.close();
        if(em.isOpen()) {
            fail("Closed EntityManager is still open");
        }
        try{
            q.getResultList();
        }catch(java.lang.IllegalStateException e){
            exceptionWasThrown=true;
        }
        if (!exceptionWasThrown){
            fail("Query on Closed EntityManager did not throw an exception");
        }
    }
    
    public void testNullifyAddressIn() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.createQuery("UPDATE Employee e SET e.address = null WHERE e.address.country IN ('Canada', 'US')").executeUpdate();
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    //test for bug 5234283: WRONG =* SQL FOR LEFT JOIN ON DERBY AND DB2 PLATFORMS
    public void testLeftJoinOneToOneQuery() {
        EntityManager em = createEntityManager();
        List results = em.createQuery("SELECT a FROM Employee e LEFT JOIN e.address a").getResultList();
        em.close();
    }

    // test for GlassFish bug 711 - throw a descriptive exception when an uninstantiated valueholder is serialized and then accessed
    public void testSerializedLazy(){
        oracle.toplink.essentials.ejb.cmp3.EntityManager em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();      
       
        em.getTransaction().begin();
        
        Employee emp = new Employee();
        emp.setFirstName("Owen");
        emp.setLastName("Hargreaves");
		emp.setId(40);
        Address address = new Address();
        address.setCity("Munich");
        emp.setAddress(address);
        em.persist(emp);
        em.flush();
        em.getTransaction().commit();
        em.close();
        clearCache();
        em = (oracle.toplink.essentials.ejb.cmp3.EntityManager) createEntityManager();
        String ejbqlString = "SELECT e FROM Employee e WHERE e.firstName = 'Owen' and e.lastName = 'Hargreaves'";
        List result = em.createQuery(ejbqlString).getResultList();
        emp = (Employee)result.get(0);
        Exception exception = null;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteStream);
                
            stream.writeObject(emp);
            stream.flush();
            byte arr[] = byteStream.toByteArray();
            ByteArrayInputStream inByteStream = new ByteArrayInputStream(arr);
            ObjectInputStream inObjStream = new ObjectInputStream(inByteStream);

            emp = (Employee) inObjStream.readObject();
            emp.getAddress();
        } catch (ValidationException e) {
            if (e.getErrorCode() == ValidationException.INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION){
                exception = e;
            } else {
                fail("An unexpected exception was thrown while testing serialization of ValueHolders: " + e.toString());
            }
        } catch (Exception e){
            fail("An unexpected exception was thrown while testing serialization of ValueHolders: " + e.toString());
        }

        assertNotNull("The correct exception was not thrown while traversing an uninstantiated lazy relationship on a serialized object: " + exception, exception);
        em.getTransaction().begin();
        emp = (Employee)em.find(Employee.class, emp.getId());
        em.remove(emp);
        em.getTransaction().commit();
    }
    
    //test for bug 5170395: GET THE SEQUENCING EXCEPTION WHEN RUNNING FOR THE FIRST TIME ON A CLEAR SCHEMA
    public void testSequenceObjectDefinition() {
        EntityManager em = createEntityManager();
        ServerSession ss = ((oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)em).getServerSession();
        if(!ss.getLogin().getPlatform().supportsSequenceObjects()) {
            // platform that supports sequence objects is required for this test
            em.close();
            return;
        }
        String seqName = "testSequenceObjectDefinition";
        try {
            // first param is preallocationSize, second is startValue
            // both should be positive
            internalTestSequenceObjectDefinition(10, 1, seqName, em, ss);
            internalTestSequenceObjectDefinition(10, 5, seqName, em, ss);
            internalTestSequenceObjectDefinition(10, 15, seqName, em, ss);
        } finally {
            em.close();
        }
    }

    protected void internalTestSequenceObjectDefinition(int preallocationSize, int startValue, String seqName, EntityManager em, ServerSession ss) {
        NativeSequence sequence = new NativeSequence(seqName, preallocationSize, startValue, false);
        sequence.onConnect(ss.getPlatform());
        SequenceObjectDefinition def = new SequenceObjectDefinition(sequence);
        // create sequence
        String createStr = def.buildCreationWriter(ss, new StringWriter()).toString();
        em.getTransaction().begin();
        em.createNativeQuery(createStr).executeUpdate();
        em.getTransaction().commit();
        try {
            // sequence value preallocated
            Vector seqValues = sequence.getGeneratedVector(null, ss);
            int firstSequenceValue = ((Number)seqValues.elementAt(0)).intValue();
            if(firstSequenceValue != startValue) {
                fail(seqName + " sequence with preallocationSize = "+preallocationSize+" and startValue = " + startValue + " produced wrong firstSequenceValue =" + firstSequenceValue);
            }
        } finally {
            sequence.onDisconnect(ss.getPlatform());
            // drop sequence
            String dropStr = def.buildDeletionWriter(ss, new StringWriter()).toString();
            em.getTransaction().begin();
            em.createNativeQuery(dropStr).executeUpdate();
            em.getTransaction().commit();
        }
    }
    
    public void testMergeDetachedObject() {
        // Step 1 - read a department and clear the cache.
        clearCache();
        EntityManager em = createEntityManager();
        EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllSQLDepartments");
        Collection departments = query.getResultCollection();
        
        Department detachedDepartment;
        
        // This test seems to get called twice. Once with departments populated
        // and a second time with the department table empty.
        if (departments.isEmpty()) {
            em.getTransaction().begin();
            detachedDepartment = new Department();
            detachedDepartment.setName("Department X");
            em.persist(detachedDepartment);
            em.getTransaction().commit();
        } else {
            detachedDepartment = (Department) departments.iterator().next();
        }
        
        em.close();
        clearCache();
        
        // Step 2 - create a new em, create a new employee with the 
        // detached department and then query the departments again.
        em = createEntityManager();
        em.getTransaction().begin();
        
        Employee emp = new Employee();
        emp.setFirstName("Crazy");
        emp.setLastName("Kid");
		emp.setId(41);
        emp.setDepartment(detachedDepartment);
            
        em.persist(emp);
        em.getTransaction().commit();
        
        try {
            ((EJBQueryImpl) em.createNamedQuery("findAllSQLDepartments")).getResultCollection();
        } catch (NullPointerException e) {
            assertTrue("The detached department caused a null pointer on the query execution.", false);
        }
        
        em.close();
    }
    
    //bug gf830 - attempting to merge a removed entity should throw an IllegalArgumentException
    public void testMergeRemovedObject() {
    	//create an Employee
        Employee emp = new Employee();
        emp.setFirstName("testMergeRemovedObjectEmployee");
		emp.setId(42);
  
        //persist the Employee
        EntityManager em = createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(emp);
            em.getTransaction().commit();
        }catch (RuntimeException re){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw re;
        }
        
        em.getTransaction().begin();
        em.remove(emp); 	//attempt to remove the Employee
        try{  
            em.merge(emp);	//then attempt to merge the Employee
            fail("No exception thrown when merging a removed entity is attempted.");
        }catch (IllegalArgumentException iae){
        	//expected
        }catch (Exception e) {
        	fail("Wrong exception type thrown: " + e.getClass());
        }finally {
            em.getTransaction().rollback();
            
            //clean up - ensure removal of employee
            em.getTransaction().begin();	
            em.remove(em.find(Employee.class, emp.getId())); 
            em.getTransaction().commit();
            em.close();
        } 
    }
    
    //merge(null) should throw IllegalArgumentException
    public void testMergeNull(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        	em.merge(null);
        }catch (IllegalArgumentException iae){
        	return;
        }catch (Exception e) {
        	fail("Wrong exception type thrown: " + e.getClass());
        }finally {
            em.getTransaction().rollback();
            em.close();
        }
        fail("No exception thrown when entityManager.merge(null) attempted.");        
    }
    
    //persist(null) should throw IllegalArgumentException
    public void testPersistNull(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        	em.persist(null);
        }catch (IllegalArgumentException iae){
        	return;
        }catch (Exception e) {
        	fail("Wrong exception type thrown: " + e.getClass());
        }finally {
            em.getTransaction().rollback();
            em.close();
        }
        fail("No exception thrown when entityManager.persist(null) attempted.");        
    }
    
    //contains(null) should throw IllegalArgumentException
    public void testContainsNull(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        	em.contains(null);
        }catch (IllegalArgumentException iae){
        	return;
        }catch (Exception e) {
        	fail("Wrong exception type thrown: " + e.getClass());
        }finally {
            em.getTransaction().rollback();
            em.close();
        }
        fail("No exception thrown when entityManager.contains(null) attempted.");        
    }

	//bug gf732 - removing null entity should throw an IllegalArgumentException
    public void testRemoveNull(){
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
        	em.remove(null);
        }catch (IllegalArgumentException iae){
        	return;
        }catch (Exception e) {
        	fail("Wrong exception type thrown: " + e.getClass());
        }finally {
            em.getTransaction().rollback();
            em.close();
        }
        fail("No exception thrown when entityManager.remove(null) attempted.");        
    }
    
    //GlassFish Bug854
    public void testCreateEntityManagerFactory() {
        EntityManagerFactory emf = null;

        try{
        	emf=Persistence.createEntityManagerFactory("default",null);
        	emf=Persistence.createEntityManagerFactory("default");
        } catch (NullPointerException npe ) {
        	// un-expected behaviour
        	npe.printStackTrace();
            fail("EntityManagerFactoryProvider.createEntityManagerFactory(String emName, Map properties) didn't catch Null pointer exception");
        } catch (Exception e) {
            fail("EntityManagerFactoryProvider.createEntityManagerFactory(String emName, Map properties) threw a wrong exception: " + e.getMessage());
        } finally{
        	if (emf!=null) emf.close();
        }
    }
    
    //GlassFish Bug854  PU name doesn't exist or PU with the wrong name
    public void testCreateEntityManagerFactory2() {
        EntityManagerFactory emf = null;

        EntityManagerFactoryProvider provider = new EntityManagerFactoryProvider();

        try {
            try {
                emf = provider.createEntityManagerFactory("default123", null);
            } catch (Exception e) {
                fail("Exception is not expected, but thrown:" + e);
            }
            assertNull(emf);
        
            try {
                emf = Persistence.createEntityManagerFactory("default123");
                fail("PersistenceException is expected");
            } catch (PersistenceException ex) {
                // expected
            } catch (Exception e) {
                fail("PersistenceException is expected, but thrown:" + e);
            }
        } finally {
            if (emf != null) {
                emf.close();
            }
        }
    }
    
    //Glassfish bug 702 - prevent primary key updates
    public void testPrimaryKeyUpdate() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        Employee emp = new Employee();
        emp.setFirstName("Groucho");
        emp.setLastName("Marx");
        em.persist(emp);

        Integer id = emp.getId();
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        emp.setId(id + 1);
        
        try {
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }

            if (pe.getCause() instanceof ValidationException) {
                ValidationException ve = (ValidationException) pe.getCause();
                if (ve.getErrorCode() == ValidationException.PRIMARY_KEY_UPDATE_DISALLOWED) {
                    return;
                } else {
                    fail("Wrong error code for ValidationException: " + ve.getErrorCode());
                }
            } else {
                fail("ValiationException expected, thrown: " + pe.getCause());
            }
        } catch (Exception e) {
            fail("Wrong exception type thrown: " + e.getClass());
        } finally {
            em.close();
        }
        fail("No exception thrown when primary key update attempted.");        
    }
    
    //Glassfish bug 702 - prevent primary key updates, same value is ok
    public void testPrimaryKeyUpdateSameValue() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        Employee emp = new Employee();
        emp.setFirstName("Harpo");
        emp.setLastName("Marx");
        em.persist(emp);

        Integer id = emp.getId();
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        emp.setId(id);
        
        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }

            fail("Unexpected exception thrown: " + e.getClass());
        } finally {
            em.close();
        }
    }

    //Glassfish bug 702 - prevent primary key updates, overlapping PK/FK
    public void testPrimaryKeyUpdatePKFK() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        Employee emp = new Employee();
        emp.setFirstName("Groucho");
        emp.setLastName("Marx");
        em.persist(emp);

        Employee emp2 = new Employee();
        emp2.setFirstName("Harpo");
        emp2.setLastName("Marx");
        em.persist(emp2);

        PhoneNumber phone = new PhoneNumber("home", "415", "0007");
        phone.setOwner(emp);
        em.persist(phone);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        phone.setOwner(emp2);
        
        try {
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }

            if (pe.getCause() instanceof ValidationException) {
                ValidationException ve = (ValidationException) pe.getCause();
                if (ve.getErrorCode() == ValidationException.PRIMARY_KEY_UPDATE_DISALLOWED) {
                    return;
                } else {
                    fail("Wrong error code for ValidationException: " + ve.getErrorCode());
                }
            } else {
                fail("ValiationException expected, thrown: " + pe.getCause());
            }
        } catch (Exception e) {
            fail("Wrong exception type thrown: " + e.getClass());
        } finally {
            em.close();
        }
        fail("No exception thrown when primary key update attempted.");        
    }
    
    // Test cascade merge on a detached entity
    public void testCascadeMergeDetached() {
        // setup
        Project p1 = new Project();
        p1.setName("Project1");
        Project p2 = new Project();
        p1.setName("Project2");
        Employee e1 = new Employee();
        e1.setFirstName("Employee1");
        Employee e2 = new Employee();
        e2.setFirstName("Employee2");
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.persist(p1);
            em.persist(p2);
            em.persist(e1);
            em.persist(e2);

            em.getTransaction().commit();
        } catch (RuntimeException re){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw re;
        }
        em.close();
        // end of setup

        //p1,p2,e1,e2 are detached
        
        // associate relationships
        //p1 -> e1 (one-to-one)
        p1.setTeamLeader(e1);
        //e1 -> e2 (one-to-many)
        e1.addManagedEmployee(e2);
        //e2 -> p2 (many-to-many)
        e2.addProject(p2);
        p2.addTeamMember(e2);

        em = createEntityManager();
        em.getTransaction().begin();
        try {
            Project mp1 = em.merge(p1); // cascade merge
            assertTrue(em.contains(mp1));
            assertTrue("Managed instance and detached instance must not be same", mp1 != p1);
            
            Employee me1 = mp1.getTeamLeader();
            assertTrue("Cascade merge failed", em.contains(me1));
            assertTrue("Managed instance and detached instance must not be same", me1 != e1);
            
            Employee me2 = me1.getManagedEmployees().iterator().next();
            assertTrue("Cascade merge failed", em.contains(me2));
            assertTrue("Managed instance and detached instance must not be same", me2 != e2);

            Project mp2 = me2.getProjects().iterator().next();
            assertTrue("Cascade merge failed", em.contains(mp2));
            assertTrue("Managed instance and detached instance must not be same", mp2 != p2);

            em.getTransaction().commit();
        } catch (RuntimeException re){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw re;
        }
        em.close();
    }

    // Test cascade merge on a managed entity
    // Test for GF#1139 - Cascade doesn't work when merging managed entity
    public void testCascadeMergeManaged() {
        // setup
        Project p1 = new Project();
        p1.setName("Project1");
        Project p2 = new Project();
        p1.setName("Project2");
        Employee e1 = new Employee();
        e1.setFirstName("Employee1");
        Employee e2 = new Employee();
        e2.setFirstName("Employee2");
            
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.persist(p1);
            em.persist(p2);
            em.persist(e1);
            em.persist(e2);

            em.getTransaction().commit();
        } catch (RuntimeException re){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw re;
        }
        em.close();
        // end of setup
        
        //p1,p2,e1,e2 are detached
        em = createEntityManager();
        em.getTransaction().begin();
        try {
            Project mp1 = em.merge(p1);
            assertTrue(em.contains(mp1));
            assertTrue("Managed instance and detached instance must not be same", mp1 != p1);

            //p1 -> e1 (one-to-one)
            mp1.setTeamLeader(e1);
            mp1 = em.merge(mp1); // merge again - trigger cascade merge

            Employee me1 = mp1.getTeamLeader();
            assertTrue("Cascade merge failed", em.contains(me1));
            assertTrue("Managed instance and detached instance must not be same", me1 != e1);

            //e1 -> e2 (one-to-many)
            me1.addManagedEmployee(e2);
            me1 = em.merge(me1); // merge again - trigger cascade merge
            
            Employee me2 = me1.getManagedEmployees().iterator().next();
            assertTrue("Cascade merge failed", em.contains(me2));
            assertTrue("Managed instance and detached instance must not be same", me2 != e2);

            //e2 -> p2 (many-to-many)
            me2.addProject(p2);
            p2.addTeamMember(me2);
            me2 = em.merge(me2); // merge again - trigger cascade merge

            Project mp2 = me2.getProjects().iterator().next();
            assertTrue("Cascade merge failed", em.contains(mp2));
            assertTrue("Managed instance and detached instance must not be same", mp2 != p2);

            em.getTransaction().commit();
        } catch (RuntimeException re){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw re;
        }
        em.close();
    }

    //Glassfish bug 1021 - allow cascading persist operation to non-entities
    public void testCascadePersistToNonEntitySubclass() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();

        Employee emp = new Employee();
        emp.setFirstName("Albert");
        emp.setLastName("Einstein");

        SuperLargeProject s1 = new SuperLargeProject("Super 1");
        Collection projects = new ArrayList();
        projects.add(s1);
        emp.setProjects(projects);
        em.persist(emp);

        try {
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            fail("Persist operation was not cascaded to related non-entity, thrown: " + e);
        } finally {
            em.close();
        }
    }

    /**
     * Bug 801
     * Test to ensure when property access is used and the underlying variable is changed the change
     * is correctly reflected in the database
     * 
     * In this test we test making the change before the object is managed
     */
    public void testInitializeFieldForPropertyAccess(){
        Employee employee = new Employee();
        employee.setFirstName("Andy");
        employee.setLastName("Dufresne");
        Address address = new Address();
        address.setCity("Shawshank");
        employee.setAddressField(address);
        
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        em.persist(employee);
        try{
            em.getTransaction().commit();
        } catch (RuntimeException e){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        int id = employee.getId();
        
        clearCache();
        
        employee = (Employee)em.find(Employee.class, new Integer(id));
        address = employee.getAddress();
        
        assertTrue("The address was not persisted.", employee.getAddress() != null);
        assertTrue("The address was not correctly persisted.", employee.getAddress().getCity().equals("Shawshank"));
        
        em.getTransaction().begin();
        employee.setAddress(null);
        em.remove(address);
        em.remove(employee);
        em.getTransaction().commit();
    
    }

    /**
     * Bug 801
     * Test to ensure when property access is used and the underlying variable is changed the change
     * is correctly reflected in the database
     * 
     * In this test we test making the change after the object is managed
     */
    public void testSetFieldForPropertyAccess(){       
        EntityManager em = createEntityManager();
        
        Employee employee = new Employee();
        employee.setFirstName("Andy");
        employee.setLastName("Dufresne");
        Address address = new Address();
        address.setCity("Shawshank");
        employee.setAddress(address);
        
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
        int id = employee.getId();
        int addressId = address.getId();
        
        em.getTransaction().begin();
        employee = (Employee)em.find(Employee.class, new Integer(id));
        employee.getAddress();

        address = new Address();
        address.setCity("Metropolis");
        employee.setAddressField(address);
        try{
            em.getTransaction().commit();
        } catch (RuntimeException e){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        
        clearCache();
        
        employee = (Employee)em.find(Employee.class, new Integer(id));
        address = employee.getAddress();

        assertTrue("The address was not persisted.", employee.getAddress() != null);
        assertTrue("The address was not correctly persisted.", employee.getAddress().getCity().equals("Metropolis"));
    
        Address initialAddress = (Address)em.find(Address.class, new Integer(addressId));
        em.getTransaction().begin();
        employee.setAddress(null);
        em.remove(address);
        em.remove(employee);
        em.remove(initialAddress);
        em.getTransaction().commit();
    }

    /**
     * Bug 801
     * Test to ensure when property access is used and the underlying variable is changed the change
     * is correctly reflected in the database
     * 
     * In this test we test making the change after the object is refreshed
     */
    public void testSetFieldForPropertyAccessWithRefresh(){       
        EntityManager em = createEntityManager();
        
        Employee employee = new Employee();
        employee.setFirstName("Andy");
        employee.setLastName("Dufresne");
        Address address = new Address();
        address.setCity("Shawshank");
        employee.setAddress(address);
        
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
        int id = employee.getId();
        int addressId = address.getId();
        
        em.getTransaction().begin();
        em.refresh(employee);
        employee.getAddress();

        address = new Address();
        address.setCity("Metropolis");
        employee.setAddressField(address);
        try{
            em.getTransaction().commit();
        } catch (RuntimeException e){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        
        clearCache();
        
        employee = (Employee)em.find(Employee.class, new Integer(id));
        address = employee.getAddress();

        assertTrue("The address was not persisted.", employee.getAddress() != null);
        assertTrue("The address was not correctly persisted.", employee.getAddress().getCity().equals("Metropolis"));
    
        Address initialAddress = (Address)em.find(Address.class, new Integer(addressId));
        em.getTransaction().begin();
        employee.setAddress(null);
        em.remove(address);
        em.remove(employee);
        em.remove(initialAddress);
        em.getTransaction().commit();
    }
    
    /**
     * Bug 801
     * Test to ensure when property access is used and the underlying variable is changed the change
     * is correctly reflected in the database
     * 
     * In this test we test making the change when an existing object is read into a new EM
     */
    public void testSetFieldForPropertyAccessWithNewEM(){       
        EntityManager em = createEntityManager();
        
        Employee employee = new Employee();
        employee.setFirstName("Andy");
        employee.setLastName("Dufresne");
        Address address = new Address();
        address.setCity("Shawshank");
        employee.setAddress(address);
        
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
        int id = employee.getId();
        int addressId = address.getId();
        
        em = createEntityManager();
        
        em.getTransaction().begin();
        employee = em.find(Employee.class, new Integer(id));
        employee.getAddress();

        address = new Address();
        address.setCity("Metropolis");
        employee.setAddressField(address);
        try{
            em.getTransaction().commit();
        } catch (RuntimeException e){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            throw e;
        }
        
        clearCache();
        
        employee = (Employee)em.find(Employee.class, new Integer(id));
        address = employee.getAddress();

        assertTrue("The address was not persisted.", employee.getAddress() != null);
        assertTrue("The address was not correctly persisted.", employee.getAddress().getCity().equals("Metropolis"));
        
        Address initialAddress = (Address)em.find(Address.class, new Integer(addressId));
        em.getTransaction().begin();
        employee.setAddress(null);
        em.remove(address);
        em.remove(employee);
        em.remove(initialAddress);
        em.getTransaction().commit();
    }
    	//bug gf674 - EJBQL delete query with IS NULL in WHERE clause produces wrong sql
     public void testDeleteAllPhonesWithNullOwner() {
         EntityManager em = createEntityManager();
         em.getTransaction().begin();
         try {
         	em.createQuery("DELETE FROM PhoneNumber ph WHERE ph.owner IS NULL").executeUpdate();
         } catch (Exception e) {
         	fail("Exception thrown: " + e.getClass());
         } finally {
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
             em.close();
         }
     }
     public void testDeleteAllProjectsWithNullTeamLeader() {
         internalDeleteAllProjectsWithNullTeamLeader("Project");
     }
     public void testDeleteAllSmallProjectsWithNullTeamLeader() {
         internalDeleteAllProjectsWithNullTeamLeader("SmallProject");
     }
     public void testDeleteAllLargeProjectsWithNullTeamLeader() {
         internalDeleteAllProjectsWithNullTeamLeader("LargeProject");
     }
     protected void internalDeleteAllProjectsWithNullTeamLeader(String className) {
         String name = "testDeleteAllProjectsWithNull";
         
         // setup
         SmallProject sp = new SmallProject();
         sp.setName(name);
         LargeProject lp = new LargeProject();
         lp.setName(name);
         EntityManager em = createEntityManager();
         try {
             em.getTransaction().begin();
             // make sure there are no pre-existing objects with this name
           	em.createQuery("DELETE FROM "+className+" p WHERE p.name = '"+name+"'").executeUpdate();
             em.persist(sp);
             em.persist(lp);
             em.getTransaction().commit();
         } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
             throw ex;
         } finally {
             em.close();
         }
                 
         // test
         em = createEntityManager();
         em.getTransaction().begin();
         try {
         	em.createQuery("DELETE FROM "+className+" p WHERE p.name = '"+name+"' AND p.teamLeader IS NULL").executeUpdate();
             em.getTransaction().commit();
         } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
            throw e;
         } finally {
             em.close();
         }

         // verify
         String error = null;
         em = createEntityManager();
         List result = em.createQuery("SELECT OBJECT(p) FROM Project p WHERE p.name = '"+name+"'").getResultList();
         if(result.isEmpty()) {
             if(!className.equals("Project")) {
                 error = "Target Class " + className +": no objects left";
             }
         } else {
             if(result.size() > 1) {
                 error = "Target Class " + className +": too many objects left: " + result.size();
             } else {
                 Project p = (Project)result.get(0);
                 if(p.getClass().getName().endsWith(className)) {
                     error = "Target Class " + className +": object of wrong type left: " + p.getClass().getName();
                 }
             }
         }

         // clean up
         try {
             em.getTransaction().begin();
             // make sure there are no pre-existing objects with this name
           	em.createQuery("DELETE FROM "+className+" p WHERE p.name = '"+name+"'").executeUpdate();
             em.getTransaction().commit();
         } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
             throw ex;
         } finally {
             em.close();
         }
         
         if(error != null) {
             fail(error);
         }
     }

    // gf1408: DeleteAll and UpdateAll queries broken on some db platforms;
    // gf1451: Complex updates to null using temporary storage do not work on Derby;
    // gf1860: TopLink provides too few values.
    // The tests forces the use of temporary storage to test null assignment to an integer field
    // on all platforms.
    public void testUpdateUsingTempStorage() {
        internalUpdateUsingTempStorage(false);
    }
    public void testUpdateUsingTempStorageWithParameter() {
        internalUpdateUsingTempStorage(true);
    }
    protected void internalUpdateUsingTempStorage(boolean useParameter) {
        String firstName = "testUpdateUsingTempStorage";
        int n = 3;
        
        // setup
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            // make sure there are no pre-existing objects with this name
           	em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
           	em.createQuery("DELETE FROM Address a WHERE a.country = '"+firstName+"'").executeUpdate();
            // populate Employees
            for(int i=1; i<=n; i++) {
                Employee emp = new Employee();
                emp.setFirstName(firstName);
                emp.setLastName(Integer.toString(i));
                emp.setSalary(i*100);
                emp.setRoomNumber(i);
                
                Address address = new Address();
                address.setCountry(firstName);
                address.setCity(Integer.toString(i));
                
                emp.setAddress(address);

                em.persist(emp);
            }
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
                
        // test
        em = createEntityManager();
        em.getTransaction().begin();
        int nUpdated = 0;
        try {
            if(useParameter) {
                nUpdated = em.createQuery("UPDATE Employee e set e.salary = e.roomNumber, e.roomNumber = e.salary, e.address = :address where e.firstName = '" + firstName + "'").setParameter("address", null).executeUpdate();
            } else {
                nUpdated = em.createQuery("UPDATE Employee e set e.salary = e.roomNumber, e.roomNumber = e.salary, e.address = null where e.firstName = '" + firstName + "'").executeUpdate();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
        	fail("Exception thrown: " + e.getClass());
        } finally {
            em.close();
        }

        // verify
        String error = null;
        em = createEntityManager();
        List result = em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.firstName = '"+firstName+"'").getResultList();
        em.close();
        int nReadBack = result.size();
        if(n != nUpdated) {
            error = "n = "+n+", but nUpdated ="+nUpdated+";";
        }
        if(n != nReadBack) {
            error = " n = "+n+", but nReadBack ="+nReadBack+";";
        }
        for(int i=0; i<nReadBack; i++) {
            Employee emp = (Employee)result.get(i);
            if(emp.getAddress() != null) {
                error = " Employee "+emp.getLastName()+" still has address;";
            }
            int ind = Integer.valueOf(emp.getLastName()).intValue();
            if(emp.getSalary() != ind) {
                error = " Employee "+emp.getLastName()+" has wrong salary "+emp.getSalary()+";";
            }
            if(emp.getRoomNumber() != ind*100) {
                error = " Employee "+emp.getLastName()+" has wrong roomNumber "+emp.getRoomNumber()+";";
            }
        }

        // clean up
        em = createEntityManager();
        try {
            em.getTransaction().begin();
            // make sure there are no objects left with this name
          	em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate();
           	em.createQuery("DELETE FROM Address a WHERE a.country = '"+firstName+"'").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                 em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
        
        if(error != null) {
            fail(error);
        }
    }

    protected void createProjectsWithName(String name, Employee teamLeader) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();

            SmallProject sp = new SmallProject();
            sp.setName(name);

            LargeProject lp = new LargeProject();
            lp.setName(name);

            em.persist(sp);
            em.persist(lp);
            
            if(teamLeader != null) {
                SmallProject sp2 = new SmallProject();
                sp2.setName(name);
                sp2.setTeamLeader(teamLeader);
    
                LargeProject lp2 = new LargeProject();
                lp2.setName(name);
                lp2.setTeamLeader(teamLeader);
    
                em.persist(sp2);
                em.persist(lp2);   
            }

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    protected void deleteProjectsWithName(String name) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();

          	em.createQuery("DELETE FROM Project p WHERE p.name = '"+name+"'").executeUpdate();
            
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void testUpdateAllSmallProjects() {
        internalTestUpdateAllProjects(SmallProject.class);
    }
    public void testUpdateAllLargeProjects() {
        internalTestUpdateAllProjects(LargeProject.class);
    }
    public void testUpdateAllProjects() {
        internalTestUpdateAllProjects(Project.class);
    }
    protected void internalTestUpdateAllProjects(Class cls) {
        String className = Helper.getShortClassName(cls);
        String name = "testUpdateAllProjects";
        String newName = "testUpdateAllProjectsNEW";
        HashMap map = null;
        boolean ok = false;
        
        try {
            // setup
            // populate Projects - necessary only if no SmallProject and/or LargeProject objects already exist.
            createProjectsWithName(name, null);
            // save the original names of projects: will set them back in cleanup
            // to restore the original state.
            EntityManager em = createEntityManager();
            List projects = em.createQuery("SELECT OBJECT(p) FROM Project p").getResultList();
            map = new HashMap(projects.size());
            for(int i=0; i<projects.size(); i++) {
                Project p = (Project)projects.get(i);
                map.put(p.getId(), p.getName());
            }        
    
            // test
            em.getTransaction().begin();
            try {
                em.createQuery("UPDATE "+className+" p set p.name = '"+newName+"'").executeUpdate();
                em.getTransaction().commit();
            } catch (RuntimeException ex) {
                if(em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw ex;
            } finally {
                em.close();
            }
            
            // verify
            em = createEntityManager();
            String errorMsg = "";
            projects = em.createQuery("SELECT OBJECT(p) FROM Project p").getResultList();
            for(int i=0; i<projects.size(); i++) {
                Project p = (Project)projects.get(i);
                String readName = p.getName();
                if(cls.isInstance(p)) {
                    if(!newName.equals(readName)) {
                        errorMsg = errorMsg + "haven't updated name: " + p + "; ";
                    }
                } else {
                    if(newName.equals(readName)) {
                        errorMsg = errorMsg + "have updated name: " + p + "; ";
                    }
                }
            }
            em.close();

            if(errorMsg.length() > 0) {
                fail(errorMsg);
            } else {
                ok = true;
            }
        } finally {
            // clean-up
            try {
                if(map != null) {
                    EntityManager em = createEntityManager();
                    List projects = em.createQuery("SELECT OBJECT(p) FROM Project p").getResultList();
                    em.getTransaction().begin();
                    try {
                        for(int i=0; i<projects.size(); i++) {
                            Project p = (Project)projects.get(i);
                            String oldName = (String)map.get(((Project)projects.get(i)).getId());
                            p.setName(oldName);
                        }
                        em.getTransaction().commit();
                    } catch (RuntimeException ex) {
                        if(em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        throw ex;
                    } finally {
                        em.close();
                    }
                }
                // delete projects that createProjectsWithName has created in setup
                deleteProjectsWithName(name);
            } catch (RuntimeException ex) {
                // eat clean-up exception in case the test failed
                if(ok) {
                    throw ex;
                }
            }
        }        
    }
    
    public void testUpdateAllSmallProjectsWithName() {
        internalTestUpdateAllProjectsWithName(SmallProject.class);
    }
    public void testUpdateAllLargeProjectsWithName() {
        internalTestUpdateAllProjectsWithName(LargeProject.class);
    }
    public void testUpdateAllProjectsWithName() {
        internalTestUpdateAllProjectsWithName(Project.class);
    }
    protected void internalTestUpdateAllProjectsWithName(Class cls) {
        String className = Helper.getShortClassName(cls);
        String name = "testUpdateAllProjects";
        String newName = "testUpdateAllProjectsNEW";
        boolean ok = false;
        
        try {
            // setup
            // make sure no projects with the specified names exist
            deleteProjectsWithName(name);
            deleteProjectsWithName(newName);
            // populate Projects
            createProjectsWithName(name, null);
    
            // test
            EntityManager em = createEntityManager();
            em.getTransaction().begin();
            try {
                em.createQuery("UPDATE "+className+" p set p.name = '"+newName+"' WHERE p.name = '"+name+"'").executeUpdate();
                em.getTransaction().commit();
            } catch (RuntimeException ex) {
                if(em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw ex;
            } finally {
                em.close();
            }
            
            // verify
            em = createEntityManager();
            String errorMsg = "";
            List projects = em.createQuery("SELECT OBJECT(p) FROM Project p WHERE p.name = '"+newName+"' OR p.name = '"+name+"'").getResultList();
            for(int i=0; i<projects.size(); i++) {
                Project p = (Project)projects.get(i);
                String readName = p.getName();
                if(cls.isInstance(p)) {
                    if(!readName.equals(newName)) {
                        errorMsg = errorMsg + "haven't updated name: " + p + "; ";
                    }
                } else {
                    if(readName.equals(newName)) {
                        errorMsg = errorMsg + "have updated name: " + p + "; ";
                    }
                }
            }
            em.close();
            
            if(errorMsg.length() > 0) {
                fail(errorMsg);
            } else {
                ok = true;
            }
        } finally {
            // clean-up
            // make sure no projects with the specified names left
            try {
                deleteProjectsWithName(name);
                deleteProjectsWithName(newName);
            } catch (RuntimeException ex) {
                // eat clean-up exception in case the test failed
                if(ok) {
                    throw ex;
                }
            }
        }
    }
    
    public void testUpdateAllSmallProjectsWithNullTeamLeader() {
        internalTestUpdateAllProjectsWithNullTeamLeader(SmallProject.class);
    }
    public void testUpdateAllLargeProjectsWithNullTeamLeader() {
        internalTestUpdateAllProjectsWithNullTeamLeader(LargeProject.class);
    }
    public void testUpdateAllProjectsWithNullTeamLeader() {
        internalTestUpdateAllProjectsWithNullTeamLeader(Project.class);
    }
    protected void internalTestUpdateAllProjectsWithNullTeamLeader(Class cls) {
        String className = Helper.getShortClassName(cls);
        String name = "testUpdateAllProjects";
        String newName = "testUpdateAllProjectsNEW";
        Employee empTemp = null;
        boolean ok = false;
        
        try {
            // setup
            // make sure no projects with the specified names exist
            deleteProjectsWithName(name);
            deleteProjectsWithName(newName);
            EntityManager em = createEntityManager();
            Employee emp = null;
            List employees = em.createQuery("SELECT OBJECT(e) FROM Employee e").getResultList();
            if(employees.size() > 0) {
                emp = (Employee)employees.get(0);
            } else {
                em.getTransaction().begin();
                try {
                    emp = new Employee();
                    emp.setFirstName(name);
                    emp.setLastName("TeamLeader");
                    em.persist(emp);
                    em.getTransaction().commit();
                    empTemp = emp;
                } catch (RuntimeException ex) {
                    if(em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    em.close();
                    throw ex;
                }
            }
            em.close();
            // populate Projects
            createProjectsWithName(name, emp);
    
            // test
            em = createEntityManager();
            em.getTransaction().begin();
            try {
                em.createQuery("UPDATE "+className+" p set p.name = '"+newName+"' WHERE p.name = '"+name+"' AND p.teamLeader IS NULL").executeUpdate();
                em.getTransaction().commit();
            } catch (RuntimeException ex) {
                if(em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw ex;
            } finally {
                em.close();
            }
            
            // verify
            em = createEntityManager();
            String errorMsg = "";
            List projects = em.createQuery("SELECT OBJECT(p) FROM Project p WHERE p.name = '"+newName+"' OR p.name = '"+name+"'").getResultList();
            for(int i=0; i<projects.size(); i++) {
                Project p = (Project)projects.get(i);
                String readName = p.getName();
                if(cls.isInstance(p) && p.getTeamLeader()==null) {
                    if(!readName.equals(newName)) {
                        errorMsg = errorMsg + "haven't updated name: " + p + "; ";
                    }
                } else {
                    if(readName.equals(newName)) {
                        errorMsg = errorMsg + "have updated name: " + p + "; ";
                    }
                }
            }
            em.close();
            
            if(errorMsg.length() > 0) {
                fail(errorMsg);
            } else {
                ok = true;
            }
        } finally {
            // clean-up
            // make sure no projects with the specified names exist
            try {
                deleteProjectsWithName(name);
                deleteProjectsWithName(newName);
                if(empTemp != null) {
                    EntityManager em = createEntityManager();
                    em.getTransaction().begin();
                    try {
                        em.createQuery("DELETE FROM Employee e WHERE e.id = '"+empTemp.getId()+"'").executeUpdate();
                        em.getTransaction().commit();
                    } catch (RuntimeException ex) {
                        if(em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        throw ex;
                    } finally {
                        em.close();
                    }
                }
            } catch (RuntimeException ex) {
                // eat clean-up exception in case the test failed
                if(ok) {
                    throw ex;
                }
            }
        }
    }
    
    public void testRollbackOnlyOnException() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            Employee emp = em.find(Employee.class, "");
            fail("IllegalArgumentException has not been thrown");
        } catch(IllegalArgumentException ex) {
            assertTrue("Transaction is not roll back only", em.getTransaction().getRollbackOnly());
        } finally {
            em.getTransaction().rollback();
            em.close();
        }
    }

    public void testClosedEmShouldThrowException() {
        EntityManager em = createEntityManager();
        em.close();
        String errorMsg = "";

        try {
            em.clear();
            errorMsg = errorMsg + "; em.clear() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.clear() threw wrong exception: " + ex.getMessage();
        }
        try {
            em.close();
            errorMsg = errorMsg + "; em.close() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.close() threw wrong exception: " + ex.getMessage();
        }
        try {
            em.contains(null);
            errorMsg = errorMsg + "; em.contains() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.contains threw() wrong exception: " + ex.getMessage();
        }
        try {
            em.getDelegate();
            errorMsg = errorMsg + "; em.getDelegate() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.getDelegate() threw wrong exception: " + ex.getMessage();
        }
        try {
            em.getReference(Employee.class, new Integer(1));
            errorMsg = errorMsg + "; em.getReference() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.getReference() threw wrong exception: " + ex.getMessage();
        }
        try {
            em.joinTransaction();
            errorMsg = errorMsg + "; em.joinTransaction() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.joinTransaction() threw wrong exception: " + ex.getMessage();
        }
        try {
            em.lock(null, null);
            errorMsg = errorMsg + "; em.lock() didn't throw exception";
        } catch(IllegalStateException ise) {
            // expected
        } catch(RuntimeException ex) {
            errorMsg = errorMsg + "; em.lock() threw wrong exception: " + ex.getMessage();
        }
        
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    //gf 1217 - Ensure join table defaults correctly when 'mappedby' not specified
    public void testOneToManyDefaultJoinTableName() {
        Department dept  = new Department();
        Employee manager = new Employee();
        dept.addManager(manager);
        
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(dept);
            em.getTransaction().commit();
        }catch (RuntimeException e) {
            throw e;
        }finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
    // gf1732
    public void testMultipleEntityManagerFactories() {
        // close the original factory
        closeEntityManagerFactory();
        // create the new one - not yet deployed
        EntityManagerFactory factory1 =  getEntityManagerFactory();
        // create the second one
        EntityManagerFactory factory2 =  Persistence.createEntityManagerFactory("default", getDatabaseProperties());
        // deploy
        factory2.createEntityManager();
        // close
        factory2.close();

        try {
            // now try to getEM from the first one - this used to throw exception
            factory1.createEntityManager();
            // don't close factory1 if all is well
        } catch (PersistenceException ex) {
            fail("factory1.createEM threw exception: " + ex.getMessage());
            factory1.close();
        }
    }

    // gf2074: EM.clear throws NPE
    public void testClearEntityManagerWithoutPersistenceContext() {
        EntityManager em = createEntityManager();
        try {
            em.clear();
        }finally {
            em.close();
        }
    }
    
    // Used by testClearEntityManagerWithoutPersistenceContextSimulateJTA().
    // At first tried to use JTATransactionController class, but that introduced dependencies 
    // on javax.transaction package (and therefore failed in gf entity persistence tests).
    static class DummyExternalTransactionController extends oracle.toplink.essentials.transaction.AbstractTransactionController {
        public boolean isRolledBack_impl(Object status){return false;}
        protected void registerSynchronization_impl(oracle.toplink.essentials.transaction.AbstractSynchronizationListener listener, Object txn) throws Exception{}
        protected Object getTransaction_impl() throws Exception {return null;}
        protected Object getTransactionKey_impl(Object transaction) throws Exception {return null;}
        protected Object getTransactionStatus_impl() throws Exception {return null;}
        protected void beginTransaction_impl() throws Exception{}
        protected void commitTransaction_impl() throws Exception{}
        protected void rollbackTransaction_impl() throws Exception{}
        protected void markTransactionForRollback_impl() throws Exception{}
        protected boolean canBeginTransaction_impl(Object status){return false;}
        protected boolean canCommitTransaction_impl(Object status){return false;}
        protected boolean canRollbackTransaction_impl(Object status){return false;}
        protected boolean canIssueSQLToDatabase_impl(Object status){return false;}
        protected boolean canMergeUnitOfWork_impl(Object status){return false;}
        protected String statusToString_impl(Object status){return "";}
    }
    // gf2074: EM.clear throws NPE (JTA case)
    public void testClearEntityManagerWithoutPersistenceContextSimulateJTA() {
        EntityManager em = createEntityManager();
        ServerSession ss = ((oracle.toplink.essentials.ejb.cmp3.EntityManager)em).getServerSession();
        em.close();
        // in non-JTA case session doesn't have external transaction controller
        boolean hasExternalTransactionController = ss.hasExternalTransactionController();
        if(!hasExternalTransactionController) {
            // simulate JTA case
            ss.setExternalTransactionController(new DummyExternalTransactionController());
        }
        try {
            testClearEntityManagerWithoutPersistenceContext();
        }finally {
            if(!hasExternalTransactionController) {
                // remove the temporary set TransactionController
                ss.setExternalTransactionController(null);
            }
        }
    }
    
    // gf1597: CascadeType.ALL does not work when removing entity
    public void testCascadeDeleteSelfReference() {
        EntityManager em = createEntityManager();
        Employee e1 = new Employee();
        e1.setFirstName("Employee1");
        Employee e2 = new Employee();
        e2.setFirstName("Employee2");
        Employee e3 = new Employee();
        e2.setFirstName("Employee3");
        
        em.getTransaction().begin();
        em.persist(e1);
        e2.setManager(e1);
        e1.getManagedEmployees().add(e2);
        e3.setManager(e1);
        e1.getManagedEmployees().add(e3);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.remove(e1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        }  finally {
            // clean up
           em.close();
        }
    }
    
    // gf1597: CascadeType.ALL does not work when removing entity
    public void testCascadeDeleteSelfReference1() {
        EntityManager em = createEntityManager();
        Employee e1 = new Employee();
        e1.setFirstName("Employee1");
        Employee e2 = new Employee();
        e2.setFirstName("Employee2");
        Employee e3 = new Employee();
        e2.setFirstName("Employee3");
        
        em.getTransaction().begin();
        em.persist(e1);
        e2.setManager(e1);
        e1.getManagedEmployees().add(e2);
        e3.setManager(e2);
        e2.getManagedEmployees().add(e3);
        e1.setManager(e3);
        e3.getManagedEmployees().add(e1);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        em.remove(e1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        }  finally {
            // clean up
           em.close();
        }
    }
    
    // gf1597: CascadeType.ALL does not work when removing entity
    public void testCascadeDeleteSelfReference2() {
        EntityManager em = createEntityManager();
        Employee e1 = new Employee();
        e1.setFirstName("Employee1");
        Employee e2 = new Employee();
        e2.setFirstName("Employee2");
        Employee e3 = new Employee();
        e2.setFirstName("Employee3");
        Integer id = null;
        
        em.getTransaction().begin();
        em.persist(e1);
        e2.setManager(e1);
        e1.getManagedEmployees().add(e2);
        e3.setManager(e1);
        e1.getManagedEmployees().add(e3);
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        // make sure e2 is not removed
        e2.setManager(null);
        e1.getManagedEmployees().remove(e2);
        id = e2.getId();
        
        em.remove(e1);
        try {
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        }  finally {
            // clean up
           em.close();
        }
        
        EntityManager em1 = createEntityManager();
        assertTrue("Employee e2 should not have been removed!", 
                em1.createQuery("SELECT e FROM Employee e WHERE e.id = ?1").setParameter(1, id).getSingleResult() != null);
        em1.close();
    }
    
    // GF 2621
    public void testDoubleMerge(){
        EntityManager em = createEntityManager();
        
        Employee employee = new Employee();
        employee.setId(44);
        employee.setVersion(0);
        employee.setFirstName("Alfie");

        Employee employee2 = new Employee();
        employee2.setId(44);
        employee2.setVersion(0);
        employee2.setFirstName("Phillip");        
        
        try {
            em.getTransaction().begin();
            em.merge(employee);
            em.merge(employee2);
            em.flush();
        } catch (PersistenceException e){
            fail("A double merge of an object with the same key, caused two inserts instead of one.");
        } finally {
            em.getTransaction().rollback();
        }
    }

    // gf 3032
    public void testPessimisticLockHintStartsTransaction(){
        Assert.assertFalse("Warning: DerbyPlatform does not currently support pessimistic locking",  ((Session)JUnitTestCase.getServerSession()).getPlatform().isDerby());
        Assert.assertFalse("Warning: PostgreSQLPlatform. does not currently support pessimistic locking",  ((Session)JUnitTestCase.getServerSession()).getPlatform().isPostgreSQL());
        oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl em = (oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl)createEntityManager();
        em.getTransaction().begin();
        Query query = em.createNamedQuery("findAllEmployeesByFirstName");
        query.setHint("toplink.pessimistic-lock", PessimisticLock.Lock);
        query.setParameter("firstname", "Sarah");
        List results = query.getResultList();
        assertTrue("The extended persistence context is not in a transaction after a pessmimistic lock query", em.getActivePersistenceContext(em.getTransaction()).getParent().isInTransaction());
        
        em.getTransaction().rollback();
        
    }
    
    public void testManagedEmployeesMassInsertUseSequencing() throws Exception {
        internalTestManagedEmployeesMassInsertOrMerge(true, true);
    }
    
    public void testManagedEmployeesMassInsertDoNotUseSequencing() throws Exception {
        internalTestManagedEmployeesMassInsertOrMerge(true, false);
    }
        
    public void testManagedEmployeesMassMergeUseSequencing() throws Exception {
        internalTestManagedEmployeesMassInsertOrMerge(false, true);
    }
    
   public void testManagedEmployeesMassMergeDoNotUseSequencing() throws Exception {
        internalTestManagedEmployeesMassInsertOrMerge(false, false);
    }
    
    // gf3152: toplink goes into a 2^n loop and comes to a halt on two cascade.
    // before the fix this method took almost 10 minutes to complete on my local Oracle db.
    // The test 
    // shouldInsert == true indicate that em.persist should be used, 
    // otherwise em.merge.
    // shouldUseSequencing == true indicates that the pk value will be assigned by sequencing,
    // otherwise the test generates ids and directly assignes them into the newly created objects.
    protected void internalTestManagedEmployeesMassInsertOrMerge(boolean shouldInsert, boolean shouldUseSequencing) throws Exception {
        // Example: nLevels == 3; nDirects = 4.
        // First all the Employees corresponding to nLevels and nDirects values are created:
        // There is always the single (highest ranking) topEmployee on Level_0;
        // He/she has 4 Level_1 direct subordinates;
        // each of those has 4 Level_2 directs, 
        // each of those has 4 Level_3 directs.
        // For debugging: 
        // Employee's firstName is always his level (in "Level_2" format);
        // Employee's lastName is his number in his level (from 0 to number of employees of this level - 1)
        // in "Number_3" format.

        // number of management levels
        int nLevels = 2;
        // number of direct employees each manager has
        int nDirects = 50;        
        // used to keep ids in case sequencing is not used
        int id = 0;
        EntityManager em = null;
        if(!shouldUseSequencing) {
            // obtain the first unused sequence number
            Employee emp = new Employee();
            em = createEntityManager();
            em.getTransaction().begin();
            em.persist(emp);
            id = emp.getId();
            em.getTransaction().rollback();
            em.close();
        }

        // topEmployee - the only one on level 0.
        Employee topEmployee = new Employee();
        topEmployee.setFirstName("Level_0");
        topEmployee.setLastName("Number_0");
        if(!shouldUseSequencing) {
            topEmployee.setId(id++);
        }

        // During each nLevel loop iterartion 
        // this array contains direct managers for the Employees to be created -
        // all the Employees of nLevel - 1 level.
        ArrayList<Employee> employeesForHigherLevel = new ArrayList<Employee>(1);
        // In the end of each nLevel loop iterartion 
        // this array contains all Employees created during this iteration -
        // all the Employees of nLevel level.
        ArrayList<Employee> employeesForCurrentLevel;
        employeesForHigherLevel.add(topEmployee);
        // total number of employees
        int nEmployeesTotal = 1;
        for (int nLevel = 1; nLevel <= nLevels; nLevel++) {
            employeesForCurrentLevel = new ArrayList<Employee>(employeesForHigherLevel.size() * nDirects);
            Iterator<Employee> it = employeesForHigherLevel.iterator();
            while(it.hasNext()) {
                Employee mgr = it.next();
                for(int nCurrent = 0; nCurrent < nDirects; nCurrent++) {
                    Employee employee = new Employee();
                    employee.setFirstName("Level_" + nLevel);
                    employee.setLastName("Number_" + employeesForCurrentLevel.size());
                    if(!shouldUseSequencing) {
                        employee.setId(id++);
                    }
                    employeesForCurrentLevel.add(employee);
                    mgr.addManagedEmployee(employee);
                }
            }
            employeesForHigherLevel = employeesForCurrentLevel;
            nEmployeesTotal = nEmployeesTotal + employeesForCurrentLevel.size();
        }
        
        em = createEntityManager();
        em.getTransaction().begin();
        try {
            if(shouldInsert) {
                em.persist(topEmployee);
            } else {
                em.merge(topEmployee);
            }
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }            

        // cleanup
        em = createEntityManager();
        em.getTransaction().begin();
        try {
            em.createQuery("DELETE FROM Employee e WHERE e.firstName LIKE 'Level_%'").executeUpdate();
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            ((EntityManagerImpl)em).getServerSession().getIdentityMapAccessor().initializeAllIdentityMaps();
            em.close();
        }
    }

    // gf3585: bug 6006423: BULK DELETE QUERY FOLLOWED BY A MERGE RETURNS DELETED OBJECT
    public void testBulkDeleteThenMerge() {
        String firstName = "testBulkDeleteThenMerge";

        // setup - create Employee
        EntityManager em = createEntityManager(); 
        em.getTransaction().begin();
        Employee emp = new Employee();
        emp.setFirstName(firstName);
        emp.setLastName("Original");
        em.persist(emp); 
        em.getTransaction().commit();
        em.close();

        int id = emp.getId();
        
        // test
        // delete the Employee using bulk delete
        em = createEntityManager(); 
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Employee e WHERE e.firstName = '"+firstName+"'").executeUpdate(); 
        em.getTransaction().commit();
        em.close();

        // then re-create and merge the Employee using the same pk
        em = createEntityManager(); 
        em.getTransaction().begin();
        emp = new Employee();
        emp.setId(id);
        emp.setFirstName(firstName);
        emp.setLastName("New");
        em.merge(emp); 
        try {
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
        
        // verify
        String errorMsg = "";
        em = createEntityManager(); 
        // is the right Employee in the cache?
        emp = (Employee)em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.id = " + id).getSingleResult();
        if(emp == null) {
            errorMsg = "Cache: Employee is not found; ";
        } else {
            if(!emp.getLastName().equals("New")) {
                errorMsg = "Cache: wrong lastName = "+emp.getLastName()+"; should be New; ";
            }
        }
        // is the right Employee in the db?
        emp = (Employee)em.createQuery("SELECT OBJECT(e) FROM Employee e WHERE e.id = " + id).setHint("toplink.refresh", Boolean.TRUE).getSingleResult();
        if(emp == null) {
            errorMsg = errorMsg + "DB: Employee is not found";
        } else {
            if(!emp.getLastName().equals("New")) {
                errorMsg = "DB: wrong lastName = "+emp.getLastName()+"; should be New";
            }
            // clean up in case the employee is in the db
            em.getTransaction().begin();
            em.remove(emp);
            em.getTransaction().commit();
        }
        em.close();

        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public void testNativeSequences() {
        ServerSession ss = JUnitTestCase.getServerSession();
        boolean doesPlatformSupportIdentity = ss.getPlatform().supportsIdentity();
        boolean doesPlatformSupportSequenceObjects = ss.getPlatform().supportsSequenceObjects();
        String errorMsg = "";
        
        // SEQ_GEN_IDENTITY sequence defined by
        // @GeneratedValue(strategy=IDENTITY)
        boolean isIdentity = ss.getPlatform().getSequence("SEQ_GEN_IDENTITY").shouldAcquireValueAfterInsert();
        if(doesPlatformSupportIdentity != isIdentity) {
            errorMsg = "SEQ_GEN_IDENTITY: doesPlatformSupportIdentity = " + doesPlatformSupportIdentity +", but isIdentity = " + isIdentity +"; ";
        }

        // ADDRESS_SEQ sequence defined by
        // @GeneratedValue(generator="ADDRESS_SEQ")
        // @SequenceGenerator(name="ADDRESS_SEQ", allocationSize=25)
        boolean isSequenceObject = !ss.getPlatform().getSequence("ADDRESS_SEQ").shouldAcquireValueAfterInsert();
        if(doesPlatformSupportSequenceObjects != isSequenceObject) {
            errorMsg = errorMsg +"ADDRESS_SEQ: doesPlatformSupportSequenceObjects = " + doesPlatformSupportSequenceObjects +", but isSequenceObject = " + isSequenceObject;
        }
        
        if(errorMsg.length() > 0) {
            fail(errorMsg);
        }
    }
    
    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
