package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

@Stateless
@EJB(name="ejb/SfulBean", 
beanInterface=com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.use_nonjtadatasource.ejb.SfulBean.class)
@TransactionManagement(TransactionManagementType.BEAN)
public class SlessBean implements Tester{
    
    public Map<String, Boolean> doTest() {
        Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
        
        DataSource ds = null;
        Connection conn = null;
        PreparedStatement ps = null;
        UserTransaction utx = null;
        try {
            System.out.println("I am in client");

            utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            ds = (DataSource)(new javax.naming.InitialContext()).lookup("java:comp/DefaultDataSource");
            
            utx.begin();
            conn = ds.getConnection();
            ps = conn.prepareStatement("Update EJB32_PERSISTENCE_CONTEXT_PERSON set name = 'newName' where id = 1");
            
            String lookupName = "java:comp/env/ejb/SfulBean";
            
            InitialContext initCtx = new InitialContext();
            SfulBean sfulBean = (SfulBean) initCtx.lookup(lookupName);
            
            Person person = sfulBean.testUsingNonJTADataSource(resultMap);
            
            utx.rollback();
            utx = null;
            resultMap.put("testRollBackDoesNotClearUnsynchPC", sfulBean.testRollBackDoesNotClearUnsynchPC(person));

            
            System.out.println("DoTest method ends");
            return resultMap;
        } catch (Exception e) {
            if (utx != null) {
                try {
                    utx.rollback();
                } catch (Exception ex) {}
            }
            throw new EJBException(e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {}

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {}

        }
    }
}
