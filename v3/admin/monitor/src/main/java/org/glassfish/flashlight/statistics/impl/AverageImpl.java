
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

import java.util.Collection;
import org.glassfish.flashlight.datatree.TreeNode;
import org.glassfish.flashlight.statistics.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author Harpreet Singh
 */
@Service (name="average")
@Scoped (PerLookup.class)
public class AverageImpl extends AbstractTreeNode implements Average {

    
    
    long min = -1;
    long max = 0;

    AtomicLong times = new AtomicLong (0);
    
    AtomicLong sum = new AtomicLong(0);
    
    private static final String NAME = "average";
        
    public AverageImpl (){
        super.name = NAME;
        super.instance = this;
        super.enabled = true;
    }
    

    public void addDataPoint (long value){
        if (min == -1) // initial seeding
            min = value;
        
        if (value < min)
            min = value;
        
        else if (value > max)
            max = value;
            
        sum.addAndGet (value);
        times.incrementAndGet ();
    }
    
    public double getAverage (){  
        double total = sum.doubleValue();
        double count = times.doubleValue();
        return total/count;

    }
    
    public void setReset (){
        times.set (0);
        sum.set (0);
           
    }
    
    public long getMin() {
        return min;
    }
   
    public long getMax (){
        return max;
    }    
    
    public String toString() {
        return String.valueOf(getAverage());
    }


    @Override
    public Object getValue() {
        return getAverage ();
    }

    public long getSize() {
        return times.get();
    }
  

}
