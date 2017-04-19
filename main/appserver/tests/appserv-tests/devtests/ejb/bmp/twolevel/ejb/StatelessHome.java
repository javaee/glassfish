package com.sun.s1asdev.ejb.bmp.twolevel.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface StatelessHome
    extends EJBHome
{
    public Stateless create()
        throws RemoteException, CreateException;

}
