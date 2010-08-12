/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.max.ee.sfsb;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Remove;
import javax.ejb.Stateful;

/**
 *
 * @author mk
 */
@Stateful
@LocalBean
public class SimpleSessionBean
    implements Serializable {

    private static AtomicInteger ai = new AtomicInteger();

    private String id;

    private long counter = 0;

    public SimpleSessionBean() {
        id = "id-" + ai.incrementAndGet();
    }
    public String getId() {
        return id;
    }

    public long getCounter() {
        return counter;
    }

    public long  incrementCounter() {
        return counter++;
    }

    public String asString() {
        return id + ": accessCount: " + counter;
    }

    @Remove
    public void cleanup() {
    }
}
