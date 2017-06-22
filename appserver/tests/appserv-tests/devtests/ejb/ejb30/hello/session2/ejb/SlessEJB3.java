/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.annotation.PostConstruct;
import javax.ejb.SessionContext;
import javax.annotation.Resource;

import java.util.Collection;
import java.util.Iterator;

@Stateless
@Remote({SlessSub.class})
public class SlessEJB3 implements SlessSub
{

    private @EJB Sful sful1;
    private @EJB Sful sful2;

    private @Resource EJBContext ejbContext;

    private @Resource SessionContext ejbContext2;

    @Resource(type=javax.ejb.SessionContext.class) 
    private EJBContext ejbContext3;

    private EJBContext ejbContext4;
    @Resource
    private void setEJBContext4(EJBContext context) {
        ejbContext4 = context;
    }

    private SessionContext ejbContext5;
    @Resource
    private void setEJBContext5(SessionContext context) {
        ejbContext5 = context;

        try {
            context.getTimerService();
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "context.getTimerService() in " +
                               "setContext method");
        }

        try {
            context.getBusinessObject(SlessSub.class);
            throw new RuntimeException("Should have gotten IllegalStateEx");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when accessing " +
                               "context.getBusinessObject() in " +
                               "setContext method");
        }

    }

    private EJBContext ejbContext6;
    @Resource(type=javax.ejb.SessionContext.class)
        private void setEJBContext6(EJBContext context) {
        ejbContext6 = context;
    }
    
    @PostConstruct
    public void afterCreate() {
        System.out.println("In SlessEJB3::afterCreate() marked as PostConstruct");

        // Access to getBusinessObject is allowed here
        ((SessionContext) ejbContext).
            getBusinessObject(SlessSub.class);

    }

    public String hello() {
        System.out.println("In SlessEJB3:hello()");

        System.out.println("Calling myself through my remote business object");
        SlessSub me = ((SessionContext) ejbContext).
            getBusinessObject(SlessSub.class);
        String whoami = me.getId();
        System.out.println("i am " + whoami);

        return "hello from sless ejb3";
    }

    public String hello2() throws javax.ejb.CreateException {
        throw new javax.ejb.CreateException();
    }

    public String hello3() {

        System.out.println("in hello3()");

        System.out.println("sful1 = " + sful1);
        System.out.println("sful2 = " + sful2);

        sful1.set("1");
        sful2.set("2");
        String get1 = sful1.get();
        String get2 = sful2.get();
        if( get1.equals(get2) ) {
            System.out.println("get1 =" + get1);
            System.out.println("get2 =" +  get2);
            throw new javax.ejb.EJBException("SFSB get test failed");
        }

        return "hello3()";
    }

    public String getId() {
        return "SlessEJB3";
    }

    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB3::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB3::roundTrip2 " + 
                           collectionOfSless);
        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());  
        }
        return collectionOfSless;
    }    

}
