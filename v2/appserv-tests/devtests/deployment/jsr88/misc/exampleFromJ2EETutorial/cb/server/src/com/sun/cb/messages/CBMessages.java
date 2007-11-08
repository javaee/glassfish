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

package com.sun.cb.messages;

import java.util.*;

public class CBMessages extends ListResourceBundle {
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

