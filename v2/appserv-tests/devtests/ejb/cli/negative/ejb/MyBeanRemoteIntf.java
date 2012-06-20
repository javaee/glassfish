package com.acme;

import javax.ejb.*;

@Remote
public interface MyBeanRemoteIntf {
    public int getCount(int i);
} 
