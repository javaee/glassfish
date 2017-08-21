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

// Client.java

public class Client {

  public static void main(String[] args) {
    try {
      // Initialize the ORB.
      org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);
      // Get the manager Id
      byte[] managerId = "RMIBankManager".getBytes();
      // Locate an account manager. Give the full POA name and the servant ID.
      Bank.AccountManager manager = 
	Bank.AccountManagerHelper.bind(orb, "/rmi_bank_poa", managerId);
      // Use any number of argument pairs to indicate name,balance of accounts to create
      if (args.length == 0 || args.length % 2 != 0) {
        args = new String[2];
        args[0] = "Jack B. Quick";
        args[1] = "123.23";

      }
      int i = 0;
      while (i < args.length) {
        String name = args[i++];
        float balance;
        try {
          balance = new Float(args[i++]).floatValue();
        } catch (NumberFormatException n) {
          balance = 0;
        }
        Bank.AccountData data = new Bank.AccountData(name, balance);
        Bank.Account account = manager.create(data);
        System.out.println
          ("Created account for " + name + " with opening balance of $" + balance);
      } 
    
      java.util.Hashtable accounts = manager.getAccounts();
    
      for (java.util.Enumeration e = accounts.elements(); e.hasMoreElements();) {
        Bank.Account account = Bank.AccountHelper.narrow((org.omg.CORBA.Object)e.nextElement());
        String name = account.name();
        float balance = account.getBalance();
        System.out.println("Current balance in " + name + "'s account is $" + balance);
        System.out.println("Crediting $10 to " + name + "'s account.");
        account.setBalance(balance + (float)10.0);
        balance = account.getBalance();
        System.out.println("New balance in " + name + "'s account is $" + balance);
      }
    } catch (java.rmi.RemoteException e) {
      System.err.println(e);
    }
  }
}
