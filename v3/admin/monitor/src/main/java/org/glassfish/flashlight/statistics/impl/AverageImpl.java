
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

/**
 *
 * Minimum Elements in Queue = 10; Queue can grow in bucketSize to new capacity 
 * desinated through setSize
 * XXX To determine max bucketSize of the Q and code in overflow logic
 * XXX Throw statistics specific exception
 * XXX logging...
 * @author Harpreet Singh
 */
public class AverageImpl extends AbstractTreeNode implements Average {

    
    int bucketSize = 100;
    int current = 0;
    boolean flagQFilledOnce = false;
    
    long min = 0;
    long max = 0;
    boolean firstDataPointAdded = false;
    
    private static final String NAME = "average";
    
    // Keep 10,000 in Queue. 
    // XXX to determine what is the right bucketSize
    
    double average = 0D;
    
    AtomicLongArray queue;
    
    
    public AverageImpl (int size){
        this.bucketSize = size;
        queue = new AtomicLongArray (size);    
        super.name = NAME;
        super.instance = this;
        super.enabled = true;
    }
    
    public void setBucketSize (int bucketSize){            
        setReset (bucketSize);
        resizeQueue (bucketSize);
    }
    
    public int getSize (){
        return queue.length();
    }
     
    public int getBucketSize (){
        return this.bucketSize;
    }
    
    public void addDataPoint (long value){
        if (!firstDataPointAdded){
            min = max = value;
            firstDataPointAdded = true;
        }
        if (current == bucketSize){
            current = 0;
        }            
        queue.set (current++, value);
        if (value < min)
            min = value;
        else if (value > max)
            max = value;
    }
    
    public double getRunningAverage (){        
        double sum = 0;
        int length = (flagQFilledOnce)? queue.length(): current;
        
        flagQFilledOnce = (length == queue.length())? true : false;
        
        for (int i=0; i< length; i++){
             sum += queue.get(i);
        }
        this.average = sum / length;
        return this.average;
    }
    
    public void setReset (int size){
        queue = null;
        queue = new AtomicLongArray (size);   
        flagQFilledOnce = false;
    }
    
    private void resizeQueue (int size){
        chopOrExpand (size);
    }
    
    private void chopOrExpand(int size) {
        AtomicLongArray tmp = queue;
        queue = new AtomicLongArray(size);
        if (queue.length() <= tmp.length()){
            // Keep Most Recent in memory
            for (int i=queue.length()-1; i>=0; i--){
                queue.set(i, tmp.get(i));
                flagQFilledOnce = true;
                current = 0;
            }
            
        } else{ // Keep All
        for (int i = 0; i < tmp.length(); i++) {
                queue.set(i, tmp.get(i));
                flagQFilledOnce = false;
                current = i;
            }
        }
    }

    public long getMin() {
        return min;
    }
   
    public long getMax (){
        return max;
    }

    public long getMinInCurrentDataSet() {
        long mini = 0;
        for (int i=0; i<queue.length (); i++){
           if (i==0) 
               mini = queue.get (0);
           else {
                long tmp = queue.get (i);
                if (tmp < mini)
                    mini = tmp;
           }
        }
        return mini;
    }

    public long getMaxInCurrentDataSet() {
        long maxi = 0;
        for (int i=0; i<queue.length (); i++){
           if (i==0) 
               maxi = queue.get (0);
           else {
                long tmp = queue.get (i);
                if (tmp > maxi)
                    maxi = tmp;
           }
        }
        return maxi;

    }
    
    
    public String toString() {
        return queue.toString();
    }


    @Override
    public Object getValue() {
        return getRunningAverage ();
    }
  

}
