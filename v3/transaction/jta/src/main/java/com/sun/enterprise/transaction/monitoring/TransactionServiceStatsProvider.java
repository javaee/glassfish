/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.transaction.monitoring;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Collects the Transaction Service monitoring data and provides it to the callers.
 *
 * @author Marina Vatkina
 */
//@AMXMetadata(type="transaction-service-mon", group="monitoring", isSingleton=true)
@ManagedObject
@Description("Transaction Service Statistics")
public class TransactionServiceStatsProvider {

    private CountStatisticImpl activeCount = new CountStatisticImpl("ActiveCount", "count", 
            "Provides the number of transactions that are currently active.");

    private CountStatisticImpl committedCount = new CountStatisticImpl("CommittedCount", "count", 
            "Provides the number of transactions that have been committed.");

    private CountStatisticImpl rolledbackCount = new CountStatisticImpl("RolledbackCount", "count", 
            "Provides the number of transactions that have been rolled back.");

    private boolean isFrozen = false;

    @ManagedAttribute(id="activecount")
    @Description( "Provides the number of transactions that are currently active." )
    public CountStatistic getActiveCount() {
        return activeCount.getStatistic();
    }

    @ManagedAttribute(id="committedcount")
    @Description( "Provides the number of transactions that have been committed." )
    public CountStatistic getCommittedCount() {
        return committedCount.getStatistic();
    }

    @ManagedAttribute(id="rolledbackcount")
    @Description( "Provides the number of transactions that have been rolled back." )
    public CountStatistic getRolledbackCount() {
        return rolledbackCount.getStatistic();
    }
    
    @ManagedAttribute(id="state")
    @Description( "Indicates if the transaction service has been frozen." )
    public String getState() {
        return (isFrozen)? "True": "False";
    }
    
    @ProbeListener("glassfish:transaction:transaction-service:activated")
    public void transactionActivatedEvent() {
        activeCount.increment();
    }

    @ProbeListener("glassfish:transaction:transaction-service:deactivated")
    public void transactionDeactivatedEvent() {
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:committed")
    public void transactionCommittedEvent() {
        committedCount.increment();
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:rolledback")
    public void transactionRolledbackEvent() {
        rolledbackCount.increment();
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:freeze")
    public void freezeEvent(@ProbeParam("isFrozen") boolean b) {
        isFrozen = b;
    }
}
