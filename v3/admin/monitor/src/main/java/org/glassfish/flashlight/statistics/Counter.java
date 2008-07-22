/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

/**
 * TBD Implement Java EE Statistics
 * @author Harpreet Singh
 */
public interface Counter {

    public void decrement();

    public long getCount();

    public void increment();

    public void setCount(long count);

    public void setReset(boolean reset);
    
}
