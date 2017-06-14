/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
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


package dispatcher;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import cart.ShoppingCart;
import database.*;
import exception.*;
import foo.Bar;

public class Dispatcher extends HttpServlet {
    public void init() {
       //to test --libraries
       (new Bar()).baz();
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String bookId = null;
        BookDetails book = null;
        String clear = null;
        BookDBAO bookDBAO =
            (BookDBAO) getServletContext()
                           .getAttribute("bookDBAO");

        HttpSession session = request.getSession();
        String selectedScreen = request.getServletPath();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

        if (cart == null) {
            cart = new ShoppingCart();

            session.setAttribute("cart", cart);
        }

        if (selectedScreen.equals("/bookcatalog")) {
            bookId = request.getParameter("Add");

            if (!bookId.equals("")) {
                try {
                    book = bookDBAO.getBookDetails(bookId);

                    if (book.getOnSale()) {
                        double sale = book.getPrice() * .85;
                        Float salePrice = new Float(sale);
                        book.setPrice(salePrice.floatValue());
                    }

                    cart.add(bookId, book);
                } catch (BookNotFoundException ex) {
                    // not possible
                }
            }
        } else if (selectedScreen.equals("/bookshowcart")) {
            bookId = request.getParameter("Remove");

            if (bookId != null) {
                cart.remove(bookId);
            }

            clear = request.getParameter("Clear");

            if ((clear != null) && clear.equals("clear")) {
                cart.clear();
            }
        } else if (selectedScreen.equals("/bookreceipt")) {
            // Update the inventory
            try {
                bookDBAO.buyBooks(cart);
            } catch (OrderException ex) {
                try {
                    request.getRequestDispatcher("/bookordererror.jsp")
                           .forward(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            request.getRequestDispatcher("/template/template.jsp")
                   .forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("selectedScreen", request.getServletPath());

        try {
            request.getRequestDispatcher("/template/template.jsp")
                   .forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
