/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.ejb.embedded;

import com.sun.common.util.logging.LoggingConfigImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Attribute;
import javax.xml.namespace.QName;

import com.sun.enterprise.util.i18n.StringManager;

/**
 */
public class DomainXmlTransformer {

    private File in;
    private File out;
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
    private final XMLOutputFactory xof = XMLOutputFactory.newInstance();

    private Logger _logger = Logger.getAnonymousLogger(
            "com.sun.logging.enterprise.system.container.ejb.LogStrings");

    private static final String NETWORK_LISTENERS = "network-listeners";
    private static final String IIOP_LISTENER = "iiop-listener";
    private static final String PROTOCOLS = "protocols";
    private static final String APPLICATIONS = "applications";
    private static final String JMS_HOST = "jms-host";
    private static final String JMX_CONNECTOR = "jmx-connector";
    private static final String LAZY_INIT_ATTR = "lazy-init";
    private static final String ENABLED = "enabled";
    private static final String FALSE = "false";

    private static final StringManager localStrings = 
        StringManager.getManager(DomainXmlTransformer.class);

    public DomainXmlTransformer(File domainXml) {
        in = domainXml;
    }

    public DomainXmlTransformer(File domainXml, Logger logger) {
        in = domainXml;
        _logger = logger;
    }

    public File transform() {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        XMLEventReader parser = null;
        XMLEventWriter writer = null;
        XMLInputFactory xif =
                (XMLInputFactory.class.getClassLoader() == null) ?
                XMLInputFactory.newInstance() :
                XMLInputFactory.newInstance(XMLInputFactory.class.getName(),
                        XMLInputFactory.class.getClassLoader());
        
        try {
            fis = new FileInputStream(in);
            out = File.createTempFile("domain", "xml");
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("[DomainXmlTransformer] Creating temp domain file: " + out);
            }

            out.deleteOnExit();
            fos = new FileOutputStream(out);
            parser = xif.createXMLEventReader(fis);

            writer = xof.createXMLEventWriter(fos);
            while (parser.hasNext()) {
                XMLEvent event = parser.nextEvent();
                if (event.isStartElement()) {
                    String name = event.asStartElement().getName().getLocalPart();
                    if (name.equals(NETWORK_LISTENERS) 
                            || name.equals(JMS_HOST)
                            || name.equals(JMX_CONNECTOR)
                            || name.equals(PROTOCOLS)
                            || name.equals(IIOP_LISTENER)
                            || name.equals(APPLICATIONS)) {
                        if( name.equals(IIOP_LISTENER) || name.equals(JMS_HOST)) {

                            // Make sure lazy init is not enabled by creating a new start element
                            // based on the original but that never includes the lazy init attribute
                            StartElement newStartEvent = getAdjustedStartEvent(event, LAZY_INIT_ATTR);
                            writer.add(newStartEvent);

                        } else if( name.equals(JMX_CONNECTOR) ) {
                            // Disable this element
                            StartElement newStartEvent = getDisabledStartEvent(event);
                            writer.add(newStartEvent);

                        } else {
                            writer.add(event);
                        }
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.fine("[DomainXmlTransformer] Skipping details of: " + name);
                        }
                        event = getEndEventFor(parser, name);
                    }
                } 
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("[DomainXmlTransformer] Processing: " + event); 
                } 
                writer.add(event);
            }
            writer.flush();
            writer.close();

        } catch (Exception e) {
            _logger.log(Level.SEVERE, "ejb.embedded.tmp_file_create_error", e.getMessage());
            _logger.log(Level.FINE, e.getMessage(), e);
            return null;
        } finally {
            try {
                if (parser != null) {
                    parser.close();
                }
            } catch (Exception e) {}
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {}
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {}
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {}
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("[DomainXmlTransformer] Created temp domain file: " + out);
        }
        return out;
    }

    private XMLEvent getEndEventFor(XMLEventReader parser, String name) 
            throws XMLStreamException, EOFException {
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            if (event.isEndElement()
                    && event.asEndElement().getName().getLocalPart().equals(name)) {
               if (_logger.isLoggable(Level.FINEST)) {
                   _logger.finest("[DomainXmlTransformer] END: " + name);
               }
               return event;
           }
        }

        throw new EOFException(localStrings.getString(
                        "ejb.embedded.no_matching_end_element", name));
    }

    /** Create a new start element based on the original but that does not include 
     * any attributes.
     */
    private StartElement getEmptyStartEvent(XMLEvent event) {
        StartElement oldStartEvent = event.asStartElement();
        return xmlEventFactory.createStartElement(oldStartEvent.getName(), 
                null, oldStartEvent.getNamespaces());
    }

    /** Create a new start element based on the original but that does not include 
     * the specified attribute.
     */
    private StartElement getAdjustedStartEvent(XMLEvent event, String skipValue) {
        Set attributes = new HashSet();

        for(java.util.Iterator i = event.asStartElement().getAttributes(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            if( !a.getName().getLocalPart().equals(skipValue) ) {
                attributes.add(a);
            }
        }

        StartElement oldStartEvent = event.asStartElement();
        return xmlEventFactory.createStartElement(oldStartEvent.getName(), 
                attributes.iterator(), oldStartEvent.getNamespaces());
    }

    /** Create a new start element based on the original but that marks it as disabled
     */
    private StartElement getDisabledStartEvent(XMLEvent event) {
        Set attributes = new HashSet();

        for(java.util.Iterator i = event.asStartElement().getAttributes(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            if( !a.getName().getLocalPart().equals(ENABLED) ) {
                attributes.add(a);
            }
        }

        Attribute newAttribute = xmlEventFactory.createAttribute(ENABLED, FALSE);
        attributes.add(newAttribute);

        StartElement oldStartEvent = event.asStartElement();
        return xmlEventFactory.createStartElement(oldStartEvent.getName(), 
                attributes.iterator(), oldStartEvent.getNamespaces());
    }
}
