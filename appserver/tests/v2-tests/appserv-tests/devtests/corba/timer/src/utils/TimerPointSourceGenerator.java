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

package com.sun.corba.ee.impl.orbutil.newtimer ;

import java.io.IOException ;
import java.io.File ;
import java.io.PrintStream ;

import org.xml.sax.SAXException ;
import org.xml.sax.Attributes ;

import org.xml.sax.helpers.DefaultHandler ;

import javax.xml.parsers.SAXParserFactory ;
import javax.xml.parsers.ParserConfigurationException ;
import javax.xml.parsers.SAXParser ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Collections ;
import java.util.Properties ;

import java.io.IOException ;

import com.sun.corba.ee.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.ee.spi.orbutil.newtimer.Timer ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.ee.spi.orbutil.newtimer.TimerFactoryBuilder ;

import com.sun.corba.ee.spi.orbutil.generic.Pair ;

import com.sun.corba.ee.impl.codegen.Identifier ;

import com.sun.corba.ee.spi.codegen.Type ;
import com.sun.corba.ee.spi.codegen.Expression ;
import com.sun.corba.ee.spi.codegen.Signature ;

import static java.lang.reflect.Modifier.* ;

import static com.sun.corba.ee.spi.codegen.Wrapper.* ;

/** Class used to compile an XML description of timer and timergroup
 * information into a Java source file.  Uses the codegen library
 * to generate the source file.
 *
 * Note on bootstrapping: the time point file needs to be generated
 * before the bulk of the ORB is built.  This requires compiling
 * the newtimer and codegen classes into a library file that is 
 * checked into SCCS.
 */
public class TimerPointSourceGenerator {
    private static class TimingInfoProcessor {
	private boolean done = false ;
	private String pkg ;
	private TimerFactory tf ;
	private Map<String,List<String>> contents ;
	private TimerGroup currentTimerGroup ;

	private void checkForValidIdentifier( String name ) {
	    if (!Identifier.isValidIdentifier( name )) 
		throw new IllegalArgumentException(
		    "name " + name + " is not a valid Java identifier" ) ;
	}

	private void checkDone() {
	    if (done)
		throw new IllegalStateException(
		    "past getResult: no other methods may be called" ) ;
	}

	public TimingInfoProcessor( String name, String pkg ) {
	    this.done = false ;
	    this.pkg = pkg ;
	    checkForValidIdentifier( name ) ;
	    if (!Identifier.isValidFullIdentifier( pkg ))
		throw new IllegalArgumentException(
		    pkg + " is not a valid package name" ) ;
	    this.tf = TimerFactoryBuilder.make( name, name ) ;
	    this.contents = new HashMap<String,List<String>>() ;
	    this.currentTimerGroup = null ;
	}

	public void addTimer( String name, String desc ) {
	    checkDone() ;
	    checkForValidIdentifier( name ) ;
	    tf.makeTimer( name, desc ) ;
	    currentTimerGroup = null ;
	}

	public void addTimerGroup( String name, String desc ) {
	    checkDone() ;
	    checkForValidIdentifier( name ) ;
	    currentTimerGroup = tf.makeTimerGroup( name, desc ) ;
	}

	public void contains( String name ) {
	    checkDone() ;
	    if (currentTimerGroup == null) {
		throw new IllegalStateException(
		    "contains must be called after an addTimerGroup call" ) ;
	    } else {
		String cname = currentTimerGroup.name() ;
		List<String> list = contents.get( cname ) ;
		if (list == null) {
		    list = new ArrayList<String>() ;
		    contents.put( cname, list ) ;
		}

		list.add( name ) ;
	    }
	}

	private static Controllable getControllable( TimerFactory tf, String name ) {
	    Controllable result = tf.timers().get( name ) ;
	    if (result == null)
		result = tf.timerGroups().get( name ) ;
	    if (result == null)
		throw new IllegalArgumentException( 
		    name + " is not a valid Timer or TimerGroup name" ) ;
	    return result ;
	}

	private void updateTimerFactoryContents() {
	    //  Use the Map<String,List<String>> to fill in the TimerGroup
	    //  containment relation
	    for (String str : contents.keySet()) {
		List<String> list = contents.get(str) ;
		TimerGroup tg = tf.timerGroups().get( str ) ;
		for (String content : list) {
		    tg.add( getControllable( tf, content ) ) ;
		}
	    }
	}

	public Pair<String,TimerFactory> getResult() {
	    checkDone() ;
	    done = true ;
	    updateTimerFactoryContents() ;
	    Pair<String,TimerFactory> result = 
		new Pair<String,TimerFactory>( pkg, tf ) ;
	    return result ;
	}
    }

