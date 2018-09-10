/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package examples.sfsb;

import java.io.Serializable;

/**
 * This class reflects the results of a buy/sell transaction.
 */
public final class TradeResult implements Serializable {

  public static final int SELL = 0;
  public static final int BUY  = 1;

  private int    numberTraded;    // Number of shares really bought or sold
  private int    action;          // Whether shares were bought or sold
  private double price;           // Price shares were bought or sold at

  public TradeResult(){
  }

  /**
   * Returns the number of shares and sales price for a
   * buy or sell transaction in a TradeResult object.
   *
   * @param numberTraded      int Number of shares traded
   * @param price             double Price shares sold at
   * @param action            int Action taken
   * @return                  TradeResult
   */
  public TradeResult(int numberTraded, double price, int action) {
    this.numberTraded = numberTraded;
    this.price        = price;
    this.action       = action;
  }

  public int getNumberTraded() {
    return numberTraded;
  }

  public void setNumberTraded( int numberTraded ){
    this.numberTraded = numberTraded;
  }

  public int getActionTaken() {
    return action;
  }

  public void setActionTaken( int action ){
    this.action = action;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice( double price ){
    this.price = price;
  }

  public String toString() {
    String result = numberTraded + " shares";
    if(action == SELL) {
      result += "sold";
    } else {
      result += "bought";
    }
    result += " at a price of " + price;
    return result;
  }
}
