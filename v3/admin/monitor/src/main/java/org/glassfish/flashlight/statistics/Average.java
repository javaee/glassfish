
package org.glassfish.flashlight.statistics;

import org.glassfish.flashlight.datatree.TreeNode;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Harpreet SIngh
 */

@Contract
public interface Average extends TreeNode {

    public void addDataPoint(long value);

    public double getAverage();

    public long getSize();

    public void setReset ();
    
    public long getMin ();
    
    public long getMax ();

    @Override
    public String toString();    
    
}
