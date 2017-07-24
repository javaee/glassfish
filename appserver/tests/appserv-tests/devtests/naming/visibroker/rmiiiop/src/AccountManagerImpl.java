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

// AccountManagerImpl.java
import org.omg.PortableServer.*;

import java.util.*;

public class AccountManagerImpl extends Bank.AccountManagerPOA {

  public java.util.Hashtable getAccounts () {
    return _accounts;
  }

  public Bank.Account get (java.lang.String arg0) {
    return (Bank.Account) _accounts.get(arg0);
  }

  public synchronized Bank.Account create (Bank.AccountData arg0) {
    // Lookup the account in the account dictionary.
    Bank.Account account = (Bank.Account) _accounts.get(arg0.getName());
    // If there was no account in the dictionary, create one.
    if(account == null) {
      // Create the account implementation, given the balance.
      AccountImpl accountServant = new AccountImpl(arg0);
      try {
        // Activate it on the default POA which is root POA for this servant
        account = Bank.AccountHelper.narrow(_default_POA().servant_to_reference(accountServant));
      } catch (Exception e) {
        e.printStackTrace();
      }
      // Print out the new account.
      System.out.println("Created " + arg0.getName() + "'s account: " + account);
      // Save the account in the account dictionary.
      _accounts.put(arg0.getName(), account);
    }
    // Return the account.
    return account;
  }
  private Hashtable _accounts = new Hashtable();
}

