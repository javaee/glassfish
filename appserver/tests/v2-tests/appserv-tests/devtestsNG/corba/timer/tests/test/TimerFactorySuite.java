/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package corba.timer ;

import java.util.Map ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.ee.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.ee.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Named ;
import com.sun.corba.ee.spi.orbutil.newtimer.NamedBase ;
import com.sun.corba.ee.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.ee.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;

public class TimerFactorySuite {
    private String tfName = "TFTF" ;
    private String tfDescription = "The TimerFactorySuite TimerFactory" ;
    private TimerFactory tf ;

    private String timer1Name = "t1" ;
    private String timer1Description = "Timer one" ;
    private Timer t1 ;

    private String timer2Name = "t2" ;
    private String timer2Description = "Timer two" ;
    private Timer t2 ;

    private String timerGroup1Name = "tg1" ;
    private String timerGroup1Description = "TimerGroup one" ;
    private TimerGroup tg1 ;

    private static final int EXPECTED_NUM_TIMERS = 2 ;
    
    // Remember, the factory is also a TimerGroup!
    private static final int EXPECTED_NUM_TIMER_GROUPS = 2 ;

    private static final int EXPECTED_NUM_IDS = 
	EXPECTED_NUM_TIMERS + EXPECTED_NUM_TIMER_GROUPS ;


    @Configuration( beforeTest = true )
    public void setUp() {
	tf = TimerFactoryBuilder.make( tfName, tfDescription ) ;
	t1 = tf.makeTimer( timer1Name, timer1Description ) ;
	t2 = tf.makeTimer( timer2Name, timer2Description ) ;
	tg1 = tf.makeTimerGroup( timerGroup1Name, timerGroup1Description ) ;
    }

    @Configuration( afterTest = true ) 
    public void tearDown() {
	TimerFactoryBuilder.destroy( tf ) ;
    }

    @Test()
    public void testNumberOfIds() {
	Assert.assertEquals( EXPECTED_NUM_IDS, tf.numberOfIds() ) ;
    }

    @Test()
    public void testGetControllable1() {
	int id = t1.id() ;
	Controllable con = tf.getControllable( id ) ;
	Assert.assertTrue( con instanceof Timer ) ;
	Timer timer = Timer.class.cast( con ) ;
	Assert.assertEquals( t1, timer ) ;
    }

    @Test()
    public void testGetControllable2() {
	int id = t2.id() ;
	Controllable con = tf.getControllable( id ) ;
	Assert.assertTrue( con instanceof Timer ) ;
	Timer timer = Timer.class.cast( con ) ;
	Assert.assertEquals( t2, timer ) ;
    }

    @Test()
    public void testGetControllable3() {
	int id = tg1.id() ;
	Controllable con = tf.getControllable( id ) ;
	Assert.assertTrue( con instanceof TimerGroup ) ;
	TimerGroup tg = TimerGroup.class.cast( con ) ;
	Assert.assertEquals( tg1, tg ) ;
    }

    @Test()
    public void testMakeLogEventHandler() {
	String name = "LogEventHandler1" ;
	TimerEventHandler h1 = tf.makeLogEventHandler( name ) ;
	Assert.assertEquals( h1.name(), name ) ;
	try {
	    tf.makeLogEventHandler( name ) ;
	    Assert.fail( "Should throw an exception" ) ;
	} catch (IllegalArgumentException exc) {
	    // this is correct
	} catch (Throwable thr) {
	    Assert.fail( "Unexpected exception " + thr ) ;
	}
	try {
	    tf.makeStatsEventHandler( name ) ;
	    Assert.fail( "Should throw an exception" ) ;
	} catch (IllegalArgumentException exc) {
	    // this is correct
	} catch (Throwable thr) {
	    Assert.fail( "Unexpected exception " + thr ) ;
	}
	tf.removeTimerEventHandler( h1 ) ;
	h1 = tf.makeLogEventHandler( name ) ;
	tf.removeTimerEventHandler( h1 ) ;
	h1 = tf.makeStatsEventHandler( name ) ;
    }

    @Test()
    public void testTimers() {
	Map<String,? extends Timer> tmap = tf.timers() ;
	Assert.assertEquals( tmap.size(), EXPECTED_NUM_TIMERS ) ;
	Timer x1 = tmap.get( timer1Name ) ;
	Assert.assertEquals( x1, t1 ) ;
	Timer x2 = tmap.get( timer2Name ) ;
	Assert.assertEquals( x2, t2 ) ;
    }

    @Test()
    public void testTimerGroups() {
	Map<String,? extends TimerGroup> tmap = tf.timerGroups() ;
	Assert.assertEquals( tmap.size(), EXPECTED_NUM_TIMER_GROUPS ) ;
	TimerGroup x1 = tmap.get( timerGroup1Name ) ;
	Assert.assertEquals( x1, tg1 ) ;
	TimerGroup x2 = tmap.get( tfName ) ;
	Assert.assertEquals( x2, tf ) ;
    }

    @Test()
    public void testMakeController() {
	String name = "Controller1" ;
	TimerEventController controller = tf.makeController( name ) ;
	Assert.assertEquals( controller.name(), name ) ;
	try {
	    tf.makeController( name ) ;
	    Assert.fail( "Should throw an exception" ) ;
	} catch (IllegalArgumentException exc) {
	    // this is correct
	} catch (Throwable thr) {
	    Assert.fail( "Unexpected exception " + thr ) ;
	}
	tf.removeController( controller ) ;
	controller = tf.makeController( name ) ;
    }

    // enabledSet and activeSet are tested in the ActivationSuite
}
