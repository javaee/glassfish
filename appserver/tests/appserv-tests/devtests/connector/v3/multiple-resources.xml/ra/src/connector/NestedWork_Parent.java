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

import javax.resource.spi.work.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.ArrayList;

public class NestedWork_Parent extends DeliveryWork implements WorkContextProvider {
    //    private WorkContexts ics = null;
    private List<WorkContext> contextsList = new ArrayList<WorkContext>();
    private MessageEndpoint ep;
    private int numOfMessages;
    private int workCount = 1;
    private String op = null;
    private WorkManager wm;
    private XATerminator xa;
    private boolean transactedChild;
    private boolean transacted;
    private boolean successfulPVForChild;
    private boolean translationRequired;

    public NestedWork_Parent(MessageEndpoint ep, int numOfMessages, String op, int workCount,
                             WorkManager wm, XATerminator xa, boolean transacted, boolean transactedChild,
                             boolean successfulPVForChild, boolean translationRequired) {
        super(ep, numOfMessages, op);
        this.workCount = workCount;
        this.ep = ep;
        this.numOfMessages = numOfMessages;
        this.op = op;
        this.wm = wm;
        this.xa = xa;
        this.transacted =  transacted;
        this.transactedChild = transactedChild;
        this.successfulPVForChild = successfulPVForChild;
        this.translationRequired =  translationRequired;
    }

    public NestedWork_Parent(MessageEndpoint ep, int numOfMessages, String op, boolean keepCount,
                             int workCount, WorkManager wm, XATerminator xa, boolean transacted, boolean transactedChild,
                             boolean successfulPVForChild, boolean translationRequired) {
        super(ep, numOfMessages, op, keepCount);
        this.workCount = workCount;
        this.ep = ep;
        this.numOfMessages = numOfMessages;
        this.op = op;
        this.wm = wm;
        this.xa = xa;
        this.transacted =  transacted;
        this.transactedChild = transactedChild;
        this.successfulPVForChild = successfulPVForChild;
        this.translationRequired = translationRequired;
    }

    public List<WorkContext> getWorkContexts() {
        return contextsList;
    }


    public void addWorkContext(WorkContext ic) {
        contextsList.add(ic);
    }

    public void run() {
        for (int i = 0; i < workCount; i++) {

            ExecutionContext ec1 = null;
            try {
                if(transacted){
                    super.run();
                }

                NestedWork_Child w1 = new NestedWork_Child(ep, numOfMessages, op, transactedChild, translationRequired);

                if (transactedChild) {
                    ec1 = startTx();
                    TransactionContext tic = new TransactionContext();
                    tic.setXid(ec1.getXid());
                    w1.addWorkContext(tic);
                }

                if(successfulPVForChild){
                    MySecurityContext sic =
                        new MySecurityContextWithListener("prasath", "jagadish", "jagadish",  translationRequired,  true, true);
                    w1.addWorkContext(sic);
                }else{
                    MySecurityContext sic =
                            new MySecurityContextWithListener("abc", "xyz", "jagadish",  translationRequired,  true, false);
                    w1.addWorkContext(sic);
                }

                debug("executing nested work parent instance [ " + i + " ] ");
                wm.doWork(w1, 0, null, null);
                if (transactedChild) {
                    xa.commit(ec1.getXid(), true);
                    debug("commiting nested work parent instance [ " + i + " ] ");
                }
                debug("completed nested work parent instance [ " + i + " ] ");

            } catch (Exception we) {
                debug(we.toString());
            }
        }
    }

    private ExecutionContext startTx() {
        ExecutionContext ec = new ExecutionContext();
        try {
            Xid xid = new XID();
            ec.setXid(xid);
            ec.setTransactionTimeout(50 * 1000); //50 seconds
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ec;
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [Nested Work - Parent]: " + message);
    }

}
