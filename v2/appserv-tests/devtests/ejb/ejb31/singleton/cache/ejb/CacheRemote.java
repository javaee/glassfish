package com.acme;

import javax.ejb.Remote;

@Remote
public interface CacheRemote {

    public int checkCache();

}