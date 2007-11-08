package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.HashMap;
import javax.ejb.EntityContext;
import java.util.logging.*;
import com.sun.ejte.ccl.reporter.*;

public abstract class AccountBean extends EnterpriseBeanLogger implements javax.ejb.EntityBean
{
    
    private static Logger logger = Logger.getLogger("bank.admin");
    private static ConsoleHandler ch = new ConsoleHandler();
    
    public abstract String getAccountID();
    public abstract void setAccountID(String id);
    
    public abstract Double getAmount();
    public abstract void setAmount(Double amt);
    
    public abstract HashMap getPrivileges();
    public abstract void setPrivileges(HashMap privileges);
    

        //Business methods

    public String ejbCreateAccount(AccountDataObject ado)
    throws javax.ejb.CreateException
    {

        toXML("ejbCreateAccount","Enter");
        setAccountID(ado.getAccountID());
        setAmount(ado.getAmount());
        setPrivileges(ado.getPermissionList());
        toXML("ejbCreateAccount","Created Account: "+ ado);
        toXML("ejbCreateAccount","Exit");

        return null;
    }


    public void ejbPostCreateAccount(AccountDataObject ado)
    throws javax.ejb.CreateException
    {}

    public AccountDataObject getDAO()
    {
        return new AccountDataObject(getAccountID(),getAmount(),getPrivileges());
    }

    public EntityContext ejbContext;

    public void ejbActivate() {
    }

    public void ejbPassivate() {}

    public void ejbLoad() {
        toXML("ejbLoad","CMP Account");
    }

    
    public void ejbStore() {
        toXML("ejbStore","CMP Account");
    }

    public void ejbRemove() {
        toXML("ejbRemove","CMP Account");
    }

    public void setEntityContext(EntityContext ctx) {
        ejbContext=ctx;
    }

    public void unsetEntityContext() {
        ejbContext=null;
    }

    public EntityContext getEJBContext() {
        return ejbContext;
    }


  }
