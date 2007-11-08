package com.sun.s1asdev.ejb30.eempassivation;

import javax.ejb.Stateless;
import javax.ejb.EJB;

import javax.naming.*;

@Stateless
@EJB(name = "ejb/SfulDelegate", beanInterface = com.sun.s1asdev.ejb30.eempassivation.SfulDelegate.class)
public class SlessBean implements Sless {

    public SfulDelegate createSfulDelegate() {
        SfulDelegate val = null;

        try {
            Context initCtx = new InitialContext();
            val = (SfulDelegate) initCtx
                    .lookup("java:comp/env/ejb/SfulDelegate");
        } catch (Exception ex) {
        }

        return val;
    }
}