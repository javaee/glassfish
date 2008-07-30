/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.flashlight.statistics.impl;

import org.glassfish.flashlight.statistics.TimeStatsMillis;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

/**
 *
 * @author Harpreet Singh
 */
@Service (name="timeStatsMillis")
@Scoped (PerLookup.class)
public class TimeStatsMillisImpl extends TimeStatsAbstractImpl
    implements TimeStatsMillis {
    
    private static final String NAME = "timeStatsMillis";
    
    public TimeStatsMillisImpl (){
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
