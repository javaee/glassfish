
package org.glassfish.flashlight.statistics;


/**
 * @author Harpreet SIngh
 */

public interface Average {

    public void addDataPoint(long value);

    public int getBucketSize ();

    public double getRunningAverage();

    public int getSize();

    public void setReset(int bucketSize);

    public void setBucketSize(int bucketSize);
    
    public long getMin ();
    
    public long getMax ();

    /**
     * Heavier operation than getMin
     * @return
     */
    public long getMinInCurrentDataSet ();
    /**
     * Heavier operation than getMax
     * @return
     */
    public long getMaxInCurrentDataSet ();
    
    public String toString();    
    
}
