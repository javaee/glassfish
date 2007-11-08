/*
 * @(#)TimerPointsSuite.java	1.3 06/02/28
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package corba.timer ;

import java.util.Iterator ;

import java.io.IOException ;

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

// source is in optional directory
import com.sun.corba.ee.impl.orbutil.newtimer.TimerPointSourceGenerator ;

import com.sun.corba.ee.spi.orbutil.generic.Pair ;

public class TimerPointsSuite {
    @Test()
    public void testXMLParser() {
	// We should be running this test from the corba/timer directory.
	String fileName = "src/test/timing.xml" ;

	Pair<String,TimerFactory> result = null ;
	try {
	    // Parse the XML file.
	    result = TimerPointSourceGenerator.parseDescription( fileName ) ;

	    // Try to generate the source file
	    TimerPointSourceGenerator.generateSourceFile( "gen", result ) ;
	} catch (IOException ioexc) {
	    ioexc.printStackTrace() ;
	    Assert.fail( "IOException: " + ioexc ) ;
	}
    }
}
