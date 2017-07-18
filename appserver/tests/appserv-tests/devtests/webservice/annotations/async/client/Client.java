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

package client;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import endpoint.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef
        static AddNumbersImplService service;

        public static void main(String[] args) {
	    stat.addDescription("async-service");
            Client client = new Client();
            client.doSyncTest();
            client.doASyncPollTest();
            client.doASyncCallBackTest();
	    stat.printSummary("async-service");
        }

        public void doSyncTest() {
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                int ret = port.addNumbers(2222, 1234);
		if(ret!=(2222+1234)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-sync-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-sync-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-sync-test", stat.FAIL);
            }
        }

        private void doASyncPollTest () {
            System.out.println ("Invoking Asynchronous Polling addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                Response<AddNumbersResponse> resp = port.addNumbersAsync(1234, 5678);
                Thread.sleep (2000);
                AddNumbersResponse output = resp.get();
                int ret = output.getReturn();
		if(ret!=(1234+5678)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-poll-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-poll-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-poll-test", stat.FAIL);
            }
        }

        public void doASyncCallBackTest () {
            System.out.println ("Invoking Asynchronous Callback addNumbers");
            try {
                AddNumbersImpl port = service.getAddNumbersImplPort();
                AddNumbersCallbackHandler cbh = new AddNumbersCallbackHandler();
                Future<?> response = port.addNumbersAsync(9876, 5432, cbh);
                Thread.sleep (2000);
                AddNumbersResponse output = cbh.getResponse ();
                int ret = output.getReturn();
		if(ret!=(9876+5432)) {
                    System.out.println("Unexpected add result " + ret);
                    stat.addStatus("async-service-callbackhandler-test", stat.FAIL);
                    return;
		}
                System.out.println(ret);
                stat.addStatus("async-service-callbackhandler-test", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("async-service-callbackhandler-test", stat.FAIL);
            }
        }

        // The actual call back handler
        private class AddNumbersCallbackHandler implements AsyncHandler<AddNumbersResponse> {
            private AddNumbersResponse output;
            public void handleResponse (Response<AddNumbersResponse> response) {
                try {
                    output = response.get ();
                } catch (ExecutionException e) {
                    e.printStackTrace ();
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }

            AddNumbersResponse getResponse (){
                return output;
            }
        }
}

