package com.sun.s1asdev.jdbc.cpdsperf.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleBMPHome
    extends EJBHome
{
    SimpleBMP create( int numTimes )
        throws RemoteException, CreateException;

}
