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
package org.glassfish.examples.operations.tests;

import org.glassfish.examples.operations.application.BankingService;
import org.glassfish.examples.operations.application.DepositorService;
import org.glassfish.examples.operations.application.TransferService;
import org.glassfish.examples.operations.application.WithdrawlService;
import org.glassfish.examples.operations.application.internal.BankingServiceImpl;
import org.glassfish.examples.operations.scopes.DepositScopeContext;
import org.glassfish.examples.operations.scopes.WithdrawlScopeContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * These test cases demonstrate the use of Operation scopes
 * 
 * @author jwells
 *
 */
public class OperationsTest {
    public static final ServiceLocatorFactory FACTORY = ServiceLocatorFactory.getInstance();
    
    private final static String CHASE_BANK = "Chase";
    private final static String BOA_BANK = "Bank of America";
    private final static String SJFCU_BANK = "South Jersey Federal Credit Union";
    
    private final static long ALICE_ACCOUNT = 1000;
    private final static long BOB_ACCOUNT = 2000;
    private final static long CAROL_ACCOUNT = 3000;
    
    private ServiceLocator getServiceLocator() {
        ServiceLocator retVal = FACTORY.create(null);
        
        ExtrasUtilities.enableOperations(retVal);
        ServiceLocatorUtilities.addClasses(retVal,
                DepositScopeContext.class,
                WithdrawlScopeContext.class,
                BankingServiceImpl.class,
                DepositorService.class,
                WithdrawlService.class,
                TransferService.class);
        
        return retVal;
    }
    
    /**
     * Tests that we can transfer funds between ALICE and BOB
     */
    @Test
    public void testTransferBetweenBanks() {
        ServiceLocator locator = getServiceLocator();
        
        BankingService bankingService = locator.getService(BankingService.class);
        
        // First, initialize the accounts of ALICE, BOB and CAROL with 100 funds
        int aliceBalance = bankingService.getWithdrawlBalance(CHASE_BANK, ALICE_ACCOUNT);
        int bobBalance = bankingService.getWithdrawlBalance(BOA_BANK, BOB_ACCOUNT);
        int carolBalance = bankingService.getWithdrawlBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(100, aliceBalance);
        Assert.assertEquals(100, bobBalance);
        Assert.assertEquals(100, carolBalance);
        
        // OK, lets transfer that 100 from alice to bob
        int amtTransferred = bankingService.transferFunds(CHASE_BANK, ALICE_ACCOUNT, BOA_BANK, BOB_ACCOUNT, 100);
        
        Assert.assertEquals(100, amtTransferred);
        
        // And lets check the withdrawl funds again, alice should have zero, bob should have 100, carol should still have 100
        aliceBalance = bankingService.getWithdrawlBalance(CHASE_BANK, ALICE_ACCOUNT);
        bobBalance = bankingService.getWithdrawlBalance(BOA_BANK, BOB_ACCOUNT);
        carolBalance = bankingService.getWithdrawlBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(0, aliceBalance);
        Assert.assertEquals(100, bobBalance);
        Assert.assertEquals(100, carolBalance);
        
        // But now bob should have 100 in his deposit account
        int toAlice = bankingService.getDepositedBalance(CHASE_BANK, ALICE_ACCOUNT);
        int toBob = bankingService.getDepositedBalance(BOA_BANK, BOB_ACCOUNT);
        int toCarol = bankingService.getDepositedBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(0, toAlice);
        Assert.assertEquals(100, toBob);
        Assert.assertEquals(0, toCarol);
        
        // Now lets have Carol transfer to Alice
        amtTransferred = bankingService.transferFunds(SJFCU_BANK, CAROL_ACCOUNT, CHASE_BANK, ALICE_ACCOUNT, 100);
        
        // Now Alice and Carol should have nothing left, while Bob still has his original 100
        aliceBalance = bankingService.getWithdrawlBalance(CHASE_BANK, ALICE_ACCOUNT);
        bobBalance = bankingService.getWithdrawlBalance(BOA_BANK, BOB_ACCOUNT);
        carolBalance = bankingService.getWithdrawlBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(0, aliceBalance);
        Assert.assertEquals(100, bobBalance);
        Assert.assertEquals(0, carolBalance);
        
        // At this point Alice and Carol should each have 100 in there deposit account
        toAlice = bankingService.getDepositedBalance(CHASE_BANK, ALICE_ACCOUNT);
        toBob = bankingService.getDepositedBalance(BOA_BANK, BOB_ACCOUNT);
        toCarol = bankingService.getDepositedBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(100, toAlice);
        Assert.assertEquals(100, toBob);
        Assert.assertEquals(0, toCarol);
        
        // Yay, the Operations worked properly!
    }
}
