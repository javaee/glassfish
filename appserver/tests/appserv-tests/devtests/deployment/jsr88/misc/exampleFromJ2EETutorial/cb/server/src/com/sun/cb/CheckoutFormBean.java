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

package com.sun.cb;

import java.math.BigDecimal;
import java.util.*;

public class CheckoutFormBean {
  private String firstName;
  private String lastName;
  private String email;
  private String areaCode;
  private String phoneNumber;
  private String street;
  private String city;
  private String state;
  private String zip;
  private int CCOption;
  private String CCNumber;
  private HashMap errors;
  private ShoppingCart cart;
  private RetailPriceList rpl;
  private OrderConfirmations ocs;
  private ResourceBundle messages;
  

  public boolean validate() {
    boolean allOk=true;
    if (firstName.equals("")) {
      errors.put("firstName",messages.getString("FirstNameError"));
      firstName="";
      allOk=false;
    } else {
      errors.put("firstName","");
    }
    
    if (lastName.equals("")) {
      errors.put("lastName",messages.getString("LastNameError"));
      lastName="";
      allOk=false;
    } else {
      errors.put("lastName","");
    }

    if (email.equals("") || (email.indexOf('@') == -1)) {
      errors.put("email",messages.getString("EMailError"));
      email="";
      allOk=false;
    } else {
      errors.put("email","");
    }

    if (areaCode.equals("")) {
      errors.put("areaCode",messages.getString("AreaCodeError"));
      areaCode="";
      allOk=false;
    } else {
      errors.put("areaCode","");
    }

    if (phoneNumber.equals("")) {
      errors.put("phoneNumber",messages.getString("PhoneNumberError"));
      phoneNumber="";
      allOk=false;
    } else {
      errors.put("phoneNumber","");
    }

    if (street.equals("")) {
      errors.put("street",messages.getString("StreetError"));
      street="";
      allOk=false;
    } else {
      errors.put("street","");
    }

    if (city.equals("")) {
      errors.put("city",messages.getString("CityError"));
      city="";
      allOk=false;
    } else {
      errors.put("city","");
    }

    if (state.equals("")) {
      errors.put("state",messages.getString("StateError"));
      state="";
      allOk=false;
    } else {
      errors.put("state","");
    }

    if (zip.equals("") || zip.length() !=5 ) {
      errors.put("zip",messages.getString("ZipError"));
      zip="";
      allOk=false;
    } else {
      try {
        int x = Integer.parseInt(zip);
      	errors.put("zip","");
      } catch (NumberFormatException e) {
        errors.put("zip",messages.getString("ZipError"));
        zip="";
        allOk=false;
      }	
    }

    if (CCNumber.equals("")) {
      errors.put("CCNumber",messages.getString("CCNumberError"));
      CCNumber="";
      allOk=false;
    } else {
      errors.put("CCNumber","");
    }

    ocs.clear();

    ConfirmationBean confirmation = null;

    if (allOk) {
      String orderId = CCNumber;
       
      AddressBean address = 
        new AddressBean(street, city, state, zip);
      CustomerBean customer = 
        new CustomerBean(firstName, lastName, 
          "(" + areaCode+ ") " + phoneNumber, email);
          
      for (Iterator d = rpl.getDistributors().iterator(); 
          d.hasNext(); ) {
        String distributor = (String)d.next();
        System.out.println(distributor);				
        ArrayList lis = new ArrayList();
        BigDecimal price = new BigDecimal("0.00");
        BigDecimal total = new BigDecimal("0.00");
        for (Iterator c = cart.getItems().iterator(); 
            c.hasNext(); ) {
          ShoppingCartItem sci = (ShoppingCartItem) c.next();
          if ((sci.getItem().getDistributor()).
              equals(distributor) && 
              sci.getPounds().floatValue() > 0) {
            price = sci.getItem().getWholesalePricePerPound().
              multiply(sci.getPounds());	
            total = total.add(price);
            LineItemBean li = new LineItemBean(
              sci.getItem().getCoffeeName(), sci.getPounds(), 
              sci.getItem().getWholesalePricePerPound());
            lis.add(li);
          }
        }

        LineItemBean[] lineItems = new LineItemBean[lis.size()];
        int i=0;
        for(Iterator j = lis.iterator(); j.hasNext();) {
          lineItems[i] = (LineItemBean)j.next();
          i++;
        }
        
        if (lineItems.length != 0) {
          OrderBean order = new OrderBean(address, customer, 
            orderId, lineItems, total);
            
          String SAAJOrderURL = 
            URLHelper.getSaajURL() + "/orderCoffee";
          if (distributor.equals(SAAJOrderURL)) {
            OrderRequest or = new OrderRequest(SAAJOrderURL);
            confirmation = or.placeOrder(order);
          }
          else {
            OrderCaller ocaller = new OrderCaller(distributor);
            confirmation = ocaller.placeOrder(order);				
          }
          OrderConfirmation oc = 
            new OrderConfirmation(order, confirmation);
          ocs.add(oc);
        } 
      }
    }
    return allOk;
  }
  
  public HashMap getErrors() {
    return errors;
  }

  public String getErrorMsg(String s) {
    String errorMsg =(String)errors.get(s.trim());
    return (errorMsg == null) ? "":errorMsg;
  }

  public CheckoutFormBean(ShoppingCart cart, RetailPriceList rpl, ResourceBundle messages) {
   firstName="";
   lastName="";
   email="";
   areaCode="";
   phoneNumber="";
   street="";
   city="";
   state="";
   zip="";
   CCOption=0;
   CCNumber="";
   errors = new HashMap();
   this.cart = cart;
   this.rpl = rpl;
   this.messages = messages;
   ocs = new OrderConfirmations();
  }
  
  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getZip() {
    return zip;
  }

  public String getAreaCode() {
    return areaCode;
  }
  
  public String getPhoneNumber() {
    return phoneNumber;
  }
   
  public String getStreet() {
    return street;
  }
   
  public String getCity() {
    return city;
  }
   
  public String getState() {
    return state;
  }
   
  
  public int getCCOption() {
    return CCOption;
  }
 
  
  public String getCCNumber() {
    return CCNumber;
  }
   
  
  public OrderConfirmations getOrderConfirmations() {
    return ocs;
  }
  
  public void setMessages(ResourceBundle messages) {
    this.messages=messages;
  }

  public void setFirstName(String firstname) {
    this.firstName=firstname;
  }

  public void setLastName(String lastname) {
    this.lastName=lastname;
  }

  public void setEmail(String email) {
    this.email=email;
  }

  public void setZip(String zip) {
    this.zip=zip;
  }

  public void setAreaCode(String areaCode) {
    this.areaCode=areaCode ;
  }
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber=phoneNumber ;
  }
  
  public void setStreet(String street) {
    this.street=street ;
  }
  
  public void setCity(String city) {
    this.city=city ;
  }
  
  public void setState(String state) {
    this.state=state ;
  }
  
  public void setCCOption(int CCOption) {
    this.CCOption=CCOption ;
  }
  public void setCCNumber(String CCNumber) {
    this.CCNumber=CCNumber ;
  }
  
  public void setErrors(String key, String msg) {	
    errors.put(key,msg);
  }

}



