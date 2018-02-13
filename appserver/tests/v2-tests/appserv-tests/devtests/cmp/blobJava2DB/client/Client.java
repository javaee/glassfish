/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 *
 * @author  mvatkina
 * @version
 */
public class Client {
    private static test.BlobTestHome bhome = null;
    
    private static SimpleReporterAdapter stat =
	new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        boolean isJava2DBTest = args.length > 0;
        
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/RemoteBlobTest");
            bhome =
                (test.BlobTestHome) PortableRemoteObject.narrow(
                        objref, test.BlobTestHome.class);

            System.out.println("START");
	    stat.addDescription("Blob");

            test(100, "FOO", new byte[]{'A', 'B', 'C'});
            test(200, "BAR", new byte[]{'M', 'N', 'O'});
            test(300, null, new byte[]{'X', 'Y', 'Z'});

            if (!isJava2DBTest) {
                System.out.println("Testing old...");
                test.BlobTest bean = bhome.findByPrimaryKey(new Integer(1));
                System.out.println(new String(bean.getBlb()));
                System.out.println(new String(bean.getByteblb()));
                System.out.println(new String(bean.getByteblb2()));
            } else {
                try {
                    test(40, "BAZ", null);
		    stat.addStatus("ejbclient Blob", stat.FAIL);
                    throw new Exception(
                            "Failed to catch expected exception for insert of null blob value");
                } catch (Exception ex) {
                    System.out.println(
                            "Caught expected exception when inserting null blob value");
		    //stat.addStatus("ejbclient Blob", stat.PASS);
                }
            }

	    stat.addStatus("ejbclient Blob", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient Blob", stat.FAIL);
        }
	stat.printSummary("Blob");
    }

    static void test(int id, String name, byte chars[])
            throws Exception {
        
        Integer pk = new Integer(id);
        test.BlobTest bean = bhome.create(pk, name, chars);
        System.out.println("Created: " + bean.getPrimaryKey());

        System.out.println("Testing new...");
        bean = bhome.findByPrimaryKey(pk);

        byte[] blb = bean.getBlb();
        String blbString = blb == null ? "null" : new String(blb);

        byte[] byteBlb = bean.getByteblb();
        String byteBlbString = byteBlb == null ? "null" : new String(byteBlb);

        byte[] byteBlb2 = bean.getByteblb2();
        String byteBlb2String = byteBlb2 == null ? "null" : new String(byteBlb2);

        System.out.println("blb=" + blbString);
        System.out.println("byteblb=" + byteBlbString);
        System.out.println("byteblb2=" + byteBlb2String);
    }
}
