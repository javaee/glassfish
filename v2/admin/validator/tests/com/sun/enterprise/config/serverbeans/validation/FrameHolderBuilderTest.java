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

import java.io.StringReader;
import junit.framework.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.3 $
 */

public class FrameHolderBuilderTest extends TestCase {

      // In this test we'll have 1 domain, 2 configs (config1,
      // config2), 1 cluster and three servers (s1, s2, s3). The
      // inheritance paths from the tip to the root will be:
      // sf1 config1 domain
      // sf2 cluster config1 domain
      // sf3 config2 domain
      // Each entity will have a single key/value pair, being its own name.
    public void testComplexInheritanceHierarchy() throws Exception {
        final String domain = "domain";
        final String config1 = "config1";
        final String config2 = "config2";
        final String cluster = "cluster";
        final String s1 = "s1";
        final String s2 = "s2";
        final String s3 = "s3";

        FrameHolder expected = new FrameHolder();
          // First, the definitions
        final Frame df = expected.getDomainFrame().put(domain, domain);
        final Frame cf1 = expected.getConfigFrame(config1).put(config1, config1);
        final Frame cf2 = expected.getConfigFrame(config2).put(config2, config2);
        final Frame cl = expected.getClusterFrame(cluster).put(cluster, cluster);
        final Frame sf1 = expected.getServerFrame(s1).put(s1, s1);
        final Frame sf2 = expected.getServerFrame(s2).put(s2, s2);
        final Frame sf3 = expected.getServerFrame(s3).put(s3, s3);
          // Then the connections, in rank order
          //Rank 1
        cf1.inheritFrom(df);
        cf2.inheritFrom(df);
          //Rank 2
        sf1.inheritFrom(cf1);
        cl.inheritFrom(cf1);
        sf3.inheritFrom(cf2);
          //Rank 3
        sf2.inheritFrom(cl);

        final String input = "" +
        "<doc>"+
        "  <config name='config1'>" +
        "    <system-property name='config1' value='config1'/>" +
        "  </config>" +
        "  <config name='config2'>" +
        "    <system-property name='config2' value='config2'/>" +
        "  </config>" +
        "  <server name='s1' config-ref='config1'>" +
        "    <system-property name='s1' value='s1'/>"+
        "  </server>" +
        "  <server name='s2'>" +
        "    <system-property name='s2' value='s2'/>" +
        "  </server>" +
        "  <server name='s3' config-ref='config2'>" +
        "    <system-property name='s3' value='s3'/>" +
        "  </server>" +
        "  <cluster name='cluster' config-ref='config1'>" +
        "    <server-ref name='s2'/>" +
        "    <system-property name='cluster' value='cluster'/>" +
        "  </cluster>" +
        "  <system-property name='domain' value='domain'/>" +
        "</doc>"
        ;

        XMLReader r = XMLReaderFactory.newInstance(null);
        r.setContentHandler(fb);
        r.parse(new InputSource(new StringReader(input)));
        
        assertEquals(df, fb.getFrameHolder().getDomainFrame());
        assertEquals(cf1, fb.getFrameHolder().getConfigFrame(config1));
        assertEquals(cf2, fb.getFrameHolder().getConfigFrame(config2));
        assertEquals(sf1, fb.getFrameHolder().getServerFrame(s1));
        assertEquals(sf2, fb.getFrameHolder().getServerFrame(s2));
        assertEquals(sf3, fb.getFrameHolder().getServerFrame(s3));
        assertEquals(cl, fb.getFrameHolder().getClusterFrame(cluster));
        assertEquals(expected, fb.getFrameHolder());
    }
        
    public void testClusterPropertyEventHandling() throws Exception {
        final String global = "global";
        final String initialValue = "initialValue";
        final String cluster1 = "cluster1";
        final String myName = "myName";
        final String myValue = "myValue";
        final String overridden = "overridden";

        FrameHolder expected = new FrameHolder();
        expected.getDomainFrame().put(global, initialValue);
        expected.getClusterFrame(cluster1).put(myName, myValue);
        expected.getClusterFrame(cluster1).put(global, overridden);
        fb.startDocument();
        fireClusterStart(fb, cluster1, null, null);
        fireSystemPropertyEvents(fb, myName, myValue);
        fireSystemPropertyEvents(fb, global, overridden);
        fb.endElement("", FrameHolderBuilder.CLUSTER, "");
        fireSystemPropertyEvents(fb, global, initialValue);

        assertEquals(myValue, expected.getClusterFrame(cluster1).lookup(myName));
        assertEquals(initialValue, expected.getDomainFrame().lookup(global));
        assertEquals(initialValue, fb.getFrameHolder().getDomainFrame().lookup(global));
        assertEquals(expected.getDomainFrame(), fb.getFrameHolder().getDomainFrame());
        assertEquals(expected.getClusterFrame(cluster1), fb.getFrameHolder().getClusterFrame(cluster1));
        assertEquals(expected, fb.getFrameHolder());

    }
        
