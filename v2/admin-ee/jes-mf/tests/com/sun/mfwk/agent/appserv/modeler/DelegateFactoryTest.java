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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.modeler;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.logging.Logger;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import com.sun.mfwk.agent.appserv.delegate.DefaultDelegate;

/**
 * Unit test class for DelegateFactory.
 */
public class DelegateFactoryTest extends TestCase {

    private Element _mbean = null;

    public DelegateFactoryTest(String name) {
        super(name);
    }

    protected void setUp() {
        try {
            System.out.println("----setUp----");
            NodeList list = ConfigReader.getMBeans("server-template.xml", null);
            if (list != null) {
                int size = list.getLength();
                System.out.println("Number of Mbeans: " + size);

                _mbean = (Element) list.item(0);

                if (_mbean == null) {
                    fail("MBean is NULL");
                } else {
                    String on = ConfigReader.getMBeanObjectName(_mbean, false);
                    System.out.println("Object Name: " + on);

                    String pon = ConfigReader.getMBeanObjectName(_mbean, true);
                    System.out.println("Proxy Object Name: " + pon);
                }

            } else {
                System.out.println("Number of Mbeans is zero");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    protected void tearDown() {
    }

    public void testCreate() {        

        try {
            System.out.println("\n---- Running Test: testCreate ----");

            // setting up context
            Logger l = Logger.getLogger("com.sun.cmm.as");
            ModelerContext ctx = new ModelerContext();
            ctx.setLogger(l);
            ctx.addToken("server.name", "server");

            // needs connection to mbean server
            DelegateFactory df = new DelegateFactory(_mbean, null, ctx, null);
            DefaultDelegate delegate = (DefaultDelegate) df.create();
            if (delegate == null) {
                System.out.println("Delegate is NULL");
            } else {
                System.out.println("Delegate is not NULL");
            }

            Map attr = delegate.getAttributeMappings();
            assertTrue(attr.size() != 0);

            Set keys = attr.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object obj = attr.get(key);
                System.out.println("Key: " + key + " " + obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }        
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(DelegateFactoryTest.class);
    }
}