    private static class Handler extends DefaultHandler {
	private static final int WIDTH = 4 ;

	// Names of XML elements and attributes
	private static final String TIMER_ELEMENT = "timer" ;
	private static final String TIMING_ELEMENT = "timing" ;
	private static final String TIMER_GROUP_ELEMENT = "timerGroup" ;
	private static final String CONTAINS_ELEMENT = "contains" ;

	private static final String NAME_ATTR = "name" ;
	private static final String DESCRIPTION_ATTR = "description" ;
	private static final String PACKAGE_ATTR = "package" ;

	private boolean debug ;
	private int level ;
	private char[] pad ;
	private TimingInfoProcessor tip ;
	private Pair<String,TimerFactory> result ;

	public Handler( boolean debug ) {
	    this.debug = debug ;
	    this.level = 0 ;
	    setPad() ;
	    this.tip = null ;
	    this.result = null ;
	}

	private void indent() {
	    level++ ;
	    setPad() ;
	}

	private void undent() {
	    level-- ;
	    setPad() ;
	}

	private void setPad() {
	    int length = WIDTH * level ;
	    pad = new char[length] ;
	    for (int ctr=0; ctr<length; ctr++)
		pad[ctr] = ' ' ;
	}

	private void dprint( String msg ) {
	    if (debug) {
		System.out.print( pad ) ;
		System.out.println( msg ) ;
	    }
	}

	public void startDocument() throws SAXException {
	    dprint( "startDocument called" ) ;
	}

	public void endDocument() throws SAXException {
	    dprint( "endDocument called" ) ;
	    result = tip.getResult() ;
	}

