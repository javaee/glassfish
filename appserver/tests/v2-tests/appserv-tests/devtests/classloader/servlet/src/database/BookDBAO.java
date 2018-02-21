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

package database;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;
import exception.*;
import cart.*;


public class BookDBAO {
    private ArrayList books;
    Connection con;

    public BookDBAO() throws Exception {
        try {
            InitialContext ic = new InitialContext();
            Context envCtx = (Context) ic.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/BookDB");
            con = ds.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Couldn't open connection to database: " +
                ex.getMessage());
        }
    }

    public void remove() {
        try {
            con.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List getBooks() throws BooksNotFoundException {
        books = new ArrayList();

        try {
            String selectStatement = "select * " + "from books";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {
                BookDetails bd =
                    new BookDetails(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getFloat(5),
                        rs.getBoolean(6), rs.getInt(7), rs.getString(8),
                        rs.getInt(9));

                if (rs.getInt(9) > 0) {
                    books.add(bd);
                }
            }

            prepStmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BooksNotFoundException(ex.getMessage());
        }

        Collections.sort(books);

        return books;
    }

    public BookDetails getBookDetails(String bookId)
        throws BookNotFoundException {
        try {
            String selectStatement = "select * " + "from books where id = ? ";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            prepStmt.setString(1, bookId);

            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {
                BookDetails bd =
                    new BookDetails(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getFloat(5),
                        rs.getBoolean(6), rs.getInt(7), rs.getString(8),
                        rs.getInt(9));
                prepStmt.close();

                return bd;
            } else {
                prepStmt.close();
                System.err.println("Couldn't find book: " + bookId);
                throw new BookNotFoundException("Couldn't find book: " +
                    bookId);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BookNotFoundException("Couldn't find book: " + bookId +
                " " + ex.getMessage());
        }
    }

    public void buyBooks(ShoppingCart cart) throws OrderException {
        Collection items = cart.getItems();
        Iterator i = items.iterator();

        try {
            con.setAutoCommit(false);

            while (i.hasNext()) {
                ShoppingCartItem sci = (ShoppingCartItem) i.next();
                BookDetails bd = (BookDetails) sci.getItem();
                String id = bd.getBookId();
                int quantity = sci.getQuantity();
                buyBook(id, quantity);
            }

            con.commit();
            con.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                con.rollback();
                throw new OrderException("Transaction failed: " +
                    ex.getMessage());
            } catch (SQLException sqx) {
                throw new OrderException("Rollback failed: " +
                    sqx.getMessage());
            }
        }
    }

    public void buyBook(String bookId, int quantity) throws OrderException {
        try {
            String selectStatement = "select * " + "from books where id = ? ";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            prepStmt.setString(1, bookId);

            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {
                int inventory = rs.getInt(9);
                prepStmt.close();

                if ((inventory - quantity) >= 0) {
                    String updateStatement =
                        "update books set inventory = inventory - ? where id = ?";
                    prepStmt = con.prepareStatement(updateStatement);
                    prepStmt.setInt(1, quantity);
                    prepStmt.setString(2, bookId);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                } else {
                    throw new OrderException("Not enough of " + bookId +
                        " in stock to complete order.");
                }
            }
        } catch (Exception ex) {
            throw new OrderException("Couldn't purchase book: " + bookId +
                ex.getMessage());
        }
    }
}
