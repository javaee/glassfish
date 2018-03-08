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

import java.util.List ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.ArrayList ;
import java.util.Set ;
import java.util.HashSet ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.ee.spi.orbutil.newtimer.NamedBase ;
import com.sun.corba.ee.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.ee.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Named ;
import com.sun.corba.ee.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.ee.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;

import com.sun.corba.ee.spi.orbutil.newtimer.NamedBase ;

import com.sun.corba.ee.spi.orbutil.generic.Pair ;

import static java.util.Arrays.asList ;

public class ActivationSuite {
    // Test set up:
    //Timer groups: 
    //	ga contains gb, gc
    //	gb contains gd
    //	gc contains gd
    //	gd contains ge
    //	ge contains gc
    //
    //	Timers (in groups)
    //	ga contains t1
    //	gb contains t2, t3
    //  gc contains t4
    //  gd contains t5, t6
    //  ge contains t7, t8
    //
    // All descriptions are the same as the names
    //
    
    private String tfName = "TFActivation" ;

    private List<Pair<String,List<String>>> data = asList(
	mkPair( "ga", "gb", "t1" ),
	mkPair( "gb", "gd", "t2", "t3" ),
	mkPair( "gc", "gd", "t4" ),
	mkPair( "gd", "ge", "t5", "t6" ),
	mkPair( "ge", "gc", "t7", "t8" ) ) ;

    private List<String> t1_t8 = asList(
	"t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8" ) ;
    private List<String> t2_t8 = asList(
	"t2", "t3", "t4", "t5", "t6", "t7", "t8" ) ;
    private List<String> t4_t8 = asList(
	"t4", "t5", "t6", "t7", "t8" ) ;

    // List<Pair<List<String>,List<String>>>
    // for each element of list:
    // first is Controllables to enable, second is expected active set
    //
    private List<Pair<List<String>,List<String>>> testData = asList(
	mkPList( asList( "t1", "t3" ), asList( "t1", "t3" )),
	mkPList( asList( "ga" ), t1_t8 ),
	mkPList( asList( "gb" ), t2_t8 ),
	mkPList( asList( "gc" ), t4_t8 ),
	mkPList( asList( "gd" ), t4_t8 ),
	mkPList( asList( "ge" ), t4_t8 ),
	mkPList( asList( "ge", "t1", "t3" ), 
	    asList( "t1", "t3", "t4", "t5", "t6", "t7", "t8" )),
	mkPList( asList( "gb", "gc" ), t2_t8 )) ;

    private List<String> evIn = asList( 
	"t1<10", "t1>120", 
	"t2<13", "t3<15", "t1<21", "t1>4", "t3>34", "t2>27",
	"t1<12", "t1<23", "t1<31", "t1>24", "t1>8", "t1>91" ) ;

    // Timings:
    // t1 20 25 55 86 189
    // t2 114
    // t3 74
    // 
    private List<String> evOut = asList( 
	"t1<", "t1>", 
	"t3<", "t1<", "t1>", "t3>", 
	"t1<", "t1<", "t1<", "t1>", "t1>", "t1>" ) ;
    private String controllerName = "TestController" ;

    private TimerFactory tf ;
    private List<Timer> timers ;
    private List<TimerGroup> timerGroups ;

    private MyTimerEventHandler h1 ;
    private MyTimerEventHandler h2 ;
    private TimerEventController controller ;
    
    private Pair<String,List<String>> mkPair( String key, String... values ) {
	return new Pair<String,List<String>>(
	    key, asList( values ) ) ;
    }

    private Pair<List<String>,List<String>> mkPList( List<String> first,
	List<String> second ) {
	
	return new Pair<List<String>,List<String>>(
	    first, second ) ;
    }

    private <T> Set<T> asSet( T... args ) {
	Set<T> result = new HashSet<T>() ;
	for (T t : args) 
	    result.add( t ) ;
	return result ;
    }

    private Controllable makeOrGetControllable( String str ) {
	if (str.charAt(0)=='g') {
	    // Make a TimerGroup
	    TimerGroup tg = tf.timerGroups().get( str ) ;
	    if (tg == null) {
		tg = tf.makeTimerGroup( str, str ) ;
		timerGroups.add( tg ) ;
	    }
	    return tg ;
	} else if (str.charAt(0)=='t') {
	    // Make a Timer
	    Timer t = tf.timers().get( str ) ;
	    if (t == null) {
		t = tf.makeTimer( str, str ) ;
		timers.add( t ) ;
	    }
	    return t ;
	} else {
	    // error in test data
	    Assert.fail( "Bad data string" ) ;
	    return null ;
	}
    }

