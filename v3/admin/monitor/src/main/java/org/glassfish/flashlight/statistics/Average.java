
package org.glassfish.flashlight.statistics;

import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Contract;
import org.glassfish.j2ee.statistics.RangeStatistic;
/**
 * @author Harpreet SIngh
 */

@Contract
public interface Average extends RangeStatistic{

    public void addDataPoint(long value);

    public double getAverage();

    public long getSize();

    public void setReset ();
    
    public long getMin ();
    
    public long getMax ();

    public long getTotal ();
}
