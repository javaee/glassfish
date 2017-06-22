/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb.messages;

import java.util.*;

public class CBMessages_en extends ListResourceBundle {
  public Object[][] getContents() {
    return contents;
  }

  static final Object[][] contents = {

  {"ServerError", "Your request cannot be completed.  The server got the following error: "},
  {"TitleServerError", "Server Error"},
  {"TitleOrderForm", "Order Form"},
  {"TitleCheckoutForm", "Checkout Form"},
  {"TitleCheckoutAck", "Confirmation"},
  {"OrderInstructions", "Enter the amount of coffee and click Update to update the totals.<br>Click Checkout to proceed with your order. "},
  {"OrderForm", "OrderForm"},
  {"Price", "Price"},
  {"Quantity", "Quantity"},
  {"Total", "Total"},
  {"Update", "Update"},
  {"Checkout", "Checkout"},
  {"CheckoutInstructions", "To complete your order, fill in the form and click Submit."},
  {"YourOrder", "Your order totals "},
  {"CheckoutForm", "Checkout Form"},
  {"FirstName", "First Name"},
  {"FirstNameError", "Please enter your first name."},
  {"LastName", "Last Name"},
  {"LastNameError", "Please enter your last name."},
  {"EMail", " E-Mail"},
  {"EMailError", "Please enter a valid e-mail address."},
  {"PhoneNumber", "Phone Number"},
  {"AreaCodeError", "Please enter your area code."},
  {"PhoneNumberError", "Please enter your phone number."},
  {"Street", "Street"},
  {"StreetError", "Please enter your street."},
  {"City", "City"},
  {"CityError", "Please enter your city."},
  {"State", "State"},
  {"StateError", "Please enter your state."},
  {"Zip", "Zip"},
  {"ZipError", "Please enter a valid zip code."},
  {"CCOption", "Credit Card"},
  {"CCNumber", "Credit Card Number"},
  {"CCNumberError", "Please enter your credit card number."},
  {"Submit", "Submit"},
  {"Reset", "Reset"},
  {"ItemPrice", "Price"},
  {"OrderConfirmed", "Your order has been confirmed."},
  {"ShipDate", "Ship Date"},
  {"Items", "Items"},
  {"Coffee", "Coffee"},
  {"Pounds", "Pounds"},
  {"ContinueShopping", "Continue Shopping"}
  };
}

