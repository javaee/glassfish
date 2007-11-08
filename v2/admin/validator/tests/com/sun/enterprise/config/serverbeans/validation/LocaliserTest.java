/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.enterprise.config.serverbeans.validation;

import junit.framework.*;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import com.sun.enterprise.util.LocalStringManager;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Handler;
import java.io.Writer;
import java.io.IOException;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class LocaliserTest extends TestCase {
    public void testLocation() throws Exception {
        final String input = "<top xmlns:m='messages'><m:location>location</m:location><m:messages><m:message id='mymessage'><m:param num='0'>Arg 1</m:param><m:param num='1'>Arg 2</m:param></m:message></m:messages></top>";
        
        final InputSource is = getInputSource(input);
        final String prefix = DomainXmlVerifier.class.getName();
        final LocalStringManager lsm = getLocalStringManager(prefix+".mymessage", "this is {0} and this is {1}");
        final StringWriter output = new StringWriter();
        final Localiser uut = new Localiser(lsm, output, prefix);
        getParser().parse(is, uut);
        assertEquals("location this is Arg 1 and this is Arg 2\n", output.toString());
    }

    
    public void testBasicOperationWithPrefix() throws Exception {
        final String input = "<m:messages xmlns:m='messages'><m:message id='mymessage'><m:param num='0'>Arg 1</m:param><m:param num='1'>Arg 2</m:param></m:message></m:messages>";
        
        final InputSource is = getInputSource(input);
        final String prefix = DomainXmlVerifier.class.getName();
        final LocalStringManager lsm = getLocalStringManager(prefix+".mymessage", "this is {0} and this is {1}");
        final StringWriter output = new StringWriter();
        final Localiser uut = new Localiser(lsm, output, prefix);
        getParser().parse(is, uut);
        assertEquals("this is Arg 1 and this is Arg 2\n", output.toString());
    }

    public void testLoggingOperation() throws Exception {
        final String input = "<m:messages xmlns:m='messages'><m:message id='mymessage'><m:param num='0'>Arg 1</m:param><m:param num='1'>Arg 2</m:param></m:message></m:messages>";
        
        InputSource is = getInputSource(input);
        LocalStringManager lsm = getLocalStringManager("unknown", "this is {0} and this is {1}");
        StringWriter output = new StringWriter();
        StringWriter log = new StringWriter();
        Logger logger = getLogger(log);
        Localiser uut = new Localiser(lsm, output, logger);
        getParser().parse(is, uut);
        assertEquals("SEVERE Internal Error, message id  \"mymessage\" not present in localisation file", log.toString());
        assertEquals("", output.toString());
}
        
    public void testErrorOperation() throws Exception {
        final String input = "<m:messages xmlns:m='messages'><m:message id='mymessage'><m:param num='0'>Arg 1</m:param><m:param num='1'>Arg 2</m:param></m:message></m:messages>";
        
        InputSource is = getInputSource(input);
        LocalStringManager lsm = getLocalStringManager("unknown", "this is {0} and this is {1}");
        StringWriter output = new StringWriter();
        Localiser uut = new Localiser(lsm, output);
        getParser().parse(is, uut);
        assertEquals("Internal Error, message id  \"mymessage\" not present in localisation file\n", output.toString());
}
    
        
    public void testBasicOperation() throws Exception {
        final String input = "<m:messages xmlns:m='messages'><m:message id='mymessage'><m:param num='0'>Arg 1</m:param><m:param num='1'>Arg 2</m:param></m:message></m:messages>";
        
        InputSource is = getInputSource(input);
        LocalStringManager lsm = getLocalStringManager("mymessage", "this is {0} and this is {1}");
        StringWriter output = new StringWriter();
        Localiser uut = new Localiser(lsm, output);
        getParser().parse(is, uut);
        assertEquals("this is Arg 1 and this is Arg 2\n", output.toString());
    }

    private Logger getLogger(final Writer log){
        final Logger l = Logger.getAnonymousLogger();
        l.setUseParentHandlers(false);
        l.addHandler(getHandler(log));
        return l;
    }

    private Handler getHandler(final Writer log){
        return new Handler(){
                final Writer l = log;
                public void publish(LogRecord record){
                    try {
                        l.write(record.getLevel() +" "+ record.getMessage());
                        l.flush();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    
                }
                public void flush(){
                }
                public void close() throws SecurityException {
                }
            };
    }
    
                
    private SAXParser getParser() throws Exception {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        assertTrue("should be namespace aware", spf.isNamespaceAware());
        return spf.newSAXParser();
    }
    
    private InputSource getInputSource(final String input) throws Exception {
        return new InputSource(new StringReader(input));
    }

    private LocalStringManager getLocalStringManager(final String key, final String message){
        return new LocalStringManager(){
                final String k = key;
                final String m = message;
                public String getLocalString(final Class c, final String key, final String def){
                    return (this.k.equals(key) ? this.m : def);
                }
                public String getLocalString(final Class c, final String key, final String def, final Object [] args){
                    final String format = (this.k.equals(key) ? this.m : def);
                    final String msg = MessageFormat.format(format, args);
                    return msg;
                }
            };
    }


    public LocaliserTest(String name){
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(LocaliserTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new LocaliserTest(args[i]));
        }
        return ts;
    }
}