    public void testServerInheritingFromConfig() throws Exception {
        final String k1 = "k1";
        final String v1 = "v1";
        final String k2 = "k2";
        final String v2 = "v2";
        final String k3 = "k3";
        final String v3 = "v3";
        final String k4 = "k4";
        final String v4 = "v4";
        final String config = "config";
        final String config1 = "config1";
        final String server = "server";
        final String server1 = "server1";

        final String input = "" +
        "<doc>" +
        "  <config name='config'>"+
        "    <system-property name='k2' value='v2'/>"+
        "  </config>"+
        "  <server name='server' config-ref='config'>"+
        "    <system-property name='k3 value='k3'/>"+
        "  </server>"+
        "  <system-property name='k1' value='v1'/>"+
        "  <config name='config1'/>"+
        "</doc>"
;
        
        
        FrameHolder expected = new FrameHolder();
        expected.getDomainFrame().put(k1, v1);
        expected.getConfigFrame(config).put(k2, v2);
        expected.getConfigFrame(config).inheritFrom(expected.getDomainFrame());
        expected.getConfigFrame(config1).inheritFrom(expected.getDomainFrame());
        expected.getServerFrame(server).put(k3, v3);
        expected.getServerFrame(server).inheritFrom(expected.getConfigFrame(config));
        expected.getServerFrame(server1).put(k4, v4);
        expected.getServerFrame(server1).inheritFrom(expected.getConfigFrame(config));

        fb.startDocument();
        fireConfigStart(fb, config);
        fireSystemPropertyEvents(fb, k2, v2);
        fb.endElement("", FrameHolderBuilder.CONFIG, "");
        fireServerStart(fb, server, config);
        fireSystemPropertyEvents(fb, k3, v3);
        fb.endElement("", FrameHolderBuilder.SERVER, "");
        fireSystemPropertyEvents(fb, k1, v1);
        fireConfigStart(fb, config1);
        fb.endElement("", FrameHolderBuilder.CONFIG, "");
        fireServerStart(fb, server1, config);
        fireSystemPropertyEvents(fb, k4, v4);
        fb.endElement("", FrameHolderBuilder.SERVER, "");
        
        assertEquals(expected.getDomainFrame(), fb.getFrameHolder().getDomainFrame());
        assertEquals(expected.getConfigFrame(config), fb.getFrameHolder().getConfigFrame(config));
        assertEquals(expected.getServerFrame(server), fb.getFrameHolder().getServerFrame(server));
        assertEquals(expected, fb.getFrameHolder());
    }
    
        

        
        
    public void testServerSystemPropertyEventHandling() throws Exception {
        final String global = "global";
        final String initialValue = "initialValue";
        final String server1 = "server1";
        final String myName = "myName";
        final String myValue = "myValue";
        final String overridden = "overridden";
        final String input = ""+
        "<doc>" +
        " <server name='server1'>"+
        "    <system-property name='myName' value='myValue'/>"+
        "    <system-property name='global' value='overridden'/>" +
        " </server>"+
        " <system-property name='global' value='initialValue'/>" +
        "</doc>"
        ;
        
        FrameHolder expected = new FrameHolder();
        expected.getDomainFrame().put(global, initialValue);
        expected.getServerFrame(server1).put(myName, myValue);
        expected.getServerFrame(server1).put(global, overridden);

        XMLReader r = XMLReaderFactory.newInstance(null);
        r.setContentHandler(fb);
        r.parse(new InputSource(new StringReader(input)));
        
        assertEquals(myValue, expected.getServerFrame(server1).lookup(myName));
        assertEquals(initialValue, expected.getDomainFrame().lookup(global));
        assertEquals(initialValue, fb.getFrameHolder().getDomainFrame().lookup(global));
        assertEquals(expected.getDomainFrame(), fb.getFrameHolder().getDomainFrame());
        assertEquals(expected.getServerFrame(server1), fb.getFrameHolder().getServerFrame(server1));
        assertEquals(expected, fb.getFrameHolder());

    }
  
