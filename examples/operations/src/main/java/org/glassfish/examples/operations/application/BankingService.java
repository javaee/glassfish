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
package org.glassfish.examples.operations.application;

import org.jvnet.hk2.annotations.Contract;

/**
 * This service transfers funds between accounts using the {@link TransferService} by
 * setting up the proper Operation for the Depositorer and Transferer.
 * 
 * @author jwells
 *
 */
@Contract
public interface BankingService {
    /**
     * Transfers funds between the withdrawl account and the deposit account.  If there
     * is not enough funds in the withdrawl account it may transfer less than the requested
     * amount
     * 
     * @param withdrawlBank The bank from which the withdrawl is being made
     * @param withdrawlAccount The account number to withdrawl funds from
     * @param depositorBank The bank to which the deposit is being done
     * @param depositAccount The account number to deposit funds to
     * @param funds The number of funds to transfer
     * @return The actual funds transferred
     */
    public int transferFunds(String withdrawlBank, long withdrawlAccount, String depositorBank, long depositAccount, int funds);
    
    /**
     * Tells how much money has been deposited in the given account in the
     * given bank
     * 
     * @param bank the name of the bank of the account to check
     * @param account the account number to get the deposited amount from
     * @return the amount of money deposited in this account
     */
    public int getDepositedBalance(String bank, long account);
    
    /**
     * Tells how much money that is available for withdrawl from the
     * given account number at the given bank.  Note that if the
     * account number has never been seen before the amount given
     * at the start is 100 funds
     * 
     * @param bank the name of the bank of the account to check
     * @param account the account number to check
     * @return the amount of money deposited in this account
     */
    public int getWithdrawlBalance(String bank, long account);

}
