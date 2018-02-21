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

public class BookDetails implements Comparable {
    private String bookId = null;
    private String title = null;
    private String firstName = null;
    private String surname = null;
    private float price = 0.0F;
    private boolean onSale = false;
    private int year = 0;
    private String description = null;
    private int inventory = 0;

    public BookDetails() {
    }

    public BookDetails(String bookId, String surname, String firstName,
        String title, float price, boolean onSale, int year,
        String description, int inventory) {
        this.bookId = bookId;
        this.title = title;
        this.firstName = firstName;
        this.surname = surname;
        this.price = price;
        this.onSale = onSale;
        this.year = year;
        this.description = description;
        this.inventory = inventory;
    }

    public String getBookId() {
        return this.bookId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getSurname() {
        return this.surname;
    }

    public float getPrice() {
        return this.price;
    }

    public boolean getOnSale() {
        return this.onSale;
    }

    public int getYear() {
        return this.year;
    }

    public String getDescription() {
        return this.description;
    }

    public int getInventory() {
        return this.inventory;
    }

    public void setBookId(String id) {
        this.bookId = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public int compareTo(Object o) {
        BookDetails n = (BookDetails) o;
        int lastCmp = title.compareTo(n.title);

        return (lastCmp);
    }
}
