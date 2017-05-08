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






