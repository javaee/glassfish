package com.sun.s1asdev.jdbc.statementtimeout.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMP
        extends EJBLocalObject {
    public boolean statementTest();

    public boolean preparedStatementTest();

    public boolean callableStatementTest();
}
