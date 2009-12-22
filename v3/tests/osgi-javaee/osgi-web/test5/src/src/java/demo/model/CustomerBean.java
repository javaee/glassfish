/*
 * $Id: CustomerBean.java,v 1.3 2005/12/14 22:27:22 rlubke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package demo.model;


import java.io.Serializable;


/** <p>JavaBean represented the data for an individual customer.</p> */

public class CustomerBean implements Serializable {


    public CustomerBean() {
        this(null, null, null, 0.0);
    }


    public CustomerBean(String accountId, String name,
                        String symbol, double totalSales) {
        this.accountId = accountId;
        this.name = name;
        this.symbol = symbol;
        this.totalSales = totalSales;
    }


    private String accountId = null;


    public String getAccountId() {
        return (this.accountId);
    }


    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    private String name = null;


    public String getName() {
        return (this.name);
    }


    public void setName(String name) {
        this.name = name;
    }


    private String symbol = null;


    public String getSymbol() {
        return (this.symbol);
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    private double totalSales = 0.0;


    public double getTotalSales() {
        return (this.totalSales);
    }


    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer("CustomerBean[accountId=");
        sb.append(accountId);
        sb.append(",name=");
        sb.append(name);
        sb.append(",symbol=");
        sb.append(symbol);
        sb.append(",totalSales=");
        sb.append(totalSales);
        sb.append("]");
        return (sb.toString());
    }


}
