/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.libpam;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InteractiveTester extends TestCase {
    public InteractiveTester(String testName) {
        super(testName);
    }

    public void testPositiveCase() throws Exception {
        for (int i=0; i<1000; i++)
            testOne();
    }

    public void testOne() throws Exception {
        UnixUser u = new PAM("sshd").authenticate(System.getProperty("user.name"), System.getProperty("password"));
        if(!printOnce) {
            System.out.println(u.getUID());
            System.out.println(u.getGroups());
            printOnce = true;
        }
    }

    public void testGetGroups() throws Exception {
        System.out.println(new PAM("sshd").getGroupsOfUser(System.getProperty("user.name")));
    }

    public void testConcurrent() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(10);
        Set<Future<?>> result = new HashSet<Future<?>>();
        for( int i=0; i<1000; i++ ) {
            result.add(es.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    testOne();
                    return null;
                }
            }));
        }
        // wait for completion
        for (Future<?> f : result) {
            f.get();
        }
        es.shutdown();
    }

    public void testNegative() throws Exception {
        try {
            new PAM("sshd").authenticate("bogus","bogus");
            fail("expected a failure");
        } catch (PAMException e) {
            // yep
        }
    }

    public static void main(String[] args) throws Exception {
        UnixUser u = new PAM("sshd").authenticate(args[0], args[1]);
        System.out.println(u.getUID());
        System.out.println(u.getGroups());
        System.out.println(u.getGecos());
        System.out.println(u.getDir());
        System.out.println(u.getShell());
    }

    private boolean printOnce;
}
