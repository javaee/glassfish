package com.acme;

import javax.ejb.*;

@Stateless
public class MyBean implements MyBeanRemoteIntf {
    public int getCount(int i) {
        return 1 + i;
    }
} 
