/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package test.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestBeanInterface;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NoInterfaceProxySerializableEJBTestServlet extends HttpServlet {

    @Inject
    TestBeanInterface tbi;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // set state
        tbi.setState("TEST");

        // Test serializability of EJB in TestBean.
        try {
            File tmpFile = File.createTempFile("SerializableProxyTest", null);
            FileOutputStream fout = new FileOutputStream(tmpFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            System.out.println("Writing " + tbi + " to file-" + tmpFile);
            oos.writeObject(tbi);
            oos.close();

            FileInputStream fin = new FileInputStream(tmpFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            System.out.println("Attempting to read " + tbi + " from file-"
                    + tmpFile);

            TestBeanInterface tb = (TestBeanInterface) ois.readObject();
            // check if we have access to the same stateful session bean
            System.out.println(tb.getState().equals("TEST"));
            if (!tb.getState().equals("TEST"))
                msg += "Failed to serialize stateful bean";
            //check if we can invoke stateless EJB
            tb.method1();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
            msg += "Failed to serialize/deserialize proxy to EJB";
        }

        writer.write(msg + "\n");
    }
}
