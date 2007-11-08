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

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.math.BigDecimal;

public class Dispatcher extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) {   
    HttpSession session = request.getSession();
    ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
    if (messages == null) {
        Locale locale=request.getLocale();
        messages = ResourceBundle.getBundle("com.sun.cb.messages.CBMessages", locale); 
        session.setAttribute("messages", messages);
    }

    ServletContext context = getServletContext();
    RetailPriceList rpl = (RetailPriceList)context.getAttribute("retailPriceList");
    if (rpl == null) {
      try {
          rpl = new RetailPriceList();
          context.setAttribute("retailPriceList", rpl);
        } catch (Exception ex) {
          context.log("Couldn't create price list: " + ex.getMessage());
        }
    }
    ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
    if (cart == null) {
        cart = new ShoppingCart(rpl);
        session.setAttribute("cart", cart);
    }


    String selectedScreen = request.getServletPath();
    if (selectedScreen.equals("/checkoutForm")) {
      CheckoutFormBean checkoutFormBean = new CheckoutFormBean(cart, rpl, messages);

      request.setAttribute("checkoutFormBean", checkoutFormBean);
      try {
        checkoutFormBean.setFirstName(request.getParameter("firstName"));
        checkoutFormBean.setLastName(request.getParameter("lastName"));
        checkoutFormBean.setEmail(request.getParameter("email"));
        checkoutFormBean.setAreaCode(request.getParameter("areaCode"));
        checkoutFormBean.setPhoneNumber(request.getParameter("phoneNumber"));
        checkoutFormBean.setStreet(request.getParameter("street"));
        checkoutFormBean.setCity(request.getParameter("city"));
        checkoutFormBean.setState(request.getParameter("state"));
        checkoutFormBean.setZip(request.getParameter("zip"));
        checkoutFormBean.setCCNumber(request.getParameter("CCNumber"));
        checkoutFormBean.setCCOption(Integer.parseInt(request.getParameter("CCOption")));
      } catch (NumberFormatException e) {
        // not possible
      }
    }
    try {
        request.getRequestDispatcher("/template/template.jsp").forward(request, response);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
    String selectedScreen = request.getServletPath();
    ServletContext context = getServletContext();

    RetailPriceList rpl = (RetailPriceList)context.getAttribute("retailPriceList");
    if (rpl == null) {
      try {
          rpl = new RetailPriceList();
          context.setAttribute("retailPriceList", rpl);
        } catch (Exception ex) {
          context.log("Couldn't create price list: " + ex.getMessage());
        }
    }
    ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
    if (cart == null ) {
        cart = new ShoppingCart(rpl);
        session.setAttribute("cart", cart);
    }
 
    if (selectedScreen.equals("/orderForm")) {
      cart.clear();
      for(Iterator i = rpl.getItems().iterator(); i.hasNext(); ) {
        RetailPriceItem item = (RetailPriceItem) i.next();
        String coffeeName = item.getCoffeeName();
        BigDecimal pounds = new BigDecimal(request.getParameter(coffeeName + "_pounds"));
        BigDecimal price = item.getRetailPricePerPound().multiply(pounds).setScale(2, BigDecimal.ROUND_HALF_UP);
        ShoppingCartItem sci = new ShoppingCartItem(item, pounds, price);
        cart.add(sci);
      }

    } else if (selectedScreen.equals("/checkoutAck")) {
      CheckoutFormBean checkoutFormBean = new CheckoutFormBean(cart, rpl, messages);

      request.setAttribute("checkoutFormBean", checkoutFormBean);
      try {
        checkoutFormBean.setFirstName(request.getParameter("firstName"));
        checkoutFormBean.setLastName(request.getParameter("lastName"));
        checkoutFormBean.setEmail(request.getParameter("email"));
        checkoutFormBean.setAreaCode(request.getParameter("areaCode"));
        checkoutFormBean.setPhoneNumber(request.getParameter("phoneNumber"));
        checkoutFormBean.setStreet(request.getParameter("street"));
        checkoutFormBean.setCity(request.getParameter("city"));
        checkoutFormBean.setState(request.getParameter("state"));
        checkoutFormBean.setZip(request.getParameter("zip"));
        checkoutFormBean.setCCNumber(request.getParameter("CCNumber"));
        checkoutFormBean.setCCOption(Integer.parseInt(request.getParameter("CCOption")));
      } catch (NumberFormatException e) {
        // not possible
      }
      if (!checkoutFormBean.validate()) {
        try {
            request.getRequestDispatcher("/checkoutForm.jsp").forward(request, response);
        } catch(Exception e) {
            e.printStackTrace();
        }
      }
    }

    try {
        request.getRequestDispatcher("/template/template.jsp").forward(request, response);
    } catch(Exception e) {
    }
  }
}






