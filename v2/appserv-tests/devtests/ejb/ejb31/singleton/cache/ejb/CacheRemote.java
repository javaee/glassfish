package com.acme;

import javax.ejb.Remote;
import java.util.concurrent.Future;

@Remote
public interface CacheRemote {

    public int checkCache();

    public void fooAsync();

}