package oracle.toplink.essentials.testing.tests.ejb.ejbqltesting;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.TemporalType;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

import oracle.toplink.essentials.testing.framework.junit.JUnitTestCase;
import oracle.toplink.essentials.testing.models.cmp3.datetime.DateTimePopulator;
import oracle.toplink.essentials.testing.models.cmp3.datetime.DateTimeTableCreator;

//Test all kinds of combinations of date time types
public class JUnitEJBQLDateTimeTestSuite extends JUnitTestCase {
    public JUnitEJBQLDateTimeTestSuite() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitEJBQLDateTimeTestSuite.class);

        return new TestSetup(suite) {
            protected void setUp(){
                new DateTimeTableCreator().replaceTables(JUnitTestCase.getServerSession());
                
                DateTimePopulator dateTimePopulator = new DateTimePopulator();                
                dateTimePopulator.persistExample(getServerSession());   
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    public void testSqlDate() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.date = :date").
            setParameter("date", cal.getTime(), TemporalType.DATE).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testSqlDateToTS() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.date = :date").
            setParameter("date", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTime() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.time = :time").
            setParameter("time", cal.getTime(), TemporalType.TIME).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimeToTS() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.time = :time").
            setParameter("time", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimestamp() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp = :timestamp").
            setParameter("timestamp", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimestampToDate() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp = :timestamp").
            setParameter("timestamp", cal.getTime(), TemporalType.DATE).
            getResultList();
        
        assertTrue("There should be zero result", result.size() == 0);
    }

    public void testTimestampToTime() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp = :timestamp").
            setParameter("timestamp", cal.getTime(), TemporalType.TIME).
            getResultList();
        
        assertTrue("There should be zero result", result.size() == 0);
    }

   public void testUtilDate() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.utilDate = :utilDate").
            setParameter("utilDate", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testCalenderWithUtilDate() {
         GregorianCalendar cal = new GregorianCalendar(); 
         cal.set(1901, 11, 31, 23, 59, 59);
         cal.set(Calendar.MILLISECOND, 999);

         List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.calendar = :calendar").
             setParameter("calendar", cal.getTime(), TemporalType.TIMESTAMP).
             getResultList();
         
         assertTrue("There should be one result", result.size() == 1);
     }

    public void testSqlDateWithCal() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.date = :date").
            setParameter("date", cal, TemporalType.DATE).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimeWithCal() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.time = :time").
            setParameter("time", cal, TemporalType.TIME).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimestampWithCal() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp = :timestamp").
            setParameter("timestamp", cal, TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testUtilDateWithCal() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.utilDate = :utilDate").
            setParameter("utilDate", cal, TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testCalendar() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.calendar = :calendar").
            setParameter("calendar", cal, TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be one result", result.size() == 1);
    }

    public void testTimestampGreaterThan() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp > :timestamp").
            setParameter("timestamp", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be three result", result.size() == 3);
    }

    public void testTimestampLessThan() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(2001, 6, 1, 3, 45, 32);
        cal.set(Calendar.MILLISECOND, 87);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp < :timestamp").
            setParameter("timestamp", cal.getTime(), TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be three result", result.size() == 2);
    }

//IN node is going to be fixed and then this test will run
    public void testTimestampIn() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        GregorianCalendar cal2 = new GregorianCalendar(); 
        cal2.set(2001, 6, 1, 3, 45, 32);
        cal2.set(Calendar.MILLISECOND, 87);

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp IN (:timestamp1, :timestamp2)").
            setParameter("timestamp1", cal.getTime(), TemporalType.TIMESTAMP).
            setParameter("timestamp2", cal2, TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be two result", result.size() == 2);
    }

    public void testTimestampBetween() {
        GregorianCalendar cal = new GregorianCalendar(); 
        cal.set(1901, 11, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);

        Calendar cal2 = Calendar.getInstance(); 

        List result = createEntityManager().createQuery("SELECT OBJECT(o) FROM DateTime o WHERE o.timestamp BETWEEN :timestamp1 AND :timestamp2").
            setParameter("timestamp1", cal.getTime(), TemporalType.TIMESTAMP).
            setParameter("timestamp2", cal2, TemporalType.TIMESTAMP).
            getResultList();
        
        assertTrue("There should be four result", result.size() == 4);
    }

    public static void main(String[] args) {
        // Now run JUnit.
        junit.swingui.TestRunner.main(args);
    }
}
