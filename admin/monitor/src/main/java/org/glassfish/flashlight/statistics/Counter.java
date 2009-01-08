/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.datatree.TreeElement;
import org.jvnet.hk2.annotations.Contract;

/**
 * TBD Implement Java EE Statistics
 * @author Harpreet Singh
 */
@Contract 
public interface Counter extends TreeNode, CountStatistic{

    public void decrement();

    public long getCount();

    public void increment();

    public void setCount(long count);

    public void setReset(boolean reset);
    
}
