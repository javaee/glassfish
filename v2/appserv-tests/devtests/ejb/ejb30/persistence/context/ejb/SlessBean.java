package com.sun.s1asdev.ejb.ejb30.persistence.context;

import java.util.Vector;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.EJBException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.TransactionRequiredException;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
public class SlessBean implements Sless {

    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-context-par1.jar#em", properties={ @PersistenceProperty(name="foo", value="bar"), @PersistenceProperty(name="foobar", value="123") } ) EntityManager emCreate;
    private @PersistenceContext(unitName="lib/ejb-ejb30-persistence-context-par3.jar#em") EntityManager emFind;
    private @PersistenceContext(unitName="em2") EntityManager emRemove;

    public void createPerson(String name) {
        
        Person p = new Person(name);

        boolean containsP = emCreate.contains(p);
        System.out.println("before persist containsP = " + containsP);        
        if( containsP ) {
            throw new EJBException("contains() should be false");
        }

        emCreate.persist(p);
        System.out.println("Created " + p);

        containsP = emCreate.contains(p);
        System.out.println("after persist , containsP = " + containsP);
        if( !containsP ) {
            throw new EJBException("contains() should be true");
        }


        try {
            emCreate.close();
            throw new EJBException("close() should have thrown an exception");
        } catch(IllegalStateException ise) {
            System.out.println("Got expected IllegalStateException " +
                               "when calling close() on a container-managed " +
                               "EntityManager");
        }
        
        // isOpen doesn't make any sense on a container-managed entity manager,
        // but it shouldn't throw an exception.
        emCreate.isOpen();

    }

    public Person findPerson(String name) {

        Person p = emFind.find(Person.class, name);

        System.out.println("Found " + p);

        return p;
    }

