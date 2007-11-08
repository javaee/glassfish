package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import java.util.Date;
import javax.ejb.FinderException;

public interface AccountLocalHome extends javax.ejb.EJBLocalHome
{
	public AccountLocal createAccount (AccountDataObject dao)
	throws javax.ejb.CreateException;
        public AccountLocal findByPrimaryKey(String id) throws javax.ejb.FinderException;
}
