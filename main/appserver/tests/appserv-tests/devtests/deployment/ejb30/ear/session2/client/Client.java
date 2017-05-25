/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session2.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBs;
import javax.ejb.NoSuchEJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.naming.InitialContext;
import javax.naming.Context;
import com.sun.s1asdev.ejb.ejb30.hello.session2.*;


@EJBs( 
 { @EJB(name="ejb/TypeLevelSless1", beanName="SlessEJB", 
       beanInterface=Sless.class),
   @EJB(name="ejb/TypeLevelSless2", beanName="SlessEJB2", 
       beanInterface=Sless.class)
 })
@EJB(name="ejb/TypeLevelSless3", beanInterface=SlessSub.class)
public class Client extends ClientSuper {

    public static void main (String[] args) {

        Client client = new Client(args);
        client.doTest();
    }  

    private static @EJB Sful sful1;
    private static @EJB Sful sful2;
    private static @EJB Sful sful3;
    private static @EJB Sful sful4;
    private static @EJB Sful sful5;

    private static @EJB(beanName="SlessEJB") Sless sless1;

    // linked to SlessEJB2 via sun-application-client.xml 
    // so no beanName disambiguation is needed
    private static @EJB Sless sless2;

    // Only one target bean with Remote intf SlessSub so no linking info
    // necessary
    private static @EJB SlessSub sless3;

    public Client (String[] args) {
    }
    
    public void doTest() {

        try {
            
            System.out.println("Calling superSless1");

            Sless superSlessLookup = (Sless) new javax.naming.InitialContext().lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session2.client.ClientSuper/superSless1");
            superSlessLookup.hello();
            System.out.println("Called superSless1");

            sful1.hello();

            sful2.hello();

            sful1.set("1");
            sful2.set("2");

            String get1 = sful1.get();
            String get2 = sful2.get();
            System.out.println("get1 =" + get1);
            System.out.println("get2 =" + get2);

            if( get1.equals(get2) ) {
                throw new Exception("SFSB get test failed");
            }

            sful1.doRemoveMethodSessionSyncTests();
            
            // Call application-defined @Remove method. This method has
            // no relationship to EJBObject.remove().  It's a coincidence
            // that it has the same name.
            sful1.remove();
            boolean passed = true;
            try {
                sful1.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            // Force @Remove method to throw an exception, but bean should
            // not be removed because retainIfException=true
            passed = true;
            try {
                sful2.removeRetainIfException(true);
                passed = false;
            } catch(Exception e) {
                System.out.println("Successfully got application exception " +
                                   " from remote SFSB @Remove method");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }


            // Now call it again but don't have it throw an exception.  In
            // this case, the bean should be removed.
            sful2.removeRetainIfException(false);

            passed = true;
            try {
                sful2.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }

            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }


            // Call an @Remove method for which retainIfException=false and
            // make it throw an exception.  The bean should still be removed.
            passed = true;
            try {
                sful3.removeNotRetainIfException(true);
                passed = false;
            } catch(Exception e) {
                System.out.println("Got expected exception from @Remove mthd");
            }

            if( !passed ) {
                throw new Exception("Didn't get expected sfsb remove exception");
            }

            passed = true;
            try {
                sful3.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            // Call an @Remove method for which retainIfException=false and
            // don't have it throw an exception.  The bean should be removed.
            passed = true;
            sful4.removeNotRetainIfException(false);
            try {
                sful4.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            // Call an @Remove method for which retainIfException=true and
            // have it throw a system exception.  retainIfException only
            // applies to Application exceptions, so bean should still be
            // removed.
            passed = true;
            sful5.hello();
            try {
                sful5.removeMethodThrowSysException(true);
            } catch(EJBException e) {
                System.out.println("got expected EJBException from " +
                                   "removeMethodThrowSysException()");
            }
            try {
                sful5.hello();
                passed = false;
            } catch(NoSuchEJBException e) {
                System.out.println("Successfully got exception after " +
                                   "attempting to access implicitly removed" +
                                   " sfsb");
            }
            if( !passed ) {
                throw new Exception("Didn't get expected sfsb access exception");
            }

            sless1.hello();
            Sless r1 = sless1.roundTrip(sless1);
            sless1.roundTrip(sless2);

            sless2.hello();
            sless2.roundTrip(sless1);
            Sless r2 = sless2.roundTrip(sless2);

            try {
                sless1.hello2();
                throw new Exception("Did not receive CreateException");
            } catch(javax.ejb.CreateException ce) {
                System.out.println("Successfully caught app exception");
            }

            if( sless1.getId().equals(sless2.getId()) ) {
                throw new Exception("getId() test failed");
            }

            if( r1.getId().equals(r2.getId()) ) {
                throw new Exception("remote param passing getId() test failed");
            }
            
            sless3.hello();
            sless3.hello3();


            Context ic = new InitialContext();

            Sless typeLevelSless1 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless1");
            Sless typeLevelSless2 = (Sless)
                ic.lookup("java:comp/env/ejb/TypeLevelSless2");
            SlessSub typeLevelSless3 = (SlessSub)
                ic.lookup("java:comp/env/ejb/TypeLevelSless3");
            typeLevelSless1.hello();
            typeLevelSless2.hello();
            typeLevelSless3.hello();
            if( typeLevelSless1.getId().equals(typeLevelSless2.getId()) ) {
                throw new Exception("type-level@EJB getId() test failed");
            }
            System.out.println("Finished Type-level @EJB checks");

            System.out.println("test complete");

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    	return;
    }

}

