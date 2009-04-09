package com.sun.s1asdev.jdbc.maxconnectionusage.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBLocalObject {
    public boolean test1(boolean useXA);

    public String test2(boolean useXA, int value);

    public String test3(int count, boolean useXA, int value);
}
