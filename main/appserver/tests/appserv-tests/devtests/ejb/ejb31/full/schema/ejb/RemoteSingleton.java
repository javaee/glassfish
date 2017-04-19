package com.acme;

import javax.ejb.Remote;

@Remote
public interface RemoteSingleton {

    public boolean getTestResult();

}