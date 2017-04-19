package com.acme;

import java.util.concurrent.*;

public interface Hello {

    public void protectedSyncRemote();

    public void unprotectedSyncRemote();

    public Future<Object> protectedAsyncRemote();

    public Future<Object> unprotectedAsyncRemote();

    public void testProtectedSyncLocal();

    public void testProtectedAsyncLocal();

    public void testUnprotectedSyncLocal();

    public void testUnprotectedAsyncLocal();

}