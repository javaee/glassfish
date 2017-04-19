package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.extendedscope_cross_sfsb.ejb;

import javax.ejb.EJBLocalObject;

public interface TC2Finder extends EJBLocalObject{
    Person findPerson(String name);
}
