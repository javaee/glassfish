package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import java.util.Date;
import javax.ejb.FinderException;

public interface CustomerLocalHome extends javax.ejb.EJBLocalHome
{
	public CustomerLocal createCustomer (String id,String name)
	throws javax.ejb.CreateException;
        public CustomerLocal findByPrimaryKey(String id) throws javax.ejb.FinderException;

}
