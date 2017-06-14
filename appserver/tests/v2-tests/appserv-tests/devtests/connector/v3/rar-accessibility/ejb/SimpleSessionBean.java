package com.sun.s1asdev.connector.rar_accessibility_test.ejb;


import javax.ejb.EJBContext;
import javax.ejb.SessionBean;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleSessionBean implements SessionBean {

    private EJBContext ejbcontext;
    private transient javax.ejb.SessionContext m_ctx = null;
    transient javax.sql.DataSource ds;


    public void setSessionContext(javax.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public boolean test1(int expectedCount) {

        System.out.println("Excpected count : " + expectedCount);
        String [] resources = new String[] {"blackbox-tx-cr","blackbox-notx-cr","blackbox-xa-cr"};

        int count = 0;
        for(String res : resources){
            try{
                InitialContext ctx = new InitialContext();
                Object o = ctx.lookup(res);
                System.out.println("CLASS_NAME: "+o.getClass().getName());
                System.out.println("CLASS_LOADER: " + 
                        Thread.currentThread().getContextClassLoader().loadClass(o.getClass().getName()).getClassLoader());
                count++;
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        System.out.println("Actual count : " + count);
        return (expectedCount == count);
    }
}



