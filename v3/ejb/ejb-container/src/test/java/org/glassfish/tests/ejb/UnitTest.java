/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package org.glassfish.tests.ejb;

import org.junit.Test;
import org.glassfish.tests.ejb.sample.SimpleEjb;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

public class UnitTest {

    @Test
    public void test() {

        // Calculate test-classes location
        String cname = "org/glassfish/tests/ejb/UnitTest.class";
        URL source = UnitTest.class.getClassLoader().getResource(cname);
        String dir = source.getPath().substring(0, source.getPath().length()-cname.length());
        String[] list = (new File(dir + "/META-INF")).list();
        System.out.println("Files in dir/META-INF: " + java.util.Arrays.toString(list));

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, new File(dir));
        String gf = System.getenv().get("GF_HOME");
        if (gf != null) {
            System.err.println("+++GF location: " + gf);
            p.put("glassfish.ejb.embedded.glassfish.installation", gf);
        } else {
            System.err.println("+++GF location NOT specified via GF_HOME");
        }

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/test-classes/SimpleEjb");
            if (ejb!=null) {
                System.out.println("Invoking EJB...");
                System.out.println("EJB said: " + ejb.saySomething());
                System.out.println("JPA call returned: " + ejb.testJPA());
            }
        } catch (Exception e) {
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        try {
            System.out.println("Creating another container without closing...");
            EJBContainer c0 = EJBContainer.createEJBContainer();
            if (c0 != null)
                throw new RuntimeException("Created another container without closing the current...");
        } catch (EJBException e) { 
            System.out.println("Caught expected: " + e.getMessage());
        }

        c.close();
        System.out.println("Creating container after closing the previous...");
        c = EJBContainer.createEJBContainer(p);
        c.close();

        System.out.println("..........FINISHED UnitTest");
    }

    public static void main(String[] s) {
        UnitTest t = new UnitTest();
        t.test();
    }
}
