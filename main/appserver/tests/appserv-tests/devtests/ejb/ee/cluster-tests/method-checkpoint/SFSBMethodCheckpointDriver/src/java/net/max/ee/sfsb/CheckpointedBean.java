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
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author mk
 */
@Stateful
@LocalBean
public class CheckpointedBean
    implements Serializable {

    private static AtomicInteger ai = new AtomicInteger();

    private String id;

    private long counter = 0;

    public CheckpointedBean() {
        id = "id-" + ai.incrementAndGet();
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void doCheckpoint() {
        //Called for Tx checkpointing
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public String getId() {
        return id;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public long getCounter() {
        return counter;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public long  incrementCounter() {
        return counter++;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public String asString() {
        return id + ": accessCount: " + counter;
    }

    @Remove
    public void cleanup() {
    }
}
