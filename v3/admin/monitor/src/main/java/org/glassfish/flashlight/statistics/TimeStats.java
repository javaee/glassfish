/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics;

/**
 * TBD: Implement Average
 * @author Harpreet Singh
 */
public interface TimeStats {
    
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
    public void setTime (long time);
    public void setReset (boolean reset);
    
}
