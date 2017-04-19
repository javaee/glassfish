/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;
import javax.naming.*;
import java.util.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import javax.annotation.Resource;

@Stateless(mappedName="com.sun.s1asdev.ejb.ejb30.clientview.adapted.DummyEJBRemoteSless")
@Remote({DummySlessRemote.class, DummySlessRemote2.class})
public class DummyEJBRemoteSless
    implements DummySlessRemote, DummySlessRemote2  {

    public void dummy() {
    }

    public void dummy2() {
    }

}

