package com.sun.s1asdev.jdbc.txisolation.ejb;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;


public class SimpleBMPBean
        implements SessionBean {


    DataSource ds;
    public void setSessionContext(SessionContext sessionContext) {
        initializeDataSource();
    }

    private DataSource getDataSource() {
        if(ds !=null){
            return ds;
        }else{
            initializeDataSource();
            return ds;
        }
    }

    private void initializeDataSource(){
      Context context = null;
        try {
            context = new InitialContext();
              ds = (DataSource) context.lookup("java:comp/env/DataSource");
        } catch (NamingException e) {
            throw new EJBException("cant find datasource");
        }
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public boolean test1(int isolationLevel){
        boolean result = false;
    Connection con = null;
        try{
            con = getDataSource().getConnection();
            if(isolationLevel == con.getTransactionIsolation()){
               result = true;
            }
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }finally{
            if(con != null){
                try{
                    con.close();
                }catch(Exception e){e.printStackTrace();}
            }
        }
    return result;        
    }

    public boolean modifyIsolation(int isolationLevel){
        boolean result = false;
    Connection con = null;
        try{
            con = getDataSource().getConnection();
            con.setTransactionIsolation(isolationLevel);
            result = true;
        }catch(SQLException sqe){
            sqe.printStackTrace();
        }finally{
            if(con != null){
                try{
                    con.close();
                }catch(Exception e){e.printStackTrace();}
            }
        }
    return result;
    }

     public void ejbLoad() {}
    public void ejbStore() {}
    public void ejbRemove() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}
    public void unsetEntityContext() {}
    public void ejbPostCreate() {}
}
