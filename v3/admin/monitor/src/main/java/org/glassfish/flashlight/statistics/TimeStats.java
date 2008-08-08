/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

import javax.management.j2ee.statistics.TimeStatistic;
import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Harpreet Singh
 */

public interface TimeStats extends TreeNode , TimeStatistic {
    
    public double getTime ();
    public void setTime (long time);
    public void entry ();
    public void exit ();
    public long getMinimumTime ();
    public long getMaximumTime ();
    
    public long getTimesCalled ();
    public void setReset (boolean reset);
    
}
