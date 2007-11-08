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
package oracle.toplink.essentials.testing.tests.cmp3.advanced.compositepk;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.Query;

import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;
import oracle.toplink.essentials.sessions.DatabaseSession;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.Cubicle;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.JuniorScientist;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.Scientist;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.ScientistPK;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.Department;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.DepartmentPK;
import oracle.toplink.essentials.testing.models.cmp3.advanced.compositepk.CompositePKTableCreator;
 
public class AdvancedCompositePKJunitTest extends JUnitTestCase {
    private static DepartmentPK m_departmentPK;
    private static ScientistPK m_scientist1PK, m_scientist2PK, m_scientist3PK, m_jScientistPK; 
    
    public AdvancedCompositePKJunitTest() {
        super();
    }
    
    public AdvancedCompositePKJunitTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
    
    public void setUp() {
        clearCache();
        super.setUp();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("AdvancedCompositePKJunitTest");
        suite.addTest(new AdvancedCompositePKJunitTest("testCreateDepartment"));
        suite.addTest(new AdvancedCompositePKJunitTest("testCreateScientists"));
        suite.addTest(new AdvancedCompositePKJunitTest("testReadDepartment"));
        suite.addTest(new AdvancedCompositePKJunitTest("testReadJuniorScientist"));
        suite.addTest(new AdvancedCompositePKJunitTest("testAnyAndAll"));

        return new TestSetup(suite) {
            protected void setUp() {               
                DatabaseSession session = JUnitTestCase.getServerSession();
                new CompositePKTableCreator().replaceTables(session);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testCreateDepartment() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try {
            // make sure the department is not left from the previous test run
            em.createQuery("DELETE FROM Department d WHERE d.name = 'DEPT A' AND d.role = 'ROLE A' AND d.location = 'LOCATION A'").executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
        }
        clearCache();
        em.close();
        em = createEntityManager();
        em.getTransaction().begin();
        try {
        
            Department department = new Department();
            department.setName("DEPT A");
            department.setRole("ROLE A");
            department.setLocation("LOCATION A");
            em.persist(department);
            
            em.getTransaction().commit();
            m_departmentPK = department.getPK();
        } catch (RuntimeException e) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw e;
        }
    }
    
    public void testCreateScientists() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {    
            Department department = (Department) em.merge(em.find(Department.class, m_departmentPK));
            
            Cubicle cubicle1 = new Cubicle("G");
            em.persist(cubicle1);
            
            Scientist scientist1 = new Scientist();
            scientist1.setFirstName("Guy");
            scientist1.setLastName("Pelletier");
            scientist1.setCubicle(cubicle1);
            department.addScientist(scientist1);
            em.persist(scientist1);
        
            Cubicle cubicle2 = new Cubicle("T");
            em.persist(cubicle2);
            
            Scientist scientist2 = new Scientist();
            scientist2.setFirstName("Tom");
            scientist2.setLastName("Ware");
            scientist2.setCubicle(cubicle2);
            department.addScientist(scientist2);
            em.persist(scientist2);
            
            Cubicle cubicle3 = new Cubicle("G");
            em.persist(cubicle3);
            
            Scientist scientist3 = new Scientist();
            scientist3.setFirstName("Gordon");
            scientist3.setLastName("Yorke");
            scientist3.setCubicle(cubicle3);
            department.addScientist(scientist3);
            em.persist(scientist3);
            
            Cubicle cubicle4 = new Cubicle("J");
            em.persist(cubicle4);
            
            JuniorScientist jScientist = new JuniorScientist();
            jScientist.setFirstName("Junior");
            jScientist.setLastName("Sao");
            jScientist.setCubicle(cubicle4);
            department.addScientist(jScientist);
            em.persist(jScientist);
            
            em.getTransaction().commit();
            m_scientist1PK = scientist1.getPK();
            m_scientist2PK = scientist2.getPK();
            m_scientist3PK = scientist3.getPK();
            m_jScientistPK = jScientist.getPK();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw e;
        }
    }
    
    public void testReadDepartment() {
        Department department = (Department) createEntityManager().find(Department.class, m_departmentPK);
        
        assertTrue("Error on reading back the ordered department list.", department != null);
        assertTrue("The number of scientists were incorrect.", department.getScientists().size() > 0);
    }
    
    public void testReadJuniorScientist() {
        JuniorScientist jScientist;
        
        jScientist = (JuniorScientist) createEntityManager().find(JuniorScientist.class, m_jScientistPK);
        assertTrue("Error on reading back the junior scientist.", jScientist != null);
    }

    //bug gf672 - JBQL Select query with IN/ANY in WHERE clause and subselect fails.
    public void testAnyAndAll() {
        EntityManager em = createEntityManager();
        
        // queries to test
        
        Query query1 = em.createQuery("SELECT s FROM Scientist s WHERE s = ANY (SELECT s2 FROM Scientist s2)");
        List<Scientist> results1 = query1.getResultList();

        Query query2 = em.createQuery("SELECT s FROM Scientist s WHERE s = ALL (SELECT s2 FROM Scientist s2)");
        List<Scientist> results2 = query2.getResultList();

        Query query3 = em.createQuery("SELECT s FROM Scientist s WHERE s.department = ALL (SELECT DISTINCT d FROM Department d WHERE d.name = 'DEPT A' AND d.role = 'ROLE A' AND d.location = 'LOCATION A')");
        List<Scientist> results3 = query3.getResultList();

        Query query4 = em.createQuery("SELECT s FROM Scientist s WHERE s.department = ANY (SELECT DISTINCT d FROM Department d JOIN d.scientists ds JOIN ds.cubicle c WHERE c.code = 'G')");
        List<Scientist> results4 = query4.getResultList();

        // control queries
        
        Query controlQuery1 = em.createQuery("SELECT s FROM Scientist s");
        List<Scientist> controlResults1 = controlQuery1.getResultList();
        
        List<Scientist> controlResults2;
        if(controlResults1.size() == 1) {
            controlResults2 = controlResults1;
        } else {
            controlResults2 = new ArrayList<Scientist>();
        }
        
        Query controlQuery3 = em.createQuery("SELECT s FROM Scientist s JOIN s.department d WHERE d.name = 'DEPT A' AND d.role = 'ROLE A' AND d.location = 'LOCATION A'");
        List<Scientist> controlResults3 = controlQuery3.getResultList();
        
        Query controlQuery4 = em.createQuery("SELECT s FROM Scientist s WHERE EXISTS (SELECT DISTINCT d FROM Department d JOIN d.scientists ds JOIN ds.cubicle c WHERE c.code = 'G' AND d = s.department)");
        List<Scientist> controlResults4 = controlQuery4.getResultList();

        em.close();        

        // compare results - they should be the same
        compareResults(results1, controlResults1, "query1");
        compareResults(results2, controlResults2, "query2");
        compareResults(results3, controlResults3, "query3");
        compareResults(results4, controlResults4, "query4");
    } 

    protected void compareResults(List results, List controlResults, String testName) {
        if(results.size() != controlResults.size()) {
            fail(testName + ": results.size() = " + results.size() + "; controlResults.size() = " + controlResults.size());
        }        
        for (Object s : results) {
            if(!controlResults.contains(s)) {
                fail(testName + ": " + s + "contained in results but not in controlResults");
            }
        }
    }
}