    private Controllable getControllable( String str ) {
	if (str.charAt(0)=='g') {
	    // Get a TimerGroup
	    TimerGroup tg = tf.timerGroups().get( str ) ;
	    Assert.assertTrue( tg != null ) ;
	    Assert.assertEquals( tg.name(), str ) ;
	    return tg ;
	} else if (str.charAt(0)=='t') {
	    // Get a Timer
	    Timer t = tf.timers().get( str ) ;
	    Assert.assertTrue( t != null ) ;
	    Assert.assertEquals( t.name(), str ) ;
	    return t ;
	} else {
	    // error in test data
	    Assert.fail( "Bad data string" ) ;
	    return null ;
	}
    }

    @Configuration( beforeTest = true )
    public void setUp() {

	tf = TimerFactoryBuilder.make( tfName, tfName ) ;
	timers = new ArrayList<Timer>() ;
	timerGroups = new ArrayList<TimerGroup>() ;
	timerGroups.add( tf ) ;

	for (Pair<String,List<String>> elem : data) {
	    String head = elem.first() ;
	    List<String> tail = elem.second() ;
	    Controllable con = makeOrGetControllable( head ) ;
	    Assert.assertTrue( con instanceof TimerGroup ) ;
	    TimerGroup container = TimerGroup.class.cast( con ) ;
	    for (String str : tail) {
		Controllable c2 = makeOrGetControllable( str ) ;
		container.add( c2 ) ;
	    }
	}

	h1 = new MyTimerEventHandler( "h1" ) ;
	h2 = new MyTimerEventHandler( "h2" ) ;
	controller = tf.makeController( controllerName ) ;
    }

    private <T extends Controllable> void checkList( List<? extends T> list,
	Map<String,? extends T> map ) {

	Assert.assertEquals( list.size(), map.size() ) ;
	for (T t : list) {
	    Assert.assertEquals( t, map.get( t.name() ) ) ;
	}
    }


    @Test()
    public void validate() {
	checkList( timers, tf.timers() ) ;
	checkList( timerGroups, tf.timerGroups() ) ;

	for (Pair<String,List<String>> elem : data) {
	    String head = elem.first() ;
	    List<String> tail = elem.second() ;
	    Controllable con = getControllable( head ) ;
	    Assert.assertTrue( con instanceof TimerGroup ) ;
	    TimerGroup container = TimerGroup.class.cast( con ) ;
	    Assert.assertTrue( container.contents().size() == tail.size() ) ;
	    for (String str : tail) {
		Controllable c2 = getControllable( str ) ;
		Assert.assertTrue( container.contents().contains( c2 ) ) ;
	    }
	}
    }

    private Set<Controllable> makeControllableSet( List<String> strs ) {
	Set<Controllable> result = new HashSet<Controllable>() ;
	for (String str : strs) {
	    Controllable c = getControllable( str ) ;
	    result.add( c ) ;
	}

	return result ;
    }
    
    private void enableControllables( Set<Controllable> cons ) {
	for (Controllable c : cons) 
	    c.enable() ;
    }

    private void disableAllControllables() {
	for (Controllable c : tf.contents())
	    c.disable() ;
    }

    @Test()
    public void testTimerEnable() {
	for (Pair<List<String>,List<String>> pair : testData) {
	    disableAllControllables() ;
	    Set<Controllable> tds = makeControllableSet( pair.first() ) ;
	    Set<Controllable> ers = makeControllableSet( pair.second() ) ;
	    enableControllables( tds ) ;
	    Set<Timer> ars = tf.activeSet() ;
	    Assert.assertTrue( ers.equals( ars ) ) ;
	}
	disableAllControllables() ;
    }

    private class MyTimerEventHandler extends NamedBase implements TimerEventHandler {
	List<TimerEvent> events ;

	public MyTimerEventHandler( String name ) {
	    super( tf, name ) ;
	    events = new ArrayList<TimerEvent>() ;
	}

	public void notify( TimerEvent event ) {
	    events.add( event ) ;
	}

	public List<TimerEvent> events() {
	    return events ;
	}
    }

    private Pair<Integer,Pair<Timer,TimerEvent.TimerEventType>> parseEventDescription( 
	String str ) {

	if (str.length() < 3)
	    Assert.fail() ;
	String tname = str.substring( 0, 2 ) ;
	char ch = str.charAt( 2 ) ;
	String timeStr = str.substring( 3 ) ;
	int delay = 0;
	if ((timeStr != null) && (timeStr.length() > 0))
	    delay = Integer.parseInt( timeStr ) ;
	Controllable c = getControllable( tname ) ;
	Assert.assertTrue( c != null && c instanceof Timer ) ;
	Timer t = Timer.class.cast( c ) ;
	TimerEvent.TimerEventType etype = null ;
	if (ch == '<') 
	    etype = TimerEvent.TimerEventType.ENTER ;
	else if (ch == '>')
	    etype = TimerEvent.TimerEventType.EXIT ;
	else 
	    Assert.fail() ;

	Pair<Timer,TimerEvent.TimerEventType> res1 = 
	    new Pair<Timer,TimerEvent.TimerEventType>( t, etype ) ;
	Pair<Integer,Pair<Timer,TimerEvent.TimerEventType>> result = 
	    new Pair<Integer,Pair<Timer,TimerEvent.TimerEventType>>( delay, res1 ) ;

	return result ;
    }

