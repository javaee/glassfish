/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Harpreet Singh
 */

public interface TimeStats extends TreeNode {
    
    public long getTime ();
    public void entry ();
    public void exit ();
    public long getMinimumTime ();
    public long getMaximumTime ();
    
    public long getTimesCalled ();
    /* TBD
    public float getRunningAverage ();
    public void setRunningAverageBucketSize (int bucketSize);
    public int getRunningAverageBucketSize ();
    */
    // for testing purposes only
    public void setReset (boolean reset);
    
}
