/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.dcode;

import javax.ejb.Stateful;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Init;
import javax.ejb.RemoteHome;

@RemoteHome(SfulHome.class)
@Stateful
public class SfulEJB 
{

    // reference to a Remote Business object from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.Sless sless;

    // reference to a RemoteHome from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemoteHome slessHome;

    com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote slessRemote;
    
    @Init
    public void create() {
        System.out.println("In SfulEJB::create");
        try {
            slessRemote = slessHome.create();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        System.out.println("Calling sless");

        sless.hello();

        try {        

            slessRemote.hello();

            com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote 
                anotherRemote = slessHome.create();
        
            anotherRemote.hello();

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
            
        System.out.println("Called sless.hello()");

        return "hello";
    }

}
