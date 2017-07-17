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

package connector;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;

public class V3WorkDispatcher extends WorkDispatcher {
    public V3WorkDispatcher(String id, BootstrapContext ctx, MessageEndpointFactory factory, ActivationSpec spec) {
        super(id, ctx, factory, spec);
    }

    public void run() {

        try {
            synchronized (Controls.readyLock) {
                debug("WAIT...");
                Controls.readyLock.wait();


                if (stop) {
                    return;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
            MessageEndpoint ep = factory.createEndpoint(new FakeXAResource());
            int numOfMessages = 1;
            debug("V3WorkDispatcher sleeping");
            //Thread.sleep(10000);
            debug("V3WorkDispatcher woke up");
            //importing transaction

            //write/commit
            ExecutionContext ec = startTx();
            debug("Start TX - " + ec.getXid());

            debug("V3WorkDispatcher about to submit work");
            DeliveryWork w =
                    new DeliveryWork(ep, numOfMessages, "WRITE");
            wm.doWork(w, 0, null, null);
                      xa.commit(ec.getXid(), true);

            debug("V3WorkDispatcher done work");
            debug("DONE WRITE TO DB");
            Controls.expectedResults = numOfMessages;
            notifyAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            debug("V3WorkDispatcher calling DONE()");
            done();
            debug("V3WorkDispatcher finished calling DONE()");
            
        }
    }
}
