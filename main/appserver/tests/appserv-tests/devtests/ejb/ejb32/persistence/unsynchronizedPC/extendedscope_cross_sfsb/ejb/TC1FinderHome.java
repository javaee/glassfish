package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface TC1FinderHome extends EJBLocalHome{
    public TC1Finder createFinder() throws CreateException;
}
