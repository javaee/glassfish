[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

## Operations

Operations are building blocks for crafting scope/context pairs like RequestScoped, ApplicationScoped or
TransactionScoped.  HK2 is designed to run in any java process, including those that do not have a container
such as Java EE.  Even when running on a JVM with no container, many applications still have a concept of
user requests or of different user applications running inside the same JVM.  In those cases it may be useful
for the writers of the system to use HK2 Operations to create a class of service whose life-cycle is controlled
by the start or stop of those user requests or controlling application or whatever other demarcation is needed
by the process as a whole.

### Operations Introduction

A HK2 Operation is defined first by a scope annotation.  Any scope annotation will do, but normally these
scopes are proxiable since they are meant to be injected into services with different lifecycles, such
as Singleton scoped services.  Also, they are normally marked to not be proxiable for the same scope.  It is not
a requirement that scopes used for HK2 operations are proxiable, it is just the normal case that they
are.

Once an HK2 Operation scope has been defined an implementation of [Context][context] must be added to
the target ServiceLocator.  To create an HK2 Operation [Context][context] all that is required is to extend
[OperationContext][operationcontext] and implement the getScope method, returning the class of the scope.

The system software will use the [OperationManager][operationmanager] to create instances of the
HK2 Operation.  A thread may only have have one instance of an HK2 Operation active at any time. 
The following statements are also true:

* Any number of HK2 Operations of different types (scopes) can be active on a single thread
* An instance of a HK2 Operation can be active on more than one thread at a time

When an instance of a HK2 Operation is created an [OperationHandle][operationhandle] is
returned.  The [OperationHandle][operationhandle] can be used to suspend, resume or destroy
the Operation instance.  It can be used to discover all the threads upon which an Operation
instance is active.  The [OperationHandle][operationhandle] is itself registered in HK2 as a
service in the Operation scope for which it was created, which is useful because arbitrary data
can be associated with the [OperationHandle][operationhandle].

The following example will illustrate basic usage of HK2 Operations.

### Operations Example

The example can be found under the HK2 source tree at examples/operations.  In the example
there is an banking application that maintains a deposit ledger and withdrawal ledger indexed
by account number.  These two ledgers keep track of the funds deposited or those available for
withdrawal.  Both services are scoped by the bank they represent.  There is a banking service
that can get the amounts deposited in an account at a bank or available for withdrawal from an
account at a bank or it can transfer money from one bank account to another bank account.  It does
this by switching the bank of the deposit account and/or the withdrawal account using HK2 Operations.

#### Example Operation Scope/Context pairs

There are two operation types/scopes defined in this example, a Deposit scope for the
depositor ledgers and a Withdrawal scope for the withdrawal ledgers.  Here is the
definition of the Deposit scope:

```java
@Scope
@Proxiable(proxyForSameScope = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DepositScope {
}
```java

Notice that the DepositScope is proxiable, and that it will not be proxied for other services
that are also in the DepositScope scope.  In HK2 every defined scope needs an implementation
of [Context][context].  Since this is to be an HK2 Operation the user only need to extend
[OperationContext][operationcontext] as below:

```java
@Singleton
public class DepositScopeContext extends OperationContext<DepositScope> {

    public Class<? extends Annotation> getScope() {
        return DepositScope.class;
    }
    
}
```java

The only method that needs to be implemented is the getScope method which need only 
return the class of the scope annotation.  The DespositScope has now been fully
defined as an HK2 Operation.  Below find the complete definition of the
WithdrawalScope:

```java
@Scope
@Proxiable(proxyForSameScope = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WithdrawalScope {
}
```java

```java
@Singleton
public class WithdrawalScopeContext extends OperationContext<WithdrawalScope> {

    public Class<? extends Annotation> getScope() {
        return WithdrawalScope.class;
    }

}
```java

#### Operation Scoped Services

Lets take a look at two services that are in the HK2 Operations scopes that we
just defined.  The first one is the DepositLedger, which keeps track of how much
has been deposited in an account by account number:

```java
@DepositScope
public class DepositorService {
    private Map<Long, Integer> accounts = new HashMap<Long, Integer>();
    
    public void depositFunds(long account, int funds) {
        Integer balance = accounts.get(account);
        if (balance == null) {
            accounts.put(account, funds);
        }
        else {
            int original = balance;
            original += funds;
            accounts.put(account, original);
        }
    }
    
    public int getBalance(long account) {
        Integer balance = accounts.get(account);
        if (balance == null) return 0;
        return balance;
        
    }

}
```java

This simple service is in the DepositScope.  The important thing to notice about this
service is that it does not keep track in any way which bank it is keeping these
accounts for.  That is because the bank where these accounts reside will be managed
by the system software using HK2 Operations.  That frees this service to only manage
accounts and not the banks where the accounts reside.  The withdrawal service is very
similar:

```java
@WithdrawalScope
public class WithdrawalService {
    private Map<Long, Integer> accounts = new HashMap<Long, Integer>();
    
    public int withdrawlFunds(long account, int funds) {
        Integer balance = accounts.get(account);
        if (balance == null) {
            // How nice, 100 for me!
            balance = new Integer(100);
        }
        
        int current = balance;
        if (funds > current) {
            funds = current;
        }
        
        current = current - funds;
        accounts.put(account, current);
        
        return funds;
    }
    
    public int getBalance(long account) {
        Integer balance = accounts.get(account);
        if (balance == null) {
            accounts.put(account, 100);
        }
        
        return accounts.get(account);
    }
}
```java

This service, like the previous one is in a proxiable HK2 Operation
scope.  In this case it is in the WithdrawalScope.  Like the previous
service, this service only keeps track of accounts with a single map,
since the WithdrawalScope will be managed per bank by the software
setting up the environment in which these services will get called.

The TransferService is in the Singleton scope, which is just a normal
scope, not an HK2 Operations scope.  Since a Singelton service has
a different lifecycle than the services in either DepositorScope or
WithdrawalScope the DepositorService and WithdrawalService injected
into it will instead inject a proxy, so that the actual service used
will depend on the context of the call.  This is the TransferService:

```java
@Singleton
public class TransferService {
    @Inject
    private DepositorService depositor;
    
    @Inject
    private WithdrawalService withdrawer;
    
    public int doTransfer(long depositAccount, long withdrawlAccount, int funds) {
        int recieved = withdrawer.withdrawlFunds(withdrawlAccount, funds);
        depositor.depositFunds(depositAccount, recieved);
        
        return recieved;
    }

}
```java

The TransferService injects the DepositorService and the WithdrawalService in order to
transfer funds from the withdrawer to the depositor.  This service also doesn't know
which banks are involved in the transfer, so it is also expecting the system software
to have used the HK2 Operations system in order to properly set the context of the
call to doTransfer.

#### The Banking Service

The BankingService is the service whose implementation will use the HK2 Operations
feature in order to properly setup the context in which the services in the
DepositScope and WithdrawalScope will always be called.  The BankingService is the
service given to end-users to use directly and so is represented by the following
contract:

```java
@Contract
public interface BankingService {
    /**
     * Transfers funds between the withdrawal account and the deposit account.  If there
     * is not enough funds in the withdrawal account it may transfer less than the requested
     * amount
     * 
     * @param withdrawlBank The bank from which the withdrawal is being made
     * @param withdrawlAccount The account number to withdrawal funds from
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
     * Tells how much money that is available for withdrawal from the
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
```java

It is an implementation of the BankingService that the user code will use to
perform the balance check and transfer operations.  Lets take a look at the
implementation of the getWithdrawalBalance in the implementation of BankingService,
BankingServiceImpl:

```java
@Singleton
public class BankingServiceImpl implements BankingService {
    @Inject
    private OperationManager manager;
    
    @Inject
    private WithdrawalService withdrawerAgent;
    
    private final Map<String, OperationHandle<WithdrawalScope>> withdrawers = new HashMap<String, OperationHandle<WithdrawalScope>>();
    
    private synchronized OperationHandle<WithdrawalScope> getWithdrawerBankHandle(String bank) {
        OperationHandle<WithdrawalScope> withdrawer = withdrawers.get(bank);
        if (withdrawer == null) {
            // create and start it
            withdrawer = manager.createOperation(WithdrawalScopeImpl.INSTANCE);
            withdrawers.put(bank, withdrawer);
        }
        
        return withdrawer;
    }

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
```java

Code not involved in the implementation of getWithdrawalBalance has been removed from
the code snippet above.

The BankingServiceImpl has a Map that goes from a Bank (represented as a String) to
an HK2 [OperationHandle][operationhandle] of type WithdrawalScope.  The method
getWithdrawerBankHandle looks in the Map for an [OperationHandle][operationhandle].
If it does not find one, it uses the createOperation method of
[OperationManager][operationmanager] in order to create it.  This method does
not start the operation, it just retrieves it from the Map or creates it.

The getWithdrawalBalance method of BankingServiceImpl uses the getWithdrawerBankHandle
to get the WithdrawalScope [OperationHandle][operationhandle] associated with the
given bank and then associates that [OperationHandle][operationhandle] with the
current thread by calling the resume method.  Calling the resume method
of [OperationHandle][operationhandle] sets the calling context for the injected field
withdrawerAgent, which will now use the WithdrawalService for that particular bank.
Once the underlying operation has completed, the suspend method of
[OperationHandle][operationhandle] is called to disassociate that HK2 Operation
from the current thread.

The getDepositedBalance method of BankingServiceImpl works the same way as the
getWithdrawalBalance with the difference being that the HK2 Operation used is
of type DepositScope as opposed to WithdrawalScope.

#### Transferring Funds Between Banks

The implementation of the method transferFunds in BankingServiceImpl is seen below:

```java
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
```java

The interesting thing about this method is that it illustrates that two HK2 Operations
of different types (DepositScope and WithdrawalScope) can be active on a single thread
at the same time.  This duality allows the transfer service to operate between banks simultaneously.

#### Seeing it Work

To see the example working there is a junit test case.  The test case simply creates
some Bank Strings (Chase, Bank of America and South Jersey Federal Credit Union) and
then checks balances, makes transfers between banks, and checks the balances afterwards
to make sure they are as expected.  Here it is:

```java
@Test
    public void testTransferBetweenBanks() {
        ServiceLocator locator = getServiceLocator();
        
        BankingService bankingService = locator.getService(BankingService.class);
        
        // First, initialize the accounts of ALICE, BOB and CAROL with 100 funds
        int aliceBalance = bankingService.getWithdrawalBalance(CHASE_BANK, ALICE_ACCOUNT);
        int bobBalance = bankingService.getWithdrawalBalance(BOA_BANK, BOB_ACCOUNT);
        int carolBalance = bankingService.getWithdrawalBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
        Assert.assertEquals(100, aliceBalance);
        Assert.assertEquals(100, bobBalance);
        Assert.assertEquals(100, carolBalance);
        
        // OK, lets transfer that 100 from alice to bob
        int amtTransferred = bankingService.transferFunds(CHASE_BANK, ALICE_ACCOUNT, BOA_BANK, BOB_ACCOUNT, 100);
        
        Assert.assertEquals(100, amtTransferred);
        
        // And lets check the withdrawl funds again, alice should have zero, bob should have 100, carol should still have 100
        aliceBalance = bankingService.getWithdrawalBalance(CHASE_BANK, ALICE_ACCOUNT);
        bobBalance = bankingService.getWithdrawalBalance(BOA_BANK, BOB_ACCOUNT);
        carolBalance = bankingService.getWithdrawalBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
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
        aliceBalance = bankingService.getWithdrawalBalance(CHASE_BANK, ALICE_ACCOUNT);
        bobBalance = bankingService.getWithdrawalBalance(BOA_BANK, BOB_ACCOUNT);
        carolBalance = bankingService.getWithdrawalBalance(SJFCU_BANK, CAROL_ACCOUNT);
        
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
```java

### Conclusion

HK2 Operations provide a convenient set of tools for building scopes/context pairs that
follow the general rule of "one operation on a thread at a time."  There are many Operations
that correspond to this rule, such as RequestScope, ApplicationScope and TransactionScope.  Using
a consistent facility such as HK2 Operations can reduce the code needed by your application to
manage those scopes.

[context]: apidocs/org/glassfish/hk2/api/Context.html
[operationcontext]: apidocs/org/glassfish/hk2/extras/operation/OperationContext.html
[operationmanager]: apidocs/org/glassfish/hk2/extras/operation/OperationManager.html
[operationhandle]: apidocs/org/glassfish/hk2/extras/operation/OperationHandle.html
