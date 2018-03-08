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

package messages;

import java.util.*;


public class BookstoreMessages extends ListResourceBundle {
    static final Object[][] contents =
    {
        {
            "ServerError",
            "Your request cannot be completed.  The server got the following error: "
        },
        { "TitleServerError", "Server Error" },
        { "TitleShoppingCart", "Shopping Cart" },
        { "TitleReceipt", "Receipt" },
        { "TitleBookCatalog", "Book Catalog" },
        { "TitleCashier", "Cashier" },
        { "TitleBookDescription", "Book Description" },
        { "Visitor", "You are visitor number " },
        
        { "What", "What We\'re Reading" },
        {
            "Talk",
            " talks about how web components can transform the way you develop applications for the web. This is a must read for any self respecting web developer!"
        },
        { "Start", "Start Shopping" },
        { "Critics", "Here's what the critics say: " },
        { "Price", "Our Price: " },
        { "CartRemoved", "You just removed " },
        { "CartCleared", "You just cleared your shopping cart!" },
        { "CartContents", "Your shopping cart contains " },
        { "CartItem", " item" },
        { "CartItems", " items" },
        { "CartAdded1", "You added " },
        { "CartAdded2", " to your shopping cart." },
        { "CartCheck", "Check Shopping Cart" },
        { "CartAdd", "Add to Cart" },
        { "By", "by" },
        { "Buy", "Buy Your Books" },
        { "Choose", "Please choose from our selections:" },
        { "ItemQuantity", "Quantity" },
        { "ItemTitle", "Title" },
        { "ItemPrice", "Price" },
        { "RemoveItem", "Remove Item" },
        { "Subtotal", "Subtotal:" },
        { "ContinueShopping", "Continue Shopping" },
        { "Checkout", "Check Out" },
        { "ClearCart", "Clear Cart" },
        { "CartEmpty", "Your cart is empty." },
        { "Amount", "Your total purchase amount is:" },
        {
            "Purchase",
            "To purchase the items in your shopping cart, please provide us with the following information:"
        },
        { "Name", "Name:" },
        { "CCNumber", "Credit Card Number:" },
        { "Submit", "Submit Information" },
        { "Catalog", "Back to the Catalog" },
        { "ThankYou", "Thank you for purchasing your books from us " },
        { "ThankYouParam", "Thank you, {0} for purchasing your books from us " },
        {
            "OrderError",
            "Your order could not be completed due to insufficient inventory."
        },
        { "With", "With" },
        
        { "Shipping", "Shipping:" },
        { "QuickShip", "Quick Shipping" },
        { "NormalShip", "Normal Shipping" },
        { "SaverShip", "Saver Shipping" },
        { "ShipDate", "Your order will be shipped on " },
        { "ShipDateLC", "your order will be shipped on " },
        
        { "ConfirmAdd", "You just added \"{0}\" to your shopping cart" },
        { "ConfirmRemove", "You just removed \"{0}\" from your shopping cart" },
        {
            "CartItemCount",
            "Your shopping cart contains " +
            "{0,choice,0#no items|1#one item|1< {0} items}"
        },
        { "Newsletters", "FREE Newsletter Subscriptions:" },
        { "ThanksMsg", "Thank you.  Please click Submit to purchase your books." },
        {
            "DukeFanClub",
            "I'd like to join the Duke Fan Club, free with my purchase of over $100"
        },
        { "UpdateQuantities", "Update Quantities" },
        {
            "QuantitiesUpdated",
            "You just updated the quantity of each book in your shopping cart"
        },
        { "Quantities", "Copies of book in cart" },
        { "ChooseLocale", "Choose Your Preferred Locale From the Map" },
        { "English", "English" },
        { "German", "German" },
        { "Spanish", "Spanish" },
        { "French", "French" },
        { "CustomerInfo", "Enter your information into the form." },
        { "BookCatalog", "Add books from the catalog to your shopping cart." },
        { "ShoppingCart", "This page lists the books in your shopping cart." }
    };

    public Object[][] getContents() {
        return contents;
    }
}
