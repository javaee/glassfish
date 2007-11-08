package com.sun.s1asdev.ejb.bmp.txtests.simple.ejb;

import java.io.Serializable;

public class CustomerInfo
    implements Serializable
{
    private int customerID;
    private String customerPhone;

    public CustomerInfo(int id, String phone) {
        this.customerID = id;
        this.customerPhone = phone;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String toString() {
        return "CustomerIfo: id=" + customerID + "; phone=" + customerPhone;
    }
}
