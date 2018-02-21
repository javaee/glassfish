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

package corba.framework ;

import java.util.Map ;
import java.util.ArrayList ;

import java.io.File ;
import java.io.PrintStream ;
import java.io.PrintWriter ;

import com.sun.corba.ee.spi.orbutil.newtimer.Timer;
import com.sun.corba.ee.spi.orbutil.newtimer.Statistics;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEvent;
import com.sun.corba.ee.spi.orbutil.newtimer.Controllable;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerManager;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController;
import com.sun.corba.ee.spi.orbutil.newtimer.LogEventHandler;


public class TimerUtils {
    private TimerUtils() {} 

    public static void dumpLogToFile( TimerFactory tf, LogEventHandler log, 
	File file ) {
	
	PrintStream ps = null ;
	try {
	    ps = new PrintStream( file ) ;

	    // Dump the timer and timer group names to the file in numerical order
	    // Time group names are not used, but this will be compatible
	    // with the log event processor.
	    for (int ctr=0; ctr<tf.numberOfIds(); ctr++) {
		Controllable con = tf.getControllable( ctr ) ;
		ps.println( "BEGIN " + con.name() ) ;
		ps.println( "END " + con.name() ) ;
	    }

	    ps.println( "#####" ) ;

	    // Dump the timer info
	    for ( TimerEvent event : log ) {
		int id = event.timer().id() ;
		if (event.type() == TimerEvent.TimerEventType.ENTER)
		    id = 2*id ;
		else // EXIT event
		    id = 2*id + 1 ;
		ps.println( id + " " + event.time() ) ;
	    }
	} catch (Exception exc) {
	    System.out.println( "Error in dumping " + log + " to file " + file
		+ ": " + exc ) ;
	    exc.printStackTrace() ;
	} finally {
	    if (ps != null)
		ps.close() ;
	}
    }

    private static class Connector {
	public String name ;
	public long count ;
	public double min ;
	public double max ;
	public double average ;
	public double standardDeviation ;
    
	public Connector( String name, long count, double min,
	    double max, double average, double standardDeviation ) {

	    this.name = name ;
	    this.count = count ;
	    this.min = min ;
	    this.max = max ;
	    this.average = average ;
	    this.standardDeviation = standardDeviation ;
	}
    }

    private static String concat( String[] strs ) {
	StringBuilder sb = new StringBuilder() ;
	for( String str : strs ) {
	    sb.append( str ) ;
	    sb.append( '\n' ) ;
	}
	return sb.toString() ;
    }

    /** Write the Timer data in the Map to the named file with the given
     * title.
     */
    public static void writeHtmlTable( Map<Timer, Statistics> data,
	String fname, String title ) {

	// open a file and a printWriter here
	PrintWriter pw = null ;
	try {
	    pw = new PrintWriter( fname ) ;
            pw.println( 
       "<html>"+
       "<head>"+
       "  <title>" + title + "</title>"+
       "</head>"+
       "<body>"+
       "<h1>"+ title +"</h1>"+
       "<table border=1 cellpadding=2 cellspacing=2>"+
       "    <tr>"+
       "      <td>name</td>"+
       "      <td>count</td>"+
       "      <td>min</td>"+
       "      <td>max</td>"+
       "      <td>average</td>"+
       "      <td>std deviation</td>"+
       "    </tr>");

	   for (Timer timer : data.keySet() ) {
	       Statistics stats = data.get( timer ) ;
            pw.println( 
	"    <tr>"+
	"      <td>"+timer.name()+"</td>"+
	"      <td>"+stats.count()+"</td>"+
	"      <td>"+stats.min()+"</td>"+
	"      <td>"+stats.max()+"</td>"+
	"      <td>"+stats.average()+"</td>"+
	"      <td>"+stats.standardDeviation()+"</td>"+
	"    </tr>");
	   }
           pw.println(
    	"</table>"+
	"</body>"+
	"</html>");

	} catch (Exception exc) {
	    throw new RuntimeException( exc ) ;
	} finally {
	    if (pw != null)
		pw.close() ;
	}

    }
}

