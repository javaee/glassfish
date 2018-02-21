/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package cart;

import java.util.*;
import database.BookDetails;


public class ShoppingCart {
    HashMap items = null;
    int numberOfItems = 0;

    public ShoppingCart() {
        items = new HashMap();
    }

    public synchronized void add(String bookId, BookDetails book) {
        if (items.containsKey(bookId)) {
            ShoppingCartItem scitem = (ShoppingCartItem) items.get(bookId);
            scitem.incrementQuantity();
            System.out.println("in add, quantity is " + scitem.getQuantity());
        } else {
            ShoppingCartItem newItem = new ShoppingCartItem(book);
            items.put(bookId, newItem);
            System.out.println("in add, quantity is " + newItem.getQuantity());
        }

        //      numberOfItems++;
    }

    public synchronized void remove(String bookId) {
        if (items.containsKey(bookId)) {
            ShoppingCartItem scitem = (ShoppingCartItem) items.get(bookId);
            scitem.decrementQuantity();

            if (scitem.getQuantity() <= 0) {
                items.remove(bookId);
            }

            numberOfItems--;
        }
    }

    public synchronized List getItems() {
        List results = new ArrayList();
        Iterator items = this.items.values()
                                   .iterator();

        while (items.hasNext()) {
            results.add(items.next());
        }

        return (results);
    }

    protected void finalize() throws Throwable {
        items.clear();
    }

    public synchronized int getNumberOfItems() {
        numberOfItems = 0;

        for (Iterator i = getItems()
                              .iterator(); i.hasNext();) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            numberOfItems += item.getQuantity();
            System.out.println("number of items is " + numberOfItems);
        }

        return numberOfItems;
    }

    public synchronized double getTotal() {
        double amount = 0.0;

        for (Iterator i = getItems()
                              .iterator(); i.hasNext();) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            BookDetails bookDetails = (BookDetails) item.getItem();

            amount += (item.getQuantity() * bookDetails.getPrice());
        }

        return roundOff(amount);
    }

    private double roundOff(double x) {
        long val = Math.round(x * 100); // cents

        return val / 100.0;
    }

    public synchronized void clear() {
        System.err.println("Clearing cart.");
        items.clear();
        numberOfItems = 0;
    }
}