    public void testConfigSystemPropertyEventHandling() throws Exception {
        final String global = "global";
        final String initialValue = "initialValue";
        final String config1 = "config1";
        final String myName = "myName";
        final String myValue = "myValue";
        final String overridden = "overridden";

        final String input = "" +
        "<doc>" +
        " <config name='config1'>"+
        "  <system-property name='myName' value='myValue'/>"+
        "  <system-property name='global' value='overridden'/>" +
        " </config>" +
        " <system-property name='global' value='initialValue'/>"+
        "</doc>"
        ;
        
        FrameHolder expected = new FrameHolder();
        expected.getDomainFrame().put(global, initialValue);
        expected.getConfigFrame(config1).put(myName, myValue);
        expected.getConfigFrame(config1).put(global, overridden);
        expected.getConfigFrame(config1).inheritFrom(expected.getDomainFrame());

        XMLReader r = XMLReaderFactory.newInstance(null);
        r.setContentHandler(fb);
        r.parse(new InputSource(new StringReader(input)));
        
        assertEquals(myValue, expected.getConfigFrame(config1).lookup(myName));
        assertEquals(initialValue, expected.getDomainFrame().lookup(global));
        assertEquals(initialValue, fb.getFrameHolder().getDomainFrame().lookup(global));
        assertEquals(expected.getDomainFrame(), fb.getFrameHolder().getDomainFrame());
        assertEquals(expected.getConfigFrame(config1), fb.getFrameHolder().getConfigFrame(config1));
        assertEquals(expected, fb.getFrameHolder());
    }

    private void fireClusterStart(FrameHolderBuilder fb, String name, String configRef, String serverRef) throws Exception {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", FrameHolderBuilder.NAME, "", "", name);
        if (configRef != null){
            atts.addAttribute("", FrameHolderBuilder.CONFIG_REF, "", "", configRef);
        }
        if (serverRef != null){
            atts.addAttribute("", FrameHolderBuilder.SERVER_REF, "", "", serverRef);
        }
        fb.startElement("", FrameHolderBuilder.CLUSTER, "", atts);
    }
    private void fireServerStart(FrameHolderBuilder fb, String name, String configRef) throws Exception {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", FrameHolderBuilder.NAME, "", "", name);
        if (configRef != null){
            atts.addAttribute("", FrameHolderBuilder.CONFIG_REF, "", "", configRef);
        }
        
        fb.startElement("", FrameHolderBuilder.SERVER, "", atts);
    }

    private void fireConfigStart(FrameHolderBuilder fb, String name) throws Exception {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", FrameHolderBuilder.NAME, "", "", name);
        fb.startElement("", FrameHolderBuilder.CONFIG, "", atts);
    }

        
    public void testBasicSystemPropertyEventHandling() throws Exception {
        fb.startDocument();
        fireSystemPropertyEvents(fb, "prop1", "val1");
        FrameHolder expected = new FrameHolder();
        expected.getDomainFrame().put("prop1", "val1");
        assertEquals(expected, fb.getFrameHolder());
        fireSystemPropertyEvents(fb, "prop2", "val2");
        assertFalse(expected.equals(fb.getFrameHolder()));
        expected.getDomainFrame().put("prop2", "val2");
        assertEquals(expected, fb.getFrameHolder());
    }

    private void fireSystemPropertyEvents(FrameHolderBuilder fb, String name, String value) throws Exception {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", FrameHolderBuilder.NAME, "", "", name);
        atts.addAttribute("", FrameHolderBuilder.VALUE, "", "", value);
        fb.startElement("", FrameHolderBuilder.SYSTEM_PROPERTY, "", atts);
        fb.endElement("", FrameHolderBuilder.SYSTEM_PROPERTY, "");
    }

        
    public void testStartEndDocumentEventHandling() throws Exception {
        fb.startDocument();
        fb.endDocument();
        assertEquals(new FrameHolder(), fb.getFrameHolder());
    }

    public FrameHolderBuilderTest(String name){
        super(name);
    }

    private FrameHolderBuilder fb;
    
    protected void setUp() {
        fb = new FrameHolderBuilder();
    }

    protected void tearDown() {
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(FrameHolderBuilderTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new FrameHolderBuilderTest(args[i]));
        }
        return ts;
    }
}