    public void removePerson(String name) {

        Person p = emRemove.find(Person.class, name);

        emRemove.remove(p);

        // make sure calling flush works within a tx
        emCreate.flush();

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void nonTxTest2(String name) {

        Query q1 = emFind.createQuery("SELECT OBJECT(p) FROM Person p");
        List results = q1.getResultList();
        System.out.println("results = " + results);
        if( results.size() != 2) {
            throw new EJBException("wrong num results");
        }

        Query q2 = emFind.createQuery("SELECT OBJECT(p) FROM Person p");
        q2.setMaxResults(1);
        List results2 = q2.getResultList();
        System.out.println("results = " + results2);
        if( results2.size() != 1) {
            throw new EJBException("wrong num results");
        }

        // call it again on same object to make sure max results in
        // honored the second time around as well
        List results3 = q2.getResultList();
        System.out.println("results = " + results3);
        if( results3.size() != 1) {
            throw new EJBException("wrong num results");
        }
        
        
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Person nonTxFindPerson(String name) {

        Person p = emFind.find(Person.class, name);
        System.out.println("Found " + p);

        Person p2 = emFind.find(Person.class, name);
        System.out.println("Found " + p2);

        if( !name.equals(p2.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        System.out.println("Getting q1 from EM");
        Query q1 = emFind.createQuery("SELECT OBJECT(p) FROM Person p WHERE p.name='" + name + "'"); 

        System.out.println("Executing q1");
        Person p3 = (Person) q1.getSingleResult();
        System.out.println("Found " + p3);

        if( !name.equals(p3.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        boolean containsP3 = emFind.contains(p3);

        if( containsP3 ) {
            throw new EJBException("incorrect value of non-tx contains()");
        }

        try {
            emFind.contains(new Object());
            throw new EJBException("Expected IllegalArgumentException");
        } catch(IllegalArgumentException iae) {
            System.out.println("Successfully got IllegalArgumentException " +
                               "when calling contains with non-entity");
        }


        System.out.println("Getting q2 from EM");
        Query q2 = emFind.createQuery("SELECT OBJECT(p) FROM Person p WHERE p.name='" + name + "'"); 

        try {
            q2.setFirstResult(-1);
            throw new EJBException("Expected IllegalArgumentException");
        } catch(IllegalArgumentException iae) {
            System.out.println("Got expected IllegalArgumentException when calling setFirstResult with negative value");
        }

        System.out.println("Getting named query q3 from EM");
        Query q3 = emFind.createNamedQuery("findPersonByName");

        System.out.println("Executing q2");
        Person p4 = (Person) q2.getSingleResult();
        System.out.println("Found " + p4);
        if( !name.equals(p4.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        // Make sure we can still use q1
        System.out.println("Executing q1 again");
        Person p5 = (Person) q1.getSingleResult();
        System.out.println("Found " + p5);
        if( !name.equals(p5.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        System.out.println("Calling getReference");
        Person p6 = emFind.getReference(Person.class, name);
        System.out.println("Found " + p6);

        if( !name.equals(p6.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }


        try {
            emFind.flush();
            throw new EJBException("flush should have thrown an exception");
        } catch(TransactionRequiredException tre) {
            System.out.println("Got expected TransactionRequiredException " +
                               "when calling flush outside a transaction");
        }

        try {
            emFind.persist(p);
            throw new EJBException("persist should have thrown an exception");
        } catch(TransactionRequiredException tre) {
            System.out.println("Got expected TransactionRequiredException " +
                               "when calling persist outside a transaction");
        }

        try {
            emFind.merge(p);
            throw new EJBException("merge should have thrown an exception");
        } catch(TransactionRequiredException tre) {
            System.out.println("Got expected TransactionRequiredException " +
                               "when calling merge outside a transaction");
        }
        
        try {
            emFind.remove(p);
            throw new EJBException("remove should have thrown an exception");
        } catch(TransactionRequiredException tre) {
            System.out.println("Got expected TransactionRequiredException " +
                               "when calling remove outside a transaction");
        }

        try {
            emFind.refresh(p);
            throw new EJBException("refresh should have thrown an exception");
        } catch(TransactionRequiredException tre) {
            System.out.println("Got expected TransactionRequiredException " +
                               "when calling refresh outside a transaction");
        }

        try {
            emFind.close();
            throw new EJBException("close() should have thrown an exception");
        } catch(IllegalStateException ise) {
            System.out.println("Got expected IllegalStateException " +
                               "when calling close() on a container-managed " +
                               "EntityManager");
        }

        System.out.println("Executing q3");
        q3.setParameter("pName", name);

        try {
            q3.setMaxResults(-1);
            throw new EJBException("Expected IllegalArgumentException");
        } catch(IllegalArgumentException iae) {
            System.out.println("Got expected IllegalArgumentException when calling setMaxResults with negative value");
        }


        Person p7 = (Person) q3.getSingleResult();
        System.out.println("Found " + p7);
        if( !name.equals(p7.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        Person p8 = (Person) q3.getSingleResult();
        System.out.println("Found " + p8);
        if( !name.equals(p8.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }
        
        Query q4 = emFind.createNativeQuery("SELECT p.name FROM EJB30_PERSISTENCE_CONTEXT_PERSON p WHERE p.name LIKE '" + name + "'");
        String p9 = (String) q4.getSingleResult(); // ((Vector) q4.getSingleResult()).elementAt(0);
        System.out.println("Found " + p9);
        if( !name.equals(p9) ) {
            throw new EJBException("finder results not equivalent");
        }
        // execute native query again
        String p10 = (String) q4.getSingleResult(); // ((Vector) q4.getSingleResult()).elementAt(0);
        System.out.println("Found " + p10);
        if( !name.equals(p10) ) {
            throw new EJBException("finder results not equivalent");
        }
        

        Query q5 = emFind.createNativeQuery("SELECT p.name FROM EJB30_PERSISTENCE_CONTEXT_PERSON p WHERE p.name LIKE '" + name + "'", Person.class);
        Person p11 = (Person) q5.getSingleResult();
        System.out.println("Found " + p11);
        if( !name.equals(p11.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        // Execute native query again
        Person p12 = (Person) q5.getSingleResult();
        System.out.println("Found " + p12);
        if( !name.equals(p12.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        Query q6 = emFind.createNativeQuery("SELECT p.name FROM EJB30_PERSISTENCE_CONTEXT_PERSON p WHERE p.name LIKE '" + name + "'", "PersonSqlMapping");
        
        Person p13 = (Person) q6.getSingleResult();
        System.out.println("Found " + p13);
        if( !name.equals(p13.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        // Execute native query again
        Person p14 = (Person) q6.getSingleResult();
        System.out.println("Found " + p14);
        if( !name.equals(p14.getName()) ) {
            throw new EJBException("finder results not equivalent");
        }

        return p;
    }

}
