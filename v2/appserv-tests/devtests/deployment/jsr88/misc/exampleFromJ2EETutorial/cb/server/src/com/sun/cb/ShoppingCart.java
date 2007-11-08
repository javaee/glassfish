/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.  U.S. 
 * Government Rights - Commercial software.  Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and 
 * applicable provisions of the FAR and its supplements.  Use is subject 
 * to license terms.  
 * 
 * This distribution may include materials developed by third parties. 
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks 
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and 
 * other countries.  
 * 
 * Copyright (c) 2003 Sun Microsystems, Inc. Tous droits reserves.
 * 
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de 
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions 
 * en vigueur de la FAR (Federal Acquisition Regulations) et des 
 * supplements a celles-ci.  Distribue par des licences qui en 
 * restreignent l'utilisation.
 * 
 * Cette distribution peut comprendre des composants developpes par des 
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE 
 * sont des marques de fabrique ou des marques deposees de Sun 
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */

package com.sun.cb;

import java.util.*;
import java.math.BigDecimal;
import com.sun.cb.RetailPriceList;
import com.sun.cb.RetailPriceItem;
 
public class ShoppingCart {
  ArrayList items = null;
  BigDecimal total = new BigDecimal("0.00");
  int numberOfItems = 0;
  
  public ShoppingCart(RetailPriceList rpl) {
      items = new ArrayList();
      
      for(Iterator i = rpl.getItems().iterator(); i.hasNext(); ) {
        RetailPriceItem item = (RetailPriceItem) i.next();
        ShoppingCartItem sci = new ShoppingCartItem(item, new BigDecimal("0.0"), new BigDecimal("0.00"));
        items.add(sci);
        numberOfItems++;
      }
  }
  
  public synchronized void add (ShoppingCartItem item) {
    items.add(item);
    total = total.add(item.getPrice()).setScale(2);
    numberOfItems++;
  }

  public synchronized int getNumberOfItems() {
    return numberOfItems;
  }

  public synchronized ArrayList getItems() {
      return items;
  }

  protected void finalize() throws Throwable {
      items.clear();
  }

  public synchronized BigDecimal getTotal() {
    return total;
  }


  public synchronized void clear() {
      numberOfItems = 0;
      total = new BigDecimal("0.00");
      items.clear();
  }
}