    private void sleep( int time ) {
	if (time == 0)
	    return ;

	try {
	    Thread.sleep( time ) ;
	} catch (Exception exc) {
	    // ignore this
	}
    }

    private void generateEvents( TimerEventController tec, List<String> ed ) {
	for (String str : ed ) {
	    Pair<Integer,Pair<Timer,TimerEvent.TimerEventType>> p = 
		parseEventDescription( str ) ;
	    int delay = p.first() ;
	    if (p.second().second() == TimerEvent.TimerEventType.ENTER) {
		tec.enter( p.second().first() ) ;
		sleep( delay ) ;
	    } else {
		sleep( delay ) ;
		tec.exit( p.second().first() ) ;
	    }
	}
    }

    // Test for correct event sequence with non-decreasing time stamps
    private void validateEvents( List<TimerEvent> elist, List<String> ed ) {
	Iterator<TimerEvent> teiter = elist.iterator() ;
	Iterator<String> editer = ed.iterator() ;
	long time = -1 ;
	while (teiter.hasNext() && editer.hasNext()) {
	    TimerEvent te = teiter.next() ;
	    String str = editer.next() ;
	    Pair<Integer,Pair<Timer,TimerEvent.TimerEventType>> p = parseEventDescription( str ) ;
	    Assert.assertEquals( te.timer(), p.second().first() ) ;
	    Assert.assertEquals( te.type(), p.second().second() ) ;
	    Assert.assertTrue( te.time() >= time ) ;
	    time = te.time() ;
	}
	
	Assert.assertEquals( teiter.hasNext(), editer.hasNext() ) ;
    }


    private void displayStats( String msg, Statistics stats ) {
	System.out.println( msg ) ;
	System.out.println( "\tcount              = " + stats.count() ) ;
	System.out.println( "\tmin                = " + stats.min() ) ;
	System.out.println( "\tmax                = " + stats.max() ) ;
	System.out.println( "\taverage            = " + stats.average() ) ;
	System.out.println( "\tstandard deviation = " + stats.standardDeviation() ) ;
    }

    private void displayStatsMap( Map<Timer,Statistics> smap ) {
	for (Timer t : smap.keySet()) {
	    displayStats( "Statistics for Timer " + t, smap.get(t) ) ;
	}
    }

    @Test()
    public void testStatsHandler() {
	List<String> elist = asList( "t1", "t2", "t3" ) ;
	Set<Controllable> cset = makeControllableSet( elist ) ;
	String shName = "Stats1" ;
	StatsEventHandler handler = tf.makeStatsEventHandler( shName ) ;
	controller.register( handler ) ;
	enableControllables( cset ) ;
	generateEvents( controller, evIn ) ;
	Map<Timer,Statistics> smap = handler.stats() ;
	displayStatsMap( smap ) ;

	handler.clear() ;
	smap = handler.stats() ;
	for (Timer timer : smap.keySet()) {
	    Statistics stats = smap.get( timer ) ;
	    Assert.assertTrue( stats.count() == 0 ) ;
	}
    }

    @Test()
    public void testTimerController() {
	List<String> elist = asList( "t1", "t3" ) ;
	Set<Controllable> cset = makeControllableSet( elist ) ;
	enableControllables( cset ) ;

	// Test for correct name and factory of controller
	Assert.assertEquals( controller.name(), controllerName ) ;
	Assert.assertEquals( controller.factory(), tf ) ;

	// Test registration of event handlers
	controller.register( h1 ) ;
	controller.register( h2 ) ;
	Set<TimerEventHandler> etecSet = asSet( (TimerEventHandler)h1, 
	    (TimerEventHandler)h2 ) ;
	Set<TimerEventHandler> atecSet = controller.handlers() ;
	Assert.assertEquals( etecSet, atecSet ) ;

	// Test event handler deregistration
	controller.deregister( h2 ) ;
	etecSet = asSet( (TimerEventHandler)h1 ) ;
	atecSet = controller.handlers() ;
	Assert.assertEquals( etecSet, atecSet ) ;

	// Test for correct generation of event sequence with
	// activated timers.
	controller.register( h2 ) ;
	generateEvents( controller, evIn ) ;
	List<TimerEvent> events1 = h1.events() ;
	List<TimerEvent> events2 = h2.events() ;
	Assert.assertEquals( events1, events2 ) ;

	validateEvents( h1.events(), evOut ) ;

	// clean up
	controller.deregister( h1 ) ;
	controller.deregister( h2 ) ;
	disableAllControllables() ;
    }

    @Configuration( afterTest = true ) 
    public void tearDown() {
	TimerFactoryBuilder.destroy( tf ) ;
    }
}
