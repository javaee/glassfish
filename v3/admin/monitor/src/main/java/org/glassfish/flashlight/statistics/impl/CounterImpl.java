/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.*;
import java.util.concurrent.atomic.AtomicLong;
import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;

/**
 * @author Harpreet Singh
 */
public class CounterImpl extends AbstractTreeNode implements Counter {

    private AtomicLong count = new AtomicLong (0);
    long max = 0;
    long min = 0;
    private static final String NAME = "counter";

    public CounterImpl (){
        super.setName(NAME);
        super.setEnabled (true);
    }
    public long getCount() {
        return count.get();
    }

    public void setCount(long count) {
        if (count > max)
            max = count;
        else 
            min = count;
        
        this.count.set(count);
    }
  
    public void increment (){
       long cnt = this.count.incrementAndGet();
       if (cnt > max)
           max = cnt;
    }

    public void decrement (){
        long cnt = this.count.decrementAndGet();
        if (cnt < min)
            min = cnt;
    }
    public void setReset (boolean reset){
        if (reset){
            this.count.set(0);
        }
    }
    
    @Override
    public Object getValue (){
        return getCount ();
    }
    /*
     * Methods to return a JavaEE Statistics 
     */
 
}
