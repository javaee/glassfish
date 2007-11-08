/*
 * @(#)NamedBaseSuite.java	1.2 06/02/06
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package corba.timer ;

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

// Test NamedBase
public class NamedBaseSuite {
    private String name = "MyName" ;
    private TimerFactory factory ;
    private NamedTest nb1 ;
    private NamedTest nb2 ;

    private static class NamedTest extends NamedBase {
	public NamedTest( TimerFactory factory, String name ) {
	    super( factory, name ) ;
	}

	public void finish( TimerFactory factory ) {
	    setFactory( factory ) ;
	}
    }

    @Configuration( beforeTest = true )
    public void setUp() {
	factory = TimerFactoryBuilder.make( "NTF", "No description" ) ;
	nb1 = new NamedTest( factory, name ) ;
	nb2 = new NamedTest( null, name ) ;
    }

    @Configuration( afterTest = true ) 
    public void tearDown() {
	TimerFactoryBuilder.destroy( factory ) ;
    }

    @Test() 
    public void name1() {
	Assert.assertEquals( name, nb1.name() ) ;
    }

    @Test() 
    public void name2() {
	Assert.assertEquals( name, nb2.name() ) ;
    }

    @Test()
    public void factory1() {
	Assert.assertEquals( factory, nb1.factory() ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void factory2() {
	TimerFactory tf = nb2.factory() ;
    }

    @Test()
    public void equals() {
	Assert.assertEquals( nb1, nb2 ) ;
    }

    @Test()
    public void hashCode1() {
	Assert.assertEquals( nb1.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void hashCode2() {
	Assert.assertEquals( nb2.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void toString1() {
	Assert.assertEquals( factory.name() + ":" + 
	    name, nb1.toString() ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void toString2() {
	String ts = nb2.toString() ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void setFactory1() {
	nb1.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "toString2", "factory2" } ) 
    public void setFactory2() {
	nb2.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void factory2Post() {	
	Assert.assertEquals( factory, nb2.factory() ) ;	
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void toString2Post() {	
	Assert.assertEquals( factory.name() + ":" + 
	    name, nb2.toString() ) ;
    }
}
