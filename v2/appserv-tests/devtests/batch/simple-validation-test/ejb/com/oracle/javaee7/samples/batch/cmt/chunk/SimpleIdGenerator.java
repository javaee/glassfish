/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.javaee7.samples.batch.cmt.chunk;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author makannan
 */
public class SimpleIdGenerator
    implements IdGenerator {
    
    private AtomicInteger counter = new AtomicInteger(0);
    
    public String nextId() {
        return "" + counter.incrementAndGet();
    }
}
