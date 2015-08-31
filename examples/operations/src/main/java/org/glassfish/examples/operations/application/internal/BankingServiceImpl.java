/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.glassfish.examples.operations.application.internal;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.examples.operations.application.BankingService;
import org.glassfish.examples.operations.application.DepositorService;
import org.glassfish.examples.operations.application.TransferService;
import org.glassfish.examples.operations.application.WithdrawalService;
import org.glassfish.examples.operations.scopes.DepositScope;
import org.glassfish.examples.operations.scopes.WithdrawalScope;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;

/**
 * Implementation of the Banking Service that uses Operations
 * to switch between banks
 * 
 * @author jwells
 *
 */
@Singleton
public class BankingServiceImpl implements BankingService {
    @Inject
    private OperationManager manager;
    
    @Inject
    private TransferService transferAgent;
    
    @Inject
    private DepositorService depositorAgent;
    
    @Inject
    private WithdrawalService withdrawerAgent;
    
    private final Map<String, OperationHandle<DepositScope>> depositors = new HashMap<String, OperationHandle<DepositScope>>();
    private final Map<String, OperationHandle<WithdrawalScope>> withdrawers = new HashMap<String, OperationHandle<WithdrawalScope>>();
    
    private synchronized OperationHandle<DepositScope> getDepositBankHandle(String bank) {
        OperationHandle<DepositScope> depositor = depositors.get(bank);
        if (depositor == null) {
            // create and start it
            depositor = manager.createOperation(DepositScopeImpl.INSTANCE);
            depositors.put(bank, depositor);
        }
        
        return depositor;
    }
    
    private synchronized OperationHandle<WithdrawalScope> getWithdrawerBankHandle(String bank) {
        OperationHandle<WithdrawalScope> withdrawer = withdrawers.get(bank);
        if (withdrawer == null) {
            // create and start it
            withdrawer = manager.createOperation(WithdrawalScopeImpl.INSTANCE);
            withdrawers.put(bank, withdrawer);
        }
        
        return withdrawer;
    }

    /* (non-Javadoc)
     * @see org.glassfish.examples.operations.application.BankingService#transferFunds(java.lang.String, long, java.lang.String, long, int)
     */
    @Override
    public synchronized int transferFunds(String withdrawlBank, long withdrawlAccount,
            String depositorBank, long depositAccount, int funds) {
        OperationHandle<DepositScope> depositor = getDepositBankHandle(depositorBank);
        OperationHandle<WithdrawalScope> withdrawer = getWithdrawerBankHandle(withdrawlBank);
        
        // Set the context for the transfer
        depositor.resume();
        withdrawer.resume();
        
        // At this point the scopes are set properly, we can just call the service!
        try {
            return transferAgent.doTransfer(depositAccount, withdrawlAccount, funds);
        }
        finally {
            // Turn off the two scopes
            withdrawer.suspend();
            depositor.suspend();
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.examples.operations.application.BankingService#getDepositedBalance(java.lang.String, long)
     */
    @Override
    public int getDepositedBalance(String bank, long account) {
        OperationHandle<DepositScope> depositor = getDepositBankHandle(bank);
        
        // Set the context for the deposit balance check
        depositor.resume();
        
        try {
            return depositorAgent.getBalance(account);
        }
        finally {
            // Suspend the operation
            depositor.suspend();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.examples.operations.application.BankingService#getWithdrawlBalance(java.lang.String, long)
     */
    @Override
    public int getWithdrawalBalance(String bank, long account) {
        OperationHandle<WithdrawalScope> withdrawer = getWithdrawerBankHandle(bank);
        
        // Set the context for the withdrawal balance check
        withdrawer.resume();
        
        try {
            return withdrawerAgent.getBalance(account);
        }
        finally {
            // suspend the operation
            withdrawer.suspend();
        }
    }

}
