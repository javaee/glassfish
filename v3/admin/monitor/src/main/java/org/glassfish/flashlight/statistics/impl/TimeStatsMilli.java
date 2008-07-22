/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

/**
 *
 * @author Harpreet Singh
 */
public class TimeStatsMilli extends TimeStatsAbstractImpl {
    
    private static final String NAME = "timeStatsMillis";
    
    public TimeStatsMilli (){
        super.setName(NAME);
        super.setEnabled(true);
    }
    @Override
    public void entry (){
        entryTime = System.currentTimeMillis();
    }
    
    @Override
    public void exit() {
        exitTime = System.currentTimeMillis();
        super.postExit ();
      }
    @Override 
    public Object getValue (){
        return getTime ();
    }

}
