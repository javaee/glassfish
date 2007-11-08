/*
 * @(#)Client.java	1.5 06/03/23
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package corba.timer ;

import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;

import java.io.PrintWriter ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.ee.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerManager ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;

import com.sun.corba.ee.impl.orbutil.newtimer.VersionedHashSet ;
import com.sun.corba.ee.impl.orbutil.newtimer.TimingPoints ;

import corba.framework.TimerUtils ;

import static java.util.Arrays.asList ;

public class Client {
    // Test TimerFactoryBuilder
    @Test()
    public void factoryBuilderCreate1() {
	String name = "TF1" ;
	String description = "First Test Factory" ;
	TimerFactory tf = TimerFactoryBuilder.make( name, description ) ;
	Assert.assertEquals( tf.name(), name ) ;
	Assert.assertEquals( tf.description(), description ) ;
	TimerFactoryBuilder.destroy( tf ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalArgumentException.class } )
    public void factoryBuilderCreate2() {
	String name = "TF1" ;
	String description = "First Test Factory" ;
	TimerFactory tf = TimerFactoryBuilder.make( name, description ) ;
	try {
	    tf = TimerFactoryBuilder.make( name, description ) ;
	} finally {
	    TimerFactoryBuilder.destroy( tf ) ;
	}
    }

    @Test()
    public void factoryBuilderCreate3() {
	String name = "TF1" ;
	String description = "First Test Factory" ;
	TimerFactory tf = TimerFactoryBuilder.make( name, description ) ;
	TimerFactoryBuilder.destroy( tf ) ;
	tf = TimerFactoryBuilder.make( name, description ) ;
	TimerFactoryBuilder.destroy( tf ) ;
    }

    @Test()
    public void testVersionedHashSet() {
	String[] data = { "red", "orange", "yellow", "green", "blue", "indigo", "violet" } ;
	String toRemove1 = "green" ;
	String toRemove2 = "blue" ;

	VersionedHashSet<String> tset = new VersionedHashSet<String>() ;
	long version = tset.version() ;

	// test add
	for (String str : data)
	    tset.add( str ) ;
	Assert.assertTrue( version != tset.version() ) ;
	version = tset.version() ;

	// Add that does not change set does not change version
	tset.add( toRemove1 ) ;
	Assert.assertEquals( version, tset.version() ) ;
	
	// test direct remove
	tset.remove( toRemove1 ) ;	
	Assert.assertTrue( version != tset.version() ) ;
	version = tset.version() ;

	// Remove that does not change set does not change version
	tset.remove( toRemove1 ) ;	
	Assert.assertEquals( version, tset.version() ) ;

	// test iterator without remove
	String rainbow = "" ;
	for (String str : tset)
	    rainbow += " " + str ;

	Assert.assertEquals( version, tset.version() ) ;

	// test iterator with remove
	Iterator<String> iter = tset.iterator() ;
	while (iter.hasNext()) {
	    String str = iter.next() ;
	    if (str.equals( toRemove2 )) 
		iter.remove() ;
	}

	Assert.assertTrue( version != tset.version() ) ;
    }

    private static void sleep( int time ) {
	try {
	    Thread.sleep( time ) ;
	} catch (Exception exc) {
	    // ignore it 
	}
    }

    private void recordCall( TimingPoints tp, Timer top, 
	TimerEventController controller, int transportDelay ) {

	controller.enter( top ) ;

	tp.enter_hasNextNext() ;
	sleep( 1 ) ;
	tp.exit_hasNextNext() ;

	tp.enter_connectionSetup() ;
	sleep( 4 ) ;
	tp.exit_connectionSetup() ;

	tp.enter_clientEncoding() ;
	sleep( 100 ) ;
	tp.exit_clientEncoding() ;

	tp.enter_clientTransportAndWait() ;
	sleep( transportDelay ) ;
	tp.exit_clientTransportAndWait() ;

	tp.enter_clientDecoding() ;
	sleep( 40 ) ;
	tp.exit_clientDecoding() ;

	controller.exit( top ) ;
    }

    Map<Timer,Statistics> makeData() {
	// Setup timing points and a top-level timer
	Properties props = new Properties() ;
	props.setProperty( "org.omg.CORBA.ORBClass", 
	    "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
	ORB orb = (ORB)ORB.init( new String[0], props ) ;

	try {
	    TimerManager<TimingPoints> tm = orb.getTimerManager() ;
	    TimingPoints tp = tm.points() ;
	    TimerFactory tf = tm.factory() ;
	    TimerEventController controller = tm.controller() ;
	    StatsEventHandler handler = tf.makeStatsEventHandler( "TestStats" ) ;
	    controller.register( handler ) ;
	    Timer top = tf.makeTimer( "top", "Encloses the entire operation" ) ;
	    top.enable() ;
	    tp.transport.enable() ;
	    handler.clear() ;

	    // Simulate the actions of the ORB client transport 
	    recordCall( tp, top, controller, 25 ) ;
	    recordCall( tp, top, controller, 31 ) ;
	    recordCall( tp, top, controller, 27 ) ;
	    recordCall( tp, top, controller, 42 ) ;
	    recordCall( tp, top, controller, 19 ) ;
	    recordCall( tp, top, controller, 21 ) ;
	    recordCall( tp, top, controller, 23 ) ;
	    recordCall( tp, top, controller, 25 ) ;
	    recordCall( tp, top, controller, 34 ) ;
	    recordCall( tp, top, controller, 33 ) ;
	    recordCall( tp, top, controller, 31 ) ;
	    recordCall( tp, top, controller, 28 ) ;
	    recordCall( tp, top, controller, 27 ) ;
	    recordCall( tp, top, controller, 29 ) ;
	    recordCall( tp, top, controller, 30 ) ;
	    recordCall( tp, top, controller, 31 ) ;
	    recordCall( tp, top, controller, 28 ) ;

	    return handler.stats() ;
	} finally {
	    orb.destroy() ;
	}
    }

    @Test()
    void generateStatsTable() {
	Map<Timer, Statistics> data = makeData() ;

	TimerUtils.writeHtmlTable( data, "stats.html", 
	    "Client Test Timing Data" ) ;
    }

    public static void main( String[] args ) {
	TestNG tng = new TestNG() ;

	Class<?> cls = null ;

	try {
	    cls = Class.forName( "corba.timer.TimerPointsSuite" ) ;
	} catch (Exception exc) {
	    System.out.println( "Caught exception: " + exc ) ;
	    exc.printStackTrace() ;
	}

	List<Class<?>> classes = new ArrayList<Class<?>>( asList( 
	    Client.class, 
	    NamedBaseSuite.class, 
	    ControllableBaseSuite.class,
	    TimerFactorySuite.class,
	    ActivationSuite.class ) ) ;

	if (cls != null)
	    classes.add( cls ) ;

	Class[] tngClasses = classes.toArray( new Class<?>[ classes.size() ] ) ;

	tng.setTestClasses( tngClasses ) ;

	tng.run() ;
        System.out.println("tng.hasFailure="+tng.hasFailure());
	System.exit( tng.hasFailure() ? 1 : 0 ) ;

    }
}
