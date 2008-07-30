/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.TimeStatsNanos;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;


/**
 *
 * @author Harpreet Singh
 */
@Service (name="timeStatsNanos")
@Scoped (PerLookup.class)
public class TimeStatsNanosImpl extends TimeStatsAbstractImpl
    implements TimeStatsNanos {
    
    private static final String NAME = "timeStatsNanos";
    
    public TimeStatsNanosImpl (){
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
