package com.sun.s1asdev.ejb.ejb32.mdb.ejb;

import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author David Blevins
 */
@Singleton
@LocalBean
@Lock(LockType.READ)
public class ResultsBean implements ResultsRemote {

    private final List<String> invoked = new ArrayList<String>();
    private final CountDownLatch expected = new CountDownLatch(3);

    public void addInvoked(String name) {
        invoked.add(name);
        expected.countDown();
    }

    public boolean awaitInvocations() {
        try {
            return expected.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.interrupted();
            return false;
        }
    }

    public List<String> getInvoked() {
        return invoked;
    }
}
