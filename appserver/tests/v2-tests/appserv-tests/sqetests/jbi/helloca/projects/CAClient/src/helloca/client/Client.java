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
 * Main.java
 *
 * Created on January 13, 2007, 7:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package helloca.client;

import caclient.ejbws.Account;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Iterator;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPFaultException;
/**
 *
 * @author Sony Manuel
 */
public class Client {
    
    static SimpleReporterAdapter reporter = new SimpleReporterAdapter();
    /** Creates a new instance of Main */
    public Client() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        reporter.addDescription("Hello Composite App Test");
        // TODO code application logic here
        Client m = new Client();
        m.testPing();
        m.testInOut();
        //m.testFault();
            reporter.printSummary("HelloCa Test");
    }
    
    public void testPing() {
        String testId = "jbi.helloca.testapp.testPing";
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.ping();
            reporter.addStatus(testId, reporter.PASS);
        } catch (Exception ex) {
            reporter.addStatus(testId, reporter.FAIL);
            ex.printStackTrace();
        }
    }
    
    public void testInOut() {
        String testId = "jbi.helloca.testapp.testInOut";
        Account acc = new Account();
        Holder<Account> holder = new Holder<Account>();
        holder.value = acc;
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.createAccount("Foo", 100.00, holder);
            if (holder.value.getId() == 1000 &&
                    holder.value.getName().equals("Foo") &&
                    holder.value.getBalance() == 100.00)
                reporter.addStatus(testId, reporter.PASS);
            else
                reporter.addStatus(testId, reporter.FAIL);
        } catch (Exception ex) {
            reporter.addStatus(testId, reporter.FAIL);
            ex.printStackTrace();
        }
    }
    
    
    public void testFault() {
        String testId = "jbi.helloca.testapp.testFault";
        Account acc = new Account();
        Holder<Account> holder = new Holder<Account>();
        holder.value = acc;
        try { // Call Web Service Operation
            caclient.ejbws.MiscService service = new caclient.ejbws.MiscService();
            caclient.ejbws.Misc port = service.getMiscPort();
            port.createAccount("Bar", 0.0, holder);
            reporter.addStatus(testId, reporter.FAIL);
        } catch (SOAPFaultException sfe) {
            reporter.addStatus(testId, reporter.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            reporter.addStatus(testId, reporter.FAIL);
        }
    }
}
