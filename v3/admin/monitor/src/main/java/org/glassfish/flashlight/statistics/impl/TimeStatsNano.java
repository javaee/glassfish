/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;


/**
 *
 * @author Harpreet Singh
 */
public class TimeStatsNano extends TimeStatsAbstractImpl {
    
    private static final String NAME = "timeStatsNanos";
    
    public TimeStatsNano (){
        super.setName(NAME);
        super.setEnabled(true);
    }
    @Override
    public void entry (){
        entryTime = System.nanoTime();
    }
    
    @Override
    public void exit() {
        exitTime = System.nanoTime();
        super.postExit ();
      }
    @Override 
    public Object getValue (){
        return getTime ();
    }
}
