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

@Stateful(mappedName="com.sun.s1asdev.ejb.ejb30.clientview.adapted.DummyEJB")
@Remote({DummyRemote.class, DummyRemote2.class})
public class DummyEJB
    implements DummyRemote, DummyRemote2  {

    public void dummy() {
    }

    public void dummy2() {
    }

}
