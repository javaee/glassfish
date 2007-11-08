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

import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.PerformanceTireInfo;
import oracle.toplink.essentials.testing.models.cmp3.inheritance.TireInfo;

public class ReportQueryMultipleReturnInheritanceTestSuite extends JUnitTestCase {
    protected boolean m_reset = false;    // reset gets called twice on error
    protected PerformanceTireInfo tireInfo;
        
    public ReportQueryMultipleReturnInheritanceTestSuite() {
    }
    
    public ReportQueryMultipleReturnInheritanceTestSuite(String name) {
        super(name);
    }
    
    public void setUp () {
        super.setUp();
        m_reset = true;
        this.tireInfo = new PerformanceTireInfo();
        tireInfo.setPressure(32);
        tireInfo.setSpeedRating(220);
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        try{
            em.persist(tireInfo);
            em.getTransaction().commit();
        }catch (RuntimeException ex){
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
            throw ex;
        }
    }
    
    public void tearDown () {
        if (m_reset) {
            EntityManager em = createEntityManager();
            em.getTransaction().begin();
            try{
                TireInfo localTire = (TireInfo)em.find(TireInfo.class, tireInfo.getId());
                em.remove(localTire);
                em.getTransaction().commit();
            }catch (RuntimeException ex){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
                throw ex;
            }
            m_reset = false;
            super.tearDown();
        }
    }
    
    public void testInheritanceMultiTableException(){
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.returnWithoutReportQueryResult();
        reportQuery.setReferenceClass(TireInfo.class);
        reportQuery.addAttribute("tireinfo",reportQuery.getExpressionBuilder());
        List result = (List)getServerSession().executeQuery(reportQuery);
        Object resultItem = result.get(0);
        assertTrue("Failed to return Employees correctly, Not A PerformanceTireInfo", PerformanceTireInfo.class.isAssignableFrom(resultItem.getClass()));
        assertTrue("Did not populate all fields.  Missing 'pressure'", ((PerformanceTireInfo)resultItem).getPressure() != null);
        assertTrue("Did not populate all fields.  Missing 'speedrating'", ((PerformanceTireInfo)resultItem).getSpeedRating() != null);
    }
    
    
    public static Test suite() {
        return new TestSuite(ReportQueryMultipleReturnInheritanceTestSuite.class) {
        
            protected void setUp(){               
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    

}
