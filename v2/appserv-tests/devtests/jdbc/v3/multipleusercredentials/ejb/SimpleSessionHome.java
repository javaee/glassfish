package com.sun.s1asdev.jdbc.multipleusercredentials.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleSessionHome extends EJBLocalHome {
    SimpleSession create()
            throws CreateException;

}