	public void startElement( String namespaceURI,
	    String lName, String qName, Attributes attrs ) 
	    throws SAXException {

	    indent() ;

	    // only qName is useful
	    dprint( "namespaceURI=" + namespaceURI ) ;
	    dprint( "lName=" + lName ) ;
	    dprint( "qName=" + qName ) ;
	    dprint( "Attributes:" ) ;

	    // only local name, value are useful
	    for (int ctr=0; ctr<attrs.getLength(); ctr++) {
		dprint( "\tlocal name    =" + attrs.getLocalName(ctr) ) ;
		dprint( "\tqualified name=" + attrs.getQName(ctr) ) ;
		dprint( "\tvalue         =" + attrs.getValue(ctr) ) ;
	    }

	    if (qName.equals( TIMING_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String pkg = attrs.getValue( PACKAGE_ATTR ) ;
		tip = new TimingInfoProcessor( name, pkg ) ;
	    } else if (qName.equals( TIMER_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String desc = attrs.getValue( DESCRIPTION_ATTR ) ;
		tip.addTimer( name, desc ) ;
	    } else if (qName.equals( TIMER_GROUP_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String desc = attrs.getValue( DESCRIPTION_ATTR ) ;
		tip.addTimerGroup( name, desc ) ;
	    } else if (qName.equals( CONTAINS_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		tip.contains( name ) ;
	    } else {
		throw new IllegalStateException(
		    "Unknown XML element: " + qName ) ;
	    }
	}

	public void endElement( String namespaceURI,
	    String lName, String qName ) throws SAXException {

	    undent() ;
	}

	public Pair<String,TimerFactory> getResult() {
	    return result ;
	}
    }

    public static Pair<String,TimerFactory> parseDescription( 
	String fileName ) throws IOException {
	return parseDescription( fileName, false ) ;
    }

    /** Return the package for the file to generate and a TimerFactory
     * that contains all of the information from the XML source file
     * given by fileName.
     */
    public static Pair<String,TimerFactory> parseDescription( 
	String fileName, boolean debug ) throws IOException {

	Handler handler = new Handler( debug ) ;
	SAXParserFactory factory = SAXParserFactory.newInstance() ;
	factory.setValidating( true ) ;
	File file = null ;

	try {
	    SAXParser saxParser = factory.newSAXParser() ;
	    file = new File( fileName ) ;
	    saxParser.parse( file, handler ) ;
	} catch (Exception exc) {
	    System.out.println( "Exception in processing " + file + ": " + exc ) ;
	    exc.printStackTrace() ;
	    return null ;
	}

	return handler.getResult() ;
    }

    /** Generate the source file for the Timers in the TimerFactory from the description.
     * The file is generated in the directory given by the package from the description 
     * starting at the dirName.  The name of the file is NAME.java, where NAME
     * is the TimerFactory name from the description.
     */
    public static void generateSourceFile( String dirName, 
	Pair<String,TimerFactory> description ) throws IOException {

	_clear() ;
	_package( description.first() ) ;
	_import( "java.lang.Object" ) ;
	_import( "java.lang.String" ) ;
	_import( "com.sun.corba.ee.spi.orb.ORB" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.Controllable" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.TimerManager" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.TimerFactory" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.Timer" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.TimerEventController" ) ;
	_import( "com.sun.corba.ee.spi.orbutil.newtimer.TimerGroup" ) ;

	TimerFactory tf = description.second() ;
	_class( PUBLIC, tf.name(), _t("Object") ) ; 

	// no static initializer needed
	
	generateFields( tf ) ;
	generateConstructor( tf ) ;
	generateEnterExitMethods( tf ) ;

	_end() ; // of class generation

	File dir = new File( dirName ) ;
	String pkg = description.first() ;
	String fileDir = pkg.replace( '.', File.separatorChar ) ;
	File fdir = new File( dir, fileDir ) ;
	// mkdirs only returns true if dirs were created, so
	// false may or may not indicate an error.
	fdir.mkdirs() ; 
	File file = new File( fdir, tf.name() + ".java" ) ;
	PrintStream ps = new PrintStream( file ) ; 
	_sourceCode( ps, new Properties() ) ;
    }

    private static void generateFields( TimerFactory tf ) {
	_data( PRIVATE|FINAL, _t("TimerEventController"), "controller" ) ;

	for (Timer t : tf.timers().values()) {
	    Type type = _t("Timer") ;
	    _data( PUBLIC|FINAL, type, t.name() ) ;
	}

	for (TimerGroup tg : tf.timerGroups().values()) {
	    Type type = _t("TimerGroup") ;
	    _data( PUBLIC|FINAL, type, tg.name() ) ;
	}
    }

    private static void generateConstructor( TimerFactory tf ) {
	_constructor( PUBLIC ) ;
	    _arg( _t("TimerFactory"), "tf" ) ;
	    _arg( _t("TimerEventController"), "controller" ) ;
	_body() ;

	// set up orb and tm
	_assign( _field( _this(), "controller" ), _v( "controller" ) ) ;

	// create all timers
	Signature tsig = _s( _t("Timer"), _t("String"), _t("String") ) ;
	for (Timer t : tf.timers().values() ) {
	    _assign( _v( t.name() ), 
		_call( _v("tf"), "makeTimer", tsig,
		    _const(t.name()), _const(t.description()))) ;
	}
    
	// create all timer groups
	Signature tgsig = _s( _t("TimerGroup"), _t("String"), 
	    _t("String") ) ;
	for (TimerGroup tg : tf.timerGroups().values() ) {
	    _assign( _v( tg.name() ), 
		_call( _v("tf"), "makeTimerGroup", tgsig,
		    _const(tg.name()), _const(tg.description()))) ;
	}
	
	// fill in timer group containment
	Signature addSig = _s( _void(), _t("Controllable")) ;
	for (TimerGroup tg : tf.timerGroups().values() ) {
	    for (Controllable c : tg.contents() ) {
		_expr( 
		    _call( _v(tg.name()), "add", addSig,
			_v(c.name()))) ;
	    }
	}

	_end() ;
    }

    private static void generateEnterExitMethods( TimerFactory tf ) {
	// generate enter and exit method for each Timer
	for (Timer t : tf.timers().values() ) {
	    generateMethod( "enter", t.name() ) ;
	    generateMethod( "exit", t.name() ) ;
	}
    }

    // XXX we should add JavaDoc support to codegen, and use it
    // for the enter/exit methods.
    private static void generateMethod( String op, String timer ) {
	_method( PUBLIC, _void(), op+"_"+timer ) ;
	_body() ;
	    Signature sig = _s(_void(), _t("TimerEventController")) ;
	    _expr( _call( _v("controller"), op, sig, _v(timer))) ;
	_end() ;
    }

    public static void main( String[] args ) {
	// arguments: infile outdir debug
	if (args.length != 3) {
	    System.out.println( "Required arguments: input-file output-directory" ) ;
	    System.exit( 1 ) ;
	} else {
	    try {
		String infile = args[0] ;
		String outdir = args[1] ;
		boolean debug = Boolean.parseBoolean( args[2] ) ;
		Pair<String,TimerFactory> result = parseDescription( infile,
		    debug ) ;
		generateSourceFile( outdir, result ) ;
	    } catch (Exception exc) {
		System.out.println( "Failed with exception: " + exc ) ;
		exc.printStackTrace() ;
		System.exit( 1 ) ;
	    }
	}
    }
}
