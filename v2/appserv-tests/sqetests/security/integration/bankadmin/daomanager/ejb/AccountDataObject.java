package com.sun.s1peqe.security.integration.bankadmin.daomanager;

import java.util.HashMap;

public class AccountDataObject implements java.io.Serializable
{
    private String accountId="*****empty****";
    private Double amount;
    private HashMap permList;

    public AccountDataObject(){}

    public AccountDataObject(String id,Double amt,HashMap permList)
    {
        this.accountId=id;
        this.amount=amt;
        this.permList=permList;
    }

    public void setAccountID(String id){
        accountId=id;
    }

    public void setAmount(Double amt){
        amount=amt;
    }

    public void setPermissionList(HashMap list){
        permList=list;
    }

    public String getAccountID(){
        return accountId;
    }

    public Double getAmount(){
        return amount;
    }

    public HashMap getPermissionList(){
        return permList;
    }

    public String toString(){
		return new String(getAccountID());
	}

}

