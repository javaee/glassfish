package com.sun.s1asdev.jdbc.resenabledisable.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface SimpleBMPHome
        extends EJBLocalHome {
    SimpleBMP create()
            throws CreateException;

}
