package com.sun.s1asdev.jdbc.contauth.ejb;

import javax.ejb.EJBLocalObject;
import java.rmi.*;

public interface SimpleBMP
    extends EJBLocalObject {
    public boolean test1();
    public boolean test2();
}
