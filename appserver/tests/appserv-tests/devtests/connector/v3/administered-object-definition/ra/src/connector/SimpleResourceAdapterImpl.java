/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.*;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.TransactionSupport.TransactionSupportLevel;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.*;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * This is a sample resource adapter
 * 
 * @author Qingqing Ouyang
 */
@Connector(
/* moduleName="My Simple RAR", */
    displayName = "Simple Resource Adapter", 
    vendorName = "Java Software", 
    eisType = "Generic Type", 
    version = "1.0Alpha",
    transactionSupport=TransactionSupportLevel.XATransaction)

public class SimpleResourceAdapterImpl extends AbstractResourceAdapter
        implements ResourceAdapter, java.io.Serializable {

    private static final long serialVersionUID = -8470883868587583959L;
    private BootstrapContext ctx;
    private WorkManager wm;

    private Work work;

    public SimpleResourceAdapterImpl() {
        debug("constructor...");
    }

    public void start(BootstrapContext ctx)  throws ResourceAdapterInternalException {

        debug("001. Simple RA start...");

        this.ctx = ctx;
        debug("002. Simple RA start...");
        this.wm = ctx.getWorkManager();
        debug("003. Simple RA start...");

        // testing creat timer
        Timer timer = null;
        try {
            timer = ctx.createTimer();
        } catch (UnavailableException ue) {
            System.out.println("Error");
            throw new ResourceAdapterInternalException("Error form bootstrap");
        }
        debug("004. Simple RA start...");
    }

    public void stop() {
        debug("999. Simple RA stop...");
        if (work != null) {
            ((WorkDispatcher) work).stop();

            synchronized (Controls.readyLock) {
                Controls.readyLock.notify();
            }

        }
    }

    public void endpointActivation(MessageEndpointFactory factory,
            ActivationSpec spec) throws NotSupportedException {
        try {
            debug("B.000. Create and schedule Dispatcher");
            spec.validate();
            work = new WorkDispatcher("DISPATCHER", ctx, factory, spec);
            wm.scheduleWork(work, 4 * 1000, null, null);
            debug("B.001. Scheduled Dispatcher");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void endpointDeactivation(MessageEndpointFactory endpointFactory,
            ActivationSpec spec) {
        debug("endpointDeactivation called...");

        ((WorkDispatcher) work).stop();
    }

    public XAResource[] getXAResources(ActivationSpec[] specs)
            throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
